package com.communote.common.virusscan.exception;

/**
 * A standard time out exception
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class TimeoutException extends Exception {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 3847730927861583071L;

    /**
     * @see Exception#Exception()
     */
    public TimeoutException() {
    }

    /**
     * @see Exception#Exception(String)
     * @param message
     *            the detail message. The detail message is saved for later retrieval by the
     *            getMessage() method.
     */
    public TimeoutException(String message) {
        super(message);
    }

    /**
     * @see Exception#Exception(Throwable)
     * @param cause
     *            the cause (which is saved for later retrieval by the getCause() method). (A null
     *            value is permitted, and indicates that the cause is nonexistent or unknown.)
     * @param message
     *            the detail message. The detail message is saved for later retrieval by the
     *            getMessage() method.
     */
    public TimeoutException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @see Exception#Exception(String, Throwable)
     * @param cause
     *            the cause (which is saved for later retrieval by the getCause() method). (A null
     *            value is permitted, and indicates that the cause is nonexistent or unknown.)
     */
    public TimeoutException(Throwable cause) {
        super(cause);
    }

}
