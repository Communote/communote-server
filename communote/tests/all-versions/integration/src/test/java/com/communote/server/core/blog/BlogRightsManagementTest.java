package com.communote.server.core.blog;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.blog.BlogRightsManagement;
import com.communote.server.api.core.config.ConfigurationManager;
import com.communote.server.api.core.config.type.ClientProperty;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.core.external.ExternalObjectManagement;
import com.communote.server.model.blog.Blog;
import com.communote.server.model.blog.BlogRole;
import com.communote.server.model.external.ExternalObject;
import com.communote.server.model.user.User;
import com.communote.server.test.external.MockExternalObjectSource;
import com.communote.server.test.ldap.LdapCommunoteIntegrationTest;
import com.communote.server.test.util.AuthenticationTestUtils;
import com.communote.server.test.util.TestUtils;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class BlogRightsManagementTest extends LdapCommunoteIntegrationTest {

    @Autowired
    private BlogRightsManagement topicRightsManagement;
    @Autowired
    private ExternalObjectManagement externalObjectManagement;

    private ConfigurationManager propertiesManager;
    private MockExternalObjectSource externalObjectSource;

    @AfterClass
    public void cleanup() {
        externalObjectManagement.unregisterExternalObjectSource(externalObjectSource);
    }

    /**
     * Setups all common parameters for this tests.
     *
     * @throws Exception
     *             Exception.
     */
    @BeforeClass(dependsOnGroups = "integration-test-setup")
    public void setup() throws Exception {
        propertiesManager = CommunoteRuntime.getInstance().getConfigurationManager();
        AuthenticationTestUtils.setManagerContext();
        externalObjectSource = TestUtils.createNewExternalObjectSource(true);
    }

    /**
     *
     * Test for {@link BlogRightsManagement#assignEntityForExternal}
     *
     * @throws Exception
     *             Exception.
     */
    @Test
    public void testAssignExternalEntity() throws Exception {
        User manager = TestUtils.createRandomUser(true);
        User user = TestUtils.createRandomUser(false);
        Long topicId = TestUtils.createRandomBlog(false, false, manager).getId();

        final List<Timestamp> crawlTimeStamps = new ArrayList<>();
        TestUtils.addAndCheckCrawlLastModificationDateForTopic(topicId, crawlTimeStamps);

        String externalId = UUID.randomUUID().toString();

        ExternalObject externalObject = ExternalObject.Factory.newInstance(
                externalObjectSource.getIdentifier(), externalId);
        externalObjectManagement.assignExternalObject(topicId, externalObject);

        topicRightsManagement.assignEntityForExternal(topicId, user.getId(), BlogRole.MEMBER,
                externalObjectSource.getIdentifier(), externalId);

        BlogRole role = topicRightsManagement.getRoleOfEntity(topicId, user.getId(), true);
        Assert.assertEquals(role, BlogRole.MEMBER);
        TestUtils.addAndCheckCrawlLastModificationDateForTopic(topicId, crawlTimeStamps);

        topicRightsManagement.removeMemberByEntityIdForExternal(topicId, user.getId(),
                externalObjectSource.getIdentifier());

        role = topicRightsManagement.getRoleOfEntity(topicId, user.getId(), true);
        Assert.assertNull(role);
        TestUtils.addAndCheckCrawlLastModificationDateForTopic(topicId, crawlTimeStamps);

        // check trusted methods
        topicRightsManagement.assignEntityForExternalTrusted(topicId, user.getId(),
                BlogRole.MANAGER, externalObjectSource.getIdentifier());

        role = topicRightsManagement.getRoleOfEntity(topicId, user.getId(), true);
        Assert.assertEquals(role, BlogRole.MANAGER);
        TestUtils.addAndCheckCrawlLastModificationDateForTopic(topicId, crawlTimeStamps);

        topicRightsManagement.removeMemberByEntityIdForExternal(topicId, user.getId(),
                externalObjectSource.getIdentifier());
        role = topicRightsManagement.getRoleOfEntity(topicId, user.getId(), true);
        Assert.assertNull(role);
        TestUtils.addAndCheckCrawlLastModificationDateForTopic(topicId, crawlTimeStamps);

    }

    /**
     * Test for {@link BlogRightsManagement#assignManagementAccessToCurrentUser(Long)}
     *
     * @throws Exception
     *             in case the test failed
     */
    @Test
    public void testAssignManagementAccessToCurrentUser() throws Exception {
        User user1 = TestUtils.createRandomUser(false);
        User user2 = TestUtils.createRandomUser(false);
        User manager = TestUtils.createRandomUser(true);
        Long topicId = TestUtils.createRandomBlog(false, false, user1).getId();

        final List<Timestamp> crawlTimeStamps = new ArrayList<>();
        TestUtils.addAndCheckCrawlLastModificationDateForTopic(topicId, crawlTimeStamps);

        AuthenticationTestUtils.setSecurityContext(user2);
        try {
            topicRightsManagement.assignManagementAccessToCurrentUser(topicId);
            Assert.fail("Non managers should be able to gain topic access");
        } catch (AuthorizationException e) {
            // Okay.
        }
        AuthenticationTestUtils.setSecurityContext(manager);
        topicRightsManagement.assignManagementAccessToCurrentUser(topicId);

        TestUtils.addAndCheckCrawlLastModificationDateForTopic(topicId, crawlTimeStamps);
    }

    /**
     *
     * Test for {@link BlogRightsManagement#setAllCanReadAllCanWrite(Long, boolean, boolean)}
     *
     * @throws Exception
     *             Exception.
     */
    @Test
    public void testSetAllCanReadAllCanWrite() throws Exception {
        User manager = TestUtils.createRandomUser(true);
        User user = TestUtils.createRandomUser(false);
        Long blogId = TestUtils.createRandomBlog(false, false, manager).getId();

        final List<Timestamp> crawlTimeStamps = new ArrayList<>();
        TestUtils.addAndCheckCrawlLastModificationDateForTopic(blogId, crawlTimeStamps);

        topicRightsManagement.addEntity(blogId, user.getId(), BlogRole.MANAGER);
        TestUtils.addAndCheckCrawlLastModificationDateForTopic(blogId, crawlTimeStamps);

        propertiesManager.updateClientConfigurationProperty(
                ClientProperty.ALLOW_ALL_CAN_READ_WRITE_FOR_ALL_USERS, Boolean.TRUE.toString());
        // Client Manager
        AuthenticationTestUtils.setSecurityContext(manager);
        Blog blog = topicRightsManagement.setAllCanReadAllCanWrite(blogId, true, false);
        Assert.assertTrue(blog.isAllCanRead());
        Assert.assertFalse(blog.isAllCanWrite());
        TestUtils.addAndCheckCrawlLastModificationDateForTopic(blogId, crawlTimeStamps);
        // Normal User
        AuthenticationTestUtils.setSecurityContext(user);
        blog = topicRightsManagement.setAllCanReadAllCanWrite(blogId, true, true);
        Assert.assertTrue(blog.isAllCanRead());
        Assert.assertTrue(blog.isAllCanWrite());
        TestUtils.addAndCheckCrawlLastModificationDateForTopic(blogId, crawlTimeStamps);

        propertiesManager.updateClientConfigurationProperty(
                ClientProperty.ALLOW_ALL_CAN_READ_WRITE_FOR_ALL_USERS, Boolean.FALSE.toString());
        // Client Manager
        AuthenticationTestUtils.setSecurityContext(manager);
        blog = topicRightsManagement.setAllCanReadAllCanWrite(blogId, true, false);
        Assert.assertTrue(blog.isAllCanRead());
        Assert.assertFalse(blog.isAllCanWrite());
        TestUtils.addAndCheckCrawlLastModificationDateForTopic(blogId, crawlTimeStamps);

        // Normal User
        // Normal user is able to reset, but not to set.
        AuthenticationTestUtils.setSecurityContext(user);
        blog = topicRightsManagement.setAllCanReadAllCanWrite(blogId, true, true);
        Assert.assertTrue(blog.isAllCanRead());
        Assert.assertFalse(blog.isAllCanWrite());

        blog = topicRightsManagement.setAllCanReadAllCanWrite(blogId, false, false);
        Assert.assertFalse(blog.isAllCanRead());
        Assert.assertFalse(blog.isAllCanWrite());
        TestUtils.addAndCheckCrawlLastModificationDateForTopic(blogId, crawlTimeStamps);
        propertiesManager.updateClientConfigurationProperty(
                ClientProperty.ALLOW_ALL_CAN_READ_WRITE_FOR_ALL_USERS, Boolean.TRUE.toString());
    }
}
