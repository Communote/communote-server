package com.communote.server.model.user.group;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class ExternalUserGroup extends Group {
    /**
     * Constructs new instances of {@link ExternalUserGroup}.
     */
    public static final class Factory {
        /**
         * Constructs a new instance of {@link ExternalUserGroup}.
         */
        public static ExternalUserGroup newInstance() {
            return new ExternalUserGroupImpl();
        }

        /**
         * Constructs a new instance of {@link ExternalUserGroup}, taking all required and/or
         * read-only properties as arguments.
         */
        public static ExternalUserGroup newInstance(String externalSystemId, String externalId,
                String alias, String name) {
            final ExternalUserGroup entity = new ExternalUserGroupImpl();
            entity.setExternalSystemId(externalSystemId);
            entity.setExternalId(externalId);
            entity.setAlias(alias);
            entity.setName(name);
            return entity;
        }

        /**
         * Constructs a new instance of {@link ExternalUserGroup}, taking all possible properties
         * (except the identifier(s))as arguments.
         */
        public static ExternalUserGroup newInstance(String externalSystemId, String externalId,
                String additionalProperty, String alias, String name, String description,
                java.util.Set<com.communote.server.model.user.CommunoteEntity> groupMembers,
                java.util.Set<GroupProperty> properties,
                java.util.Set<com.communote.server.model.blog.BlogMember> memberships,
                com.communote.server.model.global.GlobalId globalId, java.util.Set<Group> groups,
                java.util.Set<com.communote.server.model.tag.Tag> tags) {
            final ExternalUserGroup entity = new ExternalUserGroupImpl();
            entity.setExternalSystemId(externalSystemId);
            entity.setExternalId(externalId);
            entity.setAdditionalProperty(additionalProperty);
            entity.setAlias(alias);
            entity.setName(name);
            entity.setDescription(description);
            entity.setGroupMembers(groupMembers);
            entity.setProperties(properties);
            entity.setMemberships(memberships);
            entity.setGlobalId(globalId);
            entity.setGroups(groups);
            entity.setTags(tags);
            return entity;
        }
    }

    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = -8528246518765209995L;

    private String externalSystemId;

    private String externalId;

    private String additionalProperty;

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

        sb.append("externalId='");
        sb.append(externalId);
        sb.append("', ");

        sb.append("additionalProperty='");
        sb.append(additionalProperty);
        sb.append("', ");

        sb.append(super.attributesToString());

        return sb.toString();
    }

    /**
     * This entity does not have any identifiers but since it extends the <code>Group</code> class
     * it will simply delegate the call up there.
     *
     * @see Group#equals(Object)
     */
    @Override
    public boolean equals(Object object) {
        return super.equals(object);
    }

    /**
     * <p>
     * an optional member holding some additional data for the group. The interpretation depends on
     * the external system. For LDAP it would hold the DN for example.
     * </p>
     */
    public String getAdditionalProperty() {
        return this.additionalProperty;
    }

    /**
     *
     */
    public String getExternalId() {
        return this.externalId;
    }

    /**
     *
     */
    public String getExternalSystemId() {
        return this.externalSystemId;
    }

    /**
     * This entity does not have any identifiers but since it extends the <code>Group</code> class
     * it will simply delegate the call up there.
     *
     * @see Group#hashCode()
     */
    @Override
    public int hashCode() {
        return super.hashCode();
    }

    public void setAdditionalProperty(String additionalProperty) {
        this.additionalProperty = additionalProperty;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public void setExternalSystemId(String externalSystemId) {
        this.externalSystemId = externalSystemId;
    }
}