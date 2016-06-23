package com.communote.server.core.follow;


/**
 * The default exception thrown for unexpected errors occurring within
 * {@link com.communote.server.core.follow.FollowManagement}.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class FollowManagementException extends RuntimeException {
    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = 5733045180087835393L;

    /**
     * Constructs a new instance of <code>FollowManagementException</code>.
     *
     * @param message
     *            the throwable message.
     */
    public FollowManagementException(String message) {
        super(message);
    }

    /**
     * Constructs a new instance of <code>FollowManagementException</code>.
     *
     * @param message
     *            the throwable message.
     * @param throwable
     *            the parent of this Throwable.
     */
    public FollowManagementException(String message, Throwable throwable) {
        super(message, throwable);
    }
}