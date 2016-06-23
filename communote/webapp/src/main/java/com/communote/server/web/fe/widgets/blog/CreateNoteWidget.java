package com.communote.server.web.fe.widgets.blog;

import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.StringEscapeUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.JsonNodeFactory;
import org.codehaus.jackson.node.ObjectNode;

import com.communote.common.util.HTMLHelper;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.attachment.AttachmentData;
import com.communote.server.api.core.blog.BlogAccessException;
import com.communote.server.api.core.blog.BlogData;
import com.communote.server.api.core.blog.BlogManagement;
import com.communote.server.api.core.blog.BlogNotFoundException;
import com.communote.server.api.core.config.ConfigurationManager;
import com.communote.server.api.core.note.NoteData;
import com.communote.server.api.core.note.NoteRenderContext;
import com.communote.server.api.core.note.NoteRenderMode;
import com.communote.server.api.core.property.PropertyManagement;
import com.communote.server.api.core.property.StringPropertyTO;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.api.core.tag.TagData;
import com.communote.server.api.core.user.DetailedUserData;
import com.communote.server.api.util.JsonHelper;
import com.communote.server.core.blog.NoteManagement;
import com.communote.server.core.blog.NoteNotFoundException;
import com.communote.server.core.blog.notes.processors.CreateRepostNoteMetadataRenderingPreProcessor;
import com.communote.server.core.blog.notes.processors.RepostNoteStoringPreProcessor;
import com.communote.server.core.common.session.SessionHandler;
import com.communote.server.core.security.SecurityHelper;
import com.communote.server.core.user.UserManagement;
import com.communote.server.core.vo.blog.AutosaveNoteData;
import com.communote.server.model.blog.Blog;
import com.communote.server.model.user.ImageSizeType;
import com.communote.server.model.user.UserRole;
import com.communote.server.persistence.blog.FilterNoteProperty;
import com.communote.server.service.NoteService;
import com.communote.server.service.UserService;
import com.communote.server.web.commons.FormAction;
import com.communote.server.web.commons.MessageHelper;
import com.communote.server.web.fe.portal.blog.helper.BlogSearchHelper;
import com.communote.server.web.fe.portal.blog.helper.CreateBlogPostFeHelper;
import com.communote.server.web.fe.portal.blog.helper.UserSearchHelper;
import com.communote.server.widgets.AbstractWidget;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class CreateNoteWidget extends AbstractWidget {

    private String action;
    private ObjectNode noteCreationData;

    /**
     * Creates a JSON object with data about a note.
     *
     * @param item
     *            the list item from which the data will be extracted
     * @param isAutosave
     *            whether the item is an autosave or published note
     * @param plaintextOnly
     *            true if the client expects the content in plaintext and not HTML
     * @return the JSON object
     */
    private ObjectNode createJsonNoteObject(NoteData item, boolean isAutosave,
            boolean plaintextOnly) {

        ObjectNode jsonObj = JsonHelper.getSharedObjectMapper().createObjectNode();

        if (plaintextOnly) {
            jsonObj.put("content", HTMLHelper.htmlToPlaintextExt(item.getContent(), false));
        } else {
            jsonObj.put("content", item.getContent());
        }
        jsonObj.put("tags", extractTags(item));
        jsonObj.put("usersToNotify", extractUsersToNotify(item));
        jsonObj.put("attachments", extractAttachments(item));
        jsonObj.put("isDirectMessage", item.isDirect());
        jsonObj.put("isAutosave", isAutosave);
        if (item.getBlog() != null) {
            jsonObj.put("targetBlog", BlogSearchHelper.createBlogSearchJSONResult(item.getBlog()
                    .getId(), item.getBlog().getAlias(), item.getBlog().getTitle(), false));
        }
        if (item.getObjectProperties() != null) {
            ArrayNode properties = jsonObj.putArray("properties");
            for (StringPropertyTO property : item.getObjectProperties()) {
                ObjectNode propertyJson = properties.addObject();
                propertyJson.put("keyGroup", property.getKeyGroup());
                propertyJson.put("key", property.getPropertyKey());
                propertyJson.put("value", property.getPropertyValue());
            }
        }
        return jsonObj;
    }

    /**
     * Creates a JSON object with note data for edit case.
     *
     * @param noteId
     *            ID of the note
     * @param locale
     *            the locale of the current user
     * @param plaintextOnly
     *            true if the client expects the content in plaintext and not HTML
     * @return the json object with the note date for edit
     * @throws NoteNotFoundException
     *             in case the note to edit cannot be found
     * @throws AuthorizationException
     *             in case the user has no read access to the note
     */
    private ObjectNode createJsonNoteObjectForEdit(long noteId, Locale locale, boolean plaintextOnly)
            throws NoteNotFoundException, AuthorizationException {
        if (noteId > 0) {
            // don't pass a render mode because we want the original note
            NoteData item = getNoteManagement().getNote(noteId,
                    new NoteRenderContext(null, locale));
            ObjectNode noteToEdit = this.createJsonNoteObject(item, false, plaintextOnly);
            noteToEdit.put("numberOfDiscussionNotes", item.getNumberOfDiscussionNotes());
            return noteToEdit;
        } else {
            throw new NoteNotFoundException("Note to edit does not exist");
        }
    }

    /**
     * Createsa JSON object with note data for reply case.
     *
     * @param parentNoteId
     *            the id of the parent note for the reply
     * @param locale
     *            the locale of the current user
     * @param plaintextOnly
     *            true if the client expects the content in plaintext and not HTML
     * @param authorNotification
     *            true if the notification syntax to inform the author of the reply should be added
     * @param inheritTags
     *            true if the reply should inherit all the tags of the parent note
     * @throws NoteNotFoundException
     *             in case the note to edit cannot be found
     * @throws AuthorizationException
     *             in case the user has no read access to the note
     * @return the note with initial data for a reply
     */
    private ObjectNode createJsonNoteObjectForReply(long parentNoteId, Locale locale,
            boolean plaintextOnly, boolean authorNotification, boolean inheritTags)
            throws NoteNotFoundException, AuthorizationException {
        if (parentNoteId <= 0) {
            throw new NoteNotFoundException("Parent note does not exist");
        }
        NoteData parentNoteItem = getNoteManagement().getNote(parentNoteId,
                new NoteRenderContext(null, locale));
        NoteData newNoteItem = new NoteData();
        getRequest().setAttribute("isDirectReply", parentNoteItem.isDirect());
        newNoteItem.setBlog(parentNoteItem.getBlog());
        String notifier = getNotifier(authorNotification, parentNoteItem);
        newNoteItem.setContent(notifier);
        newNoteItem.setDirect(parentNoteItem.isDirect());
        if (inheritTags) {
            newNoteItem.setTags(parentNoteItem.getTags());
        }
        return this.createJsonNoteObject(newNoteItem, false, plaintextOnly);
    }

    /**
     * Createsa JSON object with note data for reply case.
     *
     * @param repostNoteId
     *            the id of the original note to repost
     * @param locale
     *            the locale of the current user
     * @param plaintextOnly
     *            true if the client expects the content in plaintext and not HTML
     * @param copyAttachments
     *            true if the attachments of the original note should be copied to the repost
     * @param inheritTags
     *            true if the repost should inherit all the tags of the original note
     * @throws NoteNotFoundException
     *             in case the note to edit cannot be found
     * @throws AuthorizationException
     *             in case the user has no read access to the note
     * @return the note with initial data for a repost
     */
    private ObjectNode createJsonNoteObjectForRepost(long repostNoteId, Locale locale,
            boolean plaintextOnly, boolean copyAttachments, boolean inheritTags)
            throws NoteNotFoundException, AuthorizationException {
        if (repostNoteId <= 0) {
            throw new NoteNotFoundException("Note to repost does not exist");
        }
        NoteRenderContext renderContext = new NoteRenderContext(
                plaintextOnly ? NoteRenderMode.REPOST_PLAIN : NoteRenderMode.REPOST, locale);
        renderContext.getModeOptions().put(
                CreateRepostNoteMetadataRenderingPreProcessor.MODE_OPTION_DISABLE_COPY_ATTACHMENTS,
                String.valueOf(!copyAttachments));
        renderContext.getModeOptions().put(
                CreateRepostNoteMetadataRenderingPreProcessor.MODE_OPTION_ENABLE_COPY_TAGS,
                String.valueOf(inheritTags));
        NoteData originalNoteItem = getNoteManagement().getNote(repostNoteId, renderContext);
        return this.createJsonNoteObject(originalNoteItem, false, plaintextOnly);
    }

    /**
     * Creates a JSON object with autosave data.
     *
     * @param noteId
     *            ID of the note
     * @param parentNoteId
     *            the ID of the parent note
     * @param repostNoteId
     *            Id of a possible repost note.
     * @param locale
     *            the locale of the current user
     * @param plaintextOnly
     *            true if the client expects the content in plaintext and not HTML
     * @return Response as JSON
     */
    private ObjectNode createJsonNoteObjectFromAutosave(long noteId, long parentNoteId,
            long repostNoteId, Locale locale, boolean plaintextOnly) {
        Long nId = noteId < 0 ? null : noteId;
        Long parentId = parentNoteId < 0 ? null : parentNoteId;
        FilterNoteProperty[] propertyFilters = null;
        // when not editing or commenting filter for the repost property
        if (nId == null && parentId == null) {
            propertyFilters = new FilterNoteProperty[1];
            propertyFilters[0] = new FilterNoteProperty();
            propertyFilters[0].setKeyGroup(PropertyManagement.KEY_GROUP);
            propertyFilters[0].setPropertyKey(RepostNoteStoringPreProcessor.KEY_ORIGIN_NOTE_ID);
            if (repostNoteId >= 0) {
                // add repost note properties
                propertyFilters[0].setPropertyValue(String.valueOf(repostNoteId));
            } else {
                // ensure the repost is not taken in the default create note case
                propertyFilters[0].setInclude(false);
            }
        }
        AutosaveNoteData autosave = getNoteManagement().getAutosave(nId, parentId,
                propertyFilters, locale);
        if (autosave != null) {
            ObjectNode jsonObject = createJsonNoteObject(autosave, true, plaintextOnly);
            jsonObject.put("autosaveNoteId", autosave.getId());
            jsonObject.put("autosaveVersion", autosave.getVersion());
            jsonObject.put("crosspostBlogs", extractCrosspostBlogs(autosave));
            return jsonObject;
        }
        return null;
    }

    /**
     * Extracts the attachments from a note and creates a JSON array.
     *
     * @param item
     *            the item that holds the note data
     * @return the array
     */
    private ArrayNode extractAttachments(NoteData item) {
        ArrayNode result = JsonHelper.getSharedObjectMapper().createArrayNode();
        List<AttachmentData> attachments = item.getAttachments();
        if (attachments != null) {
            for (AttachmentData attachment : attachments) {
                ObjectNode attach = CreateBlogPostFeHelper.createAttachmentJSONObject(attachment);
                result.add(attach);
            }
        }
        return result.size() == 0 ? null : result;
    }

    /**
     * Extracts the crosspost blogs from an autosave and creates a JSON array.
     *
     * @param item
     *            the item that holds the note data
     * @return the array
     */
    private ArrayNode extractCrosspostBlogs(AutosaveNoteData item) {
        ArrayNode result = JsonHelper.getSharedObjectMapper().createArrayNode();
        if (item.getCrosspostBlogs() != null) {
            for (BlogData b : item.getCrosspostBlogs()) {
                String title = b.getTitle();
                ObjectNode blog = BlogSearchHelper.createBlogSearchJSONResult(b.getId(),
                        b.getNameIdentifier(), title, false);
                result.add(blog);
            }
        }
        if (result.size() == 0) {
            return null;
        }
        return result;
    }

    /**
     * Extracts the tags from a note and creates a JSON array.
     *
     * @param item
     *            the item that holds the note data
     * @return the array
     */
    private ArrayNode extractTags(NoteData item) {
        ArrayNode result = JsonHelper.getSharedObjectMapper().createArrayNode();
        // create items manually because TagData POJO is incompatible to REST API TagResource
        if (item.getTags() != null) {
            for (TagData tagItem : item.getTags()) {
                ObjectNode tag = result.addObject();
                tag.put("tagId", tagItem.getId());
                tag.put("tagStoreAlias", tagItem.getTagStoreAlias());
                tag.put("tagStoreTagId", tagItem.getTagStoreTagId());
                String languageCode = null;
                if (tagItem.getLocale() != null) {
                    languageCode = tagItem.getLocale().toString().toLowerCase();
                }
                tag.put("languageCode", languageCode);
                tag.put("name", tagItem.getName());
                tag.put("description", tagItem.getDescription());
                tag.put("defaultName", tagItem.getDefaultName());
            }
        }
        return result;
    }

    /**
     * Extracts the users to notify from a note and creates a JSON array.
     *
     * @param item
     *            the item that holds the note data
     * @return the array
     */
    private ArrayNode extractUsersToNotify(NoteData item) {
        ArrayNode result = JsonHelper.getSharedObjectMapper().createArrayNode();
        if (item.getNotifiedUsers() != null) {
            for (DetailedUserData ul : item.getNotifiedUsers()) {
                ObjectNode user = UserSearchHelper.createUserSearchJSONResult(ul,
                        ImageSizeType.MEDIUM);
                result.add(user);
            }
        }
        // add @@ mentions by building fake users
        if (item.isMentionDiscussionAuthors()) {
            result.add(UserSearchHelper.createUserSearchJSONResult(null, null,
                    MessageHelper.getText(getRequest(), "autosuggest.atat.discussion"), null,
                    NoteManagement.CONSTANT_MENTION_DISCUSSION_PARTICIPANTS));
        }
        if (item.isMentionTopicAuthors()) {
            result.add(UserSearchHelper.createUserSearchJSONResult(null, null,
                    MessageHelper.getText(getRequest(), "autosuggest.atat.authors"), null,
                    NoteManagement.CONSTANT_MENTION_TOPIC_AUTHORS));
        }
        if (item.isMentionTopicReaders()) {
            result.add(UserSearchHelper.createUserSearchJSONResult(null, null,
                    MessageHelper.getText(getRequest(), "autosuggest.atat.all"), null,
                    NoteManagement.CONSTANT_MENTION_TOPIC_READERS));
        }
        if (item.isMentionTopicManagers()) {
            result.add(UserSearchHelper.createUserSearchJSONResult(null, null,
                    MessageHelper.getText(getRequest(), "autosuggest.atat.managers"), null,
                    NoteManagement.CONSTANT_MENTION_TOPIC_MANAGERS));
        }
        if (result.size() == 0) {
            return null;
        }
        return result;
    }

    /**
     * @return the action
     */
    public String getAction() {
        return action;
    }

    /**
     * The name for the attachments session attribute.
     *
     * @return the name
     */
    public String getAttachmentsSessionAttributeName() {
        return CreateBlogPostFeHelper.PARAM_ATTACHMENTS_SESSION_ID;
    }

    /**
     * The value for the attachments session attribute.
     *
     * @return the value
     */
    public String getAttachmentsSessionAttributeValue() {
        return "AttachmentsOf" + this.getWidgetId();
    }

    private Blog getDefaultBlog() {
        ConfigurationManager configManager = CommunoteRuntime.getInstance()
                .getConfigurationManager();
        if (configManager.getClientConfigurationProperties().isDefaultBlogEnabled()) {
            Long defaultBlogId = configManager.getClientConfigurationProperties()
                    .getDefaultBlogId();
            if (defaultBlogId != null) {
                BlogManagement blogManagement = ServiceLocator.findService(BlogManagement.class);
                try {
                    return blogManagement.getBlogById(defaultBlogId, true);
                } catch (BlogNotFoundException e) {
                    // ignore
                } catch (BlogAccessException e) {
                    // ignore
                }
            }
        }
        return null;
    }

    /**
     * The note creation data as JSON object. This object holds the default blog ID if any and can
     * have two two additional members: an autosave and a note to be edited.
     *
     * @return string representation of the JSON object
     */
    public String getNoteCreationData() {
        if (noteCreationData == null) {
            return JsonHelper.writeJsonTreeAsString(noteCreationData);
        }
        return StringEscapeUtils.escapeXml(JsonHelper.writeJsonTreeAsString(noteCreationData));
    }

    /**
     * @return the notemanagement service class
     */
    private NoteService getNoteManagement() {
        return ServiceLocator.instance().getService(NoteService.class);
    }

    /**
     * @param authorNotification
     *            If auther should be notified.
     * @param parentNoteItem
     *            The parent item.
     * @return The notifier string.
     */
    private String getNotifier(boolean authorNotification, NoteData parentNoteItem) {
        if (!authorNotification
                || ServiceLocator.findService(UserService.class).hasRole(
                        parentNoteItem.getUser().getId(), UserRole.ROLE_SYSTEM_USER)) {
            return "";
        }
        StringBuilder notifier = new StringBuilder();
        String parentAuthorAlias = parentNoteItem.getUser().getAlias();
        if (parentNoteItem.isDirect()) {
            String currentUserAlias = SecurityHelper.getCurrentUserAlias();
            if (!currentUserAlias.equals(parentAuthorAlias)) {
                notifier.append("@" + parentAuthorAlias.toLowerCase() + " ");
            }
            List<DetailedUserData> usersToNotify = parentNoteItem.getNotifiedUsers();

            for (DetailedUserData userListItem : usersToNotify) {
                String alias = userListItem.getAlias();
                if (!alias.equals(parentAuthorAlias)
                        && !alias.startsWith(UserManagement.ANONYMIZE_USER_PREFIX)
                        && !userListItem.getAlias().equals(currentUserAlias)) {
                    notifier.append("@" + alias.toLowerCase() + " ");
                }
            }
            if (notifier.length() > 0) {
                notifier.insert(0, "d ");
            }
        } else {
            notifier.append("@");
            notifier.append(parentAuthorAlias.toLowerCase() + " ");
        }
        return notifier.toString();
    }

    /**
     * Return the blog ID of target bolg of the note.
     *
     * @return the blog ID
     */
    public Long getTargetBlog() {
        return 0L;
    }

    /**
     * {@inheritDoc}
     *
     * @see com.communote.server.widgets.Widget#getTile(String)
     */
    @Override
    public String getTile(String outputType) {
        return "widget.blog.createNote." + outputType;
    }

    // TODO error handling: set application error in response header and send error message to
    // client
    @Override
    public Object handleRequest() {
        this.action = getParameter("action", FormAction.CREATE);
        long noteId = getLongParameter("noteId", -1L);
        long parentNoteId = getLongParameter("parentPostId", -1L);
        long repostNoteId = getLongParameter("repostNoteId", -1L);

        boolean plaintextOnly = getBooleanParameter("plaintextOnly", false);
        boolean autosaveDisabled = getBooleanParameter("autosaveDisabled", false);
        Locale locale = SessionHandler.instance().getCurrentLocale(getRequest());
        ObjectNode initialNote = null;
        ObjectNode autosave = null;
        noteCreationData = JsonHelper.getSharedObjectMapper().createObjectNode();
        // check for autosave
        try {
            Long defaultBlogId = putDefaultBlogInfo(noteCreationData);
            setParameter("defaultBlogId", String.valueOf(defaultBlogId));
            if (!autosaveDisabled) {
                autosave = createJsonNoteObjectFromAutosave(noteId, parentNoteId, repostNoteId,
                        locale, plaintextOnly);
            }
            boolean inheritTags = getBooleanParameter("inheritTags", false);
            if (isCommentAction()) {
                boolean authorNotification = getBooleanParameter("authorNotification", true);
                initialNote = createJsonNoteObjectForReply(parentNoteId, locale, plaintextOnly,
                        authorNotification, inheritTags);
            } else if (isEditAction()) {
                initialNote = createJsonNoteObjectForEdit(noteId, locale, plaintextOnly);
            } else if (repostNoteId > -1) {
                initialNote = createJsonNoteObjectForRepost(repostNoteId, locale, plaintextOnly,
                        getBooleanParameter("copyAttachments", true), inheritTags);
            }
            setResponseMetadata("autosave", autosave);
            setResponseMetadata("initialNote", initialNote);
        } catch (NoteNotFoundException e) {
            MessageHelper.saveErrorMessageFromKey(getRequest(),
                    "error.blogpost.create.post.not.found");
        } catch (AuthorizationException e) {
            MessageHelper.saveErrorMessageFromKey(getRequest(),
                    "error.blogpost.create.post.no.access");
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initParameters() {
        // nothing
    }

    /**
     * Whether the widget is rendered for commenting a note.
     *
     * @return true if rendered for commenting a note, false otherwise
     */
    public boolean isCommentAction() {
        return FormAction.COMMENT.equals(this.action);
    }

    /**
     * Whether the widget is rendered for creating a new note.
     *
     * @return true if rendered for creating a new note, false otherwise
     */
    public boolean isCreateAction() {
        return FormAction.CREATE.equals(this.action);
    }

    /**
     * Whether the widget is rendered for editing a note.
     *
     * @return true if rendered for editing a note, false otherwise
     */
    public boolean isEditAction() {
        return FormAction.EDIT.equals(this.action);
    }

    /**
     * Adds the data of the default blog if it is activated
     *
     * @param noteData
     *            the data to modify
     * @return the ID of the default blog if enabled, null otherwise
     *
     */
    private Long putDefaultBlogInfo(ObjectNode noteData) {
        Long defaultBlogId = null;
        Blog defaultBlog = null;
        JsonNode defaultBlogTitle;
        JsonNode defaultBlogAlias;
        defaultBlog = getDefaultBlog();
        JsonNodeFactory factory = JsonHelper.getSharedObjectMapper().getNodeFactory();
        if (defaultBlog != null) {
            defaultBlogId = defaultBlog.getId();
            defaultBlogTitle = factory.textNode(defaultBlog.getTitle());
            defaultBlogAlias = factory.textNode(defaultBlog.getNameIdentifier());
        } else {
            defaultBlogTitle = factory.nullNode();
            defaultBlogAlias = factory.nullNode();
        }
        noteData.put("defaultBlogId",
                defaultBlogId == null ? factory.nullNode() : factory.numberNode(defaultBlogId));
        noteData.put("defaultBlogTitle", defaultBlogTitle);
        noteData.put("defaultBlogAlias", defaultBlogAlias);
        return defaultBlogId;
    }

}
