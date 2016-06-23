package com.communote.server.model.messaging;

/**
 * <p>
 * Configuration of a specific MessagerConnector as type.
 * </p>
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class MessagerConnectorConfig implements java.io.Serializable {
    /**
     * Constructs new instances of
     * {@link com.communote.server.model.messaging.MessagerConnectorConfig}.
     */
    public static final class Factory {
        /**
         * Constructs a new instance of
         * {@link com.communote.server.model.messaging.MessagerConnectorConfig}.
         */
        public static com.communote.server.model.messaging.MessagerConnectorConfig newInstance() {
            return new com.communote.server.model.messaging.MessagerConnectorConfigImpl();
        }

        /**
         * Constructs a new instance of
         * {@link com.communote.server.model.messaging.MessagerConnectorConfig}, taking all required
         * and/or read-only properties as arguments.
         */
        public static com.communote.server.model.messaging.MessagerConnectorConfig newInstance(
                com.communote.server.model.messaging.MessagerConnectorType type,
                boolean onlyIfAvailable, Integer priority) {
            final com.communote.server.model.messaging.MessagerConnectorConfig entity = new com.communote.server.model.messaging.MessagerConnectorConfigImpl();
            entity.setType(type);
            entity.setOnlyIfAvailable(onlyIfAvailable);
            entity.setPriority(priority);
            return entity;
        }

        /**
         * Constructs a new instance of
         * {@link com.communote.server.model.messaging.MessagerConnectorConfig}, taking all possible
         * properties (except the identifier(s))as arguments.
         */
        public static com.communote.server.model.messaging.MessagerConnectorConfig newInstance(
                com.communote.server.model.messaging.MessagerConnectorType type, String properties,
                boolean onlyIfAvailable, Integer priority) {
            final com.communote.server.model.messaging.MessagerConnectorConfig entity = new com.communote.server.model.messaging.MessagerConnectorConfigImpl();
            entity.setType(type);
            entity.setProperties(properties);
            entity.setOnlyIfAvailable(onlyIfAvailable);
            entity.setPriority(priority);
            return entity;
        }
    }

    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = 2800605410977646700L;

    private com.communote.server.model.messaging.MessagerConnectorType type;

    private String properties;

    private boolean onlyIfAvailable = false;

    private Integer priority = Integer.valueOf(0);

    private Long id;

    /**
     * Builds a string showing the current attribute values
     */

    public String attributesToString() {
        StringBuilder sb = new StringBuilder();

        sb.append("class='");
        sb.append(this.getClass().getName());
        sb.append("', ");

        sb.append("type='");
        sb.append(type);
        sb.append("', ");

        sb.append("properties='");
        sb.append(properties);
        sb.append("', ");

        sb.append("onlyIfAvailable='");
        sb.append(onlyIfAvailable);
        sb.append("', ");

        sb.append("priority='");
        sb.append(priority);
        sb.append("', ");

        sb.append("id='");
        sb.append(id);
        sb.append("', ");

        return sb.toString();
    }

    /**
     * Returns <code>true</code> if the argument is an MessagerConnectorConfig instance and all
     * identifiers for this entity equal the identifiers of the argument entity. Returns
     * <code>false</code> otherwise.
     */
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof MessagerConnectorConfig)) {
            return false;
        }
        final MessagerConnectorConfig that = (MessagerConnectorConfig) object;
        if (this.id == null || that.getId() == null || !this.id.equals(that.getId())) {
            return false;
        }
        return true;
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
    public Integer getPriority() {
        return this.priority;
    }

    /**
     * 
     */
    public String getProperties() {
        return this.properties;
    }

    /**
     * 
     */
    public com.communote.server.model.messaging.MessagerConnectorType getType() {
        return this.type;
    }

    /**
     * Returns a hash code based on this entity's identifiers.
     */
    public int hashCode() {
        int hashCode = 0;
        hashCode = 29 * hashCode + (id == null ? 0 : id.hashCode());

        return hashCode;
    }

    /**
     * 
     */
    public boolean isOnlyIfAvailable() {
        return this.onlyIfAvailable;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setOnlyIfAvailable(boolean onlyIfAvailable) {
        this.onlyIfAvailable = onlyIfAvailable;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public void setProperties(String properties) {
        this.properties = properties;
    }

    public void setType(com.communote.server.model.messaging.MessagerConnectorType type) {
        this.type = type;
    }
}