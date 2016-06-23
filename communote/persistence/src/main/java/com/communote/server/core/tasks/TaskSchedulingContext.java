package com.communote.server.core.tasks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.communote.common.util.Pair;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.client.ClientTO;
import com.communote.server.api.core.event.EventDispatcher;
import com.communote.server.api.core.event.EventListener;
import com.communote.server.api.core.task.TaskTO;
import com.communote.server.persistence.user.client.ClientHelper;

/**
 * Object holding some data which the {@link RunScheduledTasksJob} needs for scheduling the task
 * handlers. This instance also acts as listener for TaskAddedEvents
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class TaskSchedulingContext implements EventListener<TaskScheduledEvent> {
    /**
     * Key under which this instance is stored in the context of the scheduler instance
     */
    public final static String KEY_TASK_SCHEDULING_CONTEXT = "taskSchedulingContext";

    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(TaskSchedulingContext.class);

    private final String triggerName;
    private final String triggerGroup;
    private Scheduler scheduler;
    // saves per client the unique name of a task and the registration time and start time of the
    // task
    private final Map<ClientTO, Map<String, Pair<Long, Long>>> newTasksPerClient;
    // saves the scheduled tasks per client as mapping of client ID to task IDs
    private final Map<Long, Set<Long>> scheduledTasksPerClient;

    private long taskExecutionVariance = 0L;

    private boolean jobIsRunning;

    // holds the number of runs that were incomplete because of a failure

    private long rescheduleTimestamp;

    private long startTimestamp;

    // restart after 5min when all tasks have been scheduled/triggered
    private final long rescheduleOffset = 300000;

    // try restarting after 2 minutes when there was an error
    private final long failureRescheduleOffset = 120000;

    /**
     * Creates a new scheduling context for the provided trigger. The new instance is automatically
     * registered at the event dispatcher.
     *
     * @param trigger
     *            the trigger of the task scheduling job
     * @param eventDispatcher
     *            The event dispatcher to register this bean.
     */
    public TaskSchedulingContext(Trigger trigger, EventDispatcher eventDispatcher) {
        triggerGroup = trigger.getGroup();
        triggerName = trigger.getName();
        newTasksPerClient = new HashMap<ClientTO, Map<String, Pair<Long, Long>>>();
        scheduledTasksPerClient = new HashMap<Long, Set<Long>>();
        eventDispatcher.register(this);
    }

    /**
     * Add a new task for the current client. If the task scheduling job is currently running the
     * task will be considered when rescheduling the job. In case the task scheduler is not running
     * the task might trigger an earlier execution of the job.
     *
     * @param taskName
     *            unique name of the task
     * @param nextExecution
     *            the time of the next execution of the task
     */
    private void addTask(String taskName, Date nextExecution) {
        Long addTimeStamp = System.currentTimeMillis();
        Scheduler theScheduler = getScheduler();
        try {
            theScheduler.pauseTrigger(triggerName, triggerGroup);
            Trigger schedulerTrigger = theScheduler.getTrigger(triggerName, triggerGroup);
            if (schedulerTrigger == null) {
                LOGGER.warn("Skipping task scheduling as no trigger was found: {}"
                        + " (The application might not be started yet.)", taskName);
                return;
            }
            synchronized (this) {
                // gap in time between start of job and setting running state
                // next fire time is null when a trigger with a repeat count of 0 is started
                if (jobIsRunning || schedulerTrigger.getNextFireTime() == null) {
                    ClientTO client = ClientHelper.getCurrentClient();
                    // ignore any calls while the app is not yet initialized
                    if (client != null) {
                        Map<String, Pair<Long, Long>> newTasksOfCurrentClient = newTasksPerClient
                                .get(client);
                        if (newTasksOfCurrentClient == null) {
                            newTasksOfCurrentClient = new HashMap<String, Pair<Long, Long>>();
                            newTasksPerClient.put(client, newTasksOfCurrentClient);
                        }
                        newTasksOfCurrentClient.put(taskName, new Pair<Long, Long>(addTimeStamp,
                                nextExecution.getTime()));
                        LOGGER.debug("Task scheduler is running, marked task for immediate processing");
                    }
                } else {
                    // reschedule with earlier start date if necessary
                    // this method is usually called from a not yet committed transaction so we must
                    // add a small offset to let the transaction finish before checking for the task
                    Date effectiveNextExecution = new Date(System.currentTimeMillis() + 2000);
                    if (effectiveNextExecution.before(nextExecution)) {
                        effectiveNextExecution = nextExecution;
                    }
                    if (schedulerTrigger.getNextFireTime().after(effectiveNextExecution)) {
                        Trigger newTrigger = new SimpleTrigger(triggerName, triggerGroup,
                                effectiveNextExecution);
                        newTrigger.setJobDataMap(schedulerTrigger.getJobDataMap());
                        newTrigger.setJobName(schedulerTrigger.getJobName());
                        newTrigger.setJobGroup(schedulerTrigger.getJobGroup());
                        theScheduler.rescheduleJob(triggerName, triggerGroup, newTrigger);
                        LOGGER.debug("Rescheduled task scheduler to fire earlier");
                    } else {
                        theScheduler.resumeTrigger(triggerName, triggerGroup);
                    }
                }
            }
        } catch (SchedulerException e) {
            // could be critical, e.g. if resuming failed, but there is no clean way to recover
            LOGGER.error("Exception while scheduling the task scheduler, the "
                    + "task handling might not be working anymore", e);
        } catch (NullPointerException e) {
            LOGGER.error("Exception while scheduling the task scheduler, the "
                    + "task handling might not be working anymore: " + e.getMessage());
        }
    }

    /**
     * Get the clients with new tasks which should start before the provided timestamp. This method
     * must be called from a synchronized context.
     *
     * @param timestamp
     *            The timestamp to test for
     * @return a possibly empty list containing the clients with new tasks
     */
    private List<ClientTO> getClientsWithNewTasks(long timestamp) {
        List<ClientTO> clients = new ArrayList<ClientTO>();
        for (Map.Entry<ClientTO, Map<String, Pair<Long, Long>>> entry : newTasksPerClient
                .entrySet()) {
            for (Pair<Long, Long> taskInfo : entry.getValue().values()) {
                if (taskInfo.getRight() < timestamp) {
                    clients.add(entry.getKey());
                    break;
                }
            }
        }
        return clients;
    }

    /**
     * Try to reschedule the task scheduling job after all task handlers for all clients have been
     * scheduled. If other threads added new urgent tasks in the mean time the job won't be
     * rescheduled and the clients for which new tasks were added will be returned.
     *
     * @param oldTrigger
     *            the trigger that started the task scheduling job
     * @return a collection of clients for which new tasks exist that must be processed before the
     *         job can be rescheduled. If null is returned the job was rescheduled.
     * @throws SchedulerException
     *             in case the scheduling failed
     */
    public synchronized List<ClientTO> getClientsWithNewTasksOrRescheduleJob(Trigger oldTrigger)
            throws SchedulerException {
        long now = System.currentTimeMillis();
        if (now > rescheduleTimestamp) {
            // update reschedule timestamp by incrementing the old timestamp until the value is
            // higher than current timestamp
            rescheduleTimestamp = rescheduleTimestamp
                    + (((now - startTimestamp) / rescheduleOffset) * rescheduleOffset);
        }
        // check if there are any tasks that have a start time that is before the
        // rescheduleTimestamp
        List<ClientTO> clients = getClientsWithNewTasks(rescheduleTimestamp);
        if (clients.size() > 0) {
            // return clients to let Job process these clients again
            return clients;
        }
        // reschedule job
        rescheduleJob(oldTrigger, new Date(rescheduleTimestamp));
        return null;
    }

    /**
     * Returns the tasks IDs of the currently scheduled tasks
     *
     * @param client
     *            the client for which the scheduled tasks should be returned
     * @return the scheduled task IDs
     */
    public synchronized Collection<Long> getCurrentlyScheduledTaskIds(ClientTO client) {
        Set<Long> taskIds = scheduledTasksPerClient.get(client.getId());

        Collection<Long> scheduledIds;
        if (taskIds == null) {
            scheduledIds = Collections.emptyList();
        } else {
            scheduledIds = new ArrayList<Long>();
            scheduledIds.addAll(taskIds);
        }
        return scheduledIds;
    }

    /**
     * @return Now.
     */
    public Date getNextFireTime() {
        return new Date();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<TaskScheduledEvent> getObservedEvent() {
        return TaskScheduledEvent.class;
    }

    /**
     * @return the lazily initialized scheduler
     */
    private Scheduler getScheduler() {
        // lazily init scheduler because we cannot inject it as this class is injected to the
        // scheduler factory bean
        if (this.scheduler == null) {
            this.scheduler = ServiceLocator.findService(Scheduler.class);
        }
        return this.scheduler;
    }

    /**
     * @return the current task execution variance in milliseconds
     * @see TaskSchedulingContext#incrementTaskExecutionVariance()
     */
    public long getTaskExecutionVariance() {
        return taskExecutionVariance;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handle(TaskScheduledEvent event) {
        LOGGER.debug("Received event notification about a new task: {} ({})",
                event.getUniqueTaskName(), event.getNextExecution());
        this.addTask(event.getUniqueTaskName(), event.getNextExecution());
    }

    /**
     * Increment an offset that will be used when setting the start time of a task execution.
     * Calling this method is usually only useful in a clustered environment where different nodes
     * compete for task executions. The variance can help to reduce the possibility of a parallel
     * executions which would be blocked anyway.
     */
    public void incrementTaskExecutionVariance() {
        long variance = taskExecutionVariance + 250;
        // don't let it drift of more than 3 seconds
        taskExecutionVariance = variance % 3250;
    }

    /**
     * Callback to inform the scheduling context that the task scheduling job has been started.
     *
     * @param trigger
     *            the trigger that started the job
     */
    public synchronized void jobStarted(Trigger trigger) {
        jobIsRunning = true;
        startTimestamp = trigger.getStartTime().getTime();
        rescheduleTimestamp = startTimestamp + rescheduleOffset;
    }

    /**
     * Reschedule the job associated with oldTrigger
     *
     * @param oldTrigger
     *            the old trigger whose job should be rescheduled
     * @param newDate
     *            the date for rescheduling the job
     * @throws SchedulerException
     *             in case the scheduling failed
     */
    private void rescheduleJob(Trigger oldTrigger, Date newDate) throws SchedulerException {
        SimpleTrigger newTrigger = new SimpleTrigger(oldTrigger.getName(), oldTrigger.getGroup(),
                newDate);
        newTrigger.setJobGroup(oldTrigger.getJobGroup());
        newTrigger.setJobName(oldTrigger.getJobName());
        jobIsRunning = false;
        getScheduler().rescheduleJob(oldTrigger.getName(), oldTrigger.getGroup(), newTrigger);
    }

    /**
     * Reschedule the task scheduling job for cases where the scheduling of tasks resulted in an
     * error.
     *
     * @param oldTrigger
     *            the trigger that started the task scheduling job
     * @throws SchedulerException
     *             in case the rescheduling failed
     */
    public void rescheduleJobOnFailure(Trigger oldTrigger) throws SchedulerException {
        rescheduleJob(oldTrigger, new Date(System.currentTimeMillis() + failureRescheduleOffset));
    }

    /**
     * Callback to inform the task scheduling context that the handlers for all tasks of the current
     * client that were available at a specific point of time have been scheduled
     *
     * @param queryTime
     *            the time at which the caller checked for available tasks
     */
    public synchronized void scheduledAllTaskHandlersSince(Date queryTime) {
        // remove all tasks of the current client that have been added before the fetch time but
        // were not processed. This is necessary for cases where the task add event occurs but the
        // task is not persisted e.g. due to a rollback
        ClientTO client = ClientHelper.getCurrentClient();
        Map<String, Pair<Long, Long>> newTasksOfCurrentClient = newTasksPerClient.get(client);
        if (newTasksOfCurrentClient != null) {
            Iterator<String> taskNamesIter = newTasksOfCurrentClient.keySet().iterator();
            while (taskNamesIter.hasNext()) {
                Pair<Long, Long> taskInfo = newTasksOfCurrentClient.get(taskNamesIter.next());
                // the left member of the pair is the timestamp the task was added
                if (queryTime.getTime() > taskInfo.getLeft()) {
                    taskNamesIter.remove();
                }
            }
        }
    }

    /**
     * Callback to inform the context that a previously scheduled handler was processed.
     *
     * @param task
     *            the task that was started
     * @param client
     *            the client on which the task is running
     */
    public synchronized void scheduledTaskProcessed(TaskTO task, ClientTO client) {
        Set<Long> tasks = scheduledTasksPerClient.get(client.getId());
        if (tasks != null) {
            tasks.remove(task.getId());
        }
    }

    /**
     * Callback to inform the task scheduling context that the handler for a specific task has been
     * scheduled
     *
     * @param task
     *            the task for which the handler has been scheduled
     * @param client
     *            the client for which the handler was scheduled
     */
    public synchronized void taskHandlerScheduled(TaskTO task, ClientTO client) {
        Map<String, Pair<Long, Long>> newTasksOfCurrentClient = newTasksPerClient.get(client);
        if (newTasksOfCurrentClient != null) {
            newTasksOfCurrentClient.remove(task.getUniqueName());
        }
        Set<Long> tasks = scheduledTasksPerClient.get(client.getId());
        if (tasks == null) {
            tasks = new HashSet<Long>();
            scheduledTasksPerClient.put(client.getId(), tasks);
        }
        tasks.add(task.getId());
    }
}
