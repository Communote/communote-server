package com.communote.server.persistence.blog;

import java.util.Collection;
import java.util.Iterator;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.blog.BlogData;
import com.communote.server.model.blog.Blog;
import com.communote.server.model.user.User;
import com.communote.server.test.CommunoteIntegrationTest;
import com.communote.server.test.util.AuthenticationTestUtils;
import com.communote.server.test.util.TestUtils;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class BlogDaoTest extends CommunoteIntegrationTest {
    /**
     * Tests {@link BlogDao#getLastUsedBlogs(Long, int)}.
     *
     * @throws InterruptedException
     *             Exception
     */
    @Test
    public void testGetLastUsedBlogs() throws InterruptedException {
        User user1 = TestUtils.createRandomUser(false);
        User user2 = TestUtils.createRandomUser(false);
        Blog blog1 = TestUtils.createRandomBlog(true, true, user2);
        Blog blog2 = TestUtils.createRandomBlog(true, true, user1);
        Blog blog3 = TestUtils.createRandomBlog(true, true, user1);
        Blog blog4 = TestUtils.createRandomBlog(true, true, user2);
        Blog blog5 = TestUtils.createRandomBlog(false, true, user1);

        BlogDao blogDao = ServiceLocator.findService(BlogDao.class);
        Collection<BlogData> lastUsedBlogs = blogDao.getLastUsedBlogs(user1.getId(), 3);
        Assert.assertTrue(lastUsedBlogs.isEmpty());

        TestUtils.createAndStoreCommonNote(blog1, user1.getId(), "test");

        lastUsedBlogs = blogDao.getLastUsedBlogs(user1.getId(), 3);
        Iterator<BlogData> iterator = lastUsedBlogs.iterator();
        Assert.assertEquals(lastUsedBlogs.size(), 1);
        Assert.assertEquals(iterator.next().getId(), blog1.getId());
        Thread.sleep(1000);
        TestUtils.createAndStoreCommonNote(blog2, user1.getId(), "test");

        lastUsedBlogs = blogDao.getLastUsedBlogs(user1.getId(), 3);
        iterator = lastUsedBlogs.iterator();
        Assert.assertEquals(lastUsedBlogs.size(), 2);
        Assert.assertEquals(iterator.next().getId(), blog2.getId());
        Assert.assertEquals(iterator.next().getId(), blog1.getId());
        Thread.sleep(1000);
        TestUtils.createAndStoreCommonNote(blog1, user1.getId(), "test");

        lastUsedBlogs = blogDao.getLastUsedBlogs(user1.getId(), 3);
        iterator = lastUsedBlogs.iterator();
        Assert.assertEquals(lastUsedBlogs.size(), 2);
        Assert.assertEquals(iterator.next().getId(), blog1.getId());
        Assert.assertEquals(iterator.next().getId(), blog2.getId());
        Thread.sleep(1000);
        TestUtils.createAndStoreCommonNote(blog3, user1.getId(), "test");

        lastUsedBlogs = blogDao.getLastUsedBlogs(user1.getId(), 3);
        iterator = lastUsedBlogs.iterator();
        Assert.assertEquals(lastUsedBlogs.size(), 3);
        Assert.assertEquals(iterator.next().getId(), blog3.getId());
        Assert.assertEquals(iterator.next().getId(), blog1.getId());
        Assert.assertEquals(iterator.next().getId(), blog2.getId());
        Thread.sleep(1000);
        TestUtils.createAndStoreCommonNote(blog4, user1.getId(), "test");

        lastUsedBlogs = blogDao.getLastUsedBlogs(user1.getId(), 3);
        iterator = lastUsedBlogs.iterator();
        Assert.assertEquals(lastUsedBlogs.size(), 3);
        Assert.assertEquals(iterator.next().getId(), blog4.getId());
        Assert.assertEquals(iterator.next().getId(), blog3.getId());
        Assert.assertEquals(iterator.next().getId(), blog1.getId());
        Thread.sleep(1000);
        TestUtils.createAndStoreCommonNote(blog5, user1.getId(), "test");

        lastUsedBlogs = blogDao.getLastUsedBlogs(user1.getId(), 3);
        iterator = lastUsedBlogs.iterator();
        Assert.assertEquals(lastUsedBlogs.size(), 3);
        Assert.assertEquals(iterator.next().getId(), blog5.getId());
        Assert.assertEquals(iterator.next().getId(), blog4.getId());
        Assert.assertEquals(iterator.next().getId(), blog3.getId());
        Thread.sleep(1000);
        TestUtils.createAndStoreCommonNote(blog1, user1.getId(), "test");

        lastUsedBlogs = blogDao.getLastUsedBlogs(user1.getId(), 3);
        iterator = lastUsedBlogs.iterator();
        Assert.assertEquals(lastUsedBlogs.size(), 3);
        Assert.assertEquals(iterator.next().getId(), blog1.getId());
        Assert.assertEquals(iterator.next().getId(), blog5.getId());
        Assert.assertEquals(iterator.next().getId(), blog4.getId());

    }

    /**
     * Tests {@link BlogDao#getMostUsedBlogs(Long, int, int)}.
     *
     * Setting the valid day range is not considered here.
     */
    @Test
    public void testGetMostUsedBlogs() {
        User user1 = TestUtils.createRandomUser(false);
        User user2 = TestUtils.createRandomUser(false);
        Blog blog1 = TestUtils.createRandomBlog(true, true, user2);
        Blog blog2 = TestUtils.createRandomBlog(true, true, user1);
        Blog blog3 = TestUtils.createRandomBlog(true, true, user1);
        Blog blog4 = TestUtils.createRandomBlog(true, true, user2);
        Blog blog5 = TestUtils.createRandomBlog(false, true, user1);

        BlogDao blogDao = ServiceLocator.findService(BlogDao.class);
        AuthenticationTestUtils.setSecurityContext(user1);
        Collection<BlogData> mostUsedBlogs = blogDao.getMostUsedBlogs(user1.getId(), 3, 180);
        Assert.assertEquals(mostUsedBlogs.size(), 0);

        // One blog
        TestUtils.createAndStoreCommonNote(blog1, user1.getId(), "test");

        AuthenticationTestUtils.setSecurityContext(user1);
        mostUsedBlogs = blogDao.getMostUsedBlogs(user1.getId(), 3, 180);
        Iterator<BlogData> iterator = mostUsedBlogs.iterator();
        Assert.assertEquals(mostUsedBlogs.size(), 1);
        Assert.assertEquals(iterator.next().getId(), blog1.getId());

        // Two blogs
        TestUtils.createAndStoreCommonNote(blog1, user1.getId(), "test");
        TestUtils.createAndStoreCommonNote(blog2, user1.getId(), "test");

        AuthenticationTestUtils.setSecurityContext(user1);
        mostUsedBlogs = blogDao.getMostUsedBlogs(user1.getId(), 3, 180);
        iterator = mostUsedBlogs.iterator();
        Assert.assertEquals(mostUsedBlogs.size(), 2);
        Assert.assertEquals(iterator.next().getId(), blog1.getId());
        Assert.assertEquals(iterator.next().getId(), blog2.getId());

        // Three blogs
        TestUtils.createAndStoreCommonNote(blog1, user1.getId(), "test");
        TestUtils.createAndStoreCommonNote(blog2, user1.getId(), "test");
        TestUtils.createAndStoreCommonNote(blog3, user1.getId(), "test");

        AuthenticationTestUtils.setSecurityContext(user1);
        mostUsedBlogs = blogDao.getMostUsedBlogs(user1.getId(), 3, 180);
        iterator = mostUsedBlogs.iterator();
        Assert.assertEquals(mostUsedBlogs.size(), 3);
        Assert.assertEquals(iterator.next().getId(), blog1.getId());
        Assert.assertEquals(iterator.next().getId(), blog2.getId());
        Assert.assertEquals(iterator.next().getId(), blog3.getId());

        // Four blogs, but still only 3 blogs returned.
        TestUtils.createAndStoreCommonNote(blog1, user1.getId(), "test");
        TestUtils.createAndStoreCommonNote(blog2, user1.getId(), "test");
        TestUtils.createAndStoreCommonNote(blog3, user1.getId(), "test");
        TestUtils.createAndStoreCommonNote(blog4, user1.getId(), "test");

        AuthenticationTestUtils.setSecurityContext(user1);
        mostUsedBlogs = blogDao.getMostUsedBlogs(user1.getId(), 3, 180);
        iterator = mostUsedBlogs.iterator();
        Assert.assertEquals(mostUsedBlogs.size(), 3);
        Assert.assertEquals(iterator.next().getId(), blog1.getId());
        Assert.assertEquals(iterator.next().getId(), blog2.getId());
        Assert.assertEquals(iterator.next().getId(), blog3.getId());

        // Fife blogs, but still only 3 blogs returned
        TestUtils.createAndStoreCommonNote(blog1, user1.getId(), "test");
        TestUtils.createAndStoreCommonNote(blog2, user1.getId(), "test");
        TestUtils.createAndStoreCommonNote(blog3, user1.getId(), "test");
        TestUtils.createAndStoreCommonNote(blog4, user1.getId(), "test");
        TestUtils.createAndStoreCommonNote(blog5, user1.getId(), "test");

        AuthenticationTestUtils.setSecurityContext(user1);
        mostUsedBlogs = blogDao.getMostUsedBlogs(user1.getId(), 3, 180);
        iterator = mostUsedBlogs.iterator();
        Assert.assertEquals(mostUsedBlogs.size(), 3);
        Assert.assertEquals(iterator.next().getId(), blog1.getId());
        Assert.assertEquals(iterator.next().getId(), blog2.getId());
        Assert.assertEquals(iterator.next().getId(), blog3.getId());

        // Rearrange order
        TestUtils.createAndStoreCommonNote(blog4, user1.getId(), "test");
        TestUtils.createAndStoreCommonNote(blog4, user1.getId(), "test");
        TestUtils.createAndStoreCommonNote(blog4, user1.getId(), "test");
        TestUtils.createAndStoreCommonNote(blog4, user1.getId(), "test");
        TestUtils.createAndStoreCommonNote(blog4, user1.getId(), "test");
        TestUtils.createAndStoreCommonNote(blog5, user1.getId(), "test");
        TestUtils.createAndStoreCommonNote(blog5, user1.getId(), "test");
        TestUtils.createAndStoreCommonNote(blog5, user1.getId(), "test");
        TestUtils.createAndStoreCommonNote(blog5, user1.getId(), "test");
        TestUtils.createAndStoreCommonNote(blog5, user1.getId(), "test");

        mostUsedBlogs = blogDao.getMostUsedBlogs(user1.getId(), 3, 180);
        iterator = mostUsedBlogs.iterator();
        Assert.assertEquals(mostUsedBlogs.size(), 3);
        Assert.assertEquals(iterator.next().getId(), blog4.getId());
        Assert.assertEquals(iterator.next().getId(), blog5.getId());
        Assert.assertEquals(iterator.next().getId(), blog1.getId());
    }
}
