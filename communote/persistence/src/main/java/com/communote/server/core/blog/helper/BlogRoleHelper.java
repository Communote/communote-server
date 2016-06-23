package com.communote.server.core.blog.helper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.communote.server.model.blog.BlogRole;
import com.communote.server.persistence.common.messages.ResourceBundleManager;

/**
 * Helper class for working with blog role constants.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public final class BlogRoleHelper {

    /** message key for the localized user roles. */
    private static final String KEY_USER_GROUP_ROLE = "user.group.role";

    /** cached localized user roles. */
    private static Map<Locale, Map<BlogRole, String>> ROLES = new HashMap<Locale, Map<BlogRole, String>>();

    /**
     * Convert a numeric representation of a role into the role object.
     * 
     * @param numericRole
     *            the numeric representation of the role
     * @return the role
     * @see BlogRoleHelper#convertRoleToNumeric(BlogRole)
     */
    public static BlogRole convertNumericToRole(int numericRole) {
        BlogRole role;
        if (numericRole <= 1) {
            role = BlogRole.VIEWER;
        } else if (numericRole == 2) {
            role = BlogRole.MEMBER;
        } else {
            role = BlogRole.MANAGER;
        }
        return role;
    }

    /**
     * Convert a role into a numeric representation. The higher the returned value the higher is the
     * access right.
     * 
     * @param role
     *            the role to convert
     * @return the numeric representation of the role
     */
    public static int convertRoleToNumeric(BlogRole role) {
        int numericRole;
        if (role.equals(BlogRole.VIEWER)) {
            numericRole = 1;
        } else if (role.equals(BlogRole.MEMBER)) {
            numericRole = 2;
        } else {
            numericRole = 3;
        }
        return numericRole;
    }

    /**
     * Gets the localized user group roles. Null value can be used for no role access
     * 
     * @param locale
     *            the locale
     * @return the user group roles
     */
    public static Map<BlogRole, String> getBlogRoles(Locale locale) {
        Map<BlogRole, String> result = null;
        result = ROLES.get(locale);
        if (result == null) {
            result = new HashMap<BlogRole, String>();
            ResourceBundleManager resources = ResourceBundleManager.instance();
            for (String key : BlogRole.names()) {
                String role = resources.getText(KEY_USER_GROUP_ROLE + "." + key.toLowerCase(),
                        locale);
                if (StringUtils.isBlank(role)) {
                    role = key;
                }
                result.put(BlogRole.fromString(key), role);
            }
            result.put(null, "user.group.role.none");
            ROLES.put(locale, result);
        }
        return result;
    }

    /**
     * @return the roles sorted by access level (starting with read access)
     */
    public static List<String> getBlogRolesSortedByAccess() {
        List<String> roles = new ArrayList<String>(3);
        roles.add(BlogRole.VIEWER.getValue());
        roles.add(BlogRole.MEMBER.getValue());
        roles.add(BlogRole.MANAGER.getValue());

        return roles;
    }

    /**
     * Get back the upper role of given roles
     * 
     * @param firstRole
     *            BlogRole
     * @param secRole
     *            BolgRole
     * @return the of the role
     */
    public static BlogRole getUpperRole(BlogRole firstRole, BlogRole secRole) {
        int numFirstRole = convertRoleToNumeric(firstRole), numSecRole = convertRoleToNumeric(secRole);
        if (numFirstRole >= numSecRole) {
            return firstRole;
        } else {
            return secRole;
        }
    }

    /**
     * Return whether a role represents the same or a higher access right than another role
     * 
     * @param actualRole
     *            the role to evaluate. Can be null. In that case the role is interpreted as no
     *            access and thus, false is returned.
     * @param requiredRole
     *            the role representing the access right the other role should fulfill. Can be null.
     *            In that case the role is interpreted as no access is required and thus, true is
     *            returned.
     * @return true if the actualRole represents an access right that is higher or equal to the
     *         required role, otherwise false is returned
     */
    public static boolean sufficientAccess(BlogRole actualRole, BlogRole requiredRole) {
        if (requiredRole == null) {
            return true;
        }
        if (actualRole == null) {
            return false;
        }
        int numericActualRole = BlogRoleHelper.convertRoleToNumeric(actualRole);
        int numericRequiredRole = BlogRoleHelper.convertRoleToNumeric(requiredRole);
        return numericActualRole >= numericRequiredRole;
    }

    /**
     * private constructor
     */
    private BlogRoleHelper() {

    }
}
