package com.communote.server.core.common.caching.eh;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import net.sf.ehcache.Ehcache;

import org.apache.log4j.Logger;

import com.communote.server.core.common.caching.Cache;
import com.communote.server.core.common.caching.CacheException;
import com.communote.server.core.common.caching.CacheManager;
import com.communote.server.core.common.caching.CacheManagerInitializationException;
import com.communote.server.core.common.caching.NoCache;

/**
 * Implementation of the {@link CacheManager} interface using Ehcache. The Ehcache configuration is
 * searched on the class-path and must be provided via {@link #setConfigurer(EhCacheConfigurer)}
 * before initializing the manager.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class EhCacheManager implements CacheManager {

    private static final Logger LOG = Logger.getLogger(EhCacheManager.class);

    private net.sf.ehcache.CacheManager ehCacheManager;
    /**
     * for initializing purposes return a cache that does nothing
     */
    private Cache managedCache = new NoCache();
    private EhCacheConfigurer configurationProvider;
    private List<String> additionalCaches;

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getAdditionalCaches() {
        if (additionalCaches == null) {
            String[] cacheName = ehCacheManager.getCacheNames();
            List<String> list = new ArrayList<String>();
            for (String cache : cacheName) {
                if (!cache.equals(configurationProvider.getMainCacheName())) {
                    list.add(cache);
                }
            }
            list.removeAll(configurationProvider.getProtectedCacheNames());
            additionalCaches = list;
        }
        return additionalCaches;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Cache getCache() {
        return managedCache;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(Properties properties) {
        if (configurationProvider == null) {
            throw new CacheManagerInitializationException(
                    "The ConfigurationProvider must be set before initializing the manager.");
        }
        String cacheName = configurationProvider.getMainCacheName();
        if (LOG.isInfoEnabled()) {
            LOG.info("Initializing Ehcache with name " + cacheName);
        }
        try {
            ehCacheManager = SingletonEhCacheManagerFactory.createCacheManager(
                    configurationProvider, properties);
            Ehcache cacheBackend = ehCacheManager.getEhcache(cacheName);
            if (cacheBackend == null) {
                throw new CacheManagerInitializationException("Ehcache with name " + cacheName
                        + " is not defined.");
            }
            managedCache = new EhCache(cacheBackend);
        } catch (CacheException e) {
            throw new CacheManagerInitializationException("Creating the Ehcache manager failed.", e);
        } catch (IllegalStateException e) {
            throw new CacheManagerInitializationException(
                    "Creating the Ehcache failed because cache with name " + cacheName
                            + " is not alive.", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void invalidateAdditionalCache(String id) {
        if (additionalCaches.contains(id)) {
            invalidateCache(id);
        } else {
            throw new CacheException("Cache with id " + id + " could not be found.");
        }
    }

    /**
     * Invalidates the given cache.
     * 
     * @param id
     *            the name of the cache
     * 
     * @throws CacheException
     *             if id of the cache could not be found or invalidation fails
     */
    private void invalidateCache(String id) throws CacheException {
        try {
            ehCacheManager.getCache(id).removeAll();
        } catch (net.sf.ehcache.CacheException e) {
            LOG.error("The Ehcache could not be invalidated.", e);
            throw new CacheException(e);
        } catch (IllegalStateException e) {
            LOG.error("The Ehcache is not alive.");
            throw new CacheException("The Ehcache is not alive.");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void invalidateMainCache() {
        invalidateCache(configurationProvider.getMainCacheName());
    }

    /**
     * Sets the configurer that should be used when initializing the EhCache. This Method must be
     * run before calling the {@link #init(Properties)} method.
     * 
     * @param provider
     *            the provider to be used
     */
    public void setConfigurer(EhCacheConfigurer provider) {
        this.configurationProvider = provider;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void shutdown() {
        if (ehCacheManager != null) {
            ehCacheManager.shutdown();
        }
    }

}
