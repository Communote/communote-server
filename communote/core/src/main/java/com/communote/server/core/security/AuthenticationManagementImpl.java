package com.communote.server.core.security;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionInterceptor;

import com.communote.common.converter.IdentityConverter;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.blog.BlogAccessException;
import com.communote.server.api.core.blog.BlogIdentifierValidationException;
import com.communote.server.api.core.blog.BlogManagement;
import com.communote.server.api.core.blog.BlogNotFoundException;
import com.communote.server.api.core.blog.CreationBlogTO;
import com.communote.server.api.core.blog.NonUniqueBlogIdentifierException;
import com.communote.server.api.core.common.ClientAndChannelContextHolder;
import com.communote.server.api.core.config.ApplicationConfigurationProperties;
import com.communote.server.api.core.config.ClientConfigurationProperties;
import com.communote.server.api.core.config.type.ClientProperty;
import com.communote.server.api.core.config.type.ClientPropertySecurity;
import com.communote.server.api.core.event.EventDispatcher;
import com.communote.server.api.core.note.NoteContentType;
import com.communote.server.api.core.note.NoteManagementAuthorizationException;
import com.communote.server.api.core.note.NoteStoringTO;
import com.communote.server.api.core.note.processor.NoteStoringPreProcessorException;
import com.communote.server.api.core.property.PropertyManagement;
import com.communote.server.api.core.property.StringPropertyTO;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.api.core.security.SecurityCodeManagement;
import com.communote.server.api.core.user.UserNotFoundException;
import com.communote.server.core.general.RunInTransaction;
import com.communote.server.core.general.TransactionException;
import com.communote.server.core.general.TransactionManagement;
import com.communote.server.core.mail.MailSender;
import com.communote.server.core.mail.messages.user.ManagerSecurityWarnMailMessage;
import com.communote.server.core.mail.messages.user.UserLockedMailMessage;
import com.communote.server.core.security.authentication.AuthAgainstInternalDBWhileExternalUserAccountException;
import com.communote.server.core.security.event.UserAuthenticatedEvent;
import com.communote.server.core.tag.TagParserFactory;
import com.communote.server.core.user.UserManagement;
import com.communote.server.core.user.UserManagementHelper;
import com.communote.server.core.user.security.ApplicationPropertyUserPassword;
import com.communote.server.core.user.security.UserPasswordManagement;
import com.communote.server.core.user.validation.UserActivationValidationException;
import com.communote.server.core.vo.blog.NoteModificationStatus;
import com.communote.server.model.blog.Blog;
import com.communote.server.model.note.NoteCreationSource;
import com.communote.server.model.security.ChannelType;
import com.communote.server.model.user.User;
import com.communote.server.model.user.UserRole;
import com.communote.server.model.user.UserStatus;
import com.communote.server.model.user.security.AuthenticationFailedStatus;
import com.communote.server.model.user.security.ForgottenPasswordSecurityCode;
import com.communote.server.model.user.security.UnlockUserSecurityCode;
import com.communote.server.persistence.blog.CreateBlogPostHelper;
import com.communote.server.persistence.common.messages.ResourceBundleManager;
import com.communote.server.persistence.user.UserDao;
import com.communote.server.persistence.user.security.AuthenticationFailedStatusDao;
import com.communote.server.persistence.user.security.UnlockUserSecurityCodeDao;
import com.communote.server.service.NoteService;

