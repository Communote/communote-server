package com.communote.server.core.common.caching;

/**
 * The key for caching an element.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public interface CacheKey {

    /**
     * String representation of the cache key which will be used for caching.
     * 
     * @return the string representation
     */
    String getCacheKeyString();

    /**
     * Returns true if the cache key should be treated as client specific. Using such a cache key
     * will result in caching an element per client. A return value of false indicates that the data
     * will be cached application wide.
     * 
     * @return whether the cache key should be treated as client specific
     */
    boolean isUniquePerClient();
}
