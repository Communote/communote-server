package com.communote.server.core.security.authentication;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.common.EmailValidationException;
import com.communote.server.api.core.user.UserNotFoundException;
import com.communote.server.core.security.FieldUserIdentification;
import com.communote.server.core.security.UserDetails;
import com.communote.server.core.security.UserIdentification;
import com.communote.server.core.user.AliasAlreadyExistsException;
import com.communote.server.core.user.EmailAlreadyExistsException;
import com.communote.server.core.user.InvalidUserStatusTransitionException;
import com.communote.server.core.user.NoClientManagerLeftException;
import com.communote.server.core.user.PermanentIdMissmatchException;
import com.communote.server.core.user.UserManagement;
import com.communote.server.core.user.UserManagementException;
import com.communote.server.core.user.validation.UserActivationValidationException;
import com.communote.server.external.acegi.UserAccountTemporarilyDisabledException;
import com.communote.server.model.config.ExternalSystemConfiguration;
import com.communote.server.model.user.User;
import com.communote.server.model.user.UserStatus;
import com.communote.server.persistence.user.ExternalUserVO;
import com.communote.server.service.UserService;
import com.communote.server.service.UserService.UserServiceRetrievalFlag;

/**
 * Authentication provider that authenticates a user against an external user repository.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class ExternalAuthenticationProvider extends BaseCommunoteAuthenticationProvider {

    private static final Logger LOG = LoggerFactory.getLogger(ExternalAuthenticationProvider.class);

    /**
     * Create the user identification to be used for the user service. Allow subclasses to overwrite
     * it.
     *
     * @param userVO
     *            the user vo previously retrieved.
     * @return the user identification
     */
    protected UserIdentification createUserIdentification(ExternalUserVO userVO) {
        FieldUserIdentification userIdentification = new FieldUserIdentification()
                .setPreviousExternalUser(userVO)
                .setExternalSystemId(getConfiguration().getSystemId());
        return userIdentification;
    }

    /**
     * Returns the configuration for the external authentication.
     *
     * @return the configuration
     */
    protected abstract ExternalSystemConfiguration getConfiguration();

    /**
     * Get the user for the given identification
     *
     * @param userIdentification
     *            the identification (should contain the previously vo
     * @return the user
     * @throws UserNotFoundException
     *             in case user was not found
     */
    protected User getUser(UserIdentification userIdentification) throws UserNotFoundException {
        User user = ServiceLocator.instance().getService(UserService.class).getUser(
                userIdentification, UserServiceRetrievalFlag.CREATE,
                UserServiceRetrievalFlag.FORCE_EXTERNAL_REPO_CHECK);
        return user;
    }

    /**
     * @return the user management
     */
    private UserManagement getUserManagement() {
        return ServiceLocator.instance().getService(UserManagement.class);
    }

    /**
     * Evaluates the caught AuthenticationException and updates the user (e.g. disable his account)
     * if necessary and possible. This will only be possible if the authentication's principal has a
     * name (i.e. a.getName() != null).
     *
     * @param e
     *            the exception
     * @param userIdentifier
     *            identifier (email or alias) of the user for whom the authentication was attempted
     * @return the exception that should be rethrown
     * @throws AuthenticationException
     *             the passed exception
     * @throws EmailAlreadyExistsException
     *             when the new email exists
     * @throws AliasAlreadyExistsException
     *             when the new alias exists
     * @throws EmailValidationException
     *             when the email cannot be validated
     * @throws UserActivationValidationException
     *             in case the user cannot be updated
     * @throws InvalidUserStatusTransitionException
     *             when changing the user status is not possible
     * @throws NoClientManagerLeftException
     *             when changing the user status is not possible because the last client manager
     *             would be logged out
     * @throws PermanentIdMissmatchException
     *             when the permanentID changed
     */

    private AuthenticationException handleAuthenticationException(AuthenticationException e,
            String userIdentifier) throws AuthenticationException, EmailAlreadyExistsException,
            EmailValidationException, AliasAlreadyExistsException,
            UserActivationValidationException, InvalidUserStatusTransitionException,
            NoClientManagerLeftException, PermanentIdMissmatchException {

        ExternalSystemConfiguration config = getConfiguration();
        // TODO can userId really be null? maybe in case of failed token auth?
        if (config == null || userIdentifier == null) {
            return e;
        }
        ExternalUserVO userVO = new ExternalUserVO();

        userVO.setExternalUserName(userIdentifier);
        userVO.setSystemId(config.getSystemId());
        boolean update = true;
        // handle the different exceptions by setting a new status
        // TODO UsernameNotFoundException should be handled here too, but is dangerous if we have
        // more than one external services and the 1st one does not contain the user
        if (e instanceof AccountExpiredException || e instanceof CredentialsExpiredException
                || e instanceof DisabledException || e instanceof LockedException) {
            userVO.setStatus(UserStatus.TEMPORARILY_DISABLED);
            e = new UserAccountTemporarilyDisabledException(e.getMessage());
            // } else if (e instanceof AuthenticationServiceException) {
            // TODO following correct
            // reactivate account to have normal login as fallback -- no, let communote manager
            // reactivate user account
            // userVO.setStatus(UserStatus.ACTIVE);
        } else if (e instanceof BadCredentialsException) {
            // typically thrown if the password was wrong, the cause might be that the password
            // was changed in the external repository. Theoretically we should call
            // AuthenticationManagement.onInvalidAuthentication if Authentication was a
            // UsernamePasswordAuthentication. But this is the wrong place because several
            // AuthenticationProviders will be called if available. Thus it is possible that some
            // providers fail and the last succeeds but login still fails because of the
            // onInvalidAuthentication logic. Therefore we throw a custom exception which holds the
            // details of the external system.
            e = new BadCredentialsForExternalSystemException(config.getSystemId(), e.getMessage(),
                    e.getCause());
            update = false;
        } else {
            // do nothing
            update = false;
        }
        if (update) {
            UserManagement um = getUserManagement();
            um.updateExternalUser(userVO);
        }
        return e;
    }

    /**
     * @param e
     *            the exception
     * @param userVO
     *            the user vo
     * @return the exception for re-throwing
     */
    private AuthenticationException handleException(Exception e, ExternalUserVO userVO) {
        AuthenticationException authException;
        if (e instanceof AuthenticationException) {
            authException = (AuthenticationException) e;
            LOG.debug("Authentication against external repository failed with " + e.getMessage());
        } else if (e instanceof AliasAlreadyExistsException) {
            LOG.debug("External user with alias " + userVO.getAlias()
                    + " cannot be created or updated in local database "
                    + "because the alias already exists.");
            authException = new AuthenticationServiceException(
                    "Failed to update or create external user in local database.");
        } else if (e instanceof EmailValidationException) {
            LOG.debug("External user with email " + userVO.getEmail()
                    + " cannot be created or updated in local database "
                    + "because the email address is not valid.");
            authException = new AuthenticationServiceException(
                    "Failed to update or create external user in local database.");
        } else if (e instanceof EmailAlreadyExistsException) {
            LOG.debug("External user with email " + userVO.getEmail()
                    + " cannot be created or updated in local database "
                    + "because the email already exists.");
            authException = new AuthenticationServiceException(
                    "Failed to update or create external user in local database.", e);
        } else if (e instanceof UserActivationValidationException) {
            LOG.debug("External user cannot be activated in local database: {}", e.getMessage());
            authException = new AuthenticationServiceException(
                    "Failed to update or create external user in local database.");

        } else if (e instanceof InvalidUserStatusTransitionException) {
            InvalidUserStatusTransitionException iuste = (InvalidUserStatusTransitionException) e;
            LOG.debug("Changing the status of external user from " + iuste.getCurrentStatus()
                    + " to " + iuste.getFailedNewStatus() + " failed.");
            // let authentication fail
            authException = new AuthenticationServiceException(
                    "Failed to update or create external user in local database.");
        } else if (e instanceof NoClientManagerLeftException) {
            // happens if user was disabled in external repository but cannot be disabled in local
            // db to not lock out last client manager -> let authentication fail, DB-Auth provider
            // will still allow login
            authException = new AuthenticationServiceException(
                    "Failed to update or create external user in local database.");
        } else if (e instanceof UserManagementException) {
            LOG.debug("Creating or updating external user failed.", e);
            authException = new AuthenticationServiceException(
                    "Failed to update or create external user in local database.");
        } else {
            LOG.error("Unexcpected exception on updating external user.", e);
            authException = new AuthenticationServiceException(
                    "Unexcpected exception on updating external user " + e.getMessage(), e);
        }
        return authException;
    }

    /**
     * Try to retrieve the user details by using {@link #retrieveExternalUser(Authentication)} and
     * synchronize the returned data with the local database.
     *
     * @param authentication
     *            the authentication details
     * @return the user details. May return null if the AuthenticationProvider is unable to support
     *         authentication of the passed Authentication object. In such a case, the next
     *         AuthenticationProvider that supports the presented Authentication class will be
     *         tried.
     * @throws AuthenticationException
     *             if there is no user for the provided authentication or the authentication cannot
     *             be approved
     * @see BaseCommunoteAuthenticationProvider#handleRetrieveUserDetails(Authentication)
     */
    @Override
    protected final UserDetails handleRetrieveUserDetails(Authentication authentication)
            throws AuthenticationException {
        ExternalUserVO userVO = null;

        try {
            try {
                userVO = retrieveExternalUser(authentication);
                userVO.setClearPassword(getConfiguration().isPrimaryAuthentication());
                // just to be on the safe side...
                userVO.setPassword(null);
                // if the external system did not provide a locale and a locale context is
                // available, take the locale of that context (e.g. exposed by Communote's request
                // filter chain).
                if (userVO.getDefaultLanguage() == null
                        && LocaleContextHolder.getLocaleContext() != null) {
                    userVO.setDefaultLanguage(LocaleContextHolder.getLocale());
                }
            } catch (AuthenticationException e) {
                if (authentication.getName() != null) {
                    throw handleAuthenticationException(e, authentication.getName());
                } else {
                    throw e;
                }
            }

            UserIdentification userIdentification = createUserIdentification(userVO);

            try {
                User user = getUser(userIdentification);
                String username;
                // set username as provided by authentication if it is a UsernamePasswordAuth so
                // that subsequent requests with the same username do not need to re-authenticate
                // again. Necessary since username in external repo might be different to Communote
                // alias.
                if (authentication instanceof UsernamePasswordAuthenticationToken) {
                    username = authentication.getName();
                } else {
                    username = user.getAlias();
                }
                // convert user to user details
                return new UserDetails(user, username);
            } catch (UserNotFoundException e) {
                // TODO why always returning null here - shouldn't we throw an exceptions if the the
                // external user was found but could not be updated or created in local DB?
                LOG.error("Error retrieving user by userIdentification=" + userIdentification, e);
            }

        } catch (Exception e) {
            throw handleException(e, userVO);
        }
        return null;
    }

    /**
     * Tries to retrieve the user details from the associated external resource.
     *
     * @param authentication
     *            the authentication details
     * @return the user VO describing the returned user. Attributes that the external resource does
     *         not return should be left null.
     * @throws AuthenticationException
     *             if there is no user for the provided authentication or the authentication cannot
     *             be approved. Subclasses should throw an appropriate sub-exception.
     */
    protected abstract ExternalUserVO retrieveExternalUser(Authentication authentication)
            throws AuthenticationException;

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean supportsUserQuerying() {
        ExternalSystemConfiguration configuration = getConfiguration();
        return configuration != null && configuration.isAllowExternalAuthentication()
                && configuration.isPrimaryAuthentication();
    }
}
