package com.communote.server.core.security.ldap;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class LdapAliasAlreadyExistsException extends LdapUserException {

    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = -3612996747126703122L;

    /**
     * Constructs a new instance of LdapAliasAlreadyExistsException
     *
     */
    public LdapAliasAlreadyExistsException(String message) {
        super(message);
    }

}
