package com.communote.plugins.api.rest.v24.resource.topic.rolebulkexternal;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.communote.plugins.api.rest.v24.exception.ExtensionNotSupportedException;
import com.communote.plugins.api.rest.v24.exception.ResponseBuildException;
import com.communote.plugins.api.rest.v24.resource.DefaultParameter;
import com.communote.plugins.api.rest.v24.resource.DefaultResourceHandler;
import com.communote.plugins.api.rest.v24.resource.topic.TopicResourceHelper;
import com.communote.plugins.api.rest.v24.resource.topic.rolebulkexternal.rolebulkexternaltopicright.RoleBulkExternalTopicRightResource;
import com.communote.plugins.api.rest.v24.response.ResponseHelper;
import com.communote.plugins.api.rest.v24.service.IllegalRequestParameterException;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.blog.BlogAccessException;
import com.communote.server.api.core.blog.BlogManagement;
import com.communote.server.api.core.blog.BlogNotFoundException;
import com.communote.server.core.blog.helper.BlogRoleHelper;
import com.communote.server.core.external.BlogRightsSynchronizer;
import com.communote.server.model.blog.BlogRole;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class RoleBulkExternalResourceHandler
        extends
        DefaultResourceHandler<CreateRoleBulkExternalParameter, DefaultParameter, DefaultParameter,
        GetRoleBulkExternalParameter, DefaultParameter> {

    /** Logger. */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(RoleBulkExternalResourceHandler.class);

    /**
     * Returns the {@link BlogManagement}.
     * 
     * @return Returns the {@link BlogManagement}.
     */
    private BlogManagement getTopicManagement() {
        return ServiceLocator.instance().getService(BlogManagement.class);
    }

    /**
     * Get blog role bulk for external
     * 
     * @param createRoleBulkExternalParameter
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
     *             Exception.
     * @throws IllegalRequestParameterException
     *             illegal parameter in request
     * @throws ResponseBuildException
     *             exception while building the response
     * @throws ExtensionNotSupportedException
     *             extension is not supported
     * @throws BlogAccessException
     *             in case the current user is not allowed to update the blog rights
     */
    @Override
    public Response handleCreateInternally(
            CreateRoleBulkExternalParameter createRoleBulkExternalParameter,
            String requestedMimeType, UriInfo uriInfo, String requestSessionId, Request request)
            throws BlogNotFoundException, IllegalRequestParameterException, ResponseBuildException,
            ExtensionNotSupportedException, BlogAccessException {
        String externalSystemId = createRoleBulkExternalParameter.getExternalSystemId();

        Long topicId = TopicResourceHelper.getTopicIdByIdentifier((createRoleBulkExternalParameter
                .getTopicIdentifier() != null) ? createRoleBulkExternalParameter
                .getTopicIdentifier().name() : null, createRoleBulkExternalParameter.getTopicId(),
                getTopicManagement());

        Map<String, BlogRole> externalUserRoles = new HashMap<String, BlogRole>();
        Map<String, BlogRole> externalGroupRoles = new HashMap<String, BlogRole>();

        LOGGER.debug("parsed elements of json string "
                + createRoleBulkExternalParameter.getTopicRights().length);

        for (RoleBulkExternalTopicRightResource entity : createRoleBulkExternalParameter
                .getTopicRights()) {
            if (StringUtils.isNotBlank(entity.getExternalUserId())) {
                BlogRole role;
                if (externalUserRoles.containsKey(entity.getExternalUserId())) {
                    role = BlogRoleHelper.getUpperRole(
                            externalUserRoles.get(entity.getExternalUserId()),
                            BlogRole.fromString(entity.getRole().name()));
                    LOGGER.trace("overwrite external user " + entity.getExternalUserId()
                            + " with role " + role);
                } else {
                    role = BlogRole.fromString(entity.getRole().name());
                    LOGGER.trace("add external user " + entity.getExternalUserId() + " with role "
                            + role);
                }
                externalUserRoles.put(entity.getExternalUserId(), role);
            } else if (StringUtils.isNotBlank(entity.getExternalGroupId())) {
                BlogRole role;
                if (externalGroupRoles.containsKey(entity.getExternalGroupId())) {
                    role = BlogRoleHelper.getUpperRole(
                            externalGroupRoles.get(entity.getExternalGroupId()),
                            BlogRole.fromString(entity.getRole().name()));
                    LOGGER.trace("overwrite external group " + entity.getExternalGroupId()
                            + " with role " + role);
                } else {
                    role = BlogRole.fromString(entity.getRole().name());
                    LOGGER.trace("add external group " + entity.getExternalGroupId()
                            + " with role "
                            + role);
                }
                externalGroupRoles.put(entity.getExternalGroupId(), role);
            } else {
                throw new IllegalRequestParameterException("externalUserId or externalGroupId",
                        null, "user- or groupalias for entity is null");
            }
        }

        BlogRightsSynchronizer blogRightsSynchronizer = new BlogRightsSynchronizer(topicId,
                externalSystemId);
        blogRightsSynchronizer.replaceRights(createRoleBulkExternalParameter.getExternalId(),
                externalUserRoles, externalGroupRoles);

        return ResponseHelper.buildSuccessResponse(null, request);
    }

    /**
     * Get blog role bulk for external
     * 
     * @param getRoleBulkExternalParameter
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
     */
    @Override
    public Response handleGetInternally(GetRoleBulkExternalParameter getRoleBulkExternalParameter,
            String requestedMimeType, UriInfo uriInfo, String requestSessionId, Request request) {
        // TODO Auto-generated method stub
        return null;
    }

}
