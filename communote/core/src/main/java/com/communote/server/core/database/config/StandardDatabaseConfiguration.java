package com.communote.server.core.database.config;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.criterion.MatchMode;
import org.hibernate.dialect.function.SQLFunction;

import com.communote.server.api.core.config.database.DatabaseConfiguration;
import com.communote.server.core.database.hibernate.C3P0DataSourceConnectionProvider;
import com.communote.server.core.database.hibernate.StandardLikeFulltextFunction;

/**
 * DatabaseConfiguration without fulltext support. The HQL fulltext function will be translated to a
 * standard LIKE query.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class StandardDatabaseConfiguration implements DatabaseConfiguration {

    /**
     * Name of a System property which can hold the full qualified class name of a
     * org.hibernate.connection.ConnectionProvider
     */
    public static final String COMMUNOTE_CONNECTION_PROVIDER_CLASS = "communote.connectionProviderClass";

    /**
     * Get the class name of the org.hibernate.connection.ConnectionProvider to use. This
     * implementation will return the value of the system property
     * {@value #COMMUNOTE_CONNECTION_PROVIDER_CLASS} or C3P0DataSourceConnectionProvider if the
     * property is not set.
     *
     * @return the full qualified class name of the connection provider
     */
    @Override
    public String getConnectionProviderClassName() {
        String connectionProviderName = System.getProperty(COMMUNOTE_CONNECTION_PROVIDER_CLASS);
        if (StringUtils.isBlank(connectionProviderName)) {
            connectionProviderName = C3P0DataSourceConnectionProvider.class.getName();

        }

        return connectionProviderName;
    }

    @Override
    public SQLFunction getDatepartSQLFunction() {
        // not supported
        return null;
    }

    /**
     * {@inheritDoc}
     *
     * Evaluates to the % syntax
     */
    @Override
    public String getFulltextParameterValue(String pattern, MatchMode matchMode,
            boolean doNotUseFulltext) {
        return matchMode.toMatchString(pattern);
    }

    /**
     * {@inheritDoc}
     *
     * Evaluates to the % syntax
     */
    @Override
    public final String getFulltextParameterValue(String pattern,
            org.hibernate.criterion.MatchMode matchMode) {
        return this.getFulltextParameterValue(pattern, matchMode, false);
    }

    /**
     * {@inheritDoc}
     *
     * The like function will be returned
     */
    @Override
    public SQLFunction getFulltextSQLFunction() {
        return new StandardLikeFulltextFunction();
    }

    @Override
    public boolean isExtendSubselectsWithOuterConditions() {
        return false;
    }

    /**
     * @return always returns false
     */
    @Override
    public boolean isUseFulltextFeature() {
        return false;
    }

}
