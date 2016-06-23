package com.communote.server.core.user.group;

import java.util.UUID;

import org.springframework.security.core.context.SecurityContext;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.blog.BlogRightsManagement;
import com.communote.server.core.security.AuthenticationHelper;
import com.communote.server.core.user.UserGroupMemberManagement;
import com.communote.server.core.user.groups.ExternalUserGroupRetriever;
import com.communote.server.core.user.groups.UserGroupSynchronizer;
import com.communote.server.model.blog.BlogRole;
import com.communote.server.model.user.CommunoteEntity;
import com.communote.server.model.user.User;
import com.communote.server.model.user.group.ExternalUserGroup;
import com.communote.server.persistence.user.group.ExternalUserGroupDao;
import com.communote.server.test.CommunoteIntegrationTest;
import com.communote.server.test.external.MockExternalUserGroupAccessor;
import com.communote.server.test.util.AuthenticationTestUtils;
import com.communote.server.test.util.TestUtils;

/**
 * This contains tests for {@link UserGroupSynchronizationJob}.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class UserGroupSynchronizerTest extends CommunoteIntegrationTest {

    private final static String EXTERNAL_SYSTEM_ID = UUID.randomUUID().toString().replace("-", "");

    private User user1;
    private User user2;
    private User user3;

    private MockExternalUserGroupAccessor accessor;

    private UserGroupSynchronizer synchronizer;

    private ExternalUserGroupDao externalUGDao;

    /**
     * Asserts, that non of the objects in the list is null.
     *
     * @param objects
     *            List of objects to check.
     */
    private void assertNotNull(Object... objects) {
        int i = 0;
        for (Object object : objects) {
            Assert.assertNotNull(object, "Object at index " + i + " was null.");
            i++;
        }
    }

    /**
     * @param child
     *            The child group.
     * @param parent
     *            The parent group.
     *
     * @return True, if a member, else false.
     * @throws Exception
     *             Exception.
     */
    private boolean isMember(CommunoteEntity child, ExternalUserGroup parent) throws Exception {
        return ServiceLocator.findService(UserGroupMemberManagement.class).containsEntityDirectly(
                parent.getId(), child.getId());
    }

    /**
     *
     * @throws Exception
     *             Exception.
     */
    @BeforeClass(dependsOnGroups = "integration-test-setup")
    public void setupLast() throws Exception {
        user1 = TestUtils.createRandomUserForExternalSystem(EXTERNAL_SYSTEM_ID);
        user2 = TestUtils.createRandomUserForExternalSystem(EXTERNAL_SYSTEM_ID);
        user3 = TestUtils.createRandomUserForExternalSystem(EXTERNAL_SYSTEM_ID);
        accessor = new MockExternalUserGroupAccessor(EXTERNAL_SYSTEM_ID);
        externalUGDao = ServiceLocator.findService(ExternalUserGroupDao.class);
    }

    /**
     * Setups the worker for each method.
     */
    @BeforeMethod
    public void setupMethod() {
        synchronizer = new UserGroupSynchronizer(EXTERNAL_SYSTEM_ID, accessor);
    }

    /**
     * Synchronize users and groups using the synchronizer and EXTERNAL_SYSTEM_ID.
     */
    private void synchronize() {
        SecurityContext currentContext = AuthenticationHelper.setInternalSystemToSecurityContext();
        synchronizer.synchronize(new ExternalUserGroupRetriever(EXTERNAL_SYSTEM_ID));
        AuthenticationHelper.setSecurityContext(currentContext);
    }

    /**
     * This tests simulates an accessor, which returns the same group twice.
     *
     * @throws Exception
     *             Exception
     */
    @Test
    public void testDuplicateGroups() throws Exception {
        MockExternalUserGroupAccessor accessor = new MockExternalUserGroupAccessor(
                EXTERNAL_SYSTEM_ID);
        UserGroupSynchronizer synchronizer = new UserGroupSynchronizer(EXTERNAL_SYSTEM_ID, accessor);

        User user = TestUtils.createRandomUserForExternalSystem(EXTERNAL_SYSTEM_ID);
        accessor.addUserToGroup(user.getAlias(), "group42");
        accessor.addUserToGroup(user.getAlias(), "Group42");

        accessor.getGroup("Group42").setExternalId("group42");
        accessor.getGroup("Group42").setDescription("group42");
        accessor.getGroup("Group42").setName("group42");
        accessor.getGroup("Group42").setAlias("group42");

        SecurityContext currentContext = AuthenticationHelper.setInternalSystemToSecurityContext();
        synchronizer.synchronize(new ExternalUserGroupRetriever(EXTERNAL_SYSTEM_ID));

        ExternalUserGroup group1 = externalUGDao.findByExternalId("group42", EXTERNAL_SYSTEM_ID);
        ExternalUserGroup group1b = externalUGDao.findByExternalId("Group42", EXTERNAL_SYSTEM_ID);

        Assert.assertNotNull(group1);
        Assert.assertNull(group1b);

        UserGroupMemberManagement userGroupMemberManagement = ServiceLocator
                .findService(UserGroupMemberManagement.class);

        Assert.assertTrue(userGroupMemberManagement.containsUser(group1.getId(), user.getId()));
        accessor.removeUserFromGroup(user.getAlias(), "group42");
        accessor.removeUserFromGroup(user.getAlias(), "Group42");

        synchronizer.synchronize(new ExternalUserGroupRetriever(EXTERNAL_SYSTEM_ID));

        group1 = externalUGDao.findByExternalId("group42", EXTERNAL_SYSTEM_ID);
        Assert.assertNotNull(group1);
        Assert.assertFalse(userGroupMemberManagement.containsUser(group1.getId(), user.getId()));
        AuthenticationHelper.setSecurityContext(currentContext);
    }

    /**
     * Test that it is possible to sync users, even if the users joined a new group between group
     * and user synchronisation (regression test for KENMEI-2846: NullPointerException during user
     * synchronization).
     */
    // TODO test does not test the problem because it modifies the groups after a sync
    @Test
    public void testForKenmei2846() {
        accessor.addUserToGroup(user1.getAlias(), "kenmei2846-1");
        synchronize();
        accessor.addUserToGroup(user1.getAlias(), "kenmei2846-2");
        synchronize();
        // TODO Assert missing
    }

    /**
     * Test topic access after removal of external group from topic (regression test for
     * KENMEI-4931).
     *
     * <p>
     * Scenario used:
     * <ol>
     * <li>Create topic</li>
     * <li>Assign external group A to topic</li>
     * <li>Login with user B from group A for the first time</li>
     * <li>Synchronize groups</li>
     * <li>Remove group A from topic</li>
     * <li>The Bug: The user B still has access to the topic but shouldn't</li>
     * </ol>
     * </p>
     *
     * @throws Exception
     *             in case the test failed
     */
    @Test
    public void testForKenmei4931Scenario1() throws Exception {
        String externalSystemId = "System4931_" + random();
        String externalGroupId = "Group4931_" + random();
        User user = TestUtils.createRandomUser(true);
        User externalUser1 = TestUtils.createRandomUser(false, externalSystemId);
        User externalUser2 = TestUtils.createRandomUser(false, externalSystemId);
        Long topicId = TestUtils.createRandomBlog(false, false, user).getId();
        BlogRightsManagement blogRightsManagement = ServiceLocator.instance().getService(
                BlogRightsManagement.class);
        MockExternalUserGroupAccessor accessor = new MockExternalUserGroupAccessor(externalSystemId);
        accessor.addUserToGroup(externalUser1.getAlias(), externalGroupId);
        accessor.addUserToGroup(externalUser2.getAlias(), externalGroupId);
        UserGroupSynchronizer synchronizer = new UserGroupSynchronizer(externalSystemId, accessor);
        AuthenticationHelper.setInternalSystemToSecurityContext();
        synchronizer.synchronize(new ExternalUserGroupRetriever(externalSystemId));
        ExternalUserGroup externalGroup = externalUGDao.findByExternalId(externalGroupId,
                externalSystemId);
        AuthenticationTestUtils.setSecurityContext(user);
        blogRightsManagement.addEntity(topicId, externalGroup.getId(), BlogRole.MEMBER);
        AuthenticationTestUtils.setSecurityContext(externalUser1);
        Assert.assertTrue(blogRightsManagement.currentUserHasWriteAccess(topicId, false));
        AuthenticationTestUtils.setSecurityContext(externalUser2);
        Assert.assertTrue(blogRightsManagement.currentUserHasWriteAccess(topicId, false));
        User externalUser3 = TestUtils.createRandomUser(false, externalSystemId);
        accessor.addUserToGroup(externalUser3.getAlias(), externalGroupId);
        AuthenticationHelper.setInternalSystemToSecurityContext();
        synchronizer.synchronize(new ExternalUserGroupRetriever(externalSystemId));
        AuthenticationTestUtils.setSecurityContext(externalUser3);
        Assert.assertTrue(blogRightsManagement.currentUserHasWriteAccess(topicId, false));

        AuthenticationTestUtils.setSecurityContext(user);
        blogRightsManagement.removeMemberByEntityId(topicId, externalGroup.getId());
        AuthenticationTestUtils.setSecurityContext(externalUser1);
        Assert.assertFalse(blogRightsManagement.currentUserHasWriteAccess(topicId, false));
        AuthenticationTestUtils.setSecurityContext(externalUser2);
        Assert.assertFalse(blogRightsManagement.currentUserHasWriteAccess(topicId, false));
        AuthenticationTestUtils.setSecurityContext(externalUser3);
        Assert.assertFalse(blogRightsManagement.currentUserHasWriteAccess(topicId, false));
    }

    /**
     * Test topic access after reducing the access privileges of an external group (regression test
     * for KENMEI-4931)
     *
     * <p>
     * Scenario used:
     * <ol>
     * <li>Create topic</li>
     * <li>Add an external group as manager or member to topic</li>
     * <li>add new external user to that external group and start synchronization</li>
     * <li>reduce topic access of group to view</li>
     * <li>Bug: the new user of step 3 still has manage or member access</li>
     * </ol>
     * </p>
     *
     * @throws Exception
     *             Exception.
     */
    @Test
    public void testForKenmei4931Scenario2() throws Exception {
        String externalSystemId = "System4931_2_" + random();
        String externalGroupId = "Group4931_2_" + random();
        User user = TestUtils.createRandomUser(true);
        User externalUser1 = TestUtils.createRandomUser(false, externalSystemId);
        User externalUser2 = TestUtils.createRandomUser(false, externalSystemId);
        Long topicId = TestUtils.createRandomBlog(false, false, user).getId();
        BlogRightsManagement blogRightsManagement = ServiceLocator.instance().getService(
                BlogRightsManagement.class);
        MockExternalUserGroupAccessor accessor = new MockExternalUserGroupAccessor(externalSystemId);
        accessor.addUserToGroup(externalUser1.getAlias(), externalGroupId);
        accessor.addUserToGroup(externalUser2.getAlias(), externalGroupId);
        UserGroupSynchronizer synchronizer = new UserGroupSynchronizer(externalSystemId, accessor);
        AuthenticationHelper.setInternalSystemToSecurityContext();
        synchronizer.synchronize(new ExternalUserGroupRetriever(externalSystemId));
        ExternalUserGroup externalGroup = externalUGDao.findByExternalId(externalGroupId,
                externalSystemId);
        AuthenticationTestUtils.setSecurityContext(user);
        blogRightsManagement.addEntity(topicId, externalGroup.getId(), BlogRole.MEMBER);
        AuthenticationTestUtils.setSecurityContext(externalUser1);
        Assert.assertTrue(blogRightsManagement.currentUserHasWriteAccess(topicId, false));
        AuthenticationTestUtils.setSecurityContext(externalUser2);
        Assert.assertTrue(blogRightsManagement.currentUserHasWriteAccess(topicId, false));
        // add new external user to group and start sync
        User externalUser3 = TestUtils.createRandomUser(false, externalSystemId);
        accessor.addUserToGroup(externalUser3.getAlias(), externalGroupId);
        AuthenticationHelper.setInternalSystemToSecurityContext();
        synchronizer.synchronize(new ExternalUserGroupRetriever(externalSystemId));
        AuthenticationTestUtils.setSecurityContext(externalUser3);
        Assert.assertTrue(blogRightsManagement.currentUserHasWriteAccess(topicId, false));

        AuthenticationTestUtils.setSecurityContext(user);
        blogRightsManagement.changeRoleOfMemberByEntityId(topicId, externalGroup.getId(),
                BlogRole.VIEWER);
        AuthenticationTestUtils.setSecurityContext(externalUser1);
        Assert.assertEquals(blogRightsManagement.getRoleOfCurrentUser(topicId, false),
                BlogRole.VIEWER);
        AuthenticationTestUtils.setSecurityContext(externalUser2);
        Assert.assertEquals(blogRightsManagement.getRoleOfCurrentUser(topicId, false),
                BlogRole.VIEWER);
        AuthenticationTestUtils.setSecurityContext(externalUser3);
        Assert.assertEquals(blogRightsManagement.getRoleOfCurrentUser(topicId, false),
                BlogRole.VIEWER);
    }

    /**
     * Test that parents of groups will be correctly synchronized and non existing groups will be
     * removed.
     *
     * @throws Exception
     *             exception.
     */
    @Test(dependsOnMethods = "testUpdateGroups")
    public void testSynchronizeGroups() throws Exception {
        accessor.addParentGroup("group1", "parent1");
        accessor.addParentGroup("Group1", "parent1");
        accessor.addParentGroup("group1", "parent2");
        accessor.addParentGroup("parent1", "parent3");
        accessor.addParentGroup("group2", "parent3");
        accessor.addParentGroup("group2", "parent4");
        accessor.addParentGroup("parent4", "parent5");
        accessor.addParentGroup("parent5", "parent6");
        accessor.addUserToGroup(user1.getAlias(), "group1");
        accessor.addUserToGroup(user1.getAlias(), "Group1");
        accessor.addUserToGroup(user2.getAlias(), "group2");

        synchronize();

        ExternalUserGroup group1 = externalUGDao.findByExternalId("group1", EXTERNAL_SYSTEM_ID);
        ExternalUserGroup group1b = externalUGDao.findByExternalId("group1", EXTERNAL_SYSTEM_ID);
        ExternalUserGroup group2 = externalUGDao.findByExternalId("group2", EXTERNAL_SYSTEM_ID);
        ExternalUserGroup parent1 = externalUGDao.findByExternalId("parent1", EXTERNAL_SYSTEM_ID);
        ExternalUserGroup parent2 = externalUGDao.findByExternalId("parent2", EXTERNAL_SYSTEM_ID);
        ExternalUserGroup parent3 = externalUGDao.findByExternalId("parent3", EXTERNAL_SYSTEM_ID);
        ExternalUserGroup parent4 = externalUGDao.findByExternalId("parent4", EXTERNAL_SYSTEM_ID);
        ExternalUserGroup parent5 = externalUGDao.findByExternalId("parent5", EXTERNAL_SYSTEM_ID);
        ExternalUserGroup parent6 = externalUGDao.findByExternalId("parent6", EXTERNAL_SYSTEM_ID);

        assertNotNull(group1, group2, parent1, parent2, parent3, parent4, parent5, parent6);
        Assert.assertTrue(isMember(group1, parent1));
        Assert.assertTrue(isMember(group1b, parent1));
        Assert.assertTrue(isMember(group1, parent2));
        Assert.assertTrue(isMember(parent1, parent3));
        Assert.assertTrue(isMember(group2, parent3));
        Assert.assertTrue(isMember(group2, parent4));
        Assert.assertTrue(isMember(group1, parent1));
        Assert.assertTrue(isMember(parent4, parent5));
        Assert.assertTrue(isMember(parent5, parent6));

        UserGroupMemberManagement userGroupMemberManagement = ServiceLocator
                .findService(UserGroupMemberManagement.class);
        Assert.assertTrue(userGroupMemberManagement.containsUser(parent1.getId(), user1.getId()));
        Assert.assertTrue(userGroupMemberManagement.containsUser(parent6.getId(), user2.getId()));

        accessor.removeParentGroup("group1", "parent2");
        accessor.removeParentGroup("parent4", "parent5");
        accessor.removeParentGroup("parent5", "parent6");

        synchronize();

        Assert.assertFalse(isMember(group1, parent2));
        Assert.assertFalse(isMember(parent4, parent5));
        Assert.assertFalse(isMember(parent5, parent6));

        accessor.removeGroup("parent2");

        synchronize();

        parent2 = externalUGDao.findByExternalId("parent2", EXTERNAL_SYSTEM_ID);
        Assert.assertNull(parent2);
    }

    /**
     * Tests the user synchronization.
     *
     * @throws Exception
     *             Exception.
     */
    @Test
    public void testSynchronizeUsers() throws Exception {
        accessor.addUserToGroup(user1.getAlias(), "group1");
        accessor.addUserToGroup(user1.getAlias(), "Group1");
        accessor.addUserToGroup(user1.getAlias(), "group2");
        accessor.addUserToGroup(user1.getAlias(), "group3");
        accessor.addUserToGroup(user2.getAlias(), "group3");
        accessor.addUserToGroup(user2.getAlias(), "group4");
        accessor.addUserToGroup(user3.getAlias(), "group5");

        accessor.addGroup("nonExistingGroup");

        synchronize();

        ExternalUserGroup group1 = externalUGDao.findByExternalId("group1", EXTERNAL_SYSTEM_ID);
        ExternalUserGroup group1b = externalUGDao.findByExternalId("Group1", EXTERNAL_SYSTEM_ID);
        ExternalUserGroup group2 = externalUGDao.findByExternalId("group2", EXTERNAL_SYSTEM_ID);
        ExternalUserGroup group3 = externalUGDao.findByExternalId("group3", EXTERNAL_SYSTEM_ID);
        ExternalUserGroup group4 = externalUGDao.findByExternalId("group4", EXTERNAL_SYSTEM_ID);
        ExternalUserGroup group5 = externalUGDao.findByExternalId("group5", EXTERNAL_SYSTEM_ID);
        ExternalUserGroup nonExistingGroup = externalUGDao.findByExternalId("non-existing-group",
                EXTERNAL_SYSTEM_ID);

        Assert.assertNotNull(group1);
        Assert.assertNotNull(group1b);
        Assert.assertNotNull(group2);
        Assert.assertNotNull(group3);
        Assert.assertNotNull(group4);
        Assert.assertNotNull(group5);
        Assert.assertNull(nonExistingGroup);

        UserGroupMemberManagement userGroupMemberManagement = ServiceLocator
                .findService(UserGroupMemberManagement.class);

        Assert.assertTrue(userGroupMemberManagement.containsUser(group1.getId(), user1.getId()));
        Assert.assertTrue(userGroupMemberManagement.containsUser(group1b.getId(), user1.getId()));
        Assert.assertTrue(userGroupMemberManagement.containsUser(group2.getId(), user1.getId()));
        Assert.assertTrue(userGroupMemberManagement.containsUser(group3.getId(), user1.getId()));
        Assert.assertTrue(userGroupMemberManagement.containsUser(group3.getId(), user2.getId()));
        Assert.assertTrue(userGroupMemberManagement.containsUser(group4.getId(), user2.getId()));
        Assert.assertTrue(userGroupMemberManagement.containsUser(group5.getId(), user3.getId()));

        accessor.addUserToGroup(user2.getAlias(), "group1");
        accessor.removeUserFromGroup(user1.getAlias(), "group1");
        accessor.removeUserFromGroup(user2.getAlias(), "group4");

        synchronize();

        Assert.assertTrue(userGroupMemberManagement.containsUser(group1.getId(), user2.getId()));
        Assert.assertFalse(userGroupMemberManagement.containsUser(group1.getId(), user1.getId()));
        Assert.assertFalse(userGroupMemberManagement.containsUser(group4.getId(), user2.getId()));
    }

    /**
     * This tests, if group information will correctly be updated.
     *
     * @throws Exception
     *             Exception.
     */
    @Test(dependsOnMethods = "testSynchronizeUsers")
    public void testUpdateGroups() throws Exception {
        String newGroup1Name = UUID.randomUUID().toString();
        String newGroup2Name = UUID.randomUUID().toString();
        String newGroup3Description = UUID.randomUUID().toString();
        accessor.getGroup("group1").setName(newGroup1Name);
        accessor.getGroup("group2").setName(newGroup2Name);
        accessor.getGroup("Group1").setAlias("group1");
        accessor.getGroup("group3").setDescription(newGroup3Description);

        synchronize();

        ExternalUserGroup group1 = externalUGDao.findByExternalId("group1", EXTERNAL_SYSTEM_ID);
        ExternalUserGroup group2 = externalUGDao.findByExternalId("group2", EXTERNAL_SYSTEM_ID);
        ExternalUserGroup group3 = externalUGDao.findByExternalId("group3", EXTERNAL_SYSTEM_ID);

        Assert.assertEquals(group1.getName(), newGroup1Name);
        Assert.assertEquals(group2.getName(), newGroup2Name);
        Assert.assertEquals(group3.getDescription(), newGroup3Description);
    }
}
