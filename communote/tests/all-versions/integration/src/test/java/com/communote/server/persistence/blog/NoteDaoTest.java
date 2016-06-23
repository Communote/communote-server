package com.communote.server.persistence.blog;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.math.RandomUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.note.NoteStoringTO;
import com.communote.server.core.blog.FavoriteManagement;
import com.communote.server.core.blog.NoteNotFoundException;
import com.communote.server.core.vo.blog.NoteModificationResult;
import com.communote.server.core.vo.blog.NoteModificationStatus;
import com.communote.server.model.blog.Blog;
import com.communote.server.model.note.Note;
import com.communote.server.model.user.User;
import com.communote.server.service.NoteService;
import com.communote.server.test.CommunoteIntegrationTest;
import com.communote.server.test.util.AuthenticationTestUtils;
import com.communote.server.test.util.TestUtils;

/**
 * This class contains tests for {@link NoteDao}.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class NoteDaoTest extends CommunoteIntegrationTest {

    private User user2;
    private User user1;
    private User user3;
    private Blog blog;
    private NoteService noteService;
    private NoteDao noteDao;

    /**
     * Assert that the note IDs of the discussion are correct
     *
     * @param user
     *            the user to put in the security context before testing
     * @param discussionId
     *            the ID of the discussion
     * @param expectedNoteIds
     *            the IDs of the expected notes
     */
    private void assertCorrectDiscussionNoteIds(User user, Long discussionId,
            List<Long> expectedNoteIds) {
        AuthenticationTestUtils.setSecurityContext(user);
        List<Long> noteIds = noteDao.getNoteIdsOfDiscussion(discussionId);
        Assert.assertEquals(noteIds.size(), expectedNoteIds.size());
        for (Long noteId : noteIds) {
            Assert.assertTrue(expectedNoteIds.contains(noteId));
        }
    }

    /**
     * Creates the note from the provided TO and asserts that it was created correctly
     *
     * @param storingTO
     *            the TO
     * @return the ID of the created note
     * @throws Exception
     *             in case the creation failed
     */
    private Long createNote(NoteStoringTO storingTO) throws Exception {
        storingTO.setIsDirectMessage(false);
        NoteModificationResult result = noteService.createNote(storingTO, null);
        Assert.assertEquals(result.getStatus(), NoteModificationStatus.SUCCESS);
        Assert.assertNotNull(result.getNoteId());
        return result.getNoteId();
    }

    /**
     * Setup.
     *
     * @throws Exception
     *             Exception.
     */
    @BeforeClass(dependsOnGroups = "integration-test-setup")
    public void setup() throws Exception {
        noteService = ServiceLocator.instance().getService(NoteService.class);
        user1 = TestUtils.createRandomUser(false);
        user2 = TestUtils.createRandomUser(false);
        user3 = TestUtils.createRandomUser(false);
        blog = TestUtils.createRandomBlog(true, true, user1, user2, user3);
        noteDao = ServiceLocator.findService(NoteDao.class);
    }

    /**
     * This is a test for {@link NoteDao#findNearestNote(long, boolean)}
     *
     * @throws Exception
     *             The test will fail, if any exception is thrown.
     */
    @Test
    public void testFindNearestNote() throws Exception {
        NoteStoringTO note = TestUtils.createCommonNote(blog, user1.getId());
        note.setCreationDate(new Timestamp((System.currentTimeMillis() / 1000) * 1000 + 3600000));
        Long note1 = noteService.createNote(note, null).getNoteId();
        Long note2 = noteService.createNote(note, null).getNoteId();
        Long note3 = noteService.createNote(note, null).getNoteId();

        // First note does not have an older note.
        Assert.assertNull(noteDao.findNearestNote(note1, note.getCreationDate(), false));
        Assert.assertEquals(noteDao.findNearestNote(note1, note.getCreationDate(), true).getId(),
                note2);
        // Second note has older and younger note.
        Assert.assertEquals(noteDao.findNearestNote(note2, note.getCreationDate(), false).getId(),
                note1);
        Assert.assertEquals(noteDao.findNearestNote(note2, note.getCreationDate(), true).getId(),
                note3);
        // Last note does not have a younger note.
        Assert.assertEquals(noteDao.findNearestNote(note3, note.getCreationDate(), false).getId(),
                note2);
        Assert.assertNull(noteDao.findNearestNote(note3, note.getCreationDate(), true));
    }

    /**
     * Test for {@link NoteDao#getFavoriteNoteIds(Long, Long, Long)}
     *
     * @throws NoteNotFoundException
     *             Exception.
     */
    @Test
    public void testGetFavoriteNoteIds() throws NoteNotFoundException {
        User user = TestUtils.createRandomUser(false);
        Long userId = user.getId();
        Blog blog = TestUtils.createRandomBlog(true, true, user);
        List<Long> favoriteNotes = new ArrayList<Long>();
        FavoriteManagement favoriteManagement = ServiceLocator
                .findService(FavoriteManagement.class);
        do {
            if (RandomUtils.nextBoolean() && RandomUtils.nextBoolean()) {
                Long noteId = TestUtils.createAndStoreCommonNote(blog, userId, " Test"
                        + favoriteNotes.size());
                AuthenticationTestUtils.setSecurityContext(user);
                favoriteManagement.markNoteAsFavorite(noteId);
                favoriteNotes.add(noteId);
            }
        } while (favoriteNotes.size() < 100);
        for (int i = 0; i < 10; i++) {
            Long start = favoriteNotes.get(i * 10);
            Long end = favoriteNotes.get(i * 10 + 9);
            Collection<Long> favoriteNoteIds = noteDao.getFavoriteNoteIds(userId, start, end);
            Assert.assertEquals(favoriteNoteIds.size(), 10);
            for (int e = i * 10; e < i * 10 + 10; e++) {
                Assert.assertTrue(favoriteNoteIds.contains(favoriteNotes.get(e)));
            }
        }
        // No Range.
        Collection<Long> favoriteNoteIds = noteDao.getFavoriteNoteIds(userId, -1L, -1L);
        Assert.assertEquals(favoriteNoteIds.size(), 100);
        for (Long noteId : favoriteNoteIds) {
            Assert.assertTrue(favoriteNotes.contains(noteId));
        }

        // Out of Range
        Assert.assertEquals(noteDao.getFavoriteNoteIds(userId, -1L, favoriteNotes.get(0) - 1)
                .size(), 0);
        Assert.assertEquals(noteDao.getFavoriteNoteIds(userId, favoriteNotes.get(99) + 1, -1L)
                .size(), 0);
    }

    /**
     * Test for {@link NoteDao#getNoteIdsOfDiscussion(Long)}
     *
     * @throws Exception
     *             in case the test fails
     */
    @Test
    public void testGetNoteIdsOfDiscussion() throws Exception {
        Long discussionId = TestUtils.createAndStoreCommonNote(blog, user1.getId(), "root message");
        Assert.assertEquals(noteDao.getNoteIdsOfDiscussion(discussionId).size(), 0);
        List<Long> commentIds = new ArrayList<Long>();
        int count = 3;
        for (int i = 1; i <= count; i++) {
            NoteStoringTO comment = TestUtils.createCommonNote(blog, user1.getId(), "comment " + i);
            comment.setParentNoteId(discussionId);
            commentIds.add(createNote(comment));
            assertCorrectDiscussionNoteIds(user1, discussionId, commentIds);
            assertCorrectDiscussionNoteIds(user2, discussionId, commentIds);
        }

        // comment on comment test
        NoteStoringTO comment = TestUtils.createCommonNote(blog, user1.getId(),
                "comment on comment");
        comment.setParentNoteId(commentIds.get(0));
        commentIds.add(createNote(comment));
        assertCorrectDiscussionNoteIds(user1, discussionId, commentIds);
        assertCorrectDiscussionNoteIds(user2, discussionId, commentIds);

        // create autosave and assert it is not counted
        NoteStoringTO autosave = TestUtils.createCommonAutosave(blog, user1.getId(),
                "comment autosave");
        // do not add to commentIds because it must count
        autosave.setParentNoteId(commentIds.get(0));
        createNote(autosave);
        assertCorrectDiscussionNoteIds(user1, discussionId, commentIds);
        assertCorrectDiscussionNoteIds(user2, discussionId, commentIds);

        // create DM and check that it is counted as well
        comment = TestUtils.createCommonNote(blog, user1.getId(), "comment as DM");
        comment.setParentNoteId(discussionId);
        comment.setIsDirectMessage(true);
        comment.getUsersNotToNotify().add(user3.getAlias());
        commentIds.add(createNote(comment));
        assertCorrectDiscussionNoteIds(user1, discussionId, commentIds);
        assertCorrectDiscussionNoteIds(user2, discussionId, commentIds);
        assertCorrectDiscussionNoteIds(user3, discussionId, commentIds);
    }

    /**
     * Tests {@link NoteDao#getNotesForBlog(Long, Long, Integer)}
     */
    @Test
    public void testGetNotesForBlog() {
        for (int i = 0; i < 5 + RandomUtils.nextInt(5); i++) {
            Blog blog = TestUtils.createRandomBlog(true, true, user1);
            List<Long> noteIds = new ArrayList<Long>();
            int numberOfNotes = 50 + RandomUtils.nextInt(50);
            for (int e = 0; e < numberOfNotes; e++) {
                noteIds.add(TestUtils.createAndStoreCommonNote(blog, user1.getId(), "Note " + i
                        + "." + e));
            }
            Assert.assertEquals(noteDao.getNotesForBlog(blog.getId(), null, null).size(),
                    noteIds.size());
            Assert.assertEquals(noteDao.getNotesForBlog(blog.getId(), 0L, 0).size(), noteIds.size());
            int limit = 5 + RandomUtils.nextInt(10);
            for (int f = 0; f < numberOfNotes - limit; f++) {
                List<Note> notes = noteDao.getNotesForBlog(blog.getId(), noteIds.get(f), limit);
                Assert.assertEquals(notes.size(), limit);
                List<Long> subNotesIds = noteIds.subList(f, f + limit);
                for (Note note : notes) {
                    Assert.assertTrue(subNotesIds.contains(note.getId()));
                }
            }
            for (int f = numberOfNotes - limit; f < numberOfNotes; f++) {
                List<Note> notes = noteDao.getNotesForBlog(blog.getId(), noteIds.get(f), limit);
                Assert.assertEquals(notes.size(), numberOfNotes - f);
                List<Long> subNotesIds = noteIds.subList(f, noteIds.size());
                for (Note note : notes) {
                    Assert.assertTrue(subNotesIds.contains(note.getId()));
                }
            }
        }
    }
}
