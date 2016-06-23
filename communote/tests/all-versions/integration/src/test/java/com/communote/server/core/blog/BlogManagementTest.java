package com.communote.server.core.blog;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.communote.common.converter.IdentityConverter;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.blog.BlogAccessException;
import com.communote.server.api.core.blog.BlogData;
import com.communote.server.api.core.blog.BlogIdentifierValidationException;
import com.communote.server.api.core.blog.BlogManagement;
import com.communote.server.api.core.blog.BlogRightsManagement;
import com.communote.server.api.core.blog.BlogTO;
import com.communote.server.api.core.blog.CreationBlogTO;
import com.communote.server.api.core.blog.NonUniqueBlogIdentifierException;
import com.communote.server.api.core.blog.TopicPermissionManagement;
import com.communote.server.api.core.common.ClientAndChannelContextHolder;
import com.communote.server.api.core.common.NotFoundException;
import com.communote.server.api.core.config.type.ClientProperty;
import com.communote.server.api.core.note.NoteStoringTO;
import com.communote.server.api.core.property.PropertyManagement;
import com.communote.server.api.core.property.PropertyType;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.core.converter.blog.BlogToDetailBlogListItemConverter;
import com.communote.server.core.filter.ResultSpecification;
import com.communote.server.core.filter.listitems.blog.BlogTagListItem;
import com.communote.server.core.filter.listitems.blog.DetailBlogListItem;
import com.communote.server.core.query.QueryManagement;
import com.communote.server.core.security.AuthenticationHelper;
import com.communote.server.core.security.AuthenticationManagement;
import com.communote.server.core.security.SecurityHelper;
import com.communote.server.core.security.UserDetails;
import com.communote.server.core.vo.query.blog.BlogQuery;
import com.communote.server.core.vo.query.blog.BlogQueryParameters;
import com.communote.server.core.vo.query.blog.TopicAccessLevel;
import com.communote.server.core.vo.query.converters.BlogDataToBlogTagListItemQueryResultConverter;
import com.communote.server.model.blog.Blog;
import com.communote.server.model.note.Note;
import com.communote.server.model.note.NoteCreationSource;
import com.communote.server.model.property.StringProperty;
import com.communote.server.model.security.ChannelType;
import com.communote.server.model.user.User;
import com.communote.server.model.user.UserStatus;
import com.communote.server.service.NoteService;
import com.communote.server.test.CommunoteIntegrationTest;
import com.communote.server.test.util.AuthenticationTestUtils;
import com.communote.server.test.util.TestUtils;

