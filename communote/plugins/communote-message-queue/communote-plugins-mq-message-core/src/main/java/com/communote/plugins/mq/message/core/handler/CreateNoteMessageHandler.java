package com.communote.plugins.mq.message.core.handler;

import java.sql.Timestamp;

import org.apache.commons.lang3.StringUtils;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.communote.common.converter.IdentityConverter;
import com.communote.plugins.activity.base.service.ActivityService;
import com.communote.plugins.mq.message.base.data.status.Status;
import com.communote.plugins.mq.message.base.handler.CommunoteMessageHandler;
import com.communote.plugins.mq.message.base.message.CommunoteReplyMessage;
import com.communote.plugins.mq.message.core.data.note.Note;
import com.communote.plugins.mq.message.core.data.topic.BaseTopic;
import com.communote.plugins.mq.message.core.data.user.BaseEntity;
import com.communote.plugins.mq.message.core.handler.exception.NoNoteSpecifiedException;
import com.communote.plugins.mq.message.core.message.note.CreateNoteMessage;
import com.communote.plugins.mq.message.core.util.ConverterUtils;
import com.communote.plugins.mq.message.core.util.TopicUtils;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.blog.BlogAccessException;
import com.communote.server.api.core.blog.BlogNotFoundException;
import com.communote.server.api.core.note.NoteContentType;
import com.communote.server.api.core.note.NoteStoringTO;
import com.communote.server.api.core.property.StringPropertyTO;
import com.communote.server.core.exception.ErrorCodes;
import com.communote.server.core.security.SecurityHelper;
import com.communote.server.core.user.UserManagement;
import com.communote.server.core.vo.blog.NoteModificationResult;
import com.communote.server.core.vo.blog.NoteModificationStatus;
import com.communote.server.model.note.NoteCreationSource;
import com.communote.server.model.user.User;
import com.communote.server.persistence.blog.CreateBlogPostHelper;
import com.communote.server.service.NoteService;

