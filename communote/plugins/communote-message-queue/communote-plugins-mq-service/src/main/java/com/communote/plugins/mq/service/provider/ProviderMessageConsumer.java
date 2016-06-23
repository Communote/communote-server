package com.communote.plugins.mq.service.provider;

/**
 * 
 * Abstracts JMS specific message consumer from the variety of CNT specific message handlers
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public interface ProviderMessageConsumer {

    /**
     * handles transfer message
     * 
     * @param message
     *            message to be handled
     *            
     * @return reply message if any
     */
    TransferMessage receiveMessage(TransferMessage message);

    /**
     * specifies type selector for the messages, that should be delivered to this consumer
     * 
     * @return message type
     */
    String getConsumedMessageType();

    /**
     * 
     * specifies version selector for the messages, that should be delivered to this consumer
     * 
     * @return version
     */
    String getConsumedMessageVersion();

}
