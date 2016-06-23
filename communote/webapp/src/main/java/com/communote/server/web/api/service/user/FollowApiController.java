/**
 *
 */
package com.communote.server.web.api.service.user;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.ServletRequestUtils;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.blog.BlogAccessException;
import com.communote.server.api.core.blog.BlogManagement;
import com.communote.server.api.core.blog.BlogNotFoundException;
import com.communote.server.api.core.common.NotFoundException;
import com.communote.server.core.blog.NoteNotFoundException;
import com.communote.server.core.blog.helper.BlogManagementHelper;
import com.communote.server.core.follow.FollowManagement;
import com.communote.server.core.tag.TagManagement;
import com.communote.server.core.tag.TagNotFoundException;
import com.communote.server.core.user.UserProfileDetails;
import com.communote.server.core.user.UserProfileManagement;
import com.communote.server.core.user.helper.UserNameHelper;
import com.communote.server.model.blog.Blog;
import com.communote.server.web.api.service.ApiException;
import com.communote.server.web.api.service.BaseRestApiController;
import com.communote.server.web.api.service.IllegalRequestParameterException;
import com.communote.server.web.api.service.MissingRequestParameterException;
import com.communote.server.web.api.to.ApiResult;
import com.communote.server.web.commons.MessageHelper;

/**
 * API controller for following and unfollowing users, discussions, blogs and tags.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * @deprecated Use new generated REST-API instead.
 */
@Deprecated
public class FollowApiController extends BaseRestApiController {

    private static final String PARAM_BLOG_ID = "blogId";
    private static final String PARAM_USER_ID = "userId";
    private static final String PARAM_NOTE_ID = "noteId";
    private static final String PARAM_DISCUSSION_ID = "discussionId";
    private static final String PARAM_TAG = "tag";
    private FollowManagement followManagement;

    /**
     * Creates a localized message string describing the success of the follow/unfollow operation
     * called on a blog.
     *
     * @param request
     *            the current request
     * @param blogId
     *            the ID of the blog that was followed/unfollowed
     * @param isFollow
     *            whether it was the follow or unfollow operation
     * @return the message
     * @throws BlogAccessException
     * @throws BlogNotFoundException
     */
    private String createFollowUnfollowBlogSuccessMessage(HttpServletRequest request, Long blogId,
            boolean isFollow) {
        // TODO there should be some caching
        Blog blog;
        try {
            blog = ServiceLocator.instance().getService(BlogManagement.class)
                    .getBlogById(blogId, false);
        } catch (BlogNotFoundException e) {
            throw BlogManagementHelper.convertException(e);
        } catch (BlogAccessException e) {
            throw BlogManagementHelper.convertException(e);
        }
        String title = blog.getTitle();
        String messageKey = isFollow ? "follow.follow.blog.success"
                : "follow.unfollow.blog.success";
        return MessageHelper.getText(request, messageKey, new String[] { title });
    }

    /**
     * Creates a localized message string describing the success of the follow/unfollow operation
     * called on a user.
     *
     * @param request
     *            the current request
     * @param userId
     *            the ID of the user that was followed/unfollowed
     * @param isFollow
     *            whether it was the follow or unfollow operation
     * @return the message
     */
    private String createFollowUnfollowUserSuccessMessage(HttpServletRequest request, Long userId,
            boolean isFollow) {
        UserProfileDetails details = ServiceLocator.findService(UserProfileManagement.class)
                .getUserProfileDetailsById(userId, false);
        String displayName = UserNameHelper.getSimpleDefaultUserSignature(details);
        String messageKey = isFollow ? "follow.follow.user.success"
                : "follow.unfollow.user.success";
        return MessageHelper.getText(request, messageKey,
                new String[] { displayName });
    }

