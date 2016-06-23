package de.communardo.kenmei.database.update.v3_1;

import java.util.Date;

import liquibase.FileOpener;
import liquibase.change.custom.CustomTaskChange;
import liquibase.database.Database;
import liquibase.exception.CustomChangeException;
import liquibase.exception.InvalidChangeDefinitionException;
import liquibase.exception.SetupException;
import liquibase.exception.UnsupportedChangeException;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.task.TaskAlreadyExistsException;
import com.communote.server.api.core.task.TaskAlreadyRunningException;
import com.communote.server.api.core.task.TaskManagement;
import com.communote.server.core.tasks.DeleteOrphanedAttachmentsTaskHandler;

/**
 * Custom change to add the task handler for deleting the orphaned attachments from the database.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class AddDeleteOrphanedAttachmentsTaskHandlerToDB implements CustomTaskChange {

    private final String deprecatedTaskName = "deleteOrphanedAttachmentsClassHandler";

    @Override
    public void execute(Database arg0) throws CustomChangeException, UnsupportedChangeException {
        TaskManagement taskManagement = ServiceLocator.findService(TaskManagement.class);
        if (taskManagement.findTask(deprecatedTaskName) != null) {
            try {
                taskManagement.removeTask(deprecatedTaskName);
            } catch (TaskAlreadyRunningException e) {
                throw new CustomChangeException("Cannot remove the old task " + deprecatedTaskName
                        + " because it is already running. Please delete it manually.");
            }
        }
        // add the task with a little offset from now to shift DB activities, especially on SaaS,
        // away from Communote start
        Date nextExecutionDate = new Date(System.currentTimeMillis() + 10800000);
        try {
            taskManagement.addTask("DeleteOrphanedAttachments", true, 604800000L,
                    nextExecutionDate,
                    DeleteOrphanedAttachmentsTaskHandler.class);
        } catch (TaskAlreadyExistsException e) {
            throw new CustomChangeException("Task already exists", e);
        }
    }

    @Override
    public String getConfirmationMessage() {
        return null;
    }

    @Override
    public void setFileOpener(FileOpener arg0) {
        // nothing
    }

    @Override
    public void setUp() throws SetupException {
        // nothing

    }

    @Override
    public void validate(Database arg0) throws InvalidChangeDefinitionException {
        // nothing

    }

}
