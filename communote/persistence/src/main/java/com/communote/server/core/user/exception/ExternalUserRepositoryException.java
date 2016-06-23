package com.communote.server.core.user.exception;


/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ExternalUserRepositoryException extends GeneralUserManagementException {
    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = 8319500602616687532L;

    /**
     * Constructs a new instance of ExternalUserRepositoryException
     *
     * @param message
     *            the throwable message.
     */
    public ExternalUserRepositoryException(String message) {
        super(message);
    }

    /**
     * Constructs a new instance of ExternalUserRepositoryException
     *
     * @param message
     *            the throwable message.
     * @param throwable
     *            the parent of this Throwable.
     */
    public ExternalUserRepositoryException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
