package com.communote.server.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.communote.common.converter.IdentityConverter;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.note.NoteManagementAuthorizationException;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.model.blog.Blog;
import com.communote.server.model.note.Note;
import com.communote.server.model.user.User;
import com.communote.server.persistence.blog.NoteDao;
import com.communote.server.service.NoteService;
import com.communote.server.test.CommunoteIntegrationTest;
import com.communote.server.test.util.AuthenticationTestUtils;
import com.communote.server.test.util.TestUtils;

/**
 * This is a test for the {@link NoteService}
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class NoteServiceTest extends CommunoteIntegrationTest {

    @Autowired
    private NoteService noteService;

    /**
     * This tests {@link NoteService#deleteNotesOfUser(Long)}, especially if the update of the last
     * discussion creation date works.
     *
     * @throws AuthorizationException
     *             The test should fail, if this occurs.
     */
    @Test
    public void testDeleteNotesOfUser() throws AuthorizationException {
        NoteDao noteDao = ServiceLocator.findService(NoteDao.class);
        User user1 = TestUtils.createRandomUser(true);
        User user2 = TestUtils.createRandomUser(true);
        Blog topic = TestUtils.createRandomBlog(false, false, user1, user2);

        // Root note without answer -> Discussion is null
        Long parentNoteId = TestUtils.createAndStoreCommonNote(topic, user2.getId(), "Test");
        noteService.deleteNotesOfUser(user2.getId());
        Assert.assertNull(noteDao.load(parentNoteId));

        // Root note with answer from other user -> Anonymize
        parentNoteId = TestUtils.createAndStoreCommonNote(topic, user2.getId(), "Test");
        long date = noteDao.load(parentNoteId).getLastDiscussionNoteCreationDate().getTime();
        sleep(1200);
        Long answerId = TestUtils.createAndStoreCommonNote(topic, user1.getId(), "Test",
                parentNoteId);
        long answerDate = noteDao.load(answerId).getCreationDate().getTime();
        Assert.assertEquals(answerDate, noteDao.load(parentNoteId)
                .getLastDiscussionNoteCreationDate().getTime());
        noteService.deleteNotesOfUser(user2.getId());
        Assert.assertEquals(answerDate, noteDao.load(parentNoteId)
                .getLastDiscussionNoteCreationDate().getTime());

        // Answer from "deleted" user. -> Discussion will be refreshed.
        parentNoteId = TestUtils.createAndStoreCommonNote(topic, user1.getId(), "Test");
        date = noteDao.load(parentNoteId).getLastDiscussionNoteCreationDate().getTime();
        sleep(1200);
        answerId = TestUtils.createAndStoreCommonNote(topic, user2.getId(), "Test", parentNoteId);
        Assert.assertNotEquals(date, noteDao.load(parentNoteId).getLastDiscussionNoteCreationDate()
                .getTime());
        noteService.deleteNotesOfUser(user2.getId());
        Assert.assertNull(noteDao.load(answerId));
        Assert.assertEquals(date, noteDao.load(parentNoteId).getLastDiscussionNoteCreationDate()
                .getTime());
    }

    /**
     * This tests, that the last discussion creation date will be correctly set, after a note will
     * be deleted.
     *
     * @throws NoteManagementAuthorizationException
     *             The test will fail, if this exception is thrown.
     */
    @Test
    public void testUpdateLastDicussionDateOnDeleteNote()
            throws NoteManagementAuthorizationException {
        User user1 = TestUtils.createRandomUser(false);
        User user2 = TestUtils.createRandomUser(false);
        AuthenticationTestUtils.setSecurityContext(user1);
        Blog topic = TestUtils.createRandomBlog(false, false, user1, user2);
        Long note1Id = TestUtils.createAndStoreCommonNote(topic, user1.getId(), "1");
        sleep(1100);
        Long note11Id = TestUtils.createAndStoreCommonNote(topic, user1.getId(), "11", note1Id);
        sleep(1100);
        Long note12Id = TestUtils.createAndStoreCommonNote(topic, user1.getId(), "12", note11Id);
        IdentityConverter<Note> converter = new IdentityConverter<Note>();
        Note note1 = noteService.getNote(note1Id, converter);
        Note note12 = noteService.getNote(note12Id, converter);
        Assert.assertEquals(note1.getLastDiscussionNoteCreationDate(), note12.getCreationDate());
        noteService.deleteNote(note12Id, true, false);
        Assert.assertEquals(noteService.getNote(note1Id, converter)
                .getLastDiscussionNoteCreationDate(), noteService.getNote(note11Id, converter)
                .getCreationDate());
        // test with DMs in discussion (KENMEI-5761)
        sleep(1100);
        AuthenticationTestUtils.setSecurityContext(user2);
        Long dmNote12 = TestUtils.createAndStoreCommonNote(topic, user2.getId(),
                "d @" + user2.getAlias() + " 12", note1Id);
        Assert.assertNotNull(dmNote12);
        // dm does not change the lastDiscussionNoteCreationDate
        Assert.assertEquals(noteService.getNote(note1Id, converter)
                .getLastDiscussionNoteCreationDate(), noteService.getNote(note11Id, converter)
                .getCreationDate());
        sleep(1100);
        note12Id = TestUtils.createAndStoreCommonNote(topic, user2.getId(), "12", note11Id);
        Assert.assertEquals(noteService.getNote(note1Id, converter)
                .getLastDiscussionNoteCreationDate(), noteService.getNote(note12Id, converter)
                .getCreationDate());
        // to test KENMEI-5761 it's important to delete note as author because he is the only one
        // who sees dmNote12
        noteService.deleteNote(note12Id, true, false);
        Assert.assertEquals(noteService.getNote(note1Id, converter)
                .getLastDiscussionNoteCreationDate(), noteService.getNote(note11Id, converter)
                .getCreationDate());
    }
}
