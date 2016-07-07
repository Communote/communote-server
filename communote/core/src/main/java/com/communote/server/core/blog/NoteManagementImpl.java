package com.communote.server.core.blog;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionInterceptor;

import com.communote.common.converter.CollectionConverter;
import com.communote.common.converter.Converter;
import com.communote.common.util.Pair;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.blog.BlogNotFoundException;
import com.communote.server.api.core.blog.BlogRightsManagement;
import com.communote.server.api.core.common.NotFoundException;
import com.communote.server.api.core.config.ConfigurationManager;
import com.communote.server.api.core.config.type.ClientProperty;
import com.communote.server.api.core.event.EventDispatcher;
import com.communote.server.api.core.note.AutosavePropertyFilterProviderManager;
import com.communote.server.api.core.note.NoteData;
import com.communote.server.api.core.note.NoteManagementAuthorizationException;
import com.communote.server.api.core.note.NotePermissionManagement;
import com.communote.server.api.core.note.NoteRenderContext;
import com.communote.server.api.core.note.NoteStoringFailDefinition;
import com.communote.server.api.core.note.NoteStoringTO;
import com.communote.server.api.core.note.processor.NoteStoringPostProcessorManager;
import com.communote.server.api.core.note.processor.NoteStoringPreProcessorException;
import com.communote.server.api.core.note.processor.NoteStoringPreProcessorManager;
import com.communote.server.api.core.property.PropertyHelper;
import com.communote.server.api.core.property.PropertyManagement;
import com.communote.server.api.core.property.PropertyType;
import com.communote.server.api.core.property.StringPropertyFilter;
import com.communote.server.api.core.property.StringPropertyTO;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.api.core.tag.TagTO;
import com.communote.server.api.core.task.TaskAlreadyExistsException;
import com.communote.server.core.blog.notes.DirectMessageConversionException;
import com.communote.server.core.blog.notes.processors.NotePostProcessTaskHandler;
import com.communote.server.core.blog.notes.processors.exceptions.DirectMessageMissingRecipientException;
import com.communote.server.core.blog.notes.processors.exceptions.DirectMessageWrongRecipientForAnswerException;
import com.communote.server.core.blog.notes.processors.exceptions.MessageKeyNoteContentException;
import com.communote.server.core.common.LimitHelper;
import com.communote.server.core.common.caching.CacheManager;
import com.communote.server.core.filter.listitems.SimpleNoteListItem;
import com.communote.server.core.mail.MailManagement;
import com.communote.server.core.mail.messages.user.NoteLimitReachedMailMessage;
import com.communote.server.core.retrieval.helper.AttachmentHelper;
import com.communote.server.core.security.AuthenticationHelper;
import com.communote.server.core.security.SecurityHelper;
import com.communote.server.core.storing.AttachmentStillAssignedException;
import com.communote.server.core.storing.NoteLimitReachedException;
import com.communote.server.core.storing.ResourceStoringHelper;
import com.communote.server.core.storing.ResourceStoringManagement;
import com.communote.server.core.storing.ResourceStoringManagementException;
import com.communote.server.core.tag.TagManagement;
import com.communote.server.core.tag.TagNotFoundException;
import com.communote.server.core.tag.TagParser;
import com.communote.server.core.tag.TagParserFactory;
import com.communote.server.core.tag.TagStoreNotFoundException;
import com.communote.server.core.user.UserManagement;
import com.communote.server.core.user.UserManagementHelper;
import com.communote.server.core.vo.blog.AutosaveNoteData;
import com.communote.server.core.vo.blog.DiscussionNoteData;
import com.communote.server.core.vo.blog.NoteModificationResult;
import com.communote.server.core.vo.blog.NoteModificationStatus;
import com.communote.server.core.vo.query.QueryResultConverter;
import com.communote.server.core.vo.query.note.SimpleNoteListItemToAutosaveNoteDataConverter;
import com.communote.server.core.vo.query.note.SimpleNoteListItemToNoteDataQueryResultConverter;
import com.communote.server.core.vo.uti.UserNotificationResult;
import com.communote.server.model.attachment.Attachment;
import com.communote.server.model.attachment.AttachmentStatus;
import com.communote.server.model.blog.Blog;
import com.communote.server.model.blog.BlogRole;
import com.communote.server.model.blog.UserToBlogRoleMapping;
import com.communote.server.model.note.Content;
import com.communote.server.model.note.Note;
import com.communote.server.model.note.NoteCreationSource;
import com.communote.server.model.note.NoteStatus;
import com.communote.server.model.tag.Tag;
import com.communote.server.model.user.User;
import com.communote.server.model.user.UserRole;
import com.communote.server.model.user.UserStatus;
import com.communote.server.persistence.blog.BlogDao;
import com.communote.server.persistence.blog.NoteDao;
import com.communote.server.persistence.common.messages.ResourceBundleManager;
import com.communote.server.persistence.resource.AttachmentDao;
import com.communote.server.persistence.resource.ContentDao;
import com.communote.server.persistence.user.UserDao;
import com.communote.server.persistence.user.UserNotePropertyDao;
import com.communote.server.persistence.user.client.ClientHelper;
import com.communote.server.persistence.user.note.UserNoteEntityDao;

