package com.communote.plugins.api.rest.v24.resource.topic.role;

import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang3.StringUtils;

import com.communote.plugins.api.rest.v24.exception.ExtensionNotSupportedException;
import com.communote.plugins.api.rest.v24.exception.ResponseBuildException;
import com.communote.plugins.api.rest.v24.resource.DefaultParameter;
import com.communote.plugins.api.rest.v24.resource.DefaultResourceHandler;
import com.communote.plugins.api.rest.v24.resource.topic.TopicResourceHelper;
import com.communote.plugins.api.rest.v24.response.ResponseHelper;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.blog.BlogAccessException;
import com.communote.server.api.core.blog.BlogManagement;
import com.communote.server.api.core.blog.BlogNotFoundException;
import com.communote.server.api.core.blog.NoBlogManagerLeftException;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.api.core.user.CommunoteEntityNotFoundException;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class RoleResourceHandler
extends
DefaultResourceHandler<CreateRoleParameter, DefaultParameter, DefaultParameter, GetRoleParameter, DefaultParameter> {

    /**
     * Returns the {@link BlogManagement}.
     *
     * @return Returns the {@link BlogManagement}.
     */
    private BlogManagement getTopicManagement() {
        return ServiceLocator.instance().getService(BlogManagement.class);
    }

    /**
     * Create the role of internal user for blog
     *
     * @param createRoleParameter
     *            - an object that contains all the parameter that can or have to be used for such a
     *            request
     * @param requestedMimeType
     *            - is the mime type that indicates which data exchange format to use
     * @param uriInfo
     *            - this object is created by the request and contains some request specific data
     * @param requestSessionId
     *            sessionId
     * @param request
     *            - javax request
     * @return an response object containing a http status code and a message
     * @throws BlogNotFoundException
     *             blog not found
     * @throws BlogAccessException
     *             can not access blog
     * @throws CommunoteEntityNotFoundException
     *             can not found entity (user/group)
     * @throws NoBlogManagerLeftException
     *             no active user with managment rights
     * @throws ResponseBuildException
     *             exception while building the response
     * @throws ExtensionNotSupportedException
     *             extension is not supported
     * @throws AuthorizationException
     *             in case the the current user tries to become manager of a topic but is not the
     *             client manager
     */
    @Override
    public Response handleCreateInternally(CreateRoleParameter createRoleParameter,
            String requestedMimeType, UriInfo uriInfo, String requestSessionId, Request request)
                    throws BlogNotFoundException, CommunoteEntityNotFoundException,
                    NoBlogManagerLeftException, ResponseBuildException, ExtensionNotSupportedException,
                    AuthorizationException {

        Long blogId = TopicResourceHelper.getTopicIdByIdentifier((createRoleParameter
                .getTopicIdentifier() != null) ? createRoleParameter.getTopicIdentifier().name()
                        : null, createRoleParameter.getTopicId(), getTopicManagement());

        if (createRoleParameter.getEntityId() != null) {
            RoleResourceHelper.assignOrRemoveRoleForEntity(blogId,
                    RoleResourceHelper.getBlogRole(createRoleParameter.getRole()),
                    createRoleParameter.getEntityId());
        } else if (StringUtils.isNotBlank(createRoleParameter.getUserAlias())) {
            RoleResourceHelper.assignOrRemoveRoleForUser(createRoleParameter.getUserAlias(),
                    blogId, RoleResourceHelper.getBlogRole(createRoleParameter.getRole()));
        } else if (StringUtils.isNotBlank(createRoleParameter.getGroupAlias())) {
            RoleResourceHelper.assignOrRemoveRoleForGroup(createRoleParameter.getGroupAlias(),
                    blogId, RoleResourceHelper.getBlogRole(createRoleParameter.getRole()));
        }

        return ResponseHelper.buildSuccessResponse(null, request);
    }

    /**
     * Get blog role
     *
     * @param getRoleParameter
     *            - an object that contains all the parameter that can or have to be used for such a
     *            request
     * @param requestedMimeType
     *            - is the mime type that indicates which data exchange format to use
     * @param uriInfo
     *            - this object is created by the request and contains some request specific data
     * @param requestSessionId
     *            sessionId
     * @param request
     *            - javax request
     * @return null because it is not implemented
     * @throws ResponseBuildException
     *             exception while building the response
     * @throws ExtensionNotSupportedException
     *             extension is not supported
     */
    @Override
    public Response handleGetInternally(GetRoleParameter getRoleParameter,
            String requestedMimeType, UriInfo uriInfo, String requestSessionId, Request request)
                    throws ResponseBuildException, ExtensionNotSupportedException {
        return ResponseHelper.buildSuccessResponse(null, request);
    }

}
