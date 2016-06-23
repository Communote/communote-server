package com.communote.server.model.user.security;

/**
 * 
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class AuthenticationFailedStatus implements java.io.Serializable {
    /**
     * Constructs new instances of
     * {@link com.communote.server.model.user.security.AuthenticationFailedStatus}.
     */
    public static final class Factory {
        /**
         * Constructs a new instance of
         * {@link com.communote.server.model.user.security.AuthenticationFailedStatus}.
         */
        public static com.communote.server.model.user.security.AuthenticationFailedStatus newInstance() {
            return new com.communote.server.model.user.security.AuthenticationFailedStatusImpl();
        }

        /**
         * Constructs a new instance of
         * {@link com.communote.server.model.user.security.AuthenticationFailedStatus}, taking all
         * required and/or read-only properties as arguments.
         */
        public static com.communote.server.model.user.security.AuthenticationFailedStatus newInstance(
                int failedAuthCounter, com.communote.server.model.security.ChannelType channelType) {
            final com.communote.server.model.user.security.AuthenticationFailedStatus entity = new com.communote.server.model.user.security.AuthenticationFailedStatusImpl();
            entity.setFailedAuthCounter(failedAuthCounter);
            entity.setChannelType(channelType);
            return entity;
        }

        /**
         * Constructs a new instance of
         * {@link com.communote.server.model.user.security.AuthenticationFailedStatus}, taking all
         * possible properties (except the identifier(s))as arguments.
         */
        public static com.communote.server.model.user.security.AuthenticationFailedStatus newInstance(
                java.sql.Timestamp lockedTimeout, int failedAuthCounter,
                com.communote.server.model.security.ChannelType channelType) {
            final com.communote.server.model.user.security.AuthenticationFailedStatus entity = new com.communote.server.model.user.security.AuthenticationFailedStatusImpl();
            entity.setLockedTimeout(lockedTimeout);
            entity.setFailedAuthCounter(failedAuthCounter);
            entity.setChannelType(channelType);
            return entity;
        }
    }

    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = 5011969570906014980L;

    private java.sql.Timestamp lockedTimeout;

    private int failedAuthCounter;

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

        sb.append("lockedTimeout='");
        sb.append(lockedTimeout);
        sb.append("', ");

        sb.append("failedAuthCounter='");
        sb.append(failedAuthCounter);
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
     * Returns <code>true</code> if the argument is an AuthenticationFailedStatus instance and all
     * identifiers for this entity equal the identifiers of the argument entity. Returns
     * <code>false</code> otherwise.
     */
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof AuthenticationFailedStatus)) {
            return false;
        }
        final AuthenticationFailedStatus that = (AuthenticationFailedStatus) object;
        if (this.id == null || that.getId() == null || !this.id.equals(that.getId())) {
            return false;
        }
        return true;
    }

    /**
     * <p>
     * type of channel
     * </p>
     */
    public com.communote.server.model.security.ChannelType getChannelType() {
        return this.channelType;
    }

    /**
     * <p>
     * counter of failed auth attemps
     * </p>
     */
    public int getFailedAuthCounter() {
        return this.failedAuthCounter;
    }

    /**
     * 
     */
    public Long getId() {
        return this.id;
    }

    /**
     * <p>
     * time of locking a user account for a specific channel
     * </p>
     */
    public java.sql.Timestamp getLockedTimeout() {
        return this.lockedTimeout;
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

    public void setFailedAuthCounter(int failedAuthCounter) {
        this.failedAuthCounter = failedAuthCounter;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setLockedTimeout(java.sql.Timestamp lockedTimeout) {
        this.lockedTimeout = lockedTimeout;
    }
}