/**
 * @see com.communote.server.core.blog.NoteManagement
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
@Service("noteManagement")
public class NoteManagementImpl extends NoteManagementBase {

    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(NoteManagementImpl.class);

    /** The tag parser. */
    private TagParser tagParser;

    @Autowired
    private NoteStoringPreProcessorManager noteContentProcessorManagement;
    @Autowired
    private NoteStoringPostProcessorManager notePostProcessorExtensionPoint;
    @Autowired
    private NoteDao noteDao;
    @Autowired
    private BlogDao blogDao;
    @Autowired
    private ContentDao contentDao;
    @Autowired
    private AttachmentDao attachmentDao;
    @Autowired
    private UserDao userDao;
    @Autowired
    private EventDispatcher eventDispatcher;
    @Autowired
    private CacheManager cacheManager;
    @Autowired
    private TagManagement tagManagement;
    @Autowired
    private PropertyManagement propertyManagement;
    @Autowired
    private ResourceStoringManagement resourceStoringManagement;
    @Autowired
    private BlogRightsManagement topicRightsManagement;
    @Autowired
    private UserManagement userManagement;
    @Autowired
    private MailManagement mailManagement;
    @Autowired
    private NotePermissionManagement notePermissionManagement;
    @Autowired
    private AutosavePropertyFilterProviderManager autosavePropertyFilterProviderManager;

    private DiscussionDetailsRetriever discussionDetailsRetriever;

    /**
     * Creates tasks for sending notifications.
     *
     * @param storingTO
     *            the original noteStoringTO for which a note (or several) is created
     * @param usersToIgnore
     *            List of users which should be ignored.
     * @param notesForPostProcessing
     *            List of notes to do post processing.
     * @throws TaskAlreadyExistsException
     *             in case the notification tasks cannot be created
     */
    private void addNotesForPostProcessing(NoteStoringTO storingTO, Collection<User> usersToIgnore,
            Collection<Note> notesForPostProcessing) throws TaskAlreadyExistsException {

        if (notesForPostProcessing == null) {
            return;
        }

        Map<String, String> properties = new HashMap<>();
        if (usersToIgnore != null) {
            String userIdsString = StringUtils.join(usersToIgnore, ',');
            // TODO this is not generic enough
            if (userIdsString != null) {
                properties.put(NotePostProcessTaskHandler.PROPERTY_KEY_USER_IDS_NO_NOTIFY,
                        userIdsString);
            }
        }
        notePostProcessorExtensionPoint.process(notesForPostProcessing, storingTO, properties);

    }

    /**
     * Asserts some preconditions for {@link #createNote(NoteStoringTO, Set, StringPropertyFilter[])
     *
     * @param noteStoringTO The note.
     * @param topic The topic.
     * @throws BlogNotFoundException Exception.
     * @throws NoteStoringPreProcessorException Exception.
     */
    private void assertCreateNotePrecondition(NoteStoringTO noteStoringTO, Blog topic)
            throws BlogNotFoundException, NoteStoringPreProcessorException {
        if (topic == null) {
            LOGGER.error("Note creation failed because the topic with ID {} does not exist.",
                    noteStoringTO.getBlogId());
            throw new BlogNotFoundException(
                    "Note creation failed because the topic does not exist",
                    noteStoringTO.getBlogId(), null);
        }
        if (noteStoringTO.getCreationSource().equals(NoteCreationSource.SYSTEM)
                && !topic.isCreateSystemNotes()) {
            throw new NoteStoringPreProcessorException(
                    "System notes cannot be created in this topic");
        }
    }

    /**
     * Tests whether the user has the required rights to post to a blog.
     *
     * @param blog
     *            the blog to which the use wants to post
     * @param userId
     *            the id of the user in question
     * @throws NoteManagementAuthorizationException
     *             in case creator has not the required authorization to post to the blog
     */
    private void assertHasWriteAccess(Blog blog, Long userId)
            throws NoteManagementAuthorizationException {
        if (!topicRightsManagement.userHasWriteAccess(blog.getId(), userId, false)) {
            throw new NoteManagementAuthorizationException(
                    "The user has not the required rights to create a user tagged post.",
                    blog.getTitle());

        }
    }

    /**
     * Method to assert the note, which should be updated.
     *
     * @param noteStoringTO
     *            The storing to.
     * @param noteToEdit
     *            The note to update.
     * @throws NoteNotFoundException
     *             Exception.
     * @throws NoteManagementAuthorizationException
     *             Exception.
     */
    private void assertNoteToEditForUpdateNote(NoteStoringTO noteStoringTO, Note noteToEdit)
            throws NoteNotFoundException, NoteManagementAuthorizationException {
        if (noteToEdit == null) {
            throw new NoteNotFoundException("The Note which should be updated does not exist.");
        }
        if (!noteToEdit.getUser().getId().equals(noteStoringTO.getCreatorId())
                || NoteCreationSource.SYSTEM.equals(noteToEdit.getCreationSource())) {
            throw new NoteManagementAuthorizationException(
                    "Only creator can modify his post. The creation source of this post is '"
                            + noteToEdit.getCreationSource() + "'.", noteToEdit.getBlog()
                            .getTitle());
        }
        if (noteStoringTO.isPublish() && noteToEdit.isDirect() && noteToEdit.getChildren() != null
                && !noteToEdit.getChildren().isEmpty()) {
            throw new NoteManagementAuthorizationException(
                    "It is not allowed to edit a direct message with children. Note ID is '"
                            + noteToEdit.getId() + "'.", noteToEdit.getBlog().getTitle());
        }
    }

    /**
     * Check if the current user has read access to the given note.
     *
     * @param note
     *            The note.
     * @throws AuthorizationException
     *             Thrown, if the user is not allowed to read this note.
     */
    private void assertReadAccessOnNote(Note note) throws AuthorizationException {
        if (!topicRightsManagement.currentUserHasReadAccess(note.getBlog().getId(), false)) {
            throw new AuthorizationException("The user has no access to this blog.");
        }
        if (!SecurityHelper.isInternalSystem() && note.isDirect()) {
            Long currentUserId = SecurityHelper.getCurrentUserId();
            if (currentUserId == null) {
                // for instance in case of the public user
                throw new AuthorizationException("Only authenticated users can access this note");
            }
            if (!note.getUser().getId().equals(currentUserId)
                    && !note.getUsersToBeNotified().contains(userDao.load(currentUserId))) {
                throw new AuthorizationException(
                        "The user is not allowed to read this direct message.");
            }
        }
    }

    /**
     * This method asserts the validity of a direct message.
     *
     * @param noteStoringTO
     *            The storing TO.
     * @param result
     *            The modification result.
     * @param blog2users
     *            association between blogs and users to be notified that was created by evalTargets
     * @throws NoteStoringPreProcessorException
     *             in case the storingTO is not a valid DM
     */
    private void assertValidDirectMessage(NoteStoringTO noteStoringTO,
            NoteModificationResult result, Map<Blog, Collection<User>> blog2users)
            throws NoteStoringPreProcessorException {
        if (!noteStoringTO.isPublish() || !noteStoringTO.isIsDirectMessage()) {
            return;
        }
        // assert there is not more than 1 topic since DM crossposting isn't allowed
        if (blog2users.keySet().size() > 1 || noteStoringTO.getAdditionalBlogs() != null
                && !noteStoringTO.getAdditionalBlogs().isEmpty()) {
            throw new MessageKeyNoteContentException(
                    "error.blogpost.blog.content.processing.failed.direct.multiple.blogs");
        }
        // check users of that blog
        Collection<User> usersToNotify = blog2users.values().iterator().next();
        if (noteStoringTO.isMentionDiscussionAuthors() || noteStoringTO.isMentionTopicAuthors()
                || noteStoringTO.isMentionTopicReaders()) {
            throw new DirectMessageMissingRecipientException(true);
        }
        // DMs require at least one notification to a user
        if (usersToNotify.size() == 0) {
            // check the reason for the missing notifications an throw
            // appropriate exception
            UserNotificationResult userNotificationResult = result.getUserNotificationResult();
            throw new DirectMessageMissingRecipientException(
                    userNotificationResult.getUnresolvableUsers(),
                    userNotificationResult.getUninformableUsers());
        }
        Note parentNote;
        if (noteStoringTO.getParentNoteId() != null
                && (parentNote = noteDao.load(noteStoringTO.getParentNoteId())).isDirect()) {
            Collection<String> originalReceivers = new HashSet<>();
            originalReceivers.add(parentNote.getUser().getAlias().toLowerCase());
            for (User user : parentNote.getUsersToBeNotified()) {
                originalReceivers.add(user.getAlias().toLowerCase());
            }
            // compare original receivers against new receivers of the comment
            // use blog2users, because it contains all existing users with read
            // access
            for (User userToNotify : usersToNotify) {
                if (!originalReceivers.contains(userToNotify.getAlias().toLowerCase())) {
                    throw new DirectMessageWrongRecipientForAnswerException("The user \""
                            + userToNotify + "\" is not in "
                            + "the list of the original receivers or is "
                            + "not the original author.");

                }
            }
        }
    }

    /**
     * Tests whether an attachment can be added to the given note.
     *
     * @param noteId
     *            the ID of the note the attachment should be added to
     * @param attachment
     *            the attachment to check
     * @param creatorId
     *            the creator of the note with ID noteId
     * @return true if the attachment was not yet added to another note, false if the attachment is
     *         already connected to a note the current user did not create
     * @throws AttachmentAlreadyAssignedException
     *             in case the attachment is already assigned to another note created by the same
     *             user
     */
    private boolean canAddAttachment(Long noteId, Attachment attachment, Long creatorId)
            throws AttachmentAlreadyAssignedException {
        Note attachedNote = attachment.getNote();
        if (attachedNote != null && !attachedNote.getId().equals(noteId)) {
            if (!creatorId.equals(attachedNote.getUser().getId())) {
                // silently skip attachments that were taken from other users
                LOGGER.warn("User " + creatorId + " tried to add the attachment "
                        + attachment.getId() + " which was uploaded by another user to his note");
                return false;
            } else {
                // throw an exception if the attachment is already added to
                // another note of the same
                // author
                throw new AttachmentAlreadyAssignedException(
                        "Attachment is already assigned to another note", attachment.getId(),
                        attachment.getName());
            }
        }
        return true;
    }

    /**
     * Tests whether the limit for notes will be reached when a certain number of notes are added.
     * Also sends notification emails that the limit was reached is about to be reached.
     *
     * @param items
     *            the number of notes to be added
     * @return true if the limit will not be reached, false otherwise
     */
    private boolean checkNoteLimit(long items) {
        long limit = ResourceStoringHelper.getCountLimit();
        boolean result = true;
        if (limit > 0) {
            ConfigurationManager configManager = CommunoteRuntime.getInstance()
                    .getConfigurationManager();
            long count = noteDao.getNotesCount();
            result = count + items < limit;
            float percent = count + items / limit;
            if (percent >= 1.0F) {
                String mailSentDate = configManager.getClientConfigurationProperties().getProperty(
                        ClientProperty.CLIENT_USER_TAGGED_COUNT_100_MAIL);
                java.sql.Date today = new java.sql.Date(new java.util.Date().getTime());
                if (mailSentDate == null || !java.sql.Date.valueOf(mailSentDate).equals(today)) {
                    configManager.updateClientConfigurationProperty(
                            ClientProperty.CLIENT_USER_TAGGED_COUNT_100_MAIL, today.toString());
                    sendLimitReachedEmail(count, limit);
                }
            } else if (percent >= 0.9F) {
                String mailSentDate = configManager.getClientConfigurationProperties().getProperty(
                        ClientProperty.CLIENT_USER_TAGGED_COUNT_90_MAIL);
                java.sql.Date today = new java.sql.Date(new java.util.Date().getTime());
                if (mailSentDate == null || !java.sql.Date.valueOf(mailSentDate).equals(today)) {
                    configManager.updateClientConfigurationProperty(
                            ClientProperty.CLIENT_USER_TAGGED_COUNT_90_MAIL, today.toString());
                    sendLimitReachedEmail(count, limit);
                }
            }
        }
        return result;
    }

    /**
     * Removes all attachments whose IDs are not in the attachmentIds array.
     *
     * @param attachments
     *            set of attachments assigned to a note
     * @param attachmentIds
     *            array of attachment IDs that should be added to the note
     * @return the cleaned attachmentIds array which only contains the IDs of attachments which are
     *         not yet assigned to the note
     */
    private Long[] cleanAssignedAttachments(Set<Attachment> attachments, Long[] attachmentIds) {
        if (attachments.size() > 0) {
            // remove all attachments whose IDs are not in attachmentIds
            Set<Long> attachmentIdSet = new HashSet<>(Arrays.asList(attachmentIds));
            Iterator<Attachment> it = attachments.iterator();
            while (it.hasNext()) {
                Attachment attachment = it.next();
                if (attachmentIdSet.contains(attachment.getId())) {
                    // remove existing IDs to avoid re-adding an attachment
                    attachmentIdSet.remove(attachment.getId());
                } else {
                    it.remove();
                    internalDeleteAttachment(attachment);
                }
            }
            attachmentIds = attachmentIdSet.toArray(new Long[attachmentIdSet.size()]);
        }
        return attachmentIds;
    }

    /**
     * Load and convert a note with the provided converter into the target list item.
     *
     * @param <T>
     *            the type of the list item
     * @param noteId
     *            the ID of the note
     * @param converter
     *            the converter to use
     * @param target
     *            the target object to fill
     * @throws NoteNotFoundException
     *             in case there is no note with the given ID or the convertion failed
     * @throws AuthorizationException
     *             in case the current user has no read access to the note
     */
    private <T extends NoteData> void convertNote(Long noteId,
            QueryResultConverter<SimpleNoteListItem, T> converter, T target)
            throws NoteNotFoundException, AuthorizationException {
        Note note = noteDao.load(noteId);
        if (note == null) {
            throw new NoteNotFoundException("The Note was not found. noteId=" + noteId);
        }
        assertReadAccessOnNote(note);
        Content content = note.getContent();
        if (content == null) {
            throw new NoteNotFoundException("The Note Content was not found. noteId=" + noteId);
        }

        if (!converter
                .convert(new SimpleNoteListItem(note.getId(), note.getCreationDate()), target)) {
            throw new NoteNotFoundException("The note convertion failed. noteId=" + noteId);
        }
    }

    @Override
    public void correctTopicOfComment(Long noteId) {
        Note comment = noteDao.load(noteId);
        if (comment.getParent() == null) {
            return;
        }
        // load discussion root note from DB to avoid cache effects in clustered environments
        Note discussionRoot = noteDao.forceLoad(comment.getDiscussionId());
        if (!discussionRoot.getBlog().getId().equals(comment.getBlog().getId())) {
            comment.setBlog(discussionRoot.getBlog());
            updateFollowableItems(noteId, false);
        }
    }

    /**
     * Creates or updates the content of a note.
     *
     * @param note
     *            the note
     * @param newContent
     *            the new content to set
     * @param createShortenedContent
     *            whether to calculate the shortened content
     * @throws NoteStoringPreProcessorException
     *             if the content cannot be shortened
     */
    private void createOrUpdateContent(Note note, String newContent, boolean createShortenedContent)
            throws NoteStoringPreProcessorException {
        Content content;
        if (note.getId() == null) {
            content = Content.Factory.newInstance();
            content.setContent(newContent);
            contentDao.create(content);
            note.setContent(content);
        } else {
            content = note.getContent();
            if (!content.getContent().equals(newContent)) {
                content.setContent(newContent);
            }
        }
        if (createShortenedContent) {
            NoteShortener s = new NoteShortener();
            String shortenedContent = s.processNoteContent(newContent);
            content.setShortContent(shortenedContent);
        }
    }

    /**
     * Evaluates the users to be notified stored in the storing TO and the additional blogs for
     * existence and read write access. Results are stored in a modification result description.
     *
     * @param storingTO
     *            the storing transfer object
     * @param targetBlog
     *            the original target blog for note creation
     * @param blog2users
     *            is used to store the users with read access for each found blog.
     * @return a result object with status set to SUCCESS if no problems or only warnings occurred
     */
    private NoteModificationResult evalTargets(NoteStoringTO storingTO, Blog targetBlog,
            Map<Blog, Collection<User>> blog2users) {
        NoteModificationResult result = prepareModificationResult();
        setMentionsFlags(storingTO);
        Collection<User> usersToNotify = resolveUserAliases(storingTO.getUsersToNotify(),
                result.getUserNotificationResult());
        Collection<String> uninformableUsers = result.getUserNotificationResult()
                .getUninformableUsers();
        blog2users.put(targetBlog,
                getUsersWithReadAccess(usersToNotify, targetBlog, uninformableUsers));
        result.setDirect(storingTO.isIsDirectMessage());
        Collection<Blog> targetBlogs = resolveBlogNameIds(storingTO.getAdditionalBlogs(), result,
                targetBlog.getId(), storingTO.getCreatorId());
        if (storingTO.isIsDirectMessage() && storingTO.getUsersNotToNotify() != null) {
            uninformableUsers.addAll(storingTO.getUsersNotToNotify());
        } else {
            for (Blog blog : targetBlogs) {
                Collection<String> tmpUninformableUsers = new HashSet<>();
                blog2users.put(blog,
                        getUsersWithReadAccess(usersToNotify, blog, tmpUninformableUsers));
                Iterator<String> it = uninformableUsers.iterator();
                while (it.hasNext()) {
                    String userAlias = it.next();
                    if (!tmpUninformableUsers.contains(userAlias)) {
                        it.remove();
                    }
                }
            }
        }
        // check if we should stop or treat unresolvables as warnings
        NoteStoringFailDefinition failDef = storingTO.getFailDefinition();
        if (failDef.isFailOnUnresolvableBlogs() && result.getUnresolvableBlogs().size() > 0
                || failDef.isFailOnMissingBlogWriteAccess()
                && result.getUnwritableBlogs().size() > 0) {
            result.setStatus(NoteModificationStatus.CROSSPOST_ERROR);
        } else if (failDef.isFailOnUnresolvableUsers()
                && result.getUserNotificationResult().getUnresolvableUsers().size() > 0
                || failDef.isFailOnUninformableUser() && uninformableUsers.size() > 0) {
            result.setStatus(NoteModificationStatus.NOTIFICATION_ERROR);
        } else {
            result.setStatus(NoteModificationStatus.SUCCESS);
        }
        return result;
    }

    /**
     * Finds a previous autosave that will be modified or published.
     *
     * @param storingTO
     *            the TO describing the note modification request
     * @param originalNoteId
     *            the ID of the note to be edited in case of an edit, otherwise null
     * @param parentNoteId
     *            the ID of the parent note in case of a reply, otherwise null
     * @return the autosaved note or null
     */
    private Note extractAutosave(NoteStoringTO storingTO, Long originalNoteId, Long parentNoteId) {
        Note autosave = null;
        if (storingTO.getAutosaveNoteId() != null) {
            autosave = noteDao.load(storingTO.getAutosaveNoteId());
            if (autosave != null && NoteStatus.AUTOSAVED.equals(autosave.getStatus())) {
                return autosave;
            } else {
                // if the autosave already exists but is published force creation of new autosave
                autosave = null;
            }
        }
        if (!storingTO.isPublish()) {
            // the note will be stored as autosave, thus assure there is only one autosave
            Long asId = noteDao.getAutosave(
                    storingTO.getCreatorId(),
                    originalNoteId,
                    parentNoteId,
                    getAutosavePropertyFilters(originalNoteId, parentNoteId,
                            storingTO.getProperties()));
            if (asId != null) {
                autosave = noteDao.load(asId);
            }
        }

        return autosave;
    }

    @Override
    @Transactional(readOnly = true)
    public AutosaveNoteData getAutosave(Long noteId, Long parentNoteId,
            Collection<StringPropertyTO> properties, Locale locale) {
        Long currentUserId = SecurityHelper.assertCurrentUserId();
        if (noteId != null && parentNoteId == null) {
            // in case the autosave refers to an edit of a reply we must pass
            // both params to the DAO
            // to find the correct autosave, thus check whether the note to edit
            // is a reply
            Note note = noteDao.load(noteId);
            if (note != null && note.getParent() != null) {
                parentNoteId = note.getParent().getId();
            }
        }
        Long autosaveId = noteDao.getAutosave(currentUserId, noteId, parentNoteId,
                getAutosavePropertyFilters(noteId, parentNoteId, properties));
        if (autosaveId != null) {
            try {
                SimpleNoteListItemToAutosaveNoteDataConverter converter = new SimpleNoteListItemToAutosaveNoteDataConverter(
                        AutosaveNoteData.class, locale);
                AutosaveNoteData target = new AutosaveNoteData();
                convertNote(autosaveId, converter, target);
                return target;
            } catch (NoteNotFoundException e) {
                // do nothing
            } catch (AuthorizationException e) {
                // user does not have read access to the blog anymore: just
                // return nothing but log
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("No access to autosave (" + autosaveId
                            + ") because blog read right for user " + currentUserId
                            + " was removed.");
                }
            }
        }
        return null;
    }

    private Collection<StringPropertyFilter> getAutosavePropertyFilters(Long noteId,
            Long parentNoteId, Collection<StringPropertyTO> properties) {
        if (noteId != null) {
            return autosavePropertyFilterProviderManager.getFiltersForUpdate(noteId, properties);
        } else if (parentNoteId != null) {
            return autosavePropertyFilterProviderManager.getFiltersForComment(parentNoteId,
                    properties);
        }
        return autosavePropertyFilterProviderManager.getFiltersForCreate(properties);
    }

    /**
     * Returns the blog with the name identifier blogNameId or null if no such blog exists.
     *
     * @param blogNameId
     *            the name identifier / alias of the blog
     * @return the blog
     */
    private Blog getBlogFromBlogNameId(String blogNameId) {
        // interpret string as name identifier
        Blog blog = blogDao.findByNameIdentifier(blogNameId);
        if (blog == null) {
            LOGGER.debug("A blog with the name identifier {} was not found.", blogNameId);
        }
        return blog;
    }

    @Override
    @Transactional(readOnly = true)
    public NoteData getNote(long noteId, NoteRenderContext renderContext)
            throws NoteNotFoundException, AuthorizationException {
        NoteData target = new NoteData();
        SimpleNoteListItemToNoteDataQueryResultConverter<NoteData> converter = new SimpleNoteListItemToNoteDataQueryResultConverter<>(
                NoteData.class, renderContext);
        convertNote(noteId, converter, target);
        return target;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public <T> T getNote(Long noteId, Converter<Note, T> converter) {
        Note note;
        // TODO should throw NoteNotFoundException
        if (noteId == null || (note = noteDao.load(noteId)) == null) {
            return null;
        }
        // TODO should check for read access
        return converter.convert(note);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public long getNoteCount() {
        return noteDao.getNotesCount();
    }

    /**
     * Get the tag parser.
     *
     * @return The tag parser to use
     */
    private TagParser getTagParser() {
        if (tagParser == null) {
            tagParser = TagParserFactory.instance().getDefaultTagParser();
        }
        return tagParser;
    }

    /**
     * Returns all users with read access to a given blog.
     *
     * @param users
     *            the users to test for read access
     * @param blog
     *            the blog
     * @param uninformableUsers
     *            stores the aliases of the users that do not have read access; can be null
     * @return return a collection of users with read access
     */
    private Collection<User> getUsersWithReadAccess(Collection<User> users, Blog blog,
            Collection<String> uninformableUsers) {
        ArrayList<User> informableUsers = new ArrayList<>();
        for (User u : users) {
            if (topicRightsManagement.userHasReadAccess(blog.getId(), u.getId(), false)) {
                informableUsers.add(u);
            } else if (uninformableUsers != null) {
                uninformableUsers.add(u.getAlias());
            }
        }
        return informableUsers;
    }

    @Override
    protected NoteModificationResult handleCreateNote(NoteStoringTO noteStoringTO,
            Set<String> additionalBlogIds) throws BlogNotFoundException,
            NoteManagementAuthorizationException, NoteStoringPreProcessorException {
        Blog blog = blogDao.load(noteStoringTO.getBlogId());
        assertCreateNotePrecondition(noteStoringTO, blog);
        // TODO assert that current user is internal system or the user of TO, otherwise throw
        // Exception
        assertHasWriteAccess(blog, noteStoringTO.getCreatorId());
        // No crosspost for comments.
        if (noteStoringTO.getParentNoteId() == null) {
            mergeAdditionalBlogAliases(noteStoringTO, additionalBlogIds);
        } else {
            noteStoringTO.setAdditionalBlogs(null);
        }

        noteContentProcessorManagement.process(noteStoringTO);
        Map<Blog, Collection<User>> blog2users = new HashMap<>();
        NoteModificationResult result = evalTargets(noteStoringTO, blog, blog2users);
        if (!result.getStatus().equals(NoteModificationStatus.SUCCESS)) {
            return result;
        }

        assertValidDirectMessage(noteStoringTO, result, blog2users);

        Note autosave = extractAutosave(noteStoringTO, null, noteStoringTO.getParentNoteId());
        if (autosave != null) {
            if (noteStoringTO.getParentNoteId() != null) {
                blog = autosave.getBlog();
            }
            // set the possibly new blog to assure only one autosave on create
            // action (and not one per blog)
            result = internalUpdateWithCrossposts(autosave, noteStoringTO, blog, blog2users, null,
                    result);
        } else {
            Note parentNote = null;
            if (noteStoringTO.getParentNoteId() != null) {
                parentNote = noteDao.load(noteStoringTO.getParentNoteId());
            }
            result = internalCreateWithCrossposts(noteStoringTO, blog, parentNote, null,
                    blog2users, null, result);
        }
        // TODO invalidating the cache here is error prone as the transaction is not yet committed.
        // Should fire an event in the NoteService and handle it in the provider instead.
        if (noteStoringTO.isPublish() && NoteModificationStatus.SUCCESS.equals(result.getStatus())) {
            // invalidate the cache holding the n used blogs, the maxResults is not
            // important
            cacheManager.getCache().invalidate(
                    new UsedBlogsCacheKey(0, noteStoringTO.getCreatorId()),
                    UsedBlogsCacheKey.LAST_USED_BLOGS_PROVIDER);
            cacheManager.getCache().invalidate(
                    new UsedBlogsCacheKey(0, noteStoringTO.getCreatorId()),
                    UsedBlogsCacheKey.MOST_USED_BLOGS_PROVIDER);
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void handleDeleteAutosave(Long postId) throws NoteManagementAuthorizationException {
        Note note = noteDao.load(postId);
        if (note == null) {
            return;
        }
        // only author may delete post
        Long userId = SecurityHelper.getCurrentUserId();
        if (!note.getUser().getId().equals(userId)) {
            throw new NoteManagementAuthorizationException("The user with id " + userId
                    + " is not allowed to delete the draft with id " + postId, note.getBlog()
                    .getTitle());
        }
        if (!NoteStatus.PUBLISHED.equals(note.getStatus())) {
            note.getTags().clear();
            note.getUsersToBeNotified().clear();
            note.setOrigin(null);
            if (note.getCrosspostBlogs() != null) {
                note.getCrosspostBlogs().clear();
            }
            noteDao.remove(note);
            contentDao.remove(note.getContent());
            internalDeleteAttachments(note.getAttachments());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void handleDeleteNote(Long postId, boolean deleteSystemPosts,
            boolean clientManagerCanDelete) throws NoteManagementAuthorizationException {
        Note note = noteDao.load(postId);
        if (note == null) {
            return;
        }

        SecurityContext currentSecurityContext = null;
        boolean switchedAuth = false;
        try {
            Long userId = SecurityHelper.getCurrentUserId();
            if (!SecurityHelper.isInternalSystem()) {
                boolean isClientManager = SecurityHelper.isClientManager();
                boolean isTopicManager = topicRightsManagement.userHasManagementAccess(note
                        .getBlog().getId(), userId);
                if ((!clientManagerCanDelete || !isClientManager) && !isTopicManager) {
                    if (!note.getUser().getId().equals(userId)) {
                        throw new AccessDeniedException("The user with id " + userId
                                + " is not allowed to delete the post with id " + postId);
                    }
                    if (discussionDetailsRetriever.getReplyCount(note) > 0) {
                        throw new AccessDeniedException("The user with id " + userId
                                + " is not allowed to delete the commented post with id " + postId);
                    }
                }

                // change the security context in case it is a different user (e.g. the topic
                // manager)
                // deleting the note
                switchedAuth = true;
                currentSecurityContext = AuthenticationHelper.setInternalSystemToSecurityContext();

            }
            if (NoteCreationSource.SYSTEM.equals(note.getCreationSource()) && !deleteSystemPosts) {
                throw new NoteManagementAuthorizationException(
                        "The creation source of this post is '" + note.getCreationSource()
                                + "'. The user with id " + userId
                                + " is not allowed to delete the post with id " + postId, note
                                .getBlog().getTitle());
            }
            internalDeleteNoteWithReplies(note);

        } finally {
            if (switchedAuth) {
                AuthenticationHelper.setSecurityContext(currentSecurityContext);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Set<Long> handleDeleteNotesOfUser(Long userId) throws AuthorizationException {
        if (!SecurityHelper.isInternalSystem()
                && !SecurityHelper.assertCurrentUserId().equals(userId)
                && !SecurityHelper.isClientManager()) {
            throw new AuthorizationException("The current user with ID "
                    + SecurityHelper.getCurrentUserId()
                    + " is not allowed to delete the resources of user with ID " + userId);
        }

        SecurityContext currentSecurityContext = null;
        boolean switchedAuth = false;

        // collect discussion IDs for invalidation
        final Set<Long> discussionIds = new HashSet<>();
        final Set<Long> changedDiscussions = new HashSet<>();
        final Set<Long> deletedNotes = new HashSet<>();
        try {
            if (!SecurityHelper.isInternalSystem()) {
                // change the security context in case it is a different user (e.g. the client
                // manager) deleting the note
                switchedAuth = true;
                currentSecurityContext = AuthenticationHelper.setInternalSystemToSecurityContext();
            }
            // TODO better user the client locale?
            Locale locale = userDao.load(userId).getLanguageLocale();
            // TODO might lead to OOM exception!
            Collection<Note> notes = noteDao.getNotesOfUser(userId);

            for (Note note : notes) {
                NoteStatus status = note.getStatus();
                Long discussionId = note.getDiscussionId();
                int oldSize = deletedNotes.size();
                internalDeleteOrAnonymizeNoteOfUser(note, deletedNotes, locale);
                if (oldSize == deletedNotes.size() && NoteStatus.PUBLISHED.equals(status)) {
                    discussionIds.add(discussionId);
                } else {
                    // The note was deleted and not anonymized.
                    changedDiscussions.add(discussionId);
                }
            }
        } finally {
            if (switchedAuth) {
                AuthenticationHelper.setSecurityContext(currentSecurityContext);
            }
        }
        return changedDiscussions;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<SimpleNoteListItem> handleGetCommentsOfDiscussion(Long noteId)
            throws NoteNotFoundException {
        User user;
        if (SecurityHelper.isPublicUser() || SecurityHelper.isInternalSystem()) {
            user = null;
        } else {
            user = SecurityHelper.assertCurrentKenmeiUser();
        }
        Note note = noteDao.load(noteId);
        if (note == null) {
            throw new NoteNotFoundException("The Note was not found. noteId=" + noteId);
        }
        return discussionDetailsRetriever.getCommentsInDiscussion(note.getDiscussionId(), user);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Long handleGetDiscussionId(Long noteId) throws NoteNotFoundException {
        Note note = noteDao.load(noteId);
        if (note == null) {
            throw new NoteNotFoundException("The Note was not found. noteId=" + noteId);
        }
        return note.getDiscussionId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected DiscussionNoteData handleGetNoteWithComments(Long noteId,
            QueryResultConverter<SimpleNoteListItem, DiscussionNoteData> converter)
                    throws NoteNotFoundException, AuthorizationException {
        DiscussionNoteData result = new DiscussionNoteData();
        convertNote(noteId, converter, result);
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected int handleGetNumberOfNotesInDiscussion(Long noteId) throws NoteNotFoundException {
        User user;
        if (SecurityHelper.isPublicUser() || SecurityHelper.isInternalSystem()) {
            user = null;
        } else {
            user = SecurityHelper.assertCurrentKenmeiUser();
        }
        Note note = noteDao.load(noteId);
        if (note == null) {
            throw new NoteNotFoundException("The Note was not found. noteId=" + noteId);
        }
        return discussionDetailsRetriever.getDiscussionNoteCount(note.getDiscussionId(), user);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected int handleGetNumberOfReplies(Long noteId) throws NoteNotFoundException {
        Note note = noteDao.load(noteId);

        if (note == null) {
            throw new NoteNotFoundException("Bote with ID " + noteId + " does not exist");
        }
        return discussionDetailsRetriever.getReplyCount(note);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected NoteModificationResult handleUpdateNote(NoteStoringTO noteStoringTO, Long noteId,
            Set<String> additionalBlogIds, boolean resendNotifications)
            throws BlogNotFoundException, NoteNotFoundException,
            NoteManagementAuthorizationException, NoteStoringPreProcessorException {

        Note noteToEdit = noteDao.load(noteId);
        assertNoteToEditForUpdateNote(noteStoringTO, noteToEdit);
        assertHasWriteAccess(noteToEdit.getBlog(), noteStoringTO.getCreatorId());

        Note parent = noteToEdit.getParent();
        if (parent != null) {
            noteStoringTO.setParentNoteId(parent.getId());
        }
        mergeAdditionalBlogAliases(noteStoringTO, additionalBlogIds);
        noteStoringTO.setIsDirectMessage(noteToEdit.isDirect());
        noteContentProcessorManagement.process(noteStoringTO);

        if (noteStoringTO.isPublish() && !noteToEdit.isDirect()
                && noteStoringTO.isIsDirectMessage()) {
            throw new DirectMessageConversionException(
                    "It is not allowed to convert a note into a direct message. Note ID is '"
                            + noteToEdit.getId());
        }
        Map<Blog, Collection<User>> blog2users = new HashMap<>();
        NoteModificationResult result = evalTargets(noteStoringTO, noteToEdit.getBlog(), blog2users);
        if (!result.getStatus().equals(NoteModificationStatus.SUCCESS)) {
            return result;
        }
        assertValidDirectMessage(noteStoringTO, result, blog2users);
        Collection<User> usersToIgnore = null;
        if (noteStoringTO.isSendNotifications() && !resendNotifications
                && noteToEdit.getUsersToBeNotified() != null) {
            usersToIgnore = new HashSet<>();
            for (User user : noteToEdit.getUsersToBeNotified()) {
                usersToIgnore.add(user);
            }
        }
        // check whether the noteToEdit is a reply
        Long parentId = noteToEdit.getParent() != null ? noteToEdit.getParent().getId() : null;
        Note autosave = extractAutosave(noteStoringTO, noteId, parentId);
        if (autosave == null) {
            if (noteStoringTO.isPublish()) {
                result = internalUpdateWithCrossposts(noteToEdit, noteStoringTO, null, blog2users,
                        usersToIgnore, result);
            } else {
                result = internalCreateWithCrossposts(noteStoringTO, noteToEdit.getBlog(),
                        noteToEdit.getParent(), noteToEdit, blog2users, usersToIgnore, result);
            }
        } else {
            if (noteStoringTO.isPublish()) {
                // remove attachments from draft to allow adding them to the
                // note to update
                for (Attachment attach : autosave.getAttachments()) {
                    attach.setNote(null);
                }
                autosave.getAttachments().clear();
                result = internalUpdateWithCrossposts(noteToEdit, noteStoringTO, null, blog2users,
                        usersToIgnore, result);
            } else {
                result = internalUpdateWithCrossposts(autosave, noteStoringTO, null, blog2users,
                        usersToIgnore, result);
            }
        }
        if (result.getStatus().equals(NoteModificationStatus.SUCCESS) && noteStoringTO.isPublish()
                && autosave != null) {
            this.handleDeleteAutosave(autosave.getId());
        }
        return result;
    }

    /**
     * internal init
     */
    @PostConstruct
    private void init() {
        discussionDetailsRetriever = new DiscussionDetailsRetriever(this.noteDao);
        eventDispatcher.register(discussionDetailsRetriever);
    }

    /**
     * Creates the crossposts for a note. In case of an autosave only the crosspost blogs will be
     * associated with the note.
     *
     * @param sourceNote
     *            the note for which the crossposts should be created
     * @param storingTO
     *            the TO holding the data for creating the crossposts
     * @param blog2users
     *            mapping from blogs to users to be notified. Contains all crosspost blogs and can
     *            contain the blog of srcNote which will be skipped.
     * @param createdNotes
     *            used to collect notes for which user notifications should be sent, can be null
     * @param lastModificationDate
     *            the modification date to be set for the notes
     * @return Collection of tags which hat problems while creation.
     * @throws NoteLimitReachedException
     *             if the limit for notes was reached
     * @throws NoteStoringPreProcessorException
     *             if the content cannot be shortened
     * @throws AttachmentAlreadyAssignedException
     *             in case an attachment is already assigned to another note created by the same
     *             user
     * @throws NoteManagementAuthorizationException
     *             should not occur
     * @throws NoteNotFoundException
     *             should not occur
     */
    private Collection<String> internalCreateCrosspostsForNote(Note sourceNote,
            NoteStoringTO storingTO, Map<Blog, Collection<User>> blog2users,
            Collection<Note> createdNotes, Timestamp lastModificationDate)
            throws NoteLimitReachedException, NoteStoringPreProcessorException,
            AttachmentAlreadyAssignedException, NoteManagementAuthorizationException,
            NoteNotFoundException {
        Long blogIdToSkip = sourceNote.getBlog().getId();
        Collection<String> tagsWithProblems = new HashSet<>();
        if (storingTO.isPublish()) {
            for (Blog blog : blog2users.keySet()) {
                if (!blog.getId().equals(blogIdToSkip)) {
                    Collection<User> usersToNotify = blog2users.get(blog);
                    try {
                        storingTO.setAttachmentIds(AttachmentHelper.copyAttachments(sourceNote
                                .getAttachments()));
                    } catch (AuthorizationException e) {
                        LOGGER.error("Unexpected exception while copying the attachments", e);
                        throw new NoteManagementAuthorizationException(
                                "Unexpected exception while copying the attachments",
                                blog.getTitle());
                    }
                    Pair<Note, List<String>> noteResult = internalCreateNewNote(storingTO, blog,
                            null, null, usersToNotify, sourceNote.getCreationDate(),
                            lastModificationDate);
                    tagsWithProblems.addAll(noteResult.getRight());

                    createdNotes.add(noteResult.getLeft());

                }
            }
        } else {
            sourceNote.getCrosspostBlogs().clear();
            // add new crosspostblogs
            for (Blog blog : blog2users.keySet()) {
                if (!blog.getId().equals(blogIdToSkip)) {
                    sourceNote.getCrosspostBlogs().add(blog);
                }
            }
        }
        return tagsWithProblems;
    }

    /**
     * Internal method to create a new note.
     *
     * @param storingTO
     *            TO containing all data required for note creation/update
     * @param blog
     *            identifies the blog into which the post should be created
     * @param parent
     *            the parent user tagged post or null if not a comment
     * @param originalNote
     *            in case of creating an autosave of an edit this note refers to the original note
     *            to be edited, otherwise has to be null
     * @param usersToNotify
     *            collection of users that will be added as notification targets
     * @param creationDate
     *            The creation date to set for the note.
     * @param lastModificationDate
     *            The last modification date to set for the note.
     * @return the created note and list of tags with problems.
     * @throws NoteLimitReachedException
     *             if the limit of UTPs was reached
     * @throws NoteStoringPreProcessorException
     *             if one of the preprocessors failed
     * @throws AttachmentAlreadyAssignedException
     *             in case an attachment is already assigned to another note created by the same
     *             user
     * @throws NoteManagementAuthorizationException
     *             should not occur
     * @throws NoteNotFoundException
     *             should not occur
     */
    private Pair<Note, List<String>> internalCreateNewNote(NoteStoringTO storingTO, Blog blog,
            Note parent, Note originalNote, Collection<User> usersToNotify, Timestamp creationDate,
            Timestamp lastModificationDate) throws NoteLimitReachedException,
            NoteStoringPreProcessorException, AttachmentAlreadyAssignedException,
            NoteManagementAuthorizationException, NoteNotFoundException {
        if (!checkNoteLimit(1)) {
            throw new NoteLimitReachedException(
                    "The limit for the user tagged items would be reached if the items would be added, limit is: "
                            + ResourceStoringHelper.getCountLimit());
        }
        Note note = Note.Factory.newInstance();
        note.setUser(userDao.load(storingTO.getCreatorId()));
        note.setParent(parent);
        note.setCreationSource(storingTO.getCreationSource());
        // add blog relation
        note.setBlog(blog);
        List<String> tagsWithProblems = updateTags(storingTO, note);
        createOrUpdateContent(note, storingTO.getContent(), storingTO.isPublish());
        note.setLastModificationDate(lastModificationDate);
        note.setCreationDate(creationDate);
        note.getUsersToBeNotified().addAll(usersToNotify);
        note.setDirect(storingTO.isIsDirectMessage());
        note.setMentionTopicAuthors(storingTO.isMentionTopicAuthors());
        note.setMentionTopicManagers(storingTO.isMentionTopicManagers());
        note.setMentionTopicReaders(storingTO.isMentionTopicReaders());
        note.setMentionDiscussionAuthors(storingTO.isMentionDiscussionAuthors());
        // set the direct users/messages
        internalSetDirectUsers(note);
        note.setVersion(storingTO.getVersion() == null ? 0L : storingTO.getVersion());
        if (storingTO.isPublish()) {
            note.setStatus(NoteStatus.PUBLISHED);
        } else {
            note.setStatus(NoteStatus.AUTOSAVED);
            note.setOrigin(originalNote);
            // make copies of the attachments of the origin
            if (originalNote != null && originalNote.getAttachments() != null) {
                note.setAttachments(new HashSet<Attachment>());
                Set<Attachment> originalAttachments = new HashSet<>();
                for (Attachment attach : originalNote.getAttachments()) {
                    Attachment copy;
                    try {
                        copy = resourceStoringManagement.storeCopyOfAttachment(attach);
                    } catch (AuthorizationException e) {
                        LOGGER.error("Unexpected exception while copying the attachments.", e);
                        throw new NoteManagementAuthorizationException(
                                "Unexpected exception while copying the attachments.",
                                blog.getTitle());
                    }
                    originalAttachments.add(copy);
                    copy.setNote(originalNote);
                    // disconnect the attachment from the originalNote,
                    // updateAttachments will
                    // connect it with the autosave
                    attach.setNote(null);
                }
                originalNote.getAttachments().clear();
                originalNote.getAttachments().addAll(originalAttachments);
            }
        }

        // create new note, must be done before update attachments, otherwise we
        // get transient object exceptions (note not saved)
        note = noteDao.create(note);
        updateProperties(note, storingTO);
        updateAttachments(note, storingTO.getAttachmentIds(), storingTO.getCreatorId());
        if (parent != null) {
            parent.getChildren().add(note);
            note.setDiscussionPath(parent.getDiscussionPath() + note.getId() + "/");
            note.setDiscussionId(parent.getDiscussionId());
        } else {
            note.setDiscussionPath("/" + note.getId() + "/");
            note.setDiscussionId(note.getId());
            note.setLastDiscussionNoteCreationDate(creationDate);
        }

        noteDao.updateFollowableItems(note, false);
        return new Pair<>(note, tagsWithProblems);
    }

    /**
     * Creates a new note / autosave with all required crosspost copies and sends the notifications.
     *
     * @param storingTO
     *            the data used for note creation / modification
     * @param targetBlog
     *            the target blog for note creation
     * @param parentNote
     *            the parent note the new note should be a child of or null
     * @param originalNote
     *            the note the autosave should relate to
     * @param blog2users
     *            map containing all blogs in which the post will be created (this includes the
     *            targetblog ) and the associated users to be added as notification targets. Whether
     *            the users actually receive notifications depends on the isSendNotifications flag
     *            of the post storing TO and the usersToIgnore black list
     * @param usersToIgnore
     *            a collection of users to be excluded from sending notifications, can be null
     * @param modificationResult
     *            the current modification status which will be updated and returned
     * @return whether the modification was successful
     *
     */
    private NoteModificationResult internalCreateWithCrossposts(NoteStoringTO storingTO,
            Blog targetBlog, Note parentNote, Note originalNote,
            Map<Blog, Collection<User>> blog2users, Collection<User> usersToIgnore,
            NoteModificationResult modificationResult) {
        NoteModificationStatus status = NoteModificationStatus.SUCCESS;
        Throwable errorCause = null;
        try {
            Collection<User> usersToNotify = blog2users.get(targetBlog);
            Timestamp creationDate = storingTO.getCreationDate();
            Pair<Note, List<String>> noteResult = internalCreateNewNote(storingTO, targetBlog,
                    parentNote, originalNote, usersToNotify, creationDate, creationDate);
            Note note = noteResult.getLeft();
            modificationResult.setNoteId(note.getId());
            modificationResult.setVersion(note.getVersion());
            modificationResult.getTagsWithProblems().addAll(noteResult.getRight());
            // storage for post processing
            Collection<Note> createdNotes = new ArrayList<>();
            if (storingTO.isPublish()) {
                createdNotes.add(note);
            }
            modificationResult.getTagsWithProblems().addAll(
                    internalCreateCrosspostsForNote(note, storingTO, blog2users, createdNotes,
                            creationDate));

            addNotesForPostProcessing(storingTO, usersToIgnore, createdNotes);
        } catch (NoteLimitReachedException e) {
            status = NoteModificationStatus.LIMIT_REACHED;
        } catch (NoteManagementAuthorizationException e) {
            status = NoteModificationStatus.SYSTEM_ERROR;
        } catch (NoteNotFoundException e) {
            status = NoteModificationStatus.SYSTEM_ERROR;
        } catch (NoteStoringPreProcessorException e) {
            LOGGER.error("A note storing pre-processor failed", e);
            status = NoteModificationStatus.SYSTEM_ERROR;
        } catch (ResourceStoringManagementException e) {
            status = NoteModificationStatus.MISSING_ATTACHMENT;
            LOGGER.error("Error storing the attachments of a note.", e);
        } catch (BlogManagementException e) {
            status = NoteModificationStatus.SYSTEM_ERROR;
            LOGGER.error("Error in note creation.", e);
        } catch (TaskAlreadyExistsException e) {
            status = NoteModificationStatus.SYSTEM_ERROR;
            LOGGER.error("Error while creating tasks for notifying users", e);
        } catch (AttachmentAlreadyAssignedException e) {
            status = NoteModificationStatus.SYSTEM_ERROR;
            LOGGER.debug(e.getMessage());
            errorCause = e;
        }
        modificationResult.setStatus(status);
        if (!modificationResult.getStatus().equals(NoteModificationStatus.SUCCESS)) {
            modificationResult.setErrorCause(errorCause);
            // force rollback without throwing exception
            TransactionInterceptor.currentTransactionStatus().setRollbackOnly();
        }
        return modificationResult;
    }

    /**
     * Delete an attachment of a note.
     *
     * @param attachment
     *            the attachment to delete
     */
    private void internalDeleteAttachment(Attachment attachment) {
        try {
            attachment.setNote(null);
            resourceStoringManagement.deleteAttachment(attachment.getId());
        } catch (AuthorizationException | AttachmentStillAssignedException e) {
            LOGGER.error("Unexpected exception while deleting attachments of a note", e);
            throw new NoteManagementException(
                    "Unexpected exception while deleting attachments of a note", e);
        }
    }

    /**
     * Delete the attachments of a note.
     *
     * @param attachments
     *            the attachments to delete
     */
    private void internalDeleteAttachments(Collection<Attachment> attachments) {
        if (attachments != null) {
            for (Attachment attach : attachments) {
                internalDeleteAttachment(attach);
            }
            attachments.clear();
        }
    }

    /**
     *
     * @param note
     *            the note
     */
    private void internalDeleteDirectUsers(Note note) {
        if (note.isDirect()) {
            // remove the direct users association
            note.getDirectUsers().clear();
        }
    }

    /**
     * Deletes a note with all replies.
     *
     * @param note
     *            the note to delete
     */
    private void internalDeleteNoteWithReplies(Note note) {
        internalDeleteDirectUsers(note);
        ServiceLocator.findService(UserNotePropertyDao.class).removePropertiesForNote(note.getId());
        ServiceLocator.findService(UserNoteEntityDao.class).removeUserNoteEntitiesForNote(
                note.getId());

        note.getTags().clear();
        note.getUsersToBeNotified().clear();
        note.setOrigin(null);
        note.setParent(null);
        note.getCrosspostBlogs().clear();
        note.getFavoriteUsers().clear();

        // Remove all versions (drafts) of this note.
        for (Note version : note.getVersionOf()) {
            internalDeleteNoteWithReplies(version);
        }
        // Delete all child notes.
        for (Note child : note.getChildren()) {
            internalDeleteNoteWithReplies(child);
        }

        internalDeleteAttachments(note.getAttachments());

        noteDao.remove(note);
        contentDao.remove(note.getContent());

    }

    /**
     * Deletes a note if it has no replies otherwise the note will be anonymized to preserve the
     * discussion.
     *
     * @param note
     *            the note to delete
     * @param deletedNotes
     *            store for saving the IDs of the deleted notes to avoid removing notes more than
     *            once
     * @param the
     *            locale to use for creating a message when anonymizing a note
     */
    private void internalDeleteOrAnonymizeNoteOfUser(Note note, Set<Long> deletedNotes,
            Locale locale) {
        // check for edit-drafts
        for (Note version : note.getVersionOf()) {
            internalDeleteOrAnonymizeNoteOfUser(version, deletedNotes, locale);
        }

        // clear general stuff of the note
        note.getTags().clear();
        note.getUsersToBeNotified().clear();
        note.getProperties().clear();
        internalDeleteAttachments(note.getAttachments());

        // remove previously deleted children
        // required for the following isEmpty check
        Iterator<Note> children = note.getChildren().iterator();
        while (children.hasNext()) {
            Note child = children.next();
            if (deletedNotes.contains(child.getId())) {
                children.remove();
            }
        }

        // delete if the note has no children
        // otherwise anonymize the message and preserve the discussion
        if (note.getChildren().isEmpty()) {
            internalDeleteNoteWithReplies(note);
            deletedNotes.add(note.getId());
        } else {
            note.getContent().setShortContent(null);
            note.getContent().setContent(
                    ResourceBundleManager.instance().getText("note.anonymize.message", locale));
            note.setLastModificationDate(new Timestamp(System.currentTimeMillis()));
        }
    }

    /**
     * Add notified users of a direct message to a special collection for faster lookup
     *
     * @param note
     *            the note to modify, if the note is not a direct note, it won't be modified
     */
    private void internalSetDirectUsers(Note note) {
        if (note.isDirect()) {
            Set<User> directUsers = new HashSet<>(note.getUsersToBeNotified());
            directUsers.add(note.getUser());
            note.setDirectUsers(directUsers);
        }
    }

    /**
     * Internal method to update the data of an existing note.
     *
     * @param note
     *            the existing note
     * @param noteStoringTO
     *            the TO holding the new data
     * @param targetBlog
     *            the new blog to set, if null the blog will not be changed
     * @param usersToNotify
     *            the users to notify to be added
     * @throws NoteStoringPreProcessorException
     *             if the content cannot be shortened
     * @return the modified note
     * @throws AttachmentAlreadyAssignedException
     *             in case an attachment is already assigned to another note created by the same
     *             user
     * @throws NoteManagementAuthorizationException
     *             should not occur
     * @throws NoteNotFoundException
     *             should not occur
     */
    private Pair<Timestamp, List<String>> internalUpdateNoteData(Note note,
            NoteStoringTO noteStoringTO, Blog targetBlog, Collection<User> usersToNotify)
                    throws NoteStoringPreProcessorException, AttachmentAlreadyAssignedException,
                    NoteManagementAuthorizationException, NoteNotFoundException {
        if (targetBlog != null) {
            note.setBlog(targetBlog);
        }

        // clear existing data
        internalDeleteDirectUsers(note);
        note.getTags().clear();
        note.getUsersToBeNotified().clear();
        // set new data
        List<String> tagsWithProblems = updateTags(noteStoringTO, note);
        createOrUpdateContent(note, noteStoringTO.getContent(), noteStoringTO.isPublish());
        updateAttachments(note, noteStoringTO.getAttachmentIds(), noteStoringTO.getCreatorId());
        note.setLastModificationDate(noteStoringTO.getCreationDate());
        note.getUsersToBeNotified().addAll(usersToNotify);
        note.setDirect(noteStoringTO.isIsDirectMessage());
        note.setMentionDiscussionAuthors(noteStoringTO.isMentionDiscussionAuthors());
        note.setMentionTopicAuthors(noteStoringTO.isMentionTopicAuthors());
        note.setMentionTopicReaders(noteStoringTO.isMentionTopicReaders());
        note.setMentionTopicManagers(noteStoringTO.isMentionTopicManagers());
        internalSetDirectUsers(note);
        note.getCrosspostBlogs().clear();
        // publish note if requested
        Timestamp lastModificationDate = note.getLastModificationDate();
        if (noteStoringTO.isPublish()) {
            // if the note changes from autosave to published set the new
            // creation date
            if (NoteStatus.AUTOSAVED.equals(note.getStatus())) {
                note.setCreationDate(note.getLastModificationDate());
                lastModificationDate = note.getCreationDate();
            }
            note.setStatus(NoteStatus.PUBLISHED);
        }
        updateProperties(note, noteStoringTO);
        // update version if higher
        Long v = note.getVersion();
        Long newVersion = noteStoringTO.getVersion() == null ? 0L : noteStoringTO.getVersion();
        if (v.compareTo(newVersion) < 0) {
            note.setVersion(newVersion);
        }
        noteDao.updateFollowableItems(note, true);
        return new Pair<>(lastModificationDate, tagsWithProblems);
    }

    /**
     * Internal method to update an existing and create crossposts if necessary.
     *
     * @param note
     *            the existing note
     * @param storingTO
     *            the TO holding the new data
     * @param targetBlog
     *            the new blog to set, if null the blog won't be changed
     * @param blog2users
     *            map containing all blogs in which the post will be created (this includes the
     *            targetblog ) and the associated users to be added as notification targets. Whether
     *            the users actually receive notifications depends on the isSendNotifications flag
     *            of the post storing TO and the usersToIgnore black list
     * @param usersToIgnore
     *            a collection of users to be excluded from sending notifications, can be null
     * @param modificationResult
     *            the current modification status which will be updated and returned
     * @return whether the modification was successful
     */
    private NoteModificationResult internalUpdateWithCrossposts(Note note, NoteStoringTO storingTO,
            Blog targetBlog, Map<Blog, Collection<User>> blog2users,
            Collection<User> usersToIgnore, NoteModificationResult modificationResult) {
        NoteModificationStatus status = NoteModificationStatus.SUCCESS;
        Throwable errorCause = null;
        if (targetBlog == null) {
            targetBlog = note.getBlog();
        }
        try {
            Collection<User> usersToNotify = blog2users.get(targetBlog);
            Pair<Timestamp, List<String>> noteResult = internalUpdateNoteData(note, storingTO,
                    targetBlog, usersToNotify);
            modificationResult.getTagsWithProblems().addAll(noteResult.getRight());
            modificationResult.setNoteId(note.getId());
            modificationResult.setVersion(note.getVersion());
            // storage for sending notifications
            Collection<Note> updatePosts = new ArrayList<>();
            if (storingTO.isPublish()) {
                updatePosts.add(note);
            }
            modificationResult.getTagsWithProblems().addAll(
                    internalCreateCrosspostsForNote(note, storingTO, blog2users, updatePosts,
                            noteResult.getLeft()));
            addNotesForPostProcessing(storingTO, usersToIgnore, updatePosts);
        } catch (NoteLimitReachedException e) {
            status = NoteModificationStatus.LIMIT_REACHED;
        } catch (NoteManagementAuthorizationException e) {
            status = NoteModificationStatus.SYSTEM_ERROR;
        } catch (NoteNotFoundException e) {
            status = NoteModificationStatus.SYSTEM_ERROR;
        } catch (NoteStoringPreProcessorException e) {
            LOGGER.error("A note storing pre-processor failed", e);
            status = NoteModificationStatus.SYSTEM_ERROR;
        } catch (ResourceStoringManagementException e) {
            status = NoteModificationStatus.SYSTEM_ERROR;
            LOGGER.error("Error while storing attachments of a note.", e);
        } catch (BlogManagementException e) {
            status = NoteModificationStatus.SYSTEM_ERROR;
            LOGGER.error("Error during update of a note.", e);
        } catch (TaskAlreadyExistsException e) {
            status = NoteModificationStatus.SYSTEM_ERROR;
            LOGGER.error("Error while creating tasks for notifying users", e);
        } catch (AttachmentAlreadyAssignedException e) {
            status = NoteModificationStatus.SYSTEM_ERROR;
            LOGGER.debug(e.getMessage());
            errorCause = e;
        }
        modificationResult.setStatus(status);
        if (!modificationResult.getStatus().equals(NoteModificationStatus.SUCCESS)) {
            modificationResult.setErrorCause(errorCause);
            // force rollback without throwing exception
            TransactionInterceptor.currentTransactionStatus().setRollbackOnly();
        }
        return modificationResult;
    }

    /**
     * Merge the set with additional blog aliases into the set of the TO with same purpose.
     *
     * @param storingTO
     *            the storing TO
     * @param additionalBlogAliases
     *            the aliases to add, can be null
     */
    private void mergeAdditionalBlogAliases(NoteStoringTO storingTO,
            Set<String> additionalBlogAliases) {
        if (storingTO.getAdditionalBlogs() == null) {
            storingTO.setAdditionalBlogs(new HashSet<String>());
        }
        if (additionalBlogAliases != null) {
            storingTO.getAdditionalBlogs().addAll(additionalBlogAliases);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void moveToTopic(Long discussionId, Long topicId)
            throws NoteManagementAuthorizationException {
        Note rootNote = noteDao.load(discussionId);
        if (rootNote.getParent() != null) {
            throw new MovingOfNonRootNotesNotAllowedException(discussionId);
        }
        Long currentUserId = SecurityHelper.assertCurrentUserId();
        if (!notePermissionManagement.hasPermission(discussionId,
                NotePermissionManagement.PERMISSION_MOVE)
                || !topicRightsManagement.userHasWriteAccess(topicId, currentUserId, false)) {
            throw new NoteManagementAuthorizationException(
                    "The current user is not allowed to move the given discussion.", null);
        }
        noteDao.moveToTopic(discussionId, blogDao.load(topicId));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean noteExists(Long noteId) {
        return noteDao.load(noteId) != null;
    }

    /**
     * Prepares the modification result.
     *
     * @return an initialized modification result object
     */
    private NoteModificationResult prepareModificationResult() {
        UserNotificationResult userNotificationResult = new UserNotificationResult(
                new ArrayList<String>(), new HashSet<String>());
        return new NoteModificationResult(NoteModificationStatus.SYSTEM_ERROR,
                new ArrayList<String>(), new ArrayList<String>(), -1L, 0L, false,
                userNotificationResult);
    }

    /**
     * Tries to resolve blog name identifiers to existing blogs and tests whether a given user has
     * write access to the resolved blog.
     *
     * @param blogNameIds
     *            a collection of blog name identifiers to check
     * @param modResult
     *            stores the unresolvable name IDs and the blogs to which the user has no write
     *            access
     * @param blogIdToIgnore
     *            a blog ID to ignore, to avoid double checking
     * @param userId
     *            the ID of the user for whom the write access is to be checkecd
     * @return a collection of resolved blogs to which user has write access
     */
    private Collection<Blog> resolveBlogNameIds(Collection<String> blogNameIds,
            NoteModificationResult modResult, Long blogIdToIgnore, Long userId) {
        Collection<Blog> blogs = new ArrayList<>();
        String localizedBlogTitle;

        if (blogNameIds == null) {
            return blogs;
        }
        for (String blogNameId : blogNameIds) {
            Blog crossPostBlog = getBlogFromBlogNameId(blogNameId);
            if (crossPostBlog != null) {
                if (!crossPostBlog.getId().equals(blogIdToIgnore)) {
                    try {
                        assertHasWriteAccess(crossPostBlog, userId);
                        blogs.add(crossPostBlog);
                    } catch (NoteManagementAuthorizationException e) {
                        localizedBlogTitle = crossPostBlog.getTitle();
                        modResult.getUnwritableBlogs().add(localizedBlogTitle);
                    }
                }
            } else {
                modResult.getUnresolvableBlogs().add(blogNameId);
            }
        }
        return blogs;
    }

    /**
     * Tries to resolve user aliases to existing users.
     *
     * @param usersToNotify
     *            List with strings of user IDs, email addresses or aliases
     * @param notifyResult
     *            used to store unresolvable aliases
     * @return the users found
     */
    private Collection<User> resolveUserAliases(Collection<String> usersToNotify,
            UserNotificationResult notifyResult) {
        if (CollectionUtils.isEmpty(usersToNotify)) {
            // nothing to do;
            return Collections.emptyList();
        }
        ArrayList<User> foundUsers = new ArrayList<>();
        for (String alias : usersToNotify) {
            User user = userManagement.findUserByAlias(alias);
            if (user != null && user.getStatus().equals(UserStatus.ACTIVE)) {
                foundUsers.add(user);
            } else {
                notifyResult.getUnresolvableUsers().add(alias);
            }
        }
        return foundUsers;
    }

    /**
     * Send limit reached email.
     *
     * @param count
     *            actual count.
     * @param limit
     *            limit.
     */
    private void sendLimitReachedEmail(long count, long limit) {
        List<User> clientManager = userManagement.findUsersByRole(
                UserRole.ROLE_KENMEI_CLIENT_MANAGER, UserStatus.ACTIVE);
        Map<Locale, Collection<User>> localizedUsers = UserManagementHelper
                .getUserByLocale(clientManager);
        for (Locale locale : localizedUsers.keySet()) {
            NoteLimitReachedMailMessage message = new NoteLimitReachedMailMessage(
                    localizedUsers.get(locale), locale, ClientHelper.getCurrentClientId(),
                    LimitHelper.getCountPercentAsString(count, limit),
                    LimitHelper.getCountLimitAsString(limit));
            mailManagement.sendMail(message);
        }
    }

    /**
     * Method to set the mentions flags.
     *
     * @param note
     *            The note to process.
     */
    private void setMentionsFlags(NoteStoringTO note) {
        boolean mentionTopicAuthors = note.getUsersToNotify().remove(
                NoteManagement.CONSTANT_MENTION_TOPIC_AUTHORS);
        boolean mentionTopicReaders = note.getUsersToNotify().remove(
                NoteManagement.CONSTANT_MENTION_TOPIC_READERS);
        boolean mentionDiscussion = note.getUsersToNotify().remove(
                NoteManagement.CONSTANT_MENTION_DISCUSSION_PARTICIPANTS);
        boolean mentionTopicManagers = note.getUsersToNotify().remove(
                NoteManagement.CONSTANT_MENTION_TOPIC_MANAGERS);
        // flag might already be set by pre-processors
        note.setMentionTopicManagers(mentionTopicManagers || note.isMentionTopicManagers());
        note.setMentionTopicReaders(mentionTopicReaders || note.isMentionTopicReaders());
        note.setMentionTopicAuthors(mentionTopicAuthors || note.isMentionTopicAuthors());
        note.setMentionDiscussionAuthors(mentionDiscussion || note.isMentionDiscussionAuthors());
        if (note.isIsDirectMessage() && note.isMentionTopicManagers()) {
            Collection<String> mappedUsers = topicRightsManagement.getMappedUsers(note.getBlogId(),
                    new CollectionConverter<UserToBlogRoleMapping, String>() {
                @Override
                public String convert(UserToBlogRoleMapping source) {
                    Long userId = source.getUserId();
                    User user = userDao.load(userId);
                    return user != null ? user.getAlias() : null;
                }
            }, BlogRole.MANAGER);
            note.getUsersToNotify().addAll(mappedUsers);
        }
    }

    /**
     * Updates the attachments of a note by adding all attachments (Attachment objects) referenced
     * by the IDs in the array. Attachments of the note which are not referenced by an element of
     * the array are removed from the note and DB.
     *
     * @param note
     *            the note to update
     * @param attachmentIds
     *            array of attachment IDs for which the associated attachments will be added to the
     *            note
     * @param creatorId
     *            ID of the user creating the note
     * @throws AttachmentAlreadyAssignedException
     *             in case an attachment is already assigned to another note created by the same
     *             user
     */
    private void updateAttachments(Note note, Long[] attachmentIds, Long creatorId)
            throws AttachmentAlreadyAssignedException {
        Set<Attachment> attachments = note.getAttachments();
        if (attachments == null) {
            attachments = new HashSet<>();
            note.setAttachments(attachments);
        }
        if (attachmentIds == null || attachmentIds.length == 0) {
            internalDeleteAttachments(attachments);
            return;
        }
        attachmentIds = cleanAssignedAttachments(attachments, attachmentIds);
        for (Long attachmentId : attachmentIds) {
            Attachment attachment = attachmentDao.load(attachmentId);
            if (attachment == null) {
                throw new ResourceStoringManagementException("Attachment with id " + attachmentId
                        + " cannot be resolved to an existing attachment");
            }
            if (canAddAttachment(note.getId(), attachment, creatorId)) {
                attachment.setStatus(AttachmentStatus.PUBLISHED);
                attachments.add(attachment);
                attachment.setNote(note);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateFollowableItems(Long noteId, boolean updateChildren) {
        noteDao.updateFollowableItems(noteDao.load(noteId), updateChildren);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateLastDiscussionCreationDate(Long noteId, Timestamp creationDate) {
        Note note = noteDao.load(noteId);
        if (note != null) {
            Note discussionNote = noteDao.load(note.getDiscussionId());
            discussionNote.setLastDiscussionNoteCreationDate(creationDate);
            noteDao.update(discussionNote);
        }
    }

    /**
     * Updates or creates the properties for the given note.
     *
     * @param note
     *            The note.
     * @param storingTO
     *            The note with the new properties.
     * @throws NoteManagementAuthorizationException
     *             should not occur
     * @throws NoteNotFoundException
     *             should not occur
     */
    private void updateProperties(Note note, NoteStoringTO storingTO)
            throws NoteManagementAuthorizationException, NoteNotFoundException {
        // remove all properties which are not set in the storingTO anymore by adding them with a
        // null value
        if (storingTO.getProperties() == null) {
            storingTO.setProperties(new HashSet<StringPropertyTO>());
        }
        PropertyHelper.nullifyMissingProperties(note.getProperties(), storingTO.getProperties());
        try {
            propertyManagement.setObjectProperties(PropertyType.NoteProperty, note.getId(),
                    storingTO.getProperties());
        } catch (NotFoundException e) {
            // should not occur within same transaction
            LOGGER.error("Unexpected exception while updating properties", e);
            throw new NoteNotFoundException("unexpected exception", e);
        } catch (AuthorizationException e) {
            // should not occur within same transaction
            LOGGER.error("Unexpected exception while updating properties", e);
            throw new NoteManagementAuthorizationException(
                    "Unexpected exception while updating properties", "");
        }
    }

    /**
     * Updates the tags of the final note from the target TO.
     *
     * @param sourceNoteStoringTO
     *            The source note containing the tags.
     * @param targetNote
     *            The target note.
     * @return List of tags with problems on creation.
     */
    private List<String> updateTags(NoteStoringTO sourceNoteStoringTO, Note targetNote) {
        Pair<List<Tag>, List<String>> tags = getTagParser().findOrCreateTags(
                sourceNoteStoringTO.getUnparsedTags());
        targetNote.getTags().addAll(tags.getLeft());
        for (TagTO tag : sourceNoteStoringTO.getTags()) {
            try {
                Tag storedTag = tagManagement.storeTag(tag);
                targetNote.getTags().add(storedTag);
            } catch (TagNotFoundException e) {
                tags.getRight().add(tag.getName());
                LOGGER.warn("A tag wasn't found or couldn't be created: {}, {}", tag.getName(),
                        e.getMessage());
            } catch (TagStoreNotFoundException e) {
                tags.getRight().add(tag.getName());
                LOGGER.warn("A tag wasn't found or couldn't be created: {} , {} ", tag.getName(),
                        e.getMessage());
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                // For KENMEI-4485
                tags.getRight().add(tag.getName());
                LOGGER.error("There was an error creating a tag: {}", tag.getName(), e);
            }
        }
        return tags.getRight();
    }
}
