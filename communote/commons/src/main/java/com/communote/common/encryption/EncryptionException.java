package com.communote.common.encryption;

/**
 * Exception which is thrown when the de- or encryption failed
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class EncryptionException extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * Constructor
     * 
     * @param message
     *            detail message
     */
    public EncryptionException(String message) {
        super(message);
    }

    /**
     * @param message
     *            detail message
     * @param cause
     *            cause of the exception
     */
    public EncryptionException(String message, Throwable cause) {
        super(message, cause);
    }
}
