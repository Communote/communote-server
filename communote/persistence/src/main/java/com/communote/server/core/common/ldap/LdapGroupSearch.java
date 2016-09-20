package com.communote.server.core.common.ldap;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.ldap.core.ContextExecutor;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;

import com.communote.server.core.vo.user.group.ExternalGroupVO;
import com.communote.server.model.config.LdapConfiguration;
import com.communote.server.model.config.LdapSearchBaseDefinition;

/**
 * Class for searching Groups within a LDAP.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class LdapGroupSearch {

    /** Logger. */
    private final static Logger LOGGER = LoggerFactory.getLogger(LdapGroupSearch.class);

    private final LdapContextSource context;
    private final LdapAttributesMapper<ExternalGroupVO> attributeMapper;
    private final SearchControls controls;
    private final List<LdapSearchBaseDefinition> searchBases;
    private String groupSearchFilter;

    private final String[] returningAttributes;

    /**
     * Constructor.
     *
     * @param configuration
     *            The configuration to be used.
     * @throws LdapAttributeMappingException
     *             Exception.
     */
    public LdapGroupSearch(LdapConfiguration configuration) throws LdapAttributeMappingException {
        searchBases = configuration.getGroupSyncConfig().getGroupSearch().getSearchBases();
        groupSearchFilter = configuration.getGroupSyncConfig().getGroupSearch().getSearchFilter();
        if (!groupSearchFilter.matches("\\(.*\\)")) {
            groupSearchFilter = "(" + groupSearchFilter + ")";
        }
        attributeMapper = new LdapGroupAttributesMapper(configuration.getGroupSyncConfig()
                .getGroupSearch().getPropertyMapping(), configuration.getSystemId(), configuration
                .getGroupSyncConfig().isGroupIdentifierIsBinary());
        context = LdapSearchUtils.createLdapContext(configuration, attributeMapper);
        controls = new SearchControls();
        controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        controls.setCountLimit(0);
        controls.setDerefLinkFlag(false);
        Set<String> returningAttributes = new HashSet<String>();
        for (String attribute : attributeMapper.getSupportedAttributeNames()) {
            String ldapName = attributeMapper.getLdapAttributName(attribute);
            if (ldapName != null) {
                returningAttributes.add(ldapName);
            }
        }
        this.returningAttributes = returningAttributes.toArray(new String[returningAttributes
                .size()]);
        controls.setReturningAttributes(this.returningAttributes);
    }

    /**
     * Adds the configured group search filter via &-condition to the provided search filter if not
     * empty.
     *
     * @param filter
     *            the filter to extend
     * @return the extended filter
     */
    private String extendFilterByGroupSearchFilter(String filter) {
        if (StringUtils.isBlank(groupSearchFilter)) {
            return filter;
        }
        String finalFilter;
        if (StringUtils.isBlank(filter)) {
            finalFilter = groupSearchFilter;
        } else {
            finalFilter = "(&" + filter + groupSearchFilter + ")";
        }
        return finalFilter;
    }

    /**
     * Searches for entries within the directory
     *
     * @param searchString
     *            Search string.
     * @return The results.
     */
    public Collection<ExternalGroupVO> findGroups(String searchString) {
        searchString = LdapUtils.escapeLdapSearchFilterValue(searchString);
        final Collection<ExternalGroupVO> groups = new HashSet<ExternalGroupVO>();
        StringBuilder searchFilterBuilder = new StringBuilder("(|");
        for (String attribute : attributeMapper.getSupportedAttributeNames()) {
            String ldapAttributName = attributeMapper.getLdapAttributName(attribute);
            searchFilterBuilder.append("(");
            searchFilterBuilder.append(ldapAttributName);
            searchFilterBuilder.append("=");
            searchFilterBuilder.append(searchString);
            searchFilterBuilder.append(")");
        }
        searchFilterBuilder.append(")");
        final String searchFilter = extendFilterByGroupSearchFilter(searchFilterBuilder.toString());
        ContextExecutor entrySearch = new ContextExecutor() {

            @Override
            public Object executeWithContext(DirContext dirContext) throws NamingException {
                for (LdapSearchBaseDefinition searchBase : searchBases) {
                    controls.setSearchScope(searchBase.isSearchSubtree() ? SearchControls.SUBTREE_SCOPE
                            : SearchControls.ONELEVEL_SCOPE);
                    NamingEnumeration<SearchResult> searchResult = null;
                    try {
                        searchResult = dirContext.search(searchBase.getSearchBase(), searchFilter,
                                controls);
                        mapSearchResult(groups, searchResult, searchBase.getSearchBase());
                    } catch (NamingException e) {
                        LOGGER.error(e.getMessage(), e);
                    } finally {
                        LdapUtils.closeNamingEnumeration(searchResult);
                    }
                }
                return null;
            }
        };
        try {
            new LdapTemplate(context).executeReadOnly(entrySearch);
        } catch (DataAccessException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return groups;
    }

    /**
     * Retrieves a group by its DN if the group exists
     *
     * @param groupDN
     *            the DN of the group to retrieve
     * @return the group or null if there is no matching group
     * @throws DataAccessException
     *             in case of an error while communicating with the repository
     * @throws LdapAttributeMappingException
     *             in case the retrieved entry cannot be mapped to the VO
     */
    public ExternalGroupVO getGroup(String groupDN) throws DataAccessException,
            LdapAttributeMappingException {
        return LdapSearchUtils.retrieveEntry(context, groupDN, groupSearchFilter,
                returningAttributes, searchBases, attributeMapper);
    }

    /**
     * Checks, if the given group exists.
     *
     * @param groupDN
     *            The groups dn.
     * @return True, if the group exists, else false.
     * @throws DataAccessException
     *             Exception.
     */
    public boolean hasGroup(String groupDN) throws DataAccessException {
        try {
            if (getGroup(groupDN) != null) {
                return true;
            }
        } catch (LdapAttributeMappingException e) {
            LOGGER.debug("Group with DN {} exists but cannot be mapped: {}", groupDN, e.getMessage());
        }
        return false;
    }

    /**
     * @param groups
     *            The groups.
     * @param searchResult
     *            The search result to use.
     * @param searchBase
     *            the search base.
     */
    private void mapSearchResult(Collection<ExternalGroupVO> groups,
            NamingEnumeration<SearchResult> searchResult, String searchBase) {
        while (searchResult.hasMoreElements()) {
            SearchResult element = searchResult.nextElement();
            try {
                String dn;
                if (element.isRelative()) {
                    dn = element.getName() + "," + searchBase;
                } else {
                    dn = element.getName();
                }
                ExternalGroupVO externalGroupVO = attributeMapper.mapAttributes(dn,
                        element.getAttributes());
                groups.add(externalGroupVO);
            } catch (LdapAttributeMappingException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }
}
