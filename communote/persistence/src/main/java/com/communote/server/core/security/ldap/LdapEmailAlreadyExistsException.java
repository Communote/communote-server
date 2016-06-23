package com.communote.server.core.security.ldap;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class LdapEmailAlreadyExistsException extends LdapUserException {

    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = -2790398105890677403L;

    /**
     * Constructs a new instance of LdapEmailAlreadyExistsException
     *
     */
    public LdapEmailAlreadyExistsException(String message) {
        super(message);
    }

}
