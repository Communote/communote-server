package com.communote.plugins.api.rest.v30.resource.topic.roleexternal;

import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang3.StringUtils;

import com.communote.plugins.api.rest.v30.exception.ExtensionNotSupportedException;
import com.communote.plugins.api.rest.v30.exception.ResponseBuildException;
import com.communote.plugins.api.rest.v30.resource.DefaultParameter;
import com.communote.plugins.api.rest.v30.resource.DefaultResourceHandler;
import com.communote.plugins.api.rest.v30.resource.topic.TopicResourceHelper;
import com.communote.plugins.api.rest.v30.response.ResponseHelper;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.blog.BlogAccessException;
import com.communote.server.api.core.blog.BlogManagement;
import com.communote.server.api.core.blog.BlogNotFoundException;
import com.communote.server.api.core.external.ExternalObjectNotAssignedException;
import com.communote.server.api.core.user.CommunoteEntityNotFoundException;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class RoleExternalResourceHandler
extends
DefaultResourceHandler<CreateRoleExternalParameter, DefaultParameter, DefaultParameter, GetRoleExternalParameter, DefaultParameter> {

    /**
     * Returns the {@link BlogManagement}.
     *
     * @return Returns the {@link BlogManagement}.
     */
    private BlogManagement getTopicManagement() {
        return ServiceLocator.instance().getService(BlogManagement.class);
    }

    /**
     * Create topic role for external system
     *
     * @param createRoleExternalParameter
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
     *
     * @throws BlogNotFoundException
     *             in case the blog does not exist
     * @throws CommunoteEntityNotFoundException
     *             in case the user or group does not exist
     * @throws BlogAccessException
     *             in case the current user is not allowed to modify the access rights
     * @throws ResponseBuildException
     *             exception while building the response
     * @throws ExtensionNotSupportedException
     *             extension is not supported
     * @throws ExternalObjectNotAssignedException
     */
    @Override
    public Response handleCreateInternally(CreateRoleExternalParameter createRoleExternalParameter,
            String requestedMimeType, UriInfo uriInfo, String requestSessionId, Request request)
                    throws BlogNotFoundException, CommunoteEntityNotFoundException, ResponseBuildException,
            ExtensionNotSupportedException, BlogAccessException, ExternalObjectNotAssignedException {
        String externalSystemId = createRoleExternalParameter.getExternalSystemId();
        Long topicId = TopicResourceHelper.getTopicIdByIdentifier((createRoleExternalParameter
                .getTopicIdentifier() != null) ? createRoleExternalParameter.getTopicIdentifier()
                        .name() : null, createRoleExternalParameter.getTopicId(), getTopicManagement());

        if (StringUtils.isNotBlank(createRoleExternalParameter.getExternalUserId())) {
            RoleExternalResourceHelper.assignOrRemoveExternalForUser(topicId,
                    RoleExternalResourceHelper.getBlogRole(createRoleExternalParameter.getRole()),
                    createRoleExternalParameter.getExternalUserId(), externalSystemId,
                    createRoleExternalParameter.getExternalId());
        } else if (StringUtils.isNotBlank(createRoleExternalParameter.getExternalGroupId())) {
            RoleExternalResourceHelper.assignOrRemoveExternalForGroup(topicId,
                    RoleExternalResourceHelper.getBlogRole(createRoleExternalParameter.getRole()),
                    createRoleExternalParameter.getExternalGroupId(), externalSystemId,
                    createRoleExternalParameter.getExternalId());
        }

        return ResponseHelper.buildSuccessResponse(null, request);
    }

    /**
     * Get blog role for external system
     *
     * @param getRoleExternalParameter
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
    public Response handleGetInternally(GetRoleExternalParameter getRoleExternalParameter,
            String requestedMimeType, UriInfo uriInfo, String requestSessionId, Request request)
                    throws ResponseBuildException, ExtensionNotSupportedException {
        return ResponseHelper.buildSuccessResponse(null, request);
    }
}
