package com.communote.server.api.core.installer;

import java.util.List;
import java.util.Map;

import com.communote.server.api.core.bootstrap.BootstrapException;
import com.communote.server.api.core.client.ClientTO;
import com.communote.server.api.core.client.InvalidClientIdException;
import com.communote.server.api.core.common.EmailValidationException;
import com.communote.server.api.core.config.ConfigurationInitializationException;
import com.communote.server.api.core.config.ConfigurationUpdateException;
import com.communote.server.api.core.config.CoreConfigurationPropertyConstant;
import com.communote.server.api.core.config.StartupProperties;
import com.communote.server.api.core.config.database.DatabaseType;
import com.communote.server.api.core.config.type.ApplicationProperty;
import com.communote.server.api.core.user.UserVO;
import com.communote.server.model.user.User;

/**
 * Helper to install Communote. The methods should be called in the following order
 * <ul>
 * <li>{@link #updateDatabaseSettings(Map)}</li>
 * <li>{@link #initializeDatabase(DatabaseInitializationStatusCallback)}</li>
 * <li>{@link #createCommunoteAccount(String, String)}</li>
 * <li>{@link #initializeCommunoteAccount(UserVO, Map)}</li>
 * <li>{@link #initializeApplicationAfterInstallation()}</li>
 * </ul>
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public interface CommunoteInstaller {

    /**
     * @return whether it is possible to connect to the database with the current database
     *         configuration settings as returned by {@link #getDatabaseSettings()}
     */
    public boolean canConnectToDatabase();

    /**
     * Create the Communote account (aka the global client).
     *
     * @throws InvalidClientIdException
     *             in case the ID of the client is not valid
     * @throws CommunoteInstallerException
     *             in case creating the client failed
     */
    public void createCommunoteAccount(String accountName, String timezoneId)
            throws InvalidClientIdException, CommunoteInstallerException;

    /**
     * @return an existing administrator user or null if there is none yet
     */
    public User getAdminAccount();

    /**
     * @return the details of the Communote account (aka global client) or null if the account has
     *         not been created yet
     */
    public ClientTO getCommunoteAccount();

    /**
     * @return the current database connection settings
     */
    public StartupProperties getDatabaseSettings();

    /**
     * @return the database type of the current database connection settings
     */
    public DatabaseType getDatabaseType();

    /**
     * Get the database type for the given identifier
     *
     * @param identifier
     *            the identifier of the type
     *
     * @return the database type or null if there is no matching type
     */
    public DatabaseType getSupportedDatabaseType(String identifier);

    /**
     * @return get a list of supported database types
     */
    public List<DatabaseType> getSupportedDatabaseTypes();

    /**
     * Initialize the application if all steps of the installation were completed.
     *
     * @throws BootstrapException
     *             in case not all steps of the installation were completed
     */
    public void initializeApplicationAfterInstallation() throws BootstrapException;

    /**
     * Initialize the Communote account (aka global client) that was previously created with
     * {@link #createCommunoteAccount(String, String)}
     *
     * @param adminAccount
     *            VO with details of the first Communote user with administrative privileges
     * @param params
     *            application parameters to initialize the Communote account with
     * @throws EmailValidationException
     *             in case the email address of the administrator user is invalid
     * @throws ConfigurationInitializationException
     *             in case the provided application parameters caused an error
     * @throws CommunoteInstallerException
     *             in case the initialization failed
     */
    public void initializeCommunoteAccount(UserVO adminAccount,
            Map<ApplicationProperty, String> params) throws EmailValidationException,
            ConfigurationInitializationException, CommunoteInstallerException;

    /**
     * Initialize the application database and inform the provided callback about the current
     * status.
     *
     * @param statusCallback
     *            the callback to receive status updates
     * @return true if the initialization succeeded, false otherwise
     */
    public boolean initializeDatabase(DatabaseInitializationStatusCallback statusCallback);

    /**
     * Returns whether the global database is initialized, which is the schema exists.
     *
     * @return true if the database is initialized
     */
    public boolean isDatabaseInitialized();

    /**
     * Update the database settings and return the resulting configuration.
     *
     * @param type
     *            the database type
     * @param settings
     *            the settings like host and username for connecting to the database of the given
     *            type
     * @return the new configuration
     * @throws ConfigurationUpdateException
     *             in case the configuration cannot be saved
     */
    public StartupProperties updateDatabaseSettings(DatabaseType type,
            Map<CoreConfigurationPropertyConstant, String> settings)
            throws ConfigurationUpdateException;

}