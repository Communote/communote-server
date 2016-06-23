package com.communote.plugins.mq.service.message;

import com.communote.plugins.mq.message.base.message.BaseMessage;
import com.communote.plugins.mq.message.base.message.CommunoteReplyMessage;
import com.communote.plugins.mq.service.exception.MessageQueueCommunicationException;

/**
 * Abstracts MQ infrastructure from the CNT handlers
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public interface CommunoteMessageSender {

    /**
     * Sends a message to CNT internal
     * 
     * @param message
     *            the message
     * @param messageType
     *            the type of the message
     * @param version
     *            the version of the message
     * @throws Exception
     *             in case of an error
     */
    void sendMessage(BaseMessage message, String messageType, String version)
            throws MessageQueueCommunicationException;

    /**
     * is used to send a reply to the incoming message
     * 
     * @param message
     *            reply message
     * 
     */

    void sendReplyMessage(CommunoteReplyMessage message) throws MessageQueueCommunicationException;

}
