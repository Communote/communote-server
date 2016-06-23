package com.communote.server.core.blog;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.math.RandomUtils;
import org.springframework.security.access.AccessDeniedException;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.blog.BlogAccessException;
import com.communote.server.api.core.blog.BlogManagement;
import com.communote.server.api.core.blog.BlogNotFoundException;
import com.communote.server.model.blog.Blog;
import com.communote.server.model.user.User;
import com.communote.server.persistence.blog.NoteDao;
import com.communote.server.test.CommunoteIntegrationTest;
import com.communote.server.test.util.AuthenticationTestUtils;
import com.communote.server.test.util.TestUtils;

/**
 * Testing the blog management functionality
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class BlogManagementTest2 extends CommunoteIntegrationTest {

    private Blog blogForDeletion;
    private Blog blogForMovingFrom;
    private Blog blogForMovingTo;
    private User user;
    private BlogManagement blogManagement;
    private NoteDao noteDao;
    private int numberOfMessages;

    /**
     * Setup.
     *
     * @param numberOfMessages
     *            The number of messages to generate.
     *
     * @throws Exception
     *             Exception.
     */
    @Parameters({ "numberOfMessages" })
    @BeforeClass(dependsOnGroups = "integration-test-setup")
    public void setup(@Optional("1000") String numberOfMessages) throws Exception {
        user = TestUtils.createRandomUser(false);
        blogForDeletion = TestUtils.createRandomBlog(true, true, user);
        blogForMovingFrom = TestUtils.createRandomBlog(true, true, user);
        blogForMovingTo = TestUtils.createRandomBlog(true, true, user);
        this.numberOfMessages = Integer.parseInt(numberOfMessages);
        for (int i = 1; i <= this.numberOfMessages; i++) {
            TestUtils.createAndStoreCommonNote(blogForDeletion, user.getId(), "Message " + i);
            TestUtils.createAndStoreCommonNote(blogForMovingFrom, user.getId(), "Message " + i);
        }
        blogManagement = ServiceLocator.instance().getService(BlogManagement.class);
        noteDao = ServiceLocator.findService(NoteDao.class);
        Assert.assertEquals(noteDao.getNotesForBlog(blogForDeletion.getId(), null, null).size(),
                this.numberOfMessages);
        Assert.assertEquals(noteDao.getNotesForBlog(blogForMovingFrom.getId(), null, null).size(),
                this.numberOfMessages);
    }

    /**
     * Test the blog deletion.
     *
     * @throws Exception
     *             Exception.
     */
    @Test
    public void testDeleteBlog() throws Exception {
        AuthenticationTestUtils.setSecurityContext(user);
        blogManagement.deleteBlog(blogForDeletion.getId(), null);
        Assert.assertEquals(noteDao.getNotesForBlog(blogForDeletion.getId(), null, null).size(), 0);
    }

    /**
     * Test the blog deletion but moves all notes.
     *
     * @throws Exception
     *             Exception.
     */
    @Test
    public void testDeleteBlogWithMovingNotes() throws Exception {
        AuthenticationTestUtils.setSecurityContext(user);
        blogManagement.deleteBlog(blogForMovingFrom.getId(), blogForMovingTo.getId());
        Assert.assertEquals(noteDao.getNotesForBlog(blogForMovingFrom.getId(), null, null).size(),
                0);
        Assert.assertEquals(noteDao.getNotesForBlog(blogForMovingTo.getId(), null, null).size(),
                numberOfMessages);
    }

    /**
     * Test for {@link BlogManagement#resetGlobalPermissions()} when a client manager calls the
     * method.
     *
     * @throws BlogAccessException
     * @throws BlogNotFoundException
     */
    @Test
    public void testResetGlobalPermissionsForClientManager() throws BlogNotFoundException,
    BlogAccessException {
        User user = TestUtils.createRandomUser(true);
        List<Long> blogIds = new ArrayList<Long>();
        for (int i = 10 + RandomUtils.nextInt(10); i > 0; i--) {
            blogIds.add(TestUtils.createRandomBlog(i % 2 == 0, i % 2 == 1, user).getId());
        }
        AuthenticationTestUtils.setSecurityContext(user);
        blogManagement.resetGlobalPermissions();
        for (Long blogId : blogIds) {
            Blog blog = blogManagement.getBlogById(blogId, false);
            Assert.assertFalse(blog.isAllCanRead());
            Assert.assertFalse(blog.isAllCanWrite());
        }
    }

    /**
     * Test for {@link BlogManagement#resetGlobalPermissions()} when a normal user calls the method.
     */
    @Test(expectedExceptions = AccessDeniedException.class)
    public void testResetGlobalPermissionsForNonClientManager() {
        User user = TestUtils.createRandomUser(false);
        AuthenticationTestUtils.setSecurityContext(user);
        blogManagement.resetGlobalPermissions();
    }

}
