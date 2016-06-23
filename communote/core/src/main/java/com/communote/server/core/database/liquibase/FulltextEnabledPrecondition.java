package com.communote.server.core.database.liquibase;

import liquibase.database.Database;
import liquibase.exception.CustomPreconditionErrorException;
import liquibase.exception.CustomPreconditionFailedException;
import liquibase.preconditions.CustomPrecondition;

import com.communote.server.api.core.application.CommunoteRuntime;

/**
 * Precondition that checks if the fulltext feture is enabled for communote.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class FulltextEnabledPrecondition implements CustomPrecondition {

    /**
     * {@inheritDoc}
     */
    @Override
    public void check(Database arg0) throws CustomPreconditionFailedException,
    CustomPreconditionErrorException {

        boolean isFulltextSearch = CommunoteRuntime.getInstance().getConfigurationManager()
                .getStartupProperties().isFulltextSearch();

        if (!isFulltextSearch) {
            // must pass a throwable otherwise liquibase will die with a NullPointerException
            // because it relies on the existence of a cause
            throw new CustomPreconditionErrorException("Fulltext is not enabled", new Exception(
                    "INFO: Fulltext is not enabled"));
        }
    }
}
