package com.communote.server.model.blog;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class ExternalBlogMember extends com.communote.server.model.blog.BlogMemberImpl {
    /**
     * Constructs new instances of {@link com.communote.server.model.blog.ExternalBlogMember}.
     */
    public static final class Factory {
        /**
         * Constructs a new instance of {@link com.communote.server.model.blog.ExternalBlogMember}.
         */
        public static com.communote.server.model.blog.ExternalBlogMember newInstance() {
            return new com.communote.server.model.blog.ExternalBlogMemberImpl();
        }

        /**
         * Constructs a new instance of {@link com.communote.server.model.blog.ExternalBlogMember},
         * taking all required and/or read-only properties as arguments.
         */
        public static com.communote.server.model.blog.ExternalBlogMember newInstance(
                com.communote.server.model.blog.BlogRole role,
                com.communote.server.model.blog.Blog blog,
                com.communote.server.model.user.CommunoteEntity kenmeiEntity) {
            final com.communote.server.model.blog.ExternalBlogMember entity = new com.communote.server.model.blog.ExternalBlogMemberImpl();
            entity.setRole(role);
            entity.setBlog(blog);
            entity.setMemberEntity(kenmeiEntity);
            return entity;
        }

        /**
         * Constructs a new instance of {@link com.communote.server.model.blog.ExternalBlogMember},
         * taking all possible properties (except the identifier(s))as arguments.
         */
        public static com.communote.server.model.blog.ExternalBlogMember newInstance(
                String externalSystemId, com.communote.server.model.blog.BlogRole role,
                com.communote.server.model.blog.Blog blog,
                com.communote.server.model.user.CommunoteEntity kenmeiEntity) {
            final com.communote.server.model.blog.ExternalBlogMember entity = new com.communote.server.model.blog.ExternalBlogMemberImpl();
            entity.setExternalSystemId(externalSystemId);
            entity.setRole(role);
            entity.setBlog(blog);
            entity.setMemberEntity(kenmeiEntity);
            return entity;
        }
    }

    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = 6225510311129625636L;

    private String externalSystemId;

    /**
     * Builds a string showing the current attribute values
     */
    @Override
    public String attributesToString() {
        StringBuilder sb = new StringBuilder();

        sb.append("class='");
        sb.append(this.getClass().getName());
        sb.append("', ");

        sb.append("externalSystemId='");
        sb.append(externalSystemId);
        sb.append("', ");

        sb.append(super.attributesToString());

        return sb.toString();
    }

    /**
     * This entity does not have any identifiers but since it extends the
     * <code>com.communote.server.persistence.blog.BlogMemberImpl</code> class it will simply
     * delegate the call up there.
     *
     * @see com.communote.server.model.blog.BlogMember#equals(Object)
     */
    @Override
    public boolean equals(Object object) {
        return super.equals(object);
    }

    /**
     * <p>
     * The systemId of the system that created/added the member. If the value is null, the member
     * was created by communote.
     * </p>
     */
    @Override
    public String getExternalSystemId() {
        return this.externalSystemId;
    }

    /**
     * This entity does not have any identifiers but since it extends the
     * <code>com.communote.server.persistence.blog.BlogMemberImpl</code> class it will simply
     * delegate the call up there.
     *
     * @see com.communote.server.model.blog.BlogMember#hashCode()
     */
    @Override
    public int hashCode() {
        return super.hashCode();
    }

    public void setExternalSystemId(String externalSystemId) {
        this.externalSystemId = externalSystemId;
    }
}