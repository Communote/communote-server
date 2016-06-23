package com.communote.server.core.note;

import java.util.HashSet;
import java.util.Locale;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.note.NoteData;
import com.communote.server.api.core.note.NoteRenderContext;
import com.communote.server.api.core.user.UserData;
import com.communote.server.model.blog.Blog;
import com.communote.server.model.user.User;
import com.communote.server.service.NoteService;
import com.communote.server.test.CommunoteIntegrationTest;
import com.communote.server.test.util.TestUtils;


/**
 * Tests the notification functionality for crossposts.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class CrosspostNotificationTest extends CommunoteIntegrationTest {

    private User user1;
    private User user2;
    private User user3;
    private Blog blog1;
    private Blog blog3;
    private Blog blog2;

    /**
     * Assert the provided user aliases are all among the notified users and there is no other
     * notified user.
     * 
     * @param note
     *            the note to check
     * @param userAliases
     *            the aliases of the users that should have been notified
     */
    private void assertNotifiedUsers(NoteData note, String[] userAliases) {
        Assert.assertEquals(note.getNotifiedUsers().size(), userAliases.length);
        HashSet<String> aliases = new HashSet<String>();
        for (String alias : userAliases) {
            aliases.add(alias);
        }
        for (UserData user : note.getNotifiedUsers()) {
            Assert.assertTrue(aliases.contains(user.getAlias()));
            aliases.remove(user.getAlias());
        }
    }

    /**
     * Setup.
     * 
     * @throws Exception
     *             Exception.
     */
    @BeforeClass(dependsOnGroups = "integration-test-setup")
    public void setup() throws Exception {
        user1 = TestUtils.createRandomUser(true);
        user2 = TestUtils.createRandomUser(true);
        user3 = TestUtils.createRandomUser(true);
        blog1 = TestUtils.createRandomBlog(true, true, user1);
        blog2 = TestUtils.createRandomBlog(false, false, user1, user2);
        blog3 = TestUtils.createRandomBlog(false, false, user1, user3);
    }

    /**
     * Tests that the correct receivers are contained within a crosspost note.
     * 
     * @throws Exception
     *             Exception.
     */
    @Test
    public void testCrosspostNotifications() throws Exception {
        String message = "Hello @" + user1.getAlias() + " @" + user2.getAlias() + " @"
                + user3.getAlias() + " in Blogs &" + blog1.getNameIdentifier() + " &"
                + blog2.getNameIdentifier() + " &" + blog3.getNameIdentifier();
        NoteService noteManagement = ServiceLocator.instance().getService(NoteService.class);
        Long noteId = TestUtils.createAndStoreCommonNote(blog1, user1.getId(), message);
        // We assume, that the crossposts have the next ids in the row.
        NoteData noteInBlog1 = noteManagement.getNote(noteId, new NoteRenderContext(null,
                Locale.ENGLISH));
        NoteData note2 = noteManagement.getNote(noteId + 1, new NoteRenderContext(null,
                Locale.ENGLISH));
        NoteData note3 = noteManagement.getNote(noteId + 2, new NoteRenderContext(null,
                Locale.ENGLISH));
        Assert.assertNotNull(noteInBlog1);
        Assert.assertNotNull(note2);
        Assert.assertNotNull(note3);
        // associate note to correct blog, as we do not know which one is crosspost is created first
        NoteData noteInBlog2 = null, noteInBlog3 = null;
        if (note2.getBlog().getAlias().equals(blog2.getNameIdentifier())) {
            noteInBlog2 = note2;
        }
        if (note3.getBlog().getAlias().equals(blog2.getNameIdentifier())) {
            noteInBlog2 = note3;
        }
        if (note2.getBlog().getAlias().equals(blog3.getNameIdentifier())) {
            noteInBlog3 = note2;
        }
        if (note3.getBlog().getAlias().equals(blog3.getNameIdentifier())) {
            noteInBlog3 = note3;
        }
        Assert.assertNotNull(noteInBlog2,
                "Haven't found crosspost note in " + blog2.getNameIdentifier());
        Assert.assertNotNull(noteInBlog3,
                "Haven't found crosspost note in " + blog3.getNameIdentifier());

        assertNotifiedUsers(noteInBlog1,
                new String[] { user1.getAlias(), user2.getAlias(), user3.getAlias() });
        assertNotifiedUsers(noteInBlog2, new String[] { user1.getAlias(), user2.getAlias() });
        assertNotifiedUsers(noteInBlog3, new String[] { user1.getAlias(), user3.getAlias() });
    }
}
