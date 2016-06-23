package com.communote.server.test.util;

import org.springframework.security.core.Authentication;

import com.communote.server.api.ServiceLocator;
import com.communote.server.core.security.AuthenticationHelper;
import com.communote.server.core.user.UserManagement;
import com.communote.server.model.user.User;
import com.communote.server.model.user.UserRole;
import com.communote.server.model.user.UserStatus;

/**
 * Helper class for authentication related operations.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class AuthenticationTestUtils {

    /**
     *
     * @param authentication
     *            the authentication to set to the security context, using <code>null</code> will
     *            remove an existing authentication
     */
    public static void setAuthentication(Authentication authentication) {
        AuthenticationHelper.setAuthentication(authentication);
    }

    /**
     * Method which searches for active managers and puts the first into the context.
     *
     * @return The original authentication.
     */
    public static Authentication setManagerContext() {
        return AuthenticationHelper.setAsAuthenticatedUser(ServiceLocator.instance()
                .getService(UserManagement.class)
                .findUsersByRole(UserRole.ROLE_KENMEI_CLIENT_MANAGER, UserStatus.ACTIVE).get(0));
    }

    /**
     * Set the given user to the security context.
     *
     * @param user
     *            The user to set in the context
     * @return the authentication that was replaced by the new one, can be null
     */
    public static Authentication setSecurityContext(User user) {
        return AuthenticationHelper.setAsAuthenticatedUser(user);
    }

    /**
     *
     * @param newAuthentication
     *            the new authentication to use.
     */
    public static void unsetManagerContext(Authentication newAuthentication) {
        AuthenticationHelper.setAuthentication(newAuthentication);
    }

    /**
     * Helper
     */
    private AuthenticationTestUtils() {
    }
}
