package com.communote.server.api.core.task;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.communote.server.model.task.Task;
import com.communote.server.model.task.TaskProperty;
import com.communote.server.model.task.TaskStatus;

/**
 * Transfer object for a {@link com.communote.server.model.task.Task}.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class TaskTO {

    private Date nextExecution;

    private Date lastExecution;

    private Long interval;

    private TaskHandler handler;

    private Long id;

    private String uniqueName;

    private TaskStatus status;

    private boolean active;

    private Date rescheduleDate = null;
    private final Map<String, String> taskProperties = new HashMap<String, String>();

    /**
     * Empty constructor.
     */
    public TaskTO() {
        // Does nothing.
    }

    /**
     * Creates this TaskTO out of the given Task entity.
     *
     * @param task
     *            The task.
     * @param skipHandlerCreation
     *            If true, the handler wouldn't be created through the constructor.
     * @throws ClassNotFoundException
     *             Thrown, if there is no class for the given handler.
     * @throws IllegalAccessException
     *             Thrown, if it was not possible to initialize the handler class.
     * @throws InstantiationException
     *             Thrown, if it was not possible to initialize the handler class.
     */
    public TaskTO(Task task, boolean skipHandlerCreation) throws ClassNotFoundException,
    InstantiationException, IllegalAccessException {
        if (!skipHandlerCreation) {
            Class<?> forName = Class.forName(task.getHandlerClassName());
            this.handler = (TaskHandler) forName.newInstance();
        }
        this.id = task.getId();
        this.active = task.getActive();
        this.interval = task.getTaskInterval();
        this.nextExecution = task.getNextExecution();
        this.lastExecution = task.getLastExecution();
        this.status = task.getTaskStatus();
        this.uniqueName = task.getUniqueName();
        for (TaskProperty property : task.getProperties()) {
            setProperty(property.getPropertyKey(), property.getPropertyValue());
        }
    }

    /**
     * @return the handler
     */
    public TaskHandler getHandler() {
        return handler;
    }

    /**
     * @return the id
     */
    public Long getId() {
        return id;
    }

    /**
     * @return the interval
     */
    public Long getInterval() {
        return interval;
    }

    /**
     * @return the lastExecution
     */
    public Date getLastExecution() {
        return lastExecution;
    }

    /**
     * @return the nextExecution
     */
    public Date getNextExecution() {
        return nextExecution;
    }

    /**
     * @return all properties stored with the task
     */
    public Map<String, String> getProperties() {
        return taskProperties;
    }

    /**
     * Returns the value of a stored property.
     *
     * @param key
     *            the key of the property
     * @return the value or null
     */
    public String getProperty(String key) {
        return this.taskProperties.get(key);
    }

    /**
     * Returns the reschedule date.
     *
     * @return <code>null</code>
     */
    public Date getRescheduleDate() {
        return rescheduleDate;
    }

    /**
     * @return the status
     */
    public TaskStatus getStatus() {
        return status;
    }

    /**
     * @return the uniqueName
     */
    public String getUniqueName() {
        return uniqueName;
    }

    /**
     * Returns whether the task has a specific property.
     *
     * @param key
     *            the key of the property to check for
     * @return true if the property is contained
     */
    public boolean hasProperty(String key) {
        return this.taskProperties.containsKey(key);
    }

    /**
     * @return the active
     */
    public boolean isActive() {
        return active;
    }

    /**
     * @param handler
     *            the handler to set
     */
    public void setHandler(TaskHandler handler) {
        this.handler = handler;
    }

    /**
     * Sets a property for the task.
     *
     * @param key
     *            the key of the property
     * @param value
     *            the value of the property
     */
    public void setProperty(String key, String value) {
        this.taskProperties.put(key, value);
    }

    /**
     * @param rescheduleDate
     *            the rescheduleDate to set
     */
    public void setRescheduleDate(Date rescheduleDate) {
        this.rescheduleDate = rescheduleDate;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized String toString() {
        return uniqueName + super.toString();
    }
}
