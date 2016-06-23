package com.communote.server.api.core.note;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.communote.common.matcher.Matchable;
import com.communote.server.api.core.attachment.AttachmentData;
import com.communote.server.api.core.blog.UserBlogData;
import com.communote.server.api.core.common.IdentifiableEntityData;
import com.communote.server.api.core.property.StringPropertyTO;
import com.communote.server.api.core.property.StringPropertyableTO;
import com.communote.server.api.core.security.permission.Permission;
import com.communote.server.api.core.tag.TagData;
import com.communote.server.api.core.user.DetailedUserData;
import com.communote.server.model.note.Note;
import com.communote.server.model.note.NoteCreationSource;

/**
 * Value object holding the data of a note.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class NoteData extends IdentifiableEntityData implements StringPropertyableTO, Matchable {

    private static final long serialVersionUID = 8002573699655778516L;

    private UserBlogData blog;
    private Date creationDate;
    private Date lastModificationDate;
    private Date lastDiscussionCreationDate;
    private NoteCreationSource creationSource;
    private List<AttachmentData> attachments = new ArrayList<AttachmentData>();
    private final Map<String, List<AttachmentData>> filteredAttachments = new HashMap<String, List<AttachmentData>>();

    private List<TagData> tags = new ArrayList<TagData>();
    private List<DetailedUserData> notifiedUsers = new ArrayList<DetailedUserData>();
    private Long discussionId;
    private int discussionDepth;
    private String discussionPath;
    private int numberOfComments = 0;
    private int numberOfDiscussionNotes = 0;
    private NoteData parent;
    private String content;
    private String shortContent;

    private DetailedUserData user;
    private boolean direct = false;
    private boolean favorite = false;
    private boolean forMe = false;
    private boolean systemNote = false;
    private Long version;
    private Set<Permission<Note>> permissions = new HashSet<Permission<Note>>();
    private Set<StringPropertyTO> properties;
    private boolean mentionTopicReaders;
    private boolean mentionTopicAuthors;
    private boolean mentionTopicManagers;
    private boolean mentionDiscussionAuthors;

    private boolean matching;

    /**
     * Add an object property.
     *
     * @param property
     *            the property to add
     */
    public void addObjectProperty(StringPropertyTO property) {
        if (property != null) {
            if (this.properties == null) {
                this.properties = new HashSet<StringPropertyTO>();
            }
            this.properties.add(property);
        }
    }

    /**
     * @return a collection with details about the attachments that were added to the note
     */
    public List<AttachmentData> getAttachments() {
        return attachments;
    }

    /**
     * Returns a MIME type filtered subset of the attachments that were added to the note.
     *
     * @param filter
     *            a regular expression the MIME type must match for the attachment to be included
     * @return the filtered attachments
     */
    public List<AttachmentData> getAttachments(String filter) {

        if (!this.filteredAttachments.containsKey(filter)) {
            List<AttachmentData> attachments = new ArrayList<AttachmentData>();

            for (AttachmentData attachment : this.attachments) {
                if (attachment.getMimeTyp().matches(filter)) {
                    attachments.add(attachment);
                }
            }
            this.filteredAttachments.put(filter, attachments);
        }

        return this.filteredAttachments.get(filter);
    }

    /**
     * @return a list item with details about the blog of the note
     */
    public UserBlogData getBlog() {
        return blog;
    }

    /**
     * @return the content of the note
     */
    public String getContent() {
        return content;
    }

    /**
     * @return the date the note was created
     */
    public Date getCreationDate() {
        return creationDate;
    }

    /**
     * @return the source which sent the note
     */
    public NoteCreationSource getCreationSource() {
        return creationSource;
    }

    /**
     * @return the depth of the note within the discussion. The root note of the discussion has
     *         depth 0. A reply on the root note note has depth 1, a reply on that reply depth 2 and
     *         so on.
     */
    public int getDiscussionDepth() {
        return discussionDepth;
    }

    /**
     * @return the ID of the discussion the note belongs to
     */
    public Long getDiscussionId() {
        return discussionId;
    }

    /**
     * @return The discussionPath <b>separated with spaces</b> instead of forward slashes, i.e.
     *         "12 34 56".
     */
    public String getDiscussionPath() {
        return discussionPath;
    }

    /**
     * @return The date, the last note of this discussion was created.
     */
    public Date getLastDiscussionCreationDate() {
        return lastDiscussionCreationDate;
    }

    /**
     * @return the date of the last modification of the note. This value will equal the creation
     *         date if the note wasn't modified yet.
     */
    public Date getLastModificationDate() {
        return lastModificationDate;
    }

    /**
     * @return a collection with details about the users that were notified about the note
     */
    public List<DetailedUserData> getNotifiedUsers() {
        return notifiedUsers;
    }

    /**
     * Returns the number of replies to this note. This includes replies on replies and also replies
     * that are direct messages which the current user might not be able to read.
     *
     * @return the number of replies
     */
    public int getNumberOfComments() {
        return numberOfComments;
    }

    /**
     * Returns the number of notes in the discussion this note is part of. Notes the current user is
     * not allowed to read, i.e. direct messages that were not sent to that user, are not included.
     *
     * @return the number of notes in the discussion
     */
    public int getNumberOfDiscussionNotes() {
        return numberOfDiscussionNotes;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<StringPropertyTO> getObjectProperties() {
        return properties;
    }

    /**
     * @return The parent of this note. The parent may not contain correct data about its children
     *         or allTags. Moreover, the parent of the parent usually won't be contained.
     */
    public NoteData getParent() {
        return parent;
    }

    /**
     * @return the permissions
     */
    public Set<Permission<Note>> getPermissions() {
        return permissions;
    }

    /**
     * @return a shortened version of the content of the note, can be null if the content was short
     *         enough so that no shortened content had to be generated
     */
    public String getShortContent() {
        return shortContent;
    }

    /**
     * @return the tags added to this note
     */
    public List<TagData> getTags() {
        return tags;
    }

    /**
     * @return an object with details about the author of the note
     */
    public DetailedUserData getUser() {
        return user;
    }

    /**
     * @return the autosave version of the note
     */
    public Long getVersion() {
        return version;
    }

    /**
     * Checks, if the note has the given permission.
     *
     * @param permission
     *            The permission.
     * @return True, if the NoteData has the given permission.
     */
    public boolean hasPermission(Permission<Note> permission) {
        return permissions.contains(permission);
    }

    /**
     * Checks, if the note has the given permission.
     *
     * @param permissionIdentifier
     *            The permissions identifier as string.
     * @return True, if the NoteData has the given permission.
     */
    public boolean hasPermission(String permissionIdentifier) {
        for (Permission<Note> permission : permissions) {
            if (permission.getIdentifier().equals(permissionIdentifier)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns whether the current user can reply to this note. This is only possible if the current
     * user has write access to the blog of the note and it is not a system note. Moreover, the
     * author of the note must not have been anonymized.
     *
     * @return true if the current user can reply to this note
     */
    public boolean isCommentable() {
        return permissions.contains(NotePermissionManagement.PERMISSION_COMMENT);
    }

    /**
     * Whether the current user can delete this note. Deleting is only possible if the current user
     * is the manager of the blog or he is the author of the note and there are no comments to the
     * note and it is not a direct message.
     *
     * @return true if the current user can delete this note, false otherwise
     */
    public boolean isDeletable() {
        return permissions.contains(NotePermissionManagement.PERMISSION_DELETE);
    }

    /**
     * @return whether the note is a direct message
     */
    public boolean isDirect() {
        return direct;
    }

    /**
     * @return whether the current user can edit this note
     */
    public boolean isEditable() {
        return permissions.contains(NotePermissionManagement.PERMISSION_EDIT);
    }

    /**
     * @return whether the current user marked the note as a favorite
     */
    public boolean isFavorite() {
        return favorite;
    }

    /**
     * @return whether the current user was one of the notified users
     */
    public boolean isForMe() {
        return forMe;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isMatched() {
        return matching;
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
     * @return whether the note can be reposted by other users. Will be true if the note has the
     *         REPOST permission, false otherwise.
     */
    public boolean isRepostable() {
        return permissions.contains(NotePermissionManagement.PERMISSION_REPOST);
    }

    /**
     * @return whether this is a system note
     */
    public boolean isSystemNote() {
        return systemNote;
    }

    /**
     * @param attachments
     *            the attachments that were added to the note
     */
    public void setAttachments(List<AttachmentData> attachments) {
        this.attachments = attachments;
    }

    /**
     * @param blog
     *            the blog to set
     */
    public void setBlog(UserBlogData blog) {
        this.blog = blog;
    }

    /**
     * @param content
     *            the content to set
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * @param creationDate
     *            the creation date of the note
     */
    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    /**
     * @param creationSource
     *            the source which sent the note
     */
    public void setCreationSource(NoteCreationSource creationSource) {
        this.creationSource = creationSource;
    }

    /**
     * @param direct
     *            true if this note is a direct message, false otherwise
     */
    public void setDirect(boolean direct) {
        this.direct = direct;
    }

    /**
     * @param discussionDepth
     *            the depth of the note within the discussion
     * @see NoteData#getDiscussionDepth()
     */
    public void setDiscussionDepth(int discussionDepth) {
        this.discussionDepth = discussionDepth;
    }

    /**
     * @param discussionId
     *            the ID of the discussion this note is a part of
     */
    public void setDiscussionId(Long discussionId) {
        this.discussionId = discussionId;
    }

    /**
     * @param discussionPath
     *            a string that contains all the IDs of the parent notes of this note up to the root
     *            note separated by a slash or space character. The string is expected to start with
     *            the ID of the root note of the discussion.
     */
    public void setDiscussionPath(String discussionPath) {
        if (discussionPath != null) {
            this.discussionPath = discussionPath.replace('/', ' ').trim();
        }
    }

    /**
     * @param favorite
     *            true if the current user marked this note as a favorite, false otherwise
     */
    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }

    /**
     * @param forMe
     *            true if the current user was one of the notified users, false otherwise
     */
    public void setForMe(boolean forMe) {
        this.forMe = forMe;
    }

    /**
     * @param lastDiscussionCreationDate
     *            the lastDiscussionCreationDate to set
     */
    public void setLastDiscussionCreationDate(Date lastDiscussionCreationDate) {
        this.lastDiscussionCreationDate = lastDiscussionCreationDate;
    }

    /**
     * @param lastModificationDate
     *            the date of the last modification of the note
     */
    public void setLastModificationDate(Date lastModificationDate) {
        this.lastModificationDate = lastModificationDate;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setMatched(boolean matching) {
        this.matching = matching;
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
     * @param notifiedUsers
     *            the users that were notified about the note
     */
    public void setNotifiedUsers(List<DetailedUserData> notifiedUsers) {
        this.notifiedUsers = notifiedUsers;
    }

    /**
     * Sets the number of replies to this note. This must include replies on replies and also
     * replies that are direct messages which the current user might not be able to read.
     *
     * @param numberOfComments
     *            the number of replies
     */
    public void setNumberOfComments(int numberOfComments) {
        this.numberOfComments = numberOfComments;
    }

    /**
     * Set the number of notes in the discussion this note belongs to. Notes the current user is not
     * allowed to read, i.e. direct messages that were not sent to that user, must not be included.
     *
     * @param numberOfDiscussionNotes
     *            the number of notes in the discussion
     */
    public void setNumberOfDiscussionNotes(int numberOfDiscussionNotes) {
        this.numberOfDiscussionNotes = numberOfDiscussionNotes;
    }

    /**
     * Set the properties of the note.
     *
     * @param properties
     *            the properties
     */
    public void setObjectProperties(Set<StringPropertyTO> properties) {
        this.properties = properties;
    }

    /**
     * @param parent
     *            the parent note of the note
     */
    public void setParent(NoteData parent) {
        this.parent = parent;
    }

    /**
     * @param permissions
     *            the permissions to set
     */
    public void setPermissions(Set<Permission<Note>> permissions) {
        if (permissions == null) {
            return;
        }
        this.permissions = permissions;
    }

    /**
     * @param shortContent
     *            a shortened version of the content of the note, can be null if there there is no
     *            short version
     */
    public void setShortContent(String shortContent) {
        this.shortContent = shortContent;
    }

    /**
     * @param systemNote
     *            true if the note is a system note, false otherwise
     */
    public void setSystemNote(boolean systemNote) {
        this.systemNote = systemNote;
    }

    /**
     * @param tags
     *            the tags of the note
     */
    public void setTags(List<TagData> tags) {
        this.tags = tags;
    }

    /**
     * @param user
     *            the author of the note
     */
    public void setUser(DetailedUserData user) {
        this.user = user;
    }

    /**
     * @param version
     *            the autosave version of the note
     */
    public void setVersion(Long version) {
        this.version = version;
    }
}
