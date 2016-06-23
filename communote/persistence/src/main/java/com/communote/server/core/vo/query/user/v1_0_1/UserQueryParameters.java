package com.communote.server.core.vo.query.user.v1_0_1;

import com.communote.server.api.core.user.DetailedUserData;
import com.communote.server.model.user.User;
import com.communote.server.model.user.UserProfile;


/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class UserQueryParameters extends
        com.communote.server.core.vo.query.user.UserQueryParameters {

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean needTransformListItem() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DetailedUserData transformResultItem(Object resultItem) {
        User user = (User) resultItem;
        UserProfile profile = user.getProfile();

        DetailedUserData item = new DetailedUserData();
        item.setAlias(user.getAlias());
        // Don't expose email address.
        item.setEmail(null);
        item.setFirstName(profile.getFirstName());
        item.setLastName(profile.getLastName());
        item.setLastModificationDate(profile.getLastModificationDate());
        item.setLastPhotoModificationDate(profile.getLastPhotoModificationDate());
        item.setSalutation(profile.getSalutation());
        item.setId(user.getId());
        return item;
    }
}
