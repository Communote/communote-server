package com.communote.server.core.tasks;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.client.ClientDelegate;
import com.communote.server.api.core.client.ClientDelegateCallback;
import com.communote.server.api.core.client.ClientTO;
import com.communote.server.api.core.task.TaskHandler;
import com.communote.server.api.core.task.TaskHandlerException;
import com.communote.server.api.core.task.TaskTO;
import com.communote.server.core.user.client.ClientManagement;
import com.communote.server.model.client.ClientStatus;

/**
 * The {@link ClientTaskHandler} provides a method {@link #runOnClient(TaskTO)} that will be
 * executed for each client for a {@link TaskTO}.
 *
 * By default all active clients are used, however the behavior can be changed by overwriting
 * {@link #checkRunOnClient(ClientTO)}
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://communote.com/</a>
 *
 */
public abstract class ClientTaskHandler implements TaskHandler {

    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientTaskHandler.class);

    /**
     *
     * @param client
     *            the client to check
     * @return true if the handle should be run on this client
     */
    protected boolean checkRunOnClient(ClientTO client) {
        return client.getClientStatus().equals(ClientStatus.ACTIVE);
    }

    /**
     * Check if the task should actually be run. Default implementation just returns true.
     *
     * @param task
     *            the task to check
     * @return true if the task will actually be handled or false to skip
     */

    protected boolean checkRunTask(TaskTO task) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void run(final TaskTO task) throws TaskHandlerException {
        if (!checkRunTask(task)) {
            return;
        }
        Collection<ClientTO> allClients = ServiceLocator.findService(ClientManagement.class)
                .getAllClients();
        for (ClientTO client : allClients) {
            if (!checkRunOnClient(client)) {
                continue;
            }
            try {
                new ClientDelegate(client).execute(new ClientDelegateCallback<Object>() {
                    @Override
                    public Object doOnClient(ClientTO client) throws Exception {
                        runOnClient(task);
                        return null;
                    }
                });
            } catch (Exception e) {
                LOGGER.error("Error running task {} on client " + client.getClientId(),
                        task.getUniqueName(), e);
            }
        }
    }

    /**
     * Is called for each client
     *
     * @param task
     *            the task
     * @throws Exception
     *             in case something goes wrong
     */
    protected abstract void runOnClient(TaskTO task) throws Exception;

}
