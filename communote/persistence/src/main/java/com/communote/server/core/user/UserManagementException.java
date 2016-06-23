package com.communote.server.core.user;


/**
 * The default exception thrown for unexpected errors occurring within
 * {@link com.communote.server.core.user.UserManagement}.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class UserManagementException extends RuntimeException {
    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = -2892515549997342230L;

    /**
     * Constructs a new instance of <code>UserManagementException</code>.
     *
     * @param message
     *            the throwable message.
     */
    public UserManagementException(String message) {
        super(message);
    }

    /**
     * Constructs a new instance of <code>UserManagementException</code>.
     *
     * @param message
     *            the throwable message.
     * @param throwable
     *            the parent of this Throwable.
     */
    public UserManagementException(String message, Throwable throwable) {
        super(message, throwable);
    }

}