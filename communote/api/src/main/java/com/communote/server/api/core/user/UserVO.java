package com.communote.server.api.core.user;

import com.communote.server.model.user.UserRole;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class UserVO implements java.io.Serializable {
    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = 3783821883359146281L;

    private String password;

    private java.util.Locale language;

    private String email;

    private UserRole[] roles;

    private String alias;

    private String firstName;

    private String lastName;

    private String timeZoneId;

    public UserVO() {
        this.language = null;
        this.email = null;
        this.roles = null;
    }

    /**
     * 
     * @since 3.5
     */
    public UserVO(java.util.Locale language, String email, UserRole[] roles) {
        this.language = language;
        this.email = email;
        this.roles = roles;
    }

    /**
     * 
     * @since 3.5
     */
    public UserVO(String password, java.util.Locale language, String email, UserRole[] roles,
            String alias, String firstName, String lastName, String timeZoneId) {
        this.password = password;
        this.language = language;
        this.email = email;
        this.roles = roles;
        this.alias = alias;
        this.firstName = firstName;
        this.lastName = lastName;
        this.timeZoneId = timeZoneId;
    }

    /**
     *
     */
    public String getAlias() {
        return this.alias;
    }

    /**
     *
     */
    public String getEmail() {
        return this.email;
    }

    /**
     *
     */
    public String getFirstName() {
        return this.firstName;
    }

    /**
     *
     */
    public java.util.Locale getLanguage() {
        return this.language;
    }

    /**
     *
     */
    public String getLastName() {
        return this.lastName;
    }

    /**
     *
     */
    public String getPassword() {
        return this.password;
    }

    /**
     *
     */
    public UserRole[] getRoles() {
        return this.roles;
    }

    /**
     * <p>
     * The id of the users time zone.
     * </p>
     */
    public String getTimeZoneId() {
        return this.timeZoneId;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLanguage(java.util.Locale language) {
        this.language = language;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setRoles(UserRole[] roles) {
        this.roles = roles;
    }

    public void setTimeZoneId(String timeZoneId) {
        this.timeZoneId = timeZoneId;
    }

}