package com.communote.server.core.permission.filters;

import java.util.Set;

import com.communote.server.api.core.blog.BlogManagement;
import com.communote.server.api.core.blog.CreationBlogTO;
import com.communote.server.api.core.blog.TopicPermissionManagement;
import com.communote.server.api.core.config.type.ClientProperty;
import com.communote.server.api.core.property.PropertyManagement;
import com.communote.server.api.core.property.StringPropertyTO;
import com.communote.server.api.core.security.permission.Permission;
import com.communote.server.core.security.SecurityHelper;
import com.communote.server.model.blog.Blog;

/**
 * Filter for evaluating the {@link ClientProperty#ALLOW_TOPIC_CREATE_FOR_ALL_USERS} option for the
 * topic create permission.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class TopicCreateOptionPermissionFilter implements TopicPermissionFilter {

    /**
     * {@inheritDoc}
     */
    @Override
    public void filter(Blog blog, Set<Permission<Blog>> permissions) {
        // nothing to do here
    }

    @Override
    public void filterForCreation(CreationBlogTO entity, Set<Permission<Blog>> permissions) {
        String val = ClientProperty.ALLOW_TOPIC_CREATE_FOR_ALL_USERS
                .getValue(Boolean.toString(ClientProperty.DEFAULT_ALLOW_TOPIC_CREATE_FOR_ALL_USERS));

        // allow creation if current user is client manager and the client property is either not
        // set or set to true.
        boolean allowCreate = SecurityHelper.isClientManager() || val == null
                || Boolean.parseBoolean(val);

        if (!allowCreate && entity != null) {
            // check if this is a personal topic, and if so allow the creation
            StringPropertyTO personalProperty = entity.getProperty(PropertyManagement.KEY_GROUP,
                    BlogManagement.PROPERTY_KEY_PERSONAL_TOPIC_USER_ID);
            if (personalProperty != null && personalProperty.getPropertyValue() != null) {
                try {
                    allowCreate = Long.parseLong(personalProperty.getPropertyValue()) > 0;
                } catch (NumberFormatException nfe) {
                    // ignore
                }
            }
        }

        if (allowCreate) {
            permissions.add(TopicPermissionManagement.PERMISSION_CREATE_TOPIC);
        }

    }

    /**
     * @return Integer.Max_Value
     */
    @Override
    public int getOrder() {
        return Integer.MAX_VALUE;
    }

}
