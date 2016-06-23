package com.communote.server.model.user.security;

/**
 * <p>
 * A security code for unlocking a locked user
 * </p>
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class UnlockUserSecurityCode extends
        com.communote.server.model.security.SecurityCodeImpl {
    /**
     * Constructs new instances of
     * {@link com.communote.server.model.user.security.UnlockUserSecurityCode}.
     */
    public static final class Factory {
        /**
         * Constructs a new instance of
         * {@link com.communote.server.model.user.security.UnlockUserSecurityCode}.
         */
        public static com.communote.server.model.user.security.UnlockUserSecurityCode newInstance() {
            return new com.communote.server.model.user.security.UnlockUserSecurityCodeImpl();
        }

        /**
         * Constructs a new instance of
         * {@link com.communote.server.model.user.security.UnlockUserSecurityCode}, taking all
         * required and/or read-only properties as arguments.
         */
        public static com.communote.server.model.user.security.UnlockUserSecurityCode newInstance(
                com.communote.server.model.security.ChannelType channel, String code,
                com.communote.server.model.security.SecurityCodeAction action,
                java.sql.Timestamp creatingDate) {
            final com.communote.server.model.user.security.UnlockUserSecurityCode entity = new com.communote.server.model.user.security.UnlockUserSecurityCodeImpl();
            entity.setChannel(channel);
            entity.setCode(code);
            entity.setAction(action);
            entity.setCreatingDate(creatingDate);
            return entity;
        }

        /**
         * Constructs a new instance of
         * {@link com.communote.server.model.user.security.UnlockUserSecurityCode}, taking all
         * possible properties (except the identifier(s))as arguments.
         */
        public static com.communote.server.model.user.security.UnlockUserSecurityCode newInstance(
                com.communote.server.model.security.ChannelType channel, String code,
                com.communote.server.model.security.SecurityCodeAction action,
                java.sql.Timestamp creatingDate, com.communote.server.model.user.User kenmeiUser) {
            final com.communote.server.model.user.security.UnlockUserSecurityCode entity = new com.communote.server.model.user.security.UnlockUserSecurityCodeImpl();
            entity.setChannel(channel);
            entity.setCode(code);
            entity.setAction(action);
            entity.setCreatingDate(creatingDate);
            entity.setUser(kenmeiUser);
            return entity;
        }
    }

    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = 1759345309495369755L;

    private com.communote.server.model.security.ChannelType channel;

    /**
     * Builds a string showing the current attribute values
     */
    @Override
    public String attributesToString() {
        StringBuilder sb = new StringBuilder();

        sb.append("class='");
        sb.append(this.getClass().getName());
        sb.append("', ");

        sb.append("channel='");
        sb.append(channel);
        sb.append("', ");

        sb.append(super.attributesToString());

        return sb.toString();
    }

    /**
     * This entity does not have any identifiers but since it extends the
     * <code>com.communote.server.persistence.common.security.SecurityCodeImpl</code> class it will
     * simply delegate the call up there.
     *
     * @see com.communote.server.model.security.SecurityCode#equals(Object)
     */
    public boolean equals(Object object) {
        return super.equals(object);
    }

    /**
     * <p>
     * type of channel
     * </p>
     */
    public com.communote.server.model.security.ChannelType getChannel() {
        return this.channel;
    }

    /**
     * This entity does not have any identifiers but since it extends the
     * <code>com.communote.server.persistence.common.security.SecurityCodeImpl</code> class it will
     * simply delegate the call up there.
     *
     * @see com.communote.server.model.security.SecurityCode#hashCode()
     */
    public int hashCode() {
        return super.hashCode();
    }

    public void setChannel(com.communote.server.model.security.ChannelType channel) {
        this.channel = channel;
    }
}