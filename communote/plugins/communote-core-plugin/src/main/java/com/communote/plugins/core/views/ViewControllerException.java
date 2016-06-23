package com.communote.plugins.core.views;

/**
 * Exception to be thrown in case of an error while processing the request. This exception can be
 * used to convey a specific error code to the client.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class ViewControllerException extends Exception {

    private static final long serialVersionUID = 8491413781847562851L;
    private final int statusCode;

    /**
     * 
     * @param errorCode
     *            HTTP error code describing the type of error to send to the client
     * @param message
     *            The message to pass alongside the error code.
     */
    public ViewControllerException(int errorCode, String message) {
        this(errorCode, message, null);
    }

    /**
     * 
     * @param statusCode
     *            Http status code to indicate the type of error.
     * @param message
     *            The message to pass alongside the error code.
     * @param cause
     *            The original cause.
     */
    public ViewControllerException(int statusCode, String message, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
    }

    /**
     * @return the statusCode
     */
    public int getErrorCode() {
        return statusCode;
    }
}
