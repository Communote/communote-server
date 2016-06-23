package com.communote.plugins.api.rest.v24.resource.topic.role;

import org.apache.commons.lang3.StringUtils;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.blog.BlogAccessException;
import com.communote.server.api.core.blog.BlogNotFoundException;
import com.communote.server.api.core.blog.BlogRightsManagement;
import com.communote.server.api.core.blog.NoBlogManagerLeftException;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.api.core.user.CommunoteEntityNotFoundException;
import com.communote.server.core.security.SecurityHelper;
import com.communote.server.core.user.UserManagement;
import com.communote.server.model.blog.BlogRole;
import com.communote.server.model.user.User;
import com.communote.server.model.user.group.Group;
import com.communote.server.persistence.user.group.GroupDao;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public final class RoleResourceHelper {

    /**
     * Remove or assign role for entity
     *
     * @param topicId
     *            identifier of a blog
     * @param role
     *            role of the blog
     * @param entityId
     *            identifier of an entity (user/group)
     * @throws NoBlogManagerLeftException
     *             no active user with managment rights
     * @throws BlogNotFoundException
     *             blog not found
     * @throws CommunoteEntityNotFoundException
     *             can not found entity (user/group)
     * @throws AuthorizationException
     *             in case the the current user tries to become manager of a topic but is not the
     *             client manager
     */
    public static void assignOrRemoveRoleForEntity(Long topicId, BlogRole role, Long entityId)
            throws NoBlogManagerLeftException, BlogNotFoundException,
            CommunoteEntityNotFoundException, AuthorizationException {
        BlogRightsManagement topicRightsManagement = ServiceLocator
                .findService(BlogRightsManagement.class);
        if (role == null) {
            topicRightsManagement.removeMemberByEntityId(topicId, entityId);
        } else if (entityId.equals(SecurityHelper.getCurrentUserId())
                && !topicRightsManagement.currentUserHasManagementAccess(topicId)) {
            // KENMEI-6018: The user is not a user of this topic and so tries to gain access.
            if (role.equals(BlogRole.MANAGER)) {
                topicRightsManagement.assignManagementAccessToCurrentUser(topicId);
            } else {
                throw new BlogAccessException("Current user is not manager of topic", topicId,
                        BlogRole.MANAGER, null);
            }
        } else {
            topicRightsManagement.assignEntity(topicId, entityId, role);
        }
    }

    /**
     * Assign or remove role for a group
     *
     * @param groupAlias
     *            alias of group
     * @param blogId
     *            identifier of the blog
     * @param role
     *            role of the blog
     * @throws NoBlogManagerLeftException
     *             no active user with managment rights
     * @throws BlogNotFoundException
     *             blog not found
     * @throws CommunoteEntityNotFoundException
     *             can not found entity (user/group)
     * @throws AuthorizationException
     *             should not occur
     */
    public static void assignOrRemoveRoleForGroup(String groupAlias, Long blogId, BlogRole role)
            throws NoBlogManagerLeftException, BlogNotFoundException,
            CommunoteEntityNotFoundException, AuthorizationException {
        Group group = ServiceLocator.findService(GroupDao.class).findByAlias(groupAlias);
        if (group != null) {
            assignOrRemoveRoleForEntity(blogId, role, group.getId());
        } else {
            throw new CommunoteEntityNotFoundException();
        }
    }

    /**
     * Assign or remove role for a user
     *
     * @param userAlias
     *            alias of user
     * @param blogId
     *            identifier of the blog
     * @param role
     *            role of the blog
     * @throws NoBlogManagerLeftException
     *             no active user with managment rights
     * @throws BlogNotFoundException
     *             blog not found
     * @throws CommunoteEntityNotFoundException
     *             can not found entity (user/group)
     * @throws AuthorizationExceptionin
     *             case the the current user tries to become manager of a topic but is not the
     *             client manager
     */
    public static void assignOrRemoveRoleForUser(String userAlias, Long blogId, BlogRole role)
            throws NoBlogManagerLeftException, BlogNotFoundException,
            CommunoteEntityNotFoundException, AuthorizationException {
        User user = ServiceLocator.instance().getService(UserManagement.class)
                .findUserByAlias(userAlias);
        if (user != null) {
            assignOrRemoveRoleForEntity(blogId, role, user.getId());
        } else {
            throw new CommunoteEntityNotFoundException();
        }
    }

    /**
     * Assign role resources.
     *
     * @param roleResources
     *            the external object resources.
     * @param topicId
     *            the identifier of the topic.
     * @throws NoBlogManagerLeftException
     *             cannot add role because no topic manager exists after create of topic
     * @throws CommunoteEntityNotFoundException
     *             can not found entity (user or group)
     * @throws BlogNotFoundException
     *             can not found topic
     * @throws AuthorizationException
     *             in case the the current user tries to become manager of a topic but is not the
     *             client manager
     */
    public static void assignRoleResources(RoleResource[] roleResources, Long topicId)
            throws BlogNotFoundException, CommunoteEntityNotFoundException,
            NoBlogManagerLeftException, AuthorizationException {
        if (roleResources != null) {
            for (RoleResource roleResource : roleResources) {

                if (roleResource.getEntityId() != null) {
                    RoleResourceHelper.assignOrRemoveRoleForEntity(topicId,
                            RoleResourceHelper.getBlogRole(roleResource.getRole()),
                            roleResource.getEntityId());
                } else if (StringUtils.isNotBlank(roleResource.getUserAlias())) {
                    RoleResourceHelper.assignOrRemoveRoleForUser(roleResource.getUserAlias(),
                            topicId, RoleResourceHelper.getBlogRole(roleResource.getRole()));
                } else if (StringUtils.isNotBlank(roleResource.getGroupAlias())) {
                    RoleResourceHelper.assignOrRemoveRoleForGroup(roleResource.getGroupAlias(),
                            topicId, RoleResourceHelper.getBlogRole(roleResource.getRole()));
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
        if (!ERole.NONE.equals(eRole)) {
            role = BlogRole.fromString(eRole.name());
        }
        return role;
    }

    /**
     * Private constructor to avoid instances of utility class.
     */
    private RoleResourceHelper() {
        // Do nothing
    }

}
