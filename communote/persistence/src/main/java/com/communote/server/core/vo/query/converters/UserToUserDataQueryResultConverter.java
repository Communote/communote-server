package com.communote.server.core.vo.query.converters;

import com.communote.server.api.core.user.DetailedUserData;
import com.communote.server.core.user.UserManagementHelper;
import com.communote.server.model.user.User;

/**
 * Converts a user entity into a value object
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class UserToUserDataQueryResultConverter extends
DirectQueryResultConverter<User, DetailedUserData> {

    @Override
    public boolean convert(User source, DetailedUserData target) {
        target.setId(source.getId());
        target.setAlias(source.getAlias());
        target.setEmail(source.getEmail());
        target.setFirstName(source.getProfile().getFirstName());
        target.setLastName(source.getProfile().getLastName());
        target.setSalutation(source.getProfile().getSalutation());
        target.setLastModificationDate(source.getProfile().getLastModificationDate());
        target.setLastPhotoModificationDate(source.getProfile().getLastPhotoModificationDate());
        target.setEffectiveUserTimeZone(UserManagementHelper.getEffectiveUserTimeZone(source
                .getId()));
        target.setStatus(source.getStatus());
        return true;
    }

    @Override
    public DetailedUserData create() {
        return new DetailedUserData();
    }
}
