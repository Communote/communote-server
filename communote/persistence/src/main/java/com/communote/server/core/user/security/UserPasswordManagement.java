package com.communote.server.core.user.security;

import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.api.core.user.UserNotFoundException;
import com.communote.server.core.common.exceptions.PasswordValidationException;
import com.communote.server.core.user.ExternalUserPasswordChangeNotAllowedException;
import com.communote.server.model.user.User;
import com.communote.server.persistence.common.security.SecurityCodeNotFoundException;

/**
 * Manage the passwords of users stored in the database.
 *
 * @author Communote Team - <a href="https://github.com/Communote">https://github.com/Communote</a>
 * @since 3.5
 */
public interface UserPasswordManagement {

    /**
     * Change the password of a user.
     *
     * @param userId
     *            the ID of the user for whom the password should be changed
     * @param newPassword
     *            the new clear text password
     * @throws PasswordValidationException
     *             in case the new password doesn't fulfill the minimum security requirements to be
     *             used in user accounts
     * @throws UserNotFoundException
     *             in case the user with the given ID does not exist
     * @throws AuthorizationException
     *             in case the current user is not the user with the userId or the current user is
     *             not client admin or internal system user
     * @throws ExternalUserPasswordChangeNotAllowedException
     *             in case the user was provided by the primary external user repository
     */
    void changePassword(Long userId, String newPassword)
            throws PasswordValidationException, UserNotFoundException, AuthorizationException,
            ExternalUserPasswordChangeNotAllowedException;

    /**
     * Change the password of a user who used the forgotten password feature
     * ({@link #requestPasswordChange(String)}) to request a new password.
     *
     * @param securityCode
     *            security code created by the forgotten password feature
     * @param newPassword
     *            the new clear text password
     * @throws SecurityCodeNotFoundException
     *             in case the security code does not exist or does not belong to a forgotten
     *             password security code
     * @throws PasswordValidationException
     *             in case the new password doesn't fulfill the minimum security requirements to be
     *             used in user accounts
     * @throws ExternalUserPasswordChangeNotAllowedException
     *             in case the user was provided by the primary external user repository
     */
    void changePassword(String securityCode, String newPassword)
            throws SecurityCodeNotFoundException, PasswordValidationException,
            ExternalUserPasswordChangeNotAllowedException;

    /**
     * <p>
     * Change the password of a user.
     * </p>
     * <p>
     * Note: this method does not check whether the current user is allowed to change the password.
     * Moreover, an existing transaction is required.
     * </p>
     *
     * @param user
     *            the user whose password should be changed
     * @param newPassword
     *            the new clear text password
     * @throws PasswordValidationException
     *             in case the new password doesn't fulfill the minimum security requirements to be
     *             used in user accounts
     * @throws ExternalUserPasswordChangeNotAllowedException
     *             in case the user was provided by the primary external user repository
     */
    void changePassword(User user, String newPassword)
            throws PasswordValidationException, ExternalUserPasswordChangeNotAllowedException;

    /**
     * <p>
     * Check that the (hashed) password of the user matches the given clear text password. If the
     * password matches it will be checked whether the password should be updated to meet the
     * current security requirements. An update will be necessary if the user's password was created
     * with a hash function other than the current hash function or if the hash function didn't
     * change but was reconfigured to produce a stronger hash value.
     * </p>
     * <p>
     * Note: if an update of password is necessary the given clear text password is not validated
     * with {@link #validatePassword(String)}. Moreover, an existing transaction is required.
     * </p>
     *
     * @param user
     *            the user whose password should be checked and updated
     * @param password
     *            the clear text password to test against
     * @return true if the password matches
     */
    boolean checkAndUpdatePassword(User user, String password);

    /**
     * Check that the (hashed) password of the user matches the given clear text password.
     *
     * @param userId
     *            the ID of the user whose password should be checked
     * @param password
     *            the clear text password to test against
     * @return false if the password doesn't match or the user doesn't exist
     */
    boolean checkPassword(Long userId, String password);

    /**
     * Check that the (hashed) password of the user matches the given clear text password.
     *
     * @param user
     *            the user whose password should be checked
     * @param password
     *            the clear text password to test against
     * @return true if the password matches
     */
    boolean checkPassword(User user, String password);

    /**
     * Register a hash function for generating and checking password hashes.
     *
     * @param hashFunction
     *            the function to register
     */
    void register(PasswordHashFunction hashFunction);

    /**
     * Request a new password for the given user. A mail with a security code will be sent to the
     * user. The code can be used in {@link #changePassword(String, String)}.
     *
     * @param email
     *            the email of the user requesting the password change
     * @throws UserNotFoundException
     *             in case there is no user with that email
     * @throws ExternalUserPasswordChangeNotAllowedException
     *             in case the user was provided by the primary external user repository
     */
    void requestPasswordChange(String email)
            throws UserNotFoundException, ExternalUserPasswordChangeNotAllowedException;

    /**
     * Remove a previously registered password hash function. If the given function is not
     * registered nothing will happen.
     *
     * @param hashFunction
     *            the hash function to remove
     * @throws IllegalArgumentException
     *             in case the the built-in default hash function should be removed
     */
    void unregister(PasswordHashFunction hashFunction);

    /**
     * Validate that a password matches the minimum security requirements to be used in user
     * accounts
     *
     * @param newPassword
     *            the password to validate
     * @throws PasswordValidationException
     *             in case the password is not valid
     */
    void validatePassword(String newPassword) throws PasswordValidationException;

}