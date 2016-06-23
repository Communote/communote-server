package com.communote.server.model.user;

/**
 * <p>
 * Configuration for user notification.
 * </p>
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class NotificationConfig implements java.io.Serializable {
    /**
     * Constructs new instances of {@link com.communote.server.model.user.NotificationConfig}.
     */
    public static final class Factory {
        /**
         * Constructs a new instance of {@link com.communote.server.model.user.NotificationConfig}.
         */
        public static com.communote.server.model.user.NotificationConfig newInstance() {
            return new com.communote.server.model.user.NotificationConfigImpl();
        }

        /**
         * Constructs a new instance of {@link com.communote.server.model.user.NotificationConfig},
         * taking all possible properties (except the identifier(s))as arguments.
         */
        public static com.communote.server.model.user.NotificationConfig newInstance(
                String fallback,
                java.util.Set<com.communote.server.model.messaging.MessagerConnectorConfig> configs) {
            final com.communote.server.model.user.NotificationConfig entity = new com.communote.server.model.user.NotificationConfigImpl();
            entity.setFallback(fallback);
            entity.setConfigs(configs);
            return entity;
        }
    }

    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = 3822459560380685528L;

    private String fallback;

    private Long id;

    private java.util.Set<com.communote.server.model.messaging.MessagerConnectorConfig> configs = new java.util.HashSet<com.communote.server.model.messaging.MessagerConnectorConfig>();

    /**
     * Builds a string showing the current attribute values
     */

    public String attributesToString() {
        StringBuilder sb = new StringBuilder();

        sb.append("class='");
        sb.append(this.getClass().getName());
        sb.append("', ");

        sb.append("fallback='");
        sb.append(fallback);
        sb.append("', ");

        sb.append("id='");
        sb.append(id);
        sb.append("', ");

        return sb.toString();
    }

    /**
     * Returns <code>true</code> if the argument is an NotificationConfig instance and all
     * identifiers for this entity equal the identifiers of the argument entity. Returns
     * <code>false</code> otherwise.
     */
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof NotificationConfig)) {
            return false;
        }
        final NotificationConfig that = (NotificationConfig) object;
        if (this.id == null || that.getId() == null || !this.id.equals(that.getId())) {
            return false;
        }
        return true;
    }

    /**
     * 
     */
    public java.util.Set<com.communote.server.model.messaging.MessagerConnectorConfig> getConfigs() {
        return this.configs;
    }

    /**
     * 
     */
    public String getFallback() {
        return this.fallback;
    }

    /**
     * 
     */
    public Long getId() {
        return this.id;
    }

    /**
     * Returns a hash code based on this entity's identifiers.
     */
    public int hashCode() {
        int hashCode = 0;
        hashCode = 29 * hashCode + (id == null ? 0 : id.hashCode());

        return hashCode;
    }

    public void setConfigs(
            java.util.Set<com.communote.server.model.messaging.MessagerConnectorConfig> configs) {
        this.configs = configs;
    }

    public void setFallback(String fallback) {
        this.fallback = fallback;
    }

    public void setId(Long id) {
        this.id = id;
    }
}