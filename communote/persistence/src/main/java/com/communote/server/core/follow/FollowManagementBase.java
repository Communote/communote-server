package com.communote.server.core.follow;

import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 * Spring Service base class for <code>com.communote.server.service.follow.FollowManagement</code>,
 * provides access to all services and entities referenced by this service.
 * </p>
 * 
 * @see com.communote.server.core.follow.FollowManagement
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class FollowManagementBase implements
        com.communote.server.core.follow.FollowManagement {

    /**
     * @see com.communote.server.core.follow.FollowManagement#followBlog(Long)
     * 
     *      {@inheritDoc}
     */
    @Override
    public void followBlog(Long blogId)
            throws com.communote.server.api.core.common.NotFoundException,
            com.communote.server.api.core.blog.BlogAccessException {
        if (blogId == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.service.follow.FollowManagement.followBlog(Long blogId) "
                            + "- 'blogId' can not be null");
        }
        try {
            this.handleFollowBlog(blogId);
        } catch (RuntimeException rt) {
            throw new com.communote.server.core.follow.FollowManagementException(
                    "Error performing 'com.communote.server.service.follow.FollowManagement.followBlog(java"
                            + ".lang.Long blogId)' --> " + rt, rt);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.communote.server.core.follow.FollowManagement#followDiscussion(Long)
     */
    @Override
    public void followDiscussion(Long discussionId)
            throws com.communote.server.api.core.blog.BlogAccessException,
            com.communote.server.api.core.common.NotFoundException {
        if (discussionId == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.service.follow.FollowManagement.followDiscussion(Long"
                            + " discussionId) - 'discussionId' can not be null");
        }
        try {
            this.handleFollowDiscussion(discussionId);
        } catch (RuntimeException rt) {
            throw new com.communote.server.core.follow.FollowManagementException(
                    "Error performing 'com.communote.server.service.follow.FollowManagement.followDiscussion"
                            + "(Long discussionId)' --> " + rt, rt);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.communote.server.core.follow.FollowManagement#followDiscussionByNoteId(Long)
     */
    @Override
    public void followDiscussionByNoteId(Long noteId)
            throws com.communote.server.api.core.common.NotFoundException,
            com.communote.server.api.core.blog.BlogAccessException {
        if (noteId == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.service.follow.FollowManagement.followDiscussionByNoteId("
                            + "Long noteId) - 'noteId' can not be null");
        }
        try {
            this.handleFollowDiscussionByNoteId(noteId);
        } catch (RuntimeException rt) {
            throw new com.communote.server.core.follow.FollowManagementException(
                    "Error performing 'com.communote.server.service.follow.FollowManagement."
                            + "followDiscussionByNoteId(Long noteId)' --> " + rt, rt);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.communote.server.core.follow.FollowManagement#followsBlog(Long)
     */
    @Override
    @Transactional(readOnly = true)
    public boolean followsBlog(Long blogId) {
        if (blogId == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.service.follow.FollowManagement.followsBlog(Long blogId) "
                            + "- 'blogId' can not be null");
        }
        try {
            return this.handleFollowsBlog(blogId);
        } catch (RuntimeException rt) {
            throw new com.communote.server.core.follow.FollowManagementException(
                    "Error performing 'com.communote.server.service.follow.FollowManagement.followsBlog"
                            + "(Long blogId)' --> " + rt, rt);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.communote.server.core.follow.FollowManagement#followsDiscussion(Long)
     */
    @Override
    @Transactional(readOnly = true)
    public boolean followsDiscussion(Long discussionId) {
        if (discussionId == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.service.follow.FollowManagement.followsDiscussion(Long discussonId) "
                            + "- 'discussonId' can not be null");
        }
        try {
            return this.handleFollowsDiscussion(discussionId);
        } catch (RuntimeException rt) {
            throw new com.communote.server.core.follow.FollowManagementException(
                    "Error performing 'com.communote.server.service.follow.FollowManagement.followsDiscussion"
                            + "(Long discussonId)' --> " + rt, rt);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.communote.server.core.follow.FollowManagement#followsUser(Long)
     */
    @Override
    @Transactional(readOnly = true)
    public boolean followsUser(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.service.follow.FollowManagement.followsUser(Long userId) "
                            + "- 'userId' can not be null");
        }
        try {
            return this.handleFollowsUser(userId);
        } catch (RuntimeException rt) {
            throw new com.communote.server.core.follow.FollowManagementException(
                    "Error performing 'com.communote.server.service.follow.FollowManagement.followsUser"
                            + "(Long userId)' --> " + rt, rt);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.communote.server.core.follow.FollowManagement#followTag(Long)
     */
    @Override
    public void followTag(Long tagId)
            throws com.communote.server.api.core.common.NotFoundException {
        if (tagId == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.service.follow.FollowManagement.followTag(Long tagId)"
                            + " - 'tagId' can not be null");
        }
        try {
            this.handleFollowTag(tagId);
        } catch (RuntimeException rt) {
            throw new com.communote.server.core.follow.FollowManagementException(
                    "Error performing 'com.communote.server.service.follow.FollowManagement.followTag"
                            + "(Long tagId)' --> " + rt, rt);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.communote.server.core.follow.FollowManagement#followUser(Long)
     */
    @Override
    public void followUser(Long userId)
            throws com.communote.server.api.core.common.NotFoundException {
        if (userId == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.service.follow.FollowManagement.followUser(Long userId)"
                            + " - 'userId' can not be null");
        }
        try {
            this.handleFollowUser(userId);
        } catch (RuntimeException rt) {
            throw new com.communote.server.core.follow.FollowManagementException(
                    "Error performing 'com.communote.server.service.follow.FollowManagement.followUser"
                            + "(Long userId)' --> " + rt, rt);
        }
    }

    /**
     * {@inheritDoc} Performs the core logic for {@link #followBlog(Long)}
     */
    protected abstract void handleFollowBlog(Long blogId)
            throws com.communote.server.api.core.common.NotFoundException,
            com.communote.server.api.core.blog.BlogAccessException;

    /**
     * {@inheritDoc} Performs the core logic for {@link #followDiscussion(Long)}
     */
    protected abstract void handleFollowDiscussion(Long discussionId)
            throws com.communote.server.api.core.blog.BlogAccessException,
            com.communote.server.api.core.common.NotFoundException;

    /**
     * {@inheritDoc} Performs the core logic for {@link #followDiscussionByNoteId(Long)}
     */
    protected abstract void handleFollowDiscussionByNoteId(Long noteId)
            throws com.communote.server.api.core.common.NotFoundException,
            com.communote.server.api.core.blog.BlogAccessException;

    /**
     * {@inheritDoc} Performs the core logic for {@link #followsBlog(Long)}
     */
    protected abstract boolean handleFollowsBlog(Long blogId);

    /**
     * {@inheritDoc} Performs the core logic for {@link #followsDiscussion(Long)}
     */
    protected abstract boolean handleFollowsDiscussion(Long discussionId);

    /**
     * {@inheritDoc} Performs the core logic for {@link #followsUser(Long)}
     */
    protected abstract boolean handleFollowsUser(Long userId);

    /**
     * {@inheritDoc} Performs the core logic for {@link #followTag(Long)}
     */
    protected abstract void handleFollowTag(Long tagId)
            throws com.communote.server.api.core.common.NotFoundException;

    /**
     * {@inheritDoc} Performs the core logic for {@link #followUser(Long)}
     */
    protected abstract void handleFollowUser(Long userId)
            throws com.communote.server.api.core.common.NotFoundException;

    /**
     * {@inheritDoc} Performs the core logic for {@link #unfollowBlog(Long)}
     */
    protected abstract void handleUnfollowBlog(Long blogId);

    /**
     * {@inheritDoc} Performs the core logic for {@link #unfollowDiscussion(Long)}
     */
    protected abstract void handleUnfollowDiscussion(Long discussionId);

    /**
     * {@inheritDoc} Performs the core logic for {@link #unfollowDiscussionByNoteId(Long)}
     */
    protected abstract void handleUnfollowDiscussionByNoteId(Long noteId);

    /**
     * {@inheritDoc} Performs the core logic for {@link #unfollowTag(Long)}
     */
    protected abstract void handleUnfollowTag(Long tagId);

    /**
     * {@inheritDoc} Performs the core logic for {@link #unfollowUser(Long)}
     */
    protected abstract void handleUnfollowUser(Long userId);

    /**
     * {@inheritDoc}
     * 
     * @see com.communote.server.core.follow.FollowManagement#unfollowBlog(Long)
     */
    @Override
    public void unfollowBlog(Long blogId) {
        if (blogId == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.service.follow.FollowManagement.unfollowBlog"
                            + "(Long blogId) - 'blogId' can not be null");
        }
        try {
            this.handleUnfollowBlog(blogId);
        } catch (RuntimeException rt) {
            throw new com.communote.server.core.follow.FollowManagementException(
                    "Error performing 'com.communote.server.service.follow.FollowManagement."
                            + "unfollowBlog(Long blogId)' --> " + rt, rt);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.communote.server.core.follow.FollowManagement#unfollowDiscussion(Long)
     */
    @Override
    public void unfollowDiscussion(Long discussionId) {
        if (discussionId == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.service.follow.FollowManagement.unfollowDiscussion"
                            + "(Long discussionId) - 'discussionId' can not be null");
        }
        try {
            this.handleUnfollowDiscussion(discussionId);
        } catch (RuntimeException rt) {
            throw new com.communote.server.core.follow.FollowManagementException(
                    "Error performing 'com.communote.server.service.follow.FollowManagement."
                            + "unfollowDiscussion(Long discussionId)' --> " + rt, rt);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.communote.server.core.follow.FollowManagement#unfollowDiscussionByNoteId(Long)
     */
    @Override
    public void unfollowDiscussionByNoteId(Long noteId) {
        if (noteId == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.service.follow.FollowManagement.unfollowDiscussionByNoteId"
                            + "(Long noteId) - 'noteId' can not be null");
        }
        try {
            this.handleUnfollowDiscussionByNoteId(noteId);
        } catch (RuntimeException rt) {
            throw new com.communote.server.core.follow.FollowManagementException(
                    "Error performing 'com.communote.server.service.follow.FollowManagement."
                            + "unfollowDiscussionByNoteId(Long noteId)' --> " + rt, rt);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.communote.server.core.follow.FollowManagement#unfollowTag(Long)
     */
    @Override
    public void unfollowTag(Long tagId) {
        if (tagId == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.service.follow.FollowManagement.unfollowTag(Long tagId) "
                            + "- 'tagId' can not be null");
        }
        try {
            this.handleUnfollowTag(tagId);
        } catch (RuntimeException rt) {
            throw new com.communote.server.core.follow.FollowManagementException(
                    "Error performing 'com.communote.server.service.follow.FollowManagement.unfollowTag"
                            + "(Long tagId)' --> " + rt, rt);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.communote.server.core.follow.FollowManagement#unfollowUser(Long)
     */
    @Override
    public void unfollowUser(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.service.follow.FollowManagement.unfollowUser(Long userId)"
                            + " - 'userId' can not be null");
        }
        try {
            this.handleUnfollowUser(userId);
        } catch (RuntimeException rt) {
            throw new com.communote.server.core.follow.FollowManagementException(
                    "Error performing 'com.communote.server.service.follow.FollowManagement.unfollowUser"
                            + "(Long userId)' --> " + rt, rt);
        }
    }

}