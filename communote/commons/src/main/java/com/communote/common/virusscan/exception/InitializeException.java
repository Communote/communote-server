package com.communote.common.virusscan.exception;

/**
 * This class indicates a failed initialization
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class InitializeException extends RuntimeException {

    private static final long serialVersionUID = -6436297528496374121L;

    /**
     * @see Exception#Exception()
     */
    public InitializeException() {
        super();
    }

    /**
     * @see Exception#Exception(String)
     * @param message
     *            the detail message. The detail message is saved for later retrieval by the
     *            getMessage() method.
     */
    public InitializeException(String message) {
        super(message);
    }

    /**
     * @see Exception#Exception(String, Throwable)
     * @param cause
     *            the cause (which is saved for later retrieval by the getCause() method). (A null
     *            value is permitted, and indicates that the cause is nonexistent or unknown.)
     * @param message
     *            the detail message. The detail message is saved for later retrieval by the
     *            getMessage() method.
     */
    public InitializeException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @see Exception#Exception(Throwable)
     * @param cause
     *            the cause (which is saved for later retrieval by the getCause() method). (A null
     *            value is permitted, and indicates that the cause is nonexistent or unknown.)
     */
    public InitializeException(Throwable cause) {
        super(cause);
    }

}
