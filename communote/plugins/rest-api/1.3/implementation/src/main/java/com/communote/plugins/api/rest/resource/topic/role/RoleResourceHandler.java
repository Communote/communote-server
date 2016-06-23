package com.communote.plugins.api.rest.resource.topic.role;

import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang3.StringUtils;

import com.communote.plugins.api.rest.exception.ExtensionNotSupportedException;
import com.communote.plugins.api.rest.exception.ResponseBuildException;
import com.communote.plugins.api.rest.resource.DefaultParameter;
import com.communote.plugins.api.rest.resource.DefaultResourceHandler;
import com.communote.plugins.api.rest.resource.topic.TopicResourceHelper;
import com.communote.plugins.api.rest.response.ResponseHelper;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.blog.BlogAccessException;
import com.communote.server.api.core.blog.BlogManagement;
import com.communote.server.api.core.blog.BlogNotFoundException;
import com.communote.server.api.core.blog.BlogRightsManagement;
import com.communote.server.api.core.blog.NoBlogManagerLeftException;
import com.communote.server.api.core.user.CommunoteEntityNotFoundException;
import com.communote.server.core.user.UserManagement;
import com.communote.server.model.blog.BlogRole;
import com.communote.server.model.user.User;
import com.communote.server.model.user.group.Group;
import com.communote.server.persistence.user.group.GroupDao;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class RoleResourceHandler
extends
DefaultResourceHandler<CreateRoleParameter, DefaultParameter, DefaultParameter, GetRoleParameter, DefaultParameter> {

    /**
     * Remove or assign role for entity
     *
     * @param blogId
     *            identifier of a blog
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
     */
    private void assignOrRemoveRoleForEntity(Long blogId, BlogRole role, Long entityId)
            throws NoBlogManagerLeftException, BlogNotFoundException, BlogAccessException,
            CommunoteEntityNotFoundException {
        if (role == null) {
            getBlogRightsManagement().removeMemberByEntityId(blogId, entityId);
        } else {
            getBlogRightsManagement().assignEntity(blogId, entityId, role);
        }
    }

    /**
     * Assign or remove role for a group
     *
     * @param createRoleParameter
     *            - an object that contains all the parameter that can or have to be used for such a
     *            request
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
     */
    private void assignOrRemoveRoleForGroup(CreateRoleParameter createRoleParameter, Long blogId,
            BlogRole role) throws NoBlogManagerLeftException, BlogNotFoundException,
            BlogAccessException, CommunoteEntityNotFoundException {
        Group group = getGroupDao().findByAlias(createRoleParameter.getGroupAlias());
        if (group != null) {
            assignOrRemoveRoleForEntity(blogId, role, group.getId());
        } else {
            throw new CommunoteEntityNotFoundException();
        }
    }

    /**
     * Assign or remove role for a user
     *
     * @param createRoleParameter
     *            - an object that contains all the parameter that can or have to be used for such a
     *            request
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
     */
    private void assignOrRemoveRoleForUser(CreateRoleParameter createRoleParameter, Long blogId,
            BlogRole role) throws NoBlogManagerLeftException, BlogNotFoundException,
            BlogAccessException, CommunoteEntityNotFoundException {
        User user = getUserManagement().findUserByAlias(createRoleParameter.getUserAlias());
        if (user != null) {
            assignOrRemoveRoleForEntity(blogId, role, user.getId());
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
     * @param createRoleParameter
     *            - an object that contains all the parameter that can or have to be used for such a
     *            request
     * @return blog role
     */
    private BlogRole getBlogRole(CreateRoleParameter createRoleParameter) {
        BlogRole role = null;
        if (createRoleParameter.getRole().compareTo(ERole.NONE) != 0) {
            role = BlogRole.fromString(createRoleParameter.getRole().name());
        }
        return role;
    }

    /**
     * Returns the {@link GroupDao}.
     *
     * @return Returns the {@link GroupDao}.
     */
    private GroupDao getGroupDao() {
        return ServiceLocator.findService(GroupDao.class);
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
     * Returns the {@link UserManagement}.
     *
     * @return Returns the {@link UserManagement}.
     */
    private UserManagement getUserManagement() {
        return ServiceLocator.instance().getService(UserManagement.class);
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
     * @throws BlogAccessException
     *             can not access blog
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
     */
    @Override
    public Response handleCreateInternally(CreateRoleParameter createRoleParameter,
            String requestedMimeType, UriInfo uriInfo, String requestSessionId, Request request)
                    throws BlogNotFoundException, BlogAccessException, CommunoteEntityNotFoundException,
                    NoBlogManagerLeftException, ResponseBuildException, ExtensionNotSupportedException {

        Long blogId = TopicResourceHelper.getTopicIdByIdentifier((createRoleParameter
                .getTopicIdentifier() != null) ? createRoleParameter.getTopicIdentifier().name()
                        : null, createRoleParameter.getTopicId(), getTopicManagement());

        if (createRoleParameter.getEntityId() != null) {
            assignOrRemoveRoleForEntity(blogId, getBlogRole(createRoleParameter),
                    createRoleParameter.getEntityId());
        } else if (StringUtils.isNotBlank(createRoleParameter.getUserAlias())) {
            assignOrRemoveRoleForUser(createRoleParameter, blogId, getBlogRole(createRoleParameter));
        } else if (StringUtils.isNotBlank(createRoleParameter.getGroupAlias())) {
            assignOrRemoveRoleForGroup(createRoleParameter, blogId,
                    getBlogRole(createRoleParameter));
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
