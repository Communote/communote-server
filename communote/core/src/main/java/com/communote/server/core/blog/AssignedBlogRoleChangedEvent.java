package com.communote.server.core.blog;

import com.communote.server.api.core.event.Event;

/**
 * Event to notify about a change of a blog role of a user. This covers all roles that have been
 * assigned to the user either directly or indirectly through group membership. The reason for the
 * event can be that the role was added, removed or modified.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class AssignedBlogRoleChangedEvent implements Event {
    private final Long blogId;
    private final Long userId;

    /**
     * Creates a new event.
     * 
     * @param blogId
     *            the ID of the blog
     * @param userId
     *            the ID of the user
     */
    public AssignedBlogRoleChangedEvent(Long blogId, Long userId) {
        this.blogId = blogId;
        this.userId = userId;
    }

    /**
     * @return the ID of the blog
     */
    public Long getBlogId() {
        return this.blogId;
    }

    /**
     * @return the ID of the user
     */
    public Long getUserId() {
        return this.userId;
    }
}
