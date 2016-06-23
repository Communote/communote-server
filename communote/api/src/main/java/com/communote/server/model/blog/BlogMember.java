package com.communote.server.model.blog;

/**
 * Represents a member of blog who is characterized by his role.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class BlogMember implements java.io.Serializable {
    /**
     * Constructs new instances of {@link com.communote.server.model.blog.BlogMember}.
     */
    public static final class Factory {
        /**
         * Constructs a new instance of {@link com.communote.server.model.blog.BlogMember}.
         */
        public static com.communote.server.model.blog.BlogMember newInstance() {
            return new com.communote.server.model.blog.BlogMemberImpl();
        }

        /**
         * Constructs a new instance of {@link com.communote.server.model.blog.BlogMember}, taking
         * all possible properties (except the identifier(s))as arguments.
         */
        public static com.communote.server.model.blog.BlogMember newInstance(
                com.communote.server.model.blog.BlogRole role,
                com.communote.server.model.blog.Blog blog,
                com.communote.server.model.user.CommunoteEntity kenmeiEntity) {
            final com.communote.server.model.blog.BlogMember entity = new com.communote.server.model.blog.BlogMemberImpl();
            entity.setRole(role);
            entity.setBlog(blog);
            entity.setMemberEntity(kenmeiEntity);
            return entity;
        }
    }

    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = 324033498248867996L;

    private com.communote.server.model.blog.BlogRole role;

    private Long id;

    private com.communote.server.model.blog.Blog blog;

    private com.communote.server.model.user.CommunoteEntity memberEntity;

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
     * Returns <code>true</code> if the argument is an BlogMember instance and all identifiers for
     * this entity equal the identifiers of the argument entity. Returns <code>false</code>
     * otherwise.
     */
    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof BlogMember)) {
            return false;
        }
        final BlogMember that = (BlogMember) object;
        if (this.id == null || that.getId() == null || !this.id.equals(that.getId())) {
            return false;
        }
        return true;
    }

    /**
     *
     */
    public com.communote.server.model.blog.Blog getBlog() {
        return this.blog;
    }

    /**
     * <p>
     * The systemId of the system that created/added the member. If the value is null, the member
     * was created by communote.
     * </p>
     */
    public abstract String getExternalSystemId();

    /**
     *
     */
    public Long getId() {
        return this.id;
    }

    /**
     * @return the entity that is a member of the blog
     */
    public com.communote.server.model.user.CommunoteEntity getMemberEntity() {
        return this.memberEntity;
    }

    /**
     * <p>
     * The role of the member in the bog.
     * </p>
     */
    public com.communote.server.model.blog.BlogRole getRole() {
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

    public void setBlog(com.communote.server.model.blog.Blog blog) {
        this.blog = blog;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setMemberEntity(com.communote.server.model.user.CommunoteEntity entity) {
        this.memberEntity = entity;
    }

    public void setRole(com.communote.server.model.blog.BlogRole role) {
        this.role = role;
    }
}