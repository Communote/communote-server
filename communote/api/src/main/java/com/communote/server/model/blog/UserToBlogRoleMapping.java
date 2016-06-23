package com.communote.server.model.blog;

/**
 * <p>
 * A helper entity which maps a user to its effective role in a blog.
 * </p>
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class UserToBlogRoleMapping implements java.io.Serializable {
    /**
     * Constructs new instances of {@link com.communote.server.model.blog.UserToBlogRoleMapping}.
     */
    public static final class Factory {
        /**
         * Constructs a new instance of
         * {@link com.communote.server.model.blog.UserToBlogRoleMapping}.
         */
        public static com.communote.server.model.blog.UserToBlogRoleMapping newInstance() {
            return new com.communote.server.model.blog.UserToBlogRoleMappingImpl();
        }

        /**
         * Constructs a new instance of
         * {@link com.communote.server.model.blog.UserToBlogRoleMapping}, taking all required and/or
         * read-only properties as arguments.
         */
        public static com.communote.server.model.blog.UserToBlogRoleMapping newInstance(
                Long blogId, Long userId, int numericRole, boolean grantedByGroup) {
            final com.communote.server.model.blog.UserToBlogRoleMapping entity = new com.communote.server.model.blog.UserToBlogRoleMappingImpl();
            entity.setBlogId(blogId);
            entity.setUserId(userId);
            entity.setNumericRole(numericRole);
            entity.setGrantedByGroup(grantedByGroup);
            return entity;
        }

        /**
         * Constructs a new instance of
         * {@link com.communote.server.model.blog.UserToBlogRoleMapping}, taking all possible
         * properties (except the identifier(s))as arguments.
         */
        public static com.communote.server.model.blog.UserToBlogRoleMapping newInstance(
                Long blogId, Long userId, int numericRole, String externalSystemId,
                boolean grantedByGroup,
                java.util.Set<com.communote.server.model.user.group.Group> grantingGroups) {
            final com.communote.server.model.blog.UserToBlogRoleMapping entity = new com.communote.server.model.blog.UserToBlogRoleMappingImpl();
            entity.setBlogId(blogId);
            entity.setUserId(userId);
            entity.setNumericRole(numericRole);
            entity.setExternalSystemId(externalSystemId);
            entity.setGrantedByGroup(grantedByGroup);
            entity.setGrantingGroups(grantingGroups);
            return entity;
        }
    }

    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = 7417598038193852003L;

    private Long blogId;

    private Long userId;

    private int numericRole;

    private String externalSystemId;

    private boolean grantedByGroup;

    private Long id;

    private java.util.Set<com.communote.server.model.user.group.Group> grantingGroups = new java.util.HashSet<com.communote.server.model.user.group.Group>();

    /**
     * Builds a string showing the current attribute values
     */

    public String attributesToString() {
        StringBuilder sb = new StringBuilder();

        sb.append("class='");
        sb.append(this.getClass().getName());
        sb.append("', ");

        sb.append("blogId='");
        sb.append(blogId);
        sb.append("', ");

        sb.append("userId='");
        sb.append(userId);
        sb.append("', ");

        sb.append("numericRole='");
        sb.append(numericRole);
        sb.append("', ");

        sb.append("externalSystemId='");
        sb.append(externalSystemId);
        sb.append("', ");

        sb.append("grantedByGroup='");
        sb.append(grantedByGroup);
        sb.append("', ");

        sb.append("id='");
        sb.append(id);
        sb.append("', ");

        return sb.toString();
    }

    /**
     * Returns <code>true</code> if the argument is an UserToBlogRoleMapping instance and all
     * identifiers for this entity equal the identifiers of the argument entity. Returns
     * <code>false</code> otherwise.
     */
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof UserToBlogRoleMapping)) {
            return false;
        }
        final UserToBlogRoleMapping that = (UserToBlogRoleMapping) object;
        if (this.id == null || that.getId() == null || !this.id.equals(that.getId())) {
            return false;
        }
        return true;
    }

    /**
     * <p>
     * ID of the blog
     * </p>
     */
    public Long getBlogId() {
        return this.blogId;
    }

    /**
     * <p>
     * The id of external system where the right got imported from
     * </p>
     */
    public String getExternalSystemId() {
        return this.externalSystemId;
    }

    /**
     * <p>
     * The groups which granted the role to the user because of the membership of the user in the
     * group and adding the group to the blog.
     * </p>
     */
    public java.util.Set<com.communote.server.model.user.group.Group> getGrantingGroups() {
        return this.grantingGroups;
    }

    /**
     * 
     */
    public Long getId() {
        return this.id;
    }

    /**
     * <p>
     * A numeric representation of the role the user has in the blog.
     * </p>
     */
    public int getNumericRole() {
        return this.numericRole;
    }

    /**
     * <p>
     * ID of the user.
     * </p>
     */
    public Long getUserId() {
        return this.userId;
    }

    /**
     * Returns a hash code based on this entity's identifiers.
     */
    public int hashCode() {
        int hashCode = 0;
        hashCode = 29 * hashCode + (id == null ? 0 : id.hashCode());

        return hashCode;
    }

    /**
     * <p>
     * whether the role was granted because of a membership of the user in a group that was added to
     * the blog
     * </p>
     */
    public boolean isGrantedByGroup() {
        return this.grantedByGroup;
    }

    public void setBlogId(Long blogId) {
        this.blogId = blogId;
    }

    public void setExternalSystemId(String externalSystemId) {
        this.externalSystemId = externalSystemId;
    }

    public void setGrantedByGroup(boolean grantedByGroup) {
        this.grantedByGroup = grantedByGroup;
    }

    public void setGrantingGroups(
            java.util.Set<com.communote.server.model.user.group.Group> grantingGroups) {
        this.grantingGroups = grantingGroups;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setNumericRole(int numericRole) {
        this.numericRole = numericRole;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}