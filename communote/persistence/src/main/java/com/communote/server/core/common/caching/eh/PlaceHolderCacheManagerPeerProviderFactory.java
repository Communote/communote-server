package com.communote.server.core.common.caching.eh;

import java.util.Properties;

import net.sf.ehcache.CacheException;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.distribution.CacheManagerPeerProvider;
import net.sf.ehcache.distribution.CacheManagerPeerProviderFactory;

import org.apache.commons.lang.StringUtils;

/**
 * Factory to create PlaceHolderCacheManagerPeerProvider for specific replication schemes. The
 * Factory checks the properties for a property named "scheme" whose value must be the name of the
 * replication scheme for which a place holding provider will be created.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class PlaceHolderCacheManagerPeerProviderFactory extends CacheManagerPeerProviderFactory {

    /**
     * {@inheritDoc}
     */
    @Override
    public CacheManagerPeerProvider createCachePeerProvider(CacheManager cacheManager,
            Properties properties) {
        String providerScheme = properties.getProperty("scheme");
        if (StringUtils.isBlank(providerScheme)) {
            throw new CacheException(
                    "No scheme specified for which a place holder should be registered");
        }
        return new PlaceHolderCacheManagerPeerProvider(providerScheme);
    }

}
