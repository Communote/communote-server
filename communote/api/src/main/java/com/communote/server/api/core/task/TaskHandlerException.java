package com.communote.server.api.core.task;

/**
 * Exception to be thrown by a task handler if the execution failed.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class TaskHandlerException extends Exception {

    private static final long serialVersionUID = 9037095198156607754L;

    /**
     * Constructor.
     */
    public TaskHandlerException() {
        super();
    }

    /**
     * Constructor.
     *
     * @param message
     *            Message.
     */
    public TaskHandlerException(String message) {
        super(message);
    }

    /**
     * Constructor.
     *
     * @param message
     *            Message.
     * @param cause
     *            Cause.
     */
    public TaskHandlerException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructor.
     *
     * @param cause
     *            Cause.
     */
    public TaskHandlerException(Throwable cause) {
        super(cause);
    }

}
