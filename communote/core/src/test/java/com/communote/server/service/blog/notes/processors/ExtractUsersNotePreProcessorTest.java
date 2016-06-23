package com.communote.server.service.blog.notes.processors;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.math.RandomUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.communote.server.api.core.note.NoteStoringTO;
import com.communote.server.api.core.note.processor.NoteStoringPreProcessorException;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.core.blog.NoteManagement;
import com.communote.server.core.blog.notes.processors.ExtractUsersNotePreProcessor;
import com.communote.server.core.blog.notes.processors.exceptions.DirectMessageMissingRecipientException;

/**
 * Tests for {@link ExtractUsersNotePreProcessor}.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class ExtractUsersNotePreProcessorTest {
    private final ExtractUsersNotePreProcessor processor = new ExtractUsersNotePreProcessor();

    /**
     * helper to assert that the correct users are extracted
     *
     * @param expectedAliases
     *            space separated string of user aliases that should have been extracted. Should be
     *            empty if no users should have been extracted.
     * @param foundAliases
     *            set of found aliases
     * @param failureMessage
     *            a message to print when the assert fails, for better readability
     */
    private void assertUsersExtracted(String expectedAliases, Set<String> foundAliases,
            String failureMessage) {
        String[] expectedNotifications = expectedAliases.length() == 0 ? new String[0]
                : expectedAliases.split(" ");
        Assert.assertEquals(foundAliases.size(), expectedNotifications.length, failureMessage);
        for (String userAlias : expectedNotifications) {
            Assert.assertTrue(foundAliases.contains(userAlias), failureMessage);
        }
    }

    /**
     * Tests for not extracting the direct receiver.
     *
     * @throws NoteStoringPreProcessorException
     *             Exception.
     * @throws AuthorizationException
     *             AuthorizationExcpetion
     */
    @Test
    public void dontExtractDirectMessageReceiver() throws NoteStoringPreProcessorException,
            AuthorizationException {
        String user = RandomStringUtils.randomAlphanumeric(1);
        NoteStoringTO note = new NoteStoringTO();
        note.setContent("test" + RandomStringUtils.randomAlphanumeric(1000) + " d @" + user + " "
                + RandomStringUtils.randomAlphanumeric(1000));
        processor.process(note);
        Assert.assertFalse(note.isIsDirectMessage());
        Assert.assertEquals(note.getUsersToNotify().size(), 1);
        Assert.assertEquals(note.getUsersNotToNotify().size(), 0);
    }

    /**
     *
     * Tests for correct extracting of users to be notified.
     *
     * @throws NoteStoringPreProcessorException
     *             Exception.
     */
    @Test
    public void extractUsersToNotify() throws NoteStoringPreProcessorException {
        String[][] notes = new String[][] { { "This is a note @you.", " you " },
                { "This is a note @you", " you " },
                { "@12 @This @is @a @note @you.", " you 12 This is a note " }, { "@you", " you " }, };
        for (String[] note : notes) {
            NoteStoringTO noteStoringTO = new NoteStoringTO();
            noteStoringTO.setContent(note[0]);
            processor.process(noteStoringTO);
            Assert.assertFalse(noteStoringTO.isIsDirectMessage());
            for (String user : noteStoringTO.getUsersToNotify()) {
                Assert.assertTrue(note[1].contains(user));
            }
        }
    }

    /**
     *
     * @param includeUsersToBeNotified
     *            True if users to be notified should be contained.
     * @param usersToBeNotified
     *            List of users to be notified.
     * @return A random message.
     *
     *
     */
    private String generateMessage(boolean includeUsersToBeNotified, Set<String> usersToBeNotified) {
        if (includeUsersToBeNotified) {
            String message = "";
            for (int i = 0; i < RandomUtils.nextInt(20) + 10; i++) {
                String userToBeNotified = RandomStringUtils.randomAlphanumeric(10)
                        .replace(" ", "_") + i;
                usersToBeNotified.add(userToBeNotified);
                message += " " + RandomStringUtils.randomAlphanumeric(RandomUtils.nextInt(10))
                        + " @" + userToBeNotified + " ";
            }
            return message;
        }
        return RandomStringUtils.randomAlphanumeric(RandomUtils.nextInt(1000) + 10);
    }

    /**
     * @return A String of random length (<=100) out of whitespaces.
     */
    private String generateWhiteSpaces() {
        String whitespaces = "";
        for (int i = 0; i < RandomUtils.nextInt(100); i++) {
            whitespaces += " ";
        }
        return whitespaces;
    }

    /**
     * Tests that two times the same user is correct for sending a DM. This could occur when the
     * user is defined via syntax and direct selection in the users tab
     *
     * @throws NoteStoringPreProcessorException
     *             Exception.
     * @throws AuthorizationException
     *             Exception
     */
    @Test
    public void testDirectMessageToTwoTimesTheSameUsers() throws NoteStoringPreProcessorException,
            AuthorizationException {
        String user1 = RandomStringUtils.randomAlphanumeric(10).replace(" ", "_") + "1";
        String whitespaces = generateWhiteSpaces();
        String message = generateMessage(false, new HashSet<String>());
        NoteStoringTO note = new NoteStoringTO();
        note.setContent(whitespaces + ExtractUsersNotePreProcessor.DIRECT_MESSAGE_PREFIX + " @"
                + user1 + " " + message);
        note.setUsersToNotify(new HashSet<String>());
        note.getUsersToNotify().add(user1);
        processor.process(note);
        Assert.assertTrue(note.isIsDirectMessage());
        Assert.assertEquals(note.getUsersToNotify().iterator().next(), user1);
    }

    /**
     * This tests for correct recognition of dms and correct extraction of users to be notified or
     * not to be notified.
     *
     * @throws Exception
     *             In case of an error.
     */
    @Test
    public void testForDirectMessageRecognition() throws Exception {
        // Format {message, isDirect, usersToBeNotified, usersNotToBeNotified}
        // Users in the list have to be surrounded by whitespaces.
        String[][] notes = new String[][] {
                {
                        "<p>d<p><p>@det<br>@ama&nbsp;@tle</p><b>@row</b> @ken<b>mei</b></p>normaler text",
                        "true", "ama tle det row", "" },
                { "d @test @ich blubbla @hufi assad @bla", "true", "ich test", "bla hufi" },
                { "d @test @sgds @sdf  blubbla", "true", "sgds sdf test", "" },
                { "D @test @sgds @sdf  blubbla", "true", "sgds sdf test", "" },
                { "d blubbla", "false", "", "" },
                { "D @@managers", "true", "", "" },
                { "D @kenmei @@managers", "true", "kenmei", "" },
                { "D @@managers @kenmei", "true", "kenmei", "" },
                { "<p>d <br />@test blubbla", "true", "test", "" },
                { "<p>d<br /> @test blubbla", "true", "test", "" },
                { "<p>d<br /> @te.st blubbla", "true", "te.st", "" },
                { "<p>d<br /> @te_st blubbla", "true", "te_st", "" },
                { "<p>d<br /> @te-st blubbla", "true", "te-st", "" },
                { "<p>d <br />@test blubbla @me sdffd ...", "true", "test", "me" },
                { "<p>d<p><p>@det<br>@ama&nbsp;@tle</p><b>@row</b> @kenmei</p>normaler text</p>",
                        "true", "ama tle det row kenmei", "" },
                { "<p>D<p><p>@det<br>@ama&nbsp;@tle</p><b>@row</b> @kenmei</p>normaler text</p>",
                        "true", "ama tle det row kenmei", "" },
                {
                                "<div><div><div><div><div><div><p>d @user</p><p>Hello.</p>"
                                        + "</div></div></div></div></div></div>", "true", "user", "" },
                // Test for KENMEI-3238 { "D@user1 @user2 Direktnotiz", "false", "user2", "" },

                {
                        "<div><p style=\"text-autospace:none\">D @user  <p style=\"text-autospace:none\">",
                        "true", "user", "" },
                // Test for KENMEI-4685, note: this test is a negative test: note is recognized as
                // DM but no user can be extracted
                { "d @user, @me, @row content", "true", "", "row" },
                // Test for KENMEI-5140
                { "d @c0f52cead34d4b3cab158add4f911f0d 50bea62b-b3db-43db-ad86-7c76fde74e01",
                        "true", "c0f52cead34d4b3cab158add4f911f0d", "" } };
        for (int i = 0; i < notes.length; i++) {
            // for better readability
            int testCaseNumber = i + 1;
            String[] note = notes[i];
            NoteStoringTO noteStoringTO = new NoteStoringTO();
            noteStoringTO.setContent(note[0]);
            processor.process(noteStoringTO);
            Assert.assertEquals(noteStoringTO.isIsDirectMessage(), Boolean.parseBoolean(note[1]),
                    "Is DM test failed for test case " + testCaseNumber);
            Assert.assertEquals(noteStoringTO.getContent(), note[0]);
            assertUsersExtracted(note[2], noteStoringTO.getUsersToNotify(),
                    "Assert failed for users to be notified in test case " + testCaseNumber);
            assertUsersExtracted(note[3], noteStoringTO.getUsersNotToNotify(),
                    "Assert failed for users not to be notified in test case " + testCaseNumber);
        }
    }

    /**
     * Test for direct messages with @@ notification.
     *
     * @throws NoteStoringPreProcessorException
     *             The test should fail, if thrown.
     */
    @Test
    public void testForDirectMessageWithAtAt() throws NoteStoringPreProcessorException {
        // @@managers
        NoteStoringTO noteStoringTO = new NoteStoringTO();
        noteStoringTO.setContent("d @kenmei blabla "
                + NoteManagement.CONSTANT_MENTION_TOPIC_MANAGERS);
        processor.process(noteStoringTO);
        Assert.assertFalse(noteStoringTO.isMentionTopicManagers());

        noteStoringTO = new NoteStoringTO();
        noteStoringTO.setContent("d @kenmei " + NoteManagement.CONSTANT_MENTION_TOPIC_MANAGERS);
        processor.process(noteStoringTO);
        Assert.assertTrue(noteStoringTO.isMentionTopicManagers());

        // @@discussion
        noteStoringTO = new NoteStoringTO();
        noteStoringTO.setContent("d @kenmei blabla "
                + NoteManagement.CONSTANT_MENTION_DISCUSSION_PARTICIPANTS);
        processor.process(noteStoringTO);
        Assert.assertFalse(noteStoringTO.isMentionDiscussionAuthors());

        noteStoringTO = new NoteStoringTO();
        noteStoringTO.setContent("d @kenmei "
                + NoteManagement.CONSTANT_MENTION_DISCUSSION_PARTICIPANTS);
        try {
            processor.process(noteStoringTO);
            Assert.fail();
        } catch (DirectMessageMissingRecipientException e) {
            // Okay.
        }

        // @@authors
        noteStoringTO = new NoteStoringTO();
        noteStoringTO.setContent("d @kenmei blabla "
                + NoteManagement.CONSTANT_MENTION_TOPIC_AUTHORS);
        processor.process(noteStoringTO);
        Assert.assertFalse(noteStoringTO.isMentionTopicAuthors());

        noteStoringTO = new NoteStoringTO();
        noteStoringTO.setContent("d @kenmei " + NoteManagement.CONSTANT_MENTION_TOPIC_AUTHORS);
        try {
            processor.process(noteStoringTO);
            Assert.fail();
        } catch (DirectMessageMissingRecipientException e) {
            // Okay.
        }

        // @@members
        noteStoringTO = new NoteStoringTO();
        noteStoringTO.setContent("d @kenmei blabla "
                + NoteManagement.CONSTANT_MENTION_TOPIC_READERS);
        processor.process(noteStoringTO);
        Assert.assertFalse(noteStoringTO.isMentionTopicReaders());

        noteStoringTO = new NoteStoringTO();
        noteStoringTO.setContent("d @kenmei " + NoteManagement.CONSTANT_MENTION_TOPIC_READERS);
        try {
            processor.process(noteStoringTO);
            Assert.fail();
        } catch (DirectMessageMissingRecipientException e) {
            // Okay.
        }
    }
}
