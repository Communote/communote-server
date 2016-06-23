package com.communote.server.core.follow;

import com.communote.common.util.NumberHelper;
import com.communote.server.core.common.caching.IdBasedRangeCacheKey;

/**
 * Cache key for selected followable entities.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class FollowCacheKey extends IdBasedRangeCacheKey {
    /**
     * Content that can be cached and that is supported by this cache key.
     */
    enum FollowedEntityType {
        BLOG, USER, DISCUSSION, TAG
    }

    private final FollowedEntityType content;
    private final String cacheKey;

    /**
     * Constructor for internal use only
     *
     * @param userId
     *            the ID of the user who followed the items
     * @param type
     *            the type of content that is followed
     * @param range
     *            the range check or cache
     */
    private FollowCacheKey(Long userId, FollowedEntityType type, long[] range) {
        super(userId, range[0], range[1]);
        if (type == null) {
            throw new IllegalArgumentException("Content must not be null");
        }
        this.content = type;
        this.cacheKey = type.name() + super.getCacheKeyString();
    }

    /**
     * Creates a new cache key for the
     *
     * @param userId
     *            the ID of the user who followed the items
     * @param followedEntityId
     *            the ID of the entity for which the follow status should be cached or retrieved
     * @param type
     *            the type of content that is followed, this is the entity type the followedEntityId
     *            refers to
     */
    public FollowCacheKey(Long userId, Long followedEntityId, FollowedEntityType type) {
        this(userId, type, NumberHelper.getRangeOfNumber(followedEntityId, 200L));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getCacheKeyString() {
        return this.cacheKey;
    }

    /**
     * @return the type of content that is followed
     */
    public FollowedEntityType getFollowedEntityType() {
        return this.content;
    }
}
