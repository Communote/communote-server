package com.communote.server.api.core.blog;

import com.communote.server.model.blog.BlogRole;

/**
 * Value object which holds the data of a blog/topic and the role of the current user in that topic.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class UserBlogData extends BlogData implements java.io.Serializable {
    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = -2227039527716329462L;

    private BlogRole userRole;

    public UserBlogData() {
        super();
        this.userRole = null;
    }

    public UserBlogData(com.communote.server.model.blog.BlogRole userRole,
            String nameIdentifier, String description, Long blogId, String title,
            java.util.Date lastModificationDate) {
        super(nameIdentifier, description, blogId, title, lastModificationDate);
        this.userRole = userRole;
    }

    /**
     * Copies constructor from other UserBlogData
     *
     * @param otherBean
     *            , cannot be <code>null</code>
     * @throws NullPointerException
     *             if the argument is <code>null</code>
     */
    public UserBlogData(UserBlogData otherBean) {
        this(otherBean.getUserRole(), otherBean.getNameIdentifier(), otherBean.getDescription(),
                otherBean.getId(), otherBean.getTitle(), otherBean.getLastModificationDate());
    }

    /**
     * Copies all properties from the argument value object into this value object.
     */
    public void copy(UserBlogData otherBean) {
        if (otherBean != null) {
            this.setUserRole(otherBean.getUserRole());
            this.setNameIdentifier(otherBean.getNameIdentifier());
            this.setDescription(otherBean.getDescription());
            this.setId(otherBean.getId());
            this.setTitle(otherBean.getTitle());
            this.setLastModificationDate(otherBean.getLastModificationDate());
        }
    }

    /**
     * @return the role the user has in the blog
     */
    public BlogRole getUserRole() {
        return this.userRole;
    }

    /**
     * Sets the role the user has in the blog.
     *
     * @param userRole
     *            the role of the user
     */
    public void setUserRole(BlogRole userRole) {
        this.userRole = userRole;
    }

}