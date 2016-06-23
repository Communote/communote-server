package com.communote.plugins.api.rest.resource.note;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Request;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import com.communote.common.util.HTMLHelper;
import com.communote.common.util.PageableList;
import com.communote.plugins.api.rest.request.RequestHelper;
import com.communote.plugins.api.rest.resource.attachment.AttachmentResource;
import com.communote.plugins.api.rest.resource.tag.TagResource;
import com.communote.plugins.api.rest.service.IllegalRequestParameterException;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.attachment.AttachmentData;
import com.communote.server.api.core.note.NoteContentType;
import com.communote.server.api.core.note.NoteData;
import com.communote.server.api.core.note.NoteRenderContext;
import com.communote.server.api.core.note.NoteRenderMode;
import com.communote.server.api.core.note.NoteStoringFailDefinition;
import com.communote.server.api.core.note.NoteStoringTO;
import com.communote.server.api.core.tag.TagData;
import com.communote.server.api.core.tag.TagStoreType;
import com.communote.server.api.core.tag.TagTO;
import com.communote.server.api.core.user.DetailedUserData;
import com.communote.server.core.common.session.SessionHandler;
import com.communote.server.core.query.QueryManagement;
import com.communote.server.core.security.SecurityHelper;
import com.communote.server.core.vo.query.QueryDefinitionRepository;
import com.communote.server.core.vo.query.config.QueryParametersParameterNameProvider;
import com.communote.server.core.vo.query.config.TimelineQueryParametersConfigurator;
import com.communote.server.core.vo.query.note.SimpleNoteListItemToNoteDataQueryResultConverter;
import com.communote.server.core.vo.query.post.NoteQuery;
import com.communote.server.core.vo.query.post.NoteQueryParameters;
import com.communote.server.model.i18n.Message;
import com.communote.server.model.note.NoteCreationSource;
import com.communote.server.model.user.Language;

