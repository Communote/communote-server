package com.communote.server.core.security.ldap;

/**
 * Base exception for LDAP related user data validation exceptions.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class LdapUserException extends Exception {

    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = -7531817762414309810L;

    /**
     * The default constructor.
     */
    public LdapUserException() {
    }

    /**
     * Constructs a new instance of LdapUserException
     *
     */
    public LdapUserException(String message) {
        super(message);
    }

    /**
     * Constructs a new instance of LdapUserException
     *
     */
    public LdapUserException(String message, Throwable cause) {
        super(message, cause);
    }

}
