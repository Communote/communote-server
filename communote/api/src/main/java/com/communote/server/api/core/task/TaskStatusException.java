package com.communote.server.api.core.task;

import com.communote.server.model.task.TaskStatus;

/**
 * Exception to indicate that a task has not the expected status.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class TaskStatusException extends TaskManagementException {

    /**
     * default serial version UID
     */
    private static final long serialVersionUID = 1L;
    private final TaskStatus expectedStatus;
    private final TaskStatus actualStatus;

    /**
     * Creates a new exception with the details.
     *
     * @param actualStatus
     *            the actual status
     * @param expectedStatus
     *            the expected status
     *
     */
    public TaskStatusException(TaskStatus actualStatus, TaskStatus expectedStatus) {
        super("The task has not the expected status. Actual: " + actualStatus + ", expected: "
                + expectedStatus);
        this.expectedStatus = expectedStatus;
        this.actualStatus = actualStatus;
    }

    /**
     * @return the actual status of the task
     */
    public TaskStatus getActualStatus() {
        return this.actualStatus;
    }

    /**
     * @return the expected status of the task
     */
    public TaskStatus getExpectedStatus() {
        return this.expectedStatus;
    }

}
