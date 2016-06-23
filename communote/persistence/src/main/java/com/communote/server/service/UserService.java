package com.communote.server.service;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContext;

import com.communote.common.converter.IdentityConverter;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.config.ClientConfigurationProperties;
import com.communote.server.api.core.config.type.ClientProperty;
import com.communote.server.api.core.property.StringPropertyTO;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.api.core.user.UserNotFoundException;
import com.communote.server.core.common.util.ExceptionHelper;
import com.communote.server.core.external.ExternalUserRepository;
import com.communote.server.core.security.AuthenticationHelper;
import com.communote.server.core.security.FieldUserIdentification;
import com.communote.server.core.security.SecurityHelper;
import com.communote.server.core.security.UserIdentification;
import com.communote.server.core.user.AliasAlreadyExistsException;
import com.communote.server.core.user.UserGroupManagement;
import com.communote.server.core.user.UserManagement;
import com.communote.server.core.user.group.AliasValidationException;
import com.communote.server.core.user.group.GroupNotFoundException;
import com.communote.server.core.vo.user.group.ExternalGroupVO;
import com.communote.server.model.config.ExternalSystemConfiguration;
import com.communote.server.model.user.ExternalUserAuthentication;
import com.communote.server.model.user.User;
import com.communote.server.model.user.UserProfileFields;
import com.communote.server.model.user.UserRole;
import com.communote.server.model.user.group.Group;
import com.communote.server.persistence.user.ExternalUserVO;
import com.communote.server.persistence.user.group.ExternalUserGroupDao;
import com.communote.server.plugins.exceptions.PluginException;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class UserService {

    /**
     * These flags allow to control the retrieval process of a user in the UserService.
     *
     * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
     *
     */
    public enum UserServiceRetrievalFlag {
        /**
         * This flag defines that a user should be created from an external repository (if not found
         * before). It should typically be set if a user is retrieved within a login process.
         */
        CREATE,
        /**
         * This flag defines that the user lookup will be forced on the configured external system.
         * The flag will be ignored if there is no external system configured. Use it during the
         * login process to synchronize the users properties.
         *
         * Remark: The flag will be ignored if there is no repository configured or if the user
         * identification is marked as system user
         */
        FORCE_EXTERNAL_REPO_CHECK;

        /**
         * Checks if the flag is contained in flags
         *
         * @param flags
         *            the flags
         * @param flag
         *            the flag
         * @return true if flag is in flags
         */
        public static boolean containsFlag(UserServiceRetrievalFlag[] flags,
                UserServiceRetrievalFlag flag) {
            if (flags != null) {
                for (int i = 0; i < flags.length; i++) {
                    if (flags[i] != null && flags[i].equals(flag)) {
                        return true;
                    }
                }
            }
            return false;
        }
    }

    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);
    @Autowired
    private UserManagement userManagement;
    @Autowired
    private UserGroupManagement userGroupManagement;

    @Autowired
    private ExternalUserGroupDao externalUserGroupDao;

    /**
     * Contains the default core repositories.
     */
    private Map<String, ExternalUserRepository> defaultRepositories;

    /**
     * Map comprises mapping of system id to user repository
     */
    private final Map<String, ExternalUserRepository> externalUserRepositories = new ConcurrentHashMap<String, ExternalUserRepository>();

    /**
     * Check if group could create automatically
     *
     * @return true or false
     */
    private boolean canCreateGroupAutomatically() {
        return CommunoteRuntime
                .getInstance()
                .getConfigurationManager()
                .getClientConfigurationProperties()
                .getProperty(ClientProperty.CREATE_EXTERNAL_GROUP_AUTOMATICALLY,
                        ClientProperty.DEFAULT_CREATE_EXTERNAL_GROUP_AUTOMATICALLY);
    }

    /**
     *
     * @return whether an internal user should be created automatically for an external user
     */
    private boolean canCreateUserAutomatically() {
        return CommunoteRuntime
                .getInstance()
                .getConfigurationManager()
                .getClientConfigurationProperties()
                .getProperty(ClientProperty.CREATE_EXTERNAL_USER_AUTOMATICALLY,
                        ClientProperty.DEFAULT_CREATE_EXTERNAL_USER_AUTOMATICALLY);
    }

    /**
     * Create and get group from an external group repository
     *
     * @param externalGroupId
     *            external group identifier in the external system
     * @param externalUserRepository
     *            repository where groups can be found
     * @return the group of internal database
     * @throws GroupNotFoundException
     *             in case the external group was not found in external system or internal database.
     */
    private Group createAndGetExternalGroup(String externalGroupId,
            ExternalUserRepository externalUserRepository) throws GroupNotFoundException {
        if (externalUserRepository != null
                && externalUserRepository.getExternalUserGroupAccessor() != null) {
            SecurityContext currentContext = AuthenticationHelper
                    .setInternalSystemToSecurityContext();
            try {
                ExternalGroupVO externalGroupVO = externalUserRepository
                        .getExternalUserGroupAccessor().getGroup(externalGroupId);

                if (externalGroupVO == null) {
                    throw new GroupNotFoundException("Group with external identifier "
                            + externalGroupId + " not found");
                }

                // don't init with null unless you check for null below
                Long groupId;

                try {
                    groupId = userGroupManagement.createOrUpdateExternalGroup(externalGroupVO);
                } catch (RuntimeException e) {
                    if (ExceptionHelper.isConstraintViolationException(e)) {
                        LOGGER.trace("Constraint violation exception, try again", e);
                        groupId = userGroupManagement.createOrUpdateExternalGroup(externalGroupVO);
                    } else {
                        throw e;
                    }
                }

                return userGroupManagement.findGroupById(groupId, new IdentityConverter<Group>());
            } catch (AliasAlreadyExistsException e) {
                throw new GroupNotFoundException("Group from external repository cannot be added "
                        + "because the alias of the external group " + externalGroupId
                        + " already exists");
            } catch (AliasValidationException e) {
                throw new GroupNotFoundException("Group from external repository cannot be added "
                        + "because the alias of the external group " + externalGroupId
                        + " is not valid");
            } catch (PluginException e) {
                throw new GroupNotFoundException(
                        "Retrieving group from external repository failed", e);
            } finally {
                AuthenticationHelper.setSecurityContext(currentContext);
            }

        } else {
            throw new GroupNotFoundException("external group " + externalGroupId
                    + " can not be found because repository does not exist");
        }

    }

    /**
     * Get user from an external system and create or update an internal user
     *
     * @param userIdentification
     *            the user identification to use for retrieving the user
     * @param flags
     *            flags to use to control the retrieval, can be null
     * @return existing or created user, never null
     * @throws UserNotFoundException
     *             in case no external system is set or user cannot be found, created or updated
     */
    private User createAndGetExternalUser(UserIdentification userIdentification,
            UserServiceRetrievalFlag... flags) throws UserNotFoundException {
        User user = null;

        ExternalUserRepository externalUserRepository = getExternalUserRepository(userIdentification
                .getExternalSystemId());
        // check if we have an external repo and if we are allowed to create the user
        if (externalUserRepository != null
                && (UserServiceRetrievalFlag.containsFlag(flags, UserServiceRetrievalFlag.CREATE) || canCreateUserAutomatically())) {

            // set the internal system to security context
            SecurityContext currentContext = AuthenticationHelper
                    .setInternalSystemToSecurityContext();
            try {

                ExternalUserVO externalUserVO = externalUserRepository.getUser(userIdentification);

                if (externalUserVO != null) {

                    try {
                        user = userManagement.createOrUpdateExternalUser(externalUserVO);
                    } catch (Exception e) {
                        if (ExceptionHelper.isConstraintViolationException(e)) {
                            user = userManagement.createOrUpdateExternalUser(externalUserVO);
                        } else {
                            throw e;
                        }
                    }

                }

            } catch (Exception e) {
                if (!(e instanceof UserNotFoundException)) {
                    LOGGER.debug(e.getMessage(), e);
                    throw new UserNotFoundException("external user " + userIdentification
                            + " cannot be created from external repository into internal database",
                            e);
                }

            } finally {
                // set it if it is null (which will be a remove)
                AuthenticationHelper.setSecurityContext(currentContext);
            }
        }

        if (user == null) {

            throw new UserNotFoundException("user " + userIdentification
                    + " can not be found in external system: "
                    + userIdentification.getExternalSystemId());
        }
        return user;

    }

    /**
     * Returns all active user repositories.
     *
     * @return List of user repositories.
     */
    public Collection<ExternalUserRepository> getActiveUserRepositories() {
        Set<ExternalUserRepository> repositories = new HashSet<ExternalUserRepository>();
        for (ExternalUserRepository repository : defaultRepositories.values()) {
            if (repository.isActive()) {
                repositories.add(repository);
            }
        }
        for (ExternalUserRepository repository : externalUserRepositories.values()) {
            if (repository.isActive()) {
                repositories.add(repository);
            }
        }
        return repositories;
    }

    /**
     * Get the available repository for the given system id. Available means it is not necessarily
     * an active and properly configured repository.
     *
     * @param systemId
     *            the system id to get the repository of
     * @return the external user repository, can be null
     */
    public ExternalUserRepository getAvailableUserRepository(String systemId) {
        if (systemId == null) {
            return null;
        }
        return this.externalUserRepositories.get(systemId);
    }

    /**
     * External Repos can return additional properties that will be included e.g. in the rest
     * resource
     *
     * @param userId
     *            the id of the user the authentication is for
     * @param externalUserAuthentication
     *            the authentication to get additional properties for
     * @throws AuthorizationException
     */
    public Collection<StringPropertyTO> getExternalLoginProperties(Long userId,
            ExternalUserAuthentication externalUserAuthentication) throws AuthorizationException {

        ExternalUserRepository externalUserRepository = this
                .getAvailableUserRepository(externalUserAuthentication.getSystemId());
        if (externalUserRepository == null) {
            return Collections.emptySet();
        }
        return externalUserRepository
                .getExternalLoginProperties(userId, externalUserAuthentication);

    }

    public ExternalSystemConfiguration getExternalSystemConfiguration(String externalSystemId) {
        ExternalUserRepository userRepository = this.externalUserRepositories.get(externalSystemId);
        ExternalSystemConfiguration config = userRepository == null ? null : userRepository
                .getConfiguration();

        return config;

    }

    /**
     * Get the external user repository based on the repository mode. In "strict" mode always the
     * primary repository will be returned. If the requested repository is not active null is
     * returned.
     *
     * @param externalSystemId
     *            identifier of the external system. Can be null to get the primary repository.
     * @return the repository or null if there is no matching repository or the repository is not
     *         active
     * @see ClientProperty.REPOSITORY_MODE
     */
    public ExternalUserRepository getExternalUserRepository(String externalSystemId) {
        if (externalSystemId == null) {
            return getPrimaryExternalUserRepository();
        }
        ExternalUserRepository repo = null;
        ClientProperty.REPOSITORY_MODE repoMode = CommunoteRuntime.getInstance()
                .getConfigurationManager().getClientConfigurationProperties().getRepositoryMode();
        switch (repoMode) {
        case STRICT:
            repo = getPrimaryExternalUserRepository();
            break;
        case FLEXIBLE:
            repo = externalUserRepositories.get(externalSystemId);
            if (repo != null && !repo.isActive()) {
                repo = null;
            }
            break;
        default:
            throw new IllegalArgumentException("RepositoryMode " + repoMode + " is not valid.");
        }
        return repo;

    }

    /**
     * Get a group by its alias. The group will be searched in the local database. If not found, the
     * primary repository will be queried.
     *
     * @param alias
     *            the alias of the group
     * @return the found group
     * @throws GroupNotFoundException
     *             in case the group was not found
     */
    public Group getGroup(String alias) throws GroupNotFoundException {
        Group group = userGroupManagement.findGroupByAlias(alias, new IdentityConverter<Group>());
        if (group == null) {
            group = getGroup(alias, null);
        }
        return group;
    }

    /**
     * Get group specific from external system
     *
     * @param externalGroupId
     *            group identifier of the external system
     * @param externalSystemId
     *            identifier of the external system
     * @return {@link Group} the found group, never null
     * @throws GroupNotFoundException
     *             in case the group can not be found
     */
    public Group getGroup(String externalGroupId, String externalSystemId)
            throws GroupNotFoundException {

        // TODO consider following case: externalSystemId is null: next call returns null, but we
        // might have a primary repo and the group can be already in DB for the externalSystemId of
        // that repo. in case canCreateAutomatically is false the group won't be found. Strange
        // behavior!

        // ask for the primary repo
        ExternalUserRepository repo = getExternalUserRepository(externalSystemId);
        if (repo != null) {
            externalSystemId = repo.getExternalSystemId();
        }

        // if still null => fail
        if (externalSystemId == null) {
            throw new GroupNotFoundException(
                    "Group "
                            + externalGroupId
                            + " does not exist or cannot be created since the externalSystemId cannot be determined. ");
        }

        Group userGroup = externalUserGroupDao.findByExternalId(externalGroupId, externalSystemId);

        if (userGroup == null && canCreateGroupAutomatically()) {

            userGroup = createAndGetExternalGroup(externalGroupId,
                    getExternalUserRepository(externalSystemId));
        }

        if (userGroup == null) {
            throw new GroupNotFoundException("Group " + externalGroupId + " for externalSystemId "
                    + externalSystemId + " does not exist");
        }
        return userGroup;
    }

    /**
     * Get the names of the user profile fields that cannot be changed by the user. The immutable
     * profile fields depend on the external authentication the user has and the external systems
     * that are active. If a primary external system is defined only this system is used to
     * determine the immutable fields.
     *
     * @param userId
     *            the id of user to get the fixed fields for
     * @return the fields
     */
    public Collection<UserProfileFields> getImmutableProfileFieldsOfUser(Long userId) {
        User user = ServiceLocator.instance().getService(UserManagement.class)
                .findUserByUserId(SecurityHelper.assertCurrentUserId(), true);
        return getImmutableProfileFieldsOfUser(user);
    }

    /**
     * Get the names of the user profile fields that cannot be changed by the user. The immutable
     * profile fields depend on the external authentication the user has and the external systems
     * that are active. If a primary external system is defined only this system is used to
     * determine the immutable fields.
     *
     * @param user
     *            the user to get the immutable fields for. The external auths must be loaded for
     *            this user.
     * @return the fields
     */
    private Collection<UserProfileFields> getImmutableProfileFieldsOfUser(User user) {
        ClientConfigurationProperties clientConfigurationProperties = CommunoteRuntime
                .getInstance().getConfigurationManager().getClientConfigurationProperties();

        Collection<UserProfileFields> immutableFields = new HashSet<>();

        // only add fields of primary if strict, otherwise add fields of all authentications
        boolean requirePrimary = ClientProperty.REPOSITORY_MODE.STRICT
                .equals(clientConfigurationProperties.getRepositoryMode());
        String primaryAuth = clientConfigurationProperties.getPrimaryExternalAuthentication();
        // go through the authentications of the user
        for (ExternalUserAuthentication authentication : user.getExternalAuthentications()) {

            if (requirePrimary && !authentication.getSystemId().equals(primaryAuth)) {
                // not the primary one but primary external system exists? => ignore this
                // authentication
                continue;
            }
            ExternalUserRepository userRepository = this.externalUserRepositories
                    .get(authentication.getSystemId());
            if (userRepository != null) {
                // get the configuration
                ExternalSystemConfiguration externalSystemConfiguration = userRepository
                        .getConfiguration();

                // external system for this authentication is activated
                if (externalSystemConfiguration != null
                        && externalSystemConfiguration.isAllowExternalAuthentication()) {
                    immutableFields.addAll(userRepository.getProvidedProfileFieldNames());
                }
            }
        }
        return immutableFields;
    }

    /**
     * @return the primary external user repository that is active
     */
    private ExternalUserRepository getPrimaryExternalUserRepository() {
        String primaryRepository = CommunoteRuntime.getInstance().getConfigurationManager()
                .getClientConfigurationProperties().getPrimaryExternalAuthentication();

        // concurrent hash map cannot handle null keys
        if (primaryRepository != null) {
            ExternalUserRepository primary = externalUserRepositories.get(primaryRepository);
            if (primary != null && primary.isActive()) {
                return primary;
            }
        }
        return null;
    }

    public Collection<ExternalUserRepository> getRegistedExternalUserRepositories() {
        Set<ExternalUserRepository> repositories = new HashSet<ExternalUserRepository>();
        for (ExternalUserRepository repository : defaultRepositories.values()) {
            repositories.add(repository);
        }
        for (ExternalUserRepository repository : externalUserRepositories.values()) {
            repositories.add(repository);
        }
        return repositories;
    }

    /**
     * Get a user by its alias. This will first look in the internal database for a matching user.
     * If there is no matching use but an external system is configured and it is allowed to
     * automatically create users from external systems, it will be tried to find and create the
     * user with the details provided by the external system.
     *
     * @param alias
     *            the alias of the user
     * @return the existing user
     * @throws UserNotFoundException
     *             in case there is no matching user or a matching external user cannot be created
     */
    public User getUser(String alias) throws UserNotFoundException {
        User user = userManagement.findUserByAlias(alias);
        if (user == null) {
            user = createAndGetExternalUser(new FieldUserIdentification().setUserAlias(alias));
        }
        return user;
    }

    /**
     * Get a user that originates from a specific external system. If there isn't a matching user
     * but the provided external system is activated and it is allowed to automatically create users
     * from external systems, it will be tried to find and create the user with the details provided
     * by the external system.
     *
     * @param externalUserId
     *            identifier of the user in the external system
     * @param externalSystemId
     *            identifier of the external system
     * @return the existing user
     * @throws UserNotFoundException
     *             in case there is no matching user or a matching external user cannot be created
     */
    public User getUser(String externalUserId, String externalSystemId)
            throws UserNotFoundException {
        User user = userManagement.findUserByExternalUserId(externalUserId, externalSystemId);
        if (user == null) {
            user = createAndGetExternalUser(new FieldUserIdentification().setExternalSystemId(
                    externalSystemId).setExternalUserId(externalUserId));
        }
        return user;
    }

    /**
     * Get a user specified by the given {@link UserIdentification}. Depending on configuration and
     * retrieval flags this can lead to a lookup of the user in an external repository.
     *
     * @param userIdentification
     *            identification of the user
     * @param flags
     *            optional flags to configure how the user should be retrieved
     * @return the user
     * @throws UserNotFoundException
     *             in case the user does not exist or in case the user cannot be created or updated
     *             after a successful lookup in an external repository
     */
    public User getUser(UserIdentification userIdentification, UserServiceRetrievalFlag... flags)
            throws UserNotFoundException {
        User user = null;
        ExternalUserRepository repo = getExternalUserRepository(userIdentification
                .getExternalSystemId());

        // don't go for database if the flag is set, unless there is no active external repo or we
        // are dealing with a system user
        if (repo == null
                || Boolean.TRUE.equals(userIdentification.getIsSystemUser())
                || !UserServiceRetrievalFlag.containsFlag(flags,
                        UserServiceRetrievalFlag.FORCE_EXTERNAL_REPO_CHECK)) {
            user = userManagement.findUser(userIdentification);
        }
        if (user == null) {
            user = createAndGetExternalUser(userIdentification, flags);
        }
        if (user == null) {
            throw new UserNotFoundException("Error getting user by userIdentification="
                    + userIdentification);
        }
        return user;
    }

    /**
     * Return whether the provided user has the given role.
     *
     * @param userId
     *            the ID of the user to test
     * @param role
     *            the role to test for
     * @return true if the user exists and has the role, false otherwise
     */
    public boolean hasRole(Long userId, UserRole role) {
        if (role != null) {
            UserRole[] roles = userManagement.getRolesOfUser(userId);
            if (roles == null) {
                return false;
            }
            for (UserRole assignedRole : roles) {
                if (role.equals(assignedRole)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isUserRepositoryActive(String externalSystemId) {
        for (ExternalUserRepository userRepo : this.getActiveUserRepositories()) {
            if (StringUtils.equals(externalSystemId, userRepo.getExternalSystemId())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Register an external user repository. If there is already a repository for the
     * externalSystemId it will be replaced.
     *
     * @param externalSystemId
     *            identifier of the external repository (LDAP, Confluence, ...)
     * @param externalUserRepository
     *            the repository to register
     */
    public synchronized void registerRepository(String externalSystemId,
            ExternalUserRepository externalUserRepository) {
        if (externalSystemId == null) {
            throw new IllegalArgumentException("externalSystemId cannot be null.");
        }
        if (externalUserRepository == null) {
            throw new IllegalArgumentException("externalUserRepository cannot be null.");
        }
        externalUserRepositories.put(externalSystemId, externalUserRepository);
    }

    /**
     * Set the default repositories
     *
     * @param defaultRepositories
     *            map of repositories
     */
    public synchronized void setDefaultRepositories(
            Map<String, ExternalUserRepository> defaultRepositories) {
        if (this.defaultRepositories != null) {
            throw new IllegalStateException("default repositories exist");
        } else {
            this.defaultRepositories = defaultRepositories;
            for (String externalRepositorySystemId : defaultRepositories.keySet()) {
                registerRepository(externalRepositorySystemId,
                        defaultRepositories.get(externalRepositorySystemId));
            }
        }
    }

    /**
     * Unregister an external user repository if is not an default repository
     *
     * @param externalSystemId
     *            of the external repository (LDAP, Confluence, Sharepoint, ...)
     */
    public synchronized void unregisterRepository(String externalSystemId) {
        // TODO inconsistent: repo with ID of default repo can be added but not removed!
        if (defaultRepositories.get(externalSystemId) == null) {
            externalUserRepositories.remove(externalSystemId);
        }
    }
}
