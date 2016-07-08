package com.communote.server.core.user;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.communote.common.converter.IdentityConverter;
import com.communote.server.api.core.blog.BlogAccessException;
import com.communote.server.api.core.common.EmailValidationException;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.api.core.user.UserNotFoundException;
import com.communote.server.api.core.user.UserVO;
import com.communote.server.core.common.exceptions.InvalidOperationException;
import com.communote.server.core.user.validation.UserActivationValidationException;
import com.communote.server.model.user.User;
import com.communote.server.model.user.UserRole;
import com.communote.server.model.user.UserStatus;
import com.communote.server.persistence.user.ExternalUserVO;

/**
 * <p>
 * Spring Service base class for <code>com.communote.server.service.user.UserManagement</code>,
 * provides access to all services and entities referenced by this service.
 * </p>
 *
 * @see UserManagement
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
@Transactional(propagation = Propagation.REQUIRED)
public abstract class UserManagementBase implements UserManagement {
    @Override
    public void acceptTermsOfUse(Long userId) throws UserNotFoundException, AuthorizationException,
            UserActivationValidationException {
        if (userId == null) {
            throw new IllegalArgumentException("'userId' cannot be null");
        }
        try {
            this.handleAcceptTermsOfUse(userId);
        } catch (RuntimeException rt) {
            throw new UserManagementException(
                    "Error performing 'com.communote.server.service.user.UserManagement.acceptTermsOfUse(Long userId)' --> "
                            + rt, rt);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void anonymizeUser(Long userId, Long[] blogIds, boolean becomeManager)
            throws com.communote.server.api.core.security.AuthorizationException,
            NoClientManagerLeftException, UserDeletionDisabledException,
            com.communote.server.api.core.blog.NoBlogManagerLeftException {
        if (userId == null) {
            throw new IllegalArgumentException(
                    "UserManagement.anonymizeUser(Long userId, Long[] blogIds, boolean becomeManager) - 'userId' can not be null");
        }
        this.handleAnonymizeUser(userId, blogIds, becomeManager);
    }

    @Override
    public User assignUserRole(Long userId, UserRole role) throws AuthorizationException,
            InvalidOperationException {
        if (userId == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.service.user.UserManagement.assignUserRole(Long userId, UserRole role) - 'userId' can not be null");
        }
        if (role == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.service.user.UserManagement.assignUserRole(Long userId, UserRole role) - 'role' can not be null");
        }
        try {
            return this.handleAssignUserRole(userId, role);
        } catch (RuntimeException rt) {
            throw new UserManagementException(
                    "Error performing 'com.communote.server.service.user.UserManagement.assignUserRole(Long userId, UserRole role)' --> "
                            + rt, rt);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean changeEmailAddress(Long userId, String newEmail, boolean sendConfirmationLink)
            throws com.communote.server.api.core.common.EmailValidationException,
            EmailAlreadyExistsException {
        if (userId == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.service.user.UserManagement.changeEmailAddress(Long userId, String newEmail) - 'userId' can not be null");
        }
        if (newEmail == null || newEmail.trim().length() == 0) {
            throw new IllegalArgumentException(
                    "com.communote.server.service.user.UserManagement.changeEmailAddress(Long userId, String newEmail) - 'newEmail' can not be null or empty");
        }
        try {
            return this.handleChangeEmailAddress(userId, newEmail, sendConfirmationLink);
        } catch (RuntimeException rt) {
            throw new UserManagementException(
                    "Error performing 'com.communote.server.service.user.UserManagement.changeEmailAddress(Long userId, String newEmail)' --> "
                            + rt, rt);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void changePassword(Long userId, String newPassword)
            throws com.communote.server.core.common.exceptions.PasswordLengthException {
        if (userId == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.service.user.UserManagement.changePassword(Long userId, String newPassword) - 'userId' can not be null");
        }
        if (newPassword == null || newPassword.trim().length() == 0) {
            throw new IllegalArgumentException(
                    "com.communote.server.service.user.UserManagement.changePassword(Long userId, String newPassword) - 'newPassword' can not be null or empty");
        }
        try {
            this.handleChangePassword(userId, newPassword);
        } catch (RuntimeException rt) {
            throw new UserManagementException(
                    "Error performing 'com.communote.server.service.user.UserManagement.changePassword(Long userId, String newPassword)' --> "
                            + rt, rt);
        }
    }

    @Override
    public void changeUserStatusByManager(Long userId, UserStatus newStatus)
            throws AuthorizationException, UserNotFoundException,
            InvalidUserStatusTransitionException, NoClientManagerLeftException,
            UserActivationValidationException {
        try {
            handleChangeUserStatusByManager(userId, newStatus);
        } catch (RuntimeException rt) {
            throw new UserManagementException("Unexpected exception changing user status", rt);
        }

    }

    @Override
    public void confirmNewEmailAddress(String securityCode)
            throws com.communote.server.persistence.common.security.SecurityCodeNotFoundException,
            EmailAlreadyExistsException {
        if (securityCode == null || securityCode.trim().length() == 0) {
            throw new IllegalArgumentException(
                    "com.communote.server.service.user.UserManagement.confirmNewEmailAddress(String securityCode) - 'securityCode' can not be null or empty");
        }
        try {
            this.handleConfirmNewEmailAddress(securityCode);
        } catch (RuntimeException rt) {
            throw new UserManagementException(
                    "Error performing 'com.communote.server.service.user.UserManagement.confirmNewEmailAddress(String securityCode)' --> "
                            + rt, rt);
        }
    }

    @Override
    public User confirmUser(String securitycode, UserVO user)
            throws com.communote.server.persistence.common.security.SecurityCodeNotFoundException,
            com.communote.server.api.core.common.EmailValidationException,
            EmailAlreadyExistsException, AliasAlreadyExistsException,
            com.communote.server.core.common.exceptions.PasswordLengthException,
            InvalidUserStatusTransitionException {
        if (securitycode == null || securitycode.trim().length() == 0) {
            throw new IllegalArgumentException(
                    "com.communote.server.service.user.UserManagement.confirmUser(String securitycode, UserVO user) - 'securitycode' can not be null or empty");
        }
        if (user == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.service.user.UserManagement.confirmUser(String securitycode, UserVO user) - 'user' can not be null");
        }
        if (user.getLanguage() == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.service.user.UserManagement.confirmUser(String securitycode, UserVO user) - 'user.language' can not be null");
        }
        if (user.getEmail() == null || user.getEmail().trim().length() == 0) {
            throw new IllegalArgumentException(
                    "com.communote.server.service.user.UserManagement.confirmUser(String securitycode, UserVO user) - 'user.email' can not be null or empty");
        }
        if (user.getRoles() == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.service.user.UserManagement.confirmUser(String securitycode, UserVO user) - 'user.roles' can not be null");
        }
        try {
            return this.handleConfirmUser(securitycode, user);
        } catch (RuntimeException rt) {
            throw new UserManagementException(
                    "Error performing 'com.communote.server.service.user.UserManagement.confirmUser(String securitycode, UserVO user)' --> "
                            + rt, rt);
        }
    }

    @Override
    public User createOrUpdateExternalUser(ExternalUserVO userVO)
            throws com.communote.server.api.core.common.EmailValidationException,
            EmailAlreadyExistsException, AliasAlreadyExistsException,
            InvalidUserStatusTransitionException, UserActivationValidationException,
            NoClientManagerLeftException, PermanentIdMissmatchException {
        if (userVO == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.service.user.UserManagement.createOrUpdateExternalUser(ExternalUserVO userVO) - 'userVO' can not be null");
        }
        if (userVO.getExternalUserName() == null
                || userVO.getExternalUserName().trim().length() == 0) {
            throw new IllegalArgumentException(
                    "com.communote.server.service.user.UserManagement.createOrUpdateExternalUser(ExternalUserVO userVO) - 'userVO.externalUserName' can not be null or empty");
        }
        if (userVO.getSystemId() == null || userVO.getSystemId().trim().length() == 0) {
            throw new IllegalArgumentException(
                    "com.communote.server.service.user.UserManagement.createOrUpdateExternalUser(ExternalUserVO userVO) - 'userVO.systemId' can not be null or empty");
        }
        try {
            return this.handleCreateOrUpdateExternalUser(userVO);
        } catch (RuntimeException rt) {
            throw new UserManagementException(
                    "Error performing 'com.communote.server.service.user.UserManagement.createOrUpdateExternalUser(ExternalUserVO userVO)' --> "
                            + rt, rt);
        }
    }

    @Override
    public User createUser(UserVO user, boolean emailConfirmation, boolean managerConfirmation)
            throws EmailAlreadyExistsException,
            com.communote.server.api.core.common.EmailValidationException,
            AliasAlreadyExistsException,
            com.communote.server.core.common.exceptions.PasswordLengthException {
        if (user == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.service.user.UserManagement.createKenmeiUser(UserVO user, boolean emailConfirmation, boolean managerConfirmation) - 'user' can not be null");
        }
        if (user.getLanguage() == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.service.user.UserManagement.createKenmeiUser(UserVO user, boolean emailConfirmation, boolean managerConfirmation) - 'user.language' can not be null");
        }
        if (user.getEmail() == null || user.getEmail().trim().length() == 0) {
            throw new IllegalArgumentException(
                    "com.communote.server.service.user.UserManagement.createKenmeiUser(UserVO user, boolean emailConfirmation, boolean managerConfirmation) - 'user.email' can not be null or empty");
        }
        if (user.getRoles() == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.service.user.UserManagement.createKenmeiUser(UserVO user, boolean emailConfirmation, boolean managerConfirmation) - 'user.roles' can not be null");
        }
        try {
            return this.handleCreateUser(user, emailConfirmation, managerConfirmation);
        } catch (RuntimeException rt) {
            throw new UserManagementException(
                    "Error performing 'com.communote.server.service.user.UserManagement.createKenmeiUser(UserVO user, boolean emailConfirmation, boolean managerConfirmation)' --> "
                            + rt, rt);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public User findUserByAlias(String alias) {
        if (alias == null || alias.trim().length() == 0) {
            throw new IllegalArgumentException(
                    "com.communote.server.service.user.UserManagement.findKenmeiUserByAlias(String alias) - 'alias' can not be null or empty");
        }
        try {
            return this.handleFindUserByAlias(alias);
        } catch (RuntimeException rt) {
            throw new UserManagementException(
                    "Error performing 'com.communote.server.service.user.UserManagement.findKenmeiUserByAlias(String alias)' --> "
                            + rt, rt);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public User findUserByEmail(String email) {
        if (email == null || email.trim().length() == 0) {
            throw new IllegalArgumentException(
                    "com.communote.server.service.user.UserManagement.findKenmeiUserByEmail(String email) - 'email' can not be null or empty");
        }
        try {
            return this.handleFindUserByEmail(email);
        } catch (RuntimeException rt) {
            throw new UserManagementException(
                    "Error performing 'com.communote.server.service.user.UserManagement.findKenmeiUserByEmail(String email)' --> "
                            + rt, rt);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public User findUserByEmailAlias(String username) {
        if (username == null || username.trim().length() == 0) {
            throw new IllegalArgumentException(
                    "com.communote.server.service.user.UserManagement.findKenmeiUserByEmailAlias(String username) - 'username' can not be null or empty");
        }
        try {
            return this.handleFindUserByEmailAlias(username);
        } catch (RuntimeException rt) {
            throw new UserManagementException(
                    "Error performing 'com.communote.server.service.user.UserManagement.findKenmeiUserByEmailAlias(String username)' --> "
                            + rt, rt);
        }
    }

    /**
     * @see UserManagement#findUserByExternalUserId(String, String)
     */
    @Override
    @Transactional(readOnly = true)
    public User findUserByExternalUserId(String externalUserId, String systemId) {
        if (externalUserId == null || externalUserId.trim().length() == 0) {
            throw new IllegalArgumentException(
                    "com.communote.server.service.user.UserManagement.findKenmeiUserByExternalUserId(String externalUserId, String systemId) - 'externalUserId' can not be null or empty");
        }
        if (systemId == null || systemId.trim().length() == 0) {
            throw new IllegalArgumentException(
                    "com.communote.server.service.user.UserManagement.findKenmeiUserByExternalUserId(String externalUserId, String systemId) - 'systemId' can not be null or empty");
        }
        try {
            return this.handleFindUserByExternalUserId(externalUserId, systemId);
        } catch (RuntimeException rt) {
            throw new UserManagementException(
                    "Error performing 'com.communote.server.service.user.UserManagement.findKenmeiUserByExternalUserId(String externalUserId, String systemId)' --> "
                            + rt, rt);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public User findUserByUserId(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException(
                    "findKenmeiUserByUserId(Long userId) - 'userId' can not be null");
        }
        try {
            return this.getUserById(userId, new IdentityConverter<User>());
        } catch (RuntimeException rt) {
            throw new UserManagementException(
                    "Error performing 'com.communote.server.service.user.UserManagement.findKenmeiUserByUserId(Long userId)' --> "
                            + rt, rt);
        }
    }

    /**
     * @see UserManagement#getActiveUserCount()
     */
    @Override
    @Transactional(readOnly = true)
    public long getActiveUserCount() {
        try {
            return this.handleGetActiveUserCount();
        } catch (RuntimeException rt) {
            throw new UserManagementException(
                    "Error performing 'com.communote.server.service.user.UserManagement.getActiveUserCount()' --> "
                            + rt, rt);
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
     * @see UserManagement#getRolesOfUser(Long)
     */
    @Override
    @Transactional(readOnly = true)
    public UserRole[] getRolesOfUser(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("com.communote.server.service.user"
                    + ".UserManagement.getRolesOfUser(Long userId) - 'userId' can not be null");
        }
        try {
            return this.handleGetRolesOfUser(userId);
        } catch (RuntimeException rt) {
            throw new UserManagementException(
                    "Error performing 'com.communote.server.service.user."
                            + "UserManagement.getRolesOfUser(Long userId)' --> " + rt, rt);
        }
    }

    protected abstract void handleAcceptTermsOfUse(Long userId) throws UserNotFoundException,
    AuthorizationException, UserActivationValidationException;

    /**
     * Performs the core logic for {@link #anonymizeUser(Long, Long[], boolean)}
     */
    protected abstract void handleAnonymizeUser(Long userId, Long[] blogIds, boolean becomeManager)
            throws com.communote.server.api.core.security.AuthorizationException,
            NoClientManagerLeftException, UserDeletionDisabledException,
            com.communote.server.api.core.blog.NoBlogManagerLeftException;

    /**
     * Performs the core logic for {@link #assignUserRole(Long, UserRole)}
     *
     * @throws AuthorizationException
     *             in case the current user is not a client manager
     * @throws InvalidOperationException
     *             in case the user is already a system or a crawl user or the role to assign is the
     *             system or crawl user role
     */
    protected abstract User handleAssignUserRole(Long userId, UserRole role)
            throws AuthorizationException, InvalidOperationException;

    /**
     * Performs the core logic for {@link #changeEmailAddress(Long, String)}
     *
     * @param userId
     *            Id of the user, which email should be changed.
     * @param newEmail
     *            The new email address.
     * @param sendConfirmationLink
     *            If true an confirmation link will be send to the user to confirm the new address.
     *            False is only possible, if the current use an administrator.
     * @return True, when the address was changed.
     */
    protected abstract boolean handleChangeEmailAddress(Long userId, String newEmail,
            boolean sendConfirmationLink)
                    throws com.communote.server.api.core.common.EmailValidationException,
                    EmailAlreadyExistsException;

    /**
     * Performs the core logic for {@link #changePassword(Long, String)}
     */
    protected abstract void handleChangePassword(Long userId, String newPassword)
            throws com.communote.server.core.common.exceptions.PasswordLengthException;

    protected abstract void handleChangeUserStatusByManager(Long userId, UserStatus newStatus)
            throws UserActivationValidationException, InvalidUserStatusTransitionException,
            UserNotFoundException, NoClientManagerLeftException;

    /**
     * Performs the core logic for {@link #confirmNewEmailAddress(String)}
     */
    protected abstract void handleConfirmNewEmailAddress(String securityCode)
            throws com.communote.server.persistence.common.security.SecurityCodeNotFoundException,
            EmailAlreadyExistsException;

    /**
     * Performs the core logic for {@link #confirmUser(String, UserVO)}
     */
    protected abstract User handleConfirmUser(String securitycode, UserVO user)
            throws com.communote.server.persistence.common.security.SecurityCodeNotFoundException,
            com.communote.server.api.core.common.EmailValidationException,
            EmailAlreadyExistsException, AliasAlreadyExistsException,
            com.communote.server.core.common.exceptions.PasswordLengthException,
            InvalidUserStatusTransitionException;

    /**
     * Performs the core logic for {@link #createOrUpdateExternalUser(ExternalUserVO)}
     */
    protected abstract User handleCreateOrUpdateExternalUser(ExternalUserVO userVO)
            throws com.communote.server.api.core.common.EmailValidationException,
            EmailAlreadyExistsException, AliasAlreadyExistsException,
            InvalidUserStatusTransitionException, UserActivationValidationException,
            NoClientManagerLeftException, PermanentIdMissmatchException;

    /**
     * Performs the core logic for {@link #createUser(UserVO, boolean, boolean)}
     */
    protected abstract User handleCreateUser(UserVO user, boolean emailConfirmation,
            boolean managerConfirmation) throws EmailAlreadyExistsException,
            com.communote.server.api.core.common.EmailValidationException,
            AliasAlreadyExistsException,
            com.communote.server.core.common.exceptions.PasswordLengthException;

    /**
     * Performs the core logic for {@link #findUserByAlias(String)}
     */
    protected abstract User handleFindUserByAlias(String alias);

    /**
     * Performs the core logic for {@link #findUserByEmail(String)}
     */
    protected abstract User handleFindUserByEmail(String email);

    /**
     * Performs the core logic for {@link #findKenmeiUserByEmailAlias(String)}
     */
    protected abstract User handleFindUserByEmailAlias(String username);

    /**
     * Performs the core logic for {@link #findUserByExternalUserId(String, String)}
     */
    protected abstract User handleFindUserByExternalUserId(String externalUserId, String systemId);

    /**
     * Performs the core logic for {@link #getActiveUserCount()}
     */
    protected abstract long handleGetActiveUserCount();

    /**
     * Performs the core logic for {@link #getRolesOfUser(Long)}
     */
    protected abstract UserRole[] handleGetRolesOfUser(Long userId);

    /**
     * Performs the core logic for
     * {@link #inviteUserToBlog(Long, User, UserVO, com.communote.server.model.blog.BlogRole)}
     *
     * @throws BlogAccessException
     *             in case the current user is not allowed to add the user
     * @throws AuthorizationException
     *             in case there is no current user
     */
    protected abstract User handleInviteUserToBlog(Long blogId, UserVO userData,
            com.communote.server.model.blog.BlogRole role) throws AliasAlreadyExistsException,
            com.communote.server.api.core.common.EmailValidationException,
            EmailAlreadyExistsException, PermanentIdMissmatchException, BlogAccessException,
            AuthorizationException;

    /**
     * Performs the core logic for {@link #inviteUserToClient(User, UserVO)}
     */
    protected abstract User handleInviteUserToClient(UserVO user)
            throws com.communote.server.api.core.common.EmailValidationException,
            EmailAlreadyExistsException, AliasAlreadyExistsException,
            PermanentIdMissmatchException, AuthorizationException;

    /**
     * Performs the core logic for {@link #permanentlyDisableUser(Long, Long[], boolean)}
     */
    protected abstract void handlePermanentlyDisableUser(Long userId, Long[] blogIds,
            boolean becomeManager)
                    throws com.communote.server.api.core.security.AuthorizationException,
                    NoClientManagerLeftException, UserDeletionDisabledException,
                    InvalidUserStatusTransitionException,
                    com.communote.server.api.core.blog.NoBlogManagerLeftException;

    /**
     * Performs the core logic for {@link #registerUser(String, java.util.Locale)}
     *
     * @param type
     */
    protected abstract User handleRegisterUser(String email, java.util.Locale locale,
            RegistrationType type) throws EmailValidationException, EmailAlreadyExistsException;

    /**
     * Performs the core logic for {@link #removeUserRole(Long, UserRole)}
     *
     * @throws InvalidOperationException
     *             in case the user is a system or crawl user and the system or crawl user role
     *             should be removed
     * @throws AuthorizationException
     *             in case the current user is not client manager
     * @throws NoClientManagerLeftException
     *             in case the client manager role should be removed from the last user with that
     *             role
     */
    protected abstract User handleRemoveUserRole(Long userId, UserRole role)
            throws AuthorizationException, InvalidOperationException, NoClientManagerLeftException;

    /**
     * Performs the core logic for {@link #resetExternalUserId(String)}
     */
    protected abstract void handleResetPermanentId(String systemId)
            throws com.communote.server.api.core.security.AuthorizationException;

    /**
     * Performs the core logic for {@link #sendNewPWLink(User)}
     *
     * @throws ExternalUsersMayNotChangeTheirPasswordException
     */
    protected abstract void handleSendNewPWLink(User user)
            throws ExternalUsersMayNotChangeTheirPasswordException;

    /**
     * Performs the core logic for {@link #unlockUser(String)}
     */
    protected abstract User handleUnlockUser(String securityCode)
            throws com.communote.server.persistence.common.security.SecurityCodeNotFoundException;

    /**
     * Performs the core logic for {@link #updateExternalUser(ExternalUserVO)}
     */
    protected abstract User handleUpdateExternalUser(ExternalUserVO userVO)
            throws AliasAlreadyExistsException, EmailAlreadyExistsException,
            com.communote.server.api.core.common.EmailValidationException,
            InvalidUserStatusTransitionException, NoClientManagerLeftException,
            PermanentIdMissmatchException, UserActivationValidationException;

    /**
     * Performs the core logic for {@link #updateLanguage(Long, String)}
     */
    protected abstract void handleUpdateLanguage(Long userId, String languageCode);

    @Override
    public User inviteUserToBlog(Long blogId, UserVO userData,
            com.communote.server.model.blog.BlogRole role) throws AliasAlreadyExistsException,
            com.communote.server.api.core.common.EmailValidationException,
            EmailAlreadyExistsException,

            PermanentIdMissmatchException, AuthorizationException {
        if (blogId == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.service.user.UserManagement.inviteUserToBlog(Long blogId, User inviter, UserVO userData, com.communote.server.persistence.blog.BlogRole role) - 'blogId' can not be null");
        }
        if (userData == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.service.user.UserManagement.inviteUserToBlog(Long blogId, User inviter, UserVO userData, com.communote.server.persistence.blog.BlogRole role) - 'userData' can not be null");
        }
        if (userData.getLanguage() == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.service.user.UserManagement.inviteUserToBlog(Long blogId, User inviter, UserVO userData, com.communote.server.persistence.blog.BlogRole role) - 'userData.language' can not be null");
        }
        if (userData.getEmail() == null || userData.getEmail().trim().length() == 0) {
            throw new IllegalArgumentException(
                    "com.communote.server.service.user.UserManagement.inviteUserToBlog(Long blogId, User inviter, UserVO userData, com.communote.server.persistence.blog.BlogRole role) - 'userData.email' can not be null or empty");
        }
        if (userData.getRoles() == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.service.user.UserManagement.inviteUserToBlog(Long blogId, User inviter, UserVO userData, com.communote.server.persistence.blog.BlogRole role) - 'userData.roles' can not be null");
        }
        if (role == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.service.user.UserManagement.inviteUserToBlog(Long blogId, User inviter, UserVO userData, com.communote.server.persistence.blog.BlogRole role) - 'role' can not be null");
        }
        try {
            return this.handleInviteUserToBlog(blogId, userData, role);
        } catch (RuntimeException rt) {
            throw new UserManagementException(
                    "Error performing 'com.communote.server.service.user.UserManagement.inviteUserToBlog(Long blogId, User inviter, UserVO userData, com.communote.server.persistence.blog.BlogRole role)' --> "
                            + rt, rt);
        }
    }

    @Override
    public User inviteUserToClient(UserVO user)
            throws com.communote.server.api.core.common.EmailValidationException,
            EmailAlreadyExistsException, AliasAlreadyExistsException,
            PermanentIdMissmatchException, AuthorizationException {
        if (user == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.service.user.UserManagement.inviteUserToClient(User inviter, UserVO user) - 'user' can not be null");
        }
        if (user.getLanguage() == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.service.user.UserManagement.inviteUserToClient(User inviter, UserVO user) - 'user.language' can not be null");
        }
        if (user.getEmail() == null || user.getEmail().trim().length() == 0) {
            throw new IllegalArgumentException(
                    "com.communote.server.service.user.UserManagement.inviteUserToClient(User inviter, UserVO user) - 'user.email' can not be null or empty");
        }
        if (user.getRoles() == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.service.user.UserManagement.inviteUserToClient(User inviter, UserVO user) - 'user.roles' can not be null");
        }
        try {
            return this.handleInviteUserToClient(user);
        } catch (RuntimeException rt) {
            throw new UserManagementException(
                    "Error performing 'com.communote.server.service.user.UserManagement.inviteUserToClient(User inviter, UserVO user)' --> "
                            + rt, rt);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void permanentlyDisableUser(Long userId, Long[] blogIds, boolean becomeManager)
            throws com.communote.server.api.core.security.AuthorizationException,
            NoClientManagerLeftException, UserDeletionDisabledException,
            InvalidUserStatusTransitionException,
            com.communote.server.api.core.blog.NoBlogManagerLeftException {
        if (userId == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.service.user.UserManagement.permanentlyDisableUser(Long userId, Long[] blogIds, boolean becomeManager) - 'userId' can not be null");
        }
        this.handlePermanentlyDisableUser(userId, blogIds, becomeManager);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public User registerUser(String email, java.util.Locale locale, RegistrationType type)
            throws com.communote.server.api.core.common.EmailValidationException,
            EmailAlreadyExistsException {
        if (email == null || email.trim().length() == 0) {
            throw new IllegalArgumentException(
                    "com.communote.server.service.user.UserManagement.registerUser(String email, java.util.Locale local) - 'email' can not be null or empty");
        }
        if (locale == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.service.user.UserManagement.registerUser(String email, java.util.Locale local) - 'local' can not be null");
        }
        try {
            return this.handleRegisterUser(email, locale, type);
        } catch (RuntimeException rt) {
            throw new UserManagementException(
                    "Error performing 'com.communote.server.service.user.UserManagement.registerUser(String email, java.util.Locale local)' --> "
                            + rt, rt);
        }
    }

    @Override
    public User removeUserRole(Long userId, UserRole role) throws AuthorizationException,
            InvalidOperationException, NoClientManagerLeftException {
        if (userId == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.service.user.UserManagement.removeUserRole(Long userId, UserRole role) - 'userId' can not be null");
        }
        if (role == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.service.user.UserManagement.removeUserRole(Long userId, UserRole role) - 'role' can not be null");
        }
        try {
            return this.handleRemoveUserRole(userId, role);
        } catch (RuntimeException rt) {
            throw new UserManagementException(
                    "Error performing 'com.communote.server.service.user.UserManagement.removeUserRole(Long userId, UserRole role)' --> "
                            + rt, rt);
        }
    }

    /**
     * @see UserManagement#resetExternalUserId(String)
     */
    @Override
    public void resetPermanentId(String systemId)
            throws com.communote.server.api.core.security.AuthorizationException {
        if (systemId == null || systemId.trim().length() == 0) {
            throw new IllegalArgumentException(
                    "com.communote.server.service.user.UserManagement.resetExternalUserId(String systemId) - 'systemId' can not be null or empty");
        }
        try {
            this.handleResetPermanentId(systemId);
        } catch (RuntimeException rt) {
            throw new UserManagementException(
                    "Error performing 'com.communote.server.service.user.UserManagement.resetExternalUserId(String systemId)' --> "
                            + rt, rt);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sendNewPWLink(User user) throws ExternalUsersMayNotChangeTheirPasswordException {
        if (user == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.service.user.UserManagement.sendNewPWLink(User user) - 'user' can not be null");
        }
        try {
            this.handleSendNewPWLink(user);
        } catch (RuntimeException rt) {
            throw new UserManagementException(
                    "Error performing 'com.communote.server.service.user.UserManagement.sendNewPWLink(User user)' --> "
                            + rt, rt);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public User unlockUser(String securityCode)
            throws com.communote.server.persistence.common.security.SecurityCodeNotFoundException {
        if (securityCode == null || securityCode.trim().length() == 0) {
            throw new IllegalArgumentException(
                    "com.communote.server.service.user.UserManagement.unlockUser(String securityCode) - 'securityCode' can not be null or empty");
        }
        try {
            return this.handleUnlockUser(securityCode);
        } catch (RuntimeException rt) {
            throw new UserManagementException(
                    "Error performing 'com.communote.server.service.user.UserManagement.unlockUser(String securityCode)' --> "
                            + rt, rt);
        }
    }

    @Override
    public User updateExternalUser(ExternalUserVO userVO) throws AliasAlreadyExistsException,
            EmailAlreadyExistsException,
            com.communote.server.api.core.common.EmailValidationException,
            UserActivationValidationException, InvalidUserStatusTransitionException,
            NoClientManagerLeftException, PermanentIdMissmatchException {
        if (userVO == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.service.user.UserManagement.updateExternalUser(ExternalUserVO userVO) - 'userVO' can not be null");
        }
        if (userVO.getExternalUserName() == null
                || userVO.getExternalUserName().trim().length() == 0) {
            throw new IllegalArgumentException(
                    "com.communote.server.service.user.UserManagement.updateExternalUser(ExternalUserVO userVO) - 'userVO.externalUserName' can not be null or empty");
        }
        if (userVO.getSystemId() == null || userVO.getSystemId().trim().length() == 0) {
            throw new IllegalArgumentException(
                    "com.communote.server.service.user.UserManagement.updateExternalUser(ExternalUserVO userVO) - 'userVO.systemId' can not be null or empty");
        }
        try {
            return this.handleUpdateExternalUser(userVO);
        } catch (RuntimeException rt) {
            throw new UserManagementException(
                    "Error performing 'com.communote.server.service.user.UserManagement.updateExternalUser(ExternalUserVO userVO)' --> "
                            + rt, rt);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateLanguage(Long userId, String languageCode) {
        if (userId == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.service.user.UserManagement.updateLanguage(Long userId, String languageCode) - 'userId' can not be null");
        }
        if (languageCode == null || languageCode.trim().length() == 0) {
            throw new IllegalArgumentException(
                    "com.communote.server.service.user.UserManagement.updateLanguage(Long userId, String languageCode) - 'languageCode' can not be null or empty");
        }
        try {
            this.handleUpdateLanguage(userId, languageCode);
        } catch (RuntimeException rt) {
            throw new UserManagementException(
                    "Error performing 'com.communote.server.service.user.UserManagement.updateLanguage(Long userId, String languageCode)' --> "
                            + rt, rt);
        }
    }
}