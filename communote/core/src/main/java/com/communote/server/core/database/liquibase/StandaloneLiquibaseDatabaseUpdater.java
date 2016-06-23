package com.communote.server.core.database.liquibase;

import java.sql.Connection;

/**
 * Database updater for a standalone installation
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class StandaloneLiquibaseDatabaseUpdater extends AbstractLiquibaseDatabaseUpdater {

    private final String[] contexts;

    public StandaloneLiquibaseDatabaseUpdater() {
        this.contexts = new String[] { AbstractLiquibaseDatabaseUpdater.CHANGESET_CONTEXT_CLIENT,
                AbstractLiquibaseDatabaseUpdater.CHANGESET_CONTEXT_GLOBAL,
                AbstractLiquibaseDatabaseUpdater.CHANGESET_CONTEXT_STANDALONE };
    }

    @Override
    protected String[] getContexts() {
        return contexts;
    }

    @Override
    protected String getDatabaseInformation(Connection connection) {
        // no need to return additional details because it will always be the global database that
        // is updated
        return "global database";
    }

}
