package com.communote.server.core.common.exceptions;

/**
 * @author Communote Team - <a href="https://github.com/Communote">https://github.com/Communote</a>
 * @since 3.5
 */
public class PasswordValidationException extends Exception {

    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = -7227187347227565918L;

    /**
     * Constructs a new exception with a detail message.
     *
     * @param message
     *            the details
     *
     */
    public PasswordValidationException(String message) {
        super(message);
    }

}
