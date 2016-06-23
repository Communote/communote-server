package com.communote.plugins.mq.test;

import java.util.Date;
import java.util.Random;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.communote.plugins.mq.message.base.data.security.UserIdentity;
import com.communote.plugins.mq.message.base.data.security.UserIdentityContext;
import com.communote.plugins.mq.message.base.message.CommunoteReplyMessage;
import com.communote.plugins.mq.message.base.message.ReplyType;
import com.communote.plugins.mq.message.core.data.note.Note;
import com.communote.plugins.mq.message.core.data.topic.BaseTopic;
import com.communote.plugins.mq.message.core.data.topic.Topic;
import com.communote.plugins.mq.message.core.message.note.CreateNoteMessage;
import com.communote.plugins.mq.message.core.message.topic.CreateTopicMessage;
import com.communote.plugins.mq.message.core.message.topic.DeleteTopicMessage;
import com.communote.plugins.mq.message.core.message.topic.EditTopicMessage;
import com.communote.plugins.mq.message.core.message.topic.SetTopicRolesMessage;
import com.communote.plugins.mq.message.core.message.topic.TopicReplyMessage;
import com.communote.plugins.mq.message.core.message.topic.UpdateTopicRolesMessage;
import com.communote.server.model.blog.BlogRole;


/**
 * Simple test case for producing content for the message queue.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ProducerMessageQueueTest extends MessageQueueTest {

    private Topic topic;

    /**
     * First create a topic and then an note in this topic, uses the ability to change the creation date
     * @throws Exception
     *             in case of an error
     */
    @Test
    public void testCreateNote() throws Exception {
        CreateTopicMessage message = getMessageQueueTestUtils().createCreateTopicMessage(
                getCommunoteManagerAlias(), false, true);

        TopicReplyMessage reply = sendMessage(message, TopicReplyMessage.class);

        // it should be a validation error
        Assert.assertEquals(reply.getStatus().getStatusCode(), "OKAY",
                "error creating topic for note creation");
        Assert.assertNotNull(reply.getTopic(), "topic must not be null");
        Assert.assertNotNull(reply.getTopic().getTopicId(), "topic.id must not be null");

        CreateNoteMessage noteMessage = new CreateNoteMessage();
        noteMessage.setAuthentication(message.getAuthentication());

        Note note = new Note();
        note.setCreationDate(new Date(new Date().getTime() - 100000));
        note.setContent("Posting back in time content " + note.getCreationDate());
        note.setContentType(Note.CONTENT_TYPE_PLAIN_TEXT);

        note.setTopics(new BaseTopic[] { new BaseTopic() });
        note.getTopics()[0].setTopicId(reply.getTopic().getTopicId());

        noteMessage.setNote(note);

        CommunoteReplyMessage replyMessage = sendMessage(noteMessage, CommunoteReplyMessage.class);

        Assert.assertEquals(replyMessage.getStatus().getStatusCode(), "OKAY",
                "error in creating note");
    }

    /**
     * Test a failing authentication. Why first create a topic? It showed up that missing
     * authentication leads to different behavior, CreateTopic gives a NOT_FOUND and in CreateNote a
     * AccessDeniedException was not handled correctly. This is why this test ist for. For missing
     * authentication in create note.
     * 
     * @throws Exception
     *             in case of an error
     */
    @Test
    public void testCreateNoteWithMissingAuthentication() throws Exception {
        CreateTopicMessage message = getMessageQueueTestUtils().createCreateTopicMessage(
                getCommunoteManagerAlias(), false, true);

        TopicReplyMessage reply = sendMessage(message, TopicReplyMessage.class);

        // it should be a validation error
        Assert.assertEquals(reply.getStatus().getStatusCode(), "OKAY",
                "error creating topic for note creation");
        Assert.assertNotNull(reply.getTopic(), "topic must not be null");
        Assert.assertNotNull(reply.getTopic().getTopicId(), "topic.id must not be null");

        CreateNoteMessage noteMessage = new CreateNoteMessage();
        // noteMessage.setAuthentication(message.getAuthentication());

        Note note = new Note();
        note.setCreationDate(new Date(new Date().getTime() - 100000));
        note.setContent("Posting back in time content " + note.getCreationDate());
        note.setContentType(Note.CONTENT_TYPE_PLAIN_TEXT);

        note.setTopics(new BaseTopic[] { new BaseTopic() });
        note.getTopics()[0].setTopicId(reply.getTopic().getTopicId());

        noteMessage.setNote(note);

        CommunoteReplyMessage replyMessage = sendMessage(noteMessage, CommunoteReplyMessage.class);

        Assert.assertEquals(replyMessage.getStatus().getStatusCode(), "AUTHORIZATION_ERROR",
                "error in creating note");
    }

    /**
     * Method to the test the topic creation.
     * 
     * @throws Exception
     *             Exception.
     */
    @Test
    public void testCreateTopic() throws Exception {
        CreateTopicMessage message = getMessageQueueTestUtils().createCreateTopicMessage(
                getCommunoteManagerAlias(),
                false, true);
        TopicReplyMessage reply = sendMessage(message, TopicReplyMessage.class);
        Assert.assertEquals(reply.getStatus().getStatusCode(), "OKAY");
        Assert.assertNotNull(reply.getTopic().getTopicId());
        Assert.assertTrue(reply.getTopic().getTopicRights().isAllCanWrite());
        Assert.assertFalse(reply.getTopic().getTopicRights().isAllCanRead());
        topic = reply.getTopic();

        // Create again -> Should fail now, as the topic already exists.
        reply = sendMessage(message, TopicReplyMessage.class);
        Assert.assertEquals(reply.getStatus().getStatusCode(), "BAD_REQUEST");
        Assert.assertNull(reply.getTopic());

    }

    /**
     * Method to test the deletion of a topic.
     * 
     * @throws Exception
     *             Exception.
     */
    @Test(dependsOnMethods = "testUpdateTopicRoles")
    public void testDeleteTopic() throws Exception {
        DeleteTopicMessage message = getMessageQueueTestUtils().createDeleteTopicMessage(
                getCommunoteManagerAlias(),
                topic.getTopicId());
        CommunoteReplyMessage reply = sendMessage(message, CommunoteReplyMessage.class);
        Assert.assertEquals(reply.getStatus().getStatusCode(), "OKAY");

        // Delete again -> Should fail now, as the topic is already deleted.
        reply = sendMessage(message, CommunoteReplyMessage.class);
        Assert.assertEquals(reply.getStatus().getStatusCode(), "NOT_FOUND");
    }

    /**
     * Method to test, that editing a topic works.
     * 
     * @throws Exception
     *             Exception.
     */
    @Test(dependsOnMethods = "testCreateTopic")
    public void testEditTopic() throws Exception {
        EditTopicMessage message = getMessageQueueTestUtils().createEditTopicMessage(
                getCommunoteManagerAlias(),
                topic.getTopicId(), "FalscherHase", true, false);

        TopicReplyMessage reply = sendMessage(message, TopicReplyMessage.class);
        Assert.assertEquals(reply.getStatus().getStatusCode(), "OKAY");

        Assert.assertEquals(topic.getExternalObjects().length, reply.getTopic()
                .getExternalObjects().length);

        Assert.assertTrue(reply.getTopic().getTopicRights().isAllCanRead());
        Assert.assertFalse(reply.getTopic().getTopicRights().isAllCanWrite());
        Assert.assertEquals(topic.getTopicId(), reply.getTopic().getTopicId());
        Assert.assertEquals(topic.getTopicAlias(), reply.getTopic().getTopicAlias());
        Assert.assertTrue(reply.getTopic().getTags().length > 0);
        Assert.assertEquals(message.getTopic().getTags().length, reply.getTopic().getTags().length);

        EditTopicMessage message2 = getMessageQueueTestUtils().createEditTopicMessage(
                getCommunoteManagerAlias(), -1, topic.getTopicAlias(), true, false);
        TopicReplyMessage reply2 = sendMessage(message2, TopicReplyMessage.class);
        Assert.assertEquals(reply2.getStatus().getStatusCode(), "OKAY");
    }

    /**
     * Test invalid json
     * 
     * @throws Exception
     *             in case of an error
     */
    @Test
    public void testInvalidJsonObjectnames() throws Exception {
        CreateTopicMessage message = getMessageQueueTestUtils().createCreateTopicMessage(
                getCommunoteManagerAlias(), false, true);

        message.setReplyType(ReplyType.FULL);
        String json = getMapper().writeValueAsString(message);

        json = json.replace("topicAlias", "sailAcipot");

        TopicReplyMessage reply = sendMessage(json,
                message.getClass(), TopicReplyMessage.class);

        // it should be a validation error
        Assert.assertEquals(reply.getStatus().getStatusCode(), "BAD_REQUEST");
    }

    /**
     * Test invalid json
     * 
     * @throws Exception
     *             in case of an error
     */
    @Test
    public void testInvalidJsonSyntax() throws Exception {
        CreateTopicMessage message = getMessageQueueTestUtils().createCreateTopicMessage(
                getCommunoteManagerAlias(), false, true);

        message.setReplyType(ReplyType.FULL);
        String json = getMapper().writeValueAsString(message);
        json = json.replaceFirst(",", "<");
        json = json.substring(0, json.length() - 10);

        TopicReplyMessage reply = sendMessage(json,
                message.getClass(), TopicReplyMessage.class);

        // it should be a validation error
        Assert.assertEquals(reply.getStatus().getStatusCode(), "BAD_REQUEST");
    }

    /**
     * Method to test sending of messages.
     * 
     * @throws Exception
     *             Exception.
     */
    @Test(dependsOnMethods = "testCreateTopic")
    public void testSendMessages() throws Exception {
        for (int i = 0; i < new Random().nextInt(10) + 10; i++) {
            CreateNoteMessage message =
                    getMessageQueueTestUtils().createRandomNoteMessage(getCommunoteManagerAlias(),
                            topic.getTopicId());
            CommunoteReplyMessage reply = sendMessage(message,
                    CommunoteReplyMessage.class);
            Assert.assertEquals(reply.getStatus().getStatusCode(),
                    "OKAY");
        }

    }

    /**
     * Method to test, that it is possible to set a topic role.
     * 
     * @throws Exception
     *             Exception.
     */
    @Test(dependsOnMethods = "testCreateTopic")
    public void testSetTopicRoles() throws Exception {
        SetTopicRolesMessage message = getMessageQueueTestUtils().createSetTopicRolesMessage(
                getCommunoteManagerAlias(), getCommunoteUserAlias(), BlogRole.MANAGER,
                topic.getTopicId());
        CommunoteReplyMessage reply = sendMessage(message, CommunoteReplyMessage.class);
        Assert.assertEquals(reply.getStatus().getStatusCode(), "OKAY");
    }

    /**
     * Method to test the update of a topic role.
     * 
     * @throws Exception
     *             Exception.
     */
    @Test(dependsOnMethods = "testSetTopicRoles")
    public void testUpdateTopicRoles() throws Exception {
        UpdateTopicRolesMessage message = getMessageQueueTestUtils().createUpdateTopicRolesMessage(
                getCommunoteManagerAlias(), getCommunoteUserAlias(), BlogRole.VIEWER,
                topic.getTopicId(), topic.getTopicAlias());
        CommunoteReplyMessage reply = sendMessage(message, CommunoteReplyMessage.class);
        Assert.assertEquals(reply.getStatus().getStatusCode(), "OKAY");

        UpdateTopicRolesMessage message2 = getMessageQueueTestUtils()
                .createUpdateTopicRolesMessage(getCommunoteManagerAlias(), getCommunoteUserAlias(),
                        BlogRole.VIEWER, topic.getTopicAlias());
        CommunoteReplyMessage reply2 =
                sendMessage(message2, CommunoteReplyMessage.class);
        Assert.assertEquals(reply2.getStatus().getStatusCode(), "OKAY");

    }

    /**
     * Tests an invalid identity context number
     * 
     * @throws Exception
     *             in case something went wrong
     */
    @Test
    public void testValidationError() throws Exception {
        CreateTopicMessage message = getMessageQueueTestUtils().createCreateTopicMessage(
                getCommunoteManagerAlias(), false, true);
        UserIdentity identity = new UserIdentity();
        identity.setIdentity("ishouldbeanumber");
        identity.setIdentityType("userId");
        ((UserIdentityContext) message.getIdentityContext()).setIdentity(identity);

        TopicReplyMessage reply = sendMessage(message, TopicReplyMessage.class);

        // it should be a validation error
        Assert.assertEquals(reply.getStatus().getStatusCode(), "VALIDATION_ERROR");
    }
}
