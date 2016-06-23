package com.communote.server.widgets;

/**
 * Exception to be thrown when a {@link Widget} cannot be created by a {@link WidgetFactory}
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class WidgetCreationException extends Exception {

    /**
     * default serial version ID
     */
    private static final long serialVersionUID = 1L;

    /**
     * Create an exception with a detail message
     * 
     * @param message
     *            the detail message
     */
    public WidgetCreationException(String message) {
        super(message);
    }

    /**
     * Create an exception with a detail message and a cause
     * 
     * @param message
     *            the detail message
     * @param cause
     *            the cause of the exception
     */
    public WidgetCreationException(String message, Throwable cause) {
        super(message, cause);
    }

}
