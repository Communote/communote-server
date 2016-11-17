package com.communote.server.core.blog.notes.processors;

import java.util.HashSet;
import java.util.Set;

/**
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
     * <p>
     * Flag to show, that all users with at least one message on the discussion will be notified.
     * </p>
     */
    public boolean isMentionDiscussionAuthors() {
        return this.mentionDiscussionAuthors;
    }

    /**
     * <p>
     * Flag to show, that all (active) users with at least one note in the topic where notified.
     * </p>
     */
    public boolean isMentionTopicAuthors() {
        return this.mentionTopicAuthors;
    }

    /**
     * <p>
     * Flag to show, that all users with management access of the topic where mentioned.
     * </p>
     */
    public boolean isMentionTopicManagers() {
        return this.mentionTopicManagers;
    }

    /**
     * <p>
     * Flag to show, that all users with at least read access of the topic should where mentioned.
     * </p>
     */
    public boolean isMentionTopicReaders() {
        return this.mentionTopicReaders;
    }

    public void setMentionDiscussionAuthors(boolean mentionDiscussionAuthors) {
        this.mentionDiscussionAuthors = mentionDiscussionAuthors;
    }

    public void setMentionTopicAuthors(boolean mentionTopicAuthors) {
        this.mentionTopicAuthors = mentionTopicAuthors;
    }

    public void setMentionTopicManagers(boolean mentionTopicManagers) {
        this.mentionTopicManagers = mentionTopicManagers;
    }

    public void setMentionTopicReaders(boolean mentionTopicReaders) {
        this.mentionTopicReaders = mentionTopicReaders;
    }

}
