package com.communote.plugins.mq.provider.jms;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TemporaryQueue;
import javax.jms.TextMessage;

import org.easymock.EasyMock;
import org.easymock.IArgumentMatcher;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.communote.plugins.mq.provider.jms.JMSMessageConsumerImpl.MessageDeliveryMode;
import com.communote.plugins.mq.service.exception.MessageQueueCommunicationException;
import com.communote.plugins.mq.service.provider.ProviderMessageConsumer;
import com.communote.plugins.mq.service.provider.TransferMessage;

/**
 * The Class TestJMSMessageConsumer.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class JMSMessageConsumerTest {

    private static final class MockMessageConsumer implements MessageConsumer {
        @Override
        public void close() throws JMSException {
            // TODO Auto-generated method stub

        }

        @Override
        public MessageListener getMessageListener() throws JMSException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public String getMessageSelector() throws JMSException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Message receive() throws JMSException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Message receive(long timeout) throws JMSException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Message receiveNoWait() throws JMSException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public void setMessageListener(MessageListener listener) throws JMSException {
            // TODO Auto-generated method stub

        }
    }

    private class MockNoSendJMSMessageSender extends JMSMessageSender {

        @Override
        public void sendMessageInternal(TransferMessage transferMessage)
                throws MessageQueueCommunicationException {

        }

        @Override
        public void sendReplyMessage(TransferMessage transferMessage)
                throws MessageQueueCommunicationException {

        }

    }

    /**
     * The Class TestRunnable.
     */
    private class TestRunnable implements Runnable {

        /** The was started. */
        private boolean wasStarted = false;

        /**
         * @return the wasStarted
         */
        public boolean isWasStarted() {
            return wasStarted;
        }

        /*
         * (non-Javadoc)
         * 
         * @see Runnable#run()
         */
        @Override
        public void run() {
            wasStarted = true;
        }

    }

    /**
     * The Class TransferMessageContentMatcher.
     */
    private class TransferMessageContentMatcher implements IArgumentMatcher {

        /** The expected content. */
        private final String expectedContent;

        /**
         * Instantiates a new transfer message content matcher.
         * 
         * @param tm
         *            the tm
         */
        public TransferMessageContentMatcher(TransferMessage tm) {
            this.expectedContent = tm.getContent();
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.easymock.IArgumentMatcher#appendTo(StringBuffer)
         */
        @Override
        public void appendTo(StringBuffer buffer) {
            buffer.append("TransferMessage object with the content == \""
                    + expectedContent + "\" is expected");
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.easymock.IArgumentMatcher#matches(Object)
         */
        @Override
        public boolean matches(Object message) {
            boolean res = false;
            if (message instanceof TransferMessage) {
                if (expectedContent.equals(((TransferMessage) message)
                        .getContent())) {
                    res = true;
                }
            }

            return res;
        }
    }

    /** The message mock. */
    private Message messageMock;

    /** The communote message consumer mock. */
    private ProviderMessageConsumer communoteMessageConsumerMock;

    private TemporaryQueue testQueue;

    /** The producer mock. */
    private MessageProducer producerMock;

    /** The session mock. */
    private Session sessionMock;

    /**
     * Eq tm content.
     * 
     * @param tm
     *            the tm
     * @return the transfer message
     */
    private TransferMessage eqTMContent(TransferMessage tm) {
        EasyMock.reportMatcher(new TransferMessageContentMatcher(tm));
        return null;
    }

    /**
     * Inits the mocks.
     */
    @BeforeMethod
    public void initMocks() {
        messageMock = EasyMock.createMock(TextMessage.class);
        communoteMessageConsumerMock = EasyMock
                .createMock(ProviderMessageConsumer.class);
        testQueue = EasyMock.createMock(TemporaryQueue.class);
        producerMock = EasyMock.createMock(MessageProducer.class);
        sessionMock = EasyMock.createMock(Session.class);
    }

    /**
     * Test init in pull mode.
     * 
     * @throws JMSException
     *             the jMS exception
     * @throws InterruptedException
     *             the interrupted exception
     */
    @Test
    public void testInitInPullMode() throws JMSException, InterruptedException {
        TestRunnable tt = new TestRunnable();
        Thread pollingThread = new Thread(tt);

        JMSMessageConsumer consumer = new JMSMessageConsumerImpl();
        consumer.setMessageDeliveryMode(MessageDeliveryMode.PULL);
        consumer.setPollingThread(pollingThread);
        consumer.start();
        pollingThread.join(50);

        Assert.assertTrue(tt.isWasStarted());
    }

    /**
     * Test init in push mode.
     */
    @Test
    public void testInitInPushMode() {

    }

    /**
     * Test jms adapter messages listener_on message.
     * 
     * @throws JMSException
     *             the jMS exception
     */
    @Test
    public void testJMSAdapterMessagesListenerOnMessage() throws JMSException {

        EasyMock.expect(
                messageMock
                        .getStringProperty(TransferMessage.HEADER_CONTENT_TYPE))
                .andReturn("JSON");
        EasyMock.expect(
                messageMock
                        .getBooleanProperty(TransferMessage.HEADER_TRUST_USER))
                .andReturn(false);
        String testCorrelationId = "correlationId";
        EasyMock.expect(messageMock.getJMSCorrelationID()).andReturn(
                testCorrelationId);
        String testContent = "test content";
        EasyMock.expect(((TextMessage) messageMock).getText()).andReturn(
                testContent);
        EasyMock.expect(((TextMessage) messageMock).getJMSReplyTo()).andReturn(
                testQueue);
        EasyMock.expect(((TextMessage) messageMock).getStringProperty("JMSXUserID")).andReturn(
                null);
        TransferMessage tm = new TransferMessage();
        tm.setContent(testContent);
        EasyMock.expect(communoteMessageConsumerMock.getConsumedMessageType())
                .andReturn("TestMessageTYpe");
        TransferMessage answerTM = new TransferMessage();

        EasyMock.expect(
                communoteMessageConsumerMock.receiveMessage(eqTMContent(tm)))
                .andReturn(answerTM);

        EasyMock.replay(messageMock, communoteMessageConsumerMock);

        JMSMessageConsumerImpl consumer = new JMSMessageConsumerImpl();
        consumer.setCommunoteMessageConsumer(communoteMessageConsumerMock);
        consumer.setJmsMessageConsumer(new MockMessageConsumer());
        consumer.setJmsSender(new MockNoSendJMSMessageSender());

        JMSAdapterMessagesListener adapter = new JMSAdapterMessagesListener(consumer);
        adapter.onMessage(messageMock);
        EasyMock.verify(messageMock, communoteMessageConsumerMock);
    }
}
