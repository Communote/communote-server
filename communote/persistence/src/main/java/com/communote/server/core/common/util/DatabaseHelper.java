package com.communote.server.core.common.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.config.StartupProperties;
import com.communote.server.api.core.config.database.DatabaseConnectionException;

/**
 * Helper for database operations.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class DatabaseHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseHelper.class);

    /**
     * Close the given connection . Can be null. Ignores any exception.
     *
     * @param connection
     *            SQL Connection
     */
    public static void closeConnection(Connection connection) {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            LOGGER.debug(e.getMessage());
        }
    }

    /**
     * @return The startup properties.
     */
    private static StartupProperties getStartupProperties() {
        return CommunoteRuntime.getInstance().getConfigurationManager().getStartupProperties();
    }

    /**
     * Tries to connect to the global database.
     *
     * @throws DatabaseConnectionException
     *             in case the connection cannot be established
     */
    public static void testDatabaseConnection() throws DatabaseConnectionException {
        StartupProperties props = getStartupProperties();
        testDatabaseConnection(props.getDatabaseDriverClassName(), props.getDatabaseUrl(),
                props.getDatabaseUserName(), props.getDatabaseUserPassword());
    }

    /**
     * Tries to connect to the named database.
     *
     * @param driverClassName
     *            name of the driver class
     * @param url
     *            the URL to the database
     * @param login
     *            the login name to use for the connection
     * @param password
     *            the password the password to use for the connection
     * @throws DatabaseConnectionException
     *             in case the connection cannot be established
     */
    public static void testDatabaseConnection(String driverClassName, String url, String login,
            String password) throws DatabaseConnectionException {
        if (StringUtils.isBlank(driverClassName)) {
            throw new DatabaseConnectionException("The driver class name cannot be empty");
        }
        if (StringUtils.isBlank(url)) {
            throw new DatabaseConnectionException("The jdbc URL cannot be empty");
        }
        try {
            Class.forName(driverClassName);
        } catch (Exception e) {
            LOGGER.error("Retriving the database driver failed.", e);
            throw new DatabaseConnectionException("Driver " + driverClassName + " does not exist.");
        }
        Connection connection = null;

        try {
            connection = DriverManager.getConnection(url, login, password);
        } catch (SQLException e) {
            throw new DatabaseConnectionException(e.getLocalizedMessage(), e);
        } finally {
            closeConnection(connection);
        }
    }

    private DatabaseHelper() {
        // no construction
    }

}
