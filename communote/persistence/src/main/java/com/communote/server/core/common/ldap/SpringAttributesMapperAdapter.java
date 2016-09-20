package com.communote.server.core.common.ldap;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;

import org.springframework.ldap.UncategorizedLdapException;
import org.springframework.ldap.core.AttributesMapper;

/**
 * Adapter for the AttributesMapper interface used in Spring's LdapTemplate which calls Communote's
 * LdapAttributesMapper
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class SpringAttributesMapperAdapter implements AttributesMapper {

    private final LdapAttributesMapper<?> mapper;
    private final String dn;

    public SpringAttributesMapperAdapter(String dn, LdapAttributesMapper<?> mapper) {
        this.dn = dn;
        this.mapper = mapper;
    }

    /**
     * {@inheritDoc}
     *
     * @throws UncategorizedLdapException
     *             in case the wrapped mapper could not map the attributes. The actual exception
     *             will be contained in the cause.
     */
    @Override
    public Object mapFromAttributes(Attributes attributes) throws NamingException,
    UncategorizedLdapException {
        try {
            return mapper.mapAttributes(dn, attributes);
        } catch (LdapAttributeMappingException e) {
            throw new UncategorizedLdapException("Mapping LDAP attributes failed", e);
        }
    }

}
