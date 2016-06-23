package com.communote.server.model.config;

/**
 * <p>
 * Describes a Configuration to create an LDAP Authenticator.
 * </p>
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class LdapConfiguration extends
        com.communote.server.model.config.ExternalSystemConfiguration {
    /**
     * Constructs new instances of {@link com.communote.server.model.config.LdapConfiguration}.
     */
    public static final class Factory {
        /**
         * Constructs a new instance of {@link com.communote.server.model.config.LdapConfiguration}.
         */
        public static com.communote.server.model.config.LdapConfiguration newInstance() {
            return new com.communote.server.model.config.LdapConfigurationImpl();
        }

        /**
         * Constructs a new instance of {@link com.communote.server.model.config.LdapConfiguration},
         * taking all required and/or read-only properties as arguments.
         */
        public static com.communote.server.model.config.LdapConfiguration newInstance(String url,
                String managerPassword, String managerDN, boolean userIdentifierIsBinary,
                boolean dynamicMode,
                com.communote.server.model.config.LdapSearchConfiguration userSearch,
                boolean allowExternalAuthentication, String systemId,
                boolean primaryAuthentication, boolean synchronizeUserGroups) {
            final com.communote.server.model.config.LdapConfiguration entity = new com.communote.server.model.config.LdapConfigurationImpl();
            entity.setUrl(url);
            entity.setManagerPassword(managerPassword);
            entity.setManagerDN(managerDN);
            entity.setUserIdentifierIsBinary(userIdentifierIsBinary);
            entity.setDynamicMode(dynamicMode);
            entity.setUserSearch(userSearch);
            entity.setAllowExternalAuthentication(allowExternalAuthentication);
            entity.setSystemId(systemId);
            entity.setPrimaryAuthentication(primaryAuthentication);
            entity.setSynchronizeUserGroups(synchronizeUserGroups);
            return entity;
        }

        /**
         * Constructs a new instance of {@link com.communote.server.model.config.LdapConfiguration},
         * taking all possible properties (except the identifier(s))as arguments.
         */
        public static com.communote.server.model.config.LdapConfiguration newInstance(String url,
                String managerPassword, String managerDN, boolean userIdentifierIsBinary,
                String saslMode, String serverDomain, String queryPrefix, boolean dynamicMode,
                boolean allowExternalAuthentication, String systemId,
                boolean primaryAuthentication, boolean synchronizeUserGroups,
                com.communote.server.model.config.LdapGroupSyncConfiguration groupSyncConfig,
                com.communote.server.model.config.LdapSearchConfiguration userSearch) {
            final com.communote.server.model.config.LdapConfiguration entity = new com.communote.server.model.config.LdapConfigurationImpl();
            entity.setUrl(url);
            entity.setManagerPassword(managerPassword);
            entity.setManagerDN(managerDN);
            entity.setUserIdentifierIsBinary(userIdentifierIsBinary);
            entity.setSaslMode(saslMode);
            entity.setServerDomain(serverDomain);
            entity.setQueryPrefix(queryPrefix);
            entity.setDynamicMode(dynamicMode);
            entity.setAllowExternalAuthentication(allowExternalAuthentication);
            entity.setSystemId(systemId);
            entity.setPrimaryAuthentication(primaryAuthentication);
            entity.setSynchronizeUserGroups(synchronizeUserGroups);
            entity.setGroupSyncConfig(groupSyncConfig);
            entity.setUserSearch(userSearch);
            return entity;
        }
    }

    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = -7760989505251011599L;

    private String url;

    private String managerPassword;

    private String managerDN;

    private boolean userIdentifierIsBinary;

    private String saslMode;

    private String serverDomain;

    private String queryPrefix;

    private boolean dynamicMode;

    private com.communote.server.model.config.LdapGroupSyncConfiguration groupSyncConfig;

    private com.communote.server.model.config.LdapSearchConfiguration userSearch;

    /**
     * Builds a string showing the current attribute values
     */
    @Override
    public String attributesToString() {
        StringBuilder sb = new StringBuilder();

        sb.append("class='");
        sb.append(this.getClass().getName());
        sb.append("', ");

        sb.append("url='");
        sb.append(url);
        sb.append("', ");

        sb.append("managerPassword='");
        sb.append(managerPassword);
        sb.append("', ");

        sb.append("managerDN='");
        sb.append(managerDN);
        sb.append("', ");

        sb.append("userIdentifierIsBinary='");
        sb.append(userIdentifierIsBinary);
        sb.append("', ");

        sb.append("saslMode='");
        sb.append(saslMode);
        sb.append("', ");

        sb.append("serverDomain='");
        sb.append(serverDomain);
        sb.append("', ");

        sb.append("queryPrefix='");
        sb.append(queryPrefix);
        sb.append("', ");

        sb.append("dynamicMode='");
        sb.append(dynamicMode);
        sb.append("', ");

        sb.append(super.attributesToString());

        return sb.toString();
    }

    /**
     * <p>
     * creates a detached deep copy of the entity
     * </p>
     */
    public abstract com.communote.server.model.config.LdapConfiguration deepCopy();

    /**
     * This entity does not have any identifiers but since it extends the
     * <code>com.communote.server.persistence.config.ExternalSystemConfigurationImpl</code> class it
     * will simply delegate the call up there.
     *
     * @see com.communote.server.model.config.ExternalSystemConfiguration#equals(Object)
     */
    @Override
    public boolean equals(Object object) {
        return super.equals(object);
    }

    /**
     *
     */
    public com.communote.server.model.config.LdapGroupSyncConfiguration getGroupSyncConfig() {
        return this.groupSyncConfig;
    }

    /**
     * <p>
     * DN of the LDAP Account who has privileges to loop through the user list
     * </p>
     */
    public String getManagerDN() {
        return this.managerDN;
    }

    /**
     * <p>
     * Password of the ldap account who has privileges to loop through the user list
     * </p>
     */
    public String getManagerPassword() {
        return this.managerPassword;
    }

    /**
     *
     */
    public String getQueryPrefix() {
        return this.queryPrefix;
    }

    /**
     * <p>
     * The SASL authentication mode to be used. If null the authentication will be in simple mode.
     * </p>
     */
    public String getSaslMode() {
        return this.saslMode;
    }

    /**
     *
     */
    public String getServerDomain() {
        return this.serverDomain;
    }

    /**
     * <p>
     * Url of the LDAP Server
     * </p>
     */
    public String getUrl() {
        return this.url;
    }

    /**
     *
     */
    public com.communote.server.model.config.LdapSearchConfiguration getUserSearch() {
        return this.userSearch;
    }

    /**
     * This entity does not have any identifiers but since it extends the
     * <code>com.communote.server.persistence.config.ExternalSystemConfigurationImpl</code> class it
     * will simply delegate the call up there.
     *
     * @see com.communote.server.model.config.ExternalSystemConfiguration#hashCode()
     */
    @Override
    public int hashCode() {
        return super.hashCode();
    }

    /**
     *
     */
    public boolean isDynamicMode() {
        return this.dynamicMode;
    }

    /**
     * <p>
     * true if the value of the LDAP attribute mapped in propertyMapping to the unique user
     * identifier is to be interpreted as a binary value
     * </p>
     */
    public boolean isUserIdentifierIsBinary() {
        return this.userIdentifierIsBinary;
    }

    public void setDynamicMode(boolean dynamicMode) {
        this.dynamicMode = dynamicMode;
    }

    public void setGroupSyncConfig(
            com.communote.server.model.config.LdapGroupSyncConfiguration groupSyncConfig) {
        this.groupSyncConfig = groupSyncConfig;
    }

    public void setManagerDN(String managerDN) {
        this.managerDN = managerDN;
    }

    public void setManagerPassword(String managerPassword) {
        this.managerPassword = managerPassword;
    }

    public void setQueryPrefix(String queryPrefix) {
        this.queryPrefix = queryPrefix;
    }

    public void setSaslMode(String saslMode) {
        this.saslMode = saslMode;
    }

    public void setServerDomain(String serverDomain) {
        this.serverDomain = serverDomain;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setUserIdentifierIsBinary(boolean userIdentifierIsBinary) {
        this.userIdentifierIsBinary = userIdentifierIsBinary;
    }

    public void setUserSearch(com.communote.server.model.config.LdapSearchConfiguration userSearch) {
        this.userSearch = userSearch;
    }
}