/**
 * Tests for the blog management features
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class BlogManagementTest extends CommunoteIntegrationTest {

    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(BlogManagementTest.class);

    /**
     * First Test blog title
     */
    public final static String TEST_BLOG_TITLE = "Testblog";
    /**
     * Second Test blog title
     */
    public final static String TEST_BLOG_TITLE2 = "another Testblog";
    /**
     * Third Test blog title
     */
    public final static String TEST_BLOG_TITLE3 = "3rd Testblog";
    /**
     * First Test blog name identifier
     */
    public final static String TEST_BLOG_IDENTIFIER = "TestBlogIdentifier1";
    /**
     * Second Test blog name identifier
     */
    public final static String TEST_BLOG_IDENTIFIER2 = "TestBlogIdentifier2";
    /**
     * Third Test blog name identifier
     */
    public final static String TEST_BLOG_IDENTIFIER3 = "TestBlogIdentifier3";

    /**
     * Test blog description
     */
    public final static String TEST_BLOG_DESCRIPTION = "Blog Description";

    private User creator;
    private User admin;

    @Autowired
    private BlogManagement blogManagement;

    @Autowired
    private BlogRightsManagement topicRightsManagement;

    private void authenticateUser(User user) {
        ClientAndChannelContextHolder.setChannel(ChannelType.API);
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                new UserDetails(user, user.getAlias()), user.getPassword());

        ServiceLocator.findService(AuthenticationManagement.class).onSuccessfulAuthentication(auth);

        AuthenticationTestUtils.setSecurityContext(user);
    }

    /**
     * Cleanup
     */
    @AfterClass
    public void cleanUp() {
        AuthenticationTestUtils.setSecurityContext(creator);
        try {
            Blog blog = blogManagement.findBlogByIdentifier(TEST_BLOG_IDENTIFIER3);
            // remove the created test blogs
            if (blog != null) {
                blogManagement.deleteBlog(blog.getId(), null);
            }
            blog = blogManagement.findBlogByIdentifier(TEST_BLOG_IDENTIFIER2);
            if (blog != null) {
                blogManagement.deleteBlog(blog.getId(), null);
            }
            blog = blogManagement.findBlogByIdentifier(TEST_BLOG_IDENTIFIER);
            if (blog != null) {
                blogManagement.deleteBlog(blog.getId(), null);
            }
        } catch (Exception e) {
            LOGGER.error("Clean up after test failed. ", e);
        }

        resetDefaultTopicPermissionOptions();

        AuthenticationHelper.removeAuthentication();
    }

    private void createAndCheckPersonalTopic() throws NotFoundException, AuthorizationException {
        User user = TestUtils.createRandomUser(false);

        Assert.assertNotNull(user);
        Assert.assertNotNull(user.getId());
        Assert.assertEquals(user.getStatus(), UserStatus.ACTIVE);

        authenticateUser(user);
        Assert.assertEquals(SecurityHelper.getCurrentUserId(), user.getId());

        List<BlogTagListItem> managerTopics = getManageableTopics();

        Assert.assertTrue(
                managerTopics != null && managerTopics.size() == 1,
                "There should be exactly one manageable topic (=personal topic) for the new created user. But got: "
                        + (managerTopics == null ? "null" : managerTopics.size()) + " topics.");

        BlogTagListItem personalTopic = managerTopics.get(0);

        Assert.assertNotNull(personalTopic);

        StringProperty property = ServiceLocator.findService(PropertyManagement.class)
                .getObjectProperty(PropertyType.BlogProperty, personalTopic.getId(),
                        PropertyManagement.KEY_GROUP,
                        BlogManagement.PROPERTY_KEY_PERSONAL_TOPIC_USER_ID);
        Assert.assertNotNull(property,
                "The personal topic user id property is not set on the personal topic.");
        String id = property.getPropertyValue();
        Assert.assertNotNull(id,
                "The personal topic user id property is not set on the personal topic.");
        Assert.assertEquals(Long.parseLong(id), user.getId().longValue(),
                "The id of the propery should be the id of the user");
    }

    /**
     * Generates a complete BlogTO object where all values are set for a first Blog object. Using
     * this method from other test classes is discouraged.
     *
     * @return BlogTo object with values set for one specific Blog.
     */
    public CreationBlogTO generateBlogTO() {
        CreationBlogTO blog = generateBlogTOBasics();
        blog.setTitle(TEST_BLOG_TITLE);
        blog.setDescription(TEST_BLOG_DESCRIPTION);
        blog.setNameIdentifier(TEST_BLOG_IDENTIFIER);
        return blog;
    }

    /**
     * Generates a complete BlogTO object where all values are set for a second Blog object.
     *
     * @return BlogTo object with values set for one specific Blog.
     */
    private CreationBlogTO generateBlogTO2() {
        CreationBlogTO blog = generateBlogTOBasics();
        blog.setTitle(TEST_BLOG_TITLE2);
        blog.setDescription(TEST_BLOG_DESCRIPTION);
        blog.setNameIdentifier(TEST_BLOG_IDENTIFIER2);
        return blog;
    }

    /**
     * Generates a new BlogTO Object and sets the basic values for testing the BlogManagement
     * methods. These re the values, which are identical during the tests and all BlogTO object used
     * here.
     *
     * @return Newly generated BlogTO object with identical values of all BlogTO objects used during
     *         this test.
     */
    private CreationBlogTO generateBlogTOBasics() {
        CreationBlogTO blogDataObj = new CreationBlogTO();
        String[] tags = { "blogtag", TEST_BLOG_TITLE };
        blogDataObj.setUnparsedTags(tags);
        blogDataObj.setCreatorUserId(creator.getId());
        return blogDataObj;
    }

    /**
     *
     * @return all topic the current user is manager
     */
    private List<BlogTagListItem> getManageableTopics() {
        BlogQueryParameters queryParameters = new BlogQueryParameters();
        queryParameters.setAccessLevel(TopicAccessLevel.MANAGER);
        queryParameters.setUserId(SecurityHelper.assertCurrentUserId());

        BlogQuery<BlogData, BlogQueryParameters> query = new BlogQuery<BlogData, BlogQueryParameters>(
                BlogData.class);

        queryParameters.setResultSpecification(new ResultSpecification(0, 10));
        List<BlogTagListItem> managerTopics = ServiceLocator.findService(QueryManagement.class)
                .query(query, queryParameters,
                        new BlogDataToBlogTagListItemQueryResultConverter(Locale.ENGLISH));
        return managerTopics;
    }

    /**
     * Sets the initialize
     */
    @BeforeClass(dependsOnGroups = GROUP_INTEGRATION_TEST_SETUP)
    public void initialize() {
        creator = TestUtils.createRandomUser(false);
        Assert.assertNotNull(creator, "Creator could not be found");

        admin = TestUtils.createRandomUser(true);
        Assert.assertNotNull(admin, "admin could not be found");
    }

    private void resetDefaultTopicPermissionOptions() {
        AuthenticationTestUtils.setSecurityContext(admin);
        CommunoteRuntime
                .getInstance()
                .getConfigurationManager()
                .updateClientConfigurationProperty(ClientProperty.CREATE_PERSONAL_BLOG,
                        Boolean.toString(ClientProperty.DEFAULT_CREATE_PERSONAL_BLOG));
        CommunoteRuntime
                .getInstance()
                .getConfigurationManager()
                .updateClientConfigurationProperty(ClientProperty.ALLOW_TOPIC_CREATE_FOR_ALL_USERS,
                        Boolean.toString(ClientProperty.DEFAULT_ALLOW_TOPIC_CREATE_FOR_ALL_USERS));

        AuthenticationHelper.removeAuthentication();
    }

    /**
     * Set the client properties for personal topic creation and for all users creating topics
     *
     * @param createPersonalBlog
     * @param allowTopicCreate
     */
    private void setPersonalBlogAndAllowTopicCreateProperties(boolean createPersonalBlog,
            boolean allowTopicCreate) {

        AuthenticationTestUtils.setSecurityContext(admin);

        CommunoteRuntime
                .getInstance()
                .getConfigurationManager()
                .updateClientConfigurationProperty(ClientProperty.ALLOW_TOPIC_CREATE_FOR_ALL_USERS,
                        Boolean.toString(allowTopicCreate));

        Assert.assertEquals(ClientProperty.ALLOW_TOPIC_CREATE_FOR_ALL_USERS
                .getValue(ClientProperty.DEFAULT_ALLOW_TOPIC_CREATE_FOR_ALL_USERS),
                allowTopicCreate,
                "Property ClientProperty.ALLOW_TOPIC_CREATE_FOR_ALL_USERS should be set to true now.");

        CommunoteRuntime
                .getInstance()
                .getConfigurationManager()
                .updateClientConfigurationProperty(ClientProperty.CREATE_PERSONAL_BLOG,
                        Boolean.toString(createPersonalBlog));

        Assert.assertEquals(ClientProperty.CREATE_PERSONAL_BLOG
                .getValue(ClientProperty.DEFAULT_CREATE_PERSONAL_BLOG), createPersonalBlog,
                "Property for ClientProperty.CREATE_PERSONAL_BLOG should be set to true now.");

        AuthenticationHelper.removeAuthentication();
    }

    /**
     * Test whether global blog rights can be changed.
     *
     * @throws Exception
     *             in case the test failed
     */
    @Test(dependsOnGroups = { "DeleteBlog" })
    public void testChangeGlobalBlogRights() throws Exception {
        AuthenticationTestUtils.setSecurityContext(creator);
        Blog blog = blogManagement.findBlogByIdentifier(TEST_BLOG_IDENTIFIER);
        BlogToDetailBlogListItemConverter<DetailBlogListItem> converter;
        converter = new BlogToDetailBlogListItemConverter<DetailBlogListItem>(
                DetailBlogListItem.class, false, false, false, true, false, topicRightsManagement);
        DetailBlogListItem item = blogManagement.getBlogById(blog.getId(), converter);
        boolean newReadAccess = !item.isAllCanRead();
        boolean newWriteAccess = !item.isAllCanWrite();
        topicRightsManagement.setAllCanReadAllCanWrite(blog.getId(), newReadAccess, newWriteAccess);
        blog = blogManagement.findBlogByIdentifier(TEST_BLOG_IDENTIFIER);
        item = blogManagement.getBlogById(blog.getId(), converter);
        Assert.assertEquals(item.isAllCanRead(), newReadAccess,
                "Changing global read access failed.");
        Assert.assertEquals(item.isAllCanWrite(), newWriteAccess,
                "Changing global write access failed.");
        topicRightsManagement.setAllCanReadAllCanWrite(blog.getId(), !newReadAccess,
                !newWriteAccess);
        blog = blogManagement.findBlogByIdentifier(TEST_BLOG_IDENTIFIER);
        item = blogManagement.getBlogById(blog.getId(), converter);
        Assert.assertEquals(item.isAllCanRead(), !newReadAccess,
                "Changing global read access failed.");
        Assert.assertEquals(item.isAllCanWrite(), !newWriteAccess,
                "Changing global write access failed.");
    }

    /**
     * Test for creating a new blog.
     *
     * @throws Exception
     *             in case the test failed
     */
    @Test(groups = { "CreateBlog" })
    public void testCreateBlog() throws Exception {
        // test creation as client manager
        AuthenticationTestUtils.setManagerContext();
        CreationBlogTO blogTO = generateBlogTO();
        blogManagement.createBlog(blogTO);
        AuthenticationTestUtils.setSecurityContext(creator);
        Blog blog = blogManagement.findBlogByIdentifier(TEST_BLOG_IDENTIFIER);
        Assert.assertNotNull(blog);
        Assert.assertEquals(blog.getNameIdentifier(), TEST_BLOG_IDENTIFIER.toLowerCase());
        Assert.assertEquals(blog.getDescription(), TEST_BLOG_DESCRIPTION);
        Assert.assertEquals(blog.getTitle(), TEST_BLOG_TITLE);
        Assert.assertTrue(topicRightsManagement.userHasManagementAccess(blog.getId(),
                creator.getId()));
        // test creating a blog as another user that is not client manager
        blogTO = generateBlogTO();
        String alias = UUID.randomUUID().toString();
        blogTO.setNameIdentifier(alias);
        blogManagement.createBlog(blogTO);
        blog = blogManagement.findBlogByIdentifier(alias);
        Assert.assertNotNull(blog);
        Assert.assertEquals(blog.getNameIdentifier(), alias.toLowerCase());
        Assert.assertEquals(blog.getDescription(), TEST_BLOG_DESCRIPTION);
        Assert.assertEquals(blog.getTitle(), TEST_BLOG_TITLE);
        Assert.assertTrue(topicRightsManagement.userHasManagementAccess(blog.getId(),
                creator.getId()));
    }

    /**
     * Test for creating a new blog with an invalid alias.
     *
     * @throws Exception
     *             in case the test failed
     */
    @Test(dependsOnMethods = { "testCreateBlog" })
    public void testCreateBlogByAdminWithNoPermissionsForAllUsers() throws Exception {
        TopicPermissionManagement topicPermissionManagement = ServiceLocator
                .findService(TopicPermissionManagement.class);

        CreationBlogTO blogTO = new CreationBlogTO();
        blogTO.setTitle(UUID.randomUUID().toString());
        blogTO.setDescription(UUID.randomUUID().toString());
        blogTO.setNameIdentifier(UUID.randomUUID().toString());
        blogTO.setCreatorUserId(admin.getId());

        try {
            setPersonalBlogAndAllowTopicCreateProperties(true, false);

            AuthenticationTestUtils.setSecurityContext(admin);

            Assert.assertTrue(SecurityHelper.isClientManager());
            Assert.assertTrue(topicPermissionManagement
                    .hasPermissionForCreation(TopicPermissionManagement.PERMISSION_CREATE_TOPIC));

            Blog blog = blogManagement.createBlog(blogTO);

            Assert.assertNotNull(blog, "There should be a blog created for the admin.");
        } catch (BlogAccessException e) {
            // success if permission of exception matches
            Assert.fail("If it is a client manager the blog should be created.");
        } finally {
            // reset it to avoid failing other tests
            CommunoteRuntime
                    .getInstance()
                    .getConfigurationManager()
                    .updateClientConfigurationProperty(
                            ClientProperty.ALLOW_TOPIC_CREATE_FOR_ALL_USERS,
                            Boolean.TRUE.toString());

            // user should now be able to create again
            Assert.assertTrue(topicPermissionManagement
                    .hasPermissionForCreation(TopicPermissionManagement.PERMISSION_CREATE_TOPIC));

            AuthenticationHelper.removeAuthentication();
        }
    }

    /**
     * Test for creating a new blog with an invalid alias.
     *
     * @throws Exception
     *             in case the test failed
     */
    @Test(dependsOnMethods = { "testCreateBlog" })
    public void testCreateBlogInvalidAlias() throws Exception {
        CreationBlogTO blogTO = generateBlogTO();
        blogTO.setNameIdentifier("\u00A7$_/d");
        try {
            blogManagement.createBlog(blogTO);
            Assert.fail("Creating a blog with an invalid alias must not succeed");
        } catch (BlogIdentifierValidationException e) {
            // success
        }
    }

    /**
     * Test for creating a blog with the same name identifier.
     *
     * @throws Exception
     *             in case the test failed
     */
    @Test(groups = { "CreateBlog" }, dependsOnMethods = { "testCreateBlog" })
    public void testCreateBlogSameIdentifier() throws Exception {
        CreationBlogTO blogTO = generateBlogTO();
        blogTO.setTitle(TEST_BLOG_TITLE2 + " unique");
        try {
            blogManagement.createBlog(blogTO);
            Assert.fail("Adding the same blog twice should fail because the identifier must be unique!");
        } catch (NonUniqueBlogIdentifierException e) {
            // success
        }
    }

    /**
     * Test for creating a new blog with an invalid alias.
     *
     * @throws Exception
     *             in case the test failed
     */
    @Test(dependsOnMethods = { "testCreateBlog" })
    public void testCreateBlogWithNoPermissions() throws Exception {
        TopicPermissionManagement topicPermissionManagement = ServiceLocator
                .findService(TopicPermissionManagement.class);

        CreationBlogTO blogTO = new CreationBlogTO();
        blogTO.setTitle(UUID.randomUUID().toString());
        blogTO.setDescription(UUID.randomUUID().toString());
        blogTO.setNameIdentifier(UUID.randomUUID().toString());
        blogTO.setCreatorUserId(creator.getId());

        try {
            setPersonalBlogAndAllowTopicCreateProperties(true, false);

            AuthenticationTestUtils.setSecurityContext(creator);

            Assert.assertFalse(SecurityHelper.isClientManager());
            Assert.assertFalse(topicPermissionManagement
                    .hasPermissionForCreation(TopicPermissionManagement.PERMISSION_CREATE_TOPIC));

            blogManagement.createBlog(blogTO);

            Assert.fail("Should not created the blog.");
        } catch (BlogAccessException e) {
            // success if permission of exception matches
            Assert.assertEquals(e.getPermission(),
                    TopicPermissionManagement.PERMISSION_CREATE_TOPIC);
        } finally {
            // reset it to avoid failing other tests
            CommunoteRuntime
                    .getInstance()
                    .getConfigurationManager()
                    .updateClientConfigurationProperty(
                            ClientProperty.ALLOW_TOPIC_CREATE_FOR_ALL_USERS,
                            Boolean.TRUE.toString());

            // user should now be able to create again
            Assert.assertTrue(topicPermissionManagement
                    .hasPermissionForCreation(TopicPermissionManagement.PERMISSION_CREATE_TOPIC));

            AuthenticationHelper.removeAuthentication();
        }
    }

    /**
     * Test that the NO personal topic is created
     */
    @Test(dependsOnMethods = { "testCreateBlog" })
    public void testCreateNoPersonalBlog() {
        try {

            setPersonalBlogAndAllowTopicCreateProperties(false, true);

            User user = TestUtils.createRandomUser(false);

            Assert.assertNotNull(user);
            Assert.assertNotNull(user.getId());
            Assert.assertEquals(user.getStatus(), UserStatus.ACTIVE);

            authenticateUser(user);
            Assert.assertEquals(SecurityHelper.getCurrentUserId(), user.getId());

            List<BlogTagListItem> managerTopics = getManageableTopics();

            Assert.assertTrue(managerTopics == null || managerTopics.size() == 0,
                    "There should be no manageable topic for the new created user. But got: "
                            + (managerTopics == null ? "null" : managerTopics.size()) + " topics.");

        } finally {
            resetDefaultTopicPermissionOptions();

            AuthenticationHelper.removeAuthentication();
        }
    }

    /**
     * Test that the personal topic is created and the personal topic property is set correctly
     *
     * @throws AuthorizationException
     * @throws NotFoundException
     */
    @Test(dependsOnMethods = { "testCreateBlog" })
    public void testCreatePersonalBlog() throws NotFoundException, AuthorizationException {
        try {
            AuthenticationTestUtils.setSecurityContext(admin);

            setPersonalBlogAndAllowTopicCreateProperties(true, true);

            createAndCheckPersonalTopic();

        } finally {

            resetDefaultTopicPermissionOptions();

            AuthenticationHelper.removeAuthentication();
        }
    }

    /**
     * Test that the personal topic is created while
     * {@link ClientProperty#ALLOW_TOPIC_CREATE_FOR_ALL_USERS} is false and the personal topic
     * property is set correctly.
     *
     * @throws AuthorizationException
     * @throws NotFoundException
     */
    @Test(dependsOnMethods = { "testCreateBlog" })
    public void testCreatePersonalBlogWithAllUsersCreateTopicDisabled() throws NotFoundException,
            AuthorizationException {
        try {
            setPersonalBlogAndAllowTopicCreateProperties(true, false);

            createAndCheckPersonalTopic();

        } finally {

            resetDefaultTopicPermissionOptions();

            AuthenticationHelper.removeAuthentication();
        }
    }

    /**
     * Test for deleting an existing blog.
     *
     * @throws Exception
     *             in case the test failed
     */
    @Test(groups = "DeleteBlog", dependsOnGroups = "UpdateBlog")
    public void testDeleteBlog() throws Exception {
        AuthenticationTestUtils.setSecurityContext(creator);
        Blog blog = blogManagement.findBlogByIdentifier(TEST_BLOG_IDENTIFIER3);
        blogManagement.deleteBlog(blog.getId(), null);
        blog = blogManagement.findBlogByIdentifier(TEST_BLOG_IDENTIFIER3);
        Assert.assertNull(blog);
        blog = blogManagement.findBlogByIdentifier(TEST_BLOG_IDENTIFIER);
        Assert.assertNull(blog);

        blog = blogManagement.findBlogByIdentifier(TEST_BLOG_IDENTIFIER2);
        blogManagement.deleteBlog(blog.getId(), null);
        blog = blogManagement.findBlogByIdentifier(TEST_BLOG_IDENTIFIER2);
        Assert.assertNull(blog);

        blogManagement.createBlog(generateBlogTO());
        blogManagement.createBlog(generateBlogTO2());
        blog = blogManagement.findBlogByIdentifier(TEST_BLOG_IDENTIFIER);
        Assert.assertNotNull(blog);
        blog = blogManagement.findBlogByIdentifier(TEST_BLOG_IDENTIFIER2);
        Assert.assertNotNull(blog);
    }

    /**
     * Test that a SYSTEM note is not moved if its topic is deleted and a move-target is given.
     *
     * @throws Exception
     *             in case the test failed
     */
    @Test
    public void testDeleteTopicWithSystemNotes() throws Exception {
        User user = TestUtils.createRandomUser(false);
        AuthenticationTestUtils.setSecurityContext(user);
        CreationBlogTO blogTO = TestUtils.createRandomCreationBlogTO(user.getId(), true, true,
                null, null);
        blogTO.setCreateSystemNotes(true);
        Blog topic1 = blogManagement.createBlog(blogTO);
        blogTO = TestUtils.createRandomCreationBlogTO(user.getId(), true, true, null, null);
        blogTO.setCreateSystemNotes(true);
        Blog topic2 = blogManagement.createBlog(blogTO);
        NoteStoringTO note = TestUtils.createCommonNote(topic1, user.getId(), "Test");
        note.setCreationSource(NoteCreationSource.SYSTEM);
        NoteService noteService = ServiceLocator.findService(NoteService.class);
        Long noteId = noteService.createNote(note, null).getNoteId();
        // System notes should be deleted and not moved (as long as there are no comments)
        blogManagement.deleteBlog(topic1.getId(), topic2.getId());
        Assert.assertNull(blogManagement.getBlogById(topic1.getId(), new IdentityConverter<Blog>()));
        Assert.assertNull(noteService.getNote(noteId, new IdentityConverter<Note>()));
    }

    /**
     * Try to update a blog with another title and name identifier.
     *
     * @throws Exception
     *             in case the test failed
     */
    @Test(groups = { "UpdateBlog" }, dependsOnMethods = { "testUpdateBlogSameIdentifier" })
    public void testUpdateBlog() throws Exception {
        AuthenticationTestUtils.setSecurityContext(creator);
        Blog b = blogManagement.findBlogByIdentifier(TEST_BLOG_IDENTIFIER);
        BlogTO blogTO = generateBlogTO();
        blogTO.setTitle(TEST_BLOG_TITLE3);
        blogTO.setNameIdentifier(TEST_BLOG_IDENTIFIER3);
        Blog updatedBlog = blogManagement.updateBlog(b.getId(), blogTO);
        Assert.assertEquals(updatedBlog.getId(), b.getId());
        Assert.assertEquals(updatedBlog.getTitle(), TEST_BLOG_TITLE3);
        // must match lower case because it is converted into lower case
        Assert.assertEquals(updatedBlog.getNameIdentifier(), TEST_BLOG_IDENTIFIER3.toLowerCase());
        updatedBlog = blogManagement.findBlogByIdentifier(TEST_BLOG_IDENTIFIER3);
        Assert.assertNotNull(updatedBlog, "The blog to update was not found.");
        Assert.assertEquals(updatedBlog.getId(), b.getId());
        // old alias must not exist anymore
        Assert.assertNull(blogManagement.findBlogByIdentifier(TEST_BLOG_IDENTIFIER));
    }

    /**
     * Test for updating the alias of a blog to a value of an existing blog.
     *
     * @throws Exception
     *             in case the test failed
     */
    @Test(groups = { "UpdateBlog" }, dependsOnMethods = { "testCreateBlogSameIdentifier" })
    public void testUpdateBlogSameIdentifier() throws Exception {
        AuthenticationTestUtils.setSecurityContext(creator);
        try {
            blogManagement.createBlog(generateBlogTO2());
            Blog b = blogManagement.findBlogByIdentifier(TEST_BLOG_IDENTIFIER);
            BlogTO blogTO = generateBlogTO();
            blogTO.setNameIdentifier(TEST_BLOG_IDENTIFIER2);
            blogManagement.updateBlog(b.getId(), blogTO);
            Assert.fail();
        } catch (NonUniqueBlogIdentifierException e) {
            // success
        }
    }

}
