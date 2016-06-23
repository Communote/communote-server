package com.communote.server.core.filter.listitems.blog;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class BlogTagListItem extends com.communote.server.api.core.blog.BlogData
implements java.io.Serializable {
    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = 2429788117751331004L;

    private java.util.Collection<com.communote.server.api.core.tag.TagData> tags;

    public BlogTagListItem() {
        super();
    }

    /**
     * Copies constructor from other BlogTagListItem
     *
     * @param otherBean
     *            , cannot be <code>null</code>
     * @throws NullPointerException
     *             if the argument is <code>null</code>
     */
    public BlogTagListItem(BlogTagListItem otherBean) {
        this(otherBean.getNameIdentifier(), otherBean.getDescription(), otherBean.getId(),
                otherBean.getTitle(), otherBean.getLastModificationDate(), otherBean.getTags());
    }

    public BlogTagListItem(String nameIdentifier, String description, Long blogId, String title,
            java.util.Date lastModificationDate) {
        super(nameIdentifier, description, blogId, title, lastModificationDate);
    }

    public BlogTagListItem(String nameIdentifier, String description, Long blogId, String title,
            java.util.Date lastModificationDate,
            java.util.Collection<com.communote.server.api.core.tag.TagData> tags) {
        super(nameIdentifier, description, blogId, title, lastModificationDate);
        this.tags = tags;
    }

    /**
     * Copies all properties from the argument value object into this value object.
     */
    public void copy(BlogTagListItem otherBean) {
        if (otherBean != null) {
            this.setNameIdentifier(otherBean.getNameIdentifier());
            this.setDescription(otherBean.getDescription());
            this.setId(otherBean.getId());
            this.setTitle(otherBean.getTitle());
            this.setLastModificationDate(otherBean.getLastModificationDate());
            this.setTags(otherBean.getTags());
        }
    }

    /**
     * Get the tags
     *
     */
    public java.util.Collection<com.communote.server.api.core.tag.TagData> getTags() {
        return this.tags;
    }

    /**
     * Sets the tags
     */
    public void setTags(java.util.Collection<com.communote.server.api.core.tag.TagData> tags) {
        this.tags = tags;
    }

}