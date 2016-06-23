package com.communote.server.core.blog.notes.processors;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.communote.server.api.core.blog.BlogRightsManagement;
import com.communote.server.api.core.config.type.ClientProperty;
import com.communote.server.api.core.note.NoteStoringTO;
import com.communote.server.api.core.note.processor.NoteStoringPostProcessorContext;
import com.communote.server.core.blog.TooManyMentionedUsersNoteManagementException;
import com.communote.server.model.note.Note;
import com.communote.server.model.user.User;

/**
 * Processor for the @@discussion notation.
 * <p>
 * Notifies all authors of parent notes of a note and thus all users that participated in the
 * discussion the note is a part of.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class DiscussionNotificationNoteProcessor extends NotificationNoteProcessor {

    private final BlogRightsManagement topicRightsManagement;
    private final boolean parentTreeOnly;

    /**
     * Constructor which creates a new notification note processor
     * 
     * @param parentTreeOnly
     *            if true only the notes which are in the parent tree of the note to process will be
     *            considered. If false all notes of the discussion will be considered.
     * @param topicRightsManagement
     *            Right management for topics.
     */
    public DiscussionNotificationNoteProcessor(boolean parentTreeOnly,
            BlogRightsManagement topicRightsManagement) {
        this.parentTreeOnly = parentTreeOnly;
        this.topicRightsManagement = topicRightsManagement;
    }

    /**
     * Starts at a root note and collects all authors of all children recursively.
     * 
     * @param rootNote
     *            the root noot to start from
     * @param blogId
     *            the ID of the blog of the root note
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
        User author = rootNote.getUser();
        processAuthor(author, blogId, currentAuthorId, usersToNotify, usersNoReadAccess,
                userIdsToSkip);
        // process children
        Set<Note> comments = rootNote.getChildren();
        if (comments != null) {
            for (Note comment : comments) {
                extractAuthorsFromDiscussionSubTree(comment, blogId, currentAuthorId,
                        usersToNotify, usersNoReadAccess, userIdsToSkip);
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

    /**
     * {@inheritDoc}
     */
    @Override
    public int getOrder() {
        // value smaller than UserNotificationProcessors priority
        return 90;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Collection<User> getUsersToNotify(Note note, NoteStoringPostProcessorContext context) {
        Set<User> usersToNotify = new HashSet<User>();
        if (!note.isMentionDiscussionAuthors() || note.getParent() == null) {
            return usersToNotify;
        }
        Set<Long> usersNoReadAccess = new HashSet<Long>();
        // to get all users participating in the discussion we have to walk the discussion tree
        // upwards to the root note and than collect all children recursively and return the
        // authors, but ignore the author of this note
        Long currentAuthorId = Boolean.getBoolean("com.communote.mention.discussion.ignore-author")
                ? note.getUser().getId() : -1L;
        if (this.parentTreeOnly) {
            Note parent;
            Set<Long> processedNotes = new HashSet<Long>();
            while ((parent = note.getParent()) != null) {
                User author = parent.getUser();
                processAuthor(author, note.getBlog().getId(), currentAuthorId, usersToNotify,
                        usersNoReadAccess, context.getUserIdsToSkip());
                note = parent;
                if (processedNotes.contains(note.getId())) {
                    break;
                }
                processedNotes.add(note.getId());
            }
        } else {
            Note discussionRoot = getDiscussionRootNote(note);
            if (discussionRoot != null) {
                extractAuthorsFromDiscussionSubTree(discussionRoot, note.getBlog().getId(),
                        currentAuthorId, usersToNotify, usersNoReadAccess,
                        context.getUserIdsToSkip());
            }
        }
        return usersToNotify;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean process(Note note, NoteStoringTO noteStoringTO,
            Map<String, String> properties) {
        // all notes with a parent note should be processed
        int maxUsers = ClientProperty.MAX_NUMBER_OF_MENTIONED_USERS
                .getValue(ClientProperty.DEFAULT_MAX_NUMBER_OF_MENTIONED_USERS);
        // TODO how to get information about users to skip at check time?
        if (maxUsers > 0
                && maxUsers < getUsersToNotify(note, new NoteStoringPostProcessorContext(new Long[0]))
                        .size()) {
            throw new TooManyMentionedUsersNoteManagementException();
        }
        boolean processMe = noteStoringTO.isSendNotifications()
                && noteStoringTO.isMentionDiscussionAuthors()
                && note.getParent() != null;

        return processMe;
    }

    /**
     * Stores the author in the usersToNotify collection if he has read access and should not be
     * skipped
     * 
     * @param author
     *            the author to process
     * @param blogId
     *            the ID of the blog of the discussion
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
    private void processAuthor(User author, Long blogId, Long currentAuthorId,
            Set<User> usersToNotify, Set<Long> usersNoReadAccess, Set<Long> userIdsToSkip) {
        Long authorId = author.getId();
        // avoid unnecessary blog access checks
        if (!authorId.equals(currentAuthorId) && !usersNoReadAccess.contains(authorId)
                && !userIdsToSkip.contains(authorId) && !usersToNotify.contains(author)) {
            boolean readAccess = topicRightsManagement.userHasReadAccess(blogId, authorId,
                    false);
            if (readAccess) {
                usersToNotify.add(author);
            } else {
                usersNoReadAccess.add(authorId);
            }
        }
    }
}
