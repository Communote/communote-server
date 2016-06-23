package com.communote.server.core.user;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.util.Assert;

import com.communote.server.api.ServiceLocator;
import com.communote.server.model.user.User;
import com.communote.server.model.user.UserAuthority;
import com.communote.server.model.user.UserRole;

/**
 * The Class UserAuthorityHelper contains helper methods for the user authorities and user roles.
 * <p>
 * TODO Move helper to helper package
 * <p>
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class UserAuthorityHelper {

    /**
     * Test whether the granted authorities contain a given role.
     *
     * @param authorities
     *            the granted authorities of a user as returned by
     *            {@link #getGrantedAuthorities(User)}
     * @param role
     *            the role to test
     * @return true if the role is contained in the authorities
     */
    public static boolean containsRole(Collection<GrantedAuthority> authorities, UserRole role) {
        for (GrantedAuthority grantedAuthority : authorities) {
            if (role.getValue().equals(
                    grantedAuthority.getAuthority())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets a set of all available user roles.
     *
     * @return the roles
     */
    public static Set<UserRole> getAllRoles() {
        Set<UserRole> roles = new HashSet<UserRole>();
        for (String s : UserRole.literals()) {
            roles.add(UserRole.fromString(s));
        }
        return roles;
    }

    /**
     * Gets the assignable roles for an user.
     *
     * @param user
     *            the user
     * @return the assignable roles
     */
    public static UserRole[] getAssignableRoles(User user) {
        Set<UserRole> roles = getUserRoles(user);
        Collection<?> result = CollectionUtils.subtract(getAllRoles(), roles);
        return result.toArray(new UserRole[result.size()]);
    }

    /**
     * Gets the granted Spring Security authorities of an user.
     *
     * @param user
     *            the user
     * @return the granted authorities
     */
    public static Collection<GrantedAuthority> getGrantedAuthorities(User user) {
        Set<UserRole> userRoles = getUserRoles(user);
        Collection<GrantedAuthority> grantedAuthorities = new ArrayList<GrantedAuthority>();
        for (UserRole role : userRoles) {
            grantedAuthorities.add(new SimpleGrantedAuthority(role.getValue()));
        }
        return grantedAuthorities;
    }

    /**
     * Creates a set of UserAuthority entities from UserRole's.
     *
     * @param roles
     *            the roles
     * @return the set of user authorities
     */
    public static Set<UserAuthority> getUserAuthorities(UserRole[] roles) {
        Set<UserAuthority> authorities = new HashSet<UserAuthority>();
        if (roles != null) {
            for (UserRole role : roles) {
                UserAuthority authority = UserAuthority.Factory.newInstance();
                authority.setRole(role);
                authorities.add(authority);
            }
        }
        return authorities;
    }

    /**
     * Gets the user roles.
     *
     * @param user
     *            the user
     * @return the user roles
     */
    public static Set<UserRole> getUserRoles(User user) {
        Assert.notNull(user, "user can not be null on calling getUserRoles");
        return getUserRoles(user.getId());
    }

    /**
     * Gets the user roles.
     *
     * @param userId
     *            the ID of the user
     * @return the user roles
     */
    public static Set<UserRole> getUserRoles(Long userId) {
        Assert.notNull(userId, "userId can not be null on calling getUserRoles");
        Set<UserRole> result = new HashSet<UserRole>();
        UserRole[] roles = ServiceLocator.findService(UserManagement.class).getRolesOfUser(
                userId);
        if (roles != null) {
            for (UserRole r : roles) {
                result.add(r);
            }
        }
        return result;
    }

    /**
     * Checks if a user has a given user role.
     *
     * @param user
     *            the user
     * @param role
     *            the role
     * @return true, if the user has the authority
     */
    public static boolean hasAuthority(User user, UserRole role) {
        return getUserRoles(user).contains(role);
    }

    /**
     * Checks if a user has a given user role.
     *
     * @param userId
     *            ID of the user
     * @param role
     *            the role
     * @return true, if the user has the authority
     */
    public static boolean hasAuthority(Long userId, UserRole role) {
        return getUserRoles(userId).contains(role);
    }

    /**
     * Instantiates a new user authority helper.
     */
    private UserAuthorityHelper() {
    }
}
