package com.communote.server.core.blog.events;

import com.communote.server.model.blog.BlogRole;

/**
 * Event defining that the rights for all users changed.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class AllUsersTopicAccessRightsChangedEvent extends TopicAccessRightsChangedEvent {

    private static final long serialVersionUID = 7636360267036081014L;

    /**
     * Constructor.
     * 
     * @param topicId
     *            ID of the topic, where the rights were changed
     * @param topicTitle
     *            Title of the topic, where the rights were changed
     * @param grantingUserId
     *            Id of the user who changed the rights.
     * @param oldRole
     *            The previous role. Might be null if none.
     * @param newRole
     *            Role which was set, a value of "null" means, that all rights where removed.
     */
    public AllUsersTopicAccessRightsChangedEvent(long topicId, String topicTitle,
            long grantingUserId,
            BlogRole oldRole, BlogRole newRole) {
        super(topicId, topicTitle, grantingUserId, oldRole, newRole);
    }

}
