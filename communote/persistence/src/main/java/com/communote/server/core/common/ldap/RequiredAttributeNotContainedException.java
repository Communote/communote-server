package com.communote.server.core.common.ldap;

/**
 * Exception to be thrown if a required attribute was not contained in the response of an LDAP query
 * and thus could not be mapped back.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class RequiredAttributeNotContainedException extends LdapAttributeMappingException {
    private final String ldapAttributeName;

    /**
     * default serial version UID
     */
    private static final long serialVersionUID = 1L;

    private final String dn;

    /**
     * Creates an exception with a detail message.
     * 
     * @param ldapAttributeName
     *            the name of the LDAP attribute that was not contained
     * @param dn
     *            Dn of the entity, which provoked this exception.
     */
    public RequiredAttributeNotContainedException(String ldapAttributeName, String dn) {
        this(ldapAttributeName, dn, null);
    }

    /**
     * Creates an exception with a detail message and a cause.
     * 
     * @param ldapAttributeName
     *            the name of the LDAP attribute that was not contained
     * @param dn
     *            Dn of the entity, which provoked this exception.
     * @param cause
     *            the cause
     */
    public RequiredAttributeNotContainedException(String ldapAttributeName, String dn,
            Throwable cause) {
        super("Found no value for a required attribute: " + ldapAttributeName + " for dn: " + dn,
                cause);
        this.ldapAttributeName = ldapAttributeName;
        this.dn = dn;
    }

    /**
     * Creates an exception with a detail message and a cause.
     * 
     * @param ldapAttributeName
     *            the name of the LDAP attribute that was not contained
     * @param cause
     *            the cause
     */
    public RequiredAttributeNotContainedException(String ldapAttributeName, Throwable cause) {
        this(ldapAttributeName, "", cause);
    }

    /**
     * Returns the dn for the entity the attribute is missing.
     * 
     * @return the dn
     */
    public String getDn() {
        return dn;
    }

    /**
     * Returns the name of the LDAP attribute that was not contained
     * 
     * @return name of the LDAP attribute
     */
    public String getLdapAttributeName() {
        return ldapAttributeName;
    }
}
