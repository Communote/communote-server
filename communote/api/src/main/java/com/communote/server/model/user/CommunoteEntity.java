package com.communote.server.model.user;

/**
 * Parent type of users and groups.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
// TODO what is a better name
public abstract class CommunoteEntity implements java.io.Serializable,
        com.communote.server.model.follow.Followable,
        com.communote.server.model.property.Propertyable, com.communote.server.model.tag.Taggable {
    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = 3560800605207753153L;

    private Long id;

    private java.util.Set<com.communote.server.model.blog.BlogMember> memberships = new java.util.HashSet<com.communote.server.model.blog.BlogMember>();

    private com.communote.server.model.global.GlobalId globalId;

    private java.util.Set<com.communote.server.model.user.group.Group> groups = new java.util.HashSet<com.communote.server.model.user.group.Group>();

    private java.util.Set<com.communote.server.model.tag.Tag> tags = new java.util.HashSet<com.communote.server.model.tag.Tag>();

    /**
     * Builds a string showing the current attribute values
     */

    public String attributesToString() {
        StringBuilder sb = new StringBuilder();

        sb.append("class='");
        sb.append(this.getClass().getName());
        sb.append("', ");

        sb.append("id='");
        sb.append(id);
        sb.append("', ");

        return sb.toString();
    }

    /**
     * Returns <code>true</code> if the argument is an CommunoteEntity instance and all identifiers
     * for this entity equal the identifiers of the argument entity. Returns <code>false</code>
     * otherwise.
     */
    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof CommunoteEntity)) {
            return false;
        }
        final CommunoteEntity that = (CommunoteEntity) object;
        if (this.id == null || that.getId() == null || !this.id.equals(that.getId())) {
            return false;
        }
        return true;
    }

    /**
     * <p>
     * Return the alias either of group or of user
     * </p>
     */
    public abstract String getAlias();

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
    public java.util.Set<com.communote.server.model.user.group.Group> getGroups() {
        return this.groups;
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
    public java.util.Set<com.communote.server.model.blog.BlogMember> getMemberships() {
        return this.memberships;
    }

    /**
     * <p>
     * Properties
     * </p>
     */
    @Override
    public abstract java.util.Set<? extends com.communote.server.model.property.StringProperty> getProperties();

    /**
     *
     */
    @Override
    public java.util.Set<com.communote.server.model.tag.Tag> getTags() {
        return this.tags;
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

    @Override
    public void setGlobalId(com.communote.server.model.global.GlobalId globalId) {
        this.globalId = globalId;
    }

    public void setGroups(java.util.Set<com.communote.server.model.user.group.Group> groups) {
        this.groups = groups;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setMemberships(java.util.Set<com.communote.server.model.blog.BlogMember> memberships) {
        this.memberships = memberships;
    }

    @Override
    public void setTags(java.util.Set<com.communote.server.model.tag.Tag> tags) {
        this.tags = tags;
    }
}