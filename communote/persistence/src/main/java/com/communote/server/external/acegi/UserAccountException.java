package com.communote.server.external.acegi;

import org.springframework.security.core.AuthenticationException;

/**
 * Base exception which holds the login name of the user that could not be authenticated.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class UserAccountException extends AuthenticationException {
    private static final long serialVersionUID = 8492587996848721321L;
    private final String username;

    /**
     * specific constructor
     *
     * @param msg
     *            exception message.
     * @param username
     *            The users name.
     */
    public UserAccountException(String msg, String username) {
        super(msg);
        this.username = username;
    }

    /**
     * @return the user name
     */
    public String getUsername() {
        return username;
    }
}
