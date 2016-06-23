package com.communote.server.api.core.image;


/**
 * Exception indicating that an image was temporarily not found. if this exception is thrown by the
 * load method of an image provider the missing image should not be cached.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ImageTemporarilyNotFoundException extends ImageNotFoundException {

    /**
     * default serial version UID
     */
    private static final long serialVersionUID = 1L;

    /**
     * Create a new exception with detail message
     *
     * @param message
     *            the details
     */
    public ImageTemporarilyNotFoundException(String message) {
        super(message);
    }

    /**
     * Create a new exception with detail message and cause
     *
     * @param message
     *            the details
     * @param cause
     *            the cause
     */
    public ImageTemporarilyNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
