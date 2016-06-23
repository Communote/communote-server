package com.communote.server.service.exceptions;

/**
 * Exception to be thrown when serialization or deserialization of the item data from or to JSON
 * failed.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class NavigationItemDataSerializationException extends Exception {

    /**
     * default serial version UID
     */
    private static final long serialVersionUID = 1L;

    /**
     * Create a new exception with a detail message
     * 
     * @param message
     *            the details
     */
    public NavigationItemDataSerializationException(String message) {
        super(message);
    }

    /**
     * Create a new exception with a detail message and a cause
     * 
     * @param message
     *            the details
     * @param cause
     *            the cause of the exception
     */
    public NavigationItemDataSerializationException(String message, Throwable cause) {
        super(message, cause);
    }
}
