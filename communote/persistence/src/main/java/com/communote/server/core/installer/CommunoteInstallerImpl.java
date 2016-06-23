package com.communote.server.core.installer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.bootstrap.ApplicationInitializationException;
import com.communote.server.api.core.client.ClientNotFoundException;
import com.communote.server.api.core.client.ClientTO;
import com.communote.server.api.core.client.InvalidClientIdException;
import com.communote.server.api.core.common.EmailValidationException;
import com.communote.server.api.core.config.ConfigurationInitializationException;
import com.communote.server.api.core.config.ConfigurationManager;
import com.communote.server.api.core.config.ConfigurationUpdateException;
import com.communote.server.api.core.config.CoreConfigurationPropertyConstant;
import com.communote.server.api.core.config.StartupProperties;
import com.communote.server.api.core.config.database.DatabaseConnectionException;
import com.communote.server.api.core.config.database.DatabaseType;
import com.communote.server.api.core.config.type.ApplicationProperty;
import com.communote.server.api.core.config.type.CorePropertyDatabase;
import com.communote.server.api.core.installer.CommunoteInstaller;
import com.communote.server.api.core.installer.CommunoteInstallerException;
import com.communote.server.api.core.installer.DatabaseInitializationStatusCallback;
import com.communote.server.api.core.user.UserVO;
import com.communote.server.api.service.ClientRetrievalService;
import com.communote.server.core.bootstrap.ApplicationInitializer;
import com.communote.server.core.common.database.DatabaseUpdateException;
import com.communote.server.core.common.database.DatabaseUpdateType;
import com.communote.server.core.common.database.DatabaseUpdater;
import com.communote.server.core.common.util.DatabaseHelper;
import com.communote.server.core.user.UserManagement;
import com.communote.server.core.user.client.ClientManagementException;
import com.communote.server.model.user.User;
import com.communote.server.model.user.UserRole;
import com.communote.server.model.user.UserStatus;
import com.communote.server.persistence.user.client.ClientHelper;
import com.communote.server.service.ClientCreationService;

