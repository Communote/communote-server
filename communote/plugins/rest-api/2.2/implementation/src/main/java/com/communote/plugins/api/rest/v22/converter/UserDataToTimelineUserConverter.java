package com.communote.plugins.api.rest.v22.converter;

import com.communote.plugins.api.rest.v22.resource.timelineuser.TimelineUserResource;
import com.communote.server.api.core.user.UserData;
import com.communote.server.core.vo.query.user.DataAccessUserConverter;
import com.communote.server.model.user.UserStatus;

/**
 * UserDataToTimelineUserConverter to convert the temporary object into a TimelineUserResource
 * 
 * @param <T>
 *            The {@link UserData} which is the incoming list
 * @param <O>
 *            The {@link TimelineUserResource} which is the final list
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class UserDataToTimelineUserConverter<T extends UserData, O extends TimelineUserResource>
        extends DataAccessUserConverter<T, O> {

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean convert(T source, O target) {
        fillTimelineUserResource(source, target);
        return true;
    }

    /**
     * @return new {@link TimelineUserResource}
     */
    @SuppressWarnings("unchecked")
    @Override
    public O create() {
        return (O) new TimelineUserResource();
    }

    /**
     * Fills a {@link TimelineUserResource} from a {@link UserData}.
     * 
     * @param userListItem
     *            the user to convert
     * @param timelineUserResource
     *            the filled item
     */
    protected void fillTimelineUserResource(T userListItem, O timelineUserResource) {
        if (userListItem.getStatus().equals(UserStatus.PERMANENTLY_DISABLED)
                || userListItem.getStatus().equals(UserStatus.DELETED)) {
            timelineUserResource.setAlias(null);
        } else {
            timelineUserResource.setAlias(userListItem.getAlias());
        }
        timelineUserResource.setFirstName(userListItem.getFirstName());
        timelineUserResource.setLastName(userListItem.getLastName());
        timelineUserResource.setUserId(userListItem.getId());
    }

}
