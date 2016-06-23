package com.communote.server.core.tasks;

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import com.communote.common.util.Pair;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.client.ClientDelegate;
import com.communote.server.api.core.client.ClientDelegateCallback;
import com.communote.server.api.core.client.ClientNotFoundException;
import com.communote.server.api.core.client.ClientTO;
import com.communote.server.api.core.task.InvalidInstanceException;
import com.communote.server.api.core.task.TaskManagement;
import com.communote.server.api.service.ClientRetrievalService;
import com.communote.server.persistence.user.client.ClientHelper;

/**
 * Tool to clean up tasks that couldn't be stopped or marked as failed at the end of the task
 * execution for instance because of missing DB connections. The Cleaner will be triggered by the
 * {@link TaskExecutionCleanupJob}.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class TaskExecutionCleaner {
    /**
     * Key under which this instance is stored in the context of the scheduler instance
     */
    public static final String KEY_TASK_EXECUTION_CLEANER = "taskExecutionCleanupContext";
    private final static Logger LOG = Logger.getLogger(TaskExecutionCleaner.class);

    private final ConcurrentHashMap<String, Pair<String, String>> failedTasks;
    private final ConcurrentHashMap<String, Pair<String, String>> stoppedTasks;

    /**
     * Constructor.
     */
    public TaskExecutionCleaner() {
        failedTasks = new ConcurrentHashMap<String, Pair<String, String>>();
        stoppedTasks = new ConcurrentHashMap<String, Pair<String, String>>();
    }

    /**
     * add a task to the task store
     *
     * @param tasks
     *            the store to add to
     * @param taskName
     *            the name of the task to add
     */
    private void addTask(ConcurrentHashMap<String, Pair<String, String>> tasks, String taskName) {
        String clientId = ClientHelper.getCurrentClientId();
        tasks.put(clientId + " " + taskName, new Pair<String, String>(clientId, taskName));
    }

    /**
     * Add a task that should be marked as failed.
     *
     * @param taskName
     *            the unique name of the task
     */
    public void addTaskToFail(String taskName) {
        addTask(failedTasks, taskName);
    }

    /**
     * Add a task that should be stopped.
     *
     * @param taskName
     *            the unique name of the task
     */
    public void addTaskToStop(String taskName) {
        addTask(stoppedTasks, taskName);
    }

    /**
     * Process the named task on the given client.
     *
     * @param client
     *            the client the task belongs to
     * @param taskName
     *            the name of the task
     * @param fail
     *            true if the tasks should be marked as failed, false if it should be stopped
     * @return whether the task was successfully stopped or marked as failed
     */
    private boolean processClientTask(ClientTO client, final String taskName, final boolean fail) {
        final TaskManagement taskManagement = ServiceLocator.findService(TaskManagement.class);
        ClientDelegate delegate = new ClientDelegate(client);
        try {
            delegate.execute(new ClientDelegateCallback<Object>() {
                /**
                 * {@inheritDoc}
                 */
                @Override
                public Object doOnClient(ClientTO client) throws Exception {
                    try {
                        if (fail) {
                            taskManagement.failTaskExecution(taskName);
                        } else {
                            taskManagement.stopTaskExecution(taskName);
                        }
                    } catch (InvalidInstanceException e) {
                        // this should not happen, but in case we can't do anything about it so the
                        // taskEntry can be removed
                        LOG.error("Unexpected error handling task " + taskName + " on client "
                                + client.getClientId(), e);
                    }
                    return null;
                }
            });
        } catch (Exception e) {
            // any other exception means stopping or marking as failed did not work so try it again
            if (fail) {
                LOG.error("Error marking task " + taskName + " of client " + client.getClientId()
                        + " as failed", e);
            } else {
                LOG.error("Error stopping task " + taskName + " of client " + client.getClientId(),
                        e);
            }
            return false;
        }
        return true;
    }

    /**
     * Process all tasks in the store
     *
     * @param tasks
     *            the store containing the tasks
     * @param fail
     *            true if the tasks should be marked as failed, false if it should be stopped
     */
    private void processTasks(ConcurrentHashMap<String, Pair<String, String>> tasks, boolean fail) {
        Iterator<Pair<String, String>> taskEntryIt = tasks.values().iterator();
        while (taskEntryIt.hasNext()) {
            Pair<String, String> taskEntry = taskEntryIt.next();
            try {
                ClientTO client = ServiceLocator.findService(ClientRetrievalService.class)
                        .findClient(taskEntry.getLeft());
                // if the operation succeeded remove the entry, otherwise try again later
                if (processClientTask(client, taskEntry.getRight(), fail)) {
                    taskEntryIt.remove();
                }
            } catch (ClientNotFoundException e) {
                // client does not exist anymore -> remove entry
                taskEntryIt.remove();
            }
        }
    }

    /**
     * Process all tasks that should be marked as failed.
     */
    public void processTasksToFail() {
        processTasks(failedTasks, true);
    }

    /**
     * Process all tasks that should be stopped.
     */
    public void processTasksToStop() {
        processTasks(stoppedTasks, false);
    }
}
