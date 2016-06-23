package com.communote.server.model.tag;

/**
 * 
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class GlobalTagCategory extends
        com.communote.server.model.tag.AbstractTagCategoryImpl {
    /**
     * Constructs new instances of {@link com.communote.server.model.tag.GlobalTagCategory}.
     */
    public static final class Factory {
        /**
         * Constructs a new instance of {@link com.communote.server.model.tag.GlobalTagCategory}.
         */
        public static com.communote.server.model.tag.GlobalTagCategory newInstance() {
            return new com.communote.server.model.tag.GlobalTagCategoryImpl();
        }

        /**
         * Constructs a new instance of {@link com.communote.server.model.tag.GlobalTagCategory},
         * taking all required and/or read-only properties as arguments.
         */
        public static com.communote.server.model.tag.GlobalTagCategory newInstance(String name,
                String prefix, boolean multipleTags) {
            final com.communote.server.model.tag.GlobalTagCategory entity = new com.communote.server.model.tag.GlobalTagCategoryImpl();
            entity.setName(name);
            entity.setPrefix(prefix);
            entity.setMultipleTags(multipleTags);
            return entity;
        }

        /**
         * Constructs a new instance of {@link com.communote.server.model.tag.GlobalTagCategory},
         * taking all possible properties (except the identifier(s))as arguments.
         */
        public static com.communote.server.model.tag.GlobalTagCategory newInstance(String name,
                String prefix, String description, boolean multipleTags,
                java.util.List<com.communote.server.model.tag.CategorizedTag> tags) {
            final com.communote.server.model.tag.GlobalTagCategory entity = new com.communote.server.model.tag.GlobalTagCategoryImpl();
            entity.setName(name);
            entity.setPrefix(prefix);
            entity.setDescription(description);
            entity.setMultipleTags(multipleTags);
            entity.setTags(tags);
            return entity;
        }
    }

    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = -9040018458634449654L;

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
     * <code>com.communote.server.persistence.tag.AbstractTagCategoryImpl</code> class it will
     * simply delegate the call up there.
     *
     * @see com.communote.server.model.tag.AbstractTagCategory#equals(Object)
     */
    public boolean equals(Object object) {
        return super.equals(object);
    }

    /**
     * This entity does not have any identifiers but since it extends the
     * <code>com.communote.server.persistence.tag.AbstractTagCategoryImpl</code> class it will
     * simply delegate the call up there.
     *
     * @see com.communote.server.model.tag.AbstractTagCategory#hashCode()
     */
    public int hashCode() {
        return super.hashCode();
    }
}