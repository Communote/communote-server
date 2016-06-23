package com.communote.plugins.mq.provider.jms;

import javax.jms.Destination;
import javax.jms.MessageProducer;

/**
 * Sender for test.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class MockJMSMessageSender extends JMSMessageSender {
    /**
     * 
     */
    private final MessageProducer messageProducer;

    /**
     * @param jmsMessageSenderTest
     */
    public MockJMSMessageSender(MessageProducer MessageProducer) {
        messageProducer = MessageProducer;
    }

    @Override
    protected MessageProducer getProducer(Destination dest) {
        return messageProducer;
    }
}