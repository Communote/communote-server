package com.communote.server.model.config;

/**
 * @see com.communote.server.model.config.ConfluenceConfiguration
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ConfluenceConfigurationImpl extends
        com.communote.server.model.config.ConfluenceConfiguration {
    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = -4775499995511966418L;

    /**
     * {@inheritDoc}
     */
    @Override
    public ConfluenceConfiguration deepCopy() {
        ConfluenceConfiguration cloneConfig = ConfluenceConfiguration.Factory.newInstance(
                this.getAuthenticationApiUrl(), this.getImageApiUrl(), this.getAdminLogin(),
                this.getAdminPassword(), this.getServiceUrl(), this.getPermissionsUrl(),
                this.getBasePath(), this.isAllowExternalAuthentication(), this.getSystemId(),
                this.isPrimaryAuthentication(), this.isSynchronizeUserGroups());
        return cloneConfig;
    }

    @Override
    public String getConfigurationUrl() {
        return "/admin/client/confluenceAuthentication";
    }

}