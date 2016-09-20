package com.communote.server.core.common.ldap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.SearchControls;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.ldap.UncategorizedLdapException;
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

    private static final LdapServerCacheElementProvider LDAP_SERVER_CACHE_ELEMENT_PROVIDER = new LdapServerCacheElementProvider();

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
                        StringHelper.toString(binaryAttributes, " "));
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
                .getCache()
                .get(new LdapServerCacheKey(domain, queryPrefix),
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
     * Retrieve an entry with the named attributes from the LDAP directory and convert it with the
     * provided attributes mapper.
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
     *            search base definitions to assert that the DN conforms to at least one of the
     *            search bases
     * @return the converted entry from the LDAP directory with the provided DN, that conforms to
     *         one of the search bases and is retrievable by the search filter or null if there is
     *         no such entry
     * @throws DataAccessException
     *             in case of access problems while communicating with the repository
     * @throws LdapAttributeMappingException
     *             in case the entry cannot be converted
     */
    public static <T> T retrieveEntry(LdapContextSource contextFactory, final String dn,
            final String searchFilter, final String[] attributesToReturn,
            Collection<LdapSearchBaseDefinition> searchBaseDefs, LdapAttributesMapper<T> mapper)
                    throws DataAccessException, LdapAttributeMappingException {
        if (searchBaseDefs != null) {
            if (!LdapUtils.dnConformsToSearchBaseDefinitions(dn, searchBaseDefs)) {
                return null;
            }
        }
        // OBJECT scope
        SearchControls searchControls = new SearchControls(SearchControls.OBJECT_SCOPE, 1, 0,
                attributesToReturn, false, false);
        LdapTemplate template = new LdapTemplate(contextFactory);
        try {
            List result = template.search(dn, searchFilter, searchControls,
                    new SpringAttributesMapperAdapter(dn, mapper));
            if (!result.isEmpty()) {
                return (T) result.get(0);
            }
        } catch (org.springframework.ldap.NameNotFoundException e) {
            // doesn't exist in LDAP, do nothing
        } catch (org.springframework.ldap.InvalidNameException e) {
            LOGGER.debug("Encountered invalid DN {}", dn);
        } catch (UncategorizedLdapException e) {
            Throwable cause = e.getCause();
            if (cause instanceof LdapAttributeMappingException) {
                throw (LdapAttributeMappingException) cause;
            }
            throw e;
        }
        return null;
    }

    /**
     * Private constructor to avoid instances of utility class.
     */
    private LdapSearchUtils() {
        // Do nothing
    }
}
