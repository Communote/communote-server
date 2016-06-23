package com.communote.server.core.common.caching.eh;

import java.util.List;
import java.util.Properties;

import net.sf.ehcache.CacheException;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.config.Configuration;

/**
 * An EhCacheConfigurer gives access to a configuration for the EhCache backend. The configuration
 * is usually loaded from an XML file.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public interface EhCacheConfigurer {

    /**
     * Returns the configuration to be used to initialize the EhCache.
     * 
     * @param properties
     *            optional properties to override some of the configuration settings at runtime
     * @return the configuration
     * @throws CacheException
     *             if parsing the configuration failed
     */
    public Configuration getConfiguration(Properties properties) throws CacheException;

    /**
     * Returns the name of the cache of the provided configuration which should be used as the main
     * cache. The main cache is the cache returned by {@link EhCacheManager#getCache()}.
     * 
     * @return the name of the main cache
     */
    public String getMainCacheName();

    /**
     * Returns a list of names of caches which should not be changed by {@link CacheManager}.
     * 
     * @return the list of names of caches
     */
    public List<String> getProtectedCacheNames();

    /**
     * Called after the CacheManager which was configured with the configuration of the
     * configuration file provided by this instance has been created. This allows additional
     * configuration of the manager or the managed caches. Note: the configuration possibilities are
     * rather limited after the cache manager and the caches have been created. See the EhCache
     * documentation for details.
     * 
     * @param cacheManager
     *            the created cache manager
     */
    public void postConfiguration(CacheManager cacheManager);

}
