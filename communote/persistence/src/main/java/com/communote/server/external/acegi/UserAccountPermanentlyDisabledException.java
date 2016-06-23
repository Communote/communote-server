package com.communote.server.external.acegi;

import org.springframework.security.authentication.DisabledException;

/**
 * Exception to be thrown if a user account is permanently disabled.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class UserAccountPermanentlyDisabledException extends DisabledException {

    private static final long serialVersionUID = 7808174224400215223L;

    /**
     * specific constructor
     *
     * @param msg
     *            exception message
     */
    public UserAccountPermanentlyDisabledException(String msg) {
        super(msg);
    }
}
