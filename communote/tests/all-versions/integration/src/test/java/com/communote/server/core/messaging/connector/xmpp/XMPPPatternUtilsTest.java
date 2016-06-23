package com.communote.server.core.messaging.connector.xmpp;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.client.ClientNotFoundException;
import com.communote.server.api.core.config.ApplicationConfigurationPropertyConstant;
import com.communote.server.api.core.config.type.ApplicationPropertyXmpp;
import com.communote.server.core.messaging.connectors.xmpp.XMPPPatternUtils;
import com.communote.server.model.blog.Blog;
import com.communote.server.model.user.User;
import com.communote.server.test.CommunoteIntegrationTest;
import com.communote.server.test.util.AuthenticationTestUtils;
import com.communote.server.test.util.TestUtils;

/**
 * Test for the {@link XMPPPatternUtils}.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class XMPPPatternUtilsTest extends CommunoteIntegrationTest {

    private final static String BLOG_SUFFIX = "@" + UUID.randomUUID().toString();
    private final static String USER_SUFFIX = "@" + UUID.randomUUID().toString();
    private User user;
    private Blog blog;

    /**
     * Setup.
     *
     * @throws Exception
     *             Exception.
     */
    @BeforeClass(groups = "integration-test-setup")
    public void setup() throws Exception {
        Map<ApplicationConfigurationPropertyConstant, String> settings = new HashMap<ApplicationConfigurationPropertyConstant, String>();
        settings.put(ApplicationPropertyXmpp.BLOG_SUFFIX, BLOG_SUFFIX);
        settings.put(ApplicationPropertyXmpp.USER_SUFFIX, USER_SUFFIX);
        AuthenticationTestUtils.setManagerContext();
        CommunoteRuntime.getInstance().getConfigurationManager()
                .updateApplicationConfigurationProperties(settings);
        user = TestUtils.createRandomUser(false);
        blog = TestUtils.createRandomBlog(true, true, user);
    }

    /**
     * Test for {@link XMPPPatternUtils#extractBlogId(String)}
     */
    @Test
    public void testExtractBlogId() {
        Assert.assertEquals(
                XMPPPatternUtils.extractBlogId(blog.getNameIdentifier() + ".global" + BLOG_SUFFIX),
                blog.getId());
    }

    /**
     * Test for {@link XMPPPatternUtils#extractClient(String)}
     *
     * @throws ClientNotFoundException
     *             Exception.
     */
    @Test
    public void testExtractClient() throws ClientNotFoundException {
        Assert.assertEquals(
                XMPPPatternUtils.extractClient(UUID.randomUUID() + ".global" + BLOG_SUFFIX)
                .getClientId(), "global");

        try {
            XMPPPatternUtils.extractClient(UUID.randomUUID() + "." + UUID.randomUUID()
                    + BLOG_SUFFIX);
            Assert.fail("The client shouldn't exist.");
        } catch (ClientNotFoundException e) {
            // Okay
        }
    }

    /**
     * Test for {@link XMPPPatternUtils#extractClientIdFromUser(String)}
     */
    @Test
    public void testExtractClientIdFromUser() {
        Assert.assertEquals(
                XMPPPatternUtils.extractClientIdFromUser(UUID.randomUUID() + ".global"
                        + USER_SUFFIX), "global");
    }

    /**
     * Test for {@link XMPPPatternUtils#extractKenmeiUser(String)}
     */
    @Test
    public void testExtractKenmeiUser() {
        Assert.assertNull(XMPPPatternUtils.extractKenmeiUser(UUID.randomUUID() + ".global"
                + USER_SUFFIX));
        Assert.assertEquals(
                XMPPPatternUtils.extractKenmeiUser(user.getAlias() + ".global" + USER_SUFFIX)
                .getId(), user.getId());
    }

    /**
     * Test for {@link XMPPPatternUtils#getBlogSuffix()}
     */
    @Test
    public void testGetBlogSuffix() {
        Assert.assertEquals(XMPPPatternUtils.getBlogSuffix(), BLOG_SUFFIX);
    }

    /**
     * Test for {@link XMPPPatternUtils#getUserSuffix()}
     */
    @Test
    public void testGetUserSuffix() {
        Assert.assertEquals(XMPPPatternUtils.getUserSuffix(), USER_SUFFIX);
    }
}
