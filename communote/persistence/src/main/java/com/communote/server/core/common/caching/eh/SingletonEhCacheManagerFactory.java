package com.communote.server.core.common.caching.eh;

import java.io.File;
import java.net.URL;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import net.sf.ehcache.CacheException;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.config.Configuration;
import net.sf.ehcache.config.ConfigurationFactory;
import net.sf.ehcache.config.ConfigurationHelper;
import net.sf.ehcache.distribution.CacheManagerPeerListener;
import net.sf.ehcache.distribution.CacheManagerPeerProvider;
import net.sf.ehcache.event.CacheEventListener;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.communote.server.api.core.application.CommunoteRuntime;

/**
 * Creates and provides a singleton EhCache CacheManager.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class SingletonEhCacheManagerFactory {
    private static final Logger LOG = Logger.getLogger(SingletonEhCacheManagerFactory.class);
    private static final String DISK_STORE_SUBDIR = "ehcache";
    private static CacheManager SINGLETON;

    /**
     * Checks the configuration for peer listener that work with replication schemes for which place
     * holders exist. If such a peer listener and place holder exists the listener will be attached
     * to the place holder.
     *
     * @param cacheManager
     *            the cache manager to be modified
     * @param helper
     *            the configuration helper
     */
    private static void attachPeerListenerToPlaceHolder(CacheManager cacheManager,
            ConfigurationHelper helper) {
        Map<String, CacheManagerPeerListener> peerListenerMap = helper.createCachePeerListeners();
        if (peerListenerMap != null) {
            for (String scheme : peerListenerMap.keySet()) {
                CacheManagerPeerListener listener = cacheManager.getCachePeerListener(scheme);
                if (listener instanceof PlaceHolderCacheManagerPeerListener) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Adding cache peer listener for scheme " + scheme
                                + " to place holder");
                    }
                    ((PlaceHolderCacheManagerPeerListener) listener)
                    .attachCacheManagerPeerListener(peerListenerMap.get(scheme));
                } else {
                    LOG.warn("Ignoring cache peer listener for scheme " + scheme
                            + " because there is no place holder for that scheme");
                }
            }
        }
    }

    /**
     * Checks the configuration for peer provider that work with replication schemes for which place
     * holders exist. If such a peer provider and place holder exists the provider will be attached
     * to the place holder.
     *
     * @param cacheManager
     *            the cache manager to be modified
     * @param helper
     *            the configuration helper
     */
    private static void attachPeerProviderToPlaceHolder(CacheManager cacheManager,
            ConfigurationHelper helper) {
        Map<String, CacheManagerPeerProvider> peerProviderMap = helper.createCachePeerProviders();
        if (peerProviderMap != null) {
            for (String scheme : peerProviderMap.keySet()) {
                CacheManagerPeerProvider provider = cacheManager
                        .getCacheManagerPeerProvider(scheme);
                if (provider instanceof PlaceHolderCacheManagerPeerProvider) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Adding cache peer provider for scheme " + scheme
                                + " to place holder");
                    }
                    ((PlaceHolderCacheManagerPeerProvider) provider)
                    .attachCacheManagerPeerProvider(peerProviderMap.get(scheme));
                } else {
                    LOG.warn("Ignoring cache peer provider for scheme " + scheme
                            + " because there is no place holder for that scheme");
                }
            }
        }
    }

    /**
     * Creates the singleton cache manager if it does not yet exist. If it does exist the provided
     * configuration will be used to update settings of the cache manager and the managed caches.
     * Updates will only cover the registration of new listeners and creation of new caches. Other
     * modifications are not supported by EhCache.
     *
     * @param configurer
     *            provides the configuration to use when initializing or updating the cache manager
     *
     * @param runtimeSettings
     *            additional settings which will be passed to the configurer to allow overriding
     *            some settings
     * @return the cache manager
     * @throws CacheException
     *             in case the creation failed
     */
    public static CacheManager createCacheManager(EhCacheConfigurer configurer,
            Properties runtimeSettings) throws CacheException {
        internalCreateCacheManager(configurer.getConfiguration(runtimeSettings));
        configurer.postConfiguration(SINGLETON);
        return SINGLETON;
    }

    /**
     * Creates the singleton cache manager if it does not yet exist. If it does exist the provided
     * configuration will be used to update settings of the cache manager and the managed caches.
     * Updates will only cover the registration of new listeners and creation of new caches. Other
     * modifications are not supported by EhCache.
     *
     * @param pathToResource
     *            path to the EhCache XML configuration resource. The resource is expected to be on
     *            the class path.
     * @return the cache manager
     * @throws CacheException
     *             in case the creation failed
     */
    public static CacheManager createCacheManager(String pathToResource) throws CacheException {
        Configuration config = SingletonEhCacheManagerFactory.parseConfiguration(pathToResource);
        return internalCreateCacheManager(config);
    }

    /**
     * Extend the already initialized cache manager with additional configuration.
     *
     * @param cacheManager
     *            the cache manager to modify
     * @param config
     *            the configuration to parse for settings that can be added at runtime
     */
    @SuppressWarnings("unchecked")
    private static void extendEhCacheWithCustomConfig(CacheManager cacheManager,
            Configuration config) {

        ConfigurationHelper helper = new ConfigurationHelper(cacheManager, config);
        Set<net.sf.ehcache.Cache> caches = helper.createCaches();
        for (net.sf.ehcache.Cache cache : caches) {
            if (cacheManager.cacheExists(cache.getName())) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Skipping cache with name " + cache.getName()
                            + " because it already exists");
                }
                Set<CacheEventListener> listeners = cache.getCacheEventNotificationService()
                        .getCacheEventListeners();
                net.sf.ehcache.Cache existCache = cacheManager.getCache(cache.getName());
                for (CacheEventListener listener : listeners) {
                    LOG.debug("copy cache event listener for cache " + cache.getName());
                    existCache.getCacheEventNotificationService().registerListener(listener);
                }
            } else {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Adding cache with name " + cache.getName());
                }
                cacheManager.addCache(cache);
            }
        }
        attachPeerProviderToPlaceHolder(cacheManager, helper);
        attachPeerListenerToPlaceHolder(cacheManager, helper);
    }

    /**
     * @return singleton CacheManager, which will be null if {@link #createCacheManager(String)}
     *         hasn't been called yet.
     */
    public static CacheManager getInstance() {
        return SINGLETON;
    }

    /**
     * Actual creation of the singleton.
     *
     * @param config
     *            the parsed EhCache configuration
     * @return the cache manager
     * @throws CacheException
     *             in case the creation failed
     */
    private static synchronized CacheManager internalCreateCacheManager(Configuration config)
            throws CacheException {
        if (SINGLETON != null) {
            extendEhCacheWithCustomConfig(SINGLETON, config);
        } else {
            // for better control over the files created by the application override the diskstore
            // path
            // for flushing cached data to disk
            overrideDiskStorePath(config);
            // disable the update check
            config.setUpdateCheck(false);
            SINGLETON = new CacheManager(config);
        }
        return SINGLETON;
    }

    /**
     * Override the disk store path that is used for flushing cached elements to disk.
     *
     * @param config
     *            the loaded configuration
     */
    private static void overrideDiskStorePath(Configuration config) {
        File diskStoreDir = new File(CommunoteRuntime.getInstance().getConfigurationManager()
                .getStartupProperties().getCacheRootDirectory(), DISK_STORE_SUBDIR);
        String diskStorePath = diskStoreDir.getAbsolutePath();
        if (StringUtils.isNotEmpty(diskStorePath)) {
            if (config.getDiskStoreConfiguration() != null) {
                // override disk store path
                config.getDiskStoreConfiguration().setPath(diskStorePath);
            }
        }
    }

    /**
     * Parses the configuration from the provided resource location
     *
     * @param pathToResource
     *            the path to the resource
     * @return the parsed configuration
     * @throws CacheException
     *             in case parsing failed
     */
    private static Configuration parseConfiguration(String pathToResource) throws CacheException {
        URL confFileURL = SingletonEhCacheManagerFactory.class.getResource(pathToResource);
        if (confFileURL == null) {
            throw new CacheException("The path to the Ehcache "
                    + "configuration file is not defined.");
        }
        return ConfigurationFactory.parseConfiguration(confFileURL);
    }

    /**
     * Private constructor to avoid creation
     */
    private SingletonEhCacheManagerFactory() {

    }

}
