package com.communote.server.core.common.caching;

import java.io.Serializable;

/**
 * A CacheElementProvider is required when an element is requested but not yet cached. In that case
 * the {@link #load(Object)} method is called and the returned element will be cached.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 * @param <K>
 *            the type of the key to be used for caching
 * @param <T>
 *            the type of the objects that this provider provides
 */
public interface CacheElementProvider<K extends CacheKey, T extends Serializable> {
    /**
     * Constant representing eternal caching.
     */
    public static final int ETERNAL_TIME_TO_LIVE = -1;

    /**
     * Returns a unique string naming the content type of the elements provided by this provider
     * (e.g. 'image' when providing image data).
     * 
     * @return the string name
     */
    public String getContentType();

    /**
     * Returns the time in seconds describing how long a provided element will be cached without
     * expiring (when it is not replaced manually). Return {@link #ETERNAL_TIME_TO_LIVE} when the
     * element should never expire.
     * 
     * @return the time to live in the cache in seconds.
     */
    public int getTimeToLive();

    /**
     * Provides an element identified by the key for caching.
     * 
     * @param key
     *            the key to identify the requested element
     * @return the loaded object or null if there is no element for the key
     * @throws CacheElementProviderException
     *             in case the loading failed and nothing should or can be cached
     */
    public T load(K key) throws CacheElementProviderException;
}
