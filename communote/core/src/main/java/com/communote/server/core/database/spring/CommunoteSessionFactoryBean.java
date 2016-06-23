package com.communote.server.core.database.spring;

import org.hibernate.HibernateException;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.dialect.function.SQLFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.hibernate3.LocalSessionFactoryBean;

import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.config.ConfigurationManager;
import com.communote.server.api.core.config.database.DatabaseConfiguration;
import com.communote.server.api.core.config.database.DatabaseConfigurationFactory;
import com.communote.server.core.database.hibernate.HibernateDatabaseConfigurationFactory;

/**
 * LocalSessionFactoryBean which configures the Hibernate session factory with the settings provided
 * by a {@link DatabaseConfiguration}
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class CommunoteSessionFactoryBean extends LocalSessionFactoryBean {

    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(CommunoteSessionFactoryBean.class);

    private DatabaseConfigurationFactory databaseConfigFactory;

    /**
     * Set the connection provider to the Hibernate configuration
     *
     * @param config
     *            the Hibernate configuration to set the connection provider
     * @param databaseConfiguration
     *            the Communote database configuration that
     */
    private void configConnectionProvider(Configuration config,
            DatabaseConfiguration databaseConfiguration) {
        String connectionProviderName = databaseConfiguration.getConnectionProviderClassName();

        config.setProperty(Environment.CONNECTION_PROVIDER, connectionProviderName);
        LOGGER.info("Using connectionProvider: {}", connectionProviderName);
    }

    /**
     * Sets the SQLFunction depending on dialect in the configuration
     *
     * @param config
     *            the configuration
     * @param databaseConfiguration
     *            the database configuration that will be used
     */
    private void configFulltext(Configuration config, DatabaseConfiguration databaseConfiguration) {

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Fulltext feature is {}",
                    databaseConfiguration.isUseFulltextFeature() ? "supported" : "not supported");
        }

        config.addSqlFunction("fulltext", databaseConfiguration.getFulltextSQLFunction());

        SQLFunction datePartSQLFunction = databaseConfiguration.getDatepartSQLFunction();
        if (datePartSQLFunction != null) {
            config.addSqlFunction("date_part", datePartSQLFunction);
        }

        LOGGER.info("Using databaseConfiguration: "
                + databaseConfiguration.getClass().getName()
                + " fulltext function: "
                + databaseConfiguration.getFulltextSQLFunction().getClass().getName()
                + " date_part function: "
                + (datePartSQLFunction == null ? "not supported." : datePartSQLFunction.getClass()
                        .getName()));
    }

    @Override
    protected void postProcessConfiguration(Configuration config) throws HibernateException {

        ConfigurationManager propsManager = CommunoteRuntime.getInstance()
                .getConfigurationManager();
        // TODO this is ugly. Actually the ConfigurationManager should take care of the
        // factory by itself but due to package structure this won't compile. We need to clean up
        // and move stuff to api package and an impl of ConfigurationManager to core.
        if (databaseConfigFactory == null) {
            propsManager
                    .setDatabaseConfigurationFactory(new HibernateDatabaseConfigurationFactory());
        } else {
            propsManager.setDatabaseConfigurationFactory(databaseConfigFactory);
        }
        DatabaseConfiguration databaseConfig = propsManager.getDatabaseConfiguration();
        configConnectionProvider(config, databaseConfig);
        configFulltext(config, databaseConfig);

        String hibernateDialect = propsManager.getStartupProperties().getHibernateDialect();
        if (hibernateDialect != null) {
            // set dialect if available, if not the dialect will be provided by the driver
            config.setProperty(Environment.DIALECT, hibernateDialect);
        }

    }

    /**
     * Set the database configuration factory that should be used to get a
     * {@link DatabaseConfiguration} for configuring Hibernate's session factory. By default an
     * instance of {@link HibernateDatabaseConfigurationFactory} will be used.
     *
     * @param factory
     *            the factory to use. Must not be null.
     */
    public void setDatabaseConfigurationFactory(DatabaseConfigurationFactory factory) {
        if (factory == null) {
            throw new IllegalArgumentException("The factory cannot be null");
        }
        this.databaseConfigFactory = factory;
    }
}
