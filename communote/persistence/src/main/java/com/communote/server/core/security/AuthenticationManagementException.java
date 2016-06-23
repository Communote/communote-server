package com.communote.server.core.security;

/**
 * The default exception thrown for unexpected errors occurring within
 * {@link com.communote.server.core.security.AuthenticationManagement}.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class AuthenticationManagementException extends RuntimeException {
    /**
     * The serial version UID of this class.
     */
    private static final long serialVersionUID = 7610663468467044343L;

    /**
     * Constructs a new instance of <code>AuthenticationManagementException</code>.
     *
     * @param message
     *            the throwable message.
     */
    public AuthenticationManagementException(String message) {
        super(message);
    }

    /**
     * Constructs a new instance of <code>AuthenticationManagementException</code>.
     *
     * @param message
     *            the details message.
     * @param cause
     *            cause of the exception
     */
    public AuthenticationManagementException(String message, Throwable cause) {
        super(message, cause);
    }
}