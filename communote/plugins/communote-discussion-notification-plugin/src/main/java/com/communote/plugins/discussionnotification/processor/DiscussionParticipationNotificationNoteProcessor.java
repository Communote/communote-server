package com.communote.plugins.discussionnotification.processor;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.communote.common.converter.Converter;
import com.communote.plugins.discussionnotification.DiscussionNotificationActivator;
import com.communote.plugins.discussionnotification.definition.DiscussionParticipationNotificationDefinition;
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
import com.communote.server.model.note.NoteStatus;
import com.communote.server.model.user.User;

/**
 * Processor which collects users who want to be notified about notes of discussions they
 * participated in without being explicitly notified.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class DiscussionParticipationNotificationNoteProcessor extends NotificationNoteProcessor {

    private class IdExtractingConverter implements Converter<User, User> {
        private final Set<Long> collector;

        public IdExtractingConverter(Set<Long> collector) {
            this.collector = collector;
        }

        @Override
        public User convert(User source) {
            collector.add(source.getId());
            return source;
        }
    }

    private static final Logger LOGGER = LoggerFactory
            .getLogger(DiscussionParticipationNotificationNoteProcessor.class);

    public static final int ORDER = 50;

    private final BlogRightsManagement topicRightsManagement;
    private final boolean parentTreeOnly;
    private final DiscussionParticipationNotificationDefinition notificationDefinition;
    private final PropertyManagement propertyManagement;

    /**
     * Constructor which creates a new notification note processor
     *
     * @param parentTreeOnly
     *            if true only the notes which are in the parent tree of the note to process will be
     *            considered. If false all notes of the discussion will be considered.
     * @param topicRightsManagement
     *            Right management for topics.
     * @param definition
     *            the notification definition for which this processor extracts the user to be
     *            notified
     */
    public DiscussionParticipationNotificationNoteProcessor(boolean parentTreeOnly,
            BlogRightsManagement topicRightsManagement, PropertyManagement propertyManagement,
            DiscussionParticipationNotificationDefinition definition) {
        this.parentTreeOnly = parentTreeOnly;
        this.topicRightsManagement = topicRightsManagement;
        this.propertyManagement = propertyManagement;
        this.notificationDefinition = definition;
    }

    /**
     * Starts at a root note and collects all authors of all children recursively.
     *
     * @param rootNote
     *            the root note to start from
     * @param blogId
     *            the ID of the topic of the root note
     * @param currentAuthorId
     *            the ID of the author to skip because it refers to the author of the note which
     *            triggered this processor
     * @param usersToNotify
     *            collection for storing the authors to be notified
     * @param usersNoReadAccess
     *            collection for storing users which do not have read access to a blog. Useful to
     *            avoid unnecessary blog access checks
     * @param userIdsToSkip
     *            collection of user IDs to ignore
     */
    private void extractAuthorsFromDiscussionSubTree(Note rootNote, Long blogId,
            Long currentAuthorId, Set<User> usersToNotify, Set<Long> usersNoReadAccess,
            Set<Long> userIdsToSkip) {
        if (!NoteStatus.PUBLISHED.equals(rootNote.getStatus())) {
            return;
        }
        User author = rootNote.getUser();
        processAuthor(author, blogId, currentAuthorId, usersToNotify, usersNoReadAccess,
                userIdsToSkip);
        // process children
        Set<Note> comments = rootNote.getChildren();
        if (comments != null) {
            for (Note comment : comments) {
                extractAuthorsFromDiscussionSubTree(comment, blogId, currentAuthorId, usersToNotify,
                        usersNoReadAccess, userIdsToSkip);
            }
        }
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
        return "discussionParticipation";
    }

    @Override
    public NotificationDefinition getNotificationDefinition() {
        return notificationDefinition;
    }

    @Override
    public int getOrder() {
        return ORDER;
    }

    private Set<Long> getUsersNotWatchingDiscussion(Long discussionId) {
        HashSet<Long> userIds = new HashSet<>();
        try {
            propertyManagement.getUsersOfProperty(discussionId,
                    DiscussionNotificationActivator.KEY_GROUP,
                    DiscussionNotificationActivator.PROPERTY_KEY_WATCHED_DISCUSSION,
                    Boolean.FALSE.toString(), new IdExtractingConverter(userIds));
        } catch (NotFoundException | AuthorizationException e) {
            LOGGER.warn("Getting users who disabled notification for discussion {} failed: {}",
                    discussionId, e.getMessage());
        }
        return userIds;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Collection<User> getUsersToNotify(Note note, NoteStoringPostProcessorContext context,
            Set<Long> userIdsToSkip) {
        Set<User> usersToNotify = new HashSet<User>();
        // skip direct messages because we assume that the only authors can be informed if they have
        // read access to the DM, which is only the case if they are mentioned. In that case they
        // are probably already notified (if not disabled).
        if (note.getParent() == null || note.isDirect()) {
            return usersToNotify;
        }
        Set<Long> allUserIdsToSkip = getUsersNotWatchingDiscussion(note.getDiscussionId());
        allUserIdsToSkip.addAll(userIdsToSkip);
        Set<Long> usersNoReadAccess = new HashSet<Long>();
        // To get all users participating in the discussion we have to walk the discussion tree
        // upwards to the root note and than collect all children recursively and return the
        // authors, but ignore the author of this note
        Long currentAuthorId = note.getUser().getId();
        if (this.parentTreeOnly) {
            Note parent;
            while ((parent = note.getParent()) != null) {
                User author = parent.getUser();
                processAuthor(author, note.getBlog().getId(), currentAuthorId, usersToNotify,
                        usersNoReadAccess, allUserIdsToSkip);
                note = parent;
            }
        } else {
            Note discussionRoot = getDiscussionRootNote(note);
            if (discussionRoot != null) {
                extractAuthorsFromDiscussionSubTree(discussionRoot, note.getBlog().getId(),
                        currentAuthorId, usersToNotify, usersNoReadAccess, allUserIdsToSkip);
            }
        }
        return usersToNotify;
    }

    @Override
    protected boolean isSendNotifications(Note note, NoteStoringTO noteStoringTO,
            Map<String, String> properties, NoteNotificationDetails resendDetails) {
        // TODO do not notify participating authors if resendDetails is not null (and thus editing
        // with disabled resend flag)?
        return note.getParent() != null && !note.isDirect();
    }

    /**
     * Stores the author in the usersToNotify collection if he has read access and should not be
     * skipped
     *
     * @param author
     *            the author to process
     * @param topicId
     *            the ID of the topic of the discussion
     * @param currentAuthorId
     *            the ID of the author to skip because it refers to the author of the note which
     *            triggered this processor
     * @param usersToNotify
     *            collection for storing the authors to be notified
     * @param usersNoReadAccess
     *            collection for storing users which do not have read access to a topic. Useful to
     *            avoid unnecessary topic access checks
     * @param userIdsToSkip
     *            collection of user IDs to ignore
     */
    private void processAuthor(User author, Long topicId, Long currentAuthorId,
            Set<User> usersToNotify, Set<Long> usersNoReadAccess, Set<Long> userIdsToSkip) {
        Long authorId = author.getId();
        // avoid unnecessary topic access checks
        if (authorId.equals(currentAuthorId) || usersNoReadAccess.contains(authorId)
                || userIdsToSkip.contains(authorId) || usersToNotify.contains(author)) {
            return;
        }
        boolean readAccess = topicRightsManagement.userHasReadAccess(topicId, authorId, false);
        if (readAccess) {
            usersToNotify.add(author);
        } else {
            usersNoReadAccess.add(authorId);
        }
    }
}
