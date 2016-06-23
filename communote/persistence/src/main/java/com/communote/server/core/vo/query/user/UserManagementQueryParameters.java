package com.communote.server.core.vo.query.user;

import com.communote.server.api.core.common.IdentifiableEntityData;
import com.communote.server.core.filter.listitems.UserManagementListItem;
import com.communote.server.model.user.User;
import com.communote.server.model.user.UserProfile;


/**
 * The Class UserManagementQueryInstance.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class UserManagementQueryParameters extends UserQueryParameters {

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
    public IdentifiableEntityData transformResultItem(Object resultItem) {
        User user = (User) resultItem;
        UserManagementListItem transformedItem = new UserManagementListItem();
        transformedItem.setId(user.getId());
        transformedItem.setEmail(user.getEmail());
        transformedItem.setRoles(user.getRoles());
        transformedItem.setStatus(user.getStatus());
        transformedItem.setAlias(user.getAlias());
        UserProfile profile = user.getProfile();
        if (profile != null) {
            transformedItem.setFirstName(profile.getFirstName());
            transformedItem.setLastName(profile.getLastName());
        }
        return transformedItem;

    }

}
