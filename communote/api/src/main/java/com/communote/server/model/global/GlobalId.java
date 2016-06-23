package com.communote.server.model.global;

/**
 * 
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class GlobalId implements java.io.Serializable {
    /**
     * Constructs new instances of {@link com.communote.server.model.global.GlobalId}.
     */
    public static final class Factory {
        /**
         * Constructs a new instance of {@link com.communote.server.model.global.GlobalId}.
         */
        public static com.communote.server.model.global.GlobalId newInstance() {
            return new com.communote.server.model.global.GlobalIdImpl();
        }

        /**
         * Constructs a new instance of {@link com.communote.server.model.global.GlobalId}, taking
         * all possible properties (except the identifier(s))as arguments.
         */
        public static com.communote.server.model.global.GlobalId newInstance(
                String globalIdentifier,
                java.util.Set<com.communote.server.model.user.User> followers) {
            final com.communote.server.model.global.GlobalId entity = new com.communote.server.model.global.GlobalIdImpl();
            entity.setGlobalIdentifier(globalIdentifier);
            entity.setFollowers(followers);
            return entity;
        }
    }

    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = -4122046334446759954L;

    private String globalIdentifier;

    private Long id;

    private java.util.Set<com.communote.server.model.user.User> followers = new java.util.HashSet<com.communote.server.model.user.User>();

    /**
     * Builds a string showing the current attribute values
     */

    public String attributesToString() {
        StringBuilder sb = new StringBuilder();

        sb.append("class='");
        sb.append(this.getClass().getName());
        sb.append("', ");

        sb.append("globalIdentifier='");
        sb.append(globalIdentifier);
        sb.append("', ");

        sb.append("id='");
        sb.append(id);
        sb.append("', ");

        return sb.toString();
    }

    /**
     * Returns <code>true</code> if the argument is an GlobalId instance and all identifiers for
     * this entity equal the identifiers of the argument entity. Returns <code>false</code>
     * otherwise.
     */
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof GlobalId)) {
            return false;
        }
        final GlobalId that = (GlobalId) object;
        if (this.id == null || that.getId() == null || !this.id.equals(that.getId())) {
            return false;
        }
        return true;
    }

    /**
     * 
     */
    public java.util.Set<com.communote.server.model.user.User> getFollowers() {
        return this.followers;
    }

    /**
     * <p>
     * The global identifier value
     * </p>
     */
    public String getGlobalIdentifier() {
        return this.globalIdentifier;
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

    public void setFollowers(java.util.Set<com.communote.server.model.user.User> followers) {
        this.followers = followers;
    }

    public void setGlobalIdentifier(String globalIdentifier) {
        this.globalIdentifier = globalIdentifier;
    }

    public void setId(Long id) {
        this.id = id;
    }
}