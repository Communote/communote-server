package com.communote.server.core.common.caching;

/**
 * Cache key for identifying elements by an ID.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class IdBasedCacheKey implements CacheKey {

    private final Long id;

    /**
     * Creates a cache key encapsulating the ID.
     * 
     * @param id
     *            the ID to be cached; must not be null
     */
    public IdBasedCacheKey(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("The id must not be null.");
        }
        this.id = id;
    }

    /**
     * {@inheritDoc}
     */
    public String getCacheKeyString() {
        return id.toString();
    }

    /**
     * The wrapped ID.
     * 
     * @return the ID
     */
    public Long getId() {
        return id;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isUniquePerClient() {
        return true;
    }

}
