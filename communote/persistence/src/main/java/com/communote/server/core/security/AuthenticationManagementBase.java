package com.communote.server.core.security;

import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.communote.server.api.core.user.UserNotFoundException;
import com.communote.server.model.user.User;

/**
 * <p>
 * Spring Service base class for
 * <code>com.communote.server.core.api.security.AuthenticationManagement</code>, provides access to
 * all services and entities referenced by this service.
 * </p>
 *
 * @see com.communote.server.core.security.AuthenticationManagement
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
@Transactional(propagation = Propagation.REQUIRED)
public abstract class AuthenticationManagementBase implements AuthenticationManagement {

    /**
     * Performs the core logic for {@link #onSuccessfulAuthentication(Authentication)}
     */
    protected abstract User handleOnSuccessfulAuthentication(Authentication authentication);

    protected abstract void handleOnSuccessfulAuthentication(Long userId)
            throws UserNotFoundException;

    protected abstract void handleValidateUserLogin(Long userId)
            throws AccountPermanentlyLockedException, AccountTemporarilyLockedException,
            AccountNotActivatedException, TermsNotAcceptedException, UserNotFoundException,
            AccountPermanentlyDisabledException, AccountTemporarilyDisabledException;

    @Override
    @Transactional(propagation = Propagation.NEVER)
    public User onSuccessfulAuthentication(Authentication authentication) {
        if (authentication == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.core.api.security.AuthenticationManagement."
                            + "onSuccessfulAuthentication - 'authentication' can not be null");
        }
        try {
            return this.handleOnSuccessfulAuthentication(authentication);
        } catch (RuntimeException rt) {
            throw new AuthenticationManagementException(
                    "Error performing 'com.communote.server.core.api.security.AuthenticationManagement."
                            + "onSuccessfulAuthentication' --> " + rt, rt);
        }
    }

    @Override
    public void onSuccessfulAuthentication(Long userId) throws UserNotFoundException {
        if (userId == null) {
            throw new IllegalArgumentException("userId must not be null");
        }
        try {
            this.handleOnSuccessfulAuthentication(userId);
        } catch (RuntimeException rt) {
            throw new com.communote.server.core.security.AuthenticationManagementException(
                    rt.getMessage(), rt);
        }
    }

    @Override
    public void validateUserLogin(Long userId) throws AccountPermanentlyLockedException,
            AccountTemporarilyLockedException, AccountNotActivatedException,
            TermsNotAcceptedException, UserNotFoundException, AccountPermanentlyDisabledException,
            AccountTemporarilyDisabledException {
        if (userId == null) {
            throw new IllegalArgumentException("userId cannot be null");
        }
        try {
            this.handleValidateUserLogin(userId);
        } catch (RuntimeException rt) {
            throw new com.communote.server.core.security.AuthenticationManagementException(
                    "Error performing 'com.communote.server.core.api.security.AuthenticationManagement.validateUserLogin()' --> "
                            + rt, rt);
        }
    }
}