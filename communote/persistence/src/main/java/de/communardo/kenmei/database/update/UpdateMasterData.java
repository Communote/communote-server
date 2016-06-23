/**
 *
 */
package de.communardo.kenmei.database.update;

import liquibase.FileOpener;
import liquibase.change.custom.CustomTaskChange;
import liquibase.database.Database;
import liquibase.exception.CustomChangeException;
import liquibase.exception.InvalidChangeDefinitionException;
import liquibase.exception.SetupException;
import liquibase.exception.UnsupportedChangeException;

import com.communote.server.api.ServiceLocator;
import com.communote.server.core.user.MasterDataManagement;

/**
 * Update task that brings the MasterData stored in the client database to the latest state of the
 * properties files.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class UpdateMasterData implements CustomTaskChange {

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(Database arg0) throws CustomChangeException, UnsupportedChangeException {
        // no special precautions for new clients required
        ServiceLocator.findService(MasterDataManagement.class).postInitialization();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getConfirmationMessage() {
        // nothing
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
