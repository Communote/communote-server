package com.communote.server.core.vo.query.note;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.communote.common.matcher.Matcher;
import com.communote.common.util.PageableList;
import com.communote.server.api.core.note.NoteData;
import com.communote.server.api.core.note.NoteRenderContext;
import com.communote.server.api.core.note.NoteRenderMode;
import com.communote.server.api.core.note.NoteStoringTO;
import com.communote.server.api.core.tag.TagData;
import com.communote.server.api.core.tag.TagStoreType.Types;
import com.communote.server.core.filter.ResultSpecification;
import com.communote.server.core.filter.listitems.SimpleNoteListItem;
import com.communote.server.core.query.QueryManagement;
import com.communote.server.core.security.AuthenticationHelper;
import com.communote.server.core.tag.TagManagement;
import com.communote.server.core.vo.blog.DiscussionNoteData;
import com.communote.server.core.vo.query.DiscussionFilterMode;
import com.communote.server.core.vo.query.QueryParameters.OrderDirection;
import com.communote.server.core.vo.query.TaggingCoreItemUTPExtension;
import com.communote.server.core.vo.query.TimelineFilterViewType;
import com.communote.server.core.vo.query.java.note.MatcherFactory;
import com.communote.server.core.vo.query.note.SimpleNoteListItemToDiscussionNoteDataConverter;
import com.communote.server.core.vo.query.post.NoteQuery;
import com.communote.server.core.vo.query.post.NoteQueryParameters;
import com.communote.server.model.blog.Blog;
import com.communote.server.model.tag.Tag;
import com.communote.server.model.user.User;
import com.communote.server.service.NoteService;
import com.communote.server.test.CommunoteIntegrationTest;
import com.communote.server.test.util.AuthenticationTestUtils;
import com.communote.server.test.util.TestUtils;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class NoteQueryDefinitionTest extends CommunoteIntegrationTest {

    private User user;

    @Autowired
    private NoteService noteManagement;
    @Autowired
    private QueryManagement queryManagement;
    @Autowired
    private TagManagement tagManagement;

    private final int maxRoot = 10;

    private final int maxComment = 10;

    private final int numEarlyTags = 5;

    /**
     * Check the discussion order for {@link #testDiscussionFilterMode()}
     *
     * @param queryResult
     *            the result
     * @param rootNote1Id
     *            the id of root note 1
     * @param rootNote2Id
     *            the id of root note 2
     * @param rootNote3Id
     *            the id of root note 3
     */
    private void assertDiscussionOrder(PageableList<SimpleNoteListItem> queryResult,
            Long rootNote1Id, Long rootNote2Id, Long rootNote3Id) {
        // now check the order of the notes returned. root note 3 should be the latest, than root
        // note 1 (because got a newer comment) and at last root note 3
        Assert.assertEquals(queryResult.get(0).getId(), rootNote3Id,
                "rootNote3 should be the first.");
        // this is correct! rootNote1 should be the 2nd in the list
        Assert.assertEquals(queryResult.get(1).getId(), rootNote1Id,
                "rootNote1 should be the second.");
        Assert.assertEquals(queryResult.get(2).getId(), rootNote2Id,
                "rootNote2 should be the third.");
    }

    /**
     * Check that the root notes are in correct order
     *
     * @param queryResult
     *            the results to check (only root notes will be considered
     * @param rootNoteIds
     *            the correct root notes order, starting with the oldest
     * @param size
     *            the size the queryResult should have
     * @param commentCount
     *            the size the comments of each root note should have
     */
    private void checkForAllRootNotes(PageableList<DiscussionNoteData> queryResult,
            List<Long> rootNoteIds, int size, int commentCount) {
        Assert.assertEquals(queryResult.size(), size, "The result should contain all notes");

        rootNoteIds = new ArrayList<Long>(rootNoteIds);
        Collections.reverse(rootNoteIds);
        int rootNoteOrder = 0;
        // check the order
        for (DiscussionNoteData data : queryResult) {
            if (data.getParent() == null) {
                Assert.assertEquals(data.getId(), rootNoteIds.get(rootNoteOrder++));
                Assert.assertEquals(data.getComments().size(), commentCount);
            }
        }
    }

    /**
     * Remove authentication
     */
    @AfterTest
    public void cleanUp() {
        AuthenticationHelper.removeAuthentication();
    }

    /**
     * First creates a number of root notes and then for each root note a bunch of comments. Each
     * note of one discussion is tagged with "discussion0". Each root note is tagged with
     * "rootnote0", each comment with "comment0.1" (the first number is the number of the root
     * note).
     *
     * Between the creation of notes the creation time is advances by two minutes.
     *
     * @param topic
     *            the blog to add the notes to
     * @return the ids of the root note starting with the oldest
     * @throws Exception
     *             in case of an error
     */
    private List<Long> createTestNotes(Blog topic) throws Exception {
        List<Long> rootNoteIds = new ArrayList<Long>();
        Date startDate = new Date();

        int dateInc = 0;
        for (int i = 0; i < maxRoot; i++) {
            // create not with tags "root,rootnote0,discussion0"
            NoteStoringTO rootnote = TestUtils.createCommonNote(topic, user.getId(), "Root Note "
                    + i + " #root #rootnote" + i + " #discussion" + i);
            rootnote.setCreationDate(new Timestamp(startDate.getTime() + dateInc++ * 60000));
            Long rootNoteId = noteManagement.createNote(rootnote, null).getNoteId();
            Assert.assertNotNull(rootNoteId);
            rootNoteIds.add(rootNoteId);
        }
        for (int i = 0; i < maxRoot; i++) {
            for (int j = 0; j < maxComment; j++) {
                // create not with tags "comment,discussion0,comment0.2,no2_"
                String content = "Comment Note " + i + "." + "j" + " #root #discussion" + i
                        + " #comment" + i + "." + j + " #no" + i * maxComment + j + "_";
                if (j < numEarlyTags) {
                    content += " #earlytag";
                } else {
                    content += " #latertag";
                }
                NoteStoringTO commentnote = TestUtils
                        .createCommonNote(topic, user.getId(), content);
                commentnote.setParentNoteId(rootNoteIds.get(i));
                commentnote.setCreationDate(new Timestamp(startDate.getTime() + dateInc++ * 60000));
                Long commentNoteId = noteManagement.createNote(commentnote, null).getNoteId();
                Assert.assertNotNull(commentNoteId);
            }
        }
        return rootNoteIds;
    }

    /**
     * Setups all common parameters for this tests.
     *
     * @throws Exception
     *             in case the test failed
     */
    @BeforeClass(dependsOnGroups = "integration-test-setup")
    public void setup() throws Exception {
        user = TestUtils.createRandomUser(false);
    }

    /**
     * This tests, if it is possible to search child notes, but only get the root notes.
     *
     * @throws Exception
     *             in case the test failed
     */
    @Test
    public void testDiscussionFilterMode() throws Exception {

        AuthenticationTestUtils.setSecurityContext(user);

        //
        // Preparation: create a blog with a root note and an answer
        //
        // create blog
        Blog topicForDiscussionFiltering = TestUtils.createRandomBlog(true, true, user);
        // create a note
        Long rootNote1Id = noteManagement.createNote(
                TestUtils.createCommonNote(topicForDiscussionFiltering, user.getId(),
                        "Root Note 1 #root #note #rootnote1"), null).getNoteId();
        // create a reply to the note
        NoteStoringTO comment1RootNote1 = TestUtils.createCommonNote(topicForDiscussionFiltering,
                user.getId(), "Comment Note 1 #comment #tag");
        comment1RootNote1.setParentNoteId(rootNote1Id);
        noteManagement.createNote(comment1RootNote1, null);

        // get the tag for filter later
        Tag tag = tagManagement.findTag("root", Types.NOTE);

        //
        // Filter with and without the discussion mode
        //
        NoteQuery query = new NoteQuery();
        NoteQueryParameters noteQueryParameters = new NoteQueryParameters();
        TaggingCoreItemUTPExtension filter = new TaggingCoreItemUTPExtension();
        filter.setBlogFilter(new Long[] { topicForDiscussionFiltering.getId() });
        noteQueryParameters.setTypeSpecificExtension(filter);

        PageableList<SimpleNoteListItem> queryResult = queryManagement.query(query,
                noteQueryParameters);
        Assert.assertEquals(queryResult.getMinNumberOfElements(), 2);

        // now filter only for root notes.
        noteQueryParameters.setDiscussionFilterMode(DiscussionFilterMode.IS_ROOT);
        queryResult = queryManagement.query(query, noteQueryParameters);
        Assert.assertEquals(queryResult.getMinNumberOfElements(), 1);
        Assert.assertEquals(queryResult.get(0).getId(), rootNote1Id);

        // filter for a tag
        Set<Long> tagFilter = new HashSet<Long>();
        tagFilter.add(tag.getId());
        noteQueryParameters.setTagIds(tagFilter);
        queryResult = queryManagement.query(query, noteQueryParameters);
        Assert.assertEquals(queryResult.getMinNumberOfElements(), 1);
        Assert.assertEquals(queryResult.get(0).getId(), rootNote1Id);

        NoteStoringTO rootNote2 = TestUtils.createCommonNote(topicForDiscussionFiltering,
                user.getId(), "Root Note 2 #root #note #rootnote2");
        rootNote2.setCreationDate(new Timestamp(new Date().getTime()));
        // create a note
        Long rootNote2Id = noteManagement.createNote(rootNote2, null).getNoteId();
        Assert.assertNotNull(rootNote2Id);

        // create a reply to the note
        NoteStoringTO comment2RootNote1 = TestUtils
                .createCommonNote(topicForDiscussionFiltering, user.getId(),
                        "Comment for Root Note 1 but written after root note 2. #comment #tag #comment1rootnote1");
        comment2RootNote1.setParentNoteId(rootNote1Id);
        comment2RootNote1.setCreationDate(new Timestamp(
                rootNote2.getCreationDate().getTime() + 60000));
        noteManagement.createNote(comment2RootNote1, null);

        //
        // Add another root note, that should be the first one.
        //

        // create a note
        NoteStoringTO rootNote3 = TestUtils
                .createCommonNote(topicForDiscussionFiltering, user.getId(),
                        "Root Note 3, written after root note 2 and 1 and all other comments. #root #rootnote3");
        // adapt creation date
        rootNote3.setCreationDate(new Timestamp(rootNote2.getCreationDate().getTime() + 1200000));

        Long rootNote3Id = noteManagement.createNote(rootNote3, null).getNoteId();
        Assert.assertNotNull(rootNote1Id);

        //
        // filter for the root notes and check if the order is correct.
        //
        noteQueryParameters.setDiscussionFilterMode(DiscussionFilterMode.IS_ROOT);
        noteQueryParameters.setTagIds(null);
        noteQueryParameters.setSortByDate(OrderDirection.DESCENDING);
        noteQueryParameters.setSortById(OrderDirection.DESCENDING);
        queryResult = queryManagement.query(query, noteQueryParameters);

        Assert.assertEquals(queryResult.getMinNumberOfElements(), 3, "Number of root notes.");

        assertDiscussionOrder(queryResult, rootNote1Id, rootNote2Id, rootNote3Id);
    }

    /**
     * Test the filtering of root notes.
     *
     * This test first creates a number of root notes. Than for each root note a number of replies
     * are created using different tags.
     *
     * Later it is filtered for the tags to check the correct results.
     *
     * @throws Exception
     *             in case of an error
     */
    @Test(dependsOnMethods = "testDiscussionFilterMode")
    public void testFilteringAndReturningRootNotes() throws Exception {

        AuthenticationTestUtils.setSecurityContext(user);

        NoteQueryParameters noteQueryParameters = new NoteQueryParameters();
        SimpleNoteListItemToDiscussionNoteDataConverter converter = new SimpleNoteListItemToDiscussionNoteDataConverter(
                new NoteRenderContext(NoteRenderMode.PLAIN, Locale.ENGLISH),
                TimelineFilterViewType.COMMENT, noteQueryParameters);
        Blog topic = TestUtils.createRandomBlog(true, true, user);

        List<Long> rootNoteIds = createTestNotes(topic);

        NoteQuery query = new NoteQuery();

        noteQueryParameters.setDiscussionFilterMode(DiscussionFilterMode.IS_ROOT);
        noteQueryParameters.setTagIds(null);
        noteQueryParameters.setSortByDate(OrderDirection.DESCENDING);
        noteQueryParameters.setSortById(OrderDirection.DESCENDING);

        TaggingCoreItemUTPExtension filter = new TaggingCoreItemUTPExtension();
        filter.setBlogFilter(new Long[] { topic.getId() });
        noteQueryParameters.setTypeSpecificExtension(filter);

        noteQueryParameters.getResultSpecification()
        .setNumberOfElements(maxRoot * (1 + maxComment));

        PageableList<DiscussionNoteData> queryResult = queryManagement.query(query,
                noteQueryParameters, converter);

        checkForAllRootNotes(queryResult, rootNoteIds, maxRoot, maxComment);

        queryResult = queryManagement.query(query, noteQueryParameters, converter);

        checkForAllRootNotes(queryResult, rootNoteIds, maxRoot, maxComment);

        // check for a comment tag. It should return one discussion
        Tag aCommentTag = tagManagement.findTag("comment1.2", Types.NOTE);
        Assert.assertNotNull(aCommentTag, "Tag comment1.2 should have been created.");

        noteQueryParameters.getTagIds().clear();
        noteQueryParameters.getTagIds().add(aCommentTag.getId());
        // set a matcher to filter the comments according to the query parameters
        Matcher<NoteData> matcher = MatcherFactory.createMatcher(noteQueryParameters);
        converter.setCommentMatcher(matcher, true);

        queryResult = queryManagement.query(query, noteQueryParameters, converter);
        Assert.assertEquals(queryResult.size(), 1);
        for (NoteData data : queryResult.get(0).getComments()) {
            boolean foundDiscussion1Tag = false;
            boolean foundComment1Tag = false;
            for (TagData tag : data.getTags()) {
                if (tag.getName().equals("discussion1")) {
                    foundDiscussion1Tag = true;
                }
                if (tag.getId().equals(aCommentTag.getId())) {
                    foundComment1Tag = true;
                }
            }
            if (!foundDiscussion1Tag) {
                Assert.fail("Tag discussion1 for this discussion not found.");
            }
            if (!foundComment1Tag) {
                Assert.fail("Tag comment1.2 for the comments not found.");
            }
        }

        // now query for early notes
        Tag earlyTag = tagManagement.findTag("earlytag", Types.NOTE);

        noteQueryParameters.getTagIds().clear();
        noteQueryParameters.getTagIds().add(earlyTag.getId());
        // create new matcher for changed query parameters
        matcher = MatcherFactory.createMatcher(noteQueryParameters);
        converter.setCommentMatcher(matcher, true);
        queryResult = queryManagement.query(query, noteQueryParameters, converter);

        checkForAllRootNotes(queryResult, rootNoteIds, maxRoot, numEarlyTags);
        // check the order
        for (NoteData data : queryResult) {
            for (TagData tag : data.getTags()) {
                if (data.getParent() != null) {
                    // TODO this should fail, since it will contains the last two not all
                    Assert.assertEquals(tag.getName(), earlyTag.getName());
                }
            }
        }
    }

    /**
     * Test that it is not possible to get more notes than the limit.
     *
     * @throws Exception
     *             in case the test failed
     */
    @Test
    public void testNoteLimit() throws Exception {

        AuthenticationTestUtils.setSecurityContext(user);

        Blog blog = TestUtils.createRandomBlog(true, true, user);
        NoteQuery query = new NoteQuery();
        NoteQueryParameters noteQueryInstance = new NoteQueryParameters();
        TaggingCoreItemUTPExtension extension = new TaggingCoreItemUTPExtension();
        extension.setBlogFilter(new Long[] { blog.getId() });
        noteQueryInstance.setTypeSpecificExtension(extension);
        int upperLimit = 402;
        // Create more notes than upper limit (400)
        for (int i = 0; i < upperLimit; i++) {
            noteManagement.createNote(
                    TestUtils.createCommonNote(blog, user.getId(), "Note Limit Test " + i), null)
                    .getNoteId();
        }
        // test limit of 25 when no result specification is set
        Assert.assertEquals(queryManagement.query(query, noteQueryInstance).size(), 25);
        Assert.assertEquals(queryManagement.executeQueryComplete(query, noteQueryInstance).size(),
                upperLimit);

        // assert that the upper limit takes effect when the 'unlimited' option is defined
        ResultSpecification resultSpecification = new ResultSpecification();
        noteQueryInstance.setResultSpecification(resultSpecification);
        Assert.assertEquals(400, queryManagement.query(query, noteQueryInstance).size());
        Assert.assertEquals(queryManagement.executeQueryComplete(query, noteQueryInstance).size(),
                upperLimit);

        resultSpecification.setNumberOfElements(0);
        Assert.assertEquals(400, queryManagement.query(query, noteQueryInstance).size());
        Assert.assertTrue(queryManagement.executeQueryComplete(query, noteQueryInstance).size() == upperLimit);

        // assert the upper or default limit do not override an explicitly defined limit
        resultSpecification.setNumberOfElements(13);
        Assert.assertEquals(13, queryManagement.query(query, noteQueryInstance).size());
        Assert.assertTrue(queryManagement.executeQueryComplete(query, noteQueryInstance).size() == upperLimit);

        // assert that not more than upper limit items can be returned
        resultSpecification.setNumberOfElements(500);
        Assert.assertEquals(400, queryManagement.query(query, noteQueryInstance).size());
        Assert.assertTrue(queryManagement.executeQueryComplete(query, noteQueryInstance).size() == upperLimit);
    }
}
