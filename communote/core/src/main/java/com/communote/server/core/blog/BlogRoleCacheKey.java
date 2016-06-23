package com.communote.server.core.blog;

import com.communote.server.core.common.caching.CacheKey;

/**
 * Key for caching blog roles per user and blog
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class BlogRoleCacheKey implements CacheKey {

    private final Long blogId;
    private final Long userId;
    private final String cacheKey;

    /**
     * Creates a cache key for a user and blog.
     * 
     * @param blogId
     *            the ID of the blog
     * @param userId
     *            the ID of the user
     */
    public BlogRoleCacheKey(Long blogId, Long userId) {
        this.blogId = blogId;
        this.userId = userId;
        this.cacheKey = userId.toString() + "_" + blogId;
    }

    /**
     * @return the ID of the blog
     */
    public Long getBlogId() {
        return blogId;
    }

    /**
     * {@inheritDoc}
     */
    public String getCacheKeyString() {
        return this.cacheKey;
    }

    /**
     * @return the ID of the user
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isUniquePerClient() {
        return true;
    }

}
