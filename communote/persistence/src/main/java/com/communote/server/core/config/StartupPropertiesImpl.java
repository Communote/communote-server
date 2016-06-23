package com.communote.server.core.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.communote.common.io.FileHelper;
import com.communote.common.io.IOHelper;
import com.communote.common.properties.PropertiesUtils;
import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.config.ConfigurationInitializationException;
import com.communote.server.api.core.config.ConfigurationUpdateException;
import com.communote.server.api.core.config.CoreConfigurationPropertyConstant;
import com.communote.server.api.core.config.StartupProperties;
import com.communote.server.api.core.config.type.ApplicationPropertySecurity;
import com.communote.server.api.core.config.type.CoreProperty;
import com.communote.server.api.core.config.type.CorePropertyDatabase;

/**
 * Holds the properties that are required for the startup of the application.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class StartupPropertiesImpl implements StartupProperties {

    /** The default schmea separator for jdbc url if no separator is configured */
    private static final String DEFAULT_SCHEMA_SEPARATOR = "/";
    private static final Logger LOGGER = LoggerFactory.getLogger(StartupPropertiesImpl.class);
    private static final String JRE_TRUSTSTORE_PATH = "/lib/security/cacerts";
    private static final String JRE_TRUSTSTORE_PASSWORD = "changeit";
    /** default data dir name. Must not be changed! */
    private static final String SUBDIR_NAME_DATA = "communote-data";
    private static final String SUBDIR_NAME_CONF = "conf";
    private static final String SUBDIR_NAME_CONF_COMMUNOTE = "communote";
    private static final String SUBDIR_NAME_CACHE = "cache";
    private static final String SUBDIR_NAME_SERVERID = "server";
    private static final String FILE_NAME_SERVERID_PREFIX = "server_";
    private static final String CORE_PROPERTIES_FILE_NAME = "core.properties";
    private static final String STARTUP_PROPERTIES_FILE_NAME = "startup.properties";
    private static final String KEYSTORE_FILE_NAME = "communote.ks";
    /** for legacy reasons the truststore is named .keystore dont get confused! */
    private static final String TRUSTSTORE_FILE_NAME = "communote.keystore";
    private static final String PROPERTY_INSTANCE_NAME = "communote.instance.name";
    private static final String PROPERTY_DATA_DIR = "communote.data.dir";
    private static final String PROPERTY_CONFIG_DIR = "communote.config.dir";
    private static final String PROPERTY_SERVER_ID = "communote.server.id";
    private static final String PROPERTY_PLUGIN_DIR = "communote.plugin.dir";
    private Map<String, String> coreProperties;
    private String databaseUrl;
    private final File pluginUrl;
    private final File dataDirectory;
    private final File cacheRootDirectory;
    private File configDirectory;
    private String instanceName;
    /**
     * base directory of the installation. Shouldn't be used directly, use resolveInstallationDir
     * instead and handle null values.
     */
    private File serverInstallDir;
    private final String serverId;
    private KeyStore keyStore;
    private KeyStore trustStore;

    /**
     * Constructs a new instance of the startup properties.
     *
     * @param webAppRealPath
     *            the file system path to the web application which is used to resolve the server
     *            installation directory.
     * @throws ConfigurationInitializationException
     *             in case retrieving some of the required properties fails
     */
    public StartupPropertiesImpl(String webAppRealPath) throws ConfigurationInitializationException {
        readEnvironmentProperties(webAppRealPath);
        Properties startupProps = readStartupProperties(configDirectory);
        dataDirectory = resolveDataDir(startupProps, webAppRealPath);
        pluginUrl = resolvePluginDir(startupProps);
        cacheRootDirectory = resolveCacheRootDirectory(dataDirectory);
        serverId = resolveServerId(dataDirectory);
        loadCoreProperties();
    }

    /**
     * Creates the database URL from protocol, host, port and database name.
     *
     * @return the URL
     */
    private synchronized String createDatabaseUrl() {
        String host = getDatabaseHost();
        String dbName = getDatabaseName();
        String protocol = getDatabaseProtocol();
        String protocolSeparator = getDatabaseProtocolSeparator();
        if (host == null || protocol == null || dbName == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder(protocol);
        if (StringUtils.isEmpty(protocolSeparator)) {
            protocolSeparator = DEFAULT_PROTOCOL_SEPARATOR;
        }
        sb.append(protocolSeparator);
        sb.append(host);
        Integer port = getDatabasePort();
        if (port != null) {
            sb.append(":" + port);
        }
        sb.append(getDatabaseSchemaSeparator() + dbName);
        if (isFulltextSearch()
                && "org.hibernate.dialect.SQLServerDialect".equals(getHibernateDialect())
                && !dbName.contains(";prepareSQL=2")) {
            sb.append(";prepareSQL=2");
        }
        return sb.toString();
    }

    @Override
    public File getCacheRootDirectory() {
        return cacheRootDirectory;
    }

    @Override
    public File getConfigurationDirectory() {
        return configDirectory;
    }

    @Override
    public String getCoreProperty(CoreConfigurationPropertyConstant property) {
        return coreProperties.get(property.getKeyString());
    }

    @Override
    public String getDatabaseDriverClassName() {
        return coreProperties.get(CorePropertyDatabase.DATABASE_DRIVER_CLASS_NAME.getKeyString());
    }

    @Override
    public String getDatabaseHost() {
        return coreProperties.get(CorePropertyDatabase.DATABASE_HOST.getKeyString());
    }

    @Override
    public String getDatabaseName() {
        return coreProperties.get(CorePropertyDatabase.DATABASE_NAME.getKeyString());
    }

    @Override
    public Integer getDatabasePort() {
        String portString = coreProperties.get(CorePropertyDatabase.DATABASE_PORT.getKeyString());
        Integer port = null;
        if (StringUtils.isNotBlank(portString)) {
            try {
                port = new Integer(portString);
            } catch (NumberFormatException e) {
                LOGGER.error("Provided database port is not a number");
            }
        }
        return port;
    }

    @Override
    public String getDatabaseProtocol() {
        return coreProperties.get(CorePropertyDatabase.DATABASE_PROTOCOL.getKeyString());
    }

    @Override
    public String getDatabaseProtocolSeparator() {
        String protocolSeparator = coreProperties
                .get(CorePropertyDatabase.DATABASE_PROTOCOL_SEPARATOR.getKeyString());
        if (protocolSeparator == null) {
            return DEFAULT_PROTOCOL_SEPARATOR;
        }
        return protocolSeparator;
    }

    @Override
    public String getDatabaseSchemaSeparator() {
        String protocolSeparator = coreProperties.get(CorePropertyDatabase.SCHEMA_SEPARATOR
                .getKeyString());
        if (protocolSeparator == null) {
            return DEFAULT_SCHEMA_SEPARATOR;
        }
        return protocolSeparator;
    }

    @Override
    public String getDatabaseUrl() {
        if (this.databaseUrl == null) {
            this.databaseUrl = createDatabaseUrl();
        }
        return this.databaseUrl;
    }

    @Override
    public String getDatabaseUserName() {
        return coreProperties.get(CorePropertyDatabase.DATABASE_USER_NAME.getKeyString());
    }

    @Override
    public String getDatabaseUserPassword() {
        return coreProperties.get(CorePropertyDatabase.DATABASE_USER_PASSWORD.getKeyString());
    }

    @Override
    public File getDataDirectory() {
        return dataDirectory;
    }

    @Override
    public String getHibernateDialect() {
        return coreProperties.get(CorePropertyDatabase.HIBERNATE_DIALECT.getKeyString());
    }

    @Override
    public String getInstanceName() {
        return instanceName;
    }

    @Override
    public KeyStore getKeyStore() {
        if (keyStore == null || trustStore == null) {
            initKeyTrustStore();
        }
        return keyStore;
    }

    @Override
    public File getPluginDir() {
        return pluginUrl;
    }

    /**
     * Searches the environment for the specified property name and returns it value. If the
     * property is marked as required an exception will be thrown if the property cannot be found or
     * is not set.
     *
     * @param envContext
     *            the environment context to search
     * @param propertyName
     *            the name of the property to retrieve
     * @param required
     *            whether the property is required
     * @return the value. This might be null or a white space string if the required parameter is
     *         set to false.
     */
    private String getPropertyFromEnvironment(Context envContext, String propertyName,
            boolean required) {
        String value = null;
        Throwable failedCause = null;
        try {
            value = envContext.lookup(propertyName).toString();
        } catch (NamingException e) {
            failedCause = e;
        }
        if (required) {
            if (StringUtils.isBlank(value)) {
                String errorMsg = "Required property " + propertyName + " not found in environment";
                if (failedCause != null) {
                    LOGGER.error(errorMsg, failedCause);
                    throw new ConfigurationInitializationException(errorMsg, failedCause);
                } else {
                    LOGGER.error(errorMsg);
                    throw new ConfigurationInitializationException(errorMsg);
                }
            }
        }
        return value;
    }

    @Override
    public String getServerId() {
        return serverId;
    }

    @Override
    public KeyStore getTrustStore() {
        if (keyStore == null || trustStore == null) {
            initKeyTrustStore();
        }
        return trustStore;
    }

    /**
     * Copies all root certificates form javas default keystore to the application keystore
     *
     * @throws KeyStoreException
     *             thrown if the requested keystore type is not available
     * @throws IOException
     *             thrown if there is an I/O or format problem with the keystore data, if a password
     *             is required but not given, or if the given password was incorrect
     * @throws NoSuchAlgorithmException
     *             thrown if the algorithm used to check the integrity of the keystore cannot be
     *             found
     * @throws CertificateException
     *             thrown if any of the certificates in the keystore could not be loaded
     */
    private void initializeRootCertificates() throws KeyStoreException, IOException,
            NoSuchAlgorithmException, CertificateException {
        KeyStore rootKeyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        File rootKeyStoreFile = new File(System.getProperty("java.home") + JRE_TRUSTSTORE_PATH);
        char[] password = JRE_TRUSTSTORE_PASSWORD.toCharArray();

        LOGGER.debug("Start initializing the communote truststore file.");

        if (rootKeyStoreFile.exists()) {
            rootKeyStore.load(new FileInputStream(rootKeyStoreFile), password);

            LOGGER.debug(rootKeyStore.size() + " certificates where found in JRE truststore.");
            Enumeration<String> aliases = rootKeyStore.aliases();

            while (aliases.hasMoreElements()) {
                String alias = aliases.nextElement();
                Certificate cert = rootKeyStore.getCertificate(alias);
                if (cert instanceof X509Certificate) {
                    try {
                        ((X509Certificate) cert).checkValidity();
                        // certificate is valid
                        LOGGER.debug("Valid           > {}", alias);
                    } catch (CertificateExpiredException e) {
                        // this certificate is expired, don't add it to the
                        // keystore
                        cert = null;
                        LOGGER.debug("Expired         > {}", alias);
                    } catch (CertificateNotYetValidException e) {
                        // certificate will be valid in future
                        LOGGER.debug("Not Yet Valid   > {}", alias);
                    }
                }
                if (cert != null) {
                    LOGGER.debug("Add {}", alias);
                    trustStore.setCertificateEntry(alias, rootKeyStore.getCertificate(alias));
                }
            }
            LOGGER.debug("{} certificates where added.", trustStore.size());
            LOGGER.debug("Finish initialization of certificates to the truststore file.");
        } else {
            LOGGER.error("FAILED to initialize communote truststore. Root certificates of JRE not found.");
        }
    }

    /**
     * Init the key and trust stores
     */
    private synchronized void initKeyTrustStore() {
        FileInputStream fis = null;
        FileOutputStream fos = null;
        if (trustStore == null) {
            try {
                String path = getDataDirectory().getAbsolutePath() + File.separator
                        + TRUSTSTORE_FILE_NAME;
                char[] password = ApplicationPropertySecurity.TRUSTED_CA_TRUSTSTORE_PASSWORD
                        .getValue().toCharArray();
                File trustStoreFile = new File(path);
                trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
                if (trustStoreFile.exists()) {
                    fis = new FileInputStream(trustStoreFile);
                    trustStore.load(fis, password);
                } else {
                    trustStoreFile.getParentFile().mkdirs();
                    trustStoreFile.createNewFile();
                    trustStore.load(null, password);
                    initializeRootCertificates();
                    // store
                    fos = new FileOutputStream(trustStoreFile);
                    trustStore.store(fos, password);
                }
            } catch (Exception e) {
                LOGGER.error("Loading TrustStore failed", e);
            } finally {
                IOHelper.close(fis);
                IOHelper.close(fos);
                fis = null;
                fos = null;
            }
        }
        if (keyStore == null) {
            try {
                String path = getDataDirectory().getAbsolutePath() + File.separator
                        + KEYSTORE_FILE_NAME;
                char[] password = ApplicationPropertySecurity.KEYSTORE_PASSWORD.getValue()
                        .toCharArray();
                File keyStoreFile = null;
                keyStoreFile = new File(path);
                keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
                if (keyStoreFile.exists()) {
                    fis = new FileInputStream(keyStoreFile);
                    keyStore.load(fis, password);
                } else {
                    keyStoreFile.getParentFile().mkdirs();
                    keyStoreFile.createNewFile();
                    keyStore.load(null, password);
                    // store
                    fos = new FileOutputStream(keyStoreFile);
                    keyStore.store(fos, password);
                }
            } catch (Exception e) {
                LOGGER.error("Loading KeyStore failed", e);
            } finally {
                IOHelper.close(fis);
                IOHelper.close(fos);
                fis = null;
                fos = null;
            }
        }
        try {
            setDefaulSslContext();
        } catch (Exception e) {
            LOGGER.error("Setting Default SSL Context failed", e);
        }
    }

    @Override
    public boolean isFulltextSearch() {
        String fulltextEnabled = System.getProperties().getProperty(
                CorePropertyDatabase.DATABASE_SPECIFIC_FULL_TEXT_SEARCH.getKeyString());
        if (StringUtils.isBlank(fulltextEnabled)) {
            fulltextEnabled = coreProperties
                    .get(CorePropertyDatabase.DATABASE_SPECIFIC_FULL_TEXT_SEARCH.getKeyString());
        }
        if (StringUtils.isBlank(fulltextEnabled)) {
            return true;
        }
        return !"false".equals(fulltextEnabled);
    }

    @Override
    public boolean isInstallationDone() {
        return new Boolean(coreProperties.get(CoreProperty.INSTALLATION_DONE.getKeyString()));
    }

    /**
     * Returns true if the property is a database specific setting.
     *
     * @param property
     *            the property to test
     * @return true if the property is a database specific setting, false otherwise
     * @throws ConfigurationUpdateException
     *             in case the installation is already done
     */
    private boolean isPropertyDatabaseSpecific(CoreConfigurationPropertyConstant property)
            throws ConfigurationUpdateException {
        if (property instanceof CorePropertyDatabase) {
            if (isInstallationDone()) {
                throw new ConfigurationUpdateException("Database configuration ("
                        + property.getKeyString()
                        + ") cannot be modified after the installation has been completed", null);
            }
            return true;
        }
        return false;
    }

    /**
     * Loads the core properties from file.
     *
     * @throws ConfigurationInitializationException
     *             if reading the file failed
     */
    private void loadCoreProperties() throws ConfigurationInitializationException {
        File corePropsFile = new File(dataDirectory, CORE_PROPERTIES_FILE_NAME);
        coreProperties = new HashMap<String, String>();
        try {
            Properties coreProps = PropertiesUtils.loadPropertiesFromFile(corePropsFile);
            for (Object key : coreProps.keySet()) {
                String keyString = (String) key;
                coreProperties.put(keyString, coreProps.getProperty(keyString));
            }
        } catch (FileNotFoundException e) {
            LOGGER.info("Property file " + CORE_PROPERTIES_FILE_NAME
                    + " does not exist. Starting with default settings");
        } catch (IOException e) {
            String errorMsg = "Reading the core properties file " + corePropsFile.getAbsolutePath()
                    + " failed";
            LOGGER.error(errorMsg);
            throw new ConfigurationInitializationException(errorMsg);
        }
    }

    /**
     * Reads some properties from the runtime environment.
     *
     * @param webAppRealPath
     *            the file system path to the web application which is used to resolve the server
     *            installation directory.
     * @throws ConfigurationInitializationException
     *             if the required properties are not defined
     */
    private void readEnvironmentProperties(String webAppRealPath)
            throws ConfigurationInitializationException {
        Context envContext;
        try {
            Context initContext = new InitialContext();
            envContext = (Context) initContext.lookup("java:comp/env");
        } catch (NamingException e) {
            String errorMessage = "Retrieving required properties from evironment failed";
            LOGGER.error(errorMessage, e);
            throw new ConfigurationInitializationException(errorMessage, e);
        }
        instanceName = getPropertyFromEnvironment(envContext, PROPERTY_INSTANCE_NAME, true);
        LOGGER.info("Name of instance is " + instanceName);
        String configDir = getPropertyFromEnvironment(envContext, PROPERTY_CONFIG_DIR, false);
        if (StringUtils.isBlank(configDir)) {
            // fallback to subdir of installation dir
            File installDir = resolveInstallationDir(webAppRealPath);
            if (installDir == null) {
                String errorMsg = "Cannot not use the default configuration settings for your setup. You will have to"
                        + " define the configuration and data directory manually. Please have a look at the"
                        + " installation manual for details.";
                LOGGER.error(errorMsg + "\n");
                throw new ConfigurationInitializationException(errorMsg);
            }
            configDirectory = new File(new File(installDir, SUBDIR_NAME_CONF),
                    SUBDIR_NAME_CONF_COMMUNOTE);
        } else {
            configDirectory = new File(configDir);
        }
        LOGGER.info("Using configuration directory " + configDirectory.getAbsolutePath());
    }

    /**
     * Reads the startup.properties file.
     *
     * @param configDirFile
     *            file pointing to the configuration directory
     * @return the parsed startup.properties or null if the properties are not defined
     */
    private Properties readStartupProperties(File configDirFile) {
        File configProps = new File(configDirFile, STARTUP_PROPERTIES_FILE_NAME);
        Properties startupProps = null;
        try {
            startupProps = PropertiesUtils.loadPropertiesFromFile(configProps);
        } catch (FileNotFoundException e) {
            LOGGER.info("Startup properties file " + configProps.getAbsolutePath() + " not found");
        } catch (IOException e) {
            LOGGER.warn("Reading the startup properties file " + configProps.getAbsolutePath()
                    + " failed");
        }
        return startupProps;
    }

    /**
     * Resolves the cache root directory.
     *
     * @param dataDir
     *            the data directory
     * @return the root directory for caching data on this instance
     * @throws ConfigurationInitializationException
     *             if the cache directory cannot be resolved
     */
    private File resolveCacheRootDirectory(File dataDir)
            throws ConfigurationInitializationException {
        File cacheDir = new File(dataDir, SUBDIR_NAME_CACHE);
        validateDir(cacheDir);
        // create a subdir for the instance
        File instanceCacheRootDir = new File(cacheDir, instanceName);
        validateDir(instanceCacheRootDir);
        try {
            FileUtils.cleanDirectory(instanceCacheRootDir);
        } catch (IOException e) {
            LOGGER.warn("Error clearing the cache directory {} : {}", instanceCacheRootDir,
                    e.getMessage());
        }
        LOGGER.info("Using cache directory: {}", instanceCacheRootDir);
        return instanceCacheRootDir;
    }

    /**
     * Resolves the data directory.
     *
     * @param startupProps
     *            the startup properties or null if not defined
     * @param webAppRealPath
     *            the file system path to the web application which is used to resolve the server
     *            installation directory.
     * @return the data directory
     * @throws ConfigurationInitializationException
     *             if the data directory cannot be resolved
     */
    private File resolveDataDir(Properties startupProps, String webAppRealPath)
            throws ConfigurationInitializationException {
        String dataDirName = null;
        if (startupProps != null) {
            dataDirName = startupProps.getProperty(PROPERTY_DATA_DIR);
        }
        File dataDir = null;
        if (StringUtils.isBlank(dataDirName)) {
            LOGGER.info("Data directory not defined in startup properties. Will use default");
            File installDir = resolveInstallationDir(webAppRealPath);
            if (installDir == null) {
                String errorMsg = "Cannot create a default data directory for your setup. You will have to configure"
                        + " the data directory before starting Communote. Please have a look at the installation"
                        + " manual for details.";
                LOGGER.error(errorMsg + "\n");
                throw new ConfigurationInitializationException(errorMsg);
            }
            dataDir = new File(installDir, SUBDIR_NAME_DATA);
        } else {
            dataDir = new File(dataDirName);
        }
        LOGGER.info("Using data directory " + dataDir.getAbsolutePath());
        validateDir(dataDir);
        return dataDir;
    }

    /**
     * Resolve the base directory of the installation. This method is intended to be run during
     * initialization. It will return null if the directory cannot be resolved, for instance if the
     * running server isn't a tomcat engine or the directory is a subdir of the webapps directory.
     * Callers should handle a null value as a fatal error and throw an appropriate
     * ConfigurationInit exception.
     *
     * @param webAppRealPath
     *            the file system path to the web application which is used to resolve the server
     *            installation directory.
     * @return the directory or null
     */
    private File resolveInstallationDir(String webAppRealPath) {
        if (serverInstallDir == null) {
            String catalinaBase = System.getProperty("catalina.base");
            // in case it's not a tomcat installation the catalina dir isn't set
            if (catalinaBase == null) {
                LOGGER.error("Cannot determin default configuration or data directory because "
                        + "application does not seem to be running within Tomcat");
                return null;
            }
            // catalina base is parent of webapps directory
            File webappsParent = new File(catalinaBase, "webapps");
            File parent = null;
            File webAppRealPathFile = new File(webAppRealPath);
            try {
                parent = webAppRealPathFile.getCanonicalFile().getParentFile();
                if (parent != null) {
                    // fail if the installation parent directory is a subdir of webapps; usually
                    // only WAR based installation - could navigate some dirs upwards to change to
                    // parent of webapps but using that dir isn't what linux admin would expect
                    if (parent.getCanonicalPath().startsWith(webappsParent.getCanonicalPath())) {
                        LOGGER.debug(
                                "Cannot use installation base directoy {} for creating default configuration or data "
                                        + "directory because it is a subdir of the webapps directory",
                                parent.getAbsolutePath());
                    } else {
                        serverInstallDir = parent;
                    }
                } else {
                    LOGGER.error("Resolving the server installation directory relative to "
                            + webAppRealPath + " failed");
                }
            } catch (IOException e) {
                LOGGER.error("Resolving the server installation directory relative to "
                        + webAppRealPath + " failed", e);
            }
        }
        return serverInstallDir;
    }

    /**
     * Resolve plugin directory data
     *
     * @param startupProps
     *            the startup properties or null if not defined
     * @return The plugins dir.
     * @throws ConfigurationInitializationException
     *             Exception.
     */
    private File resolvePluginDir(Properties startupProps)
            throws ConfigurationInitializationException {
        String pluginDirName = null;
        if (startupProps != null) {
            pluginDirName = startupProps.getProperty(PROPERTY_PLUGIN_DIR);
        }
        File pluginDirFile = null;
        if (StringUtils.isBlank(pluginDirName)) {
            LOGGER.info("Plugin directory not defined in startup properties. Will use default");
            pluginDirFile = new File(getDataDirectory(), "plugins");
        } else {
            pluginDirFile = new File(pluginDirName);
        }
        LOGGER.info("Using plugin directory " + pluginDirFile.getAbsolutePath());
        validateDir(pluginDirFile);
        return pluginDirFile;
    }

    /**
     * Loads the server ID from file or creates and stores it if it does not yet exist.
     *
     * @param dataDir
     *            the data directory
     * @return the server ID
     * @throws ConfigurationInitializationException
     *             if loading or storing the server ID fails
     */
    private String resolveServerId(File dataDir) throws ConfigurationInitializationException {
        File serverIdPath = new File(dataDir, SUBDIR_NAME_SERVERID);
        validateDir(serverIdPath);
        File serverIdFile = new File(serverIdPath, FILE_NAME_SERVERID_PREFIX + instanceName);
        String resolvedServerId = null;
        if (serverIdFile.exists()) {
            try {
                Properties props = PropertiesUtils.loadPropertiesFromFile(serverIdFile);
                resolvedServerId = props.getProperty(PROPERTY_SERVER_ID);
            } catch (IOException e) {
                String errorMsg = "Loading the server settings from file "
                        + serverIdFile.getAbsolutePath() + " failed";
                LOGGER.error(errorMsg);
                throw new ConfigurationInitializationException(errorMsg);
            }
        }
        if (StringUtils.isBlank(resolvedServerId)) {
            resolvedServerId = UUID.randomUUID().toString();
            Properties props = new Properties();
            props.setProperty(PROPERTY_SERVER_ID, resolvedServerId);
            try {
                PropertiesUtils.storePropertiesToFile(props, serverIdFile);
            } catch (IOException e) {
                String errorMsg = "Storing the server settings failed";
                LOGGER.error(errorMsg);
                throw new ConfigurationInitializationException(errorMsg);
            }
        }
        return resolvedServerId;
    }

    @Override
    public void setCertificate(String alias, Certificate certificate) throws KeyStoreException {
        KeyStore trustStore = getTrustStore();
        try {
            if (certificate == null) {
                trustStore.deleteEntry(alias);
            } else {
                trustStore.setCertificateEntry(alias, certificate);
            }
            String path = getDataDirectory().getAbsolutePath() + File.separator
                    + TRUSTSTORE_FILE_NAME;
            char[] password = ApplicationPropertySecurity.TRUSTED_CA_TRUSTSTORE_PASSWORD.getValue()
                    .toCharArray();
            trustStore.store(new FileOutputStream(path), password);
            setDefaulSslContext();
        } catch (Exception e) {
            if (e instanceof KeyStoreException) {
                throw (KeyStoreException) e;
            }
            throw new KeyStoreException(e);
        }
    }

    /**
     * Sets the current keystore as system property.
     */
    private void setDefaulSslContext() {
        String trustStoreFilename = getDataDirectory().getAbsolutePath() + File.separator
                + TRUSTSTORE_FILE_NAME;
        String keyStoreFilename = getDataDirectory().getAbsolutePath() + File.separator
                + KEYSTORE_FILE_NAME;
        String keyPassword = ApplicationPropertySecurity.KEYSTORE_PASSWORD.getValue();
        String trustPassword = ApplicationPropertySecurity.TRUSTED_CA_TRUSTSTORE_PASSWORD
                .getValue();
        LOGGER.debug("Setting trustStoreFilename to {}", trustStoreFilename);
        LOGGER.debug("Setting keyStoreFilename to {}", keyStoreFilename);
        if (trustStoreFilename != null) {
            System.setProperty("javax.net.ssl.trustStore", trustStoreFilename);
            System.setProperty("javax.net.ssl.trustStorePassword", trustPassword);
        }
        if (keyStoreFilename != null) {
            System.setProperty("javax.net.ssl.keyStore", keyStoreFilename);
            System.setProperty("javax.net.ssl.keyStorePassword", keyPassword);
        }

    }

    /**
     * Updates the named property.
     *
     * @param property
     *            the property to update
     * @param value
     *            the new value
     * @throws ConfigurationUpdateException
     *             if the update failed
     */
    void update(CoreConfigurationPropertyConstant property, String value)
            throws ConfigurationUpdateException {
        Map<String, String> newProps = new HashMap<String, String>();
        newProps.putAll(coreProperties);
        if (updatePropertyValue(newProps, property, value)) {
            updateCoreProperties(newProps, isPropertyDatabaseSpecific(property));
        }
    }

    /**
     * Updates a set of properties.
     *
     * @param settings
     *            a mapping of the properties to their new value
     * @throws ConfigurationUpdateException
     *             if the update failed
     */
    void update(Map<CoreConfigurationPropertyConstant, String> settings)
            throws ConfigurationUpdateException {
        Map<String, String> newProps = new HashMap<String, String>();
        newProps.putAll(coreProperties);
        boolean resetApplication = false;
        boolean propsChanged = false;
        for (CoreConfigurationPropertyConstant property : settings.keySet()) {
            if (updatePropertyValue(newProps, property, settings.get(property))) {
                propsChanged = true;
                if (isPropertyDatabaseSpecific(property)) {
                    resetApplication = true;
                }
            }
        }
        if (propsChanged) {
            updateCoreProperties(newProps, resetApplication);
        }
    }

    /**
     * Updates the core.properties file
     *
     * @param newProperties
     *            the new properties to set
     * @param resetApplication
     *            whether to reset application
     * @throws ConfigurationUpdateException
     *             if writing to the file failed.
     */
    private void updateCoreProperties(Map<String, String> newProperties, boolean resetApplication)
            throws ConfigurationUpdateException {
        File corePropsFile = new File(dataDirectory, CORE_PROPERTIES_FILE_NAME);
        try {
            Properties propsToStore = new Properties();
            propsToStore.putAll(newProperties);
            PropertiesUtils.storePropertiesToFile(propsToStore, corePropsFile);
            synchronized (this) {
                this.coreProperties = newProperties;
                // reset url
                this.databaseUrl = null;
                if (resetApplication) {
                    CommunoteRuntime.getInstance().stop();
                    CommunoteRuntime.getInstance().start();
                }
            }
        } catch (IOException e) {
            String errorMsg = "Writing the " + CORE_PROPERTIES_FILE_NAME + " failed";
            LOGGER.error(errorMsg, e);
            throw new ConfigurationUpdateException(errorMsg,
                    "configuration.core.properties.saving.failed");
        }
    }

    /**
     * Updates a property value in the property mapping by creating, overriding or removing entries.
     * The latter occurs when the new value is null.
     *
     * @param props
     *            the properties mapping to modify
     * @param property
     *            the property to update
     * @param value
     *            the new value
     * @return true if the property changed (value modified, added, or removed)
     */
    private boolean updatePropertyValue(Map<String, String> props,
            CoreConfigurationPropertyConstant property, String value) {
        if (value != null) {
            String oldValue = props.put(property.getKeyString(), value);
            return !value.equals(oldValue);
        } else {
            return props.remove(property.getKeyString()) != null;
        }
    }

    private void validateDir(File dir) {
        try {
            FileHelper.validateDir(dir);
        } catch (FileNotFoundException e) {
            throw new ConfigurationInitializationException(e.getMessage());
        }
    }
}