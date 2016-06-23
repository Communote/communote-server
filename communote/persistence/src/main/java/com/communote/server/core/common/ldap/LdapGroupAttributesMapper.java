package com.communote.server.core.common.ldap;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.naming.directory.Attributes;

import com.communote.server.core.user.helper.ValidationPatterns;
import com.communote.server.core.vo.user.group.ExternalGroupVO;

/**
 * Mapper for groups.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class LdapGroupAttributesMapper extends LdapAttributesMapper<ExternalGroupVO> {

    private static Set<String> REQUIRED_ATTRIBUTES = new HashSet<String>();
    private static Set<String> SUPPORTED_ATTRIBUTES = new HashSet<String>();
    private final String externalSystemId;
    private final boolean binaryGroupId;

    static {
        for (LdapGroupAttribute groupAttribute : LdapGroupAttribute.values()) {
            SUPPORTED_ATTRIBUTES.add(groupAttribute.getName());
            if (groupAttribute.isRequired()) {
                REQUIRED_ATTRIBUTES.add(groupAttribute.getName());
            }
        }
    }

    /**
     * @see LdapAttributesMapper#LdapAttributesMapper(Map)
     * @param mapping
     *            The mapping.
     * @param externalSystemId
     *            Id of the external system.
     * @param binaryGroupId
     *            True, if the group id is binary.
     * @throws LdapAttributeMappingException
     *             Exception.
     */
    public LdapGroupAttributesMapper(Map<String, String> mapping, String externalSystemId,
            boolean binaryGroupId) throws LdapAttributeMappingException {
        super(mapping);
        this.externalSystemId = externalSystemId;
        this.binaryGroupId = binaryGroupId;
    }

    /**
     * @see LdapAttributesMapper#LdapAttributesMapper(String)
     * @param mapping
     *            The mapping.
     * @param externalSystemId
     *            Id of the external system.
     * @param binaryGroupId
     *            True, if the group id is binary.
     * @throws LdapAttributeMappingException
     *             Exception.
     */
    public LdapGroupAttributesMapper(String mapping, String externalSystemId, boolean binaryGroupId)
            throws LdapAttributeMappingException {
        super(mapping);
        this.externalSystemId = externalSystemId;
        this.binaryGroupId = binaryGroupId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] getBinaryLdapAttributeName() {
        if (binaryGroupId) {
            return new String[] { getLdapAttributName(LdapGroupAttribute.UID.getName()) };
        }
        return null;
    }

    /**
     * @return [name]
     */
    @Override
    protected Set<String> getRequiredAttributeNames() {
        return REQUIRED_ATTRIBUTES;
    }

    /**
     * @return [alias, description]
     */
    @Override
    public Set<String> getSupportedAttributeNames() {
        return SUPPORTED_ATTRIBUTES;
    }

    /**
     * Constructs an ExernalGroupVO from the given attributes.
     * 
     * {@inheritDoc}
     * 
     * @throws LdapAttributeMappingException
     *             Exception.
     */
    @Override
    public ExternalGroupVO mapAttributes(String dn, Attributes attributes)
            throws LdapAttributeMappingException {
        ExternalGroupVO group = new ExternalGroupVO();
        group.setExternalId(getAttributeValueForKenmeiAttributeName(dn, attributes,
                LdapGroupAttribute.UID.getName()));
        group.setAdditionalProperty(dn);
        // DN is unique within one LDAP dir
        group.setMergeOnAdditionalProperty(true);
        group.setExternalSystemId(externalSystemId);
        group.setName(getAttributeValueForKenmeiAttributeName(dn, attributes,
                LdapGroupAttribute.NAME
                        .getName()));
        if (isKenmeiAttributeMapped(LdapGroupAttribute.DESCRIPTION.getName())) {
            group.setDescription(getAttributeValueForKenmeiAttributeName(dn, attributes,
                    LdapGroupAttribute.DESCRIPTION.getName()));
        }
        if (isKenmeiAttributeMapped(LdapGroupAttribute.ALIAS.getName())) {
            group.setAlias(getAttributeValueForKenmeiAttributeName(dn, attributes,
                    LdapGroupAttribute.ALIAS.getName()).replaceAll(
                    ValidationPatterns.UNSUPPORTED_CHARACTERS_IN_ALIAS, "_"));
        }
        return group;
    }
}
