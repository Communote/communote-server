package com.communote.server.core.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.switchuser.SwitchUserGrantedAuthority;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.model.user.UserRole;
import com.communote.server.model.user.User;

/**
 * This helper contains methods to switch a user context. Switch user means that the current
 * authentication will be hold and another user will be set as authenticated allowing to do
 * operations for the target user without him logging on. Typical use case is the message queue
 * component, which only authenticated a system user and then process messages in the context of
 * another user.
 *
 * The current authentication will be stored in a {@link SwitchUserGrantedAuthority} of the new user
 * which then will be used when switching back.
 *
 * The target user will only have the roles as defined in {@link #ROLE_SWITCH_ORGINAL_USER}, all
 * other roles will not be used as authority for the target user. Therefore the target user will not
 * have client admin rights, even if the admin role is assigned in the database.
 *
 * The switching of a user is only allowed if the current user has a system role authority (
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 * @see UserRole#ROLE_SYSTEM_USER
 */
public final class SwitchUserHelper {

    private final static Logger LOGGER = LoggerFactory.getLogger(SwitchUserHelper.class);

    /**
     * the authority in the switched users context
     */
    private static final String ROLE_SWITCH_ORGINAL_USER = "ROLE_SWITCH_ORIGINAL_USER";

    /**
     * the set of roles the switched user can have
     */
    private final static Set<String> ALLOWED_SWITCH_ROLE_NAMES;

    static {
        Set<String> help = new HashSet<String>();
        help.add(UserRole.ROLE_KENMEI_USER.getValue());
        help.add(UserRole.ROLE_SYSTEM_USER.getValue());

        ALLOWED_SWITCH_ROLE_NAMES = Collections.unmodifiableSet(help);
    }

    /**
     *
     * @return true if the current user can switch to another user
     */
    public static boolean canSwitchUser() {
        return SecurityHelper.hasRole(UserRole.ROLE_SYSTEM_USER);
    }

    /**
     * Create an authentication for the target user that will contain the current auth as granted
     * authentication. This method does not do any checking if the current user is actually alowed
     * to do the switching (therefore it is a private method).
     *
     * @param targetUser
     *            the user for the new authentication
     * @return the authentication of the target user
     */
    private static Authentication createSwitchUserAuthentication(User targetUser) {

        UsernamePasswordAuthenticationToken targetUserAuthentication;

        Authentication currentAuth;

        try {
            // Check first if we are already switched.
            currentAuth = removeSwitchedUser();
        } catch (AuthenticationCredentialsNotFoundException e) {
            currentAuth = SecurityContextHolder.getContext().getAuthentication();
        }

        org.springframework.security.core.userdetails.User targetUserDetails = new UserDetails(
                targetUser, targetUser.getAlias());

        GrantedAuthority switchAuthority = new SwitchUserGrantedAuthority(ROLE_SWITCH_ORGINAL_USER,
                currentAuth);

        // add the new switch user authority
        List<GrantedAuthority> newAuths = new ArrayList<GrantedAuthority>();
        for (GrantedAuthority authority : targetUserDetails.getAuthorities()) {
            // only use roles that are allowed
            if (ALLOWED_SWITCH_ROLE_NAMES.contains(authority.getAuthority())) {
                newAuths.add(authority);
            }
        }
        newAuths.add(switchAuthority);

        // create the new authentication token
        targetUserAuthentication = new UsernamePasswordAuthenticationToken(targetUserDetails,
                targetUser.getPassword(), newAuths);

        return targetUserAuthentication;
    }

    /**
     * @return if there is a switched user return the original one, returns null if there is no
     *         switched user
     */
    public static Authentication getOriginalAuthentication() {

        // need to check to see if the current user has a SwitchUserGrantedAuthority
        Authentication current = SecurityContextHolder.getContext().getAuthentication();

        if (current == null) {
            return null;
        }

        Authentication original = null;
        // iterate over granted authorities and find the 'switch user' authority
        Collection<? extends GrantedAuthority> authorities = current.getAuthorities();

        for (GrantedAuthority auth : authorities) {
            // check for switch user type of authority
            if (auth instanceof SwitchUserGrantedAuthority) {
                original = ((SwitchUserGrantedAuthority) auth).getSource();
                LOGGER.debug("Found original switch user granted authority [ {} ]", original);
            }
        }

        return original;
    }

    /**
     *
     * @return true if there is a currently switched user
     */
    public static boolean isUserSwitched() {
        return getOriginalAuthentication() != null;
    }

    /**
     * Throws an exception in case no switched user exists
     *
     * @return remove a switched user
     */
    public static Authentication removeSwitchedUser() {
        Authentication original = getOriginalAuthentication();
        if (original == null) {
            throw new AuthenticationCredentialsNotFoundException(
                    "Switched user authority not found!");
        }
        SecurityContextHolder.getContext().setAuthentication(original);
        LOGGER.debug("Switched back to original user: {}", original);

        return original;
    }

    /**
     * Switch the user to the target user. Method will thrown an AccessDeniedException if the
     * current user is not allowed to switch the user.
     *
     * No admin roles will be granted to the target user if even if its a client admin in the
     * databse.
     *
     * @param targetUser
     *            the user to switch to
     * @return get the authentication for the target user
     * @throws SwitchUserNotAllowedException
     *             in case the switching is not allowed
     */
    public static Authentication switchUser(User targetUser) throws SwitchUserNotAllowedException {
        // only let the current user do it
        try {
            SecurityHelper.assertCurrentUserRole(UserRole.ROLE_SYSTEM_USER);
        } catch (AuthorizationException e) {
            throw new SwitchUserNotAllowedException(SecurityHelper.getCurrentUserId(),
                    targetUser.getId(), "Current user is not allowed to switch to a user", e);
        }

        Authentication target = createSwitchUserAuthentication(targetUser);
        if (target != null) {
            ServiceLocator.findService(AuthenticationManagement.class).onSuccessfulAuthentication(
                    target);
            LOGGER.debug("Switched to target user: {}", target);
        }
        return target;
    }

    /**
     * We are a helper class
     */
    private SwitchUserHelper() {
        // nothing to implement here
    }
}
