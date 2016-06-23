package com.communote.server.core.database.config;

import org.hibernate.criterion.MatchMode;
import org.hibernate.dialect.function.SQLFunction;

import com.communote.server.core.database.hibernate.MSSQLServerFulltextFunction;

/**
 * Database Configuration for an MS SQL Server. Will make use of the CONTAINS of MSSQL.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class MSSQLServerDatabaseConfiguration extends FulltextSupportingDatabaseConfiguration {

    private final static String WILDCARD = "*";
    private final static String PADDING = "\"";

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getSpecificFulltextParameterValue(String pattern, MatchMode matchMode) {
        return getWildcardPattern(pattern, matchMode, WILDCARD, PADDING, PADDING);
    }

    /**
     * {@inheritDoc} Will return s SQL Function rendering the "CONTAINS" of MSSQL
     */
    @Override
    protected SQLFunction getSpecificFulltextSQLFunction() {
        return new MSSQLServerFulltextFunction();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isExtendSubselectsWithOuterConditions() {
        // extended the subselects because MSSQL showed really bad performance in some queries like
        // the tag-cloud (KENMEI-5083)
        return true;
    }
}
