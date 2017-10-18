package com.communote.server.test;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import javax.naming.Context;
import javax.naming.NamingException;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.StringUtils;
import org.easymock.EasyMock;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.XmlWebApplicationContext;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;

import com.communote.common.encryption.EncryptionUtils;
import com.communote.common.io.FileHelper;
import com.communote.common.properties.PropertiesUtils;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.config.ApplicationConfigurationPropertyConstant;
import com.communote.server.api.core.config.ConfigurationManager;
import com.communote.server.api.core.config.database.DatabaseType;
import com.communote.server.api.core.config.type.ApplicationProperty;
import com.communote.server.api.core.config.type.ClientProperty;
import com.communote.server.api.core.config.type.CoreProperty;
import com.communote.server.api.core.config.type.CorePropertyDatabase;
import com.communote.server.api.core.installer.CommunoteInstaller;
import com.communote.server.core.application.DefaultRuntimeBuilder;
import com.communote.server.core.installer.CommunoteInstallerImpl;
import com.communote.server.core.osgi.OSGiManagement;
import com.communote.server.core.security.AuthenticationHelper;
import com.communote.server.test.installer.MockInitialContextFactory;
import com.communote.server.test.util.AuthenticationTestUtils;
import com.communote.server.test.util.DatabaseUtils;
import com.communote.server.web.osgi.LocalizationResolverBundleListener;

