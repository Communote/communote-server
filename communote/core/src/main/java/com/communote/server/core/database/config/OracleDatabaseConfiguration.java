package com.communote.server.core.database.config;

import org.hibernate.criterion.MatchMode;
import org.hibernate.dialect.function.SQLFunction;

import com.communote.server.core.database.hibernate.OracleFulltextFunction;


/**
 * Database Configuration for Oracle. Will make use of the CONTAINS of Oracle.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class OracleDatabaseConfiguration extends FulltextSupportingDatabaseConfiguration {

    private final static String WILDCARD = "%";
    private final static String PADDING = "'";

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getSpecificFulltextParameterValue(String pattern, MatchMode matchMode) {
        return getWildcardPattern(pattern, matchMode, WILDCARD, PADDING, PADDING);
    }

    /**
     * {@inheritDoc} Will return s SQL Function rendering the "CONTAINS" of Oracle
     */
    @Override
    protected SQLFunction getSpecificFulltextSQLFunction() {
        return new OracleFulltextFunction();
    }
}
