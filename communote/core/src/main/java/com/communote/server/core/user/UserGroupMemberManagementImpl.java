package com.communote.server.core.user;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.blog.BlogRightsManagement;
import com.communote.server.api.core.event.EventDispatcher;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.api.core.user.UserData;
import com.communote.server.api.core.user.UserNotFoundException;
import com.communote.server.core.blog.events.UserToTopicRoleMappingChangedEvent;
import com.communote.server.core.blog.helper.BlogRoleHelper;
import com.communote.server.core.groups.GroupUtils;
import com.communote.server.core.security.SecurityHelper;
import com.communote.server.core.user.group.CantAddParentAsChildException;
import com.communote.server.core.user.group.GroupNotFoundException;
import com.communote.server.core.user.group.GroupOperationNotPermittedException;
import com.communote.server.model.blog.BlogMember;
import com.communote.server.model.blog.BlogRole;
import com.communote.server.model.blog.ExternalBlogMember;
import com.communote.server.model.blog.UserToBlogRoleMapping;
import com.communote.server.model.user.CommunoteEntity;
import com.communote.server.model.user.ExternalUserAuthentication;
import com.communote.server.model.user.User;
import com.communote.server.model.user.group.ExternalUserGroup;
import com.communote.server.model.user.group.Group;
import com.communote.server.model.user.group.UserOfGroup;
import com.communote.server.model.user.group.UserOfGroupModificationType;
import com.communote.server.persistence.blog.BlogMemberDao;
import com.communote.server.persistence.blog.ExternalBlogMemberDao;
import com.communote.server.persistence.blog.UserToBlogRoleMappingDao;
import com.communote.server.persistence.helper.dao.LazyClassLoaderHelper;
import com.communote.server.persistence.user.CommunoteEntityDao;
import com.communote.server.persistence.user.UserDao;
import com.communote.server.persistence.user.group.ExternalUserGroupDao;
import com.communote.server.persistence.user.group.GroupDao;
import com.communote.server.persistence.user.group.UserOfGroupDao;

