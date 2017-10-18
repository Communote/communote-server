package com.communote.server.installer;

import java.io.File;
import java.io.FileOutputStream;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.NamingException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.easymock.EasyMock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.client.ClientTO;
import com.communote.server.api.core.common.ClientAndChannelContextHolder;
import com.communote.server.api.core.config.ApplicationConfigurationPropertyConstant;
import com.communote.server.api.core.config.ConfigurationManager;
import com.communote.server.api.core.config.CoreConfigurationPropertyConstant;
import com.communote.server.api.core.config.StartupProperties;
import com.communote.server.api.core.config.database.DatabaseType;
import com.communote.server.api.core.config.type.ApplicationProperty;
import com.communote.server.api.core.config.type.ApplicationPropertyMailing;
import com.communote.server.api.core.config.type.CorePropertyDatabase;
import com.communote.server.api.core.installer.CommunoteInstaller;
import com.communote.server.api.core.user.UserVO;
import com.communote.server.api.service.ClientRetrievalService;
import com.communote.server.core.application.DefaultRuntimeBuilder;
import com.communote.server.core.user.UserManagement;
import com.communote.server.model.user.User;
import com.communote.server.model.user.UserRole;
import com.communote.server.persistence.user.client.ClientHelper;
import com.communote.server.test.installer.LogDatabaseInstallationCallback;
import com.communote.server.test.installer.MockInitialContextFactory;
import com.communote.server.test.util.DatabaseUtils;

