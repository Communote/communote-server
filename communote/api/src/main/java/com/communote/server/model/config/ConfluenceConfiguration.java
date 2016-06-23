package com.communote.server.model.config;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class ConfluenceConfiguration extends
        com.communote.server.model.config.ExternalSystemConfiguration {
    /**
     * Constructs new instances of {@link com.communote.server.model.config.ConfluenceConfiguration}
     * .
     */
    public static final class Factory {
        /**
         * Constructs a new instance of
         * {@link com.communote.server.model.config.ConfluenceConfiguration}.
         */
        public static com.communote.server.model.config.ConfluenceConfiguration newInstance() {
            return new com.communote.server.model.config.ConfluenceConfigurationImpl();
        }

        /**
         * Constructs a new instance of
         * {@link com.communote.server.model.config.ConfluenceConfiguration}, taking all required
         * and/or read-only properties as arguments.
         */
        public static com.communote.server.model.config.ConfluenceConfiguration newInstance(
                String authenticationApiUrl, boolean allowExternalAuthentication, String systemId,
                boolean primaryAuthentication, boolean synchronizeUserGroups) {
            final com.communote.server.model.config.ConfluenceConfiguration entity = new com.communote.server.model.config.ConfluenceConfigurationImpl();
            entity.setAuthenticationApiUrl(authenticationApiUrl);
            entity.setAllowExternalAuthentication(allowExternalAuthentication);
            entity.setSystemId(systemId);
            entity.setPrimaryAuthentication(primaryAuthentication);
            entity.setSynchronizeUserGroups(synchronizeUserGroups);
            return entity;
        }

        /**
         * Constructs a new instance of
         * {@link com.communote.server.model.config.ConfluenceConfiguration}, taking all possible
         * properties (except the identifier(s))as arguments.
         */
        public static com.communote.server.model.config.ConfluenceConfiguration newInstance(
                String authenticationApiUrl, String imageApiUrl, String adminLogin,
                String adminPassword, String serviceUrl, String permissionsUrl, String basePath,
                boolean allowExternalAuthentication, String systemId,
                boolean primaryAuthentication, boolean synchronizeUserGroups) {
            final com.communote.server.model.config.ConfluenceConfiguration entity = new com.communote.server.model.config.ConfluenceConfigurationImpl();
            entity.setAuthenticationApiUrl(authenticationApiUrl);
            entity.setImageApiUrl(imageApiUrl);
            entity.setAdminLogin(adminLogin);
            entity.setAdminPassword(adminPassword);
            entity.setServiceUrl(serviceUrl);
            entity.setPermissionsUrl(permissionsUrl);
            entity.setBasePath(basePath);
            entity.setAllowExternalAuthentication(allowExternalAuthentication);
            entity.setSystemId(systemId);
            entity.setPrimaryAuthentication(primaryAuthentication);
            entity.setSynchronizeUserGroups(synchronizeUserGroups);
            return entity;
        }
    }

    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = -6025290062384014289L;

    private String authenticationApiUrl;

    private String imageApiUrl;

    private String adminLogin;

    private String adminPassword;

    private String serviceUrl;

    private String permissionsUrl;

    private String basePath;

    /**
     * Builds a string showing the current attribute values
     */
    @Override
    public String attributesToString() {
        StringBuilder sb = new StringBuilder();

        sb.append("class='");
        sb.append(this.getClass().getName());
        sb.append("', ");

        sb.append("authenticationApiUrl='");
        sb.append(authenticationApiUrl);
        sb.append("', ");

        sb.append("imageApiUrl='");
        sb.append(imageApiUrl);
        sb.append("', ");

        sb.append("adminLogin='");
        sb.append(adminLogin);
        sb.append("', ");

        sb.append("adminPassword='");
        sb.append(adminPassword);
        sb.append("', ");

        sb.append("serviceUrl='");
        sb.append(serviceUrl);
        sb.append("', ");

        sb.append("permissionsUrl='");
        sb.append(permissionsUrl);
        sb.append("', ");

        sb.append("basePath='");
        sb.append(basePath);
        sb.append("', ");

        sb.append(super.attributesToString());

        return sb.toString();
    }

    /**
     * <p>
     * creates a detached deep copy of the entity
     * </p>
     */
    public abstract com.communote.server.model.config.ConfluenceConfiguration deepCopy();

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
     * <p>
     * Login of the admin account
     * </p>
     */
    public String getAdminLogin() {
        return this.adminLogin;
    }

    /**
     * <p>
     * Password of the admin account
     * </p>
     */
    public String getAdminPassword() {
        return this.adminPassword;
    }

    /**
     * <p>
     * The complete url to the confluence auhentication api
     * </p>
     */
    public String getAuthenticationApiUrl() {
        return this.authenticationApiUrl;
    }

    /**
     * <p>
     * Path to Confluence installation.
     * </p>
     */
    public String getBasePath() {
        return this.basePath;
    }

    /**
     * <p>
     * The image api url to the confluence image api
     * </p>
     */
    @Override
    public String getImageApiUrl() {
        return this.imageApiUrl;
    }

    /**
     * <p>
     * Url to the permissions service.
     * </p>
     */
    public String getPermissionsUrl() {
        return this.permissionsUrl;
    }

    /**
     * <p>
     * Url to the SOAP service.
     * </p>
     */
    public String getServiceUrl() {
        return this.serviceUrl;
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

    public void setAdminLogin(String adminLogin) {
        this.adminLogin = adminLogin;
    }

    public void setAdminPassword(String adminPassword) {
        this.adminPassword = adminPassword;
    }

    public void setAuthenticationApiUrl(String authenticationApiUrl) {
        this.authenticationApiUrl = authenticationApiUrl;
    }

    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }

    public void setImageApiUrl(String imageApiUrl) {
        this.imageApiUrl = imageApiUrl;
    }

    public void setPermissionsUrl(String permissionsUrl) {
        this.permissionsUrl = permissionsUrl;
    }

    public void setServiceUrl(String serviceUrl) {
        this.serviceUrl = serviceUrl;
    }
}