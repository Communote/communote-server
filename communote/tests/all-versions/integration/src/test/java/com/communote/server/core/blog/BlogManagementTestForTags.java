package com.communote.server.core.blog;

import java.util.HashSet;
import java.util.UUID;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.blog.BlogManagement;
import com.communote.server.api.core.blog.CreationBlogTO;
import com.communote.server.api.core.tag.TagStoreType;
import com.communote.server.api.core.tag.TagTO;
import com.communote.server.model.blog.Blog;
import com.communote.server.model.tag.Tag;
import com.communote.server.model.user.User;
import com.communote.server.test.CommunoteIntegrationTest;
import com.communote.server.test.util.AuthenticationTestUtils;
import com.communote.server.test.util.TestUtils;

/**
 * Tests for {@link BlogManagement} with focus on tagging.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class BlogManagementTestForTags extends CommunoteIntegrationTest {

    private User user;
    private BlogManagement blogManagement;
    private CreationBlogTO blogTO;
    private Long blogId;

    /**
     * Setup.
     */
    @BeforeClass(dependsOnGroups = "integration-test-setup")
    public void setup() {
        user = TestUtils.createRandomUser(false);
        AuthenticationTestUtils.setSecurityContext(user);
        blogManagement = ServiceLocator.instance().getService(BlogManagement.class);
    }

    /**
     * Test for creating a blog with tags
     *
     * @throws Exception
     *             Exception.
     */
    @Test
    public void testCreateBlogForTagTO() throws Exception {
        TagTO tag = new TagTO(UUID.randomUUID().toString(), TagStoreType.Types.BLOG);
        blogTO = new CreationBlogTO(false, false, UUID.randomUUID().toString(), false);
        blogTO.setCreatorUserId(user.getId());
        blogTO.setTags(new HashSet<TagTO>());
        blogTO.getTags().add(tag);
        Blog blog = blogManagement.createBlog(blogTO);
        blogId = blog.getId();
        Assert.assertEquals(blog.getTags().size(), 1);
        Assert.assertEquals(blog.getTags().iterator().next().getName(), tag.getDefaultName());
    }

    /**
     * Test for creating a blog with unparsed tags
     *
     * @throws Exception
     *             Exception.
     */
    @Test(dependsOnMethods = "testUpdateBlogForTagTO")
    public void testCreateBlogForUnparsedTag() throws Exception {
        String tag = UUID.randomUUID().toString();
        blogTO = new CreationBlogTO(false, false, UUID.randomUUID().toString(), false);
        blogTO.setCreatorUserId(user.getId());
        blogTO.setUnparsedTags(new String[] { tag });
        Blog blog = blogManagement.createBlog(blogTO);
        blogId = blog.getId();
        Assert.assertEquals(blog.getTags().size(), 1);
        Assert.assertEquals(blog.getTags().iterator().next().getName(), tag);
    }

    /**
     * Test for updating a blog with tags
     *
     * @throws Exception
     *             Exception.
     */
    @Test(dependsOnMethods = "testCreateBlogForTagTO")
    public void testUpdateBlogForTagTO() throws Exception {
        AuthenticationTestUtils.setSecurityContext(user);
        TagTO tag = new TagTO(UUID.randomUUID().toString(), TagStoreType.Types.BLOG);
        blogTO.getTags().add(tag);
        Blog blog = blogManagement.updateBlog(blogId, blogTO);
        Assert.assertEquals(blog.getTags().size(), 2);
        blogTO.getTags().clear();
        blogTO.getTags().add(tag);
        blog = blogManagement.updateBlog(blogId, blogTO);
        Assert.assertEquals(blog.getTags().size(), 1);
        Tag dbTag = blog.getTags().iterator().next();
        Assert.assertEquals(dbTag.getName(), tag.getDefaultName());
        Assert.assertNotNull(dbTag.getId());
        blogTO.getTags().clear();
        blog = blogManagement.updateBlog(blogId, blogTO);
        Assert.assertEquals(blog.getTags().size(), 0);
    }

    /**
     * Test for updating a blog with unparsed tags
     *
     * @throws Exception
     *             Exception.
     */
    @Test(dependsOnMethods = "testCreateBlogForUnparsedTag")
    public void testUpdateBlogForUnparsedTag() throws Exception {
        AuthenticationTestUtils.setSecurityContext(user);
        String tag = UUID.randomUUID().toString();
        blogTO.setUnparsedTags(new String[] { blogTO.getUnparsedTags()[0], tag });
        Blog blog = blogManagement.updateBlog(blogId, blogTO);
        Assert.assertEquals(blog.getTags().size(), 2);
        blogTO.setUnparsedTags(new String[] { tag });
        blog = blogManagement.updateBlog(blogId, blogTO);
        Assert.assertEquals(blog.getTags().size(), 1);
        Tag dbTag = blog.getTags().iterator().next();
        Assert.assertEquals(dbTag.getName(), tag);
        Assert.assertNotNull(dbTag.getId());
        blogTO.setUnparsedTags(null);
        blog = blogManagement.updateBlog(blogId, blogTO);
        Assert.assertEquals(blog.getTags().size(), 0);
    }
}
