package com.communote.server.api.core.blog;

import com.communote.server.api.core.security.permission.Permission;
import com.communote.server.api.core.security.permission.PermissionManagement;
import com.communote.server.model.blog.Blog;

/**
 * Permission management for topics
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public interface TopicPermissionManagement extends
PermissionManagement<Blog, CreationBlogTO, BlogAccessException> {
    // TODO Add permissions for: WRITE, READ, WRITE_ALL, READ_ALL

    /** If set, the user is allowed to edit the topics details, like tags, description, etc.. */
    public final static Permission<Blog> PERMISSION_EDIT_DETAILS = new Permission<Blog>(
            "EDIT_DETAILS");

    /** If set, the user is allowed to edit the users access list of the topic. */
    public final static Permission<Blog> PERMISSION_EDIT_ACCESS_CONTROL_LIST = new Permission<Blog>(
            "EDIT_ACCESS_CONTROL_LIST");

    /** If set, the user is allowed to delete the topic. */
    public final static Permission<Blog> PERMISSION_DELETE_TOPIC = new Permission<Blog>(
            "DELETE_TOPIC");

    /** If set, the user is allowed to invite other users to the topic. */
    public final static Permission<Blog> PERMISSION_INVITE_USER = new Permission<Blog>(
            "INVITE_USER");

    /** If set, the user is allowed to view the topic's details, like tags, description etc.. */
    public static final Permission<Blog> PERMISSION_VIEW_TOPIC_DETAILS = new Permission<Blog>(
            "VIEW_TOPIC_DETAILS");

    /**
     * If set, the user is allowed to create topics
     */
    public static final Permission<Blog> PERMISSION_CREATE_TOPIC = new Permission<Blog>(
            "PERMISSION_CREATE_TOPIC");
}
