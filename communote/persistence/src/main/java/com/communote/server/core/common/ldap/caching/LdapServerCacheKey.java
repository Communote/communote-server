package com.communote.server.core.common.ldap.caching;

import com.communote.server.core.common.caching.CacheKey;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class LdapServerCacheKey implements CacheKey {

    private final String domain;
    private final String queryPrefix;

    /**
     * Constructor.
     * 
     * @param domain
     *            Domain for the server.
     * @param queryPrefix
     *            The query prefix.
     */
    public LdapServerCacheKey(String domain, String queryPrefix) {
        this.domain = domain;
        this.queryPrefix = queryPrefix;
    }

    /**
     * @return "ldapServers".
     */
    @Override
    public String getCacheKeyString() {
        return "ldapServers|" + domain + "|" + queryPrefix;
    }

    /**
     * @return the domain
     */
    public String getDomain() {
        return domain;
    }

    /**
     * @return the queryPrefix
     */
    public String getQueryPrefix() {
        return queryPrefix;
    }

    /**
     * @return True.
     */
    @Override
    public boolean isUniquePerClient() {
        return true;
    }

}
