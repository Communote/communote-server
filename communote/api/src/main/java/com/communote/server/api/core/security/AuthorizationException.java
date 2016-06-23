package com.communote.server.api.core.security;


/**
 * Thrown to indicate that the current user does not have the appropriate rights to execute an
 * action.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class AuthorizationException extends Exception {

    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = 8134260495191018534L;

    /**
     * Constructs a new instance of AuthorizationException
     *
     */
    public AuthorizationException(String message) {
        super(message);
    }

    /**
     * Constructs a new instance of AuthorizationException
     *
     */
    public AuthorizationException(String message, Throwable throwable) {
        super(message, throwable);
    }

}
