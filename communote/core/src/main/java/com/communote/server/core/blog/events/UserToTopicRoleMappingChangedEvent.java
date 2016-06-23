package com.communote.server.core.blog.events;

import com.communote.server.api.core.event.Event;
import com.communote.server.model.blog.BlogRole;

/**
 * Event indicating a change of a UserToTopicRoleMapping. This does not necessarily mean, that the
 * final topic role change for the particular user.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class UserToTopicRoleMappingChangedEvent implements Event {

    private static final long serialVersionUID = 284674859483227717L;

    private final Long topicId;
    private final Long entityId;
    private final boolean isGroup;
    private final Long grantingGroupId;
    private final BlogRole oldRoleOfMapping;
    private final BlogRole newRoleOfMapping;
    private final BlogRole beforeTopicRole;

    public UserToTopicRoleMappingChangedEvent(
            Long topicId,
            Long entityId,
            boolean isGroup,
            BlogRole oldRoleOfMapping,
            BlogRole newRoleOfMapping,
            BlogRole beforeTopicRole) {
        this(topicId,
                entityId,
                isGroup,
                null,
                oldRoleOfMapping,
                newRoleOfMapping,
                beforeTopicRole);

    }

    public UserToTopicRoleMappingChangedEvent(
            Long topicId,
            Long entityId,
            boolean isGroup,
            Long grantingGroupId,
            BlogRole oldRoleOfMapping,
            BlogRole newRoleOfMapping,
            BlogRole beforeTopicRole) {

        this.topicId = topicId;
        this.entityId = entityId;
        this.isGroup = isGroup;
        this.grantingGroupId = grantingGroupId;
        this.oldRoleOfMapping = oldRoleOfMapping;
        this.newRoleOfMapping = newRoleOfMapping;
        this.beforeTopicRole = beforeTopicRole;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        UserToTopicRoleMappingChangedEvent other = (UserToTopicRoleMappingChangedEvent) obj;
        if (beforeTopicRole == null) {
            if (other.beforeTopicRole != null) {
                return false;
            }
        } else if (!beforeTopicRole.equals(other.beforeTopicRole)) {
            return false;
        }
        if (entityId == null) {
            if (other.entityId != null) {
                return false;
            }
        } else if (!entityId.equals(other.entityId)) {
            return false;
        }
        if (isGroup != other.isGroup) {
            return false;
        }
        if (newRoleOfMapping == null) {
            if (other.newRoleOfMapping != null) {
                return false;
            }
        } else if (!newRoleOfMapping.equals(other.newRoleOfMapping)) {
            return false;
        }
        if (oldRoleOfMapping == null) {
            if (other.oldRoleOfMapping != null) {
                return false;
            }
        } else if (!oldRoleOfMapping.equals(other.oldRoleOfMapping)) {
            return false;
        }
        if (topicId == null) {
            if (other.topicId != null) {
                return false;
            }
        } else if (!topicId.equals(other.topicId)) {
            return false;
        }
        return true;
    }

    public BlogRole getBeforeTopicRole() {
        return beforeTopicRole;
    }

    public Long getEntityId() {
        return entityId;
    }

    public Long getGrantingGroupId() {
        return grantingGroupId;
    }

    public BlogRole getNewRoleOfMapping() {
        return newRoleOfMapping;
    }

    public BlogRole getOldRoleOfMapping() {
        return oldRoleOfMapping;
    }

    public Long getTopicId() {
        return topicId;
    }

    public boolean isGroup() {
        return isGroup;
    }

}
