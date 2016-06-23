package com.communote.server.api.core.blog;

import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.api.core.security.permission.Permission;
import com.communote.server.model.blog.Blog;
import com.communote.server.model.blog.BlogRole;

/**
 * Thrown if a user has not the required permission or role to access or modify a blog.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class BlogAccessException extends AuthorizationException {

    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = 5152688816528749416L;

    private Long blogId;

    private BlogRole requiredRole;

    private BlogRole userRole;

    private Permission<Blog> permission;

    /**
     * Constructor.
     *
     * @param message
     *            The message.
     * @param blogId
     *            The blogs id.
     * @param permission
     *            The missing permission.
     */
    public BlogAccessException(String message, long blogId, Permission<Blog> permission) {
        super(message);
        this.blogId = blogId;
        this.setPermission(permission);
    }

    /**
     * Constructs a new instance of BlogAccessException
     *
     * @param message
     *            The message.
     * @param blogId
     *            The blogId.
     * @param requiredRole
     *            The required role.
     * @param userRole
     *            The users role.
     */
    public BlogAccessException(String message, Long blogId, BlogRole requiredRole, BlogRole userRole) {
        super(message);
        this.blogId = blogId;
        this.requiredRole = requiredRole;
        this.userRole = userRole;
    }

    /**
     * Constructs a new instance of BlogAccessException
     *
     */
    public BlogAccessException(String message, Throwable throwable, Long blogId,
            BlogRole requiredRole, BlogRole userRole) {
        super(message, throwable);
        this.blogId = blogId;
        this.requiredRole = requiredRole;
        this.userRole = userRole;
    }

    /**
     * <p>
     * ID of the blog for which the access is not granted
     * </p>
     */
    public Long getBlogId() {
        return this.blogId;
    }

    /**
     * @return the permission
     */
    public Permission<Blog> getPermission() {
        return permission;
    }

    /**
     * <p>
     * the role which is required but the member did not have
     * </p>
     */
    public BlogRole getRequiredRole() {
        return this.requiredRole;
    }

    /**
     * <p>
     * the role the user had
     * </p>
     */
    public BlogRole getUserRole() {
        return this.userRole;
    }

    public void setBlogId(Long blogId) {
        this.blogId = blogId;
    }

    /**
     * @param permission
     *            the permission to set
     */
    public void setPermission(Permission<Blog> permission) {
        this.permission = permission;
    }

    public void setRequiredRole(BlogRole requiredRole) {
        this.requiredRole = requiredRole;
    }

    public void setUserRole(BlogRole userRole) {
        this.userRole = userRole;
    }

}
