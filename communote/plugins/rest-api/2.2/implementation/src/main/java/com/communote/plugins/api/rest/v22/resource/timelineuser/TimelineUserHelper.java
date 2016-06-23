package com.communote.plugins.api.rest.v22.resource.timelineuser;

import com.communote.server.api.core.user.UserData;
import com.communote.server.model.user.UserStatus;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public final class TimelineUserHelper {

    /**
     * Build the {@link TimelineUserResource} from the {@link UserData}
     * 
     * @param userListItem
     *            item of user
     * @return {@link TimelineUserResource}
     */
    public static TimelineUserResource buildTimelineUserResource(UserData userListItem) {
        return buildTimelineUserResource(userListItem, new TimelineUserResource());
    }

    /**
     * Build the {@link TimelineUserResource} from the {@link UserData}
     * 
     * @param userListItem
     *            item of user
     * @param timelineUserResource
     *            resource of timeline user
     * @return {@link TimelineUserResource}
     */
    public static TimelineUserResource buildTimelineUserResource(UserData userListItem,
            TimelineUserResource timelineUserResource) {
        if (userListItem.getStatus().equals(UserStatus.PERMANENTLY_DISABLED)
                || userListItem.getStatus().equals(UserStatus.DELETED)) {
            timelineUserResource.setAlias(null);
        } else {
            timelineUserResource.setAlias(userListItem.getAlias());
        }
        timelineUserResource.setFirstName(userListItem.getFirstName());
        timelineUserResource.setLastName(userListItem.getLastName());
        timelineUserResource.setUserId(userListItem.getId());
        return timelineUserResource;
    }

    /**
     * Default constructor
     */
    private TimelineUserHelper() {

    }
}
