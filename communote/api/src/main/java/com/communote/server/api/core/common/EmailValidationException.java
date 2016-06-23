package com.communote.server.api.core.common;


/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class EmailValidationException extends Exception {

    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = 8364243473314366179L;

    /**
     * Constructs a new instance of EmailValidationException
     *
     */
    public EmailValidationException(String message) {
        super(message);
    }

    /**
     * Constructs a new instance of EmailValidationException
     *
     */
    public EmailValidationException(String message, Throwable throwable) {
        super(message, throwable);
    }

}
