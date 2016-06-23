package com.communote.server.core.user;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.communote.common.converter.Converter;
import com.communote.common.util.DateHelper;
import com.communote.common.util.DescendingOrderComparator;
import com.communote.common.util.Pair;
import com.communote.common.validation.EmailValidator;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.blog.BlogManagement;
import com.communote.server.api.core.blog.BlogNotFoundException;
import com.communote.server.api.core.blog.BlogRightsManagement;
import com.communote.server.api.core.blog.NoBlogManagerLeftException;
import com.communote.server.api.core.common.EmailValidationException;
import com.communote.server.api.core.common.NotFoundException;
import com.communote.server.api.core.config.ClientConfigurationProperties;
import com.communote.server.api.core.config.type.ClientProperty;
import com.communote.server.api.core.config.type.ClientPropertySecurity;
import com.communote.server.api.core.event.EventDispatcher;
import com.communote.server.api.core.property.PropertyManagement;
import com.communote.server.api.core.property.PropertyType;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.api.core.security.SecurityCodeManagement;
import com.communote.server.api.core.tag.TagTO;
import com.communote.server.api.core.task.TaskAlreadyExistsException;
import com.communote.server.api.core.task.TaskManagement;
import com.communote.server.api.core.user.CommunoteEntityNotFoundException;
import com.communote.server.api.core.user.UserNotFoundException;
import com.communote.server.api.core.user.UserVO;
import com.communote.server.core.common.exceptions.InvalidOperationException;
import com.communote.server.core.common.exceptions.PasswordLengthException;
import com.communote.server.core.config.ClientConfigurationHelper;
import com.communote.server.core.mail.MailManagement;
import com.communote.server.core.mail.MailMessageHelper;
import com.communote.server.core.mail.messages.ForgottenPWMailMessage;
import com.communote.server.core.mail.messages.GenericMailMessage;
import com.communote.server.core.mail.messages.MailMessage;
import com.communote.server.core.mail.messages.MailModelPlaceholderConstants;
import com.communote.server.core.mail.messages.user.ActivateUserMailMessage;
import com.communote.server.core.mail.messages.user.ConfirmEmailAddressMessage;
import com.communote.server.core.mail.messages.user.ConfirmUserMailMessage;
import com.communote.server.core.mail.messages.user.InviteUserToBlogMailMessage;
import com.communote.server.core.mail.messages.user.InviteUserToBlogWithExternalAuthMailMessage;
import com.communote.server.core.mail.messages.user.InviteUserToClientMailMessage;
import com.communote.server.core.mail.messages.user.InviteUserToClientWithExternalAuthMailMessage;
import com.communote.server.core.mail.messages.user.RemindUserLoginMailMessage;
import com.communote.server.core.mail.messages.user.RemindUserRegistrationMailMessage;
import com.communote.server.core.mail.messages.user.manager.ActivateUserForManagerMailMessage;
import com.communote.server.core.mail.messages.user.manager.NewUserConfirmedForManagerMessage;
import com.communote.server.core.security.AuthenticationHelper;
import com.communote.server.core.security.SecurityHelper;
import com.communote.server.core.security.UserIdentification;
import com.communote.server.core.tag.TagManagement;
import com.communote.server.core.tag.TagNotFoundException;
import com.communote.server.core.tag.TagStoreNotFoundException;
import com.communote.server.core.user.events.UserStatusChangedEvent;
import com.communote.server.core.user.groups.SynchronizeExternalUserTaskHandler;
import com.communote.server.core.user.helper.ValidationPatterns;
import com.communote.server.core.user.listener.UserLimitNotificationOnUserActivation;
import com.communote.server.core.user.validation.UserActivationValidationException;
import com.communote.server.core.user.validation.UserActivationValidator;
import com.communote.server.model.blog.Blog;
import com.communote.server.model.blog.BlogRole;
import com.communote.server.model.global.GlobalId;
import com.communote.server.model.security.SecurityCode;
import com.communote.server.model.security.SecurityCodeAction;
import com.communote.server.model.tag.Tag;
import com.communote.server.model.user.ExternalUserAuthentication;
import com.communote.server.model.user.NotificationConfig;
import com.communote.server.model.user.User;
import com.communote.server.model.user.UserAuthority;
import com.communote.server.model.user.UserProfile;
import com.communote.server.model.user.UserRole;
import com.communote.server.model.user.UserStatus;
import com.communote.server.model.user.security.AuthenticationFailedStatus;
import com.communote.server.model.user.security.EmailSecurityCode;
import com.communote.server.model.user.security.ForgottenPasswordSecurityCode;
import com.communote.server.model.user.security.InviteUserToBlogSecurityCode;
import com.communote.server.model.user.security.InviteUserToClientSecurityCode;
import com.communote.server.model.user.security.UnlockUserSecurityCode;
import com.communote.server.model.user.security.UserSecurityCode;
import com.communote.server.persistence.blog.BlogDao;
import com.communote.server.persistence.common.security.SecurityCodeDao;
import com.communote.server.persistence.common.security.SecurityCodeNotFoundException;
import com.communote.server.persistence.user.ExternalUserAuthenticationDao;
import com.communote.server.persistence.user.ExternalUserVO;
import com.communote.server.persistence.user.NotificationConfigDao;
import com.communote.server.persistence.user.UserAuthorityDao;
import com.communote.server.persistence.user.UserDao;
import com.communote.server.persistence.user.UserProfileDao;
import com.communote.server.persistence.user.client.ClientHelper;
import com.communote.server.persistence.user.security.AuthenticationFailedStatusDao;
import com.communote.server.persistence.user.security.EmailSecurityCodeDao;
import com.communote.server.persistence.user.security.ForgottenPasswordSecurityCodeDao;
import com.communote.server.persistence.user.security.InviteSecurityCodeDao;
import com.communote.server.persistence.user.security.InviteUserToBlogSecurityCodeDao;
import com.communote.server.persistence.user.security.InviteUserToClientSecurityCodeDao;
import com.communote.server.persistence.user.security.UnlockUserSecurityCodeDao;
import com.communote.server.persistence.user.security.UserSecurityCodeDao;
import com.communote.server.service.NoteService;

