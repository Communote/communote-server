package com.communote.plugins.activity.base.service;

/**
 * Thrown to indicate that the activity service couldn't complete an operation.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ActivityServiceException extends Exception {

    /**
     * serial version UID
     */
    private static final long serialVersionUID = -4548480369920556263L;

    /**
     * Create a new exception with detail message and optional cause.
     * 
     * @param message
     *            the detail message
     * @param cause
     *            optional cause of the exception
     */
    public ActivityServiceException(String message, Throwable cause) {
        super(message, cause);
    }

}
