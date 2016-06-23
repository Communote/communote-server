package com.communote.server.core.common.caching.eh;

import java.util.Properties;

import net.sf.ehcache.CacheException;
import net.sf.ehcache.Status;
import net.sf.ehcache.distribution.RMICacheReplicatorFactory;
import net.sf.ehcache.event.CacheEventListener;
import net.sf.ehcache.extension.CacheExtension;

import org.apache.log4j.Logger;

/**
 * Cache extensions which can switch a cache into a distributed cache that works with a specific
 * replication scheme. Note: currently only the RMI replication is supported.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class DistributionSupportCacheExtension implements CacheExtension {

    private static final Logger LOG = Logger.getLogger(DistributionSupportCacheExtension.class);

    private final net.sf.ehcache.Ehcache cacheBackend;
    private final Properties settings;
    private CacheEventListener cacheEventListener;

    /**
     * Creates a cache extension for the provided cache.
     * 
     * @param cache
     *            the cache to be extended
     * @param settings
     *            additional settings that were contained in the configuration file
     */
    public DistributionSupportCacheExtension(net.sf.ehcache.Ehcache cache, Properties settings) {
        this.cacheBackend = cache;
        this.settings = settings;
    }

    /**
     * {@inheritDoc}
     */
    public CacheExtension clone(net.sf.ehcache.Ehcache cache) throws CloneNotSupportedException {
        throw new CloneNotSupportedException("Cloning is not supported for this CacheExtension");
    }

    /**
     * Create the cache event listener that does the replication.
     * 
     * @return the event listener
     */
    private CacheEventListener createEventListener() {
        return new RMICacheReplicatorFactory().createCacheEventListener(settings);
    }

    /**
     * Turns the distribution for the extended cache off
     */
    public synchronized void disableDistribution() {
        if (this.cacheEventListener != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Disabling distribution for cache " + cacheBackend.getName());
            }
            this.cacheBackend.getCacheEventNotificationService().unregisterListener(
                    this.cacheEventListener);
            this.cacheEventListener.dispose();
            this.cacheEventListener = null;
        }
    }

    /**
     * {@inheritDoc}
     */
    public void dispose() throws CacheException {
        disableDistribution();
    }

    /**
     * Turns the distribution for the extended cache on
     */
    public synchronized void enableDistribution() {
        if (this.cacheEventListener == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Enabling distribution for cache " + cacheBackend.getName());
            }
            this.cacheEventListener = createEventListener();
            this.cacheBackend.getCacheEventNotificationService().registerListener(
                    this.cacheEventListener);
        }
    }

    /**
     * {@inheritDoc}
     */
    public Status getStatus() {
        return cacheBackend.getStatus();
    }

    /**
     * {@inheritDoc}
     */
    public void init() {
    }

}
