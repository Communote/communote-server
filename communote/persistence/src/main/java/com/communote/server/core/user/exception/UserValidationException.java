package com.communote.server.core.user.exception;


/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class UserValidationException extends GeneralUserManagementException {

    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = 2913039431658022236L;

    /**
     * Constructs a new instance of UserValidationException
     *
     * @param message
     *            the throwable message.
     */
    public UserValidationException(String message) {
        super(message);
    }

    /**
     * Constructs a new instance of UserValidationException
     *
     * @param message
     *            the throwable message.
     * @param throwable
     *            the parent of this Throwable.
     */
    public UserValidationException(String message, Throwable throwable) {
        super(message, throwable);
    }

}
