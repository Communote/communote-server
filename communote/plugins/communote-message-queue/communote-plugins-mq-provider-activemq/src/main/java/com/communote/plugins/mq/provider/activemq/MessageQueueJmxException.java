package com.communote.plugins.mq.provider.activemq;

/**
 * Exception for an failed access or usage of the jmx mbeans
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */

public class MessageQueueJmxException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /**
     * 
     * @param message
     *            message
     */
    public MessageQueueJmxException(String message) {
        super(message);
    }

    /**
     * 
     * @param message
     *            the message
     * @param th
     *            the exception, throwable
     */
    public MessageQueueJmxException(String message, Throwable th) {
        super(message, th);
    }
}