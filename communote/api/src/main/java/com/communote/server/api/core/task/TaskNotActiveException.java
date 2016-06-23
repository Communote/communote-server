package com.communote.server.api.core.task;

/**
 * Indicates that the task is not active.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class TaskNotActiveException extends TaskManagementException {

    private static final long serialVersionUID = 6992198243066776123L;

    public TaskNotActiveException(String uniqueName) {
        super("Task with name " + uniqueName + " is not active");
    }
}
