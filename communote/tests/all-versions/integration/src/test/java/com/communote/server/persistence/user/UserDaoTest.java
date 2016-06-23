package com.communote.server.persistence.user;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.config.type.ClientProperty;
import com.communote.server.core.common.caching.CacheManager;
import com.communote.server.core.mail.MailMessageHelper;
import com.communote.server.core.security.AuthenticationHelper;
import com.communote.server.core.user.UserManagement;
import com.communote.server.model.user.ExternalUserAuthentication;
import com.communote.server.model.user.User;
import com.communote.server.model.user.UserRole;
import com.communote.server.model.user.UserStatus;
import com.communote.server.test.CommunoteIntegrationTest;
import com.communote.server.test.util.TestUtils;

/**
 * Test for {@link UserDao}
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class UserDaoTest extends CommunoteIntegrationTest {

    @Autowired
    private UserDao userDao;
    @Autowired
    private ExternalUserAuthenticationDao authenticationDao;
    @Autowired
    private UserManagement userManagement;
    @Autowired
    private CacheManager cacheManager;

    /**
     * Test that users with anonymous email address suffix are not in the result of
     * findNotLoggedInActiveUser (Regression test for KENMEI-6853: system users get reminder emails)
     */
    // TODO this is strange, see UserDaoImpl#handleFindNotLoggedInActiveUser why
    @Test
    public void testExcludeAnonymousEmailAddressFromNotLoggedInUsers() {
        // Set all users to already received the reminder and already logged in some day.
        for (User user : userDao.loadAll()) {
            user.setReminderMailSent(true);
            user.setLastLogin(new Timestamp(System.currentTimeMillis()));
            userDao.update(user);
        }

        Assert.assertEquals(userDao.findNotLoggedInActiveUser(new Date(), false, false).size(), 0);

        User user = TestUtils.createRandomUser(false);
        user.setReminderMailSent(false);
        user.setLastLogin(null);
        user.setEmail(UUID.randomUUID() + MailMessageHelper.ANONYMOUS_EMAIL_ADDRESS_SUFFIX);
        user.setStatusChanged(new Timestamp(System.currentTimeMillis() - 1000000));
        userDao.update(user);

        // The original error should fail here, as the anonymous user was returned as well.
        Assert.assertEquals(userDao.findNotLoggedInActiveUser(new Date(), false, false).size(), 0);
    }

    /**
     * Test that system users are excluded when searching for the unconfirmed users.
     *
     *
     * @throws Exception
     *             in case the test failed
     */
    @Test
    public void testExcludeSystemUsersFromUnconfirmedUsers() throws Exception {
        // Set all users to already received the reminder.
        for (User user : userDao.loadAll()) {
            user.setReminderMailSent(true);
            userDao.update(user);
        }
        Assert.assertEquals(userDao.findNotConfirmedUser(new Date(), false).size(), 0);

        User user = TestUtils.createRandomUser(false);
        user.setReminderMailSent(false);
        user.setStatus(UserStatus.INVITED);
        user.setStatusChanged(new Timestamp(System.currentTimeMillis() - 1000000));
        userDao.update(user);
        User systemUser = TestUtils.createRandomUser(false, null, UserRole.ROLE_SYSTEM_USER);
        systemUser.setStatus(UserStatus.REGISTERED);
        systemUser.setReminderMailSent(false);
        systemUser.setStatusChanged(new Timestamp(System.currentTimeMillis() - 1000000));
        userDao.update(systemUser);

        List<User> unconfirmedUsers = userDao.findNotConfirmedUser(new Date(), false);
        Assert.assertEquals(unconfirmedUsers.size(), 1);
        Assert.assertEquals(unconfirmedUsers.get(0).getId(), user.getId());
    }

    /**
     * Test for {@link UserDao#findLatestBySystemId(String, Long, int)}.
     */
    @Test
    public void testFindLatestBySystemId() {
        String externalSystemId = random();
        int numberOfExternalUsers = new Random().nextInt(20) + 20;
        List<Long> externalIds = new ArrayList<>();
        for (int i = 0; i < numberOfExternalUsers; i++) {
            externalIds.add(TestUtils.createRandomUser(false, externalSystemId).getId());
        }
        long lastUserId = -1;
        int countedNumberOfExternalUsers = 0;
        List<User> externalUsers;
        while (!(externalUsers = userDao.findLatestBySystemId(externalSystemId, lastUserId, 5))
                .isEmpty()) {
            countedNumberOfExternalUsers += externalUsers.size();
            for (User externalUser : externalUsers) {
                Assert.assertTrue(externalIds.contains(externalUser.getId()));
                lastUserId = externalUser.getId();
            }
        }
        Assert.assertEquals(countedNumberOfExternalUsers, numberOfExternalUsers);
    }

    /**
     * regression test for KENMEI-5251: PSQLException if user name contains \0
     */
    @Test
    public void testForKENMEI5251() {
        userDao.findByAlias("\0"); // The original error throws an exception here
    }

    /**
     * Test for {@link UserDao#getActiveUserCount(String, UserRole)}
     *
     * @throws Exception
     *             in case the test failed
     */
    // TODO ROLE_CRAWL_USER
    @Test
    public void testGetActiveUserCount() throws Exception {
        TestUtils.createRandomUser(false);
        AuthenticationHelper.setInternalSystemToSecurityContext();
        long initialManagerCount = userDao.getActiveUserCount(null,
                UserRole.ROLE_KENMEI_CLIENT_MANAGER);
        Assert.assertTrue(initialManagerCount > 0);
        long initialUserCount = userDao.getActiveUserCount(null, UserRole.ROLE_KENMEI_USER);
        long initialSystemUserCount = userDao.getActiveUserCount(null, UserRole.ROLE_SYSTEM_USER);
        long initialUserExcludingSystemUserCount = userDao.getActiveUserCount();
        // cannot make more assumptions on initialUserExcludingSystemUserCount because
        // getActiveUserCount() includes external users and excludes all system users
        // (getActiveUserCount(null, UserRole.ROLE_SYSTEM_USER) returns only system user that can
        // log in)
        Assert.assertTrue(initialUserExcludingSystemUserCount >= initialUserCount);
        Assert.assertEquals(userDao.getActiveUserCount(null, null), initialUserCount
                + initialSystemUserCount);

        String externalSystemId = UUID.randomUUID().toString();
        Assert.assertEquals(
                userDao.getActiveUserCount(externalSystemId, UserRole.ROLE_KENMEI_USER), 0);
        Assert.assertEquals(
                userDao.getActiveUserCount(externalSystemId, UserRole.ROLE_KENMEI_CLIENT_MANAGER),
                0);

        // #users++
        TestUtils.createRandomUser(false, externalSystemId);

        Assert.assertEquals(
                userDao.getActiveUserCount(externalSystemId, UserRole.ROLE_KENMEI_USER), 1);
        Assert.assertEquals(
                userDao.getActiveUserCount(externalSystemId, UserRole.ROLE_KENMEI_CLIENT_MANAGER),
                0);

        // #users++; #managers++
        TestUtils.createRandomUser(true, externalSystemId);

        Assert.assertEquals(
                userDao.getActiveUserCount(externalSystemId, UserRole.ROLE_KENMEI_USER), 2);
        Assert.assertEquals(
                userDao.getActiveUserCount(externalSystemId, UserRole.ROLE_KENMEI_CLIENT_MANAGER),
                1);
        Assert.assertEquals(userDao.getActiveUserCount(null, UserRole.ROLE_KENMEI_CLIENT_MANAGER),
                initialManagerCount + 1);
        Assert.assertEquals(userDao.getActiveUserCount(null, UserRole.ROLE_KENMEI_USER),
                initialUserCount + 2);

        // System user ++

        TestUtils.createRandomUser(false, null, UserRole.ROLE_SYSTEM_USER);

        Assert.assertEquals(userDao.getActiveUserCount(), initialUserExcludingSystemUserCount + 2);
        Assert.assertEquals(
                userDao.getActiveUserCount(externalSystemId, UserRole.ROLE_KENMEI_USER), 2);
        Assert.assertEquals(
                userDao.getActiveUserCount(externalSystemId, UserRole.ROLE_KENMEI_CLIENT_MANAGER),
                1);
        Assert.assertEquals(userDao.getActiveUserCount(null, UserRole.ROLE_KENMEI_CLIENT_MANAGER),
                initialManagerCount + 1);
        Assert.assertEquals(userDao.getActiveUserCount(null, UserRole.ROLE_KENMEI_USER),
                initialUserCount + 2);
        Assert.assertEquals(userDao.getActiveUserCount(null, UserRole.ROLE_SYSTEM_USER),
                initialSystemUserCount + 1);
    }

    /**
     * Test whether search for external users respects the configurable compare mode correctly
     */
    @Test
    public void testSearchExternalUserCaseSensitivity() {
        String externalId = UUID.randomUUID().toString().toLowerCase();
        User user = TestUtils.createRandomUser(false);
        ExternalUserAuthentication authentication = ExternalUserAuthentication.Factory.newInstance(
                externalId, externalId);
        authentication = authenticationDao.create(authentication);
        user = userDao.load(user.getId());
        user.setExternalAuthentications(new HashSet<ExternalUserAuthentication>());
        user.getExternalAuthentications().add(authentication);
        userDao.update(user);

        // Disable lower case comparison.
        CommunoteRuntime
        .getInstance()
        .getConfigurationManager()
        .updateClientConfigurationProperty(
                ClientProperty.COMPARE_EXTERNAL_USER_IDS_LOWERCASE,
                Boolean.FALSE.toString());

        // Find, upper case must fail
        Assert.assertNotNull(userDao.findByExternalUserId(externalId, externalId));
        Assert.assertNull(userDao.findByExternalUserId(externalId.toUpperCase(), externalId));

        // Enable lower case comparison.
        CommunoteRuntime
        .getInstance()
        .getConfigurationManager()
        .updateClientConfigurationProperty(
                ClientProperty.COMPARE_EXTERNAL_USER_IDS_LOWERCASE, Boolean.TRUE.toString());

        // Find, upper case may not fail
        Assert.assertNotNull(userDao.findByExternalUserId(externalId, externalId));
        Assert.assertNotNull(userDao.findByExternalUserId(externalId.toUpperCase(), externalId));
    }
}
