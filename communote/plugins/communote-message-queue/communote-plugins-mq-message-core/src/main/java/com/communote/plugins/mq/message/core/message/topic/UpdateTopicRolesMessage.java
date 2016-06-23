package com.communote.plugins.mq.message.core.message.topic;

import com.communote.plugins.mq.message.core.data.role.ExternalTopicRole;
import com.communote.plugins.mq.message.core.data.topic.BaseTopic;

/**
 * update topic role message
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class UpdateTopicRolesMessage extends TopicSpecificMessage<BaseTopic> {

    private ExternalTopicRole[] roles;

    /**
     * @return the topicRole
     */
    public ExternalTopicRole[] getRoles() {
        return roles;
    }

    /**
     * @param topicRole
     *            the topicRole to set
     */
    public void setRoles(ExternalTopicRole[] topicRole) {
        this.roles = topicRole;
    }

}
