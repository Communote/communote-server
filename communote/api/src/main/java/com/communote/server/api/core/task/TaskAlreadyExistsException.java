package com.communote.server.api.core.task;

/**
 * Thrown to indicate that a task with the same unique name already exists
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class TaskAlreadyExistsException extends TaskManagementException {

    private static final long serialVersionUID = 5190906163995313893L;
    private final String uniqueName;

    public TaskAlreadyExistsException(String uniqueName) {
        super("Task with name " + uniqueName + " already exists");
        this.uniqueName = uniqueName;
    }

    public String getUniqueName() {
        return uniqueName;
    }

}
