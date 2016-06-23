package com.communote.server.core.common.ldap;

import java.util.List;

import javax.naming.directory.SearchControls;

import org.apache.commons.lang.StringUtils;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.ldap.SpringSecurityLdapTemplate;
import org.springframework.security.ldap.search.LdapUserSearch;
import org.springframework.util.Assert;

import com.communote.server.model.config.LdapConfiguration;
import com.communote.server.model.config.LdapSearchBaseDefinition;
import com.communote.server.model.config.LdapSearchConfiguration;
import com.communote.server.persistence.user.ExternalUserVO;

/**
 * Search for a user in LDAP directory.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class CommunoteLdapUserSearch implements LdapUserSearch {

    private final LdapContextSource initialDirContextFactory;
    private final LdapSearchConfiguration userSearchConfig;
    private final String userSearchFilter;
    private final LdapUserAttributesMapper userAttributesMapper;

    /**
     * Creates a search for retrieving LDAP users.
     * 
     * @param config
     *            the LDAP configuration
     * @param kenmeiAttributeName
     *            the Kenmei attribute to be used for the {@link #searchForUser(String)} method. A
     *            search for this attribute should return at most one user.
     * @throws LdapAttributeMappingException
     *             if it's not possible to create an {@link LdapUserAttributesMapper} from the LDAP
     *             configuration
     */
    public CommunoteLdapUserSearch(LdapConfiguration config, LdapUserAttribute kenmeiAttributeName)
            throws LdapAttributeMappingException {
        Assert.notNull(config, "LDAP configuration must be set");
        Assert.notNull(config.getUserSearch(), "LDAP user search configuration must be set");
        this.userAttributesMapper = new LdapUserAttributesMapper(config);
        this.initialDirContextFactory = LdapSearchUtils.createLdapContext(config,
                userAttributesMapper);
        this.userSearchConfig = config.getUserSearch();
        this.userSearchFilter = buildSearchFilterForUserSearch(kenmeiAttributeName);
    }

    /**
     * Creates a search for retrieving LDAP users.
     * 
     * @param userSearchConfig
     *            the user search configuration of the LDAP configuration
     * @param mapper
     *            the attributes mapper to be used
     * @param context
     *            the initialized JNDI context based on the provided LDAP configuration
     * @param kenmeiAttributeName
     *            the Kenmei attribute to be used for the {@link #searchForUser(String)} method. A
     *            search for this attribute should return at most one user.
     * @param userLogin
     *            the login to use for querying the LDAP server. If null the configured managerDn
     *            will be used.
     * @param userPassword
     *            the password to use with the userLogin when querying the LDAP server
     */
    public CommunoteLdapUserSearch(LdapSearchConfiguration userSearchConfig,
            LdapUserAttributesMapper mapper, LdapContextSource context,
            LdapUserAttribute kenmeiAttributeName, String userLogin, String userPassword) {
        Assert.notNull(userSearchConfig, "LDAP user search configuration must be set");
        this.initialDirContextFactory = context;
        if (userLogin != null) {
            initialDirContextFactory.setUserDn(userLogin);
            initialDirContextFactory.setPassword(userPassword);
        }
        this.userSearchConfig = userSearchConfig;
        this.userAttributesMapper = mapper;
        this.userSearchFilter = buildSearchFilterForUserSearch(kenmeiAttributeName);
    }

    /**
     * Builds an LDAP search filter for searching for a user where the search term matches the value
     * of the LDAP attribute of the provided Kenmei attribute name.
     * 
     * @param kenmeiAttributeName
     *            the attribute name
     * @return the search filter
     */
    private String buildSearchFilterForUserSearch(LdapUserAttribute kenmeiAttributeName) {
        if (StringUtils.isNotBlank(userSearchConfig.getSearchFilter())) {
            return "(&(" + userAttributesMapper.getLdapAttributName(kenmeiAttributeName) + "={0})"
                    + userSearchConfig.getSearchFilter() + ")";
        }
        return "(" + userAttributesMapper.getLdapAttributName(kenmeiAttributeName) + "={0})";
    }

    /**
     * Returns the {@link LdapUserAttributesMapper} used by the search.
     * 
     * @return the mapper
     */
    public LdapUserAttributesMapper getUserAttributesMapper() {
        return userAttributesMapper;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DirContextOperations searchForUser(String username) {
        SpringSecurityLdapTemplate template = new SpringSecurityLdapTemplate(
                initialDirContextFactory);
        List<LdapSearchBaseDefinition> searchBaseDefs = userSearchConfig.getSearchBases();
        String[] params = { LdapUtils.escapeLdapSearchFilterValue(username) };
        SearchControls searchControls = new SearchControls();
        template.setSearchControls(searchControls);
        searchControls.setReturningAttributes(userAttributesMapper.getMappedLdapAttributeNames());
        for (LdapSearchBaseDefinition searchBase : searchBaseDefs) {
            searchControls
                    .setSearchScope(searchBase.isSearchSubtree() ? SearchControls.SUBTREE_SCOPE
                            : SearchControls.ONELEVEL_SCOPE);
            try {
                return template.searchForSingleEntry(
                        searchBase.getSearchBase(), userSearchFilter, params);
            } catch (IncorrectResultSizeDataAccessException e) {
                // ignore no results case to allow checking other search bases
                if (e.getActualSize() != 0) {
                    // Search should never return multiple results if properly configured, so just
                    // rethrow
                    throw e;
                }
            }
        }
        throw new UsernameNotFoundException("User " + username + " not found in directory.");
    }

    /**
     * Same as {@link #searchForUser(String)} but automatically transforms the LdapUserDetails to
     * the VO by invoking
     * {@link #transformResult(org.springframework.security.ldap.userdetails.LdapUserDetails)}.
     * 
     * @param username
     *            the user name to search for. The search must not return more than one value
     * @return the user
     * @throws LdapAttributeMappingException
     *             if the transformation fails
     * @throws UsernameNotFoundException
     *             in case no user is found
     * @throws IncorrectResultSizeDataAccessException
     *             in case more than users are found
     */
    public ExternalUserVO searchForUserTransformed(String username)
            throws LdapAttributeMappingException {
        DirContextOperations details = searchForUser(username);
        return transformResult(details);
    }

    /**
     * Transforms the LdapUserDetails into a VO.
     * 
     * @param details
     *            the details to transform
     * @return the transformed VO
     * @throws LdapAttributeMappingException
     *             if the transformation fails
     */
    public ExternalUserVO transformResult(DirContextOperations details)
            throws LdapAttributeMappingException {
        ExternalUserVO userVO = userAttributesMapper.mapAttributes(
                details.getDn().toString(), details.getAttributes());
        return userVO;
    }
}
