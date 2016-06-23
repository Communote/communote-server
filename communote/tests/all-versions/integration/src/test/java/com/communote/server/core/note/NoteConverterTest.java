package com.communote.server.core.note;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.UUID;

import org.apache.commons.lang.math.RandomUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.communote.common.util.PageableList;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.blog.UserBlogData;
import com.communote.server.api.core.note.NoteData;
import com.communote.server.api.core.note.NoteRenderContext;
import com.communote.server.api.core.note.NoteRenderMode;
import com.communote.server.api.core.note.NoteStoringTO;
import com.communote.server.api.core.tag.TagData;
import com.communote.server.api.core.tag.TagStoreType;
import com.communote.server.api.core.tag.TagTO;
import com.communote.server.api.core.user.DetailedUserData;
import com.communote.server.core.blog.FavoriteManagement;
import com.communote.server.core.query.QueryManagement;
import com.communote.server.core.query.QueryManagementParameterNameProvider;
import com.communote.server.core.tag.TagManagement;
import com.communote.server.core.user.UserManagementHelper;
import com.communote.server.core.user.UserProfileDetails;
import com.communote.server.core.user.UserProfileManagement;
import com.communote.server.core.vo.blog.DiscussionNoteData;
import com.communote.server.core.vo.blog.NoteModificationResult;
import com.communote.server.core.vo.query.QueryParameters.OrderDirection;
import com.communote.server.core.vo.query.TimelineFilterViewType;
import com.communote.server.core.vo.query.config.TimelineQueryParametersConfigurator;
import com.communote.server.core.vo.query.note.SimpleNoteListItemToDiscussionNoteDataConverter;
import com.communote.server.core.vo.query.note.SimpleNoteListItemToNoteDataQueryResultConverter;
import com.communote.server.core.vo.query.post.NoteQuery;
import com.communote.server.core.vo.query.post.NoteQueryParameters;
import com.communote.server.model.blog.Blog;
import com.communote.server.model.blog.BlogRole;
import com.communote.server.model.note.Note;
import com.communote.server.model.tag.Tag;
import com.communote.server.model.user.User;
import com.communote.server.persistence.blog.NoteDao;
import com.communote.server.service.NoteService;
import com.communote.server.test.CommunoteIntegrationTest;
import com.communote.server.test.util.AuthenticationTestUtils;
import com.communote.server.test.util.TestUtils;

