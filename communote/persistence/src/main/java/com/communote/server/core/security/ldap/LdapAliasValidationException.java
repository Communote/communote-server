package com.communote.server.core.security.ldap;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class LdapAliasValidationException extends LdapUserException {

    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = 1573612672507829169L;

    /**
     * Constructs a new instance of LdapAliasValidationException
     *
     */
    public LdapAliasValidationException(String message) {
        super(message);
    }

    /**
     * Constructs a new instance of LdapAliasValidationException
     *
     */
    public LdapAliasValidationException(String message, Throwable cause) {
        super(message, cause);
    }

}
