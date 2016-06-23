package com.communote.server.model.config;

/**
 * <p>
 * This abstract class defines an external system configuration
 * </p>
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class ExternalSystemConfiguration implements java.io.Serializable,
        com.communote.server.model.config.ExternalImageApiUrlProvider {
    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = -6703686040373045843L;

    private boolean allowExternalAuthentication;

    private String systemId;

    private boolean primaryAuthentication;

    private boolean synchronizeUserGroups;

    private Long id;

    /**
     * Builds a string showing the current attribute values
     */

    public String attributesToString() {
        StringBuilder sb = new StringBuilder();

        sb.append("class='");
        sb.append(this.getClass().getName());
        sb.append("', ");

        sb.append("allowExternalAuthentication='");
        sb.append(allowExternalAuthentication);
        sb.append("', ");

        sb.append("systemId='");
        sb.append(systemId);
        sb.append("', ");

        sb.append("primaryAuthentication='");
        sb.append(primaryAuthentication);
        sb.append("', ");

        sb.append("synchronizeUserGroups='");
        sb.append(synchronizeUserGroups);
        sb.append("', ");

        sb.append("id='");
        sb.append(id);
        sb.append("', ");

        return sb.toString();
    }

    /**
     * Returns <code>true</code> if the argument is an ExternalSystemConfiguration instance and all
     * identifiers for this entity equal the identifiers of the argument entity. Returns
     * <code>false</code> otherwise.
     */
    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof ExternalSystemConfiguration)) {
            return false;
        }
        final ExternalSystemConfiguration that = (ExternalSystemConfiguration) object;
        if (this.id == null || that.getId() == null || !this.id.equals(that.getId())) {
            return false;
        }
        return true;
    }

    /**
     * <p>
     * Get the URL to the configuration in the Communote administration.
     * </p>
     */
    public abstract String getConfigurationUrl();

    /**
     *
     */
    public Long getId() {
        return this.id;
    }

    /**
     *
     */
    @Override
    public abstract String getImageApiUrl();

    /**
     * <p>
     * The unique identifier of the external system.
     * </p>
     */
    public String getSystemId() {
        return this.systemId;
    }

    /**
     * Returns a hash code based on this entity's identifiers.
     */
    @Override
    public int hashCode() {
        int hashCode = 0;
        hashCode = 29 * hashCode + (id == null ? 0 : id.hashCode());

        return hashCode;
    }

    /**
     * <p>
     * True if the configuration should be used for authentication
     * </p>
     */
    public boolean isAllowExternalAuthentication() {
        return this.allowExternalAuthentication;
    }

    /**
     * <p>
     * Whether the external system is the primary system that is used for authentication of users
     * logging in via web channel. Only one system can be primary.
     * </p>
     */
    public boolean isPrimaryAuthentication() {
        return this.primaryAuthentication;
    }

    /**
     * <p>
     * True if the configuration should be used importing user groups
     * </p>
     */
    public boolean isSynchronizeUserGroups() {
        return this.synchronizeUserGroups;
    }

    public void setAllowExternalAuthentication(boolean allowExternalAuthentication) {
        this.allowExternalAuthentication = allowExternalAuthentication;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setPrimaryAuthentication(boolean primaryAuthentication) {
        this.primaryAuthentication = primaryAuthentication;
    }

    public void setSynchronizeUserGroups(boolean synchronizeUserGroups) {
        this.synchronizeUserGroups = synchronizeUserGroups;
    }

    public void setSystemId(String systemId) {
        this.systemId = systemId;
    }
}