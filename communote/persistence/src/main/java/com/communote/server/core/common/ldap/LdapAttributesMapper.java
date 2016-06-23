package com.communote.server.core.common.ldap;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.naming.directory.Attributes;

import org.apache.commons.lang.StringUtils;

import com.communote.common.string.StringHelper;


/**
 * Generic mapper for LDAP attributes to Kenmei counterparts.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 * @param <T>
 *            the target class this mapper can map to
 */
public abstract class LdapAttributesMapper<T> {

    private Map<String, String> kenmeiToLdapMapping;

    private String kenmeiToLdapMappingString;
    private String[] mappedLdapAttributeNames;

    /**
     * Creates a mapper based on a mapping from Kenmei attribute names to LDAP attribute names.
     * 
     * @param mapping
     *            a mapping from Kenmei attribute names to LDAP attribute names
     * @throws LdapAttributeMappingException
     *             if an attribute is not supported or a required attribute is missing
     */
    public LdapAttributesMapper(Map<String, String> mapping) throws LdapAttributeMappingException {
        init(mapping);
    }

    /**
     * Convenience constructor to initialize the mapper from a string based mapping definition.
     * 
     * @param mapping
     *            a mapping in the form
     *            kenmeiAttribute1=ldapAttribute1,kenmeiAttribute2=ldapAttribute2
     * 
     * @throws LdapAttributeMappingException
     *             if an attribute is not supported or a required attribute is missing
     */
    public LdapAttributesMapper(String mapping) throws LdapAttributeMappingException {
        Map<String, String> parsedMapping = StringHelper.getStringAsMap(mapping);
        init(parsedMapping);
    }

    /**
     * Returns the value for the named LDAP attribute. If the LDAP attribute is not contained or the
     * attribute has no value {@code null} is returned. If the attribute has more than one value
     * only the first will be returned.
     * 
     * @param attributes
     *            the attributes from which the value should be extracted
     * @param ldapAttributeName
     *            the name of the attribute in LDAP
     * @param isBinary
     *            whether the value is to be interpreted as binary
     * @return the value converted to string
     * @throws LdapAttributeMappingException
     *             if there was an error while retrieving the value
     */
    protected String getAttributeValue(Attributes attributes, String ldapAttributeName)
            throws LdapAttributeMappingException {
        List<String> valueList = LdapUtils.getAttributeValues(attributes, ldapAttributeName);
        if (valueList == null || valueList.size() == 0) {
            return null;
        }
        return valueList.get(0);
    }

    /**
     * Similar to {@link #getAttributeValue(Attributes, String, boolean)} but expects a Kenmei
     * attribute name which will be automatically mapped to the LDAP attribute name. If the named
     * attribute is a required attribute the value must be non-null otherwise an exception is
     * thrown.
     * 
     * @param dn
     *            Needed for logging.
     * @param attributes
     *            the attributes from which the value should be extracted
     * @param kenmeiAttributeName
     *            the name of the attribute in Kenmei. The attribute must be one of the
     *            {@link #getSupportedAttributeNames()}.
     * @param isBinary
     *            whether the value is to be interpreted as binary
     * @return the value converted to string
     * @throws LdapAttributeMappingException
     *             if there was an error while retrieving the value or the attribute name is not
     *             supported or the attribute is required but did not return a value
     */
    protected String getAttributeValueForKenmeiAttributeName(String dn, Attributes attributes,
            String kenmeiAttributeName) throws LdapAttributeMappingException {
        String ldapAttributeName = getLdapAttributName(kenmeiAttributeName);
        if (ldapAttributeName == null) {
            throw new LdapAttributeMappingException("Attribute " + kenmeiAttributeName
                    + " is not valid for dn: " + dn);
        }
        String value = getAttributeValue(attributes, ldapAttributeName);
        if (value == null && getRequiredAttributeNames().contains(kenmeiAttributeName)) {
            throw new RequiredAttributeNotContainedException(ldapAttributeName, dn);
        }
        return value;
    }

