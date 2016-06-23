package com.communote.plugin.ldap.helper;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.naming.NameNotFoundException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.ldap.core.ContextExecutor;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.LdapEncoder;
import org.springframework.ldap.core.LdapTemplate;

import com.communote.server.core.common.ldap.LdapAttributeMappingException;
import com.communote.server.core.common.ldap.LdapGroupAttribute;
import com.communote.server.core.common.ldap.LdapGroupAttributesMapper;
import com.communote.server.core.common.ldap.LdapSearchUtils;
import com.communote.server.core.common.ldap.LdapUtils;
import com.communote.server.core.vo.user.group.ExternalGroupVO;
import com.communote.server.model.config.LdapConfiguration;
import com.communote.server.model.config.LdapSearchBaseDefinition;
import com.communote.server.plugins.api.externals.ExternalEntityVisitor;

/**
 * This retriever is able to visit members of the given filter. The members can be retrieved via
 * member mode (memberOf) or non member mode (members).
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class MemberAndNonMemberModeVisitingRetriever extends VisitingRetriever<ExternalGroupVO> {

    /** Logger. */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(MemberAndNonMemberModeVisitingRetriever.class);

    private final LdapGroupAttributesMapper ldapGroupAttributesMapper;
    private final LdapConfiguration ldapConfiguration;
    private String groupSearchFilter;
    private String filter;
    private final ContextSource context;
    private final Map<String, ExternalGroupVO> dnToGroupsMap = new HashMap<String, ExternalGroupVO>();

    private final String[] returningAttributes;

    private final long internalHighestCommittedUSN;

    /**
     * Constructor.
     * 
     * @param filter
     *            The filter to search for.
     * @param ldapConfiguration
     *            The ldap configuration to use.
     * @param internalHighestCommittedUSN
     *            The usn to use.
     * @param pagingSize
     *            The maximal size of requestet pages.
     * @param timeout
     *            The timeout for connections.
     * @param isPagingAllowed
     *            True, if paging is allowed.
     * @param ldapGroupAttributesMapper
     *            Mapper for group attributes.
     * @throws NamingException
     *             Exception
     */
    public MemberAndNonMemberModeVisitingRetriever(String filter,
            LdapConfiguration ldapConfiguration, int timeout, int pagingSize,
            long internalHighestCommittedUSN, boolean isPagingAllowed,
            LdapGroupAttributesMapper ldapGroupAttributesMapper) throws NamingException {
        super(ldapConfiguration, timeout, pagingSize, internalHighestCommittedUSN,
                isPagingAllowed, ldapGroupAttributesMapper.getBinaryLdapAttributeName());
        this.ldapGroupAttributesMapper = ldapGroupAttributesMapper;
        this.filter = filter;
        this.ldapConfiguration = ldapConfiguration;
        this.internalHighestCommittedUSN = internalHighestCommittedUSN;
        groupSearchFilter = ldapConfiguration.getGroupSyncConfig().getGroupSearch()
                .getSearchFilter();
        if (!groupSearchFilter.startsWith("(") && !groupSearchFilter.endsWith(")")) {
            groupSearchFilter = "(" + groupSearchFilter + ")";
        }
        this.context = LdapSearchUtils.createLdapContext(ldapConfiguration,
                ldapGroupAttributesMapper);
        Set<String> returningAttributes = new HashSet<String>();
        for (String attribute : ldapGroupAttributesMapper.getSupportedAttributeNames()) {
            String ldapName = ldapGroupAttributesMapper.getLdapAttributName(attribute);
            if (ldapName != null) {
                returningAttributes.add(ldapName);
            }
        }
        this.returningAttributes = returningAttributes.toArray(new String[returningAttributes
                .size()]);
    }

    /**
     * {@inheritDoc}
     * 
     * @param incremental
     *            Incremental mode is currently not supported in non member mode.
     */
    @Override
    public void accept(ExternalEntityVisitor<ExternalGroupVO> visitor, boolean incremental)
            throws RetrieverException {
        if (ldapConfiguration.getGroupSyncConfig().isMemberMode()) {
            super.accept(visitor, incremental);
        } else {
            acceptForNonMemberMode(visitor, incremental);
        }
    }

    /**
     * Visits the entities in non member mode.
     * 
     * @param visitor
     *            The visitor.
     * @param incremental
     *            Incremental mode is currently not supported in non member mode.
     * @throws RetrieverException
     *             Exception.
     */
    private void acceptForNonMemberMode(final ExternalEntityVisitor<ExternalGroupVO> visitor,
            final boolean incremental)
            throws RetrieverException {
        ContextExecutor entrySearch = new ContextExecutor() {
            @Override
            public Object executeWithContext(DirContext dirContext) throws NamingException {
                try {
                    String synchronizationAttribute = ldapGroupAttributesMapper
                            .getLdapAttributName(LdapGroupAttribute.MEMBERSHIP.getName());
                    Attributes dnAttributes = dirContext.getAttributes(getSearchFilter(),
                            new String[] { synchronizationAttribute });
                    List<String> groupDNs = LdapUtils.getAttributeValues(dnAttributes,
                            synchronizationAttribute);
                    if (groupDNs != null) {
                        String searchFilter = incremental ? "(&(" + USN_ATTRIBUTE_KEY + ">="
                                + (internalHighestCommittedUSN + 1) + ")" + groupSearchFilter
                                + ")" : groupSearchFilter;
                        // handle the DNs
                        for (String groupDN : groupDNs) {
                            handleGroup(visitor, dirContext, searchFilter, groupDN);
                        }
                    }
                } catch (NameNotFoundException e) {
                    LOGGER.info(
                            "The user might have been deleted or moved within the external system"
                                    + " repository. Searchfilter: {}, Message: {}",
                            getSearchFilter(), e.getMessage());
                } catch (LdapAttributeMappingException e) {
                    LOGGER.error(e.getMessage(), e);
                }
                return null;
            }

            /**
             * Method to handle the group.
             * 
             * @param visitor
             *            The visitor.
             * @param dirContext
             *            The context to use.
             * @param searchFilter
             *            The search filter.
             * @param groupDN
             *            The groups dn.
             */
            private void handleGroup(final ExternalEntityVisitor<ExternalGroupVO> visitor,
                    DirContext dirContext, String searchFilter, String groupDN) {
                if (getSearchBaseDefinitions() != null
                        && !LdapUtils.dnConformsToSearchBaseDefinitions(groupDN,
                                getSearchBaseDefinitions())) {
                    LOGGER.debug("DN {} does not confirm to searchBases", groupDN);
                    return;
                }
                try {
                    if (dnToGroupsMap.containsKey(groupDN)) {
                        visitor.visit(dnToGroupsMap.get(groupDN));
                    } else {
                        // use OBJECT scope for DN based query
                        NamingEnumeration<SearchResult> result = dirContext.search(
                                groupDN.replace("/", "\\/"), // KENMEI-5545
                                searchFilter, new SearchControls(
                                        SearchControls.OBJECT_SCOPE, 1, 0,
                                        returningAttributes, false, false));
                        ExternalGroupVO externalGroupVO = null;
                        if (result.hasMore()) {
                            externalGroupVO = ldapGroupAttributesMapper
                                    .mapAttributes(
                                            groupDN, result.next().getAttributes());
                            visitor.visit(externalGroupVO);
                        }
                        dnToGroupsMap.put(groupDN, externalGroupVO);
                    }
                } catch (Exception e) {
                    LOGGER.warn("Error visiting group {} {}", groupDN,
                            e.getMessage());
                    LOGGER.debug(e.getMessage(), e);
                }
            }
        };

        LdapTemplate template = new LdapTemplate(context);
        try {
            template.executeReadOnly(entrySearch);
        } catch (DataAccessException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ContextMapper getContextMapper() {
        return new ContextMapper() {
            @Override
            public Object mapFromContext(Object ctx) {
                DirContextAdapter context = (DirContextAdapter) ctx;
                try {
                    return ldapGroupAttributesMapper.mapAttributes(context.getNameInNamespace(),
                            context.getAttributes());
                } catch (LdapAttributeMappingException e) {
                    LOGGER.warn("Error mapping a user from LDAP/AD: {} for {}", e.getMessage(),
                            context.getDn());
                }
                return null;
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<LdapSearchBaseDefinition> getSearchBaseDefinitions() {
        return ldapConfiguration.getGroupSyncConfig().getGroupSearch().getSearchBases();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getSearchFilter() {
        if (!ldapConfiguration.getGroupSyncConfig().isMemberMode()) {
            return filter;
        }
        String finalFilter = "(" + ldapGroupAttributesMapper
                .getLdapAttributName(LdapGroupAttribute.MEMBERSHIP.getName()) + "="
                + LdapEncoder.filterEncode(filter) + ")";
        String result;
        if (StringUtils.isBlank(groupSearchFilter)) {
            result = finalFilter;
        } else if (StringUtils.isBlank(finalFilter)) {
            result = groupSearchFilter;
        } else {
            result = "(&" + finalFilter + groupSearchFilter + ")";
        }
        return result;
    }

    /**
     * Method to set the filter to use. This allows better reuse of the retriever.
     * 
     * @param filter
     *            The filter to set.
     */
    public void setFilter(String filter) {
        this.filter = filter;
    }

}
