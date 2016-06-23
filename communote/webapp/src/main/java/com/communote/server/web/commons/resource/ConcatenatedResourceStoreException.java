package com.communote.server.web.commons.resource;

/**
 * Exception thrown by the {@link ConcatenatedResourceStore} when a category couldn't be added,
 * updated or removed.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ConcatenatedResourceStoreException extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * Create a new exception with detail message.
     * 
     * @param message
     *            the details describing the exception
     */
    public ConcatenatedResourceStoreException(String message) {
        super(message);
    }

    /**
     * Create a new exception with detail message.
     * 
     * @param message
     *            the details describing the exception
     * @param cause
     *            the cause of the exception
     */
    public ConcatenatedResourceStoreException(String message, Throwable cause) {
        super(message, cause);
    }
}