/**
 * Class for testing converters which convert SimpleNoteListItem objects to NoteData (and
 * subclasses) instances
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class NoteConverterTest extends CommunoteIntegrationTest {

    private static final NoteQuery NOTE_QUERY = new NoteQuery();
    private static final QueryManagementParameterNameProvider NAMES = QueryManagementParameterNameProvider.INSTANCE;
    private User user1, user2;
    private NoteService noteManagement;
    private QueryManagement queryManagement;
    private TagManagement tagManagement;
    private UserProfileManagement userProfileManagement;
    private Blog discussionTestBlog;
    private HashMap<String, Long> discussionNotes;
    private HashMap<String, Long> discussionNoteParents;
    private ArrayList<String> chronologicallyOrderedComments;
    private ArrayList<String> threadViewOrderedComments;

    /**
     * Assert that the blog list item matches the expected blog
     *
     * @param blogItem
     *            the item to check
     * @param expectedBlog
     *            the expected blog
     * @param expectedRole
     *            the role the current user should have in the list item
     */
    private void assertBlog(UserBlogData blogItem, Blog expectedBlog, BlogRole expectedRole) {
        Assert.assertEquals(blogItem.getId(), expectedBlog.getId());
        Assert.assertEquals(blogItem.getDescription(), expectedBlog.getDescription());
        Assert.assertEquals(blogItem.getTitle(), expectedBlog.getTitle());
        Assert.assertEquals(blogItem.getAlias(), expectedBlog.getNameIdentifier());
        Assert.assertEquals(blogItem.getNameIdentifier(), expectedBlog.getNameIdentifier());
        Assert.assertEquals(blogItem.getLastModificationDate(),
                expectedBlog.getLastModificationDate());
        Assert.assertEquals(blogItem.getUserRole(), expectedRole);
        // TODO also check tags and properties?
    }

    /**
     * Assert that the notified users contain the expected user
     *
     * @param noteItem
     *            the note item to check
     * @param expectedUser
     *            the user that must be contained
     */
    private void assertContainsNotifiedUser(NoteData noteItem, User expectedUser) {
        for (DetailedUserData userItem : noteItem.getNotifiedUsers()) {
            if (userItem.getId().equals(expectedUser.getId())) {
                assertUser(userItem, expectedUser);
                return;
            }
        }
        Assert.fail("User " + expectedUser.getAlias() + " was not notified");
    }

    /**
     * Asserts that the note has the expected tag
     *
     * @param noteItem
     *            the note item to check
     * @param expectedTag
     *            the tag the note should have
     */
    private void assertContainsTag(NoteData noteItem, Tag expectedTag) {
        for (TagData tagItem : noteItem.getTags()) {
            if (tagItem.getId().equals(expectedTag.getId())) {
                Assert.assertEquals(tagItem.getDefaultName(), expectedTag.getDefaultName());
                Assert.assertEquals(tagItem.getTagStoreTagId(), expectedTag.getTagStoreTagId());
                Assert.assertEquals(tagItem.getTagStoreAlias(), expectedTag.getTagStoreAlias());
                return;
            }
        }
        Assert.fail("Expected tag " + expectedTag.getDefaultName() + " is not contained");
    }

    /**
     * Assert that the result item contains the data that is expected by the testNoteConverter test.
     *
     * @param noteItem
     *            the result item to check
     * @param expectedBlog
     *            the expected blog
     * @param expectedBlogRole
     *            the expected role of the current user in that blog
     * @param expectedNoteId
     *            the expected note ID
     * @param expectedTO
     *            the TO that is expected to have been used for creating the note
     * @param expectedNotifiedUsers
     *            the users that should have been notified
     * @param expectedFlags
     *            map of flags the note should have
     */
    private void assertNoteListDataForTestNoteConverter(NoteData noteItem, Blog expectedBlog,
            BlogRole expectedBlogRole, Long expectedNoteId, NoteStoringTO expectedTO,
            User[] expectedNotifiedUsers, HashMap<String, Boolean> expectedFlags) {
        Long noteId = noteItem.getId();
        Assert.assertEquals(noteId, expectedNoteId);
        // don't use notemanagement because it uses the same converter we are testing here
        Note loadedNote = ServiceLocator.findService(NoteDao.class).load(noteId);
        Assert.assertEquals(noteItem.getAttachments().size(), 0);
        assertBlog(noteItem.getBlog(), expectedBlog, expectedBlogRole);
        // pre processors modify the TO, so there is no need to wrap content with <p> etc.
        Assert.assertEquals(noteItem.getContent(), expectedTO.getContent());
        Assert.assertEquals(noteItem.getCreationDate(), loadedNote.getCreationDate());
        Assert.assertEquals(noteItem.getDiscussionDepth(),
                loadedNote.getDiscussionId().equals(noteId) ? 0 : 1);
        Assert.assertEquals(noteItem.getDiscussionId(), loadedNote.getDiscussionId());
        Assert.assertEquals(noteItem.getLastModificationDate(),
                loadedNote.getLastModificationDate());
        assertNotifiedUsers(noteItem, expectedNotifiedUsers);
        // root note has 1 comment, the reply has no comment
        Assert.assertEquals(noteItem.getNumberOfComments(),
                loadedNote.getDiscussionId().equals(noteId) ? 1 : 0);
        Assert.assertEquals(noteItem.getNumberOfDiscussionNotes(), 2);
        assertNoteProperties(noteItem);
        assertNoteTags(noteItem, expectedTO.getTags());
        assertUser(noteItem.getUser(), user1);
        Assert.assertEquals(noteItem.isCommentable(), expectedFlags.get("isCommentable")
                .booleanValue());
        Assert.assertEquals(noteItem.isDeletable(), expectedFlags.get("isDeletable").booleanValue());
        Assert.assertEquals(noteItem.isDirect(), expectedFlags.get("isDirect").booleanValue());
        Assert.assertEquals(noteItem.isEditable(), expectedFlags.get("isEditable").booleanValue());
        Assert.assertEquals(noteItem.isFavorite(), expectedFlags.get("isFavorite").booleanValue());
        Assert.assertEquals(noteItem.isForMe(), expectedFlags.get("isForMe").booleanValue());
    }

    /**
     * Assert that the note properties are filled correctly.
     *
     * @param noteItem
     *            the item to test
     */
    private void assertNoteProperties(NoteData noteItem) {
        // expect that the 2 properties added by the LikeNoteRenderingPreprocessor are there
        Assert.assertEquals(noteItem.getProperties().size(), 2);
        Boolean currenUserLikesNote = noteItem.getProperty("currentUserLikesNote");
        Assert.assertFalse(currenUserLikesNote);
        Collection<DetailedUserData> likers = noteItem.getProperty("usersWhichLikeThisPost");
        Assert.assertEquals(likers.size(), 0);
    }

    /**
     * Assert that the tags in the note list item are as expected. Only works with explicitly
     * assigned tags and not with hash tags.
     *
     * @param noteItem
     *            the note list item to check
     * @param expectedTags
     *            the tags that should be in the note, can be empty
     */
    private void assertNoteTags(NoteData noteItem, Collection<TagTO> expectedTags) {
        Assert.assertEquals(noteItem.getTags().size(), expectedTags.size());
        for (TagTO tagTO : expectedTags) {
            Tag tag = tagManagement.findTag(tagTO.getDefaultName(), TagStoreType.Types.NOTE);
            assertContainsTag(noteItem, tag);
        }
    }

    /**
     * assert that all provided users were notified
     *
     * @param noteItem
     *            the note item to check
     * @param expectedUsers
     *            the expected users, should be an empty array if no users should have been notified
     */
    private void assertNotifiedUsers(NoteData noteItem, User[] expectedUsers) {
        Assert.assertEquals(noteItem.getNotifiedUsers().size(), expectedUsers.length);
        for (User expectedUser : expectedUsers) {
            assertContainsNotifiedUser(noteItem, expectedUser);
        }
    }

    /**
     * Assert that the list item has correct and sufficient data
     *
     * @param userItem
     *            the user to check
     * @param expectedUser
     *            the user entity with expected values
     */
    private void assertUser(DetailedUserData userItem, User expectedUser) {
        Assert.assertEquals(userItem.getAlias(), expectedUser.getAlias());
        Assert.assertEquals(userItem.getEmail(), expectedUser.getEmail());
        Assert.assertEquals(userItem.getId(), expectedUser.getId());
        Assert.assertEquals(userItem.getStatus(), expectedUser.getStatus());
        UserProfileDetails expectedProfile = userProfileManagement.getUserProfileDetailsById(
                expectedUser.getId(), false);
        Assert.assertEquals(userItem.getFirstName(), expectedProfile.getFirstName());
        Assert.assertEquals(userItem.getLastName(), expectedProfile.getLastName());
        Assert.assertEquals(userItem.getSalutation(), expectedProfile.getSalutation());
        Assert.assertEquals(userItem.getEffectiveUserTimeZone(),
                UserManagementHelper.getEffectiveUserTimeZone(expectedUser.getId()));
        // TODO check tags, properties and mod dates?
    }

    /**
     * Configures a basic QueryInstance for retrieving notes
     *
     * @param parameters
     *            parameters.
     * @return the configured tagging instance
     */
    protected NoteQueryParameters configureQueryInstance(HashMap<String, String> parameters) {
        NoteQueryParameters noteQueryInstance = NOTE_QUERY.createInstance();
        TimelineQueryParametersConfigurator<NoteQueryParameters> qic;
        qic = new TimelineQueryParametersConfigurator<NoteQueryParameters>(NAMES);
        qic.configure(parameters, noteQueryInstance);
        return noteQueryInstance;
    }

    /**
     * Creates a discussion and stores the noteIds in the discussionNotes map. the key of the map is
     * name like 'level_x-n' where 'x' represents the level in the discussion tree and 'n' the
     * number of that note in the level. An increasing 'n' reflects the creation date in that a
     * higher 'n' means the note was created later.
     *
     * The resulting discussion looks like this:
     *
     * flattened threaded view: level_0, level_1-0, level_2-1, level_3-1, level_1-1, level_2-2,
     * level_1-2, level_2-0, level_3-0 (stored in threadViewOrderedComments)
     *
     * chronological (oldest first): level_0, level_1-0, level_1-1, level_1-2, level_2-0, level_2-1,
     * level_3-0, level_2-2, level_3-1 (stored in chronologicallyOrderedComments)
     *
     * @throws Exception
     *             in case the creation of the discussion failed
     */
    private void createDiscussion() throws Exception {
        discussionNotes = new HashMap<String, Long>();
        discussionNoteParents = new HashMap<String, Long>();
        chronologicallyOrderedComments = new ArrayList<String>();
        threadViewOrderedComments = new ArrayList<String>();
        discussionTestBlog = TestUtils.createRandomBlog(false, false, user1, user2);

        // set user1 for notes on even discussion levels, user2 for others
        NoteStoringTO noteTO = TestUtils.createCommonNote(discussionTestBlog, user1.getId(),
                "parent note");
        NoteModificationResult parentNoteResult = noteManagement.createNote(noteTO, null);
        discussionNotes.put("level_0", parentNoteResult.getNoteId());
        // create replies and store them directly in the chrono ordered comment list
        noteTO = TestUtils.createCommonNote(discussionTestBlog, user2.getId(), "reply note");
        noteTO.setParentNoteId(parentNoteResult.getNoteId());
        NoteModificationResult noteResult = noteManagement.createNote(noteTO, null);
        discussionNotes.put("level_1-0", noteResult.getNoteId());
        discussionNoteParents.put("level_1-0", noteTO.getParentNoteId());
        chronologicallyOrderedComments.add("level_1-0");

        noteTO = TestUtils.createCommonNote(discussionTestBlog, user2.getId(), "reply note");
        noteTO.setParentNoteId(parentNoteResult.getNoteId());
        noteResult = noteManagement.createNote(noteTO, null);
        discussionNotes.put("level_1-1", noteResult.getNoteId());
        discussionNoteParents.put("level_1-1", noteTO.getParentNoteId());
        chronologicallyOrderedComments.add("level_1-1");

        noteTO = TestUtils.createCommonNote(discussionTestBlog, user2.getId(), "reply note");
        noteTO.setParentNoteId(parentNoteResult.getNoteId());
        noteResult = noteManagement.createNote(noteTO, null);
        discussionNotes.put("level_1-2", noteResult.getNoteId());
        discussionNoteParents.put("level_1-2", noteTO.getParentNoteId());
        chronologicallyOrderedComments.add("level_1-2");

        // replies on replies (level 2), changing order of parents to get a better sort scenario
        noteTO = TestUtils.createCommonNote(discussionTestBlog, user1.getId(), "reply note");
        noteTO.setParentNoteId(discussionNotes.get("level_1-2"));
        noteResult = noteManagement.createNote(noteTO, null);
        discussionNotes.put("level_2-0", noteResult.getNoteId());
        discussionNoteParents.put("level_2-0", noteTO.getParentNoteId());
        chronologicallyOrderedComments.add("level_2-0");

        noteTO = TestUtils.createCommonNote(discussionTestBlog, user1.getId(), "reply note");
        noteTO.setParentNoteId(discussionNotes.get("level_1-0"));
        noteResult = noteManagement.createNote(noteTO, null);
        discussionNotes.put("level_2-1", noteResult.getNoteId());
        discussionNoteParents.put("level_2-1", noteTO.getParentNoteId());
        chronologicallyOrderedComments.add("level_2-1");

        // replies on replies on replies
        noteTO = TestUtils.createCommonNote(discussionTestBlog, user2.getId(), "reply note");
        noteTO.setParentNoteId(discussionNotes.get("level_2-0"));
        noteResult = noteManagement.createNote(noteTO, null);
        discussionNotes.put("level_3-0", noteResult.getNoteId());
        discussionNoteParents.put("level_3-0", noteTO.getParentNoteId());
        chronologicallyOrderedComments.add("level_3-0");

        // create a reply on previous layer (reply on reply)
        noteTO = TestUtils.createCommonNote(discussionTestBlog, user1.getId(), "reply note");
        noteTO.setParentNoteId(discussionNotes.get("level_1-1"));
        noteResult = noteManagement.createNote(noteTO, null);
        discussionNotes.put("level_2-2", noteResult.getNoteId());
        discussionNoteParents.put("level_2-2", noteTO.getParentNoteId());
        chronologicallyOrderedComments.add("level_2-2");

        // another level 3
        noteTO = TestUtils.createCommonNote(discussionTestBlog, user2.getId(), "reply note");
        noteTO.setParentNoteId(discussionNotes.get("level_2-1"));
        noteResult = noteManagement.createNote(noteTO, null);
        discussionNotes.put("level_3-1", noteResult.getNoteId());
        discussionNoteParents.put("level_3-1", noteTO.getParentNoteId());
        chronologicallyOrderedComments.add("level_3-1");

        // save threaded view order
        threadViewOrderedComments.add("level_1-0");
        threadViewOrderedComments.add("level_2-1");
        threadViewOrderedComments.add("level_3-1");
        threadViewOrderedComments.add("level_1-1");
        threadViewOrderedComments.add("level_2-2");
        threadViewOrderedComments.add("level_1-2");
        threadViewOrderedComments.add("level_2-0");
        threadViewOrderedComments.add("level_3-0");
    }

    /**
     * Setups all common parameters for this tests.
     *
     * @throws Exception
     *             Exception.
     */
    @BeforeClass(dependsOnGroups = "integration-test-setup")
    public void setup() throws Exception {
        user1 = TestUtils.createRandomUser(false);
        user2 = TestUtils.createRandomUser(false);
        noteManagement = ServiceLocator.instance().getService(NoteService.class);
        queryManagement = ServiceLocator.findService(QueryManagement.class);
        tagManagement = ServiceLocator.findService(TagManagement.class);
        userProfileManagement = ServiceLocator.findService(UserProfileManagement.class);
        createDiscussion();
    }

    /**
     * Uses NoteManagement and the provided converter to fetch a discussion with its comments and
     * asserts that the discussion is filled correctly
     *
     * @param converter
     *            converter to fill the discussion list item with discussion details and a sorted
     *            list of comments
     * @param orderedComments
     *            list of comments in the expected sort order. The list must be one of the lists
     *            filled by createDiscussion.
     * @throws Exception
     *             in case the discussion isn't filled correctly
     */
    private void testDiscussionConverter(SimpleNoteListItemToDiscussionNoteDataConverter converter,
            ArrayList<String> orderedComments) throws Exception {
        AuthenticationTestUtils.setSecurityContext(user1);
        Long discussionId = discussionNotes.get("level_0");
        DiscussionNoteData discussionData = noteManagement.getNoteWithComments(discussionId,
                converter);
        Assert.assertEquals(discussionData.getId(), discussionId);
        Assert.assertNull(discussionData.getParent());
        Assert.assertEquals(discussionData.getDiscussionDepth(), 0);
        // root note is not considered when calculating into the number of comments
        Assert.assertEquals(discussionData.getNumberOfComments(), discussionNotes.size() - 1);
        Assert.assertEquals(discussionData.getNumberOfDiscussionNotes(), discussionNotes.size());
        Assert.assertEquals(discussionData.getComments().size(), discussionNotes.size() - 1);
        assertBlog(discussionData.getBlog(), discussionTestBlog, BlogRole.MANAGER);
        assertUser(discussionData.getUser(), user1);
        // check that notes are ordered correctly by comparing against list created in
        // createDiscussion
        for (int i = 0; i < orderedComments.size(); i++) {
            String noteRefString = orderedComments.get(i);
            Long expectedNoteId = discussionNotes.get(noteRefString);
            Long expectedParentId = discussionNoteParents.get(noteRefString);
            NoteData noteItem = discussionData.getComments().get(i);
            Assert.assertEquals(noteItem.getId(), expectedNoteId);
            Assert.assertEquals(noteItem.getParent().getId(), expectedParentId);
            Assert.assertEquals(noteItem.getNumberOfDiscussionNotes(), discussionNotes.size());
            int levelNumberStartIdx = noteRefString.indexOf("_") + 1;
            int level = Integer.parseInt(noteRefString.substring(levelNumberStartIdx,
                    noteRefString.indexOf("-", levelNumberStartIdx)));
            Assert.assertEquals(noteItem.getDiscussionDepth(), level);
            Assert.assertNotNull(noteItem.getContent());
            assertBlog(noteItem.getBlog(), discussionTestBlog, BlogRole.MANAGER);
            assertUser(noteItem.getUser(), level % 2 == 0 ? user1 : user2);
            // createCommonNote adds an unparsed tag
            Assert.assertEquals(noteItem.getTags().size(), 1);
            Assert.assertEquals(noteItem.getAttachments().size(), 0);
        }
    }

    /**
     * Tests the discussion note converter that returns the comments in chronological order. Works
     * on the discussion created by createDiscussion.
     *
     * @throws Exception
     *             in case the test fails
     */
    @Test
    public void testDiscussionNoteConverterChronological() throws Exception {

        SimpleNoteListItemToDiscussionNoteDataConverter converter;
        converter = new SimpleNoteListItemToDiscussionNoteDataConverter(new NoteRenderContext(
                NoteRenderMode.PORTAL, Locale.ENGLISH), TimelineFilterViewType.COMMENT, null);
        testDiscussionConverter(converter, chronologicallyOrderedComments);
    }

    /**
     * Tests the getAllTags method on a discussion data object filled by a discussion converter.
     *
     * @throws Exception
     *             in case the test failed
     */
    @Test
    public void testDiscussionNoteConverterGetAllTags() throws Exception {
        ArrayList<String> expectedTags = new ArrayList<String>();
        Blog blog = TestUtils.createRandomBlog(false, false, user1, user2);
        NoteStoringTO noteTO = TestUtils.createCommonNote(blog, user1.getId(), "parent note");
        noteTO.setUnparsedTags(null);
        String tagName = "P" + UUID.randomUUID().toString();
        noteTO.getTags().add(new TagTO(tagName, TagStoreType.Types.NOTE));
        expectedTags.add(tagName);
        NoteModificationResult parentNoteResult = noteManagement.createNote(noteTO, null);
        // reply without tag
        noteTO = TestUtils.createCommonNote(blog, user1.getId(), "reply note");
        noteTO.setParentNoteId(parentNoteResult.getNoteId());
        noteTO.setUnparsedTags(null);
        NoteModificationResult noteResult = noteManagement.createNote(noteTO, null);
        // reply with 2 tags
        noteTO = TestUtils.createCommonNote(blog, user1.getId(), "reply note");
        noteTO.setParentNoteId(noteResult.getNoteId());
        noteTO.setUnparsedTags(null);
        tagName = "B" + UUID.randomUUID().toString();
        noteTO.getTags().add(new TagTO(tagName, TagStoreType.Types.NOTE));
        expectedTags.add(tagName);
        tagName = "A" + UUID.randomUUID().toString();
        noteTO.getTags().add(new TagTO(tagName, TagStoreType.Types.NOTE));
        expectedTags.add(tagName);
        noteManagement.createNote(noteTO, null);
        // reply with 1 new and one existing tag
        noteTO = TestUtils.createCommonNote(blog, user1.getId(), "reply note");
        noteTO.setParentNoteId(parentNoteResult.getNoteId());
        noteTO.setUnparsedTags(null);
        tagName = "D" + UUID.randomUUID().toString();
        noteTO.getTags().add(new TagTO(tagName, TagStoreType.Types.NOTE));
        expectedTags.add(tagName);
        noteTO.getTags().add(new TagTO(expectedTags.get(0), TagStoreType.Types.NOTE));
        noteManagement.createNote(noteTO, null);
        // get discussion and compare getAllTags
        SimpleNoteListItemToDiscussionNoteDataConverter converter;
        converter = new SimpleNoteListItemToDiscussionNoteDataConverter(new NoteRenderContext(
                NoteRenderMode.PORTAL, Locale.ENGLISH), TimelineFilterViewType.THREAD, null);
        DiscussionNoteData discussionData = noteManagement.getNoteWithComments(
                parentNoteResult.getNoteId(), converter);
        String[] allTags = discussionData.getAllTags(Locale.ENGLISH);
        Collections.sort(expectedTags, Collator.getInstance(Locale.ENGLISH));
        Assert.assertEquals(allTags.length, expectedTags.size());
        for (int i = 0; i < allTags.length; i++) {
            Assert.assertEquals(allTags[i], expectedTags.get(i));
        }
    }

    /**
     * Tests the discussion note converter that returns the comments in threaded view order. Works
     * on the discussion created by createDiscussion.
     *
     * @throws Exception
     *             in case the test fails
     */
    @Test
    public void testDiscussionNoteConverterThreaded() throws Exception {
        SimpleNoteListItemToDiscussionNoteDataConverter converter;
        converter = new SimpleNoteListItemToDiscussionNoteDataConverter(new NoteRenderContext(
                NoteRenderMode.PORTAL, Locale.ENGLISH), TimelineFilterViewType.THREAD, null);
        testDiscussionConverter(converter, threadViewOrderedComments);
    }

    /**
     * Tests that the note list data object is correctly filled by the converter
     *
     * @throws Exception
     *             in case the test failed
     */
    @Test
    public void testNoteConverter() throws Exception {
        Blog topic = TestUtils.createRandomBlog(false, false, user1, user2);
        NoteStoringTO parentNote = TestUtils.createCommonNote(topic, user1.getId(), "parent note");
        parentNote.setUnparsedTags(null);
        NoteModificationResult parentNoteResult = noteManagement.createNote(parentNote, null);
        Thread.sleep(1200); // Wait a little more than a second, because of databases which do
        // not store milliseconds.
        NoteStoringTO replyNote = TestUtils.createCommonNote(topic, user1.getId(), "reply note");
        replyNote.setUnparsedTags(null);
        replyNote.getUsersToNotify().add(user2.getAlias());
        replyNote.setParentNoteId(parentNoteResult.getNoteId());
        TagTO tag1 = new TagTO(UUID.randomUUID().toString(), TagStoreType.Types.NOTE);
        replyNote.getTags().add(tag1);
        NoteModificationResult replyNoteResult = noteManagement.createNote(replyNote, null);
        AuthenticationTestUtils.setSecurityContext(user1);
        ServiceLocator.findService(FavoriteManagement.class).markNoteAsFavorite(
                replyNoteResult.getNoteId());
        NoteRenderContext renderContext = new NoteRenderContext(NoteRenderMode.HTML, Locale.ENGLISH);
        // fetch these 2 notes by filtering by blog id
        NoteQueryParameters queryInstance = configureQueryInstance(new HashMap<String, String>());
        queryInstance.setSortByDate(OrderDirection.DESCENDING);
        queryInstance.getTypeSpecificExtension().setBlogId(topic.getId());
        SimpleNoteListItemToNoteDataQueryResultConverter<NoteData> converter;
        converter = new SimpleNoteListItemToNoteDataQueryResultConverter<NoteData>(NoteData.class,
                renderContext);
        PageableList<NoteData> results = queryManagement
                .query(NOTE_QUERY, queryInstance, converter);
        Assert.assertEquals(results.size(), 2);
        Assert.assertEquals(results.getMinNumberOfElements(), 2);
        HashMap<String, Boolean> expectedFlags = new HashMap<String, Boolean>();
        expectedFlags.put("isCommentable", Boolean.TRUE);
        expectedFlags.put("isDeletable", Boolean.TRUE);
        expectedFlags.put("isDirect", Boolean.FALSE);
        expectedFlags.put("isEditable", Boolean.TRUE);
        expectedFlags.put("isForMe", Boolean.FALSE);
        // because it is sorted by date (newest first), the reply is first entry
        for (int i = 0; i < 2; i++) {
            NoteStoringTO expectedTO = parentNote;
            Long expectedNoteId = parentNoteResult.getNoteId();
            User[] expectedNotifiedUsers = new User[] { };
            if (i == 0) {
                expectedTO = replyNote;
                expectedFlags.put("isFavorite", Boolean.TRUE);
                expectedNoteId = replyNoteResult.getNoteId();
                expectedNotifiedUsers = new User[] { user2 };
            } else {
                expectedFlags.put("isFavorite", Boolean.FALSE);
            }
            NoteData noteItem = results.get(i);
            assertNoteListDataForTestNoteConverter(noteItem, topic, BlogRole.MANAGER,
                    expectedNoteId, expectedTO, expectedNotifiedUsers, expectedFlags);
            if (i == 0) {
                // TODO actually we would have to check all attributes of parent note list item ...
                NoteData parentNoteItem = noteItem.getParent();
                Assert.assertEquals(parentNoteItem.getId(), parentNoteResult.getNoteId());
                assertBlog(parentNoteItem.getBlog(), topic, BlogRole.MANAGER);
                assertUser(parentNoteItem.getUser(), user1);
                Assert.assertEquals(parentNoteItem.getContent(), parentNote.getContent());
            } else {
                Assert.assertNull(noteItem.getParent());
            }
        }
        // do query again for other user
        AuthenticationTestUtils.setSecurityContext(user2);
        converter = new SimpleNoteListItemToNoteDataQueryResultConverter<NoteData>(NoteData.class,
                renderContext);
        results = queryManagement.query(NOTE_QUERY, queryInstance, converter);
        expectedFlags.put("isDeletable", Boolean.FALSE);
        expectedFlags.put("isEditable", Boolean.FALSE);
        expectedFlags.put("isFavorite", Boolean.FALSE);
        for (int i = 0; i < 2; i++) {
            NoteStoringTO expectedTO = parentNote;
            Long expectedNoteId = parentNoteResult.getNoteId();
            User[] expectedNotifiedUsers = new User[] { };
            if (i == 0) {
                expectedTO = replyNote;
                expectedFlags.put("isForMe", Boolean.TRUE);
                expectedNoteId = replyNoteResult.getNoteId();
                expectedNotifiedUsers = new User[] { user2 };
            } else {
                expectedFlags.put("isForMe", Boolean.FALSE);
            }
            NoteData noteItem = results.get(i);
            assertNoteListDataForTestNoteConverter(noteItem, topic, BlogRole.MEMBER,
                    expectedNoteId, expectedTO, expectedNotifiedUsers, expectedFlags);
            if (i == 0) {
                // TODO actually we would have to check all attributes of parent note list item ...
                NoteData parentNoteItem = noteItem.getParent();
                Assert.assertEquals(parentNoteItem.getId(), parentNoteResult.getNoteId());
                assertBlog(parentNoteItem.getBlog(), topic, BlogRole.MEMBER);
                assertUser(parentNoteItem.getUser(), user1);
                Assert.assertEquals(parentNoteItem.getContent(), parentNote.getContent());
            } else {
                Assert.assertNull(noteItem.getParent());
            }
        }
    }

    /**
     *
     * Creates random notes and uses the query system and a NoteConverter to fetch them.
     *
     * @throws Exception
     *             In case the test fails
     *
     */
    @Test
    public void testNoteConverterSimple() throws Exception {
        Blog blog1 = TestUtils.createRandomBlog(true, true, user1, user2);
        HashMap<Long, String> map = new HashMap<Long, String>();
        int limit = 10 + RandomUtils.nextInt(50);
        for (int i = 0; i < limit; i++) {
            String randContent = UUID.randomUUID().toString();
            NoteModificationResult noteResult = noteManagement.createNote(
                    TestUtils.createCommonNote(blog1, user1.getId(), randContent), null);
            map.put(noteResult.getNoteId(), randContent);
        }
        AuthenticationTestUtils.setSecurityContext(user1);
        NoteQueryParameters queryInstance = configureQueryInstance(new HashMap<String, String>());
        queryInstance.getTypeSpecificExtension().setBlogId(blog1.getId());
        SimpleNoteListItemToNoteDataQueryResultConverter<NoteData> converter;
        converter = new SimpleNoteListItemToNoteDataQueryResultConverter<NoteData>(NoteData.class,
                new NoteRenderContext(NoteRenderMode.HTML, Locale.ENGLISH));
        PageableList<NoteData> results = queryManagement
                .query(NOTE_QUERY, queryInstance, converter);
        Assert.assertTrue(results.size() > 0);
        for (NoteData noteItem : results) {
            Long noteId = noteItem.getId();
            Assert.assertTrue(map.containsKey(noteId));
            // don't use notemanagement because it uses the same converter we are testing here
            Note loadedNote = ServiceLocator.findService(NoteDao.class).load(noteId);
            Assert.assertEquals(noteItem.getAttachments().size(), 0);
            assertBlog(noteItem.getBlog(), blog1, BlogRole.MANAGER);
            Assert.assertEquals(noteItem.getContent(), "<p>" + map.get(noteId) + "</p>");
            Assert.assertEquals(noteItem.getCreationDate(), loadedNote.getCreationDate());
            Assert.assertEquals(noteItem.getDiscussionDepth(), 0);
            Assert.assertEquals(noteItem.getDiscussionId(), noteId);
            Assert.assertEquals(noteItem.getLastModificationDate(),
                    loadedNote.getLastModificationDate());
            assertNotifiedUsers(noteItem, new User[] { });
            Assert.assertEquals(noteItem.getNumberOfComments(), 0);
            Assert.assertEquals(noteItem.getNumberOfDiscussionNotes(), 1);
            Assert.assertNull(noteItem.getParent());
            assertNoteProperties(noteItem);
            // createCommonNote adds an unparsed tag
            Assert.assertEquals(noteItem.getTags().size(), 1);
            assertUser(noteItem.getUser(), user1);
            Assert.assertTrue(noteItem.isCommentable());
            Assert.assertTrue(noteItem.isDeletable());
            Assert.assertFalse(noteItem.isDirect());
            Assert.assertTrue(noteItem.isEditable());
            Assert.assertFalse(noteItem.isFavorite());
            Assert.assertFalse(noteItem.isForMe());
        }

    }
}
