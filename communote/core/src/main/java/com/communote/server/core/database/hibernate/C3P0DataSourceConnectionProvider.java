package com.communote.server.core.database.hibernate;

import java.beans.PropertyVetoException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;

import org.hibernate.HibernateException;
import org.hibernate.connection.ConnectionProvider;
import org.hibernate.util.JDBCExceptionReporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.DelegatingDataSource;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;

import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.config.StartupProperties;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.mchange.v2.c3p0.PooledDataSource;

/**
 * Connection Provider which uses a c3p0 pooled {@link DataSource}.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class C3P0DataSourceConnectionProvider implements ConnectionProvider {

    /** Logger. */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(C3P0DataSourceConnectionProvider.class);
    /**
     * Map of data sources
     */
    private DataSource cachedDataSource;

    /**
     * Close all data sources
     */
    @Override
    public synchronized void close() {
        LOGGER.info("Closing all database connections ... ");

        closeDataSource(cachedDataSource);
        cachedDataSource = null;

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void closeConnection(Connection con) throws SQLException {
        // simply call Connection.close
        try {
            con.close();
        } catch (SQLException ex) {
            JDBCExceptionReporter.logExceptions(ex);
            throw ex;
        }

    }

    /**
     * Close the datasource. This part is c3p0 specific, since the close is not generally available
     * on the {@link DataSource} interface.
     *
     * @param dataSource
     *            the data source to close. if null, nothing will be done.
     */
    private void closeDataSource(DataSource dataSource) {
        try {
            // if wrapped get target DataSource
            while (dataSource instanceof DelegatingDataSource) {
                dataSource = ((DelegatingDataSource) dataSource).getTargetDataSource();
            }
            if (dataSource instanceof PooledDataSource) {
                LOGGER.info("Closing database connection pool.");
                ((PooledDataSource) dataSource).close();
            }
        } catch (Exception e) {
            LOGGER.error("An error occurred while closing the connections", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void configure(Properties props) throws HibernateException {
        // nothing to do
    }

    /**
     * Create the DataSource from which the connections should be retrieved. This implementation
     * creates and configures a c3p0 pooled DataSource which is passed to
     * {@link #postDataSourceCreation(DataSource)}. The result of that method will be returned.
     *
     * @return the data source
     * @throws SQLException
     *             in case of an error
     * @throws PropertyVetoException
     *             in case of an error
     */
    protected DataSource createDataSource() throws SQLException, PropertyVetoException {

        StartupProperties props = CommunoteRuntime.getInstance().getConfigurationManager()
                .getStartupProperties();

        ComboPooledDataSource dataSource = new ComboPooledDataSource("global");
        dataSource.setUser(props.getDatabaseUserName());
        dataSource.setPassword(props.getDatabaseUserPassword());
        dataSource.setJdbcUrl(props.getDatabaseUrl());
        dataSource.setDriverClass(props.getDatabaseDriverClassName());

        return postDataSourceCreation(dataSource);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Connection getConnection() throws SQLException {
        DataSource source = getDataSource();
        Connection connection = source.getConnection();
        return connection;
    }

    /**
     * Get the datasource for the client
     *
     * @return the datasource
     * @throws SQLException
     *             in case the datasource could not be created
     */
    protected DataSource getDataSource() throws SQLException {

        if (cachedDataSource == null) {
            synchronized (this) {

                // no yet cached? create it and cache it.
                if (cachedDataSource == null) {
                    DataSource dataSource = null;
                    try {
                        dataSource = createDataSource();
                    } catch (PropertyVetoException e) {
                        throw new SQLException(e.getMessage());
                    }
                    cachedDataSource = dataSource;
                }

            }
        }

        return cachedDataSource;
    }

    /**
     * Method that is called after DataSource creation to allow further modifications of the
     * DataSource. The default implementation will wrap the DataSource into a
     * LazyConnectionDataSourceProxy.
     *
     * @param dataSource
     *            the DataSource that was just created
     * @return the final, lazily connecting DataSource
     */
    protected DataSource postDataSourceCreation(DataSource dataSource) {
        LazyConnectionDataSourceProxy lazyDataSource = new LazyConnectionDataSourceProxy(dataSource);
        return lazyDataSource;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean supportsAggressiveRelease() {
        // the pooling might return different connections
        return false;
    }

}
