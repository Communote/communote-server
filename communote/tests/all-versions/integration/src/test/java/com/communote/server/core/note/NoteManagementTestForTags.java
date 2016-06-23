package com.communote.server.core.note;

import java.util.HashSet;
import java.util.Locale;
import java.util.UUID;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.note.NoteData;
import com.communote.server.api.core.note.NoteRenderContext;
import com.communote.server.api.core.note.NoteStoringTO;
import com.communote.server.api.core.tag.TagStoreType;
import com.communote.server.api.core.tag.TagTO;
import com.communote.server.core.security.AuthenticationHelper;
import com.communote.server.model.blog.Blog;
import com.communote.server.model.user.User;
import com.communote.server.service.NoteService;
import com.communote.server.test.CommunoteIntegrationTest;
import com.communote.server.test.util.TestUtils;

/**
 * Tests for {@link NoteService} which focus on tagging.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class NoteManagementTestForTags extends CommunoteIntegrationTest {

    private User user;
    private Blog blog;
    private NoteService noteManagement;
    private NoteStoringTO noteStoringTO;
    private Long noteId;

    /**
     * Setup.
     */
    @BeforeClass(dependsOnGroups = "integration-test-setup")
    public void setup() {
        user = TestUtils.createRandomUser(false);
        blog = TestUtils.createRandomBlog(false, false, user);
        noteManagement = ServiceLocator.instance().getService(NoteService.class);
    }

    /**
     * Test for {@link NoteService#createNote()}.
     *
     * @throws Exception
     *             Exception.
     */
    @Test
    public void testCreateNote() throws Exception {
        AuthenticationHelper.setAsAuthenticatedUser(user);

        noteStoringTO = TestUtils.createCommonNote(blog, user.getId());
        TagTO tag = new TagTO(UUID.randomUUID().toString(), TagStoreType.Types.NOTE);
        noteStoringTO.getTags().add(tag);
        noteStoringTO.setUnparsedTags(null);
        noteId = noteManagement.createNote(noteStoringTO, new HashSet<String>()).getNoteId();
        NoteRenderContext context = new NoteRenderContext(null, Locale.ENGLISH);
        NoteData note = noteManagement.getNote(noteId, context);
        Assert.assertEquals(note.getTags().size(), 1);
        Assert.assertEquals(note.getTags().iterator().next().getName(), tag.getDefaultName());
    }

    /**
     * Test for {@link NoteService#updateNote(NoteStoringTO, Long, java.util.Set, boolean)}.
     *
     * @throws Exception
     *             Exception.
     */
    @Test(dependsOnMethods = "testCreateNote")
    public void testUpdateNote() throws Exception {
        AuthenticationHelper.setAsAuthenticatedUser(user);

        TagTO tag = new TagTO(UUID.randomUUID().toString(), TagStoreType.Types.NOTE);
        noteStoringTO.getTags().add(tag);
        noteManagement.updateNote(noteStoringTO, noteId, new HashSet<String>(), false);
        NoteRenderContext context = new NoteRenderContext(null, Locale.ENGLISH);
        NoteData note = noteManagement.getNote(noteId, context);
        Assert.assertEquals(note.getTags().size(), 2);
        noteStoringTO.getTags().clear();
        noteStoringTO.getTags().add(tag);
        noteManagement.updateNote(noteStoringTO, noteId, new HashSet<String>(), false);
        note = noteManagement.getNote(noteId, context);
        Assert.assertEquals(note.getTags().size(), 1);
        Assert.assertEquals(note.getTags().iterator().next().getName(), tag.getDefaultName());
        noteStoringTO.getTags().clear();
        noteManagement.updateNote(noteStoringTO, noteId, new HashSet<String>(), false);
        note = noteManagement.getNote(noteId, context);
        Assert.assertEquals(note.getTags().size(), 0);
    }
}
