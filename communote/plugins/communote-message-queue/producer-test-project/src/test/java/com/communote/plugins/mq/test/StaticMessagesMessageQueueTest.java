package com.communote.plugins.mq.test;

import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.communote.plugins.mq.message.base.message.CommunoteReplyMessage;
import com.communote.plugins.mq.message.core.message.note.CreateNoteMessage;
import com.communote.plugins.mq.message.core.message.topic.CreateTopicMessage;
import com.communote.plugins.mq.message.core.message.topic.DeleteTopicMessage;
import com.communote.plugins.mq.message.core.message.topic.EditTopicMessage;
import com.communote.plugins.mq.message.core.message.topic.SetTopicRolesMessage;
import com.communote.plugins.mq.message.core.message.topic.TopicReplyMessage;
import com.communote.plugins.mq.message.core.message.topic.UpdateTopicRolesMessage;

/**
 * Test for static messages.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class StaticMessagesMessageQueueTest extends MessageQueueTest {

    private Long topicId;
    private String topicAlias;
    private final String externalObjectId = UUID.randomUUID().toString();

    /**
     * Removes the topic, after the test was done.
     * 
     * @throws Exception
     *             Exception.
     */
    @BeforeClass(dependsOnMethods = "setupMessageQueue")
    public void createTopic() throws Exception {
        CreateTopicMessage message = getMessageQueueTestUtils().createCreateTopicMessage(
                getCommunoteManagerAlias(), false, false);
        TopicReplyMessage reply = sendMessage(message, TopicReplyMessage.class);
        topicId = reply.getTopic().getTopicId();
        topicAlias = reply.getTopic().getTopicAlias();
    }

    /**
     * @return A list of messages in JSON format to send.
     */
    @DataProvider(name = "messages")
    public Object[][] getMessages() {
        return new Object[][] {
                // change rights to allCanRead = allCanWrite = true
                { "/mq-messages/EditTopicMessageUpdateRights.json", EditTopicMessage.class,
                        TopicReplyMessage.class },
                { "/mq-messages/CreateActivityNoteMessage.json", CreateNoteMessage.class,
                        CommunoteReplyMessage.class },
                { "/mq-messages/CreateTopicMessage.json", CreateTopicMessage.class,
                        TopicReplyMessage.class },
                { "/mq-messages/EditTopicMessage.json", EditTopicMessage.class,
                        TopicReplyMessage.class },
                { "/mq-messages/EditTopicMessageAllTrue.json", EditTopicMessage.class,
                        TopicReplyMessage.class },
                { "/mq-messages/EditTopicMessageMergeExternalObjects.json", EditTopicMessage.class,
                        TopicReplyMessage.class },
                { "/mq-messages/SetTopicRolesMessage.json", SetTopicRolesMessage.class,
                        CommunoteReplyMessage.class },
                { "/mq-messages/SetTopicRolesMessageForGroup.json", SetTopicRolesMessage.class,
                        CommunoteReplyMessage.class },
                { "/mq-messages/UpdateTopicRolesMessage.json", UpdateTopicRolesMessage.class,
                        CommunoteReplyMessage.class },
                { "/mq-messages/UpdateTopicRolesMessageForGroup.json",
                        UpdateTopicRolesMessage.class,
                        CommunoteReplyMessage.class },
                { "/mq-messages/CreateNoteMessage.json", CreateNoteMessage.class,
                        CommunoteReplyMessage.class },
                { "/mq-messages/DeleteTopicMessage.json", DeleteTopicMessage.class,
                        CommunoteReplyMessage.class } // Delete topic must be the last.
        };
    }

    /**
     * Sends the messages in JSON format.
     * 
     * @param messagePath
     *            Path to the message.
     * @param clazzOfMessage
     *            Class of the message
     * @param clazzOfReplyMessage
     *            Class of the reply.
     * @throws Exception
     *             Exception.
     */
    @Test(dataProvider = "messages")
    public void test(Object messagePath, Object clazzOfMessage, Object clazzOfReplyMessage)
            throws Exception {
        String message = IOUtils.toString(getClass().getResourceAsStream(messagePath.toString()));
        message = message.replace("@@TOPIC_MANAGER@@", getCommunoteManagerAlias());
        message = message.replace("@@TOPIC_MANAGER_ID@@", getCommunoteManagerId().toString());
        message = message.replace("@@COMMUNOTE_USER@@", getCommunoteUserAlias());
        message = message.replace("@@TOPIC_ALIAS@@", topicAlias);
        message = message.replace("@@TOPIC_ID@@", Long.toString(topicId));
        message = message.replace("@@EXTERNAL_OBJECT_ID@@", externalObjectId);
        message = message.replace("@@CNT_AUTHENTICATION_USERNAME@@",
                getCntAuthenticationUsername(true));
        message = message.replace("@@CNT_AUTHENTICATION_PASSWORD@@",
                getCntAuthenticationPassword(true));
        message = message.replace("@@IDENTITY_TYPE@@",
                "userAlias");
        message = message.replace("@@EXTERNAL_GROUP@@", getExternalGroupId());

        int randoms = StringUtils.countMatches(message, "@@RANDOM@@");
        for (int i = 0; i < randoms; i++) {
            message = StringUtils.replaceOnce(message, "@@RANDOM@@", UUID.randomUUID().toString());
        }
        @SuppressWarnings("unchecked")
        CommunoteReplyMessage reply = sendMessage(message, (Class<?>) clazzOfMessage,
                (Class<CommunoteReplyMessage>) clazzOfReplyMessage);
        Assert.assertEquals(reply.getStatus().getStatusCode(), "OKAY", "Test of '" + messagePath
                + "' failed");
    }
}
