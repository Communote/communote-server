package com.communote.server.core.note;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.apache.directory.shared.ldap.util.ArrayUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.communote.common.converter.IdentityConverter;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.note.NoteData;
import com.communote.server.api.core.note.NoteManagementAuthorizationException;
import com.communote.server.api.core.note.NoteRenderContext;
import com.communote.server.api.core.note.NoteStoringTO;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.api.core.user.UserData;
import com.communote.server.core.blog.notes.processors.exceptions.DirectMessageMissingRecipientException;
import com.communote.server.core.blog.notes.processors.exceptions.DirectMessageWrongRecipientForAnswerException;
import com.communote.server.core.blog.notes.processors.exceptions.MessageKeyNoteContentException;
import com.communote.server.core.vo.blog.NoteModificationResult;
import com.communote.server.core.vo.blog.NoteModificationStatus;
import com.communote.server.core.vo.uti.UserNotificationResult;
import com.communote.server.model.blog.Blog;
import com.communote.server.model.note.Note;
import com.communote.server.model.user.User;
import com.communote.server.service.NoteService;
import com.communote.server.test.CommunoteIntegrationTest;
import com.communote.server.test.util.AuthenticationTestUtils;
import com.communote.server.test.util.TestUtils;
import com.communote.server.test.util.TestUtils.TestUtilsException;