/**
 * Helper for {@link NoteResourceHandler}
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public final class NoteResourceHelper {

    /**
     * Comparator to sort note resources by their creation date in ascending order
     */
    public static class NoteResourceComparator implements Comparator<NoteResource> {
        /**
         * Compares notes via their creation date.
         * 
         * {@inheritDoc}
         */
        @Override
        public int compare(NoteResource o1, NoteResource o2) {
            return o1.getCreationDate().compareTo(o2.getCreationDate());
        }

    }

    private static NoteQuery NOTE_QUERY_DEFINITION = QueryDefinitionRepository.instance()
            .getQueryDefinition(NoteQuery.class);

    /**
     * Build the attachment part of the note
     * 
     * @param note
     *            note of {@link NoteData}
     * @param newNoteResource
     *            {@link NoteResource}
     */
    public static void buildNoteResourceAttachments(NoteData note, NoteResource newNoteResource) {
        /*
         * Get important facts of the note's attachments. Also, a list for the attribute
         * 'attachmentsIds' is created (this is still needed as it is used by Communote Desktop
         * Client (CNDC))
         */
        int arrayPosition;
        Collection<AttachmentData> attachments = note.getAttachments();
        if (attachments != null) {
            AttachmentResource[] attachmentResources = new AttachmentResource[attachments.size()];
            Long[] attachmentIds = new Long[attachments.size()];
            arrayPosition = 0;
            for (AttachmentData attachment : attachments) {
                AttachmentResource attachmentResource = new AttachmentResource();
                attachmentResource.setAttachmentId(attachment.getId());
                attachmentResource.setContentLength(attachment.getSize());
                attachmentResource.setFileName(attachment.getFileName());
                attachmentResource.setFileType(attachment.getMimeTyp());
                attachmentResources[arrayPosition] = attachmentResource;
                attachmentIds[arrayPosition] = attachment.getId();
                arrayPosition++;
            }
            newNoteResource.setAttachments(attachmentResources);
            newNoteResource.setAttachmentIds(attachmentIds);
        } else {
            newNoteResource.setAttachments(new AttachmentResource[0]);
            newNoteResource.setAttachmentIds(new Long[0]);
        }
    }

    /**
     * Build the notification part of the note
     * 
     * @param note
     *            note of {@link NoteData}
     * @param newNoteResource
     *            {@link NoteResource}
     */
    public static void buildNoteResourceNotifications(NoteData note,
            NoteResource newNoteResource) {
        List<DetailedUserData> usersToNotifyList = note.getNotifiedUsers();
        Long currentUserId = SecurityHelper.assertCurrentUser().getUserId();
        if (usersToNotifyList != null) {
            String[] usersToNotify = new String[usersToNotifyList.size()];
            boolean isNoteForMe = false;
            for (int i = 0; i < usersToNotifyList.size(); i++) {
                if (usersToNotifyList.get(i).getId().equals(currentUserId)) {
                    isNoteForMe = true;
                }
                usersToNotify[i] = usersToNotifyList.get(i).getAlias();
            }
            newNoteResource.setIsNoteForMe(isNoteForMe);
            newNoteResource.setUsersToNotify(usersToNotify);
        }
    }

    /**
     * Get note storing transferobject
     * 
     * @param createNoteParameter
     *            parameters to create an note
     * @return {@link NoteStoringTO}
     * @throws IllegalRequestParameterException
     *             text value is wrong
     */
    public static NoteStoringTO buildNoteStoringTO(CreateNoteParameter createNoteParameter)
            throws IllegalRequestParameterException {
        NoteStoringTO noteStoringTO = new NoteStoringTO();
        noteStoringTO.setBlogId(createNoteParameter.getTopicId());
        noteStoringTO.setContent(NoteResourceHelper.checkNoteText(createNoteParameter.getText(),
                createNoteParameter.getIsHtml(), createNoteParameter.getMaxTextLength()));
        noteStoringTO.setIsDirectMessage(createNoteParameter.getIsDirectMessage() == null ? false
                : createNoteParameter.getIsDirectMessage());
        noteStoringTO.setUsersToNotify(getUsersToNotify(createNoteParameter.getUsersToNotify()));
        noteStoringTO.setTags(buildTags(createNoteParameter.getTags()));
        NoteResourceHelper.setToForParameterUnspecificElements(createNoteParameter.getIsHtml(),
                noteStoringTO);
        noteStoringTO.setAutosaveNoteId(createNoteParameter.getAutosaveNoteId());
        noteStoringTO.setPublish(createNoteParameter.getPublish() == null ? true
                : createNoteParameter.getPublish());
        noteStoringTO.setAttachmentIds(createNoteParameter.getAttachmentIds());
        if (createNoteParameter.getNoteVersion() != null) {
            noteStoringTO.setVersion(createNoteParameter.getNoteVersion());
        }
        return noteStoringTO;
    }

    /**
     * Get note storing transferobject
     * 
     * @param editNoteParameter
     *            parameters to edit an note
     * @return {@link NoteStoringTO}
     * @throws IllegalRequestParameterException
     *             text value is wrong
     */
    public static NoteStoringTO buildNoteStoringTO(EditNoteParameter editNoteParameter)
            throws IllegalRequestParameterException {
        NoteStoringTO noteStoringTO = new NoteStoringTO();
        noteStoringTO.setBlogId(editNoteParameter.getTopicId());
        noteStoringTO.setContent(NoteResourceHelper.checkNoteText(editNoteParameter.getText(),
                editNoteParameter.getIsHtml(), editNoteParameter.getMaxTextLength()));
        noteStoringTO.setUsersToNotify(getUsersToNotify(editNoteParameter.getUsersToNotify()));
        noteStoringTO.setTags(buildTags(editNoteParameter.getTags()));
        NoteResourceHelper.setToForParameterUnspecificElements(editNoteParameter.getIsHtml(),
                noteStoringTO);
        noteStoringTO.setAutosaveNoteId(editNoteParameter.getAutosaveNoteId());
        noteStoringTO.setPublish(editNoteParameter.getPublish() == null ? true
                : editNoteParameter.getPublish());
        noteStoringTO.setAttachmentIds(editNoteParameter.getAttachmentIds());
        if (editNoteParameter.getNoteVersion() != null) {
            noteStoringTO.setVersion(editNoteParameter.getNoteVersion());
        }
        return noteStoringTO;
    }

    /**
     * Build TagResources form Collection of {@link TagData}
     * 
     * @param tagListItems
     *            Collection of {@link TagData}
     * @return array of {@link TagResource}
     */
    public static TagResource[] buildTags(Collection<TagData> tagListItems) {
        TagResource[] tagResources = new TagResource[tagListItems.size()];
        int i = 0;
        for (TagData tagListItem : tagListItems) {
            TagResource tagResource = new TagResource();
            tagResource.setTagId(tagListItem.getId());
            tagResource.setDefaultName(tagListItem.getDefaultName());
            tagResource.setTagStoreAlias(tagListItem.getTagStoreAlias());
            tagResource.setTagStoreTagId(tagListItem.getTagStoreTagId());
            tagResource.setName(tagListItem.getName());
            tagResources[i] = tagResource;
            i++;
        }
        return tagResources;
    }

    /**
     * Get a set of tag transfer objects for storing with note
     * 
     * @param tagResources
     *            list of {@link TagResource}
     * @return set of {@link TagTO}
     */
    private static HashSet<TagTO> buildTags(TagResource[] tagResources) {
        HashSet<TagTO> tags = new HashSet<TagTO>();
        if (tagResources != null) {
            for (TagResource tagResource : tagResources) {
                TagTO tagTO = new TagTO(tagResource.getDefaultName(), TagStoreType.Types.NOTE);
                tagTO.setTagStoreAlias(tagResource.getTagStoreAlias());
                tagTO.setTagStoreTagId(tagResource.getTagStoreTagId());
                tagTO.setId(tagResource.getTagId());
                if (StringUtils.isNotBlank(tagResource.getLanguageCode())) {
                    Language language = Language.Factory.newInstance(tagResource.getLanguageCode(),
                            "", "");
                    if (StringUtils.isNotBlank(tagResource.getName())) {
                        tagTO.getNames().add(
                                Message.Factory.newInstance("", tagResource.getName(), false,
                                        language));
                    }
                    if (StringUtils.isNotBlank(tagResource.getDescription())) {
                        tagTO.getDescriptions().add(
                                Message.Factory.newInstance("", tagResource.getDescription(),
                                        false, language));
                    }
                }
                tags.add(tagTO);
            }
        }
        return tags;
    }

    /**
     * Check the text of an note
     * 
     * @param text
     *            textContent of the not
     * @param isHtml
     *            is content html
     * @param maxTextLength
     *            max length of text for note
     * @return content or text of the note
     * @throws IllegalRequestParameterException
     *             text value is wrong
     */
    public static String checkNoteText(String text, Boolean isHtml, Integer maxTextLength)
            throws IllegalRequestParameterException {
        if (text == null || StringUtils.isBlank(text)) {
            throw new IllegalRequestParameterException("text", text, "Note text is empty.");
        } else if (isHtml != null && isHtml && !HTMLHelper.containsNonEmptyTextNodes(text)) {
            throw new IllegalRequestParameterException("text", text,
                    "No text between HTML elements.");
        }
        if (maxTextLength != null && maxTextLength < text.length()) {
            throw new IllegalRequestParameterException("text", text,
                    "Text of Note is to long. Max " + maxTextLength + " allowed.");
        }
        return text;
    }

    /**
     * Get query instance to filter notes
     * 
     * @param noteCollectionParameter
     *            Extract context parameter needed for filter
     * @param nameProvider
     *            The name provider.
     * @param locale
     *            locale of the current user
     * @return NoteQueryInstance
     */
    public static NoteQueryParameters configureQueryInstance(
            GetCollectionNoteParameter noteCollectionParameter,
            QueryParametersParameterNameProvider nameProvider, Locale locale) {
        NoteQueryParameters queryParameters = NOTE_QUERY_DEFINITION.createInstance();
        TimelineQueryParametersConfigurator<NoteQueryParameters> queryInstanceConfigurator;
        queryInstanceConfigurator = new TimelineQueryParametersConfigurator<NoteQueryParameters>(
                nameProvider);
        queryInstanceConfigurator.configure(
                generateParameterMap(noteCollectionParameter, nameProvider), queryParameters);

        queryParameters.getTypeSpecificExtension().setIncludeChildTopics(false);
        return queryParameters;
    }

    /**
     * Creates a suitable note render context.
     * 
     * @param filterHtml
     *            if true the content should be returned as plain text
     * @param beautifyPlainText
     *            if true beautifications can be applied when creating the plain text. Will be
     *            ignored if filterHtml is false.
     * @param request
     *            the current request
     * @return the prepared note render context
     */
    public static NoteRenderContext createNoteRenderContext(Boolean filterHtml,
            boolean beautifyPlainText, Request request) {
        HttpServletRequest httpServletRequest = RequestHelper.getHttpServletRequest(request);
        NoteRenderContext renderContext = new NoteRenderContext(filterHtml != null
                && filterHtml.booleanValue() ? NoteRenderMode.PLAIN : NoteRenderMode.HTML,
                SessionHandler.instance().getCurrentLocale(httpServletRequest));
        renderContext.setRequest(httpServletRequest);
        if (beautifyPlainText) {
            renderContext.getModeOptions().put(NoteRenderMode.PLAIN_MODE_OPTION_KEY_BEAUTIFY,
                    Boolean.TRUE.toString());
        }
        return renderContext;
    }

    /**
     * Generate map with all valid parameters
     * 
     * @param noteCollectionParameter
     *            the parameters for the request
     * @param nameProvider
     *            The name provider.
     * @return a map containing converted parameters
     */
    private static HashMap<String, Object> generateParameterMap(
            GetCollectionNoteParameter noteCollectionParameter,
            QueryParametersParameterNameProvider nameProvider) {
        HashMap<String, Object> parameters = new HashMap<String, Object>();

        // global parameter-groups
        parameters.put(nameProvider.getNameForMaxCount(),
                ObjectUtils.toString(noteCollectionParameter.getMaxCount()));
        parameters.put(nameProvider.getNameForOffset(),
                ObjectUtils.toString(noteCollectionParameter.getOffset()));

        // local parameters
        parameters.put(nameProvider.getNameForBlogAliases(),
                StringUtils.join(noteCollectionParameter.getF_topicAliases(), ","));
        parameters.put(nameProvider.getNameForBlogIds(),
                StringUtils.join(noteCollectionParameter.getF_topicIds(), ","));
        parameters.put(nameProvider.getNameForDirectMessages(),
                BooleanUtils.toBoolean(noteCollectionParameter.getF_showDirectMessages()));
        parameters.put(nameProvider.getNameForDiscussionId(),
                ObjectUtils.toString(noteCollectionParameter.getF_discussionId()));
        parameters.put(nameProvider.getNameForFavorite(),
                BooleanUtils.toBoolean(noteCollectionParameter.getF_showFavorites()));
        parameters.put(nameProvider.getNameForFollowedNotes(),
                BooleanUtils.toBoolean(noteCollectionParameter.getF_showFollowedItems()));
        parameters.put(nameProvider.getNameForFullTextSearchString(),
                StringUtils.trimToEmpty(noteCollectionParameter.getF_fullTextSearchString()));
        parameters.put(nameProvider.getNameForIncludeComments(),
                BooleanUtils.toBoolean(noteCollectionParameter.getF_includeComments()));
        parameters.put(nameProvider.getNameForParentPostId(),
                ObjectUtils.toString(noteCollectionParameter.getF_parentNoteId()));
        parameters.put(nameProvider.getNameForPostSearchString(),
                StringUtils.trimToEmpty(noteCollectionParameter.getF_noteSearchString()));
        parameters.put(nameProvider.getNameForPropertyFilter(),
                StringUtils.trimToEmpty(noteCollectionParameter.getF_propertyFilter()));
        parameters.put(nameProvider.getNameForShowPostsForMe(),
                BooleanUtils.toBoolean(noteCollectionParameter.getF_showNotesForMe()));
        Date date = noteCollectionParameter.getF_startDate();
        if (date != null) {
            parameters.put(nameProvider.getNameForStartDate(), date.getTime());
        }
        date = noteCollectionParameter.getF_endDate();
        if (date != null) {
            parameters.put(nameProvider.getNameForEndDate(), date.getTime());
        }
        parameters.put(nameProvider.getNameForTagPrefix(),
                StringUtils.trimToEmpty(noteCollectionParameter.getF_tagPrefix()));
        parameters.put(nameProvider.getNameForTags(),
                StringUtils.join(noteCollectionParameter.getF_tags(), ","));
        parameters.put(nameProvider.getNameForUserIds(),
                StringUtils.join(noteCollectionParameter.getF_userIds(), ","));
        parameters.put(nameProvider.getNameForUserSearchString(),
                StringUtils.trimToEmpty(noteCollectionParameter.getF_userSearchString()));

        return parameters;
    }

    /**
     * Get a {@link PageableList} of a {@link NoteQueryParameters}
     * 
     * @param noteQueryInstance
     *            the prepared query instance
     * @param renderContext
     *            the render context to use
     * @return {@link PageableList}
     */
    public static PageableList<NoteData> getPageableList(NoteQueryParameters noteQueryInstance,
            NoteRenderContext renderContext) {
        SimpleNoteListItemToNoteDataQueryResultConverter<NoteData> converter;
        converter = new SimpleNoteListItemToNoteDataQueryResultConverter<NoteData>(
                NoteData.class, renderContext);
        PageableList<NoteData> postListItems = ServiceLocator
                .instance().getService(QueryManagement.class)
                .query(NOTE_QUERY_DEFINITION, noteQueryInstance, converter);
        return postListItems;
    }

    /**
     * Get the user to notify
     * 
     * @param usersToNotify
     *            users to notify as array
     * @return users to notify
     */
    private static HashSet<String> getUsersToNotify(String[] usersToNotify) {
        HashSet<String> usersToNotifyList = new HashSet<String>();
        if (usersToNotify != null) {
            for (String userToNotify : usersToNotify) {
                usersToNotifyList.add(userToNotify);
            }
        }
        return usersToNotifyList;
    }

    /**
     * Set elements of {@link NoteStoringTO} object that are the same in create or edit method
     * 
     * @param isHtml
     *            is note text content html
     * @param noteStoringTO
     *            {@link NoteStoringTO}
     */
    public static void setToForParameterUnspecificElements(Boolean isHtml,
            NoteStoringTO noteStoringTO) {
        noteStoringTO.setContentType(isHtml != null && isHtml ? NoteContentType.HTML
                : NoteContentType.PLAIN_TEXT);
        noteStoringTO.setCreationSource(NoteCreationSource.API);
        noteStoringTO.setFailDefinition(new NoteStoringFailDefinition());
        noteStoringTO.setPublish(true);
        noteStoringTO.setSendNotifications(true);
        noteStoringTO.setCreatorId(SecurityHelper.getCurrentUserId());
    }

    /**
     * Sort the notes by date in ascending order and return a subset that contains at most maxCount
     * notes, starting with the oldest
     * 
     * @param notesToSort
     *            the notes to sort
     * @param maxCount
     *            parameters to get a collection of notes
     * @return the sorted sub set which is a view on the provided collection. The provided
     *         collection should therefore not be modified.
     */
    public static List<NoteResource> sortAndLimitNotes(List<NoteResource> notesToSort,
            Integer maxCount) {
        Collections.sort(notesToSort, new NoteResourceComparator());
        int size = notesToSort.size();
        maxCount = maxCount == null ? 10 : maxCount;

        return notesToSort.subList(0, maxCount > size ? size : maxCount);
    }

    /**
     * Default Constructor
     */
    private NoteResourceHelper() {

    }

}
