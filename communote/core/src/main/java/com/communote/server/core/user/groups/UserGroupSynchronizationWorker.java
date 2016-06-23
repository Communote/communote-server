package com.communote.server.core.user.groups;

import com.communote.server.api.ServiceLocator;
import com.communote.server.core.external.ExternalUserRepository;
import com.communote.server.core.external.IncrementalRepositoryChangeTracker;
import com.communote.server.plugins.api.externals.ExternalUserGroupAccessor;
import com.communote.server.service.UserService;

/**
 * This class handles the synchronization of users and groups.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class UserGroupSynchronizationWorker {

    /**
     * Works.
     * 
     * @param doFullSynchronization
     *            If set to true, a full synchronization should be done (only used for
     *            {@link IncrementalRepositoryChangeTracker}).
     */
    public void work(boolean doFullSynchronization) {
        UserService userService = ServiceLocator.instance().getService(UserService.class);
        for (ExternalUserRepository repository : userService.getActiveUserRepositories()) {
            ExternalUserGroupAccessor externalUserGroupAccessor = repository
                    .getExternalUserGroupAccessor();
            if (externalUserGroupAccessor != null) {
                IncrementalRepositoryChangeTracker incrementalRepositoryChangeTracker = repository
                        .getIncrementalRepositoryChangeTracker(doFullSynchronization);
                UserGroupRetriever retriever;
                if (incrementalRepositoryChangeTracker != null) {
                    retriever = new IncrementalUserGroupRetriever(
                            incrementalRepositoryChangeTracker);
                } else {
                    retriever = new ExternalUserGroupRetriever(repository.getExternalSystemId());
                }
                new UserGroupSynchronizer(repository.getExternalSystemId(),
                        externalUserGroupAccessor).synchronize(retriever);
            }
        }
    }
}
