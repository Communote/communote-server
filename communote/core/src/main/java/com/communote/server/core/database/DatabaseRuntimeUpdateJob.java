package com.communote.server.core.database;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.quartz.QuartzJobBean;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.common.ClientAndChannelContextHolder;
import com.communote.server.core.common.database.DatabaseUpdateType;
import com.communote.server.core.common.database.DatabaseUpdater;

/**
 * Job that runs the RUNTIME database updates.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class DatabaseRuntimeUpdateJob extends QuartzJobBean {
    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseRuntimeUpdateJob.class);

    /**
     * {@inheritDoc}
     */
    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        DatabaseUpdater databaseUpdater = ServiceLocator.findService(DatabaseUpdater.class);
        if (databaseUpdater.skipUpdates()) {
            LOGGER.info("Ignoring runtime updates because updates should be skipped");
            return;
        }
        LOGGER.info("Starting runtime database updates");
        updateGlobalDatabase(databaseUpdater);
        LOGGER.info("Runtime database updates done");
    }

    /**
     * Update the global database.
     *
     * @param updater
     *            the updater to use
     */
    private void updateGlobalDatabase(DatabaseUpdater updater) {
        try {
            // clear thread local, just in case of some misconfiguration
            ClientAndChannelContextHolder.clear();
            updater.execute(DatabaseUpdateType.RUNTIME_UPDATE);
        } catch (Exception e) {
            LOGGER.error("Error updating database of global client.", e);
        }
    }

}
