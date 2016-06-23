package com.communote.plugins.api.rest.v30.resource.note;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Request;

import org.apache.commons.lang3.StringUtils;

import com.communote.common.util.HTMLHelper;
import com.communote.plugins.api.rest.v30.request.RequestHelper;
import com.communote.plugins.api.rest.v30.resource.attachment.AttachmentResource;
import com.communote.plugins.api.rest.v30.resource.tag.TagResource;
import com.communote.plugins.api.rest.v30.service.IllegalRequestParameterException;
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
import com.communote.server.core.security.SecurityHelper;
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
        boolean isNoteForMe = false;
        if (usersToNotifyList != null) {
            String[] usersToNotify = new String[usersToNotifyList.size()];
            Long[] usersToNotifyIds = new Long[usersToNotifyList.size()];
            for (int i = 0; i < usersToNotifyList.size(); i++) {
                if (usersToNotifyList.get(i).getId().equals(currentUserId)) {
                    isNoteForMe = true;
                }
                usersToNotify[i] = usersToNotifyList.get(i).getAlias();
                usersToNotifyIds[i] = usersToNotifyList.get(i).getId();
            }
            newNoteResource.setUsersToNotify(usersToNotify);
            newNoteResource.setUsersToNotifyIds(usersToNotifyIds);
        }
        newNoteResource.setIsNoteForMe(isNoteForMe || note.isForMe());
    }

    /**
     * Get note storing transfer object
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
        noteStoringTO.setIsDirectMessage((createNoteParameter.getIsDirectMessage() == null) ? false
                : createNoteParameter.getIsDirectMessage());

        noteStoringTO.setUsersToNotify(getUsersToNotify(createNoteParameter.getUsersToNotify()));
        noteStoringTO.setTags(buildTags(createNoteParameter.getTags()));
        NoteResourceHelper.setToForParameterUnspecificElements(createNoteParameter.getIsHtml(),
                noteStoringTO);
        noteStoringTO.setAutosaveNoteId(createNoteParameter.getAutosaveNoteId());
        noteStoringTO.setPublish((createNoteParameter.getPublish() == null) ? true
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
        if (editNoteParameter.getText() != null && editNoteParameter.getText().length() > 0) {
            noteStoringTO.setContent(NoteResourceHelper.checkNoteText(editNoteParameter.getText(),
                    editNoteParameter.getIsHtml(), editNoteParameter.getMaxTextLength()));
        }
        noteStoringTO.setUsersToNotify(getUsersToNotify(editNoteParameter.getUsersToNotify()));
        noteStoringTO.setTags(buildTags(editNoteParameter.getTags()));
        NoteResourceHelper.setToForParameterUnspecificElements(editNoteParameter.getIsHtml(),
                noteStoringTO);
        noteStoringTO.setAutosaveNoteId(editNoteParameter.getAutosaveNoteId());
        noteStoringTO.setPublish((editNoteParameter.getPublish() == null) ? true
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
    private static String checkNoteText(String text, Boolean isHtml, Integer maxTextLength)
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
     * Get the user to notify
     * 
     * @param usersToNotify
     *            users to notify as array
     * @return users to notify
     */
    private static HashSet<String> getUsersToNotify(String[] usersToNotify) {
        HashSet<String> usersToNotiyList = new HashSet<String>();
        if (usersToNotify != null) {
            for (String userToNotify : usersToNotify) {
                usersToNotiyList.add(userToNotify);
            }
        }
        return usersToNotiyList;
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
     * Default Constructor
     */
    private NoteResourceHelper() {

    }

}
