package com.communote.server.core.common.caching;

import java.io.Serializable;

import com.communote.server.api.core.security.AuthorizationException;


/**
 * This element provider checks for authorization first.
 * 
 * @param <T>
 *            The cache key.
 * @param <U>
 *            The object to cache.
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public interface AccessRestrictedCacheElementProvider<T extends CacheKey, U extends Serializable>
        extends CacheElementProvider<T, U> {
    /**
     * @param cacheKey
     *            The cache key to check for.
     * @throws AuthorizationException
     *             Thrown, when the user is not allowed to access the item.
     */
    public void assertAccess(T cacheKey) throws AuthorizationException;
}
