package com.communote.server.api.core.config;

import java.io.File;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.Certificate;

/**
 * Holds the properties that are required for the startup of the application.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public interface StartupProperties {

    /** The default protocol separator for jdbc url if no separator is configured */
    public static final String DEFAULT_PROTOCOL_SEPARATOR = "://";

    /**
     * Returns the root directory for caching data of the running instance.
     *
     * @return the root directory
     */
    public File getCacheRootDirectory();

    /**
     * Returns the configuration directory of the running instance.
     *
     * @return the configuration directory
     */
    public File getConfigurationDirectory();

    /**
     * @param property
     *            The property.
     * @return The value for property or null if not existend.
     */
    public String getCoreProperty(CoreConfigurationPropertyConstant property);

    /**
     * Returns the driver class name.
     *
     * @return the name of the driver class
     */
    public String getDatabaseDriverClassName();

    /**
     * The name the database or null if not set.
     *
     * @return the database name
     */
    public String getDatabaseHost();

    /**
     * The name the database or null if not set.
     *
     * @return the database name
     */
    public String getDatabaseName();

    /**
     * The port the database is listening on or null if not set or not required.
     *
     * @return the port of the database connection
     */
    public Integer getDatabasePort();

    /**
     * The protocol of the database connection or null if not set.
     *
     * @return the protocol
     */
    public String getDatabaseProtocol();

    /**
     * The protocol separator of the database connection or null if not set.
     *
     * @return The protocol separator.
     */
    public String getDatabaseProtocolSeparator();

    /**
     * The schema separator of the database connection or the default if not set.
     *
     * @return The schema separator.
     */
    public String getDatabaseSchemaSeparator();

    /**
     * Returns the JDBC URL to connect to the database or null if not all required properties are
     * set.
     *
     * @return the database URL
     */
    public String getDatabaseUrl();

    /**
     * Returns the user name of the user that accesses the database or null if not set.
     *
     * @return the user name
     */
    public String getDatabaseUserName();

    /**
     * Returns the password of the user that accesses the database or null if not set.
     *
     * @return the password
     */
    public String getDatabaseUserPassword();

    /**
     * Returns the directory where Communote stores its data.
     *
     * @return the data directory
     */
    public File getDataDirectory();

    /**
     * Returns the full qualified class name of the Hibernate dialect class.
     *
     * @return the hibernate dialect class name
     */
    public String getHibernateDialect();

    /**
     * Returns the name of the running instance.
     *
     * @return the name of the instance
     */
    public String getInstanceName();

    /**
     * Return the key store containing secure keys (dont conflict it with the trust store!)
     *
     * @return the key store
     */
    public KeyStore getKeyStore();

    /**
     * Returns the plug in directory
     *
     * @return String
     */
    public File getPluginDir();

    /**
     * Returns the server ID of this installation.
     *
     * @return the server ID
     */
    public String getServerId();

    /**
     * @return The actual KeyStore or null, if there where errors.
     */
    public KeyStore getTrustStore();

    /**
     *
     * @return True, if the full text features of the database should be used. If the database does
     *         not support the fulltext feature this property is ignored and queries will fall back
     *         to LIKE.
     */
    public boolean isFulltextSearch();

    /**
     * Whether the installation is done.
     *
     * @return true if the installation is done.
     */
    public boolean isInstallationDone();

    /**
     * @param alias
     *            Alias of the certificate.
     * @param certificate
     *            The new certificate. If null, the certificate will be removed.
     * @throws KeyStoreException
     *             Exception.
     */
    public void setCertificate(String alias, Certificate certificate) throws KeyStoreException;

}