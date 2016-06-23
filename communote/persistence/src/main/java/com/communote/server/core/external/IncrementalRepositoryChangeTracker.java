package com.communote.server.core.external;

import java.util.Collection;

import com.communote.server.model.user.User;
import com.communote.server.model.user.group.ExternalUserGroup;


/**
 * Tracker for incremental repository changes.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public interface IncrementalRepositoryChangeTracker {
    /**
     * Method to return the next groups from the repository.
     * 
     * @return List of groups or null if no groups left.
     */
    Collection<ExternalUserGroup> getNextGroups();

    /**
     * Method to return the next users from the repository.
     * 
     * @return List of users or null if no users left.
     */
    Collection<User> getNextUsers();

    /**
     * If this method returns true, the UserGroupSynchronizer has to visit the members of a group
     * explicitly. This could be useful, if getNextUser doesn't return users, which group membership
     * has changed.
     * 
     * @return True, if the synchronizer needs to visit the members of groups explicitly. Default is
     *         false.
     */
    boolean needsToAcceptMembersOfGroups();

    /**
     * Method to be called before the synchronization starts.
     */
    void start();

    /**
     * Method to be called after the synchronization ran.
     * 
     * @param success
     *            True, if the sync was successful.
     */
    void stop(boolean success);

}
