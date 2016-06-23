package com.communote.server.core.blog;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.testng.Assert;

import com.communote.common.converter.IdentityConverter;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.blog.BlogManagement;
import com.communote.server.api.core.blog.BlogRightsManagement;
import com.communote.server.api.core.blog.CreationBlogTO;
import com.communote.server.core.user.UserGroupManagement;
import com.communote.server.core.user.UserGroupMemberManagement;
import com.communote.server.core.vo.user.group.ExternalGroupVO;
import com.communote.server.core.vo.user.group.GroupVO;
import com.communote.server.model.blog.Blog;
import com.communote.server.model.blog.BlogRole;
import com.communote.server.model.user.User;
import com.communote.server.model.user.group.Group;
import com.communote.server.persistence.user.group.UserOfGroupDao;
import com.communote.server.test.CommunoteIntegrationTest;
import com.communote.server.test.util.AuthenticationTestUtils;

/**
 * Base class for blog access rights tests.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class BlogRightsTestBase extends CommunoteIntegrationTest {

    /**
     * Assert the required blog access is granted
     *
     * @param blogId
     *            the ID of the blog to test for access
     * @param entityId
     *            the ID of the entity to test for access
     * @param requiredRole
     *            the required access
     * @param ignoreAllCan
     *            whether to ignore the allCanRead or allCanWrite
     * @param mustBeDirectMember
     *            if the user must be direct member
     */
    protected void assertBlogAccess(Long blogId, Long entityId, BlogRole requiredRole,
            boolean ignoreAllCan, boolean mustBeDirectMember) {
        boolean readAccess = getBlogRightsManagement().userHasReadAccess(blogId, entityId,
                ignoreAllCan);
        boolean writeAccess = getBlogRightsManagement().userHasWriteAccess(blogId, entityId,
                ignoreAllCan);
        boolean manageAccess = getBlogRightsManagement().userHasManagementAccess(blogId, entityId);
        boolean isDirectMember = getBlogRightsManagement().isEntityDirectMember(blogId, entityId);
        BlogRole entityRole = getBlogRightsManagement().getRoleOfEntity(blogId, entityId,
                ignoreAllCan);
        BlogRole userRole = getBlogRightsManagement().getRoleOfUser(blogId, entityId, ignoreAllCan);
        String assertionFailedMessage = "Assertion failed for blog " + blogId + " and entity "
                + entityId;
        Assert.assertEquals(entityRole, userRole, assertionFailedMessage);
        if (requiredRole == null) {
            Assert.assertFalse(readAccess, assertionFailedMessage);
            Assert.assertFalse(writeAccess, assertionFailedMessage);
            Assert.assertFalse(manageAccess, assertionFailedMessage);
            Assert.assertFalse(isDirectMember, assertionFailedMessage);
            Assert.assertNull(userRole, assertionFailedMessage);
        } else if (BlogRole.VIEWER.equals(requiredRole)) {
            Assert.assertTrue(readAccess, assertionFailedMessage);
            Assert.assertFalse(writeAccess, assertionFailedMessage);
            Assert.assertFalse(manageAccess, assertionFailedMessage);
            Assert.assertEquals(isDirectMember, mustBeDirectMember, assertionFailedMessage);
            Assert.assertEquals(userRole, BlogRole.VIEWER, assertionFailedMessage);
        } else if (BlogRole.MEMBER.equals(requiredRole)) {
            Assert.assertTrue(readAccess, assertionFailedMessage);
            Assert.assertTrue(writeAccess, assertionFailedMessage);
            Assert.assertFalse(manageAccess, assertionFailedMessage);
            Assert.assertEquals(isDirectMember, mustBeDirectMember, assertionFailedMessage);
            Assert.assertEquals(userRole, BlogRole.MEMBER);
        } else {
            Assert.assertTrue(readAccess, assertionFailedMessage);
            Assert.assertTrue(writeAccess, assertionFailedMessage);
            Assert.assertTrue(manageAccess, assertionFailedMessage);
            Assert.assertEquals(isDirectMember, mustBeDirectMember, assertionFailedMessage);
            Assert.assertEquals(userRole, BlogRole.MANAGER, assertionFailedMessage);
        }
    }

    /**
     * Assert that a group and its members have the required role.
     *
     * @param blogId
     *            the ID of the blog to test
     * @param groupId
     *            ID of the group to test
     * @param requiredRole
     *            the required role
     * @param ignoreAllCan
     *            whether the allCanRead and allCanWrite flags should be ignored
     * @param groupMembersToSkip
     *            a set of userIds of group members that should be skipped when testing the roles of
     *            the memberships. Useful if there users that are direct blog members. Set to null
     *            if all group members should be tested.
     * @throws Exception
     *             if the test fails
     */
    protected void assertGroupBlogAccess(Long blogId, Long groupId, BlogRole requiredRole,
            boolean ignoreAllCan, Set<Long> groupMembersToSkip) throws Exception {
        BlogRole entityRole = getBlogRightsManagement().getRoleOfEntity(blogId, groupId,
                ignoreAllCan);
        boolean isDirectMember = getBlogRightsManagement().isEntityDirectMember(blogId, groupId);
        if (requiredRole != null) {
            Assert.assertTrue(isDirectMember, "group not direct member");
        } else {
            Assert.assertFalse(isDirectMember, "group is direct member");
        }
        Assert.assertEquals(entityRole, requiredRole);
        Collection<Long> memberUserIds = ServiceLocator.findService(UserOfGroupDao.class)
                .getUsersOfGroup(groupId);
        int skippedMembers = 0;
        for (Long id : memberUserIds) {
            if (groupMembersToSkip != null && groupMembersToSkip.contains(id)) {
                skippedMembers++;
            } else {
                assertBlogAccess(blogId, id, requiredRole, ignoreAllCan, false);
            }
        }
        int expectedMembersToSkip = groupMembersToSkip != null ? groupMembersToSkip.size() : 0;
        Assert.assertEquals(skippedMembers, expectedMembersToSkip,
                "Did not skip the required members.");
    }

    /**
     * Creates a test blog.
     *
     * @param creator
     *            the creator
     * @param blogAlias
     *            the alias/name identifier to assign to the topic
     * @throws Exception
     *             if the creation fails
     */
    protected void createBlog(User creator, String blogAlias) throws Exception {
        CreationBlogTO blogTO = new CreationBlogTO();
        blogTO.setTitle(blogAlias);
        blogTO.setNameIdentifier(blogAlias);
        blogTO.setCreatorUserId(creator.getId());
        Authentication currentAuth = AuthenticationTestUtils.setSecurityContext(creator);
        try {
            Blog b = ServiceLocator.instance().getService(BlogManagement.class).createBlog(blogTO);
            BlogRightsManagement brm = getBlogRightsManagement();
            boolean directMember = brm.isEntityDirectMember(b.getId(), creator.getId());
            Assert.assertTrue(directMember, "Creator is not direct member of blog.");
            boolean isManager = brm.userHasManagementAccess(b.getId(), creator.getId());
            Assert.assertTrue(isManager, "Creator is not manager of blog.");
        } finally {
            AuthenticationTestUtils.setAuthentication(currentAuth);
        }
    }

    /**
     * Creates a group.
     *
     * @param alias
     *            the alias of the group
     * @param name
     *            the name of the group
     * @param externalSystemId
     *            the ID of the external system, can be null
     * @return the created group
     * @throws Exception
     *             if the creation failed
     */
    protected Group createGroup(String alias, String name, String externalSystemId)
            throws Exception {
        UserGroupManagement gm = ServiceLocator.findService(UserGroupManagement.class);
        Group group = null;
        if (externalSystemId == null) {
            GroupVO vo = new GroupVO(name, alias, null);
            group = gm.createGroup(vo);
        } else {
            ExternalGroupVO vo = new ExternalGroupVO("external_" + UUID.randomUUID(),
                    externalSystemId, UUID.randomUUID().toString(), name, alias, null);
            Long groupId = gm.createExternalGroup(vo);
            group = gm.findGroupById(groupId, new IdentityConverter<Group>());
        }
        Assert.assertNotNull(group, "group creation failed");
        Assert.assertEquals(group.getAlias(), alias.toLowerCase());
        Assert.assertEquals(group.getName(), name);
        return group;
    }

    /**
     * @return the blog management
     */
    protected BlogManagement getBlogManagement() {
        return ServiceLocator.instance().getService(BlogManagement.class);
    }

    /**
     * @return the blog rights management
     */
    protected BlogRightsManagement getBlogRightsManagement() {
        return ServiceLocator.instance().getService(BlogRightsManagement.class);
    }

    /**
     * @return the group management
     */
    protected UserGroupManagement getGroupManagement() {
        return ServiceLocator.instance().getService(UserGroupManagement.class);
    }

    /**
     * @return the group member management
     */
    protected UserGroupMemberManagement getGroupMemberManagement() {
        return ServiceLocator.findService(UserGroupMemberManagement.class);
    }
}
