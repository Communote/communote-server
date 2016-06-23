package com.communote.server.core.security.authentication;

import java.util.List;
import java.util.Map;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.user.UserNotFoundException;
import com.communote.server.api.core.user.UserVO;
import com.communote.server.core.security.AccountNotActivatedException;
import com.communote.server.core.security.AccountPermanentlyDisabledException;
import com.communote.server.core.security.AccountPermanentlyLockedException;
import com.communote.server.core.security.AccountTemporarilyDisabledException;
import com.communote.server.core.security.AccountTemporarilyLockedException;
import com.communote.server.core.security.AuthenticationManagement;
import com.communote.server.core.security.AuthenticationManagementException;
import com.communote.server.core.security.TermsNotAcceptedException;
import com.communote.server.core.security.TermsOfUseNotAcceptedException;
import com.communote.server.core.security.UserDetails;
import com.communote.server.core.user.UserManagement;
import com.communote.server.external.acegi.UserAccountPermanentlyDisabledException;
import com.communote.server.external.acegi.UserAccountPermanentlyLockedException;
import com.communote.server.external.acegi.UserAccountTemporarilyDisabledException;
import com.communote.server.external.acegi.UserAccountTemporarilyLockedException;
import com.communote.server.model.user.User;
import com.communote.server.persistence.user.InvitationField;

/**
 * Base class for all Communote authentication providers.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class BaseCommunoteAuthenticationProvider implements AuthenticationProvider {

    @Override
    public Authentication authenticate(Authentication authentication)
            throws AuthenticationException {
        UserDetails details = retrieveAndAuthenticateUserDetails(authentication);
        if (details == null) {
            return null;
        }
        Long userId = details.getUserId();
        if (userId == null) {
            return null;
        }
        // do additional authentication checks
        doAdditionalAuthenticationChecks(userId);
        User user = ServiceLocator.findService(UserManagement.class).findUserByUserId(userId);

        if (user == null) {
            throw new AuthenticationServiceException(
                    "User was not found for authenticated user with ID: " + details.getUserId());
        }

        Authentication authResult = createSuccessAuthentication(details, authentication);

        return authResult;
    }

    /**
     * Creates a successful {@link Authentication} object. Subclasses will usually store the
     * original credentials the user supplied (not salted or encoded passwords) in the returned
     * Authentication object.
     *
     * @param details
     *            the authenticated user details. Subclasses should store it as principal in the
     *            authentication object.
     * @param authentication
     *            that was presented to the provider for validation
     * @return the successful authentication token
     */
    protected abstract Authentication createSuccessAuthentication(UserDetails details,
            Authentication authentication);

    /**
     * Does some additional authentication checks.
     *
     * @param details
     *            the details of the authenticated user
     * @throws AuthenticationException
     *             in case the additional checks let the authentication fail
     */
    private void doAdditionalAuthenticationChecks(Long userId) throws AuthenticationException {
        try {
            ServiceLocator.findService(AuthenticationManagement.class).validateUserLogin(userId);
        } catch (TermsNotAcceptedException e) {
            throw new TermsOfUseNotAcceptedException(e.getMessage(), e.getUserId());
        } catch (UserNotFoundException e) {
            throw new UsernameNotFoundException(e.getMessage());
        } catch (AccountNotActivatedException e) {
            // TODO define own exception
            throw new LockedException(e.getMessage());
        } catch (AccountPermanentlyLockedException e) {
            throw new UserAccountPermanentlyLockedException(e.getMessage());
        } catch (AccountTemporarilyLockedException e) {
            throw new UserAccountTemporarilyLockedException(e.getLockedTimeout());
        } catch (AccountPermanentlyDisabledException e) {
            throw new UserAccountPermanentlyDisabledException(
                    "The user account was permanently disabled.");
        } catch (AccountTemporarilyDisabledException e) {
            throw new UserAccountTemporarilyDisabledException(
                    "The user account was temporarily disabled.");
        } catch (AuthenticationManagementException e) {
            throw new AuthenticationServiceException("Authentication failed.", e);

        }
    }

    /**
     * @return Returns an identifier for this provider.
     */
    public abstract String getIdentifier();

    /**
     * @return all invitation fields this provider requires
     */
    // TODO see notes on queryUser
    public abstract List<InvitationField> getInvitationFields();

    /**
     * Tries to retrieve user details using the passed authentication.
     *
     * @param authentication
     *            describes the user to retrieve
     * @return the user details. May return null if the AuthenticationProvider is unable to support
     *         authentication of the passed Authentication object. In such a case, the next
     *         AuthenticationProvider that supports the presented Authentication class will be
     *         tried.
     * @throws AuthenticationException
     *             in case there is no user for the passed authentication
     */
    protected abstract UserDetails handleRetrieveUserDetails(Authentication authentication)
            throws AuthenticationException;

    /**
     * Queries a user and returns the user data
     *
     * @param queryData
     *            the data to use for query. See {@link InvitationField} for possible values. The
     *            fields this provider uses are defined by {@link #getInvitationFields()}
     * @return the found user as value object, or null
     * @throws AuthenticationException
     */
    // TODO this should be a feature of the UserService (which delegates to a matching UserRepo; the
    // internal db could be repo too) because invitation and retrieval of user data is independent
    // from authentication (e.g. remote user authentication takes care of authentication and
    // database, LDAP or any other system provides the user details).
    public abstract UserVO queryUser(Map<InvitationField, String> queryData);

    /**
     * Uses the passed authentication to retrieve and authenticate the user Details. Subclasses
     * should override the {@link #handleRetrieveUserDetails(Authentication)} method to provide an
     * appropriate implementation for user retrieval. In case the user cannot be authenticated an
     * exception is thrown.
     *
     * @param authentication
     *            the appropriate authentication
     * @return the user details
     * @throws AuthenticationException
     *             in case the user cannot be authenticated
     */
    public UserDetails retrieveAndAuthenticateUserDetails(Authentication authentication)
            throws AuthenticationException {
        UserDetails details = handleRetrieveUserDetails(authentication);
        return details;
    }

    /**
     * Checks if the provider is enabled and configured for user querying
     *
     * @return true if this provider, in his current state, supports user querying
     */
    public abstract boolean supportsUserQuerying();
}
