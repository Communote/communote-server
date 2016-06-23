package com.communote.server.core.common.caching;

/**
 * Exception to signal a problem in the CacheElementProvider.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class CacheElementProviderException extends Exception {

    /**
     * default serial version UID
     */
    private static final long serialVersionUID = 1L;

    /**
     * Create a new exception with detail message.
     * 
     * @param message
     *            the details
     */
    public CacheElementProviderException(String message) {
        super(message);
    }

    /**
     * Create a new exception with detail message and cause.
     * 
     * @param message
     *            the details
     * @param cause
     *            the cause of the exception
     */
    public CacheElementProviderException(String message, Throwable cause) {
        super(message, cause);
    }

}
