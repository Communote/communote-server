package com.communote.server.core.database.liquibase;

import java.util.Set;

import liquibase.database.PostgresDatabase;
import liquibase.database.structure.DatabaseSnapshot;
import liquibase.diff.DiffStatusListener;
import liquibase.exception.JDBCException;

/**
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class CommunotePostgresDatabase extends PostgresDatabase {

    @Override
    public DatabaseSnapshot createDatabaseSnapshot(String schema,
            Set<DiffStatusListener> statusListeners) throws JDBCException {
        return new CommunotePostgresDatabaseSnapshot(this, statusListeners, schema);
    }

}
