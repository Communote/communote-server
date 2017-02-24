package com.communote.server.model.note;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;

import com.communote.server.model.attachment.Attachment;
import com.communote.server.model.blog.Blog;
import com.communote.server.model.follow.Followable;
import com.communote.server.model.global.GlobalId;
import com.communote.server.model.property.Propertyable;
import com.communote.server.model.tag.Tag;
import com.communote.server.model.tag.Taggable;
import com.communote.server.model.user.User;

/**
 * <p>
 * A note of a topic
 * </p>
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class Note implements Serializable, Followable, Propertyable, Taggable {
    /**
     * Constructs new instances of {@link Note}.
     */
    public static final class Factory {
        /**
         * Constructs a new instance of {@link Note}.
         */
        public static Note newInstance() {
            return new Note();
        }

        /**
         * Constructs a new instance of {@link Note}, taking all required and/or read-only
         * properties as arguments.
         */
        public static Note newInstance(Timestamp creationDate, Timestamp lastModificationDate,
                Timestamp crawlLastModificationDate, NoteCreationSource creationSource,
                boolean direct, NoteStatus status, Long version, boolean mentionTopicReaders,
                boolean mentionTopicAuthors, boolean mentionTopicManagers,
                boolean mentionDiscussionAuthors, Blog blog, User user, Content content) {
            final Note entity = new Note();
            entity.setCreationDate(creationDate);
            entity.setLastModificationDate(lastModificationDate);
            entity.setCrawlLastModificationDate(crawlLastModificationDate);
            entity.setCreationSource(creationSource);
            entity.setDirect(direct);
            entity.setStatus(status);
            entity.setVersion(version);
            entity.setMentionTopicReaders(mentionTopicReaders);
            entity.setMentionTopicAuthors(mentionTopicAuthors);
            entity.setMentionTopicManagers(mentionTopicManagers);
            entity.setMentionDiscussionAuthors(mentionDiscussionAuthors);
            entity.setBlog(blog);
            entity.setUser(user);
            entity.setContent(content);
            return entity;
        }

        /**
         * Constructs a new instance of {@link Note}, taking all possible properties (except the
         * identifier(s))as arguments.
         */
        public static Note newInstance(Timestamp creationDate, Timestamp lastModificationDate,
                Timestamp lastDiscussionNoteCreationDate, Timestamp crawlLastModificationDate,
                NoteCreationSource creationSource, boolean direct, NoteStatus status, Long version,
                String discussionPath, Long discussionId, boolean mentionTopicReaders,
                boolean mentionTopicAuthors, boolean mentionTopicManagers,
                boolean mentionDiscussionAuthors, GlobalId globalId, Blog blog,
                Set<User> favoriteUsers, Set<Note> children, Note parent, Set<Note> versionOf,
                Note origin, Set<Blog> crosspostBlogs, Set<GlobalId> followableItems,
                Set<NoteProperty> properties, Set<User> directUsers, User user, Set<Tag> tags,
                Set<User> usersToBeNotified, Set<Attachment> attachments, Content content) {
            final Note entity = new Note();
            entity.setCreationDate(creationDate);
            entity.setLastModificationDate(lastModificationDate);
            entity.setLastDiscussionNoteCreationDate(lastDiscussionNoteCreationDate);
            entity.setCrawlLastModificationDate(crawlLastModificationDate);
            entity.setCreationSource(creationSource);
            entity.setDirect(direct);
            entity.setStatus(status);
            entity.setVersion(version);
            entity.setDiscussionPath(discussionPath);
            entity.setDiscussionId(discussionId);
            entity.setMentionTopicReaders(mentionTopicReaders);
            entity.setMentionTopicAuthors(mentionTopicAuthors);
            entity.setMentionTopicManagers(mentionTopicManagers);
            entity.setMentionDiscussionAuthors(mentionDiscussionAuthors);
            entity.setGlobalId(globalId);
            entity.setBlog(blog);
            entity.setFavoriteUsers(favoriteUsers);
            entity.setChildren(children);
            entity.setParent(parent);
            entity.setVersionOf(versionOf);
            entity.setOrigin(origin);
            entity.setCrosspostBlogs(crosspostBlogs);
            entity.setFollowableItems(followableItems);
            entity.setProperties(properties);
            entity.setDirectUsers(directUsers);
            entity.setUser(user);
            entity.setTags(tags);
            entity.setUsersToBeNotified(usersToBeNotified);
            entity.setAttachments(attachments);
            entity.setContent(content);
            return entity;
        }
    }

    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = -7968085337872565641L;

    private Timestamp creationDate;

    private Timestamp lastModificationDate;

    private Timestamp lastDiscussionNoteCreationDate;

    private Timestamp crawlLastModificationDate;

    private NoteCreationSource creationSource;

    private boolean direct;

    private NoteStatus status;

    private Long version;

    private String discussionPath;

    private Long discussionId;

    private boolean mentionTopicReaders;

    private boolean mentionTopicAuthors;

    private boolean mentionTopicManagers;

    private boolean mentionDiscussionAuthors;

    private Long id;

    private GlobalId globalId;

    private Blog blog;

    private Set<User> favoriteUsers = new HashSet<User>();

    private Set<Note> children = new HashSet<Note>();

    private Note parent;

    private Set<Note> versionOf = new HashSet<Note>();

    private Note origin;

    private Set<Blog> crosspostBlogs = new HashSet<Blog>();

    private Set<GlobalId> followableItems = new HashSet<GlobalId>();

    private Set<NoteProperty> properties = new HashSet<NoteProperty>();

    private Set<User> directUsers = new HashSet<User>();

    private User user;

    private Set<Tag> tags = new HashSet<Tag>();

    private Set<User> usersToBeNotified = new HashSet<User>();

    private Set<Attachment> attachments = new HashSet<Attachment>();

    private Content content;

    /**
     * Builds a string showing the current attribute values
     */

    public String attributesToString() {
        StringBuilder sb = new StringBuilder();

        sb.append("class='");
        sb.append(this.getClass().getName());
        sb.append("', ");

        sb.append("creationDate='");
        sb.append(creationDate);
        sb.append("', ");

        sb.append("lastModificationDate='");
        sb.append(lastModificationDate);
        sb.append("', ");

        sb.append("lastDiscussionNoteCreationDate='");
        sb.append(lastDiscussionNoteCreationDate);
        sb.append("', ");

        sb.append("crawlLastModificationDate='");
        sb.append(crawlLastModificationDate);
        sb.append("', ");

        sb.append("creationSource='");
        sb.append(creationSource);
        sb.append("', ");

        sb.append("direct='");
        sb.append(direct);
        sb.append("', ");

        sb.append("status='");
        sb.append(status);
        sb.append("', ");

        sb.append("version='");
        sb.append(version);
        sb.append("', ");

        sb.append("discussionPath='");
        sb.append(discussionPath);
        sb.append("', ");

        sb.append("discussionId='");
        sb.append(discussionId);
        sb.append("', ");

        sb.append("mentionTopicReaders='");
        sb.append(mentionTopicReaders);
        sb.append("', ");

        sb.append("mentionTopicAuthors='");
        sb.append(mentionTopicAuthors);
        sb.append("', ");

        sb.append("mentionTopicManagers='");
        sb.append(mentionTopicManagers);
        sb.append("', ");

        sb.append("mentionDiscussionAuthors='");
        sb.append(mentionDiscussionAuthors);
        sb.append("', ");

        sb.append("id='");
        sb.append(id);
        sb.append("', ");

        return sb.toString();
    }

    /**
     * Returns <code>true</code> if the argument is an Note instance and all identifiers for this
     * entity equal the identifiers of the argument entity. Returns <code>false</code> otherwise.
     */
    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof Note)) {
            return false;
        }
        final Note that = (Note) object;
        if (this.id == null || that.getId() == null || !this.id.equals(that.getId())) {
            return false;
        }
        return true;
    }

    /**
     *
     */
    public Set<Attachment> getAttachments() {
        return this.attachments;
    }

    /**
     *
     */
    public Blog getBlog() {
        return this.blog;
    }

    /**
     *
     */
    public Set<Note> getChildren() {
        return this.children;
    }

    /**
     *
     */
    public Content getContent() {
        return this.content;
    }

    /**
     * <p>
     * The last modification date of the blog.
     * </p>
     */
    public Timestamp getCrawlLastModificationDate() {
        return this.crawlLastModificationDate;
    }

    /**
     * <p>
     * The date of the tag
     * </p>
     */
    public Timestamp getCreationDate() {
        return this.creationDate;
    }

    /**
     * <p>
     * Defines the source of creation.
     * </p>
     */
    public NoteCreationSource getCreationSource() {
        return this.creationSource;
    }

    /**
     * <p>
     * Denotes the crosspost targets of an autosave.
     * </p>
     */
    public Set<Blog> getCrosspostBlogs() {
        return this.crosspostBlogs;
    }

    /**
     *
     */
    public Set<User> getDirectUsers() {
        return this.directUsers;
    }

    /**
     * <p>
     * The ID of the fist parent within a discussion.
     * </p>
     */
    public Long getDiscussionId() {
        return this.discussionId;
    }

    /**
     * <p>
     * The discussion path of the format /parent_parent_id/parent_id/my_id
     * </p>
     */
    public String getDiscussionPath() {
        return this.discussionPath;
    }

    /**
     *
     */
    public Set<User> getFavoriteUsers() {
        return this.favoriteUsers;
    }

    /**
     *
     */
    public Set<GlobalId> getFollowableItems() {
        return this.followableItems;
    }

    @Override
    public GlobalId getFollowId() {
        Long rootNoteId = getDiscussionId();
        if (rootNoteId.equals(getId())) {
            return getGlobalId();
        } else {
            Note parent = getParent();
            while (parent != null) {
                if (parent.getId().equals(this.getDiscussionId())) {
                    return parent.getGlobalId();
                }
                parent = parent.getParent();
            }
            throw new IllegalStateException("Note " + getId() + " references not existing note "
                    + rootNoteId + " as discussion root");
        }
    }

    @Override
    public GlobalId getGlobalId() {
        return this.globalId;
    }

    /**
     *
     */
    public Long getId() {
        return this.id;
    }

    /**
     * <p>
     * The date of youngest/newest comment within the discussion. It is only maintained for the root
     * note.
     * </p>
     */
    public Timestamp getLastDiscussionNoteCreationDate() {
        return this.lastDiscussionNoteCreationDate;
    }

    /**
     * <p>
     * The date the UTR has been modified last
     * </p>
     */
    public Timestamp getLastModificationDate() {
        return this.lastModificationDate;
    }

    /**
     *
     */
    public Note getOrigin() {
        return this.origin;
    }

    /**
     *
     */
    public Note getParent() {
        return this.parent;
    }

    /**
     *
     */
    @Override
    public Set<NoteProperty> getProperties() {
        return this.properties;
    }

    /**
     *
     */
    public NoteStatus getStatus() {
        return this.status;
    }

    /**
     * <p>
     * The tags which are assigned by user
     * </p>
     */
    @Override
    public Set<Tag> getTags() {
        return this.tags;
    }

    /**
     *
     */
    public User getUser() {
        return this.user;
    }

    /**
     * @return the users who were explicitly mentioned in the note. In case no users were mentioned
     *         the set will be empty. Never returns null.
     */
    public Set<User> getUsersToBeNotified() {
        return this.usersToBeNotified;
    }

    /**
     *
     */
    public Long getVersion() {
        return this.version;
    }

    /**
     *
     */
    public Set<Note> getVersionOf() {
        return this.versionOf;
    }

    /**
     * Returns a hash code based on this entity's identifiers.
     */
    @Override
    public int hashCode() {
        int hashCode = 0;
        hashCode = 29 * hashCode + (id == null ? 0 : id.hashCode());

        return hashCode;
    }

    /**
     * <p>
     * True if it is a direct message
     * </p>
     */
    public boolean isDirect() {
        return this.direct;
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

    public void setAttachments(Set<Attachment> attachments) {
        this.attachments = attachments;
    }

    public void setBlog(Blog blog) {
        this.blog = blog;
    }

    public void setChildren(Set<Note> children) {
        this.children = children;
    }

    public void setContent(Content content) {
        this.content = content;
    }

    public void setCrawlLastModificationDate(Timestamp crawlLastModificationDate) {
        this.crawlLastModificationDate = crawlLastModificationDate;
    }

    public void setCreationDate(Timestamp creationDate) {
        this.creationDate = creationDate;
    }

    public void setCreationSource(NoteCreationSource creationSource) {
        this.creationSource = creationSource;
    }

    public void setCrosspostBlogs(Set<Blog> crosspostBlogs) {
        this.crosspostBlogs = crosspostBlogs;
    }

    public void setDirect(boolean direct) {
        this.direct = direct;
    }

    public void setDirectUsers(Set<User> directUsers) {
        this.directUsers = directUsers;
    }

    public void setDiscussionId(Long discussionId) {
        this.discussionId = discussionId;
    }

    public void setDiscussionPath(String discussionPath) {
        this.discussionPath = discussionPath;
    }

    public void setFavoriteUsers(Set<User> favoriteUsers) {
        this.favoriteUsers = favoriteUsers;
    }

    public void setFollowableItems(Set<GlobalId> followableItems) {
        this.followableItems = followableItems;
    }

    @Override
    public void setGlobalId(GlobalId globalId) {
        this.globalId = globalId;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setLastDiscussionNoteCreationDate(Timestamp lastDiscussionNoteCreationDate) {
        this.lastDiscussionNoteCreationDate = lastDiscussionNoteCreationDate;
    }

    public void setLastModificationDate(Timestamp lastModificationDate) {
        this.lastModificationDate = lastModificationDate;
        // also set the crawl last modification date if the given last modification is younger
        if (getCrawlLastModificationDate() == null) {
            this.setCrawlLastModificationDate(lastModificationDate);
        } else if (lastModificationDate != null
                && lastModificationDate.getTime() > getCrawlLastModificationDate().getTime()) {
            this.setCrawlLastModificationDate(lastModificationDate);
        }
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

    public void setOrigin(Note origin) {
        this.origin = origin;
    }

    public void setParent(Note parent) {
        this.parent = parent;
    }

    public void setProperties(Set<NoteProperty> properties) {
        this.properties = properties;
    }

    public void setStatus(NoteStatus status) {
        this.status = status;
    }

    @Override
    public void setTags(Set<Tag> tags) {
        this.tags = tags;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setUsersToBeNotified(Set<User> usersToBeNotified) {
        if (usersToBeNotified == null) {
            usersToBeNotified = new HashSet<>();
        }
        this.usersToBeNotified = usersToBeNotified;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public void setVersionOf(Set<Note> versionOf) {
        this.versionOf = versionOf;
    }

}