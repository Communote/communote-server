package com.communote.server.core.note;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.communote.server.api.core.blog.BlogManagement;
import com.communote.server.api.core.note.NoteManagementAuthorizationException;
import com.communote.server.api.core.note.NoteStoringTO;
import com.communote.server.core.blog.MovingOfNonRootNotesNotAllowedException;
import com.communote.server.core.blog.NoteManagement;
import com.communote.server.core.general.RunInTransaction;
import com.communote.server.core.general.TransactionException;
import com.communote.server.core.general.TransactionManagement;
import com.communote.server.model.blog.Blog;
import com.communote.server.model.note.Note;
import com.communote.server.model.user.User;
import com.communote.server.persistence.blog.NoteDao;
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
public class NoteManagementTestForMove extends CommunoteIntegrationTest {

    @Autowired
    private NoteManagement noteManagement;
    @Autowired
    private NoteService noteService;
    @Autowired
    private BlogManagement topicManagement;
    @Autowired
    private TransactionManagement transactionManagement;
    @Autowired
    private NoteDao noteDao;
    private User user1;
    private Blog topic1;
    private User user2;
    private Blog topic2;

    /**
     * Setup method.
     */
    @BeforeClass(dependsOnGroups = GROUP_INTEGRATION_TEST_SETUP)
    public void beforeClass() {
        user1 = TestUtils.createRandomUser(false);
        user2 = TestUtils.createRandomUser(false);
        topic1 = TestUtils.createRandomBlog(true, true, user1);
        topic2 = TestUtils.createRandomBlog(true, false, user1);
    }

    /**
     * Test for moving to a topic, which does not exist.
     *
     * @throws NoteManagementAuthorizationException
     *             This exception is expected.
     */
    @Test(expectedExceptions = NoteManagementAuthorizationException.class)
    public void moveToNonExistingTopic() throws NoteManagementAuthorizationException {
        Long noteId = TestUtils.createAndStoreCommonNote(topic1, user1.getId(), "Hi");
        AuthenticationTestUtils.setSecurityContext(user1);
        noteManagement.moveToTopic(noteId, -1L);
    }

    /**
     * A test where the topic manager moves a note.
     *
     * @throws NoteManagementAuthorizationException
     *             The test should fail if thrown.
     */
    @Test
    public void moveToTopicAsManager() throws NoteManagementAuthorizationException {
        Long noteId = TestUtils.createAndStoreCommonNote(topic1, user2.getId(), "test");

        AuthenticationTestUtils.setSecurityContext(user1);
        noteManagement.moveToTopic(noteId, topic2.getId());
        Assert.assertEquals(noteDao.load(noteId).getBlog().getId(), topic2.getId());
    }

    /**
     * A test with dm's as note and replies.
     *
     * @throws NoteManagementAuthorizationException
     *             The test should fail if thrown.
     * @throws InterruptedException
     */
    @Test
    public void moveToTopicWithDMs() throws NoteManagementAuthorizationException,
            InterruptedException {
        Long rootNoteId = TestUtils.createAndStoreCommonNote(topic1, user1.getId(), "Hi");
        Long directReplyNoteId = TestUtils.createAndStoreCommonNote(topic1, user2.getId(), "d @"
                + user1.getAlias() + " Hi", rootNoteId);
        AuthenticationTestUtils.setSecurityContext(user1);

        List<Timestamp> crawlDates1 = new ArrayList<>();
        List<Timestamp> crawlDates2 = new ArrayList<>();

        TestUtils.addAndCheckCrawlLastModificationDateForNote(rootNoteId, crawlDates1);
        TestUtils.addAndCheckCrawlLastModificationDateForNote(directReplyNoteId, crawlDates2);
        // wait a second (because of MySQL which is only exact to the second)
        sleep(1000);
        noteManagement.moveToTopic(rootNoteId, topic2.getId());

        Assert.assertEquals(noteDao.load(rootNoteId).getBlog().getId(), topic2.getId());
        Assert.assertEquals(noteDao.load(directReplyNoteId).getBlog().getId(), topic2.getId());
        Assert.assertTrue(noteDao.load(directReplyNoteId).isDirect());

        TestUtils.addAndCheckCrawlLastModificationDateForNote(rootNoteId, crawlDates1);
        TestUtils.addAndCheckCrawlLastModificationDateForNote(directReplyNoteId, crawlDates2);

        rootNoteId = TestUtils.createAndStoreCommonNote(topic1, user1.getId(),
                "d @" + user2.getAlias() + " Hi");
        AuthenticationTestUtils.setSecurityContext(user1);
        noteManagement.moveToTopic(rootNoteId, topic2.getId());
        Assert.assertEquals(noteDao.load(rootNoteId).getBlog().getId(), topic2.getId());
        Assert.assertTrue(noteDao.load(rootNoteId).isDirect());

        rootNoteId = TestUtils.createAndStoreCommonNote(topic1, user1.getId(),
                "d @" + user2.getAlias() + " Hi");
        directReplyNoteId = TestUtils.createAndStoreCommonNote(topic1, user2.getId(),
                "d @" + user1.getAlias() + " Hi", rootNoteId);
        AuthenticationTestUtils.setSecurityContext(user1);

        crawlDates1.clear();
        crawlDates2.clear();
        TestUtils.addAndCheckCrawlLastModificationDateForNote(rootNoteId, crawlDates1);
        TestUtils.addAndCheckCrawlLastModificationDateForNote(directReplyNoteId, crawlDates2);

        noteManagement.moveToTopic(rootNoteId, topic2.getId());

        Assert.assertEquals(noteDao.load(rootNoteId).getBlog().getId(), topic2.getId());
        Assert.assertTrue(noteDao.load(rootNoteId).isDirect());
        Assert.assertEquals(noteDao.load(directReplyNoteId).getBlog().getId(), topic2.getId());
        Assert.assertTrue(noteDao.load(directReplyNoteId).isDirect());

        crawlDates1.clear();
        crawlDates2.clear();
        TestUtils.addAndCheckCrawlLastModificationDateForNote(rootNoteId, crawlDates1);
        TestUtils.addAndCheckCrawlLastModificationDateForNote(directReplyNoteId, crawlDates2);
    }

