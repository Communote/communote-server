package com.communote.server.api.core.config.database;


/**
 * Factory to create a database configuration.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public interface DatabaseConfigurationFactory {

    /**
     * @return the created database configuration
     */
    DatabaseConfiguration createDatabaseConfiguration();
}
