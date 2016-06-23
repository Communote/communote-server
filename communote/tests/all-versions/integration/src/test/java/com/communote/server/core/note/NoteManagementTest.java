package com.communote.server.core.note;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.UUID;

import org.apache.commons.lang.math.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.communote.common.converter.IdentityConverter;
import com.communote.common.string.StringEscapeHelper;
import com.communote.common.util.PageableList;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.attachment.AttachmentData;
import com.communote.server.api.core.blog.BlogAccessException;
import com.communote.server.api.core.blog.BlogManagement;
import com.communote.server.api.core.blog.BlogNotFoundException;
import com.communote.server.api.core.blog.CreationBlogTO;
import com.communote.server.api.core.common.NotFoundException;
import com.communote.server.api.core.note.NoteContentType;
import com.communote.server.api.core.note.NoteData;
import com.communote.server.api.core.note.NoteManagementAuthorizationException;
import com.communote.server.api.core.note.NoteRenderContext;
import com.communote.server.api.core.note.NoteRenderMode;
import com.communote.server.api.core.note.NoteStoringTO;
import com.communote.server.api.core.note.processor.NoteStoringPreProcessorException;
import com.communote.server.api.core.property.PropertyManagement;
import com.communote.server.api.core.property.PropertyType;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.core.blog.FavoriteManagement;
import com.communote.server.core.blog.NoteNotFoundException;
import com.communote.server.core.crc.RepositoryConnectorDelegate;
import com.communote.server.core.query.QueryManagement;
import com.communote.server.core.security.AuthenticationHelper;
import com.communote.server.core.storing.ResourceStoringManagement;
import com.communote.server.core.user.note.UserNoteEntityService;
import com.communote.server.core.vo.blog.AutosaveNoteData;
import com.communote.server.core.vo.blog.NoteModificationResult;
import com.communote.server.core.vo.blog.NoteModificationStatus;
import com.communote.server.core.vo.content.AttachmentTO;
import com.communote.server.core.vo.query.QueryDefinitionRepository;
import com.communote.server.core.vo.query.TaggingCoreItemUTPExtension;
import com.communote.server.core.vo.query.logical.AtomicTagFormula;
import com.communote.server.core.vo.query.note.SimpleNoteListItemToNoteDataQueryResultConverter;
import com.communote.server.core.vo.query.post.NoteQuery;
import com.communote.server.core.vo.query.post.NoteQueryParameters;
import com.communote.server.core.vo.user.note.UserNoteEntityTO;
import com.communote.server.core.vo.uti.UserNotificationResult;
import com.communote.server.model.attachment.Attachment;
import com.communote.server.model.attachment.AttachmentStatus;
import com.communote.server.model.blog.Blog;
import com.communote.server.model.note.Note;
import com.communote.server.model.user.User;
import com.communote.server.model.user.UserNoteProperty;
import com.communote.server.persistence.blog.NoteDao;
import com.communote.server.persistence.crc.ContentRepositoryException;
import com.communote.server.persistence.user.UserDao;
import com.communote.server.persistence.user.UserNotePropertyDao;
import com.communote.server.service.NoteService;
import com.communote.server.test.CommunoteIntegrationTest;
import com.communote.server.test.util.AuthenticationTestUtils;
import com.communote.server.test.util.TestUtils;

