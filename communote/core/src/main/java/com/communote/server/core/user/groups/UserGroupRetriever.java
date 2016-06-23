package com.communote.server.core.user.groups;

import java.util.Collection;

import com.communote.server.model.user.User;
import com.communote.server.model.user.group.ExternalUserGroup;

/**
 * Retriever interface for users and groups.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class UserGroupRetriever {
    /**
     * Method to get the next groups.
     * 
     * @return The list of groups or an empty list, if there are no more groups.
     */
    public abstract Collection<ExternalUserGroup> getNextGroups();

    /**
     * Method to get the next users.
     * 
     * @return The list of users or an empty list, if there are no more groups.
     */
    public abstract Collection<User> getNextUsers();

    /**
     * If this method returns true, the UserGroupSynchronizer has to visit the members of a group
     * explicitly. This could be useful, if getNextUser doesn't return users, which group membership
     * has changed.
     * 
     * @return True, if the synchronizer needs to visit the members of groups explicitly. Default is
     *         false.
     */
    public boolean needsToAcceptMembersOfGroups() {
        return false;
    }

    /**
     * Called, before the synchronizations starts.
     */
    public abstract void start();

    /**
     * Called, when the synchronization has been finished.
     * 
     * @param success
     *            If true, the synchronization was successful, else not.
     */
    public abstract void stop(boolean success);

}
