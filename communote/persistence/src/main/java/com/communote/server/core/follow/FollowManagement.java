package com.communote.server.core.follow;

import java.util.List;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.communote.server.api.core.blog.BlogAccessException;
import com.communote.server.api.core.common.NotFoundException;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.core.tag.TagNotFoundException;

/**
 * Service class providing follow functionality.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
@Transactional(propagation = Propagation.REQUIRED)
public interface FollowManagement {

    /**
     * Adds the specified blog/topic to the followed items of the current user.
     * 
     * @param blogId
     *            the ID of the topic to follow
     * @throws NotFoundException
     *             in case the topic does not exist
     * @throws BlogAccessException
     *             in case the current user has no read access to the topic
     */
    public void followBlog(Long blogId) throws NotFoundException, BlogAccessException;

    /**
     * Adds the discussion with the specified ID to the followed items of the current user.
     * 
     * @param discussionId
     *            the ID of the discussion to follow
     * @throws NotFoundException
     *             in case the discussion does not exist
     * @throws BlogAccessException
     *             in case the current user has no read access to the topic of the discussion or the
     *             discussion
     */
    public void followDiscussion(Long discussionId) throws BlogAccessException, NotFoundException;

    /**
     * Adds the discussion of the specified note to the followed items of the current user.
     * 
     * @param noteId
     *            the ID of the note whose discussion should be followed
     * @throws NotFoundException
     *             in case the discussion does not exist
     * @throws BlogAccessException
     *             in case the current user has no read access to the topic of the discussion or the
     *             discussion
     */
    public void followDiscussionByNoteId(Long noteId) throws NotFoundException, BlogAccessException;

    /**
     * Returns whether the current user follows the blog.
     * 
     * 
     * @param blogId
     *            the ID of the blog
     * @return true if the blog exists and the current user follows that blog, false otherwise
     */
    @Transactional(readOnly = true)
    public boolean followsBlog(Long blogId);

    /**
     * Returns true if the current user follows the discussion, otherwise false.
     * 
     * @param discussionId
     *            the ID of the discussion
     * @return true if the discussion exists and the current user follows the discussion, otherwise
     *         false
     */
    @Transactional(readOnly = true)
    public boolean followsDiscussion(Long discussionId);

    /**
     * Returns true if the current user follows the tag, otherwise false.
     * 
     * @param id
     *            The ID of the tag.
     * @return True if the current user follows the given tag.
     */
    @Transactional(readOnly = true)
    public boolean followsTag(Long id);

    /**
     * Returns whether the current user follows the user.
     * 
     * 
     * @param userId
     *            the ID of the user
     * @return true if the user exists and the current user follows that user, otherwise false
     */
    @Transactional(readOnly = true)
    public boolean followsUser(Long userId);

    /**
     * Adds the specified tag to the followed items of the current user.
     * 
     * @param tagId
     *            the ID of the tag to follow
     * @throws NotFoundException
     *             in case the tag does not exist
     */
    public void followTag(Long tagId) throws NotFoundException;

    /**
     * Adds the specified user to the followed items of the current user.
     * 
     * @param userId
     *            ID of the user to follow
     * @throws NotFoundException
     *             in case the user does not exist
     */
    public void followUser(Long userId) throws NotFoundException;

    /**
     * Method to be called after a tag has been removed or merged with another tag. In case of a
     * merge the provided followers of the old tag will be added as followers to the new tag.
     * 
     * @param removedTagId
     *            the ID of the removed tag
     * @param newTagId
     *            the ID of the tag the old tag was merged into, can be null
     * @param followers
     *            the IDs of the users that followed the old tag
     * @throws AuthorizationException
     *             in case the calling user is not the internal system user
     * @throws TagNotFoundException
     *             in case newTagId is not null but there is no tag for that ID
     */
    void tagRemoved(Long removedTagId, Long newTagId, List<Long> followers)
            throws AuthorizationException, TagNotFoundException;

    /**
     * Removes the specified blog/topic from the followed items of the current user. If the item is
     * not followed nothing will happen.
     * 
     * @param blogId
     *            the ID of the topic to unfollow
     */
    public void unfollowBlog(Long blogId);

    /**
     * Removes the discussion with the specified ID from the followed items of the current user. If
     * the item is not followed nothing will happen.
     * 
     * @param discussionId
     *            the ID of the discussion to unfollow
     */
    public void unfollowDiscussion(Long discussionId);

    /**
     * Removes the discussion of the specified note from the followed items of the current user. If
     * the item is not followed nothing will happen.
     * 
     * @param noteId
     *            the ID of the note whose discussion should be unfollowed
     */
    public void unfollowDiscussionByNoteId(Long noteId);

    /**
     * 
     * Removes the specified tag from the followed items of the current user. If the item is not
     * followed nothing will happen. When unfollowing a tag all tags with the same lower case
     * writing will be unfollowed too.
     * 
     * @param tagId
     *            the ID of the tag to unfollow
     */
    public void unfollowTag(Long tagId);

    /**
     * 
     * Removes the specified user from the followed items of the current user. If the item is not
     * followed nothing will happen.
     * 
     * @param userId
     *            ID of the user to follow
     */
    public void unfollowUser(Long userId);

}
