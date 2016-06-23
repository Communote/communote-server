package com.communote.server.web.fe.portal.blog.controller;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.blog.TopicPermissionManagement;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.core.util.InitalFiltersVO;

/**
 * View for the blog creation. Is based on the {@link InitialFiltersViewController} but checks if
 * the current user can actually create topics.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class CreateBlogViewController extends InitialFiltersViewController {

    /**
     * 
     * @return the topic permission management
     */
    private TopicPermissionManagement getTopicPermissionManagement() {
        return ServiceLocator.findService(
                TopicPermissionManagement.class);
    }

    /**
     * Checks if the current user is allowed to create new topics
     * 
     * {@inheritDoc}
     */
    @Override
    protected InitalFiltersVO postCreateInitialFilters(InitalFiltersVO filters)
            throws AuthorizationException {

        if (!getTopicPermissionManagement()
                .hasPermissionForCreation(TopicPermissionManagement.PERMISSION_CREATE_TOPIC)) {
            throw new AuthorizationException("User is not allowed to create a topic.");
        }

        return super.postCreateInitialFilters(filters);
    }

}
