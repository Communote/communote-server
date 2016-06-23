package com.communote.plugins.mq.provider.jms;

import javax.jms.Message;
import javax.jms.MessageListener;

/**
 * JMS Messages listener.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
class JMSAdapterMessagesListener implements MessageListener {

    /**
     * 
     */
    private final JMSMessageConsumerImpl jmsMessageConsumerImpl;

    /**
     * @param jmsMessageConsumerImpl
     */
    JMSAdapterMessagesListener(JMSMessageConsumerImpl jmsMessageConsumerImpl) {
        this.jmsMessageConsumerImpl = jmsMessageConsumerImpl;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.MessageListener#onMessage(javax.jms.Message)
     */
    @Override
    public void onMessage(Message message) {
        this.jmsMessageConsumerImpl.processMessage(message);
    }
}