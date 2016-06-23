package com.communote.server.core.tasks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.quartz.InterruptableJob;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.quartz.UnableToInterruptJobException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.quartz.QuartzJobBean;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.client.ClientDelegate;
import com.communote.server.api.core.client.ClientDelegateCallback;
import com.communote.server.api.core.client.ClientTO;
import com.communote.server.api.core.task.TaskManagement;
import com.communote.server.api.core.task.TaskTO;
import com.communote.server.api.service.ClientRetrievalService;
import com.communote.server.persistence.user.client.ClientHelper;

/**
 * Looks for tasks to be run and schedules the execution of the handlers these tasks.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class RunScheduledTasksJob extends QuartzJobBean implements InterruptableJob {

    /**
     * inner exception for errors while scheduling clients
     *
     */
    private class SchedulingClientTasksException extends Exception {

        /**
         * Default serial version UID
         */
        private static final long serialVersionUID = 1L;

        /**
         * Create a new exception
         *
         * @param message
         *            a detail message
         * @param cause
         *            the cause
         */
        public SchedulingClientTasksException(String message, Throwable cause) {
            super(message, cause);
        }

    }

    private static final long DEFAULT_SLEEP_TIMER = Long.getLong(
            "com.communote.tasks.no-threads-wait-time", 500);

    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(RunScheduledTasksJob.class);

    private static final String JOB_GROUP = "communoteTaskHandler";

    private boolean run = true;

    private final TaskManagement taskManagement;

    private boolean isRunning;

    /**
     * Constructor.
     */
    public RunScheduledTasksJob() {
        taskManagement = ServiceLocator.findService(TaskManagement.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void executeInternal(final JobExecutionContext context) throws JobExecutionException {
        TaskSchedulingContext schedulingContext = null;
        LOGGER.info("Starting task scheduling job");
        Collection<ClientTO> clients = new ArrayList<ClientTO>();
        try {
            isRunning = true;
            schedulingContext = (TaskSchedulingContext) context.getScheduler().getContext()
                    .get(TaskSchedulingContext.KEY_TASK_SCHEDULING_CONTEXT);
            schedulingContext.jobStarted(context.getTrigger());
            loop: while (run) {

                // check if there are available threads. if not wait and check again in some time.
                int availableThreads = getAvailableThreads(context.getScheduler());
                if (availableThreads <= 0) {
                    LOGGER.debug("No threads available, waiting {} ms for new threads",
                            DEFAULT_SLEEP_TIMER);
                    try {
                        Thread.sleep(DEFAULT_SLEEP_TIMER);
                    } catch (InterruptedException e) {
                        // ignore
                    }
                    continue loop;
                }

                if (clients.isEmpty()) {
                    clients = ServiceLocator.findService(ClientRetrievalService.class)
                            .getAllActiveClients();
                }
                // process all clients and schedule their tasks
                List<ClientTO> nonFullyProcessedClients = handleClients(clients,
                        context.getScheduler(), schedulingContext);
                // Have we been able to schedule all tasks, or are there clients with unscheduled
                // tasks left?
                if (nonFullyProcessedClients.isEmpty()) {
                    // if all tasks have been scheduled, check if new urgent tasks were added in
                    // meantime
                    clients = schedulingContext.getClientsWithNewTasksOrRescheduleJob(context
                            .getTrigger());
                    if (clients == null) {
                        // all clients processed, and no new clients, job is done
                        run = false;
                    }
                } else {
                    // there are clients left so continue with them
                    clients = nonFullyProcessedClients;
                }
            }
            LOGGER.info("Task scheduling job completed successfully");
        } catch (Throwable e) {
            LOGGER.error("Error in running RunScheduledTaskJob. ", e);
            try {
                schedulingContext.rescheduleJobOnFailure(context.getTrigger());
                LOGGER.debug("Rescheduling RunScheduledTaskJob succeeded");
            } catch (SchedulerException innerE) {
                LOGGER.error("Error rescheduling RunScheduledTaskJob", e);
                handleSchedulerException(innerE, context);
            }
            LOGGER.info("Task scheduling job completed with errors");
        } finally {
            isRunning = false;
        }

    }

    /**
     * Get the currently available threads by checking for running and scheduled jobs. This is a
     * rough estimation because only scheduled triggers created by this job are considered. Moreover
     * this value is highly volatile and might already have changed after the call.
     *
     * @param scheduler
     *            the scheduler instance
     * @return snap shot of the currently available threads
     * @throws JobExecutionException
     *             in case the statistics cannot be retrieved
     */
    private int getAvailableThreads(Scheduler scheduler) throws JobExecutionException {
        try {
            int threadPoolSize = scheduler.getMetaData().getThreadPoolSize();
            int currentlyRunning = scheduler.getCurrentlyExecutingJobs().size();
            int scheduledTasks = scheduler.getTriggerNames(JOB_GROUP).length;
            return threadPoolSize - currentlyRunning - scheduledTasks;
        } catch (SchedulerException e) {
            LOGGER.error("Cannot determine the thread pool size", e);
            throw new JobExecutionException("Cannot schedule task handlers");
        }
    }

    /**
     * Get the unique name of the task with the name of the client
     *
     * @param client
     *            the client
     * @param task
     *            the task
     * @return the unique name with the client id
     */
    public String getClientSpecificUniqueName(ClientTO client, TaskTO task) {
        return client.getClientId() + "#" + task.getUniqueName();
    }

    /**
     * Processes the task handlers of a single client
     *
     * @param client
     *            the client for which the handlers should be processed
     * @param scheduler
     *            the scheduler instance
     * @param schedulingContext
     *            context object holding additional information for scheduling the tasks
     * @param nextFireTime
     *            the time when this job will probably be started again
     * @return whether all handlers could be scheduled. Returns false if the pool ran out of threads
     * @throws JobExecutionException
     *             in case scheduling a task handler failed
     * @throws SchedulingClientTasksException
     *             in case the scheduling of client tasks resulted in an exception
     */
    private boolean handleClient(ClientTO client, final Scheduler scheduler,
            final TaskSchedulingContext schedulingContext, final Date nextFireTime)
            throws JobExecutionException, SchedulingClientTasksException {
        try {
            ClientDelegate delegate = new ClientDelegate(client);
            return delegate.execute(new ClientDelegateCallback<Boolean>() {
                @Override
                public Boolean doOnClient(ClientTO client) throws Exception {
                    return scheduleTaskHandlers(scheduler, schedulingContext, nextFireTime, client);
                }
            });

        } catch (SchedulerException e) {
            // scheduler exceptions are rather fatal in that the scheduler cannot recover
            throw new JobExecutionException("Scheduling tasks failed for " + client.getClientId(),
                    e, false);
        } catch (Exception e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Failed to schedule all tasks for client " + client.getClientId(), e);
            }
            throw new SchedulingClientTasksException("Exception while scheduling tasks for client "
                    + client.getClientId(), e);
        }
    }

    /**
     * Tries to process all task handlers of all the provided clients.
     *
     * @param clients
     *            the clients to process
     * @param scheduler
     *            the scheduler instance
     * @param schedulingContext
     *            context object holding additional information for scheduling the tasks
     * @throws JobExecutionException
     *             in case scheduling a task handler failed
     * @throws SchedulingClientTasksException
     *             in case the scheduling of client tasks resulted in an exception
     * @return Returns the list of clients that have not been processed completely. Is never null.
     */
    private List<ClientTO> handleClients(Collection<ClientTO> clients, Scheduler scheduler,
            TaskSchedulingContext schedulingContext) throws JobExecutionException,
            SchedulingClientTasksException {
        Throwable firstException = null;
        int failedClientsCount = 0;
        List<ClientTO> nonFullyProcessedClients = new ArrayList<ClientTO>(clients);
        clients: for (ClientTO client : clients) {
            try {
                if (handleClient(client, scheduler, schedulingContext,
                        schedulingContext.getNextFireTime())) {
                    LOGGER.trace("Scheduled all task handlers for client {}", client.getClientId());
                    nonFullyProcessedClients.remove(client);
                } else {
                    LOGGER.debug("Could not schedule all task handlers because thread pool ran out of "
                            + "free threads");
                    break clients;
                }
            } catch (SchedulingClientTasksException e) {
                // save the first cause and count the failed clients, but continue with next to
                // avoid that one faulty client blocks other clients
                if (firstException == null) {
                    firstException = e.getCause();
                }
                failedClientsCount++;
            }
        }
        // in case there were failures throw first exception
        if (failedClientsCount > 0) {
            throw new SchedulingClientTasksException("Scheduling client task handlers failed for "
                    + failedClientsCount + " clients", firstException);
        }
        return nonFullyProcessedClients;
    }

    /**
     * Handle a Scheduler Exception, and wrap it and rethrow it if necassary
     *
     * @param e
     *            the scheduler exception
     * @param context
     *            the context
     * @throws JobExecutionException
     *             the wrapped exception
     */
    private void handleSchedulerException(SchedulerException e, JobExecutionContext context)
            throws JobExecutionException {
        // if scheduler is shutdown LOG.debug, don't log exception
        boolean isShutdown = false;
        try {
            isShutdown = context != null && context.getScheduler() != null
                    && context.getScheduler().isShutdown();
        } catch (SchedulerException e2) {
            LOGGER.warn("Failed reading the shutdown status of the scheduler: " + e2.getMessage(),
                    e2);
        }
        if (isShutdown) {
            LOGGER.debug("Rescheduling the task scheduler failed because Scheduler ist shutting down.");
        } else {
            LOGGER.error("Rescheduling the task scheduler failed. Tasks won't be "
                    + "processed anymore", e);
            throw new JobExecutionException("Rescheduling the tasks scheduler failed", e);
        }
    }

    /**
     * Stops the job.
     *
     * @throws UnableToInterruptJobException
     *             in case the job could not be interrupt
     */
    @Override
    public void interrupt() throws UnableToInterruptJobException {
        run = false;
        int secondsToWait = 180;
        secondsToWait *= 2;
        LOGGER.info("Stopping task scheduler ...");
        while (isRunning && secondsToWait > 0) {
            secondsToWait--;
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                // ignore
            }
        }
        if (isRunning) {
            LOGGER.warn("Task scheduler couldn't be stopped within 3 minutes");
            throw new UnableToInterruptJobException("Stopping task scheduler failed");
        }

        LOGGER.info("Task scheduler stopped");
    }

    /**
     *
     * @param scheduler
     *            the scheduler instance
     * @param schedulingContext
     *            context object holding additional information for scheduling the tasks
     * @param tasks
     *            the tasks to schedule
     * @return true if all tasks have been scheduled
     * @throws SchedulerException
     *             in case scheduling the jobs failed
     */
    private boolean scheduleTaskHandlers(Scheduler scheduler,
            TaskSchedulingContext schedulingContext, Collection<TaskTO> tasks)
            throws SchedulerException {
        boolean allTasksScheduled = true;
        ClientTO client = ClientHelper.getCurrentClient();
        for (TaskTO task : tasks) {
            String clientUniqueTaskName = getClientSpecificUniqueName(client, task);
            JobDetail jobDetail = scheduler.getJobDetail(clientUniqueTaskName, JOB_GROUP);
            if (jobDetail == null) {
                jobDetail = new JobDetail(clientUniqueTaskName, JOB_GROUP, RunTaskHandlerJob.class,
                        false, true, false);
                jobDetail.setDurability(true);
                scheduler.addJob(jobDetail, false);
            }
            JobDataMap data = new JobDataMap();
            data.put(TaskTO.class.getName(), task);
            data.put(RunTaskHandlerJob.JOB_DATA_KEY_CLIENT, client);

            if (getAvailableThreads(scheduler) > 0) {
                Date now = new Date();
                if (now.after(task.getNextExecution())) {
                    scheduler.triggerJobWithVolatileTrigger(jobDetail.getName(),
                            jobDetail.getGroup(), data);
                    if (LOGGER.isTraceEnabled()) {
                        LOGGER.trace("Scheduled task handler for immediate execution of task "
                                + clientUniqueTaskName);
                    }
                } else {
                    Date triggerDate = new Date(task.getNextExecution().getTime()
                            + schedulingContext.getTaskExecutionVariance());
                    Trigger trigger = new SimpleTrigger(clientUniqueTaskName + "Trigger",
                            JOB_GROUP, triggerDate);
                    trigger.setJobGroup(JOB_GROUP);
                    trigger.setJobName(jobDetail.getName());
                    trigger.setVolatility(true);
                    trigger.setJobDataMap(data);
                    // only schedule if not scheduled (a reschedule would not schedule unscheduled
                    // triggers)
                    if (scheduler.getTrigger(trigger.getName(), trigger.getGroup()) == null) {
                        scheduler.scheduleJob(trigger);
                        if (LOGGER.isTraceEnabled()) {
                            LOGGER.trace("Scheduled task handler for delayed execution of task "
                                    + clientUniqueTaskName);
                        }
                    }
                }
                schedulingContext.taskHandlerScheduled(task, client);
            } else {
                allTasksScheduled = false;
                break;
            }
        }
        return allTasksScheduled;
    }

    /**
     * Schedule jobs to execute the task handlers
     *
     * @param scheduler
     *            the scheduler instance
     * @param schedulingContext
     *            context object holding additional information for scheduling the tasks
     * @param nextFireTime
     *            the time when this job will probably be started again
     * @param client
     *            the current client, for convenience
     * @return true if all task handlers could be scheduled. If false is returned not all task
     *         handlers could be scheduled because of insufficient free threads.
     * @throws SchedulerException
     *             in case scheduling the jobs failed
     */
    private Boolean scheduleTaskHandlers(Scheduler scheduler,
            TaskSchedulingContext schedulingContext, Date nextFireTime, ClientTO client)
            throws SchedulerException {
        Date queryTime = new Date(System.currentTimeMillis());
        // take available threads into account when checking for tasks. Fetch one more to know if
        // scheduler should start earlier.
        int tasksToGet = Math.max(10, getAvailableThreads(scheduler) + 1);

        Collection<Long> currentlyScheduledTaskIds = schedulingContext
                .getCurrentlyScheduledTaskIds(client);

        // check for tasks that should be run until the next fire time of this job
        Collection<TaskTO> tasks = taskManagement.getNextScheduledTasks(nextFireTime, tasksToGet,
                currentlyScheduledTaskIds);

        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("The following tasks are currently schedulded: "
                    + StringUtils.join(currentlyScheduledTaskIds, ", "));
            StringBuilder taskIdsToSchedule = new StringBuilder();
            String separator = "";
            for (TaskTO task : tasks) {
                taskIdsToSchedule.append(separator);
                taskIdsToSchedule.append(task.getId());
                separator = ", ";
            }
            LOGGER.trace("Will try to schedule the following tasks: "
                    + taskIdsToSchedule.toString());
        }

        if (tasks.size() == 0) {
            schedulingContext.scheduledAllTaskHandlersSince(queryTime);
            return Boolean.TRUE;
        }
        boolean allTasksScheduled = scheduleTaskHandlers(scheduler, schedulingContext, tasks);

        // if we got all the task we requested, assume there are more tasks to schedule
        if (allTasksScheduled && tasks.size() < tasksToGet) {
            schedulingContext.scheduledAllTaskHandlersSince(queryTime);
        } else {
            // must force false return value because we assume there are more tasks
            allTasksScheduled = false;
        }
        return Boolean.valueOf(allTasksScheduled);
    }
}
