/**
 *
 */
package com.communote.server.core.common.caching;

import java.util.List;
import java.util.Properties;

/**
 * Interface describing a cache manager that manages a {@link Cache}.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public interface CacheManager {
    /**
     * Returns a list of all additional caches managed by {@link CacheManager}.
     *
     * @return a list of additional caches, an empty list if no additional caches are managed
     */
    List<String> getAdditionalCaches();

    /**
     * Returns the cache managed by this manager.
     *
     * @return the cache
     */
    Cache getCache();

    /**
     * Called to initialize the cache manager from properties. The manager might use this method to
     * initialize the associated cache.
     *
     * @param properties
     *            properties object that can contain cache manager specific settings to set up the
     *            manager
     * @throws CacheManagerInitializationException
     *             if the manager or underlying cache could not be initialized
     */
    void init(Properties properties) throws CacheManagerInitializationException;

    /**
     * Invalidates the additional cache with the given id.
     *
     * @param id
     *            id of the cache
     * @throws CacheException
     *             if id of the cache could not be found or invalidation fails
     */
    void invalidateAdditionalCache(String id) throws CacheException;

    /**
     * Invalidates the whole main cache.
     *
     * @throws CacheException
     *             if invalidation fails
     */
    void invalidateMainCache() throws CacheException;

    /**
     * Shutdown hook that allows the cache manager to do any kind of clean-up operations. This
     * method is usually called when the application is shutting down.
     */
    void shutdown();
}
