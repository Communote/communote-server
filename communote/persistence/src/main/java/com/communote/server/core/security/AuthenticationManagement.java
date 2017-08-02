package com.communote.server.core.security;

import org.springframework.security.core.Authentication;

import com.communote.server.api.core.user.UserNotFoundException;
import com.communote.server.core.security.authentication.AuthAgainstInternalDBWhileExternalUserAccountException;
import com.communote.server.model.user.User;

/**
 * <p>
 * Service methods for user authentication.
 * </p>
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public interface AuthenticationManagement {

    /**
     * Check the password of a local user for a match with a given clear text password. This method
     * should be called as part of the login process of a local user to validate the password and do
     * some additional tests. A local user is a user who wasn't provided by the active primary
     * external user repository.
     *
     * @param username
     *            the login name which can be the alias or the email address of the user
     * @param password
     *            the clear text password to test against
     * @return the user details of the user or null if the password check failed
     * @throws UserNotFoundException
     *             in case the user does not exist
     * @throws AccountNotActivatedException
     *             in case the user account is not active
     * @throws AuthAgainstInternalDBWhileExternalUserAccountException
     *             in case the user was provided by the active primary external user repository
     */
    UserDetails checkLocalUserPasswordOnLogin(String username, String password)
            throws UserNotFoundException, AccountNotActivatedException,
            AuthAgainstInternalDBWhileExternalUserAccountException;

    /**
     * This method finalizes the login process of a user. It uses the given user to create the
     * authentication and logs the user in by setting the security context. This method assumes that
     * the user is valid and authenticated.
     *
     * <p>
     * Note: This method assumes that there is NO existing transaction. If there is a current
     * transaction it will fail!
     * </p>
     *
     * @param successAuthentication
     *            the authentication to be used. it should contain UserDetails in most of the cases
     * @return the user authenticated
     */
    public User onSuccessfulAuthentication(Authentication successAuthentication);

    /**
     * This method finalizes the login process of a user. It uses the given user to create the
     * authentication and logs the user in by setting the security context. This method assumes that
     * the user is valid and authenticated.
     *
     * <p>
     * Note: This method assumes that there is NO existing transaction. If there is a current
     * transaction it will fail!
     * </p>
     *
     * @param userId
     *            ID of the user
     * @throws UserNotFoundException
     *             if the user does not exist
     */
    void onSuccessfulAuthentication(Long userId) throws UserNotFoundException;

    /**
     * Should be called if a username and password authentication for a user failed because of a
     * wrong password.
     *
     * @param username
     *            the internal login name (e-mail or alias) of the user whose authentication failed
     */
    public void onUsernamePasswordAuthenticationFailed(String username);

    /**
     * Should be called if a username and password authentication for a user failed because of a
     * wrong password.
     *
     * @param externalUserId
     *            the login name of the user in the external system whose authentication failed
     * @param externalSystemId
     *            the ID of the external system
     */
    public void onUsernamePasswordAuthenticationFailed(String externalUserId,
            String externalSystemId);

    /**
     *
     * Does additional login checks. This includes testing whether the account is locked, the user
     * is active and has accepted the terms of use. If one of the login checks fails an appropriate
     * exception will be thrown.
     *
     * @param userId
     *            the Id of the user
     *
     * @throws AccountPermanentlyLockedException
     * @throws AccountTemporarilyLockedException
     * @throws AccountNotActivatedException
     * @throws TermsNotAcceptedException
     * @throws UserNotFoundException
     * @throws AccountPermanentlyDisabledException
     * @throws AccountTemporarilyDisabledException
     */
    public void validateUserLogin(Long userId)
            throws AccountPermanentlyLockedException, AccountTemporarilyLockedException,
            AccountNotActivatedException, TermsNotAcceptedException, UserNotFoundException,
            AccountPermanentlyDisabledException, AccountTemporarilyDisabledException;

}
