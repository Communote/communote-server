package com.communote.server.core.common.caching.eh;

import java.util.Properties;

import net.sf.ehcache.CacheException;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.distribution.CacheManagerPeerListener;
import net.sf.ehcache.distribution.CacheManagerPeerListenerFactory;

import org.apache.commons.lang.StringUtils;

/**
 * Factory to create PlaceHolderCacheManagerPeerListener for specific replication schemes. The
 * Factory checks the properties for a property named "scheme" whose value must be the name of the
 * replication scheme for which a place holding listener will be created.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class PlaceHolderCacheManagerPeerListenerFactory extends CacheManagerPeerListenerFactory {

    /**
     * {@inheritDoc}
     */
    @Override
    public CacheManagerPeerListener createCachePeerListener(CacheManager cacheManager,
            Properties properties) {
        String scheme = properties.getProperty("scheme");
        if (StringUtils.isBlank(scheme)) {
            throw new CacheException(
                    "No scheme specified for which a place holder should be registered");
        }
        return new PlaceHolderCacheManagerPeerListener(scheme);
    }

}
