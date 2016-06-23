package com.communote.server.web.api.to.listitem;

import java.util.Date;

/*
 * Note: Members should not be modified because it would break API compatibility
 */

/**
 * List item describing a note that should be used by the old API when the short note format is
 * requested.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class PostListItem extends CommonPostListItem implements java.io.Serializable {
    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = -6982748002056511827L;

    private Long userId;

    private Date creationDate;

    private Date lastModificationDate;

    private Long blogId;

    /**
     * Creates a new instance
     */
    public PostListItem() {
        super();
        this.userId = null;
        this.creationDate = null;
        this.lastModificationDate = null;
        this.blogId = null;
    }

    /**
     * Copies all properties from the argument value object into this value object.
     * 
     * @param otherBean
     *            the object to copy from
     */
    public void copy(PostListItem otherBean) {
        if (otherBean != null) {
            this.setUserId(otherBean.getUserId());
            this.setCreationDate(otherBean.getCreationDate());
            this.setLastModificationDate(otherBean.getLastModificationDate());
            this.setBlogId(otherBean.getBlogId());
            this.setPostId(otherBean.getPostId());
            this.setText(otherBean.getText());
        }
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
     * @return the date of the last modification of the note
     */
    public Date getLastModificationDate() {
        return this.lastModificationDate;
    }

    /**
     * @return the user ID of the author of the note
     */
    public Long getUserId() {
        return this.userId;
    }

    /**
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
     * @param lastModificationDate
     *            the date of the last modification of the note
     */
    public void setLastModificationDate(Date lastModificationDate) {
        this.lastModificationDate = lastModificationDate;
    }

    /**
     * @param userId
     *            the user ID of the author of the note
     */
    public void setUserId(Long userId) {
        this.userId = userId;
    }

}