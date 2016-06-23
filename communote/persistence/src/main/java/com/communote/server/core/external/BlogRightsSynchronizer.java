package com.communote.server.core.external;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.blog.BlogAccessException;
import com.communote.server.api.core.blog.BlogNotFoundException;
import com.communote.server.api.core.blog.BlogRightsManagement;
import com.communote.server.api.core.external.ExternalObjectNotAssignedException;
import com.communote.server.api.core.user.CommunoteEntityNotFoundException;
import com.communote.server.api.core.user.UserNotFoundException;
import com.communote.server.core.blog.BlogRightsManagementException;
import com.communote.server.core.user.group.GroupNotFoundException;
import com.communote.server.core.vo.external.ExternalTopicRoleTO;
import com.communote.server.model.blog.BlogRole;
import com.communote.server.model.user.group.Group;
import com.communote.server.persistence.blog.BlogMemberDao;
import com.communote.server.service.UserService;

/**
 * Synchronizes the members of a given topic on the behalf of an external system.
 * <p>
 * Note: this class is not thread-safe
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
// TODO BlogRightsSynchronizer has to be more flexible: the usage of the same externalSystemId for
// user resolving and external objects requires that users and external rights are from the same
// external system which usually not the case.
public class BlogRightsSynchronizer {

    private final static Logger LOGGER = LoggerFactory.getLogger(BlogRightsSynchronizer.class);
    private final String externalSystemId;
    private final Long blogId;
    private final Map<String, Long> externalUserIdsCache;
    private final Map<String, Long> externalGroupIdsCache;
    private final HashSet<String> externalObjectNotAssignedCache;

    /**
     * @param blogId
     *            identifier of the blog whose members should be replaced
     * @param externalSystemId
     *            external system identifier
     *
     */
    public BlogRightsSynchronizer(Long blogId, String externalSystemId) {
        this.blogId = blogId;
        this.externalSystemId = externalSystemId;
        LOGGER.trace("Create BlogRightsSynchronizer for external system {}", externalSystemId);
        externalUserIdsCache = new HashMap<String, Long>();
        externalGroupIdsCache = new HashMap<String, Long>();
        // not caching the external objects that are assigned, because we don't want to add
        // members when an external object got removed in the meanwhile
        externalObjectNotAssignedCache = new HashSet<String>();
    }

    /**
     * Assign entities provided by ExternalTopicRoleTO to the blog
     *
     * @param externalTopicRoleTOs
     *            objects holding the entity identifier and the role to set
     * @param entityRolesToRemove
     *            mapping from entity ID to role, which contains entities that should be removed
     *            after this method completed. This method will remove any entity that was assigned
     *            or couldn't be processed.
     * @throws BlogAccessException
     *             in case the current user is not allowed to modify the access rights
     * @throws BlogNotFoundException
     *             in case the blog does not exist
     */
    private void assignByExternalTopicRoleTOs(List<ExternalTopicRoleTO> externalTopicRoleTOs,
            Map<Long, BlogRole> entityRolesToRemove)
                    throws BlogAccessException, BlogNotFoundException {
        if (externalTopicRoleTOs == null) {
            return;
        }
        LOGGER.debug("Start assigning {} entities for blog {} for external system {}",
                externalTopicRoleTOs.size(), blogId, externalSystemId);
        for (ExternalTopicRoleTO externalTopicRoleTO : externalTopicRoleTOs) {
            // resolve the entityId before checking whether the external object is assigned to be
            // able to remove the entityId from the members to remove, otherwise we would remove the
            // wrong users
            Long entityId = extractEntityId(externalTopicRoleTO);
            if (isExternalObjectAssigned(externalTopicRoleTO.getExternalObjectId())) {

                assignRoleToEntityForExternal(entityId, externalTopicRoleTO.getRole(),
                        externalTopicRoleTO.getExternalObjectId(), entityRolesToRemove);

            } else {
                LOGGER.debug(
                        "Skipped external role for {} because the external object {} of {} is not assigned to blog {}.",
                        entityId, externalTopicRoleTO.getExternalObjectId(), externalSystemId,
                        blogId);
                entityRolesToRemove.remove(entityId);
            }
        }
        LOGGER.debug("Finished assigning the entities to the blog {} for external system {}",
                blogId, externalSystemId);
    }

    /**
     * adds or changes the role for an external group
     *
     * @param externalObjectId
     *            identifier of an external object
     * @param externalRoles
     *            external group and role
     * @param entityRolesToRemove
     *            list of entity identifier to remove
     * @throws BlogAccessException
     *             in case the current user is not allowed to modify the access rights
     * @throws BlogNotFoundException
     *             in case the blog does not exist
     */
    private void assignGroupRoles(String externalObjectId,
            Map<String, BlogRole> externalRoles,
            Map<Long, BlogRole> entityRolesToRemove) throws BlogAccessException,
            BlogNotFoundException {
        if (externalRoles != null) {
            for (String externalEntityId : externalRoles.keySet()) {
                Long entityId = getGroupIdByExternalGroupIdFromExternalSystem(externalEntityId);
                assignRoleToEntityForExternal(entityId, externalRoles.get(externalEntityId),
                        externalObjectId, entityRolesToRemove);
            }
        }
    }

    /**
     * Assign the new role to the entity on the behalf of the external system.
     *
     * @param entityId
     *            identifier of the user or group
     * @param role
     *            the role to assign
     * @param externalObjectId
     *            identifier of the external object
     * @param entityRolesToRemove
     *            mapping from entity ID to role, which contains entities that should be removed
     *            later on. This method will remove the provided entity if it was assigned or
     *            couldn't be processed.
     * @throws BlogAccessException
     *             in case the current user is not allowed to modify the access rights
     * @throws BlogNotFoundException
     *             in case the blog does not exist
     * @return true if the entity was added or its role was modified
     */
    private boolean assignRoleToEntityForExternal(Long entityId, BlogRole role,
            String externalObjectId, Map<Long, BlogRole> entityRolesToRemove)
                    throws BlogAccessException, BlogNotFoundException {
        BlogRightsManagement blogRightsManagement = getBlogRightsManagement();
        if (role == null) {
            LOGGER.trace("Skipping null role value for entity {}", entityId);
            return false;
        }
        if (entityId != null) {
            try {
                // if entity in the currentRights has the same role no assign is necessary
                if (!(entityRolesToRemove.containsKey(entityId) && entityRolesToRemove
                        .get(entityId).equals(role))) {
                    blogRightsManagement.assignEntityForExternal(blogId, entityId, role,
                            externalSystemId, externalObjectId);
                }
                LOGGER.trace("Assigned entity {} with role {} to topic {} for external system {}",
                        entityId, role, blogId, externalSystemId);
                // remove entity from the current blogrights stack
                entityRolesToRemove.remove(entityId);
                return true;
            } catch (ExternalObjectNotAssignedException e) {
                LOGGER.error("Error synchronizing rights for entity " + entityId + " and blog "
                        + blogId, e);
                // remove entity from entities to remove to avoid wrong removal
                entityRolesToRemove.remove(entityId);
            } catch (CommunoteEntityNotFoundException e) {
                // since entity cannot be found we can keep in the entities to remove
                LOGGER.error("Error synchronizing rights for entity " + entityId + " and blog "
                        + blogId, e);
            } catch (RuntimeException e) {
                // converts BlogRightsException especially when we encounter some unexpected
                // exceptions and RTE e.g. a constraint violation due to concurrent operations
                LOGGER.error("Error synchronizing rights for entity " + entityId + " and blog "
                        + blogId, e);
                entityRolesToRemove.remove(entityId);
            }
        } else {
            LOGGER.trace("Skipping synchronize request for null entity");
        }
        return false;
    }

    /**
     * adds or changes the role for an external user
     *
     *
     * @param externalId
     *            identifier of an external object
     * @param externalRoles
     *            external user and role
     * @param entityRolesToRemove
     *            list of entity identifier to remove
     * @throws BlogAccessException
     *             in case the current user is not allowed to modify the access rights
     * @throws BlogNotFoundException
     *             in case the blog does not exist
     */
    private void assignUserRoles(String externalId,
            Map<String, BlogRole> externalRoles,
            Map<Long, BlogRole> entityRolesToRemove) throws BlogAccessException,
            BlogNotFoundException {
        if (externalRoles != null) {
            for (String externalEntityId : externalRoles.keySet()) {
                Long entityId = getUserIdByExternalUserIdFromExternalSystem(externalEntityId);
                assignRoleToEntityForExternal(entityId, externalRoles.get(externalEntityId),
                        externalId, entityRolesToRemove);
            }
        }
    }

    /**
     * Extract the entity ID from the TO. This will convert an alias of a user or group into the ID
     * and also resolve external identifiers.
     *
     * @param roleTO
     *            the TO to process
     * @return the ID of the entity or null if there is no matching entity
     */
    private Long extractEntityId(ExternalTopicRoleTO roleTO) {
        Long entityId = null;
        if (roleTO.getEntityId() != null) {
            entityId = roleTO.getEntityId();
        } else if (roleTO.getIsGroup() != null && roleTO.getIsGroup()) {
            entityId = getGroupId(roleTO.getEntityAlias(), roleTO.getExternalEntityId());
        } else {
            entityId = getUserId(roleTO.getEntityAlias(), roleTO.getExternalEntityId());
        }
        return entityId;
    }

    /**
     * Get the BlogMemberDao
     *
     * @return BlogMemberDao
     */
    private BlogMemberDao getBlogMemberDao() {
        return ServiceLocator.findService(BlogMemberDao.class);
    }

    /**
     * Gets the BlogRightsManagement of the ServiceLocator
     *
     * @return BlogRightsManagement
     */
    private BlogRightsManagement getBlogRightsManagement() {
        return ServiceLocator.findService(BlogRightsManagement.class);
    }

    /**
     * Get the {@link ExternalObjectManagement}
     *
     * @return the management of external object.
     */
    private ExternalObjectManagement getExternalObjectManagement() {
        return ServiceLocator.instance().getService(ExternalObjectManagement.class);
    }

    /**
     * Get the group ID form the internal alias or the external group identifier in combination with
     * the external system identifier. If the alias is set it is preferred.
     *
     * @param groupAlias
     *            alias of the group, can be null
     * @param externalGroupId
     *            external identifier of the group within the external system, can be null
     * @return ID of the group or null if the group cannot be resolved
     */
    private Long getGroupId(String groupAlias, String externalGroupId) {
        Long groupId = null;
        if (StringUtils.isNotBlank(groupAlias)) {
            try {
                groupId = getUserService().getGroup(groupAlias).getId();
            } catch (GroupNotFoundException e) {
                LOGGER.debug("Group with alias {} not found", groupAlias);
            }
        } else if (StringUtils.isNotBlank(externalGroupId)) {
            groupId = getGroupIdByExternalGroupIdFromExternalSystem(externalGroupId);
        }
        return groupId;
    }

    /**
     * Gets the ID of an external group
     *
     * @param externalGroupId
     *            the identifier of the group in the external system
     * @return the ID of the group
     */
    private Long getGroupIdByExternalGroupIdFromExternalSystem(String externalGroupId) {
        if (!externalGroupIdsCache.containsKey(externalGroupId)) {
            Group entity;
            try {
                entity = getUserService().getGroup(externalGroupId, externalSystemId);

                LOGGER.debug("externalGroupId {} not found", externalGroupId);
                externalGroupIdsCache.put(externalGroupId, entity.getId());
                return entity.getId();

            } catch (GroupNotFoundException e) {
                LOGGER.debug(e.getMessage(), e);
                LOGGER.warn("External group with externalGroupId {}"
                        + " not found in external system {}: {}", externalGroupId, externalSystemId);
            }
        }
        return externalGroupIdsCache.get(externalGroupId);
    }

    /**
     * Get the user ID form the internal alias or the external user identifier in combination with
     * the external system identifier. If the alias is set it is preferred.
     *
     * @param userAlias
     *            alias of the user, can be null
     * @param externalUserId
     *            external identifier of the user within the external system, can be null
     * @return ID of the user or null if the user cannot be resolved
     */
    private Long getUserId(String userAlias, String externalUserId) {
        Long userId = null;
        if (StringUtils.isNotBlank(userAlias)) {
            try {
                userId = getUserService().getUser(userAlias).getId();
            } catch (UserNotFoundException e) {
                LOGGER.debug("User with alias {} not found", userAlias);
            }
        } else if (StringUtils.isNotBlank(externalUserId)) {
            userId = getUserIdByExternalUserIdFromExternalSystem(externalUserId);
        }
        return userId;
    }

    /**
     * Get the user id of external user id from external system
     *
     * @param externalUserId
     *            identifier of external user
     * @return internal user identifier
     */
    private Long getUserIdByExternalUserIdFromExternalSystem(String externalUserId) {
        if (!externalUserIdsCache.containsKey(externalUserId)) {
            try {
                Long userId = getUserService().getUser(externalUserId,
                        externalSystemId).getId();
                externalUserIdsCache.put(externalUserId, userId);
                return userId;
            } catch (UserNotFoundException e) {
                LOGGER.warn("External user with externalUserId {}"
                        + " not found in external system {}", externalUserId, externalSystemId);
            }
        }
        return externalUserIdsCache.get(externalUserId);
    }

    /**
     * Gets the UserService of the ServiceLocator
     *
     * @return {@link UserService}
     */
    private UserService getUserService() {
        return ServiceLocator.instance().getService(UserService.class);
    }

    /**
     * Test whether an external object is assigned to the blog
     *
     * @param externalObjectId
     *            the ID of the external object with in the external system
     * @return true if the external object is assigned, false otherwise
     * @throws BlogAccessException
     *             in case the current user is not allowed to read the blog
     * @throws BlogNotFoundException
     *             in case the blog does not exist
     */
    private boolean isExternalObjectAssigned(String externalObjectId) throws BlogAccessException,
    BlogNotFoundException {
        if (externalObjectId == null || externalObjectNotAssignedCache.contains(externalObjectId)) {
            return false;
        }
        if (getExternalObjectManagement().isExternalObjectAssigned(blogId,
                externalSystemId, externalObjectId)) {
            return true;
        }
        externalObjectNotAssignedCache.add(externalObjectId);
        return false;
    }

    /**
     * Log the input parameter of synchronize function.
     *
     * @param externalObjectId
     *            identifier of the external object within the external system
     * @param externalUserRoles
     *            external user and role
     * @param externalGroupRoles
     *            external group and role
     */
    private void logSync(String externalObjectId, Map<String, BlogRole> externalUserRoles,
            Map<String, BlogRole> externalGroupRoles) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("Synchronization for blog {}", blogId);
            for (String externalUserRoleKey : externalUserRoles.keySet()) {
                LOGGER.trace("Synchronize user with externalUserId " + externalUserRoleKey
                        + " with role " + externalUserRoles.get(externalUserRoleKey).getValue());
            }
            for (String externalGroupRolesKey : externalGroupRoles.keySet()) {
                LOGGER.trace("Synchronize user with externalGroupId " + externalGroupRolesKey
                        + " with role " + externalGroupRoles.get(externalGroupRolesKey).getValue());
            }
        }
        LOGGER.debug("Synchronize {} external users and {} external groups for blog {}",
                new Object[] {
                ((externalUserRoles != null) ? externalUserRoles.size() : null),
                ((externalGroupRoles != null) ? externalGroupRoles.size() : null), blogId });
    }

    /**
     * Synchronize blog rights for an external system by merging them with the existing rights. New
     * entities will be added and existing will be updated.
     *
     * @param rolesToSetOrUpdate
     *            collection of entities and roles to add or update
     * @param rolesToRemove
     *            optional collection of entities to remove from the blog. If an entity is also
     *            contained in the rolesToSetOrUpdate it will still be removed. The role member of
     *            the TO is ignored.
     * @throws BlogAccessException
     *             in case the current user is not allowed to modify the blog access rights for the
     *             external system
     * @throws BlogNotFoundException
     *             in case the blog does not exist
     */
    public void mergeRights(List<ExternalTopicRoleTO> rolesToSetOrUpdate,
            List<ExternalTopicRoleTO> rolesToRemove) throws BlogAccessException,
            BlogNotFoundException {
        LOGGER.debug("Starting synchronization by merge for blog {} and external system {}",
                blogId, externalSystemId);
        externalObjectNotAssignedCache.clear();
        // pass an empty map for the roles to remove as the existing roles are not replaced
        Map<Long, BlogRole> empty = Collections.emptyMap();
        assignByExternalTopicRoleTOs(rolesToSetOrUpdate, empty);
        if (rolesToRemove != null) {
            // get entity IDs for removal
            HashSet<Long> entitiesToRemove = new HashSet<Long>(rolesToRemove.size());
            for (ExternalTopicRoleTO roleTO : rolesToRemove) {
                Long entityId = extractEntityId(roleTO);
                // TODO should we check the externalObjectId? The other sync methods don't do it,
                // but it's part of the TO, hm ...
                if (entityId != null) {
                    entitiesToRemove.add(entityId);
                }
            }
            removeEntities(entitiesToRemove);
        }
        LOGGER.debug("Synchronization by merge for blog {} and external system {} completed",
                blogId, externalSystemId);
    }

    /**
     * Remove entities
     *
     * @param entityRolesToRemove
     *            list of entity IDs to remove
     * @throws BlogAccessException
     *             in case the current user is not allowed to modify the blog access rights for the
     *             external system
     * @throws BlogNotFoundException
     *             in case the blog does not exist
     */
    private void removeEntities(Collection<Long> entityRolesToRemove) throws BlogNotFoundException,
    BlogAccessException {
        int entitiesRemoved = 0;
        if (!entityRolesToRemove.isEmpty()) {
            for (Long entityId : entityRolesToRemove) {
                try {
                    getBlogRightsManagement().removeMemberByEntityIdForExternal(blogId, entityId,
                            externalSystemId);
                    entitiesRemoved++;
                    if (LOGGER.isTraceEnabled()) {
                        LOGGER.trace("Removed entity " + entityId + " from blog " + blogId
                                + " for external system " + externalSystemId);
                    }

                } catch (BlogRightsManagementException e) {
                    LOGGER.error("Error removing entity " + entityId + " from blog " + blogId, e);
                }
            }
            LOGGER.debug("Removed {} entities from blog {}", entitiesRemoved, blogId);
        }
    }

    /**
     * Synchronize the members of the blog by replacing the existing member roles that were assigned
     * for the external system with the provided members and there roles. Entities assigned to the
     * blog on behalf of the external system that are not contained in the input will be removed.
     *
     * @param rolesToSet
     *            collection of entities and roles to add or update
     * @throws BlogAccessException
     *             in case the current user is not allowed to modify the blog access rights for the
     *             external system
     * @throws BlogNotFoundException
     *             in case the blog does not exist
     */
    public void replaceRights(List<ExternalTopicRoleTO> rolesToSet) throws BlogAccessException,
    BlogNotFoundException {
        LOGGER.debug(
                "Starting synchronization by replace for blog {} and external system {}",
                blogId, externalSystemId);
        externalObjectNotAssignedCache.clear();
        Map<Long, BlogRole> entityRolesToRemove = getBlogMemberDao().getBlogRoles(blogId,
                externalSystemId);

        assignByExternalTopicRoleTOs(rolesToSet, entityRolesToRemove);

        removeEntities(entityRolesToRemove.keySet());
        LOGGER.debug("Synchronization by replace for blog {} and external system {} completed",
                blogId, externalSystemId);
    }

    /**
     * Synchronize the members of the blog by replacing the existing member roles that were assigned
     * for the external system with the provided members and there roles. Entities assigned to the
     * blog on behalf of the external system that are not contained in the input will be removed.
     *
     * @param externalObjectId
     *            identifier of an external object that has to be assigned to the blog. If the
     *            external object is not assigned the roles won't be synchronized.
     * @param externalUserRoles
     *            mapping from the user identifier in the external system to the role to assign
     * @param externalGroupRoles
     *            mapping from the group identifier in the external system to the role to assign
     * @throws BlogAccessException
     *             in case the current user is not allowed to modify the blog access rights for the
     *             external system
     * @throws BlogNotFoundException
     *             in case the blog does not exist
     */
    public void replaceRights(String externalObjectId, Map<String, BlogRole> externalUserRoles,
            Map<String, BlogRole> externalGroupRoles) throws BlogAccessException,
            BlogNotFoundException {
        logSync(externalObjectId, externalUserRoles, externalGroupRoles);
        // clear external object cache
        externalObjectNotAssignedCache.clear();

        if (isExternalObjectAssigned(externalObjectId)) {
            Map<Long, BlogRole> entityRolesToRemove = getBlogMemberDao().getBlogRoles(blogId,
                    externalSystemId);
            assignUserRoles(externalObjectId, externalUserRoles, entityRolesToRemove);
            assignGroupRoles(externalObjectId, externalGroupRoles, entityRolesToRemove);

            removeEntities(entityRolesToRemove.keySet());
            LOGGER.debug("Synchronization by replace for blog {} and external object {} completed",
                    blogId, externalObjectId);
        } else {
            LOGGER.error("Cannot synchronize roles for blog " + blogId
                    + " because it is not assigned to the external object " + externalObjectId);
        }

    }

}
