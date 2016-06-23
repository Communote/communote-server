package com.communote.plugins.mq.provider.jms;

import javax.jms.JMSException;
import javax.jms.MessageConsumer;

import com.communote.plugins.mq.provider.jms.JMSMessageConsumerImpl.MessageDeliveryMode;
import com.communote.plugins.mq.service.provider.ProviderMessageConsumer;

/**
 * Interface needed for iPojo/OSGi service declaration.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public interface JMSMessageConsumer {

    /**
     * Sets CNT message consumer.
     * 
     * @param communoteMessageConsumer
     *            consumer to be set
     */
    void setCommunoteMessageConsumer(
            ProviderMessageConsumer communoteMessageConsumer);

    /**
     * Sets JMS message consumer.
     * 
     * @param jmsMessageConsumer
     *            consumer to be set
     */
    void setJmsMessageConsumer(MessageConsumer jmsMessageConsumer);

    /**
     * @param jmsSender
     *            the jmsSender to set
     */
    void setJmsSender(JMSMessageSender jmsSender);

    /**
     * Sets the message delivery mode.
     * 
     * @param messageDeliveryMode
     *            the messageDeliveryMode to set
     */
    void setMessageDeliveryMode(MessageDeliveryMode messageDeliveryMode);

    /**
     * only for test purposes.
     * 
     * @param pollingThread
     *            the new polling thread
     */
    void setPollingThread(Thread pollingThread);

    /**
     * initializes the message consumer.
     * 
     * @throws JMSException
     *             the jMS exception
     */
    void start() throws JMSException;

    /**
     * stops JMS message consumer.
     */
    void stop();
}