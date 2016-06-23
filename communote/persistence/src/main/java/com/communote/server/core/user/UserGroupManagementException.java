package com.communote.server.core.user;


/**
 * The default exception thrown for unexpected errors occurring within
 * {@link com.communote.server.core.user.UserGroupManagement}.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class UserGroupManagementException extends RuntimeException {
    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = 1056473361796105484L;

    /**
     * Constructs a new instance of <code>UserGroupManagementException</code>.
     *
     * @param message
     *            the throwable message.
     */
    public UserGroupManagementException(String message) {
        super(message);
    }

    /**
     * Constructs a new instance of <code>UserGroupManagementException</code>.
     *
     * @param message
     *            the throwable message.
     * @param throwable
     *            the parent of this Throwable.
     */
    public UserGroupManagementException(String message, Throwable throwable) {
        super(message, throwable);
    }
}