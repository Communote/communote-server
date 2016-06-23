package com.communote.server.core.common.caching;

/**
 * A CacheKey to identify cached items by an ID and a positive range. This is for instance useful
 * for caching items of a specific range and which are owned by an entity that can be identified
 * with the ID. The items to cache must be identifiable by a positive digit.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class IdBasedRangeCacheKey extends IdBasedCacheKey {

    private final long rangeStart;
    private final long rangeEnd;
    private final String cacheKeyString;

    /**
     * Creates a cache key encapsulating the ID. The range is set to [-1, -1]
     * 
     * @param id
     *            the ID to be cached; must not be null
     */
    public IdBasedRangeCacheKey(Long id) {
        this(id, -1, -1);
    }

    /**
     * Creates a cache key for an ID and a range
     * 
     * @param id
     *            the ID to be cached; must not be null
     * @param rangeStart
     *            the start of the range of the items to be cached. Negative numbers are normalized
     *            to -1. A possible interpretation of -1 would be to ignore this boundary.
     * @param rangeEnd
     *            the end of the range of the items to be cached. Negative numbers are normalized to
     *            -1. A possible interpretation of -1 would be to ignore this boundary.
     */
    public IdBasedRangeCacheKey(Long id, long rangeStart, long rangeEnd) {
        super(id);
        if (rangeStart < 0) {
            this.rangeStart = -1;
        } else {
            this.rangeStart = rangeStart;
        }
        if (rangeEnd < 0) {
            this.rangeEnd = -1;
        } else {
            this.rangeEnd = rangeEnd;
        }
        cacheKeyString = id.toString() + "s" + rangeStart + "e" + rangeEnd;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getCacheKeyString() {
        return this.cacheKeyString;
    }

    /**
     * @return the end of the range of the items to be cached
     */
    public long getRangeEnd() {
        return this.rangeEnd;
    }

    /**
     * @return the start of the range of the items to be cached
     */
    public long getRangeStart() {
        return this.rangeStart;
    }

}
