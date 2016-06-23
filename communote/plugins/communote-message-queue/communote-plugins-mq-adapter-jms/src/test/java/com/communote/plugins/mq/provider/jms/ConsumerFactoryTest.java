package com.communote.plugins.mq.provider.jms;

import java.util.Map;

import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Queue;
import javax.jms.Session;

import org.easymock.EasyMock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.communote.plugins.mq.service.provider.ProviderMessageConsumer;

/**
 * A factory for creating TestConsumer objects.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ConsumerFactoryTest {

    /** The consumer mock. */
    private ProviderMessageConsumer consumerMock;

    /** The dest mock. */
    private Queue destMock;

    /** The session mock. */
    private Session sessionMock;

    /** The jms consumer mock. */
    private MessageConsumer jmsConsumerMock;

    /** The message consumers mock. */
    private Map<ProviderMessageConsumer, JMSMessageConsumer> messageConsumersMock;

    /** The cnt jms consumer mock. */
    private JMSMessageConsumer cntJMSConsumerMock;

    private JMSAdapter providerMock;

    /**
     * Inits the mocks.
     */
    @SuppressWarnings("unchecked")
    @BeforeMethod
    public void initMocks() {
        consumerMock = EasyMock.createMock(ProviderMessageConsumer.class);
        providerMock = EasyMock.createMock(JMSAdapter.class);
        destMock = EasyMock.createMock(Queue.class);
        sessionMock = EasyMock.createMock(Session.class);
        jmsConsumerMock = EasyMock.createMock(MessageConsumer.class);
        messageConsumersMock = EasyMock.createMock(Map.class);
        cntJMSConsumerMock = EasyMock.createMock(JMSMessageConsumer.class);
    }

    /**
     * Test bind message consumer.
     * 
     * @throws JMSException
     *             the jMS exception
     */
    @Test
    public void testBindMessageConsumer() throws JMSException {
        String testConsumedMT = "consumed_mt";
        String testConsumedMV = "consumed_mv";
        String testMessageSelector = "MESSAGE_TYPE" + " = '" + testConsumedMT
                + "' AND MESSAGE_VERSION = '" + testConsumedMV + "'";
        EasyMock.expect(consumerMock.getConsumedMessageType()).andReturn(testConsumedMT);
        EasyMock.expect(consumerMock.getConsumedMessageVersion()).andReturn(testConsumedMV);
        EasyMock.expect(sessionMock.createConsumer(destMock, testMessageSelector))
                .andReturn(jmsConsumerMock);

        EasyMock.expect(providerMock.getSession()).andReturn(sessionMock);
        EasyMock.expect(providerMock.getDestination()).andReturn(destMock);

        jmsConsumerMock.setMessageListener(EasyMock
                .isA(JMSAdapterMessagesListener.class));

        EasyMock.replay(consumerMock, destMock, sessionMock, jmsConsumerMock, providerMock);
        ConsumerFactory consumerFactory = new ConsumerFactory();
        consumerFactory.setProvider(providerMock);
        consumerFactory.bindMessageConsumer(consumerMock);
        EasyMock.verify(consumerMock, destMock, sessionMock, jmsConsumerMock, providerMock);
    }

    /**
     * Test unbind message consumer.
     * 
     * @throws JMSException
     *             the jMS exception
     */
    @Test
    public void testUnbindMessageConsumer() throws JMSException {
        cntJMSConsumerMock.stop();
        EasyMock.expectLastCall();
        EasyMock.expect(messageConsumersMock.remove(consumerMock)).andReturn(cntJMSConsumerMock);
        EasyMock.replay(messageConsumersMock, cntJMSConsumerMock, jmsConsumerMock);

        ConsumerFactory consumerFactory = new ConsumerFactory();
        consumerFactory.setProvider(providerMock);
        consumerFactory.setJmsMessageConsumers(messageConsumersMock);
        consumerFactory.unbindMessageConsumer(consumerMock);

        EasyMock.verify(jmsConsumerMock);
    }

}
