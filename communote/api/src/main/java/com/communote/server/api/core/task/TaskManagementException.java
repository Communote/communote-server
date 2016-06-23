package com.communote.server.api.core.task;

/**
 * Exception to be thrown in TaskManagement.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class TaskManagementException extends Exception {

    private static final long serialVersionUID = -4200550072069662886L;

    /**
     * Default constructor
     * 
     * @param message
     *            a detail message
     */
    public TaskManagementException(String message) {
    }

    /**
     * Creates an exception that wraps another exception representing the actual cause
     *
     * @param message
     *            a detailed message
     * @param cause
     *            the cause of the exception
     */
    public TaskManagementException(String message, Throwable cause) {
        super(message, cause);
    }
}
