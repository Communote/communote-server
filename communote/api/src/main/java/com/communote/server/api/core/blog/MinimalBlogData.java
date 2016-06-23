package com.communote.server.api.core.blog;

import com.communote.server.api.core.common.IdentifiableEntityData;

/**
 * Value object holding the minimal info about a blog/topic.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class MinimalBlogData extends IdentifiableEntityData {

    private static final long serialVersionUID = -243655436057138534L;

    private String title;

    private String nameIdentifier;

    /**
     * Create a new empty list item.
     */
    public MinimalBlogData() {
        super();
        this.title = null;
    }

    /**
     * Create a new list item.
     *
     * @param id
     *            the ID of the topic
     * @param alias
     *            the alias of the topic
     * @param title
     *            the title of the topic
     */
    public MinimalBlogData(Long id, String alias, String title) {
        super();
        setId(id);
        this.title = title;
        this.nameIdentifier = alias;
    }

    /**
     * @return the alias of the topic
     */
    public String getAlias() {
        return nameIdentifier;
    }

    /**
     * @deprecated Use {@link #getId()}
     */
    @Deprecated
    public Long getBlogId() {
        return getId();
    }

    /**
     * @return the same as {@link #getAlias()}
     */
    public String getNameIdentifier() {
        return this.nameIdentifier;
    }

    /**
     * @return the title of the topic
     */
    public String getTitle() {
        return this.title;
    }

    /**
     * Set the alias of the topic
     *
     * @param alias
     *            The alias of the topic
     */
    public void setAlias(String alias) {
        this.nameIdentifier = alias;
    }

    /**
     * @deprecated Use {@link #setId(Long)}
     */
    @Deprecated
    public void setBlogId(Long blogId) {
        setId(blogId);
    }

    /**
     * Calls {@link #setAlias(String)}.
     *
     * @param alias
     *            The alias of the topic
     */
    public void setNameIdentifier(String nameIdentifier) {
        this.setAlias(nameIdentifier);
    }

    /**
     * Set the title of the topic
     *
     * @param title
     *            the title of the topic
     */
    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String toString() {
        return "MinimalBlogData [title=" + title + ", nameIdentifier=" + nameIdentifier + "]";
    }

}