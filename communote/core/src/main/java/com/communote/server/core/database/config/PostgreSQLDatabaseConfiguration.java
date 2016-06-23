package com.communote.server.core.database.config;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.MatchMode;
import org.hibernate.dialect.function.SQLFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.communote.server.core.database.hibernate.PostgreSQLDatePartFunction;
import com.communote.server.core.database.hibernate.PostgreSQLFulltextFunction;

/**
 * Database configuration for PostgreSQL with fulltext capabilities
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class PostgreSQLDatabaseConfiguration extends FulltextSupportingDatabaseConfiguration {

    private static final String PROPERTY_CONFIG_TEXT_SEARCH_CONFIGURATION_NAME =
            "communote.database.postgres.textSearchConfigurationName";

    private static final String DEFAULT_TEXT_SEARCH_CONFIGURATION_NAME = "simple";
    private static final String WILDCARD_START = StringUtils.EMPTY;
    private static final String WILDCARD_END = StringUtils.EMPTY;
    private static final Logger LOGGER = LoggerFactory
            .getLogger(PostgreSQLDatabaseConfiguration.class);

    @Override
    public SQLFunction getDatepartSQLFunction() {
        return new PostgreSQLDatePartFunction();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getSpecificFulltextParameterValue(String pattern, MatchMode matchMode) {
        return getWildcardPattern(pattern, matchMode, WILDCARD_START, WILDCARD_END,
                StringUtils.EMPTY,
                StringUtils.EMPTY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected SQLFunction getSpecificFulltextSQLFunction() {
        return new PostgreSQLFulltextFunction(getTextSearchConfigurationName());
    }

    /**
     * 
     * @return get the text search configuration name
     */
    private String getTextSearchConfigurationName() {
        String textSearchConfigurationName = System
                .getProperty(PROPERTY_CONFIG_TEXT_SEARCH_CONFIGURATION_NAME);
        if (textSearchConfigurationName == null || textSearchConfigurationName.trim().length() == 0) {
            textSearchConfigurationName = DEFAULT_TEXT_SEARCH_CONFIGURATION_NAME;
        }

        LOGGER.debug("Using textSearchConfigurationName {} ", textSearchConfigurationName);

        return textSearchConfigurationName;
    }
}
