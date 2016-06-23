package com.communote.server.core.common.caching;

import java.io.Serializable;

/**
 * Wrapper for the actual element retrieved by a {@link CacheElementProvider} which is enriched with
 * the creation timestamp of the cache entry and will be passed to the caching backend.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 * @param <T>
 *            the type of the element to cache
 */
public class CacheElementWrapper<T extends Serializable> {

    private final T element;
    private final long creationTimestamp;

    /**
     * Create a wrapper for the element. The creation timestamp is set to the current time.
     * 
     * @param element
     *            the element to cache, can be null
     */
    public CacheElementWrapper(T element) {
        this(element, System.currentTimeMillis());
    }

    /**
     * Create a wrapper for the element.
     * 
     * @param element
     *            the element to cache, can be null
     * @param creationTimestamp
     *            the creation timestamp of the cached element
     */
    public CacheElementWrapper(T element, long creationTimestamp) {
        this.element = element;
        this.creationTimestamp = creationTimestamp;
    }

    /**
     * @return the creation time of the cache element in milliseconds
     */
    public long getCreationTimestamp() {
        return creationTimestamp;
    }

    /**
     * @return the actual element
     */
    public T getElement() {
        return element;
    }
}
