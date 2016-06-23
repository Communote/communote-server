package com.communote.server.core.common.caching;

/**
 * Cache key for identifying elements by a string identifier.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class StringIdBasedCacheKey implements CacheKey {

    private final String id;
    private final boolean uniquePerClient;
    private final String prefix;

    /**
     * Creates a cache key encapsulating the ID. The key will be used application wide
     * (uniquePerClient = false).
     * 
     * @param id
     *            the ID to be cached; must not be null
     * @param prefix
     *            A prefix, which will be used for creating the cache key.
     */
    public StringIdBasedCacheKey(String id, String prefix) {
        this(id, prefix, false);
    }

    /**
     * Creates a cache key encapsulating the ID.
     * 
     * @param id
     *            the ID to be cached; must not be null
     * @param prefix
     *            A prefix, which will be used for creating the cache key.
     * @param uniquePerClient
     *            See {@link #isUniquePerClient()}
     */
    public StringIdBasedCacheKey(String id, String prefix, boolean uniquePerClient) {
        this.prefix = prefix;
        this.uniquePerClient = uniquePerClient;
        if (id == null) {
            throw new IllegalArgumentException("The id must not be null.");
        }
        this.id = id;
    }

    /**
     * {@inheritDoc}
     */
    public String getCacheKeyString() {
        return prefix + id;
    }

    /**
     * The wrapped ID.
     * 
     * @return the ID
     */
    public String getId() {
        return id;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isUniquePerClient() {
        return uniquePerClient;
    }

}
