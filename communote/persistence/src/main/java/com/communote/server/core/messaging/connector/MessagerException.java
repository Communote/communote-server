package com.communote.server.core.messaging.connector;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class MessagerException extends RuntimeException {
    /**
     * Constructs a new instance of MessagerException
     *
     * @param message
     *            the throwable message.
     */
    public MessagerException(String message) {
        super(message);
    }

    /**
     * Constructs a new instance of MessagerException
     *
     * @param message
     *            the throwable message.
     * @param throwable
     *            the parent of this Throwable.
     */
    public MessagerException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
