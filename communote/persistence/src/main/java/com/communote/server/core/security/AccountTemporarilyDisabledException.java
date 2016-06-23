package com.communote.server.core.security;

/**
 * <p>
 * Thrown to indicate that the account is temporarily disabled.
 * </p>
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class AccountTemporarilyDisabledException extends Exception {

    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = -8207872541904896920L;

    /**
     * Constructs a new instance of AccountTemporarilyDisabledException
     *
     */
    public AccountTemporarilyDisabledException(String message) {
        super(message);
    }

}
