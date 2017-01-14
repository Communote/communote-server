package com.communote.server.core.blog.notes.processors;

import java.util.HashSet;
import java.util.Set;

/**
 * Object describing which users should be or have been mentioned and thus should be or have been
 * notified about a note.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class NoteNotificationDetails {

    private boolean mentionDiscussionAuthors;
    private boolean mentionTopicAuthors;
    private boolean mentionTopicManagers;
    private boolean mentionTopicReaders;
    private final Set<Long> mentionedUserIds = new HashSet<>();

    /**
     * Add the ID of a mentioned user
     *
     * @param userId
     *            the ID of the user
     */
    public void addMentionedUser(Long userId) {
        mentionedUserIds.add(userId);
    }

    /**
     * @return the IDs of the mentioned users
     */
    public Set<Long> getMentionedUserIds() {
        return mentionedUserIds;
    }

    /**
     * @return whether all active users with at least one note in the discussion should be notified
     */
    public boolean isMentionDiscussionAuthors() {
        return this.mentionDiscussionAuthors;
    }

    /**
     * @return whether all active users with at least one note in the topic should be notified
     */
    public boolean isMentionTopicAuthors() {
        return this.mentionTopicAuthors;
    }

    /**
     * @return whether all active users with management access to the topic should be notified
     */
    public boolean isMentionTopicManagers() {
        return this.mentionTopicManagers;
    }

    /**
     * @return whether all active users with at least read access to the topic should be notified
     */
    public boolean isMentionTopicReaders() {
        return this.mentionTopicReaders;
    }

    /**
     * @see #isMentionDiscussionAuthors()
     */
    public void setMentionDiscussionAuthors(boolean mentionDiscussionAuthors) {
        this.mentionDiscussionAuthors = mentionDiscussionAuthors;
    }

    /**
     * @see #isMentionTopicAuthors()
     */
    public void setMentionTopicAuthors(boolean mentionTopicAuthors) {
        this.mentionTopicAuthors = mentionTopicAuthors;
    }

    /**
     * @see #isMentionTopicManagers()
     */
    public void setMentionTopicManagers(boolean mentionTopicManagers) {
        this.mentionTopicManagers = mentionTopicManagers;
    }

    /**
     * @see #isMentionTopicReaders()
     */
    public void setMentionTopicReaders(boolean mentionTopicReaders) {
        this.mentionTopicReaders = mentionTopicReaders;
    }

}
