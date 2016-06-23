package com.communote.server.core.common.caching;

import java.io.Serializable;

/**
 * A "cache" that does not cache anything.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class NoCache extends Cache {

    @Override
    protected CacheElementWrapper<Serializable> handleGet(String cacheKey)
            throws CacheException {
        // nothing todo
        return null;
    }

    @Override
    protected void handleInvalidate(String cacheKey) throws CacheException {
        // nothing todo
    }

    @Override
    protected void handlePut(String cacheKey, CacheElementWrapper<Serializable> cacheElement,
            int timeToLive) throws CacheException {
        // nothing todo
    }

}