package com.communote.server.core.common.ldap;

/**
 * Property constants that represent attribute names to be mapped to LDAP attributes for
 * synchronization of users with LDAP directories
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public enum LdapGroupAttribute {

    /** attribute referring to a permanent unique ID of an external {@code Group} */
    UID("uid", true),

    /**
     * attribute referring to the LDAP attribute holding member/membership information of an
     * external {@code Group}, e.g. member, memberOf, uniqueMember
     */
    MEMBERSHIP("membership", true),

    /** attribute referring to the display name of a {@code Group} */
    NAME("name", true),

    /** attribute referring to the alias of a {@code Group} */
    ALIAS("alias", false),

    /** attribute referring to the discription of a {@code Group} */
    DESCRIPTION("description", false);

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
    private LdapGroupAttribute(String name, boolean required) {
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
     * @return {@code true} (in String representation) if this attribute is required.
     */
    public String getRequired() {
        return String.valueOf(required);
    }

    /**
     * 
     * 
     * @return {@code true} if this attribute is required.
     */
    public boolean isRequired() {
        return required;
    }

    /**
     * @return value of getName
     */
    @Override
    public String toString() {
        return getName();
    }
}