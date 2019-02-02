package com.communote.server.persistence.user;

import java.io.Serializable;
import java.util.Locale;
import java.util.Set;

import com.communote.server.api.core.property.StringPropertyTO;
import com.communote.server.api.core.user.UserVO;

/**
 * A value object representing a User retrieved from an external repository like LDAP.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ExternalUserVO extends UserVO implements Serializable {
    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = 6218364498311746062L;

    private com.communote.server.model.user.UserStatus status;

    private String externalUserName;

    private boolean updateFirstName;

    private boolean updateLastName;

    private boolean updateLanguage;

    private boolean updateEmail;

    private boolean clearPassword;

    private String systemId;

    private String permanentId;

    private String additionalProperty;

    private Set<StringPropertyTO> properties;

    private Locale defaultLanguage;

    /**
     * Empty constructor.
     */
    public ExternalUserVO() {
        super();
        this.externalUserName = null;
        this.updateFirstName = false;
        this.updateLastName = false;
        this.updateLanguage = false;
        this.updateEmail = false;
        this.clearPassword = false;
        this.systemId = null;
    }

    /**
     * An optional member holding some additional data for the user. The interpretation depends on
     * the external system. For LDAP it would hold the DN for example.
     *
     * @return the additional property
     */
    public String getAdditionalProperty() {
        return this.additionalProperty;
    }

    /**
     * The default language is used if the external user is not yet created.
     *
     * @return the default language for this user
     */
    public Locale getDefaultLanguage() {
        return defaultLanguage;
    }

    /**
     * Login name of the user in the external repository. This user name will be used to create a
     * Communote alias, if the external user does not yet exist within Communote and the external
     * repository does not provide another property whose value is to be used as Communote user
     * alias. If the user name is not a legal Communote alias the local part of the email address is
     * used as alias.
     *
     * @return the external user name
     */
    public String getExternalUserName() {
        return this.externalUserName;
    }

    /**
     * Optional attribute that represents a unique identifier of the user in the external repository
     * whose value never changes.
     *
     * @return the permanent id
     */
    public String getPermanentId() {
        return this.permanentId;
    }

    /**
     *
     * @return the properties
     */
    public Set<StringPropertyTO> getProperties() {
        return properties;
    }

    /**
     * Status of the user to be set. This allows deactivating a user who was disabled or removed
     * from the external repository.
     *
     * @return the status
     */
    public com.communote.server.model.user.UserStatus getStatus() {
        return this.status;
    }

    /**
     *
     * @return the system id
     */
    public String getSystemId() {
        return this.systemId;
    }

    /**
     * Whether to clear the password of the local user.
     *
     * @return true if the password should be cleared
     * @since 3.5
     */
    public boolean isClearPassword() {
        return this.clearPassword;
    }

    /**
     * Whether to update the email address of the user if it is provided. Setting this to false will
     * prevent synchronization of an existing local user entry with data retrieved from the external
     * repository.
     *
     * @return the email
     */
    public boolean isUpdateEmail() {
        return this.updateEmail;
    }

    /**
     * Whether to update the first name if it is provided. Setting this to false will prevent
     * synchronization of an existing local user entry with data retrieved from the external
     * repository.
     *
     * @return the first name
     */
    public boolean isUpdateFirstName() {
        return this.updateFirstName;
    }

    /**
     * Whether to update the language if it is provided. Setting this to false will prevent
     * synchronization of an existing local user entry with data retrieved from the external
     * repository.
     *
     * @return the language
     */
    public boolean isUpdateLanguage() {
        return this.updateLanguage;
    }

    /**
     * Whether to update the last name if it is provided. Setting this to false will prevent
     * synchronization of an existing local user entry with data retrieved from the external
     * repository.
     *
     * @return the last name
     */
    public boolean isUpdateLastName() {
        return this.updateLastName;
    }

    /**
     * Set the additional property.
     *
     * @param additionalProperty
     *            the additional property
     */
    public void setAdditionalProperty(String additionalProperty) {
        this.additionalProperty = additionalProperty;
    }

    /**
     * Set whether a password stored for the local user belonging to this external user should be
     * cleared. This should be true if this VO was created by the primary authentication provider to
     * avoid logins with an old password after changing it in the external system.
     *
     * @param clearPassword
     *            true to clear the password
     * @since 3.5
     */
    public void setClearPassword(boolean clearPassword) {
        this.clearPassword = clearPassword;
    }

    /**
     * The default language is used if the external user is not yet created. If you want the
     * language to be it for the user even he may already choosen another language use
     * {@link #setLanguage(Locale)}
     *
     * @param defaultLanguage
     *            the default language
     */
    public void setDefaultLanguage(Locale defaultLanguage) {
        this.defaultLanguage = defaultLanguage;
    }

    /**
     * Set the external user name.
     *
     * @param externalUserName
     *            the external user name
     */
    public void setExternalUserName(String externalUserName) {
        this.externalUserName = externalUserName;
    }

    /**
     * Set the permanent id.
     *
     * @param permanentId
     *            the permanent id
     */
    public void setPermanentId(String permanentId) {
        this.permanentId = permanentId;
    }

    /**
     * Set additional user properties.
     *
     * @param properties
     *            the additional properties
     */
    public void setProperties(Set<StringPropertyTO> properties) {
        this.properties = properties;
    }

    /**
     * Set the user status.
     *
     * @param status
     *            the user status
     */
    public void setStatus(com.communote.server.model.user.UserStatus status) {
        this.status = status;
    }

    /**
     * Set the system id.
     *
     * @param systemId
     *            the system id
     */
    public void setSystemId(String systemId) {
        this.systemId = systemId;
    }

    /**
     * Set the email address.
     *
     * @param updateEmail
     *            the email address
     */
    public void setUpdateEmail(boolean updateEmail) {
        this.updateEmail = updateEmail;
    }

    /**
     * Set first name.
     *
     * @param updateFirstName
     *            the first name
     */
    public void setUpdateFirstName(boolean updateFirstName) {
        this.updateFirstName = updateFirstName;
    }

    /**
     * Set the language.
     *
     * @param updateLanguage
     *            the language
     */
    public void setUpdateLanguage(boolean updateLanguage) {
        this.updateLanguage = updateLanguage;
    }

    /**
     * Set the last name.
     *
     * @param updateLastName
     *            the last name
     */
    public void setUpdateLastName(boolean updateLastName) {
        this.updateLastName = updateLastName;
    }

}