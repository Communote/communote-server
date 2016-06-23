package com.communote.server.test;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.communote.server.model.blog.Blog;
import com.communote.server.model.user.User;
import com.communote.server.test.util.TestUtils;

/**
 * This is not a real test. It is just an example on how using the CommunoteIntegrationTest.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class SimpleCommunoteIntegrationTest extends CommunoteIntegrationTest {
    /**
     * Creates a random blog.
     *
     * @throws Exception
     *             Exception.
     */
    @Test
    public void testCreateBlog() throws Exception {
        User user = TestUtils.createRandomUser(false);
        Blog blog = TestUtils.createRandomBlog(true, true, new User[] { user });
        Assert.assertEquals(blog.isAllCanRead(), true);
        Assert.assertEquals(blog.isAllCanWrite(), true);
        Assert.assertEquals(blog.getMembers().size(), 1);
    }

    /**
     * Creates a random user using the {@link TestUtils}.
     *
     * @throws Exception
     *             Exception.
     */
    @Test
    public void testCreateUser() throws Exception {
        User user = TestUtils.createRandomUser(false);
        Assert.assertNotNull(user);
    }
}
