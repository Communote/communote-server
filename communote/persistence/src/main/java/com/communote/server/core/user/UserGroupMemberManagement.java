package com.communote.server.core.user;

import java.util.Collection;
import java.util.List;

import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.api.core.user.UserData;
import com.communote.server.api.core.user.UserNotFoundException;
import com.communote.server.core.user.group.CantAddParentAsChildException;
import com.communote.server.core.user.group.GroupNotFoundException;
import com.communote.server.core.user.group.GroupOperationNotPermittedException;

/**
 * Management for group memberships.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public interface UserGroupMemberManagement {

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
    public void addGroup(Long targetGroupId, Long groupId)
            throws GroupOperationNotPermittedException, GroupNotFoundException,
            CantAddParentAsChildException, AuthorizationException;

    /**
     * Add a user to a group
     *
     * @param groupId
     *            the ID of the group to add the user to
     * @param userId
     *            the ID of the user to add
     * @throws GroupNotFoundException
     *             in case the group does not exist
     * @throws UserNotFoundException
     *             in case the user does not exist
     * @throws GroupOperationNotPermittedException
     *             in case the group is an external group
     * @throws AuthorizationException
     *             in case the current user is not client manager
     */
    public void addUser(Long groupId, Long userId) throws GroupNotFoundException,
    UserNotFoundException, GroupOperationNotPermittedException, AuthorizationException;

    /**
     * Adds a user to an external group. The external group must have an externalSystemId that is
     * equal to the value of the externalSystemId parameter.
     *
     * @param groupId
     *            the ID of the external group to add the user to
     * @param userId
     *            the ID of the user to add
     * @param externalSystemId
     *            the ID of the external system the group should belong to
     * @throws GroupNotFoundException
     *             in case the group does not exist
     * @throws UserNotFoundException
     *             in case the user does not exist
     * @throws GroupOperationNotPermittedException
     *             in case the group is not a group from the external system identified by the
     *             externalSystemId
     * @throws AuthorizationException
     *             in case the current user is not the internal system user
     */
    public void addUserForExternal(Long groupId, Long userId, String externalSystemId)
            throws GroupNotFoundException, UserNotFoundException, AuthorizationException;

    /**
     * <p>
     * Returns true if the group or user is a direct member of the group.
     * </p>
     */
    public boolean containsEntityDirectly(Long groupId, Long entityId)
            throws GroupNotFoundException;

    /**
     * <p>
     * Returns true if the user is a direct or indirect member of the group.
     * </p>
     */
    public boolean containsUser(Long groupId, Long userId) throws GroupNotFoundException;

    /**
     * <p>
     * Returns true if the user is a direct member of the group.
     * </p>
     */
    public boolean containsUserDirectly(Long groupId, Long userId) throws GroupNotFoundException;

    /**
     * Return the number of direct members of a group.
     *
     * @param groupId
     *            the ID of the group whose members should be counted
     * @return the number of direct members
     */
    int countMembers(Long groupId);

    /**
     * Return all users who are direct members of a group.
     *
     * @param groupId
     *            Id of the group.
     * @return a list of all users who are direct members of the given group.
     *
     * @throws GroupNotFoundException
     *             in case the group does not exist
     */
    public List<UserData> getUsersOfGroup(Long groupId) throws GroupNotFoundException;

    /**
     * Return all IDs of users who are direct members of a given group.
     *
     * @param groupId
     *            Id of the group.
     * @param externalSystemId
     *            If set, only users of the given external system are considered.
     *
     * @return a list of all user IDs who are direct members of the given group.
     *
     * @throws GroupNotFoundException
     *             in case the group does not exist
     */
    public Collection<Long> getUsersOfGroup(Long groupId, String externalSystemId)
            throws GroupNotFoundException;

    /**
     * <p>
     * This method removes an entity from a group.
     * </p>
     */
    public void removeEntityFromGroup(Long groupId, Long entityId)
            throws GroupOperationNotPermittedException, GroupNotFoundException;

    /**
     * Remove an external group from all external groups of the same external system that contain
     * this group as a direct member.
     *
     * @param groupId
     *            ID of the external group to remove from the other external groups
     * @param groupIdsToIgnore
     *            list of IDs of groups the group should not be removed from. Can be null.
     * @throws GroupNotFoundException
     *             in case the group does not exist
     * @throws AuthorizationException
     *             in case the current user is not the internal system user
     */
    void removeGroupFromAllExternalGroups(Long groupId, Collection<Long> groupIdsToIgnore)
            throws GroupNotFoundException, AuthorizationException;

    /**
     * Remove a group from all groups it is a member of.
     *
     * @param groupId
     *            The id of the group to remove
     * @throws GroupNotFoundException
     *             in case the group does not exist
     * @throws AuthorizationException
     *             in case the current user is not client manager or the internal system user
     */
    public void removeGroupFromAllGroups(Long groupId) throws GroupNotFoundException,
            AuthorizationException;

    /**
     * Removes a user from an external group. The external group must have an externalSystemId that
     * is equal to the value of the externalSystemId parameter. If the group does not exist or the
     * user is not a member of that group nothing will happen.
     *
     * @param groupId
     *            the ID of the external group
     * @param userId
     *            the ID of the user to remove
     * @param externalSystemId
     *            the ID of the external system the group originated from
     * @throws AuthorizationException
     *             in case the current user is not the internal system user
     */
    public void removeUserForExternal(Long groupId, Long userId, String externalSystemId)
            throws AuthorizationException;

    /**
     *
     */
    public void removeUserFromAllGroups(Long userId) throws UserNotFoundException;

}
