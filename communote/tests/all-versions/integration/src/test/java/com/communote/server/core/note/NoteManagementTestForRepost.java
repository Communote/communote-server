package com.communote.server.core.note;

import java.util.HashSet;

import junit.framework.Assert;

import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import com.communote.common.converter.Converter;
import com.communote.server.api.core.blog.BlogNotFoundException;
import com.communote.server.api.core.note.NoteManagementAuthorizationException;
import com.communote.server.api.core.note.NoteStoringTO;
import com.communote.server.api.core.note.processor.NoteStoringPreProcessorException;
import com.communote.server.api.core.property.PropertyManagement;
import com.communote.server.api.core.property.StringPropertyTO;
import com.communote.server.core.blog.notes.processors.RepostNoteStoringPreProcessor;
import com.communote.server.core.blog.notes.processors.exceptions.InvalidPermissionForRepostException;
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

    @Autowired
    private NoteService noteService;

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

        StringPropertyTO repostProperty = new StringPropertyTO();
        repostProperty.setKeyGroup(PropertyManagement.KEY_GROUP);
        repostProperty.setPropertyKey(RepostNoteStoringPreProcessor.KEY_ORIGIN_NOTE_ID);
        repostProperty.setPropertyValue(note1Id.toString());

        // Valid repost
        NoteStoringTO repostStoringTO = TestUtils
                .createCommonNote(topic1, user2.getId(), "repost1");
        repostStoringTO.getProperties().add(repostProperty);
        AuthenticationTestUtils.setSecurityContext(user2);
        Long repostId = noteService.createNote(repostStoringTO, new HashSet<String>()).getNoteId();
        Assert.assertNotNull(repostId);
        noteService.getNote(repostId, new Converter<Note, Object>() {
            @Override
            public Object convert(Note source) {
                boolean originalAuthorFound = false;
                boolean originalNoteFound = false;
                for (NoteProperty noteProperty : source.getProperties()) {
                    if (PropertyManagement.KEY_GROUP.equals(noteProperty.getKeyGroup())
                            && RepostNoteStoringPreProcessor.KEY_ORIGIN_AUTHOR_ID
                                    .equals(noteProperty.getPropertyKey())
                            && user1.getId().toString().equals(noteProperty.getPropertyValue())) {
                        originalAuthorFound = true;
                    } else if (PropertyManagement.KEY_GROUP.equals(noteProperty
                            .getKeyGroup())
                            && RepostNoteStoringPreProcessor.KEY_ORIGIN_NOTE_ID
                                    .equals(noteProperty.getPropertyKey())
                            && note1Id.toString().equals(noteProperty.getPropertyValue())) {
                        originalNoteFound = true;
                    }
                }
                if (originalAuthorFound && originalNoteFound) {
                    return source;
                }
                Assert.fail("This is not a valid repost");
                return source;
            }
        });

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
}
