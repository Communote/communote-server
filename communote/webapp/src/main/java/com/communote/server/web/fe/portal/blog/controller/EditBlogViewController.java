package com.communote.server.web.fe.portal.blog.controller;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.communote.common.util.ParameterHelper;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.blog.BlogAccessException;
import com.communote.server.api.core.blog.BlogNotFoundException;
import com.communote.server.api.core.blog.BlogRightsManagement;
import com.communote.server.api.core.config.type.ClientProperty;
import com.communote.server.core.config.ClientConfigurationHelper;
import com.communote.server.core.external.ExternalObjectManagement;
import com.communote.server.model.blog.Blog;
import com.communote.server.model.blog.BlogRole;
import com.communote.server.web.commons.controller.VelocityViewController;

/**
 * Controller to prepare the edit blog view.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class EditBlogViewController extends VelocityViewController {

    @Override
    protected boolean prepareModel(HttpServletRequest request, HttpServletResponse response,
            Map<String, Object> model) throws Exception {
        Long topicId = ParameterHelper
                .getParameterAsLong(request.getParameterMap(), "blogId", null);
        boolean success = false;
        if (topicId == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        } else {
            BlogRightsManagement blogRightsManagement = ServiceLocator
                    .findService(BlogRightsManagement.class);
            ExternalObjectManagement externalObjectManagement = ServiceLocator
                    .findService(ExternalObjectManagement.class);
            try {
                Blog blog = blogRightsManagement.getAndCheckBlogAccess(topicId, BlogRole.MANAGER);
                model.put("blogId", topicId);
                model.put("blogAlias", blog.getNameIdentifier());
                model.put("hasExternalObjects",
                        externalObjectManagement.hasExternalObjects(topicId));
                model.put(
                        "allowPublicAccess",
                        CommunoteRuntime
                                .getInstance()
                                .getConfigurationManager()
                                .getClientConfigurationProperties()
                                .getProperty(ClientProperty.CLIENT_BLOG_ALLOW_PUBLIC_ACCESS,
                                        ClientConfigurationHelper.DEFAULT_ALLOW_PUBLIC_ACCESS));
                success = super.prepareModel(request, response, model);
            } catch (BlogAccessException e) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
            } catch (BlogNotFoundException e) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        }
        return success;
    }
}
