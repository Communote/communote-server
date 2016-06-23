package com.communote.server.core.database.liquibase;

import java.sql.Array;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import liquibase.database.structure.PostgresDatabaseSnapshot;
import liquibase.database.structure.UniqueConstraint;
import liquibase.diff.DiffStatusListener;
import liquibase.exception.JDBCException;

/**
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class CommunotePostgresDatabaseSnapshot extends PostgresDatabaseSnapshot {

    public CommunotePostgresDatabaseSnapshot(CommunotePostgresDatabase communotePostgresDatabase,
            Set<DiffStatusListener> statusListeners, String schema) throws JDBCException {
        super(communotePostgresDatabase, statusListeners, schema);
    }

    @Override
    protected void readUniqueConstraints(String schema) throws JDBCException, SQLException {
        updateListeners("Reading unique constraints for " + database.toString() + " ...");
        List<UniqueConstraint> foundUC = new ArrayList<UniqueConstraint>();
        PreparedStatement statement = null;
        ResultSet rs = null;
        try {
            // add some randomness to avoid caching of the schema results (whoever caches it) which
            // sometimes leads to a NPE because conkey is null (at least with postgresql 9.3)
            String stat = "/* " + UUID.randomUUID() + "*/ ";
            stat += "select pgc.conname, pgc.conrelid, pgc.conkey, pgcl.relname ";
            stat += "from pg_namespace pgn, pg_constraint pgc inner join pg_class pgcl on pgcl.oid = pgc.conrelid and pgcl.relkind ='r' ";
            stat += "where contype = 'u' ";
            stat += "and pgn.oid = pgc.connamespace ";
            stat += "and pgn.nspname = ?";

            statement = this.database.getConnection().prepareStatement(stat);
            statement.setString(1, schema);
            rs = statement.executeQuery();
            while (rs.next()) {
                String constraintName = rs.getString("conname");
                int conrelid = rs.getInt("conrelid");
                Array keys = rs.getArray("conkey");
                String tableName = rs.getString("relname");
                UniqueConstraint constraintInformation = new UniqueConstraint();
                constraintInformation.setName(constraintName);
                constraintInformation.setTable(tablesMap.get(tableName));
                getColumnsForUniqueConstraint(conrelid, keys, constraintInformation);
                foundUC.add(constraintInformation);
            }
            this.uniqueConstraints.addAll(foundUC);
        } finally {
            if (rs != null) {
                rs.close();
            }
            if (statement != null) {
                statement.close();
            }

        }
    }

}
