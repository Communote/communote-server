package com.communote.server.external.acegi;

import org.springframework.security.core.AuthenticationException;

/**
 * Exception to be thrown if a user account was permanently locked
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class UserAccountPermanentlyLockedException extends AuthenticationException {

    private static final long serialVersionUID = 7808174224400215223L;

    /**
     * specific constructor
     *
     * @param msg
     *            exception message
     */
    public UserAccountPermanentlyLockedException(String msg) {
        super(msg);
    }

}
