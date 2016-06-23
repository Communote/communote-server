package com.communote.server.core.security;


/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class AccountPermanentlyLockedException extends Exception {

    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = 6288950773307009029L;

    /**
     * Constructs a new instance of AccountPermanentlyLockedException
     *
     */
    public AccountPermanentlyLockedException(String message) {
        super(message);
    }

}
