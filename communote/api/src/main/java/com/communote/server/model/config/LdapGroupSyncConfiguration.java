package com.communote.server.model.config;

/**
 * <p>
 * configuration data for synchronizing groups of the defined LDAP
 * </p>
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class LdapGroupSyncConfiguration implements java.io.Serializable {
    /**
     * Constructs new instances of
     * {@link com.communote.server.model.config.LdapGroupSyncConfiguration}.
     */
    public static final class Factory {
        /**
         * Constructs a new instance of
         * {@link com.communote.server.model.config.LdapGroupSyncConfiguration}.
         */
        public static com.communote.server.model.config.LdapGroupSyncConfiguration newInstance() {
            return new com.communote.server.model.config.LdapGroupSyncConfigurationImpl();
        }

        /**
         * Constructs a new instance of
         * {@link com.communote.server.model.config.LdapGroupSyncConfiguration}, taking all possible
         * properties (except the identifier(s))as arguments.
         */
        public static com.communote.server.model.config.LdapGroupSyncConfiguration newInstance(
                boolean memberMode, boolean groupIdentifierIsBinary,
                com.communote.server.model.config.LdapConfiguration ldapConfiguration,
                com.communote.server.model.config.LdapSearchConfiguration groupSearch) {
            final com.communote.server.model.config.LdapGroupSyncConfiguration entity = new com.communote.server.model.config.LdapGroupSyncConfigurationImpl();
            entity.setMemberMode(memberMode);
            entity.setGroupIdentifierIsBinary(groupIdentifierIsBinary);
            entity.setLdapConfiguration(ldapConfiguration);
            entity.setGroupSearch(groupSearch);
            return entity;
        }
    }

    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = 7361115550846186836L;

    private boolean memberMode;

    private boolean groupIdentifierIsBinary;

    private Long id;

    private com.communote.server.model.config.LdapConfiguration ldapConfiguration;

    private com.communote.server.model.config.LdapSearchConfiguration groupSearch;

    /**
     * Builds a string showing the current attribute values
     */

    public String attributesToString() {
        StringBuilder sb = new StringBuilder();

        sb.append("class='");
        sb.append(this.getClass().getName());
        sb.append("', ");

        sb.append("memberMode='");
        sb.append(memberMode);
        sb.append("', ");

        sb.append("groupIdentifierIsBinary='");
        sb.append(groupIdentifierIsBinary);
        sb.append("', ");

        sb.append("id='");
        sb.append(id);
        sb.append("', ");

        return sb.toString();
    }

    /**
     * Returns <code>true</code> if the argument is an LdapGroupSyncConfiguration instance and all
     * identifiers for this entity equal the identifiers of the argument entity. Returns
     * <code>false</code> otherwise.
     */
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof LdapGroupSyncConfiguration)) {
            return false;
        }
        final LdapGroupSyncConfiguration that = (LdapGroupSyncConfiguration) object;
        if (this.id == null || that.getId() == null || !this.id.equals(that.getId())) {
            return false;
        }
        return true;
    }

    /**
     * 
     */
    public com.communote.server.model.config.LdapSearchConfiguration getGroupSearch() {
        return this.groupSearch;
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
    public com.communote.server.model.config.LdapConfiguration getLdapConfiguration() {
        return this.ldapConfiguration;
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
     * true if the value of the LDAP attribute mapped in propertyMapping to the unique group
     * identifier is to be interpreted as a binary value
     * </p>
     */
    public boolean isGroupIdentifierIsBinary() {
        return this.groupIdentifierIsBinary;
    }

    /**
     * <p>
     * defines how the synchronizationAttribute should be interpreted. If true than the
     * synchronizationAttribute refers to the member relation, if false it's the memberOf relation.
     * </p>
     */
    public boolean isMemberMode() {
        return this.memberMode;
    }

    public void setGroupIdentifierIsBinary(boolean groupIdentifierIsBinary) {
        this.groupIdentifierIsBinary = groupIdentifierIsBinary;
    }

    public void setGroupSearch(com.communote.server.model.config.LdapSearchConfiguration groupSearch) {
        this.groupSearch = groupSearch;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setLdapConfiguration(
            com.communote.server.model.config.LdapConfiguration ldapConfiguration) {
        this.ldapConfiguration = ldapConfiguration;
    }

    public void setMemberMode(boolean memberMode) {
        this.memberMode = memberMode;
    }
}