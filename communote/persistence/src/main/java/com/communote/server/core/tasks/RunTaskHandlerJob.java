package com.communote.server.core.tasks;

import java.util.Date;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.quartz.QuartzJobBean;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.client.ClientDelegate;
import com.communote.server.api.core.client.ClientDelegateCallback;
import com.communote.server.api.core.client.ClientTO;
import com.communote.server.api.core.task.InvalidInstanceException;
import com.communote.server.api.core.task.TaskAlreadyRunningException;
import com.communote.server.api.core.task.TaskHandlerException;
import com.communote.server.api.core.task.TaskManagement;
import com.communote.server.api.core.task.TaskManagementException;
import com.communote.server.api.core.task.TaskNotActiveException;
import com.communote.server.api.core.task.TaskTO;

/**
 * Executes the given TaskHandler.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class RunTaskHandlerJob extends QuartzJobBean {
    /**
     * key for storing the client in the job data
     */
    public static final String JOB_DATA_KEY_CLIENT = "client";

    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(RunTaskHandlerJob.class);

    private final TaskManagement taskManagement;

    /**
     * Constructor.
     */
    public RunTaskHandlerJob() {
        taskManagement = ServiceLocator.findService(TaskManagement.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        final TaskTO task = (TaskTO) context.getMergedJobDataMap().get(TaskTO.class.getName());
        final TaskExecutionCleaner executionCleaner;
        final TaskSchedulingContext schedulingContext;
        try {
            executionCleaner = (TaskExecutionCleaner) context.getScheduler().getContext()
                    .get(TaskExecutionCleaner.KEY_TASK_EXECUTION_CLEANER);
            schedulingContext = (TaskSchedulingContext) context.getScheduler().getContext()
                    .get(TaskSchedulingContext.KEY_TASK_SCHEDULING_CONTEXT);
        } catch (SchedulerException e) {
            LOGGER.error("Cannot process task " + task.getUniqueName()
                    + " beacause task execution cleaner couldn't be obtained", e);
            throw new JobExecutionException(e);
        }
        LOGGER.debug("Starting task: {}", task.getUniqueName());
        ClientTO client = (ClientTO) context.getMergedJobDataMap().get(JOB_DATA_KEY_CLIENT);
        ClientDelegate delegate = new ClientDelegate(client);
        try {
            delegate.execute(new ClientDelegateCallback<Object>() {
                /**
                 * {@inheritDoc}
                 */
                public Object doOnClient(ClientTO client) throws Exception {
                    runTask(task, schedulingContext, executionCleaner, client);
                    return null;
                }
            });

            context.getScheduler().deleteJob(context.getJobDetail().getName(),
                    context.getJobDetail().getGroup());

        } catch (Exception e) {
            LOGGER.error("Running task " + task.getUniqueName() + " on " + client.getClientId()
                    + " failed", e);
            throw new JobExecutionException(e);
        }
    }

    /**
     * Mark a task running on the current client as failed.
     * 
     * @param taskName
     *            the name of the task
     * @param cleaner
     *            the task execution cleaner to try cleanup at a later time
     */
    private void failTask(String taskName, TaskExecutionCleaner cleaner) {
        try {
            taskManagement.failTaskExecution(taskName);
        } catch (InvalidInstanceException e) {
            LOGGER.error("Unexpected error while marking a task as failed", e);
        } catch (Exception e) {
            LOGGER.error("Unexpected error while marking a task as failed", e);
            cleaner.addTaskToFail(taskName);
        }
    }

    /**
     * Run the task on the current client
     * 
     * @param task
     *            the TO containing the details of the task to be run
     * @param schedulingContext
     *            the scheduling context
     * @param cleaner
     *            handle to the cleaner for storing tasks that couldn't be stopped or marked as
     *            failed
     * @param client
     *            the current client, for convenience
     * @throws TaskManagementException
     *             in case of an unexpected exception while starting the task
     */
    private void runTask(TaskTO task, TaskSchedulingContext schedulingContext,
            TaskExecutionCleaner cleaner, ClientTO client) throws TaskManagementException {
        boolean taskStarted = startTask(task, schedulingContext);
        // inform the scheduling context that we handled the task
        schedulingContext.scheduledTaskProcessed(task, client);
        if (!taskStarted) {
            return;
        }
        boolean taskCompleted = false;
        try {
            Long originalExecutionTimestamp = task.getNextExecution().getTime();
            Long now = System.currentTimeMillis();
            Long difference = now - originalExecutionTimestamp;
            if (difference >= 300000) {
                LOGGER.info("The task '{}' was scheduled for {} and executed at {} "
                        + "with a difference of {} seconds.", task.getUniqueName(),
                        new Date(originalExecutionTimestamp), new Date(now), difference / 1000);
            } else {
                LOGGER.debug("The task '{}' was scheduled for {} and executed at {} "
                        + "with a difference of {} seconds.", task.getUniqueName(),
                        new Date(originalExecutionTimestamp), new Date(now), difference / 1000);
            }
            task.getHandler().run(task);
            taskCompleted = true;
            taskManagement.stopTaskExecution(task.getUniqueName(), task.getHandler()
                    .getRescheduleDate(new Date()));
        } catch (TaskHandlerException e) {
            LOGGER.error("Error executing a task", e);
            failTask(task.getUniqueName(), cleaner);
        } catch (InvalidInstanceException e) {
            LOGGER.error("Error stopping a task", e);
        } catch (Exception e) {
            if (taskCompleted) {
                LOGGER.error("Unexpected error stopping a task", e);
                cleaner.addTaskToStop(task.getUniqueName());
            } else {
                LOGGER.error("Unexpected error executing a task", e);
                failTask(task.getUniqueName(), cleaner);
            }
        }
    }

    /**
     * @param task
     *            The task.
     * @param schedulingContext
     *            the scheduling context
     * @return True, if the task was started, false if it was already running or couldn't be started
     * @throws TaskManagementException
     *             in case of an unexpected exception while starting the task
     */
    private boolean startTask(TaskTO task, TaskSchedulingContext schedulingContext)
            throws TaskManagementException {
        // ignore service task handlers because these tasks are handled by the CommunoteServiceManager
        // TODO we should change it to a more generic task handler: IgnoreTaskHandler or just set
        // the handler to null
        if (task.getHandler() instanceof ServiceTaskHandler) {
            LOGGER.debug("Ignoring service task: {}", task.getUniqueName());
            return false;
        }
        boolean result = true;
        try {
            if (taskManagement.startTaskExecution(task.getUniqueName()) == null) {
                result = false;
            }
        } catch (TaskAlreadyRunningException e) {
            LOGGER.debug("Skipping task, because it is already running: {}"
                    , task.getUniqueName());
            if (e.isEncounteredAtStartAttempt()) {
                LOGGER.debug("Increasing the task execution variance on this instance");
                schedulingContext.incrementTaskExecutionVariance();
            }
            result = false;
        } catch (TaskNotActiveException e) {
            LOGGER.error("Tried to run non-active task: " + task.getUniqueName());
            result = false;
        }
        return result;
    }
}
