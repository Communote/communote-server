package com.communote.server.core.external;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.blog.BlogRightsManagement;
import com.communote.server.api.core.config.ClientConfigurationPropertyConstant;
import com.communote.server.api.core.config.type.ClientProperty;
import com.communote.server.core.external.BlogRightsSynchronizer;
import com.communote.server.core.external.ExternalObjectManagement;
import com.communote.server.core.user.UserGroupManagement;
import com.communote.server.core.user.UserManagement;
import com.communote.server.core.vo.external.ExternalTopicRoleTO;
import com.communote.server.core.vo.user.group.ExternalGroupVO;
import com.communote.server.model.blog.Blog;
import com.communote.server.model.blog.BlogRole;
import com.communote.server.model.config.LdapConfiguration;
import com.communote.server.model.external.ExternalObject;
import com.communote.server.model.user.ExternalUserAuthentication;
import com.communote.server.model.user.User;
import com.communote.server.model.user.group.ExternalUserGroup;
import com.communote.server.persistence.user.ExternalUserVO;
import com.communote.server.persistence.user.UserDao;
import com.communote.server.persistence.user.group.ExternalUserGroupDao;
import com.communote.server.service.UserService;
import com.communote.server.test.external.MockExternalObjectSource;
import com.communote.server.test.external.MockExternalUserGroupAccessor;
import com.communote.server.test.external.MockExternalUserRepository;
import com.communote.server.test.external.MockExternalUserRepository.MockExternalSystemConfiguration;
import com.communote.server.test.ldap.LdapCommunoteIntegrationTest;
import com.communote.server.test.util.AuthenticationTestUtils;
import com.communote.server.test.util.TestUtils;
import com.communote.server.test.util.UserAndGroupTestUtils;

