package com.communote.server.core.user;


/**
 * The default exception thrown for unexpected errors occurring within
 * {@link com.communote.server.core.user.UserProfileManagement}.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class UserProfileManagementException extends RuntimeException {
    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = 8620419904039496818L;

    /**
     * Constructs a new instance of <code>UserProfileManagementException</code>.
     *
     * @param message
     *            the throwable message.
     */
    public UserProfileManagementException(String message) {
        super(message);
    }

    /**
     * Constructs a new instance of <code>UserProfileManagementException</code>.
     *
     * @param message
     *            the throwable message.
     * @param throwable
     *            the parent of this Throwable.
     */
    public UserProfileManagementException(String message, Throwable throwable) {
        super(message, throwable);
    }
}