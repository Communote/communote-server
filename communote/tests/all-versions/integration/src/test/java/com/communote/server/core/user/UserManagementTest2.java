package com.communote.server.core.user;

import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.blog.NoBlogManagerLeftException;
import com.communote.server.api.core.common.NotFoundException;
import com.communote.server.api.core.config.ConfigurationManager;
import com.communote.server.api.core.config.type.ClientProperty;
import com.communote.server.api.core.property.PropertyManagement;
import com.communote.server.api.core.property.PropertyType;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.core.common.exceptions.InvalidOperationException;
import com.communote.server.core.security.AuthenticationTokenManagement;
import com.communote.server.core.user.EmailAlreadyExistsException;
import com.communote.server.core.user.NoClientManagerLeftException;
import com.communote.server.core.user.UserDeletionDisabledException;
import com.communote.server.core.user.UserGroupMemberManagement;
import com.communote.server.core.user.UserManagement;
import com.communote.server.core.user.group.GroupNotFoundException;
import com.communote.server.model.blog.Blog;
import com.communote.server.model.user.User;
import com.communote.server.model.user.UserRole;
import com.communote.server.model.user.UserStatus;
import com.communote.server.model.user.group.Group;
import com.communote.server.persistence.user.ExternalUserVO;
import com.communote.server.persistence.user.UserPropertyDao;
import com.communote.server.service.UserService;
import com.communote.server.test.CommunoteIntegrationTest;
import com.communote.server.test.external.MockExternalUserGroupAccessor;
import com.communote.server.test.external.MockExternalUserRepository;
import com.communote.server.test.util.AuthenticationTestUtils;
import com.communote.server.test.util.TestUtils;

