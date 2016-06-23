package com.communote.server.core.security.ldap;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class LdapEmailValidationException extends LdapUserException {

    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = 6898899733363691662L;

    /**
     * Constructs a new instance of LdapEmailValidationException
     *
     */
    public LdapEmailValidationException(String message) {
        super(message);
    }

    /**
     * Constructs a new instance of LdapEmailValidationException
     *
     */
    public LdapEmailValidationException(String message, Throwable throwable) {
        super(message, throwable);
    }

}
