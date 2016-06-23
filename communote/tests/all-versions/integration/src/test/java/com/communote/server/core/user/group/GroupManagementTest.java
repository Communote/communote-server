/**
 *
 */
package com.communote.server.core.user.group;

import java.util.Collection;
import java.util.UUID;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeGroups;
import org.testng.annotations.Test;

import com.communote.common.converter.IdentityConverter;
import com.communote.server.api.ServiceLocator;
import com.communote.server.core.user.AliasAlreadyExistsException;
import com.communote.server.core.user.UserGroupManagement;
import com.communote.server.core.user.UserGroupMemberManagement;
import com.communote.server.core.user.group.AliasValidationException;
import com.communote.server.core.user.group.CantAddParentAsChildException;
import com.communote.server.core.user.group.GroupOperationNotPermittedException;
import com.communote.server.core.vo.user.group.ExternalGroupVO;
import com.communote.server.core.vo.user.group.GroupVO;
import com.communote.server.model.user.User;
import com.communote.server.model.user.group.Group;
import com.communote.server.persistence.user.group.GroupDao;
import com.communote.server.persistence.user.group.UserOfGroupDao;
import com.communote.server.test.CommunoteIntegrationTest;
import com.communote.server.test.util.AuthenticationTestUtils;
import com.communote.server.test.util.TestUtils;
import com.communote.server.test.util.UserAndGroupTestUtils;

