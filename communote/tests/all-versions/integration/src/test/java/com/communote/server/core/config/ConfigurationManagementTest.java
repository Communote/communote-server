package com.communote.server.core.config;

import java.util.List;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.config.ClientConfigurationProperties;
import com.communote.server.api.core.user.PrimaryAuthenticationException;
import com.communote.server.core.ConfigurationManagement;
import com.communote.server.core.user.UserManagement;
import com.communote.server.model.config.LdapConfiguration;
import com.communote.server.model.user.User;
import com.communote.server.model.user.UserRole;
import com.communote.server.model.user.UserStatus;
import com.communote.server.test.ldap.LdapCommunoteIntegrationTest;
import com.communote.server.test.util.AuthenticationTestUtils;
import com.communote.server.test.util.TestUtils;

/**
 * Tests for {@link ConfigurationManagement}
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class ConfigurationManagementTest extends LdapCommunoteIntegrationTest {

    private ClientConfigurationProperties clientConfigurationProperties;

    /**
     * Setup.
     */
    @BeforeClass(dependsOnGroups = "integration-test-setup")
    public void setup() {
        clientConfigurationProperties = CommunoteRuntime.getInstance().getConfigurationManager()
                .getClientConfigurationProperties();
    }

    /**
     * Test for {@link ConfigurationManagement#setPrimaryAuthentication(String, boolean, String)}.
     *
     * @throws Exception
     *             Exception.
     */
    @Test
    public void testSetPrimaryAuthentication() throws Exception {
        UserManagement userManagement = ServiceLocator.instance().getService(UserManagement.class);
        User manager = TestUtils.createRandomUser(true);
        AuthenticationTestUtils.setSecurityContext(manager);
        Assert.assertNull(clientConfigurationProperties.getPrimaryExternalAuthentication());
        LdapConfiguration ldapConfiguration = createLdapConfiguration();
        ldapConfiguration.setAllowExternalAuthentication(false);
        CommunoteRuntime.getInstance().getConfigurationManager()
        .updateLdapConfiguration(ldapConfiguration);

        // Test the behavior of switching the external authorization on and off.
        try {
            CommunoteRuntime.getInstance().getConfigurationManager()
            .setPrimaryAuthentication(ldapConfiguration.getSystemId(), true);
            Assert.fail("This should fail, as external authentication is deactivated.");
        } catch (PrimaryAuthenticationException e) {
            Assert.assertEquals(e.getReason(),
                    PrimaryAuthenticationException.Reasons.EXTERNAL_AUTH_NOT_ALLOWED);
        }

        ldapConfiguration.setAllowExternalAuthentication(true);
        CommunoteRuntime.getInstance().getConfigurationManager()
        .updateLdapConfiguration(ldapConfiguration);
        CommunoteRuntime.getInstance().getConfigurationManager()
        .setPrimaryAuthentication(ldapConfiguration.getSystemId(), true);

        // Test the behavior, when there isn't/ is an admin for the external system.
        try {
            CommunoteRuntime.getInstance().getConfigurationManager()
            .setPrimaryAuthentication(ldapConfiguration.getSystemId(), false);
            Assert.fail("It must not be possible to set primary authentication if there is no manager.");
        } catch (PrimaryAuthenticationException e) {
            Assert.assertEquals(e.getReason(),
                    PrimaryAuthenticationException.Reasons.NOT_ENOUGH_ADMINS);
        }

        TestUtils.createRandomUser(false, ldapConfiguration.getSystemId(),
                UserRole.ROLE_KENMEI_USER, UserRole.ROLE_KENMEI_CLIENT_MANAGER);
        CommunoteRuntime.getInstance().getConfigurationManager()
        .setPrimaryAuthentication(ldapConfiguration.getSystemId(), false);

        // Remove all internal administrators and try to disable the external system
        // -> Should work, as all external administrators become internal administrators, when the
        // external system is disabled.
        CommunoteRuntime.getInstance().getConfigurationManager()
        .setPrimaryAuthentication(ldapConfiguration.getSystemId(), true);
        AuthenticationTestUtils.setSecurityContext(manager);
        List<User> managers = userManagement.findUsersByRole(UserRole.ROLE_KENMEI_CLIENT_MANAGER,
                UserStatus.ACTIVE);
        int numberOfManager = managers.size();
        for (User user : managers) {
            if (userManagement.getExternalExternalUserAuthentications(user.getId()).isEmpty()) {
                userManagement.changeUserStatusByManager(user.getId(),
                        UserStatus.TEMPORARILY_DISABLED);
                numberOfManager--;
            }
        }
        Assert.assertEquals(
                userManagement.getActiveUserCount(null, UserRole.ROLE_KENMEI_CLIENT_MANAGER),
                numberOfManager);
        CommunoteRuntime.getInstance().getConfigurationManager()
        .setPrimaryAuthentication(null, false);

        // Reactivate Admins
        for (User user : managers) {
            if (userManagement.getExternalExternalUserAuthentications(user.getId()).isEmpty()) {
                userManagement.changeUserStatusByManager(user.getId(), UserStatus.ACTIVE);
            }
        }
    }
}
