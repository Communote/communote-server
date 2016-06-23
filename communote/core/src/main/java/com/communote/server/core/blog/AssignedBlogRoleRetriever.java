package com.communote.server.core.blog;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.event.EventListener;
import com.communote.server.core.common.caching.Cache;
import com.communote.server.core.common.caching.CacheElementProvider;
import com.communote.server.core.common.caching.CacheManager;
import com.communote.server.model.blog.BlogRole;
import com.communote.server.persistence.blog.UserToBlogRoleMappingDao;

/**
 * Gives access to the effective blog role which was assigned to a user either directly or
 * indirectly through group membership.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class AssignedBlogRoleRetriever implements EventListener<AssignedBlogRoleChangedEvent> {

    /**
     * Inner cache element provider for caching the blog roles per user
     * 
     * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a> 
     */
    private class BlogRoleCacheElementProvider implements
            CacheElementProvider<BlogRoleCacheKey, String> {
        private static final String NO_ROLE = "NO_ROLE";

        /**
         * {@inheritDoc}
         */
        public String getContentType() {
            return "blogRole";
        }

        /**
         * {@inheritDoc}
         */
        public int getTimeToLive() {
            return 3600;
        }

        /**
         * {@inheritDoc}
         */
        public String load(BlogRoleCacheKey key) {
            BlogRole role = roleMappingDao.getRoleOfUser(key.getBlogId(), key.getUserId());
            if (role == null) {
                return NO_ROLE;
            }
            return role.getValue();
        }
    }

    private final UserToBlogRoleMappingDao roleMappingDao;
    private final BlogRoleCacheElementProvider elementProvider;

    /**
     * Creates a new instance.
     * 
     * @param roleMappingDao
     *            the DAO
     */
    public AssignedBlogRoleRetriever(UserToBlogRoleMappingDao roleMappingDao) {
        this.roleMappingDao = roleMappingDao;
        elementProvider = new BlogRoleCacheElementProvider();
    }

    /**
     * Returns the effective blog role of the given user which was directly assigned to the user or
     * assigned to a group the user is a member of. Effective in that context means that in case of
     * several available roles (e.g. because of group memberships) the role with the most privileges
     * is returned.
     * 
     * @param blogId
     *            the ID of the blog
     * @param userId
     *            the ID of the user
     * @return the role or null if the user has no assigned role for that blog
     */
    public BlogRole getAssignedRole(Long blogId, Long userId) {
        BlogRoleCacheKey cacheKey = new BlogRoleCacheKey(blogId, userId);
        String roleString = ServiceLocator.findService(CacheManager.class).getCache().get(cacheKey,
                this.elementProvider);
        if (roleString.equals(BlogRoleCacheElementProvider.NO_ROLE)) {
            return null;
        }
        return BlogRole.fromString(roleString);
    }

    /**
     * {@inheritDoc}
     */
    public Class<AssignedBlogRoleChangedEvent> getObservedEvent() {
        return AssignedBlogRoleChangedEvent.class;
    }

    /**
     * {@inheritDoc}
     */
    public void handle(AssignedBlogRoleChangedEvent event) {
        // invalidate the cache
        BlogRoleCacheKey key = new BlogRoleCacheKey(event.getBlogId(), event.getUserId());
        Cache cache = ServiceLocator.findService(CacheManager.class).getCache();
        if (cache != null) {
            cache.invalidate(key, this.elementProvider);
        }
    }

}
