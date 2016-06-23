package com.communote.server.core.user;


/**
 * The default exception thrown for unexpected errors occurring within
 * {@link com.communote.server.api.core.security.SecurityCodeManagement}.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class SecurityCodeManagementException extends RuntimeException {
    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = -1641693191680293192L;

    /**
     * Constructs a new instance of <code>SecurityCodeManagementException</code>.
     *
     * @param message
     *            the throwable message.
     */
    public SecurityCodeManagementException(String message) {
        super(message);
    }

    /**
     * Constructs a new instance of <code>SecurityCodeManagementException</code>.
     *
     * @param message
     *            the throwable message.
     * @param throwable
     *            the parent of this Throwable.
     */
    public SecurityCodeManagementException(String message, Throwable throwable) {
        super(message, throwable);
    }
}