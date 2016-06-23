package com.communote.plugins.api.rest.v24.resource.topic.roleexternal;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.blog.BlogAccessException;
import com.communote.server.api.core.blog.BlogNotFoundException;
import com.communote.server.api.core.blog.BlogRightsManagement;
import com.communote.server.api.core.external.ExternalObjectNotAssignedException;
import com.communote.server.api.core.user.CommunoteEntityNotFoundException;
import com.communote.server.model.blog.BlogRole;
import com.communote.server.model.user.User;
import com.communote.server.model.user.group.Group;
import com.communote.server.service.UserService;

/**
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public final class RoleExternalResourceHelper {

    /**
     * Assign or remove role for a group of external system
     *
     * @param topicId
     *            identifier of the topic
     * @param role
     *            role of the topic
     * @param externalGroupId
     *            identifier of group in external system
     * @param externalSystemId
     *            - external system identifier
     * @param externalId
     *            - external object identifier
     * @throws BlogNotFoundException
     *             in case the blog does not exist
     * @throws CommunoteEntityNotFoundException
     *             in case the user or group does not exist
     * @throws BlogAccessException
     *             in case the current user is not allowed to modify the access rights
     * @throws ExternalObjectNotAssignedException
     */
    public static void assignOrRemoveExternalForGroup(Long topicId, BlogRole role,
            String externalGroupId, String externalSystemId, String externalId)
            throws BlogNotFoundException, CommunoteEntityNotFoundException, BlogAccessException,
            ExternalObjectNotAssignedException {
        Group group = ServiceLocator.instance().getService(UserService.class)
                .getGroup(externalGroupId, externalSystemId);
        assignOrRemoveExternalRoleForEntity(topicId, role, group.getId(), externalSystemId,
                externalId);
    }

    /**
     * Assign or remove role for a user of external system
     *
     * @param blogId
     *            identifier of the blog
     * @param role
     *            role of the blog
     * @param externalUserId
     *            - external user identifier
     * @param externalSystemId
     *            - external system identifier
     * @param externalObjectId
     *            - identifier of external object in external system
     * @throws BlogNotFoundException
     *             in case the blog does not exist
     * @throws CommunoteEntityNotFoundException
     *             in case the user or group does not exist
     * @throws BlogAccessException
     *             in case the current user is not allowed to modify the access rights
     * @throws ExternalObjectNotAssignedException
     */
    public static void assignOrRemoveExternalForUser(Long blogId, BlogRole role,
            String externalUserId, String externalSystemId, String externalObjectId)
            throws BlogNotFoundException, CommunoteEntityNotFoundException, BlogAccessException,
            ExternalObjectNotAssignedException {
        User user = ServiceLocator.instance().getService(UserService.class)
                .getUser(externalUserId, externalSystemId);
        assignOrRemoveExternalRoleForEntity(blogId, role, user.getId(), externalSystemId,
                externalObjectId);
    }

    /**
     * Remove or assign role for entity of external system
     *
     * @param blogId
     *            identifier of a blog
     * @param role
     *            role of the blog
     * @param entityId
     *            identifier of an entity (user/group)
     * @param externalSystemId
     *            - external system identifier
     * @param externalId
     *            - identifier of external object in external system
     * @throws BlogNotFoundException
     *             blog not found
     * @throws CommunoteEntityNotFoundException
     *             can not found entity (user/group)
     * @throws BlogAccessException
     *             user is not manager of the topic
     * @throws ExternalObjectNotAssignedException
     */
    public static void assignOrRemoveExternalRoleForEntity(Long blogId, BlogRole role,
            Long entityId, String externalSystemId, String externalId)
            throws BlogNotFoundException, CommunoteEntityNotFoundException, BlogAccessException,
                    ExternalObjectNotAssignedException {
        if (role == null) {
            ServiceLocator.instance().getService(BlogRightsManagement.class)
                    .removeMemberByEntityIdForExternal(blogId, entityId, externalSystemId);
        } else {
            ServiceLocator.instance().getService(BlogRightsManagement.class)
                    .assignEntityForExternal(blogId, entityId, role, externalSystemId, externalId);
        }
    }

    /**
     * Assign role external resources.
     *
     * @param roleExternalResources
     *            the external object resources.
     * @param topicId
     *            the identifier of the topic.
     * @throws BlogNotFoundException
     *             in case the blog does not exist
     * @throws CommunoteEntityNotFoundException
     *             in case the user or group does not exist
     * @throws BlogAccessException
     *             in case the current user is not allowed to modify the access rights
     * @throws ExternalObjectNotAssignedException
     */
    public static void assignRoleExternalResources(RoleExternalResource[] roleExternalResources,
            Long topicId) throws BlogNotFoundException, CommunoteEntityNotFoundException,
            BlogAccessException, ExternalObjectNotAssignedException {
        if (roleExternalResources != null) {
            for (RoleExternalResource roleExternalResource : roleExternalResources) {
                if (roleExternalResource.getExternalGroupId() != null) {
                    RoleExternalResourceHelper.assignOrRemoveExternalForGroup(topicId,
                            RoleExternalResourceHelper.getBlogRole(roleExternalResource.getRole()),
                            roleExternalResource.getExternalGroupId(),
                            roleExternalResource.getExternalSystemId(),
                            roleExternalResource.getExternalId());
                } else {
                    RoleExternalResourceHelper.assignOrRemoveExternalForUser(topicId,
                            RoleExternalResourceHelper.getBlogRole(roleExternalResource.getRole()),
                            roleExternalResource.getExternalUserId(),
                            roleExternalResource.getExternalSystemId(),
                            roleExternalResource.getExternalId());
                }
            }
        }
    }

    /**
     * Get topic role
     *
     * @param eRole
     *            role of entity
     * @return topic role
     */
    public static BlogRole getBlogRole(ERole eRole) {
        BlogRole role = null;
        if (eRole.compareTo(ERole.NONE) != 0) {
            role = BlogRole.fromString(eRole.name());
        }
        return role;
    }

    /**
     * Private constructor to avoid instances of utility class.
     */
    private RoleExternalResourceHelper() {
        // Do nothing
    }

}