/**
 * Implementation of the installer.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class CommunoteInstallerImpl implements CommunoteInstaller {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommunoteInstallerImpl.class);
    private final ApplicationInitializer initializer;
    private Boolean globalDatabaseInitialized;
    private final List<DatabaseType> supportedTypes;

    /**
     * Create an installer
     *
     * @param initializer
     *            the initializer that should be called after successfully initializing the
     *            Communote database
     */
    public CommunoteInstallerImpl(ApplicationInitializer initializer) {
        this.initializer = initializer;
        supportedTypes = new ArrayList<>();
        // add built-in types in order of preference
        supportedTypes.add(new DatabaseType("POSTGRESQL", 5432, "org.postgresql.Driver",
                "jdbc:postgresql", "org.hibernate.dialect.PostgreSQLDialect"));
        supportedTypes.add(new DatabaseType("MYSQL", 3306, "com.mysql.jdbc.Driver", "jdbc:mysql",
                "org.hibernate.dialect.MySQL5Dialect"));
        supportedTypes.add(new DatabaseType("MSSQL", 1433, "net.sourceforge.jtds.jdbc.Driver",
                "jdbc:jtds:sqlserver", "org.hibernate.dialect.SQLServerDialect"));
        supportedTypes.add(new DatabaseType("ORACLE", 1521, "oracle.jdbc.OracleDriver",
                "jdbc:oracle:thin", "org.hibernate.dialect.Oracle10gDialect", new String[] {
                        ":@//", ":@" }));
        // supportedTypes.add(new DatabaseType("HSQLDB", 9001,"org.hsqldb.jdbcDriver",
        // "jdbc:hsqldb", "org.hibernate.dialect.PostgreSQLDialect"));
    }

    @Override
    public boolean canConnectToDatabase() {
        try {
            DatabaseHelper.testDatabaseConnection();
            return true;
        } catch (DatabaseConnectionException e) {
            LOGGER.debug("Database connection failed: {}", e.getMessage());
            return false;
        }
    }

    private boolean createAndUpdateDatabaseSchema(
            DatabaseInitializationStatusCallback statusCallback) {
        boolean success = true;
        statusCallback.databaseInitialization();
        if (!isDatabaseInitialized()) {
            DatabaseUpdater databaseUpdater = ServiceLocator.findService(DatabaseUpdater.class);
            try {
                statusCallback.creatingSchema();
                databaseUpdater.execute(DatabaseUpdateType.INSTALLATION);
                // now run the first pass to merge everything to the latest
                // state
                databaseUpdater.execute(DatabaseUpdateType.FIRST_PASS_UPDATE);
                statusCallback.creatingSchemaSucceeded();

            } catch (DatabaseUpdateException e) {
                LOGGER.error("Database schema creation failed", e);
                statusCallback.creatingSchemaFailed();
                success = false;
            }
            if (success) {
                try {
                    statusCallback.writingInitialData();
                    // do the second pass update
                    databaseUpdater.execute(DatabaseUpdateType.SECOND_PASS_UPDATE);
                    statusCallback.writingInitialDataSucceeded();
                    synchronized (this) {
                        globalDatabaseInitialized = Boolean.TRUE;
                    }
                } catch (Exception e) {
                    LOGGER.error("Writing initial data failed", e);
                    statusCallback.writingInitialDataFailed();
                    success = false;
                }
            }
            statusCallback.databaseInitializationFinished(success);
        } else {
            statusCallback.databaseInitializationAlreadyDone();
        }

        return success;
    }

    @Override
    public void createCommunoteAccount(String accountName, String timezoneId)
            throws InvalidClientIdException, CommunoteInstallerException {
        try {
            ServiceLocator.findService(ClientCreationService.class).createGlobalClient(accountName,
                    timezoneId);
        } catch (ClientManagementException e) {
            throw new CommunoteInstallerException("Creating the global client failed", e);
        }
    }

    @Override
    public User getAdminAccount() {
        UserManagement um = ServiceLocator.instance().getService(UserManagement.class);

        List<User> managerList = um.findUsersByRole(UserRole.ROLE_KENMEI_CLIENT_MANAGER,
                UserStatus.ACTIVE);
        if (!managerList.isEmpty()) {
            return managerList.get(0);
        }
        return null;
    }

    @Override
    public ClientTO getCommunoteAccount() {
        try {
            return ServiceLocator.findService(ClientRetrievalService.class).findClient(
                    ClientHelper.getGlobalClientId());
        } catch (ClientNotFoundException e) {
            return null;
        }
    }

    @Override
    public StartupProperties getDatabaseSettings() {
        return CommunoteRuntime.getInstance().getConfigurationManager().getStartupProperties();
    }

    @Override
    public DatabaseType getDatabaseType() {
        StartupProperties startupProps = getDatabaseSettings();
        return getDatabaseTypeFromDriverClass(startupProps.getDatabaseDriverClassName());
    }

    private DatabaseType getDatabaseTypeFromDriverClass(String driverClassName) {
        if (driverClassName != null) {
            for (DatabaseType type : supportedTypes) {
                if (StringUtils.equalsIgnoreCase(driverClassName, type.getDriverClassName())) {
                    return type;
                }
            }
        }
        return null;
    }

    @Override
    public DatabaseType getSupportedDatabaseType(String identifier) {
        for (DatabaseType type : this.supportedTypes) {
            if (type.getIdentifier().equals(identifier)) {
                return type;
            }
        }
        return null;
    }

    @Override
    public List<DatabaseType> getSupportedDatabaseTypes() {
        return supportedTypes;
    }

    @Override
    public void initializeApplicationAfterInstallation() throws ApplicationInitializationException {
        if (CommunoteRuntime.getInstance().getConfigurationManager().getStartupProperties()
                .isInstallationDone()) {
            initializer.initializeApplication();
        } else {
            throw new ApplicationInitializationException("Installation not completed",
                    (Throwable) null);
        }
    }

    @Override
    public void initializeCommunoteAccount(UserVO adminAccount,
            Map<ApplicationProperty, String> params) throws EmailValidationException,
            ConfigurationInitializationException, CommunoteInstallerException {
        adminAccount.setRoles(new UserRole[] { UserRole.ROLE_KENMEI_CLIENT_MANAGER,
                UserRole.ROLE_KENMEI_USER });
        try {
            ServiceLocator.findService(ClientCreationService.class).initializeGlobalClient(
                    adminAccount, params);
        } catch (EmailValidationException e) {
            LOGGER.error("Admin account email address could not be validated", e);
            throw e;
        } catch (ConfigurationInitializationException e) {
            LOGGER.error(
                    "Initializing the global client with the provided configuration settings failed",
                    e);
            throw e;
        } catch (Exception e) {
            LOGGER.error("Initializing the global client failed", e);
            throw new CommunoteInstallerException("Initializing the global client failed", e);
        }
    }

    @Override
    public boolean initializeDatabase(DatabaseInitializationStatusCallback statusCallback) {
        try {
            statusCallback.establishingConnection();
            DatabaseHelper.testDatabaseConnection();
            statusCallback.establishingConnectionSucceeded();

            // wrap next call in status callback calls, because the init of all
            // the spring beans will happen here and this takes some time
            statusCallback.preparingInstallation();
            initializer.createApplicationContext();
            statusCallback.preparingInstallationSucceeded();
            return createAndUpdateDatabaseSchema(statusCallback);
        } catch (DatabaseConnectionException e) {
            statusCallback.establishingConnectionFailed(e);
        }
        return false;
    }

    @Override
    public boolean isDatabaseInitialized() {
        if (globalDatabaseInitialized == null) {
            if (CommunoteRuntime.getInstance().getConfigurationManager().getStartupProperties()
                    .isInstallationDone()) {
                globalDatabaseInitialized = Boolean.TRUE;
            } else {
                // there is no easy way to decide whether the database is already initialized. In
                // case of liquibase we would have to check whether ALL changesets were run. The
                // DatabaseUpdater#updaterRunBefore only returns whether the updater ran and not
                // whether it completed all steps.
                globalDatabaseInitialized = Boolean.FALSE;
            }
        }
        return globalDatabaseInitialized.booleanValue();
    }

    @Override
    public StartupProperties updateDatabaseSettings(DatabaseType dbType,
            Map<CoreConfigurationPropertyConstant, String> settings)
            throws ConfigurationUpdateException {
        if (dbType != null) {
            settings.put(CorePropertyDatabase.DATABASE_DRIVER_CLASS_NAME,
                    dbType.getDriverClassName());
            settings.put(CorePropertyDatabase.DATABASE_PROTOCOL, dbType.getProtocol());
            settings.put(CorePropertyDatabase.HIBERNATE_DIALECT, dbType.getDialectClassName());
            // set type specific protocol separator if default one is not supported
            if (!dbType.supportsProtocolSeparator(StartupProperties.DEFAULT_PROTOCOL_SEPARATOR)) {
                settings.put(CorePropertyDatabase.DATABASE_PROTOCOL_SEPARATOR,
                        dbType.getDefaultProtocolSeperator());
            } else {
                settings.put(CorePropertyDatabase.DATABASE_PROTOCOL_SEPARATOR, null);
            }
            if (dbType.getIdentifier().equals("MSSQL")) {
                String dbName = settings.get(CorePropertyDatabase.DATABASE_NAME);
                if (dbName != null && !dbName.contains(";prepareSQL=2")) {
                    settings.put(CorePropertyDatabase.DATABASE_NAME, dbName + ";prepareSQL=2");
                }
            }
        }
        ConfigurationManager propertiesManager = CommunoteRuntime.getInstance()
                .getConfigurationManager();
        propertiesManager.updateStartupProperties(settings);
        return propertiesManager.getStartupProperties();
    }
}
