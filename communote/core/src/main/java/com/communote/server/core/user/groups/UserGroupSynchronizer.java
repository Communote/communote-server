package com.communote.server.core.user.groups;

import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ldap.CommunicationException;
import org.springframework.security.core.context.SecurityContext;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.api.core.user.UserNotFoundException;
import com.communote.server.core.general.RunInTransaction;
import com.communote.server.core.general.TransactionException;
import com.communote.server.core.general.TransactionManagement;
import com.communote.server.core.security.AuthenticationHelper;
import com.communote.server.core.security.SecurityHelper;
import com.communote.server.core.user.AliasAlreadyExistsException;
import com.communote.server.core.user.UserGroupManagement;
import com.communote.server.core.user.UserGroupMemberManagement;
import com.communote.server.core.user.group.AliasValidationException;
import com.communote.server.core.user.group.GroupNotFoundException;
import com.communote.server.core.vo.user.group.ExternalGroupVO;
import com.communote.server.model.user.User;
import com.communote.server.model.user.UserStatus;
import com.communote.server.model.user.group.ExternalUserGroup;
import com.communote.server.model.user.group.Group;
import com.communote.server.persistence.user.UserDao;
import com.communote.server.persistence.user.group.ExternalUserGroupDao;
import com.communote.server.persistence.user.group.GroupDao;
import com.communote.server.plugins.api.externals.ExternalEntityVisitor;
import com.communote.server.plugins.api.externals.ExternalUserGroupAccessor;
import com.communote.server.plugins.api.externals.ExternalUserGroupAccessorException;
import com.communote.server.plugins.exceptions.PluginException;

