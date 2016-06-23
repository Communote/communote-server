package com.communote.server.web.api.to.listitem;

import java.util.Date;

import com.communote.server.api.core.common.IdentifiableEntityData;

/**
 * Legacy list item for the old rest API.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class BlogListItem extends IdentifiableEntityData {
    private String title;

    private Date lastModificationDate;
    private Date creationDate;

    /**
     * Backwards compatibility method.
     * 
     * @return the same as {@link #getId()}.
     */
    public Long getBlogId() {
        return getId();
    }

    /**
     * @return the creation date of the blog
     */
    public Date getCreationDate() {
        return creationDate;
    }

    /**
     * @return the last modification date of the topic
     */
    public Date getLastModificationDate() {
        return this.lastModificationDate;
    }

    /**
     * @return the title of the topic
     */
    public String getTitle() {
        return this.title;
    }

    /**
     * Backwards compatibility method which delegates to {@link #setId(Long)}
     * 
     * @param blogId
     *            set the ID of the topic
     */
    public void setBlogId(Long blogId) {
        setId(blogId);
    }

    /**
     * Set the creation date of the blog
     * 
     * @param creationDate
     *            the creation date of the blog
     */
    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    /**
     * Set the last modification date of the blog
     * 
     * @param lastModificationDate
     *            the modification date of the blog
     */
    public void setLastModificationDate(Date lastModificationDate) {
        this.lastModificationDate = lastModificationDate;
    }

    /**
     * Set the title of the topic
     * 
     * @param title
     *            the title
     */
    public void setTitle(String title) {
        this.title = title;
    }
}
