package com.communote.server.core.user.groups;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContext;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.client.ClientDelegate;
import com.communote.server.api.core.client.ClientDelegateCallback;
import com.communote.server.api.core.client.ClientTO;
import com.communote.server.api.core.config.type.ClientProperty;
import com.communote.server.api.core.task.TaskHandler;
import com.communote.server.api.core.task.TaskHandlerException;
import com.communote.server.api.core.task.TaskTO;
import com.communote.server.core.security.AuthenticationHelper;
import com.communote.server.core.user.client.ClientManagement;
import com.communote.server.model.client.ClientStatus;

/**
 * Job to synchronize user groups.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */

public class UserGroupSynchronizationTaskHandler implements TaskHandler {

    /** Logger. */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(UserGroupSynchronizationTaskHandler.class);

    private static AtomicBoolean IS_RUNNING = new AtomicBoolean(false);

    /** Lock which can be used to avoid multiple synchronization tasks running in parallel. */
    public final static Object LOCK = new Object();

    /**
     * {@inheritDoc}
     *
     * @return null;
     */
    @Override
    public Date getRescheduleDate(Date now) {
        int synchronizationInterval = ClientProperty.GROUP_SYNCHRONIZATION_INTERVAL_IN_MINUTES
                .getValue(ClientProperty.DEFAULT_GROUP_SYNCHRONIZATION_INTERVAL_IN_MINUTES);
        Calendar nextExecution = Calendar.getInstance();
        nextExecution.setTime(now);
        nextExecution.add(Calendar.MINUTE, synchronizationInterval);
        return nextExecution.getTime();
    }

    /**
     * Run's the job.
     */
    private void run() {
        LOGGER.info("Start synchronizing user groups.");
        long startTime = System.currentTimeMillis();
        Collection<ClientTO> allClients = ServiceLocator.findService(ClientManagement.class)
                .getAllClients();
        for (ClientTO client : allClients) {
            if (!client.getClientStatus().equals(ClientStatus.ACTIVE)) {
                continue;
            }
            try {
                new ClientDelegate(client).execute(new ClientDelegateCallback<Object>() {
                    @Override
                    public Object doOnClient(ClientTO client) throws Exception {
                        SecurityContext currentContext = AuthenticationHelper
                                .setInternalSystemToSecurityContext();
                        try {
                            new UserGroupSynchronizationWorker()
                                    .work(ClientProperty.GROUP_SYNCHRONIZATION_DO_FULL_SYNC
                                            .getValue(false));
                            // Reset the full sync, as it was executed.
                            CommunoteRuntime
                                    .getInstance()
                                    .getConfigurationManager()
                                    .updateClientConfigurationProperty(
                                            ClientProperty.GROUP_SYNCHRONIZATION_DO_FULL_SYNC,
                                            Boolean.FALSE.toString());
                        } finally {
                            AuthenticationHelper.setSecurityContext(currentContext);
                        }
                        return null;
                    }
                });
            } catch (Exception e) {
                LOGGER.error("Error handling synchronization for client: {}", client.getClientId(),
                        e);
            }

        }
        LOGGER.info("Finished synchronizing user groups. Needed about {} seconds",
                (System.currentTimeMillis() - startTime) / 1000);
    }

    /**
     * This is the main job.
     *
     * @param task
     *            Task with additional parameters.
     *
     * @throws TaskHandlerException
     *             Exception.
     */
    @Override
    public void run(TaskTO task) throws TaskHandlerException {
        if (IS_RUNNING.compareAndSet(false, true)) {
            synchronized (LOCK) {
                try {
                    run();
                } catch (Exception e) {
                    throw new TaskHandlerException(e);
                } finally {
                    IS_RUNNING.set(false);
                }
            }
        } else {
            LOGGER.info("Synchronizing of user groups is already running -> Skip.");
        }
    }
}