    /**
     * Returns an array of names of mapped LDAP attributes that are binary.
     * 
     * @return the array of attribute names or null if there are no binary attributes
     */
    public abstract String[] getBinaryLdapAttributeName();

    /**
     * Returns the LDAP attribute name for a Kenmei attribute.
     * 
     * @param name
     *            the name
     * @return the mapped attribute name or null if the attribute is not mapped
     */
    public String getLdapAttributName(String name) {
        return kenmeiToLdapMapping == null ? null : kenmeiToLdapMapping.get(name);
    }

    /**
     * Returns the names of the mapped LDAP attributes.
     * 
     * @return the names of the LDAP attributes. Can be empty if nothing is mapped
     */
    public String[] getMappedLdapAttributeNames() {
        if (mappedLdapAttributeNames == null) {
            if (kenmeiToLdapMapping != null) {
                // build set to avoid doubles
                HashSet<String> ldapNames = new HashSet<String>(kenmeiToLdapMapping.values());
                mappedLdapAttributeNames = ldapNames.toArray(new String[ldapNames.size()]);
            } else {
                return new String[] { };
            }
        }
        return mappedLdapAttributeNames;
    }

    /**
     * Returns the mapping of the mapper as string in the form
     * kenmeiAttribute1=ldapAttribute1,kenmeiAttribute2=ldapAttribute2
     * 
     * @return the mapping as string
     */
    public String getMappingAsString() {
        if (kenmeiToLdapMappingString == null) {
            kenmeiToLdapMappingString = StringHelper.toString(kenmeiToLdapMapping);
        }
        return kenmeiToLdapMappingString;
    }

    /**
     * Returns the subset with required Kenmei attribute names the mapper can map.
     * 
     * @return Kenmei attribute names that are required
     */
    protected abstract Set<String> getRequiredAttributeNames();

    /**
     * Returns the Kenmei attribute names the mapper can map.
     * 
     * @return the Kenmei attribute names the mapper can map
     */
    protected abstract Set<String> getSupportedAttributeNames();

    /**
     * Initializes the mapper and validates that all required properties are contained in the
     * mapping.
     * 
     * @param mapping
     *            the mapping of Kenmei attribute names to LDAP counterparts to validate
     * @throws LdapAttributeMappingException
     *             if an attribute is not supported or a required attribute is missing
     */
    protected void init(Map<String, String> mapping) throws LdapAttributeMappingException {
        for (String kenmeiAttributeName : mapping.keySet()) {
            if (!getSupportedAttributeNames().contains(kenmeiAttributeName)) {
                throw new LdapAttributeMappingException("The attribute " + kenmeiAttributeName
                        + " is not supported");
            }
            String ldapAttributeName = mapping.get(kenmeiAttributeName);
            if (StringUtils.isEmpty((ldapAttributeName))) {
                throw new LdapAttributeMappingException("The mapped attribute for "
                        + kenmeiAttributeName + " is null or empty");
            }

        }
        // check the required attributes
        if (!mapping.keySet().containsAll(getRequiredAttributeNames())) {
            throw new LdapAttributeMappingException("Required attribute is missing");
        }
        kenmeiToLdapMapping = mapping;
        kenmeiToLdapMappingString = null;
        mappedLdapAttributeNames = null;
    }

    /**
     * Returns whether a Kenmei attribute is mapped to an LDAP attribute.
     * 
     * @param kenmeiAttributeName
     *            the Kenmei attribute to check
     * @return true if it is mapped, false otherwise
     */
    public boolean isKenmeiAttributeMapped(String kenmeiAttributeName) {
        return getLdapAttributName(kenmeiAttributeName) != null;
    }

    /**
     * Maps the LDAP attributes to the generic type.
     * 
     * @param the
     *            dn of the element providing the attributes
     * @param ldapAttributes
     *            The ldap attributes.
     * @return the target object created from the attributes
     * @throws LdapAttributeMappingException
     *             if the mapping failed
     */
    public abstract T mapAttributes(String dn, Attributes ldapAttributes)
            throws LdapAttributeMappingException;
}
