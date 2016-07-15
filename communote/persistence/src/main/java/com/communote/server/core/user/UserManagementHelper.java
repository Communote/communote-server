package com.communote.server.core.user;

import java.text.DateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import org.apache.commons.lang.StringUtils;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.config.type.ClientProperty;
import com.communote.server.core.config.ClientConfigurationHelper;
import com.communote.server.core.security.SecurityHelper;
import com.communote.server.core.user.helper.ValidationPatterns;
import com.communote.server.model.user.User;
import com.communote.server.model.user.UserRole;

/**
 * Helper class for user management which amongst others provides methods for checking the limit of
 * activated user accounts of the user.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public final class UserManagementHelper {

    private static final String UNSUPPORTED_CHARACTER_REPLACEMENT = "_";
    private static final String DEFAULT_USER_ALIAS = "user";

    private static UserProfileManagement USER_PROFILE_MANAGEMENT;

    /**
     * Gets the user limit.
     *
     * @return the limit
     */
    public static long getCountLimit() {
        return CommunoteRuntime.getInstance().getConfigurationManager()
                .getClientConfigurationProperties()
                .getProperty(ClientProperty.USER_MANAGEMENT_USER_LIMIT, 0L);
    }

    /**
     * <p>
     * Returns the offset in milliseconds to add to UTC to get the time for the current user
     * respecting the effective user time zone and daylight savings.
     * </p>
     * <b>Note:</b> The offset is for the servers time.
     *
     * @return the offset in milliseconds
     */
    public static int getCurrentOffsetOfEffectiveUserTimeZone() {
        TimeZone timeZone = handleGetEffectiveUserTimeZone(SecurityHelper.getCurrentUserId(), null);
        int offset = timeZone.getRawOffset() - TimeZone.getDefault().getRawOffset();
        if (timeZone.inDaylightTime(new Date())) {
            offset += timeZone.getDSTSavings();
        }
        return offset;
    }

    /**
     * Returns the offset in milliseconds to add to UTC to get the current time for the current user
     * respecting the effective user time zone and daylight savings.
     *
     * @return the offset in milliseconds
     */
    public static int getCurrentUtcOffsetOfEffectiveUserTimeZone() {
        TimeZone timeZone = handleGetEffectiveUserTimeZone(SecurityHelper.getCurrentUserId(), null);
        return timeZone.getOffset(System.currentTimeMillis());
    }

    /**
     * Get the date formatter for the given locale which can be used to create a readable localized
     * date-time string. e.g. for ...<br>
     * German: 17. November 2009 15:00:02 MEZ<br>
     * United Kingdom: 17 November 2009 15:26:32 CET<br>
     * United States: November 17, 2009 3:26:32 PM CET
     *
     * @param userId
     *            the user id
     * @param locale
     *            the locale
     * @return the date formatter
     */
    public static DateFormat getDateFormat(Long userId, Locale locale) {
        DateFormat format = DateFormat
                .getDateTimeInstance(DateFormat.LONG, DateFormat.FULL, locale);
        TimeZone effectiveUserTimeZone = UserManagementHelper.getEffectiveUserTimeZone(userId);
        format.setTimeZone(effectiveUserTimeZone);
        return format;
    }

    /**
     * Gets the effective time zone for the currently logged in user.<br>
     * <br>
     * If no time zone exists for the user, this method returns the client time zone as fall back.<br>
     * If no time zone exists for the client, this method returns the system time zone as fall back.
     *
     * @return timeZone the effective time zone for the current user
     */
    public static TimeZone getEffectiveUserTimeZone() {
        return handleGetEffectiveUserTimeZone(SecurityHelper.getCurrentUserId(), null);
    }

    /**
     * Gets the effective time zone for the user.<br>
     * <br>
     * If no time zone exists for the user, this method returns the client time zone as fall back.<br>
     * If no time zone exists for the client, this method returns the system time zone as fall back.
     *
     * @param userId
     *            the id of a user
     *
     * @return timeZone the effective time zone for the user
     */
    public static TimeZone getEffectiveUserTimeZone(Long userId) {
        return handleGetEffectiveUserTimeZone(userId, null);
    }

    /**
     * Gets the effective time zone for the user.<br>
     * <br>
     * If no time zone exists for the user, this method returns the client time zone as fall back.<br>
     * If no time zone exists for the client, this method returns the defined time zone as fall
     * back.
     *
     * @param userId
     *            the id of a user
     * @param fallback
     *            the defined fall back
     * @return The users time zone or the fall back if null.
     */
    public static TimeZone getEffectiveUserTimeZone(Long userId, TimeZone fallback) {
        return handleGetEffectiveUserTimeZone(userId, fallback);
    }

    /**
     * Gets the effective time zone for the currently logged in user.<br>
     * <br>
     * If no time zone exists for the user, this method returns the client time zone as fall back.<br>
     * If no time zone exists for the client, this method returns the defined time zone as fall
     * back.<br>
     *
     * @param fallback
     *            the defined fall back
     * @return The users time zone or the fall back if null.
     */
    public static TimeZone getEffectiveUserTimeZone(TimeZone fallback) {
        return handleGetEffectiveUserTimeZone(SecurityHelper.getCurrentUserId(), fallback);
    }

    /**
     * @return the fall back locale if non is defined
     */
    public static Locale getFallbackLocale() {
        return Locale.ENGLISH;
    }

    /**
     * Converts an email address, only the local part of it, into a valid user alias. If the address
     * does not have local part the string 'user' will be returned.
     *
     * @param address
     *            the email address to convert
     * @return the legal alias
     */
    public static String getLegalAliasFromEmailAddress(String address) {
        // remove all \"
        String localPart = address.replaceAll("\\\\\"", "");
        int idx = address.indexOf("\"@");
        if (idx < 0) {
            idx = address.indexOf("@");
        }
        if (idx <= 0) {
            // no local part found
            return DEFAULT_USER_ALIAS;
        }
        localPart = address.substring(0, idx);
        // some clean-up
        localPart = localPart.replaceAll("\"", "");
        if (StringUtils.isBlank(localPart)) {
            localPart = DEFAULT_USER_ALIAS;
        } else {
            localPart = localPart.replaceAll(ValidationPatterns.UNSUPPORTED_CHARACTERS_IN_ALIAS,
                    UNSUPPORTED_CHARACTER_REPLACEMENT);
            if (localPart.endsWith(".")) {
                localPart = localPart.substring(0, localPart.length() - 1);
                if (localPart.length() == 0) {
                    localPart = DEFAULT_USER_ALIAS;
                }
            }
        }
        return localPart;
    }

    /**
     * Groups a collection of users by their localization settings.
     *
     * @param users
     *            the users
     * @return the users grouped by localization
     */
    public static Map<Locale, Collection<User>> getUserByLocale(Collection<User> users) {
        return getUserByLocale(users, null);
    }

    /**
     * Groups a collection of users by their localization settings.
     *
     * @param users
     *            the user collection
     * @param usersToSkip
     *            a subset of the users collection containing those users that should be included in
     *            the result. Can be null.
     * @return the users grouped by localization
     */
    public static Map<Locale, Collection<User>> getUserByLocale(Collection<User> users,
            Collection<User> usersToSkip) {
        Map<Locale, Collection<User>> result = new HashMap<Locale, Collection<User>>();
        if (users != null) {
            for (User user : users) {
                if (usersToSkip == null || !usersToSkip.contains(user)) {
                    Collection<User> localizedUsers = result.get(user.getLanguageLocale());
                    if (localizedUsers == null) {
                        localizedUsers = new HashSet<User>();
                        result.put(user.getLanguageLocale(), localizedUsers);
                    }
                    localizedUsers.add(user);
                }
            }
        }
        return result;
    }

    /**
     * @return the lazily initialized UserProfileManagement
     */
    private static UserProfileManagement getUserProfileManagement() {
        if (USER_PROFILE_MANAGEMENT == null) {
            USER_PROFILE_MANAGEMENT = ServiceLocator.findService(UserProfileManagement.class);
        }
        return USER_PROFILE_MANAGEMENT;
    }

    /**
     * Gets the effective time zone for the user.<br>
     * <br>
     * If no time zone exists for the user, this method returns the client time zone as fall back.<br>
     * If no time zone exists for the client, this method returns a defined fall back or the system
     * time zone.
     *
     * @param userId
     *            id of the a user
     * @param fallback
     *            the defined fall back
     * @return timeZone the effective time zone for the user
     */
    private static TimeZone handleGetEffectiveUserTimeZone(Long userId, TimeZone fallback) {
        TimeZone timeZone = null;

        UserProfileDetails userProfileDetails = null;

        if (!SecurityHelper.isPublicUser() && userId != null) {
            userProfileDetails = getUserProfileManagement()
                    .getUserProfileDetailsById(userId, false);
        }

        // try to use the user time zone
        if (userProfileDetails != null && userProfileDetails.getTimeZoneId() != null) {
            timeZone = TimeZone.getTimeZone(userProfileDetails.getTimeZoneId());
        }
        // try to use the client time zone
        else {
            String timeZoneString = CommunoteRuntime.getInstance().getConfigurationManager()
                    .getClientConfigurationProperties().getClientTimeZoneId();
            if (timeZoneString != null) {
                timeZone = TimeZone.getTimeZone(timeZoneString);
            }
            // use fall back definition if exists
            else if (fallback != null) {
                timeZone = fallback;
            }
            // finally use the system time zone
            else {
                timeZone = TimeZone.getDefault();
            }
        }
        return timeZone;
    }

    /**
     * Get the Value of ConfigurationPropertyConstants.Client.AUTOMATIC_USER_ACTIVATION for better
     * code reading.
     *
     * @return value of ConfigurationPropertyConstants.Client.AUTOMATIC_USER_ACTIVATION
     */
    public static boolean isAutomaticUserActivation() {
        return CommunoteRuntime
                .getInstance()
                .getConfigurationManager()
                .getClientConfigurationProperties()
                .getProperty(ClientProperty.AUTOMATIC_USER_ACTIVATION,
                        ClientConfigurationHelper.DEFAULT_AUTOMATIC_USER_ACTIVATION);
    }

    /**
     *
     * @param user
     *            the user to check
     * @return true if the is is a system user
     */
    public static boolean isSystemUser(User user) {
        for (UserRole role : user.getRoles()) {
            if (UserRole.ROLE_SYSTEM_USER.equals(role)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Private constructor because instantiation of this class is not allowed.
     */
    private UserManagementHelper() {
        // Do nothing.
    }
}
