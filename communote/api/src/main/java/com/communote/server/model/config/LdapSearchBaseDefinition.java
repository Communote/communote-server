package com.communote.server.model.config;

/**
 * <p>
 * represents an LDAP search base definition
 * </p>
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class LdapSearchBaseDefinition implements java.io.Serializable {
    /**
     * Constructs new instances of
     * {@link com.communote.server.model.config.LdapSearchBaseDefinition}.
     */
    public static final class Factory {
        /**
         * Constructs a new instance of
         * {@link com.communote.server.model.config.LdapSearchBaseDefinition}.
         */
        public static com.communote.server.model.config.LdapSearchBaseDefinition newInstance() {
            return new com.communote.server.model.config.LdapSearchBaseDefinitionImpl();
        }

        /**
         * Constructs a new instance of
         * {@link com.communote.server.model.config.LdapSearchBaseDefinition}, taking all possible
         * properties (except the identifier(s))as arguments.
         */
        public static com.communote.server.model.config.LdapSearchBaseDefinition newInstance(
                String searchBase, boolean searchSubtree) {
            final com.communote.server.model.config.LdapSearchBaseDefinition entity = new com.communote.server.model.config.LdapSearchBaseDefinitionImpl();
            entity.setSearchBase(searchBase);
            entity.setSearchSubtree(searchSubtree);
            return entity;
        }
    }

    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = 5351560515789204238L;

    private String searchBase;

    private boolean searchSubtree;

    private Long id;

    /**
     * Builds a string showing the current attribute values
     */

    public String attributesToString() {
        StringBuilder sb = new StringBuilder();

        sb.append("class='");
        sb.append(this.getClass().getName());
        sb.append("', ");

        sb.append("searchBase='");
        sb.append(searchBase);
        sb.append("', ");

        sb.append("searchSubtree='");
        sb.append(searchSubtree);
        sb.append("', ");

        sb.append("id='");
        sb.append(id);
        sb.append("', ");

        return sb.toString();
    }

    /**
     * Returns <code>true</code> if the argument is an LdapSearchBaseDefinition instance and all
     * identifiers for this entity equal the identifiers of the argument entity. Returns
     * <code>false</code> otherwise.
     */
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof LdapSearchBaseDefinition)) {
            return false;
        }
        final LdapSearchBaseDefinition that = (LdapSearchBaseDefinition) object;
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
     * search base for starting a search for entries in LDAP directory
     * </p>
     */
    public String getSearchBase() {
        return this.searchBase;
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
     * whether to search the subtree of the search base
     * </p>
     */
    public boolean isSearchSubtree() {
        return this.searchSubtree;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setSearchBase(String searchBase) {
        this.searchBase = searchBase;
    }

    public void setSearchSubtree(boolean searchSubtree) {
        this.searchSubtree = searchSubtree;
    }
}