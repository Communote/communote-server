package com.communote.server.core.security.ldap;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class LdapDnAlreadyExistsException extends LdapUserException {

    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = -6625938010247023258L;

    /**
     * Constructs a new instance of LdapDnAlreadyExistsException
     *
     */
    public LdapDnAlreadyExistsException(String message) {
        super(message);
    }

}