/**
 * @see com.communote.server.core.security.AuthenticationManagement
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
@Service("authenticationManagement")
public class AuthenticationManagementImpl extends AuthenticationManagementBase {

    private final static Logger LOGGER = LoggerFactory
            .getLogger(AuthenticationManagementImpl.class);

    @Autowired
    private AuthenticationFailedStatusDao authenticationFailedStatusDao;
    @Autowired
    private UnlockUserSecurityCodeDao unlockUserSecurityCodeDao;
    @Autowired
    private UserDao userDao;
    @Autowired
    private MailSender mailSender;
    @Autowired
    private UserManagement userManagement;
    @Autowired
    private NoteService noteManagement;
    @Autowired
    private BlogManagement blogManagement;
    @Autowired
    private TransactionManagement transactionManagement;
    @Autowired
    private SecurityCodeManagement securityCodeManagement;
    @Autowired
    private EventDispatcher eventDispatcher;
    @Autowired
    private UserPasswordManagement userPasswordManagement;

    /**
     * Asserts that the user was not provided by the active primary external user repository.
     *
     * @param user
     *            The user.
     * @throws AuthAgainstInternalDBWhileExternalUserAccountException
     *             in case the user was provided by the active primary external user repository
     */
    private void assertExternalSystem(User user)
            throws AuthAgainstInternalDBWhileExternalUserAccountException {
        ClientConfigurationProperties props = getClientConfigurationProperties();
        String primaryExternalAuthentication = props.getPrimaryExternalAuthentication();
        if (primaryExternalAuthentication == null) {
            return;
        }
        if (!props.isDBAuthenticationAllowed()) {
            throw new AuthAgainstInternalDBWhileExternalUserAccountException(
                    "Authentication agaings the internal db is deactivated for the external system.",
                    user.getAlias(), primaryExternalAuthentication);
        }
        if (user.hasExternalAuthentication(primaryExternalAuthentication)) {

            throw new AuthAgainstInternalDBWhileExternalUserAccountException(
                    "The user can't be authenticated against the internl db,"
                            + " as an external system is activated the user has a configuration for.",
                    user.getAlias(), primaryExternalAuthentication);
        }
    }

    /**
     * check if the user account is locked
     *
     * @param user
     *            user to check
     * @throws AccountPermanentlyLockedException
     *             if a user account is permanently locked
     * @throws AccountTemporarilyLockedException
     *             if a user account is temporarily locked
     */
    private void checkIfUserIsLocked(User user)
            throws AccountPermanentlyLockedException, AccountTemporarilyLockedException {

        ChannelType channel = ClientAndChannelContextHolder.getChannel();
        AuthenticationFailedStatus authFailedStatus = authenticationFailedStatusDao
                .findByUserAndChannel(user, channel);
        if (authFailedStatus == null) {
            return;
        }
        if (authFailedStatus.getFailedAuthCounter() >= getFailedAuthLimitPermlock()) {
            // send email with security code
            // create code UnlockUserSecurityCode code
            UnlockUserSecurityCode code = unlockUserSecurityCodeDao
                    .findByUserAndChannel(user.getId(), channel);
            if (code == null) {
                code = unlockUserSecurityCodeDao.createCode(user, channel);
            }
            // send message
            UserLockedMailMessage message = new UserLockedMailMessage(user,
                    ClientAndChannelContextHolder.getChannel(), code);
            mailSender.send(message);
            throw new AccountPermanentlyLockedException("The user account is permanently locked");
        }
        if (authFailedStatus.getLockedTimeout() != null && authFailedStatus.getLockedTimeout()
                .after(new Date(System.currentTimeMillis()))) {
            throw new AccountTemporarilyLockedException(authFailedStatus.getLockedTimeout());
        }

    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public UserDetails checkLocalUserPasswordOnLogin(String username, String password)
            throws UserNotFoundException, AccountNotActivatedException,
            AuthAgainstInternalDBWhileExternalUserAccountException {
        User user = userManagement.findUserByEmailAlias(username);
        if (user == null) {
            throw new UserNotFoundException("User with username '" + username + "' does not exist");
        }
        if (user.getStatus() == UserStatus.INVITED || user.getStatus() == UserStatus.REGISTERED) {
            throw new AccountNotActivatedException("The account of user '" + username
                    + "' is still in status REGISTERED or INVITED");
        }
        assertExternalSystem(user);
        boolean passwordCorrect;
        if (getApplicationConfigurationProperties().getProperty(
                ApplicationPropertyUserPassword.LOCAL_USER_PASSWORD_UPDATE_ON_LOGIN,
                ApplicationPropertyUserPassword.DEFAULT_LOCAL_USER_PASSWORD_UPDATE_ON_LOGIN)) {
            passwordCorrect = userPasswordManagement.checkAndUpdatePassword(user, password);
        } else {
            passwordCorrect = userPasswordManagement.checkPassword(user, password);
        }
        if (passwordCorrect) {
            return new UserDetails(user, username);
        }
        return null;
    }

    /**
     * Throw an exception if the terms of use have to be accepted and the user did not yet accept
     * them.
     *
     * @param user
     *            the user to check
     * @throws TermsNotAcceptedException
     *             if the terms of use have to be accepted
     * @throws UserNotFoundException
     *             if the user does not exist
     */
    private void checkTermsOfUse(User user)
            throws TermsNotAcceptedException, UserNotFoundException {
        if (!UserManagementHelper.isSystemUser(user)) {
            boolean termsNotAccpeted = user.hasStatus(UserStatus.TERMS_NOT_ACCEPTED);
            // accepting changed terms currently only required on WEB login
            boolean changedTermsNotAccepted = !termsNotAccpeted
                    && ChannelType.WEB.equals(ClientAndChannelContextHolder.getChannel())
                    && !user.isTermsAccepted() && user.hasStatus(UserStatus.ACTIVE);
            if ((termsNotAccpeted || changedTermsNotAccepted)
                    && (ClientProperty.TERMS_OF_USE_USERS_MUST_ACCEPT
                            .getValue(ClientProperty.DEFAULT_TERMS_OF_USE_USERS_MUST_ACCEPT))) {
                throw new TermsNotAcceptedException("User has not accepted the terms of use.",
                        user.getId());
            }
            if (termsNotAccpeted) {
                // might happen if user registered or was invited while accepting terms of use was
                // required. Now it is not necessary anymore so we try to change status to ACTIVE
                try {
                    ServiceLocator.findService(UserManagement.class).acceptTermsOfUse(user.getId());
                } catch (AuthorizationException e) {
                    LOGGER.error("Unexpected exception while trying to activate user", e);
                } catch (UserActivationValidationException e) {
                    // warn. No custom exception because AccountNotActivatedException will be thrown
                    // TODO throw custom exception for better user feedback?
                    LOGGER.warn("Could not activate user with ID {}: {} ", user.getId(),
                            e.getMessage());
                }
            }
        }
    }

    /**
     * Create a personal blog for the user with a welcome message post TODO move to blogmanagement,
     * use own transaction for it
     *
     * @param user
     *            the user to create the blog for
     */
    private void createPersonalTopic(User user) {
        if (!Boolean.parseBoolean(ClientProperty.CREATE_PERSONAL_BLOG
                .getValue(Boolean.toString(ClientProperty.DEFAULT_CREATE_PERSONAL_BLOG)))) {
            return;
        }
        Locale locale = user.getLanguageLocale();
        Object[] nameArgs = getPersonalBlogNameArguments(user);
        String personalBlogName = ResourceBundleManager.instance()
                .getText("blog.welcome.personal.blog.name", locale, nameArgs);
        String personalBlogDescription = ResourceBundleManager.instance()
                .getText("blog.welcome.personal.blog.description", locale, nameArgs);
        String personalBlogTags = ResourceBundleManager.instance()
                .getText("blog.welcome.personal.blog.tags", locale, nameArgs);
        String welcomePostMessage = ResourceBundleManager.instance()
                .getText("blog.welcome.personal.blog.post.text.message", locale, nameArgs);
        String welcomePostMessageTags = ResourceBundleManager.instance()
                .getText("blog.welcome.personal.blog.post.text.tags", locale, nameArgs);

        CreationBlogTO blogDetails = new CreationBlogTO();
        blogDetails.setAllCanRead(false);
        blogDetails.setAllCanWrite(false);
        blogDetails.setCreatorUserId(user.getId());
        blogDetails.setTitle(personalBlogName);
        blogDetails.setDescription(personalBlogDescription);
        blogDetails.setUnparsedTags(
                TagParserFactory.instance().getDefaultTagParser().parseTags(personalBlogTags));

        StringPropertyTO personalTopicProp = new StringPropertyTO();
        personalTopicProp.setKeyGroup(PropertyManagement.KEY_GROUP);
        personalTopicProp.setPropertyKey(BlogManagement.PROPERTY_KEY_PERSONAL_TOPIC_USER_ID);
        personalTopicProp.setPropertyValue(Long.toString(user.getId()));

        blogDetails.setProperties(new ArrayList<StringPropertyTO>());
        blogDetails.getProperties().add(personalTopicProp);

        Blog blog = null;
        try {
            String alias = blogManagement.generateUniqueBlogAlias("user." + user.getAlias(), null);
            blogDetails.setNameIdentifier(alias);
            blog = blogManagement.createBlog(blogDetails);
        } catch (NonUniqueBlogIdentifierException e) {
            LOGGER.error("Error creating personal blog for user " + user.attributesToString(), e);
            // ignore it to not disable login
        } catch (BlogIdentifierValidationException e) {
            LOGGER.error("Error creating personal blog for user " + user.attributesToString(), e);
            // ignore it to not disable login
        } catch (BlogNotFoundException e) {
            // adding no parent topic, thus this exception should not occur
            LOGGER.error("Unexpected exception", e);
        } catch (BlogAccessException e) {
            // adding no parent topic, thus this exception should not occur
            LOGGER.error("Unexpected exception", e);
        }

        if (blog != null) {
            NoteStoringTO noteStoringTO = new NoteStoringTO();

            noteStoringTO.setBlogId(blog.getId());
            noteStoringTO.setCreatorId(user.getId());
            noteStoringTO.setCreationSource(NoteCreationSource.WEB);
            noteStoringTO.setLanguage(user.getLanguageCode());
            noteStoringTO.setPublish(true);
            noteStoringTO.setVersion(0L);
            noteStoringTO.setContentType(NoteContentType.HTML);
            noteStoringTO.setContent(welcomePostMessage);
            noteStoringTO.setVersion(0L);
            CreateBlogPostHelper.setDefaultFailLevel(noteStoringTO);
            noteStoringTO.setUnparsedTags(welcomePostMessageTags);

            try {
                NoteModificationStatus status = noteManagement.createNote(noteStoringTO, null)
                        .getStatus();
                if (status.equals(NoteModificationStatus.SYSTEM_ERROR)
                        || status.equals(NoteModificationStatus.LIMIT_REACHED)) {
                    // force rollback of this transaction as well otherwise we would get an
                    // UnexpectedRollbackException
                    TransactionInterceptor.currentTransactionStatus().setRollbackOnly();
                }
            } catch (NoteManagementAuthorizationException e) {
                LOGGER.error("Error creating personal post for user on first login: "
                        + user.attributesToString(), e);
                // ignore it to not disable login
            } catch (BlogNotFoundException e) {
                LOGGER.error("Error creating personal post for user on first login: "
                        + user.attributesToString(), e);
                // ignore it to not disable login
            } catch (NoteStoringPreProcessorException e) {
                LOGGER.error("Error creating personal post for user on first login: "
                        + user.attributesToString(), e);
                // ignore it to not disable login
            }
        }
    }

    private ApplicationConfigurationProperties getApplicationConfigurationProperties() {
        return CommunoteRuntime.getInstance().getConfigurationManager()
                .getApplicationConfigurationProperties();
    }

    /**
     * @return the client configuration properties
     */
    private ClientConfigurationProperties getClientConfigurationProperties() {
        return CommunoteRuntime.getInstance().getConfigurationManager()
                .getClientConfigurationProperties();
    }

    /**
     * get property: authentication permanent lock limit
     *
     * @return specific value
     */
    private int getFailedAuthLimitPermlock() {
        return getClientConfigurationProperties().getProperty(
                ClientPropertySecurity.FAILED_AUTH_LIMIT_PERMLOCK,
                ClientPropertySecurity.DEFAULT_FAILED_AUTH_LIMIT_PERMLOCK);
    }

    /**
     * get property: authentication permanent lock limit
     *
     * @return specific value
     */
    private int getFailedAuthLockedTimespan() {
        return getClientConfigurationProperties().getProperty(
                ClientPropertySecurity.FAILED_AUTH_LOCKED_TIMESPAN,
                ClientPropertySecurity.DEFAULT_FAILED_AUTH_LOCKED_TIMESPAN);
    }

    /**
     * get property: authentication risk level steps
     *
     * @return specific value
     */
    private int getFailedAuthStepsRiskLevel() {
        return getClientConfigurationProperties().getProperty(
                ClientPropertySecurity.FAILED_AUTH_STEPS_RISK_LEVEL,
                ClientPropertySecurity.DEFAULT_FAILED_AUTH_STEPS_RISK_LEVEL);
    }

    /**
     * get property: authentication permanent lock limit
     *
     * @return specific value
     */
    private int getFailedAuthStepsTemplock() {
        return getClientConfigurationProperties().getProperty(
                ClientPropertySecurity.FAILED_AUTH_STEPS_TEMPLOCK,
                ClientPropertySecurity.DEFAULT_FAILED_AUTH_STEPS_TEMPLOCK);
    }

    /**
     * Returns an array with all needed information about the personal blog for the given user.
     *
     * @param user
     *            The user the arguments should be extracted for.
     * @return Array with arguments for the new blog.
     */
    private String[] getPersonalBlogNameArguments(User user) {
        String[] nameArgs = null;
        if (user.getProfile() != null && user.getProfile().getFirstName() != null
                && user.getProfile().getLastName() != null) {
            nameArgs = new String[] { user.getProfile().getFirstName(),
                    user.getProfile().getLastName() };
        } else {
            nameArgs = new String[] { StringUtils.EMPTY, user.getAlias() };
        }
        return nameArgs;
    }

    @Override
    protected User handleOnSuccessfulAuthentication(Authentication authentication) {

        UserDetails details = null;

        if (authentication.getPrincipal() instanceof UserDetails) {
            details = (UserDetails) authentication.getPrincipal();
        }
        if (details == null) {
            throw new AuthenticationServiceException(
                    "Prinicpial is not a UserDetails instance! " + authentication.toString());
        }

        User user = userManagement.findUserByUserId(details.getUserId());
        if (user == null) {
            throw new AuthenticationServiceException(
                    "Authenticated user was not found! authResult: " + authentication.toString());
        }
        // the user id
        final Long userId = user.getId();

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // delete forgotten password security codes
        try {
            securityCodeManagement.deleteAllCodesByUser(user.getId(),
                    ForgottenPasswordSecurityCode.class);
        } catch (Exception e) {
            LOGGER.error(
                    "Error deleting security codeds on login user: " + user.attributesToString(),
                    e);
        }

        internalLoginDatePersonalBlogTx(userId, user);

        internalRemoveFailedAuthsTx(userId, user);
        try {
            eventDispatcher.fire(new UserAuthenticatedEvent(userId));
        } catch (Exception e) {
            LOGGER.error("Error dispatching the UserAuthenticatedEvent for user {}.", userId, e);
        }
        return user;
    }

    @Override
    @Transactional(propagation = Propagation.NEVER)
    protected void handleOnSuccessfulAuthentication(Long userId) throws UserNotFoundException {
        User user = userDao.load(userId);
        if (user == null) {
            throw new UserNotFoundException("User with ID " + userId + " does not exist");
        }
        Authentication authentication = AuthenticationHelper.createAuthentication(user);
        this.handleOnSuccessfulAuthentication(authentication);
    }

    @Override
    protected void handleValidateUserLogin(Long userId) throws UserNotFoundException,
            AccountNotActivatedException, AccountTemporarilyLockedException,
            AccountPermanentlyLockedException, TermsNotAcceptedException,
            AccountPermanentlyDisabledException, AccountTemporarilyDisabledException {
        User user = userManagement.getUserById(userId, new IdentityConverter<User>());
        if (user == null) {
            throw new UserNotFoundException("User with identifier " + userId + " does not exist.");
        }

        checkIfUserIsLocked(user);
        if (user.hasStatus(UserStatus.PERMANENTLY_DISABLED) || user.hasStatus(UserStatus.DELETED)) {
            throw new AccountPermanentlyDisabledException("The account was permanently disabled.");
        }
        if (user.hasStatus(UserStatus.TEMPORARILY_DISABLED)) {
            throw new AccountTemporarilyDisabledException("The account was temporarily disabled.");
        }
        checkTermsOfUse(user);
        if (!user.getStatus().equals(UserStatus.ACTIVE)) {
            throw new AccountNotActivatedException("The account is not in status active.");
        }
    }

    private void incrementAuthenticationFailedCount(User user) {
        if (user == null) {
            return;
        }
        // needed cause delay increment (transaction)
        int failedAuthCount = 1;
        // create/update AuthenticationFailedStatus
        AuthenticationFailedStatus authFailedStatus = authenticationFailedStatusDao
                .findByUserAndChannel(user, ClientAndChannelContextHolder.getChannel());
        if (authFailedStatus == null) {
            authFailedStatus = AuthenticationFailedStatus.Factory.newInstance(1,
                    ClientAndChannelContextHolder.getChannel());
            authenticationFailedStatusDao.create(authFailedStatus);
            authFailedStatus.setLockedTimeout(new Timestamp(System.currentTimeMillis()));
            user.getFailedAuthentication().add(authFailedStatus);
            userDao.update(user);
        } else {
            authenticationFailedStatusDao.incFailedAuthCount(authFailedStatus.getId());
            failedAuthCount = authFailedStatus.getFailedAuthCounter() + 1;
        }
        if (failedAuthCount == getFailedAuthLimitPermlock()) {
            // send high level warn message to manager only once
            prepareSendUserLockedMessage(user, authFailedStatus);
        } else if (failedAuthCount % getFailedAuthStepsTemplock() == 0) {
            int multiplierCount = failedAuthCount / getFailedAuthStepsTemplock();
            authenticationFailedStatusDao.updateLockedTimeout(authFailedStatus.getId(),
                    new Timestamp(System.currentTimeMillis()
                            + getFailedAuthLockedTimespan() * multiplierCount * 1000));

            float muliplierMail = multiplierCount / getFailedAuthStepsRiskLevel();
            prepareSendPossibleHackMessages(user, authFailedStatus,
                    getFailedAuthLockedTimespan() * multiplierCount,
                    muliplierMail > 1 ? ManagerSecurityWarnMailMessage.RISK_LEVEL_HIGH
                            : muliplierMail > 0 ? ManagerSecurityWarnMailMessage.RISK_LEVEL_MEDIUM
                                    : ManagerSecurityWarnMailMessage.RISK_LEVEL_LOW);
        }
        try {
            // We only want to sent out the mail here, so no further
            // exception handling is necessary.
            checkIfUserIsLocked(user);
        } catch (AccountPermanentlyLockedException e) {
            LOGGER.warn(e.getMessage());
        } catch (AccountTemporarilyLockedException e) {
            LOGGER.warn(e.getMessage());
        }
    }

    /**
     * Create the personal blog if necessary and set the users last login date. Creates a new
     * transaction.
     *
     * @param userId
     *            the user id
     * @param user
     *            the user, only for logging
     */
    private void internalLoginDatePersonalBlogTx(final Long userId, User user) {
        //
        try {

            RunInTransaction lastLoginPersonalBlogTransaction = new RunInTransaction() {

                /**
                 * Create the personal blog if necessary and set the users last login date
                 */
                @Override
                public void execute() throws TransactionException {
                    // TODO not yet 100% correct, the last login date is only set if the personal
                    // blog
                    // creation succeeded. it would be perfect to have a user property set if the
                    // personal
                    // blog has been created or not (and the alias of that personnel blog)

                    // load the user again since we want to change it within the transaction
                    User user = userDao.load(userId);

                    // create personal blog if it is the first login
                    if (user.getLastLogin() == null && !UserManagementHelper.isSystemUser(user)) {
                        createPersonalTopic(user);
                    }

                    // set last login and update
                    user.setLastLogin(new Timestamp(new Date().getTime()));
                }
            };

            transactionManagement.execute(lastLoginPersonalBlogTransaction);

        } catch (Exception e) {
            LOGGER.error("Error creating personal blog: " + user.attributesToString(), e);
        }
    }

    /**
     * Create the personal blog if necessary and set the users last login date.
     *
     * @param userId
     *            the user id
     * @param user
     *            the user, only for logging
     */
    private void internalRemoveFailedAuthsTx(final Long userId, User user) {
        // remove lock-entry if exists
        try {

            RunInTransaction removeAuthFailed = new RunInTransaction() {

                @Override
                public void execute() throws TransactionException {

                    // load the user within this transaction
                    User user = userDao.load(userId);

                    AuthenticationFailedStatus authFailedStatus = authenticationFailedStatusDao
                            .findByUserAndChannel(user, ClientAndChannelContextHolder.getChannel());
                    if (authFailedStatus != null) {
                        authenticationFailedStatusDao.remove(authFailedStatus);
                    }

                }
            };

            transactionManagement.execute(removeAuthFailed);

        } catch (Exception e) {
            LOGGER.error(
                    "Error resetting failed authentications for user: " + user.attributesToString(),
                    e);
        }
    }

    @Override
    public void onUsernamePasswordAuthenticationFailed(String username) {
        User user = userManagement.findUserByEmailAlias(username);
        incrementAuthenticationFailedCount(user);
    }

    @Override
    public void onUsernamePasswordAuthenticationFailed(String externalUserId,
            String externalSystemId) {
        User user = userManagement.findUserByExternalUserId(externalUserId, externalSystemId);
        incrementAuthenticationFailedCount(user);
    }

    /**
     * Prepares sending a warning email message about a possible hack attempt.
     *
     * @param user
     *            the user
     * @param authFailedStatus
     *            authentication failed status details
     * @param lockedTime
     *            the time the user was locked
     * @param riskLevelKey
     *            message key for the risk level
     */
    private void prepareSendPossibleHackMessages(User user,
            AuthenticationFailedStatus authFailedStatus, long lockedTime, String riskLevelKey) {
        List<User> managers = userDao.findByRole(UserRole.ROLE_KENMEI_CLIENT_MANAGER,
                UserStatus.ACTIVE);

        // calendar (tricky)
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"), Locale.ENGLISH);
        calendar.setTime(new Date(lockedTime * 1000));

        // set message properties
        Object[] messageProps = new Object[4];
        messageProps[0] = user.getAlias();
        messageProps[1] = authFailedStatus.getChannelType().getValue();
        messageProps[2] = authFailedStatus.getFailedAuthCounter() + 1;
        messageProps[3] = calendar.get(Calendar.HOUR) + "h " + calendar.get(Calendar.MINUTE)
                + "min " + calendar.get(Calendar.SECOND) + "s";

        ManagerSecurityWarnMailMessage message = null;
        String warnReason, riskLevel;
        // iterate managers and create for each manager a message
        for (int i = 0; i < managers.size(); i++) {
            // create risk level and warn reason
            warnReason = ResourceBundleManager.instance().getText(
                    ManagerSecurityWarnMailMessage.WARN_REASON_POSSIBLE_HACK_ATTEMPT,
                    managers.get(i).getLanguageLocale(), messageProps);
            riskLevel = ResourceBundleManager.instance().getText(riskLevelKey,
                    managers.get(i).getLanguageLocale());

            // generate and send message
            message = new ManagerSecurityWarnMailMessage(managers.get(i), riskLevel, warnReason,
                    user.getId());
            mailSender.send(message);
        }
    }

    /**
     * prepare and send one or many locked user account messages
     *
     * @param user
     *            specific user
     * @param authFailedStatus
     *            status of failed authentication process
     */
    private void prepareSendUserLockedMessage(User user,
            AuthenticationFailedStatus authFailedStatus) {
        // set message properties
        Object[] messageProps = new Object[3];
        messageProps[0] = user.getAlias();
        messageProps[1] = authFailedStatus.getChannelType().getValue();
        messageProps[2] = authFailedStatus.getFailedAuthCounter() + 1;

        ManagerSecurityWarnMailMessage message = null;
        String warnReason, riskLevel;
        // iterate managers and create for each manager a message
        for (User manager : userDao.findByRole(UserRole.ROLE_KENMEI_CLIENT_MANAGER,
                UserStatus.ACTIVE)) {
            warnReason = ResourceBundleManager.instance().getText(
                    ManagerSecurityWarnMailMessage.WARN_REASON_USER_ACCOUNT_PERM_LOCKED,
                    manager.getLanguageLocale(), messageProps);
            riskLevel = ResourceBundleManager.instance().getText(
                    ManagerSecurityWarnMailMessage.RISK_LEVEL_HIGH, manager.getLanguageLocale());
            message = new ManagerSecurityWarnMailMessage(manager, riskLevel, warnReason,
                    user.getId());
            mailSender.send(message);
        }
    }

}
