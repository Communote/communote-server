package com.communote.server.core.blog.events;

import com.communote.server.model.blog.BlogRole;

/**
 * Event that is fired when topic access rights of entities (users or groups) have changed.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class EntityTopicAccessRightsChangedEvent extends TopicAccessRightsChangedEvent {

    private static final long serialVersionUID = 7093590136810921161L;
    private final long grantedUserId;
    private final boolean isGroup;

    /**
     * Constructor.
     * 
     * @param topicId
     *            ID of the topic, where the rights were changed.
     * @param topicTitle
     *            Title of the topic, where the rights were changed.
     * @param grantingUserId
     *            Id of the user who changed the rights.
     * @param grantedEntityId
     *            Id of the entity whom the right was granted to.
     * @param isGroup
     *            Set to true if grantedEntityId is a group, else false.
     * @param oldRole
     *            The previous role. Might be null if none.
     * @param newRole
     *            Role which was set, a value of "null" means, that all rights where removed.
     */
    public EntityTopicAccessRightsChangedEvent(long topicId, String topicTitle,
            long grantingUserId,
            long grantedEntityId, boolean isGroup, BlogRole oldRole, BlogRole newRole) {
        super(topicId, topicTitle, grantingUserId, oldRole, newRole);
        this.grantedUserId = grantedEntityId;
        this.isGroup = isGroup;
    }

    /**
     * @return the grantedUserId
     */
    public long getGrantedEntityId() {
        return grantedUserId;
    }

    /**
     * @return the isGroup
     */
    public boolean isGroup() {
        return isGroup;
    }

}
