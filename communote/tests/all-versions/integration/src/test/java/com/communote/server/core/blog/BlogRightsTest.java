package com.communote.server.core.blog;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.springframework.security.core.context.SecurityContext;
import org.testng.Assert;
import org.testng.annotations.AfterGroups;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeGroups;
import org.testng.annotations.Test;

import com.communote.plugin.ldap.LdapActivator;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.blog.BlogAccessException;
import com.communote.server.api.core.blog.NoBlogManagerLeftException;
import com.communote.server.core.external.ExternalObjectManagement;
import com.communote.server.core.security.AuthenticationHelper;
import com.communote.server.core.user.UserGroupManagement;
import com.communote.server.installer.InstallerTest;
import com.communote.server.model.blog.Blog;
import com.communote.server.model.blog.BlogRole;
import com.communote.server.model.external.ExternalObject;
import com.communote.server.model.user.User;
import com.communote.server.model.user.group.Group;
import com.communote.server.test.util.AuthenticationTestUtils;
import com.communote.server.test.util.TestUtils;
import com.communote.server.test.util.UserAndGroupTestUtils;

/**
 * Tests blog access restrictions. Does not test hierarchical groups.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
@Test(singleThreaded = true)
public class BlogRightsTest extends BlogRightsTestBase {

    private static final String BLOG_MANAGER_ALIAS_PATTERN_PREFIX = "managebloger_"
            + UUID.randomUUID() + "_%d";
    private static final String BLOG_MANAGER_EMAIL_PATTERN = "managebloger_" + UUID.randomUUID()
            + "_%d@localhost";
    private static final String BLOG_1_ALIAS = "blogrights-blog-1" + UUID.randomUUID();
    private static final String GROUP_1_ALIAS = "blogrights-group-1" + UUID.randomUUID();
    private static final String GROUP_2_ALIAS = "blogrights-group-2" + UUID.randomUUID();
    private static final String GROUP_1_NAME = "blogrights group 1" + UUID.randomUUID();
    private static final String GROUP_2_NAME = "blogrights group 2" + UUID.randomUUID();
    private static final String BLOG_2_ALIAS = "blogrights-blog-2" + UUID.randomUUID();
    private static final String USER_GROUP_1_ALIAS_PATTERN_PREFIX = "gr1bloger%d"
            + UUID.randomUUID();
    private static final String USER_GROUP_1_EMAIL_PATTERN = "gr1bloger%d_" + UUID.randomUUID()
            + "@localhost";
    private static final String USER_GROUP_2_ALIAS_PATTERN_PREFIX = "gr2bloger%d"
            + UUID.randomUUID();
    private static final String USER_GROUP_2_EMAIL_PATTERN = "gr2bloger%d_" + UUID.randomUUID()
            + "@localhost";
    private static final String USER_GROUP_1A2_ALIAS_PATTERN_PREFIX = "gr1a2bloger%d"
            + UUID.randomUUID();
    private static final String USER_GROUP_1A2_EMAIL_PATTERN = "gr1a2bloger%d_" + UUID.randomUUID()
            + "@localhost";
    private static final String USER_NO_GROUP_ALIAS_PATTERN_PREFIX = "bloger%d" + UUID.randomUUID();
    private static final String USER_NO_GROUP_EMAIL_PATTERN = "bloger%d_" + UUID.randomUUID()
            + "@localhost";
    private static final String USER_NON_BLOG_MEMBER_ALIAS = "iamnotinanyblog" + UUID.randomUUID();
    private static final String USER_NON_BLOG_MEMBER_EMAIL = "iamnotinanyblog_" + UUID.randomUUID()
            + "@localhost";

    private String testExternalSystemId;

    private Set<Long> usersInBothGroups;
    private User manager;

    /**
     * Cleanup after group allCanBlogMemberTestsGroup.
     *
     */
    @AfterGroups(groups = { "allCanBlogMemberTestsGroup" })
    public void afterAllCanBlogMemberTestsGroup() {
        AuthenticationHelper.removeAuthentication();

    }

    /**
     * Cleanup after group externalGroupBlogMemberTestsGroup.
     *
     * @throws Exception
     *             in case clean up failed
     */
    @AfterGroups(groups = { "externalBlogMemberTestsGroup" })
    public void afterExternalBlogMemberTestsGroup() throws Exception {
        AuthenticationHelper.removeAuthentication();
        // remove group from other blog
        AuthenticationTestUtils.setSecurityContext(UserAndGroupTestUtils.findNthUser(
                BLOG_MANAGER_ALIAS_PATTERN_PREFIX, 2));
        Long blogId = getBlogManagement().findBlogByIdentifier(BLOG_2_ALIAS).getId();
        Long groupId2 = UserAndGroupTestUtils.findGroup(GROUP_2_ALIAS).getId();
        getBlogRightsManagement().removeMemberByEntityId(blogId, groupId2);
        Long userId = UserAndGroupTestUtils.findNthUser(USER_GROUP_1A2_ALIAS_PATTERN_PREFIX, 1)
                .getId();
        Set<Long> usersToSkip = new HashSet<Long>(1);
        usersToSkip.add(userId);
        assertGroupBlogAccess(blogId, groupId2, null, false, usersToSkip);
        // user must be unaffected
        assertBlogAccess(blogId, userId, BlogRole.MANAGER, false, true);
        getBlogRightsManagement().removeMemberByEntityId(blogId, userId);
        assertBlogAccess(blogId, userId, null, false, false);
        AuthenticationHelper.removeAuthentication();
    }

    /**
     * Cleanup after group userBlogMemberTestsGroup.
     *
     * @throws Exception
     *             in case clean up failed
     */
    @AfterGroups(groups = { "groupBlogMemberTestsGroup" })
    public void afterGroupBlogMemberTestsGroup() throws Exception {
        // remove the user from the blog
        Long userId = UserAndGroupTestUtils.findNthUser(USER_GROUP_1_ALIAS_PATTERN_PREFIX, 1)
                .getId();
        AuthenticationTestUtils.setSecurityContext(UserAndGroupTestUtils.findNthUser(
                BLOG_MANAGER_ALIAS_PATTERN_PREFIX, 2));
        Long blogId = getBlogManagement().findBlogByIdentifier(BLOG_2_ALIAS).getId();
        getBlogRightsManagement().removeMemberByEntityId(blogId, userId);
        assertBlogAccess(blogId, userId, null, false, true);
        AuthenticationHelper.removeAuthentication();
    }

    /**
     * Cleanup after group modGroupBlogMemberTestsGroup.
     *
     */
    @AfterGroups(groups = { "modGroupBlogMemberTestsGroup" })
    public void afterModGroupBlogMemberTestsGroup() {
        AuthenticationHelper.removeAuthentication();
    }

    /**
     * Cleanup after group xGroupBlogMemberTestsGroup.
     *
     * @throws Exception
     *             in case clean up failed
     */
    @AfterGroups(groups = { "xGroupBlogMemberTestsGroup" })
    public void afterXGroupBlogMemberTestsGroup() throws Exception {
        // remove the group and user from the blog
        Long blogId2 = getBlogManagement().findBlogByIdentifier(BLOG_2_ALIAS).getId();
        Long groupId1 = UserAndGroupTestUtils.findGroup(GROUP_1_ALIAS).getId();
        getBlogRightsManagement().removeMemberByEntityId(blogId2, groupId1);
        Long userId = UserAndGroupTestUtils.findNthUser(USER_GROUP_1A2_ALIAS_PATTERN_PREFIX, 1)
                .getId();
        Set<Long> usersToSkip = new HashSet<Long>();
        usersToSkip.add(userId);
        assertGroupBlogAccess(blogId2, groupId1, null, false, usersToSkip);
        getBlogRightsManagement().removeMemberByEntityId(blogId2, userId);
        assertBlogAccess(blogId2, userId, null, false, true);
        AuthenticationHelper.removeAuthentication();
        // remove group from other blog
        AuthenticationTestUtils.setSecurityContext(UserAndGroupTestUtils.findNthUser(
                BLOG_MANAGER_ALIAS_PATTERN_PREFIX, 1));
        Long blogId1 = getBlogManagement().findBlogByIdentifier(BLOG_1_ALIAS).getId();
        Long groupId2 = UserAndGroupTestUtils.findGroup(GROUP_2_ALIAS).getId();
        getBlogRightsManagement().removeMemberByEntityId(blogId1, groupId2);
        AuthenticationHelper.removeAuthentication();
    }

    /**
     * Prepare before running tests of allCanBlogMemberTestsGroup.
     *
     * @throws Exception
     *             if setup fails
     */
    @BeforeGroups(groups = { "allCanBlogMemberTestsGroup" })
    public void beforeAllCanBlogMemberTestsGroup() throws Exception {
        // no setup for blog memberships, because we depend on modGroup group which provides blog 2
        // with group 1 as viewer and NO_GROUP user 1 as manager
        // add an external group to blog 1
        AuthenticationHelper.setInternalSystemToSecurityContext();
        String externalSystemId = LdapActivator.EXTERNAL_SYSTEM_ID_DEFAULT_LDAP;
        Group group = createGroup(GROUP_2_ALIAS, GROUP_2_NAME, externalSystemId);
        Long groupId = group.getId();
        UserAndGroupTestUtils.addUsersToGroup(group, USER_GROUP_2_ALIAS_PATTERN_PREFIX, 4,
                externalSystemId);
        UserAndGroupTestUtils.addUsersToGroup(group, USER_GROUP_1A2_ALIAS_PATTERN_PREFIX, 4,
                externalSystemId);

        AuthenticationHelper.removeAuthentication();
        AuthenticationTestUtils.setSecurityContext(UserAndGroupTestUtils.findNthUser(
                BLOG_MANAGER_ALIAS_PATTERN_PREFIX, 2));
        // assign external object and than a group for that external object
        Long blogId = getBlogManagement().findBlogByIdentifier(BLOG_2_ALIAS).getId();
        ExternalObject externalObjectTO = ExternalObject.Factory.newInstance();
        externalObjectTO.setExternalId(UUID.randomUUID().toString());
        externalObjectTO.setExternalName(UUID.randomUUID().toString());
        externalObjectTO.setExternalSystemId(testExternalSystemId);
        ServiceLocator.instance().getService(ExternalObjectManagement.class)
                .assignExternalObject(blogId, externalObjectTO);
        getBlogRightsManagement().assignEntityForExternal(blogId, groupId, BlogRole.VIEWER,
                externalObjectTO.getExternalSystemId(), externalObjectTO.getExternalId());

        assertGroupBlogAccess(blogId, groupId, BlogRole.VIEWER, false, usersInBothGroups);
    }

    /**
     * Prepare before running tests of modGroupBlogMemberTestsGroup.
     *
     * @throws Exception
     *             if setup fails
     */
    @BeforeGroups(groups = { "modGroupBlogMemberTestsGroup" })
    public void beforeModGroupBlogMemberTestsGroup() throws Exception {
        AuthenticationTestUtils.setSecurityContext(UserAndGroupTestUtils.findNthUser(
                BLOG_MANAGER_ALIAS_PATTERN_PREFIX, 2));
        Long blogId = getBlogManagement().findBlogByIdentifier(BLOG_2_ALIAS).getId();
        Long groupId1 = UserAndGroupTestUtils.findGroup(GROUP_1_ALIAS).getId();
        getBlogRightsManagement().addEntity(blogId, groupId1, BlogRole.VIEWER);
        Long groupId2 = UserAndGroupTestUtils.findGroup(GROUP_2_ALIAS).getId();
        getBlogRightsManagement().addEntity(blogId, groupId2, BlogRole.MEMBER);
        Long userId = UserAndGroupTestUtils.findNthUser(USER_NO_GROUP_ALIAS_PATTERN_PREFIX, 1)
                .getId();
        getBlogRightsManagement().addEntity(blogId, userId, BlogRole.MANAGER);
        AuthenticationHelper.removeAuthentication();
        // put a manager in security context
        AuthenticationTestUtils.setSecurityContext(UserAndGroupTestUtils.findNthUser(
                InstallerTest.TEST_MANAGER_USER_ALIAS, -1));
    }

    /**
     * Prepare before running tests of xGroupBlogMemberTestsGroup.
     */
    @BeforeGroups(groups = { "xGroupBlogMemberTestsGroup" })
    public void beforeXGroupBlogMemberTestsGroup() {
        AuthenticationTestUtils.setSecurityContext(UserAndGroupTestUtils.findNthUser(
                BLOG_MANAGER_ALIAS_PATTERN_PREFIX, 2));
    }

    /**
     * Does required preparations to run the tests.
     *
     * @throws Exception
     *             in case setup fails
     */
    @BeforeClass(dependsOnGroups = "integration-test-setup")
    public void prepareTests() throws Exception {
        // create users, groups and blogs
        UserAndGroupTestUtils.createDummyUsers(BLOG_MANAGER_ALIAS_PATTERN_PREFIX,
                BLOG_MANAGER_EMAIL_PATTERN, 2);
        createBlog(UserAndGroupTestUtils.findNthUser(BLOG_MANAGER_ALIAS_PATTERN_PREFIX, 1),
                BLOG_1_ALIAS);
        createBlog(UserAndGroupTestUtils.findNthUser(BLOG_MANAGER_ALIAS_PATTERN_PREFIX, 2),
                BLOG_2_ALIAS);

        UserAndGroupTestUtils.createDummyUsers(USER_NO_GROUP_ALIAS_PATTERN_PREFIX,
                USER_NO_GROUP_EMAIL_PATTERN, 4);
        UserAndGroupTestUtils.createDummyUsers(USER_GROUP_1_ALIAS_PATTERN_PREFIX,
                USER_GROUP_1_EMAIL_PATTERN, 4);
        UserAndGroupTestUtils.createDummyUsers(USER_GROUP_2_ALIAS_PATTERN_PREFIX,
                USER_GROUP_2_EMAIL_PATTERN, 4);
        UserAndGroupTestUtils.createDummyUsers(USER_GROUP_1A2_ALIAS_PATTERN_PREFIX,
                USER_GROUP_1A2_EMAIL_PATTERN, 4);

        UserAndGroupTestUtils.createDummyUsers(USER_NON_BLOG_MEMBER_ALIAS,
                USER_NON_BLOG_MEMBER_EMAIL, 1);

        manager = TestUtils.createRandomUser(true);
        AuthenticationTestUtils.setSecurityContext(manager);
        Group group = createGroup(GROUP_1_ALIAS, GROUP_1_NAME, null);
        UserAndGroupTestUtils.addUsersToGroup(group, USER_GROUP_1_ALIAS_PATTERN_PREFIX, 4, null);
        UserAndGroupTestUtils.addUsersToGroup(group, USER_GROUP_1A2_ALIAS_PATTERN_PREFIX, 4, null);
        Assert.assertEquals(getGroupMemberManagement().getUsersOfGroup(group.getId()).size(), 8);

        group = createGroup(GROUP_2_ALIAS, GROUP_2_NAME, null);
        UserAndGroupTestUtils.addUsersToGroup(group, USER_GROUP_2_ALIAS_PATTERN_PREFIX, 4, null);
        UserAndGroupTestUtils.addUsersToGroup(group, USER_GROUP_1A2_ALIAS_PATTERN_PREFIX, 4, null);
        Assert.assertEquals(getGroupMemberManagement().getUsersOfGroup(group.getId()).size(), 8);
        usersInBothGroups = new HashSet<Long>(4);
        for (int i = 1; i <= 4; i++) {
            User u = UserAndGroupTestUtils.findNthUser(USER_GROUP_1A2_ALIAS_PATTERN_PREFIX, i);
            usersInBothGroups.add(u.getId());
        }
        AuthenticationHelper.removeAuthentication();

        testExternalSystemId = TestUtils.createNewExternalObjectSource(true).getIdentifier();

    }

    /**
     * Tests the all-can-read access restriction.
     *
     * @throws Exception
     *             if the test fails
     */
    @Test(groups = { "allCanBlogMemberTestsGroup" }, dependsOnGroups = "modGroupBlogMemberTestsGroup")
    public void testAllCan1AllCanRead() throws Exception {
        Long blogId = getBlogManagement().findBlogByIdentifier(BLOG_2_ALIAS).getId();
        getBlogRightsManagement().setAllCanReadAllCanWrite(blogId, true, false);
        Long groupId1 = UserAndGroupTestUtils.findGroup(GROUP_1_ALIAS).getId();
        Long userId = UserAndGroupTestUtils.findNthUser(USER_NO_GROUP_ALIAS_PATTERN_PREFIX, 1)
                .getId();
        // manager role must be higher
        assertBlogAccess(blogId, userId, BlogRole.MANAGER, false, true);
        assertGroupBlogAccess(blogId, groupId1, BlogRole.VIEWER, false, null);
        // group rights must work when ignoring role
        assertGroupBlogAccess(blogId, groupId1, BlogRole.VIEWER, true, null);
        // check external group
        Long groupId2 = UserAndGroupTestUtils.findGroup(GROUP_2_ALIAS).getId();
        assertGroupBlogAccess(blogId, groupId2, BlogRole.VIEWER, false, null);
        // group rights must work when ignoring role
        assertGroupBlogAccess(blogId, groupId2, BlogRole.VIEWER, true, null);
        // check access of a non-member
        Long userId2 = UserAndGroupTestUtils.findNthUser(USER_NON_BLOG_MEMBER_ALIAS, -1).getId();
        assertBlogAccess(blogId, userId2, BlogRole.VIEWER, false, false);
        // no access if ignoring the all-can flags
        assertBlogAccess(blogId, userId2, null, true, false);
    }

    /**
     * Tests the all-can-write access restriction.
     *
     * @throws Exception
     *             if the test fails
     */
    @Test(groups = { "allCanBlogMemberTestsGroup" }, dependsOnMethods = "testAllCan1AllCanRead")
    public void testAllCan2AllCanWrite() throws Exception {
        Long blogId = getBlogManagement().findBlogByIdentifier(BLOG_2_ALIAS).getId();
        getBlogRightsManagement().setAllCanReadAllCanWrite(blogId, true, true);
        Long groupId1 = UserAndGroupTestUtils.findGroup(GROUP_1_ALIAS).getId();
        Long userId = UserAndGroupTestUtils.findNthUser(USER_NO_GROUP_ALIAS_PATTERN_PREFIX, 1)
                .getId();
        // manager role must be higher
        assertBlogAccess(blogId, userId, BlogRole.MANAGER, false, true);
        assertGroupBlogAccess(blogId, groupId1, BlogRole.MEMBER, false, null);
        // group rights must work when ignoring role
        assertGroupBlogAccess(blogId, groupId1, BlogRole.VIEWER, true, null);
        // check access for the external group
        Long groupId2 = UserAndGroupTestUtils.findGroup(GROUP_2_ALIAS).getId();
        assertGroupBlogAccess(blogId, groupId2, BlogRole.MEMBER, false, null);
        // group rights must work when ignoring role
        assertGroupBlogAccess(blogId, groupId2, BlogRole.VIEWER, true, null);
        // check access of a non-member
        Long userId2 = UserAndGroupTestUtils.findNthUser(USER_NON_BLOG_MEMBER_ALIAS, -1).getId();
        assertBlogAccess(blogId, userId2, BlogRole.MEMBER, false, false);
        // no access if ignoring the all-can flags
        assertBlogAccess(blogId, userId2, null, true, false);
        // down grade role of member
        getBlogRightsManagement().changeRoleOfMemberByEntityId(blogId, userId, BlogRole.VIEWER);
        assertBlogAccess(blogId, userId, BlogRole.MEMBER, false, true);
        assertBlogAccess(blogId, userId, BlogRole.VIEWER, true, true);
    }

    /**
     * Tests that deleting blogs with assigned groups, single users and external groups works
     * flawlessly. The test assumes that the other tests added some users, a normal and an external
     * group to the blogs.
     *
     * @throws Exception
     *             in case setup fails
     */
    @Test(dependsOnGroups = "allCanBlogMemberTestsGroup")
    public void testDeleteBlogsWithAssignedMembers() throws Exception {
        AuthenticationTestUtils.setSecurityContext(UserAndGroupTestUtils.findNthUser(
                BLOG_MANAGER_ALIAS_PATTERN_PREFIX, 1));
        Blog blog1 = getBlogManagement().findBlogByIdentifier(BLOG_1_ALIAS);
        getBlogManagement().deleteBlog(blog1.getId(), null);
        blog1 = getBlogManagement().findBlogByIdentifier(BLOG_1_ALIAS);
        Assert.assertNull(blog1, "The was not deleted");
        AuthenticationHelper.removeAuthentication();
        AuthenticationTestUtils.setSecurityContext(UserAndGroupTestUtils.findNthUser(
                BLOG_MANAGER_ALIAS_PATTERN_PREFIX, 2));
        Blog blog2 = getBlogManagement().findBlogByIdentifier(BLOG_2_ALIAS);
        Long groupId1 = UserAndGroupTestUtils.findGroup(GROUP_1_ALIAS).getId();
        // assert that the preconditions for test are met
        BlogRole groupRolePrecondition = getBlogRightsManagement().getRoleOfEntity(blog2.getId(),
                groupId1, true);
        Assert.assertNotNull(groupRolePrecondition, "Group 1 is not member of blog2. This "
                + "membership is required to have an effective "
                + "test. You might need to add the group"
                + " before running the test if the test order changed");
        Long groupId2 = UserAndGroupTestUtils.findGroup(GROUP_2_ALIAS).getId();
        groupRolePrecondition = getBlogRightsManagement().getRoleOfEntity(blog2.getId(), groupId2,
                true);
        Assert.assertNotNull(groupRolePrecondition, "Group 2 is not member of blog2. This "
                + "membership is required to have an effective "
                + "test. You might need to add the group"
                + " before running the test if the test order changed");
        getBlogManagement().deleteBlog(blog2.getId(), null);
        blog2 = getBlogManagement().findBlogByIdentifier(BLOG_2_ALIAS);
        Assert.assertNull(blog2, "The blog was not deleted");
        // assert that the groups still exist
        Assert.assertNotNull(UserAndGroupTestUtils.findGroup(GROUP_1_ALIAS),
                "Group 1 does not exist anymore.");
        Assert.assertNotNull(UserAndGroupTestUtils.findGroup(GROUP_2_ALIAS),
                "Group 2 does not exist anymore.");
        AuthenticationHelper.removeAuthentication();
    }

    /**
     * Tests that there are no interferences between members added through external systems and
     * members added via the FE.
     *
     * @throws Exception
     *             in case the test fails
     */
    @Test(groups = { "externalBlogMemberTestsGroup" }, dependsOnGroups = { "xGroupBlogMemberTestsGroup" })
    public void testExternal1AddGroup() throws Exception {
        AuthenticationTestUtils.setSecurityContext(manager);
        Long blogId = getBlogManagement().findBlogByIdentifierWithoutAuthorizationCheck(
                BLOG_2_ALIAS).getId();
        Long groupId2 = UserAndGroupTestUtils.findGroup(GROUP_2_ALIAS).getId();
        getBlogRightsManagement().addEntityForExternalTrusted(blogId, groupId2, BlogRole.MEMBER,
                testExternalSystemId);
        assertGroupBlogAccess(blogId, groupId2, BlogRole.MEMBER, false, null);
        // add group again normal mode
        AuthenticationTestUtils.setSecurityContext(UserAndGroupTestUtils.findNthUser(
                BLOG_MANAGER_ALIAS_PATTERN_PREFIX, 2));
        getBlogRightsManagement().addEntity(blogId, groupId2, BlogRole.VIEWER);
        // still member role cause external is higher
        assertGroupBlogAccess(blogId, groupId2, BlogRole.MEMBER, false, null);
        // add one user manually
        Long userId = UserAndGroupTestUtils.findNthUser(USER_GROUP_1A2_ALIAS_PATTERN_PREFIX, 1)
                .getId();
        getBlogRightsManagement().addEntity(blogId, userId, BlogRole.MANAGER);
        assertBlogAccess(blogId, userId, BlogRole.MANAGER, false, true);
        Set<Long> usersToSkip = new HashSet<Long>(1);
        usersToSkip.add(userId);
        assertGroupBlogAccess(blogId, groupId2, BlogRole.MEMBER, false, usersToSkip);
        AuthenticationHelper.removeAuthentication();
    }

    /**
     * Tests that there are no interferences between members added through external systems and
     * members added via the FE when changing a role of the group added by the external system.
     *
     * @throws Exception
     *             in case the test fails
     */
    @Test(groups = { "externalBlogMemberTestsGroup" }, dependsOnMethods = { "testExternal1AddGroup" })
    public void testExternal2ChangeRole() throws Exception {
        AuthenticationTestUtils.setSecurityContext(TestUtils.createRandomUser(true));
        Long blogId = getBlogManagement().findBlogByIdentifierWithoutAuthorizationCheck(
                BLOG_2_ALIAS).getId();
        Long groupId2 = UserAndGroupTestUtils.findGroup(GROUP_2_ALIAS).getId();
        getBlogRightsManagement().changeRoleOfMemberByEntityIdForExternal(blogId, groupId2,
                BlogRole.MANAGER, testExternalSystemId);
        Long userId = UserAndGroupTestUtils.findNthUser(USER_GROUP_1A2_ALIAS_PATTERN_PREFIX, 1)
                .getId();
        Set<Long> usersToSkip = new HashSet<Long>(1);
        usersToSkip.add(userId);
        assertGroupBlogAccess(blogId, groupId2, BlogRole.MANAGER, false, usersToSkip);
        // user must be unaffected
        assertBlogAccess(blogId, userId, BlogRole.MANAGER, false, true);
    }

    /**
     * Tests that there are no interferences between members added through external systems and
     * members added via the FE when removing the group added by the external system.
     *
     * @throws Exception
     *             in case the test fails
     */
    @Test(groups = { "externalBlogMemberTestsGroup" }, dependsOnMethods = { "testExternal2ChangeRole" })
    public void testExternal3RemoveGroup() throws Exception {
        AuthenticationTestUtils.setSecurityContext(TestUtils.createRandomUser(true));
        Long blogId = getBlogManagement().findBlogByIdentifierWithoutAuthorizationCheck(
                BLOG_2_ALIAS).getId();
        Long groupId2 = UserAndGroupTestUtils.findGroup(GROUP_2_ALIAS).getId();
        getBlogRightsManagement().removeMemberByEntityIdForExternal(blogId, groupId2,
                testExternalSystemId);
        Long userId = UserAndGroupTestUtils.findNthUser(USER_GROUP_1A2_ALIAS_PATTERN_PREFIX, 1)
                .getId();
        Set<Long> usersToSkip = new HashSet<Long>(1);
        usersToSkip.add(userId);
        assertGroupBlogAccess(blogId, groupId2, BlogRole.VIEWER, false, usersToSkip);
        // user must be unaffected
        assertBlogAccess(blogId, userId, BlogRole.MANAGER, false, true);
    }

    /**
     * Tests removing a member.
     *
     * @throws Exception
     *             if the test fails
     */
    @Test(groups = { "groupBlogMemberTestsGroup" }, dependsOnGroups = { "userBlogMemberTestsGroup" })
    public void testGroup1AddGroupToBlog() throws Exception {
        AuthenticationTestUtils.setSecurityContext(UserAndGroupTestUtils.findNthUser(
                BLOG_MANAGER_ALIAS_PATTERN_PREFIX, 2));
        Group group = UserAndGroupTestUtils.findGroup(GROUP_1_ALIAS);
        Long blogId = getBlogManagement().findBlogByIdentifier(BLOG_2_ALIAS).getId();
        getBlogRightsManagement().addEntity(blogId, group.getId(), BlogRole.MEMBER);
        assertGroupBlogAccess(blogId, group.getId(), BlogRole.MEMBER, false, null);
    }

    /**
     * Tests changing the role of a group.
     *
     * @throws Exception
     *             if the test fails
     */
    @Test(groups = { "groupBlogMemberTestsGroup" }, dependsOnMethods = { "testGroup1AddGroupToBlog" })
    public void testGroup2ChangeRole() throws Exception {
        AuthenticationTestUtils.setSecurityContext(UserAndGroupTestUtils.findNthUser(
                BLOG_MANAGER_ALIAS_PATTERN_PREFIX, 2));
        Group group = UserAndGroupTestUtils.findGroup(GROUP_1_ALIAS);
        Long blogId = getBlogManagement().findBlogByIdentifier(BLOG_2_ALIAS).getId();
        getBlogRightsManagement().changeRoleOfMemberByEntityId(blogId, group.getId(),
                BlogRole.VIEWER);
        assertGroupBlogAccess(blogId, group.getId(), BlogRole.VIEWER, false, null);
    }

    /**
     * Tests manually adding a user that is already blog member through group member ship.
     *
     * @throws Exception
     *             if the test fails
     */
    @Test(groups = { "groupBlogMemberTestsGroup" }, dependsOnMethods = { "testGroup2ChangeRole" })
    public void testGroup3AddGroupMemberToBlog() throws Exception {
        Long userId = UserAndGroupTestUtils.findNthUser(USER_GROUP_1_ALIAS_PATTERN_PREFIX, 1)
                .getId();
        AuthenticationTestUtils.setSecurityContext(UserAndGroupTestUtils.findNthUser(
                BLOG_MANAGER_ALIAS_PATTERN_PREFIX, 2));
        Long blogId = getBlogManagement().findBlogByIdentifier(BLOG_2_ALIAS).getId();
        getBlogRightsManagement().addEntity(blogId, userId, BlogRole.MEMBER);
        // must have higher right and must be direct member
        assertBlogAccess(blogId, userId, BlogRole.MEMBER, false, true);
        Group group = UserAndGroupTestUtils.findGroup(GROUP_1_ALIAS);
        // other blog members must be untouched
        HashSet<Long> membersToSkip = new HashSet<Long>(1);
        membersToSkip.add(userId);
        assertGroupBlogAccess(blogId, group.getId(), BlogRole.VIEWER, false, membersToSkip);
    }

    /**
     * Tests changing the role of a group member.
     *
     * @throws Exception
     *             if the test fails
     */
    @Test(groups = { "groupBlogMemberTestsGroup" }, dependsOnMethods = { "testGroup3AddGroupMemberToBlog" })
    public void testGroup4ChangeRoleOfGroupMember() throws Exception {
        AuthenticationTestUtils.setSecurityContext(UserAndGroupTestUtils.findNthUser(
                BLOG_MANAGER_ALIAS_PATTERN_PREFIX, 2));
        Long blogId = getBlogManagement().findBlogByIdentifier(BLOG_2_ALIAS).getId();
        Long userId = UserAndGroupTestUtils.findNthUser(USER_GROUP_1_ALIAS_PATTERN_PREFIX, 1)
                .getId();
        getBlogRightsManagement().changeRoleOfMemberByEntityId(blogId, userId, BlogRole.VIEWER);
        // must have new right and must be direct member
        assertBlogAccess(blogId, userId, BlogRole.VIEWER, false, true);
        Group group = UserAndGroupTestUtils.findGroup(GROUP_1_ALIAS);
        // other blog members must be untouched
        HashSet<Long> membersToSkip = new HashSet<Long>(1);
        membersToSkip.add(userId);
        assertGroupBlogAccess(blogId, group.getId(), BlogRole.VIEWER, false, membersToSkip);
    }

    /**
     * Tests removing a previously added group member from a blog.
     *
     * @throws Exception
     *             if the test fails
     */
    @Test(groups = { "groupBlogMemberTestsGroup" }, dependsOnMethods = { "testGroup4ChangeRoleOfGroupMember" })
    public void testGroup5RemoveGroupMemberFromBlog() throws Exception {
        AuthenticationTestUtils.setSecurityContext(UserAndGroupTestUtils.findNthUser(
                BLOG_MANAGER_ALIAS_PATTERN_PREFIX, 2));
        Long blogId = getBlogManagement().findBlogByIdentifier(BLOG_2_ALIAS).getId();
        Long userId = UserAndGroupTestUtils.findNthUser(USER_GROUP_1_ALIAS_PATTERN_PREFIX, 1)
                .getId();
        getBlogRightsManagement().removeMemberByEntityId(blogId, userId);
        Group group = UserAndGroupTestUtils.findGroup(GROUP_1_ALIAS);
        // user must still be in group
        Assert.assertTrue(getGroupMemberManagement().containsUserDirectly(group.getId(), userId));
        // group member rights must be untouched -> skip none
        assertGroupBlogAccess(blogId, group.getId(), BlogRole.VIEWER, false, null);
    }

    /**
     * Tests that the last user that is manager cannot be removed although there are managers that
     * were added to the blog because of their group membership.
     *
     * @throws Exception
     *             if the test fails
     */
    @Test(groups = { "groupBlogMemberTestsGroup" }, dependsOnMethods = { "testGroup5RemoveGroupMemberFromBlog" })
    public void testGroup6RemoveLastUserManagerWithGroupManager() throws Exception {
        AuthenticationTestUtils.setSecurityContext(UserAndGroupTestUtils.findNthUser(
                BLOG_MANAGER_ALIAS_PATTERN_PREFIX, 2));
        Long blogId = getBlogManagement().findBlogByIdentifier(BLOG_2_ALIAS).getId();
        Group group = UserAndGroupTestUtils.findGroup(GROUP_1_ALIAS);
        getBlogRightsManagement().changeRoleOfMemberByEntityId(blogId, group.getId(),
                BlogRole.MANAGER);
        assertGroupBlogAccess(blogId, group.getId(), BlogRole.MANAGER, false, null);
        try {
            User blog2Manager = UserAndGroupTestUtils.findNthUser(
                    BLOG_MANAGER_ALIAS_PATTERN_PREFIX, 2);
            getBlogRightsManagement().removeMemberByEntityId(blogId, blog2Manager.getId());
        } catch (NoBlogManagerLeftException e) {
            return;
        }
        Assert.fail("Expected exception NoBlogManagerLeftException not thrown.");
    }

    /**
     * Tests removing a group from a blog.
     *
     * @throws Exception
     *             if the test fails
     */
    @Test(groups = { "groupBlogMemberTestsGroup" }, dependsOnMethods = { "testGroup6RemoveLastUserManagerWithGroupManager" })
    public void testGroup7RemoveGroupFromBlog() throws Exception {
        AuthenticationTestUtils.setSecurityContext(UserAndGroupTestUtils.findNthUser(
                BLOG_MANAGER_ALIAS_PATTERN_PREFIX, 2));
        Long blogId = getBlogManagement().findBlogByIdentifier(BLOG_2_ALIAS).getId();
        // first add the user again
        Long userId = UserAndGroupTestUtils.findNthUser(USER_GROUP_1_ALIAS_PATTERN_PREFIX, 1)
                .getId();
        getBlogRightsManagement().addEntity(blogId, userId, BlogRole.MEMBER);
        // user must have higher role of group and direct member
        assertBlogAccess(blogId, userId, BlogRole.MANAGER, false, true);
        Long groupId = UserAndGroupTestUtils.findGroup(GROUP_1_ALIAS).getId();
        getBlogRightsManagement().removeMemberByEntityId(blogId, groupId);
        Set<Long> membersToSkip = new HashSet<Long>(1);
        membersToSkip.add(userId);
        // ensure group and members are is no longer member (skip user)
        assertGroupBlogAccess(blogId, groupId, null, false, membersToSkip);
        // user rights must be untouched
        assertBlogAccess(blogId, userId, BlogRole.MEMBER, false, true);
    }

    /**
     * Tests adding two groups with different roles. The groups share some members.
     *
     * @throws Exception
     *             if the test fails
     */
    @Test(groups = { "xGroupBlogMemberTestsGroup" }, dependsOnGroups = { "groupBlogMemberTestsGroup" })
    public void testIntersectedGroups1AddGroups() throws Exception {
        Long blogId = getBlogManagement().findBlogByIdentifier(BLOG_2_ALIAS).getId();
        Long groupId1 = UserAndGroupTestUtils.findGroup(GROUP_1_ALIAS).getId();

        getBlogRightsManagement().addEntity(blogId, groupId1, BlogRole.MEMBER);
        assertGroupBlogAccess(blogId, groupId1, BlogRole.MEMBER, false, null);

        // add second group which has members from first group
        Long groupId2 = UserAndGroupTestUtils.findGroup(GROUP_2_ALIAS).getId();
        getBlogRightsManagement().addEntity(blogId, groupId2, BlogRole.VIEWER);
        // the users with alias USER_GROUP_1A2_ALIAS_PATTERN_PREFIX* are member of both groups, so
        // skip them cause they must still have the higher role
        for (Long uid : usersInBothGroups) {
            // assert that the skipped users still have the higher role (but not direct member)
            assertBlogAccess(blogId, uid, BlogRole.MEMBER, false, false);
        }
        // assert role for other group members
        assertGroupBlogAccess(blogId, groupId2, BlogRole.VIEWER, false, usersInBothGroups);
    }

    /**
     * Tests changing the role of one of the groups.
     *
     * @throws Exception
     *             if the test fails
     */
    @Test(groups = { "xGroupBlogMemberTestsGroup" }, dependsOnMethods = { "testIntersectedGroups1AddGroups" })
    public void testIntersectedGroups2ChangeRoleOfGroup() throws Exception {
        Long blogId = getBlogManagement().findBlogByIdentifier(BLOG_2_ALIAS).getId();
        Long groupId2 = UserAndGroupTestUtils.findGroup(GROUP_2_ALIAS).getId();
        getBlogRightsManagement().changeRoleOfMemberByEntityId(blogId, groupId2, BlogRole.MANAGER);
        assertGroupBlogAccess(blogId, groupId2, BlogRole.MANAGER, false, null);

        Long groupId1 = UserAndGroupTestUtils.findGroup(GROUP_1_ALIAS).getId();
        assertGroupBlogAccess(blogId, groupId1, BlogRole.MEMBER, false, usersInBothGroups);
    }

    /**
     * Tests that adding a group to different blogs does not result in interferences.
     *
     * @throws Exception
     *             if the test fails
     */
    @Test(groups = { "xGroupBlogMemberTestsGroup" }, dependsOnMethods = { "testIntersectedGroups2ChangeRoleOfGroup" })
    public void testIntersectedGroups3AddGroupToAnotherBlog() throws Exception {
        Long blogId1 = getBlogManagement().findBlogByIdentifierWithoutAuthorizationCheck(
                BLOG_1_ALIAS).getId();
        // add group 2 to the other blog (but use the assign function, to test this one too)
        AuthenticationHelper.removeAuthentication();
        AuthenticationTestUtils.setSecurityContext(UserAndGroupTestUtils.findNthUser(
                BLOG_MANAGER_ALIAS_PATTERN_PREFIX, 1));
        Long groupId2 = UserAndGroupTestUtils.findGroup(GROUP_2_ALIAS).getId();
        getBlogRightsManagement().assignEntity(blogId1, groupId2, BlogRole.VIEWER);
        assertGroupBlogAccess(blogId1, groupId2, BlogRole.VIEWER, false, null);
        // make sure other blog is not effected
        Long blogId2 = getBlogManagement().findBlogByIdentifierWithoutAuthorizationCheck(
                BLOG_2_ALIAS).getId();
        assertGroupBlogAccess(blogId2, groupId2, BlogRole.MANAGER, false, null);
        // just in case: also check other group
        Long groupId1 = UserAndGroupTestUtils.findGroup(GROUP_1_ALIAS).getId();
        assertGroupBlogAccess(blogId2, groupId1, BlogRole.MEMBER, false, usersInBothGroups);
        // restore old prefix
        AuthenticationHelper.removeAuthentication();
        AuthenticationTestUtils.setSecurityContext(UserAndGroupTestUtils.findNthUser(
                BLOG_MANAGER_ALIAS_PATTERN_PREFIX, 2));
    }

    /**
     * Tests adding a user who is already a blog member due to his membership in both other groups.
     *
     * @throws Exception
     *             if the test fails
     */
    @Test(groups = { "xGroupBlogMemberTestsGroup" }, dependsOnMethods = { "testIntersectedGroups3AddGroupToAnotherBlog" })
    public void testIntersectedGroups4AddUserOfBothGroups() throws Exception {
        Long blogId = getBlogManagement().findBlogByIdentifier(BLOG_2_ALIAS).getId();
        Long userId = UserAndGroupTestUtils.findNthUser(USER_GROUP_1A2_ALIAS_PATTERN_PREFIX, 1)
                .getId();
        getBlogRightsManagement().addEntity(blogId, userId, BlogRole.VIEWER);
        // assert that user still has highest role (MANAGER - due to group 2) and is direct member
        assertBlogAccess(blogId, userId, BlogRole.MANAGER, false, true);
        // group access must be untouched
        Long groupId2 = UserAndGroupTestUtils.findGroup(GROUP_2_ALIAS).getId();
        // skip user because he is now direct member
        Set<Long> usersToSkip = new HashSet<Long>();
        usersToSkip.add(userId);
        assertGroupBlogAccess(blogId, groupId2, BlogRole.MANAGER, false, usersToSkip);
        Long groupId1 = UserAndGroupTestUtils.findGroup(GROUP_1_ALIAS).getId();
        assertGroupBlogAccess(blogId, groupId1, BlogRole.MEMBER, false, usersInBothGroups);
    }

    /**
     * Tests removing one of the groups.
     *
     * @throws Exception
     *             if the test fails
     */
    @Test(groups = { "xGroupBlogMemberTestsGroup" }, dependsOnMethods = { "testIntersectedGroups4AddUserOfBothGroups" })
    public void testIntersectedGroups5RemoveGroup() throws Exception {
        Long blogId = getBlogManagement().findBlogByIdentifier(BLOG_2_ALIAS).getId();
        // also test that the nolastblogmanagerleft exception works
        Long managerId = UserAndGroupTestUtils.findNthUser(BLOG_MANAGER_ALIAS_PATTERN_PREFIX, 2)
                .getId();
        boolean exceptionThrown = false;
        try {
            getBlogRightsManagement().removeMemberByEntityId(blogId, managerId);
        } catch (NoBlogManagerLeftException e) {
            exceptionThrown = true;
        }
        if (!exceptionThrown) {
            Assert.fail("Expected exception NoBlogManagerLeftException not thrown.");
        }
        Long groupId2 = UserAndGroupTestUtils.findGroup(GROUP_2_ALIAS).getId();
        getBlogRightsManagement().removeMemberByEntityId(blogId, groupId2);
        assertGroupBlogAccess(blogId, groupId2, null, false, usersInBothGroups);
        // check access of other group
        Long userId = UserAndGroupTestUtils.findNthUser(USER_GROUP_1A2_ALIAS_PATTERN_PREFIX, 1)
                .getId();
        Set<Long> usersToSkip = new HashSet<Long>();
        usersToSkip.add(userId);
        Long groupId1 = UserAndGroupTestUtils.findGroup(GROUP_1_ALIAS).getId();
        assertGroupBlogAccess(blogId, groupId1, BlogRole.MEMBER, false, usersToSkip);
        // check access of single user added in previous test (must have group role and has to be
        // direct member)
        assertBlogAccess(blogId, userId, BlogRole.MEMBER, false, true);
        // test that access roles of other blog are untouched
        Long blogId1 = getBlogManagement().findBlogByIdentifierWithoutAuthorizationCheck(
                BLOG_1_ALIAS).getId();
        assertGroupBlogAccess(blogId1, groupId2, BlogRole.VIEWER, false, null);
    }

    /**
     * Tests that the user rights are correctly updated when the user is added to a group that is a
     * blog member.
     *
     * @throws Exception
     *             if the test fails
     */
    @Test(groups = { "modGroupBlogMemberTestsGroup" }, dependsOnGroups = { "externalBlogMemberTestsGroup" })
    public void testModGroup1AddUserToGroup() throws Exception {
        Long groupId2 = UserAndGroupTestUtils.findGroup(GROUP_2_ALIAS).getId();
        // take second because first is already member of blog
        Long userId = UserAndGroupTestUtils.findNthUser(USER_NO_GROUP_ALIAS_PATTERN_PREFIX, 2)
                .getId();
        getGroupMemberManagement().addUser(groupId2, userId);
        // assert that user has the correct role
        Long blogId = getBlogManagement().findBlogByIdentifierWithoutAuthorizationCheck(
                BLOG_2_ALIAS).getId();
        assertBlogAccess(blogId, userId, BlogRole.MEMBER, false, false);
        // group access must still be ok
        assertGroupBlogAccess(blogId, groupId2, BlogRole.MEMBER, false, usersInBothGroups);
        // other group must be unchanged
        Long groupId1 = UserAndGroupTestUtils.findGroup(GROUP_1_ALIAS).getId();
        assertGroupBlogAccess(blogId, groupId1, BlogRole.VIEWER, false, usersInBothGroups);
    }

    /**
     * Tests that the user rights are correctly updated when the user is removed from the granting
     * group.
     *
     * @throws Exception
     *             if the test fails
     */
    @Test(groups = { "modGroupBlogMemberTestsGroup" }, dependsOnMethods = { "testModGroup1AddUserToGroup" })
    public void testModGroup2RemoveUserFromGroup() throws Exception {
        Long groupId2 = UserAndGroupTestUtils.findGroup(GROUP_2_ALIAS).getId();
        // remove previously added user
        Long userId = UserAndGroupTestUtils.findNthUser(USER_NO_GROUP_ALIAS_PATTERN_PREFIX, 2)
                .getId();
        getGroupMemberManagement().removeEntityFromGroup(groupId2, userId);
        // assert that user has no role
        Long blogId = getBlogManagement().findBlogByIdentifierWithoutAuthorizationCheck(
                BLOG_2_ALIAS).getId();
        assertBlogAccess(blogId, userId, null, false, false);
        // group access must still be ok
        assertGroupBlogAccess(blogId, groupId2, BlogRole.MEMBER, false, usersInBothGroups);
        // other group must be unchanged
        Long groupId1 = UserAndGroupTestUtils.findGroup(GROUP_1_ALIAS).getId();
        assertGroupBlogAccess(blogId, groupId1, BlogRole.VIEWER, false, usersInBothGroups);
    }

    /**
     * Tests removing a user from a group who is also a member in another group.
     *
     * @throws Exception
     *             if the test fails
     */
    @Test(groups = { "modGroupBlogMemberTestsGroup" }, dependsOnMethods = { "testModGroup2RemoveUserFromGroup" })
    public void testModGroup3RemoveGroupSharedUserFromGroup() throws Exception {
        Long groupId2 = UserAndGroupTestUtils.findGroup(GROUP_2_ALIAS).getId();
        // remove one of the users that is in both groups
        Long userId = UserAndGroupTestUtils.findNthUser(USER_GROUP_1A2_ALIAS_PATTERN_PREFIX, 1)
                .getId();
        getGroupMemberManagement().removeEntityFromGroup(groupId2, userId);
        // assert that user has the access of the other group
        Long blogId = getBlogManagement().findBlogByIdentifierWithoutAuthorizationCheck(
                BLOG_2_ALIAS).getId();
        assertBlogAccess(blogId, userId, BlogRole.VIEWER, false, false);
        // remove removed user from users to skip
        Assert.assertTrue(usersInBothGroups.remove(userId), "removed wrong user??");
        // group access must still be ok
        assertGroupBlogAccess(blogId, groupId2, BlogRole.MEMBER, false, usersInBothGroups);
        // other group must be unchanged
        Long groupId1 = UserAndGroupTestUtils.findGroup(GROUP_1_ALIAS).getId();
        assertGroupBlogAccess(blogId, groupId1, BlogRole.VIEWER, false, usersInBothGroups);
    }

    /**
     * Tests adding a user to the group who is already member of the blog.
     *
     * @throws Exception
     *             if the test fails
     */
    @Test(groups = { "modGroupBlogMemberTestsGroup" }, dependsOnMethods = { "testModGroup3RemoveGroupSharedUserFromGroup" })
    public void testModGroup4AddBlogMemberToGroup() throws Exception {
        Long groupId2 = UserAndGroupTestUtils.findGroup(GROUP_2_ALIAS).getId();
        // add the user that was added to blog manually
        Long userId = UserAndGroupTestUtils.findNthUser(USER_NO_GROUP_ALIAS_PATTERN_PREFIX, 1)
                .getId();
        getGroupMemberManagement().addUser(groupId2, userId);
        // assert that user has the access of the direct assignment (cause it's higher)
        Long blogId = getBlogManagement().findBlogByIdentifierWithoutAuthorizationCheck(
                BLOG_2_ALIAS).getId();
        assertBlogAccess(blogId, userId, BlogRole.MANAGER, false, true);
        Set<Long> usersToSkip = new HashSet<Long>();
        usersToSkip.addAll(usersInBothGroups);
        usersToSkip.add(userId);
        // group access must still be ok
        assertGroupBlogAccess(blogId, groupId2, BlogRole.MEMBER, false, usersToSkip);
        // other group must be unchanged
        Long groupId1 = UserAndGroupTestUtils.findGroup(GROUP_1_ALIAS).getId();
        assertGroupBlogAccess(blogId, groupId1, BlogRole.VIEWER, false, usersInBothGroups);
    }

    /**
     * Tests that blog rights are correctly updated when removing a user from a group that is also a
     * direct member of the blog.
     *
     * @throws Exception
     *             if the test fails
     */
    @Test(groups = { "modGroupBlogMemberTestsGroup" }, dependsOnMethods = { "testModGroup4AddBlogMemberToGroup" })
    public void testModGroup5RemoveBlogMemberFromGroup() throws Exception {
        Long groupId2 = UserAndGroupTestUtils.findGroup(GROUP_2_ALIAS).getId();
        // remove the user that was added to blog manually
        Long userId = UserAndGroupTestUtils.findNthUser(USER_NO_GROUP_ALIAS_PATTERN_PREFIX, 1)
                .getId();
        getGroupMemberManagement().removeEntityFromGroup(groupId2, userId);
        // assert that the user still has the access of the direct assignment
        Long blogId = getBlogManagement().findBlogByIdentifierWithoutAuthorizationCheck(
                BLOG_2_ALIAS).getId();
        assertBlogAccess(blogId, userId, BlogRole.MANAGER, false, true);
        // group access must still be ok
        assertGroupBlogAccess(blogId, groupId2, BlogRole.MEMBER, false, usersInBothGroups);
        // other group must be unchanged
        Long groupId1 = UserAndGroupTestUtils.findGroup(GROUP_1_ALIAS).getId();
        assertGroupBlogAccess(blogId, groupId1, BlogRole.VIEWER, false, usersInBothGroups);
    }

    /**
     * Tests that blog rights are correctly updated when deleting a group.
     *
     * @throws Exception
     *             if the test fails
     */
    @Test(groups = { "modGroupBlogMemberTestsGroup" }, dependsOnMethods = { "testModGroup5RemoveBlogMemberFromGroup" })
    public void testModGroup6RemoveGroup() throws Exception {
        Long groupId2 = UserAndGroupTestUtils.findGroup(GROUP_2_ALIAS).getId();
        // add the user that was added to blog manually to complicate things
        Long userId = UserAndGroupTestUtils.findNthUser(USER_NO_GROUP_ALIAS_PATTERN_PREFIX, 1)
                .getId();
        getGroupMemberManagement().addUser(groupId2, userId);
        // now remove the group
        ServiceLocator.instance().getService(UserGroupManagement.class).deleteGroup(groupId2);
        // assert that the user is still member of the blog
        Long blogId = getBlogManagement().findBlogByIdentifierWithoutAuthorizationCheck(
                BLOG_2_ALIAS).getId();
        assertBlogAccess(blogId, userId, BlogRole.MANAGER, false, true);
        // assert that group was removed correctly
        BlogRole role = getBlogRightsManagement().getRoleOfEntity(blogId, groupId2, false);
        Assert.assertNull(role, "group not removed");
        // check the unique members of the group
        for (int i = 1; i <= 4; i++) {
            User user = UserAndGroupTestUtils.findNthUser(USER_GROUP_2_ALIAS_PATTERN_PREFIX, i);
            assertBlogAccess(blogId, user.getId(), null, false, false);
        }
        // other group must be unchanged
        Long groupId1 = UserAndGroupTestUtils.findGroup(GROUP_1_ALIAS).getId();
        assertGroupBlogAccess(blogId, groupId1, BlogRole.VIEWER, false, null);
    }

    /**
     * Tests that blog rights are correctly updated when deleting an external group.
     *
     * @throws Exception
     *             if the test fails
     */
    @Test(groups = { "modGroupBlogMemberTestsGroup" }, dependsOnMethods = { "testModGroup6RemoveGroup" })
    public void testModGroup7RemoveExternalGroup() throws Exception {
        // create group2 as external system group
        String externalSystemId = LdapActivator.EXTERNAL_SYSTEM_ID_DEFAULT_LDAP;
        SecurityContext currentContext = AuthenticationHelper.setInternalSystemToSecurityContext();
        Group group = createGroup(GROUP_2_ALIAS, GROUP_2_NAME, externalSystemId);
        Long groupId = group.getId();
        UserAndGroupTestUtils.addUsersToGroup(group, USER_GROUP_2_ALIAS_PATTERN_PREFIX, 4,
                externalSystemId);
        UserAndGroupTestUtils.addUsersToGroup(group, USER_GROUP_1A2_ALIAS_PATTERN_PREFIX, 4,
                externalSystemId);
        AuthenticationHelper.setSecurityContext(currentContext);
        Long blogId = getBlogManagement().findBlogByIdentifierWithoutAuthorizationCheck(
                BLOG_2_ALIAS).getId();
        getBlogRightsManagement().addEntityForExternalTrusted(blogId, groupId, BlogRole.MEMBER,
                testExternalSystemId);
        // skip none cause the group has higher role than group1
        assertGroupBlogAccess(blogId, groupId, BlogRole.MEMBER, false, null);
        // remove the external group
        AuthenticationHelper.setInternalSystemToSecurityContext();
        ServiceLocator.instance().getService(UserGroupManagement.class)
                .deleteExternalGroup(groupId, externalSystemId);
        AuthenticationHelper.setSecurityContext(currentContext);
        // assert that group was removed correctly
        BlogRole role = getBlogRightsManagement().getRoleOfEntity(blogId, groupId, false);
        Assert.assertNull(role, "group not removed");
        // check the unique members of the group
        for (int i = 1; i <= 4; i++) {
            User user = UserAndGroupTestUtils.findNthUser(USER_GROUP_2_ALIAS_PATTERN_PREFIX, i);
            assertBlogAccess(blogId, user.getId(), null, false, false);
        }
        // other group must be unchanged
        Long groupId1 = UserAndGroupTestUtils.findGroup(GROUP_1_ALIAS).getId();
        assertGroupBlogAccess(blogId, groupId1, BlogRole.VIEWER, false, null);
    }

    /**
     * Tests that non blog members do not have access to a blog.
     *
     * @throws Exception
     *             if the test fails
     */
    @Test(groups = { "userBlogMemberTestsGroup" })
    public void testNonMemberAccess() throws Exception {
        Long userId = UserAndGroupTestUtils.findNthUser(USER_NON_BLOG_MEMBER_ALIAS, -1).getId();
        Long blogId = getBlogManagement().findBlogByIdentifierWithoutAuthorizationCheck(
                BLOG_1_ALIAS).getId();
        assertBlogAccess(blogId, userId, null, false, false);
    }

    /**
     * Tests adding a user to the blog.
     *
     * @throws Exception
     *             if the test fails
     */
    @Test(groups = { "userBlogMemberTestsGroup" })
    public void testUser1AddUserToBlog() throws Exception {
        AuthenticationTestUtils.setSecurityContext(UserAndGroupTestUtils.findNthUser(
                BLOG_MANAGER_ALIAS_PATTERN_PREFIX, 1));
        Long userId = UserAndGroupTestUtils.findNthUser(USER_NO_GROUP_ALIAS_PATTERN_PREFIX, 1)
                .getId();
        Long blogId = getBlogManagement().findBlogByIdentifier(BLOG_1_ALIAS).getId();
        getBlogRightsManagement().addEntity(blogId, userId, BlogRole.MEMBER);
        assertBlogAccess(blogId, userId, BlogRole.MEMBER, false, true);
    }

    /**
     * Tests changing the role of a member. Also tests that you can change the role of a manager if
     * you are not the last manager.
     *
     * @throws Exception
     *             if the test fails
     */
    @Test(groups = { "userBlogMemberTestsGroup" }, dependsOnMethods = { "testUser1AddUserToBlog" })
    public void testUser2ChangeRoleOfUser() throws Exception {
        AuthenticationTestUtils.setSecurityContext(UserAndGroupTestUtils.findNthUser(
                BLOG_MANAGER_ALIAS_PATTERN_PREFIX, 1));
        Long userId = UserAndGroupTestUtils.findNthUser(USER_NO_GROUP_ALIAS_PATTERN_PREFIX, 1)
                .getId();
        Long blogId = getBlogManagement().findBlogByIdentifier(BLOG_1_ALIAS).getId();
        getBlogRightsManagement().changeRoleOfMemberByEntityId(blogId, userId, BlogRole.MANAGER);
        assertBlogAccess(blogId, userId, BlogRole.MANAGER, false, true);
        // down-grade role by assign function and change role
        getBlogRightsManagement().assignEntity(blogId, userId, BlogRole.MEMBER);
        assertBlogAccess(blogId, userId, BlogRole.MEMBER, false, true);
        getBlogRightsManagement().changeRoleOfMemberByEntityId(blogId, userId, BlogRole.VIEWER);
        assertBlogAccess(blogId, userId, BlogRole.VIEWER, false, true);
        // try changing to same role must still succeed
        getBlogRightsManagement().changeRoleOfMemberByEntityId(blogId, userId, BlogRole.VIEWER);
        assertBlogAccess(blogId, userId, BlogRole.VIEWER, false, true);
    }

    /**
     * Tests that the client admin cannot modify groups he does not manage.
     *
     * @throws Exception
     *             if the test fails
     */
    @Test(groups = { "userBlogMemberTestsGroup" }, dependsOnMethods = { "testUser2ChangeRoleOfUser" })
    public void testUser3NoModificationByClientAdmin() throws Exception {
        AuthenticationTestUtils.setSecurityContext(UserAndGroupTestUtils.findNthUser(
                BLOG_MANAGER_ALIAS_PATTERN_PREFIX, 1));
        Long blogId = getBlogManagement().findBlogByIdentifier(BLOG_1_ALIAS).getId();
        User admin = UserAndGroupTestUtils.findNthUser(InstallerTest.TEST_MANAGER_USER_ALIAS, -1);
        AuthenticationHelper.removeAuthentication();
        AuthenticationTestUtils.setSecurityContext(admin);
        Long userId = UserAndGroupTestUtils.findNthUser(USER_NO_GROUP_ALIAS_PATTERN_PREFIX, 1)
                .getId();
        try {
            getBlogRightsManagement().changeRoleOfMemberByEntityId(blogId, userId, BlogRole.MEMBER);
            Assert.fail("Expected exception not thrown");
        } catch (BlogAccessException e) {
            // expected
            // assert role is unchanged
            assertBlogAccess(blogId, userId, BlogRole.VIEWER, false, true);
        }
    }

    /**
     * Tests changing the role of the last manager.
     *
     * @throws Exception
     *             if the test fails
     */
    @Test(groups = { "userBlogMemberTestsGroup" }, dependsOnMethods = { "testUser3NoModificationByClientAdmin" })
    public void testUser4ChangeRoleOfLastManager() throws Exception {
        User blogManager = UserAndGroupTestUtils.findNthUser(BLOG_MANAGER_ALIAS_PATTERN_PREFIX, 1);
        Long userId = blogManager.getId();
        AuthenticationTestUtils.setSecurityContext(blogManager);
        Long blogId = getBlogManagement().findBlogByIdentifier(BLOG_1_ALIAS).getId();
        try {
            getBlogRightsManagement().changeRoleOfMemberByEntityId(blogId, userId, BlogRole.VIEWER);
            Assert.fail("Expected exception NoBlogManagerLeftException not thrown.");
        } catch (NoBlogManagerLeftException e) {
            // expected exception
        }
    }

    /**
     * Tests adding a user to the blog that is already a member. Old member must be unchanged
     *
     * @throws Exception
     *             if the test fails
     */
    @Test(groups = { "userBlogMemberTestsGroup" }, dependsOnMethods = { "testUser4ChangeRoleOfLastManager" })
    public void testUser5AddSameUserToBlog() throws Exception {
        Long userId = UserAndGroupTestUtils.findNthUser(USER_NO_GROUP_ALIAS_PATTERN_PREFIX, 1)
                .getId();
        AuthenticationTestUtils.setSecurityContext(UserAndGroupTestUtils.findNthUser(
                BLOG_MANAGER_ALIAS_PATTERN_PREFIX, 1));
        Long blogId = getBlogManagement().findBlogByIdentifier(BLOG_1_ALIAS).getId();
        getBlogRightsManagement().addEntity(blogId, userId, BlogRole.MEMBER);
        // user must still have the role set on testUser1ChangeRoleOfUser
        assertBlogAccess(blogId, userId, BlogRole.VIEWER, false, true);
    }

    /**
     * Tests removing a member.
     *
     * @throws Exception
     *             if the test fails
     */
    @Test(groups = { "userBlogMemberTestsGroup" }, dependsOnMethods = { "testUser5AddSameUserToBlog" })
    public void testUser6RemoveUserFromBlog() throws Exception {
        Long userId = UserAndGroupTestUtils.findNthUser(USER_NO_GROUP_ALIAS_PATTERN_PREFIX, 1)
                .getId();
        AuthenticationTestUtils.setSecurityContext(UserAndGroupTestUtils.findNthUser(
                BLOG_MANAGER_ALIAS_PATTERN_PREFIX, 1));
        Long blogId = getBlogManagement().findBlogByIdentifier(BLOG_1_ALIAS).getId();
        getBlogRightsManagement().removeMemberByEntityId(blogId, userId);
        assertBlogAccess(blogId, userId, null, false, false);
    }

    /**
     * Tests removing a non-member.
     *
     * @throws Exception
     *             if the test fails
     */
    @Test(groups = { "userBlogMemberTestsGroup" }, dependsOnMethods = { "testUser6RemoveUserFromBlog" })
    public void testUser7RemoveNonMemberFromBlog() throws Exception {
        Long userId = UserAndGroupTestUtils.findNthUser(USER_NO_GROUP_ALIAS_PATTERN_PREFIX, 1)
                .getId();
        AuthenticationTestUtils.setSecurityContext(UserAndGroupTestUtils.findNthUser(
                BLOG_MANAGER_ALIAS_PATTERN_PREFIX, 1));
        Long blogId = getBlogManagement().findBlogByIdentifier(BLOG_1_ALIAS).getId();
        getBlogRightsManagement().removeMemberByEntityId(blogId, userId);

    }
}
