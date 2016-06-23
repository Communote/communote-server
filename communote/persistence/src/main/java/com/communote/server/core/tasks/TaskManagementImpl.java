package com.communote.server.core.tasks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.event.EventDispatcher;
import com.communote.server.api.core.task.InvalidInstanceException;
import com.communote.server.api.core.task.TaskAlreadyExistsException;
import com.communote.server.api.core.task.TaskAlreadyRunningException;
import com.communote.server.api.core.task.TaskHandler;
import com.communote.server.api.core.task.TaskManagement;
import com.communote.server.api.core.task.TaskManagementException;
import com.communote.server.api.core.task.TaskNotActiveException;
import com.communote.server.api.core.task.TaskStatusException;
import com.communote.server.api.core.task.TaskTO;
import com.communote.server.model.task.Task;
import com.communote.server.model.task.TaskExecution;
import com.communote.server.model.task.TaskProperty;
import com.communote.server.model.task.TaskStatus;
import com.communote.server.persistence.tasks.TaskDao;
import com.communote.server.persistence.tasks.TaskExecutionDao;
import com.communote.server.persistence.tasks.TaskPropertyDao;

/**
 * Management class for tasks.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
@Service("taskManagement")
@Transactional(propagation = Propagation.REQUIRED)
public class TaskManagementImpl implements TaskManagement {

    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(TaskManagementImpl.class);

    private final static String INSTANCE_NAME = CommunoteRuntime.getInstance()
            .getConfigurationManager().getStartupProperties().getInstanceName();
    @Autowired
    private TaskDao taskDao;
    @Autowired
    private TaskExecutionDao taskExecutionDao;
    @Autowired
    private TaskPropertyDao taskPropertyDao;
    @Autowired
    private EventDispatcher eventDispatcher;
    private final Map<String, Class<? extends TaskHandler>> taskHandlers = new HashMap<String, Class<? extends TaskHandler>>();

    @Override
    public Long addTask(String uniqueTaskName, boolean active, Long interval,
            Date nextExecutionDate, Class<? extends TaskHandler> handlerClass)
            throws TaskAlreadyExistsException {
        return addTask(uniqueTaskName, active, interval, nextExecutionDate,
                new HashMap<String, String>(), handlerClass);
    }

    @Override
    public Long addTask(String uniqueTaskName, boolean active, Long interval,
            Date nextExecutionDate, Map<String, String> properties,
            Class<? extends TaskHandler> handlerClass) throws TaskAlreadyExistsException {
        return addTask(uniqueTaskName, active, interval, nextExecutionDate, properties,
                handlerClass.getName());
    }

    @Override
    public Long addTask(String uniqueTaskName, boolean active, Long interval,
            Date nextExecutionDate, Map<String, String> properties, String handlerClass)
            throws TaskAlreadyExistsException {
        if (taskDao.findTaskByUniqueName(uniqueTaskName) != null) {
            throw new TaskAlreadyExistsException(uniqueTaskName);
        }
        if (nextExecutionDate == null) {
            nextExecutionDate = new Date();
        }
        Task task = Task.Factory.newInstance(uniqueTaskName, active, TaskStatus.PENDING,
                nextExecutionDate, handlerClass);
        task.setTaskInterval(interval == null ? 0L : interval);
        Set<TaskProperty> taskProperties = new HashSet<TaskProperty>();
        for (Entry<String, String> property : properties.entrySet()) {
            TaskProperty taskProperty = TaskProperty.Factory.newInstance(property.getKey(),
                    property.getValue());
            taskProperties.add(taskPropertyDao.create(taskProperty));
        }
        task.setProperties(taskProperties);
        eventDispatcher.fire(new TaskScheduledEvent(uniqueTaskName, nextExecutionDate));
        return taskDao.create(task).getId();
    }

    @Override
    public Long addTask(String uniqueTaskName, boolean active, Long interval,
            Date nextExecutionDate, String handlerClass) throws TaskAlreadyExistsException {
        return addTask(uniqueTaskName, active, interval, nextExecutionDate,
                new HashMap<String, String>(), handlerClass);
    }

    @Override
    public void addTaskHandler(Class<? extends TaskHandler> taskHandler) {
        if (taskHandler == null) {
            throw new IllegalArgumentException("TaskHandler Class cannot be null!");
        }
        this.addTaskHandler(taskHandler.getName(), taskHandler);
    }

    // TODO should add the handler instance or in case more than one handler is needed a
    // TaskHandlerFactory. This would make it easier for plugins to work with iPOJO dependencies,
    // see for example the troubles in DeleteActivitiesTaskHandler to access the activityService
    @Override
    @Transactional(propagation = Propagation.SUPPORTS)
    public void addTaskHandler(String handlerName, Class<? extends TaskHandler> taskHandler) {
        if (taskHandler == null) {
            throw new IllegalArgumentException("TaskHandler Class cannot be null!");
        }
        synchronized (this) {
            this.taskHandlers.put(handlerName, taskHandler);
        }
        LOGGER.info("TaskHandler has been added. handlername: {} taskHandlerClazz: {}",
                handlerName, taskHandler.getName());
    }

    @Override
    @Transactional(propagation = Propagation.NEVER)
    public void addTaskHandlerWithTask(String handlerName,
            Class<? extends TaskHandler> taskHandlerClazz, final Map<String, String> properties,
            final long startOffset, boolean resetFailed) {

        this.addTaskHandler(handlerName, taskHandlerClazz);

        TaskManagement taskManagement = ServiceLocator.findService(TaskManagement.class);
        TaskTO existingTask = taskManagement.findTask(handlerName);
        long startTime = System.currentTimeMillis() + startOffset;
        if (existingTask != null) {
            LOGGER.debug("Task {} already exists and has status {}", handlerName,
                    existingTask.getStatus());
            boolean reschedule = existingTask.getNextExecution() == null
                    || existingTask.getNextExecution().getTime() - startTime < 0;
            if (TaskStatus.FAILED.equals(existingTask.getStatus())) {
                if (resetFailed) {
                    taskManagement.resetTask(handlerName);
                } else {
                    reschedule = false;
                }
            }
            if (reschedule) {
                try {
                    LOGGER.debug("Rescheduling task {} to respect start offset", handlerName);
                    taskManagement.rescheduleTask(handlerName, new Date(startTime));
                } catch (TaskStatusException e) {
                    // can happen in clustered environment but isn't critical
                    LOGGER.debug("Rescheduling task {} failed because it is not pending anymore",
                            handlerName);
                }
            }
        } else {
            try {
                taskManagement.addTask(handlerName, true, 0L, new Date(startTime), properties,
                        taskHandlerClazz);
            } catch (TaskAlreadyExistsException e) {
                // might occur in clustered environment but isn't critical.
                LOGGER.debug("Adding task {} failed because it already exists", handlerName);
            }
        }
    }

    @Override
    public void failTaskExecution(String uniqueTaskName) throws InvalidInstanceException {
        Task task = taskDao.findTaskByUniqueName(uniqueTaskName);
        if (task == null || !TaskStatus.RUNNING.equals(task.getTaskStatus())) {
            return;
        }
        TaskExecution taskExecution = taskExecutionDao.findTaskExecution(uniqueTaskName);
        if (!INSTANCE_NAME.equals(taskExecution.getInstanceName())) {
            throw new InvalidInstanceException(INSTANCE_NAME, taskExecution.getInstanceName());
        }
        task.setTaskStatus(TaskStatus.FAILED);
        taskDao.update(task);
    }

    @Override
    @Transactional(readOnly = true)
    public TaskTO findTask(String uniqueTaskName) {
        Task task = taskDao.findTaskByUniqueName(uniqueTaskName);
        if (task != null) {
            try {
                return new TaskTO(task, true);
            } catch (ClassNotFoundException e) {
                // must not occur since the handler is not created
                LOGGER.error("Unexpected exception retrieving a task", e);
            } catch (InstantiationException e) {
                // must not occur since the handler is not created
                LOGGER.error("Unexpected exception retrieving a task", e);
            } catch (IllegalAccessException e) {
                // must not occur since the handler is not created
                LOGGER.error("Unexpected exception retrieving a task", e);
            }
        }
        return null;
    }

    @Override
    public TaskTO getNextScheduledTask() {
        return getNextScheduledTask(null);
    }

    @Override
    public TaskTO getNextScheduledTask(Date upperBound) {
        Task task = taskDao.findNextScheduledTask();
        TaskTO taskTO = null;
        if (task != null && (upperBound == null || task.getNextExecution().before(upperBound))) {
            try {
                taskTO = new TaskTO(task, true);
                taskTO.setHandler(this.getTaskHandler(task.getHandlerClassName()));
            } catch (Exception e) {
                if (e instanceof ClassNotFoundException) {
                    LOGGER.warn(
                            "TaskHandler not found: {}. The handler might not be registered anymore."
                                    + " You should check your plugins. Will mark the task as FAILED.",
                            task.getHandlerClassName());
                } else {
                    LOGGER.error("Problem instantiating a task ({}). Will mark the task as FAILED",
                            task.getUniqueName(), e);
                }
                task.setTaskStatus(TaskStatus.FAILED);
                taskDao.update(task);
            }
        }
        return taskTO;
    }

    @Override
    public Collection<TaskTO> getNextScheduledTasks(Date upperBound, int maxTasks,
            Collection<Long> taskIdsToExclude) {
        // get as many tasks to fulfill the number of maxTasks given in case all excluded ids are
        // in the result set
        Collection<Task> tasks = taskDao.findNextScheduledTasks(upperBound, maxTasks,
                taskIdsToExclude);
        Collection<TaskTO> result = new ArrayList<TaskTO>();
        if (tasks != null) {
            for (Task task : tasks) {
                try {
                    TaskTO taskTO = new TaskTO(task, true);
                    taskTO.setHandler(getTaskHandler(task.getHandlerClassName()));
                    result.add(taskTO);
                } catch (Exception e) {
                    if (e instanceof ClassNotFoundException) {
                        LOGGER.warn(
                                "TaskHandler not found: {}. The handler might not be registered anymore."
                                        + " You should check your plugins. Will mark the task as FAILED.",
                                task.getHandlerClassName());
                    } else {
                        LOGGER.error(
                                "Problem instantiating a task ("
                                        + task.getUniqueName()
                                        + "). Will mark the task as FAILED. "
                                        + "Maybe adding TaskHandler using TaskManagement#addTaskHandler helps.",
                                e);
                    }
                    task.setTaskStatus(TaskStatus.FAILED);
                    taskDao.update(task);
                }
            }
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<TaskExecution> getTaskExecutions(String instanceName) {
        return taskExecutionDao.findTaskExecutions(instanceName);
    }

    /**
     * Get the task handler for the given name.
     *
     * @param handlerName
     *            the name
     * @return the task handler
     * @throws Exception
     *             in case the handler could not be instantiated or not be found
     */
    @SuppressWarnings("unchecked")
    private TaskHandler getTaskHandler(final String handlerName) throws Exception {
        Class<? extends TaskHandler> taskHandler = taskHandlers.get(handlerName);
        if (taskHandler == null) {
            synchronized (this) {
                taskHandler = (Class<TaskHandler>) Class.forName(handlerName);
                if (taskHandler != null) {
                    taskHandlers.put(handlerName, taskHandler);
                }
            }
        }
        return taskHandler.newInstance();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isTaskRunningOnCurrentInstance(String uniqueTaskName) {
        Task task = taskDao.findTaskByUniqueName(uniqueTaskName);
        if (task != null && TaskStatus.RUNNING.equals(task.getTaskStatus())) {
            TaskExecution execution = taskExecutionDao.findTaskExecution(uniqueTaskName);
            if (execution != null && INSTANCE_NAME.equals(execution.getInstanceName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String killTaskExecution(Long executionId) {
        TaskExecution taskExecution = taskExecutionDao.load(executionId);
        if (taskExecution == null) {
            return null;
        }
        Task task = taskExecution.getTask();
        taskExecutionDao.remove(taskExecution);
        task.setTaskStatus(TaskStatus.PENDING);
        taskDao.update(task);
        return task.getUniqueName();
    }

    @Override
    public void removeTask(String uniqueTaskName) throws TaskAlreadyRunningException {
        Task task = taskDao.findTaskByUniqueName(uniqueTaskName);
        if (task != null) {
            if (TaskStatus.RUNNING.equals(task.getTaskStatus())) {
                throw new TaskAlreadyRunningException(false);
            }
            Set<TaskProperty> properties = task.getProperties();
            task.setProperties(null);
            taskPropertyDao.remove(properties);
            taskDao.remove(task);
            LOGGER.debug("Removed task {}", uniqueTaskName);
        }
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS)
    public boolean removeTaskHandler(String handlerName) {
        Class<? extends TaskHandler> removed;
        synchronized (this) {
            removed = this.taskHandlers.remove(handlerName);
        }
        if (removed == null) {
            LOGGER.warn("No taskHandler has been removed for handlername. ");
            return false;
        }
        LOGGER.info("TaskHandler has been removed. handlername: " + handlerName
                + " taskHandlerClazz: " + removed.getName());
        return true;
    }

    @Override
    public void rescheduleTask(String uniqueTaskName, Date nextExecutionDate)
            throws TaskStatusException {
        Task task = taskDao.findTaskByUniqueName(uniqueTaskName);
        if (task == null) {
            return;
        }
        if (!TaskStatus.PENDING.equals(task.getTaskStatus())) {
            throw new TaskStatusException(task.getTaskStatus(), TaskStatus.PENDING);
        }
        rescheduleTask(task, nextExecutionDate);
        eventDispatcher.fire(new TaskScheduledEvent(uniqueTaskName, nextExecutionDate));
    }

    /**
     * Checks and sets the next execution date of the task.
     *
     * @param task
     *            The task.
     * @param rescheduleDate
     *            Possible reschedule date (might be null).
     * @return True, when the task is rescheduled.
     */
    private boolean rescheduleTask(Task task, Date rescheduleDate) {
        if (rescheduleDate != null) {
            if (task.getLastExecution() != null) {
                // enforce at least a second difference to last execution (to reduce load and
                // compensate MySQL time precision)
                long lastPlusSecond = task.getLastExecution().getTime() + 1000;
                if (lastPlusSecond > rescheduleDate.getTime()) {
                    rescheduleDate = new Date(lastPlusSecond);
                }
            }
            task.setNextExecution(rescheduleDate);
            return true;
        }
        if (task.getTaskInterval() != null && task.getTaskInterval() > 0) {
            long now = System.currentTimeMillis();
            long lastExecutionTime = task.getLastExecution() != null ? task.getLastExecution()
                    .getTime() : now;

            long nextExecution = lastExecutionTime + task.getTaskInterval();
            while (nextExecution < now) {
                nextExecution += task.getTaskInterval();
            }
            if (task.getLastExecution() != null) {
                long lastPlusSecond = task.getLastExecution().getTime() + 1000;
                if (lastPlusSecond > nextExecution) {
                    nextExecution = lastPlusSecond;
                }
            }
            task.setNextExecution(new Date(nextExecution));
            return true;
        }
        return false;
    }

    @Override
    public void resetTask(String uniqueTaskName) {
        Task task;
        TaskExecution foundTaskExecution = taskExecutionDao.findTaskExecution(uniqueTaskName);
        if (foundTaskExecution == null) {
            task = taskDao.findTaskByUniqueName(uniqueTaskName);
        } else {
            task = foundTaskExecution.getTask();
        }
        if (task != null && TaskStatus.FAILED.equals(task.getTaskStatus())) {
            task.setTaskStatus(TaskStatus.PENDING);
            LOGGER.debug("Reseted task {}", task.getUniqueName());
            if (foundTaskExecution != null) {
                taskExecutionDao.remove(foundTaskExecution);
            }
        }
    }

    @Override
    @Transactional(propagation = Propagation.NEVER)
    public Long startTaskExecution(String uniqueTaskName) throws TaskAlreadyRunningException,
            TaskNotActiveException, TaskManagementException {
        int retryCount = 5;
        Throwable lastException = null;
        while (retryCount > 0) {
            try {
                return ServiceLocator.findService(TaskManagement.class).startTaskExecutionTx(
                        uniqueTaskName);
            } catch (DataIntegrityViolationException e) {
                throw new TaskAlreadyRunningException(true);
            } catch (CannotAcquireLockException e) {
                // in case of a deadlock (currently only seen with MySQL DB), we retry the
                // transaction
                if (LOGGER.isDebugEnabled()) {
                    if (retryCount == 5) {
                        LOGGER.debug("Encountered Deadlock while trying to start task {}",
                                uniqueTaskName);
                    } else {
                        LOGGER.debug(
                                "Encountered Deadlock number {} while trying to start task {}",
                                (5 - retryCount), uniqueTaskName);
                    }
                }
                lastException = e;
                retryCount--;
            }
        }
        throw new TaskManagementException("Unexpected exception", lastException);
    }

    @Override
    // TODO @rwi Better method name. What is Tx for? Also, shouldn't this be private and/or
    // synchronized? - @amo Tx means new transaction (cf. other methods with requires_new). cannot
    // be private because spring aop must intercept the method call. there is no need to synchronize
    // this method because we use unique constraints - tlu: method name should be more sth like
    // "aquireLockForTask" since this method does not actually start the Task
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Long startTaskExecutionTx(String uniqueTaskName) throws TaskNotActiveException,
            TaskAlreadyRunningException {
        Task task = taskDao.findTaskByUniqueName(uniqueTaskName);
        if (task == null) {
            return null;
        }
        // if the nextExecution timestamp is in the future the task was already executed, maybe by
        // another node (add 0,5 seconds because of MySQLs time precision) or start was called too
        // early
        long now = System.currentTimeMillis() + 500;
        if (task.getNextExecution().after(new Date(now))
                && TaskStatus.PENDING.equals(task.getTaskStatus())) {
            // TODO throw exception?
            LOGGER.debug("Skipping task {} because it seems to have already been executed",
                    uniqueTaskName);
            return null;
        }
        if (!task.getActive()) {
            throw new TaskNotActiveException(uniqueTaskName);
        }
        if (TaskStatus.RUNNING.equals(task.getTaskStatus())) {
            throw new TaskAlreadyRunningException(false);
        }

        task.setTaskStatus(TaskStatus.RUNNING);
        TaskExecution execution = TaskExecution.Factory.newInstance(INSTANCE_NAME, task);
        return taskExecutionDao.create(execution).getId();
    }

    @Override
    public void stopAllTaskExecutions() {
        stopAllTaskExecutions(true, new Date());
    }

    @Override
    public void stopAllTaskExecutions(boolean resetFailed, Date rescheduleDate) {
        Collection<TaskExecution> localExecutions = taskExecutionDao
                .findTaskExecutions(INSTANCE_NAME);
        for (TaskExecution execution : localExecutions) {
            String uniqueName = execution.getTask().getUniqueName();
            try {
                stopTaskExecution(uniqueName, rescheduleDate, resetFailed);
            } catch (InvalidInstanceException e) {
                LOGGER.error(
                        "Unexpected exception while trying to stop the locally running task {}",
                        uniqueName, e);
            }
        }
        // TODO what about failed tasks without execution? Should probably be reset too?
        // TODO tasks provided by plugins should probably not be reset since plugins usually reset
        // them manually when adding the taskHandler. They also might provide a delay for starting
        // the next execution. Before the handler is added the task cannot run correctly and would
        // be reset to FAILED (race-condition).
    }

    @Override
    public void stopTaskExecution(String uniqueTaskName) throws InvalidInstanceException {
        stopTaskExecution(uniqueTaskName, null, false);
    }

    @Override
    public void stopTaskExecution(String uniqueTaskName, Date rescheduleDate)
            throws InvalidInstanceException {
        stopTaskExecution(uniqueTaskName, rescheduleDate, false);
    }

    /**
     * Ends the given task.
     *
     * @param uniqueTaskName
     *            Unique name of the task.
     * @param rescheduleDate
     *            If set, this will be the new date the task will be executed.
     * @param resetFailed
     *            If true and the task has status FAILED, the task will be reset to PENDING
     * @throws InvalidInstanceException
     *             Thrown, when the task is not running on this instance.
     */
    private void stopTaskExecution(String uniqueTaskName, Date rescheduleDate, boolean resetFailed)
            throws InvalidInstanceException {
        TaskExecution foundTaskExecution = taskExecutionDao.findTaskExecution(uniqueTaskName);
        if (foundTaskExecution == null) {
            return;
        }
        if (!foundTaskExecution.getInstanceName().equals(INSTANCE_NAME)) {
            throw new InvalidInstanceException(INSTANCE_NAME, foundTaskExecution.getInstanceName());
        }
        Task task = foundTaskExecution.getTask();
        if (!resetFailed && TaskStatus.FAILED.equals(task.getTaskStatus())) {
            return;
        }

        task.setTaskStatus(TaskStatus.PENDING);
        taskExecutionDao.remove(foundTaskExecution);
        task.setLastExecution(task.getNextExecution());
        if (rescheduleTask(task, rescheduleDate)) {
            taskDao.update(task);
            LOGGER.debug("The task {} was rescheduled for {}", task.getUniqueName(),
                    task.getNextExecution());
        } else {
            taskDao.remove(task);
        }
    }
}