/**
 * Unit tests for group related service methods.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class GroupManagementTest extends CommunoteIntegrationTest {

    private static final String RANDOM = UUID.randomUUID().toString();

    private static final String EXTERNAL_GROUP_ALIAS = "testExtGroup"
            + UUID.randomUUID().toString();
    private static final String EXTERNAL_GROUP_NAME = "Test External Group"
            + UUID.randomUUID().toString();
    private static final String EXTERNAL_GROUP_DESCRIPTION = "Description of external test group 1"
            + UUID.randomUUID().toString();
    private static final String EXTERNAL_SYSTEM_ID = "test-ex-sys1" + UUID.randomUUID().toString();
    private static final String GROUP_1_ALIAS = "testGroup" + UUID.randomUUID().toString();
    private static final String GROUP_ILLEGAL_ALIAS = "test&%\u00A7?Group"
            + UUID.randomUUID().toString();
    private static final String GROUP_1_NAME = "Test Group 1" + UUID.randomUUID().toString();
    private static final String GROUP_1_DESCRIPTION = "Description of test group 1"
            + UUID.randomUUID().toString();
    private static final String GROUP_1_ALIAS_UPDATED = "testGroup1" + UUID.randomUUID().toString();
    private static final String GROUP_1_NAME_UPDATED = "New name of test group 1"
            + UUID.randomUUID().toString();
    private static final String GROUP_1_DESCRIPTION_UPDATED = "changed description of test group 1"
            + UUID.randomUUID().toString();

    private static final String GROUP_2_NAME = "Dollar$-Bill ya Gr\u00FCppchen" + RANDOM;
    private static final String GROUP_2_ALIAS_EXPECTED = "dollar_-bill_ya_gr_ppchen" + RANDOM;
    private static final String GROUP_2_DESCRIPTION = "Description of test group 2"
            + UUID.randomUUID().toString();
    private static final String GROUP_3_ALIAS_EXPECTED = "dollar_-bill_ya_gr_ppchen" + RANDOM + "1";
    private static final String GROUP_3_DESCRIPTION = "Description of test group 3"
            + UUID.randomUUID().toString();
    private User user;

    private User manager;

    /**
     * Asserts that group 2 is directly contained in group 1.
     *
     * @param group1Id
     *            ID of group 1 that has to contain group 2
     * @param group2Id
     *            ID of group 2 that has to be contained in group 1
     * @param group1MembersPattern
     *            user alias pattern of the members of group 1
     * @param group2MembersPattern
     *            user alias pattern of the members of group 2
     * @param group1MemberCount
     *            an array where the 1st value is the number of users that are direct members of
     *            group 1 and the 2nd value is the number of groups that are direct members of group
     *            1
     * @param group2MemberCount
     *            an array where the 1st value is the number of users that are direct members of
     *            group 2 and the 2nd value is the number of groups that are direct members of group
     *            2
     * @throws Exception
     *             if the assertion does not hold
     */
    private void assertGroupContainsGroup(Long group1Id, Long group2Id,
            String group1MembersPattern, String group2MembersPattern, int[] group1MemberCount,
            int[] group2MemberCount) throws Exception {
        boolean groupContained = getGroupMemberManagement().containsEntityDirectly(group1Id,
                group2Id);
        Assert.assertTrue(groupContained, "the group is not contained");
        int memberCount = ServiceLocator.findService(GroupDao.class).countMembers(group1Id);
        Assert.assertEquals(memberCount, group1MemberCount[0] + group1MemberCount[1]);
        memberCount = ServiceLocator.findService(GroupDao.class).countMembers(group2Id);
        Assert.assertEquals(memberCount, group2MemberCount[0] + group2MemberCount[1]);
        for (int i = 1; i <= group2MemberCount[0]; i++) {
            Long userId = UserAndGroupTestUtils.findNthUser(group2MembersPattern, i).getId();
            assertGroupContainsUser(group1Id, userId, false);
            // must still be in the other group
            assertGroupContainsUser(group2Id, userId, true);
        }
        for (int i = 1; i <= group1MemberCount[0]; i++) {
            Long userId = UserAndGroupTestUtils.findNthUser(group1MembersPattern, i).getId();
            // users of group 1 must not be in group 2
            userId = UserAndGroupTestUtils.findNthUser(group1MembersPattern, i).getId();
            assertGroupDoesNotContainUser(group2Id, userId);
            assertGroupContainsUser(group1Id, userId, true);
        }
    }

    /**
     * Asserts that a user is a member of a group.
     *
     * @param groupId
     *            the group to test
     * @param userId
     *            the user to test
     * @param directly
     *            if true the user must be a direct member of the group, if false the user must be
     *            an indirect member of the group
     * @throws Exception
     *             if the test fails
     */
    private void assertGroupContainsUser(Long groupId, Long userId, boolean directly)
            throws Exception {
        UserGroupMemberManagement ugmm = getGroupMemberManagement();
        boolean userDirectlyContained = ugmm.containsUserDirectly(groupId, userId);
        if (directly) {
            Assert.assertTrue(userDirectlyContained, "the user " + userId
                    + " is not directly contained in the group.");
        } else {
            Assert.assertFalse(userDirectlyContained, "the user " + userId
                    + " is directly contained in the group.");
        }
        boolean userContained = ugmm.containsUser(groupId, userId);
        Assert.assertTrue(userContained, "UserOfGroup association for " + userId + " not found");
    }

    /**
     * Asserts that a user is not a direct or indirect member of a group.
     *
     * @param groupId
     *            the group to test
     * @param userId
     *            the user to test
     * @throws Exception
     *             if the test fails
     */
    private void assertGroupDoesNotContainUser(Long groupId, Long userId) throws Exception {
        UserGroupMemberManagement ugmm = getGroupMemberManagement();
        boolean userDirectlyContained = ugmm.containsUserDirectly(groupId, userId);
        Assert.assertFalse(userDirectlyContained, "the user " + userId
                + " is still directly contained in the group.");
        boolean userContained = ugmm.containsUser(groupId, userId);
        Assert.assertFalse(userContained, "UserOfGroup association for " + userId + " still exists");
    }

    /**
     * Asserts that the UserOfGroup associations for a group are correct. Tests that the users are
     * the users of the groups (user1 to groupXMemberCount).
     *
     * @param groupId
     *            the ID of the group whose UsersOfGroup associations are to be checkt
     * @param group1MemberCount
     *            the number of users the group contains from group 1
     * @param group2MemberCount
     *            the number of users the group contains from group 1
     * @param group3MemberCount
     *            the number of users the group contains from group 1
     * @param group4MemberCount
     *            the number of users the group contains from group 1
     */
    private void assertUserOfGroupMatchesGroupMembers(Long groupId, int group1MemberCount,
            int group2MemberCount, int group3MemberCount, int group4MemberCount) {
        Collection<Long> userIds = ServiceLocator.findService(UserOfGroupDao.class)
                .getUsersOfGroup(groupId);
        String group1UserAliasPattern = getHierarchicalGroupUserAliasPattern(1);
        String group2UserAliasPattern = getHierarchicalGroupUserAliasPattern(2);
        String group3UserAliasPattern = getHierarchicalGroupUserAliasPattern(3);
        String group4UserAliasPattern = getHierarchicalGroupUserAliasPattern(4);
        for (int i = 1; i <= 3; i++) {
            Long userId;
            if (i <= group1MemberCount) {
                userId = UserAndGroupTestUtils.findNthUser(group1UserAliasPattern, i).getId();
                userIds.remove(userId);
            }
            if (i <= group2MemberCount) {
                userId = UserAndGroupTestUtils.findNthUser(group2UserAliasPattern, i).getId();
                userIds.remove(userId);
            }
            if (i <= group3MemberCount) {
                userId = UserAndGroupTestUtils.findNthUser(group3UserAliasPattern, i).getId();
                userIds.remove(userId);
            }
            if (i <= group4MemberCount) {
                userId = UserAndGroupTestUtils.findNthUser(group4UserAliasPattern, i).getId();
                userIds.remove(userId);
            }
        }
        Assert.assertEquals(userIds.size(), 0, "UserOfGroupDao does not return the expected users.");
    }

    /**
     * Prepares the hierarchical group tests.
     *
     * @throws Exception
     *             if the setup fails
     */
    @BeforeGroups(groups = { "hierarchical-groups" })
    public void beforeHgroup() throws Exception {
        UserGroupManagement gm = getGroupManagement();
        int[] usersToAdd = { 3, 3, 2, 2 };
        for (int i = 1; i <= 4; i++) {
            String alias = getHierarchicalGroupAlias(i);
            GroupVO vo = new GroupVO(getHierarchicalGroupName(i), alias, null);
            gm.createGroup(vo);
            Group group = UserAndGroupTestUtils.findGroup(alias);
            Assert.assertNotNull(group);
            String userAliasPattern = getHierarchicalGroupUserAliasPattern(i);
            UserAndGroupTestUtils.createDummyUsers(userAliasPattern,
                    getHierarchicalGroupUserEmailPattern(i), 3);
            UserAndGroupTestUtils.addUsersToGroup(group, userAliasPattern, usersToAdd[i - 1], null);
        }
    }

    /**
     * @return the group management
     */
    private UserGroupManagement getGroupManagement() {
        return ServiceLocator.findService(UserGroupManagement.class);
    }

    /**
     * @return the group member management
     */
    private UserGroupMemberManagement getGroupMemberManagement() {
        return ServiceLocator.findService(UserGroupMemberManagement.class);
    }

    /**
     * Returns the group alias for the nth hierarchical group.
     *
     * @param number
     *            the number of the group whose alias is to be returned
     * @return the alias
     */
    private String getHierarchicalGroupAlias(int number) {
        return "hierarchGroup" + RANDOM + number;
    }

    /**
     * Returns the group name for the nth hierarchical group.
     *
     * @param number
     *            the number of the group whose name is to be returned
     * @return the group name
     */
    private String getHierarchicalGroupName(int number) {
        return "name of hierarchGroup" + number;
    }

    /**
     * Returns the user alias pattern for the members of the nth hierarchical group.
     *
     * @param number
     *            the number of the group for which the user alias pattern is to be returned
     * @return the user alias pattern
     */
    private String getHierarchicalGroupUserAliasPattern(int number) {
        return "hgroup" + RANDOM + number + "user%d";
    }

    /**
     * Returns the user email address pattern for the members of the nth hierarchical group.
     *
     * @param number
     *            the number of the group for which the user email pattern is to be returned
     * @return the user email address pattern
     */
    private String getHierarchicalGroupUserEmailPattern(int number) {
        return "hgroup" + RANDOM + number + "user%d@localhost";
    }

    /**
     * Does required preparations to run the tests.
     */
    @BeforeClass(dependsOnGroups = GROUP_INTEGRATION_TEST_SETUP)
    public void prepareTests() {
        user = TestUtils.createRandomUser(true);
        manager = TestUtils.createRandomUser(true);
    }

    /**
     * Tests adding a user to a group.
     *
     * @throws Exception
     *             if the test fails
     */
    @Test(dependsOnMethods = { "testCreateGroup", "testCreateGroupAliasGenerationWhenAliasExists" })
    public void testAddUserToGroup() throws Exception {
        AuthenticationTestUtils.setSecurityContext(manager);

        Group group = UserAndGroupTestUtils.findGroup(GROUP_1_ALIAS);
        UserAndGroupTestUtils.addNthUserToGroup(group, user.getAlias(), -1, null);
        assertGroupContainsUser(group.getId(), user.getId(), true);
        // add to another group
        group = UserAndGroupTestUtils.findGroup(GROUP_2_ALIAS_EXPECTED);
        UserAndGroupTestUtils.addNthUserToGroup(group, user.getAlias(), -1, null);
        assertGroupContainsUser(group.getId(), user.getId(), true);
    }

    /**
     * tests the creation of an external group.
     *
     * @throws Exception
     *             if the test fails
     */
    @Test
    public void testCreateExternalGroup() throws Exception {
        AuthenticationTestUtils.setSecurityContext(manager);

        ExternalGroupVO vo = new ExternalGroupVO("42348", EXTERNAL_SYSTEM_ID, UUID.randomUUID()
                .toString(), EXTERNAL_GROUP_NAME, EXTERNAL_GROUP_ALIAS, EXTERNAL_GROUP_DESCRIPTION);
        UserGroupManagement gm = getGroupManagement();
        Long groupId = gm.createExternalGroup(vo);
        Group group = gm.findGroupById(groupId, new IdentityConverter<Group>());
        Assert.assertNotNull(group, "group creation failed");
        Assert.assertEquals(group.getAlias(), EXTERNAL_GROUP_ALIAS.toLowerCase());
        Assert.assertEquals(group.getName(), EXTERNAL_GROUP_NAME);
        Assert.assertEquals(group.getDescription(), EXTERNAL_GROUP_DESCRIPTION);
    }

    /**
     * tests the creation of a group.
     *
     * @throws Exception
     *             if the test fails
     */
    @Test
    public void testCreateGroup() throws Exception {
        AuthenticationTestUtils.setSecurityContext(manager);

        GroupVO vo = new GroupVO(GROUP_1_NAME, GROUP_1_ALIAS, GROUP_1_DESCRIPTION);
        UserGroupManagement gm = getGroupManagement();
        Group group = gm.createGroup(vo);
        Assert.assertNotNull(group, "group creation failed");
        Assert.assertEquals(group.getAlias(), GROUP_1_ALIAS.toLowerCase());
        Assert.assertEquals(group.getName(), GROUP_1_NAME);
        Assert.assertEquals(group.getDescription(), GROUP_1_DESCRIPTION);
    }

    /**
     * Tests the generation of a group alias from the name of the group.
     *
     * @throws Exception
     *             if the test fails
     */
    @Test
    public void testCreateGroupAliasGeneration() throws Exception {
        AuthenticationTestUtils.setSecurityContext(manager);

        GroupVO vo = new GroupVO(GROUP_2_NAME, null, GROUP_2_DESCRIPTION);
        UserGroupManagement gm = getGroupManagement();
        Group group = gm.createGroup(vo);
        Assert.assertNotNull(group, "group creation failed");
        Assert.assertEquals(group.getAlias(), GROUP_2_ALIAS_EXPECTED);
        Assert.assertEquals(group.getName(), GROUP_2_NAME);
        Assert.assertEquals(group.getDescription(), GROUP_2_DESCRIPTION);
    }

    /**
     * Tests the generation of a group alias when the alias already exists.
     *
     * @throws Exception
     *             if the test fails
     */
    @Test(dependsOnMethods = { "testCreateGroupAliasGeneration" })
    public void testCreateGroupAliasGenerationWhenAliasExists() throws Exception {
        AuthenticationTestUtils.setSecurityContext(manager);

        GroupVO vo = new GroupVO(GROUP_2_NAME, null, GROUP_3_DESCRIPTION);
        UserGroupManagement gm = getGroupManagement();
        Group group = gm.createGroup(vo);
        Assert.assertNotNull(group, "group creation failed");
        Assert.assertEquals(group.getAlias(), GROUP_3_ALIAS_EXPECTED);
        Assert.assertEquals(group.getName(), GROUP_2_NAME);
        Assert.assertEquals(group.getDescription(), GROUP_3_DESCRIPTION);
    }

    /**
     * Tests the creation of a group with an invalid alias.
     *
     * @throws Exception
     *             if the test fails
     */
    @Test(expectedExceptions = { AliasValidationException.class })
    public void testCreateGroupIllegalAlias() throws Exception {
        AuthenticationTestUtils.setSecurityContext(manager);

        GroupVO vo = new GroupVO(GROUP_1_NAME, GROUP_ILLEGAL_ALIAS, GROUP_1_DESCRIPTION);
        UserGroupManagement gm = getGroupManagement();
        gm.createGroup(vo);
    }

    /**
     * Tests the creation of a group with an already existing alias.
     *
     * @throws Exception
     *             if the test fails
     */
    @Test(dependsOnMethods = { "testCreateGroup" }, expectedExceptions = { AliasAlreadyExistsException.class })
    public void testCreateGroupSameAlias() throws Exception {
        AuthenticationTestUtils.setSecurityContext(manager);

        GroupVO vo = new GroupVO(GROUP_1_NAME, GROUP_1_ALIAS, GROUP_1_DESCRIPTION);
        UserGroupManagement gm = getGroupManagement();
        gm.createGroup(vo);
    }

    /**
     * Tests the deletion of a group without members.
     *
     * @throws Exception
     *             if the test fails
     */
    @Test(dependsOnMethods = { "testReAddUserToGroup" }, dependsOnGroups = { "groupUpdate" })
    public void testDeleteGroupWithMember() throws Exception {
        AuthenticationTestUtils.setSecurityContext(manager);

        Long groupId = UserAndGroupTestUtils.findGroup(GROUP_1_ALIAS).getId();
        getGroupManagement().deleteGroup(groupId);
        Assert.assertNull(UserAndGroupTestUtils.findGroup(GROUP_1_ALIAS));
        Assert.assertNotNull(user, "group members must not be deleted");
    }

    /**
     * Tests the deletion of a group without members.
     *
     * @throws Exception
     *             if the test fails
     */
    @Test(dependsOnMethods = { "testCreateGroupAliasGenerationWhenAliasExists" }, dependsOnGroups = { "groupUpdate" })
    public void testDeleteGroupWithoutMember() throws Exception {
        AuthenticationTestUtils.setSecurityContext(manager);

        Long groupId = UserAndGroupTestUtils.findGroup(GROUP_3_ALIAS_EXPECTED).getId();
        getGroupManagement().deleteGroup(groupId);
        Assert.assertNull(UserAndGroupTestUtils.findGroup(GROUP_3_ALIAS_EXPECTED));
    }

    /**
     * Tests adding a group to another group.
     *
     * @throws Exception
     *             if the test fails
     */
    @Test(dependsOnMethods = { "testCreateGroup" }, groups = { "hierarchical-groups" })
    public void testHgroup1AddGroupToGroup() throws Exception {
        AuthenticationTestUtils.setSecurityContext(manager);

        Long group1Id = UserAndGroupTestUtils.findGroup(getHierarchicalGroupAlias(1)).getId();
        Long group2Id = UserAndGroupTestUtils.findGroup(getHierarchicalGroupAlias(2)).getId();
        getGroupMemberManagement().addGroup(group1Id, group2Id);
        assertGroupContainsGroup(group1Id, group2Id, getHierarchicalGroupUserAliasPattern(1),
                getHierarchicalGroupUserAliasPattern(2), new int[] { 3, 1 }, new int[] { 3, 0 });
        assertUserOfGroupMatchesGroupMembers(group1Id, 3, 3, 0, 0);
        // add another group to group1
        Long group3Id = UserAndGroupTestUtils.findGroup(getHierarchicalGroupAlias(3)).getId();
        String hgroup3UserAliasPattern = getHierarchicalGroupUserAliasPattern(3);
        getGroupMemberManagement().addGroup(group1Id, group3Id);
        assertGroupContainsGroup(group1Id, group3Id, getHierarchicalGroupUserAliasPattern(1),
                hgroup3UserAliasPattern, new int[] { 3, 2 }, new int[] { 2, 0 });
        for (int i = 1; i <= 2; i++) {
            Long userId = UserAndGroupTestUtils.findNthUser(hgroup3UserAliasPattern, i).getId();
            assertGroupDoesNotContainUser(group2Id, userId);
        }
        assertUserOfGroupMatchesGroupMembers(group1Id, 3, 3, 2, 0);
        // add another group to group2
        Long group4Id = UserAndGroupTestUtils.findGroup(getHierarchicalGroupAlias(4)).getId();
        String hgroup4UserAliasPattern = getHierarchicalGroupUserAliasPattern(4);
        getGroupMemberManagement().addGroup(group2Id, group4Id);
        assertGroupContainsGroup(group2Id, group4Id, getHierarchicalGroupUserAliasPattern(2),
                hgroup4UserAliasPattern, new int[] { 3, 1 }, new int[] { 2, 0 });
        // check that users of group4 are also in group1
        for (int i = 1; i <= 2; i++) {
            Long userId = UserAndGroupTestUtils.findNthUser(hgroup4UserAliasPattern, i).getId();
            assertGroupContainsUser(group1Id, userId, false);
            assertGroupDoesNotContainUser(group3Id, userId);
        }
        // check that group4 is not directly contained in group1
        boolean groupContained = getGroupMemberManagement().containsEntityDirectly(group1Id,
                group4Id);
        Assert.assertFalse(groupContained, "Group contained in parent of group it was added to.");
        int memberCount = ServiceLocator.findService(GroupDao.class).countMembers(group1Id);
        Assert.assertEquals(memberCount, 5);
        assertUserOfGroupMatchesGroupMembers(group1Id, 3, 3, 2, 2);
    }

    /**
     * Tests that cycles of groups cannot be created.
     *
     * @throws Exception
     *             if the test fails
     */
    @Test(dependsOnMethods = { "testHgroup1AddGroupToGroup" }, groups = { "hierarchical-groups" })
    public void testHgroup2AddGroupOfHierarchy() throws Exception {
        AuthenticationTestUtils.setSecurityContext(manager);

        Group group1 = UserAndGroupTestUtils.findGroup(getHierarchicalGroupAlias(1));
        // test adding to oneself
        boolean exceptionThrown = false;
        try {
            getGroupMemberManagement().addGroup(group1.getId(), group1.getId());
        } catch (CantAddParentAsChildException e) {
            exceptionThrown = true;
        }
        Assert.assertTrue(exceptionThrown, "Adding group to itself was not blocked");
        exceptionThrown = false;
        // test adding a parent
        Group group4 = UserAndGroupTestUtils.findGroup(getHierarchicalGroupAlias(4));
        try {
            getGroupMemberManagement().addGroup(group4.getId(), group1.getId());
        } catch (CantAddParentAsChildException e) {
            exceptionThrown = true;
        }
        Assert.assertTrue(exceptionThrown,
                "Adding group that is a parent of the target group was not blocked");
    }

    /**
     * Tests adding a user to a group in the hierarchy.
     *
     * @throws Exception
     *             if the test fails
     */
    @Test(dependsOnMethods = { "testHgroup2AddGroupOfHierarchy" }, groups = { "hierarchical-groups" })
    public void testHgroup3AddUserToGroup() throws Exception {
        AuthenticationTestUtils.setSecurityContext(manager);

        Group group4 = UserAndGroupTestUtils.findGroup(getHierarchicalGroupAlias(4));
        String hgroup4UserAliasPattern = getHierarchicalGroupUserAliasPattern(4);
        UserAndGroupTestUtils.addNthUserToGroup(group4, hgroup4UserAliasPattern, 3, null);
        // test that group4 and group2 are correct
        Group group2 = UserAndGroupTestUtils.findGroup(getHierarchicalGroupAlias(2));
        assertGroupContainsGroup(group2.getId(), group4.getId(),
                getHierarchicalGroupUserAliasPattern(2), hgroup4UserAliasPattern,
                new int[] { 3, 1 }, new int[] { 3, 0 });
        // new user must also be in group1
        Long group1Id = UserAndGroupTestUtils.findGroup(getHierarchicalGroupAlias(1)).getId();
        Long addedUserId = UserAndGroupTestUtils.findNthUser(hgroup4UserAliasPattern, 3).getId();
        assertGroupContainsUser(group1Id, addedUserId, false);
        // new user must not be in group3
        Group group3 = UserAndGroupTestUtils.findGroup(getHierarchicalGroupAlias(3));
        assertGroupDoesNotContainUser(group3.getId(), addedUserId);
        assertUserOfGroupMatchesGroupMembers(group1Id, 3, 3, 2, 3);
    }

    /**
     * Tests removing a user from a group in the hierarchy.
     *
     * @throws Exception
     *             if the test fails
     */
    @Test(dependsOnMethods = { "testHgroup3AddUserToGroup" }, groups = { "hierarchical-groups" })
    public void testHgroup4RemoveUserFromGroup() throws Exception {
        AuthenticationTestUtils.setSecurityContext(manager);

        // remove user3 from group4
        Long group4Id = UserAndGroupTestUtils.findGroup(getHierarchicalGroupAlias(4)).getId();
        String hgroup4UserAliasPattern = getHierarchicalGroupUserAliasPattern(4);
        String hgroup2UserAliasPattern = getHierarchicalGroupUserAliasPattern(2);
        Long rmUserId = UserAndGroupTestUtils.findNthUser(hgroup4UserAliasPattern, 3).getId();
        getGroupMemberManagement().removeEntityFromGroup(group4Id, rmUserId);
        // test that group4 and group2 are correct
        Long group2Id = UserAndGroupTestUtils.findGroup(getHierarchicalGroupAlias(2)).getId();
        assertGroupContainsGroup(group2Id, group4Id, hgroup2UserAliasPattern,
                hgroup4UserAliasPattern, new int[] { 3, 1 }, new int[] { 2, 0 });
        // removed user must not be in any group
        Long group1Id = UserAndGroupTestUtils.findGroup(getHierarchicalGroupAlias(1)).getId();
        Long group3Id = UserAndGroupTestUtils.findGroup(getHierarchicalGroupAlias(3)).getId();
        assertGroupDoesNotContainUser(group1Id, rmUserId);
        assertGroupDoesNotContainUser(group2Id, rmUserId);
        assertGroupDoesNotContainUser(group3Id, rmUserId);
        assertGroupDoesNotContainUser(group4Id, rmUserId);
        assertUserOfGroupMatchesGroupMembers(group1Id, 3, 3, 2, 2);

        // remove user3 from group2 (to ensure that contained groups are not affected)
        rmUserId = UserAndGroupTestUtils.findNthUser(hgroup2UserAliasPattern, 3).getId();
        getGroupMemberManagement().removeEntityFromGroup(group2Id, rmUserId);
        // test that group2 and group1 are correct
        assertGroupContainsGroup(group1Id, group2Id, getHierarchicalGroupUserAliasPattern(1),
                hgroup2UserAliasPattern, new int[] { 3, 2 }, new int[] { 2, 1 });
        assertGroupDoesNotContainUser(group1Id, rmUserId);
        assertGroupDoesNotContainUser(group2Id, rmUserId);
        assertGroupDoesNotContainUser(group3Id, rmUserId);
        assertGroupDoesNotContainUser(group4Id, rmUserId);
        int memberCount = ServiceLocator.findService(GroupDao.class).countMembers(group4Id);
        Assert.assertEquals(memberCount, 2);
        assertUserOfGroupMatchesGroupMembers(group1Id, 3, 2, 2, 2);
    }

    /**
     * Tests removing a user from a group which only contains the user as indirect member.
     *
     * @throws Exception
     *             if the test fails
     */
    @Test(dependsOnMethods = { "testHgroup4RemoveUserFromGroup" }, groups = { "hierarchical-groups" })
    public void testHgroup5RemoveIndirectUserFromGroup() throws Exception {
        AuthenticationTestUtils.setSecurityContext(manager);

        // remove user2 of group4 from group2
        Long rmUserId = UserAndGroupTestUtils.findNthUser(getHierarchicalGroupUserAliasPattern(4),
                2).getId();
        Long group2Id = UserAndGroupTestUtils.findGroup(getHierarchicalGroupAlias(2)).getId();
        getGroupMemberManagement().removeEntityFromGroup(group2Id, rmUserId);
        Long group1Id = UserAndGroupTestUtils.findGroup(getHierarchicalGroupAlias(1)).getId();
        Long group3Id = UserAndGroupTestUtils.findGroup(getHierarchicalGroupAlias(3)).getId();
        Long group4Id = UserAndGroupTestUtils.findGroup(getHierarchicalGroupAlias(4)).getId();
        assertGroupContainsUser(group4Id, rmUserId, true);
        assertGroupContainsUser(group2Id, rmUserId, false);
        assertGroupContainsUser(group1Id, rmUserId, false);
        assertGroupDoesNotContainUser(group3Id, rmUserId);
    }

    /**
     * Tests removing a group from a group.
     *
     * @throws Exception
     *             if the test fails
     */
    @Test(dependsOnMethods = { "testHgroup5RemoveIndirectUserFromGroup" }, groups = { "hierarchical-groups" })
    public void testHgroup6RemoveGroupFromGroup() throws Exception {
        AuthenticationTestUtils.setSecurityContext(manager);

        // remove group2 from group1
        Long group1Id = UserAndGroupTestUtils.findGroup(getHierarchicalGroupAlias(1)).getId();
        Long group2Id = UserAndGroupTestUtils.findGroup(getHierarchicalGroupAlias(2)).getId();
        getGroupMemberManagement().removeEntityFromGroup(group1Id, group2Id);
        boolean groupContained = getGroupMemberManagement().containsEntityDirectly(group1Id,
                group2Id);
        Assert.assertFalse(groupContained, "Group is still contained");
        Long group3Id = UserAndGroupTestUtils.findGroup(getHierarchicalGroupAlias(3)).getId();
        Long group4Id = UserAndGroupTestUtils.findGroup(getHierarchicalGroupAlias(4)).getId();
        String group2UserAliasPattern = getHierarchicalGroupUserAliasPattern(2);
        String group4UserAliasPattern = getHierarchicalGroupUserAliasPattern(4);
        assertGroupContainsGroup(group2Id, group4Id, group2UserAliasPattern,
                group4UserAliasPattern, new int[] { 2, 1 }, new int[] { 2, 0 });
        assertGroupContainsGroup(group1Id, group3Id, getHierarchicalGroupUserAliasPattern(1),
                getHierarchicalGroupUserAliasPattern(3), new int[] { 3, 1 }, new int[] { 2, 0 });
        // no user of group 2 or 4 must be in group1
        for (int i = 1; i <= 2; i++) {
            assertGroupDoesNotContainUser(group1Id,
                    UserAndGroupTestUtils.findNthUser(group2UserAliasPattern, i).getId());
            assertGroupDoesNotContainUser(group1Id,
                    UserAndGroupTestUtils.findNthUser(group4UserAliasPattern, i).getId());
        }
        assertUserOfGroupMatchesGroupMembers(group1Id, 3, 0, 2, 0);
    }

    /**
     * Tests adding a group that contains a group to another group.
     *
     * @throws Exception
     *             if the test fails
     */
    @Test(dependsOnMethods = { "testHgroup6RemoveGroupFromGroup" }, groups = { "hierarchical-groups" })
    public void testHgroup7AddGroupWithSubgroupToGroup() throws Exception {
        AuthenticationTestUtils.setSecurityContext(manager);

        // add group2 (with group4 as subgroup) to group1
        Long group1Id = UserAndGroupTestUtils.findGroup(getHierarchicalGroupAlias(1)).getId();
        Long group2Id = UserAndGroupTestUtils.findGroup(getHierarchicalGroupAlias(2)).getId();
        getGroupMemberManagement().addGroup(group1Id, group2Id);
        String group1UserAliasPattern = getHierarchicalGroupUserAliasPattern(1);
        String group2UserAliasPattern = getHierarchicalGroupUserAliasPattern(2);
        assertGroupContainsGroup(group1Id, group2Id, group1UserAliasPattern,
                group2UserAliasPattern, new int[] { 3, 2 }, new int[] { 2, 1 });
        String group3UserAliasPattern = getHierarchicalGroupUserAliasPattern(3);
        String group4UserAliasPattern = getHierarchicalGroupUserAliasPattern(4);
        Long group3Id = UserAndGroupTestUtils.findGroup(getHierarchicalGroupAlias(3)).getId();
        Long group4Id = UserAndGroupTestUtils.findGroup(getHierarchicalGroupAlias(4)).getId();
        assertGroupContainsGroup(group2Id, group4Id, group2UserAliasPattern,
                group4UserAliasPattern, new int[] { 2, 1 }, new int[] { 2, 0 });
        // group3 must be untouched and group1 must contain the users of group4
        assertGroupContainsGroup(group1Id, group3Id, group1UserAliasPattern,
                group3UserAliasPattern, new int[] { 3, 2 }, new int[] { 2, 0 });
        for (int i = 1; i <= 2; i++) {
            Long userId = UserAndGroupTestUtils.findNthUser(group4UserAliasPattern, i).getId();
            assertGroupContainsUser(group1Id, userId, false);
            assertGroupDoesNotContainUser(group3Id, userId);
            userId = UserAndGroupTestUtils.findNthUser(group2UserAliasPattern, i).getId();
            assertGroupDoesNotContainUser(group3Id, userId);
        }
        // test the UserOfGroupDao.getUsersOfGroup
        assertUserOfGroupMatchesGroupMembers(group1Id, 3, 2, 2, 2);
    }

    /**
     * Tests adding a user from another group of the group hierarchy to a group.
     *
     * @throws Exception
     *             if the test fails
     */
    @Test(dependsOnMethods = { "testHgroup7AddGroupWithSubgroupToGroup" }, groups = { "hierarchical-groups" })
    public void testHgroup8AddUserOfHierarchyToGroup() throws Exception {
        AuthenticationTestUtils.setSecurityContext(manager);

        // add user1 of group1 to group2
        Group group2 = UserAndGroupTestUtils.findGroup(getHierarchicalGroupAlias(2));
        String group1UserAliasPattern = getHierarchicalGroupUserAliasPattern(1);
        UserAndGroupTestUtils.addNthUserToGroup(group2, group1UserAliasPattern, 1, null);
        Long userId = UserAndGroupTestUtils.findNthUser(group1UserAliasPattern, 1).getId();
        assertGroupContainsUser(group2.getId(), userId, true);
        int memberCount = ServiceLocator.findService(GroupDao.class).countMembers(group2.getId());
        // expecting a group and 3 direct users
        Assert.assertEquals(memberCount, 3 + 1);
        Long group1Id = UserAndGroupTestUtils.findGroup(getHierarchicalGroupAlias(1)).getId();
        Long group3Id = UserAndGroupTestUtils.findGroup(getHierarchicalGroupAlias(3)).getId();
        Long group4Id = UserAndGroupTestUtils.findGroup(getHierarchicalGroupAlias(4)).getId();
        assertGroupContainsUser(group1Id, userId, true);
        assertGroupDoesNotContainUser(group3Id, userId);
        assertGroupDoesNotContainUser(group4Id, userId);
        assertUserOfGroupMatchesGroupMembers(group1Id, 3, 2, 2, 2);
        assertUserOfGroupMatchesGroupMembers(group2.getId(), 1, 2, 0, 2);
    }

    /**
     * Tests deleting a group from the group hierarchy.
     *
     * @throws Exception
     *             if the test fails
     */
    @Test(dependsOnMethods = { "testHgroup8AddUserOfHierarchyToGroup" }, groups = { "hierarchical-groups" })
    public void testHgroup9DeleteGroup() throws Exception {
        AuthenticationTestUtils.setSecurityContext(manager);

        // delete group2 (contained in group1 and contains group4)
        Long group2Id = UserAndGroupTestUtils.findGroup(getHierarchicalGroupAlias(2)).getId();
        getGroupManagement().deleteGroup(group2Id);
        Assert.assertNull(UserAndGroupTestUtils.findGroup(getHierarchicalGroupAlias(2)),
                "Group was not deleted");
        // users of group2 and group4 must not be in group1
        Long group1Id = UserAndGroupTestUtils.findGroup(getHierarchicalGroupAlias(1)).getId();
        Long group3Id = UserAndGroupTestUtils.findGroup(getHierarchicalGroupAlias(3)).getId();
        Long group4Id = UserAndGroupTestUtils.findGroup(getHierarchicalGroupAlias(4)).getId();
        String group2UserAliasPattern = getHierarchicalGroupUserAliasPattern(2);
        String group4UserAliasPattern = getHierarchicalGroupUserAliasPattern(4);
        for (int i = 1; i <= 2; i++) {
            Long userId = UserAndGroupTestUtils.findNthUser(group2UserAliasPattern, i).getId();
            assertGroupDoesNotContainUser(group1Id, userId);
            assertGroupDoesNotContainUser(group3Id, userId);
            userId = UserAndGroupTestUtils.findNthUser(group4UserAliasPattern, i).getId();
            assertGroupDoesNotContainUser(group1Id, userId);
            assertGroupDoesNotContainUser(group3Id, userId);
            assertGroupContainsUser(group4Id, userId, true);
        }
        assertGroupContainsGroup(group1Id, group3Id, getHierarchicalGroupUserAliasPattern(1),
                getHierarchicalGroupUserAliasPattern(3), new int[] { 3, 1 }, new int[] { 2, 0 });
        assertUserOfGroupMatchesGroupMembers(group1Id, 3, 0, 2, 0);
        assertUserOfGroupMatchesGroupMembers(group4Id, 0, 0, 0, 2);
    }

    /**
     * Tests adding a user to a group for a second time.
     *
     * @throws Exception
     *             if the test fails
     */
    @Test(dependsOnMethods = { "testRemoveUserFromAllGroups" })
    public void testReAddUserToGroup() throws Exception {
        AuthenticationTestUtils.setSecurityContext(manager);

        Long groupId = UserAndGroupTestUtils.findGroup(GROUP_1_ALIAS).getId();
        getGroupMemberManagement().addUser(groupId, user.getId());
        assertGroupContainsUser(groupId, user.getId(), true);
        // add to another group
        groupId = UserAndGroupTestUtils.findGroup(GROUP_2_ALIAS_EXPECTED).getId();
        getGroupMemberManagement().addUser(groupId, user.getId());
        assertGroupContainsUser(groupId, user.getId(), true);
    }

    /**
     * Tests the removal of a user from all groups.
     *
     * @throws Exception
     *             if the test fails
     */
    @Test(dependsOnMethods = { "testAddUserToGroup" }, dependsOnGroups = { "groupUpdate" })
    public void testRemoveUserFromAllGroups() throws Exception {
        AuthenticationTestUtils.setSecurityContext(manager);

        getGroupMemberManagement().removeUserFromAllGroups(user.getId());
        Long groupId = UserAndGroupTestUtils.findGroup(GROUP_1_ALIAS).getId();
        assertGroupDoesNotContainUser(groupId, user.getId());
        groupId = UserAndGroupTestUtils.findGroup(GROUP_2_ALIAS_EXPECTED).getId();
        assertGroupDoesNotContainUser(groupId, user.getId());
    }

    /**
     * Tests the removal of a user from all groups.
     *
     * @throws Exception
     *             if the test fails
     */
    @Test(dependsOnMethods = { "testReAddUserToGroup" }, dependsOnGroups = { "groupUpdate" })
    public void testRemoveUserFromGroup() throws Exception {
        AuthenticationTestUtils.setSecurityContext(manager);

        Long groupId = UserAndGroupTestUtils.findGroup(GROUP_2_ALIAS_EXPECTED).getId();
        getGroupMemberManagement().removeEntityFromGroup(groupId, user.getId());
        assertGroupDoesNotContainUser(groupId, user.getId());
    }

    /**
     * tests the update of an external group by a call to a method intended for updating non
     * external groups.
     *
     * @throws Exception
     *             if the test fails
     */
    @Test(dependsOnMethods = { "testCreateExternalGroup" })
    public void testUpdateExternalGroup() throws Exception {
        AuthenticationTestUtils.setSecurityContext(manager);

        Long groupId = UserAndGroupTestUtils.findGroup(EXTERNAL_GROUP_ALIAS).getId();
        GroupVO vo = new GroupVO("new name", null, "new description");
        try {
            getGroupManagement().updateGroup(groupId, vo);
        } catch (GroupOperationNotPermittedException e) {
            // do nothing because we expect this exception
            return;
        }
        Assert.fail("GroupOperationNotPermittedException was not thrown.");
    }

    /**
     * Tests the update of a group.
     *
     * @throws Exception
     *             if the test fails
     */
    @Test(dependsOnMethods = { "testCreateGroup" }, groups = { "groupUpdate" })
    public void testUpdateGroup() throws Exception {
        AuthenticationTestUtils.setSecurityContext(manager);

        GroupVO vo = new GroupVO(GROUP_1_NAME_UPDATED, GROUP_1_ALIAS_UPDATED,
                GROUP_1_DESCRIPTION_UPDATED);
        Long groupId = UserAndGroupTestUtils.findGroup(GROUP_1_ALIAS).getId();
        UserGroupManagement gm = getGroupManagement();
        gm.updateGroup(groupId, vo);
        Group group = UserAndGroupTestUtils.findGroup(GROUP_1_ALIAS_UPDATED);
        Assert.assertNull(group, "alias must not be changed");
        group = UserAndGroupTestUtils.findGroup(GROUP_1_ALIAS);
        Assert.assertEquals(group.getName(), GROUP_1_NAME_UPDATED);
        Assert.assertEquals(group.getDescription(), GROUP_1_DESCRIPTION_UPDATED);
    }

    /**
     * Tests the update of a group description to null.
     *
     * @throws Exception
     *             if the test fails
     */
    @Test(dependsOnMethods = { "testCreateGroup" }, groups = { "groupUpdate" })
    public void testUpdateGroupNullDescription() throws Exception {
        AuthenticationTestUtils.setSecurityContext(manager);

        GroupVO vo = new GroupVO(GROUP_1_NAME_UPDATED, GROUP_1_ALIAS, null);
        Long groupId = UserAndGroupTestUtils.findGroup(GROUP_1_ALIAS).getId();
        UserGroupManagement gm = getGroupManagement();
        gm.updateGroup(groupId, vo);
        Group group = UserAndGroupTestUtils.findGroup(GROUP_1_ALIAS);
        Assert.assertEquals(group.getName(), GROUP_1_NAME_UPDATED);
        Assert.assertNull(group.getDescription());
    }

}
