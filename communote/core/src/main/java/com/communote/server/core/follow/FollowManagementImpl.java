package com.communote.server.core.follow;

import java.util.HashSet;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.blog.BlogAccessException;
import com.communote.server.api.core.blog.BlogNotFoundException;
import com.communote.server.api.core.blog.BlogRightsManagement;
import com.communote.server.api.core.common.NotFoundException;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.api.core.user.UserNotFoundException;
import com.communote.server.core.blog.NoteNotFoundException;
import com.communote.server.core.common.caching.CacheManager;
import com.communote.server.core.follow.FollowCacheKey.FollowedEntityType;
import com.communote.server.core.security.SecurityHelper;
import com.communote.server.core.tag.TagNotFoundException;
import com.communote.server.model.blog.Blog;
import com.communote.server.model.blog.BlogRole;
import com.communote.server.model.follow.Followable;
import com.communote.server.model.note.Note;
import com.communote.server.model.tag.Tag;
import com.communote.server.model.user.User;
import com.communote.server.persistence.blog.BlogDao;
import com.communote.server.persistence.blog.NoteDao;
import com.communote.server.persistence.tag.TagDao;
import com.communote.server.persistence.user.UserDao;

/**
 * @see com.communote.server.core.follow.FollowManagement
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
@Service("followManagement")
public class FollowManagementImpl extends FollowManagementBase {

    private FollowCacheElementProvider followCacheElementProvider;

    @Autowired
    private UserDao userDao;
    @Autowired
    private BlogDao blogDao;
    @Autowired
    private TagDao tagDao;
    @Autowired
    private NoteDao noteDao;
    @Autowired
    private CacheManager cacheManager;

    /**
     * Follows the given followable for the current user.
     *
     * @param followable
     *            The followable item. If this null the method will instantly return.
     * @param entityId
     *            the ID of the followable
     * @param entityType
     *            the type of the entity
     */
    private void followForCurrentUser(Followable followable, Long entityId,
            FollowCacheKey.FollowedEntityType entityType) {
        if (followable == null) {
            return;
        }
        // should throw authorization exception
        User currentUser = SecurityHelper.assertCurrentKenmeiUser();
        followForUser(followable, entityId, entityType, currentUser);
    }

    /**
     * Adds the given follower follow the provided followable
     *
     * @param followable
     *            the item to follow
     * @param entityId
     *            the ID of the followable
     * @param entityType
     *            the type of the followable
     * @param follower
     *            the user that should follow the followable
     */
    private void followForUser(Followable followable, Long entityId,
            FollowCacheKey.FollowedEntityType entityType, User follower) {
        // TODO both calls can lead to OutOfMemory exceptions because collections are loaded into
        // memory
        follower.getFollowedItems().add(followable.getFollowId());
        followable.getGlobalId().getFollowers().add(follower);
        if (entityId != null) {
            invalidateFollowCache(follower.getId(), entityId, entityType);
        }
    }

    /**
     * Returns true if the current user follows the entity identified by its ID and type
     *
     * @param entityId
     *            the ID of the entity whose follow status should be checked
     * @param entityType
     *            the type of the entity whose follow status should be checked
     * @return true if the item is followed, false otherwise
     */
    private boolean followsEntity(Long entityId, FollowCacheKey.FollowedEntityType entityType) {
        if (entityId == null || SecurityHelper.isInternalSystem() || SecurityHelper.isPublicUser()) {
            return false;
        }
        Long currentUserId = SecurityHelper.assertCurrentUserId();
        FollowCacheElementProvider elementProvider = getFollowCacheElementProvider();
        FollowCacheKey key = new FollowCacheKey(currentUserId, entityId, entityType);
        HashSet<Long> followedEntityIds = ServiceLocator.findService(CacheManager.class).getCache()
                .get(key, elementProvider);
        return followedEntityIds.contains(entityId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean followsTag(Long tagId) {
        return followsEntity(tagId, FollowedEntityType.TAG);
    }

    /**
     * @return the lazily initialized cache element provider
     */
    private FollowCacheElementProvider getFollowCacheElementProvider() {
        if (followCacheElementProvider == null) {
            followCacheElementProvider = new FollowCacheElementProvider(userDao);
        }
        return followCacheElementProvider;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void handleFollowBlog(Long blogId) throws NotFoundException, BlogAccessException {
        User currentUser = SecurityHelper.assertCurrentKenmeiUser();
        Blog blog = blogDao.load(blogId);
        if (blog == null) {
            throw new BlogNotFoundException("Blog to follow does not exist", blogId, null);
        }
        boolean canRead = ServiceLocator.findService(BlogRightsManagement.class).userHasReadAccess(
                blogId, currentUser.getId(), false);
        if (!canRead) {
            throw new BlogAccessException("Current user has no read access to blog to follow",
                    blogId, BlogRole.VIEWER, null);
        }
        followForCurrentUser(blog, blog.getId(), FollowCacheKey.FollowedEntityType.BLOG);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void handleFollowDiscussion(Long discussionId) throws NotFoundException,
    BlogAccessException {
        // discussionId is the noteId of the first note of the discussion
        handleFollowDiscussionByNoteId(discussionId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void handleFollowDiscussionByNoteId(Long noteId) throws NotFoundException,
    BlogAccessException {
        Note note = noteDao.load(noteId);
        if (note == null) {
            throw new NoteNotFoundException("Note to (" + noteId + ") follow does not exist");
        }
        if (!noteId.equals(note.getDiscussionId())) {
            followDiscussionByNoteId(note.getDiscussionId());
            return;
        }
        followForCurrentUser(note, note.getDiscussionId(),
                FollowCacheKey.FollowedEntityType.DISCUSSION);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean handleFollowsBlog(Long blogId) {
        Blog blog = blogDao.load(blogId);
        if (blog == null) {
            return false;
        }
        return followsEntity(blogId, FollowCacheKey.FollowedEntityType.BLOG);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean handleFollowsDiscussion(Long discussionId) {
        return followsEntity(discussionId, FollowCacheKey.FollowedEntityType.DISCUSSION);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean handleFollowsUser(Long userId) {
        User user = userDao.load(userId);
        if (user == null) {
            return false;
        }
        return followsEntity(userId, FollowCacheKey.FollowedEntityType.USER);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void handleFollowTag(Long tagId) throws NotFoundException {
        Tag tag = tagDao.load(tagId);
        if (tag == null) {
            throw new TagNotFoundException("Tag to (" + tagId + ") follow does not exist");
        }
        followForCurrentUser(tag, tag.getId(), FollowCacheKey.FollowedEntityType.TAG);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void handleFollowUser(Long userId) throws NotFoundException {
        User userToFollow = userDao.load(userId);
        if (userToFollow == null) {
            throw new UserNotFoundException("User (" + userId + ") to follow does not exist");
        }
        followForCurrentUser(userToFollow, userId, FollowCacheKey.FollowedEntityType.USER);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void handleUnfollowBlog(Long blogId) {
        Blog blog = blogDao.load(blogId);
        unfollow(blog, blogId, FollowCacheKey.FollowedEntityType.BLOG);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void handleUnfollowDiscussion(Long discusssionId) {
        // discussionId is the noteId of the first note of the discussion
        handleUnfollowDiscussionByNoteId(discusssionId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void handleUnfollowDiscussionByNoteId(Long noteId) {
        Note note = noteDao.load(noteId);
        if (note == null) {
            return;
        }
        if (!noteId.equals(note.getDiscussionId())) {
            unfollowDiscussionByNoteId(note.getDiscussionId());
            return;
        }
        unfollow(note, note.getDiscussionId(), FollowCacheKey.FollowedEntityType.DISCUSSION);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void handleUnfollowTag(Long tagId) {
        Tag tag = tagDao.load(tagId);
        unfollow(tag, tag.getId(), FollowCacheKey.FollowedEntityType.TAG);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void handleUnfollowUser(Long userId) {
        User userToFollow = userDao.load(userId);
        unfollow(userToFollow, userId, FollowCacheKey.FollowedEntityType.USER);
    }

    /**
     * Invalidates the follow cache for a user and entity
     *
     * @param currentUserId
     *            the user for whom the follow cache should be invalidated
     * @param entityId
     *            the ID of the entity
     * @param entityType
     *            the type of the entity
     */
    private void invalidateFollowCache(Long currentUserId, Long entityId,
            FollowCacheKey.FollowedEntityType entityType) {
        FollowCacheKey key = new FollowCacheKey(currentUserId, entityId, entityType);
        cacheManager.getCache().invalidate(key,
                getFollowCacheElementProvider());
    }

    @Override
    public void tagRemoved(Long removedTagId, Long newTagId, List<Long> followers)
            throws AuthorizationException, TagNotFoundException {
        if (!SecurityHelper.isInternalSystem()) {
            throw new AuthorizationException(
                    "Only the internal system user is allowed to call this method");
        }
        // the old tag is already removed, so just invalidate the caches
        for (Long followerId : followers) {
            invalidateFollowCache(followerId, removedTagId, FollowedEntityType.TAG);
        }
        if (newTagId != null) {
            Tag tag = tagDao.load(newTagId);
            if (tag == null) {
                throw new TagNotFoundException("Tag to (" + newTagId + ") follow does not exist");
            }
            for (Long followerId : followers) {
                followForUser(tag, newTagId, FollowedEntityType.TAG, userDao.load(followerId));
            }
        }
    }

    /**
     * Unfollows the given followable for the current user.
     *
     * @param followable
     *            The followable item. If this null the method will instantly return.
     * @param entityId
     *            Id of the entity to unfollow.
     * @param entityType
     *            The type of the entity.
     */
    private void unfollow(Followable followable, Long entityId,
            FollowCacheKey.FollowedEntityType entityType) {
        if (followable == null) {
            return;
        }
        // TODO both calls can lead to OutOfMemory exceptions because collections are loaded into
        // memory
        User currentUser = SecurityHelper.assertCurrentKenmeiUser();
        currentUser.getFollowedItems().remove(followable.getFollowId());
        followable.getGlobalId().getFollowers().remove(currentUser);
        if (entityId != null) {
            invalidateFollowCache(currentUser.getId(), entityId, entityType);
        }
    }
}