package com.communote.server.core.common.ldap;

/**
 * Property constants that represent attribute names to be mapped to LDAP attributes for
 * synchronization of users with LDAP directories
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public enum LdapUserAttribute {
    /** attribute referring to the alias of a {@code User} */
    ALIAS("alias", true),

    /** attribute referring to the email of a {@code User} */
    EMAIL("email", true),

    /** attribute referring to the first name of a {@code User} */
    FIRSTNAME("firstName", true),

    /** attribute referring to the last name of a {@code User} */
    LASTNAME("lastName", true),

    /** attribute referring to a permanent unique ID of an external {@code User} */
    UID("uid", true),

    /** attribute referring to a user principal name of an external {@code User} */
    UPN("userPrincipalName", false);

    /** the kenmei name of the attribute */
    private final String name;

    /** required kenmei attribute names */
    private final boolean required;

    /**
     * Constructor for enum type.
     * 
     * @param name
     *            the name as string
     * @param required
     *            ...
     */
    private LdapUserAttribute(String name, boolean required) {
        this.name = name;
        this.required = required;
    }

    /**
     * 
     * 
     * @return the kenmei name of the attribute
     */
    public String getName() {
        return name;
    }

    /**
     * 
     * 
     * @return {@code true} if this attribute is required.
     */
    public boolean isRequired() {
        return required;
    }
}