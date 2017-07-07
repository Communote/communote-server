package com.communote.server.core.user.security;

import java.util.HashMap;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.communote.common.converter.IdentityConverter;
import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.config.ApplicationConfigurationProperties;
import com.communote.server.api.core.config.ApplicationConfigurationPropertyConstant;
import com.communote.server.api.core.config.ConfigurationUpdateException;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.core.common.exceptions.PasswordLengthException;
import com.communote.server.core.general.RunInTransaction;
import com.communote.server.core.general.TransactionException;
import com.communote.server.core.general.TransactionManagement;
import com.communote.server.core.user.ExternalUserPasswordChangeNotAllowedException;
import com.communote.server.core.user.UserManagement;
import com.communote.server.model.config.ConfluenceConfiguration;
import com.communote.server.model.user.User;
import com.communote.server.test.CommunoteIntegrationTest;
import com.communote.server.test.util.AuthenticationTestUtils;
import com.communote.server.test.util.TestUtils;

/**
 * Tests for {@link UserPasswordManagement}
 *
 * @author Communote Team - <a href="https://github.com/Communote">https://github.com/Communote</a>
 *
 */
public class UserPasswordManagementTest extends CommunoteIntegrationTest {

    private class TestHashFunction implements PasswordHashFunction {

        @Override
        public boolean canHandle(String passwordHash) {
            return passwordHash.startsWith(getPrefix());
        }

        @Override
        public boolean check(String passwordHash, String password) {
            return passwordHash.substring(getPrefix().length()).equals(password);
        }

        @Override
        public String generate(String password) {
            return getPrefix() + password;
        }

        @Override
        public String getIdentifier() {
            // TODO Auto-generated method stub
            return "TEST";
        }

        String getPrefix() {
            return "$" + getIdentifier() + "$";
        }

        @Override
        public boolean needsUpdate(String passwordHash) {
            return false;
        }

    }

    @Autowired
    private UserPasswordManagement userPasswordManagement;
    @Autowired
    private UserManagement userManagement;
    @Autowired
    private TransactionManagement transactionManagement;

    private User user;
    private User admin;

    private ConfluenceConfiguration createConfluenceConfiguration() throws Exception {
        ConfluenceConfiguration config = ConfluenceConfiguration.Factory.newInstance();

        String baseUrl = "http://localhost";
        config.setBasePath(baseUrl);

        config.setAuthenticationApiUrl(baseUrl + "/auth");
        config.setImageApiUrl(baseUrl + "/images");
        config.setPermissionsUrl(baseUrl + "/permission");
        config.setServiceUrl(baseUrl + "/service");

        // value doesn't matter
        config.setSystemId("MyConfluence");
        config.setAllowExternalAuthentication(true);
        config.setPrimaryAuthentication(false);
        config.setSynchronizeUserGroups(false);

        config.setAdminLogin(StringUtils.EMPTY);
        config.setAdminPassword(StringUtils.EMPTY);

        return config;
    }

    private ApplicationConfigurationProperties getApplicationConfigurationProperties() {
        return CommunoteRuntime.getInstance().getConfigurationManager()
                .getApplicationConfigurationProperties();
    }

    @BeforeClass
    public void setup() {
        user = TestUtils.createRandomUser(false);
        admin = TestUtils.createRandomUser(true);
    }

    @Test
    public void testChangeHashFunction() throws Exception {
        updateAppConfigSetting(ApplicationPropertyUserPassword.LOCAL_USER_PASSWORD_HASH_FUNCTION,
                null);
        User testUser = TestUtils.createRandomUser(false);
        final Long testUserId = testUser.getId();
        BcryptPasswordHashFunction bcrypt = new BcryptPasswordHashFunction();
        // expect bcrypt as default
        Assert.assertNull(getApplicationConfigurationProperties()
                .getProperty(ApplicationPropertyUserPassword.LOCAL_USER_PASSWORD_HASH_FUNCTION));
        Assert.assertTrue(bcrypt.canHandle(testUser.getPassword()));

        Md5PasswordHashFunction md5 = new Md5PasswordHashFunction();

        updateAppConfigSetting(ApplicationPropertyUserPassword.LOCAL_USER_PASSWORD_HASH_FUNCTION,
                md5.getIdentifier());
        AuthenticationTestUtils.setSecurityContext(testUser);
        final String newPassword = "vnr%%ddwer";
        Assert.assertFalse(userPasswordManagement.checkPassword(testUserId, newPassword));
        userPasswordManagement.changePassword(testUserId, newPassword);
        testUser = userManagement.getUserById(testUserId, new IdentityConverter<User>());
        Assert.assertTrue(md5.canHandle(testUser.getPassword()));
        Assert.assertFalse(bcrypt.canHandle(testUser.getPassword()));

        // test checkAndUpdate
        updateAppConfigSetting(ApplicationPropertyUserPassword.LOCAL_USER_PASSWORD_HASH_FUNCTION,
                bcrypt.getIdentifier());
        transactionManagement.execute(new RunInTransaction() {
            @Override
            public void execute() throws TransactionException {
                Assert.assertTrue(userPasswordManagement.checkAndUpdatePassword(
                        userManagement.getUserById(testUserId, new IdentityConverter<User>()),
                        newPassword));
            }
        });
        testUser = userManagement.getUserById(testUserId, new IdentityConverter<User>());
        Assert.assertTrue(bcrypt.canHandle(testUser.getPassword()));
        Assert.assertFalse(md5.canHandle(testUser.getPassword()));
        updateAppConfigSetting(ApplicationPropertyUserPassword.LOCAL_USER_PASSWORD_HASH_FUNCTION,
                null);
    }