/**
 * Second test, which are based on {@link CommunoteIntegrationTest}.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class UserManagementTest2 extends CommunoteIntegrationTest {

    @Autowired
    private PropertyManagement propertyManagement;

    @Autowired
    private UserManagement userManagement;

    @Autowired
    private AuthenticationTokenManagement authenticationTokenManagement;

    @Autowired
    private UserPropertyDao userPropertyDao;

    /**
     * Tests, that it is possible to anonymize a user.
     *
     * @throws AuthorizationException
     *             Exception.
     * @throws NoBlogManagerLeftException
     *             Exception.
     * @throws UserDeletionDisabledException
     *             Exception.
     * @throws NoClientManagerLeftException
     *             Exception.
     */
    @Test
    public void testAnonymizeUserWithNoteProperties() throws AuthorizationException,
    NoClientManagerLeftException, UserDeletionDisabledException, NoBlogManagerLeftException {
        User user1 = TestUtils.createRandomUser(true);
        User user2 = TestUtils.createRandomUser(false);
        Blog blog = TestUtils.createRandomBlog(true, true, user1);

        propertyManagement.addObjectPropertyFilter(PropertyType.UserNoteProperty, "test", "test");

        Long noteId1 = TestUtils.createAndStoreCommonNote(blog, user1.getId(), "Beitrag1");
        AuthenticationTestUtils.setSecurityContext(user1);
        try {
            propertyManagement.setObjectProperty(PropertyType.UserNoteProperty, noteId1, "test",
                    "test", "true");
            AuthenticationTestUtils.setSecurityContext(user2);
            propertyManagement.setObjectProperty(PropertyType.UserNoteProperty, noteId1, "test",
                    "test", "true");
            Long noteId2 = TestUtils.createAndStoreCommonNote(blog, user2.getId(), "Beitrag2");
            AuthenticationTestUtils.setSecurityContext(user2);
            propertyManagement.setObjectProperty(PropertyType.UserNoteProperty, noteId2, "test",
                    "test", "true");
            AuthenticationTestUtils.setSecurityContext(user1);
            propertyManagement.setObjectProperty(PropertyType.UserNoteProperty, noteId2, "test",
                    "test", "true");
        } catch (NotFoundException e) {
            Assert.fail("user note property was not found.");
        }

        String aliasUser2 = user2.getAlias();
        userManagement.anonymizeUser(user2.getId(), new Long[0], true);
        User foundUser = userManagement.findUserByAlias(aliasUser2);
        Assert.assertNull(foundUser);
    }

    /**
     * Test assignment and removal of roles.
     *
     * @throws Exception
     *             in case the test failed
     */
    @Test
    public void testAssignRemoveRole() throws Exception {
        User user = TestUtils.createRandomUser(false);
        Authentication auth = AuthenticationTestUtils.setSecurityContext(user);
        try {
            try {
                userManagement.assignUserRole(user.getId(), UserRole.ROLE_KENMEI_CLIENT_MANAGER);
                Assert.fail("Only client manager should be allowed to assign a role");
            } catch (AuthorizationException e) {
                // expected
            }
            AuthenticationTestUtils.setManagerContext();
            try {
                userManagement.assignUserRole(user.getId(), UserRole.ROLE_CRAWL_USER);
                Assert.fail("Should not be possible to assign the crawl role to a normal user");
            } catch (InvalidOperationException e) {
                // expected
            }
            try {
                userManagement.assignUserRole(user.getId(), UserRole.ROLE_SYSTEM_USER);
                Assert.fail("Should not be possible to assign the system user role to a normal user");
            } catch (InvalidOperationException e) {
                // expected
            }
            userManagement.assignUserRole(user.getId(), UserRole.ROLE_KENMEI_CLIENT_MANAGER);
            userManagement.getRolesOfUser(user.getId());
            Assert.assertTrue(ArrayUtils.contains(userManagement.getRolesOfUser(user.getId()),
                    UserRole.ROLE_KENMEI_CLIENT_MANAGER));
            AuthenticationTestUtils.setSecurityContext(TestUtils.createRandomUser(false));
            try {
                userManagement.removeUserRole(user.getId(), UserRole.ROLE_KENMEI_CLIENT_MANAGER);
                Assert.fail("Only client manager should be allowed to remove a role");
            } catch (AuthorizationException e) {
                // expected
            }
            AuthenticationTestUtils.setManagerContext();
            userManagement.removeUserRole(user.getId(), UserRole.ROLE_KENMEI_CLIENT_MANAGER);
            Assert.assertFalse(ArrayUtils.contains(userManagement.getRolesOfUser(user.getId()),
                    UserRole.ROLE_KENMEI_CLIENT_MANAGER));
            try {
                userManagement.removeUserRole(user.getId(), UserRole.ROLE_KENMEI_USER);
                Assert.fail("Should not be possible to remove the user role of a normal user");
            } catch (InvalidOperationException e) {
                // expected
            }
        } finally {
            // restore auth
            AuthenticationTestUtils.setAuthentication(auth);
        }
    }

    /**
     * Tests, that it is possible to retrieve all users by its role.
     *
     * @throws Exception
     *             Exception.
     */
    @Test
    public void testFindUsersByRole() throws Exception {
        int originalManagerCount = userManagement.findUsersByRole(
                UserRole.ROLE_KENMEI_CLIENT_MANAGER, UserStatus.ACTIVE).size();
        int originalUserCount = userManagement.findUsersByRole(UserRole.ROLE_KENMEI_USER,
                UserStatus.ACTIVE).size();
        TestUtils.createRandomUser(false);
        int count2 = userManagement.findUsersByRole(UserRole.ROLE_KENMEI_CLIENT_MANAGER,
                UserStatus.ACTIVE).size();
        Assert.assertEquals(count2, originalManagerCount);
        User manager = TestUtils.createRandomUser(true);
        count2 = userManagement.findUsersByRole(UserRole.ROLE_KENMEI_CLIENT_MANAGER,
                UserStatus.ACTIVE).size();
        Assert.assertEquals(count2, originalManagerCount + 1);
        count2 = userManagement.findUsersByRole(UserRole.ROLE_KENMEI_USER, UserStatus.ACTIVE)
                .size();
        Assert.assertEquals(count2, originalUserCount + 2);

        AuthenticationTestUtils.setSecurityContext(manager);
        userManagement.removeUserRole(manager.getId(), UserRole.ROLE_KENMEI_CLIENT_MANAGER);

        count2 = userManagement.findUsersByRole(UserRole.ROLE_KENMEI_CLIENT_MANAGER,
                UserStatus.ACTIVE).size();
        Assert.assertEquals(count2, originalManagerCount);
        count2 = userManagement.findUsersByRole(UserRole.ROLE_KENMEI_USER, UserStatus.ACTIVE)
                .size();
        Assert.assertEquals(count2, originalUserCount + 2);

        userManagement.assignUserRole(manager.getId(), UserRole.ROLE_KENMEI_CLIENT_MANAGER);
        count2 = userManagement.findUsersByRole(UserRole.ROLE_KENMEI_CLIENT_MANAGER,
                UserStatus.ACTIVE).size();
        Assert.assertEquals(count2, originalManagerCount + 1);
        AuthenticationTestUtils.setSecurityContext(manager);
        userManagement.changeUserStatusByManager(manager.getId(), UserStatus.TEMPORARILY_DISABLED);
        count2 = userManagement.findUsersByRole(UserRole.ROLE_KENMEI_CLIENT_MANAGER,
                UserStatus.ACTIVE).size();
        Assert.assertEquals(count2, originalManagerCount);
    }

    /**
     * Test for {@link UserManagement#getActiveClientManagerCount(String)}
     */
    @Test
    public void testGetActiveClientManagerCount() {
        long managerCount = userManagement.getActiveUserCount(null,
                UserRole.ROLE_KENMEI_CLIENT_MANAGER);
        Assert.assertTrue(managerCount > 0);
        TestUtils.createRandomUser(false);
        Assert.assertEquals(
                userManagement.getActiveUserCount(null, UserRole.ROLE_KENMEI_CLIENT_MANAGER),
                managerCount);
        TestUtils.createRandomUser(true);
        Assert.assertEquals(
                userManagement.getActiveUserCount(null, UserRole.ROLE_KENMEI_CLIENT_MANAGER),
                managerCount + 1);
    }

    /**
     * Test that setting ClientProperty.COMPARE_EXTERNAL_USER_ALIAS_LOWERCASE is respected when
     * merging an external user.
     *
     * @throws Exception
     *             in case the test failed
     */
    @Test
    public void testMergingExternalUser() throws Exception {
        User externalUser = TestUtils.createRandomUser(false, "externalId");
        ExternalUserVO userVO = new ExternalUserVO();
        userVO.setExternalUserName(externalUser.getAlias().toUpperCase());
        userVO.setEmail(externalUser.getEmail());
        userVO.setSystemId("externalId2");
        // Set comparison to case sensitive.
        CommunoteRuntime
        .getInstance()
        .getConfigurationManager()
        .updateClientConfigurationProperty(
                ClientProperty.COMPARE_EXTERNAL_USER_ALIAS_LOWERCASE, "false");
        try {
            userManagement.createOrUpdateExternalUser(userVO);
            Assert.fail("Comparison of usernames should be case sensitive.");
        } catch (EmailAlreadyExistsException e) {
            // This is okay.
        }
        // Set comparison to case insensitive.
        CommunoteRuntime
        .getInstance()
        .getConfigurationManager()
        .updateClientConfigurationProperty(
                ClientProperty.COMPARE_EXTERNAL_USER_ALIAS_LOWERCASE, "true");
        userManagement.createOrUpdateExternalUser(userVO);
    }

    /**
     * Test that the groups of a newly created external user are synchronized automatically after
     * creation
     *
     * @throws Exception
     *             in case the test failed
     */
    @Test
    public void testSyncGroupsOnUserCreation() throws Exception {
        UserService userService = ServiceLocator.findService(UserService.class);
        // create a repo with group synchronization support and enable FLEXIBLE mode so that the
        // group can be found in the repo
        MockExternalUserRepository repo = TestUtils.createNewExternalUserRepo();
        ConfigurationManager propertiesManager = CommunoteRuntime.getInstance()
                .getConfigurationManager();
        ClientProperty.REPOSITORY_MODE repoMode = propertiesManager
                .getClientConfigurationProperties().getRepositoryMode();
        try {
            propertiesManager.updateClientConfigurationProperty(
                    ClientProperty.USER_SERVICE_REPOSITORY_MODE,
                    ClientProperty.REPOSITORY_MODE.FLEXIBLE.name());
            MockExternalUserGroupAccessor groupAccessor = new MockExternalUserGroupAccessor(
                    repo.getExternalSystemId());
            repo.setExternalUserGroupAccessor(groupAccessor);
            ExternalUserVO userVO = TestUtils.createRandomUserVoForExternalSystem(repo
                    .getExternalSystemId());
            repo.addUser(userVO);
            groupAccessor.addUserToGroup(userVO.getAlias(), "group1");
            // create user
            Long userId = ServiceLocator.findService(UserManagement.class)
                    .createOrUpdateExternalUser(userVO).getId();
            // group sync is asynchronous, thus wait and check if user is added to the group
            int count = 0;
            UserGroupMemberManagement memberManagement = ServiceLocator
                    .findService(UserGroupMemberManagement.class);
            while (count < 20) {
                count++;
                Thread.sleep(3000);
                try {
                    Group group = userService.getGroup("group1", repo.getExternalSystemId());
                    if (memberManagement.containsEntityDirectly(group.getId(), userId)) {
                        // user was added to the group -> test succeeded
                        return;
                    }
                } catch (GroupNotFoundException e) {
                    // not yet synchronized -> ignore
                }
            }
            Assert.fail("User " + userId + " was not added to external group group1 of system "
                    + repo.getExternalSystemId());
        } finally {
            userService.unregisterRepository(repo.getExternalSystemId());
            propertiesManager.updateClientConfigurationProperty(
                    ClientProperty.USER_SERVICE_REPOSITORY_MODE, repoMode.name());
        }
    }
}
