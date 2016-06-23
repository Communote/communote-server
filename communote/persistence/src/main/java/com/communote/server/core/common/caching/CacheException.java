package com.communote.server.core.common.caching;

/**
 * Exception to be thrown when there are problems with the cache backend.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class CacheException extends RuntimeException {
    private static final long serialVersionUID = 0L;

    /**
     * Creates a new exception described by the provided message.
     * 
     * @param message
     *            the message giving some details about the exception
     */
    public CacheException(String message) {
        super(message);
    }

    /**
     * Creates a new exception described by the provided message and the throwable that caused it.
     * 
     * @param message
     *            the message giving some details about the exception
     * @param cause
     *            the cause of the exception
     */
    public CacheException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a new exception wrapping the cause.
     * 
     * @param cause
     *            the cause of the exception
     */
    public CacheException(Throwable cause) {
        super(cause);
    }
}
