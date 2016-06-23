package com.communote.plugins.mq.provider.jms;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;

import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.easymock.EasyMock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.communote.plugins.mq.service.exception.MessageQueueCommunicationException;
import com.communote.plugins.mq.service.provider.TransferMessage;
import com.communote.plugins.mq.service.provider.TransferMessage.TMContentType;

/**
 * The Class TestJMSMessageSender.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class JMSMessageSenderTest {
    /** The producer mock. */
    MessageProducer producerMock;

    /** The session mock. */
    private Session sessionMock;

    /** The message mock. */
    private TextMessage messageMock;

    /**
     * Inits the mocks.
     */
    @BeforeMethod
    public void initMocks() {
        producerMock = EasyMock.createMock(MessageProducer.class);
        sessionMock = EasyMock.createMock(Session.class);
        messageMock = EasyMock.createMock(TextMessage.class);
    }

    /**
     * Test send reply message.
     * 
     * @throws JMSException
     *             the jMS exception
     * @throws MessageQueueCommunicationException
     */
    @Test
    public void testSendReplyMessage() throws JMSException, MessageQueueCommunicationException {
        String testContent = "test_content";
        String testCorrelationId = "test_correlation_id";
        TransferMessage tm = new TransferMessage();
        tm.setContent(testContent);
        tm.putHeader(TransferMessage.HEADER_CORRELATION_ID, testCorrelationId);
        tm.putHeader(TransferMessage.HEADER_MESSAGE_TYPE, "TestMessage");
        tm.putHeader(TransferMessage.HEADER_MESSAGE_VERSION, "1.0.0");
        tm.putHeader(TransferMessage.HEADER_CONTENT_TYPE, TMContentType.JSON.toString());
        EasyMock.expect(
                sessionMock.createTextMessage(testContent))
                .andReturn(messageMock);
        Queue queueMock = createMock(Queue.class);
        expect(sessionMock.createQueue(testCorrelationId)).andReturn(queueMock);
        messageMock.setJMSCorrelationID(testCorrelationId);
        messageMock.setStringProperty(TransferMessage.HEADER_MESSAGE_TYPE, "TestMessage");
        messageMock.setStringProperty(TransferMessage.HEADER_MESSAGE_VERSION, "1.0.0");
        messageMock.setStringProperty(TransferMessage.HEADER_CONTENT_TYPE,
                TMContentType.JSON.toString());
        producerMock.send(messageMock);

        EasyMock.replay(producerMock, sessionMock, messageMock);

        JMSMessageSender sender = new MockJMSMessageSender(producerMock);
        sender.setSession(sessionMock);
        sender.sendReplyMessage(tm);

        EasyMock.verify(producerMock);
    }

}
