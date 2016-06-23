package com.communote.server.model.tag;

/**
 * A tag for some entity. The name of the tag has to be unique (ignoring case)
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class Tag implements java.io.Serializable, com.communote.server.model.tag.TagName,
        com.communote.server.model.follow.Followable {
    /**
     * Constructs new instances of {@link com.communote.server.model.tag.Tag}.
     */
    public static final class Factory {
        /**
         * Constructs a new instance of {@link com.communote.server.model.tag.Tag}.
         */
        public static com.communote.server.model.tag.Tag newInstance() {
            return new com.communote.server.model.tag.TagImpl();
        }

        /**
         * Constructs a new instance of {@link com.communote.server.model.tag.Tag}, taking all
         * required and/or read-only properties as arguments.
         */
        public static com.communote.server.model.tag.Tag newInstance(String tagStoreTagId,
                String defaultName, String tagStoreAlias) {
            final com.communote.server.model.tag.Tag entity = new com.communote.server.model.tag.TagImpl();
            entity.setTagStoreTagId(tagStoreTagId);
            entity.setDefaultName(defaultName);
            entity.setTagStoreAlias(tagStoreAlias);
            return entity;
        }

        /**
         * Constructs a new instance of {@link com.communote.server.model.tag.Tag}, taking all
         * possible properties (except the identifier(s))as arguments.
         */
        public static com.communote.server.model.tag.Tag newInstance(String tagStoreTagId,
                String defaultName, String tagStoreAlias,
                com.communote.server.model.global.GlobalId globalId,
                java.util.Set<com.communote.server.model.i18n.Message> names,
                java.util.Set<com.communote.server.model.i18n.Message> descriptions) {
            final com.communote.server.model.tag.Tag entity = new com.communote.server.model.tag.TagImpl();
            entity.setTagStoreTagId(tagStoreTagId);
            entity.setDefaultName(defaultName);
            entity.setTagStoreAlias(tagStoreAlias);
            entity.setGlobalId(globalId);
            entity.setNames(names);
            entity.setDescriptions(descriptions);
            return entity;
        }
    }

    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = 6282715047366547954L;

    private String tagStoreTagId;

    private String defaultName;

    private String tagStoreAlias;

    private Long id;

    private com.communote.server.model.global.GlobalId globalId;

    private java.util.Set<com.communote.server.model.i18n.Message> names = new java.util.HashSet<com.communote.server.model.i18n.Message>();

    private java.util.Set<com.communote.server.model.i18n.Message> descriptions = new java.util.HashSet<com.communote.server.model.i18n.Message>();

    /**
     * Builds a string showing the current attribute values
     */

    public String attributesToString() {
        StringBuilder sb = new StringBuilder();

        sb.append("class='");
        sb.append(this.getClass().getName());
        sb.append("', ");

        sb.append("tagStoreTagId='");
        sb.append(tagStoreTagId);
        sb.append("', ");

        sb.append("defaultName='");
        sb.append(defaultName);
        sb.append("', ");

        sb.append("tagStoreAlias='");
        sb.append(tagStoreAlias);
        sb.append("', ");

        sb.append("id='");
        sb.append(id);
        sb.append("', ");

        return sb.toString();
    }

    /**
     * Returns <code>true</code> if the argument is an Tag instance and all identifiers for this
     * entity equal the identifiers of the argument entity. Returns <code>false</code> otherwise.
     */
    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof Tag)) {
            return false;
        }
        final Tag that = (Tag) object;
        if (this.id == null || that.getId() == null || !this.id.equals(that.getId())) {
            return false;
        }
        return true;
    }

    /**
     *
     */
    public String getDefaultName() {
        return this.defaultName;
    }

    /**
     *
     */
    public java.util.Set<com.communote.server.model.i18n.Message> getDescriptions() {
        return this.descriptions;
    }

    /**
     *
     */
    @Override
    public abstract com.communote.server.model.global.GlobalId getFollowId();

    /**
     *
     */
    @Override
    public com.communote.server.model.global.GlobalId getGlobalId() {
        return this.globalId;
    }

    /**
     *
     */
    public Long getId() {
        return this.id;
    }

    /**
     * <p>
     * The name of the tag
     * </p>
     */
    @Override
    public abstract String getName();

    /**
     *
     */
    public java.util.Set<com.communote.server.model.i18n.Message> getNames() {
        return this.names;
    }

    /**
     * <p>
     * Alias of the tag store this tag is part of.
     * </p>
     */
    public String getTagStoreAlias() {
        return this.tagStoreAlias;
    }

    /**
     * <p>
     * The lower name of the tag
     * </p>
     */
    public String getTagStoreTagId() {
        return this.tagStoreTagId;
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

    public void setDefaultName(String defaultName) {
        this.defaultName = defaultName;
    }

    public void setDescriptions(java.util.Set<com.communote.server.model.i18n.Message> descriptions) {
        this.descriptions = descriptions;
    }

    @Override
    public void setGlobalId(com.communote.server.model.global.GlobalId globalId) {
        this.globalId = globalId;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setNames(java.util.Set<com.communote.server.model.i18n.Message> names) {
        this.names = names;
    }

    public void setTagStoreAlias(String tagStoreAlias) {
        this.tagStoreAlias = tagStoreAlias;
    }

    public void setTagStoreTagId(String tagStoreTagId) {
        this.tagStoreTagId = tagStoreTagId;
    }
}