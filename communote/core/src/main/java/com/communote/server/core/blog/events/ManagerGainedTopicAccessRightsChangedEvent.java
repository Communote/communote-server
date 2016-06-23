package com.communote.server.core.blog.events;

import com.communote.server.model.blog.BlogRole;

/**
 * Event donating that a manager granted themself management access to a topic.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ManagerGainedTopicAccessRightsChangedEvent extends TopicAccessRightsChangedEvent {

    private static final long serialVersionUID = 8189847859050604392L;

    /**
     * Constructor.
     * 
     * @param topicId
     *            The topics id
     * @param grantingUserId
     *            The users id.
     */
    public ManagerGainedTopicAccessRightsChangedEvent(long topicId, String topicTitle,
            long grantingUserId) {
        super(topicId, topicTitle, grantingUserId, null, BlogRole.MANAGER);
    }

}
