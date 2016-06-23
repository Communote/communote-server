package com.communote.server.core.database.hibernate;

import java.util.List;

import org.hibernate.QueryException;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.type.Type;

/**
 * SQL Server Function for rendering the Communote fulltext function for Oracle.
 * 
 * It uses the CONTAINS functionality of Oracle.
 * 
 * For creating an index on SQL Server use:
 * 
 * create index idx_name on table_name(column_name) indextype is ctxsys.context parameters
 * ('wordlist SUBSTRING_PREF MEMORY 50M');
 * 
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class OracleFulltextFunction extends FulltextSQLFunction {

    /**
     * {@inheritDoc}
     */
    @Override
    public String render(Type firstArgumentType, List args, SessionFactoryImplementor factory)
            throws QueryException {
        if (args.size() < 2 || args.size() > 3) {
            throw new QueryException(
                    "Need exactly 2 arguments for fulltext function, but got " + args.size()
                            + " args=" + args);
        }

        return " (CONTAINS(" + args.get(0) + "," + args.get(1) + ",1) >0) and 1 ";
    }
}
