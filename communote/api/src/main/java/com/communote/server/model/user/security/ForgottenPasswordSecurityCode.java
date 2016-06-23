package com.communote.server.model.user.security;

/**
 * 
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class ForgottenPasswordSecurityCode extends
        com.communote.server.model.security.SecurityCodeImpl {
    /**
     * Constructs new instances of
     * {@link com.communote.server.model.user.security.ForgottenPasswordSecurityCode}.
     */
    public static final class Factory {
        /**
         * Constructs a new instance of
         * {@link com.communote.server.model.user.security.ForgottenPasswordSecurityCode}.
         */
        public static com.communote.server.model.user.security.ForgottenPasswordSecurityCode newInstance() {
            return new com.communote.server.model.user.security.ForgottenPasswordSecurityCodeImpl();
        }

        /**
         * Constructs a new instance of
         * {@link com.communote.server.model.user.security.ForgottenPasswordSecurityCode}, taking
         * all required and/or read-only properties as arguments.
         */
        public static com.communote.server.model.user.security.ForgottenPasswordSecurityCode newInstance(
                String code, com.communote.server.model.security.SecurityCodeAction action,
                java.sql.Timestamp creatingDate) {
            final com.communote.server.model.user.security.ForgottenPasswordSecurityCode entity = new com.communote.server.model.user.security.ForgottenPasswordSecurityCodeImpl();
            entity.setCode(code);
            entity.setAction(action);
            entity.setCreatingDate(creatingDate);
            return entity;
        }

        /**
         * Constructs a new instance of
         * {@link com.communote.server.model.user.security.ForgottenPasswordSecurityCode}, taking
         * all possible properties (except the identifier(s))as arguments.
         */
        public static com.communote.server.model.user.security.ForgottenPasswordSecurityCode newInstance(
                String code, com.communote.server.model.security.SecurityCodeAction action,
                java.sql.Timestamp creatingDate, com.communote.server.model.user.User kenmeiUser) {
            final com.communote.server.model.user.security.ForgottenPasswordSecurityCode entity = new com.communote.server.model.user.security.ForgottenPasswordSecurityCodeImpl();
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
    private static final long serialVersionUID = -4555725202762376499L;

    /**
     * Builds a string showing the current attribute values
     */
    @Override
    public String attributesToString() {
        StringBuilder sb = new StringBuilder();

        sb.append("class='");
        sb.append(this.getClass().getName());
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
     * This entity does not have any identifiers but since it extends the
     * <code>com.communote.server.persistence.common.security.SecurityCodeImpl</code> class it will
     * simply delegate the call up there.
     *
     * @see com.communote.server.model.security.SecurityCode#hashCode()
     */
    public int hashCode() {
        return super.hashCode();
    }
}