package com.communote.server.core.database.hibernate;

import java.util.HashMap;
import java.util.Map;

import org.hibernate.dialect.Oracle10gDialect;
import org.hibernate.dialect.PostgreSQLDialect;
import org.hibernate.dialect.SQLServerDialect;

import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.config.StartupProperties;
import com.communote.server.api.core.config.database.DatabaseConfiguration;
import com.communote.server.api.core.config.database.DatabaseConfigurationFactory;
import com.communote.server.core.database.config.FulltextSupportingDatabaseConfiguration;
import com.communote.server.core.database.config.MSSQLServerDatabaseConfiguration;
import com.communote.server.core.database.config.OracleDatabaseConfiguration;
import com.communote.server.core.database.config.PostgreSQLDatabaseConfiguration;
import com.communote.server.core.database.config.StandardDatabaseConfiguration;

/**
 * Factory which returns a matching DatabaseConfiguration for a Hibernate dialect name.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class HibernateDatabaseConfigurationFactory implements DatabaseConfigurationFactory {

    private final Map<String, DatabaseConfiguration> builtInConfigurations;
    private Map<String, DatabaseConfiguration> additionalConfigurations;

    public HibernateDatabaseConfigurationFactory() {
        builtInConfigurations = new HashMap<String, DatabaseConfiguration>();
        builtInConfigurations.put(SQLServerDialect.class.getName(),
                new MSSQLServerDatabaseConfiguration());
        builtInConfigurations.put(Oracle10gDialect.class.getName(),
                new OracleDatabaseConfiguration());
        builtInConfigurations.put(PostgreSQLDialect.class.getName(),
                new PostgreSQLDatabaseConfiguration());
    }

    @Override
    public DatabaseConfiguration createDatabaseConfiguration() {
        StartupProperties startupProps = CommunoteRuntime.getInstance().getConfigurationManager()
                .getStartupProperties();
        String fullClassNameOfDialect = startupProps.getHibernateDialect();
        DatabaseConfiguration config = null;
        if (fullClassNameOfDialect != null) {
            if (additionalConfigurations != null) {
                config = additionalConfigurations.get(fullClassNameOfDialect);
            }
            if (config == null) {
                config = builtInConfigurations.get(fullClassNameOfDialect);
            }
        }
        if (config == null) {
            config = new StandardDatabaseConfiguration();
        } else if (config instanceof FulltextSupportingDatabaseConfiguration) {
            // enable or disable the config based configuration
            ((FulltextSupportingDatabaseConfiguration) config).setUseFulltextFeature(startupProps
                    .isFulltextSearch());
        }
        return config;
    }

    /**
     * Register a DatabaseConfiguration for a Hibernate dialect. Adding a configuration with this
     * method will override any previously registered or built-in configuration for the same
     * dialect.
     *
     * @param fullClassNameOfDialect
     *            the full qualified class name of the Hibernate dialect
     * @param configuration
     *            the configuration to register
     */
    public void registerDatabaseConfiguration(String fullClassNameOfDialect,
            DatabaseConfiguration configuration) {
        HashMap<String, DatabaseConfiguration> configs = new HashMap<String, DatabaseConfiguration>();
        if (this.additionalConfigurations != null) {
            configs.putAll(additionalConfigurations);
        }
        configs.put(fullClassNameOfDialect, configuration);
        additionalConfigurations = configs;
    }
}
