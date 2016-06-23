package de.communardo.kenmei.database.update;

import java.util.Collection;

import liquibase.change.custom.CustomTaskChange;
import liquibase.database.Database;
import liquibase.exception.CustomChangeException;
import liquibase.exception.UnsupportedChangeException;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.client.ClientTO;
import com.communote.server.api.service.ClientRetrievalService;

import de.communardo.kenmei.database.update.v1_1_4.EncryptClientCreationDate;

/**
 * This task change runs only when there are already clients. For example it doesn't runs directly
 * after a fresh installation.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public abstract class RunOnClientCustomTaskChange implements CustomTaskChange {

    private String confirmationMessage;

    /**
     * If there are already clients this method will delegate to {@link #handleExecute(Database)},
     * else it will return.
     *
     * {@inheritDoc}
     */
    @Override
    public void execute(Database database) throws CustomChangeException, UnsupportedChangeException {
        // don't run it on newly installed systems (i.e. installed with installer); good metric is
        // whether to check whether there are clients, if not it's a new installation
        Collection<ClientTO> clients = ServiceLocator.findService(ClientRetrievalService.class)
                .getAllClients();
        if (clients == null || clients.size() == 0) {
            setConfirmationMessage("Skipping " + EncryptClientCreationDate.class.getSimpleName()
                    + " update task on new installation");
            return;
        }
        handleExecute(database);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getConfirmationMessage() {
        return confirmationMessage;
    }

    /**
     * The intention of this method is the intention of super{@link #execute(Database)}.
     *
     * @param database
     *            The database.
     * @throws CustomChangeException
     *             Exception.
     * @throws UnsupportedChangeException
     *             Exception.
     */
    protected abstract void handleExecute(Database database) throws CustomChangeException,
    UnsupportedChangeException;

    /**
     * @param confirmationMessage
     *            the confirmationMessage to set
     */
    public void setConfirmationMessage(String confirmationMessage) {
        this.confirmationMessage = confirmationMessage;
    }
}
