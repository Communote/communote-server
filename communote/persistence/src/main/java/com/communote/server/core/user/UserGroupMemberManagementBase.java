package com.communote.server.core.user;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.api.core.user.UserNotFoundException;
import com.communote.server.core.user.group.CantAddParentAsChildException;
import com.communote.server.core.user.group.GroupNotFoundException;
import com.communote.server.core.user.group.GroupOperationNotPermittedException;

/**
 * <p>
 * Spring Service base class for
 * <code>com.communote.server.service.user.UserGroupMemberManagement</code>, provides access to all
 * services and entities referenced by this service.
 * </p>
 * 
 * @see com.communote.server.core.user.UserGroupMemberManagement
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
@Transactional(propagation = Propagation.REQUIRED)
public abstract class UserGroupMemberManagementBase implements UserGroupMemberManagement {

    @Override
    public void addGroup(Long targetGroupId, Long groupId)
            throws GroupNotFoundException, CantAddParentAsChildException,
            AuthorizationException {
        if (targetGroupId == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.service.user.UserGroupMemberManagement.addGroup(Long targetGroupId, Long groupId) - 'targetGroupId' can not be null");
        }
        if (groupId == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.service.user.UserGroupMemberManagement.addGroup(Long targetGroupId, Long groupId) - 'groupId' can not be null");
        }
        try {
            this.handleAddGroup(targetGroupId, groupId);
        } catch (RuntimeException rt) {
            throw new com.communote.server.core.user.UserGroupMemberManagementException(
                    "Error performing 'com.communote.server.service.user.UserGroupMemberManagement.addGroup(Long targetGroupId, Long groupId)' --> "
                            + rt,
                    rt);
        }
    }

    @Override
    public void addUser(Long groupId, Long userId)
            throws GroupNotFoundException,
            UserNotFoundException,
            AuthorizationException {
        if (groupId == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.service.user.UserGroupMemberManagement.addUser(Long groupId, Long userId) - 'groupId' can not be null");
        }
        if (userId == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.service.user.UserGroupMemberManagement.addUser(Long groupId, Long userId) - 'userId' can not be null");
        }
        try {
            this.handleAddUser(groupId, userId);
        } catch (RuntimeException rt) {
            throw new com.communote.server.core.user.UserGroupMemberManagementException(
                    "Error performing 'com.communote.server.service.user.UserGroupMemberManagement.addUser(Long groupId, Long userId)' --> "
                            + rt,
                    rt);
        }
    }

    @Override
    public void addUserForExternal(Long groupId, Long userId,
            String externalSystemId)
            throws GroupNotFoundException,
            UserNotFoundException, AuthorizationException {
        if (groupId == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.service.user.UserGroupMemberManagement.addUserForExternal(Long groupId, Long userId, String externalSystemId) - 'groupId' can not be null");
        }
        if (userId == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.service.user.UserGroupMemberManagement.addUserForExternal(Long groupId, Long userId, String externalSystemId) - 'userId' can not be null");
        }
        if (externalSystemId == null || externalSystemId.trim().length() == 0) {
            throw new IllegalArgumentException(
                    "com.communote.server.service.user.UserGroupMemberManagement.addUserForExternal(Long groupId, Long userId, String externalSystemId) - 'externalSystemId' can not be null or empty");
        }
        try {
            this.handleAddUserForExternal(groupId, userId, externalSystemId);
        } catch (RuntimeException rt) {
            throw new com.communote.server.core.user.UserGroupMemberManagementException(
                    "Error performing 'com.communote.server.service.user.UserGroupMemberManagement.addUserForExternal(Long groupId, Long userId, String externalSystemId)' --> "
                            + rt,
                    rt);
        }
    }

    /**
     * @see com.communote.server.core.user.UserGroupMemberManagement#containsEntityDirectly(Long,
     *      Long)
     */
    @Override
    @Transactional(readOnly = true)
    public boolean containsEntityDirectly(Long groupId, Long entityId)
            throws GroupNotFoundException {
        if (groupId == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.service.user.UserGroupMemberManagement.containsEntityDirectly(Long groupId, Long entityId) - 'groupId' can not be null");
        }
        if (entityId == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.service.user.UserGroupMemberManagement.containsEntityDirectly(Long groupId, Long entityId) - 'entityId' can not be null");
        }
        try {
            return this.handleContainsEntityDirectly(groupId, entityId);
        } catch (RuntimeException rt) {
            throw new com.communote.server.core.user.UserGroupMemberManagementException(
                    "Error performing 'com.communote.server.service.user.UserGroupMemberManagement.containsEntityDirectly(Long groupId, Long entityId)' --> "
                            + rt,
                    rt);
        }
    }

    /**
     * @see com.communote.server.core.user.UserGroupMemberManagement#containsUser(Long, Long)
     */
    @Override
    @Transactional(readOnly = true)
    public boolean containsUser(Long groupId, Long userId)
            throws GroupNotFoundException {
        if (groupId == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.service.user.UserGroupMemberManagement.containsUser(Long groupId, Long userId) - 'groupId' can not be null");
        }
        if (userId == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.service.user.UserGroupMemberManagement.containsUser(Long groupId, Long userId) - 'userId' can not be null");
        }
        try {
            return this.handleContainsUser(groupId, userId);
        } catch (RuntimeException rt) {
            throw new com.communote.server.core.user.UserGroupMemberManagementException(
                    "Error performing 'com.communote.server.service.user.UserGroupMemberManagement.containsUser(Long groupId, Long userId)' --> "
                            + rt,
                    rt);
        }
    }

    /**
     * @see com.communote.server.core.user.UserGroupMemberManagement#containsUserDirectly(Long,
     *      Long)
     */
    @Override
    @Transactional(readOnly = true)
    public boolean containsUserDirectly(Long groupId, Long userId)
            throws GroupNotFoundException {
        if (groupId == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.service.user.UserGroupMemberManagement.containsUserDirectly(Long groupId, Long userId) - 'groupId' can not be null");
        }
        if (userId == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.service.user.UserGroupMemberManagement.containsUserDirectly(Long groupId, Long userId) - 'userId' can not be null");
        }
        try {
            return this.handleContainsUserDirectly(groupId, userId);
        } catch (RuntimeException rt) {
            throw new com.communote.server.core.user.UserGroupMemberManagementException(
                    "Error performing 'com.communote.server.service.user.UserGroupMemberManagement.containsUserDirectly(Long groupId, Long userId)' --> "
                            + rt,
                    rt);
        }
    }

    /**
     * Gets the current <code>principal</code> if one has been set, otherwise returns
     * <code>null</code>.
     * 
     * @return the current principal
     */
    protected java.security.Principal getPrincipal() {
        return com.communote.server.PrincipalStore.get();
    }

    /**
     * @see com.communote.server.core.user.UserGroupMemberManagement#getUsersOfGroup(Long)
     */
    @Override
    @Transactional(readOnly = true)
    public java.util.List<com.communote.server.api.core.user.UserData> getUsersOfGroup(
            Long groupId)
            throws GroupNotFoundException {
        if (groupId == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.service.user.UserGroupMemberManagement.getUsersOfGroup(Long groupId) - 'groupId' can not be null");
        }
        try {
            return this.handleGetUsersOfGroup(groupId);
        } catch (RuntimeException rt) {
            throw new com.communote.server.core.user.UserGroupMemberManagementException(
                    "Error performing 'com.communote.server.service.user.UserGroupMemberManagement.getUsersOfGroup(Long groupId)' --> "
                            + rt,
                    rt);
        }
    }

    /**
     * Add a group to a group if it is not already contained.
     * 
     * @param targetGroupId
     *            ID of the target group the other group should be added to
     * @param groupId
     *            ID of the group to add
     * @throws GroupOperationNotPermittedException
     *             in case the target group is an external group
     * @throws GroupNotFoundException
     *             in case the target group or the group to add does not exist
     * @throws CantAddParentAsChildException
     *             in case adding the group would lead to a cyclic group hierarchy
     * @throws AuthorizationException
     *             in case the current user is not client manager
     */
    protected abstract void handleAddGroup(Long targetGroupId, Long groupId)
            throws GroupOperationNotPermittedException,
            GroupNotFoundException,
            CantAddParentAsChildException,
            AuthorizationException;

    /**
     * Performs the core logic for {@link #addUser(Long, Long)}
     * 
     * @throws AuthorizationException
     *             in case the current user is not client manager
     */
    protected abstract void handleAddUser(Long groupId, Long userId)
            throws GroupNotFoundException,
            UserNotFoundException,
            GroupOperationNotPermittedException,
            AuthorizationException;

    /**
     * Performs the core logic for {@link #addUserForExternal(Long, Long, String)}
     */
    protected abstract void handleAddUserForExternal(Long groupId, Long userId,
            String externalSystemId)
            throws GroupNotFoundException,
            UserNotFoundException, AuthorizationException;

    /**
     * Performs the core logic for {@link #containsEntityDirectly(Long, Long)}
     */
    protected abstract boolean handleContainsEntityDirectly(Long groupId,
            Long entityId)
            throws GroupNotFoundException;

    /**
     * Performs the core logic for {@link #containsUser(Long, Long)}
     */
    protected abstract boolean handleContainsUser(Long groupId, Long userId)
            throws GroupNotFoundException;

    /**
     * Performs the core logic for {@link #containsUserDirectly(Long, Long)}
     */
    protected abstract boolean handleContainsUserDirectly(Long groupId,
            Long userId)
            throws GroupNotFoundException;

    /**
     * Performs the core logic for {@link #getUsersOfGroup(Long)}
     */
    protected abstract java.util.List<com.communote.server.api.core.user.UserData> handleGetUsersOfGroup(
            Long groupId)
            throws GroupNotFoundException;

    /**
     * Performs the core logic for {@link #removeEntityFromGroup(Long, Long)}
     */
    protected abstract void handleRemoveEntityFromGroup(Long groupId,
            Long entityId)
            throws GroupOperationNotPermittedException,
            GroupNotFoundException;

    /**
     * Performs the core logic for {@link #removeGroupFromAllGroups(Long)}
     * 
     * @throws AuthorizationException
     */
    protected abstract void handleRemoveGroupFromAllGroups(Long entityId)
            throws GroupNotFoundException, AuthorizationException;

    /**
     * Performs the core logic for {@link #removeUserForExternal(Long, Long, String)}
     */
    protected abstract void handleRemoveUserForExternal(Long groupId,
            Long userId, String externalSystemId) throws AuthorizationException;

    /**
     * Performs the core logic for {@link #removeUserFromAllGroups(Long)}
     */
    protected abstract void handleRemoveUserFromAllGroups(Long userId)
            throws UserNotFoundException;

    /**
     * @see com.communote.server.core.user.UserGroupMemberManagement#removeEntityFromGroup(Long,
     *      Long)
     */
    @Override
    public void removeEntityFromGroup(Long groupId, Long entityId)
            throws GroupOperationNotPermittedException,
            GroupNotFoundException {
        if (groupId == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.service.user.UserGroupMemberManagement.removeEntityFromGroup(Long groupId, Long entityId) - 'groupId' can not be null");
        }
        if (entityId == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.service.user.UserGroupMemberManagement.removeEntityFromGroup(Long groupId, Long entityId) - 'entityId' can not be null");
        }
        try {
            this.handleRemoveEntityFromGroup(groupId, entityId);
        } catch (RuntimeException rt) {
            throw new com.communote.server.core.user.UserGroupMemberManagementException(
                    "Error performing 'com.communote.server.service.user.UserGroupMemberManagement.removeEntityFromGroup(Long groupId, Long entityId)' --> "
                            + rt,
                    rt);
        }
    }

    @Override
    public void removeGroupFromAllGroups(Long entityId)
            throws GroupNotFoundException, AuthorizationException {
        if (entityId == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.service.user.UserGroupMemberManagement.removeGroupFromAllGroups(Long entityId) - 'entityId' can not be null");
        }
        try {
            this.handleRemoveGroupFromAllGroups(entityId);
        } catch (RuntimeException rt) {
            throw new com.communote.server.core.user.UserGroupMemberManagementException(
                    "Error performing 'com.communote.server.service.user.UserGroupMemberManagement.removeGroupFromAllGroups(Long entityId)' --> "
                            + rt,
                    rt);
        }
    }

    @Override
    public void removeUserForExternal(Long groupId, Long userId,
            String externalSystemId) throws AuthorizationException {
        if (groupId == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.service.user.UserGroupMemberManagement.removeUserForExternal(Long groupId, Long userId, String externalSystemId) - 'groupId' can not be null");
        }
        if (userId == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.service.user.UserGroupMemberManagement.removeUserForExternal(Long groupId, Long userId, String externalSystemId) - 'userId' can not be null");
        }
        if (externalSystemId == null || externalSystemId.trim().length() == 0) {
            throw new IllegalArgumentException(
                    "com.communote.server.service.user.UserGroupMemberManagement.removeUserForExternal(Long groupId, Long userId, String externalSystemId) - 'externalSystemId' can not be null or empty");
        }
        try {
            this.handleRemoveUserForExternal(groupId, userId, externalSystemId);
        } catch (RuntimeException rt) {
            throw new com.communote.server.core.user.UserGroupMemberManagementException(
                    "Error performing 'com.communote.server.service.user.UserGroupMemberManagement.removeUserForExternal(Long groupId, Long userId, String externalSystemId)' --> "
                            + rt,
                    rt);
        }
    }

    /**
     * @see com.communote.server.core.user.UserGroupMemberManagement#removeUserFromAllGroups(Long)
     */
    @Override
    public void removeUserFromAllGroups(Long userId)
            throws UserNotFoundException {
        if (userId == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.service.user.UserGroupMemberManagement.removeUserFromAllGroups(Long userId) - 'userId' can not be null");
        }
        try {
            this.handleRemoveUserFromAllGroups(userId);
        } catch (RuntimeException rt) {
            throw new com.communote.server.core.user.UserGroupMemberManagementException(
                    "Error performing 'com.communote.server.service.user.UserGroupMemberManagement.removeUserFromAllGroups(Long userId)' --> "
                            + rt,
                    rt);
        }
    }
}