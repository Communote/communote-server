package com.communote.server.core.user.groups;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContext;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.task.TaskHandler;
import com.communote.server.api.core.task.TaskHandlerException;
import com.communote.server.api.core.task.TaskTO;
import com.communote.server.core.external.ExternalUserRepository;
import com.communote.server.core.security.AuthenticationHelper;
import com.communote.server.core.user.UserManagement;
import com.communote.server.model.user.ExternalUserAuthentication;
import com.communote.server.model.user.User;
import com.communote.server.model.user.group.ExternalUserGroup;
import com.communote.server.plugins.api.externals.ExternalUserGroupAccessor;
import com.communote.server.service.UserService;

/**
 * Task handler for synchronizing a specified user.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class SynchronizeExternalUserTaskHandler implements TaskHandler {

    /**
     * Retriever for exactly the one user.
     */
    private class InternalUserGroupRetriever extends UserGroupRetriever {

        private final User user;
        private boolean workDone = false;

        /**
         * Constructor.
         * 
         * @param user
         *            The user to sync.
         */
        public InternalUserGroupRetriever(User user) {
            this.user = user;
        }

        /**
         * @return null.
         */
        @Override
        public Collection<ExternalUserGroup> getNextGroups() {
            return null;
        }

        /**
         * @return A collection containing the user or null, if already invoked.
         */
        @Override
        public Collection<User> getNextUsers() {
            if (workDone) {
                return null;
            }
            Collection<User> users = new ArrayList<User>();
            users.add(user);
            workDone = true;
            return users;
        }

        /**
         * Does nothing.
         */
        @Override
        public void start() {
            // Do nothing.
        }

        /**
         * Does nothing.
         * 
         * @param success
         *            Not used.
         */
        @Override
        public void stop(boolean success) {
            // Do nothing.
        }

    }

    /** Logger. */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(SynchronizeExternalUserTaskHandler.class);

    /** Key for the users alias property. */
    public final static String PROPERTY_USER_ALIAS = "userAlias";

    /**
     * @param now
     *            Not used.
     * @return null
     */
    @Override
    public Date getRescheduleDate(Date now) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run(TaskTO task) throws TaskHandlerException {
        synchronized (UserGroupSynchronizationTaskHandler.LOCK) {
            runInternally(task);
        }
    }

    /**
     * Synchronizes the given user.
     * 
     * @param task
     *            The task.
     */
    private void runInternally(TaskTO task) {
        SecurityContext currentContext = AuthenticationHelper.setInternalSystemToSecurityContext();
        try {
            String userAlias = task.getProperty(PROPERTY_USER_ALIAS);
            UserManagement userManagement = ServiceLocator.instance().getService(
                    UserManagement.class);
            User user = userManagement.findUserByAlias(userAlias);
            if (user == null) {
                LOGGER.warn("Skipping synchronization for user {}, as the user doesn't exist.",
                        userAlias);
                return;
            }
            UserService userService = ServiceLocator.instance().getService(UserService.class);
            for (ExternalUserAuthentication externalAuthentication : userManagement
                    .getExternalExternalUserAuthentications(user.getId())) {
                ExternalUserRepository externalUserRepository = userService
                        .getExternalUserRepository(externalAuthentication.getSystemId());
                if (externalUserRepository == null
                        || externalUserRepository.getExternalUserGroupAccessor() == null) {
                    continue;
                }
                ExternalUserGroupAccessor externalUserGroupAccessor = externalUserRepository
                        .getExternalUserGroupAccessor();
                new UserGroupSynchronizer(externalAuthentication.getSystemId(),
                        externalUserGroupAccessor).synchronize(new InternalUserGroupRetriever(
                        user));
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            AuthenticationHelper.setSecurityContext(currentContext);
        }
    }
}
