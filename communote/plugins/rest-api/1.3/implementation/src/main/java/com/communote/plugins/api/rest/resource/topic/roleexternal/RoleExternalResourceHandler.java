package com.communote.plugins.api.rest.resource.topic.roleexternal;

import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang3.StringUtils;

import com.communote.plugins.api.rest.exception.ExtensionNotSupportedException;
import com.communote.plugins.api.rest.exception.ResponseBuildException;
import com.communote.plugins.api.rest.request.RequestHelper;
import com.communote.plugins.api.rest.resource.DefaultParameter;
import com.communote.plugins.api.rest.resource.DefaultResourceHandler;
import com.communote.plugins.api.rest.resource.topic.TopicResourceHelper;
import com.communote.plugins.api.rest.response.ResponseHelper;
import com.communote.plugins.api.rest.to.ApiResult;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.blog.BlogAccessException;
import com.communote.server.api.core.blog.BlogManagement;
import com.communote.server.api.core.blog.BlogMemberNotFoundException;
import com.communote.server.api.core.blog.BlogNotFoundException;
import com.communote.server.api.core.blog.BlogRightsManagement;
import com.communote.server.api.core.blog.NoBlogManagerLeftException;
import com.communote.server.api.core.user.CommunoteEntityNotFoundException;
import com.communote.server.model.blog.BlogRole;
import com.communote.server.model.user.User;
import com.communote.server.model.user.group.ExternalUserGroup;
import com.communote.server.persistence.user.group.ExternalUserGroupDao;
import com.communote.server.service.UserService;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class RoleExternalResourceHandler
extends
DefaultResourceHandler<CreateRoleExternalParameter, DefaultParameter, DefaultParameter, GetRoleExternalParameter, DefaultParameter> {

    /**
     * Change or remove role for a user of external system
     *
     * @param externalUserId
     *            - external user identifier
     * @param externalSystemId
     *            - external system identifier
     * @param blogId
     *            identifier of the blog
     * @param role
     *            role of the blog
     * @throws NoBlogManagerLeftException
     *             no active user with managment rights
     * @throws BlogNotFoundException
     *             blog not found
     * @throws BlogAccessException
     *             can not access blog
     * @throws CommunoteEntityNotFoundException
     *             can not found entity (user/group)
     * @throws BlogMemberNotFoundException
     *             in case the the entity is not a member
     */
    private void changeOrRemoveExternalForUser(String externalUserId, String externalSystemId,
            Long blogId, BlogRole role) throws NoBlogManagerLeftException, BlogNotFoundException,
            BlogAccessException, CommunoteEntityNotFoundException, BlogMemberNotFoundException {
        User user = getUserService().getUser(externalUserId, externalSystemId);
        if (user != null) {
            changeOrRemoveExternalRoleForEntity(blogId, role, user.getId(), externalSystemId);
        } else {
            throw new CommunoteEntityNotFoundException();
        }
    }

    /**
     * Remove or change role for entity of external system
     *
     * @param blogId
     *            identifier of a blog
     * @param externalSystemId
     *            - external system identifier
     * @param role
     *            role of the blog
     * @param entityId
     *            identifier of an entity (user/group)
     * @throws NoBlogManagerLeftException
     *             no active user with managment rights
     * @throws BlogNotFoundException
     *             blog not found
     * @throws BlogAccessException
     *             can not access blog
     * @throws CommunoteEntityNotFoundException
     *             can not found entity (user/group)
     * @throws BlogMemberNotFoundException
     *             in case the the entity is not a member
     */
    private void changeOrRemoveExternalRoleForEntity(Long blogId, BlogRole role, Long entityId,
            String externalSystemId) throws NoBlogManagerLeftException, BlogNotFoundException,
            BlogAccessException, CommunoteEntityNotFoundException, BlogMemberNotFoundException {
        if (role == null) {
            getBlogRightsManagement().removeMemberByEntityIdForExternal(blogId, entityId,
                    externalSystemId);
        } else {
            getBlogRightsManagement().changeRoleOfMemberByEntityIdForExternal(blogId, entityId,
                    role, externalSystemId);
        }
    }

    /**
     * Change or remove role for a group of external system
     *
     * @param createRoleExternalParameter
     *            - an object that contains all the parameter that can or have to be used for such a
     *            request
     * @param externalSystemId
     *            - external system identifier
     * @param blogId
     *            identifier of the blog
     * @param role
     *            role of the blog
     * @throws NoBlogManagerLeftException
     *             no active user with managment rights
     * @throws BlogNotFoundException
     *             blog not found
     * @throws BlogAccessException
     *             can not access blog
     * @throws CommunoteEntityNotFoundException
     *             can not found entity (user/group)
     * @throws BlogMemberNotFoundException
     *             in case the the entity is not a member
     */
    private void chnageOrRemoveExternalForGroup(
            CreateRoleExternalParameter createRoleExternalParameter, String externalSystemId,
            Long blogId, BlogRole role) throws NoBlogManagerLeftException, BlogNotFoundException,
            BlogAccessException, CommunoteEntityNotFoundException, BlogMemberNotFoundException {
        ExternalUserGroup group = getExternalUserGroupDao().findByExternalId(
                createRoleExternalParameter.getExternalGroupId(), externalSystemId);
        if (group != null) {
            changeOrRemoveExternalRoleForEntity(blogId, role, group.getId(), externalSystemId);
        } else {
            throw new CommunoteEntityNotFoundException();
        }
    }

    /**
     * Returns the {@link BlogRightsManagement}.
     *
     * @return Returns the {@link BlogRightsManagement}.
     */
    private BlogRightsManagement getBlogRightsManagement() {
        return ServiceLocator.findService(BlogRightsManagement.class);
    }

    /**
     * Get blog role
     *
     * @param createRoleExternalParameter
     *            - an object that contains all the parameter that can or have to be used for such a
     *            request
     * @return blog role
     */
    private BlogRole getBlogRole(CreateRoleExternalParameter createRoleExternalParameter) {
        BlogRole role = null;
        if (createRoleExternalParameter.getRole().compareTo(ERole.NONE) != 0) {
            role = BlogRole.fromString(createRoleExternalParameter.getRole().name());
        }
        return role;
    }

    /**
     * Returns the {@link ExternalUserGroupDao}.
     *
     * @return Returns the {@link ExternalUserGroupDao}.
     */
    private ExternalUserGroupDao getExternalUserGroupDao() {
        return ServiceLocator.findService(ExternalUserGroupDao.class);
    }

    /**
     * Returns the {@link BlogManagement}.
     *
     * @return Returns the {@link BlogManagement}.
     */
    private BlogManagement getTopicManagement() {
        return ServiceLocator.instance().getService(BlogManagement.class);
    }

    /**
     * Returns the {@link UserService}.
     *
     * @return Returns the {@link UserService}.
     */
    private UserService getUserService() {
        return ServiceLocator.instance().getService(UserService.class);
    }

    /**
     * Create blog role for external system
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
     * @throws NoBlogManagerLeftException
     *             no manager for topic is available
     * @throws CommunoteEntityNotFoundException
     *             user or group not found
     * @throws BlogAccessException
     *             can not access topic
     * @throws BlogNotFoundException
     *             can not found topic
     * @throws ResponseBuildException
     *             exception while building the response
     * @throws ExtensionNotSupportedException
     *             extension is not supported
     */
    @Override
    public Response handleCreateInternally(CreateRoleExternalParameter createRoleExternalParameter,
            String requestedMimeType, UriInfo uriInfo, String requestSessionId, Request request)
                    throws BlogNotFoundException, BlogAccessException, CommunoteEntityNotFoundException,
                    NoBlogManagerLeftException, ResponseBuildException, ExtensionNotSupportedException {
        String externalSystemId = createRoleExternalParameter.getExternalSystemId();
        Long blogId = TopicResourceHelper.getTopicIdByIdentifier((createRoleExternalParameter
                .getTopicIdentifier() != null) ? createRoleExternalParameter.getTopicIdentifier()
                        .name() : null, createRoleExternalParameter.getTopicId(), getTopicManagement());
        try {
            if (StringUtils.isNotBlank(createRoleExternalParameter.getExternalUserId())) {
                changeOrRemoveExternalForUser(createRoleExternalParameter.getExternalUserId(),
                        externalSystemId, blogId, getBlogRole(createRoleExternalParameter));
            } else if (StringUtils.isNotBlank(createRoleExternalParameter.getExternalGroupId())) {
                chnageOrRemoveExternalForGroup(createRoleExternalParameter, externalSystemId,
                        blogId, getBlogRole(createRoleExternalParameter));
            }
        } catch (BlogMemberNotFoundException e) {
            // we cannot assign a member to a topic because we do not have the required information
            // to ensure that the topic is associated with an external object, thus this is not
            // supported any longer
            ApiResult<Object> apiResult = new ApiResult<Object>();
            apiResult.setMessage("not supported anymore");
            return ResponseHelper.buildErrorResponse(apiResult,
                    Response.status(Response.Status.FORBIDDEN),
                    RequestHelper.getHttpServletRequest(request));
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