    @Test
    public void testChangePassword() throws Exception {
        AuthenticationTestUtils.setSecurityContext(user);
        String newPassword = "vmxcnvopwrF";
        Assert.assertFalse(userPasswordManagement.checkPassword(user.getId(), newPassword));
        userPasswordManagement.changePassword(user.getId(), newPassword);
        Assert.assertTrue(userPasswordManagement.checkPassword(user.getId(), newPassword));

        // weak password
        try {
            userPasswordManagement.changePassword(user.getId(), "12");
            Assert.fail("Setting a weak password should fail");
        } catch (PasswordLengthException e) {
            // expected
        }

        // other user
        User user2 = TestUtils.createRandomUser(false);
        AuthenticationTestUtils.setSecurityContext(user2);
        try {
            userPasswordManagement.changePassword(user.getId(), newPassword + "-2");
            Assert.fail("Setting password for other user should fail");
        } catch (AuthorizationException e) {
            // expected
        }
        Assert.assertFalse(userPasswordManagement.checkPassword(user.getId(), newPassword + "-2"));

        // admin
        AuthenticationTestUtils.setSecurityContext(admin);
        userPasswordManagement.changePassword(user.getId(), newPassword + "-2");
        Assert.assertFalse(userPasswordManagement.checkPassword(user.getId(), newPassword));
        Assert.assertTrue(userPasswordManagement.checkPassword(user.getId(), newPassword + "-2"));

        // external user
        ConfluenceConfiguration config = createConfluenceConfiguration();
        CommunoteRuntime.getInstance().getConfigurationManager().updateConfluenceConfig(config);

        User externalUser = TestUtils.createRandomUser(false, config.getSystemId());
        // not possible when primary auth
        CommunoteRuntime.getInstance().getConfigurationManager()
                .setPrimaryAuthentication(config.getSystemId(), true);
        AuthenticationTestUtils.setSecurityContext(externalUser);
        try {
            userPasswordManagement.changePassword(externalUser.getId(), newPassword + "-2");
            Assert.fail(
                    "Changing password of user from primary external auth should not be possible");
        } catch (ExternalUserPasswordChangeNotAllowedException e) {
            // expected
        }

        // possible if not primary auth
        AuthenticationTestUtils.setSecurityContext(admin);
        CommunoteRuntime.getInstance().getConfigurationManager().setPrimaryAuthentication(null,
                true);
        AuthenticationTestUtils.setSecurityContext(externalUser);
        userPasswordManagement.changePassword(externalUser.getId(), newPassword);
        Assert.assertTrue(userPasswordManagement.checkPassword(externalUser.getId(), newPassword));

        AuthenticationTestUtils.setAuthentication(null);
    }

    @Test
    public void testRegisterHashFunction() throws Exception {
        TestHashFunction testHashFunction = new TestHashFunction();
        userPasswordManagement.register(testHashFunction);
        User testUser = TestUtils.createRandomUser(false);
        Assert.assertFalse(testHashFunction.canHandle(testUser.getPassword()),
                "Hash function needs to be set before becoming active");

        updateAppConfigSetting(ApplicationPropertyUserPassword.LOCAL_USER_PASSWORD_HASH_FUNCTION,
                testHashFunction.getIdentifier());
        AuthenticationTestUtils.setSecurityContext(testUser);
        String newPassword = "abcdefg";
        userPasswordManagement.changePassword(testUser.getId(), newPassword);
        testUser = userManagement.getUserById(testUser.getId(), new IdentityConverter<User>());
        Assert.assertTrue(testHashFunction.canHandle(testUser.getPassword()),
                "Registered hash function is not used for generating hash");
        Assert.assertTrue(userPasswordManagement.checkPassword(testUser.getId(), newPassword),
                "Registered hash function is not used for checking password");

        // remove hash function
        userPasswordManagement.unregister(testHashFunction);
        Assert.assertFalse(userPasswordManagement.checkPassword(testUser.getId(), newPassword),
                "Removed hash function is still used for checking password");

        // default should be used as fallback and checking password should work again
        userPasswordManagement.changePassword(testUser.getId(), newPassword);
        testUser = userManagement.getUserById(testUser.getId(), new IdentityConverter<User>());
        Assert.assertTrue(userPasswordManagement.checkPassword(testUser.getId(), newPassword));

        // cleanup
        updateAppConfigSetting(ApplicationPropertyUserPassword.LOCAL_USER_PASSWORD_HASH_FUNCTION,
                null);
    }

    private void updateAppConfigSetting(ApplicationConfigurationPropertyConstant key, String value)
            throws ConfigurationUpdateException {
        HashMap<ApplicationConfigurationPropertyConstant, String> settings = new HashMap<>();
        settings.put(key, value);
        Authentication auth = AuthenticationTestUtils.setSecurityContext(admin);
        CommunoteRuntime.getInstance().getConfigurationManager()
                .updateApplicationConfigurationProperties(settings);
        AuthenticationTestUtils.setAuthentication(auth);
    }
}
