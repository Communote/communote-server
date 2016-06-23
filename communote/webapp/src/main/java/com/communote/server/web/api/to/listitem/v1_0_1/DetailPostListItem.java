package com.communote.server.web.api.to.listitem.v1_0_1;

import java.util.Collection;
import java.util.Date;

import com.communote.server.api.core.attachment.AttachmentData;
import com.communote.server.web.api.to.listitem.CommonPostListItem;
import com.communote.server.web.api.to.listitem.DiscussionListItem;


/*
 * Note: Members should not be modified because it would break API compatibility
 */

/**
 * List item describing a note that should be used by the old API in versions since 1.0.1
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class DetailPostListItem extends CommonPostListItem implements
        java.io.Serializable {
    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = 668388105636085061L;

    private boolean direct;

    private boolean favorite;

    private Date creationDate;

    private Date lastModificationDate;

    private int numberOfComments;

    private String tags;

    private Long parentPostId;

    private Long blogId;

    private Long userId;

    private Long parentUserId;

    private Collection<AttachmentData> attachments;

    private DiscussionListItem discussion;

    /**
     * Creates a new instance
     */
    public DetailPostListItem() {
        super();
        this.creationDate = null;
        this.lastModificationDate = null;
        this.numberOfComments = 0;
        this.tags = null;
        this.parentPostId = null;
        this.blogId = null;
        this.userId = null;
    }

    /**
     * @return a collection with details about the attachments that were added to the note
     */
    public Collection<AttachmentData> getAttachments() {
        return this.attachments;
    }

    /**
     * @return ID of the blog of the note
     */
    public Long getBlogId() {
        return this.blogId;
    }

    /**
     * @return the date the note was created
     */
    public Date getCreationDate() {
        return this.creationDate;
    }

    /**
     * @return an object with details about the discussion this note belongs to
     */
    public DiscussionListItem getDiscussion() {
        return this.discussion;
    }

    /**
     * @return the date of the last modification of the note
     */
    public Date getLastModificationDate() {
        return this.lastModificationDate;
    }

    /**
     * Returns the number of replies to this note. This includes replies on replies and also replies
     * that are direct messages which the current user might not be able to read.
     * 
     * @return the number of replies
     */
    public int getNumberOfComments() {
        return this.numberOfComments;
    }

    /**
     * @return the ID of the parent note of the note or null if the note is not a comment
     */
    public Long getParentPostId() {
        return this.parentPostId;
    }

    /**
     * @return user ID of the author of the parent note of the note or null if the note is not a
     *         comment
     */
    public Long getParentUserId() {
        return this.parentUserId;
    }

    /**
     * @return the tags of the note as CSV
     */
    public String getTags() {
        return this.tags;
    }

    /**
     * @return the user ID of the author of the note
     */
    public Long getUserId() {
        return this.userId;
    }

    /**
     * @return whether the note is a direct message
     */
    public boolean isDirect() {
        return direct;
    }

    /**
     * @return whether the current user marked the note as a favorite
     */
    public boolean isFavorite() {
        return favorite;
    }

    /**
     * Sets the attachments
     * 
     * @param attachments
     *            the attachments that were added to the note
     */
    public void setAttachments(Collection<AttachmentData> attachments) {
        this.attachments = attachments;
    }

    /**
     * Sets the ID of the blog of the note
     * 
     * @param blogId
     *            the ID of the blog of the note
     */
    public void setBlogId(Long blogId) {
        this.blogId = blogId;
    }

    /**
     * @param creationDate
     *            the creation date of the note
     */
    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    /**
     * @param direct
     *            true if this note is a direct message, false otherwise
     */
    public void setDirect(boolean direct) {
        this.direct = direct;
    }

    /**
     * Sets the discussion
     * 
     * @param discussion
     *            object with details about the discussion this note belongs to
     */
    public void setDiscussion(DiscussionListItem discussion) {
        this.discussion = discussion;
    }

    /**
     * @param favorite
     *            true if the current user marked this note as a favorite, false otherwise
     */
    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }

    /**
     * @param lastModificationDate
     *            the date of the last modification of the note
     */
    public void setLastModificationDate(Date lastModificationDate) {
        this.lastModificationDate = lastModificationDate;
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
     * @param parentPostId
     *            the ID of the parent note of the note or null if the note is not a comment
     */
    public void setParentPostId(Long parentPostId) {
        this.parentPostId = parentPostId;
    }

    /**
     * @param parentUserId
     *            user ID of the author of the parent note of the note or null if the note is not a
     *            comment
     */
    public void setParentUserId(Long parentUserId) {
        this.parentUserId = parentUserId;
    }

    /**
     * @param tags
     *            the tags of the note as CSV
     */
    public void setTags(String tags) {
        this.tags = tags;
    }

    /**
     * @param userId
     *            the user ID of the author of the note
     */
    public void setUserId(Long userId) {
        this.userId = userId;
    }

}