package com.communote.server.model.security;

/**
 * <p>
 * A list of configuration attributes for a channel type are defined for client security.
 * </p>
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class ChannelConfiguration implements java.io.Serializable {
    /**
     * Constructs new instances of {@link com.communote.server.model.security.ChannelConfiguration}.
     */
    public static final class Factory {
        /**
         * Constructs a new instance of
         * {@link com.communote.server.model.security.ChannelConfiguration}.
         */
        public static com.communote.server.model.security.ChannelConfiguration newInstance() {
            return new com.communote.server.model.security.ChannelConfigurationImpl();
        }

        /**
         * Constructs a new instance of
         * {@link com.communote.server.model.security.ChannelConfiguration}, taking all possible
         * properties (except the identifier(s))as arguments.
         */
        public static com.communote.server.model.security.ChannelConfiguration newInstance(
                Boolean forceSsl, com.communote.server.model.security.ChannelType channelType) {
            final com.communote.server.model.security.ChannelConfiguration entity = new com.communote.server.model.security.ChannelConfigurationImpl();
            entity.setForceSsl(forceSsl);
            entity.setChannelType(channelType);
            return entity;
        }
    }

    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = -1802273206707174200L;

    private Boolean forceSsl;

    private com.communote.server.model.security.ChannelType channelType;

    private Long id;

    /**
     * Builds a string showing the current attribute values
     */

    public String attributesToString() {
        StringBuilder sb = new StringBuilder();

        sb.append("class='");
        sb.append(this.getClass().getName());
        sb.append("', ");

        sb.append("forceSsl='");
        sb.append(forceSsl);
        sb.append("', ");

        sb.append("channelType='");
        sb.append(channelType);
        sb.append("', ");

        sb.append("id='");
        sb.append(id);
        sb.append("', ");

        return sb.toString();
    }

    /**
     * Returns <code>true</code> if the argument is an ChannelConfiguration instance and all
     * identifiers for this entity equal the identifiers of the argument entity. Returns
     * <code>false</code> otherwise.
     */
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof ChannelConfiguration)) {
            return false;
        }
        final ChannelConfiguration that = (ChannelConfiguration) object;
        if (this.id == null || that.getId() == null || !this.id.equals(that.getId())) {
            return false;
        }
        return true;
    }

    /**
     * 
     */
    public com.communote.server.model.security.ChannelType getChannelType() {
        return this.channelType;
    }

    /**
     * <p>
     * Defines it is forced to use an secure channel or if an insecure channel is possible.
     * </p>
     */
    public Boolean getForceSsl() {
        return this.forceSsl;
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

    public void setChannelType(com.communote.server.model.security.ChannelType channelType) {
        this.channelType = channelType;
    }

    public void setForceSsl(Boolean forceSsl) {
        this.forceSsl = forceSsl;
    }

    public void setId(Long id) {
        this.id = id;
    }
}