/**
 * The implementation of the {@link UserManagement}. <br>
 * TODO REFACTORING getting to complex
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
// TODO extract all methods that query users or information about users into a separate management
// class like UserQueryManagement or similar to reduce the size of this class
// TODO move e-mail send code to the UserService
// TODO check deprecated methods whether they are actually deprecated or 'just' need to be refined
// TODO resolve TODOs ;)
@Service("userManagement")
public class UserManagementImpl extends UserManagementBase {

    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(UserManagementImpl.class);

    /** Default time after the user will be reminded, set to one week. */
    private final static long DEFAULT_REMIND_USER_TIME = 7 * DateHelper.DAYS;

    private List<UserActivationValidator> userActivationValidators = new ArrayList<>();

    @Autowired
    private UserDao userDao;

    @Autowired
    private UserSecurityCodeDao userSecurityCodeDao;

    @Autowired
    private EmailSecurityCodeDao emailSecurityCodeDao;

    @Autowired
    private ForgottenPasswordSecurityCodeDao forgottenPasswordSecurityCodeDao;

    @Autowired
    private ExternalUserAuthenticationDao externalUserAuthenticationDao;

    @Autowired
    private UserAuthorityDao userAuthorityDao;

    @Autowired
    private NotificationConfigDao notificationConfigDao;

    @Autowired
    private InviteUserToClientSecurityCodeDao inviteUserToClientSecurityCodeDao;

    @Autowired
    private SecurityCodeDao securityCodeDao;

    @Autowired
    private UnlockUserSecurityCodeDao unlockUserSecurityCodeDao;

    @Autowired
    private AuthenticationFailedStatusDao authenticationFailedStatusDao;

    @Autowired
    private UserProfileDao userProfileDao;

    @Autowired
    private InviteUserToBlogSecurityCodeDao inviteUserToBlogSecurityCodeDao;

    @Autowired
    private TagManagement tagManagement;

    @Autowired
    private TaskManagement taskManagement;

    @Autowired
    private BlogManagement blogManagement;

    @Autowired
    private BlogRightsManagement blogRightsManagement;

    @Autowired
    private MailManagement mailManagement;
    @Autowired
    private PropertyManagement propertyManagement;

    @Autowired
    private UserProfileManagement userProfileManagement;

    @Autowired
    private SecurityCodeManagement securityCodeManagement;

    @Autowired
    private EventDispatcher eventDispatcher;

    /**
     * Assert that the current user is a client manager
     *
     * @throws AuthorizationException
     *             in case the current user is not a client manager
     */
    private void assertClientManager() throws AuthorizationException {
        if (!SecurityHelper.isClientManager()) {
            throw new AuthorizationException("Current user is not client manager");
        }
    }

    /**
     * Checks whether the user deletion is allowed (i.e. user has appropriate rights and it's not
     * disabled) and the deletion does not lead to an unmanaged client.
     *
     * @param userId
     *            the ID of the user to be deleted
     * @param modeEnabled
     *            whether the deletion mode (anonymize or permanently disable) is enabled for the
     *            client
     * @throws NoClientManagerLeftException
     *             in case the deletion would lead to an unmanaged client
     * @throws AuthorizationException
     *             in case the user has not the required rights
     * @throws UserDeletionDisabledException
     *             in case the current user is not client manager and modeEnabled is false
     */
    private void assertUserDeletionPossible(Long userId, boolean modeEnabled)
            throws NoClientManagerLeftException, AuthorizationException,
            UserDeletionDisabledException {
        Long currentUserId = SecurityHelper.getCurrentUserId();
        // security checks
        if (SecurityHelper.isClientManager()) {
            if (currentUserId.equals(userId) && onlyOneClientManagerLeft()) {
                throw new NoClientManagerLeftException("Current user cannot be deleted because"
                        + " he is the last active client manager");
            }
        } else {
            if (!modeEnabled) {
                throw new UserDeletionDisabledException("User deletion is not enabled.");
            }
            if (!currentUserId.equals(userId)) {
                throw new AuthorizationException(
                        "Current user is not allowed to delete user with id " + userId);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean changeAlias(Long userId, String newAlias) throws AliasAlreadyExistsException,
    AliasInvalidException {
        SecurityHelper.assertCurrentUserIsClientManager();
        User user = userDao.load(userId);
        newAlias = newAlias == null ? null : newAlias.trim();
        if (user == null || user.getAlias().equals(newAlias)) {
            return false;
        }
        if (newAlias == null || !newAlias.matches(ValidationPatterns.PATTERN_ALIAS)) {
            throw new AliasInvalidException("The alias is invalid: " + newAlias == null ? "null"
                    : newAlias);
        }
        validateAlias(newAlias, user);
        user.setAlias(newAlias);
        GenericMailMessage message = new GenericMailMessage("mail.message.user.alias-changed",
                user.getLanguageLocale(), user);
        message.addToModel("alias", newAlias);
        message.addToModel("firstName", user.getProfile().getFirstName());
        message.addToModel("lastName", user.getProfile().getLastName());
        mailManagement.sendMail(message);
        return true;
    }

    /**
     * Creates localized mail messages of type ActivateUserForManagerMailMessage for sending to all
     * client manager
     *
     * @param activatedUser
     *            the activated user
     * @param firstActivation
     *            whether it is the first activation (e.g. after confirmation) or a re-activation
     *            (e.g. after being temporarily disabled)
     * @return the collection< activate user for manager mail message>
     */
    private Collection<ActivateUserForManagerMailMessage> createActivateUserForManagerMailMessage(
            User activatedUser, boolean firstActivation) {
        List<User> clientManager = ServiceLocator.instance().getService(UserManagement.class)
                .findUsersByRole(UserRole.ROLE_KENMEI_CLIENT_MANAGER, UserStatus.ACTIVE);
        Map<Locale, Collection<User>> localizedUsers = UserManagementHelper
                .getUserByLocale(clientManager);
        Collection<ActivateUserForManagerMailMessage> result = new ArrayList<ActivateUserForManagerMailMessage>();
        for (Entry<Locale, Collection<User>> item : localizedUsers.entrySet()) {
            result.add(new ActivateUserForManagerMailMessage(activatedUser, item.getKey(),
                    firstActivation, item.getValue()));
        }
        return result;
    }

    /**
     * Creates localized mail messages of type NewUserConfirmedForManagerMessage for sending to all
     * client manager
     *
     * @param confirmedUser
     *            the confirmed user
     * @return collection of mail messages to be send
     */
    private Collection<NewUserConfirmedForManagerMessage> createNewUserConfirmedForManagerMail(
            User confirmedUser) {
        List<User> clientManager = findUsersByRole(UserRole.ROLE_KENMEI_CLIENT_MANAGER,
                UserStatus.ACTIVE);
        Map<Locale, Collection<User>> localizedUsers = UserManagementHelper
                .getUserByLocale(clientManager);
        Collection<NewUserConfirmedForManagerMessage> result = new ArrayList<NewUserConfirmedForManagerMessage>();
        for (Entry<Locale, Collection<User>> item : localizedUsers.entrySet()) {
            result.add(new NewUserConfirmedForManagerMessage(confirmedUser, item.getValue(), item
                    .getKey()));
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<User> findNotDeletedUsers(boolean excludeSystemUsers) {
        return userDao.findNotDeletedUser(excludeSystemUsers);
    }

    @Override
    public User findUser(UserIdentification userIdentification) {
        User user = this.findUser(userIdentification, false);
        return user;
    }

    @Override
    public User findUser(UserIdentification userIdentification, boolean loadExternalAuthentications) {
        User user = null;

        if (userIdentification.getUserId() != null) {
            user = userDao.load(userIdentification.getUserId());
        } else if (userIdentification.getUserAlias() != null) {
            user = handleFindUserByAlias(userIdentification.getUserAlias());
        }

        if (userIdentification.getExternalSystemId() != null
                && userIdentification.getExternalUserId() != null) {
            user = handleFindUserByExternalUserId(userIdentification.getExternalUserId(),
                    userIdentification.getExternalSystemId());
        }

        if (user != null) {
            Hibernate.initialize(user.getTags());
            if (loadExternalAuthentications) {
                Hibernate.initialize(user.getExternalAuthentications());
            }
        }
        return user;
    }

    @Override
    @Transactional(readOnly = true)
    public User findUserByUserId(Long userId, boolean loadExternalAuthentications) {
        User user = userDao.load(userId);
        if (user != null) {
            Hibernate.initialize(user.getTags());
            if (loadExternalAuthentications) {
                Hibernate.initialize(user.getExternalAuthentications());
            }
        }
        return user;
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> findUsersByRole(UserRole role, UserStatus status) {
        return userDao.findByRole(role, status);
    }

    /**
     * Fire an event to notify about a user status change
     *
     * @param userId
     *            the ID of the user whose status changed
     * @param oldStatus
     *            the previous status
     * @param newStatus
     *            the new status
     */
    private void fireUserStatusChangedEvent(Long userId, UserStatus oldStatus, UserStatus newStatus) {
        // ensure status was changed
        if (newStatus != null && !newStatus.equals(oldStatus)) {
            eventDispatcher.fire(new UserStatusChangedEvent(userId, oldStatus, newStatus));
        }
    }

    @Override
    @Transactional(readOnly = true)
    public String generateUniqueAlias(String externalUserName, String emailAddress) {
        if (externalUserName != null && externalUserName.matches(ValidationPatterns.PATTERN_ALIAS)) {
            if (handleFindUserByAlias(externalUserName) == null) {
                return externalUserName;
            }
        }
        // generate an alias from the local part of the emailAddress
        String alias = UserManagementHelper.getLegalAliasFromEmailAddress(emailAddress);
        String aliasBase = alias;
        int aliasCounter = 1;
        while (handleFindUserByAlias(alias) != null) {
            alias = aliasBase + aliasCounter;
            aliasCounter++;
        }
        return alias;
    }

    @Override
    @Transactional(readOnly = true)
    public long getActiveUserCount(String systemId, UserRole role) {
        return userDao.getActiveUserCount(systemId, role);
    }

    private ClientConfigurationProperties getClientConfigurationProperties() {
        return CommunoteRuntime.getInstance().getConfigurationManager()
                .getClientConfigurationProperties();
    }

    private User getCurrentUser(UserRole requiredRole) {
        User user = null;
        Long userId = SecurityHelper.getCurrentUserId();
        if (userId != null) {
            user = userDao.load(userId);
            if (user != null && requiredRole != null) {
                for (UserRole role : user.getRoles()) {
                    if (requiredRole.equals(role)) {
                        return user;
                    }
                }
            }
        }
        return user;
    }

    /**
     * Returns the external authentication of the user with the provided systemId.
     *
     * @param user
     *            the user
     * @param systemId
     *            the ID of the external system
     * @return the authentication or null if the user has none
     */
    private ExternalUserAuthentication getExternalAuthentication(User user, String systemId) {
        if (user.getExternalAuthentications() != null) {
            for (ExternalUserAuthentication auth : user.getExternalAuthentications()) {
                if (auth.getSystemId().equals(systemId)) {
                    return auth;
                }
            }
        }
        return null;
    }

    @Override
    public Set<ExternalUserAuthentication> getExternalExternalUserAuthentications(long userId) {
        User user = userDao.load(userId);
        if (user != null) {
            Set<ExternalUserAuthentication> externalAuthentications = user
                    .getExternalAuthentications();
            if (externalAuthentications != null) {
                for (ExternalUserAuthentication externalUserAuthentication : externalAuthentications) {
                    externalUserAuthentication.getSystemId();
                }
                return externalAuthentications;
            }
        }
        return new HashSet<ExternalUserAuthentication>();
    }

    @Override
    @Transactional(readOnly = true)
    public <T> T getUserById(Long userId, Converter<User, T> converter) {
        User user = userDao.load(userId);
        if (user != null) {
            return converter.convert(user);
        }
        return null;
    }

    @Override
    protected void handleAcceptTermsOfUse(Long userId) throws UserNotFoundException,
    AuthorizationException, UserActivationValidationException {
        User user = userDao.load(userId);
        if (user == null) {
            throw new UserNotFoundException("User with ID " + userId + " does not exist");
        }
        Long currentUserId = SecurityHelper.getCurrentUserId();
        if (currentUserId != null && !currentUserId.equals(userId)
                && !SecurityHelper.isInternalSystem()) {
            throw new AuthorizationException("The current user " + currentUserId
                    + " is not allowed to accept the terms of use of user " + userId);
        }
        if (UserStatus.TERMS_NOT_ACCEPTED.equals(user.getStatus())) {
            try {
                internalChangeUserStatus(user, UserStatus.ACTIVE, false, false);
            } catch (InvalidUserStatusTransitionException e) {
                // this should not occur
                LOGGER.error(
                        "Unexpected exception while accepting terms of use for user " + userId, e);
                throw new UserManagementException(
                        "Unexpected exception while accepting terms of use", e);
            }
        } else if (UserStatus.ACTIVE.equals(user.getStatus())) {
            user.setTermsAccepted(true);
        }

    }

    @Override
    protected void handleAnonymizeUser(Long userId, Long[] blogIds, boolean becomeManager)
            throws AuthorizationException, NoBlogManagerLeftException,
            com.communote.server.core.user.NoClientManagerLeftException,
            UserDeletionDisabledException {
        try {
            assertUserDeletionPossible(userId,
                    ClientProperty.DELETE_USER_BY_ANONYMIZE_ENABLED
                            .getValue(ClientProperty.DEFAULT_DELETE_USER_BY_ANONYMIZE_ENABLED));
        } catch (UserDeletionDisabledException e) {
            throw new UserAnonymizationDisabledException(
                    "Deleting a user by making his profile anonymous is not enabled.");
        }
        internalAnonymizeUser(userId, blogIds, becomeManager);
    }

    @Override
    protected User handleAssignUserRole(Long userId, UserRole role) throws AuthorizationException,
            InvalidOperationException {
        assertClientManager();
        User user = userDao.load(userId);
        if (user != null) {
            boolean hasRole = false;
            boolean isSystemUser = false;
            boolean isCrawlUser = false;
            for (UserAuthority authority : user.getUserAuthorities()) {
                if (authority.getRole().equals(role)) {
                    hasRole = true;
                } else if (authority.getRole().equals(UserRole.ROLE_SYSTEM_USER)) {
                    isSystemUser = true;
                } else if (authority.getRole().equals(UserRole.ROLE_CRAWL_USER)) {
                    isCrawlUser = true;
                }
            }
            if (!hasRole) {
                if (UserRole.ROLE_SYSTEM_USER.equals(role)) {
                    throw new InvalidOperationException(
                            "It is not possible to convert a user into a system user. userId="
                                    + userId);
                }
                if (UserRole.ROLE_CRAWL_USER.equals(role)) {
                    throw new InvalidOperationException(
                            "It is not possible to convert a user into a crawl user. userId="
                                    + userId);
                }
                if (isSystemUser) {
                    throw new InvalidOperationException(
                            "It is not possible to assign an additional role to a system user. userId="
                                    + userId);
                }
                if (isCrawlUser) {
                    throw new InvalidOperationException(
                            "It is not possible to assign an additional role to a crawl user. userId="
                                    + userId);
                }
                UserAuthority authority = UserAuthority.Factory.newInstance(role);
                userAuthorityDao.create(authority);
                user.getUserAuthorities().add(authority);
                userDao.update(user);
            }
        }
        return user;
    }

    @Override
    protected boolean handleChangeEmailAddress(Long userId, String newEmail,
            boolean sendConfirmationEmail) throws EmailAlreadyExistsException,
            EmailValidationException {
        if (!userId.equals(SecurityHelper.getCurrentUserId()) && !SecurityHelper.isClientManager()) {
            throw new AccessDeniedException(
                    "Only the user itself or a client manager can change the email address.");
        }
        User user = userDao.load(userId);
        newEmail = newEmail == null ? null : newEmail.trim();
        if (user == null || user.getEmail().equals(newEmail)) {
            return false;
        }
        validateEmail(newEmail, user, true);
        MailMessage message = null;
        if (!sendConfirmationEmail && SecurityHelper.isClientManager()) {
            user.setEmail(newEmail);
            GenericMailMessage genericMessage = new GenericMailMessage(
                    "mail.message.user.inform-email-address", user.getLanguageLocale(), user);
            genericMessage.addToModel(MailModelPlaceholderConstants.USER, user);
            message = genericMessage;
        } else {
            EmailSecurityCode code = emailSecurityCodeDao.createCode(user, newEmail);
            message = new ConfirmEmailAddressMessage(user, code);
        }
        mailManagement.sendMail(message);
        return true;
    }

    @Override
    protected void handleChangePassword(Long userId, String newPassword)
            throws PasswordLengthException {
        User user = userDao.load(userId);
        if (user == null) {
            return;
        }
        validatePassword(newPassword, user.getRoles());
        user.setPlainPassword(newPassword);
    }

    @Override
    protected void handleChangeUserStatusByManager(Long userId, UserStatus newStatus)
            throws UserActivationValidationException, InvalidUserStatusTransitionException,
            UserNotFoundException, NoClientManagerLeftException {
        SecurityHelper.assertCurrentUserIsClientManager();
        if (userId == null || newStatus == null) {
            throw new IllegalArgumentException("userId and newStatus cannot be null");
        }
        User user = userDao.load(userId);
        if (user == null) {
            throw new UserNotFoundException("The user with ID " + userId + " does not exist.");
        }
        if (SecurityHelper.getCurrentUserId().equals(user.getId())
                && newStatus.equals(UserStatus.TEMPORARILY_DISABLED) && onlyOneClientManagerLeft()) {
            throw new NoClientManagerLeftException(
                    "The current user cannot be disabled, because he is the last client manager.");
        }
        if (UserStatus.ACTIVE.equals(newStatus)
                && user.hasStatus(UserStatus.TERMS_NOT_ACCEPTED)
                && ClientProperty.TERMS_OF_USE_USERS_MUST_ACCEPT
                .getValue(ClientProperty.DEFAULT_TERMS_OF_USE_USERS_MUST_ACCEPT)) {
            // do nothing because the user should accept the terms of use himself
            return;
        }
        internalChangeUserStatus(user, newStatus, true, false);
    }

    @Override
    protected void handleConfirmNewEmailAddress(String securityCode)
            throws SecurityCodeNotFoundException, EmailAlreadyExistsException {
        EmailSecurityCode code = emailSecurityCodeDao.findBySecurityCode(securityCode);
        if (code != null) {
            if (userDao.findByEmail(code.getNewEmailAddress()) != null) {
                emailSecurityCodeDao.remove(code);
                throw new EmailAlreadyExistsException(
                        "In the interim, another user was already registered with this email address. eMail: "
                                + code.getNewEmailAddress());
            }
            User user = code.getUser();
            user.setEmail(code.getNewEmailAddress());
            userDao.update(user);
            securityCodeDao.deleteAllCodesByUser(user.getId(), EmailSecurityCode.class);
        } else {
            throw new SecurityCodeNotFoundException("The security code '" + securityCode
                    + "' could not be found");
        }
    }

    @Override
    protected User handleConfirmUser(String securitycode, UserVO data)
            throws SecurityCodeNotFoundException, EmailValidationException,
            EmailAlreadyExistsException, AliasAlreadyExistsException, PasswordLengthException,
            InvalidUserStatusTransitionException {
        if (!getClientConfigurationProperties().isDBAuthenticationAllowed()) {
            throw new AccessDeniedException(
                    "Confirmation of user is not allowed as the database authentication is deactivated.");
        }
        SecurityCode code = securityCodeDao.findByCode(securitycode);
        User user = null;
        List<MimeMessagePreparator> mails = new ArrayList<MimeMessagePreparator>();
        if (code == null) {
            throw new SecurityCodeNotFoundException("The security code '" + securitycode
                    + "' could not be found!");
        }
        user = code.getUser();
        if (code instanceof UserSecurityCode || code instanceof InviteUserToClientSecurityCode
                || code instanceof InviteUserToBlogSecurityCode) {
            UserStatus oldStatus = user.getStatus();
            if (UserStatus.REGISTERED.equals(oldStatus) || UserStatus.INVITED.equals(oldStatus)) {

                validateEmail(data.getEmail(), user, true);
                validateAlias(data.getAlias(), user);
                if (data.isPlainPassword()) {
                    validatePassword(data.getPassword(), user.getRoles());
                }

                // update user with vo data
                userDao.userVOToEntity(data, user, false);
                // update user status to confirmed
                user.setStatus(UserStatus.CONFIRMED);
                user.setStatusChanged(new Timestamp(new Date().getTime()));
                // fire event that user was confirmed. If user is automatically upgraded to ACTIVE
                // afterwards another event will be fired.
                fireUserStatusChangedEvent(user.getId(), oldStatus, user.getStatus());
                userConfirmed(user, !(code instanceof InviteUserToClientSecurityCode), mails);
                sendMail(mails);
            }
            securityCodeDao.remove(code);
            return user;
        }
        throw new UserManagementException("code type not supported: " + code);
    }

    @Override
    protected User handleCreateOrUpdateExternalUser(ExternalUserVO userVO)
            throws EmailAlreadyExistsException, EmailValidationException,
            AliasAlreadyExistsException, UserActivationValidationException,
            InvalidUserStatusTransitionException, PermanentIdMissmatchException {
        User user = internalFindExistingExternalUser(userVO);
        if (user == null) {
            setLanguageIfNotExist(userVO);
            // test whether a user with the email already exists
            user = userDao.findByEmail(userVO.getEmail());
            if (user != null) {
                if (testMergingExternalUserWithExistingUser(user, userVO)) {
                    internalCreateOrUpdateExternalAuthentication(user, userVO);
                }
                internalUpdateExternalUser(user, userVO);
            } else {
                user = internalCreateExternalUser(userVO);
            }
        } else {
            internalUpdateExternalUser(user, userVO);
        }
        return user;
    }

    // TODO from an API point of view this method should be refactored because the parameters
    // undermine configuration settings (especially managerActivation). There should probably be
    // separate methods for creating the first client admin and system users so the parameters are
    // not necessary. But what about the unit tests?
    @Override
    protected User handleCreateUser(UserVO userVo, boolean emailConfirmation,
            boolean managerActivation) throws EmailAlreadyExistsException,
            EmailValidationException, AliasAlreadyExistsException, PasswordLengthException {
        if (userVo.isPlainPassword()) {
            validatePassword(userVo.getPassword(), userVo.getRoles());
        }
        User user = internalCreateUser(userVo, emailConfirmation, managerActivation);
        return user;
    }

    @Override
    protected User handleFindUserByAlias(String alias) {
        return userDao.findByAlias(alias);
    }

    @Override
    protected User handleFindUserByEmail(String email) {
        return userDao.findByEmail(email);
    }

    @Override
    protected User handleFindUserByEmailAlias(String username) {
        User user = null;
        if (username.contains("@")) {
            user = userDao.findByEmail(username);
        } else {
            user = userDao.findByAlias(username);
        }
        return user;
    }

    @Override
    protected User handleFindUserByExternalUserId(String userId, String systemId) {
        return userDao.findByExternalUserId(userId, systemId);
    }

    @Override
    protected long handleGetActiveUserCount() {
        return userDao.getActiveUserCount();
    }

    @Override
    protected UserRole[] handleGetRolesOfUser(Long userId) {
        User u = userDao.load(userId);
        if (u != null && u.getUserAuthorities() != null) {
            return u.getRoles();
        }
        return null;
    }

    @Override
    protected User handleInviteUserToBlog(Long blogId, UserVO userVo, BlogRole role)
            throws AliasAlreadyExistsException, EmailAlreadyExistsException,
            EmailValidationException, PermanentIdMissmatchException, AuthorizationException {

        User inviter = getCurrentUser(null);
        if (inviter == null) {
            throw new AuthorizationException("There is no current user");
        }
        List<MimeMessagePreparator> mails = new ArrayList<MimeMessagePreparator>();

        InviteUserToBlogSecurityCode freshSecurityCode = InviteUserToBlogSecurityCode.Factory
                .newInstance(inviter.getId());
        Pair<User, InviteUserToBlogSecurityCode> invitationDetails = internalInviteUser(userVo,
                freshSecurityCode, inviteUserToBlogSecurityCodeDao, mails);
        User invitedUser = invitationDetails.getLeft();
        try {
            Blog topic = blogManagement.getBlogById(blogId, false);
            blogRightsManagement.addEntity(blogId, invitedUser.getId(), role);

            if (userVo instanceof ExternalUserVO) {
                mails.add(new InviteUserToBlogWithExternalAuthMailMessage(inviter, invitedUser,
                        topic));
            } else {
                mails.add(new InviteUserToBlogMailMessage(inviter, invitedUser, topic,
                        invitationDetails.getRight()));
            }

        } catch (BlogNotFoundException e) {
            throw new UserManagementException("blog not found");
        } catch (CommunoteEntityNotFoundException e) {
            throw new UserManagementException("user not found");
        }
        sendMail(mails);
        return invitedUser;
    }

    @Override
    protected User handleInviteUserToClient(UserVO userVo) throws EmailValidationException,
    EmailAlreadyExistsException, AliasAlreadyExistsException,
    PermanentIdMissmatchException, AuthorizationException {

        User inviter = getCurrentUser(UserRole.ROLE_KENMEI_CLIENT_MANAGER);

        if (inviter == null) {
            throw new AuthorizationException("Current user " + SecurityHelper.getCurrentUserId()
                    + " is not allowed to execute this operation");
        }
        List<MimeMessagePreparator> mails = new ArrayList<MimeMessagePreparator>();

        InviteUserToClientSecurityCode freshSecurityCode = InviteUserToClientSecurityCode.Factory
                .newInstance();
        Pair<User, InviteUserToClientSecurityCode> invitationDetails = internalInviteUser(userVo,
                freshSecurityCode, inviteUserToClientSecurityCodeDao, mails);
        User invitedUser = invitationDetails.getLeft();

        if (userVo instanceof ExternalUserVO) {
            mails.add(new InviteUserToClientWithExternalAuthMailMessage(inviter, userVo
                    .getLanguage(), userVo.getEmail()));
        } else {
            mails.add(new InviteUserToClientMailMessage(inviter, userVo.getLanguage(), userVo
                    .getEmail(), invitationDetails.getRight()));
        }
        sendMail(mails);
        return invitedUser;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void handlePermanentlyDisableUser(Long userId, Long[] blogIds, boolean becomeManager)
            throws com.communote.server.api.core.security.AuthorizationException,
            NoBlogManagerLeftException,
            com.communote.server.core.user.NoClientManagerLeftException,
            UserDeletionDisabledException, InvalidUserStatusTransitionException {
        try {
            assertUserDeletionPossible(
                    userId,
                    getClientConfigurationProperties().getProperty(
                            ClientProperty.DELETE_USER_BY_DISABLE_ENABLED,
                            ClientProperty.DEFAULT_DELETE_USER_BY_DISABLE_ENABLED));
        } catch (UserDeletionDisabledException e) {
            throw new UserDisablingDisabledException("Deleting a user by disabling is not enabled.");
        }
        internalPermanentlyDisableUser(userId, blogIds, becomeManager);
    }

    /**
     * {@inheritDoc}
     *
     * @see UserManagement#registerUser(String, Locale, RegistrationType))
     */
    @Override
    protected User handleRegisterUser(String email, Locale locale, RegistrationType type)
            throws EmailValidationException, EmailAlreadyExistsException {
        if (!getClientConfigurationProperties().isRegistrationAllowed()) {
            throw new AccessDeniedException(
                    "register user with external authentication is not allowed");
        }

        validateEmail(email, null, false);

        MimeMessagePreparator mail = null;
        User user = findUserByEmail(email);
        SecurityCode code = null;
        if (user == null) {
            UserVO userVo = new UserVO();
            userVo.setEmail(email);
            userVo.setLanguage(locale);
            userVo.setRoles(new UserRole[] { UserRole.ROLE_KENMEI_USER });

            user = userDao.userVOToEntity(userVo);
            user.setStatus(UserStatus.REGISTERED);
            user.setTermsAccepted(false);
            user.setStatusChanged(new Timestamp(new Date().getTime()));
            user.setReminderMailSent(false);

            user = userDao.create(user);

            code = userSecurityCodeDao.createCode(user);
            fireUserStatusChangedEvent(user.getId(), null, user.getStatus());
        } else {
            if (user.hasStatus(UserStatus.INVITED)) {
                code = inviteUserToBlogSecurityCodeDao.findByUser(user.getId());
                if (code == null) {
                    code = inviteUserToClientSecurityCodeDao.findByUser(user.getId());
                }
            } else if (user.hasStatus(UserStatus.REGISTERED)) {
                // registered user exists, send the code again
                code = userSecurityCodeDao
                        .findByUser(user.getId(), SecurityCodeAction.CONFIRM_USER);
            } else {
                throw new EmailAlreadyExistsException("user with this email already exists");
            }
            if (code == null) {
                // should never happen
                LOGGER.error("Found no security code for invited or registerd user: {}",
                        user.getId());
                throw new UserManagementException(
                        "Found no security code for invited or registerd user");
            }
            // set registered time to current time
            user.setStatusChanged(new Timestamp(new Date().getTime()));
        }
        mail = new ConfirmUserMailMessage(user, code, type);
        mailManagement.sendMail(mail);

        return user;
    }

    @Override
    protected User handleRemoveUserRole(Long userId, UserRole role) throws AuthorizationException,
            InvalidOperationException, NoClientManagerLeftException {
        assertClientManager();
        User user = userDao.load(userId);
        if (user != null) {
            UserAuthority found = null;
            for (UserAuthority authority : user.getUserAuthorities()) {
                if (authority.getRole().equals(role)) {
                    found = authority;
                    break;
                }
            }
            if (found != null) {
                if (UserRole.ROLE_CRAWL_USER.equals(role)) {
                    throw new InvalidOperationException("The crawl user role cannot be removed");
                }
                if (UserRole.ROLE_SYSTEM_USER.equals(role)) {
                    throw new InvalidOperationException("The system user role cannot be removed");
                }
                if (UserRole.ROLE_KENMEI_USER.equals(role)) {
                    throw new InvalidOperationException("The user role cannot be removed");
                }
                // Make sure not to remove the last client manager.
                if (role.equals(UserRole.ROLE_KENMEI_CLIENT_MANAGER) && onlyOneClientManagerLeft()) {
                    throw new NoClientManagerLeftException(
                            "Removing client manager role of last client manager is not possible");
                }
                userAuthorityDao.remove(found);
                user.getUserAuthorities().remove(found);
                userDao.update(user);
            }
        }
        return user;
    }

    /**
     * {@inheritDoc}
     *
     */
    @Override
    protected void handleResetPermanentId(String systemId) throws AuthorizationException {
        try {
            SecurityHelper.assertCurrentUserIsClientManager();
        } catch (AccessDeniedException e) {
            throw new AuthorizationException("Current user is not client manager", e);
        }
        List<ExternalUserAuthentication> auths = externalUserAuthenticationDao
                .findBySystemId(systemId);
        if (auths != null) {
            for (ExternalUserAuthentication auth : auths) {
                if (auth.getPermanentId() != null) {
                    auth.setPermanentId(null);
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    // TODO move to AuthenticationManagement (or SecurityManagement?)
    @Override
    protected void handleSendNewPWLink(User user)
            throws ExternalUsersMayNotChangeTheirPasswordException {
        String primaryExternalAuthentication = getClientConfigurationProperties()
                .getPrimaryExternalAuthentication();
        if (primaryExternalAuthentication != null) {
            Set<ExternalUserAuthentication> externalExternalUserAuthentications = getExternalExternalUserAuthentications(user
                    .getId());
            for (ExternalUserAuthentication externalUserAuthentication : externalExternalUserAuthentications) {
                if (externalUserAuthentication.getSystemId().equals(primaryExternalAuthentication)) {
                    throw new ExternalUsersMayNotChangeTheirPasswordException(user.getAlias(),
                            primaryExternalAuthentication);
                }
            }
        }
        ForgottenPasswordSecurityCode code = forgottenPasswordSecurityCodeDao.createCode(user);
        ForgottenPWMailMessage message = new ForgottenPWMailMessage(user, code);
        mailManagement.sendMail(message);
    }

    /**
     * {@inheritDoc}
     *
     * @see com.communote.server.core.user.UserManagementBase#handleUnlockUser(long, String)
     */
    // TODO move to AuthenticationManagement (or SecurityManagement?)
    @Override
    protected User handleUnlockUser(String securityCode) throws SecurityCodeNotFoundException {
        UnlockUserSecurityCode code = unlockUserSecurityCodeDao.findBySecurityCode(securityCode);
        // check if code exists
        if (code != null) {
            // remove channel specific entry
            AuthenticationFailedStatus authFailedStatus = authenticationFailedStatusDao
                    .findByUserAndChannel(code.getUser(), code.getChannel());
            authenticationFailedStatusDao.remove(authFailedStatus);
            // delete securityCode
            unlockUserSecurityCodeDao.remove(code);
            return code.getUser();
        } else {
            throw new SecurityCodeNotFoundException("The security code '" + securityCode
                    + "' could not be found");
        }
    }

    @Override
    protected User handleUpdateExternalUser(ExternalUserVO userVO)
            throws AliasAlreadyExistsException, EmailValidationException,
            EmailAlreadyExistsException, UserActivationValidationException,
            InvalidUserStatusTransitionException, PermanentIdMissmatchException {
        User existingUser = internalFindExistingExternalUser(userVO);
        if (existingUser != null) {
            internalUpdateExternalUser(existingUser, userVO);
            return existingUser;
        }
        return null;
    }

    @Override
    protected void handleUpdateLanguage(Long userId, String languageCode) {
        User user = userDao.load(userId);
        if (user != null) {
            user.setLanguageCode(languageCode);
        }
    }

    @PostConstruct
    protected void init() {
        registerActivationValidator(new UserLimitUserActivationValidator(userDao));
        eventDispatcher.register(new UserLimitNotificationOnUserActivation());
    }

    /**
     * Deletes a user by making him anonymous and removing the created UTIs.
     *
     * @param userId
     *            the user to delete
     * @param blogIds
     *            blogIds that were already confirmed by the client manager to handle in some way
     *            (i.e. delete or add oneself as manager). This parameter will be ignored if the
     *            current user is not client manager.
     * @param becomeManager
     *            describes how to handle the confirmed blogs. If set to true the current user will
     *            become manager of these blogs otherwise the blogs will be deleted.
     * @throws NoBlogManagerLeftException
     *             in case some blogs would be left without manager
     */
    private void internalAnonymizeUser(Long userId, Long[] blogIds, boolean becomeManager)
            throws NoBlogManagerLeftException {

        SecurityContext currentContext = null;

        try {
            // execute as internal system
            currentContext = AuthenticationHelper.setInternalSystemToSecurityContext();

            User user = userDao.load(userId);
            if (user == null || user.hasStatus(UserStatus.DELETED)) {
                // user does not exist or has already been deleted
                return;
            }
            UserStatus oldStatus = user.getStatus();

            // check owned blogs for readers and groups for members
            List<Long> deletableBlogs = new ArrayList<>();

            AuthenticationHelper.setSecurityContext(currentContext);

            prepareBlogsForUserDeletion(userId, deletableBlogs, blogIds, becomeManager);

            AuthenticationHelper.setInternalSystemToSecurityContext();

            securityCodeManagement.deleteAllCodesByUser(userId, SecurityCode.class);
            try {
                blogManagement.deleteBlogs(deletableBlogs.toArray(new Long[deletableBlogs.size()]));
            } catch (AuthorizationException e) {
                // throw RTE to force rollback
                throw new UserManagementException("Deleting topics of user to anonymize failed", e);
            }
            // remove user from blogs for performance reasons - do it before changing the profile to
            // have a correct status message in the blog
            try {
                ServiceLocator.findService(UserGroupMemberManagement.class)
                .removeUserFromAllGroups(userId);
                blogRightsManagement.removeUserFromAllBlogs(userId, new ArrayList<Long>(0));
                ServiceLocator.instance().getService(NoteService.class)
                .deleteNotesOfUser(user.getId());
            } catch (AuthorizationException e) {
                LOGGER.error("Deletion of memberships or notes failed because of missing"
                        + " authorization.", e);
                throw new UserManagementException("Anonymizing user failed", e);
            } catch (UserNotFoundException e) {
                LOGGER.error("Deleting user from groups failed because the user was not found.", e);
                throw new UserManagementException(
                        "Anonymizing user failed because he could not be removed from all groups",
                        e);
            }

            user.setEmail(user.getId().toString()
                    + MailMessageHelper.ANONYMOUS_EMAIL_ADDRESS_SUFFIX);
            user.setAlias(ANONYMIZE_USER_PREFIX + user.getId());
            user.setStatus(UserStatus.DELETED);
            userProfileManagement.anonymizeUserProfile(user.getProfile().getId());
            user.setPassword(null);
            user.setReminderMailSent(true);
            // TODO remove ClientManager authorities for better anonymization??
            externalUserAuthenticationDao.remove(user.getExternalAuthentications());
            user.getExternalAuthentications().clear();

            // clean following stuff from this user and all his followers
            for (GlobalId globalId : user.getFollowedItems()) {
                globalId.getFollowers().remove(user);
            }
            user.getFollowedItems().clear();
            user.getGlobalId().getFollowers().clear();

            fireUserStatusChangedEvent(userId, oldStatus, user.getStatus());
        } finally {
            AuthenticationHelper.setSecurityContext(currentContext);
        }
    }

    /**
     * Change the status of the user to the new one
     *
     * @param user
     *            the user to change
     * @param newStatus
     *            the new status
     * @param isChangeByManager
     *            true of the change is done by a manager instead of the user itself
     * @param considerManagerConfirmation
     *            whether to check if the client manager needs to check the status change. Currently
     *            this is only relevant for the status change during user invitation.
     * @throws UserActivationValidationException
     *             in case the user should be activated but one of the validators denied that status
     *             change
     * @throws InvalidUserStatusTransitionException
     *             in case the user status change is not valid
     */
    // TODO do not allow Disabled or similar for last client manager?
    private void internalChangeUserStatus(User user, UserStatus newStatus,
            boolean isChangeByManager, boolean considerManagerConfirmation)
                    throws UserActivationValidationException, InvalidUserStatusTransitionException {
        UserStatus oldStatus = user.getStatus();
        if (oldStatus.equals(newStatus)) {
            return;
        }
        // in case the manager confirmation is not necessary the status change can be treated as if
        // it was triggered by the manager
        validateUserStatusChange(user, newStatus, isChangeByManager || !considerManagerConfirmation);

        boolean confirmedInvitedOrRegistered = UserStatus.CONFIRMED.equals(oldStatus)
                || UserStatus.REGISTERED.equals(oldStatus) || UserStatus.INVITED.equals(oldStatus);
        boolean isSystemUser = UserManagementHelper.isSystemUser(user);
        boolean activationRequested = newStatus.equals(UserStatus.ACTIVE);

        if (!isSystemUser
                && confirmedInvitedOrRegistered
                && activationRequested
                && ClientProperty.TERMS_OF_USE_USERS_MUST_ACCEPT
                .getValue(ClientProperty.DEFAULT_TERMS_OF_USE_USERS_MUST_ACCEPT)) {
            // user has to accept the terms of use before he can be activated
            newStatus = UserStatus.TERMS_NOT_ACCEPTED;
        }
        if (oldStatus.equals(newStatus)) {
            return;
        }

        if (newStatus.equals(UserStatus.ACTIVE)) {
            validateUserActivation(user);
        }
        user.setStatus(newStatus);
        // set time of user status change
        user.setStatusChanged(new Timestamp(new Date().getTime()));

        fireUserStatusChangedEvent(user.getId(), oldStatus, newStatus);

        if (UserStatus.ACTIVE.equals(newStatus)) {
            if (confirmedInvitedOrRegistered || UserStatus.TERMS_NOT_ACCEPTED.equals(oldStatus)) {
                // if user is promoted to ACTIVE the terms were accepted (or did not have to be
                // accepted)
                user.setTermsAccepted(true);
            }
        }
        // if the user should have been activated send some e-mails
        if (activationRequested) {
            if (isSystemUser) {
                user.setReminderMailSent(true);
            } else {
                Collection<MimeMessagePreparator> mails = new ArrayList<MimeMessagePreparator>();
                if (confirmedInvitedOrRegistered) {
                    // reset reminder mail flag to inform the user that he did not login
                    user.setReminderMailSent(false);
                }
                // TODO isRegistrationUserNotificationDeactivated is not correct as it does not
                // check whether the user is actually internal or external
                if (isChangeByManager
                        && (confirmedInvitedOrRegistered || UserStatus.TEMPORARILY_DISABLED
                                .equals(oldStatus)) && !isRegistrationUserNotificationDeactivated()) {
                    // if the manager activated the user for the first time or after he was
                    // temporarily disabled inform the user
                    mails.add(new ActivateUserMailMessage(user, confirmedInvitedOrRegistered));
                }
                if (!UserStatus.TERMS_NOT_ACCEPTED.equals(oldStatus)) {
                    // inform the managers about the activation, but not if the user did just accept
                    // the terms of use
                    mails.addAll(createActivateUserForManagerMailMessage(user,
                            confirmedInvitedOrRegistered));
                }
                // send mails
                sendMail(mails);
            }
        }
    }

    /**
     * Creates a new user from external user data.
     *
     * @param userVO
     *            the external user data
     * @return the created user
     * @throws EmailValidationException
     *             the email is not an email
     * @throws EmailAlreadyExistsException
     *             the email already exists
     * @throws AliasAlreadyExistsException
     *             if the alias already exists.
     */
    private User internalCreateExternalUser(ExternalUserVO userVO) throws EmailValidationException,
    EmailAlreadyExistsException, AliasAlreadyExistsException {
        // set some default values
        if (userVO.getRoles() == null) {
            userVO.setRoles(new UserRole[] { UserRole.ROLE_KENMEI_USER });
        }
        if (userVO.getLanguage() == null) {
            userVO.setLanguage(Locale.ENGLISH);
        }
        // create alias if necessary
        if (StringUtils.isBlank(userVO.getAlias())) {
            userVO.setAlias(generateUniqueAlias(userVO.getExternalUserName(), userVO.getEmail()));
        }
        // no password validation required
        User user = internalCreateUser(userVO, false, isManagerActivationRequired());
        internalCreateOrUpdateExternalAuthentication(user, userVO);
        updateUserProperties(user, userVO);
        Map<String, String> taskProperties = new HashMap<String, String>();
        taskProperties.put(SynchronizeExternalUserTaskHandler.PROPERTY_USER_ALIAS, user.getAlias());
        try {
            taskManagement.addTask("syncExternalUserOnFirstLogin" + user.getId(), true, null,
                    new Date(), taskProperties, SynchronizeExternalUserTaskHandler.class);
        } catch (TaskAlreadyExistsException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return user;
    }

    /**
     * Attaches a new external authentication to a user or if already contained updates the
     * existing.
     *
     * @param user
     *            the user to modify
     * @param userVO
     *            the VO holding the details of the external authentication
     */
    private void internalCreateOrUpdateExternalAuthentication(User user, ExternalUserVO userVO) {

        ExternalUserAuthentication externalAuth = null;
        Set<ExternalUserAuthentication> externalAuths = user.getExternalAuthentications();
        if (externalAuths == null) {
            externalAuths = new HashSet<ExternalUserAuthentication>();
            user.setExternalAuthentications(externalAuths);
        } else {
            externalAuth = getExternalAuthentication(user, userVO.getSystemId());
        }
        boolean create = false;
        if (externalAuth == null) {
            create = true;
            externalAuth = ExternalUserAuthentication.Factory.newInstance();
            externalAuth.setExternalUserId(userVO.getExternalUserName());
            externalAuth.setSystemId(userVO.getSystemId());
        }
        externalAuth.setAdditionalProperty(userVO.getAdditionalProperty());
        externalAuth.setPermanentId(userVO.getPermanentId());
        if (create) {
            externalAuth = externalUserAuthenticationDao.create(externalAuth);
            externalAuths.add(externalAuth);
        } else {
            // only update permanentID if changed
            if (externalAuth.getPermanentId() == null) {
                externalAuth.setPermanentId(userVO.getPermanentId());
            } else {
                externalAuth.setExternalUserId(userVO.getExternalUserName());
            }
        }

    }

    /**
     * Create a user from the provided data.
     *
     * @param user
     *            the user to create
     * @param emailConfirmationRequired
     *            whether the user should confirm the user data. If true the user will be created
     *            with status REGISTERED and an e-mail is sent to the user. If false the user is
     *            CONFIRMED or if managerActivationRequired is true the user is tried to be
     *            activated.
     * @param managerActivationRequired
     *            whether a client manager needs to activate the user manually. If false the user
     *            will have status ACTIVE or TERMS_NOT_ACCEPTED. If true the client managers will be
     *            informed.
     * @return the created user
     * @throws EmailValidationException
     *             in case the e-mail is not a valid e-mail address
     * @throws EmailAlreadyExistsException
     *             in case the e-mail address already exists
     * @throws AliasAlreadyExistsException
     *             in case the alias already exists.
     */
    // TODO this method duplicates logic with confirmUser, invite methods and
    // internalChangeUserStatus. Is there a way to merge it?
    private User internalCreateUser(UserVO userVo, boolean emailConfirmationRequired,
            boolean managerActivationRequired) throws EmailValidationException,
            EmailAlreadyExistsException, AliasAlreadyExistsException {
        validateUserForCreation(userVo);
        User user = userDao.userVOToEntity(userVo);
        // create the entities
        userAuthorityDao.create(user.getUserAuthorities());
        UserProfile profile = user.getProfile();
        // create profile if missing
        if (profile == null) {
            profile = UserProfile.Factory.newInstance();
            profile.setLastModificationDate(new Timestamp(new Date().getTime()));
            user.setProfile(profile);
        }
        // add notification config
        if (profile.getNotificationConfig() == null) {
            NotificationConfig notificationConfig = NotificationConfig.Factory.newInstance();
            notificationConfig.setFallback("mail");
            profile.setNotificationConfig(notificationConfig);
            notificationConfigDao.create(notificationConfig);
            userDao.createMailNotificationConfig(profile.getNotificationConfig().getId());
        }
        if (user.getProfile().getId() == null) {
            userProfileDao.create(user.getProfile());
        }
        // authentication
        if (user.getExternalAuthentications() != null) {
            externalUserAuthenticationDao.create(user.getExternalAuthentications());
        }
        user.setStatusChanged(new Timestamp(new Date().getTime()));
        user.setReminderMailSent(false);
        user.setStatus(UserStatus.REGISTERED);
        user.setTermsAccepted(false);
        user = userDao.create(user);

        // check if the status can be promoted
        // TODO this duplicates code of internalChangeUserStatus and userConfirmed, but is necessary
        // because the other methods send a lot of e-mails which should for instance not be sent
        // when an external user is activated
        if (!emailConfirmationRequired) {
            // if the user should not confirm the data, proceed to CONFIRMED or if possible to
            // ACTIVE.
            if (managerActivationRequired) {
                user.setStatus(UserStatus.CONFIRMED);
            } else {
                if (ClientProperty.TERMS_OF_USE_USERS_MUST_ACCEPT
                        .getValue(ClientProperty.DEFAULT_TERMS_OF_USE_USERS_MUST_ACCEPT)) {
                    user.setStatus(UserStatus.TERMS_NOT_ACCEPTED);
                } else {
                    try {
                        validateUserActivation(user);
                        user.setStatus(UserStatus.ACTIVE);
                        // user does not have to accept the terms during creation -> mark that he
                        // accepted the current terms so it is possible to reset this flag
                        // intentionally
                        // for active users when terms are changed
                        user.setTermsAccepted(true);
                    } catch (UserActivationValidationException e) {
                        LOGGER.debug("User could not be activated automatically: {}",
                                e.getMessage());
                        user.setStatus(UserStatus.CONFIRMED);
                    }
                }
            }
        }

        fireUserStatusChangedEvent(user.getId(), null, user.getStatus());

        Collection<MimeMessagePreparator> mailBuffer = new ArrayList<MimeMessagePreparator>();
        if (UserStatus.REGISTERED.equals(user.getStatus())) {
            UserSecurityCode code = userSecurityCodeDao.createCode(user);
            mailBuffer.add(new ConfirmUserMailMessage(user, code, RegistrationType.SELF));
        } else if (UserStatus.CONFIRMED.equals(user.getStatus())) {
            mailBuffer.addAll(createNewUserConfirmedForManagerMail(user));
        }
        sendMail(mailBuffer);
        return user;
    }

    /**
     * Tries to find a User by it's associated external authentication.
     *
     * @param userVO
     *            the VO containing the information of the external authentication
     * @return the found user or null
     * @throws PermanentIdMissmatchException
     *             if the user was not found by the permanentId but by the externalUserName and has
     *             a permanentId
     */
    private User internalFindExistingExternalUser(ExternalUserVO userVO)
            throws PermanentIdMissmatchException {
        User existingUser = null;
        // first check for permanent ID
        if (userVO.getPermanentId() != null) {
            Long userId = externalUserAuthenticationDao.findUserByPermanentId(userVO.getSystemId(),
                    userVO.getPermanentId());
            existingUser = userId == null ? null : userDao.load(userId);
        }
        if (existingUser == null) {
            existingUser = userDao.findByExternalUserId(userVO.getExternalUserName(),
                    userVO.getSystemId());
            if (existingUser != null) {
                // must check whether permanent id for external system is null, if not the update
                // would be wrong
                ExternalUserAuthentication auth = getExternalAuthentication(existingUser,
                        userVO.getSystemId());
                if (auth.getPermanentId() != null
                        && !auth.getPermanentId().equals(userVO.getPermanentId())) {
                    throw new PermanentIdMissmatchException("PermanentId ("
                            + userVO.getPermanentId() + ") of exisiting user (" + userVO.getAlias()
                            + ") does not match provided ID (" + auth.getPermanentId() + ")");
                }
            }
        }
        return existingUser;
    }

    /**
     * Handles blogs that would become manager-less during user deletion.
     *
     * @param blogIdsToHandle
     *            the IDs to handle in some way
     * @param blogIdsTitleMap
     *            a mapping between blogId and title
     * @param becomeManager
     *            whether the current user should become manager of the blogs or the blogs should be
     *            deleted
     */
    private void internalHandleBlogsDuringUserDeletion(List<Long> blogIdsToHandle,
            Map<Long, String> blogIdsTitleMap, boolean becomeManager) {
        if (blogIdsToHandle.size() == 0) {
            return;
        }
        // handle the blogsToHandle accordingly
        try {
            if (becomeManager) {
                for (Long blogId : blogIdsToHandle) {
                    blogRightsManagement.assignManagementAccessToCurrentUser(blogId);
                }
            } else {
                blogManagement.deleteBlogs(blogIdsTitleMap.keySet().toArray(
                        new Long[blogIdsToHandle.size()]));
            }
        } catch (AuthorizationException e) {
            // unexpected because current user is client when reaching this part
            LOGGER.error("Unexpected exception while deleting blogs as client manager.", e);
            throw new UserManagementException("Unexpected exception.", e);
        } catch (BlogNotFoundException e) {
            throw new UserManagementException("Unexpected exception.", e);
        }
    }

    /**
     * Invite a user, that is add a new user with status INVITED, if he does not yet exist. If the
     * user to create is an external user it will be created in status CONFIRMED or if automatic
     * activation is enabled in status ACTIVE.
     *
     * @param userVo
     *            VO with details describing the user
     * @param freshSecurityCode
     *            a prepared new security code that will be augmented with the new user if the user
     *            does not yet exist
     * @param codeDao
     *            the DAO to store the fresh security code
     * @param mails
     *            buffer for adding mails that should be send afterwards
     * @return a pair containing the new or existing user and the security code. The security code
     *         can be the provided code, an existing code of the user to create if he was already
     *         invited or null if no code is necessary (external users).
     * @throws EmailAlreadyExistsException
     *             in case the provided email address already belongs to an existing user
     * @throws AliasAlreadyExistsException
     *             in case the provided alias address already belongs to an existing user
     * @throws PermanentIdMissmatchException
     *             in case there is an existing user with same email address but differen permanent
     *             ID
     * @throws EmailValidationException
     *             in case the provided email address is invalid
     */
    private <T extends SecurityCode> Pair<User, T> internalInviteUser(UserVO userVo,
            T freshSecurityCode, InviteSecurityCodeDao<T> codeDao, List<MimeMessagePreparator> mails)
            throws EmailAlreadyExistsException, AliasAlreadyExistsException,
            PermanentIdMissmatchException, EmailValidationException {
        User user = validateUserVoForInvitation(userVo);
        UserStatus status;
        boolean externalAuth;
        if (userVo instanceof ExternalUserVO) {
            externalAuth = true;
            // when inviting an external user we know all the details about the user and we
            // trust the external system. Thus there is no need for confirming the e-mail address.
            status = UserStatus.CONFIRMED;
        } else {
            status = UserStatus.INVITED;
            externalAuth = false;
        }

        T invitationSecurityCode;

        if (user == null) {
            userVo.setRoles(new UserRole[] { UserRole.ROLE_KENMEI_USER });
            user = userDao.userVOToEntity(userVo);
            user.setTermsAccepted(false);
            user.setStatus(status);
            user.setStatusChanged(new Timestamp(new Date().getTime()));
            user.setReminderMailSent(false);
            user = userDao.create(user);
            if (externalAuth) {
                internalCreateOrUpdateExternalAuthentication(user, (ExternalUserVO) userVo);
                // external users are confirmed and thus might be activated automatically. No
                // security code is needed.
                invitationSecurityCode = null;
                userConfirmed(user, true, mails);
            } else {
                freshSecurityCode.setUser(user);
                invitationSecurityCode = codeDao.create(freshSecurityCode);
            }
            fireUserStatusChangedEvent(user.getId(), null, user.getStatus());
        } else {
            if (user.hasStatus(UserStatus.INVITED) && !externalAuth) {
                // invited user exists, send the code again
                invitationSecurityCode = codeDao.findByUser(user.getId());
                if (invitationSecurityCode != null) {
                    // update existing user
                    userDao.userVOToEntity(userVo, user, false);
                    // set invited time to current time
                    user.setStatusChanged(new Timestamp(new Date().getTime()));
                    userDao.update(user);
                } else {
                    // invited by user invite to client?
                    throw new EmailAlreadyExistsException("user with this email already exists");
                }
            } else if (externalAuth) {
                // try attaching the external authentication and return
                internalCreateOrUpdateExternalAuthentication(user, (ExternalUserVO) userVo);
                // special case: user was registered with an email from the external auth while
                // external auth was deactivated or he was not yet in repo -> can be treated like a
                // new invitation
                if (user.hasStatus(UserStatus.REGISTERED)) {
                    userVo.setRoles(null);
                    userDao.userVOToEntity(userVo, user, false);
                    user.setStatus(status);
                    user.setStatusChanged(new Timestamp(new Date().getTime()));
                    // is confirmed directly so no code needed
                    invitationSecurityCode = null;
                    userConfirmed(user, true, mails);
                    fireUserStatusChangedEvent(user.getId(), null, user.getStatus());
                } else {
                    // let caller know that user exists
                    throw new AliasAlreadyExistsException("external user has already been invited");
                }
            } else {
                throw new EmailAlreadyExistsException("user with this email already exists");
            }
        }
        return new Pair<>(user, invitationSecurityCode);
    }

    /**
     * Deletes a user by disabling the account, but keeping all created data.
     *
     * @param userId
     *            the user to delete
     * @param blogIds
     *            blogIds that were already confirmed by the client manager to handle in some way
     *            (i.e. delete or add oneself as manager). This parameter will be ignored if the
     *            current user is not client manager.
     * @param becomeManager
     *            describes how to handle the confirmed blogs. If set to true the current user will
     *            become manager of these blogs otherwise the blogs will be deleted.
     * @throws InvalidUserStatusTransitionException
     *             in case the user has status DELETED
     * @throws NoBlogManagerLeftException
     *             in case some groups would be left without manager and newManagerUserId does not
     *             reference an active user
     */
    private void internalPermanentlyDisableUser(Long userId, Long[] blogIds, boolean becomeManager)
            throws InvalidUserStatusTransitionException, NoBlogManagerLeftException {
        User user = userDao.load(userId);
        if (user == null || UserStatus.PERMANENTLY_DISABLED.equals(user.getStatus())) {
            // not existing or already disabled users are ignored
            return;
        }
        // it's not possible to disable a user who is already deleted
        if (UserStatus.DELETED.equals(user.getStatus())) {
            throw new InvalidUserStatusTransitionException(UserStatus.DELETED,
                    UserStatus.PERMANENTLY_DISABLED, user);
        }
        UserStatus oldStatus = user.getStatus();
        // check owned blogs for readers
        List<Long> deletableBlogs = new ArrayList<Long>();
        prepareBlogsForUserDeletion(userId, deletableBlogs, blogIds, becomeManager);
        user.setStatus(UserStatus.PERMANENTLY_DISABLED);
        user.setPassword(null);
        user.setEmail(user.getId().toString() + MailMessageHelper.ANONYMOUS_EMAIL_ADDRESS_SUFFIX);
        securityCodeManagement.deleteAllCodesByUser(userId, SecurityCode.class);
        try {
            ServiceLocator.findService(UserGroupMemberManagement.class).removeUserFromAllGroups(
                    userId);
            // the deletableBlogs will not be deleted (because user data must be kept) but become
            // manager-less
            blogRightsManagement.removeUserFromAllBlogs(userId, deletableBlogs);
        } catch (AuthorizationException e) {
            throw new UserManagementException("Removing blog memberships failed.", e);
        } catch (UserNotFoundException e) {
            LOGGER.error("Deleting user from groups failed because the user was not found.", e);
            throw new UserManagementException(
                    "Deleting user from groups failed because the user was not found.", e);
        }

        // clean following stuff
        for (GlobalId globalId : user.getFollowedItems()) {
            globalId.getFollowers().remove(user);
        }
        user.getFollowedItems().clear();
        fireUserStatusChangedEvent(userId, oldStatus, user.getStatus());
    }

    /**
     * Updates an existing external user.
     *
     * @param existingUser
     *            the user to update
     * @param userVO
     *            the data to update with. Only non-null values will be considered.
     * @throws AliasAlreadyExistsException
     *             when the new alias is already in use
     * @throws EmailValidationException
     *             when the new email cannot be validated
     * @throws EmailAlreadyExistsException
     *             when the new email is already in use
     * @throws InvalidUserStatusTransitionException
     *             if the user status could not be changed
     * @throws UserActivationValidationException
     *             in case the user could not be activated
     */
    private void internalUpdateExternalUser(User existingUser, ExternalUserVO userVO)
            throws AliasAlreadyExistsException, EmailValidationException,
            EmailAlreadyExistsException, InvalidUserStatusTransitionException,
            UserActivationValidationException {
        if (!userVO.isUpdatePassword()) {
            userVO.setPassword(null);
        }
        if (!userVO.isUpdateFirstName()) {
            userVO.setFirstName(null);
        }
        if (!userVO.isUpdateLastName()) {
            userVO.setLastName(null);
        }
        if (!userVO.isUpdateLanguage()) {
            userVO.setLanguage(null);
        }
        if (!userVO.isUpdateEmail()) {
            userVO.setEmail(null);
        } else {
            if (userVO.getEmail() != null) {
                validateEmail(userVO.getEmail(), existingUser, true);
            }
        }
        // update alias if not yet set (might be the case if the user to update is an internal
        // user with status REGISTERED)
        if (existingUser.getAlias() == null) {
            if (StringUtils.isBlank(userVO.getAlias())) {
                userVO.setAlias(generateUniqueAlias(userVO.getExternalUserName(), userVO.getEmail()));
            } else {
                validateAlias(userVO.getAlias(), existingUser);
            }
        }
        userDao.userVOToEntity(userVO, existingUser, false);
        if (userVO.isUpdatePassword()) {
            existingUser.setPassword(null);
        }
        // check for status change but avoid changing from TERMS_NOT_ACCEPTED to ACTIVE without user
        // interaction
        if (userVO.getStatus() != null
                && !existingUser.getStatus().equals(userVO.getStatus())
                && !(UserStatus.ACTIVE.equals(userVO.getStatus()) && UserStatus.TERMS_NOT_ACCEPTED
                        .equals(existingUser.getStatus()))) {
            internalChangeUserStatus(existingUser, userVO.getStatus(), false, true);
        }
        internalCreateOrUpdateExternalAuthentication(existingUser, userVO);
        updateUserProperties(existingUser, userVO);
    }

    /**
     * Updates the tags of the given user.
     *
     * @param user
     *            The user.
     * @param tags
     *            The tags of the user.
     */
    private void internalUpdateUserTags(User user, Set<TagTO> tags) {
        if (user.getTags() == null) {
            user.setTags(new HashSet<Tag>());
        }
        user.getTags().clear();
        if (tags != null) {
            for (TagTO tagTO : tags) {
                try {
                    Tag tag = tagManagement.storeTag(tagTO);
                    user.getTags().add(tag);
                } catch (TagNotFoundException e) {
                    LOGGER.error("A tag was not found or couldn't be created: {}", e.getMessage());
                } catch (TagStoreNotFoundException e) {
                    LOGGER.error("A tag store was not found: {}", e.getMessage());
                }
            }
        }
        userDao.update(user);
    }

    /**
     * @return true if the manager needs to activate an user account manually
     */
    private boolean isManagerActivationRequired() {
        return !getClientConfigurationProperties().getProperty(
                ClientProperty.AUTOMATIC_USER_ACTIVATION,
                ClientConfigurationHelper.DEFAULT_AUTOMATIC_USER_ACTIVATION);
    }

    /**
     * @return true if there is an activated external authentication and sending of user
     *         notifications (e.g. user got activated) in the registration process is deactivated.
     */
    private boolean isRegistrationUserNotificationDeactivated() {
        ClientConfigurationProperties props = getClientConfigurationProperties();
        return props.getProperty(
                ClientProperty.NO_REGISTRATION_USER_NOTIFY_EMAILS_WHEN_EXTERNAL_AUTH, false)
                && props.isExternalAuthenticationActivated();
    }

    /**
     * Returns whether there is only one active client manager.
     *
     * @return true if there is only one client manager
     */
    private boolean onlyOneClientManagerLeft() {
        long managers = 0;
        ClientConfigurationProperties props = getClientConfigurationProperties();
        String primaryExternalAuthentication = props.getPrimaryExternalAuthentication();
        if (primaryExternalAuthentication != null) {
            managers = getActiveUserCount(primaryExternalAuthentication,
                    UserRole.ROLE_KENMEI_CLIENT_MANAGER);
        }
        if (primaryExternalAuthentication == null
                || props.getProperty(ClientPropertySecurity.ALLOW_DB_AUTH_ON_EXTERNAL,
                        ClientPropertySecurity.DEFAULT_ALLOW_DB_AUTH_ON_EXTERNAL)) {
            managers += getActiveUserCount(null, UserRole.ROLE_KENMEI_CLIENT_MANAGER);
        }
        return managers <= 1;
    }

    /**
     * Gets all blogs managed by a user and tests whether there are blogs that can be deleted or
     * blogs that would be left without manager if this user is deleted. In case the current user is
     * client manager and has already confirmed how to handle all the blogs becoming manager-less,
     * this action will also be performed.
     *
     * @param managerUserId
     *            the user whose managed groups will be examined
     * @param deletableBlogs
     *            stores all blogs (their IDs) that can be deleted because the only member/reader is
     *            the user with managerUserId
     * @param confirmedBlogIds
     *            blogIds that were already confirmed by the client manager to handle in some way
     *            (i.e. delete or add oneself as manager). This parameter will be ignored if the
     *            current user is not client manager.
     * @param becomeManager
     *            describes how to handle the confirmed blogs after checking whether there are
     *            new/less blogs managed by the user. If set to true the current user will become
     *            manager of these blogs otherwise the blogs will be deleted.
     * @throws NoBlogManagerLeftException
     *             in case some blogs would be left without manager and are not listed in
     *             confirmedBlogIds
     */
    private void prepareBlogsForUserDeletion(Long managerUserId, List<Long> deletableBlogs,
            Long[] confirmedBlogIds, boolean becomeManager) throws NoBlogManagerLeftException {
        Map<Long, String> blogIdsTitleMap = new HashMap<>();
        List<Long> blogIdsToHandle = new ArrayList<>();
        // if current user is manager and the user to be deleted
        if (SecurityHelper.isClientManager()
                && !managerUserId.equals(SecurityHelper.getCurrentUserId())) {
            testForDeletableBlogs(managerUserId, deletableBlogs, blogIdsTitleMap, blogIdsToHandle,
                    confirmedBlogIds);
        } else {
            testForDeletableBlogs(managerUserId, deletableBlogs, blogIdsTitleMap, blogIdsToHandle,
                    null);
        }
        if (blogIdsTitleMap.size() > 0 && blogIdsTitleMap.size() != blogIdsToHandle.size()) {
            throw new NoBlogManagerLeftException(
                    "User cannot be deleted because there would be some blogs without manager.",
                    blogIdsTitleMap);
        }
        internalHandleBlogsDuringUserDeletion(blogIdsToHandle, blogIdsTitleMap, becomeManager);
    }

    @Override
    public synchronized void registerActivationValidator(UserActivationValidator validator) {
        ArrayList<UserActivationValidator> validators = new ArrayList<>(
                this.userActivationValidators);
        validators.add(validator);
        Collections.sort(validators, new DescendingOrderComparator());
        this.userActivationValidators = validators;
    }

    @Override
    public void resetTermsOfUse() throws AuthorizationException {
        Long currentUserId = SecurityHelper.getCurrentUserId();
        if (SecurityHelper.isClientManager() || SecurityHelper.isInternalSystem()) {
            userDao.resetTermsAccepted(currentUserId);
        } else {
            throw new AuthorizationException("Current user " + currentUserId
                    + " is not allowed to reset the terms of use");
        }
    }

    /**
     * Send mail.
     *
     * @param mails
     *            the mails
     */
    private void sendMail(Collection<MimeMessagePreparator> mails) {
        if (CollectionUtils.isNotEmpty(mails)) {
            for (MimeMessagePreparator mail : mails) {
                mailManagement.sendMail(mail);
            }
        }
    }

    // TODO split up in too methods to avoid out of memory.
    @Override
    public void sendReminderMails() {

        // ignore if registration e-mails are deactivated
        if (isRegistrationUserNotificationDeactivated()) {
            return;
        }

        long remindTime = getClientConfigurationProperties().getProperty(
                ClientProperty.REMIND_USER_TIME, DEFAULT_REMIND_USER_TIME);
        Date before = new Date(new Date().getTime() - remindTime);

        Collection<MimeMessagePreparator> mails = new ArrayList<MimeMessagePreparator>();
        List<String> emailAdressList = new ArrayList<String>();

        // users, which are registered but have not confirmed their email in a given period of time
        // finds all REGISTERED and INVITED users!
        // TODO rename to findRegisteredUser and only return users in status registered
        List<User> userlist = userDao.findNotConfirmedUser(before, false);
        if (CollectionUtils.isNotEmpty(userlist)) {
            for (User user : userlist) {

                if (UserStatus.REGISTERED.equals(user.getStatus())) {
                    UserSecurityCode userSecurityCode = userSecurityCodeDao.findByUser(
                            user.getId(), SecurityCodeAction.CONFIRM_USER);
                    mails.add(new RemindUserRegistrationMailMessage(user, userSecurityCode));
                    emailAdressList.add(user.getEmail());
                    user.setReminderMailSent(true);
                }
            }
            userDao.update(userlist);
        }

        // user which are active but did not log in, include TERMS_NOT_ACCEPTED users if terms need
        // to be accepted
        userlist = userDao.findNotLoggedInActiveUser(before, false,
                ClientProperty.TERMS_OF_USE_USERS_MUST_ACCEPT
                .getValue(ClientProperty.DEFAULT_TERMS_OF_USE_USERS_MUST_ACCEPT));
        if (CollectionUtils.isNotEmpty(userlist)) {
            for (User user : userlist) {
                mails.add(new RemindUserLoginMailMessage(user));
                emailAdressList.add(user.getEmail());
                user.setReminderMailSent(true);
            }
            userDao.update(userlist);
        }

        if (CollectionUtils.isNotEmpty(mails)) {
            LOGGER.debug("send {} reminder mails to {}", mails.size(), emailAdressList);
            sendMail(mails);
        }
    }

    /**
     * Adds a default locale to the VO if non is contained.
     *
     * @param externalUserVO
     *            VO for the external user
     */
    private void setLanguageIfNotExist(ExternalUserVO externalUserVO) {
        if (externalUserVO.getLanguage() == null) {
            if (externalUserVO.getDefaultLanguage() != null) {
                externalUserVO.setLanguage(externalUserVO.getDefaultLanguage());
            } else {
                externalUserVO.setLanguage(ClientHelper.getDefaultLanguage());
            }
        }
    }

    /**
     * Analyzes all blogs managed by a user whether they can be deleted or not.
     *
     * @param managerUserId
     *            the ID of the user whose managed groups will be examined
     * @param blogsToDelete
     *            stores all blogs (their IDs) that can be deleted because the only member/reader is
     *            the user with managerUserId
     * @param undeletableBlogs
     *            stores a mapping from blog ID to blog title of the blogs that cannot be deleted
     *            because other users are using them and there is no other manager
     * @param blogIdsToHandle
     *            stores all blog IDs of undeletableBlogs that were already confirmed by the client
     *            manager to handle in some way (i.e. delete or add oneself as manager)
     * @param confirmedBlogIds
     *            blogIds that were already confirmed by the client manager to handle in some way
     *            (i.e. delete or add oneself as manager); can be null
     */
    private void testForDeletableBlogs(Long managerUserId, List<Long> blogsToDelete,
            Map<Long, String> undeletableBlogs, List<Long> blogIdsToHandle, Long[] confirmedBlogIds) {
        List<Blog> managedBlogs = ServiceLocator.findService(BlogDao.class)
                .findDirectlyManagedBlogsOfUser(managerUserId);
        for (Blog managedBlog : managedBlogs) {
            if (!blogRightsManagement.hasAnotherManager(managedBlog.getId(), managerUserId)) {
                if (blogRightsManagement.hasAnotherReader(managedBlog.getId(), managerUserId)) {
                    if (ArrayUtils.contains(confirmedBlogIds, managedBlog.getId())) {
                        blogIdsToHandle.add(managedBlog.getId());
                    }
                    undeletableBlogs.put(managedBlog.getId(), managedBlog.getTitle());
                } else {
                    // TODO test whether there are notes of other users (so we would cover all
                    // blogs with notes written by others and readable by others)??
                    blogsToDelete.add(managedBlog.getId());
                }
            }
        }
    }

    /**
     * Tests whether merging an existing user with an external user with the same email address is
     * possible. Merging is possible if the existing user does not yet have an external
     * authentication with the same system ID (and a differing external user name) and the communote
     * alias must match the external user name.
     *
     * @param user
     *            the existing user
     * @param userVO
     *            the external user data
     * @return true if a merge is necessary, false otherwise
     * @throws EmailAlreadyExistsException
     *             when merging is not possible
     */
    private boolean testMergingExternalUserWithExistingUser(User user, ExternalUserVO userVO)
            throws EmailAlreadyExistsException {
        // test if merging the user with the external user is possible
        ExternalUserAuthentication auth = getExternalAuthentication(user, userVO.getSystemId());
        if (auth != null) {
            if (auth.getExternalUserId().equals(userVO.getExternalUserName())) {
                return false;
            }
            throw new EmailAlreadyExistsException("External user '" + user.getAlias()
                    + "' cannot be merged with existing user because the user already "
                    + "has an authentication for the external system.");
        }
        String alias = user.getAlias();
        // alias can be null for registered users, in that case merging is possible
        if (alias != null) {
            boolean compareLowerCase = ClientProperty.COMPARE_EXTERNAL_USER_ALIAS_LOWERCASE
                    .getValue(true);
            String externalUserName = userVO.getExternalUserName();
            boolean equals = compareLowerCase ? alias.equalsIgnoreCase(externalUserName) : alias
                    .equals(externalUserName);
            if (!equals) {
                throw new EmailAlreadyExistsException("External user '" + user.getAlias()
                        + "' cannot be merged with existing user '" + userVO.getExternalUserName()
                        + "' because external user id and alias of existing user are not equal.");
            }
        }
        return true;
    }

    @Override
    public synchronized void unregisterActivationValidator(UserActivationValidator validator) {
        ArrayList<UserActivationValidator> validators = new ArrayList<>(
                this.userActivationValidators);
        if (validators.remove(validator)) {
            Collections.sort(validators, new DescendingOrderComparator());
            this.userActivationValidators = validators;
        }
    }

    /**
     * Update the properties of a user.
     *
     * @param user
     *            the user to update
     * @param userVO
     *            the value object holding the user properties
     */
    private void updateUserProperties(User user, ExternalUserVO userVO) {
        if (userVO.getProperties() != null) {
            try {
                propertyManagement.setObjectProperties(PropertyType.UserProperty, user.getId(),
                        userVO.getProperties());
            } catch (AuthorizationException | NotFoundException e) {
                LOGGER.error(
                        "Unexpected exception while updating properties user with id "
                                + user.getId(), e);
                throw new UserManagementException("Unexpected exception.", e);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateUserTags(Long userId, Set<TagTO> tags) {
        User user = userDao.load(userId);
        internalUpdateUserTags(user, tags);
    }

    /**
     * Should be called after a user was confirmed. Tries to activate a user automatically or if not
     * possible creates the e-mails to inform the client managers to activate the user. If the
     * provided user is not CONFIRMED, nothing will happen.
     *
     * @param user
     *            the user
     * @param considerManagerActivation
     *            if false the configuration setting which defines whether an automatic activation
     *            is possible will not be checked and the user will be activated. This could for
     *            instance be useful if the admin invited the user.
     * @param mailBuffer
     *            collection to add additional e-mails that should be sent in from within the
     *            calling method
     */
    private void userConfirmed(User user, boolean considerManagerActivation,
            Collection<MimeMessagePreparator> mailBuffer) {

        if (user.hasStatus(UserStatus.CONFIRMED)) {
            if (considerManagerActivation && isManagerActivationRequired()) {
                mailBuffer.addAll(createNewUserConfirmedForManagerMail(user));
            } else {
                try {
                    internalChangeUserStatus(user, UserStatus.ACTIVE, false,
                            considerManagerActivation);
                } catch (UserActivationValidationException e) {
                    LOGGER.info("Cannot activate user ({}): {}" + user.getAlias(), e.getMessage());
                    if (user.hasStatus(UserStatus.CONFIRMED)) {
                        mailBuffer.addAll(createNewUserConfirmedForManagerMail(user));
                    }
                } catch (InvalidUserStatusTransitionException e) {
                    // unexpected exception because transition from CONFIRMED should be possible
                    LOGGER.error("Unexpected exception trying to activate user with ID {}",
                            user.getId());
                    // force roll-back with RTE
                    throw new UserManagementException(
                            "Unexpected exception trying to activate CONFIRMED user");
                }
            }
        }

    }

    /**
     * Checks the alias if it is valid and if a user with the given does not already exists.
     *
     * @param alias
     *            the alias
     * @param user
     *            the user which should be updated with the new alias (optional)
     * @throws AliasAlreadyExistsException
     *             the alias already exists exception
     * @throws UserManagementException
     *             if alias is empty
     */
    private void validateAlias(String alias, User user) throws AliasAlreadyExistsException {
        if (StringUtils.isBlank(alias)) {
            // TODO use checked exception ?
            throw new UserManagementException(
                    "The alias of the user can not be null, empty or blank");
        }
        User foundUser = userDao.findByAlias(alias);
        if (foundUser != null
                && (user == null || !foundUser.getId().equals(user.getId())
                        && foundUser.getAlias().equalsIgnoreCase(alias))) {
            throw new AliasAlreadyExistsException("The user alias with alias '" + alias
                    + "'already exists");
        }
    }

    /**
     * Check an email address whether it is valid and unique. Unique means there is no other user
     * with that address.
     *
     * @param email
     *            email address
     * @param userForUpdate
     *            user to assign the new email address to, can be null
     * @param checkForExistingUser
     *            true to check if there exists another user with the same email
     * @throws EmailValidationException
     *             email is invalid
     * @throws EmailAlreadyExistsException
     *             email already exists, is not unique
     */
    private void validateEmail(String email, User userForUpdate, boolean checkForExistingUser)
            throws EmailValidationException, EmailAlreadyExistsException {
        if (StringUtils.isBlank(email)) {
            throw new EmailValidationException(
                    "The email address of the user can not be null, empty or blank");
        }
        if (!EmailValidator.validateEmailAddressByRegex(email)) {
            throw new EmailValidationException("The email address '" + email + "' is invalid");
        }
        if (checkForExistingUser) {
            User foundUser = userDao.findByEmail(email);
            // already exists if a user exists with this email and the found user
            // has another id as the given user
            if (foundUser != null
                    && (userForUpdate == null || !foundUser.getId().equals(userForUpdate.getId())
                    && foundUser.getEmail().equalsIgnoreCase(email))) {
                throw new EmailAlreadyExistsException("The email address '" + email
                        + "' already exists");
            }
        }
    }

    /**
     * Validates the password
     *
     * @param newPassword
     *            the password
     * @param userRoles
     *            the roles of the user. If the roles indicate that the user is a system user the
     *            password is not checked
     * @throws PasswordLengthException
     *             the password length exception
     */
    private void validatePassword(String newPassword, UserRole[] userRoles)
            throws PasswordLengthException {
        boolean isSystemUser = false;
        for (UserRole role : userRoles) {
            if (UserRole.ROLE_SYSTEM_USER.equals(role)) {
                isSystemUser = true;
                break;
            }
        }
        if (!isSystemUser) {
            if (StringUtils.isEmpty(newPassword) || newPassword.length() < 6) {
                throw new PasswordLengthException("Password has less than 6 characters");
            }
        }
    }

    private void validateUserActivation(User user) throws UserActivationValidationException {
        for (UserActivationValidator validator : userActivationValidators) {
            validator.validateUserActivation(user);
        }
    }

    /**
     * Checks email
     *
     * @param user
     *            the user to check
     * @throws EmailValidationException
     *             invalid email
     * @throws EmailAlreadyExistsException
     *             email already exists
     * @throws AliasAlreadyExistsException
     *             if the alias already exists
     */
    private void validateUserForCreation(UserVO user) throws EmailValidationException,
    EmailAlreadyExistsException, AliasAlreadyExistsException {
        validateEmail(user.getEmail(), null, true);
        validateAlias(user.getAlias(), null);
        // authorities
        if (user.getRoles() == null || user.getRoles().length == 0) {
            // TODO checked exception
            throw new UserManagementException("the user has no roles");
        }
    }

    /**
     * Validate if the change of user state is possible. Does not check the user limit when
     * activating a user! Also won't check if a change from CONFIRMED to ACTIVE is possible w.r.t.
     * accepting the terms of use. The status change will also be invalid if the new status is
     * DELETED or PERMANENTLY_DISABLED because the proper way to set these statuses is by using the
     * appropriate service methods.
     *
     * @param user
     *            the user with his current status
     * @param newStatus
     *            the new status
     * @param isChangeByManager
     *            if the change was triggered by the manager directly or indirectly (e.g. on user
     *            invitation to the client)
     * @throws InvalidUserStatusTransitionException
     *             in case the status change is not valid
     */
    private void validateUserStatusChange(User user, UserStatus newStatus, boolean isChangeByManager)
            throws InvalidUserStatusTransitionException {
        UserStatus currentStatus = user.getStatus();
        // always invalid if changing from DELETED or PERMANENTLY_DISABLED
        boolean isValid = false;
        if (UserStatus.REGISTERED.equals(currentStatus)) {
            isValid = UserStatus.CONFIRMED.equals(newStatus);
        } else if (UserStatus.INVITED.equals(currentStatus)) {
            isValid = UserStatus.CONFIRMED.equals(newStatus)
                    || (isChangeByManager && (UserStatus.TERMS_NOT_ACCEPTED.equals(newStatus) || UserStatus.ACTIVE
                            .equals(newStatus)));
        } else if (UserStatus.TERMS_NOT_ACCEPTED.equals(currentStatus)) {
            isValid = UserStatus.ACTIVE.equals(newStatus);
        } else if (UserStatus.CONFIRMED.equals(currentStatus)) {
            isValid = UserStatus.ACTIVE.equals(newStatus)
                    || UserStatus.TERMS_NOT_ACCEPTED.equals(newStatus);
            if (isValid && !isChangeByManager && isManagerActivationRequired()) {
                // manager has to activate the user
                isValid = false;
            }
        } else if (UserStatus.ACTIVE.equals(currentStatus)) {
            isValid = UserStatus.TEMPORARILY_DISABLED.equals(newStatus);

        } else if (UserStatus.TEMPORARILY_DISABLED.equals(currentStatus)) {
            isValid = UserStatus.ACTIVE.equals(newStatus);
        }
        if (!isValid) {
            throw new InvalidUserStatusTransitionException(currentStatus, newStatus, user);
        }
    }

    /**
     * Validates the user VO for user invitation and creates an alias if not provided and external
     * authentication is used. When validation has failed an IllegalArgumentException will be
     * thrown.
     *
     * @param userVo
     *            the VO to validate
     * @return an existing user if any
     * @throws EmailAlreadyExistsException
     *             in case the email address exists and cannot be merged
     * @throws EmailValidationException
     *             the retrieved email is not valid
     * @throws AliasAlreadyExistsException
     *             the alias is already used
     * @throws PermanentIdMissmatchException
     *             in case the permanent ID does not match
     */
    private User validateUserVoForInvitation(UserVO userVo) throws EmailAlreadyExistsException,
            EmailValidationException, AliasAlreadyExistsException, PermanentIdMissmatchException {
        validateEmail(userVo.getEmail(), null, false);

        User user = handleFindUserByEmail(userVo.getEmail());
        if (userVo instanceof ExternalUserVO) {
            ExternalUserVO extUserVO = (ExternalUserVO) userVo;
            User extUser = internalFindExistingExternalUser(extUserVO);
            if (user == null) {
                user = extUser;
                if (user == null) {
                    userVo.setAlias(generateUniqueAlias(extUserVO.getExternalUserName(),
                            extUserVO.getEmail()));
                }
            } else {
                // fail if found users are different or the alias cannot be merged
                if (extUser != null && !extUser.getId().equals(user.getId())) {
                    // TODO what is the correct exception here?
                    throw new EmailAlreadyExistsException(
                            "Changing the email address of the invited user is not possible because "
                                    + "another user already has that email address.");
                } else if (extUser == null) {
                    testMergingExternalUserWithExistingUser(user, extUserVO);
                    if (user.getAlias() == null) {
                        // case: user was registered while not in external system or external system
                        // was deactivated
                        userVo.setAlias(generateUniqueAlias(extUserVO.getExternalUserName(),
                                extUserVO.getEmail()));
                    }
                }
            }
        } else {
            validateAlias(userVo.getAlias(), user);
        }
        Assert.notNull(userVo.getFirstName(), "first name must be set");
        Assert.notNull(userVo.getLastName(), "last name must be set");
        return user;
    }
}
