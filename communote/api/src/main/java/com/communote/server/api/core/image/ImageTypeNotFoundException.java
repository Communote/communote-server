package com.communote.server.api.core.image;

/**
 * Exception to be thrown if an image type is not found.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class ImageTypeNotFoundException extends Exception {

    /**
     * default serial version UID
     */
    private static final long serialVersionUID = 1L;

    /**
     * Create a new exception with a details message
     * 
     * @param message
     *            the details message
     */
    public ImageTypeNotFoundException(String message) {
        super(message);
    }
}
