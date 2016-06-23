package com.communote.server.core.common.exceptions;

/**
 * Exception to be thrown when an operation cannot be run in the current context.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class InvalidOperationException extends Exception {

    /**
     * default serial version UID
     */
    private static final long serialVersionUID = 1L;

    /**
     * Create a new exception with a detail message
     * 
     * @param message
     *            the detail message
     */
    public InvalidOperationException(String message) {
        super(message);
    }

    /**
     * Create a new exception with a detail message and a cause
     * 
     * @param message
     *            the detail message
     * @param cause
     *            the cause
     */
    public InvalidOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
