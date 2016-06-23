package com.communote.server.core.security;

import java.util.Collection;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import com.communote.common.converter.IdentityConverter;
import com.communote.common.util.NumberHelper;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.core.user.UserAuthorityHelper;
import com.communote.server.core.user.UserManagement;
import com.communote.server.model.user.User;
import com.communote.server.model.user.UserRole;

/**
 * This helper class checks the current user in the acegi system and returns the details
 * {@link UserDetails}. This details contain the email, locale and id for further usage.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public final class SecurityHelper {

    /**
     * Assert that there is a current user and load the user of it
     *
     * @return The user.
     * @deprecated this method should not be used since it returns the user entity which can be an
     *             uninitialized hibernate proxy. Use {@link #assertCurrentUserId()} instead and
     *             pass the returned userId together with an appropriate Converter (which is in most
     *             cases not the IdentityConverter!) to UserManagement.getUserById.
     */
    @Deprecated
    public static User assertCurrentKenmeiUser() {
        // using assertCurrentUserId to fail when the current user is the public user
        return ServiceLocator.findService(UserManagement.class).getUserById(assertCurrentUserId(),
                new IdentityConverter<User>());
    }

    /**
     * Checks if there is a user logged in. If not an AccessDeniedException will be thrown.
     *
     * @return The user details if everything is ok
     */
    public static UserDetails assertCurrentUser() {
        UserDetails user = getCurrentUser();
        if (user == null) {
            throw new AccessDeniedException("No user logged in, but is required");
        }
        return user;
    }

    /**
     * Gets the current logged in user and checks if the given id is matching. If not an
     * AccessDeniedException will be thrown.
     *
     * @param userId
     *            The user id to assert for the current user
     * @return The user details if everything is ok
     */
    public static UserDetails assertCurrentUser(Long userId) {
        UserDetails user = getCurrentUser();
        if (user == null) {
            throw new AccessDeniedException(
                    "Current user is not logged in but should be expected: user id=" + userId);
        }
        if (!user.getUserId().equals(userId)) {
            throw new AccessDeniedException(
                    "Current user is not expected one! Current user: userName="
                            + user.getUsername() + " id=" + user.getUserId() + "; Expected: id="
                            + userId);
        }
        return user;
    }

    /**
     * Get the current user id. Throw an exception if there is no current user.
     *
     * @return The user id
     */
    public static Long assertCurrentUserId() {
        Long userId = getCurrentUserId();
        if (userId == null) {
            throw new AccessDeniedException("No current user available!");
        }
        return userId;
    }

    /**
     * Checks if there is a user logged in and the user is client manager. If not an
     * AccessDeniedException will be thrown.
     * <p>
     * <b>Note:</b> this method should usually not be used because it throws the wrong exception!
     * Especially in service layer an {@link AuthorizationException} should be thrown which has no
     * spring dependency and is checked and thus allows better handling.
     * </p>
     *
     * @return The user details if everything is ok
     */
    public static UserDetails assertCurrentUserIsClientManager() {
        if (!isClientManager()) {
            throw new AccessDeniedException(
                    "No user logged in or not client manager, but is required");
        }
        return getCurrentUser();
    }

    /**
     * Checks if there is a user logged in with user role of user or public user. If not an
     * AccessDeniedException will be thrown.
     *
     * @return true if everything is okay
     */
    public static boolean assertCurrentUserOrPublicUser() {
        if (isCurrentUserOrPublicUser()) {
            return true;
        } else {
            throw new AccessDeniedException("No user or public user logged in, but is required");
        }
    }

    /**
     * Asserts that the current user has the given role
     *
     * @param role
     *            the role to check
     * @throws AuthorizationException
     *             in case the user does not has the role
     */
    public static void assertCurrentUserRole(UserRole role) throws AuthorizationException {
        if (!hasRole(role)) {
            throw new AuthorizationException("Current user does not has required role " + role
                    + ".");
        }
    }

    /**
     * Get the current user which is set in the acegi security context.
     *
     * @return The <code>UserDetails</code> of the current user or null if no user is logged in
     */
    public static UserDetails getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        UserDetails user = null;

        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            Object principial = authentication.getPrincipal();
            user = (UserDetails) principial;
        }
        return user;

    }

    /**
     * Get the alias of the current user, or null if no user is logged in
     *
     * @return The user alias
     */
    public static String getCurrentUserAlias() {
        UserDetails userDetails = getCurrentUser();
        String userAlias = null;
        if (userDetails != null) {
            userAlias = userDetails.getUserAlias();
        }
        return userAlias;
    }

    /**
     * Get the current user id, or null if no user is logged in
     *
     * @return The user id
     */
    public static Long getCurrentUserId() {
        UserDetails userDetails = getCurrentUser();
        Long userId = null;
        if (userDetails != null) {
            userId = userDetails.getUserId();
        }
        return userId;
    }

    /**
     *
     * @param authority
     *            the authority to check
     * @return true if the cucrrent user got this authority
     */
    private static boolean hasGrantedAuthority(String authority) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null) {
            Collection<? extends GrantedAuthority> grantedAutorities = auth.getAuthorities();

            if (auth.getPrincipal() instanceof UserDetails
                    && grantedAutorities.contains(new SimpleGrantedAuthority(authority))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks that the current user has the given role
     *
     * @param role
     *            the role to check
     * @return true if th current user has this role
     */
    public static boolean hasRole(UserRole role) {
        UserDetails user = getCurrentUser();
        if (user != null) {
            return UserAuthorityHelper.containsRole(user.getAuthorities(), role);
        }
        return false;
    }

    /**
     * @return true if the current user is client manager
     */
    public static boolean isClientManager() {
        UserDetails user = getCurrentUser();
        if (user != null) {
            for (GrantedAuthority grantedAuthority : user.getAuthorities()) {
                if (UserRole.ROLE_KENMEI_CLIENT_MANAGER.getValue().equals(
                        grantedAuthority.getAuthority())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * check if the current user id matches the given id
     *
     * @param userId
     *            the user id to check
     * @return true if the given id is not null and matches the current log-in user
     */
    public static boolean isCurrentUserId(Long userId) {
        return userId != null && NumberHelper.equals(userId, getCurrentUserId());
    }

    /**
     * Checks if there is a user logged in with user role of user or public user.
     *
     * @return true if everything is okay
     */
    public static boolean isCurrentUserOrPublicUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.getPrincipal() instanceof UserDetails) {
            Collection<? extends GrantedAuthority> grantedAutorities = auth.getAuthorities();

            if (grantedAutorities.contains(new SimpleGrantedAuthority(
                    AuthenticationHelper.PUBLIC_USER_ROLE))
                    || grantedAutorities.contains(new SimpleGrantedAuthority(
                            AuthenticationHelper.KENMEI_USER_ROLE))) {
                return true;
            }
        }

        return false;
    }

    /**
     * @return true if the current "user" is the internal system
     */
    public static boolean isInternalSystem() {
        return hasGrantedAuthority(AuthenticationHelper.ROLE_INTERNAL_SYSTEM);
    }

    /**
     * Checks the security context if the authenticated user is in role of the public user.
     *
     * @return true if the current user has user role ROLE_PUBLIC_USER
     */
    public static boolean isPublicUser() {
        return hasGrantedAuthority(AuthenticationHelper.PUBLIC_USER_ROLE);
    }

    /**
     * Do not construct me
     */
    private SecurityHelper() {

    }
}
