package com.communote.server.api.core.task;

import java.util.Date;

/**
 * Runs a specific task.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */

public interface TaskHandler {

    /**
     * Return the time a task should run again.
     *
     * @param now
     *            The date to use for any calculations.
     * @return the date for rescheduling the task. Should be null if the task should not be
     *         rescheduled again.
     */
    Date getRescheduleDate(Date now);

    /**
     * Executes the Task with the given properties.
     *
     * @param task
     *            The task.
     * @throws TaskHandlerException
     *             Exception.
     */
    void run(TaskTO task) throws TaskHandlerException;
}
