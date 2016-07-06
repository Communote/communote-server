package com.communote.server.core.note;

import java.util.HashSet;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.communote.common.converter.Converter;
import com.communote.server.api.core.blog.BlogNotFoundException;
import com.communote.server.api.core.note.NoteData;
import com.communote.server.api.core.note.NoteManagementAuthorizationException;
import com.communote.server.api.core.note.NoteRenderContext;
import com.communote.server.api.core.note.NoteRenderMode;
import com.communote.server.api.core.note.NoteStoringTO;
import com.communote.server.api.core.note.processor.NoteStoringPreProcessorException;
import com.communote.server.api.core.property.PropertyHelper;
import com.communote.server.api.core.property.PropertyManagement;
import com.communote.server.api.core.property.StringPropertyTO;
import com.communote.server.core.blog.notes.processors.RepostNoteStoringPreProcessor;
import com.communote.server.core.blog.notes.processors.exceptions.InvalidPermissionForRepostException;
import com.communote.server.core.vo.blog.AutosaveNoteData;
import com.communote.server.model.blog.Blog;
import com.communote.server.model.note.Note;
import com.communote.server.model.note.NoteProperty;
import com.communote.server.model.user.User;
import com.communote.server.service.NoteService;
import com.communote.server.test.CommunoteIntegrationTest;
import com.communote.server.test.util.AuthenticationTestUtils;
import com.communote.server.test.util.TestUtils;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class NoteManagementTestForRepost extends CommunoteIntegrationTest {

    private final class RepostValidationConverter implements Converter<Note, Object> {
        private final User originalAuthor;
        private final Long originalNoteId;

        private RepostValidationConverter(User originalAuthor, Long originalNoteId) {
            this.originalAuthor = originalAuthor;
            this.originalNoteId = originalNoteId;
        }

        @Override
        public Object convert(Note source) {
            boolean originalAuthorFound = false;
            boolean originalNoteFound = false;
            for (NoteProperty noteProperty : source.getProperties()) {
                if (PropertyManagement.KEY_GROUP.equals(noteProperty.getKeyGroup())
                        && RepostNoteStoringPreProcessor.KEY_ORIGIN_AUTHOR_ID.equals(noteProperty
                                .getPropertyKey())
                        && originalAuthor.getId().toString()
                                .equals(noteProperty.getPropertyValue())) {
                    originalAuthorFound = true;
                } else if (PropertyManagement.KEY_GROUP.equals(noteProperty.getKeyGroup())
                        && RepostNoteStoringPreProcessor.KEY_ORIGIN_NOTE_ID.equals(noteProperty
                                .getPropertyKey())
                                && originalNoteId.toString().equals(noteProperty.getPropertyValue())) {
                    originalNoteFound = true;
                }
            }
            if (originalAuthorFound && originalNoteFound) {
                return source;
            }
            Assert.fail("This is not a valid repost");
            return source;
        }
    }

    @Autowired
    private NoteService noteService;

    private StringPropertyTO createRepostProperty(Long idOfNoteToRepost) {
        StringPropertyTO repostProperty = new StringPropertyTO();
        repostProperty.setKeyGroup(PropertyManagement.KEY_GROUP);
        repostProperty.setPropertyKey(RepostNoteStoringPreProcessor.KEY_ORIGIN_NOTE_ID);
        repostProperty.setPropertyValue(idOfNoteToRepost.toString());
        return repostProperty;
    }

    /**
     * Tests the repost functionality.
     *
     * @throws NoteStoringPreProcessorException
     * @throws BlogNotFoundException
     * @throws NoteManagementAuthorizationException
     */
    @Test
    public void testRepost() throws NoteStoringPreProcessorException, BlogNotFoundException,
            NoteManagementAuthorizationException {
        final User user1 = TestUtils.createRandomUser(false);
        User user2 = TestUtils.createRandomUser(false);
        Blog topic1 = TestUtils.createRandomBlog(true, true, user1, user2);
        Blog topic2 = TestUtils.createRandomBlog(false, false, user1);

        final Long note1Id = TestUtils.createAndStoreCommonNote(topic1, user1.getId(), "test1");
        Long note2Id = TestUtils.createAndStoreCommonNote(topic2, user1.getId(), "test2");

        StringPropertyTO repostProperty = createRepostProperty(note1Id);

        // Valid repost
        NoteStoringTO repostStoringTO = TestUtils
                .createCommonNote(topic1, user2.getId(), "repost1");
        repostStoringTO.getProperties().add(repostProperty);
        AuthenticationTestUtils.setSecurityContext(user2);
        Long repostId = noteService.createNote(repostStoringTO, new HashSet<String>()).getNoteId();
        Assert.assertNotNull(repostId);
        noteService.getNote(repostId, new RepostValidationConverter(user1, note1Id));

        // Invalid repost (no access for user 2 in topic 2)
        repostStoringTO = TestUtils.createCommonNote(topic1, user2.getId(), "repost1");
        repostProperty.setPropertyValue(note2Id.toString());
        repostStoringTO.getProperties().add(repostProperty);
        try {
            noteService.createNote(repostStoringTO, new HashSet<String>()).getNoteId();
            Assert.fail("It should not be possible to create a repost for a note the user can't access.");
        } catch (InvalidPermissionForRepostException e) {
            // Okay.
        }
    }

    /**
     * Test that autosaves of reposts are not mixed with autosaves of normal notes.
     *
     * @throws Exception
     *             in case the test failed
     */
    @Test
    public void testRepostAutosave() throws Exception {
        User author = TestUtils.createRandomUser(false);
        User repostAuthor = TestUtils.createRandomUser(false);
        Blog topic1 = TestUtils.createRandomBlog(false, false, author, repostAuthor);
        Blog topic2 = TestUtils.createRandomBlog(false, false, repostAuthor);
        Long noteIdOfNoteToRepost = TestUtils.createAndStoreCommonNote(topic1, author.getId(),
                "note for reposting");
        AuthenticationTestUtils.setSecurityContext(repostAuthor);
        // create autosave of repost
        NoteStoringTO repostStoringTO = TestUtils.createCommonNote(topic2, repostAuthor.getId(),
                "repost of note for reposting");
        repostStoringTO.getProperties().add(createRepostProperty(noteIdOfNoteToRepost));
        repostStoringTO.setPublish(false);
        Long noteIdOfRepostAutosave = noteService
                .createNote(repostStoringTO, new HashSet<String>()).getNoteId();
        AutosaveNoteData autosave = noteService.getAutosave(null, null,
                repostStoringTO.getProperties(), Locale.ENGLISH);
        Assert.assertNotNull(autosave);
        Assert.assertTrue(PropertyHelper.containsPropertyTO(autosave.getObjectProperties(),
                PropertyManagement.KEY_GROUP, RepostNoteStoringPreProcessor.KEY_ORIGIN_NOTE_ID,
                noteIdOfNoteToRepost.toString()));
        // the repost autosave should not be found if the property is not set
        autosave = noteService.getAutosave(null, null, null, Locale.ENGLISH);
        Assert.assertNull(autosave);
        // create normal note in same topic with same author. Autosave of repost must still exist
        // and new note must not have the repost properties.
        Long noteIdOfNewNote = TestUtils.createAndStoreCommonNote(topic2, repostAuthor.getId(),
                "new note");
        Assert.assertNotEquals(noteIdOfNewNote, noteIdOfRepostAutosave);
        NoteData note = noteService.getNote(noteIdOfNewNote, new NoteRenderContext(
                NoteRenderMode.PORTAL, Locale.ENGLISH));
        Assert.assertFalse(PropertyHelper.containsPropertyTO(note.getObjectProperties(),
                PropertyManagement.KEY_GROUP, RepostNoteStoringPreProcessor.KEY_ORIGIN_NOTE_ID,
                noteIdOfNoteToRepost.toString()));
        autosave = noteService.getAutosave(null, null, repostStoringTO.getProperties(),
                Locale.ENGLISH);
        Assert.assertNotNull(autosave);
        Assert.assertTrue(PropertyHelper.containsPropertyTO(autosave.getObjectProperties(),
                PropertyManagement.KEY_GROUP, RepostNoteStoringPreProcessor.KEY_ORIGIN_NOTE_ID,
                noteIdOfNoteToRepost.toString()));
        // create autosave of normal note. Autosave of repost must still exist
        NoteStoringTO noteStoringTO = TestUtils.createCommonNote(topic2, repostAuthor.getId(),
                "new note 2");
        noteStoringTO.setPublish(false);
        Long noteIdOfNormalAutosave = noteService.createNote(noteStoringTO, new HashSet<String>())
                .getNoteId();
        autosave = noteService.getAutosave(null, null, null, Locale.ENGLISH);
        Assert.assertNotNull(autosave);
        Assert.assertFalse(PropertyHelper.containsPropertyTO(autosave.getObjectProperties(),
                PropertyManagement.KEY_GROUP, RepostNoteStoringPreProcessor.KEY_ORIGIN_NOTE_ID,
                noteIdOfNoteToRepost.toString()));
        autosave = noteService.getAutosave(null, null, repostStoringTO.getProperties(),
                Locale.ENGLISH);
        Assert.assertNotNull(autosave);
        Assert.assertTrue(PropertyHelper.containsPropertyTO(autosave.getObjectProperties(),
                PropertyManagement.KEY_GROUP, RepostNoteStoringPreProcessor.KEY_ORIGIN_NOTE_ID,
                noteIdOfNoteToRepost.toString()));
        // publish the autosave. Autosave of repost must still exist.
        noteStoringTO.setAutosaveNoteId(noteIdOfNormalAutosave);
        noteStoringTO.setPublish(true);
        noteService.createNote(noteStoringTO, new HashSet<String>()).getNoteId();
        Assert.assertNull(noteService.getAutosave(null, null, null, Locale.ENGLISH));
        autosave = noteService.getAutosave(null, null, repostStoringTO.getProperties(),
                Locale.ENGLISH);
        Assert.assertNotNull(autosave);
        Assert.assertTrue(PropertyHelper.containsPropertyTO(autosave.getObjectProperties(),
                PropertyManagement.KEY_GROUP, RepostNoteStoringPreProcessor.KEY_ORIGIN_NOTE_ID,
                noteIdOfNoteToRepost.toString()));
        // publish the autosave of the repost. There should be no more autosaves.
        repostStoringTO.setAutosaveNoteId(noteIdOfRepostAutosave);
        repostStoringTO.setPublish(true);
        Long repostNoteId = noteService.createNote(repostStoringTO, new HashSet<String>())
                .getNoteId();
        Assert.assertNotNull(noteService.getNote(repostNoteId, new RepostValidationConverter(
                author, noteIdOfNoteToRepost)));
        Assert.assertNull(noteService.getAutosave(null, null, null, Locale.ENGLISH));
        Assert.assertNull(noteService.getAutosave(null, null, repostStoringTO.getProperties(),
                Locale.ENGLISH));
    }
}
