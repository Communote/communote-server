package com.communote.server.model.security;

/**
 * A security code that should be confirmed by some user.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class SecurityCode implements java.io.Serializable {
    /**
     * Constructs new instances of {@link SecurityCode}.
     */
    public static final class Factory {
        /**
         * Constructs a new instance of {@link SecurityCode}.
         */
        public static SecurityCode newInstance() {
            return new SecurityCodeImpl();
        }

        /**
         * Constructs a new instance of {@link SecurityCode}, taking all required and/or read-only
         * properties as arguments.
         */
        public static SecurityCode newInstance(String code, SecurityCodeAction action,
                java.sql.Timestamp creatingDate) {
            final SecurityCode entity = new SecurityCodeImpl();
            entity.setCode(code);
            entity.setAction(action);
            entity.setCreatingDate(creatingDate);
            return entity;
        }

        /**
         * Constructs a new instance of {@link SecurityCode}, taking all possible properties (except
         * the identifier(s))as arguments.
         */
        public static SecurityCode newInstance(String code, SecurityCodeAction action,
                java.sql.Timestamp creatingDate, com.communote.server.model.user.User kenmeiUser) {
            final SecurityCode entity = new SecurityCodeImpl();
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
    private static final long serialVersionUID = -1724105405661434070L;

    private String code;

    private SecurityCodeAction action;

    private java.sql.Timestamp creatingDate;

    private Long id;

    private com.communote.server.model.user.User user;

    /**
     * Builds a string showing the current attribute values
     */

    public String attributesToString() {
        StringBuilder sb = new StringBuilder();

        sb.append("class='");
        sb.append(this.getClass().getName());
        sb.append("', ");

        sb.append("code='");
        sb.append(code);
        sb.append("', ");

        sb.append("action='");
        sb.append(action);
        sb.append("', ");

        sb.append("creatingDate='");
        sb.append(creatingDate);
        sb.append("', ");

        sb.append("id='");
        sb.append(id);
        sb.append("', ");

        return sb.toString();
    }

    /**
     * Returns <code>true</code> if the argument is an SecurityCode instance and all identifiers for
     * this entity equal the identifiers of the argument entity. Returns <code>false</code>
     * otherwise.
     */
    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof SecurityCode)) {
            return false;
        }
        final SecurityCode that = (SecurityCode) object;
        if (this.id == null || that.getId() == null || !this.id.equals(that.getId())) {
            return false;
        }
        return true;
    }

    /**
     * <p>
     * Generates a new Guid.
     * </p>
     */
    public abstract void generateNewCode();

    /**
     *
     */
    public SecurityCodeAction getAction() {
        return this.action;
    }

    /**
     *
     */
    public String getCode() {
        return this.code;
    }

    /**
     *
     */
    public java.sql.Timestamp getCreatingDate() {
        return this.creatingDate;
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
    public com.communote.server.model.user.User getUser() {
        return this.user;
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

    public void setAction(SecurityCodeAction action) {
        this.action = action;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setCreatingDate(java.sql.Timestamp creatingDate) {
        this.creatingDate = creatingDate;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setUser(com.communote.server.model.user.User user) {
        this.user = user;
    }

}