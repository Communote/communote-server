package com.communote.server.core.security;

import org.springframework.security.authentication.AuthenticationServiceException;

/**
 * This exception is thrown, when it is not possible to connect to the foreign host.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class UnknownHostAuthenticationServiceException extends AuthenticationServiceException {
    /**
     * Constructs an <code>AuthenticationServiceException</code> with the specified message.
     * 
     * @param msg
     *            the detail message
     */
    public UnknownHostAuthenticationServiceException(String msg) {
        super(msg);
    }

    /**
     * Constructs an <code>AuthenticationServiceException</code> with the specified message and root
     * cause.
     * 
     * @param msg
     *            the detail message
     * @param t
     *            root cause
     */
    public UnknownHostAuthenticationServiceException(String msg, Throwable t) {
        super(msg, t);
    }
}