/**
 * Handler for creating notes. Supports the creation of activities.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
@Provides(specifications = CommunoteMessageHandler.class)
@Component
@Instantiate
public class CreateNoteMessageHandler extends CommunoteMessageHandler<CreateNoteMessage> {

    public static final String LATEST_VERSION = "1.0.0";

    private static final Logger LOGGER = LoggerFactory.getLogger(CreateNoteMessageHandler.class);

    private final SecurityHelperWrapper securityHelper = new SecurityHelperWrapperImpl();
    @Requires
    private ActivityService activityService;
    private NoteService noteManagement;
    private UserManagement userManagement;

    /**
     * convert the content type value to the Communote counterpart
     *
     * @param note
     *            the note to process
     * @return the content type
     */
    private NoteContentType convertContentType(Note note) {
        if (Note.CONTENT_TYPE_HTML.equals(note.getContentType())) {
            return NoteContentType.HTML;
        }
        if (Note.CONTENT_TYPE_PLAIN_TEXT.equals(note.getContentType())) {
            return NoteContentType.PLAIN_TEXT;
        }
        return NoteContentType.UNKNOWN;
    }

    /**
     * Create and fill a noteStoring transfer object for creating the note
     *
     * @param note
     *            the note POJO providing the details
     * @return the TO
     * @throws BlogNotFoundException
     *             in case topic does not exist
     * @throws BlogAccessException
     *             in case the current user has no access to the topic
     */
    private NoteStoringTO convertToNoteStoringTO(Note note) throws BlogNotFoundException,
    BlogAccessException {
        NoteStoringTO noteStoringTO = new NoteStoringTO();
        convertTopics(note, noteStoringTO);
        noteStoringTO.setParentNoteId(note.getParentNoteId());
        noteStoringTO.setContent(note.getContent());
        noteStoringTO.setContentType(convertContentType(note));
        noteStoringTO.setCreationSource(NoteCreationSource.MQ);
        noteStoringTO.setCreatorId(securityHelper.assertCurrentUserId());
        if (note.getCreationDate() != null) {
            noteStoringTO.setCreationDate(new Timestamp(note.getCreationDate().getTime()));
        }
        CreateBlogPostHelper.setDefaultFailLevel(noteStoringTO);
        noteStoringTO.setIsDirectMessage(note.isDirectMessage());
        noteStoringTO.setMentionDiscussionAuthors(note.isMentionDiscussionAuthors());
        noteStoringTO.setMentionTopicAuthors(note.isMentionTopicAuthors());
        noteStoringTO.setMentionTopicManagers(note.isMentionTopicManagers());
        noteStoringTO.setMentionTopicReaders(note.isMentionTopicReaders());
        ConverterUtils.convertArray(note.getProperties(), noteStoringTO.getProperties(),
                ConverterUtils.STRING_PROPERTYTO_CONVERTER);
        if (note.isActivity()) {
            // add the activity property
            StringPropertyTO property = new StringPropertyTO();
            property.setKeyGroup(ActivityService.PROPERTY_KEY_GROUP);
            property.setPropertyKey(ActivityService.NOTE_PROPERTY_KEY_ACTIVITY);
            property.setPropertyValue(ActivityService.NOTE_PROPERTY_VALUE_ACTIVITY);
            noteStoringTO.getProperties().add(property);
        }
        noteStoringTO.setPublish(true);
        ConverterUtils.convertArray(note.getTags(), noteStoringTO.getTags(),
                ConverterUtils.MQ_TAG_TAGTO_CONVERTER);
        noteStoringTO.setUnparsedTags(note.getUnparsedTags());
        convertUsersToNotify(note, noteStoringTO);
        return noteStoringTO;
    }

    /**
     * Convert topics of note POJO into target topic and crosspost topics of TO
     *
     * @param note
     *            the source to read from
     * @param noteTO
     *            the target to write to
     * @throws BlogNotFoundException
     *             in case topic does not exist
     * @throws BlogAccessException
     *             in case the current user has no access to the topic
     */
    private void convertTopics(Note note, NoteStoringTO noteTO) throws BlogNotFoundException,
    BlogAccessException {
        BaseTopic[] topics = note.getTopics();
        if (topics != null && topics.length > 0) {
            // first topic is target blog
            noteTO.setBlogId(TopicUtils.getTopicId(topics[0]));
            // ignore all crosspost topics if creating a comment (normal topic ID must be set)
            if (note.getParentNoteId() == null) {
                for (int i = 1; i < topics.length; i++) {
                    BaseTopic crosspostTopic = topics[i];
                    // TODO handle ID based crosspost targets, if the id is
                    if (StringUtils.isNotBlank(crosspostTopic.getTopicAlias())) {
                        noteTO.getAdditionalBlogs().add(crosspostTopic.getTopicAlias());
                    } else {
                        LOGGER.warn("Ignoring cross-post topic without alias. Specifying a cross-post "
                                + "topic by its ID is currently not supported");
                    }
                }
            }
        }
    }

    /**
     * Extract the users to notify from note POJO and add to TO
     *
     * @param note
     *            the source to read from
     * @param noteTO
     *            the target to write to
     */
    private void convertUsersToNotify(Note note, NoteStoringTO noteTO) {
        BaseEntity[] usersToNotify = note.getUsersToNotify();
        if (usersToNotify != null) {
            for (int i = 0; i < usersToNotify.length; i++) {
                BaseEntity user = usersToNotify[i];
                if (!user.getIsGroup()) {
                    // TODO handle ID based users
                    if (StringUtils.isNotBlank(user.getEntityAlias())) {
                        noteTO.getUsersToNotify().add(user.getEntityAlias());
                    } else {
                        LOGGER.warn("Ignoring userToNotify without alias. Sending notifications to "
                                + "users identified by ID or externalId is currently not supported");
                    }
                } else {
                    LOGGER.warn("Ignoring group found in usersToNotify because sending notifications "
                            + "to groups is not supported");
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<CreateNoteMessage> getHandledMessageClass() {
        return CreateNoteMessage.class;
    }

    /**
     * @return the lazily initialized note management
     */
    private NoteService getNoteManagement() {
        if (noteManagement == null) {
            noteManagement = ServiceLocator.instance().getService(NoteService.class);
        }
        return noteManagement;
    }

    /**
     * @return the lazily initialized user management
     */
    private UserManagement getUserManagement() {
        if (userManagement == null) {
            userManagement = ServiceLocator.instance().getService(UserManagement.class);
        }
        return userManagement;
    }

    @Override
    public String getVersion() {
        return LATEST_VERSION;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CommunoteReplyMessage handleMessage(CreateNoteMessage message) throws Exception {
        validateMessage(message);
        Note note = message.getNote();
        NoteStoringTO noteStoringTO = convertToNoteStoringTO(note);
        NoteModificationResult result = getNoteManagement().createNote(noteStoringTO, null);
        CommunoteReplyMessage reply = new CommunoteReplyMessage();
        // Use locale from security context user, not the original one
        User currentUser = getUserManagement().getUserById(SecurityHelper.assertCurrentUserId(),
                new IdentityConverter<User>());
        String feedbackMessage = CreateBlogPostHelper.getFeedbackMessageAfterModification(result,
                currentUser.getLanguageLocale());
        // no need to set a status if operation succeeded
        Status status = null;
        if (result.getStatus().equals(NoteModificationStatus.SUCCESS)) {
            if (feedbackMessage != null) {
                status = new Status();
                status.setStatusCode(ErrorCodes.WARNING);
                status.setMessage(feedbackMessage);
            }
        } else {
            status = new Status();
            status.setMessage(feedbackMessage);
            if (result.getStatus().equals(NoteModificationStatus.SYSTEM_ERROR)) {
                status.setStatusCode(ErrorCodes.INTERNAL_ERROR);
            } else {
                // TODO is this a good error code?
                status.setStatusCode(ErrorCodes.NOT_ACCEPTABLE);
            }
        }
        reply.setStatus(status);
        return reply;
    }

    /**
     * Validate the message and check that the note POJO is set
     *
     * @param message
     *            the message to check
     * @throws NoNoteSpecifiedException
     *             in case the POJO is missing
     */
    private void validateMessage(CreateNoteMessage message) throws NoNoteSpecifiedException {
        if (message.getNote() == null) {
            throw new NoNoteSpecifiedException();
        }
    }
}
