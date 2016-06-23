package com.communote.server.core.tasks;

import java.util.Date;

import com.communote.server.api.core.event.Event;

/**
 * Event to inform about a new task that was added to the task store or when a pending task was
 * rescheduled.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class TaskScheduledEvent implements Event {

    private static final long serialVersionUID = 1L;
    private final String taskName;
    private final Date nextExecution;

    /**
     * Creates a new event
     *
     * @param taskName
     *            the unique name of the task
     * @param nextExecution
     *            the next execution date
     */
    public TaskScheduledEvent(String taskName, Date nextExecution) {
        this.taskName = taskName;
        this.nextExecution = nextExecution;
    }

    /**
     * @return the next execution date of the task
     */
    public Date getNextExecution() {
        return this.nextExecution;
    }

    /**
     * @return the unique name of the task
     */
    public String getUniqueTaskName() {
        return this.taskName;
    }
}
