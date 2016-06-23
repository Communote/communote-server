package com.communote.server.core.user;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Locale;

import com.communote.server.model.user.UserName;
import com.communote.server.model.user.UserStatus;

/**
 * Details of a user
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class UserProfileDetails implements UserName, Serializable {

    private static final long serialVersionUID = 1L;

    private Long userId = null;
    private String userAlias = null;
    private String firstName = null;
    private String lastName = null;
    private String salutation = null;
    private UserStatus userStatus = null;
    private Locale userLocale = null;
    private String timeZoneId = null;
    private final HashMap<String, String> externalUserIds;

    /**
     * Construct the <code>User</code> with the details
     *
     * @param userId
     *            The user id
     * @param userAlias
     *            The user alias
     */
    public UserProfileDetails(Long userId, String userAlias) {
        super();
        externalUserIds = new HashMap<String, String>();
        this.setUserId(userId);
        this.setUserAlias(userAlias);
    }

    /**
     * Stores the user ID that a user has within an external system.
     *
     * @param externalSystemId
     *            the ID of the external system
     * @param externalUserId
     *            the ID of the user within the external system identified by the externalSystemID
     */
    public void addExternalUserId(String externalSystemId, String externalUserId) {
        externalUserIds.put(externalSystemId, externalUserId);
    }

    /**
     * Returns the ID of the user within an external system.
     *
     * @param externalSystemId
     *            the ID of the external system
     * @return the ID of the user within the external system identified by the externalSystemID or
     *         null if the user is not a user of the external system
     */
    public String getExternalUserId(String externalSystemId) {
        return externalUserIds.get(externalSystemId);
    }

    /**
     * @return the firstName
     */
    @Override
    public String getFirstName() {
        return firstName;
    }

    /**
     * @return the lastName
     */
    @Override
    public String getLastName() {
        return lastName;
    }

    /**
     * @return the salutation
     */
    @Override
    public String getSalutation() {
        return salutation;
    }

    /**
     * @return the timeZoneId
     */
    public String getTimeZoneId() {
        return timeZoneId;
    }

    /**
     * The alias of the user
     *
     * @return The user alias
     */
    public String getUserAlias() {
        return userAlias;
    }

    /**
     * The user id of the user
     *
     * @return The user
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * @return the userLocale
     */
    public Locale getUserLocale() {
        return userLocale;
    }

    /**
     * @return the userStatus
     */
    public UserStatus getUserStatus() {
        return userStatus;
    }

    /**
     * @param firstName
     *            the firstName to set
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * @param lastName
     *            the lastName to set
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * @param salutation
     *            the salutation to set
     */
    public void setSalutation(String salutation) {
        this.salutation = salutation;
    }

    /**
     * @param timeZoneId
     *            the timeZoneId to set
     */
    public void setTimeZoneId(String timeZoneId) {
        this.timeZoneId = timeZoneId;
    }

    /**
     * Sets the user alias.
     *
     * @param alias
     *            the user alias
     */
    private void setUserAlias(String alias) {
        if (alias == null) {
            throw new IllegalArgumentException("User alias cannot be null");
        }
        this.userAlias = alias;
    }

    /**
     * Sets the user id.
     *
     * @param id
     *            the user id
     */
    private void setUserId(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("User id cannot be null");
        }
        this.userId = id;
    }

    // /**
    // * Because username can not be changed for userdetails create a new one
    // based on the given and
    // * the new username
    // *
    // * @param username
    // * new username for the userdetails
    // * @param details
    // * userdetails with all other detailed information fr this user
    // */
    // protected UserDetails(String username, LdapKenmeiUserDetails
    // details) {
    // super(username, details.getPassword(), details.isEnabled(), details
    // .isAccountNonExpired(), details.isCredentialsNonExpired(), details
    // .isAccountNonLocked(), details.getAuthorities());
    //
    // this.setUserId(details.getLdapUser().getId());
    // }

    /**
     * @param userLocale
     *            the userLocale to set
     */
    public void setUserLocale(Locale userLocale) {
        this.userLocale = userLocale;
    }

    /**
     * @param userStatus
     *            the userStatus to set
     */
    public void setUserStatus(UserStatus userStatus) {
        this.userStatus = userStatus;
    }
}
