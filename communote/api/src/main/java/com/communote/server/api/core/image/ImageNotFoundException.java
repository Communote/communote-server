package com.communote.server.api.core.image;

/**
 * Exception indicating that the image was not found.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class ImageNotFoundException extends Exception {

    private static final long serialVersionUID = -2815773334971877816L;

    /**
     * Constructor.
     */
    public ImageNotFoundException() {
        // Do nothing.
    }

    /**
     * Constructor with a simple message
     * 
     * @param message
     *            the message of this exception
     */
    public ImageNotFoundException(String message) {
        super(message);
    }

    /**
     * Constructor with a message and a cause
     * 
     * @param message
     *            the message of the exception
     * @param cause
     *            the cause of the exception
     */
    public ImageNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

}
