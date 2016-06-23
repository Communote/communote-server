package com.communote.server.core.user;

import org.springframework.security.access.AccessDeniedException;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.blog.BlogRightsManagement;
import com.communote.server.core.security.UserDetails;
import com.communote.server.core.security.SecurityHelper;

/**
 * Helper class for user security
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public final class UserSecurityHelper {

    /**
     * Assert that the current user is client manager. Throws an {@link AccessDeniedException} if it
     * is not the manager of the client.
     */
    public static void assertIsClientManager() {
        if (!SecurityHelper.isClientManager()) {
            throw new AccessDeniedException(
                    "Current user is not allowed to access the administration area of the client. userId="
                            + SecurityHelper.getCurrentUserId());
        }
    }

    /**
     * Assert that the current user is manager of a blog. Throws an {@link AccessDeniedException} if
     * it is not the manager
     *
     * @param blogId
     *            the id of the blog to check
     */
    public static void assertIsManagerOfBlog(Long blogId) {
        if (!isManagerOfBlog(blogId)) {
            throw new AccessDeniedException(
                    "Current user is not allowed to access management for blogId=" + blogId);
        }
    }

    /**
     * @param blogId
     *            the id of the blog
     * @return Returns <code>true</code> if the current user is a manger of the group
     */
    public static boolean isManagerOfBlog(Long blogId) {
        UserDetails kud = SecurityHelper.getCurrentUser();
        if (ServiceLocator.findService(BlogRightsManagement.class).userHasManagementAccess(blogId,
                kud.getUserId())) {
            return true;
        }
        return false;
    }

    /**
     * protected constructor
     */
    protected UserSecurityHelper() {

    }

}
