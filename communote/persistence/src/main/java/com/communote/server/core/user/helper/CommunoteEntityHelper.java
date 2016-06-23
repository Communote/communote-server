package com.communote.server.core.user.helper;

import com.communote.server.model.user.CommunoteEntity;
import com.communote.server.model.user.User;
import com.communote.server.model.user.group.Group;

/**
 * Helper for CommunoteEntity objects.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class CommunoteEntityHelper {

    /**
     * Returns a human readable name for the entity.
     *
     * @param entity
     *            the entity
     * @return the name
     */
    public static String getDisplayName(CommunoteEntity entity) {
        if (entity instanceof User) {
            return UserNameHelper.getSimpleDefaultUserSignature((User) entity);
        } else if (entity instanceof Group) {
            return ((Group) entity).getName();
        } else {
            throw new IllegalArgumentException("Unsupported entity type.");
        }
    }

    /**
     * Private constructor.
     */
    private CommunoteEntityHelper() {
        // nothing
    }
}
