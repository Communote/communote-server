/**
 *
 */
package com.communote.server.core.blog;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.blog.BlogMemberNotFoundException;
import com.communote.server.core.security.AuthenticationHelper;
import com.communote.server.model.blog.BlogRole;
import com.communote.server.model.user.User;
import com.communote.server.model.user.group.Group;
import com.communote.server.persistence.user.group.UserOfGroupDao;
import com.communote.server.test.util.AuthenticationTestUtils;
import com.communote.server.test.util.TestUtils;
import com.communote.server.test.util.UserAndGroupTestUtils;

/**
 * Tests blog access restrictions. Does the tests for hierarchical groups.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class BlogRightsHierarchicalGroupsTest extends BlogRightsTestBase {
    private static final String BLOG_MANAGER_ALIAS = "blogmanager_hg" + UUID.randomUUID();
    private static final String BLOG_MANAGER_EMAIL = "blogmanager_hg" + UUID.randomUUID()
            + "@localhost";
    private static final String BLOG_ALIAS_HGROUP = "blog_hgroup" + UUID.randomUUID();

    private static final String RANDOM = "_" + UUID.randomUUID() + "_";

    private Long blogId;
    private User blogManager;
    private User manager;

    /**
     * Returns the group alias for the nth hierarchical group.
     *
     * @param number
     *            the number of the group whose alias is to be returned
     * @return the alias
     */
    private String getHierarchicalGroupAlias(int number) {
        return "hierarchGroup_blog" + RANDOM + number;
    }

    /**
     * Returns the group name for the nth hierarchical group.
     *
     * @param number
     *            the number of the group whose name is to be returned
     * @return the group name
     */
    private String getHierarchicalGroupName(int number) {
        return "name of hierarchGroup_blog" + RANDOM + number;
    }

    /**
     * Returns the user alias pattern for the members of the nth hierarchical group.
     *
     * @param number
     *            the number of the group for which the user alias pattern is to be returned
     * @return the user alias pattern
     */
    private String getHierarchicalGroupUserAliasPattern(int number) {
        return "hgroup_blog" + number + "user%d" + RANDOM;
    }

    /**
     * Returns the user email address pattern for the members of the nth hierarchical group.
     *
     * @param number
     *            the number of the group for which the user email pattern is to be returned
     * @return the user email address pattern
     */
    private String getHierarchicalGroupUserEmailPattern(int number) {
        return "hgroup_blog" + RANDOM + number + "user%d@localhost";
    }

    /**
     * Prepares the tests.
     *
     * @throws Exception
     *             if the setup fails
     */
    @BeforeClass(dependsOnGroups = "integration-test-setup")
    public void prepareTests() throws Exception {
        manager = TestUtils.createRandomUser(true);
        AuthenticationTestUtils.setSecurityContext(manager);
        // create users and a group hierarchy
        int[] usersToCreate = { 3, 4, 3, 3 };
        for (int i = 1; i <= 4; i++) {
            String alias = getHierarchicalGroupAlias(i);
            createGroup(alias, getHierarchicalGroupName(i), null);
            Group group = UserAndGroupTestUtils.findGroup(alias);
            Assert.assertNotNull(group);
            String userAliasPattern = getHierarchicalGroupUserAliasPattern(i);
            UserAndGroupTestUtils.createDummyUsers(userAliasPattern,
                    getHierarchicalGroupUserEmailPattern(i), usersToCreate[i - 1]);
            UserAndGroupTestUtils.addUsersToGroup(group, userAliasPattern, 3, null);
        }
        Long group1Id = UserAndGroupTestUtils.findGroup(getHierarchicalGroupAlias(1)).getId();
        Long group2Id = UserAndGroupTestUtils.findGroup(getHierarchicalGroupAlias(2)).getId();
        Long group3Id = UserAndGroupTestUtils.findGroup(getHierarchicalGroupAlias(3)).getId();
        Long group4Id = UserAndGroupTestUtils.findGroup(getHierarchicalGroupAlias(4)).getId();
        getGroupMemberManagement().addGroup(group1Id, group3Id);
        getGroupMemberManagement().addGroup(group2Id, group4Id);
        // create the blog manager
        UserAndGroupTestUtils.createDummyUsers(BLOG_MANAGER_ALIAS, BLOG_MANAGER_EMAIL, 1);
        // create blog
        this.blogManager = UserAndGroupTestUtils.findNthUser(BLOG_MANAGER_ALIAS, -1);
        putBlogManagerInSecurityContext();
        createBlog(blogManager, BLOG_ALIAS_HGROUP);
        this.blogId = getBlogManagement().findBlogByIdentifier(BLOG_ALIAS_HGROUP).getId();
    }

    /**
     * Puts the blog manager into the security context.
     */
    private void putBlogManagerInSecurityContext() {
        AuthenticationHelper.removeAuthentication();
        AuthenticationTestUtils.setSecurityContext(this.blogManager);
    }

    /**
     * Tests adding a group that has a subgroup.
     *
     * @throws Exception
     *             if the test fails
     */
    @Test
    public void test01AddGroupToBlog() throws Exception {
        Long group1Id = UserAndGroupTestUtils.findGroup(getHierarchicalGroupAlias(1)).getId();
        putBlogManagerInSecurityContext();
        getBlogRightsManagement().addEntity(this.blogId, group1Id, BlogRole.VIEWER);
        assertGroupBlogAccess(this.blogId, group1Id, BlogRole.VIEWER, false, null);
        // assert that the blog manager is still there
        assertBlogAccess(this.blogId, this.blogManager.getId(), BlogRole.MANAGER, false, true);
    }

    /**
     * Tests adding a group that has a subgroup of another group that is already assigned to the
     * blog.
     *
     * @throws Exception
     *             if the test fails
     */
    @Test(dependsOnMethods = { "test01AddGroupToBlog" })
    public void test02AddGroupToGroup() throws Exception {
        AuthenticationTestUtils.setSecurityContext(manager);
        // add group2 to group1
        Long group1Id = UserAndGroupTestUtils.findGroup(getHierarchicalGroupAlias(1)).getId();
        Long group2Id = UserAndGroupTestUtils.findGroup(getHierarchicalGroupAlias(2)).getId();
        getGroupMemberManagement().addGroup(group1Id, group2Id);
        assertGroupBlogAccess(this.blogId, group1Id, BlogRole.VIEWER, false, null);
        // assert that the blog manager is still there
        assertBlogAccess(this.blogId, this.blogManager.getId(), BlogRole.MANAGER, false, true);
    }

    /**
     * Tests adding a user that is a member of a group that is a subgroup of a group added as blog
     * member to the blog.
     *
     * @throws Exception
     *             if the test fails
     */
    @Test(dependsOnMethods = { "test02AddGroupToGroup" })
    public void test03AddGroupMemberToBlog() throws Exception {
        // add user3 of group2 as Reader
        Long userId = UserAndGroupTestUtils.findNthUser(getHierarchicalGroupUserAliasPattern(2), 3)
                .getId();
        putBlogManagerInSecurityContext();
        getBlogRightsManagement().addEntity(this.blogId, userId, BlogRole.MEMBER);
        assertBlogAccess(this.blogId, userId, BlogRole.MEMBER, false, true);
        Long group1Id = UserAndGroupTestUtils.findGroup(getHierarchicalGroupAlias(1)).getId();
        Set<Long> usersToSkip = new HashSet<Long>();
        usersToSkip.add(userId);
        assertGroupBlogAccess(this.blogId, group1Id, BlogRole.VIEWER, false, usersToSkip);
        assertBlogAccess(this.blogId, this.blogManager.getId(), BlogRole.MANAGER, false, true);
    }

    /**
     * Tests adding a group that is a subgroup of a group which is added as blog member to the blog.
     *
     * @throws Exception
     *             if the test fails
     */
    @Test(dependsOnMethods = { "test03AddGroupMemberToBlog" })
    public void test04AddSubGroupToBlog() throws Exception {
        // add group2 as MANAGER to the blog
        Long group2Id = UserAndGroupTestUtils.findGroup(getHierarchicalGroupAlias(2)).getId();
        putBlogManagerInSecurityContext();
        getBlogRightsManagement().assignEntity(this.blogId, group2Id, BlogRole.MANAGER);
        // all members of group2 (and group4) must have MANAGER role and apart from user3 of group2
        // indirect access
        Long userId = UserAndGroupTestUtils.findNthUser(getHierarchicalGroupUserAliasPattern(2), 3)
                .getId();
        Set<Long> usersToSkip = new HashSet<Long>();
        usersToSkip.add(userId);
        assertGroupBlogAccess(this.blogId, group2Id, BlogRole.MANAGER, false, usersToSkip);
        assertBlogAccess(this.blogId, userId, BlogRole.MANAGER, false, true);
        // to test group1 members skip all users of group2 and group4
        for (int i = 1; i <= 3; i++) {
            Long uid = UserAndGroupTestUtils
                    .findNthUser(getHierarchicalGroupUserAliasPattern(2), i).getId();
            usersToSkip.add(uid);
            uid = UserAndGroupTestUtils.findNthUser(getHierarchicalGroupUserAliasPattern(4), i)
                    .getId();
            usersToSkip.add(uid);
        }
        Long group1Id = UserAndGroupTestUtils.findGroup(getHierarchicalGroupAlias(1)).getId();
        assertGroupBlogAccess(this.blogId, group1Id, BlogRole.VIEWER, false, usersToSkip);
        assertBlogAccess(this.blogId, this.blogManager.getId(), BlogRole.MANAGER, false, true);
    }

    /**
     * Tests adding a user to a group that is a subgroup of a group added to blog.
     *
     * @throws Exception
     *             if the test fails
     */
    @Test(dependsOnMethods = { "test04AddSubGroupToBlog" })
    public void test05AddUserToGroup() throws Exception {
        Long group2Id = UserAndGroupTestUtils.findGroup(getHierarchicalGroupAlias(2)).getId();
        // add 4th user to group2
        Long fourthUserId = UserAndGroupTestUtils.findNthUser(
                getHierarchicalGroupUserAliasPattern(2), 4).getId();
        AuthenticationTestUtils.setSecurityContext(manager);
        getGroupMemberManagement().addUser(group2Id, fourthUserId);
        assertBlogAccess(this.blogId, fourthUserId, BlogRole.MANAGER, false, false);
        Long userId = UserAndGroupTestUtils.findNthUser(getHierarchicalGroupUserAliasPattern(2), 3)
                .getId();
        Set<Long> usersToSkip = new HashSet<Long>();
        usersToSkip.add(userId);
        assertGroupBlogAccess(this.blogId, group2Id, BlogRole.MANAGER, false, usersToSkip);
        assertBlogAccess(this.blogId, userId, BlogRole.MANAGER, false, true);
        Long group1Id = UserAndGroupTestUtils.findGroup(getHierarchicalGroupAlias(1)).getId();
        usersToSkip.addAll(ServiceLocator.findService(UserOfGroupDao.class).getUsersOfGroup(
                group2Id));
        assertGroupBlogAccess(this.blogId, group1Id, BlogRole.VIEWER, false, usersToSkip);
        assertBlogAccess(this.blogId, this.blogManager.getId(), BlogRole.MANAGER, false, true);
    }

    /**
     * Tests changing the role of an subgroup that is not a direct member.
     *
     * @throws Exception
     *             if the test fails
     */
    @Test(dependsOnMethods = { "test05AddUserToGroup" })
    public void test06ChangeRoleOfSubGroup() throws Exception {
        // try changing the role of group 4
        Long group4Id = UserAndGroupTestUtils.findGroup(getHierarchicalGroupAlias(4)).getId();
        putBlogManagerInSecurityContext();
        try {
            getBlogRightsManagement().changeRoleOfMemberByEntityId(blogId, group4Id,
                    BlogRole.VIEWER);
            Assert.fail("BlogMemberNotFoundException was not thrown.");
        } catch (BlogMemberNotFoundException e) {
            // expected exception
        }
    }

    /**
     * Tests changing the role of an subgroup that is not a direct member.
     *
     * @throws Exception
     *             if the test fails
     */
    @Test(dependsOnMethods = { "test06ChangeRoleOfSubGroup" })
    public void test07ChangeRoleOfGroup() throws Exception {
        // change role of group 2 to VIEWER
        Long group2Id = UserAndGroupTestUtils.findGroup(getHierarchicalGroupAlias(2)).getId();
        // use assign method to test that too
        putBlogManagerInSecurityContext();
        getBlogRightsManagement().assignEntity(blogId, group2Id, BlogRole.VIEWER);
        Long userId = UserAndGroupTestUtils.findNthUser(getHierarchicalGroupUserAliasPattern(2), 3)
                .getId();
        Set<Long> usersToSkip = new HashSet<Long>();
        usersToSkip.add(userId);
        assertGroupBlogAccess(this.blogId, group2Id, BlogRole.VIEWER, false, usersToSkip);
        assertBlogAccess(this.blogId, userId, BlogRole.MEMBER, false, true);
        Long group1Id = UserAndGroupTestUtils.findGroup(getHierarchicalGroupAlias(1)).getId();
        assertGroupBlogAccess(this.blogId, group1Id, BlogRole.VIEWER, false, usersToSkip);

        // change role of group1 to MANAGER
        getBlogRightsManagement().changeRoleOfMemberByEntityId(blogId, group1Id, BlogRole.MANAGER);
        // check role of group2 directly because we would need to skip all users of that group when
        // using assertGroupBlogAccess because group has role VIEWER but members have MANAGER
        BlogRole newRole = getBlogRightsManagement().getRoleOfEntity(blogId, group2Id, false);
        Assert.assertEquals(newRole, BlogRole.VIEWER);
        assertBlogAccess(this.blogId, userId, BlogRole.MANAGER, false, true);
        assertGroupBlogAccess(this.blogId, group1Id, BlogRole.MANAGER, false, usersToSkip);

        // change everything back
        getBlogRightsManagement().changeRoleOfMemberByEntityId(blogId, group2Id, BlogRole.MANAGER);
        getBlogRightsManagement().assignEntity(blogId, group1Id, BlogRole.VIEWER);
        assertGroupBlogAccess(this.blogId, group2Id, BlogRole.MANAGER, false, usersToSkip);
        assertBlogAccess(this.blogId, userId, BlogRole.MANAGER, false, true);
        usersToSkip.addAll(ServiceLocator.findService(UserOfGroupDao.class).getUsersOfGroup(
                group2Id));
        assertGroupBlogAccess(this.blogId, group1Id, BlogRole.VIEWER, false, usersToSkip);
    }

    /**
     * Tests removing a group with sub group from a blog.
     *
     * @throws Exception
     *             if the test fails
     */
    @Test(dependsOnMethods = { "test07ChangeRoleOfGroup" })
    public void test08RemoveGroupFromBlog() throws Exception {
        // remove group 2
        Long group2Id = UserAndGroupTestUtils.findGroup(getHierarchicalGroupAlias(2)).getId();
        putBlogManagerInSecurityContext();
        getBlogRightsManagement().removeMemberByEntityId(blogId, group2Id);
        Long userId = UserAndGroupTestUtils.findNthUser(getHierarchicalGroupUserAliasPattern(2), 3)
                .getId();
        Set<Long> usersToSkip = new HashSet<Long>();
        usersToSkip.add(userId);
        boolean directMember = getBlogRightsManagement().isEntityDirectMember(blogId, group2Id);
        Assert.assertFalse(directMember, "Group is still direct member.");
        assertBlogAccess(blogId, userId, BlogRole.MEMBER, false, true);
        Long group1Id = UserAndGroupTestUtils.findGroup(getHierarchicalGroupAlias(1)).getId();
        assertGroupBlogAccess(blogId, group1Id, BlogRole.VIEWER, false, usersToSkip);

        // restore previous state
        getBlogRightsManagement().addEntity(blogId, group2Id, BlogRole.MANAGER);
        assertBlogAccess(blogId, userId, BlogRole.MANAGER, false, true);
        assertGroupBlogAccess(blogId, group2Id, BlogRole.MANAGER, false, usersToSkip);
        usersToSkip.addAll(ServiceLocator.findService(UserOfGroupDao.class).getUsersOfGroup(
                group2Id));
        assertGroupBlogAccess(blogId, group1Id, BlogRole.VIEWER, false, usersToSkip);
    }

    /**
     * Tests removing a group from a group that is a blog member.
     *
     * @throws Exception
     *             if the test fails
     */
    @Test(dependsOnMethods = { "test08RemoveGroupFromBlog" })
    public void test09RemoveGroupFromGroup() throws Exception {
        // remove group 2 from group 1
        Long group1Id = UserAndGroupTestUtils.findGroup(getHierarchicalGroupAlias(1)).getId();
        Long group2Id = UserAndGroupTestUtils.findGroup(getHierarchicalGroupAlias(2)).getId();
        AuthenticationTestUtils.setSecurityContext(manager);
        getGroupMemberManagement().removeEntityFromGroup(group1Id, group2Id);
        Long userId = UserAndGroupTestUtils.findNthUser(getHierarchicalGroupUserAliasPattern(2), 3)
                .getId();
        Set<Long> usersToSkip = new HashSet<Long>();
        usersToSkip.add(userId);
        assertGroupBlogAccess(blogId, group2Id, BlogRole.MANAGER, false, usersToSkip);
        assertBlogAccess(blogId, userId, BlogRole.MANAGER, false, true);
        assertGroupBlogAccess(blogId, group1Id, BlogRole.VIEWER, false, null);

        // restore previous state
        getGroupMemberManagement().addGroup(group1Id, group2Id);
        assertGroupBlogAccess(blogId, group2Id, BlogRole.MANAGER, false, usersToSkip);
        assertBlogAccess(blogId, userId, BlogRole.MANAGER, false, true);
        usersToSkip.addAll(ServiceLocator.findService(UserOfGroupDao.class).getUsersOfGroup(
                group2Id));
        assertGroupBlogAccess(blogId, group1Id, BlogRole.VIEWER, false, usersToSkip);
    }

    /**
     * Tests removing a user from a subgroup.
     *
     * @throws Exception
     *             if the test fails
     */
    @Test(dependsOnMethods = { "test09RemoveGroupFromGroup" })
    public void test10RemoveUserFromGroup() throws Exception {
        // remove user 4 from group2
        Long group2Id = UserAndGroupTestUtils.findGroup(getHierarchicalGroupAlias(2)).getId();
        Long fourthUserId = UserAndGroupTestUtils.findNthUser(
                getHierarchicalGroupUserAliasPattern(2), 4).getId();
        AuthenticationTestUtils.setSecurityContext(manager);
        getGroupMemberManagement().removeEntityFromGroup(group2Id, fourthUserId);
        assertBlogAccess(blogId, fourthUserId, null, false, true);

        // everthing else must be unchanged
        Long userId = UserAndGroupTestUtils.findNthUser(getHierarchicalGroupUserAliasPattern(2), 3)
                .getId();
        Set<Long> usersToSkip = new HashSet<Long>();
        usersToSkip.add(userId);
        assertGroupBlogAccess(blogId, group2Id, BlogRole.MANAGER, false, usersToSkip);
        assertBlogAccess(blogId, userId, BlogRole.MANAGER, false, true);
        usersToSkip.addAll(ServiceLocator.findService(UserOfGroupDao.class).getUsersOfGroup(
                group2Id));
        Long group1Id = UserAndGroupTestUtils.findGroup(getHierarchicalGroupAlias(1)).getId();
        assertGroupBlogAccess(blogId, group1Id, BlogRole.VIEWER, false, usersToSkip);
    }

    /**
     * Tests deleting a group from the group hierarchy.
     *
     * @throws Exception
     *             if the test fails
     */
    @Test(dependsOnMethods = { "test10RemoveUserFromGroup" })
    public void test11DeleteGroup() throws Exception {
        // make it a little more interesting by adding group 4 to group 3
        Long group3Id = UserAndGroupTestUtils.findGroup(getHierarchicalGroupAlias(3)).getId();
        Long group4Id = UserAndGroupTestUtils.findGroup(getHierarchicalGroupAlias(4)).getId();
        AuthenticationTestUtils.setSecurityContext(manager);
        getGroupMemberManagement().addGroup(group3Id, group4Id);
        // this does not change anything regarding the roles
        Long group1Id = UserAndGroupTestUtils.findGroup(getHierarchicalGroupAlias(1)).getId();
        Long group2Id = UserAndGroupTestUtils.findGroup(getHierarchicalGroupAlias(2)).getId();
        Long userId = UserAndGroupTestUtils.findNthUser(getHierarchicalGroupUserAliasPattern(2), 3)
                .getId();
        Set<Long> usersToSkip = new HashSet<Long>();
        usersToSkip.add(userId);
        assertGroupBlogAccess(blogId, group2Id, BlogRole.MANAGER, false, usersToSkip);
        assertBlogAccess(blogId, userId, BlogRole.MANAGER, false, true);
        usersToSkip.addAll(ServiceLocator.findService(UserOfGroupDao.class).getUsersOfGroup(
                group2Id));
        assertGroupBlogAccess(blogId, group1Id, BlogRole.VIEWER, false, usersToSkip);

        // delete group 2 (current hierarchy g1<-(g3<-g4,g2<-g4); current blog members:
        // (g1,V),(g2,M),(u3_g2,R)
        getGroupManagement().deleteGroup(group2Id);
        assertGroupBlogAccess(blogId, group2Id, null, false, null);
        assertBlogAccess(blogId, userId, BlogRole.MEMBER, false, true);
        assertGroupBlogAccess(blogId, group1Id, BlogRole.VIEWER, false, null);
        // just to make sure test users of group 4 separately
        Collection<Long> group4UserIds = ServiceLocator.findService(UserOfGroupDao.class)
                .getUsersOfGroup(group4Id);
        Assert.assertEquals(3, group4UserIds.size());
        for (Long g4UserId : group4UserIds) {
            assertBlogAccess(blogId, g4UserId, BlogRole.VIEWER, false, false);
        }
    }

    /**
     * Tests deleting a group with users that are also in an other group from the group hierarchy.
     *
     * @throws Exception
     *             if the test fails
     */
    @Test(dependsOnMethods = { "test11DeleteGroup" })
    public void test12DeleteMixedGroup() throws Exception {
        // create the group 2 containing 2 users from group 2 and 2 from group 3
        AuthenticationTestUtils.setSecurityContext(manager);
        createGroup(getHierarchicalGroupAlias(2), getHierarchicalGroupName(2), null);
        Group group2 = UserAndGroupTestUtils.findGroup(getHierarchicalGroupAlias(2));
        Long group2Id = group2.getId();
        UserAndGroupTestUtils.addNthUserToGroup(group2, getHierarchicalGroupUserAliasPattern(2), 1,
                null);
        UserAndGroupTestUtils.addNthUserToGroup(group2, getHierarchicalGroupUserAliasPattern(2), 2,
                null);
        UserAndGroupTestUtils.addNthUserToGroup(group2, getHierarchicalGroupUserAliasPattern(3), 1,
                null);
        UserAndGroupTestUtils.addNthUserToGroup(group2, getHierarchicalGroupUserAliasPattern(3), 2,
                null);
        // add group 4 to group 2
        Long group4Id = UserAndGroupTestUtils.findGroup(getHierarchicalGroupAlias(4)).getId();
        getGroupMemberManagement().addGroup(group2Id, group4Id);
        // add group 2 to group 1
        Long group1Id = UserAndGroupTestUtils.findGroup(getHierarchicalGroupAlias(1)).getId();
        getGroupMemberManagement().addGroup(group1Id, group2Id);
        assertGroupBlogAccess(blogId, group1Id, BlogRole.VIEWER, false, null);
        for (int i = 1; i <= 2; i++) {
            Long uid = UserAndGroupTestUtils
                    .findNthUser(getHierarchicalGroupUserAliasPattern(2), i).getId();
            assertBlogAccess(blogId, uid, BlogRole.VIEWER, false, false);
            uid = UserAndGroupTestUtils.findNthUser(getHierarchicalGroupUserAliasPattern(3), i)
                    .getId();
            assertBlogAccess(blogId, uid, BlogRole.VIEWER, false, false);
        }
        Long userId = UserAndGroupTestUtils.findNthUser(getHierarchicalGroupUserAliasPattern(2), 3)
                .getId();
        assertBlogAccess(blogId, userId, BlogRole.MEMBER, false, true);

        // add group 2 to blog
        putBlogManagerInSecurityContext();
        getBlogRightsManagement().addEntity(blogId, group2Id, BlogRole.MANAGER);
        assertBlogAccess(blogId, userId, BlogRole.MEMBER, false, true);
        Set<Long> usersToSkip = new HashSet<Long>();
        Collection<Long> group2UserIds = ServiceLocator.findService(UserOfGroupDao.class)
                .getUsersOfGroup(group2Id);
        int confirmedUsers = 0;
        for (int i = 2; i <= 4; i++) {
            for (int j = 1; j <= 2; j++) {
                Long uid = UserAndGroupTestUtils.findNthUser(
                        getHierarchicalGroupUserAliasPattern(i), j).getId();
                if (group2UserIds.contains(uid)) {
                    confirmedUsers++;
                }
            }
        }
        Long group4UserId3 = UserAndGroupTestUtils.findNthUser(
                getHierarchicalGroupUserAliasPattern(4), 3).getId();
        if (group2UserIds.contains(group4UserId3)) {
            confirmedUsers++;
        }
        Assert.assertEquals(confirmedUsers, group2UserIds.size());
        usersToSkip.addAll(group2UserIds);
        assertGroupBlogAccess(blogId, group1Id, BlogRole.VIEWER, false, usersToSkip);
        assertGroupBlogAccess(blogId, group2Id, BlogRole.MANAGER, false, null);

        // delete group 2
        AuthenticationTestUtils.setSecurityContext(manager);
        getGroupManagement().deleteGroup(group2Id);
        assertGroupBlogAccess(blogId, group2Id, null, false, null);
        assertBlogAccess(blogId, userId, BlogRole.MEMBER, false, true);
        assertGroupBlogAccess(blogId, group1Id, BlogRole.VIEWER, false, null);
        // just to be sure: test that the users of group 3 still have access
        for (int i = 1; i <= 3; i++) {
            Long uid = UserAndGroupTestUtils
                    .findNthUser(getHierarchicalGroupUserAliasPattern(3), i).getId();
            assertBlogAccess(blogId, uid, BlogRole.VIEWER, false, false);
        }
    }
}
