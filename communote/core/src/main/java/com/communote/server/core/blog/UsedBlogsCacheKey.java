package com.communote.server.core.blog;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.blog.BlogData;
import com.communote.server.api.core.config.type.ClientProperty;
import com.communote.server.core.common.caching.AbstractCacheElementProvider;
import com.communote.server.core.common.caching.CacheElementProvider;
import com.communote.server.core.common.caching.IdBasedCacheKey;
import com.communote.server.core.security.SecurityHelper;
import com.communote.server.persistence.blog.BlogDao;

/**
 * {@link com.communote.server.core.common.caching.CacheKey} for last and most used blogs.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class UsedBlogsCacheKey extends IdBasedCacheKey {

    /**
     * Object holding the IDs of n used blogs. This can be for example the most used or the last
     * used blogs.
     *
     * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
     */
    public static class UsedBlogs implements Serializable {

        /**
         * Default serial version UID
         */
        private static final long serialVersionUID = 1L;
        private final List<Long> blogIds;
        private final int maxResults;

        /**
         * Constructs a new instance
         *
         * @param blogIds
         *            the IDs of the blogs
         * @param maxResults
         *            the number of items that should have been fetched. This can be larger than the
         *            size of the blogs array because the user might not have written into that much
         *            blogs.
         */
        public UsedBlogs(List<Long> blogIds, int maxResults) {
            this.blogIds = blogIds;
            this.maxResults = maxResults;
        }

        /**
         * @return the IDs of the top blogs
         */
        public List<Long> getBlogIds() {
            return this.blogIds;
        }

        /**
         * @return the number of items that should have been fetched. This can be larger than the
         *         size of the blogs array because the user might not have written into that much
         *         blogs.
         */
        public int getMaxResults() {
            return this.maxResults;
        }
    }

    /** Provider for last used blogs */
    public static final CacheElementProvider<UsedBlogsCacheKey, UsedBlogs> LAST_USED_BLOGS_PROVIDER = new AbstractCacheElementProvider<UsedBlogsCacheKey, UsedBlogs>(
            "last-used-blogs", 3600) {
        /**
         * @param key
         *            The key.
         * @return List of BlogListItems.
         */
        @Override
        public UsedBlogs load(UsedBlogsCacheKey key) {
            List<BlogData> blogItems = ServiceLocator.findService(BlogDao.class).getLastUsedBlogs(
                    key.getId(), key.getMaxResults());
            List<Long> ids = new ArrayList<Long>(blogItems.size());
            for (BlogData item : blogItems) {
                ids.add(item.getId());
            }
            return new UsedBlogs(ids, key.getMaxResults());
        }
    };

    /** Provider for most used blogs */
    public static final CacheElementProvider<UsedBlogsCacheKey, UsedBlogs> MOST_USED_BLOGS_PROVIDER = new AbstractCacheElementProvider<UsedBlogsCacheKey, UsedBlogs>(
            "most-used-blogs", 3600) {

        /**
         * @param key
         *            The key.
         * @return List of BlogListItems.
         */
        @Override
        public UsedBlogs load(UsedBlogsCacheKey key) {
            int maxDays = CommunoteRuntime.getInstance().getConfigurationManager()
                    .getClientConfigurationProperties()
                    .getProperty(ClientProperty.NUMBER_OF_DAYS_FOR_MOST_USED_BLOGS, 182);
            List<BlogData> blogItems = ServiceLocator.findService(BlogDao.class).getMostUsedBlogs(
                    key.getId(), key.getMaxResults(), maxDays);

            List<Long> ids = new ArrayList<Long>(blogItems.size());
            for (BlogData item : blogItems) {
                ids.add(item.getId());
            }
            return new UsedBlogs(ids, key.getMaxResults());
        }
    };

    private final int maxResults;

    /**
     * Creates a new CacheKey for retrieving used blogs. The maxResults only relevant get operations
     * and can be an arbitrary value when invalidating a cache entry.
     *
     * @param maxResults
     *            max results.
     */
    public UsedBlogsCacheKey(int maxResults) {
        this(maxResults, SecurityHelper.assertCurrentUserId());
    }

    /**
     * Creates a new CacheKey for retrieving used blogs. The maxResults only relevant get operations
     * and can be an arbitrary value when invalidating a cache entry.
     *
     * @param maxResults
     *            max results.
     * @param userId
     *            Id of the user for which this key is used for.
     */
    public UsedBlogsCacheKey(int maxResults, long userId) {
        super(userId);
        this.maxResults = maxResults;
    }

    /**
     * @return the maxResults
     */
    public int getMaxResults() {
        return maxResults;
    }
}
