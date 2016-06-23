package com.communote.server.web.commons.viewtool;

import org.apache.velocity.tools.Scope;
import org.apache.velocity.tools.config.DefaultKey;
import org.apache.velocity.tools.config.ValidScope;

import com.communote.server.api.ServiceLocator;
import com.communote.server.core.follow.FollowManagement;

/**
 * Tools for following.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
@DefaultKey("followTool")
@ValidScope(Scope.REQUEST)
public class FollowTool extends RequestAwareTool {

    /**
     * @param blogId
     *            The blog id.
     * @return True, if the current user is following the given blog.
     */
    public boolean followsBlog(Long blogId) {
        return ServiceLocator.instance().getService(FollowManagement.class).followsBlog(blogId);
    }

    /**
     * @param tag
     *            The tag .
     * @return True, if the current user is following the given tag.
     */
    public boolean followsTag(Long tag) {
        return ServiceLocator.instance().getService(FollowManagement.class).followsTag(tag);
    }

    /**
     * @param userId
     *            The user id.
     * @return True, if the current user is following the given user.
     */
    public boolean followsUser(Long userId) {
        return ServiceLocator.instance().getService(FollowManagement.class).followsUser(userId);
    }
}
