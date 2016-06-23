package com.communote.server.core.security;

import org.apache.commons.lang.StringUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.core.security.AuthenticationHelper;
import com.communote.server.model.config.ConfluenceConfiguration;
import com.communote.server.test.CommunoteIntegrationTest;
import com.communote.server.test.util.AuthenticationTestUtils;
import com.communote.server.test.util.TestUtils;

/**
 * Client management tests for Confluence.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class ClientManagementConfluenceTest extends CommunoteIntegrationTest {
    /** */
    private static final String BASE_PATH_A = "http://localhost/confluence/";

    /** */
    private static final String BASE_PATH_B = "http://localhost/cf/";

    /** */
    private final static String CONFLUENCE_URL_COMMUNOTE_AUTH = "plugins/servlet/communote-auth";

    /** */
    private final static String CONFLUENCE_URL_IMAGES = "plugins/servlet/communote-user-picture?alias=";

    /** */
    private final static String CONFLUENCE_URL_SERVICE = "rpc/soap-axis/confluenceservice-v1?wsdl";

    /** */
    private final static String CONFLUENCE_URL_PERMISSION_SERVICE = "rpc/soap-axis/permission-service?wsdl";

    /**
     * Checks the validity of the configuration.
     */
    private void checkConfluenceConfiguration() {
        ConfluenceConfiguration config = loadConfluenceConfiguration();

        Assert.assertNotNull(config.getSystemId());

        String baseUrl = config.getBasePath();

        String authenticationApiUrl = baseUrl + CONFLUENCE_URL_COMMUNOTE_AUTH;
        String imageApiUrl = baseUrl + CONFLUENCE_URL_IMAGES;
        String permissionsUrl = baseUrl + CONFLUENCE_URL_PERMISSION_SERVICE;
        String serviceUrl = baseUrl + CONFLUENCE_URL_SERVICE;

        Assert.assertEquals(authenticationApiUrl, config.getAuthenticationApiUrl());
        Assert.assertEquals(imageApiUrl, config.getImageApiUrl());
        Assert.assertEquals(permissionsUrl, config.getPermissionsUrl());
        Assert.assertEquals(serviceUrl, config.getServiceUrl());

        if (config.isSynchronizeUserGroups()) {
            Assert.assertTrue(StringUtils.isNotBlank(config.getAdminLogin()),
                    "The administrator login can not be empty if the group synchronization is enabled.");
            Assert.assertTrue(StringUtils.isNotBlank(config.getAdminPassword()),
                    "The administrator password can not be empty if the group synchronization is enabled.");
        }
    }

    /**
     * Creates the Confluence configuration.
     *
     * @return the authentication configuration
     * @throws Exception
     *             Exception.
     */
    private ConfluenceConfiguration createConfluenceConfiguration() throws Exception {
        ConfluenceConfiguration config = ConfluenceConfiguration.Factory.newInstance();

        setConfluenceConfigurationUrls(config, BASE_PATH_A);

        config.setSystemId("DefaultConfluence");
        config.setAllowExternalAuthentication(true);
        config.setPrimaryAuthentication(true);
        config.setSynchronizeUserGroups(true);

        config.setAdminLogin("adminLogin");
        config.setAdminPassword("adminPassword");

        if (!config.isSynchronizeUserGroups()) {
            config.setAdminLogin(StringUtils.EMPTY);
            config.setAdminPassword(StringUtils.EMPTY);
        }

        return config;
    }

    /**
     * loads the Confluence configuration from database
     *
     * @return
     */
    private ConfluenceConfiguration loadConfluenceConfiguration() {
        return CommunoteRuntime.getInstance().getConfigurationManager()
                .getClientConfigurationProperties().getConfluenceConfiguration();
    }

    /**
     * Sets the configuration URLs based on the baseUrl.
     *
     * @param config
     *            the confluence configuration
     * @param baseUrl
     *            the used baseUrl
     */
    private void setConfluenceConfigurationUrls(ConfluenceConfiguration config, String baseUrl) {
        config.setBasePath(baseUrl);

        config.setAuthenticationApiUrl(baseUrl + CONFLUENCE_URL_COMMUNOTE_AUTH);
        config.setImageApiUrl(baseUrl + CONFLUENCE_URL_IMAGES);
        config.setPermissionsUrl(baseUrl + CONFLUENCE_URL_PERMISSION_SERVICE);
        config.setServiceUrl(baseUrl + CONFLUENCE_URL_SERVICE);
    }

    /**
     * Test create Confluence authenticate configuration.
     *
     * @throws Exception
     *             in case of an error
     */
    @Test(groups = { "confluence-client-management-test" })
    public void testCreateConfluenceAuthenticationConfiguration() throws Exception {
        AuthenticationTestUtils.setSecurityContext(TestUtils.createRandomUser(true));
        ConfluenceConfiguration config = createConfluenceConfiguration();
        CommunoteRuntime.getInstance().getConfigurationManager().updateConfluenceConfig(config);
        AuthenticationHelper.removeAuthentication();

        checkConfluenceConfiguration();
    }

    /**
     * Test updating an existing Confluence authentication configuration.
     *
     * @throws Exception
     *             in case of an error
     */
    @Test(groups = { "confluence-client-management-test" }, dependsOnMethods = { "testCreateConfluenceAuthenticationConfiguration" })
    public void testUpdateConfluenceAuthenticationConfiguration() throws Exception {
        AuthenticationTestUtils.setSecurityContext(TestUtils.createRandomUser(true));
        ConfluenceConfiguration config = loadConfluenceConfiguration();
        setConfluenceConfigurationUrls(config, BASE_PATH_B);
        CommunoteRuntime.getInstance().getConfigurationManager().updateConfluenceConfig(config);
        AuthenticationHelper.removeAuthentication();

        checkConfluenceConfiguration();
    }
}