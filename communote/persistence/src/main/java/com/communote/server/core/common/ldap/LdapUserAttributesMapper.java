package com.communote.server.core.common.ldap;

import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.naming.directory.Attributes;

import com.communote.server.api.core.property.PropertyManagement;
import com.communote.server.api.core.property.StringPropertyTO;
import com.communote.server.model.config.LdapConfiguration;
import com.communote.server.persistence.user.ExternalUserVO;


/**
 * Mapper for mapping LDAP user attributes to an ExternalUserVO.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class LdapUserAttributesMapper extends LdapAttributesMapper<ExternalUserVO> {

    private static final Set<String> REQUIRED_ATTRIBUTES;
    private static final Set<String> SUPORTED_ATTRIBUTES;

    /**
     * Return the user property key group.
     * 
     * @return the key group of the property
     */
    public static String getUserPropertyKeyGroup() {
        // bundle name of plugin
        return PropertyManagement.KEY_GROUP + ".plugins.communote-plugins-ldap";
    }

    /**
     * Return the user property key name.
     * 
     * @param externalSystemId
     *            the external system id
     * @param attribute
     *            the ldap attribute
     * @return the key name of the user property
     */
    public static String getUserPropertyKeyName(String externalSystemId, LdapUserAttribute attribute) {
        return externalSystemId + ".property." + attribute.getName();
    }

    private final LdapConfiguration ldapConfig;

    private boolean takeAliasFromDN;

    static {
        Set<String> requiredAttributes = new HashSet<String>();
        requiredAttributes.add(LdapUserAttribute.ALIAS.getName());
        requiredAttributes.add(LdapUserAttribute.EMAIL.getName());
        requiredAttributes.add(LdapUserAttribute.FIRSTNAME.getName());
        requiredAttributes.add(LdapUserAttribute.LASTNAME.getName());
        requiredAttributes.add(LdapUserAttribute.UID.getName());
        REQUIRED_ATTRIBUTES = Collections.unmodifiableSet(requiredAttributes);
        // the attribute user principal name is supported but not required
        Set<String> supportedAttributes = new HashSet<String>();
        supportedAttributes.addAll(requiredAttributes);
        supportedAttributes.add(LdapUserAttribute.UPN.getName());
        SUPORTED_ATTRIBUTES = Collections.unmodifiableSet(supportedAttributes);
    }

    /**
     * Creates a mapper from a string based mapping definition.
     * 
     * @param config
     *            an LDAP configuration
     * @throws LdapAttributeMappingException
     *             if an attribute is not supported or a required attribute is missing
     */
    public LdapUserAttributesMapper(LdapConfiguration config) throws LdapAttributeMappingException {
        super(config.getUserSearch().getPropertyMapping());
        ldapConfig = config;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] getBinaryLdapAttributeName() {
        if (ldapConfig.isUserIdentifierIsBinary()) {
            return new String[] { getLdapAttributName(LdapUserAttribute.UID.getName()) };
        }
        return null;
    }

    /**
     * Returns the LDAP attribute name for a Kenmei attribute.
     * 
     * @param attributeName
     *            the name
     * @return the mapped attribute name
     */
    public String getLdapAttributName(LdapUserAttribute attributeName) {
        return getLdapAttributName(attributeName.getName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Set<String> getRequiredAttributeNames() {
        return REQUIRED_ATTRIBUTES;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Set<String> getSupportedAttributeNames() {
        return SUPORTED_ATTRIBUTES;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void init(Map<String, String> mapping) throws LdapAttributeMappingException {
        String aliasMapping = mapping.get(LdapUserAttribute.ALIAS.getName());
        if (aliasMapping.startsWith("DN:") && aliasMapping.length() > 3) {
            mapping.put(LdapUserAttribute.ALIAS.getName(), aliasMapping.substring(3));
            takeAliasFromDN = true;
        }
        mapping.put(LdapUserAttribute.UPN.getName(), LdapUserAttribute.UPN.getName());
        super.init(mapping);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ExternalUserVO mapAttributes(String dn, Attributes ldapAttributes)
            throws LdapAttributeMappingException {
        ExternalUserVO result = new ExternalUserVO();
        // don't set alias in VO because we want it to be generated from externalUserName or email
        String mappedAlias;
        if (takeAliasFromDN) {
            // extract alias value from first RDN in a case insensitive fashion
            String rdn = LdapUtils.splitAfterFirstRdn(dn).getLeft();
            int idx = rdn.indexOf('=');
            if (idx > 0 && rdn.length() > idx + 1) {
                String attribute = rdn.substring(0, idx).trim();
                String ldapAliasAttributeName = getLdapAttributName(LdapUserAttribute.ALIAS);
                if (attribute.equalsIgnoreCase(ldapAliasAttributeName)) {
                    mappedAlias = rdn.substring(idx + 1).trim();
                } else {
                    throw new LdapAttributeMappingException("Cannot extract alias from DN '" + dn
                            + "' because attribute " + ldapAliasAttributeName
                            + " is not in left most RDN");
                }
            } else {
                throw new LdapAttributeMappingException("Cannot extract alias from DN '" + dn
                        + "' because left most RDN is syntactically wrong.");
            }
        } else {
            mappedAlias = getAttributeValueForKenmeiAttributeName(dn, ldapAttributes,
                    LdapUserAttribute.ALIAS.getName());
        }
        result.setExternalUserName(mappedAlias);
        result.setEmail(getAttributeValueForKenmeiAttributeName(dn, ldapAttributes,
                LdapUserAttribute.EMAIL.getName()));
        result.setFirstName(getAttributeValueForKenmeiAttributeName(dn, ldapAttributes,
                LdapUserAttribute.FIRSTNAME.getName()));
        result.setLastName(getAttributeValueForKenmeiAttributeName(dn, ldapAttributes,
                LdapUserAttribute.LASTNAME.getName()));
        result.setPermanentId(getAttributeValueForKenmeiAttributeName(dn, ldapAttributes,
                LdapUserAttribute.UID.getName()));
        result.setSystemId(ldapConfig.getSystemId());
        result.setAdditionalProperty(dn);

        Set<StringPropertyTO> properties = new HashSet<StringPropertyTO>();
        StringPropertyTO property = new StringPropertyTO();
        property.setLastModificationDate(new Date());
        property.setKeyGroup(getUserPropertyKeyGroup());
        property.setPropertyKey(getUserPropertyKeyName(ldapConfig.getSystemId(),
                LdapUserAttribute.UPN));
        property.setPropertyValue(getAttributeValueForKenmeiAttributeName(dn, ldapAttributes,
                LdapUserAttribute.UPN.getName()));
        properties.add(property);
        result.setProperties(properties);
        return result;
    }
}
