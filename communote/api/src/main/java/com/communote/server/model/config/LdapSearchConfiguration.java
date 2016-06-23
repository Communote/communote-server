package com.communote.server.model.config;

/**
 * 
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class LdapSearchConfiguration implements java.io.Serializable {
    /**
     * Constructs new instances of {@link com.communote.server.model.config.LdapSearchConfiguration}
     * .
     */
    public static final class Factory {
        /**
         * Constructs a new instance of
         * {@link com.communote.server.model.config.LdapSearchConfiguration}.
         */
        public static com.communote.server.model.config.LdapSearchConfiguration newInstance() {
            return new com.communote.server.model.config.LdapSearchConfigurationImpl();
        }

        /**
         * Constructs a new instance of
         * {@link com.communote.server.model.config.LdapSearchConfiguration}, taking all required
         * and/or read-only properties as arguments.
         */
        public static com.communote.server.model.config.LdapSearchConfiguration newInstance(
                String propertyMapping,
                java.util.List<com.communote.server.model.config.LdapSearchBaseDefinition> searchBases) {
            final com.communote.server.model.config.LdapSearchConfiguration entity = new com.communote.server.model.config.LdapSearchConfigurationImpl();
            entity.setPropertyMapping(propertyMapping);
            entity.setSearchBases(searchBases);
            return entity;
        }

        /**
         * Constructs a new instance of
         * {@link com.communote.server.model.config.LdapSearchConfiguration}, taking all possible
         * properties (except the identifier(s))as arguments.
         */
        public static com.communote.server.model.config.LdapSearchConfiguration newInstance(
                String searchFilter,
                String propertyMapping,
                java.util.List<com.communote.server.model.config.LdapSearchBaseDefinition> searchBases) {
            final com.communote.server.model.config.LdapSearchConfiguration entity = new com.communote.server.model.config.LdapSearchConfigurationImpl();
            entity.setSearchFilter(searchFilter);
            entity.setPropertyMapping(propertyMapping);
            entity.setSearchBases(searchBases);
            return entity;
        }
    }

    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = -5663195874280934283L;

    private String searchFilter;

    private String propertyMapping;

    private Long id;

    private java.util.List<com.communote.server.model.config.LdapSearchBaseDefinition> searchBases = new java.util.ArrayList<com.communote.server.model.config.LdapSearchBaseDefinition>();

    /**
     * Builds a string showing the current attribute values
     */

    public String attributesToString() {
        StringBuilder sb = new StringBuilder();

        sb.append("class='");
        sb.append(this.getClass().getName());
        sb.append("', ");

        sb.append("searchFilter='");
        sb.append(searchFilter);
        sb.append("', ");

        sb.append("propertyMapping='");
        sb.append(propertyMapping);
        sb.append("', ");

        sb.append("id='");
        sb.append(id);
        sb.append("', ");

        return sb.toString();
    }

    /**
     * Returns <code>true</code> if the argument is an LdapSearchConfiguration instance and all
     * identifiers for this entity equal the identifiers of the argument entity. Returns
     * <code>false</code> otherwise.
     */
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof LdapSearchConfiguration)) {
            return false;
        }
        final LdapSearchConfiguration that = (LdapSearchConfiguration) object;
        if (this.id == null || that.getId() == null || !this.id.equals(that.getId())) {
            return false;
        }
        return true;
    }

    /**
     * 
     */
    public Long getId() {
        return this.id;
    }

    /**
     * <p>
     * mapping that maps communote property names to the names of LDAP attributes
     * </p>
     */
    public String getPropertyMapping() {
        return this.propertyMapping;
    }

    /**
     * 
     */
    public java.util.List<com.communote.server.model.config.LdapSearchBaseDefinition> getSearchBases() {
        return this.searchBases;
    }

    /**
     * <p>
     * an LDAP search filter to narrow the result set
     * </p>
     */
    public String getSearchFilter() {
        return this.searchFilter;
    }

    /**
     * Returns a hash code based on this entity's identifiers.
     */
    public int hashCode() {
        int hashCode = 0;
        hashCode = 29 * hashCode + (id == null ? 0 : id.hashCode());

        return hashCode;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setPropertyMapping(String propertyMapping) {
        this.propertyMapping = propertyMapping;
    }

    public void setSearchBases(
            java.util.List<com.communote.server.model.config.LdapSearchBaseDefinition> searchBases) {
        this.searchBases = searchBases;
    }

    public void setSearchFilter(String searchFilter) {
        this.searchFilter = searchFilter;
    }
}