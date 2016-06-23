package com.communote.plugins.mq.service.exception;

/**
 * Used in case an error occured sending or receiving a message via message queue
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class MessageQueueCommunicationException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public MessageQueueCommunicationException(String message) {
        super(message);
    }

    public MessageQueueCommunicationException(String message, Throwable cause) {
        super(message, cause);
    }

}