    /**
     * Does the follow logic. This method will only handle one of the parameters (blogId, noteId,
     * discussionId, tag, userId) and will take the first one that is not null.
     *
     * @param apiResult
     *            the API result object to use for storing feedback information
     * @param request
     *            the current request
     * @param blogId
     *            the ID of the blog to follow
     * @param noteId
     *            the ID of the note of the discussion to follow
     * @param discussionId
     *            the ID of the discussion to follow
     * @param tagId
     *            the tag to follow
     * @param userId
     *            the ID of the user to follow
     */
    private void doFollow(ApiResult apiResult, HttpServletRequest request, Long blogId,
            Long noteId, Long discussionId, Long tagId, Long userId) {
        // only handle first encountered
        String message = null;
        try {
            if (blogId != -1) {
                getFollowManagement().followBlog(blogId);
                message = createFollowUnfollowBlogSuccessMessage(request, blogId, true);
            } else if (noteId != -1) {
                getFollowManagement().followDiscussionByNoteId(noteId);
                message = MessageHelper.getText(request, "follow.follow.discussion.success");
            } else if (discussionId != -1) {
                getFollowManagement().followDiscussion(discussionId);
                message = MessageHelper.getText(request, "follow.follow.discussion.success");
            } else if (tagId != -1) {
                getFollowManagement().followTag(tagId);
                message = MessageHelper.getText(request, "follow.follow.tag.success",
                        new String[] { ServiceLocator.instance().getService(TagManagement.class)
                        .findTag(tagId).getDefaultName() });
            } else {
                getFollowManagement().followUser(userId);
                message = createFollowUnfollowUserSuccessMessage(request, userId, true);
            }
        } catch (NotFoundException e) {
            apiResult.setStatus(ApiResult.ResultStatus.ERROR.name());
            if (e instanceof BlogNotFoundException) {
                message = MessageHelper.getText(request, "follow.follow.error.not.found.blog");
            } else if (e instanceof NoteNotFoundException) {
                message = MessageHelper.getText(request, "follow.follow.error.not.found.note");
            } else if (e instanceof TagNotFoundException) {
                message = MessageHelper.getText(request, "follow.follow.error.not.found.tag");
            } else {
                message = MessageHelper.getText(request, "follow.follow.error.not.found.user");
            }
        } catch (BlogAccessException e) {
            apiResult.setStatus(ApiResult.ResultStatus.ERROR.name());
        }
        apiResult.setMessage(message);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Object doPost(ApiResult apiResult, HttpServletRequest request,
            HttpServletResponse response) throws HttpRequestMethodNotSupportedException,
            ApiException {
        boolean isFollowAction = isFollowAction(request);
        Long blogId = ServletRequestUtils.getLongParameter(request, PARAM_BLOG_ID, -1);
        Long userId = ServletRequestUtils.getLongParameter(request, PARAM_USER_ID, -1);
        Long noteId = ServletRequestUtils.getLongParameter(request, PARAM_NOTE_ID, -1);
        Long discussionId = ServletRequestUtils.getLongParameter(request, PARAM_DISCUSSION_ID, -1);
        Long tag = ServletRequestUtils.getLongParameter(request, PARAM_TAG, -1);
        if (blogId == -1 && noteId == -1 && discussionId == -1 && tag == -1 && userId == -1) {
            throw new MissingRequestParameterException(new String[] { PARAM_BLOG_ID, PARAM_NOTE_ID,
                    PARAM_DISCUSSION_ID, PARAM_TAG, PARAM_USER_ID }, null);
        }
        if (isFollowAction) {
            doFollow(apiResult, request, blogId, noteId, discussionId, tag, userId);
        } else {
            doUnfollow(apiResult, request, blogId, noteId, discussionId, tag, userId);
        }
        return null;
    }

    /**
     * Does the unfollow logic. This method will only handle one of the parameters (blogId, noteId,
     * discussionId, tag, userId) and will take the first one that is not null.
     *
     * @param apiResult
     *            the API result object to use for storing feedback information
     * @param request
     *            the current request
     * @param blogId
     *            the ID of the blog to unfollow
     * @param noteId
     *            the ID of the note of the discussion to unfollow
     * @param discussionId
     *            the ID of the discussion to follow
     * @param tagId
     *            the tag to unfollow
     * @param userId
     *            the ID of the user to unfollow
     */
    private void doUnfollow(ApiResult apiResult, HttpServletRequest request, Long blogId,
            Long noteId, Long discussionId, Long tagId, Long userId) {
        // only handle first encountered
        String message = null;
        if (blogId != -1) {
            getFollowManagement().unfollowBlog(blogId);
            message = createFollowUnfollowBlogSuccessMessage(request, blogId, false);
        } else if (noteId != -1) {
            getFollowManagement().unfollowDiscussionByNoteId(noteId);
            message = MessageHelper.getText(request, "follow.unfollow.discussion.success");
        } else if (discussionId != -1) {
            getFollowManagement().unfollowDiscussion(discussionId);
            message = MessageHelper.getText(request, "follow.unfollow.discussion.success");
        } else if (tagId != -1) {
            getFollowManagement().unfollowTag(tagId);
            message = MessageHelper.getText(request, "follow.unfollow.tag.success",
                    new String[] { ServiceLocator.instance().getService(TagManagement.class)
                    .findTag(tagId).getDefaultName() });
        } else {
            getFollowManagement().unfollowUser(userId);
            message = createFollowUnfollowUserSuccessMessage(request, userId, false);
        }
        apiResult.setMessage(message);
    }

    /**
     * @return the FollowManagement service
     */
    private FollowManagement getFollowManagement() {
        if (followManagement == null) {
            followManagement = ServiceLocator.instance().getService(FollowManagement.class);
        }
        return followManagement;
    }

    /**
     * Tests whether the action of the request is follow or unfollow.
     *
     * @param request
     *            the request
     * @return true if the action is follow, false otherwise
     * @throws IllegalRequestParameterException
     *             if the action parameter is not set or set to an unsupported value
     */
    private boolean isFollowAction(HttpServletRequest request)
            throws IllegalRequestParameterException {
        String action = getNonEmptyParameter(request, "action");
        if (action.equals("follow")) {
            return true;
        }
        if (action.equals("unfollow")) {
            return false;
        }
        throw new IllegalRequestParameterException("action", action,
                "Invalid value. Allowed values are: follow|unfollow");
    }
}
