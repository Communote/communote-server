package com.communote.server.api.core.common;

/**
 * Exception to be thrown if something could not be found. If available a suitable specialized
 * subclass of this exception can be used.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class NotFoundException extends Exception {

    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = 591509520608576731L;

    /**
     * Create a NotFoundException with detail message
     *
     */
    public NotFoundException(String message) {
        super(message);
    }

    /**
     * Create a NotFoundException with detail message and cause
     *
     */
    public NotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

}
