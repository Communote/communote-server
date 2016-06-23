package com.communote.server.core.database.liquibase;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.Map.Entry;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.exception.JDBCException;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.connection.ConnectionProvider;
import org.hibernate.engine.SessionFactoryImplementor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.ResourceLoader;

import com.communote.server.core.common.database.DatabaseUpdateException;
import com.communote.server.core.common.database.DatabaseUpdateType;
import com.communote.server.core.common.database.DatabaseUpdater;

/**
 * Database updater using the liquibase framework and a configured Hibernate session factory
 * implementor to get the database connection. <br />
 * Implementation is inspired by {@see liquibase.spring.SpringLiquibase}
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class AbstractLiquibaseDatabaseUpdater extends DatabaseUpdater implements
        ResourceLoaderAware {

    private final static Logger LOGGER = LoggerFactory
            .getLogger(AbstractLiquibaseDatabaseUpdater.class);

    /**
     * Context to be used in change-sets which should be run on all databases. For a standalone
     * installation this is the one and only database. For an installation supporting multi-tenancy
     * this refers to the databases of all tenants. Using this context has the same effect as
     * providing no context at a change-set.
     */
    protected static final String CHANGESET_CONTEXT_CLIENT = "client";
    /**
     * Context to be used in change-sets which should only be run on the master (aka global)
     * database. For a standalone installation this is the one and only database. For an
     * installation supporting multi-tenancy this refers to the database of the master tenant, which
     * is the one that was created first and allows creating other tenants.
     */
    protected static final String CHANGESET_CONTEXT_GLOBAL = "global";
    /**
     * Context to be used in change-sets which should be run if the installation is a standalone
     * installation. For an installation supporting multi-tenancy change-sets with this context
     * should be ignored.
     */
    protected static final String CHANGESET_CONTEXT_STANDALONE = "standalone";

    private ResourceLoader resourceLoader;

    private LiquibaseUpdateDataProvider updateDataProvider;

    private SessionFactoryImplementor sessionFactoryImplementor;;

    public AbstractLiquibaseDatabaseUpdater() {
        // exists preconditions can lead to NPE with postgresql and liquibase 1.9.5: add a custom DB
        // with a workaround
        DatabaseFactory.getInstance().addDatabaseImplementation(new CommunotePostgresDatabase());
    }

    /**
     * Is called before a database update is started. Default implementation does nothing
     *
     * @param updateType
     *            the type of the update which should be run
     * @throws DatabaseUpdateException
     *             in case of an error, like some missing preconditions
     */
    protected void beforeDatabaseUpdate(DatabaseUpdateType updateType)
            throws DatabaseUpdateException {
    }

    /**
     * Create a liquibase
     *
     * @param connection
     *            the database connection
     * @param changeLog
     *            the ChangeLog to use
     * @return the liquibase to be used for updating
     * @throws JDBCException
     *             in case of an db error
     */
    private Liquibase createLiquibase(Connection connection, String changeLog) throws JDBCException {
        return new Liquibase(changeLog, new SpringResourceOpener(getResourceLoader(), changeLog),
                getDatabaseImplementation(connection));
    }

    @Override
    protected void doExecute(DatabaseUpdateType updateType) throws DatabaseUpdateException {
        LiquibaseUpdateDataProvider dataProvider = getUpdateDataProvider();
        if (dataProvider == null) {
            throw new DatabaseUpdateException("No LiquibaseDataProvider is set!");
        }
        String changeLogFile = dataProvider.getChangeLogLocation(updateType);
        beforeDatabaseUpdate(updateType);
        updateDatabase(changeLogFile, dataProvider.getChangeLogParameters(updateType));
    }

    /**
     * @return the connection that can be used to update the database
     * @throws SQLException
     *             in case of an sql exception
     */
    protected Connection getConnection() throws SQLException {
        return getConnectionProvider().getConnection();
    }

    /**
     * @return the {@link ConnectionProvider} to use
     */
    private ConnectionProvider getConnectionProvider() {
        return sessionFactoryImplementor.getConnectionProvider();
    }

    /**
     * The contexts of the update. Any liquibase change-set which has a context which is not the
     * returned array will be ignored. A change-set without context, however, will be executed.
     *
     * @return the contexts
     */
    protected abstract String[] getContexts();

    /**
     * Returns the database implementation matching the connection.
     *
     * @param connection
     *            the connection to use
     * @return the database implementation matching the connection
     * @throws JDBCException
     *             in case of an database connection error
     */
    private Database getDatabaseImplementation(Connection connection) throws JDBCException {
        return DatabaseFactory.getInstance().findCorrectDatabaseImplementation(connection);
    }

    /**
     * Get some information about the database. This method is only called if the update resulted in
     * an error to create a useful log message.
     *
     * @param connection
     *            the database connection that was used in the update or null if the update failed
     *            because no connection could be retrieved
     * @return the database information to add to the error log message
     */
    protected abstract String getDatabaseInformation(Connection connection);

    /**
     * @return the resourceLoader
     */
    public ResourceLoader getResourceLoader() {
        return resourceLoader;
    }

    /**
     * @return the configured sessionFactoryImplementor
     */
    protected SessionFactoryImplementor getSessionFactoryImplementor() {
        return sessionFactoryImplementor;
    }

    /**
     * @return the set liquibase data provider or an instance of {@link LiquibaseUpdateDataProvider}
     *         if no custom provider was set with
     *         {@link #setUpdateDataProvider(LiquibaseUpdateDataProvider)}
     */
    protected LiquibaseUpdateDataProvider getUpdateDataProvider() {
        if (updateDataProvider == null) {
            updateDataProvider = new LiquibaseUpdateDataProvider();
        }
        return updateDataProvider;
    }

    /**
     * Prepare the liquibase instance before doing the actual update. This implementation sets the
     * change-log parameters.
     *
     * @param liquibase
     *            the liquibase instance to prepare for the update
     * @param changeLogParameters
     *            the parameters to replace placeholders in the change-log file. Can be null.
     * @throws DatabaseUpdateException
     *             in case the preparation lead to an error
     */
    protected void prepareLiquibase(Liquibase liquibase, Map<String, Object> changeLogParameters)
            throws DatabaseUpdateException {
        if (changeLogParameters != null) {
            for (Entry<String, Object> parameter : changeLogParameters.entrySet()) {

                liquibase.setChangeLogParameterValue(parameter.getKey(), parameter.getValue());
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;

    }

    /**
     * Set the hibernate session factory implementor to use for retrieving a connection for updating
     * the database.
     *
     * @param sessionFactoryImplementor
     *            the sessionFactoryImplementor to set
     */
    public void setSessionFactoryImplementor(SessionFactoryImplementor sessionFactoryImplementor) {
        this.sessionFactoryImplementor = sessionFactoryImplementor;
    }

    /**
     * Set the data provider to use for the update
     *
     * @param updateDataProvider
     *            the data provider
     */
    public void setUpdateDataProvider(LiquibaseUpdateDataProvider updateDataProvider) {
        this.updateDataProvider = updateDataProvider;
    }

    /**
     * Update the database that is returned by {@link #getConnection()} with the given change-log
     * file and change-log parameters.
     *
     * @param changeLog
     *            the location of the change-log file on the classpath
     * @param changeLogParameters
     *            mapping from key to value where the key represents a palceholder in a change-set
     *            of the change-log file which should be replaced by the value. Can be null.
     * @throws DatabaseUpdateException
     *             in case the update failed
     */
    protected void updateDatabase(String changeLog, Map<String, Object> changeLogParameters)
            throws DatabaseUpdateException {

        Connection connection = null;

        try {

            // get the connection
            connection = getConnection();

            // create the liquibase
            Liquibase liquibase = createLiquibase(connection, changeLog);
            prepareLiquibase(liquibase, changeLogParameters);
            // update using the contexts
            liquibase.update(StringUtils.join(getContexts(), ','));
        } catch (Throwable e) {
            LOGGER.error("Error updating the database! Database: "
                    + getDatabaseInformation(connection), e);
            // avoid double-wrapping
            if (e instanceof DatabaseUpdateException) {
                throw (DatabaseUpdateException) e;
            }
            throw new DatabaseUpdateException("Error updating the database!", e);
        } finally {
            // do a rollback on error
            if (connection != null) {
                try {
                    connection.rollback();
                    connection.close();
                } catch (SQLException e) {
                    LOGGER.debug(e.getMessage(), e);
                }
            }
        }
    }

    @Override
    public boolean updaterRunBefore() {
        Connection connection = null;
        boolean run = false;
        try {
            // get the connection
            connection = getConnection();
            Database database = getDatabaseImplementation(connection);
            run = database.doesChangeLogTableExist() && database.getRanChangeSetList().size() > 0;
        } catch (Exception e) {
            // just swallow the exception
            LOGGER.debug("Checking for existance of changelog table failed.", e);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    // do nothing
                }
            }
        }
        return run;
    }
}