/**
 * @param <EntityListItem>
 * @see com.communote.server.core.user.UserGroupMemberManagement
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
@Service("userGroupMemberManagement")
public class UserGroupMemberManagementImpl<EntityListItem> extends
        com.communote.server.core.user.UserGroupMemberManagementBase {

    /** Logger. */
    private final static Logger LOGGER = LoggerFactory
            .getLogger(UserGroupMemberManagementImpl.class);

    @Autowired
    private UserDao kenmeiUserDao;

    @Autowired
    private GroupDao groupDao;

    @Autowired
    private UserToBlogRoleMappingDao userToTopicRoleMappingDao;

    @Autowired
    private BlogMemberDao topicMemberDao;

    @Autowired
    private ExternalBlogMemberDao externalTopicMemberDao;

    @Autowired
    private CommunoteEntityDao kenmeiEntityDao;

    @Autowired
    private UserOfGroupDao userOfGroupDao;

    @Autowired
    private EventDispatcher eventDispatcher;

    @Autowired
    private BlogRightsManagement topicRightsManagement;

    /**
     * Creates a UserOfGroup entity for the provided user and group if it not already exists.
     *
     * @param user
     *            the user to use
     * @param group
     *            the group to use
     * @return true if the entity was created, false if it already exists
     */
    private boolean addUserOfGroupAssociation(User user, Group group) {
        UserOfGroupDao uogDao = userOfGroupDao;
        if (!uogDao.isUserOfGroup(user.getId(), group.getId())) {
            UserOfGroup userOfGroup = UserOfGroup.Factory.newInstance(
                    UserOfGroupModificationType.ADD, group, user);
            uogDao.create(userOfGroup);
            return true;
        }
        return false;
    }

    /**
     * Creates UserOfGroup entities for the users of the UserOfGroup entities of the groupToAdd and
     * the provided group including all groups containing this group. The entities will only be
     * created if they do not yet exist.
     *
     * @param groupToAdd
     *            the group whose (transitive) members should be considered
     * @param group
     *            the group to consider
     * @return whether an entity was created
     */
    private boolean addUserOfGroupAssociationsRecursively(Group groupToAdd, Group group) {
        boolean addedEntity = false;
        Collection<Long> userIds = userOfGroupDao.getUsersOfGroup(groupToAdd.getId());
        for (Long userId : userIds) {
            User user = kenmeiUserDao.load(userId);
            if (addUserOfGroupAssociationsRecursively(user, group)) {
                addedEntity = true;
            }
        }
        return addedEntity;
    }

    /**
     * Creates UserOfGroup entities for the provided user, group and the groups containing the group
     * if these entities do not yet exist.
     *
     * @param user
     *            the user to consider
     * @param group
     *            the group to consider
     *
     * @return whether an entity was created
     */
    private boolean addUserOfGroupAssociationsRecursively(User user, Group group) {
        boolean addedEntity = false;
        for (Group parentGroup : group.getGroups()) {
            if (addUserOfGroupAssociationsRecursively(user, parentGroup)) {
                addedEntity = true;
            }
        }
        if (addUserOfGroupAssociation(user, group)) {
            addedEntity = true;
        }
        return addedEntity;
    }

    /**
     * Updates the blog rights mapping for a new group member.
     *
     * @param blogId
     *            the ID of the blog for which the rights will be stored
     * @param userId
     *            the ID of the user that was added
     * @param role
     *            the role to set
     * @param group
     *            the group the user was added to
     * @param externalSystemId
     *            the ID of the external system
     *
     */
    private void addUserToBlogRoleGrantedByGroup(Long blogId, Long userId, BlogRole role,
            Group group, String externalSystemId) {
        Collection<UserToBlogRoleMapping> existingMappings = userToTopicRoleMappingDao
                .findMappings(blogId, userId, null, true, role);

        final BlogRole beforeTopicRole = topicRightsManagement.getRoleOfEntity(blogId, userId,
                false);

        boolean exisitingGroupUpdated = false;

        // mappings to update are all mappings already containing granting groups and having
        // same external system id
        for (UserToBlogRoleMapping existingMapping : existingMappings) {
            if (StringUtils.equals(externalSystemId, existingMapping.getExternalSystemId())) {
                existingMapping.getGrantingGroups().add(group);
                exisitingGroupUpdated = true;

                eventDispatcher.fire(new UserToTopicRoleMappingChangedEvent(blogId, userId, false,
                        group.getId(), role, role, beforeTopicRole));

            }
        }
        if (!exisitingGroupUpdated) {

            // create a new mapping
            UserToBlogRoleMapping mapping = UserToBlogRoleMapping.Factory.newInstance();
            mapping.setBlogId(blogId);
            mapping.setUserId(userId);
            mapping.setNumericRole(BlogRoleHelper.convertRoleToNumeric(role));
            mapping.setExternalSystemId(externalSystemId);
            mapping.setGrantingGroups(new HashSet<Group>());
            mapping.getGrantingGroups().add(group);
            mapping.setGrantedByGroup(true);
            userToTopicRoleMappingDao.create(mapping);

            eventDispatcher.fire(new UserToTopicRoleMappingChangedEvent(blogId, userId, false,
                    group.getId(), null, role, beforeTopicRole));

        }

    }

    /**
     * @param parentGroup
     *            The parent group.
     * @param childEntity
     *            The child entity.
     * @throws GroupOperationNotPermittedException
     *             Exception.
     */
    private void assertExternalGroupRelations(Group parentGroup, CommunoteEntity childEntity)
            throws GroupOperationNotPermittedException {
        ExternalUserGroup parentAsExternal = toExternalGroup(parentGroup);
        ExternalUserGroup childAsExternal = toExternalGroup(childEntity);
        if (parentAsExternal != null && childAsExternal == null) {
            throw new GroupOperationNotPermittedException(
                    "A local group may not be child of an external group.");
        }
        if (parentAsExternal != null
                && childAsExternal != null
                && !parentAsExternal.getExternalSystemId().equals(
                        childAsExternal.getExternalSystemId())) {
            throw new GroupOperationNotPermittedException(
                    "To relate external groups, both have to be from the same external system.");
        }
    }

    /**
     * Returns a group if it exists, otherwise throws an exception.
     *
     * @param groupId
     *            the ID of the sought group
     * @return the group
     * @throws GroupNotFoundException
     *             if the group does not exist
     */
    private Group assertGroupExists(Long groupId) throws GroupNotFoundException {
        Group group = groupDao.load(groupId);
        if (group == null) {
            throw new GroupNotFoundException("The group with ID " + groupId + " does not exist.");
        }
        return group;
    }

    private User assertUserExists(Long userId) throws UserNotFoundException {
        User user = kenmeiUserDao.load(userId);
        if (user == null) {
            throw new UserNotFoundException("The user with ID " + userId + " does not exist.");
        }
        return user;
    }

    @Override
    @Transactional(readOnly = true)
    public int countMembers(Long groupId) {
        return groupDao.countMembers(groupId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public Collection<Long> getUsersOfGroup(Long groupId, String externalSystemId)
            throws GroupNotFoundException {
        Group group = assertGroupExists(groupId);
        Collection<Long> result = new HashSet<Long>();
        membersLoop: for (CommunoteEntity member : group.getGroupMembers()) {
            member = LazyClassLoaderHelper.deproxy(member, CommunoteEntity.class);
            if (member instanceof User) {
                if (externalSystemId == null) {
                    result.add(member.getId());
                    continue membersLoop;
                }
                Set<ExternalUserAuthentication> externalAuthentications = ((User) member)
                        .getExternalAuthentications();
                if (externalAuthentications == null) {
                    continue membersLoop;
                }
                for (ExternalUserAuthentication authentication : externalAuthentications) {
                    if (externalSystemId.equals(authentication.getSystemId())) {
                        result.add(member.getId());
                        continue membersLoop;
                    }
                }
            }
        }
        return result;
    }

    @Override
    protected void handleAddGroup(Long parentGroupId, Long childGroupId)
            throws GroupNotFoundException, CantAddParentAsChildException, AuthorizationException,
            GroupOperationNotPermittedException {
        if (!SecurityHelper.isInternalSystem() && !SecurityHelper.isClientManager()) {
            throw new AuthorizationException(
                    "Only internal system or client manager is allowed to add groups to a group");
        }
        Group parentGroup = assertGroupExists(parentGroupId);
        Group childGroup = assertGroupExists(childGroupId);
        if (parentGroupId.equals(childGroupId)) {
            throw new CantAddParentAsChildException("Cannot add a group to itself.");
        }
        if (GroupUtils.isChild(childGroup, parentGroup)) {
            throw new CantAddParentAsChildException("Group " + childGroupId
                    + " can't be added to Group +" + parentGroupId
                    + ", because the target group is a (indirect) parent of the group.");
        } else {
            // TODO force system user if groups are external?
            assertExternalGroupRelations(parentGroup, childGroup);
        }
        if (parentGroup.getGroupMembers().add(childGroup)) {
            childGroup.getGroups().add(parentGroup);
            LOGGER.debug("Added group {} to group {}", childGroup.getAlias(),
                    parentGroup.getAlias());
        } else {
            // some details especially useful for synchronizing external groups
            LOGGER.trace("Not adding group {} to group {} because it is already a member",
                    childGroup.getAlias(), parentGroup.getAlias());
        }
        // even if nothing has been added update the UOG relations in case something is inconsistent
        if (addUserOfGroupAssociationsRecursively(childGroup, parentGroup)) {
            updateBlogRightsAfterAdd();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void handleAddUser(Long groupId, Long userId) throws GroupNotFoundException,
    UserNotFoundException, AuthorizationException, GroupOperationNotPermittedException {
        if (!SecurityHelper.isClientManager()) {
            throw new AuthorizationException(
                    "Only client manager is allowed to add users to a group");
        }
        Group group = assertGroupExists(groupId);
        if (isExternalGroup(group)) {
            throw new GroupOperationNotPermittedException("External groups cannot be edited.");
        }
        User user = assertUserExists(userId);
        internalAddUserToGroup(group, user);
    }

    @Override
    protected void handleAddUserForExternal(Long groupId, Long userId, String externalSystemId)
            throws GroupNotFoundException, UserNotFoundException, AuthorizationException {
        if (!SecurityHelper.isInternalSystem()) {
            throw new AuthorizationException(
                    "Only the internal system user can modify external group memeberships");
        }
        try {
            User user = assertUserExists(userId);
            ExternalUserGroup group = ServiceLocator.findService(ExternalUserGroupDao.class).load(
                    groupId);
            // TODO throw specific exception if externalSystemIds do not match?
            if (group == null || !group.getExternalSystemId().equals(externalSystemId)) {
                throw new GroupNotFoundException("The group with ID " + groupId
                        + " does not exist or does not have the externalSystemId "
                        + externalSystemId);
            }
            if (internalAddUserToGroup(group, user)) {
                LOGGER.debug("Added user {} to {} group {}", user.getAlias(), externalSystemId,
                        group.getAlias());
            } else {
                LOGGER.trace("Not adding user {} to {} group {} because it is already a member",
                        user.getAlias(), externalSystemId, group.getAlias());
            }
        } catch (RuntimeException e) {
            LOGGER.error(e.getMessage(), e);
            throw e;
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean handleContainsEntityDirectly(Long groupId, Long entityId)
            throws GroupNotFoundException {
        Group group = assertGroupExists(groupId);
        return internalContainsEntityDirectly(group, kenmeiEntityDao.load(entityId));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean handleContainsUser(Long groupId, Long userId) throws GroupNotFoundException {
        assertGroupExists(groupId);
        return userOfGroupDao.isUserOfGroup(userId, groupId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean handleContainsUserDirectly(Long groupId, Long userId)
            throws GroupNotFoundException {
        Group group = assertGroupExists(groupId);
        return internalContainsEntityDirectly(group, kenmeiUserDao.load(userId));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<UserData> handleGetUsersOfGroup(Long groupId) throws GroupNotFoundException {
        Group g = assertGroupExists(groupId);
        List<UserData> result = new ArrayList<UserData>();
        // TODO What if CommunoteEntity is a group? -> handled through ClassCastException?
        for (CommunoteEntity member : g.getGroupMembers()) {
            try {
                User user = LazyClassLoaderHelper.deproxy(member, User.class);

                UserData item = new UserData();
                item.setAlias(user.getAlias());
                item.setEmail(user.getEmail());
                item.setStatus(user.getStatus());
                item.setFirstName(user.getProfile().getFirstName());
                item.setLastName(user.getProfile().getLastName());
                item.setSalutation(user.getProfile().getSalutation());
                item.setId(user.getId());

                result.add(item);
            } catch (ClassCastException e) {
                continue;
            }
        }
        return result;
    }

    /**
     * {@inheritDoc}
     *
     * @throws GroupNotFoundException
     *             Exception.
     * @throws GroupOperationNotPermittedException
     *             Exception.
     */
    @Override
    protected void handleRemoveEntityFromGroup(Long groupId, Long entityId)
            throws GroupNotFoundException, GroupOperationNotPermittedException {
        // TODO throw AuthorizationException
        SecurityHelper.assertCurrentUserIsClientManager();
        Group targetGroup = assertGroupExists(groupId);
        CommunoteEntity entityToRemove = ServiceLocator.findService(CommunoteEntityDao.class)
                .loadWithImplementation(entityId);
        if (entityToRemove == null) {
            // TODO Throw specific exception?
            return;
        }
        assertExternalGroupRelations(targetGroup, entityToRemove);
        internalRemoveEntityFromGroup(targetGroup, entityToRemove);
    }

    @Override
    protected void handleRemoveGroupFromAllGroups(Long groupId) throws GroupNotFoundException,
    AuthorizationException {
        if (!SecurityHelper.isInternalSystem() && !SecurityHelper.isClientManager()) {
            throw new AuthorizationException(
                    "Only internal system or client manager is allowed to remove a group from all groups");
        }
        Group group = groupDao.load(groupId);
        if (group == null) {
            throw new GroupNotFoundException("Group with id " + groupId + " was not found.");
        }
        internalRemoveEntityFromAllGroups(group);
    }

    @Override
    protected void handleRemoveUserForExternal(Long groupId, Long userId, String externalSystemId)
            throws AuthorizationException {
        if (!SecurityHelper.isInternalSystem()) {
            throw new AuthorizationException(
                    "Only the internal system user can modify external group mameberships");
        }
        ExternalUserGroup group = ServiceLocator.findService(ExternalUserGroupDao.class).load(
                groupId);
        // TODO throw exception if externalSystemIds do not match?
        if (group == null || !group.getExternalSystemId().equals(externalSystemId)) {
            return;
        }
        CommunoteEntity entity = kenmeiUserDao.load(userId);
        if (entity == null) {
            return;
        }
        internalRemoveEntityFromGroup(group, entity);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void handleRemoveUserFromAllGroups(Long userId) {
        if (!userId.equals(SecurityHelper.getCurrentUserId())) {
            // TODO throw AuthorizationException
            if (!SecurityHelper.isInternalSystem()) {
                SecurityHelper.assertCurrentUserIsClientManager();
            }
        }
        User user = kenmeiUserDao.load(userId);
        if (user == null) {
            return;
        }
        internalRemoveEntityFromAllGroups(user);
    }

    /**
     * Adds a user to a group.
     *
     * @param group
     *            the group to modify
     * @param userId
     *            the ID of the user to add
     * @return true if the user was added, false otherwise
     */
    private boolean internalAddUserToGroup(Group group, User user) {
        if (groupDao.isEntityMember(group.getId(), user.getId())) {
            return false;
        }
        Set<CommunoteEntity> members = group.getGroupMembers();
        if (members == null) {
            members = new HashSet<CommunoteEntity>();
            group.setGroupMembers(members);
        }
        Set<Group> usersGroups = user.getGroups();
        if (usersGroups == null) {
            usersGroups = new HashSet<Group>();
            user.setGroups(usersGroups);
        }
        usersGroups.add(group);
        members.add(user);
        if (addUserOfGroupAssociationsRecursively(user, group)) {
            updateBlogRightsAfterAdd();
        }
        return true;
    }

    /**
     * Tests whether an entity is a direct member of the group.
     *
     * @param group
     *            the group
     * @param entity
     *            the entity to check for direct membership
     * @return true if the entity is a direct member of the group, false otherwise
     */
    private boolean internalContainsEntityDirectly(Group group, CommunoteEntity entity) {
        if (entity == null) {
            return false;
        }
        boolean contained = false;
        for (CommunoteEntity member : group.getGroupMembers()) {
            if (member.getId().equals(entity.getId())) {
                contained = true;
                break;
            }
        }
        return contained;
    }

    /**
     * Removes an entity from all groups it is contained in. This also cleans UserOfGroup
     * associations.
     *
     * @param entity
     *            the entity to remove
     */
    private void internalRemoveEntityFromAllGroups(CommunoteEntity entity) {
        List<Group> parentGroupsToHandle = new ArrayList<Group>();
        for (Group group : entity.getGroups()) {
            group.getGroupMembers().remove(entity);
            parentGroupsToHandle.add(group);
        }
        entity.getGroups().clear();
        if (entity instanceof Group) {
            Group groupToRemove = (Group) entity;
            for (Group group : parentGroupsToHandle) {
                removeUserOfGroupAssociationsRecursively(groupToRemove, group);
            }
            removeUserOfGroupAssociationsRecursively(groupToRemove, groupToRemove);
        } else {
            User user = (User) entity;
            for (Group group : parentGroupsToHandle) {
                removeUserOfGroupAssociationsRecursively(user, group);
            }
        }
        updateBlogRightsAfterRemove();
    }

    /**
     * Removes a user from a group.
     *
     * @param group
     *            the group from which the user will be removed
     * @param entity
     *            the entity to remove
     */
    private void internalRemoveEntityFromGroup(Group group, CommunoteEntity entity) {

        if (groupDao.isEntityMember(group.getId(), entity.getId())) {
            entity.getGroups().remove(group);
            group.getGroupMembers().remove(entity);
            boolean updateUser2BlogRole;
            if (entity instanceof User) {
                updateUser2BlogRole = removeUserOfGroupAssociationsRecursively((User) entity, group);
            } else {
                updateUser2BlogRole = removeUserOfGroupAssociationsRecursively((Group) entity,
                        group);
            }
            if (updateUser2BlogRole) {
                updateBlogRightsAfterRemove();
            }
            LOGGER.debug("Removed {} from group {}", entity.getAlias(), group.getAlias());
        }
    }

    /**
     * True if the group is an external group.
     *
     * @param group
     *            the group or proxy
     * @return true if the group is an external group
     */
    private boolean isExternalGroup(CommunoteEntity group) {
        return toExternalGroup(group) != null;
    }

    @Override
    @Transactional
    public void removeGroupFromAllExternalGroups(Long groupId, Collection<Long> groupIdsToIgnore)
            throws GroupNotFoundException, AuthorizationException {
        if (!SecurityHelper.isInternalSystem()) {
            throw new AuthorizationException(
                    "Only internal system is allowed to remove all external groups");
        }
        Group group = assertGroupExists(groupId);
        ExternalUserGroup externalGroup = toExternalGroup(group);
        if (externalGroup == null) {
            throw new GroupNotFoundException("The group with ID " + groupId
                    + " is not an external group");
        }
        ArrayList<Group> groupsToRemoveFrom = new ArrayList<>();
        for (Group parentGroup : externalGroup.getGroups()) {
            // not checking if the externalSystemId matches because it is not possible to add a
            // group to a group from another external system
            if (isExternalGroup(parentGroup)
                    && (groupIdsToIgnore == null || !groupIdsToIgnore.contains(parentGroup.getId()))) {
                groupsToRemoveFrom.add(parentGroup);
            }
        }
        for (Group parentGroup : groupsToRemoveFrom) {
            internalRemoveEntityFromGroup(parentGroup, externalGroup);
        }
    }

    /**
     * Marks the UserOfGroup entity for the provided user and group for removal. The entity will not
     * be marked if the user is a direct member of a group which is a subgroup of the provided
     * group.
     *
     * @param user
     *            the user to consider
     * @param group
     *            the group to consider
     * @return true if the entity was marked, false otherwise
     */
    private boolean removeUserOfGroupAssociation(User user, Group group) {
        if (!GroupUtils.isChild(group, user)) {
            // mark as remove
            UserOfGroup uog = userOfGroupDao.findByUserIdGroupId(user.getId(), group.getId());
            uog.setModificationType(UserOfGroupModificationType.REMOVE);
            return true;
        }
        return false;
    }

    /**
     * Takes the transitively resolved users of the provided groupToRemove and marks the UserOfGroup
     * entities for these users and the provided group and the groups containing this group
     * recursively for removal. The entity will not be marked if the user is a direct member of a
     * group which is a subgroup of the considered group.
     *
     * @param groupToRemove
     *            group whose members should be considered
     * @param group
     *            group to consider
     * @return whether an entity was removed
     */
    private boolean removeUserOfGroupAssociationsRecursively(Group groupToRemove, Group group) {
        boolean removedEntity = false;
        Collection<Long> usersToRemove = userOfGroupDao.getUsersOfGroup(groupToRemove.getId());
        for (Long userId : usersToRemove) {
            User user = kenmeiUserDao.load(userId);
            if (removeUserOfGroupAssociationsRecursively(user, group)) {
                removedEntity = true;
            }
        }
        return removedEntity;
    }

    /**
     * Marks the UserOfGroup entity for the provided user and group and the groups containing this
     * group recursively for removal. The entity will not be marked if the user is a direct member
     * of a group which is a subgroup of the considered group.
     *
     * @param user
     *            the user to consider
     * @param group
     *            the group and all its parents to consider
     * @return true if the entity was marked, false otherwise
     */
    private boolean removeUserOfGroupAssociationsRecursively(User user, Group group) {
        boolean entityRemoved = removeUserOfGroupAssociation(user, group);
        if (entityRemoved) {
            // if the entity was removed also check the groups containing the group
            for (Group parentGroup : group.getGroups()) {
                removeUserOfGroupAssociationsRecursively(user, parentGroup);
            }
        }
        return entityRemoved;
    }

    /**
     * Deproxy a group to an external group if it is one
     *
     * @param group
     *            the group to deproxy
     * @return the external group or null if it is not an external group
     */
    private ExternalUserGroup toExternalGroup(CommunoteEntity group) {
        try {
            return LazyClassLoaderHelper.deproxy(group, ExternalUserGroup.class);
        } catch (ClassCastException e) {
            return null;
        }
    }

    /**
     * Updates the blog rights helper table after adding entities to group.
     */
    private void updateBlogRightsAfterAdd() {
        LOGGER.debug("Updating topic rights");
        Collection<UserOfGroup> addedAssociations = userOfGroupDao
                .getUserOfGroupEntities(UserOfGroupModificationType.ADD);
        Long currentGroupId = null;
        Collection<BlogMember> blogMembers = null;
        for (UserOfGroup userOfGroup : addedAssociations) {
            Long groupId = userOfGroup.getGroup().getId();
            if (!groupId.equals(currentGroupId)) {
                currentGroupId = groupId;
                blogMembers = topicMemberDao.findByEntity(currentGroupId);
            }
            for (BlogMember topicMember : blogMembers) {
                String externalSystemId = null;
                ExternalBlogMember externalTopicMember = externalTopicMemberDao.load(topicMember
                        .getId());
                if (externalTopicMember != null) {
                    externalSystemId = externalTopicMember.getExternalSystemId();
                }
                addUserToBlogRoleGrantedByGroup(topicMember.getBlog().getId(), userOfGroup
                        .getUser().getId(), topicMember.getRole(), userOfGroup.getGroup(),
                        externalSystemId);
            }
            userOfGroup.setModificationType(null);
        }
    }

    /**
     * Updates the blog rights helper table after removing entities from a group.
     */
    private void updateBlogRightsAfterRemove() {
        Collection<UserOfGroup> removedAssociations = userOfGroupDao
                .getUserOfGroupEntities(UserOfGroupModificationType.REMOVE);
        for (UserOfGroup userOfGroup : removedAssociations) {
            userToTopicRoleMappingDao.removeAllForGroupMember(userOfGroup.getUser().getId(),
                    userOfGroup.getGroup().getId());
            userOfGroupDao.remove(userOfGroup);
        }
    }
}
