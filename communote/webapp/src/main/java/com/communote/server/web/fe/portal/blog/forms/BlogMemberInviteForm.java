package com.communote.server.web.fe.portal.blog.forms;

import com.communote.server.persistence.user.InviteUserForm;

/**
 * Formular to hold information to invite a user to a blog.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>, Torstenn Lunze
 */
public class BlogMemberInviteForm extends InviteUserForm {

    /** The role. */
    private String role;

    /** The blog id. */
    private Long blogId;

    /**
     * Gets the blog id.
     * 
     * @return the blog id
     */
    public Long getBlogId() {
        return blogId;
    }

    /**
     * Gets the role.
     * 
     * @return the role
     */
    public String getRole() {
        return role;
    }

    /**
     * Sets the blog id.
     * 
     * @param blogId
     *            the new blog id
     */
    public void setBlogId(Long blogId) {
        this.blogId = blogId;
    }

    /**
     * Sets the role.
     * 
     * @param role
     *            the new role
     */
    public void setRole(String role) {
        this.role = role == null ? null : role.trim();
    }

}
