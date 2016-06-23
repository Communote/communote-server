package com.communote.server.model.security;

/**
 * 
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class IpRangeChannel implements java.io.Serializable {
    /**
     * Constructs new instances of {@link com.communote.server.model.security.IpRangeChannel}.
     */
    public static final class Factory {
        /**
         * Constructs a new instance of {@link com.communote.server.model.security.IpRangeChannel}.
         */
        public static com.communote.server.model.security.IpRangeChannel newInstance() {
            return new com.communote.server.model.security.IpRangeChannelImpl();
        }

        /**
         * Constructs a new instance of {@link com.communote.server.model.security.IpRangeChannel},
         * taking all possible properties (except the identifier(s))as arguments.
         */
        public static com.communote.server.model.security.IpRangeChannel newInstance(boolean enabled) {
            final com.communote.server.model.security.IpRangeChannel entity = new com.communote.server.model.security.IpRangeChannelImpl();
            entity.setEnabled(enabled);
            return entity;
        }
    }

    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = 898367145265056381L;

    private String type;

    private boolean enabled;

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

        sb.append("enabled='");
        sb.append(enabled);
        sb.append("', ");

        return sb.toString();
    }

    /**
     * Returns <code>true</code> if the argument is an IpRangeChannel instance and all identifiers
     * for this entity equal the identifiers of the argument entity. Returns <code>false</code>
     * otherwise.
     */
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof IpRangeChannel)) {
            return false;
        }
        final IpRangeChannel that = (IpRangeChannel) object;
        if (this.type == null || that.getType() == null || !this.type.equals(that.getType())) {
            return false;
        }
        return true;
    }

    /**
     * 
     */
    public abstract com.communote.server.model.security.ChannelType getChannel();

    /**
     * 
     */
    public String getType() {
        return this.type;
    }

    /**
     * Returns a hash code based on this entity's identifiers.
     */
    public int hashCode() {
        int hashCode = 0;
        hashCode = 29 * hashCode + (type == null ? 0 : type.hashCode());

        return hashCode;
    }

    /**
     * 
     */
    public boolean isEnabled() {
        return this.enabled;
    }

    /**
     * 
     */
    public abstract void setChannel(com.communote.server.model.security.ChannelType channel);

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setType(String type) {
        this.type = type;
    }
}