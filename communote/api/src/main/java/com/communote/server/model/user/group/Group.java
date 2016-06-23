package com.communote.server.model.user.group;

import com.communote.server.model.global.GlobalId;

/**
 * <p>
 * A group which can contain users or other groups.
 * </p>
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class Group extends com.communote.server.model.user.CommunoteEntity {
    /**
     * Constructs new instances of {@link Group}.
     */
    public static final class Factory {
        /**
         * Constructs a new instance of {@link Group}.
         */
        public static Group newInstance() {
            return new Group();
        }

        /**
         * Constructs a new instance of {@link Group}, taking all required and/or read-only
         * properties as arguments.
         */
        public static Group newInstance(String alias, String name) {
            final Group entity = new Group();
            entity.setAlias(alias);
            entity.setName(name);
            return entity;
        }

        /**
         * Constructs a new instance of {@link Group}, taking all possible properties (except the
         * identifier(s))as arguments.
         */
        public static Group newInstance(String alias, String name, String description,
                java.util.Set<com.communote.server.model.user.CommunoteEntity> groupMembers,
                java.util.Set<GroupProperty> properties,
                java.util.Set<com.communote.server.model.blog.BlogMember> memberships,
                com.communote.server.model.global.GlobalId globalId,
                java.util.Set<com.communote.server.model.tag.Tag> tags) {
            final Group entity = new Group();
            entity.setAlias(alias);
            entity.setName(name);
            entity.setDescription(description);
            entity.setGroupMembers(groupMembers);
            entity.setProperties(properties);
            entity.setMemberships(memberships);
            entity.setGlobalId(globalId);
            entity.setTags(tags);
            return entity;
        }
    }

    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = 6710158892242682672L;

    private String alias;

    private String name;

    private String description;

    private java.util.Set<com.communote.server.model.user.CommunoteEntity> groupMembers = new java.util.HashSet<com.communote.server.model.user.CommunoteEntity>();

    private java.util.Set<GroupProperty> properties = new java.util.HashSet<GroupProperty>();

    /**
     * Builds a string showing the current attribute values
     */
    @Override
    public String attributesToString() {
        StringBuilder sb = new StringBuilder();

        sb.append("class='");
        sb.append(this.getClass().getName());
        sb.append("', ");

        sb.append("alias='");
        sb.append(alias);
        sb.append("', ");

        sb.append("name='");
        sb.append(name);
        sb.append("', ");

        sb.append("description='");
        sb.append(description);
        sb.append("', ");

        sb.append(super.attributesToString());

        return sb.toString();
    }

    /**
     * This entity does not have any identifiers but since it extends the
     * <code>com.communote.server.persistence.user.KenmeiEntityImpl</code> class it will simply
     * delegate the call up there.
     *
     * @see com.communote.server.model.user.CommunoteEntity#equals(Object)
     */
    @Override
    public boolean equals(Object object) {
        return super.equals(object);
    }

    /**
     *
     */
    @Override
    public String getAlias() {
        return this.alias;
    }

    /**
     * <p>
     * A short description of the group.
     * </p>
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GlobalId getFollowId() {
        return getGlobalId();
    }

    /**
     *
     */
    public java.util.Set<com.communote.server.model.user.CommunoteEntity> getGroupMembers() {
        return this.groupMembers;
    }

    /**
     * <p>
     * Human readable name of the group.
     * </p>
     */
    public String getName() {
        return this.name;
    }

    /**
     *
     */
    @Override
    public java.util.Set<GroupProperty> getProperties() {
        return this.properties;
    }

    /**
     * This entity does not have any identifiers but since it extends the
     * <code>com.communote.server.persistence.user.KenmeiEntityImpl</code> class it will simply
     * delegate the call up there.
     *
     * @see com.communote.server.model.user.CommunoteEntity#hashCode()
     */
    @Override
    public int hashCode() {
        return super.hashCode();
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setGroupMembers(
            java.util.Set<com.communote.server.model.user.CommunoteEntity> groupMembers) {
        this.groupMembers = groupMembers;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setProperties(java.util.Set<GroupProperty> properties) {
        this.properties = properties;
    }
}