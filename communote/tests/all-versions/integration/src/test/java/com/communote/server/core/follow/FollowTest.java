package com.communote.server.core.follow;

import java.util.Locale;
import java.util.UUID;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.communote.common.util.PageableList;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.common.NotFoundException;
import com.communote.server.api.core.note.NoteRenderContext;
import com.communote.server.api.core.note.NoteStoringTO;
import com.communote.server.api.core.tag.TagStoreType.Types;
import com.communote.server.core.filter.listitems.SimpleNoteListItem;
import com.communote.server.core.query.QueryManagement;
import com.communote.server.core.tag.TagManagement;
import com.communote.server.core.vo.query.QueryParameters.OrderDirection;
import com.communote.server.core.vo.query.TaggingCoreItemUTPExtension;
import com.communote.server.core.vo.query.blog.TopicAccessLevel;
import com.communote.server.core.vo.query.post.NoteQuery;
import com.communote.server.core.vo.query.post.NoteQueryParameters;
import com.communote.server.model.blog.Blog;
import com.communote.server.model.user.User;
import com.communote.server.service.NoteService;
import com.communote.server.test.CommunoteIntegrationTest;
import com.communote.server.test.util.AuthenticationTestUtils;
import com.communote.server.test.util.TestUtils;

/**
 * Tests for the follow feature.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class FollowTest extends CommunoteIntegrationTest {
    private static final NoteQuery QUERY = new NoteQuery();
    private User user1;
    private User user2;
    private Blog blog1;
    private NoteService noteManagement;
    private FollowManagement followManagement;
    private TagManagement tagManagement;

    /**
     * @return the configured tagging instance
     */
    private NoteQueryParameters createQueryParameters() {
        NoteQueryParameters queryParameters = new NoteQueryParameters();
        TaggingCoreItemUTPExtension extension = queryParameters.getTypeSpecificExtension();
        extension.setTopicAccessLevel(TopicAccessLevel.READ);
        queryParameters.setSortByDate(OrderDirection.DESCENDING);
        queryParameters.setRetrieveOnlyFollowedItems(true);
        return queryParameters;
    }

    /**
     * Setups all common parameters for this tests.
     *
     * @throws Exception
     *             in case the test failed
     */
    @BeforeClass(dependsOnGroups = "integration-test-setup")
    public void setup() throws Exception {
        user1 = TestUtils.createRandomUser(false);
        user2 = TestUtils.createRandomUser(false);
        blog1 = TestUtils.createRandomBlog(false, false, user1, user2);
        noteManagement = ServiceLocator.instance().getService(NoteService.class);
        followManagement = ServiceLocator.instance().getService(FollowManagement.class);
        tagManagement = ServiceLocator.instance().getService(TagManagement.class);
    }

    /**
     * Test that it is possible to follow and unfollow a blog.
     *
     * @throws Exception
     *             in case the test failed
     */
    @Test
    public void testFollowUnfollowBlog() throws Exception {
        AuthenticationTestUtils.setSecurityContext(user1);
        followManagement.followBlog(blog1.getId());
        Assert.assertTrue(followManagement.followsBlog(blog1.getId()));

        followManagement.unfollowBlog(blog1.getId());
        Assert.assertFalse(followManagement.followsBlog(blog1.getId()));
        try {
            followManagement.followBlog(Long.MIN_VALUE);
            Assert.fail("Can't follow not existing blog.");
        } catch (NotFoundException e) {
            // Okay.
        }
    }

    /**
     * Test that it is possible to follow and unfollow a discussion.
     *
     * @throws Exception
     *             in case the test failed
     */
    @Test
    public void testFollowUnfollowDiscussion() throws Exception {
        AuthenticationTestUtils.setSecurityContext(user1);
        Long noteId = noteManagement.createNote(
                TestUtils.createCommonNote(blog1, user1.getId(), "This is a note"), null)
                .getNoteId();
        Long discussionId = noteManagement.getNote(noteId,
                new NoteRenderContext(null, Locale.ENGLISH)).getDiscussionId();
        followManagement.followDiscussion(discussionId);
        Assert.assertTrue(followManagement.followsDiscussion(discussionId));
        followManagement.unfollowDiscussion(discussionId);
        Assert.assertFalse(followManagement.followsDiscussion(discussionId));
        try {
            followManagement.followDiscussion(Long.MIN_VALUE);
            Assert.fail("Can't follow non existing discussion.");
        } catch (NotFoundException e) {
            // Okay.
        }
    }

    /**
     * Test that it is possible to follow and unfollow a discussion by any note id.
     *
     * @throws Exception
     *             in case the test failed
     */
    @Test
    public void testFollowUnfollowDiscussionByNoteId() throws Exception {
        AuthenticationTestUtils.setSecurityContext(user1);
        Long noteId = noteManagement.createNote(
                TestUtils.createCommonNote(blog1, user1.getId(), "This is a note"), null)
                .getNoteId();
        NoteStoringTO answer = TestUtils.createCommonNote(blog1, user1.getId(), "This is a note");
        answer.setParentNoteId(noteId);
        Long answerId = noteManagement.createNote(answer, null).getNoteId();
        NoteStoringTO answerOnAnswer = TestUtils.createCommonNote(blog1, user1.getId(),
                "This is another note");
        answerOnAnswer.setParentNoteId(answerId);
        Long answerOnAnswerId = noteManagement.createNote(answerOnAnswer, null).getNoteId();

        followManagement.followDiscussionByNoteId(answerId);

        Assert.assertTrue(followManagement.followsDiscussion(noteId));
        followManagement.unfollowDiscussionByNoteId(answerOnAnswerId);
        Assert.assertFalse(followManagement.followsDiscussion(noteId));
        try {
            followManagement.followDiscussionByNoteId(Long.MAX_VALUE);
            Assert.fail("Can't follow non existing discussion.");
        } catch (NotFoundException e) {
            // Okay.
        }
    }

    /**
     * Test that it is possible to follow and unfollow a mix of users and blogs.
     *
     * @throws Exception
     *             in case the test failed
     */
    @Test
    public void testFollowUnfollowedMixed() throws Exception {
        AuthenticationTestUtils.setSecurityContext(user1);
        followManagement.followBlog(blog1.getId());
        followManagement.followUser(user1.getId());
        followManagement.followUser(user2.getId());

        Assert.assertTrue(followManagement.followsBlog(blog1.getId()));
        Assert.assertTrue(followManagement.followsUser(user1.getId()));
        Assert.assertTrue(followManagement.followsUser(user2.getId()));

        followManagement.unfollowBlog(blog1.getId());
        followManagement.unfollowUser(user1.getId());
        followManagement.unfollowUser(user2.getId());

        Assert.assertFalse(followManagement.followsBlog(blog1.getId()));
        Assert.assertFalse(followManagement.followsUser(user1.getId()));
        Assert.assertFalse(followManagement.followsUser(user2.getId()));
    }

    /**
     * Test that it is possible to follow and unfollow a tag.
     *
     * @throws Exception
     *             in case the test failed
     */
    @Test
    public void testFollowUnfollowTag() throws Exception {
        AuthenticationTestUtils.setSecurityContext(user1);
        String tag = UUID.randomUUID().toString();
        noteManagement
        .createNote(TestUtils.createCommonNote(blog1, user1.getId(),
                "This is a note with tag #" + tag), null);
        Long tagId = tagManagement.findTag(tag, Types.NOTE).getId();
        followManagement.followTag(tagId);
        Assert.assertTrue(followManagement.followsTag(tagId));
        followManagement.unfollowTag(tagId);
        Assert.assertFalse(followManagement.followsTag(tagId));
        try {
            followManagement.followTag(Long.MIN_VALUE);
            Assert.fail("Can't follow non existing tag.");
        } catch (NotFoundException e) {
            // Okay.
        }
    }

    /**
     * Test that it is possible to follow and unfollow a user.
     *
     * @throws Exception
     *             in case the test failed
     */
    @Test
    public void testFollowUnfollowUser() throws Exception {
        AuthenticationTestUtils.setSecurityContext(user2);
        followManagement.followUser(user1.getId());
        Assert.assertTrue(followManagement.followsUser(user1.getId()));

        followManagement.unfollowUser(user1.getId());
        Assert.assertFalse(followManagement.followsUser(user1.getId()));
        try {
            followManagement.followUser(9999L);
            Assert.fail("Can't follow non existing user.");
        } catch (NotFoundException e) {
            // Okay.
        }
    }

    /**
     * Test the retrieval of notes of followed blogs.
     *
     * @throws Exception
     *             in case the test failed
     */
    @Test
    public void testQueryFollowedBlog() throws Exception {
        User user1 = TestUtils.createRandomUser(false);
        User user2 = TestUtils.createRandomUser(false);
        AuthenticationTestUtils.setSecurityContext(user1);
        Blog blog1 = TestUtils.createRandomBlog(false, false, user1, user2);
        Blog blog2 = TestUtils.createRandomBlog(false, false, user1, user2);
        followManagement.followBlog(blog1.getId());
        noteManagement.createNote(TestUtils.createCommonNote(blog1, user1.getId(), "First note."),
                null);
        noteManagement.createNote(TestUtils.createCommonNote(blog1, user2.getId(), "Second note."),
                null);
        AuthenticationTestUtils.setSecurityContext(user1);
        NoteQueryParameters queryParameters = createQueryParameters();
        QueryManagement queryManagement = ServiceLocator.findService(QueryManagement.class);
        PageableList<SimpleNoteListItem> results = queryManagement.query(QUERY, queryParameters);
        Assert.assertEquals(results.size(), 2);

        followManagement.followUser(user1.getId());
        results = queryManagement.query(QUERY, queryParameters);
        Assert.assertEquals(results.size(), 2);

        Long noteId = noteManagement.createNote(
                TestUtils.createCommonNote(blog2, user1.getId(), "Third note."), null).getNoteId();

        results = queryManagement.query(QUERY, queryParameters);
        Assert.assertEquals(results.size(), 3);

        NoteStoringTO answer = TestUtils.createCommonNote(blog2, user2.getId(), "Fourth Note.");
        answer.setParentNoteId(noteId);
        Long replyId = noteManagement.createNote(answer, null).getNoteId();

        results = queryManagement.query(QUERY, queryParameters);
        Assert.assertEquals(results.size(), 3);

        followManagement.followDiscussionByNoteId(noteId);
        results = queryManagement.query(QUERY, queryParameters);
        Assert.assertEquals(results.size(), 4);

        noteManagement.deleteNote(replyId, true, true);
        results = queryManagement.query(QUERY, queryParameters);
        Assert.assertEquals(results.size(), 3);
    }
}
