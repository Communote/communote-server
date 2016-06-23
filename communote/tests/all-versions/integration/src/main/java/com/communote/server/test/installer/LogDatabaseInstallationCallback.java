package com.communote.server.test.installer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.communote.server.api.core.config.database.DatabaseConnectionException;
import com.communote.server.api.core.installer.DatabaseInitializationStatusCallback;

/**
 * Logs all infos.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class LogDatabaseInstallationCallback implements DatabaseInitializationStatusCallback {

    /** Logger. */
    private final static Logger LOG = LoggerFactory
            .getLogger(LogDatabaseInstallationCallback.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public void creatingSchema() {
        LOG.info("Started creating schema.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void creatingSchemaFailed() {
        LOG.info("Schema creation failed.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void creatingSchemaSucceeded() {
        LOG.info("Schema creation succeed.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void databaseInitialization() {
        LOG.info("Initializing database.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void databaseInitializationAlreadyDone() {
        LOG.info("Database initializing already done.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void databaseInitializationFinished(boolean success) {
        LOG.info("Database initializing finished.");

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void establishingConnection() {
        LOG.info("Establishing connection.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void establishingConnectionFailed(DatabaseConnectionException cause) {
        LOG.info("Establishing connection failed.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void establishingConnectionSucceeded() {
        LOG.info("Establishing connection succeeded.");

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void preparingInstallation() {
        LOG.info("Preparing installation.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void preparingInstallationFailed() {
        LOG.info("Preparing installation failed.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void preparingInstallationSucceeded() {
        LOG.info("Preparing installation succeeded.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void writingInitialData() {
        LOG.info("Writing initial data.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void writingInitialDataFailed() {
        LOG.info("Writing initial data failed.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void writingInitialDataSucceeded() {
        LOG.info("Writing initial data succeeded.");
    }
}
