package com.communote.server.core.client;

/**
 * Exception thrown by the {@link ClientInitializer} to indicate that the initialization failed.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ClientInitializationException extends Exception {
    private static final long serialVersionUID = 1L;

    /**
     * Create a new exception with detail message
     * 
     * @param message
     *            detail message
     */
    public ClientInitializationException(String message) {
        super(message);
    }

    /**
     * Create a new exception with detail message and cause
     * 
     * @param message
     *            detail message
     * @param cause
     *            the cause
     */
    public ClientInitializationException(String message, Throwable cause) {
        super(message, cause);
    }
}
