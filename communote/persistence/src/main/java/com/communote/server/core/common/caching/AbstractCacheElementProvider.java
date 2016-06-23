package com.communote.server.core.common.caching;

import java.io.Serializable;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 * @param <K>
 *            Type of the cache key.
 * @param <T>
 *            Type of the result.
 */
public abstract class AbstractCacheElementProvider<K extends CacheKey, T extends Serializable>
        implements CacheElementProvider<K, T> {

    private final String contentType;
    private final int timeToLive;

    /**
     * Constructor.
     * 
     * @param contentType
     *            See {@link CacheElementProvider#getContentType()}.
     * @param timeToLive
     *            See {@link CacheElementProvider#getTimeToLive()}.
     */
    public AbstractCacheElementProvider(String contentType, int timeToLive) {
        this.contentType = contentType;
        this.timeToLive = Integer.getInteger(
                "com.communote.cache.ttl." + this.getClass().getName(), timeToLive);
    }

    /**
     * {@inheritDoc}
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * {@inheritDoc}
     */
    public int getTimeToLive() {
        return timeToLive;
    }
}
