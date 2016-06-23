package com.communote.server.core.security;


/**
 * <p>
 * Thrown to indicate that the account is permanently disabled.
 * </p>
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class AccountPermanentlyDisabledException extends Exception {

    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = -29333557975250774L;

    /**
     * Constructs a new instance of AccountPermanentlyDisabledException
     *
     */
    public AccountPermanentlyDisabledException(String message) {
        super(message);
    }

}
