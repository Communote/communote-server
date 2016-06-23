package com.communote.server.model.user;

/**
 * <p>
 * This class represents external authentication details to a user. The systemId defines the system
 * this id belongs to.
 * </p>
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class ExternalUserAuthentication implements java.io.Serializable {
    /**
     * Constructs new instances of
     * {@link com.communote.server.model.user.ExternalUserAuthentication}.
     */
    public static final class Factory {
        /**
         * Constructs a new instance of
         * {@link com.communote.server.model.user.ExternalUserAuthentication}.
         */
        public static com.communote.server.model.user.ExternalUserAuthentication newInstance() {
            return new com.communote.server.model.user.ExternalUserAuthenticationImpl();
        }

        /**
         * Constructs a new instance of
         * {@link com.communote.server.model.user.ExternalUserAuthentication}, taking all required
         * and/or read-only properties as arguments.
         */
        public static com.communote.server.model.user.ExternalUserAuthentication newInstance(
                String externalUserId, String systemId) {
            final com.communote.server.model.user.ExternalUserAuthentication entity = new com.communote.server.model.user.ExternalUserAuthenticationImpl();
            entity.setExternalUserId(externalUserId);
            entity.setSystemId(systemId);
            return entity;
        }

        /**
         * Constructs a new instance of
         * {@link com.communote.server.model.user.ExternalUserAuthentication}, taking all possible
         * properties (except the identifier(s))as arguments.
         */
        public static com.communote.server.model.user.ExternalUserAuthentication newInstance(
                String externalUserId, String systemId, String permanentId,
                String additionalProperty) {
            final com.communote.server.model.user.ExternalUserAuthentication entity = new com.communote.server.model.user.ExternalUserAuthenticationImpl();
            entity.setExternalUserId(externalUserId);
            entity.setSystemId(systemId);
            entity.setPermanentId(permanentId);
            entity.setAdditionalProperty(additionalProperty);
            return entity;
        }
    }

    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = -8870438530254836854L;

    private String externalUserId;

    private String systemId;

    private String permanentId;

    private String additionalProperty;

    private Long id;

    /**
     * Builds a string showing the current attribute values
     */

    public String attributesToString() {
        StringBuilder sb = new StringBuilder();

        sb.append("class='");
        sb.append(this.getClass().getName());
        sb.append("', ");

        sb.append("externalUserId='");
        sb.append(externalUserId);
        sb.append("', ");

        sb.append("systemId='");
        sb.append(systemId);
        sb.append("', ");

        sb.append("permanentId='");
        sb.append(permanentId);
        sb.append("', ");

        sb.append("additionalProperty='");
        sb.append(additionalProperty);
        sb.append("', ");

        sb.append("id='");
        sb.append(id);
        sb.append("', ");

        return sb.toString();
    }

    /**
     * Returns <code>true</code> if the argument is an ExternalUserAuthentication instance and all
     * identifiers for this entity equal the identifiers of the argument entity. Returns
     * <code>false</code> otherwise.
     */
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof ExternalUserAuthentication)) {
            return false;
        }
        final ExternalUserAuthentication that = (ExternalUserAuthentication) object;
        if (this.id == null || that.getId() == null || !this.id.equals(that.getId())) {
            return false;
        }
        return true;
    }

    /**
     * <p>
     * an optional member holding some additional data for the user. The interpretation depends on
     * the external system. For LDAP it would hold the DN for example.
     * </p>
     */
    public String getAdditionalProperty() {
        return this.additionalProperty;
    }

    /**
     * <p>
     * Contains the user id of a external authentication system. This is typically the login of the
     * user for the external system.
     * </p>
     */
    public String getExternalUserId() {
        return this.externalUserId;
    }

    /**
     * 
     */
    public Long getId() {
        return this.id;
    }

    /**
     * <p>
     * optional attribute that represents a unique identifier of the user in the external repository
     * whose value never changes. This allows handling a change of the externalUserId. This member
     * can be null because not all external systems provide such an attribute.
     * </p>
     */
    public String getPermanentId() {
        return this.permanentId;
    }

    /**
     * <p>
     * Identifier of the external system (e.g. Confluence XYZ).
     * </p>
     */
    public String getSystemId() {
        return this.systemId;
    }

    /**
     * Returns a hash code based on this entity's identifiers.
     */
    public int hashCode() {
        int hashCode = 0;
        hashCode = 29 * hashCode + (id == null ? 0 : id.hashCode());

        return hashCode;
    }

    public void setAdditionalProperty(String additionalProperty) {
        this.additionalProperty = additionalProperty;
    }

    public void setExternalUserId(String externalUserId) {
        this.externalUserId = externalUserId;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setPermanentId(String permanentId) {
        this.permanentId = permanentId;
    }

    public void setSystemId(String systemId) {
        this.systemId = systemId;
    }
}