/**
 * Tests for {@link BlogRightsSynchronizer}.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class BlogRightsSynchronizerTest extends LdapCommunoteIntegrationTest {
    private User user;
    private ExternalObjectManagement externalObjectManagement;

    private BlogRightsManagement blogRightsManagement;

    private MockExternalUserRepository externalUserRepository;

    /**
     * Build an external object for test
     *
     * @param externalSystemId
     *            the ID of the external system
     * @return an created external object
     */
    private ExternalObject buildExternalObject(String externalSystemId) {
        ExternalObject externalObject = ExternalObject.Factory.newInstance();

        externalObject.setExternalName(UUID.randomUUID().toString());
        externalObject.setExternalSystemId(externalSystemId);
        externalObject.setExternalId(UUID.randomUUID().toString());

        return externalObject;
    }

    /**
     * Create an external group for the external system of the externalUserRepository and add it to
     * the repo.
     *
     * @return the external ID of the group
     * @throws Exception
     *             in case the group could not be created
     */
    private String createRandomExternalGroup() throws Exception {
        AuthenticationTestUtils.setManagerContext();
        ExternalGroupVO groupVo = UserAndGroupTestUtils
                .createRandomGroupVoForExternalSystem(externalUserRepository.getExternalSystemId());
        // add to repo
        ((MockExternalUserGroupAccessor) externalUserRepository.getExternalUserGroupAccessor())
        .addGroup(groupVo);
        ServiceLocator.findService(UserGroupManagement.class).createExternalGroup(groupVo);
        return groupVo.getExternalId();
    }

    /**
     * Create an external user for the external system of the externalUserRepository and add it to
     * the repo.
     *
     * @return the created user
     * @throws Exception
     *             in case the user could not be created
     */
    private User createRandomExternalUser() throws Exception {
        ExternalUserVO userVO = TestUtils
                .createRandomUserVoForExternalSystem(externalUserRepository.getExternalSystemId());
        // add to mock repo
        externalUserRepository.addUser(userVO);
        return ServiceLocator.instance().getService(UserManagement.class)
                .createOrUpdateExternalUser(userVO);
    }

    /**
     * Get the dao for external user group
     *
     * @return the external user group dao
     */
    private ExternalUserGroupDao getExternalUserGroupDao() {
        return ServiceLocator.findService(ExternalUserGroupDao.class);
    }

    /**
     * Get the external user identifier of internal user representation
     *
     * @param externalUser
     *            internal representation of external user
     * @return external user identifier
     */
    private String getExternalUserIdOfUser(User externalUser) {
        for (ExternalUserAuthentication externalUserAuthentication : externalUser
                .getExternalAuthentications()) {
            if (externalUserAuthentication.getSystemId().equals(
                    externalUserRepository.getExternalSystemId())) {
                return externalUserAuthentication.getExternalUserId();
            }
        }
        return null;
    }

    /**
     * Setups all common parameters for this tests.
     *
     * @throws Exception
     *             Exception.
     */
    @BeforeClass(dependsOnGroups = "integration-test-setup")
    public void setup() throws Exception {
        user = TestUtils.createRandomUser(false);
        externalObjectManagement = ServiceLocator.instance().getService(
                ExternalObjectManagement.class);
        blogRightsManagement = ServiceLocator.instance().getService(BlogRightsManagement.class);
        AuthenticationTestUtils.setManagerContext();
        // TODO this test is strange. LDAP does not provide any external object rights.
        // BlogRightsSynchronizer has to be more flexible in resolving the users: the usage of the
        // same externalSystemId for user resolving and external objects requires that users and
        // external rights are from the same external system which usually not the case.
        LdapConfiguration ldapConfiguration = createGroupConfiguration(true, false, "member");
        ldapConfiguration.setAllowExternalAuthentication(true);
        ldapConfiguration.setSynchronizeUserGroups(true);
        CommunoteRuntime.getInstance().getConfigurationManager()
                .updateLdapConfiguration(ldapConfiguration);
        String externalSystemId = ldapConfiguration.getSystemId();

        CommunoteRuntime.getInstance().getConfigurationManager()
                .setPrimaryAuthentication(externalSystemId, true);

        // set that groups can been created
        Map<ClientConfigurationPropertyConstant, String> map;
        map = new HashMap<ClientConfigurationPropertyConstant, String>();
        map.put(ClientProperty.CREATE_EXTERNAL_GROUP_AUTOMATICALLY, Boolean.TRUE.toString());
        map.put(ClientProperty.CREATE_EXTERNAL_USER_AUTOMATICALLY, Boolean.TRUE.toString());
        CommunoteRuntime.getInstance().getConfigurationManager()
                .updateClientConfigurationProperties(map);
        // since the ldap stuff was moved into a plugin the ldap user repo is missing, thus we mock
        // it
        MockExternalUserGroupAccessor groupAccessor = new MockExternalUserGroupAccessor(
                externalSystemId);
        externalUserRepository = new MockExternalUserRepository(externalSystemId, groupAccessor);
        MockExternalObjectSource source = new MockExternalObjectSource(externalSystemId,
                new MockExternalObjectSource.MockExternalObjectSourceConfiguration());
        source.getConfiguration().setNumberOfMaximumExternalObjectsPerTopic(1);
        ServiceLocator.findService(ExternalObjectManagement.class).registerExternalObjectSource(
                source);
        externalUserRepository.setConfiguration(new MockExternalSystemConfiguration());
        ServiceLocator.instance().getService(UserService.class)
        .registerRepository(externalSystemId, externalUserRepository);

    }

    /**
     * Test the synchronization of {@link ExternalTopicRoleTO}
     *
     * @param blogId
     *            identifier of the blog
     * @param externalObjectId
     *            external object identifier
     * @param externalSystemId
     *            the ID of the external system
     * @param externalUser
     *            locally existing external user
     * @param externalUser2
     *            locally existing external user
     * @param externalUser3
     *            locally existing external user
     * @param externalGroupId
     *            the external ID of a group that is already in the DB
     * @throws Exception
     *             in case the test failed
     */
    private void synchronizeExternalTopicRoleTOs(Long blogId, String externalObjectId,
            String externalSystemId, User externalUser, User externalUser2, User externalUser3,
            String externalGroupId) throws Exception {

        List<ExternalTopicRoleTO> externalTopicRoleTOs = new ArrayList<ExternalTopicRoleTO>();
        // create a blog synchronizer for the provided blog and external system
        BlogRightsSynchronizer blogRightsSynchronizer = new BlogRightsSynchronizer(blogId,
                externalSystemId);

        ExternalTopicRoleTO externalTopicRoleTO = new ExternalTopicRoleTO();
        externalTopicRoleTO.setEntityId(externalUser.getId());
        externalTopicRoleTO.setRole(BlogRole.MEMBER);
        externalTopicRoleTO.setExternalObjectId(externalObjectId);
        externalTopicRoleTOs.add(externalTopicRoleTO);

        ExternalTopicRoleTO externalTopicRoleTO2 = new ExternalTopicRoleTO();
        externalTopicRoleTO2.setEntityAlias(externalUser2.getAlias());
        externalTopicRoleTO2.setRole(BlogRole.VIEWER);
        externalTopicRoleTO2.setExternalObjectId(externalObjectId);
        externalTopicRoleTOs.add(externalTopicRoleTO2);

        ExternalTopicRoleTO externalTopicRoleTO3 = new ExternalTopicRoleTO();
        externalTopicRoleTO3.setExternalEntityId(getExternalUserIdOfUser(externalUser3));
        externalTopicRoleTO3.setRole(BlogRole.MEMBER);
        externalTopicRoleTO3.setExternalObjectId(externalObjectId);
        externalTopicRoleTOs.add(externalTopicRoleTO3);

        // test synchronization of group privileges
        ExternalTopicRoleTO externalTopicRoleTO4 = new ExternalTopicRoleTO();
        externalTopicRoleTO4.setExternalEntityId(externalGroupId);
        externalTopicRoleTO4.setRole(BlogRole.MEMBER);
        externalTopicRoleTO4.setExternalObjectId(externalObjectId);
        externalTopicRoleTO4.setIsGroup(Boolean.TRUE);

        externalTopicRoleTOs.add(externalTopicRoleTO4);
        // blog manager is authorized
        AuthenticationTestUtils.setSecurityContext(user);
        // test assign of rights
        blogRightsSynchronizer.replaceRights(externalTopicRoleTOs);

        Assert.assertEquals(
                blogRightsManagement.getRoleOfEntity(blogId, externalUser.getId(), true),
                BlogRole.MEMBER);
        Assert.assertEquals(
                blogRightsManagement.getRoleOfEntity(blogId, externalUser2.getId(), true),
                BlogRole.VIEWER);
        Assert.assertEquals(
                blogRightsManagement.getRoleOfEntity(blogId, externalUser3.getId(), true),
                BlogRole.MEMBER);

        ExternalUserGroup externalGroup = getExternalUserGroupDao().findByExternalId(
                externalGroupId, externalUserRepository.getExternalSystemId());

        Assert.assertEquals(
                blogRightsManagement.getRoleOfEntity(blogId, externalGroup.getId(), true),
                BlogRole.MEMBER);

        // modify roles and remove some entries to test update and removal of rights
        externalTopicRoleTOs.remove(2); // externalTopicRoleTO3
        externalTopicRoleTO2.setRole(BlogRole.MEMBER);
        // test with group alias instead of external groupID
        externalTopicRoleTO4.setExternalEntityId(null);
        externalTopicRoleTO4.setEntityAlias(externalGroup.getAlias());
        externalTopicRoleTO4.setRole(BlogRole.VIEWER);

        blogRightsSynchronizer.replaceRights(externalTopicRoleTOs);

        Assert.assertEquals(
                blogRightsManagement.getRoleOfEntity(blogId, externalUser.getId(), true),
                BlogRole.MEMBER);
        Assert.assertEquals(
                blogRightsManagement.getRoleOfEntity(blogId, externalUser2.getId(), true),
                BlogRole.MEMBER);
        Assert.assertNull(blogRightsManagement.getRoleOfEntity(blogId, externalUser3.getId(), true));
        Assert.assertEquals(
                blogRightsManagement.getRoleOfEntity(blogId, externalGroup.getId(), true),
                BlogRole.VIEWER);
        // test again with groups local ID
        externalTopicRoleTO4.setEntityId(externalGroup.getId());
        externalTopicRoleTO4.setRole(BlogRole.MANAGER);
        blogRightsSynchronizer.replaceRights(externalTopicRoleTOs);

        Assert.assertEquals(
                blogRightsManagement.getRoleOfEntity(blogId, externalUser.getId(), true),
                BlogRole.MEMBER);
        Assert.assertEquals(
                blogRightsManagement.getRoleOfEntity(blogId, externalUser2.getId(), true),
                BlogRole.MEMBER);
        Assert.assertNull(blogRightsManagement.getRoleOfEntity(blogId, externalUser3.getId(), true));
        Assert.assertEquals(
                blogRightsManagement.getRoleOfEntity(blogId, externalGroup.getId(), true),
                BlogRole.MANAGER);
    }

    /**
     * Test the synchronization of external user roles
     *
     * @param blogId
     *            identifier of the blog
     * @param externalObjectId
     *            external object identifier
     * @param externalSystemId
     *            ID of the external system
     * @param externalUser
     *            locally existing external user
     * @param externalUser2
     *            locally existing external user
     * @param externalUser3
     *            locally existing external user
     * @param externalGroupId
     *            the external ID of a group that is already in the DB
     * @throws Exception
     *             in case the test failed
     */
    private void synchronizeExternalUserRoles(Long blogId, String externalObjectId,
            String externalSystemId, User externalUser, User externalUser2, User externalUser3,
            String externalGroupId) throws Exception {

        Map<String, BlogRole> userRoles = new HashMap<String, BlogRole>();

        userRoles.put(getExternalUserIdOfUser(externalUser), BlogRole.MEMBER);
        userRoles.put(getExternalUserIdOfUser(externalUser2), BlogRole.VIEWER);
        userRoles.put(getExternalUserIdOfUser(externalUser3), BlogRole.MEMBER);

        Map<String, BlogRole> groupRoles = new HashMap<String, BlogRole>();
        groupRoles.put(externalGroupId, BlogRole.MEMBER);

        BlogRightsSynchronizer blogRightsSynchronizer = new BlogRightsSynchronizer(blogId,
                externalSystemId);

        // manager is allowed to modify the external roles
        AuthenticationTestUtils.setSecurityContext(user);

        // test assign of rights
        blogRightsSynchronizer.replaceRights(externalObjectId, userRoles, groupRoles);

        Assert.assertEquals(
                blogRightsManagement.getRoleOfEntity(blogId, externalUser.getId(), true),
                BlogRole.MEMBER);
        Assert.assertEquals(
                blogRightsManagement.getRoleOfEntity(blogId, externalUser2.getId(), true),
                BlogRole.VIEWER);
        Assert.assertEquals(
                blogRightsManagement.getRoleOfEntity(blogId, externalUser3.getId(), true),
                BlogRole.MEMBER);

        ExternalUserGroup externalGroup = getExternalUserGroupDao().findByExternalId(
                externalGroupId, externalUserRepository.getExternalSystemId());

        Assert.assertEquals(
                blogRightsManagement.getRoleOfEntity(blogId, externalGroup.getId(), true),
                BlogRole.MEMBER);

        userRoles.remove(getExternalUserIdOfUser(externalUser3));
        userRoles.put(getExternalUserIdOfUser(externalUser2), BlogRole.MEMBER);

        groupRoles.put(externalGroupId, BlogRole.VIEWER);

        // test assign and remove of rights
        blogRightsSynchronizer.replaceRights(externalObjectId, userRoles, groupRoles);

        Assert.assertEquals(
                blogRightsManagement.getRoleOfEntity(blogId, externalUser.getId(), true),
                BlogRole.MEMBER);
        Assert.assertEquals(
                blogRightsManagement.getRoleOfEntity(blogId, externalUser2.getId(), true),
                BlogRole.MEMBER);
        Assert.assertNull(blogRightsManagement.getRoleOfEntity(blogId, externalUser3.getId(), true));
        Assert.assertEquals(
                blogRightsManagement.getRoleOfEntity(blogId, externalGroup.getId(), true),
                BlogRole.VIEWER);

        // test null values
        blogRightsSynchronizer.replaceRights(externalObjectId, null, null);
        Assert.assertNull(blogRightsManagement.getRoleOfEntity(blogId, externalUser.getId(), true));
        Assert.assertNull(blogRightsManagement.getRoleOfEntity(blogId, externalUser2.getId(), true));
        Assert.assertNull(blogRightsManagement.getRoleOfEntity(blogId, externalUser3.getId(), true));
        Assert.assertNull(blogRightsManagement.getRoleOfEntity(blogId, externalGroup.getId(), true));
    }

    /**
     * Test to verify that new users and groups are added during blog rights synchronization, when
     * CREATE_AUTOMATICALLY is configured.
     *
     * @throws Exception
     *             in case the test failed
     */
    @Test
    public void testCreationOfNewEntities() throws Exception {
        Blog topic = TestUtils.createRandomBlog(true, true, user);
        String externalSystemId = externalUserRepository.getExternalSystemId();
        ExternalObject externalObject = buildExternalObject(externalSystemId);
        externalObjectManagement.assignExternalObject(topic.getId(), externalObject);
        // create a user that is already in DB
        User externalUser = createRandomExternalUser();
        String unpersistedExternalGroupId = "externalGroup-" + TestUtils.createRandomUserAlias();
        ((MockExternalUserGroupAccessor) externalUserRepository.getExternalUserGroupAccessor())
        .addGroup(unpersistedExternalGroupId);
        ExternalUserVO unpersistedExternalUserVO = TestUtils
                .createRandomUserVoForExternalSystem(externalUserRepository.getExternalSystemId());
        externalUserRepository.addUser(unpersistedExternalUserVO);
        Map<String, BlogRole> userRoles = new HashMap<String, BlogRole>();
        userRoles.put(getExternalUserIdOfUser(externalUser), BlogRole.MEMBER);
        userRoles.put(unpersistedExternalUserVO.getExternalUserName(), BlogRole.VIEWER);
        Map<String, BlogRole> groupRoles = new HashMap<String, BlogRole>();
        groupRoles.put(unpersistedExternalGroupId, BlogRole.VIEWER);

        AuthenticationTestUtils.setSecurityContext(user);
        BlogRightsSynchronizer blogRightsSynchronizer = new BlogRightsSynchronizer(topic.getId(),
                externalSystemId);
        blogRightsSynchronizer.replaceRights(externalObject.getExternalId(), userRoles, groupRoles);
        Assert.assertEquals(
                blogRightsManagement.getRoleOfEntity(topic.getId(), externalUser.getId(), true),
                BlogRole.MEMBER);
        // check created users and group
        User createdUser = ServiceLocator.findService(UserDao.class).findByExternalUserId(
                unpersistedExternalUserVO.getExternalUserName(),
                externalUserRepository.getExternalSystemId());
        Assert.assertNotNull(createdUser);
        Assert.assertEquals(
                blogRightsManagement.getRoleOfEntity(topic.getId(), createdUser.getId(), true),
                BlogRole.VIEWER);
        ExternalUserGroup createdGroup = getExternalUserGroupDao().findByExternalId(
                unpersistedExternalGroupId, externalUserRepository.getExternalSystemId());
        Assert.assertNotNull(createdGroup);
        Assert.assertEquals(
                blogRightsManagement.getRoleOfEntity(topic.getId(), createdGroup.getId(), true),
                BlogRole.VIEWER);
    }

    /**
     * Test that users are not assigned when the provided external object is not assigned to the
     * blog. The synchronizer must handle this situation silently.
     *
     * @throws Exception
     *             in case the test failed
     */
    @Test
    public void testExternalObjectNotAssigned() throws Exception {
        Blog blog = TestUtils.createRandomBlog(true, true, user);
        String externalSystemId = externalUserRepository.getExternalSystemId();
        ExternalObject externalObject = buildExternalObject(externalSystemId);

        externalObjectManagement.assignExternalObject(blog.getId(), externalObject);
        User externalUser = createRandomExternalUser();
        User externalUser2 = createRandomExternalUser();

        BlogRightsSynchronizer blogRightsSynchronizer = new BlogRightsSynchronizer(blog.getId(),
                externalSystemId);
        // assign only one user
        Map<String, BlogRole> userRoles = new HashMap<String, BlogRole>();
        userRoles.put(getExternalUserIdOfUser(externalUser), BlogRole.MEMBER);

        AuthenticationTestUtils.setSecurityContext(user);
        blogRightsSynchronizer.replaceRights(externalObject.getExternalId(), userRoles, null);
        Assert.assertEquals(
                blogRightsManagement.getRoleOfEntity(blog.getId(), externalUser.getId(), true),
                BlogRole.MEMBER);

        // assign another user but use wrong externalObjectId new user must not be added, and old
        // must not be removed
        userRoles.clear();
        userRoles.put(getExternalUserIdOfUser(externalUser2), BlogRole.MEMBER);
        blogRightsSynchronizer.replaceRights("not assigned", userRoles, null);
        Assert.assertEquals(
                blogRightsManagement.getRoleOfEntity(blog.getId(), externalUser.getId(), true),
                BlogRole.MEMBER);
        Assert.assertNull(blogRightsManagement.getRoleOfEntity(blog.getId(), externalUser2.getId(),
                true));
    }

    /**
     * Test merge feature of blog synchronizer
     *
     * @throws Exception
     *             in case the test failed
     */
    @Test(dependsOnMethods = { "testReplaceRights" })
    public void testMergeRights() throws Exception {
        Blog blog = TestUtils.createRandomBlog(false, false, user);
        String externalSystemId = externalUserRepository.getExternalSystemId();
        ExternalObject extObject = buildExternalObject(externalSystemId);

        externalObjectManagement.assignExternalObject(blog.getId(), extObject);
        User extUser = createRandomExternalUser();
        User extUser2 = createRandomExternalUser();
        User extUser3 = createRandomExternalUser();
        User extUser4 = createRandomExternalUser();
        String extGroupId = createRandomExternalGroup();
        ExternalUserGroup extGroup = getExternalUserGroupDao().findByExternalId(extGroupId,
                externalUserRepository.getExternalSystemId());
        String extGroupId2 = createRandomExternalGroup();
        ExternalUserGroup extGroup2 = getExternalUserGroupDao().findByExternalId(extGroupId2,
                externalUserRepository.getExternalSystemId());

        BlogRightsSynchronizer blogRightsSynchronizer = new BlogRightsSynchronizer(blog.getId(),
                externalSystemId);
        // assign 2 users and 2 groups
        Map<String, BlogRole> userRoles = new HashMap<String, BlogRole>();
        userRoles.put(getExternalUserIdOfUser(extUser), BlogRole.VIEWER);
        userRoles.put(getExternalUserIdOfUser(extUser2), BlogRole.MEMBER);
        Map<String, BlogRole> groupRoles = new HashMap<String, BlogRole>();
        groupRoles.put(extGroupId, BlogRole.VIEWER);
        groupRoles.put(extGroupId2, BlogRole.MEMBER);

        AuthenticationTestUtils.setSecurityContext(user);
        blogRightsSynchronizer.replaceRights(extObject.getExternalId(), userRoles, groupRoles);

        // merge by updating externalGroup, adding externalUser4 and removing externalUser and (not
        // assigned) externalUser3
        ArrayList<ExternalTopicRoleTO> rolesToAddAndUpdate = new ArrayList<ExternalTopicRoleTO>();
        rolesToAddAndUpdate.add(new ExternalTopicRoleTO(BlogRole.MEMBER, null, extGroup.getAlias(),
                true, extObject.getExternalId(), null));
        rolesToAddAndUpdate.add(new ExternalTopicRoleTO(BlogRole.VIEWER, extUser4.getId(), null,
                null, extObject.getExternalId(), null));
        ArrayList<ExternalTopicRoleTO> rolesToRemove = new ArrayList<ExternalTopicRoleTO>();
        // role and external object are ignored
        rolesToRemove
        .add(new ExternalTopicRoleTO(null, null, extUser.getAlias(), null, null, null));
        rolesToRemove.add(new ExternalTopicRoleTO(null, extUser3.getId(), null, null, null, null));
        blogRightsSynchronizer.mergeRights(rolesToAddAndUpdate, rolesToRemove);
        Assert.assertNull(blogRightsManagement.getRoleOfEntity(blog.getId(), extUser.getId(), true));
        Assert.assertEquals(
                blogRightsManagement.getRoleOfEntity(blog.getId(), extUser2.getId(), true),
                BlogRole.MEMBER);
        Assert.assertNull(blogRightsManagement.getRoleOfEntity(blog.getId(), extUser3.getId(), true));
        Assert.assertEquals(
                blogRightsManagement.getRoleOfEntity(blog.getId(), extUser4.getId(), true),
                BlogRole.VIEWER);
        Assert.assertEquals(
                blogRightsManagement.getRoleOfEntity(blog.getId(), extGroup.getId(), true),
                BlogRole.MEMBER);
        Assert.assertEquals(
                blogRightsManagement.getRoleOfEntity(blog.getId(), extGroup2.getId(), true),
                BlogRole.MEMBER);
        // test left side null and group removal
        rolesToRemove.clear();
        rolesToRemove.add(new ExternalTopicRoleTO(null, null, null, true, null, extGroupId2));
        blogRightsSynchronizer.mergeRights(null, rolesToRemove);
        // all roles as before except group2 must be removed
        Assert.assertNull(blogRightsManagement.getRoleOfEntity(blog.getId(), extUser.getId(), true));
        Assert.assertEquals(
                blogRightsManagement.getRoleOfEntity(blog.getId(), extUser2.getId(), true),
                BlogRole.MEMBER);
        Assert.assertNull(blogRightsManagement.getRoleOfEntity(blog.getId(), extUser3.getId(), true));
        Assert.assertEquals(
                blogRightsManagement.getRoleOfEntity(blog.getId(), extUser4.getId(), true),
                BlogRole.VIEWER);
        Assert.assertEquals(
                blogRightsManagement.getRoleOfEntity(blog.getId(), extGroup.getId(), true),
                BlogRole.MEMBER);
        Assert.assertNull(blogRightsManagement.getRoleOfEntity(blog.getId(), extGroup2.getId(),
                true));

        rolesToAddAndUpdate.clear();
        rolesToAddAndUpdate.add(new ExternalTopicRoleTO(BlogRole.MEMBER, extUser4.getId(), null,
                null, extObject.getExternalId(), null));
        blogRightsSynchronizer.mergeRights(rolesToAddAndUpdate, null);
        Assert.assertNull(blogRightsManagement.getRoleOfEntity(blog.getId(), extUser.getId(), true));
        Assert.assertEquals(
                blogRightsManagement.getRoleOfEntity(blog.getId(), extUser2.getId(), true),
                BlogRole.MEMBER);
        Assert.assertNull(blogRightsManagement.getRoleOfEntity(blog.getId(), extUser3.getId(), true));
        Assert.assertEquals(
                blogRightsManagement.getRoleOfEntity(blog.getId(), extUser4.getId(), true),
                BlogRole.MEMBER);
        Assert.assertEquals(
                blogRightsManagement.getRoleOfEntity(blog.getId(), extGroup.getId(), true),
                BlogRole.MEMBER);
        Assert.assertNull(blogRightsManagement.getRoleOfEntity(blog.getId(), extGroup2.getId(),
                true));
    }

    /**
     * Test for assigning of locally existing external users and groups via the replaceRights
     * methods of the synchronizer
     *
     * @throws Exception
     *             in case the test failed
     */
    @Test
    public void testReplaceRights() throws Exception {
        AuthenticationTestUtils.setManagerContext();
        User externalUser = createRandomExternalUser();
        User externalUser2 = createRandomExternalUser();
        User externalUser3 = createRandomExternalUser();
        String externalGroupId = createRandomExternalGroup();

        // test with externalSystemId that matches the system of the external users/groups
        String externalSystemId = externalUserRepository.getExternalSystemId();
        Blog blog = TestUtils.createRandomBlog(true, true, user);
        ExternalObject externalObject = buildExternalObject(externalSystemId);
        externalObjectManagement.assignExternalObject(blog.getId(), externalObject);

        synchronizeExternalTopicRoleTOs(blog.getId(), externalObject.getExternalId(),
                externalSystemId, externalUser, externalUser2, externalUser3, externalGroupId);

        synchronizeExternalUserRoles(blog.getId(), externalObject.getExternalId(),
                externalSystemId, externalUser, externalUser2, externalUser3, externalGroupId);

        // test with externalSystemId that does not match the system of the external users/groups
        externalSystemId = externalUserRepository.getExternalSystemId();
        blog = TestUtils.createRandomBlog(true, true, user);
        externalObject = buildExternalObject(externalSystemId);
        externalObjectManagement.assignExternalObject(blog.getId(), externalObject);

        synchronizeExternalTopicRoleTOs(blog.getId(), externalObject.getExternalId(),
                externalSystemId, externalUser, externalUser2, externalUser3, externalGroupId);

        synchronizeExternalUserRoles(blog.getId(), externalObject.getExternalId(),
                externalSystemId, externalUser, externalUser2, externalUser3, externalGroupId);

    }
}
