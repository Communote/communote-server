package com.communote.server.external.acegi;

import org.springframework.security.authentication.DisabledException;

/**
 * Exception to be thrown if a user account is temporarily disabled.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class UserAccountTemporarilyDisabledException extends DisabledException {

    /**
     * the serial version id
     */
    private static final long serialVersionUID = 1L;

    /**
     * specific constructor
     *
     * @param msg
     *            exception message
     */
    public UserAccountTemporarilyDisabledException(String msg) {
        super(msg);
    }
}
