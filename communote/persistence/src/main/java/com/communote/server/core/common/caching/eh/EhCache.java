package com.communote.server.core.common.caching.eh;

import java.io.Serializable;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;

import org.apache.log4j.Logger;

import com.communote.server.core.common.caching.Cache;
import com.communote.server.core.common.caching.CacheElementWrapper;
import com.communote.server.core.common.caching.CacheException;

/**
 * Cache backend by Ehcache.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class EhCache extends Cache {

    private static final Logger LOG = Logger.getLogger(EhCache.class);
    private final Ehcache cacheBackend;

    /**
     * Creates the EhCache instance.
     * 
     * @param cacheBackend
     *            an initialized Ehcache which will be used as backend for caching data
     */
    public EhCache(Ehcache cacheBackend) {
        this.cacheBackend = cacheBackend;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected CacheElementWrapper<Serializable> handleGet(String cacheKey) {
        try {
            Element elem = cacheBackend.get(cacheKey);
            if (elem != null && !elem.isExpired()) {
                return new CacheElementWrapper<Serializable>(elem.getValue(),
                        elem.getCreationTime());
            }
            return null;
        } catch (net.sf.ehcache.CacheException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Getting element with key " + cacheKey + " failed.");
            }
            throw new CacheException(e);
        } catch (IllegalStateException e) {
            LOG.error("The Ehcache is not alive.");
            throw new CacheException("The Ehcache is not alive.", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void handleInvalidate(String cacheKey) {
        try {
            cacheBackend.remove(cacheKey);
        } catch (IllegalStateException e) {
            LOG.error("The Ehcache is not alive.");
            throw new CacheException("The Ehcache is not alive.", e);
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void handlePut(String cacheKey, CacheElementWrapper<Serializable> cacheElement,
            int timeToLive) {
        Element elem = new Element(cacheKey, cacheElement.getElement(), 1L,
                cacheElement.getCreationTimestamp(), 0L, 0L, 0L);
        if (timeToLive >= 0) {
            elem.setTimeToLive(timeToLive);
        } else {
            elem.setEternal(true);
        }
        try {
            cacheBackend.put(elem);
        } catch (net.sf.ehcache.CacheException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Caching element with key " + cacheKey + " failed.");
            }
            throw new CacheException(e);
        } catch (IllegalStateException e) {
            LOG.error("The Ehcache is not alive.");
            throw new CacheException("The Ehcache is not alive.", e);
        }
    }
}
