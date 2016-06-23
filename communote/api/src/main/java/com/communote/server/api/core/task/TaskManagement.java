package com.communote.server.api.core.task;

import java.util.Collection;
import java.util.Date;
import java.util.Map;

import com.communote.server.model.task.TaskExecution;

/**
 * Service for managing tasks. If Communote is clustered there will be more than one instance on
 * which a task can be executed. The different instances or nodes are identified by their name as
 * defined in the {@link com.communote.server.api.core.config.StartupProperties}.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public interface TaskManagement {

    /**
     * Add a new task.
     *
     * @param uniqueTaskName
     *            Unique name for this task.
     * @param active
     *            True, if active.
     * @param interval
     *            Interval for the execution.
     * @param nextExecutionDate
     *            The next execution date.
     * @param handlerClass
     *            Class of the handler.
     * @return The id of the Task.
     *
     * @throws TaskAlreadyExistsException
     *             Thrown, when there is already a task with this name.
     */
    public Long addTask(String uniqueTaskName, boolean active, Long interval,
            Date nextExecutionDate, Class<? extends TaskHandler> handlerClass)
            throws TaskAlreadyExistsException;

    /**
     * Add a new task.
     *
     * @param uniqueTaskName
     *            Unique name for this task.
     * @param active
     *            True, if active.
     * @param interval
     *            Interval for the execution.
     * @param nextExecutionDate
     *            The next execution date.
     * @param properties
     *            Properties of this task.
     * @param handlerClass
     *            Class of the handler.
     * @return The id of the Task.
     *
     * @throws TaskAlreadyExistsException
     *             Thrown, when there is already a task with this name.
     */
    public Long addTask(String uniqueTaskName, boolean active, Long interval,
            Date nextExecutionDate, Map<String, String> properties,
            Class<? extends TaskHandler> handlerClass) throws TaskAlreadyExistsException;

    /**
     * Add a new task.
     *
     * @param uniqueTaskName
     *            Unique name for this task.
     * @param active
     *            True, if active.
     * @param interval
     *            Interval for the execution.
     * @param nextExecutionDate
     *            The next execution date.
     * @param properties
     *            Properties of this task.
     * @param handlerClass
     *            Full qualified class name for the handler.
     * @return The id of the Task.
     *
     * @throws TaskAlreadyExistsException
     *             Thrown, when there is already a task with this name.
     */
    public Long addTask(String uniqueTaskName, boolean active, Long interval,
            Date nextExecutionDate, Map<String, String> properties, String handlerClass)
            throws TaskAlreadyExistsException;

    /**
     * Add a new task.
     *
     * @param uniqueTaskName
     *            Unique name for this task.
     * @param active
     *            True, if active.
     * @param interval
     *            Interval for the execution.
     * @param nextExecutionDate
     *            The next execution date.
     * @param handlerClass
     *            Full qualified class name for the handler.
     * @return The id of the Task.
     *
     * @throws TaskAlreadyExistsException
     *             Thrown, when there is already a task with this name.
     */
    public Long addTask(String uniqueTaskName, boolean active, Long interval,
            Date nextExecutionDate, String handlerClass) throws TaskAlreadyExistsException;

    /**
     * Add a handler for tasks. The handler will be registered under its full class name. If a
     * handler with the same name already exists it will be overwritten.
     *
     * @param taskHandler
     *            the task handler to add
     */
    public void addTaskHandler(Class<? extends TaskHandler> taskHandler);

    /**
     * Add a handler for tasks with the given name. If a handler with the same name already exists
     * it will be overwritten.
     *
     * @param handlerName
     *            the name of the handler to be used
     * @param taskHandler
     *            the task handler to add
     */
    public void addTaskHandler(String handlerName, Class<? extends TaskHandler> taskHandler);

    /**
     * Add a task handler and create a task for it. If a task already exists which has an execution
     * time before now + startOffset, the task will be rescheduled to run with the new start time
     * (now + startOffset).
     *
     * @param handlerName
     *            the name of the handler. The name will also be used as unique name for the task.
     * @param taskHandlerClazz
     *            the {@link TaskHandler} class
     * @param properties
     *            Properties for the task (if one is added).
     * @param startOffset
     *            the offset in milliseconds to add to the current time to delay the execution of
     *            the task
     * @param resetFailed
     *            if true and the status of an existing task is failed the task will be always
     *            rescheduled.
     */
    public void addTaskHandlerWithTask(String handlerName,
            Class<? extends TaskHandler> taskHandlerClazz, final Map<String, String> properties,
            final long startOffset, boolean resetFailed);

    /**
     * Mark the execution of a task as failed.
     *
     * @param uniqueTaskName
     *            Name of the task.
     * @throws InvalidInstanceException
     *             Thrown, when the task is not running on this instance.
     */
    public void failTaskExecution(String uniqueTaskName) throws InvalidInstanceException;

    /**
     * Get the details of a task.
     *
     * @param uniqueTaskName
     *            The name of the task
     * @return the TO containing the details of the task or null if there is no task for that name.
     *         The handler of the task won't be set
     */
    public TaskTO findTask(String uniqueTaskName);

    /**
     * Return the next task to be scheduled.
     *
     * @return TaskTO or null if there is none.
     */
    public TaskTO getNextScheduledTask();

    /**
     * Return the next task to be scheduled.
     *
     * @param upperBound
     *            The upper bound of the task's next execution (exclusive).
     * @return TaskTO or null if there is none.
     */
    public TaskTO getNextScheduledTask(Date upperBound);

    /**
     * Get the tasks to schedule next
     *
     * @param upperBound
     *            NextExecution < UpperBound.
     * @param maxTasks
     *            maximum number of tasks to return
     * @param taskIdsToExclude
     *            the IDs of the task to exclude from the query. Can be null.
     * @return List all tasks, which can be executed.
     */
    public Collection<TaskTO> getNextScheduledTasks(Date upperBound, int maxTasks,
            Collection<Long> taskIdsToExclude);

    /**
     * Return all task executions running on the Communote with the given name.
     *
     * @param instanceName
     *            Name of the instance
     * @return List of task executions of the instance
     */
    public Collection<TaskExecution> getTaskExecutions(String instanceName);

    /**
     * Return whether the given task is running on the current instance.
     *
     * @param uniqueTaskName
     *            the unique name of the task
     * @return true if the task is running on the current instance, false otherwise
     */
    public boolean isTaskRunningOnCurrentInstance(String uniqueTaskName);

    /**
     * Kill the task with given execution ID by removing the execution and setting the task back to
     * pending.
     *
     * @param executionId
     *            The execution ID
     * @return The unique name of the task, which was killed, or null if there was none.
     */
    public String killTaskExecution(Long executionId);

    /**
     * Remove a task if it is not RUNNING.
     *
     * @param uniqueTaskName
     *            The task to remove.
     * @throws TaskAlreadyRunningException
     *             Thrown, when the task is currently running.
     */
    public void removeTask(String uniqueTaskName) throws TaskAlreadyRunningException;

    /**
     * Remove the task handler class from the list of known handlers.
     *
     * @param handlerName
     *            the handler name
     * @return true if something has actually been removed
     */
    public boolean removeTaskHandler(String handlerName);

    /**
     * Reschedule a pending task.
     *
     * @param uniqueTaskName
     *            the unique name of task. If there is no task with the given name the call will be
     *            ignored.
     * @param nextExecutionDate
     *            the new execution date
     * @throws TaskStatusException
     *             in case the task is not pending
     */
    public void rescheduleTask(String uniqueTaskName, Date nextExecutionDate)
            throws TaskStatusException;

    /**
     * Reset a task back from failed to pending. Also removes an existing execution.
     *
     * @param uniqueTaskName
     *            The name of the task to reset
     */
    public void resetTask(String uniqueTaskName);

    /**
     * Mark a task as running on this instance by creating an execution for it and setting the
     * status to RUNNING. This call is ignored if the next execution timestamp of the task is in the
     * future.
     *
     * @param uniqueTaskName
     *            Name of the task to start.
     * @return the ID of the created execution or null if the task does not exist or next execution
     *         timestamp is in the future
     * @throws TaskAlreadyRunningException
     *             in case the task is already running.
     * @throws TaskNotActiveException
     *             in case the task is not activated.
     * @throws TaskManagementException
     *             in case of an unexpected error
     */
    public Long startTaskExecution(String uniqueTaskName) throws TaskAlreadyRunningException,
            TaskNotActiveException, TaskManagementException;

    /**
     * Same as {@link #startTaskExecution(String)} but can throw
     * DatabaseIntegrityViolationException. Therefore this method should never be called directly
     * and {@link #startTaskExecution(String)} should be used instead.
     *
     * @param uniqueTaskName
     *            Name of the task to start.
     * @return the ID of the created execution or null if the task does not exist or next execution
     *         timestamp is in the future
     * @throws TaskAlreadyRunningException
     *             in case the task is already running.
     * @throws TaskNotActiveException
     *             In case if the task is not activated.
     */
    // TODO should be removed and implementation should use a RunInTransaction
    public Long startTaskExecutionTx(String uniqueTaskName) throws TaskNotActiveException,
    TaskAlreadyRunningException;

    /**
     * Calls stopAllTaskExecutions(true,now).
     *
     */
    public void stopAllTaskExecutions();

    /**
     * End all task executions of this instance and schedules them again.
     *
     * @param rescheduleDate
     *            If set, this will be the new date the task will be executed.
     * @param resetFailed
     *            If set, all failed tasks will be reset.
     */
    public void stopAllTaskExecutions(boolean resetFailed, Date rescheduleDate);

    /**
     * End the execution of the given task.
     *
     * @param uniqueTaskName
     *            Name of the task.
     * @throws InvalidInstanceException
     *             in case the task is not executed on this instance
     */
    public void stopTaskExecution(String uniqueTaskName) throws InvalidInstanceException;

    /**
     * End the execution of the given task.
     *
     * @param uniqueTaskName
     *            Unique name of the task.
     * @param rescheduleDate
     *            If set, this will be the new date the task will be executed.
     *
     * @throws InvalidInstanceException
     *             in case the task is not running on this instance
     */
    public void stopTaskExecution(String uniqueTaskName, Date rescheduleDate)
            throws InvalidInstanceException;
}
