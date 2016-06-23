package com.communote.server.core.blog.events;

import com.communote.server.api.core.event.Event;
import com.communote.server.model.blog.BlogRole;

/**
 * Abstract event for access right changes.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class TopicAccessRightsChangedEvent implements Event {

    private static final long serialVersionUID = 284674859483227717L;

    private final long topicId;
    private final String topicTitle;
    private final long grantingUserId;
    private final BlogRole role;
    private final BlogRole oldRole;

    /**
     * Constructor.
     * 
     * @param topicId
     *            ID of the topic, where the rights were changed.
     * @param topicTitle
     *            Title of the topic, where the rights were changed.
     * @param grantingUserId
     *            ID of the user who changed the rights.
     * @param oldRole
     *            The previous role. Might be null if none.
     * @param newRole
     *            Role which was set, a value of "null" means, that all rights where removed.
     */
    public TopicAccessRightsChangedEvent(long topicId, String topicTitle, long grantingUserId,
            BlogRole oldRole,
            BlogRole newRole) {
        this.topicId = topicId;
        this.topicTitle = topicTitle;
        this.grantingUserId = grantingUserId;
        this.oldRole = oldRole;
        this.role = newRole;
    }

    /**
     * @return ID of the user who changed the rights.
     */
    public long getGrantingUserId() {
        return grantingUserId;
    }

    /**
     * @return The role or null if access was revoked.
     */
    public BlogRole getNewRole() {
        return role;
    }

    /**
     * @return the oldRole
     */
    public BlogRole getOldRole() {
        return oldRole;
    }

    /**
     * @return the ID of the topic, where the rights were changed
     */
    public long getTopicId() {
        return topicId;
    }

    /**
     * @return the title of the topic, where the rights were changed
     */
    public String getTopicTitle() {
        return topicTitle;
    }
}
