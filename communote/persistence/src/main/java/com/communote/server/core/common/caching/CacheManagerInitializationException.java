package com.communote.server.core.common.caching;

/**
 * Exception to be thrown when the initialization of a {@link CacheManager} or the managed cache
 * failed.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class CacheManagerInitializationException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    /**
     * Creates a new exception described by the provided message.
     * 
     * @param message
     *            the message giving some details about the exception
     */
    public CacheManagerInitializationException(String message) {
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
    public CacheManagerInitializationException(String message, Throwable cause) {
        super(message, cause);
    }
}
