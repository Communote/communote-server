package com.communote.server.api.core.image;


/**
 * Exception to be thrown if an {@link ImageProvider} cannot be registered or unregistered.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ImageProviderManagerException extends Exception {

    /**
     * default serial version UID
     */
    private static final long serialVersionUID = 1L;

    /**
     * Create a new exception with detail message
     *
     * @param message
     *            the detail message
     */
    public ImageProviderManagerException(String message) {
        super(message);
    }

    /**
     * Create a new exception with detail message and a cause
     *
     * @param message
     *            the detail message
     * @param cause
     *            the cause of the exception
     */
    public ImageProviderManagerException(String message, Throwable cause) {
        super(message, cause);
    }

}
