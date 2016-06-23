package com.communote.server.model.user.group;

/**
 * <p>
 * Association class which connects a group with all user members, including the users of the
 * transitivly contained groups.
 * </p>
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class UserOfGroup implements java.io.Serializable {
    /**
     * Constructs new instances of {@link com.communote.server.model.user.group.UserOfGroup}.
     */
    public static final class Factory {
        /**
         * Constructs a new instance of {@link com.communote.server.model.user.group.UserOfGroup}.
         */
        public static com.communote.server.model.user.group.UserOfGroup newInstance() {
            return new com.communote.server.model.user.group.UserOfGroupImpl();
        }

        /**
         * Constructs a new instance of {@link com.communote.server.model.user.group.UserOfGroup},
         * taking all required and/or read-only properties as arguments.
         */
        public static com.communote.server.model.user.group.UserOfGroup newInstance(
                com.communote.server.model.user.group.Group group,
                com.communote.server.model.user.User user) {
            final com.communote.server.model.user.group.UserOfGroup entity = new com.communote.server.model.user.group.UserOfGroupImpl();
            entity.setGroup(group);
            entity.setUser(user);
            return entity;
        }

        /**
         * Constructs a new instance of {@link com.communote.server.model.user.group.UserOfGroup},
         * taking all possible properties (except the identifier(s))as arguments.
         */
        public static com.communote.server.model.user.group.UserOfGroup newInstance(
                com.communote.server.model.user.group.UserOfGroupModificationType modificationType,
                com.communote.server.model.user.group.Group group,
                com.communote.server.model.user.User user) {
            final com.communote.server.model.user.group.UserOfGroup entity = new com.communote.server.model.user.group.UserOfGroupImpl();
            entity.setModificationType(modificationType);
            entity.setGroup(group);
            entity.setUser(user);
            return entity;
        }
    }

    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = -1207231293519214620L;

    private com.communote.server.model.user.group.UserOfGroupModificationType modificationType;

    private Long id;

    private com.communote.server.model.user.group.Group group;

    private com.communote.server.model.user.User user;

    /**
     * Builds a string showing the current attribute values
     */

    public String attributesToString() {
        StringBuilder sb = new StringBuilder();

        sb.append("class='");
        sb.append(this.getClass().getName());
        sb.append("', ");

        sb.append("modificationType='");
        sb.append(modificationType);
        sb.append("', ");

        sb.append("id='");
        sb.append(id);
        sb.append("', ");

        return sb.toString();
    }

    /**
     * Returns <code>true</code> if the argument is an UserOfGroup instance and all identifiers for
     * this entity equal the identifiers of the argument entity. Returns <code>false</code>
     * otherwise.
     */
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof UserOfGroup)) {
            return false;
        }
        final UserOfGroup that = (UserOfGroup) object;
        if (this.id == null || that.getId() == null || !this.id.equals(that.getId())) {
            return false;
        }
        return true;
    }

    /**
     * 
     */
    public com.communote.server.model.user.group.Group getGroup() {
        return this.group;
    }

    /**
     * 
     */
    public Long getId() {
        return this.id;
    }

    /**
     * <p>
     * Denotes whether the the UserOfGroup entity was modified recently and thus needs further
     * processing.
     * </p>
     */
    public com.communote.server.model.user.group.UserOfGroupModificationType getModificationType() {
        return this.modificationType;
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
    public int hashCode() {
        int hashCode = 0;
        hashCode = 29 * hashCode + (id == null ? 0 : id.hashCode());

        return hashCode;
    }

    public void setGroup(com.communote.server.model.user.group.Group group) {
        this.group = group;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setModificationType(
            com.communote.server.model.user.group.UserOfGroupModificationType modificationType) {
        this.modificationType = modificationType;
    }

    public void setUser(com.communote.server.model.user.User user) {
        this.user = user;
    }
}