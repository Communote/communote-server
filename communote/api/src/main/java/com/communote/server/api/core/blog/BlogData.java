package com.communote.server.api.core.blog;

import java.io.Serializable;
import java.util.Date;

/**
 * Value object holding the data of a blog/topic.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class BlogData extends MinimalBlogData implements Serializable {

    private static final long serialVersionUID = 3926297564593777944L;

    private String description;

    private Date lastModificationDate;
    private Date creationDate;

    /**
     * constructor
     */
    public BlogData() {
        super();
    }

    /**
     * constructor
     *
     * @param id
     *            the ID of the topic
     */
    public BlogData(Long id) {
        setId(id);
    }

    /**
     * constructor.
     *
     * @param alias
     *            the alias of the topic
     * @param id
     *            the ID of the topic
     * @param title
     *            the title of the topic
     * @param lastModificationDate
     *            the date at which the topic was last modified
     */
    public BlogData(String alias, Long id, String title, Date lastModificationDate) {
        this(alias, id, title, null, lastModificationDate);
    }

    /**
     * constructor.
     *
     * @param alias
     *            the alias of the topic
     * @param id
     *            the ID of the topic
     * @param title
     *            the title of the topic
     * @param creationDate
     *            the creation date of the topic
     * @param lastModificationDate
     *            the date at which the topic was last modified
     */
    public BlogData(String alias, Long id, String title, Date creationDate,
            Date lastModificationDate) {
        this(alias, null, id, title, lastModificationDate);
        this.creationDate = creationDate;
    }

    /**
     * constructor
     *
     * @param alias
     *            the alias of the topic
     * @param id
     *            the ID of the topic
     * @param title
     *            the title of the topic
     * @param lastModificationDate
     *            the date at which the topic was last modified.
     */
    public BlogData(String alias, String description, Long id, String title,
            Date lastModificationDate) {
        super(id, alias, title);
        this.lastModificationDate = lastModificationDate;
        this.description = description;
    }

    /**
     * @return the creation date of the blog
     */
    public Date getCreationDate() {
        return creationDate;
    }

    /**
     * @return the description of the topic
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * @return the date at which the topic was last modified
     */
    public Date getLastModificationDate() {
        return this.lastModificationDate;
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
     * Set the description of the topic
     *
     * @param description
     *            the description of the topic
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Set the date of the last modification of the topic
     *
     * @param lastModificationDate
     *            the date at which the topic was last modified
     */
    public void setLastModificationDate(Date lastModificationDate) {
        this.lastModificationDate = lastModificationDate;
    }

    @Override
    public String toString() {
        return "BlogData [description=" + description + ", lastModificationDate="
                + lastModificationDate + ", creationDate=" + creationDate
                + ", getNameIdentifier()=" + getNameIdentifier() + ", getId()=" + getId() + "]";
    }

}