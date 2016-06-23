package com.communote.plugins.mq.service.provider;

import com.communote.plugins.mq.service.exception.MessageQueueCommunicationException;

/**
 * 
 * Abstracts CNT message sender from the concrete MQ implementation
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public interface ProviderMessageSender {

    /**
     * Sends a message to the message queue internally
     * 
     * @param transferMessage
     *            the transfer message to send
     * @throws MessageQueueCommunicationException
     * @throws JMSException
     *             in case of an JMS error
     */
    void sendMessageInternal(TransferMessage transferMessage)
            throws MessageQueueCommunicationException;

    /**
     * sends reply to the MQ implementation
     * 
     * @param transferMessage
     *            message
     */
    void sendReplyMessage(TransferMessage transferMessage)
            throws MessageQueueCommunicationException;

}
