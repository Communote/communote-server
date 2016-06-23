package com.communote.server.core.follow;

import java.util.HashSet;
import java.util.List;

import com.communote.server.core.common.caching.CacheElementProvider;
import com.communote.server.persistence.user.UserDao;


/**
 * CacheElementProvider which retrieves retrieves entity IDs of followables which a user follows.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class FollowCacheElementProvider implements
        CacheElementProvider<FollowCacheKey, HashSet<Long>> {

    private final UserDao userDao;

    /**
     * Creates a new element provider
     * 
     * @param userDao
     *            the DAO for the user entity
     */
    public FollowCacheElementProvider(UserDao userDao) {
        this.userDao = userDao;
    }

    /**
     * {@inheritDoc}
     */
    public String getContentType() {
        return "followed";
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
    public HashSet<Long> load(FollowCacheKey key) {
        List<Long> followedEntityIds = null;
        switch (key.getFollowedEntityType()) {
        case BLOG:
            followedEntityIds = userDao.getFollowedBlogs(key.getId(), key.getRangeStart(), key
                    .getRangeEnd());
            break;
        case USER:
            followedEntityIds = userDao.getFollowedUsers(key.getId(), key.getRangeStart(), key
                    .getRangeEnd());
            break;
        case DISCUSSION:
            followedEntityIds = userDao.getFollowedDiscussions(key.getId(), key.getRangeStart(),
                    key.getRangeEnd());
            break;
        case TAG:
            followedEntityIds = userDao.getFollowedTags(key.getId(), key.getRangeStart(),
                    key.getRangeEnd());
            break;
        default:
            throw new IllegalArgumentException("Unsupported follow entity type "
                    + key.getFollowedEntityType().name());
        }
        if (followedEntityIds == null) {
            return new HashSet<Long>();
        }
        return new HashSet<Long>(followedEntityIds);
    }
}
