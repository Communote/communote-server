package com.communote.plugins.mq.message.core.message.topic;

import com.communote.plugins.mq.message.core.data.role.ExternalTopicRole;
import com.communote.plugins.mq.message.core.data.topic.BaseTopic;

/**
 * Set topic role message
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class SetTopicRolesMessage extends TopicSpecificMessage<BaseTopic> {

    private ExternalTopicRole[] roles;

    /**
     * @return the roles
     */
    public ExternalTopicRole[] getRoles() {
        return roles;
    }

    /**
     * @param roles
     *            the roles to set
     */
    public void setRoles(ExternalTopicRole[] roles) {
        this.roles = roles;
    }

}
