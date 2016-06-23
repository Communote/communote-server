package com.communote.common.virusscan.exception;

/**
 * This exception indicates an error in the scan process
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class VirusScannerException extends Exception {

    private static final long serialVersionUID = -7073868830062091080L;

    /**
     * @see Exception#Exception()
     */
    public VirusScannerException() {
        super();
    }

    /**
     * @see Exception#Exception(String)
     * @param message
     *            the detail message. The detail message is saved for later retrieval by the
     *            getMessage() method.
     */
    public VirusScannerException(String message) {
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
    public VirusScannerException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @see Exception#Exception(Throwable)
     * @param cause
     *            the cause (which is saved for later retrieval by the getCause() method). (A null
     *            value is permitted, and indicates that the cause is nonexistent or unknown.)
     */
    public VirusScannerException(Throwable cause) {
        super(cause);
    }

}
