package com.communote.plugins.api.rest.v22.resource.topic.role;

import org.apache.commons.lang3.StringUtils;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.blog.BlogAccessException;
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
public final class RoleResourceHelper {

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
    public static void assignOrRemoveRoleForEntity(Long blogId, BlogRole role, Long entityId)
            throws NoBlogManagerLeftException, BlogNotFoundException, BlogAccessException,
            CommunoteEntityNotFoundException {
        if (role == null) {
            ServiceLocator.instance().getService(BlogRightsManagement.class)
            .removeMemberByEntityId(blogId, entityId);
        } else {
            ServiceLocator.instance().getService(BlogRightsManagement.class)
            .assignEntity(blogId, entityId, role);
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
     * @throws BlogAccessException
     *             can not access blog
     * @throws CommunoteEntityNotFoundException
     *             can not found entity (user/group)
     */
    public static void assignOrRemoveRoleForGroup(String groupAlias, Long blogId, BlogRole role)
            throws NoBlogManagerLeftException, BlogNotFoundException, BlogAccessException,
            CommunoteEntityNotFoundException {
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
     * @throws BlogAccessException
     *             can not access blog
     * @throws CommunoteEntityNotFoundException
     *             can not found entity (user/group)
     */
    public static void assignOrRemoveRoleForUser(String userAlias, Long blogId, BlogRole role)
            throws NoBlogManagerLeftException, BlogNotFoundException, BlogAccessException,
            CommunoteEntityNotFoundException {
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
     * @throws BlogAccessException
     *             can not access topic
     * @throws BlogNotFoundException
     *             can not found topic
     */
    public static void assignRoleResources(RoleResource[] roleResources, Long topicId)
            throws BlogNotFoundException, BlogAccessException, CommunoteEntityNotFoundException,
            NoBlogManagerLeftException {
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
