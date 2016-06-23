package com.communote.common.encryption;

/**
 * Throws if an password de/encryption error occurred
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class EncryptionException extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * Constructor.
     */
    public EncryptionException() {
        super();
    }

    /**
     * @param msg
     *            Message.
     * @param innerex
     *            Inner Exception.
     */
    public EncryptionException(String msg, Throwable innerex) {
        super(msg, innerex);
    }
}
