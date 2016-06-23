package com.communote.server.core.user;


/**
 * The default exception thrown for unexpected errors occurring within
 * {@link com.communote.server.core.user.UserGroupMemberManagement}.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class UserGroupMemberManagementException extends RuntimeException {
    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = -9117997033678260427L;

    /**
     * Constructs a new instance of <code>UserGroupMemberManagementException</code>.
     *
     * @param message
     *            the throwable message.
     */
    public UserGroupMemberManagementException(String message) {
        super(message);
    }

    /**
     * Constructs a new instance of <code>UserGroupMemberManagementException</code>.
     *
     * @param message
     *            the throwable message.
     * @param throwable
     *            the parent of this Throwable.
     */
    public UserGroupMemberManagementException(String message, Throwable throwable) {
        super(message, throwable);
    }
}