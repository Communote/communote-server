package com.communote.plugins.api.rest.v30.converter;

import com.communote.common.converter.Converter;
import com.communote.plugins.api.rest.v30.resource.timelineuser.TimelineUserResource;
import com.communote.server.model.user.User;
import com.communote.server.model.user.UserStatus;


/**
 * Convert a user entity into a resource.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class UserToTimelineUserConverter implements
        Converter<User, TimelineUserResource> {
    @Override
    public TimelineUserResource convert(User source) {
        TimelineUserResource target = new TimelineUserResource();
        if (!source.getStatus().equals(UserStatus.DELETED)) {
            target.setAlias(source.getAlias());
        }
        target.setFirstName(source.getProfile().getFirstName());
        target.setLastName(source.getProfile().getLastName());
        target.setUserId(source.getId());
        return target;
    }
}
