package com.communote.plugins.api.rest.v30.resource.group;

import com.communote.common.converter.Converter;
import com.communote.server.api.ServiceLocator;
import com.communote.server.core.user.UserGroupManagement;
import com.communote.server.core.user.group.GroupNotFoundException;
import com.communote.server.model.user.group.Group;

/**
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public final class GroupResourceHelper {

    public static <T> T getGroup(
            String genericGroupId,
            boolean isAlias,
            Converter<Group, T> converter) throws GroupNotFoundException {
        T convertedGroup = null;
        UserGroupManagement userGroupManagement = ServiceLocator
                .findService(UserGroupManagement.class);

        Exception caught = null;

        try {
            if (isAlias) {
                convertedGroup = userGroupManagement.findGroupByAlias(genericGroupId,
                        converter);
            } else {
                Long groupId = Long.parseLong(genericGroupId);
                convertedGroup = userGroupManagement.findGroupById(groupId, converter);
            }
        } catch (ClassCastException e) {
            // indicates the id belongs to a user not a group
            caught = e;
        }

        if (convertedGroup == null) {
            GroupNotFoundException exception = new GroupNotFoundException(
                    "No group found for identifier="
                            + genericGroupId + " isAlias=" + isAlias, caught);
            exception.setOccuredDuringChangingBlogRights(false);
            throw exception;
        }
        return convertedGroup;
    }

    private GroupResourceHelper() {
        // nothing to do
    }
}