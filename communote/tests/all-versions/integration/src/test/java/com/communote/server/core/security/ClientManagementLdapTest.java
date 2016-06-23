package com.communote.server.core.security;

import org.springframework.ldap.AuthenticationException;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.config.ConfigurationManager;
import com.communote.server.core.common.ldap.LdapUserAttribute;
import com.communote.server.core.security.AuthenticationHelper;
import com.communote.server.core.security.authentication.ldap.LdapAuthenticator;
import com.communote.server.core.user.UserManagement;
import com.communote.server.model.config.LdapConfiguration;
import com.communote.server.model.user.User;
import com.communote.server.persistence.user.ExternalUserVO;
import com.communote.server.test.ldap.LdapCommunoteIntegrationTest;
import com.communote.server.test.util.AuthenticationTestUtils;

/**
 * Client management tests for LDAP.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class ClientManagementLdapTest extends LdapCommunoteIntegrationTest {

    private final static String TEST_CLIENT_LDAPUSER_PASSWORD = "123456";
    // user from test_ldap.ldif file
    private final static String TEST_CLIENT_LDAPUSER_USERNAME = "testuser";

    /**
     * Test create ldap authenticate configuration.
     *
     * @throws Exception
     *             in case of an error
     */
    @Test(groups = { "ldap-client-management-test" })
    public void testOnTestClient5CreateLdapAuthenticationConfiguration() throws Exception {
        AuthenticationTestUtils.setManagerContext();
        LdapConfiguration config = createLdapConfiguration();
        CommunoteRuntime.getInstance().getConfigurationManager().updateLdapConfiguration(config);
        AuthenticationHelper.removeAuthentication();
    }

    /**
     * Test create ldap user.
     *
     * @throws Exception
     *             when user creation fails
     */
    @Test(groups = { "ldap-client-management-test" }, dependsOnMethods = { "testOnTestClient5CreateLdapAuthenticationConfiguration" })
    public void testOnTestClient6CreateLdapUser() throws Exception {
        LdapConfiguration ldapConfig = CommunoteRuntime.getInstance().getConfigurationManager()
                .getClientConfigurationProperties().getLdapConfiguration();

        Assert.assertNotNull(ldapConfig, "ldap configuration can not be null");

        LdapAuthenticator authenticator = new LdapAuthenticator(ldapConfig);

        ExternalUserVO userVO = authenticator.authenticate(TEST_CLIENT_LDAPUSER_USERNAME,
                TEST_CLIENT_LDAPUSER_PASSWORD, LdapUserAttribute.ALIAS);

        userVO.setUpdateEmail(true);
        userVO.setUpdateFirstName(true);
        userVO.setUpdateLanguage(true);
        userVO.setUpdateLastName(true);
        userVO.setUpdatePassword(false);

        userVO.setSystemId(ldapConfig.getSystemId());
        AuthenticationHelper.setInternalSystemToSecurityContext();
        ServiceLocator.instance().getService(UserManagement.class)
                .createOrUpdateExternalUser(userVO);

        Object user = ServiceLocator.instance().getService(UserManagement.class)
                .findUserByAlias(TEST_CLIENT_LDAPUSER_USERNAME);
        Assert.assertNotNull(user, "ldap user was not created");
        Assert.assertEquals(((User) user).getAlias(), TEST_CLIENT_LDAPUSER_USERNAME);
    }

    /**
     * Test update the ldap authenticate configuration.
     *
     * @throws Exception
     *             client id is invalid
     */
    @Test(groups = { "ldap-client-management-test" }, dependsOnMethods = { "testOnTestClient6CreateLdapUser" })
    public void testOnTestClient7UpdateLdapAuthenticationConfiguration() throws Exception {
        AuthenticationTestUtils.setManagerContext();
        ConfigurationManager configurationManager = CommunoteRuntime.getInstance()
                .getConfigurationManager();
        LdapConfiguration ldap = configurationManager.getClientConfigurationProperties()
                .getLdapConfiguration();
        String oldManagerDN = ldap.getManagerDN();
        String oldManagerPwd = ldap.getManagerPassword();
        // set some not existing as DN and test whether authentication (binding fails)
        ldap.setManagerDN("cn=not,ou=exisitng,dc=anywhere,dc=localhost");
        ldap.setManagerPassword("123456");
        configurationManager.updateLdapConfiguration(ldap);
        LdapConfiguration ldapConfig = configurationManager.getClientConfigurationProperties()
                .getLdapConfiguration();
        try {
            LdapAuthenticator authenticator = new LdapAuthenticator(ldapConfig);
            authenticator.authenticate(TEST_CLIENT_LDAPUSER_USERNAME,
                    TEST_CLIENT_LDAPUSER_PASSWORD, LdapUserAttribute.ALIAS);
            Assert.fail("Authentication should fail with not existing bind user");
        } catch (AuthenticationException e) {
            // expected
        }
        // reset and test again
        ldap.setManagerDN(oldManagerDN);
        ldap.setManagerPassword(oldManagerPwd);
        configurationManager.updateLdapConfiguration(ldap);
        ldapConfig = configurationManager.getClientConfigurationProperties().getLdapConfiguration();
        LdapAuthenticator authenticator = new LdapAuthenticator(ldapConfig);
        authenticator.authenticate(TEST_CLIENT_LDAPUSER_USERNAME, TEST_CLIENT_LDAPUSER_PASSWORD,
                LdapUserAttribute.ALIAS);
        AuthenticationHelper.removeAuthentication();
    }
}
