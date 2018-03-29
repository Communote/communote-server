package com.communote.plugins.discussionnotification.processor;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.communote.common.converter.Converter;
import com.communote.plugins.discussionnotification.DiscussionNotificationActivator;
import com.communote.plugins.discussionnotification.definition.WatchedDiscussionNotificationDefinition;
import com.communote.server.api.core.blog.BlogRightsManagement;
import com.communote.server.api.core.common.NotFoundException;
import com.communote.server.api.core.note.NoteStoringTO;
import com.communote.server.api.core.note.processor.NoteStoringPostProcessorContext;
import com.communote.server.api.core.property.PropertyManagement;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.core.blog.notes.processors.NoteNotificationDetails;
import com.communote.server.core.blog.notes.processors.NotificationNoteProcessor;
import com.communote.server.core.messaging.NotificationDefinition;
import com.communote.server.model.note.Note;
import com.communote.server.model.user.User;

/**
 * Notification processor which extracts all users that explicitly watch the discussion of a note.
 *
 * @author Communote Team - <a href="https://github.com/Communote">https://github.com/Communote</a>
 */
public class WatchedDiscussionNotificationNoteProcessor extends NotificationNoteProcessor {

    private class TopicAccessAwareConverter implements Converter<User, User> {

        private final Long topicId;
        private final Collection<User> usersCollector;

        public TopicAccessAwareConverter(Long topicId, Collection<User> usersCollector) {
            this.topicId = topicId;
            this.usersCollector = usersCollector;
        }

        @Override
        public User convert(User source) {
            if (topicRightsManagement.userHasReadAccess(topicId, source.getId(), false)) {
                usersCollector.add(source);
                return source;
            }
            return null;
        }
    }

    private static final Logger LOGGER = LoggerFactory
            .getLogger(WatchedDiscussionNotificationNoteProcessor.class);

    private final PropertyManagement propertyManagement;
    private final BlogRightsManagement topicRightsManagement;
    private final WatchedDiscussionNotificationDefinition definition;

    public WatchedDiscussionNotificationNoteProcessor(PropertyManagement propertyManagement,
            BlogRightsManagement topicRightsManagement,
            WatchedDiscussionNotificationDefinition definition) {
        this.propertyManagement = propertyManagement;
        this.topicRightsManagement = topicRightsManagement;
        this.definition = definition;
    }

    /**
     * Returns the first note of a discussion
     *
     * @param note
     *            the note for which the discussion root note should be returned
     * @return the root note of the discussion or null if the passed in note is the first note of
     *         the discussion
     */
    private Note getDiscussionRootNote(Note note) {
        Long discussionId = note.getDiscussionId();
        if (!note.getId().equals(discussionId)) {
            return getNoteDao().load(discussionId);
        }
        return null;
    }

    @Override
    public String getId() {
        return "watchedDiscussion";
    }

    @Override
    public NotificationDefinition getNotificationDefinition() {
        return definition;
    }

    @Override
    public int getOrder() {
        // let it be called after DiscussionParticipationNotificationNoteProcessor
        return DiscussionParticipationNotificationNoteProcessor.ORDER - 1;
    }

    @Override
    protected Collection<User> getUsersToNotify(Note note, NoteStoringPostProcessorContext context,
            Set<Long> userIdsToSkip) {
        Set<User> usersToNotify = new HashSet<User>();
        if (note.getParent() == null || note.isDirect()) {
            return usersToNotify;
        }
        Note rootNote = getDiscussionRootNote(note);
        if (rootNote != null) {
            try {
                TopicAccessAwareConverter converter = new TopicAccessAwareConverter(
                        note.getBlog().getId(), usersToNotify);
                // get all users which explicitly watch the discussion and have access to the topic.
                // The converter collects them in usersToNotify
                propertyManagement.getUsersOfProperty(rootNote.getId(),
                        DiscussionNotificationActivator.KEY_GROUP,
                        DiscussionNotificationActivator.PROPERTY_KEY_WATCHED_DISCUSSION,
                        Boolean.TRUE.toString(), converter);
            } catch (NotFoundException e) {
                LOGGER.debug("Discussion root note was not found: {}", e.getMessage());
            } catch (AuthorizationException e) {
                LOGGER.warn("Getting users watching discussion {} failed: {}",
                        note.getDiscussionId(), e.getMessage());
            }
        }
        return usersToNotify;
    }

    @Override
    protected boolean isSendNotifications(Note note, NoteStoringTO orginalNoteStoringTO,
            Map<String, String> properties, NoteNotificationDetails resendDetails) {
        // ignore root note. Also skip DMs for now because user will, if she didn't disabled mention
        // notification, be notified if he is recipient of the the direct message.
        return note.getParent() != null && !note.isDirect();
    }

}
