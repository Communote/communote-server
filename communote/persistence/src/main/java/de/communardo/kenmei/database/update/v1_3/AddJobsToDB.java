package de.communardo.kenmei.database.update.v1_3;

import java.util.Date;

import liquibase.FileOpener;
import liquibase.change.custom.CustomTaskChange;
import liquibase.database.Database;
import liquibase.exception.CustomChangeException;
import liquibase.exception.InvalidChangeDefinitionException;
import liquibase.exception.SetupException;
import liquibase.exception.UnsupportedChangeException;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.task.TaskManagement;
import com.communote.server.api.core.task.TaskManagementException;

/**
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class AddJobsToDB implements CustomTaskChange {

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(Database arg0) throws CustomChangeException, UnsupportedChangeException {
        TaskManagement taskManagement = ServiceLocator.findService(TaskManagement.class);
        // now + 5 minutes.
        Long startDate = System.currentTimeMillis() + 5 * 60 * 1000;
        try {
            taskManagement.addTask("SynchronizeGroups", true, 3600000L, new Date(startDate),
                    "com.communote.server.core.user.groups.UserGroupSynchronizationTaskHandler");
            taskManagement.addTask("RemindUsers", true, 8640000L, new Date(startDate),
                    "com.communote.server.core.user.RemindUserJob");
        } catch (TaskManagementException e) {
            throw new CustomChangeException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getConfirmationMessage() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setFileOpener(FileOpener arg0) {
        // nothing
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setUp() throws SetupException {
        // nothing

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validate(Database arg0) throws InvalidChangeDefinitionException {
        // nothing

    }

}
