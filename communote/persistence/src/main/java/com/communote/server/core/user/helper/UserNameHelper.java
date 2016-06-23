package com.communote.server.core.user.helper;

import org.apache.commons.lang.StringUtils;

import com.communote.server.api.core.note.NoteData;
import com.communote.server.api.core.user.UserData;
import com.communote.server.core.filter.listitems.UserProfileDetailListItem;
import com.communote.server.core.user.UserProfileDetails;
import com.communote.server.model.user.User;
import com.communote.server.model.user.UserName;

/**
 * Helper class to get a user to string for frontend or mails
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public final class UserNameHelper {
    private final static String BLANK = " ";

    /**
     * Get the complete signature of the user which is "'SALUATAION' 'FIRSTNAME' 'LASTNAME'". If the
     * profile is not yet set, the e-mail address will be returned.
     * 
     * @param user
     *            the user to use
     * @return the salutation or null
     */
    public static String getCompleteSignature(User user) {
        return getCompleteSignature(user, BLANK, user.getEmail());
    }

    /**
     * Get the complete signature of the user which is "'SALUATAION' 'FIRSTNAME' 'LASTNAME'". If the
     * profile is not yet set, fallback will be returned.
     * 
     * @param user
     *            the user to use
     * @param separator
     *            the separator to use between the elements
     * @param fallback
     *            the fallback in case the profile is not available
     * @return the salutation or the fallback
     */
    public static String getCompleteSignature(User user, String separator, String fallback) {
        return getCompleteSignature(user.getProfile(), separator, fallback);
    }

    /**
     * Get the compelte signature
     * 
     * @param salutation
     *            the salutation
     * @param firstName
     *            first name
     * @param lastName
     *            last name
     * @param separator
     *            the separator to use
     * @param fallback
     *            a fallback if all is blank
     * @return the signature
     */
    public static String getCompleteSignature(String salutation, String firstName, String lastName,
            String separator, String fallback) {
        StringBuilder sb = new StringBuilder();
        if (StringUtils.isNotBlank(salutation)) {
            sb.append(salutation);
            sb.append(separator);
        }
        if (StringUtils.isNotBlank(firstName)) {
            sb.append(firstName);
            sb.append(separator);
        }
        if (StringUtils.isNotBlank(lastName)) {
            sb.append(lastName);
        }
        return sb.length() == 0 ? fallback : sb.toString();
    }

    /**
     * Get the complete signature of the profile which is "'SALUATAION' 'FIRSTNAME' 'LASTNAME'". If
     * the profile is not yet set, fallback will be returned.
     * 
     * @param profile
     *            the profile to use
     * @param separator
     *            the separator to use between the elements
     * @param fallback
     *            the fallback in case the profile is not available
     * @return the salutation or the fallback
     */
    public static String getCompleteSignature(UserName profile, String separator, String fallback) {
        return getCompleteSignature(profile, separator, fallback, true);
    }

    /**
     * Get the complete signature of the profile which is "'SALUATAION' 'FIRSTNAME' 'LASTNAME'". If
     * the profile is not yet set, fallback will be returned.
     * 
     * @param profile
     *            the profile to use
     * @param separator
     *            the separator to use between the elements
     * @param fallback
     *            the fallback in case the profile is not available
     * @param includeSalutation
     *            whether to include the salutation into the returned signature
     * @return the signature or the fallback
     */
    public static String getCompleteSignature(UserName profile, String separator, String fallback,
            boolean includeSalutation) {
        if (profile != null) {
            return getCompleteSignature(includeSalutation ? profile.getSalutation() : null, profile
                    .getFirstName(), profile.getLastName(), separator, fallback);
        }
        return fallback;
    }

    /**
     * Get the default username of the user which is "'SALUATAION' 'FIRSTNAME' 'LASTNAME'". If the
     * profile is not yet set, null will be returned.
     * 
     * @param user
     *            the user to use
     * @return the salutation or null
     */
    public static String getDefaultUserSignature(User user) {
        return getCompleteSignature(user, BLANK, user.getEmail());
    }

    /**
     * Get the complete signature of the user which is "'SALUATAION' 'FIRSTNAME' 'LASTNAME'". If the
     * profile is not yet set, null will be returned.
     * 
     * @param userListItem
     *            the user list item to use
     * @return the salutation or null
     */
    public static String getDefaultUserSignature(UserData userListItem) {
        return getCompleteSignature(userListItem, BLANK, userListItem.getEmail());
    }

    /**
     * @param user
     *            the user to use
     * @return a detailed signature
     */
    public static String getDetailedUserSignature(User user) {
        String firstName = null;
        String lastName = null;
        if (user.getProfile() != null) {
            firstName = user.getProfile().getFirstName();
            lastName = user.getProfile().getLastName();
        }
        return getDetailedUserSignature(firstName, lastName, user.getAlias());
    }

    /**
     * Returns the string containing "'FIRSTNAME' 'LASTNAME' ('ALIAS')". If firstname and lastname
     * are not defined the string will look like "'ALIAS'".
     * 
     * @param firstName
     *            the firstname
     * @param lastName
     *            the lastname
     * @param alias
     *            the alias
     * @return the user string
     */
    public static String getDetailedUserSignature(String firstName, String lastName, String alias) {
        StringBuilder sb = new StringBuilder();
        if (StringUtils.isNotBlank(firstName)) {
            sb.append(firstName);
            sb.append(BLANK);
        }
        if (StringUtils.isNotBlank(lastName)) {
            sb.append(lastName);
            sb.append(BLANK);
        }
        if (sb.length() > 0) {
            sb.append("(@");
            sb.append(alias);
            sb.append(")");
        } else if (StringUtils.isNotBlank(alias)) {
            sb.append(alias);
        }
        return sb.toString();
    }

    /**
     * Returns the string containing "'FIRSTNAME' 'LASTNAME' ('ALIAS')". If firstname and lastname
     * are not defined the string will look like "'ALIAS'".
     * 
     * @param userListItem
     *            the userListItem to use
     * @return the user string
     */
    public static String getDetailedUserSignature(UserData userListItem) {
        return getDetailedUserSignature(userListItem.getFirstName(), userListItem.getLastName(),
                userListItem.getAlias());
    }

    /**
     * Returns a simple user signature consisting of "'FIRSTNAME' 'LASTNAME'" or "'ALIAS'" in case
     * firstname and lastname are not defined.
     * 
     * @param user
     *            the user to use
     * @return the signature
     */
    public static String getSimpleDefaultUserSignature(User user) {
        return getCompleteSignature(user.getProfile(), BLANK, user.getAlias(), false);
    }

    /**
     * Returns a simple user signature consisting of "'FIRSTNAME' 'LASTNAME'" or "'ALIAS'" in case
     * firstname and lastname are not defined.
     * 
     * @param profileDetails
     *            object holding the user details
     * @return the signature
     */
    public static String getSimpleDefaultUserSignature(UserProfileDetails profileDetails) {
        return getCompleteSignature(profileDetails, BLANK, profileDetails.getUserAlias(), false);
    }

    /**
     * Returns a simple user signature consisting of "'FIRSTNAME' 'LASTNAME'" or "'ALIAS'" in case
     * firstname and lastname are not defined.
     * 
     * @param noteListData
     *            the user list item to use
     * @return the signature
     */
    public static String getSimpleDefaultUserSignature(NoteData noteListData) {
        return getSimpleDefaultUserSignature(noteListData.getUser());
    }

    /**
     * Returns a simple user signature consisting of "'FIRSTNAME' 'LASTNAME'" or "'ALIAS'" in case
     * firstname and lastname are not defined.
     * 
     * @param firstName
     *            the first name of the user
     * @param lastName
     *            the last name
     * @param alias
     *            the alias
     * @return the signature
     */
    public static String getSimpleDefaultUserSignature(String firstName, String lastName,
            String alias) {
        return getCompleteSignature(null, firstName, lastName, BLANK, alias);
    }

    /**
     * Returns a simple user signature consisting of "'FIRSTNAME' 'LASTNAME'" or "'ALIAS'" in case
     * firstname and lastname are not defined.
     * 
     * @param userListItem
     *            the user list item to use
     * @return the signature
     */
    public static String getSimpleDefaultUserSignature(UserData userListItem) {
        return getCompleteSignature(userListItem, BLANK, userListItem.getAlias(), false);
    }

    /**
     * To get the signature of an user.
     * 
     * @param user
     *            the user to use
     * @param format
     *            the format of the user signature
     * @return the user signature
     */
    public static String getUserSignature(User user, UserNameFormat format) {
        String signature = StringUtils.EMPTY;
        format = format == null ? UserNameFormat.SHORT : format;
        switch (format) {
        case ALIAS:
            signature = user.getAlias();
            break;
        case SHORT:
            signature = getSimpleDefaultUserSignature(user);
            break;
        case MEDIUM:
            signature = getDetailedUserSignature(user);
            break;
        case LONG:
            signature = getDefaultUserSignature(user);
            break;
        default:
            signature = getSimpleDefaultUserSignature(user);
        }
        return signature;
    }

    /**
     * To get the signature of an user.
     * 
     * @param userListItem
     *            the user list item to use
     * @param format
     *            the format of the user signature
     * @return the user signature
     */
    public static String getUserSignature(UserData userListItem, UserNameFormat format) {
        String signature = StringUtils.EMPTY;

        switch (format) {
        case ALIAS:
            signature = userListItem.getAlias();
            break;
        case SHORT:
            signature = getSimpleDefaultUserSignature(userListItem);
            break;
        case MEDIUM:
            signature = getDetailedUserSignature(userListItem);
            break;
        case LONG:
            signature = getDefaultUserSignature(userListItem);
            break;
        default:
            signature = getSimpleDefaultUserSignature(userListItem);
            break;
        }

        return signature;
    }

    /**
     * To get the signature of an user.
     * 
     * @param listItem
     *            the list item to use
     * @param format
     *            the format of the user signature
     * @return the user signature
     */
    public static String getUserSignature(UserProfileDetailListItem listItem, UserNameFormat format) {
        String signature = StringUtils.EMPTY;

        switch (format) {
        case ALIAS:
            signature = listItem.getAlias();
            break;
        case SHORT:
            signature = getCompleteSignature(listItem, BLANK, listItem.getAlias(), false);
            break;
        case MEDIUM:
            signature = getDetailedUserSignature(listItem.getFirstName(), listItem.getLastName(),
                    listItem.getAlias());
            break;
        case LONG:
            // differs from other implementations because there is no email in this item
            signature = getCompleteSignature(listItem, BLANK, listItem.getAlias());
            break;
        default:
            signature = getCompleteSignature(listItem, BLANK, listItem.getAlias(), false);
            break;
        }

        return signature;
    }

    /**
     * Private constructor to avoid instances of utility class.
     */
    private UserNameHelper() {
        // Do nothing
    }
}
