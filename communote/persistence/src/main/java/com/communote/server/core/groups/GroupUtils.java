package com.communote.server.core.groups;

import com.communote.server.model.user.CommunoteEntity;
import com.communote.server.model.user.group.Group;

/**
 * Helper class for groups.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class GroupUtils {

    /**
     * Checks if a group contains an entity either directly or indirectly, which is the entity is
     * contained in one of the transitively resolved child groups of the group .
     * 
     * @param group
     *            Potential parent group.
     * @param child
     *            entity to check for group membership
     * @return True if the entity is a child of the group.
     */
    public static boolean isChild(Group group, CommunoteEntity child) {
        boolean result = false;
        loop: for (Group directParent : child.getGroups()) {
            if (directParent.getId() == group.getId()) {
                return true;
            }
            if (isChild(group, directParent)) {
                result = true;
                break loop;
            }
        }
        return result;
    }

    /**
     * Private constructor to avoid instances of utility class.
     */
    private GroupUtils() {
        // Do nothing
    }
}
