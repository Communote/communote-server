package com.communote.server.test.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.communote.common.io.IOHelper;
import com.communote.server.api.core.config.database.DatabaseType;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class DatabaseUtils {

    private static final String SQL_SCRIPT_BASE_PATH = "com/communote/server/test/sql/";

    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseUtils.class);

    private static void createDatabase(Connection connection, String databaseName,
            HashMap<String, String> replacements, String dbTypeIdentifier, String dbOwner,
            boolean failOnSqlException) throws IOException, SQLException {
        replacements.put("@@DATABASE@@", databaseName);
        replacements.put("@@USERNAME@@", dbOwner);
        URL scriptResource = getRequiredSqlScriptResource("create", dbTypeIdentifier);
        runSqlScript(connection, scriptResource, replacements, failOnSqlException);
    }

    public static void createDatabase(String jdbcTempUrl, String suUsername, String suPassword,
            String databaseName, DatabaseType databaseType, String databaseOwner)
                    throws SQLException, IOException {
        String innerScriptName = databaseType.getIdentifier().toLowerCase(Locale.ENGLISH);
        try (Connection connection = DriverManager.getConnection(jdbcTempUrl, suUsername,
                suPassword)) {
            HashMap<String, String> replacements = new HashMap<String, String>();
            createDatabase(connection, databaseName, replacements, innerScriptName, databaseOwner,
                    true);
        }
    }

    private static void dropDatabase(Connection connection, String databaseName,
            HashMap<String, String> replacements, String dbTypeIdentifier,
            boolean failOnSqlException) throws IOException, SQLException {
        replacements.put("@@DATABASE@@", databaseName);
        URL scriptResource = getRequiredSqlScriptResource("drop", dbTypeIdentifier);
        runSqlScript(connection, scriptResource, replacements, failOnSqlException);
        // some dbs need an additional SQL script: run it if it exists
        scriptResource = getSqlScriptResource("drop", dbTypeIdentifier + "-2nd");
        if (scriptResource != null) {
            runSqlScript(connection, scriptResource, replacements, failOnSqlException);
        }
    }

    public static void dropDatabase(String jdbcTempUrl, String suUsername, String suPassword,
            String databaseName, DatabaseType databaseType, boolean failOnSqlException)
                    throws SQLException, IOException {
        String innerScriptName = databaseType.getIdentifier().toLowerCase(Locale.ENGLISH);
        try (Connection connection = DriverManager.getConnection(jdbcTempUrl, suUsername,
                suPassword)) {
            HashMap<String, String> replacements = new HashMap<String, String>();
            dropDatabase(connection, databaseName, replacements, innerScriptName,
                    failOnSqlException);
        }
    }

    public static URL getRequiredSqlScriptResource(String namePrefix, DatabaseType databaseType)
            throws IOException {
        return getRequiredSqlScriptResource(namePrefix,
                databaseType.getIdentifier().toLowerCase(Locale.ENGLISH));
    }

    private static URL getRequiredSqlScriptResource(String namePrefix, String dbTypeIdentifier)
            throws IOException {
        String scriptPath = SQL_SCRIPT_BASE_PATH + namePrefix + "." + dbTypeIdentifier + ".sql";
        URL scriptResource = ClassLoader.getSystemResource(scriptPath);
        if (scriptResource == null) {
            throw new IOException("SQL script " + scriptPath + " not found on classpath");
        }
        return scriptResource;
    }

    private static URL getSqlScriptResource(String namePrefix, String dbTypeIdentifier) {
        String scriptPath = SQL_SCRIPT_BASE_PATH + namePrefix + "." + dbTypeIdentifier + ".sql";
        return ClassLoader.getSystemResource(scriptPath);
    }

    public static void recreateDatabase(String jdbcTempUrl, String suUsername, String suPassword,
            String databaseName, DatabaseType databaseType, String databaseOwner)
                    throws SQLException, IOException {
        LOGGER.debug(
                "Going to re-create database '{}'. Using following JDBC URL for connecting: {}",
                databaseName, jdbcTempUrl);
        String innerScriptName = databaseType.getIdentifier().toLowerCase(Locale.ENGLISH);
        try (Connection connection = DriverManager.getConnection(jdbcTempUrl, suUsername,
                suPassword)) {
            HashMap<String, String> replacements = new HashMap<String, String>();
            // drop if exists and create new
            dropDatabase(connection, databaseName, replacements, innerScriptName, false);
            createDatabase(connection, databaseName, replacements, innerScriptName, databaseOwner,
                    true);
        }
    }

    public static final void runSqlScript(Connection connection, URL sqlScriptResource,
            Map<String, String> replacements, boolean failOnSqlException) throws IOException,
            SQLException {
        String sqlScript = IOUtils.toString(sqlScriptResource, "UTF-8");
        if (replacements != null) {
            for (String placeholder : replacements.keySet()) {
                sqlScript = sqlScript.replace(placeholder, replacements.get(placeholder));
            }
        }
        if (StringUtils.isBlank(sqlScript)) {
            LOGGER.info("Ignored empty script: {}", sqlScriptResource);
        } else {
            try {
                Statement statement = connection.createStatement();
                statement.execute(sqlScript);
                statement.close();
                LOGGER.info("Successfully executed script: {}", sqlScriptResource);
            } catch (SQLException e) {
                if (failOnSqlException) {
                    throw e;
                }
                LOGGER.warn("There was an error executing the script (" + sqlScriptResource
                        + "), try to continue without: "
                        + (sqlScript.length() > 300 ? sqlScript.substring(0, 300) : sqlScript)
                        + "... , Error message: " + e.getMessage());
            }
        }
    }

    public static final void runSqlScriptLineByLine(Connection connection, URL sqlScriptResource,
            Map<String, String> replacements, boolean failOnSqlException) throws IOException,
            SQLException {
        String sqlScript = IOUtils.toString(sqlScriptResource, "UTF-8");
        if (replacements != null) {
            for (String placeholder : replacements.keySet()) {
                sqlScript = sqlScript.replace(placeholder, replacements.get(placeholder));
            }
        }
        if (StringUtils.isBlank(sqlScript)) {
            LOGGER.info("Ignored empty script: {}", sqlScriptResource);
        } else {
            StringReader stringReader = null;
            BufferedReader bufferedReader = null;
            try {
                stringReader = new StringReader(sqlScript);
                bufferedReader = new BufferedReader(stringReader);
                String statementString;
                while ((statementString = bufferedReader.readLine()) != null) {
                    if (statementString.endsWith(";")) {
                        statementString = statementString
                                .substring(0, statementString.length() - 1);
                    }
                    LOGGER.debug("Executing statement {}", statementString);
                    Statement statement = connection.createStatement();
                    statement.execute(statementString);
                    statement.close();
                }
                LOGGER.info("Successfully executed script: {}", sqlScriptResource);
            } catch (SQLException e) {
                if (failOnSqlException) {
                    throw e;
                }
                LOGGER.warn("There was an error executing the script (" + sqlScriptResource
                        + "), try to continue without: "
                        + (sqlScript.length() > 300 ? sqlScript.substring(0, 300) : sqlScript)
                        + "... , Error message: " + e.getMessage());
            } finally {
                IOHelper.close(stringReader);
                IOHelper.close(bufferedReader);
            }
        }
    }

    private DatabaseUtils() {
    }
}
