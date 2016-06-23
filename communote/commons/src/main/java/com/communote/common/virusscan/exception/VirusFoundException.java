package com.communote.common.virusscan.exception;

/**
 * This exception only thrown when a virus was found in the data stream
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class VirusFoundException extends Exception {

    private static final long serialVersionUID = 7135331678811683547L;

    /**
     * @see Exception#Exception()
     */
    public VirusFoundException() {
    }

    /**
     * @see Exception#Exception(String)
     * @param message
     *            the detail message. The detail message is saved for later retrieval by the
     *            getMessage() method.
     */
    public VirusFoundException(String message) {
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
    public VirusFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @see Exception#Exception(Throwable)
     * @param cause
     *            the cause (which is saved for later retrieval by the getCause() method). (A null
     *            value is permitted, and indicates that the cause is nonexistent or unknown.)
     */
    public VirusFoundException(Throwable cause) {
        super(cause);
    }

}
