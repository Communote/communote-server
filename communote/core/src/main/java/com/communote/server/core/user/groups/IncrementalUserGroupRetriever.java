package com.communote.server.core.user.groups;

import java.util.Collection;

import com.communote.server.core.external.IncrementalRepositoryChangeTracker;
import com.communote.server.model.user.User;
import com.communote.server.model.user.group.ExternalUserGroup;


/**
 * Retriever, which works on repositories which supports incremental data recovery.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class IncrementalUserGroupRetriever extends UserGroupRetriever {

    private final IncrementalRepositoryChangeTracker incrementalRepositoryChangeTracker;

    /**
     * Constructor.
     * 
     * @param incrementalRepositoryChangeTracker
     *            The tracker to use.
     */
    public IncrementalUserGroupRetriever(
            IncrementalRepositoryChangeTracker incrementalRepositoryChangeTracker) {
        this.incrementalRepositoryChangeTracker = incrementalRepositoryChangeTracker;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<ExternalUserGroup> getNextGroups() {
        return incrementalRepositoryChangeTracker.getNextGroups();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<User> getNextUsers() {
        return incrementalRepositoryChangeTracker.getNextUsers();
    }

    /**
     * {@inheritDoc}
     * 
     * @return {@link IncrementalRepositoryChangeTracker}
     */
    @Override
    public boolean needsToAcceptMembersOfGroups() {
        return incrementalRepositoryChangeTracker.needsToAcceptMembersOfGroups();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void start() {
        incrementalRepositoryChangeTracker.start();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stop(boolean success) {
        incrementalRepositoryChangeTracker.stop(success);
    }

}
