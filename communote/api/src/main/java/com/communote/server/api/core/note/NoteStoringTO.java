package com.communote.server.api.core.note;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.communote.server.api.core.property.StringPropertyTO;
import com.communote.server.api.core.tag.TagTO;
import com.communote.server.model.note.NoteCreationSource;

/**
 * TO for notes.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class NoteStoringTO implements java.io.Serializable {

    /** The serial version UID of this class. Needed for serialization. */
    private static final long serialVersionUID = -6670443270802733972L;

    private Long creatorId;
    private Long blogId;
    private String unparsedTags;
    private Long[] attachmentIds;
    private String language;
    private Set<String> usersToNotify = new HashSet<String>();
    private Long version;
    private NoteCreationSource creationSource;
    private String content;
    private NoteContentType contentType;
    private boolean isDirectMessage = false;
    private boolean sendNotifications = false;
    private boolean publish = false;
    private Long autosaveNoteId;
    private Set<String> usersNotToNotify = new HashSet<String>();
    private Set<StringPropertyTO> properties = new HashSet<StringPropertyTO>();
    private NoteStoringFailDefinition failDefinition;
    private Long parentNoteId;
    private Set<String> additionalBlogs = new HashSet<String>();
    private Set<TagTO> tags = new HashSet<TagTO>();
    private Timestamp creationDate = new Timestamp(System.currentTimeMillis());
    private boolean mentionTopicReaders;
    private boolean mentionTopicAuthors;
    private boolean mentionTopicManagers;
    private boolean mentionDiscussionAuthors;
    private final Map<String, Object> transientProperties = new HashMap<>();

    /**
     * @return the additionalBlogs
     */
    public Set<String> getAdditionalBlogs() {
        return additionalBlogs;
    }

    /**
     * @return the attachmentIds
     */
    public Long[] getAttachmentIds() {
        return attachmentIds;
    }

    /**
     * public Long[] getAttachmentIds() { return this.attachmentIds; }
     *
     * /**
     *
     */
    public Long getAutosaveNoteId() {
        return this.autosaveNoteId;
    }

    /**
     * <p>
     * The ID of the blog for which the post will be created.
     * </p>
     */
    public Long getBlogId() {
        return this.blogId;
    }

    /**
     *
     */
    public String getContent() {
        return this.content;
    }

    /**
     * <p>
     * Describes the type of content hold within this StringContentTO. The type basically determines
     * how the content will be interpreted and processed.
     * </p>
     */
    public com.communote.server.api.core.note.NoteContentType getContentType() {
        return this.contentType;
    }

    /**
     * The creation date is the date as this {@link NoteStoringTO} got constructed. If the note is
     * an update it will be used as last modification date, and if it is a create as creation date.
     *
     * @return the creation date of this note, will never be null (is set on constructing or using
     *         {@link #setCreationDate(Timestamp)}
     */
    public Timestamp getCreationDate() {
        return creationDate;
    }

    /**
     * <p>
     * Defines the source of creation.
     * </p>
     */
    public com.communote.server.model.note.NoteCreationSource getCreationSource() {
        return this.creationSource;
    }

    /**
     * <p>
     * The ID of the author of the post.
     * </p>
     */
    public Long getCreatorId() {
        return this.creatorId;
    }

    /**
     * Get the failDefinition
     *
     */
    public com.communote.server.api.core.note.NoteStoringFailDefinition getFailDefinition() {
        return this.failDefinition;
    }

    /**
     * <p>
     * Language of post content.
     * </p>
     */
    public String getLanguage() {
        return this.language;
    }

    /**
     * @return the parentNoteId
     */
    public Long getParentNoteId() {
        return parentNoteId;
    }

    /**
     * <p>
     * String properties for this note.
     * </p>
     */
    public Set<StringPropertyTO> getProperties() {
        return this.properties;
    }

    /**
     * @return the tags
     */
    public Set<TagTO> getTags() {
        return tags;
    }

    /**
     * Get the value of previously added transient property.
     *
     * @param key
     *            the key of the property to get
     * @return the value of the transient property or null if the property was not set
     * @see #setTransientProperty(String, Object)
     */
    public Object getTransientProperty(String key) {
        return transientProperties.get(key);
    }

    /**
     * <p>
     * A string holding all tags for this post separated by comma.
     * </p>
     */
    public String getUnparsedTags() {
        return this.unparsedTags;
    }

    /**
     * <p>
     * List of user alias, which can't be notified.
     * </p>
     */
    public Set<String> getUsersNotToNotify() {
        return this.usersNotToNotify;
    }

    /**
     * <p>
     * A set of alias strings of users that should be notified about the creation of the user tagged
     * item. Only those users whose alias is known within Communote will be notified.
     * </p>
     */
    public Set<String> getUsersToNotify() {
        return this.usersToNotify;
    }

    /**
     * <p>
     * The version.
     * </p>
     */
    public Long getVersion() {
        return this.version;
    }

    /**
     * <p>
     * If this is set, the message is a direct message. Receivers will be in the list of users to be
     * notified.
     * </p>
     */
    public boolean isIsDirectMessage() {
        return this.isDirectMessage;
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
     * <p>
     * Whether to publish the note or create an autosave.
     * </p>
     */
    public boolean isPublish() {
        return this.publish;
    }

    /**
     * <p>
     * Whether to actually send notifications.
     * </p>
     */
    public boolean isSendNotifications() {
        return this.sendNotifications;
    }

    /**
     * Remove a previously added transient property.
     *
     * @param key
     *            the key of the property to remove
     * @return the value of the removed property or null if there was no property for the key
     * @see #setTransientProperty(String, Object)
     */
    public Object removeTransientProperty(String key) {
        return transientProperties.remove(key);
    }

    /**
     * @param additionalBlogs
     *            the additionalBlogs to set
     */
    public void setAdditionalBlogs(Set<String> additionalBlogs) {
        this.additionalBlogs = additionalBlogs;
    }

    /**
     * @param attachmentIds
     *            the attachmentIds to set
     */
    public void setAttachmentIds(Long[] attachmentIds) {
        this.attachmentIds = attachmentIds;
    }

    public void setAutosaveNoteId(Long autosaveNoteId) {
        this.autosaveNoteId = autosaveNoteId;
    }

    public void setBlogId(Long blogId) {
        this.blogId = blogId;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setContentType(com.communote.server.api.core.note.NoteContentType contentType) {
        this.contentType = contentType;
    }

    /**
     *
     * @param creationDate
     *            the creation date for the note, if null (default) it will be the current time
     */
    public void setCreationDate(Timestamp creationDate) {
        if (creationDate == null) {
            this.creationDate = new Timestamp(System.currentTimeMillis());
        } else {

            this.creationDate = creationDate;
        }
    }

    public void setCreationSource(com.communote.server.model.note.NoteCreationSource creationSource) {
        this.creationSource = creationSource;
    }

    public void setCreatorId(Long creatorId) {
        this.creatorId = creatorId;
    }

    /**
     * Sets the failDefinition
     */
    public void setFailDefinition(
            com.communote.server.api.core.note.NoteStoringFailDefinition failDefinition) {
        this.failDefinition = failDefinition;
    }

    public void setIsDirectMessage(boolean isDirectMessage) {
        this.isDirectMessage = isDirectMessage;
    }

    public void setLanguage(String language) {
        this.language = language;
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
     * @param parentNoteId
     *            the parentNoteId to set
     */
    public void setParentNoteId(Long parentNoteId) {
        this.parentNoteId = parentNoteId;
    }

    public void setProperties(Set<StringPropertyTO> properties) {
        this.properties = properties;
    }

    public void setPublish(boolean publish) {
        this.publish = publish;
    }

    public void setSendNotifications(boolean sendNotifications) {
        this.sendNotifications = sendNotifications;
    }

    /**
     * @param tags
     *            the tags to set
     */
    public void setTags(Set<TagTO> tags) {
        this.tags = tags;
    }

    /**
     * Add a property which will not be persisted with the note. This can for example be used to
     * exchange information between a NoteStoringPreProcessor and a NoteStoringPostProcessor.
     *
     * @param key
     *            the key of the property. If there is already a property with that key it will be
     *            replaced.
     * @param value
     *            the value of the property
     */
    public void setTransientProperty(String key, Object value) {
        transientProperties.put(key, value);
    }

    public void setUnparsedTags(String unparsedTags) {
        this.unparsedTags = unparsedTags;
    }

    public void setUsersNotToNotify(Set<String> usersNotToNotify) {
        this.usersNotToNotify = usersNotToNotify;
    }

    /**
     * <p>
     *
     * @param usersToNotify
     *            A set of alias strings of users that should be notified about the creation of the
     *            user tagged item. Only those users whose alias is known within Communote will be
     *            notified.
     *            </p>
     */
    public void setUsersToNotify(Set<String> usersToNotify) {
        this.usersToNotify = usersToNotify;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}