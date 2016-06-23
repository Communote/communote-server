package com.communote.server.core.note.processor;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.communote.server.api.core.note.NoteStoringTO;
import com.communote.server.api.core.note.processor.NoteStoringPreProcessorException;
import com.communote.server.core.blog.NoteManagement;
import com.communote.server.core.blog.notes.processors.ExtractUsersNotePreProcessor;
import com.communote.server.model.user.User;
import com.communote.server.test.CommunoteIntegrationTest;
import com.communote.server.test.util.TestUtils;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ExtractUsersNotePreProcessorTest extends CommunoteIntegrationTest {

    /**
     * Test that the @@-mentions are recognized
     *
     * @throws NoteStoringPreProcessorException
     *             in case the test failed
     */
    @Test
    public void testAtAtMentionPreProcessor() throws NoteStoringPreProcessorException {
        User topicManager = TestUtils.createRandomUser(false);
        User topicReader = TestUtils.createRandomUser(false);
        Long topicId = TestUtils.createRandomBlog(false, false, topicManager, topicReader).getId();

        ExtractUsersNotePreProcessor processor = new ExtractUsersNotePreProcessor();

        NoteStoringTO noteStoringTO = new NoteStoringTO();
        noteStoringTO.setBlogId(topicId);
        noteStoringTO.setContent(NoteManagement.CONSTANT_MENTION_TOPIC_AUTHORS);
        noteStoringTO = processor.process(noteStoringTO);
        Assert.assertTrue(noteStoringTO.isMentionTopicAuthors());

        noteStoringTO.setContent(NoteManagement.CONSTANT_MENTION_TOPIC_MANAGERS);
        noteStoringTO = processor.process(noteStoringTO);
        Assert.assertTrue(noteStoringTO.isMentionTopicManagers());

        noteStoringTO.setContent(NoteManagement.CONSTANT_MENTION_TOPIC_READERS);
        noteStoringTO = processor.process(noteStoringTO);
        Assert.assertTrue(noteStoringTO.isMentionTopicReaders());

        noteStoringTO.setContent(NoteManagement.CONSTANT_MENTION_DISCUSSION_PARTICIPANTS);
        noteStoringTO = processor.process(noteStoringTO);
        Assert.assertTrue(noteStoringTO.isMentionDiscussionAuthors());

        noteStoringTO = new NoteStoringTO();
        noteStoringTO.setBlogId(topicId);
        noteStoringTO.setIsDirectMessage(true);
        noteStoringTO.setContent(NoteManagement.CONSTANT_MENTION_TOPIC_MANAGERS
                + NoteManagement.CONSTANT_MENTION_TOPIC_AUTHORS
                + NoteManagement.CONSTANT_MENTION_TOPIC_READERS
                + NoteManagement.CONSTANT_MENTION_DISCUSSION_PARTICIPANTS);
        noteStoringTO = processor.process(noteStoringTO);
        Assert.assertFalse(noteStoringTO.isMentionDiscussionAuthors());
        Assert.assertFalse(noteStoringTO.isMentionTopicAuthors());
        Assert.assertFalse(noteStoringTO.isMentionTopicReaders());
        Assert.assertTrue(noteStoringTO.isMentionTopicManagers());
        Assert.assertEquals(noteStoringTO.getUsersToNotify().iterator().next(),
                topicManager.getAlias());

    }
}
