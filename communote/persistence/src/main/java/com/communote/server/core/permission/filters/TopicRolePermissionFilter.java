package com.communote.server.core.permission.filters;

import java.util.Set;

import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.blog.BlogRightsManagement;
import com.communote.server.api.core.blog.CreationBlogTO;
import com.communote.server.api.core.blog.TopicPermissionManagement;
import com.communote.server.api.core.config.ClientConfigurationProperties;
import com.communote.server.api.core.security.permission.Permission;
import com.communote.server.core.security.SecurityHelper;
import com.communote.server.model.blog.Blog;
import com.communote.server.model.blog.BlogRole;

/**
 * Filter for default permissions of topics.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class TopicRolePermissionFilter implements TopicPermissionFilter {

    private final BlogRightsManagement blogRightsManagement;

    /**
     * Constructor.
     *
     * @param blogRightsManagement
     *            The rights management for topics.
     */
    public TopicRolePermissionFilter(BlogRightsManagement blogRightsManagement) {
        this.blogRightsManagement = blogRightsManagement;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void filter(Blog blog, Set<Permission<Blog>> permissions) {
        BlogRole blogRole = blogRightsManagement.getRoleOfCurrentUser(blog.getId(), false);
        if (blogRole != null) {
            permissions.add(TopicPermissionManagement.PERMISSION_VIEW_TOPIC_DETAILS);
        }
        if (BlogRole.MANAGER.equals(blogRole)) {
            permissions.add(TopicPermissionManagement.PERMISSION_EDIT_ACCESS_CONTROL_LIST);
            permissions.add(TopicPermissionManagement.PERMISSION_EDIT_DETAILS);
            permissions.add(TopicPermissionManagement.PERMISSION_DELETE_TOPIC);
            ClientConfigurationProperties properties = CommunoteRuntime.getInstance()
                    .getConfigurationManager().getClientConfigurationProperties();
            if (properties.isRegistrationAllowed() || SecurityHelper.isClientManager()
                    || properties.getPrimaryExternalAuthentication() != null) {
                permissions.add(TopicPermissionManagement.PERMISSION_INVITE_USER);
            }
        }
    }

    @Override
    public void filterForCreation(CreationBlogTO entity, Set<Permission<Blog>> permissions) {
        // don't add any specific permissions for allowing to create a new entity
    }

    /**
     * @return Integer.Max_Value
     */
    @Override
    public int getOrder() {
        return Integer.MAX_VALUE;
    }

}
