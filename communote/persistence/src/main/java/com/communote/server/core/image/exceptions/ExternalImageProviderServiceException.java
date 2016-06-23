package com.communote.server.core.image.exceptions;

/**
 * Exception to be thrown by an external image provider to notify about an error of the external
 * service, for example when the service is not reachable.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ExternalImageProviderServiceException extends Exception {

    /**
     * default serial version UID
     */
    private static final long serialVersionUID = 1L;

    /**
     * Create an exception with a detail message
     * 
     * @param message
     *            the detail message
     */
    public ExternalImageProviderServiceException(String message) {
        super(message);
    }

    /**
     * Create an exception with a detail message and a cause
     * 
     * @param message
     *            the detail message
     * @param cause
     *            the cause
     */
    public ExternalImageProviderServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
