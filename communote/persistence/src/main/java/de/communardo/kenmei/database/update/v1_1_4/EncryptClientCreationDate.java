package de.communardo.kenmei.database.update.v1_1_4;

import java.util.Collection;
import java.util.Date;

import liquibase.FileOpener;
import liquibase.change.custom.CustomTaskChange;
import liquibase.database.Database;
import liquibase.exception.CustomChangeException;
import liquibase.exception.InvalidChangeDefinitionException;
import liquibase.exception.SetupException;
import liquibase.exception.UnsupportedChangeException;

import com.communote.common.encryption.EncryptionException;
import com.communote.common.encryption.EncryptionUtils;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.client.ClientTO;
import com.communote.server.api.core.config.ClientConfigurationProperties;
import com.communote.server.api.core.config.type.ApplicationProperty;
import com.communote.server.api.core.config.type.ClientProperty;
import com.communote.server.api.service.ClientRetrievalService;
import com.communote.server.persistence.user.client.ClientHelper;

/**
 * This encrypts and stores the clients creation date within the client configuration settings.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class EncryptClientCreationDate implements CustomTaskChange {
    private String confirmationMessage;

    /**
     * @param database
     *            The current database.
     *
     * @throws CustomChangeException
     *             Exception.
     *
     * @throws UnsupportedChangeException
     *             Exception.
     */
    @Override
    public void execute(Database database) throws CustomChangeException, UnsupportedChangeException {
        // don't run it on newly installed systems (i.e. installed with installer); good metric is
        // whether to check whether there are clients, if not it's a new installation
        Collection<ClientTO> clients = ServiceLocator.findService(ClientRetrievalService.class)
                .getAllClients();
        if (clients == null || clients.size() == 0) {
            confirmationMessage = "Skipping " + EncryptClientCreationDate.class.getSimpleName()
                    + " update task on new installation";
            return;
        }
        ClientConfigurationProperties clientConfigurationProperties = CommunoteRuntime
                .getInstance().getConfigurationManager().getClientConfigurationProperties();
        String encryptedCreationDate = clientConfigurationProperties
                .getProperty(ClientProperty.CREATION_DATE);
        ClientTO currentClient = ClientHelper.getCurrentClient();
        if (encryptedCreationDate == null) { // Only if it still doesn't exist.
            try {
                Date time = currentClient.getCreationDate();
                if (time == null) {
                    time = new Date(0);
                }
                encryptedCreationDate = EncryptionUtils.encrypt(Long.toString(time.getTime()),
                        ApplicationProperty.INSTALLATION_UNIQUE_ID.getValue());
                CommunoteRuntime
                .getInstance()
                .getConfigurationManager()
                .updateClientConfigurationProperty(ClientProperty.CREATION_DATE,
                        encryptedCreationDate);
            } catch (EncryptionException e) {
                throw new CustomChangeException(e);
            }
        }
        confirmationMessage = "Updated encrypted creation date..";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getConfirmationMessage() {
        return confirmationMessage;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setFileOpener(FileOpener arg0) {
        // Do nothing.
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setUp() throws SetupException {
        // Do nothing.
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validate(Database arg0) throws InvalidChangeDefinitionException {
        // Do nothing.
    }
}
