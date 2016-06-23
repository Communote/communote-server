package com.communote.server.core.common.caching;

/**
 * Common cache key class for cases where there is only one element per client that can be cached.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ClientSingleElementCacheKey implements CacheKey {

    /**
     * {@inheritDoc}
     */
    public String getCacheKeyString() {
        // nothing special because there is only one cacheable configuration per client
        return "0";
    }

    /**
     * {@inheritDoc}
     */
    public boolean isUniquePerClient() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return getCacheKeyString();
    }

}
