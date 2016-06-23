package com.communote.server.api.core.task;

import com.communote.server.model.task.TaskStatus;

/**
 * Indicates that the task is already running.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class TaskAlreadyRunningException extends TaskStatusException {

    private static final long serialVersionUID = -4144653240334375294L;
    private final boolean encounteredAtStartAttempt;

    /**
     * default constructor
     *
     * @param encounteredAtStartAttempt
     *            provides details about the circumstances when the running state was encountered.
     *            If true the task was found to be running while trying to start the task, which can
     *            only happen if another node of a clustered environment tried to start the task at
     *            the same time. If false the task was already running before trying to start it.
     */
    public TaskAlreadyRunningException(boolean encounteredAtStartAttempt) {
        super(TaskStatus.RUNNING, TaskStatus.PENDING);
        this.encounteredAtStartAttempt = encounteredAtStartAttempt;
    }

    /**
     * @return details about the circumstances when the running state was encountered. If true the
     *         task was found to be running while trying to start the task, which can only happen if
     *         another node of a clustered environment tried to start the task at the same time. If
     *         false the task was already running before trying to start it.
     */
    public boolean isEncounteredAtStartAttempt() {
        return encounteredAtStartAttempt;
    }
}
