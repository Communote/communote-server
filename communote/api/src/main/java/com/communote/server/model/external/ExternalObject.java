package com.communote.server.model.external;

/**
 * 
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class ExternalObject implements java.io.Serializable,
        com.communote.server.model.property.Propertyable {
    /**
     * Constructs new instances of {@link com.communote.server.model.external.ExternalObject}.
     */
    public static final class Factory {
        /**
         * Constructs a new instance of {@link com.communote.server.model.external.ExternalObject}.
         */
        public static com.communote.server.model.external.ExternalObject newInstance() {
            return new com.communote.server.model.external.ExternalObjectImpl();
        }

        /**
         * Constructs a new instance of {@link com.communote.server.model.external.ExternalObject},
         * taking all required and/or read-only properties as arguments.
         */
        public static com.communote.server.model.external.ExternalObject newInstance(
                String externalSystemId, String externalId) {
            final com.communote.server.model.external.ExternalObject entity = new com.communote.server.model.external.ExternalObjectImpl();
            entity.setExternalSystemId(externalSystemId);
            entity.setExternalId(externalId);
            return entity;
        }

        /**
         * Constructs a new instance of {@link com.communote.server.model.external.ExternalObject},
         * taking all possible properties (except the identifier(s))as arguments.
         */
        public static com.communote.server.model.external.ExternalObject newInstance(
                String externalSystemId, String externalId, String externalName,
                java.util.Set<com.communote.server.model.external.ExternalObjectProperty> properties) {
            final com.communote.server.model.external.ExternalObject entity = new com.communote.server.model.external.ExternalObjectImpl();
            entity.setExternalSystemId(externalSystemId);
            entity.setExternalId(externalId);
            entity.setExternalName(externalName);
            entity.setProperties(properties);
            return entity;
        }
    }

    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = 2094202537913154638L;

    private String externalSystemId;

    private String externalId;

    private String externalName;

    private Long id;

    private java.util.Set<com.communote.server.model.external.ExternalObjectProperty> properties = new java.util.HashSet<com.communote.server.model.external.ExternalObjectProperty>();

    /**
     * Builds a string showing the current attribute values
     */

    public String attributesToString() {
        StringBuilder sb = new StringBuilder();

        sb.append("class='");
        sb.append(this.getClass().getName());
        sb.append("', ");

        sb.append("externalSystemId='");
        sb.append(externalSystemId);
        sb.append("', ");

        sb.append("externalId='");
        sb.append(externalId);
        sb.append("', ");

        sb.append("externalName='");
        sb.append(externalName);
        sb.append("', ");

        sb.append("id='");
        sb.append(id);
        sb.append("', ");

        return sb.toString();
    }

    /**
     * Returns <code>true</code> if the argument is an ExternalObject instance and all identifiers
     * for this entity equal the identifiers of the argument entity. Returns <code>false</code>
     * otherwise.
     */
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof ExternalObject)) {
            return false;
        }
        final ExternalObject that = (ExternalObject) object;
        if (this.id == null || that.getId() == null || !this.id.equals(that.getId())) {
            return false;
        }
        return true;
    }

    /**
     * 
     */
    public String getExternalId() {
        return this.externalId;
    }

    /**
     * 
     */
    public String getExternalName() {
        return this.externalName;
    }

    /**
     * 
     */
    public String getExternalSystemId() {
        return this.externalSystemId;
    }

    /**
     * 
     */
    public Long getId() {
        return this.id;
    }

    /**
     * 
     */
    public java.util.Set<com.communote.server.model.external.ExternalObjectProperty> getProperties() {
        return this.properties;
    }

    /**
     * Returns a hash code based on this entity's identifiers.
     */
    public int hashCode() {
        int hashCode = 0;
        hashCode = 29 * hashCode + (id == null ? 0 : id.hashCode());

        return hashCode;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public void setExternalName(String externalName) {
        this.externalName = externalName;
    }

    public void setExternalSystemId(String externalSystemId) {
        this.externalSystemId = externalSystemId;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setProperties(
            java.util.Set<com.communote.server.model.external.ExternalObjectProperty> properties) {
        this.properties = properties;
    }
}