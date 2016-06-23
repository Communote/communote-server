package com.communote.server.model.tag;

/**
 * 
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class CategorizedTag extends com.communote.server.model.tag.TagImpl {
    /**
     * Constructs new instances of {@link com.communote.server.model.tag.CategorizedTag}.
     */
    public static final class Factory {
        /**
         * Constructs a new instance of {@link com.communote.server.model.tag.CategorizedTag}.
         */
        public static com.communote.server.model.tag.CategorizedTag newInstance() {
            return new com.communote.server.model.tag.CategorizedTagImpl();
        }

        /**
         * Constructs a new instance of {@link com.communote.server.model.tag.CategorizedTag},
         * taking all required and/or read-only properties as arguments.
         */
        public static com.communote.server.model.tag.CategorizedTag newInstance(
                String tagStoreTagId, String defaultName, String tagStoreAlias) {
            final com.communote.server.model.tag.CategorizedTag entity = new com.communote.server.model.tag.CategorizedTagImpl();
            entity.setTagStoreTagId(tagStoreTagId);
            entity.setDefaultName(defaultName);
            entity.setTagStoreAlias(tagStoreAlias);
            return entity;
        }

        /**
         * Constructs a new instance of {@link com.communote.server.model.tag.CategorizedTag},
         * taking all possible properties (except the identifier(s))as arguments.
         */
        public static com.communote.server.model.tag.CategorizedTag newInstance(
                String tagStoreTagId, String defaultName, String tagStoreAlias,
                com.communote.server.model.tag.AbstractTagCategory category,
                com.communote.server.model.global.GlobalId globalId,
                java.util.Set<com.communote.server.model.i18n.Message> names,
                java.util.Set<com.communote.server.model.i18n.Message> descriptions) {
            final com.communote.server.model.tag.CategorizedTag entity = new com.communote.server.model.tag.CategorizedTagImpl();
            entity.setTagStoreTagId(tagStoreTagId);
            entity.setDefaultName(defaultName);
            entity.setTagStoreAlias(tagStoreAlias);
            entity.setCategory(category);
            entity.setGlobalId(globalId);
            entity.setNames(names);
            entity.setDescriptions(descriptions);
            return entity;
        }
    }

    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = 5720183398673191971L;

    private com.communote.server.model.tag.AbstractTagCategory category;

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
     * <code>com.communote.server.persistence.tag.TagImpl</code> class it will simply delegate the
     * call up there.
     *
     * @see com.communote.server.model.tag.Tag#equals(Object)
     */
    public boolean equals(Object object) {
        return super.equals(object);
    }

    /**
     * 
     */
    public com.communote.server.model.tag.AbstractTagCategory getCategory() {
        return this.category;
    }

    /**
     * This entity does not have any identifiers but since it extends the
     * <code>com.communote.server.persistence.tag.TagImpl</code> class it will simply delegate the
     * call up there.
     *
     * @see com.communote.server.model.tag.Tag#hashCode()
     */
    public int hashCode() {
        return super.hashCode();
    }

    public void setCategory(com.communote.server.model.tag.AbstractTagCategory category) {
        this.category = category;
    }
}