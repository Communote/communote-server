package com.communote.plugins.mq.message.core.data.note;

import java.util.Date;

import org.codehaus.jackson.annotate.JsonIgnore;

import com.communote.plugins.mq.message.core.data.property.StringProperty;
import com.communote.plugins.mq.message.core.data.tag.Tag;
import com.communote.plugins.mq.message.core.data.topic.BaseTopic;
import com.communote.plugins.mq.message.core.data.user.BaseEntity;

/**
 * POJO holding the data of a note.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a> *
 */
public class Note {
    /**
     * constant for the content type that should be used if the type of the content is not known and
     * the server should try figure it out
     */
    public static final String CONTENT_TYPE_UNKNOWN = "UNKNOWN";
    /**
     * constant for the content type that should be used if the content is plain text
     */
    public static final String CONTENT_TYPE_PLAIN_TEXT = "PLAIN_TEXT";
    /**
     * constant for the content type that should be used if the content is HTML
     */
    public static final String CONTENT_TYPE_HTML = "HTML";
    private String content;
    private String unparsedTags;
    private boolean directMessage;
    private boolean mentionTopicReaders;
    private boolean mentionTopicAuthors;
    private boolean mentionTopicManagers;
    private boolean mentionDiscussionAuthors;
    private String contentType = CONTENT_TYPE_UNKNOWN;
    private BaseTopic[] topics;
    private Tag[] tags;
    private Date creationDate;
    private BaseEntity[] usersToNotify;
    private StringProperty[] properties;
    private Long parentNoteId;

    /**
     * @return the content of the note
     */
    public String getContent() {
        return content;
    }

    /**
     * @return the content type of the note. Supported values are 'HTML', 'PLAIN_TEXT' and
     *         'UNKNOWN'.
     */
    public String getContentType() {
        return contentType;
    }

    /**
     *
     * @return the creation date of this note (use null and it will be the time of processing)
     */
    public Date getCreationDate() {
        return creationDate;
    }

    /**
     * @return the ID of the parent note. If not null the note is created as a comment to the note
     *         with the given ID.
     */
    public Long getParentNoteId() {
        return parentNoteId;
    }

    /**
     * @return the properties to add to the note, can be null
     */
    public StringProperty[] getProperties() {
        return properties;
    }

    /**
     * @return the tags to be added to the note
     */
    public Tag[] getTags() {
        return tags;
    }

    /**
     * @return the topics the note should be created in. The first topic is used as the target
     *         topic, the remaining as crosspost targets. There must be at least one topic.
     */
    public BaseTopic[] getTopics() {
        return topics;
    }

    /**
     * @return comma separated string of tag names which will be resolved to note tags
     */
    public String getUnparsedTags() {
        return unparsedTags;
    }

    /**
     * @return the users that should be notified, can be null
     */
    public BaseEntity[] getUsersToNotify() {
        return usersToNotify;
    }

    /**
     * @return whether the note is an activity. Returns false by default. Subclasses should override
     *         it if they support activities.
     */
    @JsonIgnore
    public boolean isActivity() {
        return false;
    }

    /**
     * @return whether the note is a direct message
     */
    public boolean isDirectMessage() {
        return directMessage;
    }

    /**
     * @return the mentionDiscussionAuthors
     */
    public boolean isMentionDiscussionAuthors() {
        return mentionDiscussionAuthors;
    }

    /**
     * @return the mentionTopicAuthors
     */
    public boolean isMentionTopicAuthors() {
        return mentionTopicAuthors;
    }

    /**
     * @return the mentionTopicManagers
     */
    public boolean isMentionTopicManagers() {
        return mentionTopicManagers;
    }

    /**
     * @return the mentionTopicReaders
     */
    public boolean isMentionTopicReaders() {
        return mentionTopicReaders;
    }

    /**
     * Set the content of the note
     *
     * @param content
     *            the content to set
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * Set the content type of the note. Supported values are 'HTML', 'PLAIN_TEXT' and 'UNKNOWN'.
     *
     * @param contentType
     *            the content type to set
     */
    public void setContentType(String contentType) {
        if (CONTENT_TYPE_HTML.equals(contentType) || CONTENT_TYPE_PLAIN_TEXT.equals(contentType)) {
            this.contentType = contentType;
        } else {
            this.contentType = CONTENT_TYPE_UNKNOWN;
        }
    }

    /**
     *
     * @param creationDate
     *            the creation date of this note (use null and it will be the time of processing)
     */
    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    /**
     * Set whether the note is a direct message
     *
     * @param directMessage
     *            true if the note is a direct message, false if not
     */
    public void setDirectMessage(boolean directMessage) {
        this.directMessage = directMessage;
    }

    /**
     * @param mentionDiscussionAuthors
     *            the mentionDiscussionAuthors to set
     */
    public void setMentionDiscussionAuthors(boolean mentionDiscussionAuthors) {
        this.mentionDiscussionAuthors = mentionDiscussionAuthors;
    }

    /**
     * @param mentionTopicAuthors
     *            the mentionTopicAuthors to set
     */
    public void setMentionTopicAuthors(boolean mentionTopicAuthors) {
        this.mentionTopicAuthors = mentionTopicAuthors;
    }

    /**
     * @param mentionTopicManagers
     *            the mentionTopicManagers to set
     */
    public void setMentionTopicManagers(boolean mentionTopicManagers) {
        this.mentionTopicManagers = mentionTopicManagers;
    }

    /**
     * @param mentionTopicReaders
     *            the mentionTopicReaders to set
     */
    public void setMentionTopicReaders(boolean mentionTopicReaders) {
        this.mentionTopicReaders = mentionTopicReaders;
    }

    /**
     * Set the ID of a note this note should be created as a comment of.
     * 
     * @param parentNoteId
     *            the ID of the parent note
     */
    public void setParentNoteId(Long parentNoteId) {
        this.parentNoteId = parentNoteId;
    }

    /**
     * Set the properties which should be added to the note
     *
     * @param properties
     *            the properties to add to the note
     */
    public void setProperties(StringProperty[] properties) {
        this.properties = properties;
    }

    /**
     * Set the tags to be added to the note
     *
     * @param tags
     *            the tags of the note
     */
    public void setTags(Tag[] tags) {
        this.tags = tags;
    }

    /**
     * Set the topics the note should be created in. The first topic is used as the target topic,
     * the remaining as crosspost targets. There must be at least one topic.
     *
     * @param topics
     *            the topics
     */
    public void setTopics(BaseTopic[] topics) {
        this.topics = topics;
    }

    /**
     * Set a comma separated string of tag names which will be resolved to note tags
     *
     * @param unparsedTags
     *            the unparsed tags
     */
    public void setUnparsedTags(String unparsedTags) {
        this.unparsedTags = unparsedTags;
    }

    /**
     * Set the users that should be notified about the note
     *
     * @param usersToNotify
     *            the users to notify
     */
    public void setUsersToNotify(BaseEntity[] usersToNotify) {
        this.usersToNotify = usersToNotify;
    }
}