/**
 * Class for testing the NoteManagement.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
@Test(singleThreaded = true, testName = "blog")
public class NoteManagementTest extends CommunoteIntegrationTest {

    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(NoteManagementTest.class);
    /**
     * local blog identifier for having local test blog which does not interfere with blogmanagement
     * tests
     */
    private static final String TEST_BLOG_IDENTIFIER1 = "createPostsBlogIdent1";
    /**
     * local blog identifier for having local test blog which does not interfere with blogmanagement
     * tests
     */
    private static final String TEST_BLOG_IDENTIFIER2 = "createPostsBlogIdent2";

    /**
     * an existing user
     */
    public static final String NOT_EXISTING_USER = "full";

    /**
     * a tag for blog posts
     */
    public static final String POST_TAG = "Blogpost";

    /**
     * a tag for comments
     */
    public static final String COMMENT_TAG = "Postcomment";
    /**
     * post content with embedded tags
     */
    public static final String POST_CONTENT_1 = "Test post with embedded tag #" + POST_TAG + ".";
    /**
     * post content with embedded tags
     */
    public static final String POST_CONTENT_2 = "Another test post with embedded tags #test and #cool.";
    /**
     * post content for adding an embedded blog identifier for implicit cross posting
     */
    public static final String POST_CONTENT_3 = "Another test post with embedded blog";
    /**
     * post content with embedded users to notify
     */
    public static final String POST_CONTENT_4 = "Another test post with embedded users @"
            + NOT_EXISTING_USER + ", @";
    /**
     * some text representing content of the comment
     */
    public static final String COMMENT_CONTENT_1 = "A comment with no special content.";
    /**
     * some text representing content of the comment
     */
    public static final String POST_UPDATE_CONTENT = "Content of the updated post without embedded tag.";

    private static final HashMap<String, String> UNIQUE_TAG_POOL = new HashMap<String, String>();

    static {
        UNIQUE_TAG_POOL.put("testCreateNote", "post_test_1");
        UNIQUE_TAG_POOL.put("testCreateNoteWithAttachment", "post_test_2");
        UNIQUE_TAG_POOL.put("testCreateComment", "post_test_3");
        UNIQUE_TAG_POOL.put("testCreateCrossPost", "post_test_4");
        UNIQUE_TAG_POOL.put("testCreateImplicitCrossPost", "post_test_5");
        UNIQUE_TAG_POOL.put("testUpdateNote", "post_test_6");
    }

    private User user;
    @Autowired
    private NoteService noteService;
    @Autowired
    private PropertyManagement propertyManagement;

    @Autowired
    private BlogManagement blogManagement;

    private ResourceStoringManagement resourceStoringManagement;

    /**
     * Checks some basic properties of the created post.
     *
     * @param item
     *            the post list item of the created post
     * @param storingTO
     *            the TO used for the post creation
     * @param content
     *            the content the created post must have
     */
    private void checkBasicAssertions(NoteData item, NoteStoringTO storingTO, String content) {
        // remove html from post content
        String cleanedContent = item.getContent();
        cleanedContent = StringEscapeHelper.removeXmlMarkup(cleanedContent);
        Assert.assertEquals(cleanedContent, content, "Content of created post does not match.");
        Assert.assertNotNull(item.getLastModificationDate(), "Post has no modification date");
        Assert.assertNotNull(item.getCreationDate(), "Post has no creation date");
    }

    /**
     * Checks whether the post was created successfully and has the specified warnings.
     *
     * @param result
     *            the result object returned from post creation
     * @param unresolvableUsers
     *            the expected number of unresolvable user aliases
     * @param uninformableUsers
     *            the expected number of uninformabel users
     * @param unresolvableBlogs
     *            the expected number of unresolvable blogs
     * @param unwritableBlogs
     *            the expected number of unwritable blogs
     */
    private void checkCreationSuccessWithWarnings(NoteModificationResult result,
            int unresolvableUsers, int uninformableUsers, int unresolvableBlogs, int unwritableBlogs) {
        Assert.assertEquals(result.getStatus(), NoteModificationStatus.SUCCESS,
                "Post creation failed with status " + result.getStatus());
        UserNotificationResult nr = result.getUserNotificationResult();

        // test warnings
        Assert.assertEquals(result.getUnresolvableBlogs().size(), unresolvableBlogs, "Found "
                + result.getUnresolvableBlogs().size() + " unresolvable blogs, expected "
                + unresolvableBlogs);
        Assert.assertEquals(result.getUnwritableBlogs().size(), unwritableBlogs, "Found "
                + result.getUnwritableBlogs().size() + " unwritable blogs, expected "
                + unwritableBlogs);
        Assert.assertEquals(nr.getUninformableUsers().size(), uninformableUsers, "Found "
                + nr.getUninformableUsers().size() + " uninformable users, expeted "
                + uninformableUsers);
        Assert.assertEquals(nr.getUnresolvableUsers().size(), unresolvableUsers, "Found "
                + nr.getUnresolvableUsers().size() + " unresolvable users, expected "
                + unresolvableUsers);
    }

    /**
     * Class-wide initialization.
     *
     * @throws Exception
     *             in case the test failed
     */
    @BeforeClass(dependsOnGroups = "integration-test-setup")
    public void classInitialze() throws Exception {
        resourceStoringManagement = ServiceLocator.findService(ResourceStoringManagement.class);

        user = TestUtils.createRandomUser(true);
        AuthenticationTestUtils.setSecurityContext(user);
        // check for test blogs, if not existing create them
        Blog testBlog = blogManagement.findBlogByIdentifier(TEST_BLOG_IDENTIFIER1);
        CreationBlogTO blogTO = new CreationBlogTO();
        blogTO.setDescription("test blog for note creation");
        blogTO.setCreatorUserId(user.getId());
        if (testBlog == null) {
            blogTO.setNameIdentifier(TEST_BLOG_IDENTIFIER1);
            blogTO.setTitle("Title of " + TEST_BLOG_IDENTIFIER1);
            blogManagement.createBlog(blogTO);
        }
        testBlog = blogManagement.findBlogByIdentifier(TEST_BLOG_IDENTIFIER2);
        if (testBlog == null) {
            blogTO.setNameIdentifier(TEST_BLOG_IDENTIFIER2);
            blogTO.setTitle("Title of " + TEST_BLOG_IDENTIFIER2);
            blogManagement.createBlog(blogTO);
        }
    }

    /**
     * Cleanup method.
     */
    @AfterClass(dependsOnGroups = "integration-test-setup")
    public void cleanUp() {
        // remove the created test blogs
        try {
            Blog testBlog = blogManagement.findBlogByIdentifier(TEST_BLOG_IDENTIFIER1);
            if (testBlog != null) {
                blogManagement.deleteBlog(testBlog.getId(), null);
            }
            testBlog = blogManagement.findBlogByIdentifier(TEST_BLOG_IDENTIFIER2);
            if (testBlog != null) {
                blogManagement.deleteBlog(testBlog.getId(), null);
            }
            // TODO remove created tags - but how?
        } catch (Exception e) {
            LOGGER.error("Clean up after test failed. ", e);
        }
        AuthenticationHelper.removeAuthentication();
    }

    /**
     * Create a note with the given user in the given blog and add an attachement with the given
     * content to it
     *
     * @param user
     * @param blog
     * @param fileName
     * @param fileContent
     * @return
     * @throws AuthorizationException
     * @throws BlogNotFoundException
     * @throws NoteManagementAuthorizationException
     * @throws NoteStoringPreProcessorException
     */
    private NoteData createNoteWithAttachment(User user, Blog blog, final String fileName,
            final String fileContent) throws AuthorizationException, BlogNotFoundException,
            NoteManagementAuthorizationException, NoteStoringPreProcessorException {
        NoteStoringTO storingTO = TestUtils.generateCommonNoteStoringTO(user, blog);
        // add unique tag for retrieval after creation
        String uniqueTag = UUID.randomUUID().toString();
        storingTO.setUnparsedTags(uniqueTag);

        TestUtils.addAttachment(storingTO, fileName, fileContent);

        checkCreationSuccessWithWarnings(noteService.createNote(storingTO, null), 0, 0, 0, 0);
        NoteData item = getPostWithTag(uniqueTag, storingTO.getBlogId(), storingTO.getCreatorId());
        checkBasicAssertions(item, storingTO, fileContent);
        Assert.assertEquals(item.getAttachments().size(), 1);

        AttachmentData cr = item.getAttachments().get(0);
        Assert.assertEquals(cr.getFileName(), fileName);

        return item;
    }

    /**
     * Initializes a NoteStoringPostTO with common data. The failOnX properties are all set to
     * false.
     *
     * @return the initialized transfer object
     * @throws BlogAccessException
     *             in case the user has no access to the topic
     */
    private NoteStoringTO generateCommonNoteStoringTO() throws BlogAccessException {
        AuthenticationTestUtils.setSecurityContext(user);

        Blog testBlog = blogManagement.findBlogByIdentifier(TEST_BLOG_IDENTIFIER1);
        Assert.assertNotNull(testBlog, "Blog cannot be null");
        return TestUtils.generateCommonNoteStoringTO(user, testBlog);
    }

    /**
     * Returns a UTP that has a unique tag. To succeed there must not be more than one post.
     *
     * @param tag
     *            the unique tag
     * @param blogId
     *            the id of the blog where the post is to be found
     * @param userId
     *            the user ID of the creator
     * @return the UTP in the form a list item
     */
    private NoteData getPostWithTag(String tag, Long blogId, Long userId) {
        NoteQuery queryDef = QueryDefinitionRepository.instance().getQueryDefinition(
                NoteQuery.class);
        final NoteQueryParameters queryInstance = queryDef.createInstance();
        TaggingCoreItemUTPExtension ext = new TaggingCoreItemUTPExtension();
        ext.setBlogId(blogId);
        ext.setUserId(userId);
        queryInstance.setTypeSpecificExtension(ext);
        queryInstance.setLogicalTags(new AtomicTagFormula(tag, false));

        NoteRenderContext renderContext = new NoteRenderContext(NoteRenderMode.HTML, Locale.ENGLISH);
        PageableList<NoteData> items = ServiceLocator
                .instance()
                .getService(QueryManagement.class)
                .query(queryDef,
                        queryInstance,
                        new SimpleNoteListItemToNoteDataQueryResultConverter<NoteData>(
                                NoteData.class, renderContext));
        Assert.assertEquals(items.size(), 1, "The note tagged with " + tag
                + " has wrong count in blog with id " + blogId);
        return items.get(0);
    }

    /**
     * Test for storing a post as draft and retrieving it via getDraft.
     */
    @Test(groups = { "CreateBlogPost" }, dependsOnMethods = { "testCreateNoteWithAttachment" })
    public void testAutosave() throws Exception {
        AuthenticationTestUtils.setSecurityContext(user);
        testAutosaveForCreate();
        testAutosaveForReply();
        testAutosaveForEdit();
        testAutosaveForCreateWithChangedBlog();
    }

    /**
     * This method creates a draft.
     */
    private void testAutosaveForCreate() throws Exception {
        NoteStoringTO draft = generateCommonNoteStoringTO();
        draft.setContent(POST_CONTENT_1);
        draft.setPublish(false);
        try {
            NoteModificationResult draftResult = noteService.createNote(draft, null);
            Assert.assertNotNull(draftResult.getNoteId());
            AutosaveNoteData loadedDraft = noteService
                    .getAutosave(null, null, null, Locale.ENGLISH);
            Assert.assertEquals(draftResult.getNoteId(), loadedDraft.getId());
        } catch (BlogNotFoundException e) {
            Assert.fail("Unexpected exception " + e.getMessage());
        } catch (NoteManagementAuthorizationException e) {
            Assert.fail("Unexpected exception " + e.getMessage());
        } catch (NoteStoringPreProcessorException e) {
            Assert.fail("Unexpected exception " + e.getMessage());
        }
    }

    /**
     * This method creates a draft.
     */
    private void testAutosaveForCreateWithChangedBlog() throws Exception {
        AutosaveNoteData loadedAutosave = noteService.getAutosave(null, null, null, Locale.ENGLISH);
        NoteStoringTO draft = generateCommonNoteStoringTO();
        draft.setContent(POST_CONTENT_1);
        // reference the previous autosave
        // draft.setAutosaveNoteId(loadedAutosave.getNoteId());
        // set another blog
        Long newBlogId = blogManagement.findBlogByIdentifier(TEST_BLOG_IDENTIFIER2).getId();
        draft.setBlogId(newBlogId);
        draft.setPublish(false);
        NoteModificationResult draftResult = noteService.createNote(draft, null);
        Assert.assertNotNull(draftResult.getNoteId());
        // check if autosave was updated
        AutosaveNoteData loadedDraft = noteService.getAutosave(null, null, null, Locale.ENGLISH);
        Assert.assertNotNull(loadedDraft);
        Assert.assertEquals(loadedAutosave.getId(), loadedDraft.getId());
        Assert.assertEquals(draftResult.getNoteId(), loadedDraft.getId());
        Assert.assertEquals(loadedDraft.getBlog().getId(), newBlogId);
    }

    /**
     * This method creates a post as update to another post and tries to retrieve the draft.
     */
    private void testAutosaveForEdit() throws Exception {
        AuthenticationTestUtils.setSecurityContext(user);
        NoteStoringTO simplePost = generateCommonNoteStoringTO();
        simplePost.setContent(POST_CONTENT_1);
        NoteStoringTO editOf = generateCommonNoteStoringTO();
        editOf.setContent(POST_CONTENT_2);
        editOf.setPublish(false);
        NoteModificationResult simplePostResult = noteService.createNote(simplePost, null);
        NoteModificationResult edit = noteService.updateNote(editOf, simplePostResult.getNoteId(),
                null, false);
        AutosaveNoteData loadedDraft = noteService.getAutosave(simplePostResult.getNoteId(), null,
                null, Locale.ENGLISH);
        Assert.assertEquals(edit.getNoteId(), loadedDraft.getId());
        Assert.assertEquals(loadedDraft.getOriginalNoteId(), simplePostResult.getNoteId());
    }

    /**
     * This method creates a comment as draft and tries to retrieve it.
     */
    private void testAutosaveForReply() throws Exception {
        NoteStoringTO simplePost = generateCommonNoteStoringTO();
        simplePost.setContent(POST_CONTENT_1);
        NoteStoringTO commentToSimplePost = generateCommonNoteStoringTO();
        commentToSimplePost.setContent(POST_CONTENT_2);
        commentToSimplePost.setPublish(false);
        NoteModificationResult simplePostResult = noteService.createNote(simplePost, null);
        Long parentNoteId = simplePostResult.getNoteId();
        commentToSimplePost.setParentNoteId(parentNoteId);
        NoteModificationResult comment = noteService.createNote(commentToSimplePost, null);
        AutosaveNoteData loadedDraft = noteService.getAutosave(null, parentNoteId, null,
                Locale.ENGLISH);
        Assert.assertEquals(comment.getNoteId(), loadedDraft.getId());
        Assert.assertEquals(parentNoteId, loadedDraft.getParent().getId());
    }

    /**
     * Test for comment creation.
     */
    @Test(groups = { "CreateBlogPost" }, dependsOnMethods = { "testCreateNote" })
    public void testCreateComment() throws Exception {
        AuthenticationTestUtils.setSecurityContext(user);
        NoteStoringTO storingTO = generateCommonNoteStoringTO();
        final String content = COMMENT_CONTENT_1;
        storingTO.setContent(content);

        String uniqueTag = UNIQUE_TAG_POOL.get("testCreateComment");
        storingTO.setUnparsedTags(uniqueTag);

        NoteData item = getPostWithTag(UNIQUE_TAG_POOL.get("testCreateNote"),
                storingTO.getBlogId(), storingTO.getCreatorId());

        Long postId = item.getId();

        storingTO.setParentNoteId(postId);
        NoteModificationResult result = noteService.createNote(storingTO, null);
        checkCreationSuccessWithWarnings(result, 0, 0, 0, 0);

        NoteData comment = getPostWithTag(uniqueTag, storingTO.getBlogId(),
                storingTO.getCreatorId());
        checkBasicAssertions(comment, storingTO, content);
        Assert.assertEquals(comment.getParent().getId(), item.getId(),
                "Comment has not correct parent post id");
        // reload parent to get comment count
        Assert.assertEquals(comment.getParent().getNumberOfComments(), 1,
                "The parent has not the correct number of comments.");

    }

    /**
     * Test for cross posting.
     *
     * @throws NoteStoringPreProcessorException
     *             in case the test failed
     * @throws NoteManagementAuthorizationException
     *             in case the test failed
     * @throws BlogNotFoundException
     *             in case the test failed
     * @throws InterruptedException
     *             Exception
     * @throws NoteNotFoundException
     *             in case the test failed
     */
    @Test(groups = { "CreateBlogPost" })
    public void testCreateCrossPost() throws Exception {
        AuthenticationTestUtils.setSecurityContext(user);
        NoteStoringTO storingTO = generateCommonNoteStoringTO();
        final String content = POST_CONTENT_2;
        storingTO.setContent(content);
        String uniqueTag = UNIQUE_TAG_POOL.get("testCreateCrossPost");
        storingTO.setUnparsedTags(uniqueTag);

        Blog testBlog = blogManagement.findBlogByIdentifier(TEST_BLOG_IDENTIFIER2);
        Assert.assertNotNull(testBlog, "Blog cannot be null");
        HashSet<String> additionalBlogs = new HashSet<String>();
        additionalBlogs.add(testBlog.getNameIdentifier());
        NoteModificationResult result = noteService.createNote(storingTO, additionalBlogs);
        checkCreationSuccessWithWarnings(result, 0, 0, 0, 0);
        NoteData item = getPostWithTag(uniqueTag, storingTO.getBlogId(), storingTO.getCreatorId());
        Long originalNoteId = item.getId();
        Date originalCreationDate = item.getCreationDate();
        checkBasicAssertions(item, storingTO, content);
        // check post in other blog
        item = getPostWithTag(uniqueTag, testBlog.getId(), storingTO.getCreatorId());
        Assert.assertEquals(item.getCreationDate(), originalCreationDate);
        checkBasicAssertions(item, storingTO, content);

        Thread.sleep(2000); // Wait a while to make sure the date changed.
        Blog testBlog2 = TestUtils.createRandomBlog(true, true, user);
        additionalBlogs.add(testBlog2.getNameIdentifier());
        noteService.updateNote(storingTO, originalNoteId, additionalBlogs, false);
        item = getPostWithTag(uniqueTag, testBlog2.getId(), storingTO.getCreatorId());
        Assert.assertEquals(item.getCreationDate(), originalCreationDate);
    }

    /**
     * Test for implicit cross posting.
     */
    @Test(groups = { "CreateBlogPost" })
    public void testCreateImplicitCrossPost() throws Exception {
        AuthenticationHelper.setAsAuthenticatedUser(user);
        NoteStoringTO storingTO = generateCommonNoteStoringTO();
        final String uniqueTag = UUID.randomUUID().toString();
        final String content = POST_CONTENT_3;
        storingTO.setContent(content + " &" + TEST_BLOG_IDENTIFIER2);
        storingTO.setUnparsedTags(uniqueTag);
        storingTO.setContentType(NoteContentType.PLAIN_TEXT);

        NoteModificationResult result = noteService.createNote(storingTO, null);
        checkCreationSuccessWithWarnings(result, 0, 0, 0, 0);
        NoteData item = getPostWithTag(uniqueTag, storingTO.getBlogId(), storingTO.getCreatorId());
        checkBasicAssertions(item, storingTO, content);
        // check post in other blog
        Blog testBlog = blogManagement.findBlogByIdentifier(TEST_BLOG_IDENTIFIER2);
        item = getPostWithTag(uniqueTag, testBlog.getId(), storingTO.getCreatorId());
        checkBasicAssertions(item, storingTO, content);
    }

    /**
     * Test for simple blog post creation.
     */
    @Test(groups = { "CreateBlogPost" })
    public void testCreateNote() throws Exception {
        AuthenticationHelper.setAsAuthenticatedUser(user);

        NoteStoringTO storingTO = generateCommonNoteStoringTO();
        // add unique tag for retrieval after creation
        String uniqueTag = UNIQUE_TAG_POOL.get("testCreateNote");
        storingTO.setUnparsedTags(uniqueTag);

        storingTO.setContent(POST_CONTENT_1);
        storingTO.setContentType(NoteContentType.PLAIN_TEXT);

        NoteModificationResult result = noteService.createNote(storingTO, null);
        checkCreationSuccessWithWarnings(result, 0, 0, 0, 0);

        NoteData item = getPostWithTag(uniqueTag, storingTO.getBlogId(), storingTO.getCreatorId());
        checkBasicAssertions(item, storingTO, POST_CONTENT_1);
        Assert.assertNull(item.getParent(), "Note which is not a comment has a parent.");
        Assert.assertEquals(item.getTags().size(), 2, "Note does not have the required tags.");
        Assert.assertEquals(item.getNumberOfComments(), 0, "Note must not have comments.");
    }

    /**
     * Test for post creation with attachment.
     *
     * @throws Exception
     *             in case the test failed
     */
    @Test(groups = { "CreateBlogPost" })
    public void testCreateNoteWithAttachment() throws Exception {
        AuthenticationTestUtils.setSecurityContext(user);

        final String fileName = "attachement.txt";

        AttachmentTO attachement = TestUtils.createAttachment(fileName);

        NoteStoringTO storingTO = generateCommonNoteStoringTO();
        // add unique tag for retrieval after creation
        String uniqueTag = UNIQUE_TAG_POOL.get("testCreateNoteWithAttachment");
        storingTO.setUnparsedTags(uniqueTag);

        final String content = POST_CONTENT_1;
        storingTO.setContent(content);
        storingTO.setContentType(NoteContentType.PLAIN_TEXT);

        Attachment attachment = resourceStoringManagement.storeAttachment(attachement);

        Assert.assertNotNull(attachment, "Attachement resource should not be null!");
        Assert.assertEquals(attachment.getStatus(), AttachmentStatus.UPLOADED);
        storingTO.setAttachmentIds(new Long[] { attachment.getId() });

        checkCreationSuccessWithWarnings(noteService.createNote(storingTO, null), 0, 0, 0, 0);
        NoteData item = getPostWithTag(uniqueTag, storingTO.getBlogId(), storingTO.getCreatorId());
        checkBasicAssertions(item, storingTO, content);
        Assert.assertEquals(item.getAttachments().size(), 1);
        AttachmentData cr = item.getAttachments().get(0);
        Assert.assertEquals(cr.getFileName(), fileName);

    }

    /**
     * Test for blog post creation with user notification.
     */
    @Test(groups = { "CreateBlogPost" })
    public void testCreateNoteWithUserNotification() throws Exception {
        AuthenticationTestUtils.setSecurityContext(user);

        NoteStoringTO storingTO = generateCommonNoteStoringTO();
        storingTO.setContent(POST_CONTENT_4 + user.getAlias());
        storingTO.setContentType(NoteContentType.PLAIN_TEXT);

        NoteModificationResult result = noteService.createNote(storingTO, null);
        checkCreationSuccessWithWarnings(result, 1, 0, 0, 0);
        String rejectedUser = result.getUserNotificationResult().getUnresolvableUsers().iterator()
                .next();
        Assert.assertEquals(rejectedUser, NOT_EXISTING_USER, "Wrong user rejected: " + rejectedUser);
    }

    /**
     * Test for deletion of post with comments.
     *
     * @throws NoteManagementAuthorizationException
     *             in case the test failed
     */
    @Test
    public void testDeleteNote() throws NoteManagementAuthorizationException {
        User user = TestUtils.createRandomUser(false);
        Blog blog = TestUtils.createRandomBlog(false, false, user);
        Long noteId = TestUtils.createAndStoreCommonNote(blog, user.getId(), UUID.randomUUID()
                .toString());
        noteService.deleteNote(noteId, false, false);
    }

    /**
     * Tests, that it is possible to delete a note as internal system user.
     *
     * @throws Exception
     *             in case the test failed
     */
    @Test
    public void testDeleteNoteAsSystemUser() throws Exception {
        User user = TestUtils.createRandomUser(false);
        Blog blog = TestUtils.createRandomBlog(false, false, user);
        Long noteId = TestUtils.createAndStoreCommonNote(blog, user.getId(), random());
        AuthenticationHelper.setInternalSystemToSecurityContext();
        noteService.deleteNote(noteId, true, false);
        Assert.assertNull(ServiceLocator.findService(NoteDao.class).load(noteId));
    }

    /**
     * Tests for {@link NoteService#deleteNotesOfUser(Long)}
     *
     * @throws AuthorizationException
     *             in case the test failed
     */
    @Test
    public void testDeleteNotesOfUserWithProperties() throws AuthorizationException {
        User user = TestUtils.createRandomUser(false);
        Blog blog = TestUtils.createRandomBlog(true, true, user);
        Long noteId = TestUtils.createAndStoreCommonNote(blog, user.getId(), "TestNote");
        int counter = 10 + RandomUtils.nextInt(100);
        long[] propertyIds = new long[counter];
        AuthenticationTestUtils.setSecurityContext(user);
        for (int i = 0; i < counter; i++) {
            String keyGroup = UUID.randomUUID().toString();
            String propertyKey = UUID.randomUUID().toString();
            propertyManagement.addObjectPropertyFilter(PropertyType.UserNoteProperty, keyGroup,
                    propertyKey);
            try {
                propertyIds[i] = ((UserNoteProperty) propertyManagement.setObjectProperty(
                        PropertyType.UserNoteProperty, noteId, keyGroup, propertyKey, UUID
                        .randomUUID().toString())).getId();
            } catch (NotFoundException e) {
                Assert.fail("user note property was not found.");
            }
            Assert.assertNotNull(ServiceLocator.findService(UserNotePropertyDao.class).load(
                    propertyIds[i]));
        }
        noteService.deleteNotesOfUser(user.getId());
        Assert.assertNotNull(ServiceLocator.findService(UserDao.class).load(user.getId()));
        Assert.assertNull(ServiceLocator.findService(NoteDao.class).load(noteId));
        for (long propertyId : propertyIds) {
            Assert.assertNull(ServiceLocator.findService(UserNotePropertyDao.class)
                    .load(propertyId));
        }
    }

    /**
     * Tests for {@link NoteService#deleteNotesOfUser(Long)} with UserNoteEntity's assigned.
     *
     * @throws AuthorizationException
     *             in case the test failed
     * @throws NotFoundException
     *             in case the test failed
     */
    @Test
    public void testDeleteNotesOfUserWithUserNoteEntities() throws AuthorizationException,
    NotFoundException {
        User user = TestUtils.createRandomUser(false);
        Blog blog = TestUtils.createRandomBlog(true, true, user);
        Long noteId = TestUtils.createAndStoreCommonNote(blog, user.getId(), "TestNote");

        AuthenticationTestUtils.setSecurityContext(user);

        UserNoteEntityService uneService = ServiceLocator.instance().getService(
                UserNoteEntityService.class);
        UserNoteEntityTO userNoteEntityTO = new UserNoteEntityTO(user.getId(), noteId);
        userNoteEntityTO.setNormalizedRank(0.4);
        userNoteEntityTO.setUpdateRank(true);
        uneService.updateUserNoteEntity(userNoteEntityTO);

        noteService.deleteNotesOfUser(user.getId());

        Assert.assertNotNull(ServiceLocator.findService(UserDao.class).load(user.getId()));
        Assert.assertNull(ServiceLocator.findService(NoteDao.class).load(noteId));
    }

    /**
     * Test for deletion of post with comments.
     *
     * @throws AuthorizationException
     * @throws NoteStoringPreProcessorException
     * @throws BlogNotFoundException
     * @throws ContentRepositoryException
     */
    @Test
    public void testDeleteNoteWithAttachement() throws AuthorizationException,
            BlogNotFoundException, NoteStoringPreProcessorException, ContentRepositoryException {
        User user = TestUtils.createRandomUser(false);
        Blog blog = TestUtils.createRandomBlog(false, false, user);

        final String fileName = "attachement-for-deletion.txt";
        final String content = "delete me content.";

        final NoteData item = createNoteWithAttachment(user, blog, fileName, content);

        final AttachmentData cr = item.getAttachments().get(0);

        final AttachmentTO attachmentTO = TestUtils
                .getAttachmentFromDefaultFilesystemConnectorByContentId(cr.getContentId());
        Assert.assertNotNull(attachmentTO);

        RepositoryConnectorDelegate rcd = ServiceLocator
                .findService(RepositoryConnectorDelegate.class);
        long beforeRepositorySize = rcd.getDefaultRepositoryConnector().getRepositorySize();

        noteService.deleteNote(item.getId(), false, false);

        Note note = this.noteService.getNote(item.getId(), new IdentityConverter<Note>());
        Assert.assertNull(note);

        try {
            TestUtils.getAttachmentFromDefaultFilesystemConnectorByContentId(item.getAttachments()
                    .get(0).getContentId());
            Assert.fail("Expected ContentRepositoryException exception.");
        } catch (ContentRepositoryException e) {
            // expected case
        }

        long newRepositorySize = rcd.getDefaultRepositoryConnector().getRepositorySize();

        Assert.assertTrue(newRepositorySize < beforeRepositorySize);
        Assert.assertEquals(newRepositorySize,
                beforeRepositorySize - attachmentTO.getContentLength());
    }

    /**
     * Test for deletion of post with comments.
     *
     * @throws AuthorizationException
     * @throws NoteStoringPreProcessorException
     * @throws BlogNotFoundException
     * @throws ContentRepositoryException
     */
    @Test
    public void testDeleteNoteWithAttachementByManager() throws AuthorizationException,
            BlogNotFoundException, NoteStoringPreProcessorException, ContentRepositoryException {
        User managerUser = TestUtils.createRandomUser(false);
        User authorUser = TestUtils.createRandomUser(false);

        AuthenticationHelper.setAsAuthenticatedUser(managerUser);
        Blog blog = TestUtils.createRandomBlog(false, false, managerUser, authorUser);

        AuthenticationHelper.setAsAuthenticatedUser(authorUser);

        final String fileName = "attachement-for-deletion.txt";
        final String content = "delete me by manager content.";

        final NoteData item = createNoteWithAttachment(authorUser, blog, fileName, content);

        final AttachmentData cr = item.getAttachments().get(0);

        final AttachmentTO attachmentTO = TestUtils
                .getAttachmentFromDefaultFilesystemConnectorByContentId(cr.getContentId());
        Assert.assertNotNull(attachmentTO);

        RepositoryConnectorDelegate rcd = ServiceLocator
                .findService(RepositoryConnectorDelegate.class);
        long beforeRepositorySize = rcd.getDefaultRepositoryConnector().getRepositorySize();

        AuthenticationHelper.setAsAuthenticatedUser(managerUser);
        noteService.deleteNote(item.getId(), false, false);

        Note note = this.noteService.getNote(item.getId(), new IdentityConverter<Note>());
        Assert.assertNull(note);

        try {
            TestUtils.getAttachmentFromDefaultFilesystemConnectorByContentId(item.getAttachments()
                    .get(0).getContentId());
            Assert.fail("Expected ContentRepositoryException exception.");
        } catch (ContentRepositoryException e) {
            // expected case
        }

        long newRepositorySize = rcd.getDefaultRepositoryConnector().getRepositorySize();

        Assert.assertTrue(newRepositorySize < beforeRepositorySize);
        Assert.assertEquals(newRepositorySize,
                beforeRepositorySize - attachmentTO.getContentLength());
    }

    /**
     * Tests for {@link NoteService#deleteNote(Long, boolean, boolean)}
     *
     * @throws AuthorizationException
     *             in case the test failed
     * @throws NotFoundException
     *             in case the test failed
     */
    @Test
    public void testDeleteNoteWithProperties() throws AuthorizationException, NotFoundException {
        User user = TestUtils.createRandomUser(false);
        Blog blog = TestUtils.createRandomBlog(true, true, user);
        Long noteId = TestUtils.createAndStoreCommonNote(blog, user.getId(), "TestNote");
        int counter = 10 + RandomUtils.nextInt(100);
        long[] propertyIds = new long[counter];
        for (int i = 0; i < counter; i++) {
            AuthenticationTestUtils.setSecurityContext(TestUtils.createRandomUser(false));
            String keyGroup = UUID.randomUUID().toString();
            String propertyKey = UUID.randomUUID().toString();
            propertyManagement.addObjectPropertyFilter(PropertyType.UserNoteProperty, keyGroup,
                    propertyKey);
            propertyIds[i] = ((UserNoteProperty) propertyManagement.setObjectProperty(
                    PropertyType.UserNoteProperty, noteId, keyGroup, propertyKey, UUID.randomUUID()
                    .toString())).getId();
            Assert.assertNotNull(ServiceLocator.findService(UserNotePropertyDao.class).load(
                    propertyIds[i]));
        }
        AuthenticationTestUtils.setSecurityContext(user);
        noteService.deleteNote(noteId, true, true);
        Assert.assertNotNull(ServiceLocator.findService(UserDao.class).load(user.getId()));
        Assert.assertNull(ServiceLocator.findService(NoteDao.class).load(noteId));
        for (long propertyId : propertyIds) {
            Assert.assertNull(ServiceLocator.findService(UserNotePropertyDao.class)
                    .load(propertyId));
        }
    }

    /**
     * Regression test KENMEI-4633: deletion of a note fails with a DataIntegrityViolationException
     * in a specific constellation
     *
     * @throws Exception
     *             in case the test failed
     *
     */
    @Test
    public void testForKENMEI4633() throws Exception {
        User user1 = TestUtils.createRandomUser(false);
        User user2 = TestUtils.createRandomUser(false);
        User user3 = TestUtils.createRandomUser(false);
        Blog blog = TestUtils.createRandomBlog(true, true, user1);

        Long noteId = TestUtils.createAndStoreCommonNote(blog, user1.getId(),
                "TestNote @" + user2.getAlias() + " #" + random());
        AuthenticationTestUtils.setSecurityContext(user2);
        NoteStoringTO childNoteStoringTO = TestUtils.createCommonNote(blog, user2.getId(),
                "Reply @" + user1.getAlias(), new String[] { "contentType.image", "true" });
        childNoteStoringTO.setParentNoteId(noteId);
        Long childNoteId = noteService.createNote(childNoteStoringTO, null).getNoteId();
        String keyGroup = UUID.randomUUID().toString();
        String propertyKey = UUID.randomUUID().toString();
        propertyManagement.addObjectPropertyFilter(PropertyType.UserNoteProperty, keyGroup,
                propertyKey);

        AuthenticationTestUtils.setSecurityContext(user1);
        propertyManagement.setObjectProperty(PropertyType.UserNoteProperty, childNoteId, keyGroup,
                propertyKey, "false");
        AuthenticationTestUtils.setSecurityContext(user3);
        propertyManagement.setObjectProperty(PropertyType.UserNoteProperty, childNoteId, keyGroup,
                propertyKey, "true");
        ServiceLocator.instance().getService(FavoriteManagement.class)
                .markNoteAsFavorite(childNoteId);
        AuthenticationTestUtils.setSecurityContext(user1);
        noteService.deleteNote(noteId, false, false);
    }

    /**
     * Update a user tagged post.
     *
     * @throws Exception
     *             in case the test failed
     */
    @Test(groups = { "CreateBlogPost" }, dependsOnMethods = { "testCreateNoteWithAttachment" })
    public void testUpdateNote() throws Exception {
        AuthenticationTestUtils.setSecurityContext(user);
        NoteStoringTO storingTO = generateCommonNoteStoringTO();
        Long noteId = getPostWithTag(UNIQUE_TAG_POOL.get("testCreateNoteWithAttachment"),
                storingTO.getBlogId(), storingTO.getCreatorId()).getId();
        final String content = POST_UPDATE_CONTENT;
        storingTO.setContent(content);

        storingTO.setUnparsedTags(UUID.randomUUID().toString() + ","
                + UNIQUE_TAG_POOL.get("testUpdateNote"));
        NoteModificationResult result = noteService.updateNote(storingTO, noteId, null, false);
        checkCreationSuccessWithWarnings(result, 0, 0, 0, 0);
        // check for updated content
        NoteData updatedItem = noteService.getNote(result.getNoteId(), new NoteRenderContext(null,
                Locale.ENGLISH));
        Assert.assertNotNull(updatedItem, "Updated post cannot be found");
        checkBasicAssertions(updatedItem, storingTO, content);

        // check that tags and attachments from original post are removed
        Assert.assertEquals(updatedItem.getTags().size(), 2, "Tag count of updated item is wrong.");
        Assert.assertEquals(updatedItem.getAttachments().size(), 0, "Attachment count wrong");
    }
}
