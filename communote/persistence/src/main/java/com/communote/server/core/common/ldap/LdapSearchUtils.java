package com.communote.server.core.common.ldap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.naming.Context;
import javax.naming.InvalidNameException;
import javax.naming.NameNotFoundException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.ldap.core.ContextExecutor;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.security.ldap.DefaultSpringSecurityContextSource;

import com.communote.common.string.StringHelper;
import com.communote.server.api.ServiceLocator;
import com.communote.server.core.common.caching.CacheManager;
import com.communote.server.core.common.ldap.caching.LdapServer;
import com.communote.server.core.common.ldap.caching.LdapServerCacheElementProvider;
import com.communote.server.core.common.ldap.caching.LdapServerCacheKey;
import com.communote.server.model.config.LdapConfiguration;
import com.communote.server.model.config.LdapSearchBaseDefinition;

/**
 * Utils for LDAP access.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
// AD server discovery code from http://blog.armstrongconsulting.com/?p=105
// TODO Move to ldap plugin, when no dependency from core exists anymore.
public class LdapSearchUtils {

    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(LdapSearchUtils.class);

    private static final String FOLLOW = "follow";

    private static final LdapServerCacheElementProvider LDAP_SERVER_CACHE_ELEMENT_PROVIDER =
            new LdapServerCacheElementProvider();

    /**
     * Tests whether a connection to the LDAP directory is possible.
     *
     * @param config
     *            the LDAP configuration
     * @return True if a context could be created and closed, false otherwise
     */
    public static boolean canConnectToLdapDirectory(LdapConfiguration config) {
        try {
            createLdapContext(config, (String[]) null).getReadOnlyContext().close();
            return true;
        } catch (NamingException e) {
            LOGGER.error("Error connection to LDAP directory", e);
            return false;
        }
    }

    /**
     * Creates an LDAP context
     *
     * @param config
     *            the LDAP configuration
     * @param mapper
     *            the attributes mapper
     * @return the context factory
     */
    public static LdapContextSource createLdapContext(LdapConfiguration config,
            LdapAttributesMapper<?> mapper) {
        try {
            return createLdapContext(config, mapper.getBinaryLdapAttributeName());
        } catch (NamingException e) {
            LOGGER.error("There was an error configuring the LDAP context source.", e);
        }
        return null;
    }

    /**
     * @param config
     *            the ldap configuration
     * @param binaryAttributes
     *            optional array of attributes to be returned as binary. This argument need not to
     *            be provided if the context is not intended to retrieve entries.
     * @return the context factory
     * @throws NamingException
     *             Exception
     */
    public static LdapContextSource createLdapContext(LdapConfiguration config,
            String[] binaryAttributes) throws NamingException {
        String[] urls = getServerUrls(config);
        // create initial context factory
        for (String url : urls) {
            LdapContextSource context = new DefaultSpringSecurityContextSource(url);
            if (StringUtils.isNotEmpty(config.getManagerDN())) {
                context.setUserDn(config.getManagerDN());
                // manager password cannot be null
                if (config.getManagerPassword() == null) {
                    context.setPassword(StringUtils.EMPTY);
                } else {
                    context.setPassword(config.getManagerPassword());
                }
            }
            /*
             * This property helps to avoid PartialResultExceptions see
             * http://java.sun.com/products/jndi/tutorial/ldap/referral/jndi.html
             */
            Map<String, String> map = new HashMap<String, String>();
            map.put(Context.REFERRAL, FOLLOW);

            if (binaryAttributes != null && binaryAttributes.length > 0) {
                map.put("java.naming.ldap.attributes.binary",
                        StringHelper.toString(binaryAttributes,
                                " "));
            }
            if (config.getSaslMode() != null) {
                map.put(Context.SECURITY_AUTHENTICATION, config.getSaslMode());
            }
            context.setBaseEnvironmentProperties(map);
            try {
                context.afterPropertiesSet();
            } catch (Exception e) {
                LOGGER.error("There was an error configuring the LDAP context source.", e);
                continue;
            }
            return context;
        }
        return null;
    }

    /**
     * Tests whether an entry exists in LDAP directory.
     *
     * @param contextFactory
     *            context factory to use
     * @param dn
     *            the DN of the entry to test
     * @param searchFilter
     *            the searchFilter the entry must adhere to
     * @param searchBaseDefs
     *            search base definitions to assert that the DN is legal with respect to the DNs
     * @return true if there is an entry in the LDAP directory with provided DN, that conforms to
     *         one of the search bases and is retrievable by the search filter
     * @throws DataAccessException
     *             in case of access problems while communicating with the repository
     */
    public static boolean entryExists(LdapContextSource contextFactory, final String dn,
            final String searchFilter, Collection<LdapSearchBaseDefinition> searchBaseDefs)
                    throws DataAccessException {
        // no attributes to return
        return retrieveEntry(contextFactory, dn, searchFilter, new String[] { }, searchBaseDefs) != null;
    }

    /**
     * Searches for an active directory server within a windows domain.
     *
     * @param domain
     *            The domain to search in.
     * @param queryPrefix
     *            A prefix used for search.
     * @return The best fitting server.
     * @throws NamingException
     *             Exception.
     */
    private static String[] findServerUrlInWindowsDomain(String domain, String queryPrefix)
            throws NamingException {
        TreeMap<Integer, List<LdapServer>> serversMap = ServiceLocator
                .findService(CacheManager.class)
                .getCache().get(new LdapServerCacheKey(domain, queryPrefix),
                        LDAP_SERVER_CACHE_ELEMENT_PROVIDER);
        ArrayList<String> serverUrls = new ArrayList<String>();
        for (Entry<Integer, List<LdapServer>> entry : serversMap.entrySet()) {
            List<LdapServer> serverList = new ArrayList<LdapServer>(entry.getValue());
            serverLoop: while (!serverList.isEmpty()) {
                int weight = RandomUtils.nextInt(101);
                for (LdapServer server : serverList) {
                    if (weight <= server.getWeight()) {
                        serverUrls.add(server.getUrl());
                        serverList.remove(server);
                        continue serverLoop;
                    }
                }
                LdapServer server = serverList.get(0);
                serverUrls.add(server.getUrl());
                serverList.remove(server);
            }
        }
        return serverUrls.toArray(new String[serverUrls.size()]);
    }

    /**
     * Tries to find all available urls.
     *
     * @param config
     *            The config.
     * @return An array of possible urls.
     * @throws NamingException
     *             Exception.
     */
    public static String[] getServerUrls(LdapConfiguration config) throws NamingException {
        String[] urls = config.isDynamicMode() ? findServerUrlInWindowsDomain(
                config.getServerDomain(), config.getQueryPrefix())
                : new String[] { config.getUrl() };
                if (urls.length == 0 || (!config.isDynamicMode() && StringUtils.isEmpty(urls[0]))) {
                    throw new IllegalArgumentException("You must define the url of the ldap server");
                }
                return urls;
    }

    /**
     * Retrieves an entry with the named attributes from the LDAP directory.
     *
     * @param contextFactory
     *            context factory to use
     * @param dn
     *            the DN of the entry to retrieve
     * @param searchFilter
     *            the searchFilter the entry must adhere to
     * @param attributesToReturn
     *            the attributes to fetch
     * @param searchBaseDefs
     *            search base definitions to assert that the DN is legal with respect to the DNs
     * @return the entry from the LDAP directory with the provided DN, that conforms to one of the
     *         search bases and is retrievable by the search filter or null if there is no such
     *         entry
     * @throws DataAccessException
     *             in case of access problems while communicating with the repository
     */
    public static SearchResult retrieveEntry(LdapContextSource contextFactory,
            final String dn, final String searchFilter, final String[] attributesToReturn,
            Collection<LdapSearchBaseDefinition> searchBaseDefs) throws DataAccessException {
        if (searchBaseDefs != null) {
            if (!LdapUtils.dnConformsToSearchBaseDefinitions(dn, searchBaseDefs)) {
                return null;
            }
        }
        ContextExecutor entryExistsSearch = new ContextExecutor() {

            @Override
            public Object executeWithContext(DirContext dirContext) throws NamingException {
                SearchResult entry = null;
                try {
                    // OBJECT scope
                    NamingEnumeration<SearchResult> result = dirContext.search(dn, searchFilter,
                            new SearchControls(SearchControls.OBJECT_SCOPE, 1, 0,
                                    attributesToReturn, false, false));
                    if (result.hasMore()) {
                        entry = result.nextElement();
                    }
                } catch (NameNotFoundException e) {
                    // doesn't exist in LDAP, do nothing

                } catch (InvalidNameException e) {
                    LOGGER.debug("Encountered invalid DN {}", dn);
                }
                return entry;
            }
        };
        LdapTemplate template = new LdapTemplate(contextFactory);
        return (SearchResult) template.executeReadOnly(entryExistsSearch);
    }

    /**
     * Private constructor to avoid instances of utility class.
     */
    private LdapSearchUtils() {
        // Do nothing
    }
}