    /**
     * Test deletion of a topic after moving notes to another topic. This is a regression test for
     * KENMEI-6013: note deletion failed with a constraint violation because the followable-items
     * were not updated within the move operation.
     *
     * @throws Exception
     *             in case the test failed
     */
    public void testDeletionAfterMove() throws Exception {
        User user = TestUtils.createRandomUser(false);
        final Blog sourceTopic = TestUtils.createRandomBlog(false, false, user);
        final Blog targetTopic = TestUtils.createRandomBlog(false, false, user);
        final Long noteId = TestUtils
                .createAndStoreCommonNote(sourceTopic, user.getId(), "Move me");
        TestUtils.createAndStoreCommonNote(sourceTopic, user.getId(), "Move me", noteId);
        TestUtils.createAndStoreCommonNote(sourceTopic, user.getId(), "Move me", noteId);
        AuthenticationTestUtils.setSecurityContext(user);
        NoteStoringTO noteStoringTO = new NoteStoringTO();
        noteStoringTO.setBlogId(targetTopic.getId());
        noteService.updateNote(noteStoringTO, noteId, null);

        // caused the constraint violation
        topicManagement.deleteBlog(sourceTopic.getId(), null);

        // check that the followable items are correct
        transactionManagement.execute(new RunInTransaction() {
            @Override
            public void execute() throws TransactionException {
                Note note = noteDao.load(noteId);
                Assert.assertFalse(note.getFollowableItems().contains(sourceTopic.getFollowId()));
                Assert.assertTrue(note.getFollowableItems().contains(targetTopic.getFollowId()));
                for (Note child : note.getChildren()) {
                    Assert.assertFalse(child.getFollowableItems().contains(
                            sourceTopic.getFollowId()));
                    Assert.assertTrue(child.getFollowableItems()
                            .contains(targetTopic.getFollowId()));
                }
            }
        });
    }

    /**
     * Test for moving to a topic, the user can only read
     *
     * @throws NoteManagementAuthorizationException
     *             This exception is expected.
     */
    @Test(expectedExceptions = NoteManagementAuthorizationException.class)
    public void testMoveToReadOnlyTopic() throws NoteManagementAuthorizationException {
        Long noteId = TestUtils.createAndStoreCommonNote(topic1, user2.getId(), "Hi");
        AuthenticationTestUtils.setSecurityContext(user2);
        noteManagement.moveToTopic(noteId, topic2.getId());

    }

    /**
     * A test for {@link NoteManagement#moveToTopic(Long, Long)}
     *
     * @throws Exception
     *             in case the test failed
     */
    @Test
    public void testMoveToTopic() throws Exception {
        // Without reply
        Long noteId = TestUtils.createAndStoreCommonNote(topic1, user1.getId(), "Hi");
        AuthenticationTestUtils.setSecurityContext(user1);
        noteManagement.moveToTopic(noteId, topic2.getId());
        Assert.assertEquals(noteDao.load(noteId).getBlog().getId(), topic2.getId());

        List<Timestamp> crawlDates1 = new ArrayList<>();
        List<Timestamp> crawlDates2 = new ArrayList<>();

        // With reply
        Long rootNoteId = TestUtils.createAndStoreCommonNote(topic1, user1.getId(), "Hi");
        Long replyNoteId = TestUtils.createAndStoreCommonNote(topic1, user2.getId(), "Hi",
                rootNoteId);

        TestUtils.addAndCheckCrawlLastModificationDateForNote(rootNoteId, crawlDates1);
        TestUtils.addAndCheckCrawlLastModificationDateForNote(replyNoteId, crawlDates2);

        AuthenticationTestUtils.setSecurityContext(user2);
        try {
            noteManagement.moveToTopic(rootNoteId, topic2.getId());
            Assert.fail("User 2 shouldn't be able to move the discussion, because of missing rights.");
        } catch (NoteManagementAuthorizationException e) {
            // Okay.
        }

        AuthenticationTestUtils.setSecurityContext(user1);
        try {
            noteManagement.moveToTopic(replyNoteId, topic2.getId());
            Assert.fail("It should not be possible to move child notes.");
        } catch (MovingOfNonRootNotesNotAllowedException e) {
            // Okay.
        }

        // wait a second (because of MySQL which is only exact to the second)
        sleep(1000);
        noteManagement.moveToTopic(rootNoteId, topic2.getId());
        Assert.assertEquals(noteDao.load(rootNoteId).getBlog().getId(), topic2.getId());
        Assert.assertEquals(noteDao.load(replyNoteId).getBlog().getId(), topic2.getId());

        TestUtils.addAndCheckCrawlLastModificationDateForNote(rootNoteId, crawlDates1);
        TestUtils.addAndCheckCrawlLastModificationDateForNote(replyNoteId, crawlDates2);
    }
}
