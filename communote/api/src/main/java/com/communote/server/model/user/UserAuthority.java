package com.communote.server.model.user;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class UserAuthority implements java.io.Serializable {
    /**
     * Constructs new instances of {@link com.communote.server.model.user.UserAuthority}.
     */
    public static final class Factory {
        /**
         * Constructs a new instance of {@link com.communote.server.model.user.UserAuthority}.
         */
        public static com.communote.server.model.user.UserAuthority newInstance() {
            return new com.communote.server.model.user.UserAuthority();
        }

        /**
         * Constructs a new instance of {@link com.communote.server.model.user.UserAuthority},
         * taking all possible properties (except the identifier(s))as arguments.
         */
        public static com.communote.server.model.user.UserAuthority newInstance(
                com.communote.server.model.user.UserRole role) {
            final com.communote.server.model.user.UserAuthority entity = new com.communote.server.model.user.UserAuthority();
            entity.setRole(role);
            return entity;
        }
    }

    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = -1392011517148970626L;

    private com.communote.server.model.user.UserRole role;

    private Long id;

    /**
     * Builds a string showing the current attribute values
     */

    public String attributesToString() {
        StringBuilder sb = new StringBuilder();

        sb.append("class='");
        sb.append(this.getClass().getName());
        sb.append("', ");

        sb.append("role='");
        sb.append(role);
        sb.append("', ");

        sb.append("id='");
        sb.append(id);
        sb.append("', ");

        return sb.toString();
    }

    /**
     * Returns <code>true</code> if the argument is an UserAuthority instance and all identifiers
     * for this entity equal the identifiers of the argument entity. Returns <code>false</code>
     * otherwise.
     */
    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof UserAuthority)) {
            return false;
        }
        final UserAuthority that = (UserAuthority) object;
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
    public com.communote.server.model.user.UserRole getRole() {
        return this.role;
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

    public void setId(Long id) {
        this.id = id;
    }

    public void setRole(com.communote.server.model.user.UserRole role) {
        this.role = role;
    }
}