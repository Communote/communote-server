package com.communote.server.core.database.hibernate;

import java.util.List;

import org.hibernate.QueryException;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.type.Type;

/**
 * SQL Server Function for rendering the Communote fulltext function for MSSQL Server.
 * 
 * It uses the CONTAINS functionality of MSSQL.
 * 
 * For creating an index on SQL Server use:
 * 
 * CREATE FULLTEXT CATALOG CNTFULLTEXT;
 * 
 * CREATE FULLTEXT INDEX ON core_content ( content ) KEY INDEX PK__core_con__3214EC274316F928 ON
 * CNTFULLTEXT WITH CHANGE_TRACKING AUTO GO
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class MSSQLServerFulltextFunction extends FulltextSQLFunction {

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
        return "CONTAINS(" + args.get(0) + "," + args.get(1) + ") AND 1 ";
    }
}
