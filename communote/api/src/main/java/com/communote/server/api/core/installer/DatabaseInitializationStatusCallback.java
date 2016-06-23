package com.communote.server.api.core.installer;

import com.communote.server.api.core.config.database.DatabaseConnectionException;

/**
 * Callback interface to inform the caller of the status of the initialization of the database.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public interface DatabaseInitializationStatusCallback {

    /**
     * Called to inform that the schema is going to be created on the global database.
     */
    public void creatingSchema();

    /**
     * Called to inform that the schema creation failed.
     */
    public void creatingSchemaFailed();

    /**
     * Called to inform that the schema creation succeeded.
     */
    public void creatingSchemaSucceeded();

    /**
     * Called to inform that database initialization has started.
     */
    public void databaseInitialization();

    /**
     * Called to inform that database initialization has already been successfully completed.
     */
    public void databaseInitializationAlreadyDone();

    /**
     * Called to inform that database initialization has been successfully finished.
     *
     * @param success
     *            the status of the database initialization
     */
    public void databaseInitializationFinished(boolean success);

    /**
     * Called to inform that the connection to the global database is going to be established.
     */
    public void establishingConnection();

    /**
     * Called to inform that the connection to the global database could not be established.
     *
     * @param cause
     *            the cause for the failure
     */
    public void establishingConnectionFailed(DatabaseConnectionException cause);

    /**
     * Called to inform that the connection to the global database could be established
     * successfully.
     */
    public void establishingConnectionSucceeded();

    /**
     * Called to inform that the installation is being prepared.
     */
    public void preparingInstallation();

    /**
     * Called to inform that preparing the installation failed.
     */
    public void preparingInstallationFailed();

    /**
     * Called to inform that preparing the installation was successful.
     */
    public void preparingInstallationSucceeded();

    /**
     * Called to inform that initial data like country names and language names is stored in the
     * database.
     */
    public void writingInitialData();

    /**
     * Called to inform that storing the initial data failed.
     */
    public void writingInitialDataFailed();

    /**
     * Called to inform that storing the initial data succeeded.
     */
    public void writingInitialDataSucceeded();

}