/**
 * This contains tests for the direct messaging functionality.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
@Test
public class DirectMessageTest extends CommunoteIntegrationTest {

    private NoteService noteManagement;
    private User user1;
    private User user2;
    private User user3;
    private Blog blog1;
    private Blog blog2;

    /**
     * Creates a direct message via inline notification.
     *
     * @param aliases
     *            The receivers of the message.
     * @return A message.
     */
    private String createInlineDirectMessage(String... aliases) {
        StringBuilder sb = new StringBuilder("d ");
        for (String alias : aliases) {
            sb.append("@");
            sb.append(alias);
            sb.append(" ");
        }
        sb.append(UUID.randomUUID().toString());
        return sb.toString();
    }

    /**
     * Creates a direct message via inline notification.
     *
     * @param receiver
     *            The receiver of the message.
     * @return A message.
     */
    private String createInlineDirectMessage(User receiver) {
        return createInlineDirectMessage(receiver.getAlias());
    }

    /**
     * Setups all common parameters for this tests.
     *
     * @throws Exception
     *             in case the preparation failed
     */
    @BeforeClass(dependsOnGroups = "integration-test-setup")
    public void setup() throws Exception {
        user1 = TestUtils.createRandomUser(false);
        user2 = TestUtils.createRandomUser(false);
        user3 = TestUtils.createRandomUser(false);
        blog1 = TestUtils.createRandomBlog(false, false, user1, user2, user3);
        blog2 = TestUtils.createRandomBlog(false, false, user1, user2, user3);
        noteManagement = ServiceLocator.instance().getService(NoteService.class);
    }

    /**
     * Tests the creation of a reply to a direct message.
     *
     * @throws Exception
     *             in case the test failed
     */
    @Test
    public void testAnswerToDirectMessage() throws Exception {
        AuthenticationTestUtils.setSecurityContext(user1);
        NoteStoringTO noteStoringTO = TestUtils.createCommonNote(blog1, user1.getId(),
                createInlineDirectMessage(user2));
        NoteModificationResult result = noteManagement.createNote(noteStoringTO, null);
        AuthenticationTestUtils.setSecurityContext(user2);
        NoteStoringTO answer = TestUtils.createCommonNote(blog1, user2.getId(),
                createInlineDirectMessage(user1));
        answer.setParentNoteId(result.getNoteId());
        NoteModificationResult answerResult = noteManagement.createNote(answer, null);
        Assert.assertTrue(answerResult.isDirect());
        NoteRenderContext context = new NoteRenderContext(null, Locale.ENGLISH);
        NoteData note = noteManagement.getNote(answerResult.getNoteId(), context);
        Assert.assertTrue(note.isDirect());
        Assert.assertEquals(note.getId(), answerResult.getNoteId());
        Assert.assertEquals(note.getParent().getId(), result.getNoteId());
        AuthenticationTestUtils.setSecurityContext(user1);
        note = noteManagement.getNote(answerResult.getNoteId(), context);
        Assert.assertTrue(note.isDirect());
        Assert.assertEquals(note.getId(), answerResult.getNoteId());
    }

    /**
     * Test that a subset of the DM readers can be notified in a reply to that DM.
     *
     * @throws Exception
     *             in case the test failed
     */
    @Test
    public void testAnswerToDirectMessageToMultipleReceivers() throws Exception {
        AuthenticationTestUtils.setSecurityContext(user1);
        NoteStoringTO noteStoringTO = TestUtils.createCommonNote(blog1, user1.getId(), UUID
                .randomUUID().toString());
        noteStoringTO.setIsDirectMessage(true);
        Set<String> usersToNotify = new HashSet<String>();
        usersToNotify.add(user2.getAlias());
        usersToNotify.add(user3.getAlias());
        noteStoringTO.setUsersToNotify(usersToNotify);
        NoteModificationResult result = noteManagement.createNote(noteStoringTO, null);

        NoteStoringTO answerTO = TestUtils.createCommonNote(blog1, user2.getId(), UUID.randomUUID()
                .toString());
        answerTO.setIsDirectMessage(true);
        Set<String> usersToNotifyInAnswer = new HashSet<String>();
        usersToNotifyInAnswer.add(user1.getAlias());
        answerTO.setUsersToNotify(usersToNotifyInAnswer);
        answerTO.setParentNoteId(result.getNoteId());
        NoteModificationResult answerResult = noteManagement.createNote(answerTO, null);
        NoteRenderContext context = new NoteRenderContext(null, Locale.ENGLISH);
        NoteData answer = noteManagement.getNote(answerResult.getNoteId(), context);
        Assert.assertTrue(answer.isDirect());
        Assert.assertEquals(answer.getNotifiedUsers().size(), 1);
        Assert.assertEquals(answer.getId(), answerResult.getNoteId());
        Assert.assertEquals(answer.getParent().getId(), result.getNoteId());
        // test access
        AuthenticationTestUtils.setSecurityContext(user2);
        answer = noteManagement.getNote(answerResult.getNoteId(), context);
        Assert.assertEquals(answer.getId(), answerResult.getNoteId());
        AuthenticationTestUtils.setSecurityContext(user3);
        try {
            answer = noteManagement.getNote(answerResult.getNoteId(), context);
            Assert.fail(user3.getAlias()
                    + " has access to direct message although he was not recipient");
        } catch (AuthorizationException e) {
            // expected
        }
    }

    /**
     * Test that the answer to a direct message must have the author of the original message as
     * recipient.
     *
     * @throws DirectMessageWrongRecipientForAnswerException
     *             in case the test succeeded
     * @throws Exception
     *             in case the test failed
     */
    @Test(expectedExceptions = { DirectMessageWrongRecipientForAnswerException.class })
    public void testAnswerWithWrongRecipient() throws Exception {
        AuthenticationTestUtils.setSecurityContext(user1);
        NoteStoringTO noteStoringTO = TestUtils.createCommonNote(blog1, user1.getId(),
                createInlineDirectMessage(user2));
        NoteModificationResult result = noteManagement.createNote(noteStoringTO, null);
        AuthenticationTestUtils.setSecurityContext(user2);
        NoteStoringTO answer = TestUtils.createCommonNote(blog1, user2.getId(),
                createInlineDirectMessage(user3));
        answer.setParentNoteId(result.getNoteId());
        noteManagement.createNote(answer, null);
        Assert.fail("Answers to direct messages must have the original auther as recipient.");
    }

    /**
     * Tests that DMs without read access are not contained in discussion count.
     *
     * @throws Exception
     *             in case the test failed
     */
    @Test
    public void testCorrectNumberOfComments() throws Exception {
        Blog blog3 = TestUtils.createRandomBlog(false, false, user1, user2, user3);
        Long firstNodeId = noteManagement.createNote(
                TestUtils.createCommonNote(blog3, user1.getId(), "Note 1"), null).getNoteId();
        NoteStoringTO answer = TestUtils.createCommonNote(blog3, user2.getId(), "Note 2");
        answer.setParentNoteId(firstNodeId);
        noteManagement.createNote(answer, null);
        answer = TestUtils
                .createCommonNote(blog3, user3.getId(), "d @" + user1.getAlias() + " Hi.");
        answer.setParentNoteId(firstNodeId);
        noteManagement.createNote(answer, null);

        AuthenticationTestUtils.setSecurityContext(user1);
        Assert.assertEquals(noteManagement.getNumberOfNotesInDiscussion(firstNodeId), 3);
        AuthenticationTestUtils.setSecurityContext(user2);
        Assert.assertEquals(noteManagement.getNumberOfNotesInDiscussion(firstNodeId), 2);
        AuthenticationTestUtils.setSecurityContext(user3);
        Assert.assertEquals(noteManagement.getNumberOfNotesInDiscussion(firstNodeId), 3);
    }

    /**
     * Tests sending the DM via inline syntax and without direct selection.
     *
     * @throws Exception
     *             in case the test failed
     */
    @Test
    public void testDirectMessageWithInlineSyntax() throws Exception {
        AuthenticationTestUtils.setSecurityContext(user1);
        NoteStoringTO noteStoringTO = TestUtils.createCommonNote(blog1, user1.getId(),
                createInlineDirectMessage(user2));
        NoteModificationResult result = noteManagement.createNote(noteStoringTO, null);
        Assert.assertEquals(result.getStatus(), NoteModificationStatus.SUCCESS);
        Assert.assertTrue(result.isDirect());
        NoteRenderContext context = new NoteRenderContext(null, Locale.ENGLISH);
        NoteData note = noteManagement.getNote(result.getNoteId(), context);
        Assert.assertTrue(note.isDirect());
        Assert.assertEquals(note.getNotifiedUsers().size(), 1);
        Assert.assertEquals(note.getNotifiedUsers().iterator().next().getId(), user2.getId());
        // test access by notified user
        AuthenticationTestUtils.setSecurityContext(user2);
        note = noteManagement.getNote(result.getNoteId(), context);
        Assert.assertEquals(note.isDirect(), true);
        Assert.assertEquals(note.getId(), result.getNoteId());
        // test no access by not notified user
        AuthenticationTestUtils.setSecurityContext(user3);
        try {
            note = noteManagement.getNote(result.getNoteId(), context);
            Assert.fail(user3.getAlias()
                    + " has access to direct message although he was not recipient");
        } catch (AuthorizationException e) {
            // expected exception
        }
    }

    /**
     * Tests sending the DM with legal and illegal recipients
     *
     * @throws Exception
     *             in case the test failed
     */
    @Test
    public void testDirectMessageWithLegalAndIllegalRecipients() throws Exception {
        String randomAlias = TestUtils.createRandomUserAlias();
        Blog blogWithOneMember = TestUtils.createRandomBlog(false, false, user1);
        AuthenticationTestUtils.setSecurityContext(user1);
        NoteStoringTO noteStoringTO = TestUtils.createCommonNote(blogWithOneMember, user1.getId(),
                createInlineDirectMessage(new String[] { user1.getAlias(), user2.getAlias(),
                        randomAlias }));
        NoteModificationResult result = noteManagement.createNote(noteStoringTO, null);
        Assert.assertEquals(result.getStatus(), NoteModificationStatus.SUCCESS);
        UserNotificationResult notifyResult = result.getUserNotificationResult();
        Assert.assertEquals(notifyResult.getUninformableUsers().size(), 1);
        Assert.assertTrue(notifyResult.getUninformableUsers().contains(user2.getAlias()));
        Assert.assertEquals(notifyResult.getUnresolvableUsers().size(), 1);
        Assert.assertTrue(notifyResult.getUnresolvableUsers().contains(randomAlias));
    }

    /**
     * Tests sending the DM via direct user selection instead of the inline syntax.
     *
     * @throws Exception
     *             in case the test failed
     */
    @Test
    public void testDirectMessageWithUserSelection() throws Exception {
        AuthenticationTestUtils.setSecurityContext(user1);
        NoteStoringTO noteStoringTO = TestUtils.createCommonNote(blog1, user1.getId(), UUID
                .randomUUID().toString());
        noteStoringTO.setIsDirectMessage(true);
        Set<String> usersToNotify = new HashSet<String>();
        usersToNotify.add(user2.getAlias());
        noteStoringTO.setUsersToNotify(usersToNotify);
        NoteModificationResult result = noteManagement.createNote(noteStoringTO, null);
        Assert.assertEquals(result.getStatus(), NoteModificationStatus.SUCCESS);
        Assert.assertTrue(result.isDirect());
        NoteRenderContext context = new NoteRenderContext(null, Locale.ENGLISH);
        NoteData note = noteManagement.getNote(result.getNoteId(), context);
        Assert.assertTrue(note.isDirect());
        Assert.assertEquals(note.getNotifiedUsers().size(), 1);
        Assert.assertEquals(note.getNotifiedUsers().iterator().next().getId(), user2.getId());
        // test access by notified user
        AuthenticationTestUtils.setSecurityContext(user2);
        note = noteManagement.getNote(result.getNoteId(), context);
        Assert.assertEquals(note.isDirect(), true);
        Assert.assertEquals(note.getId(), result.getNoteId());
        // test no access by not notified user
        AuthenticationTestUtils.setSecurityContext(user3);
        try {
            note = noteManagement.getNote(result.getNoteId(), context);
            Assert.fail(user3.getAlias()
                    + " has access to direct message although he was not receiver");
        } catch (AuthorizationException e) {
            // expected exception
        }
    }

    /**
     * Tests sending the DM via direct user selection and the inline syntax.
     *
     * @throws Exception
     *             in case the test failed
     */
    @Test
    public void testDirectMessageWithUserSelectionAndInlineSyntax() throws Exception {
        AuthenticationTestUtils.setSecurityContext(user1);
        NoteStoringTO noteStoringTO = TestUtils.createCommonNote(blog1, user1.getId(),
                createInlineDirectMessage(user2));
        noteStoringTO.setIsDirectMessage(true);
        Set<String> usersToNotify = new HashSet<String>();
        usersToNotify.add(user2.getAlias());
        noteStoringTO.setUsersToNotify(usersToNotify);
        NoteModificationResult result = noteManagement.createNote(noteStoringTO, null);
        Assert.assertEquals(result.getStatus(), NoteModificationStatus.SUCCESS);
        Assert.assertTrue(result.isDirect());
        NoteRenderContext context = new NoteRenderContext(null, Locale.ENGLISH);
        NoteData note = noteManagement.getNote(result.getNoteId(), context);
        Assert.assertTrue(note.isDirect());
        Assert.assertEquals(note.getNotifiedUsers().size(), 1);
        Assert.assertEquals(note.getNotifiedUsers().iterator().next().getId(), user2.getId());

        // test that different users can be added as DM recipients
        noteStoringTO = TestUtils.createCommonNote(blog1, user1.getId(),
                createInlineDirectMessage(user2));
        noteStoringTO.setIsDirectMessage(true);
        usersToNotify = new HashSet<String>();
        usersToNotify.add(user3.getAlias());
        noteStoringTO.setUsersToNotify(usersToNotify);
        result = noteManagement.createNote(noteStoringTO, null);
        note = noteManagement.getNote(result.getNoteId(), context);
        Assert.assertEquals(result.getStatus(), NoteModificationStatus.SUCCESS);
        Assert.assertTrue(result.isDirect());
        Assert.assertEquals(note.getNotifiedUsers().size(), 2);
        for (UserData notifiedUser : note.getNotifiedUsers()) {
            Assert.assertTrue(notifiedUser.getAlias().equals(user2.getAlias())
                    || notifiedUser.getAlias().equals(user3.getAlias()));
        }
        AuthenticationTestUtils.setSecurityContext(user2);
        note = noteManagement.getNote(result.getNoteId(), context);
        Assert.assertEquals(note.isDirect(), true);
        Assert.assertEquals(note.getId(), result.getNoteId());
        AuthenticationTestUtils.setSecurityContext(user3);
        note = noteManagement.getNote(result.getNoteId(), context);
        Assert.assertEquals(note.isDirect(), true);
        Assert.assertEquals(note.getId(), result.getNoteId());
    }

    /**
     * This tests, that it is possible
     * <ul>
     * <li>to edit a DM without answer
     * <li>not to edit a dm with answer
     * </ul>
     *
     * @throws Exception
     *             Thrown when something fails.
     */
    @Test
    public void testEditDirectMessage() throws Exception {
        User user = TestUtils.createRandomUser(false);
        Blog topic = TestUtils.createRandomBlog(false, false, user);
        NoteStoringTO noteStoringTO = TestUtils.createCommonNote(topic, user.getId(),
                "d @" + user.getAlias());

        Long noteId = noteManagement.createNote(noteStoringTO, null).getNoteId();
        Assert.assertTrue(noteManagement.getNote(noteId, new IdentityConverter<Note>()).isDirect());

        // Should work.
        noteManagement.updateNote(noteStoringTO, noteId, null, false);

        TestUtils.createAndStoreCommonNote(topic, user.getId(),
                "d @" + user.getAlias() + " blabla", noteId);

        // Should fail.
        try {
            noteManagement.updateNote(noteStoringTO, noteId, null, false);
            Assert.fail("It should not be possible to edit a direct message which has comments.");
        } catch (NoteManagementAuthorizationException e) {
            // Okay
        }
    }

    /**
     * Test that it is not possible to answer to a direct message with a direct message if the
     * author of the comment is not allowed to read the parent direct message.
     *
     * @throws Throwable
     *             Should be a NoteManagementAuthorizationException
     */
    @Test(expectedExceptions = NoteManagementAuthorizationException.class)
    public void testFailureOnAnswerToNotAccessibleDirectMessage() throws Throwable {
        User user1 = TestUtils.createRandomUser(false);
        User user2 = TestUtils.createRandomUser(false);
        User user3 = TestUtils.createRandomUser(false);
        Blog topic = TestUtils.createRandomBlog(true, true, user1);
        Long parentNoteId = TestUtils.createAndStoreCommonNote(topic, user1.getId(),
                "d @" + user1.getAlias() + " @" + user2.getAlias());

        // this should throw a NoteManagementAuthorization exception
        try {
            TestUtils.createAndStoreCommonNote(topic, user3.getId(), "d @" + user1.getAlias(),
                    parentNoteId);
        } catch (TestUtilsException e) {
            throw e.getCause();
        }
        Assert.fail("This should not be possible.");
    }

    /**
     * Test that it is not possible to mention users in a direct message, which is created as a
     * reply to another direct message, that are not recipients or the author of the parent note.
     *
     * @throws DirectMessageWrongRecipientForAnswerException
     *             in case the test succeeded
     * @throws Exception
     *             in case the test failed
     */
    @Test(expectedExceptions = { DirectMessageWrongRecipientForAnswerException.class })
    public void testFailureOnAnswerWithWrongRecipient()
            throws DirectMessageWrongRecipientForAnswerException, Exception {
        AuthenticationTestUtils.setSecurityContext(user1);
        NoteStoringTO noteStoringTO = TestUtils.createCommonNote(blog1, user1.getId(), "d @"
                + user1.getAlias() + " @" + user2.getAlias() + " This is the message,");
        NoteStoringTO answer = TestUtils.createCommonNote(blog1, user1.getId(),
                "d @" + user1.getAlias() + " @" + user3.getAlias() + " This is the message,");
        Long noteId = noteManagement.createNote(noteStoringTO, null).getNoteId();
        answer.setParentNoteId(noteId);
        noteManagement.createNote(answer, null);
        Assert.fail("Answers to direct messages may not have new recipients.");
    }

    /**
     * Tests, that it is not possible to create posts in multiple blogs.
     *
     * @throws Exception
     *             in case the test failed
     */
    @Test
    public void testFailureOnCrossPosting() throws Exception {
        AuthenticationTestUtils.setSecurityContext(user1);
        NoteStoringTO noteStoringTO = TestUtils.createCommonNote(blog1, user1.getId(),
                createInlineDirectMessage(user2) + " &" + blog2.getNameIdentifier());
        try {
            noteManagement.createNote(noteStoringTO, null);
            Assert.fail("Direct message crosspost is not allowed");
        } catch (MessageKeyNoteContentException e) {
            Assert.assertEquals(e.getMessageKey(),
                    "error.blogpost.blog.content.processing.failed.direct.multiple.blogs");
        }
    }

    /**
     * Checks that it is not possible to write a DM without recipient.
     *
     * @throws Exception
     *             in case the test failed
     */
    @Test
    public void testFailureOnMissingRecipient() throws Exception {
        AuthenticationTestUtils.setSecurityContext(user1);
        NoteStoringTO noteStoringTO = TestUtils.createCommonNote(blog1, user1.getId(), UUID
                .randomUUID().toString());
        noteStoringTO.setIsDirectMessage(true);
        try {
            noteManagement.createNote(noteStoringTO, null);
            Assert.fail("DM without receipient is not possible");
        } catch (DirectMessageMissingRecipientException e) {
            // expected exception
            Assert.assertNull(e.getUninformableUsers());
            Assert.assertNull(e.getUnresolvableUsers());
        }
    }

    /**
     * Checks, that it is not possible to write a DM with a receiver, which has no access to the
     * given blog.
     *
     * @throws Exception
     *             in case the test failed
     */
    @Test
    public void testFailureOnUninformableRecipient() throws Exception {
        Blog blogWithOneMember = TestUtils.createRandomBlog(false, false, user1);
        AuthenticationTestUtils.setSecurityContext(user1);
        NoteStoringTO noteStoringTO = TestUtils.createCommonNote(blogWithOneMember, user1.getId(),
                createInlineDirectMessage(user2));
        try {
            noteManagement.createNote(noteStoringTO, null);
            Assert.fail("DM to user without permission should fail");
        } catch (DirectMessageMissingRecipientException e) {
            // expected exception
            Assert.assertNull(e.getUnresolvableUsers());
            Assert.assertNotNull(e.getUninformableUsers());
            Assert.assertEquals(e.getUninformableUsers().length, 1);
            Assert.assertEquals(e.getUninformableUsers()[0], user2.getAlias());
        }
    }

    /**
     * Checks, that it is not possible to write a DM with receivers that do not have access to the
     * given blog.
     *
     * @throws Exception
     *             in case the test failed
     */
    @Test
    public void testFailureOnUninformableRecipients() throws Exception {
        Blog blogWithOneMember = TestUtils.createRandomBlog(false, false, user1);
        AuthenticationTestUtils.setSecurityContext(user1);
        NoteStoringTO noteStoringTO = TestUtils.createCommonNote(blogWithOneMember, user1.getId(),
                createInlineDirectMessage(new String[] { user2.getAlias(), user3.getAlias() }));
        try {
            noteManagement.createNote(noteStoringTO, null);
            Assert.fail("DM to users without permission should fail");
        } catch (DirectMessageMissingRecipientException e) {
            // expected exception
            Assert.assertNull(e.getUnresolvableUsers(),
                    "Found users " + StringUtils.join(e.getUnresolvableUsers(), ','));
            Assert.assertNotNull(e.getUninformableUsers());
            Assert.assertEquals(e.getUninformableUsers().length, 2);
            Assert.assertTrue(ArrayUtils.contains(e.getUninformableUsers(), user2.getAlias()));
            Assert.assertTrue(ArrayUtils.contains(e.getUninformableUsers(), user3.getAlias()));
        }
    }

    /**
     * Checks, that it is not possible to write a DM to a user that does not exist.
     *
     * @throws Exception
     *             in case the test failed
     */
    @Test
    public void testFailureOnUnknownRecipient() throws Exception {
        AuthenticationTestUtils.setSecurityContext(user1);
        String alias = TestUtils.createRandomUserAlias();
        NoteStoringTO noteStoringTO = TestUtils.createCommonNote(blog1, user1.getId(),
                createInlineDirectMessage(alias));
        try {
            noteManagement.createNote(noteStoringTO, null);
            Assert.fail("DM to not existing user should fail");
        } catch (DirectMessageMissingRecipientException e) {
            // expected exception
            Assert.assertNull(e.getUninformableUsers());
            Assert.assertNotNull(e.getUnresolvableUsers());
            Assert.assertEquals(e.getUnresolvableUsers().length, 1);
            Assert.assertEquals(e.getUnresolvableUsers()[0], alias);
        }
    }

    /**
     * Tests the creation of a direct message is possible when another user is notified somewhere
     * else in the text. Also asserts that the ignored user is contained in the creation result.
     *
     * @throws Exception
     *             in case the test failed
     */
    @Test
    public void testWarnOnIgnoredNotification() throws Exception {
        AuthenticationTestUtils.setSecurityContext(user1);
        NoteStoringTO noteStoringTO = TestUtils.createCommonNote(blog1, user1.getId(),
                createInlineDirectMessage(user2) + " @" + user3.getAlias());
        NoteModificationResult result = noteManagement.createNote(noteStoringTO, null);
        Assert.assertEquals(result.getStatus(), NoteModificationStatus.SUCCESS);
        Assert.assertEquals(result.getUserNotificationResult().getUninformableUsers().size(), 1);
        Assert.assertEquals(result.getUserNotificationResult().getUninformableUsers().iterator()
                .next(), user3.getAlias());
        NoteData note = noteManagement.getNote(result.getNoteId(), new NoteRenderContext(null,
                Locale.ENGLISH));
        Assert.assertEquals(note.isDirect(), result.isDirect());
        Assert.assertEquals(note.getNotifiedUsers().size(), 1);
        Assert.assertEquals(note.getNotifiedUsers().iterator().next().getId(), user2.getId());
    }

}