/**
 * This class handles the synchronization of groups and and memberships.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class UserGroupSynchronizer {

    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(UserGroupSynchronizer.class);

    private final String systemId;

    // caches external groups whose data was already updated in the current run of this worker
    private final Map<String, Long> updatedExternalGroups = new HashMap<String, Long>();
    // Caches external users which where already updated within this run.
    private final Set<Long> updatedExternalUsers = new HashSet<Long>();

    private UserGroupRetriever retriever;

    private final ExternalUserGroupAccessor accessor;

    private final UserGroupManagement userGroupManagement = ServiceLocator.instance().getService(
            UserGroupManagement.class);

    private final UserGroupMemberManagement userGroupMemberManagement = ServiceLocator.instance()
            .getService(UserGroupMemberManagement.class);

    /**
     * Constructor.
     *
     * @param systemId
     *            Id of the external system.
     * @param accessor
     *            The accessor.
     */
    public UserGroupSynchronizer(String systemId, ExternalUserGroupAccessor accessor) {
        this.systemId = systemId;
        this.accessor = accessor;
    }

    /**
     * Returns the local entity ID of an external group after updating or creating it if it does not
     * exist. The update is only done if this instance did not update the group yet.
     *
     * @param externalGroup
     *            the external group to update
     * @return the local entity ID of the group
     * @throws PluginException
     *             in case of an exception while creating the group
     */
    private Long getAndUpdateLocalGroup(ExternalGroupVO externalGroup) throws PluginException {
        Long localGroupId = updatedExternalGroups.get(externalGroup.getExternalId());
        if (localGroupId == null) {
            Long existingGroupId = null;
            try {
                existingGroupId = userGroupManagement.createOrUpdateExternalGroup(externalGroup);
            } catch (AliasAlreadyExistsException e) {
                // Force service to generate an alias.
                externalGroup.setAlias(null);
                try {
                    existingGroupId = userGroupManagement
                            .createOrUpdateExternalGroup(externalGroup);
                } catch (AliasValidationException e1) {
                    throw new PluginException(e1.getMessage(), e1);
                } catch (AliasAlreadyExistsException e1) {
                    throw new PluginException(e1.getMessage(), e1);
                }
            } catch (AliasValidationException e) {
                throw new PluginException(e.getMessage(), e);
            }
            localGroupId = existingGroupId;
            updatedExternalGroups.put(externalGroup.getExternalId(), localGroupId);
        }
        return localGroupId;
    }

    /**
     * @param user
     *            The user.
     * @return List of ids of the users groups.
     */
    private Collection<Long> getLocalGroups(User user) {
        Collection<Group> localGroupsOfUser = ServiceLocator.findService(
                GroupDao.class).getGroupsOfUser(user.getId());
        Collection<Long> result = new ArrayList<Long>();
        for (Group group : localGroupsOfUser) {
            result.add(group.getId());
        }
        return result;
    }

    /**
     * This method removes the group from all parent groups it isn't a member anymore.
     *
     * @param externalUserGroup
     *            The external user group.
     * @param parentGroupIds
     *            Ids of the parent groups.
     */
    private void splitParentChildRelationShip(ExternalUserGroup externalUserGroup,
            HashSet<Long> parentGroupIds) {
        try {
            userGroupMemberManagement.removeGroupFromAllExternalGroups(externalUserGroup.getId(),
                    parentGroupIds);
        } catch (Exception e) {
            LOGGER.debug(e.getMessage(), e);
            LOGGER.warn("There was an error synchronizing an external group: {}",
                    e.getMessage());
        }
    }

    /**
     * Starts the synchronization.
     *
     * @param retriever
     *            The retriever to be used to retrieve users and groups.
     */
    public void synchronize(UserGroupRetriever retriever) {
        this.retriever = retriever;
        updatedExternalGroups.clear();
        updatedExternalUsers.clear();
        boolean upgradeToInternalSystem = false;
        if (!SecurityHelper.isInternalSystem()) {
            if (SecurityHelper.isClientManager()) {
                upgradeToInternalSystem = true;
            } else {
                LOGGER.error("The current user {} is not allowed to run this operation",
                        SecurityHelper.getCurrentUserId(), new AuthorizationException(
                                "Only client manager and internal system can use the synchronizer"));
                return;
            }
        }
        SecurityContext currentContext = null;
        try {
            if (upgradeToInternalSystem) {
                currentContext = AuthenticationHelper.setInternalSystemToSecurityContext();
            }
            retriever.start();
            synchronizeUsers();
            synchronizeGroups();
        } catch (SocketException e) {
            LOGGER.error("The synchronisation can't be finished, because of network problems.", e);
            retriever.stop(false);
        } finally {
            if (upgradeToInternalSystem) {
                AuthenticationHelper.setSecurityContext(currentContext);
            }
            retriever.stop(true);

        }
    }

    /**
     * Synchronizes a group. This covers removing a group that does not exist anymore and updating
     * the group data and the membership of the group in other groups.
     *
     * @param externalUserGroupId
     *            the ID of the external group to synchronize
     * @throws CommunicationException
     *             Thrown, when there was a communication problem with the foreign system.
     * @throws PluginException
     *             Thrown, when something internal went wrong.
     */
    private void synchronizeGroup(Long externalUserGroupId) throws CommunicationException,
            PluginException {
        ExternalUserGroup externalUserGroup = ServiceLocator
                .findService(ExternalUserGroupDao.class).load(
                        externalUserGroupId);
        if (externalUserGroup == null) {
            return;
        }
        LOGGER.debug("Synchronising group {} ({}) from {}", externalUserGroupId,
                externalUserGroup.getExternalId(), externalUserGroup.getExternalSystemId());
        // check for existence and sync group data if not yet done
        if (!updatedExternalGroups.containsKey(externalUserGroup.getExternalId())) {
            ExternalGroupVO externalGroupVO = accessor.getGroup(externalUserGroup);
            try {
                if (externalGroupVO == null) {
                    LOGGER.debug("Deleting group: {}", externalUserGroup.getAlias());
                    userGroupManagement.deleteExternalGroup(
                            externalUserGroup.getId(), externalUserGroup.getExternalSystemId());
                    return;
                }
                userGroupManagement.updateExternalGroup(externalUserGroupId, externalGroupVO);
            } catch (GroupNotFoundException e) {
                // since we are in the same session as the the load above this should not occur
                throw new PluginException("Unexpected Exception: " + e.getMessage(), e);
            } catch (AuthorizationException e) {
                // since the current user is the internal system this should not occur
                throw new PluginException("Unexpected Exception: " + e.getMessage(), e);
            }
            updatedExternalGroups.put(externalUserGroup.getExternalId(), externalUserGroup.getId());
        }
        // synchronize parent groups
        final HashSet<Long> parentGroupIds = new HashSet<Long>();
        try {
            accessor.acceptParentGroups(externalUserGroup,
                    new ExternalEntityVisitor<ExternalGroupVO>() {
                        @Override
                        public void visit(ExternalGroupVO entity) {
                            try {
                                Long localGroupId = getAndUpdateLocalGroup(entity);
                                parentGroupIds.add(localGroupId);
                            } catch (Exception e) {
                                LOGGER.debug(e.getMessage(), e);
                                LOGGER.warn("There was an error synchronizing the parent groups"
                                        + " of an external group: {}", e.getMessage());
                            }
                        }
                    });
            if (retriever.needsToAcceptMembersOfGroups()) {
                accessor.acceptMembersOfGroup(externalUserGroup, new ExternalEntityVisitor<Long>() {
                    @Override
                    public void visit(Long userId) throws Exception {
                        synchronizeUser(userId);
                    }
                });
            }
        } catch (ExternalUserGroupAccessorException e) {
            if (e.getCause() instanceof CommunicationException) {
                throw (CommunicationException) e.getCause();
            }
            LOGGER.debug(e.getMessage(), e);
            LOGGER.warn("There was an error synchronizing the parent groups"
                    + " of an external group: " + e.getMessage());
        }

        splitParentChildRelationShip(externalUserGroup, parentGroupIds);
        // Add group to parent
        for (Long parentGroupId : parentGroupIds) {
            try {
                userGroupMemberManagement.addGroup(parentGroupId, externalUserGroup.getId());
            } catch (Exception e) {
                LOGGER.debug(e.getMessage(), e);
                LOGGER.warn("There was an error synchronizing an external group: {}",
                        e.getMessage());
            }
        }
        LOGGER.debug("Finished synchronising group {}", externalUserGroupId);
    }

    /**
     * Synchronizes the user groups of the external system.
     *
     * @throws SocketException
     *             Thrown, when there was a network problem.
     */
    private void synchronizeGroups() throws SocketException {
        LOGGER.debug("Start synchronizing groups.");
        long startTime = System.currentTimeMillis();
        Collection<ExternalUserGroup> groups;
        while ((groups = retriever.getNextGroups()) != null && groups.size() > 0) {
            groups: for (final ExternalUserGroup externalGroup : groups) {
                if (externalGroup == null) {
                    LOGGER.warn("Retriever {} returned a 'null' external user group.", retriever
                            .getClass().getSimpleName());
                    continue groups;
                }
                RunInTransaction runInTransaction = new RunInTransaction() {
                    @Override
                    public void execute() throws TransactionException {
                        try {
                            if (externalGroup.getId() != null) {
                                synchronizeGroup(externalGroup.getId());
                            }
                        } catch (PluginException e) {
                            LOGGER.debug(e.getMessage(), e);
                            LOGGER.warn("There was an error synchronizing a group: {}"
                                    , e.getMessage());
                        }
                    }
                };
                try {
                    ServiceLocator.findService(TransactionManagement.class).execute(
                            runInTransaction);
                } catch (Exception e) {
                    if (e.getCause() instanceof SocketException) {
                        throw (SocketException) e.getCause();
                    }
                    LOGGER.error("There was an unknown error synchronizing a group ("
                            + externalGroup.getAlias() + "): {}", e.getMessage(), e);
                }
            }
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Finished synchronizing groups within about {} seconds.",
                    (System.currentTimeMillis() - startTime) / 1000);
        }
    }

    /**
     * @param userId
     *            The user to sync.
     */
    private void synchronizeUser(final Long userId) {
        if (userId == null || updatedExternalUsers.contains(userId)) {
            return;
        }
        updatedExternalUsers.add(userId);
        RunInTransaction clientManagerRunInTransaction = new RunInTransaction() {
            @Override
            public void execute() throws TransactionException {
                try {
                    final User user = ServiceLocator.findService(UserDao.class).load(
                            userId);
                    if (user == null) {
                        throw new UserNotFoundException("User with ID " + userId
                                + " does not exist.");
                    }
                    LOGGER.debug("Synchronizing group memberships of user {}", user.getAlias());
                    final Collection<Long> localGroupIdsOfUser = getLocalGroups(user);
                    try {
                        accessor.acceptGroupsOfUser(user,
                                new ExternalEntityVisitor<ExternalGroupVO>() {
                                    @Override
                                    public void visit(ExternalGroupVO entity) throws Exception {
                                        Long localGroupId = getAndUpdateLocalGroup(entity);
                                        userGroupMemberManagement.addUserForExternal(localGroupId,
                                                user.getId(), systemId);
                                        localGroupIdsOfUser.remove(localGroupId);
                                    }
                                });
                    } catch (ExternalUserGroupAccessorException e) {
                        throw new PluginException(e);
                    }
                    // Remove from groups the user isn't a member anymore.
                    for (Long localGroupId : localGroupIdsOfUser) {
                        userGroupMemberManagement.removeUserForExternal(localGroupId, user.getId(),
                                systemId);
                    }
                } catch (UserNotFoundException e) {
                    LOGGER.debug(e.getMessage(), e);
                    LOGGER.warn("Error syncing user with id: {} (User not found: {})",
                            userId, e.getMessage());
                } catch (AuthorizationException e) {
                    LOGGER.warn("Error syncing user with id: {} (Unexpected Exception: {})",
                            userId, e.getMessage());
                } catch (PluginException e) {
                    LOGGER.debug(e.getMessage(), e);
                    LOGGER.warn(
                            "Error syncing user with id: {} (Was not able to get remote groups for user: {})",
                            userId, e.getMessage());
                }
            }
        };
        try {
            ServiceLocator.findService(TransactionManagement.class).execute(
                    clientManagerRunInTransaction);
        } catch (Exception e) {
            LOGGER.error(
                    "There was an unknown error synchronizing a user: {}", e.getMessage(), e);
        }
    }

    /**
     * This synchronizes the users.
     */
    private void synchronizeUsers() {
        LOGGER.debug("Start synchronizing users.");
        long startTime = System.currentTimeMillis();
        Collection<User> users;
        while ((users = retriever.getNextUsers()) != null && users.size() > 0) {
            for (final User user : users) {
                if (!user.hasStatus(UserStatus.DELETED)
                        && !user.hasStatus(UserStatus.PERMANENTLY_DISABLED)) {
                    synchronizeUser(user.getId());
                }
            }
        }
        LOGGER.debug("Finished synchronizing users within about {} seconds.",
                (System.currentTimeMillis() - startTime) / 1000);
    }
}
