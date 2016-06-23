package com.communote.server.web.api.to.listitem;

import java.util.Date;

import com.communote.server.api.core.user.UserData;

/*
 * Note: Members should not be modified because it would break API compatibility
 */

/**
 * List item describing a note that should be used by the old API in versions prior to 1.0.1
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class DetailPostListItem extends CommonPostListItem implements java.io.Serializable {
    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = 7935719601341150472L;

    private java.util.Date creationDate;

    private java.util.Date lastModificationDate;

    private int numberOfComments;

    private String tags;

    private Long parentPostId;

    private UserData user;

    private UserData parentPostAuthor;

    private BlogListItem blog;

    /**
     * creates a new instance
     */
    public DetailPostListItem() {
        super();
        this.creationDate = null;
        this.lastModificationDate = null;
        this.numberOfComments = 0;
        this.tags = null;
        this.parentPostId = null;
        this.user = null;
        this.blog = null;
    }

    /**
     * Copies all properties from the argument value object into this value object.
     * 
     * @param otherBean
     *            the object to copy from
     */
    public void copy(DetailPostListItem otherBean) {
        if (otherBean != null) {
            this.setCreationDate(otherBean.getCreationDate());
            this.setLastModificationDate(otherBean.getLastModificationDate());
            this.setNumberOfComments(otherBean.getNumberOfComments());
            this.setTags(otherBean.getTags());
            this.setParentPostId(otherBean.getParentPostId());
            this.setPostId(otherBean.getPostId());
            this.setText(otherBean.getText());
            this.setUser(otherBean.getUser());
            this.setParentPostAuthor(otherBean.getParentPostAuthor());
            this.setBlog(otherBean.getBlog());
        }
    }

    /**
     * @return a list item with details about the blog of the note
     */
    public BlogListItem getBlog() {
        return this.blog;
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
     * @return the total number of direct comments including DMs the current user might not be able
     *         to read and excluding comments on comments
     */
    public int getNumberOfComments() {
        return this.numberOfComments;
    }

    /**
     * @return a list item with details about the author of the parent note of the note or null if
     *         the note is not a comment
     */
    public UserData getParentPostAuthor() {
        return this.parentPostAuthor;
    }

    /**
     * @return the ID of the parent note of the note or null if the note is not a comment
     */
    public Long getParentPostId() {
        return this.parentPostId;
    }

    /**
     * @return the tags of the note as CSV
     */
    public String getTags() {
        return this.tags;
    }

    /**
     * @return a list item with details about the author of the note
     */
    public UserData getUser() {
        return this.user;
    }

    /**
     * Sets the blog of the note
     * 
     * @param blog
     *            a list item with details about the blog of the note
     */
    public void setBlog(BlogListItem blog) {
        this.blog = blog;
    }

    /**
     * Sets the creation date of the note
     * 
     * @param creationDate
     *            the date the note was created
     */
    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    /**
     * Sets the last modification date of the note
     * 
     * @param lastModificationDate
     *            the date of the last modification of the note
     */
    public void setLastModificationDate(Date lastModificationDate) {
        this.lastModificationDate = lastModificationDate;
    }

    /**
     * Sets the number of direct comments including DMs the current user might not be able to read
     * and excluding comments on comments
     * 
     * @param numberOfComments
     *            total number of direct comments
     */
    public void setNumberOfComments(int numberOfComments) {
        this.numberOfComments = numberOfComments;
    }

    /**
     * Sets the author of the parent note
     * 
     * @param parentPostAuthor
     *            a list item with details about the author of the parent note
     */
    public void setParentPostAuthor(UserData parentPostAuthor) {
        this.parentPostAuthor = parentPostAuthor;
    }

    /**
     * Sets the ID of the parent note
     * 
     * @param parentPostId
     *            the ID of the parent note
     */
    public void setParentPostId(Long parentPostId) {
        this.parentPostId = parentPostId;
    }

    /**
     * sets the tags
     * 
     * @param tags
     *            the tags of the note as CSV
     */
    public void setTags(String tags) {
        this.tags = tags;
    }

    /**
     * Sets the author
     * 
     * @param user
     *            a list item with details about the author of the note
     */
    public void setUser(UserData user) {
        this.user = user;
    }

}