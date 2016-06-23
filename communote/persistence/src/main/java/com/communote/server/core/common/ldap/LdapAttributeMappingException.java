package com.communote.server.core.common.ldap;

/**
 * Exception to be thrown if an attribute mapping is incomplete or contains wrong data.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class LdapAttributeMappingException extends Exception {

    /**
     * default serial version UID
     */
    private static final long serialVersionUID = 1L;

    /**
     * Creates an exception with a detail message.
     * 
     * @param message
     *            the detail message
     */
    public LdapAttributeMappingException(String message) {
        super(message);
    }

    /**
     * Creates an exception with a detail message and a cause.
     * 
     * @param message
     *            the detail message
     * @param cause
     *            the cause
     */
    public LdapAttributeMappingException(String message, Throwable cause) {
        super(message, cause);
    }
}