/**
 * First test of the test suit that fills the core.properties, initializes the database and creates
 * the global client.
 *
 * All parameters default to PostgreSQL.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
@Test(testName = "communote-installation-test", groups = "Installer")
public class InstallerTest {

    /** The email address of the test user with manager access */
    public final static String TEST_MANGER_USER_EMAIL = "communote@localhost";
    /** The alias of the test user with manager access */
    public final static String TEST_MANAGER_USER_ALIAS = "communote";

    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(InstallerTest.class);
    private DatabaseType databaseType;
    private String databaseUsername;
    private String databasePort;
    private String databaseHost;
    private String databaseName;
    private String databasePassword;

    /**
     * Installation test that creates the DB schema.
     *
     * @throws Exception
     *             in case the test fails
     */
    @BeforeSuite(dependsOnMethods = "prepareDatabase")
    public void installationStep1InitializeDatabase() throws Exception {
        CommunoteInstaller installer = CommunoteRuntime.getInstance().getInstaller();

        Map<CoreConfigurationPropertyConstant, String> settings = new HashMap<CoreConfigurationPropertyConstant, String>();

        settings.put(CorePropertyDatabase.DATABASE_HOST, databaseHost);
        settings.put(CorePropertyDatabase.DATABASE_PORT, databasePort);
        settings.put(CorePropertyDatabase.DATABASE_NAME, databaseName);
        settings.put(CorePropertyDatabase.DATABASE_USER_NAME, databaseUsername);
        settings.put(CorePropertyDatabase.DATABASE_USER_PASSWORD, databasePassword);

        StartupProperties newSettings = installer.updateDatabaseSettings(this.databaseType,
                settings);
        String url = newSettings.getDatabaseUrl();
        Assert.assertNotNull(url);
        LOGGER.info("Database Url: {}", url);
        boolean success = installer.initializeDatabase(new LogDatabaseInstallationCallback());
        Assert.assertTrue(success, "DB-setup failed");
    }

    /**
     * Installation step that creates the global client.
     *
     * @throws Exception
     *             if the test fails
     */
    @BeforeSuite(dependsOnMethods = "installationStep1InitializeDatabase")
    public void installationStep2CreateGlobalClient() throws Exception {
        CommunoteRuntime.getInstance().getInstaller().createCommunoteAccount("Global Test Client",
                "time.zones.gmt.Europe/Amsterdam");
    }

    /**
     * Installation step that stores the mail-out settings.
     *
     * <p>
     * Note: whether the configuration refers to an working SMTP server or not doesn't matter
     * because the FileSystemMimeMessageSender is used which won't send emails
     * </p>
     *
     * @param mailOutHost
     *            the host name of the mail out server
     * @param mailOutPort
     *            the port of the mail out server
     * @param fromAddress
     *            the from address to be set
     * @throws Exception
     *             in case the test fails
     */
    @Parameters({ "mailOutHost", "mailOutPort", "mailOutFromAddress" })
    @BeforeSuite(dependsOnMethods = "installationStep2CreateGlobalClient")
    public void installationStep3StoreMailOutSettings(@Optional("localhost") String mailOutHost,
            @Optional("25") String mailOutPort,
            @Optional("communote-installer-test@localhost") String fromAddress) throws Exception {
        ConfigurationManager conf = CommunoteRuntime.getInstance().getConfigurationManager();
        Map<ApplicationConfigurationPropertyConstant, String> settings;
        settings = new HashMap<ApplicationConfigurationPropertyConstant, String>();
        settings.put(ApplicationPropertyMailing.HOST, mailOutHost);
        settings.put(ApplicationPropertyMailing.PORT, mailOutPort);
        settings.put(ApplicationPropertyMailing.FROM_ADDRESS, fromAddress);
        String fromName = "[Local Test] Communote-Team";
        settings.put(ApplicationPropertyMailing.FROM_ADDRESS_NAME, fromName);
        conf.updateApplicationConfigurationProperties(settings);

        Assert.assertEquals(conf.getApplicationConfigurationProperties()
                .getProperty(ApplicationPropertyMailing.HOST), mailOutHost);
        Assert.assertEquals(conf.getApplicationConfigurationProperties()
                .getProperty(ApplicationPropertyMailing.PORT), mailOutPort);
        Assert.assertEquals(conf.getApplicationConfigurationProperties()
                .getProperty(ApplicationPropertyMailing.FROM_ADDRESS), fromAddress);
        Assert.assertEquals(conf.getApplicationConfigurationProperties()
                .getProperty(ApplicationPropertyMailing.FROM_ADDRESS_NAME), fromName);
    }

    /**
     * Installation step that creates the admin and initializes the global client.
     *
     * @throws Exception
     *             if the test failed
     */
    @BeforeSuite(dependsOnMethods = "installationStep3StoreMailOutSettings")
    public void installationStep4InitializeCommunoteAccount() throws Exception {
        UserVO userVo = new UserVO();
        userVo.setAlias(TEST_MANAGER_USER_ALIAS);
        userVo.setEmail(TEST_MANGER_USER_EMAIL);
        userVo.setFirstName("Peter");
        userVo.setLanguage(Locale.ENGLISH);
        userVo.setLastName("Admin");
        userVo.setPassword("123456");
        userVo.setTimeZoneId("time.zones.gmt.Europe/Amsterdam");

        Map<ApplicationProperty, String> params = new HashMap<ApplicationProperty, String>();
        CommunoteRuntime.getInstance().getInstaller().initializeCommunoteAccount(userVo, params);

        // assert user exists
        UserManagement um = ServiceLocator.instance().getService(UserManagement.class);
        User user = um.findUserByAlias(TEST_MANAGER_USER_ALIAS);
        Assert.assertEquals(TEST_MANGER_USER_EMAIL, user.getEmail());
        Assert.assertEquals(TEST_MANAGER_USER_ALIAS, user.getAlias());
        UserRole[] roles = um.getRolesOfUser(user.getId());
        Assert.assertEquals(2, roles.length);
        boolean hasManagerRole = false;
        boolean hasUserRole = false;
        for (UserRole role : roles) {
            if (role.equals(UserRole.ROLE_KENMEI_CLIENT_MANAGER)) {
                hasManagerRole = true;
            }
            if (role.equals(UserRole.ROLE_KENMEI_USER)) {
                hasUserRole = true;
            }
        }
        Assert.assertTrue(hasManagerRole, "not manager");
        Assert.assertTrue(hasUserRole, "not user");
    }

    /**
     * Installation step that initializes the application.
     *
     * @throws Exception
     *             if the test failed
     */
    @BeforeSuite(dependsOnMethods = "installationStep4InitializeCommunoteAccount")
    public void installationStep5InitializeApplication() throws Exception {
        CommunoteRuntime.getInstance().getInstaller().initializeApplicationAfterInstallation();
        // set the global client as current client
        ClientTO client = ServiceLocator.findService(ClientRetrievalService.class)
                .findClient(ClientHelper.getGlobalClientId());
        ClientAndChannelContextHolder.setClient(client);
        Date creationDate = client.getCreationDate();
        Timestamp encryptedCreationDate = ClientHelper.getCreationDate();
        Assert.assertEquals(creationDate, encryptedCreationDate);
    }

    /**
     * Create the database for the installer test. If the database exists it will be deleted.
     *
     * @param dbTypeIdentifier
     *            identifier of the database type. Must be one of the configured database types.
     * @param dbHost
     *            the DB host
     * @param dbPort
     *            the DB port
     * @param jdbcURLQueryString
     *            the jdbc url query string (Default: "")
     * @param dbName
     *            the name of DB
     * @param dbUsername
     *            the login name of the DB user to use for connecting to the new database. The user
     *            will be the owner of the database (if the database type has this concept).
     * @param dbPassword
     *            password of the user with login dbUsername
     * @param tempDbName
     *            name of a helper database to connect to for dropping and creating the new database
     * @param dbSuUsername
     *            the login name of the DB user to use for dropping and creating the new database.
     *            This user needs to have access to the database tempDbName. If empty or null (the
     *            default) the login and password of dbUsername will be used.
     * @param dbSuPassword
     *            password of the user with login dbSuUsername
     * @throws Exception
     *             in case the preparation failed
     */
    @Parameters({ "dbTypeIdentifier", "dbHost", "dbPort", "jdbcURLQueryString", "dbName",
            "dbUsername", "dbPassword", "tempDbName", "dbSuUsername", "dbSuPassword" })
    @BeforeSuite(dependsOnMethods = "prepareEnvironmentEntries")
    public void prepareDatabase(@Optional("POSTGRESQL") String dbTypeIdentifier,
            @Optional("localhost") String dbHost, @Optional("") String dbPort,
            @Optional("") String jdbcURLQueryString,
            @Optional("communote_installer_test") String dbName,
            @Optional("communote") String dbUsername, @Optional("communote") String dbPassword,
            @Optional("postgres") String tempDbName, @Optional("") String dbSuUsername,
            @Optional("") String dbSuPassword) throws Exception {
        CommunoteInstaller installer = CommunoteRuntime.getInstance().getInstaller();

        this.databaseType = installer.getSupportedDatabaseType(dbTypeIdentifier);
        Assert.assertNotNull(databaseType, "The dbTypeIdentifier " + dbTypeIdentifier
                + " is not among the supported database types");
        this.databaseHost = dbHost;
        if (StringUtils.isBlank(dbPort)) {
            dbPort = String.valueOf(databaseType.getDefaultPort());
        }
        this.databasePort = dbPort;
        this.databaseName = dbName;
        this.databaseUsername = dbUsername;
        this.databasePassword = dbPassword;
        String jdbcTempUrl = databaseType.getProtocol() + databaseType.getDefaultProtocolSeperator()
                + dbHost + ":" + dbPort + "/" + tempDbName + jdbcURLQueryString;
        String suUsername;
        String suPassword;
        if (StringUtils.isBlank(dbSuUsername)) {
            suUsername = dbUsername;
            suPassword = dbPassword;
        } else {
            suUsername = dbSuUsername;
            suPassword = dbSuPassword;
        }
        DatabaseUtils.recreateDatabase(jdbcTempUrl, suUsername, suPassword, dbName, databaseType,
                dbUsername);
    }

    /**
     * Prepares the environment entries which are used by Communote and that are provided by tomcat
     * when running the webapp.
     *
     * @param testBaseDirectory
     *            the base directory of the test. This directory must contain a subdirectory named
     *            communote which contains META-INF/MANIFEST.MF.
     * @param mailingReceiverAddress
     *            Address of the mail receiver. Default is "communote-test-mail@localhost".
     * @throws Exception
     *             if there was an error providing the environment entries
     */
    @Parameters({ "testBaseDirectory", "mailingReceiverAddress" })
    @BeforeSuite
    public void prepareEnvironmentEntries(String testBaseDirectory,
            @Optional("communote-test-mail@localhost") String mailingReceiverAddress)
            throws Exception {
        File testBaseDir = new File(testBaseDirectory);
        Assert.assertTrue(testBaseDir.isDirectory(),
                "Test base " + testBaseDir.getAbsolutePath() + " is not an directory");
        File applicationRealDir = new File(testBaseDir, "communote");
        File manifestFile = new File(applicationRealDir,
                "META-INF" + File.separator + "MANIFEST.MF");
        Assert.assertTrue(manifestFile.exists(),
                "Manifest file " + manifestFile.getAbsolutePath() + " does not exist");
        // fake catalina base dir
        File catalinaBaseDir = new File(testBaseDir, "catalina-base");
        FileUtils.deleteQuietly(catalinaBaseDir);
        Assert.assertFalse(catalinaBaseDir.exists(), "Catalina base directory "
                + catalinaBaseDir.getAbsolutePath() + " could not be removed");
        Assert.assertTrue(catalinaBaseDir.mkdir(), "Catalina base directory "
                + catalinaBaseDir.getAbsolutePath() + " could not be created");
        System.setProperty("catalina.base", catalinaBaseDir.getAbsolutePath());

        System.setProperty("java.naming.factory.initial",
                MockInitialContextFactory.class.getName());
        Context mockInitialContext = EasyMock.createMock(Context.class);
        Context mockJeeEnvContext = EasyMock.createMock(Context.class);
        EasyMock.expect(mockInitialContext.lookup("java:comp/env")).andReturn(mockJeeEnvContext);
        EasyMock.expect(mockJeeEnvContext.lookup("communote.instance.name"))
                .andReturn("communote-test");
        // use default config dir
        EasyMock.expect(mockJeeEnvContext.lookup("communote.config.dir"))
                .andThrow(new NamingException("Not bound"));
        EasyMock.replay(mockInitialContext);
        EasyMock.replay(mockJeeEnvContext);
        MockInitialContextFactory.setMockContext(mockInitialContext);

        // create development properties to avoid sending
        Properties developmentProperties = new Properties();
        developmentProperties.setProperty("mailout.mode", "filesystem");
        File configurationDirectory = new File(testBaseDir, "conf" + File.separator + "communote");
        configurationDirectory.mkdirs();
        FileOutputStream out = new FileOutputStream(configurationDirectory.getCanonicalPath()
                + File.separator + "development.properties");
        developmentProperties.store(out, "");
        IOUtils.closeQuietly(out);

        DefaultRuntimeBuilder runtimeBuilder = DefaultRuntimeBuilder.getInstance();
        runtimeBuilder.addApplicationContextConfigLocation(
                "classpath:com/communote/server/test/spring/applicationContext-test.xml");
        runtimeBuilder.setApplicationDirectory(applicationRealDir.getAbsolutePath());
        CommunoteRuntime.init(runtimeBuilder);
        Assert.assertNotNull(CommunoteRuntime.getInstance());
        CommunoteRuntime.getInstance().start();

        File f = CommunoteRuntime.getInstance().getConfigurationManager().getStartupProperties()
                .getDataDirectory();
        Assert.assertTrue(f.exists());

        if (!CommunoteRuntime.getInstance().getApplicationInformation().isStandalone()) {
            Assert.fail("The installer test only runs successfully on the standalone version!");
        }
        Assert.assertNotNull(CommunoteRuntime.getInstance().getInstaller());
    }

    /**
     * Mock test, to let at least on test be run.
     */
    @Test
    public void test() {
        Assert.assertTrue(true);
    }

}