/**
 * Abstract test class other tests can inherit from. This class will initialize the application with
 * a newly created global client.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public abstract class CommunoteIntegrationTest {

    /**
     * Name of the TestNG group all inheriting classes can depend on in the setup methods
     * (@BeforeClass).
     */
    protected static final String GROUP_INTEGRATION_TEST_SETUP = "integration-test-setup";

    /**
     * This must be changed for every version.
     *
     * Idea: Another idea is to copy the needed bundles via maven into a specific target directory
     * and read the bundles from there. No need to configure directory here then.
     */
    // TODO How to improve this resolution
    public static final String COMMUNOTE_VERSION = System.getProperty("project.version", "3.5");

    private static final Logger LOGGER = LoggerFactory.getLogger(CommunoteIntegrationTest.class);

    private final static String UNIQUE_CLIENT_ID = UUID.randomUUID().toString();

    private String host;
    private String port;
    private String username;
    private String databaseName;
    private String password;
    private String suUsername;
    private String suPassword;

    private String jdbcURL;
    private String jdbcTempURL;

    private String jdbcURLQueryString;
    private String tempDatabase;

    private XmlWebApplicationContext testWebAppContext;

    private DatabaseType databaseType;

    private File testBaseDir;

    private File applicationRealDir;

    /**
     * Removes old stuff.
     *
     * @param cleanUp
     *            If set clean up will be done. Default is <code>false</code>.
     * @throws Exception
     *             Exception.
     */
    @Parameters({ "cleanUp" })
    @AfterClass(groups = GROUP_INTEGRATION_TEST_SETUP)
    public void cleanUp(@Optional("false") String cleanUp) throws Exception {
        if (!Boolean.parseBoolean(cleanUp)) {
            return;
        }
        CommunoteRuntime.getInstance().stop();
        testWebAppContext.close();
        DatabaseUtils.dropDatabase(jdbcTempURL, suUsername, suPassword, databaseName, databaseType,
                false);
    }

    @AfterClass
    public void cleanUpAuthenticationAfterClass() {
        AuthenticationHelper.removeAuthentication();
    }

    /**
     * Create the configuration directory with startup- and core-properties. The directory is
     * created as an instance specific sub directory of the testbasedir.
     *
     * @param instanceName
     *            The instances name.
     *
     * @return the absolute path to the created configuration directory
     * @throws Exception
     *             in case the creation failed
     */
    private String createConfigurationDirectory(String instanceName) throws Exception {
        File instanceBaseDir = new File(testBaseDir, instanceName);
        File configDir = new File(instanceBaseDir, "config");
        configDir.mkdirs();
        File dataDir = new File(instanceBaseDir, "data");
        dataDir.mkdirs();
        Properties startupProperties = new Properties();
        startupProperties.setProperty("communote.data.dir", dataDir.getAbsolutePath());
        startupProperties.setProperty("communote.plugin.dir",
                new File(instanceBaseDir, "plugins").getAbsolutePath());
        File startupPropertiesFile = new File(configDir, "startup.properties");
        startupPropertiesFile.createNewFile();
        PropertiesUtils.storePropertiesToFile(startupProperties, startupPropertiesFile);

        Properties developmentProperties = new Properties();
        // ensure no mails are sent
        developmentProperties.setProperty("mailout.mode", "filesystem");
        File developmentPropertiesFile = new File(configDir, "development.properties");
        developmentPropertiesFile.createNewFile();
        PropertiesUtils.storePropertiesToFile(developmentProperties, developmentPropertiesFile);

        Properties coreProperties = createCoreProperties();
        File corePropertiesFile = new File(dataDir, "core.properties");
        corePropertiesFile.createNewFile();
        PropertiesUtils.storePropertiesToFile(coreProperties, corePropertiesFile);
        return configDir.getAbsolutePath();
    }

    /**
     * Create and fill the core properties. Will be triggered by
     * {@link #setupApplication(String, String)} if skipApplicationInitialization option is false.
     *
     * @return the prepared properties
     */
    protected Properties createCoreProperties() {
        Properties coreProperties = new Properties();
        coreProperties.setProperty(CorePropertyDatabase.DATABASE_DRIVER_CLASS_NAME.getKeyString(),
                databaseType.getDialectClassName());
        coreProperties.setProperty(CorePropertyDatabase.DATABASE_HOST.getKeyString(), host);
        coreProperties.setProperty(CorePropertyDatabase.DATABASE_NAME.getKeyString(), databaseName);
        coreProperties.setProperty(CorePropertyDatabase.DATABASE_PORT.getKeyString(), port);
        coreProperties.setProperty(CorePropertyDatabase.DATABASE_PROTOCOL.getKeyString(),
                databaseType.getProtocol());
        coreProperties.setProperty(CorePropertyDatabase.DATABASE_PROTOCOL_SEPARATOR.getKeyString(),
                databaseType.getDefaultProtocolSeperator());
        coreProperties
        .setProperty(CorePropertyDatabase.DATABASE_USER_NAME.getKeyString(), username);
        coreProperties.setProperty(CorePropertyDatabase.DATABASE_USER_PASSWORD.getKeyString(),
                password);
        coreProperties.setProperty(CorePropertyDatabase.HIBERNATE_DIALECT.getKeyString(),
                databaseType.getDialectClassName());
        coreProperties.setProperty(CoreProperty.INSTALLATION_DONE.getKeyString(), "true");
        return coreProperties;
    }

    /**
     * @return the directory which is extracted in the setupIntegrationTest method as the
     *         application directory
     */
    protected File getApplicationDirectory() {
        return applicationRealDir;
    }

    /**
     * Method to return paths to bundles which should be installed. All bundles have to be installed
     * into the local Maven repository.
     *
     * Idea: Another idea is to copy the needed bundles via maven into a specific target directory
     * and read the bundles from there
     *
     * @return Array of paths to bundles.
     */
    public String[] getBundlePathsWithinMavenRepository() {
        return new String[] { };
    }

    /**
     * @return the type of the database on which the test is running. Will be null if
     *         setupIntegrationTest has not yet been executed.
     */
    protected DatabaseType getDatabaseType() {
        return databaseType;
    }

    /**
     * Initialize the schema from the data.*.sql and post-data.*.sql scripts. This will also add
     * some additional content.
     *
     * @throws Exception
     *             in case the initialization failed
     */
    private void initializeDatabaseSchemaAndContent() throws Exception {
        URL dataSqlResource = DatabaseUtils.getRequiredSqlScriptResource("data", databaseType);
        URL postDataSqlResource = DatabaseUtils.getRequiredSqlScriptResource("post-data",
                databaseType);
        try (Connection connection = DriverManager.getConnection(jdbcURL, username, password)) {
            runDataSqlScript(connection, dataSqlResource, null);
            HashMap<String, String> replacements = new HashMap<String, String>();
            replacements.put("@@UNIQUE_ID@@", UNIQUE_CLIENT_ID);
            Calendar installationDate = Calendar.getInstance();
            installationDate.add(Calendar.DATE, -1);
            replacements.put("@@INSTALLATION_DATE_UNENCRYPTED@@", DateFormat.getDateInstance()
                    .format(installationDate.getTime()));
            replacements.put("@@INSTALLATION_DATE@@", EncryptionUtils.encrypt(
                    Long.toString(installationDate.getTimeInMillis()), UNIQUE_CLIENT_ID));
            runDataSqlScript(connection, postDataSqlResource, replacements);
        }
    }

    /**
     * @return Returns a random string (currently an {@link UUID}).
     */
    public String random() {
        return UUID.randomUUID().toString();
    }

    protected void runDataSqlScript(Connection connection, URL dataSqlResource,
            Map<String, String> replacements) throws IOException, SQLException {
        if (databaseType.getIdentifier().equals("ORACLE")) {
            // ORACLE does not support execution of multiple lines in one statement
            DatabaseUtils.runSqlScriptLineByLine(connection, dataSqlResource, replacements, true);
        } else {
            DatabaseUtils.runSqlScript(connection, dataSqlResource, replacements, true);
        }
    }

    /**
     * Initializes the application.
     *
     * @param instanceName
     *            Name of the instance. Should only contain [a-Z0-9-_.]
     * @param skipApplicationInitialization
     *            If set to <code>true</code>, the application wont' be initialized. (Default:
     *            "false")
     * @throws Exception
     *             Exception.
     */
    @Parameters({ "instanceName", "skipApplicationInitialization" })
    @BeforeClass(dependsOnMethods = "setupDatabase", groups = GROUP_INTEGRATION_TEST_SETUP)
    public void setupApplication(@Optional String instanceName,
            @Optional("false") String skipApplicationInitialization) throws Exception {
        if (Boolean.parseBoolean(skipApplicationInitialization)) {
            return;
        }
        if (instanceName == null || instanceName.length() == 0) {
            instanceName = "communote-test-" + UUID.randomUUID();
        }
        System.setProperty("java.naming.factory.initial", MockInitialContextFactory.class.getName());
        Context mockJeeEnvContext = EasyMock.createMock(Context.class);
        EasyMock.expect(mockJeeEnvContext.lookup("communote.instance.name"))
        .andReturn(instanceName);
        EasyMock.expect(mockJeeEnvContext.lookup("communote.config.dir")).andReturn(
                createConfigurationDirectory(instanceName));
        EasyMock.replay(mockJeeEnvContext);
        Context mockInitialContext = EasyMock.createMock(Context.class);
        EasyMock.expect(mockInitialContext.lookup("java:comp/env")).andReturn(mockJeeEnvContext);
        // springs webapp context is using the InitialContext too while refreshing the webapp
        // context, thus mock lookup and close methods
        EasyMock.expect(mockInitialContext.lookup(EasyMock.not(EasyMock.eq("java:comp/env"))))
        .andThrow(new NamingException()).anyTimes();
        mockInitialContext.close();
        EasyMock.expectLastCall().anyTimes();
        EasyMock.replay(mockInitialContext);
        MockInitialContextFactory.setMockContext(mockInitialContext);

        // create Communote runtime with additional test beans
        DefaultRuntimeBuilder runtimeBuilder = DefaultRuntimeBuilder.getInstance();
        runtimeBuilder
        .addApplicationContextConfigLocation("classpath:/com/communote/server/test/spring/applicationContext-test.xml");
        runtimeBuilder.setApplicationDirectory(applicationRealDir.getAbsolutePath());
        CommunoteRuntime.init(runtimeBuilder);
        // create a webapplication context with custom test beans. This context is needed by the
        // plugins.
        this.testWebAppContext = new XmlWebApplicationContext();
        testWebAppContext
                .setConfigLocation("classpath:/com/communote/server/test/spring/webApplicationContext-test.xml");
        // Add a precondition to wait for our test webapp context.
        CommunoteRuntime.getInstance().addInitializationCondition(
                "TEST_WEB_APP_CONTEXT_INITIALIZED");
        CommunoteRuntime.getInstance().start();
        // expose test packages for testing plugins
        ServiceLocator
        .instance()
        .getService(OSGiManagement.class)
        .addFrameworkPropertiesLocation(
                "classpath:/com/communote/server/test/osgi/test_osgi.properties");
        // webapp context must be refreshed manually
        testWebAppContext.refresh();
        // webapp context is ready -> precondition is fulfilled
        CommunoteRuntime.getInstance().fulfillInitializationCondition(
                "TEST_WEB_APP_CONTEXT_INITIALIZED");
        // enable automatic user activation
        ConfigurationManager confManager = CommunoteRuntime.getInstance().getConfigurationManager();
        confManager.updateClientConfigurationProperty(ClientProperty.AUTOMATIC_USER_ACTIVATION,
                "true");
        // set appropriate dir for file uploads
        File dataDir = confManager.getStartupProperties().getDataDirectory();
        File fileDir = new File(dataDir, "filerepository");
        FileHelper.validateDir(fileDir);
        HashMap<ApplicationConfigurationPropertyConstant, String> settings = new HashMap<ApplicationConfigurationPropertyConstant, String>();
        settings.put(ApplicationProperty.FILE_SYSTEM_REPOSITORY_STORAGE_DIR_ROOT,
                fileDir.getAbsolutePath());
        AuthenticationTestUtils.setManagerContext();
        confManager.updateApplicationConfigurationProperties(settings);
        AuthenticationHelper.removeAuthentication();
    }

    /**
     * This method injects services which are annotated with @Autowired in sub classes.
     *
     * @throws IllegalAccessException
     *             see {@link Field#set(Object, Object)}
     * @throws IllegalArgumentException
     *             see {@link Field#set(Object, Object)}
     */
    @BeforeClass(dependsOnMethods = "setupApplication", groups = GROUP_INTEGRATION_TEST_SETUP)
    public void setupAutowiredServices() throws IllegalArgumentException, IllegalAccessException {
        for (Field field : getClass().getDeclaredFields()) {
            if (field.getAnnotation(Autowired.class) != null) {
                field.setAccessible(true);
                field.set(this, ServiceLocator.instance().getService(field.getType()));
                field.setAccessible(false);
            }
        }
    }

    /**
     * Drop and recreates the databaseName from the template files.
     *
     * @param skipDatabaseCreation
     *            If set to true, the databaseName creation will be skipped (Default: false).
     *
     * @throws Exception
     *             Exception.
     */
    @Parameters({ "skipDatabaseCreation" })
    @BeforeClass(dependsOnMethods = { "setupIntegrationTest" }, groups = GROUP_INTEGRATION_TEST_SETUP)
    public void setupDatabase(@Optional("false") String skipDatabaseCreation) throws Exception {
        if (BooleanUtils.toBoolean(skipDatabaseCreation)) {
            return;
        }
        LOGGER.info("Using the following JDBC URL for the test database: " + jdbcURL);
        try {
            DatabaseUtils.recreateDatabase(jdbcTempURL, suUsername, suPassword, databaseName,
                    databaseType, username);
            initializeDatabaseSchemaAndContent();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Setup credentials for DB access.
     *
     * @param dbUsername
     *            The username to use. (Default: communote).
     * @param dbPassword
     *            The passwort to use for the given user (Default: communote).
     * @param dbSuUsername
     *            name of the user to use for dropping an existing databaseName and creating a new
     *            databaseName. This user also needs to have access to the temp databaseName. If
     *            unset, username will be used.
     * @param dbSuPassword
     *            password of the user identified by suUsername. Will be the password parameter if
     *            suUsername is blank.
     */
    @Parameters({ "dbUsername", "dbPassword", "dbSuUsername", "dbSuPassword" })
    @BeforeClass(groups = GROUP_INTEGRATION_TEST_SETUP)
    public void setupDatabaseUser(@Optional("communote") String dbUsername,
            @Optional("communote") String dbPassword, @Optional("") String dbSuUsername,
            @Optional("") String dbSuPassword) {
        this.username = dbUsername;
        this.password = dbPassword;
        if (StringUtils.isBlank(dbSuUsername)) {
            this.suUsername = dbUsername;
            this.suPassword = dbPassword;
        } else {
            this.suUsername = dbSuUsername;
            this.suPassword = dbSuPassword;
        }
    }

    /**
     * Setups the class. Default configuration is for a locally installed PostgreSQL databaseName.
     *
     * @param testBaseDirectory
     *            the base directory of the test. This directory must contain a subdirectory named
     *            communote which contains META-INF/MANIFEST.MF.
     * @param dbTypeIdentifier
     *            Identifier of the database type to use. (Default: POSTGRESQL)
     * @param dbHost
     *            The databaseName host (Default: localhost).
     * @param dbPort
     *            The databaseName port, can be empty to use the default port (Default: empty).
     * @param jdbcURLQueryString
     *            the jdbc url query string (Default: "")
     * @param dbName
     *            The name of the databaseName to run the tests on (Default:
     *            communote_integration_test).
     * @param tempDbName
     *            A temporary databaseName needed for connecting while deleting and creating the
     *            main databaseName (Default: postgres).
     *
     * @throws Exception
     *             Exception.
     */
    @Parameters({ "testBaseDirectory", "dbTypeIdentifier", "dbHost", "dbPort",
            "jdbcURLQueryString", "dbName", "tempDbName" })
    @BeforeClass(dependsOnMethods = "setupDatabaseUser", groups = GROUP_INTEGRATION_TEST_SETUP)
    public void setupIntegrationTest(String testBaseDirectory,
            @Optional("POSTGRESQL") String dbTypeIdentifier, @Optional("localhost") String dbHost,
            @Optional("") String dbPort, @Optional("") String jdbcURLQueryString,
            @Optional("communote_integration_test") String dbName,
            @Optional("postgres") String tempDbName) throws Exception {

        LOGGER.info("Running integration test: {}", getClass().getName());
        testBaseDir = new File(testBaseDirectory);
        Assert.assertTrue(testBaseDir.isDirectory(), "Test base " + testBaseDir.getAbsolutePath()
                + " is not an directory");
        applicationRealDir = new File(testBaseDir, "communote");
        File manifestFile = new File(applicationRealDir, "META-INF" + File.separator
                + "MANIFEST.MF");
        Assert.assertTrue(manifestFile.exists(), "Manifest file " + manifestFile.getAbsolutePath()
                + " does not exist");

        CommunoteInstaller installer = new CommunoteInstallerImpl(null);
        DatabaseType dbType = installer.getSupportedDatabaseType(dbTypeIdentifier);
        Assert.assertNotNull(dbType, "The dbTypeIdentifier " + dbTypeIdentifier
                + " is not among the supported databaseName types");
        Class.forName(dbType.getDriverClassName());

        this.databaseType = dbType;
        this.jdbcURLQueryString = jdbcURLQueryString;

        this.host = dbHost;
        if (StringUtils.isNotBlank(dbPort)) {
            Assert.assertTrue(NumberUtils.isDigits(dbPort));
            this.port = dbPort;
        } else {
            this.port = String.valueOf(dbType.getDefaultPort());
        }

        this.databaseName = dbName;
        this.tempDatabase = tempDbName;

        this.jdbcURL = dbType.getProtocol() + dbType.getDefaultProtocolSeperator() + this.host
                + ":" + this.port + "/" + this.databaseName + this.jdbcURLQueryString;
        this.jdbcTempURL = dbType.getProtocol() + dbType.getDefaultProtocolSeperator() + this.host
                + ":" + this.port + "/" + this.tempDatabase + this.jdbcURLQueryString;
    }

    /**
     * This method setups the OSGiBundles by installing all bundles returned from
     * {@link #getBundlePathsWithinMavenRepository()}
     *
     * @throws Exception
     *             Exception.
     */
    @BeforeClass(dependsOnMethods = "setupApplication", groups = GROUP_INTEGRATION_TEST_SETUP, timeOut = 200000)
    public void setupOSGiBundles() throws Exception {
        OSGiManagement osgiManagement = ServiceLocator.instance().getService(OSGiManagement.class);
        osgiManagement.addListener(new LocalizationResolverBundleListener());
        BundleContext context = osgiManagement.getFramework().getBundleContext();
        String basePath = System.getProperty("user.home") + File.separator + ".m2" + File.separator
                + "repository";
        basePath = System.getProperty("localRepository", basePath) + File.separator;
        List<Bundle> bundles = new ArrayList<Bundle>();
        for (String bundlePath : getBundlePathsWithinMavenRepository()) {
            Bundle bundle = context.installBundle("file:///"
                    + new File(basePath + bundlePath).getAbsolutePath());
            bundle.start();
            bundles.add(bundle);

        }
        // Wait for bundles to be started.
        for (Bundle bundle : bundles) {
            while (bundle.getState() != Bundle.ACTIVE) {
                Thread.sleep(50);
            }
        }
    }

    /**
     * Remove authentication that might be left
     */
    @BeforeClass(dependsOnMethods = "setupAutowiredServices", groups = GROUP_INTEGRATION_TEST_SETUP)
    public void setupRemoveAuthentication() {
        AuthenticationHelper.removeAuthentication();
    }

    /**
     * Remove auth after each test, override it in case authentication is set for whole class
     */
    @AfterTest
    public void setupRemoveAuthenticationAfterEachTest() {
        AuthenticationHelper.removeAuthentication();
    }

    /**
     * Remove auth before each test, override it in case authentication is set for whole class
     */
    @BeforeTest
    public void setupRemoveAuthenticationBeforeEachTest() {
        AuthenticationHelper.removeAuthentication();
    }

    /**
     * Wrapper for Thread.sleep to eat the exception.
     *
     * @param millis
     *            The milliseconds to sleep.
     */
    public void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            LOGGER.warn(e.getMessage());
        }
    }
}
