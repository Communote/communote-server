package com.communote.server.core.common.database;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Component for installing and updating a Communote database to the latest state.
 * <p>
 * Since the updater is run as a part of the startup process, implementors have to make sure that
 * the individual changes to the schema or content are not run again if they have already been
 * executed before.
 * </p>
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class DatabaseUpdater {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseUpdater.class);

    private static final String PROPERTY_SKIP_UPDATES = "com.communote.database.update.skip";
    private Boolean skip;
    private final Set<DatabaseUpdateType> runUpdates = new HashSet<>(
            DatabaseUpdateType.values().length);

    /**
     * Will be invoked by {@link #execute(DatabaseUpdateType)} to do the actual update.
     *
     * @param updateType
     *            the type of update that should be executed
     * @throws DatabaseUpdateException
     *             in case of an error database update error
     */
    protected abstract void doExecute(DatabaseUpdateType updateType) throws DatabaseUpdateException;

    /**
     * Execute the given update. This method will not check whether updates should be skipped.
     *
     * @param updateType
     *            the type of update that should be executed
     * @throws DatabaseUpdateException
     *             in case of an error database update error
     */
    public void execute(DatabaseUpdateType updateType) throws DatabaseUpdateException {
        LOGGER.debug("Starting update type {}", updateType);
        doExecute(updateType);
        runUpdates.add(updateType);
        LOGGER.debug("Update type {} succeeded", updateType);
    }

    /**
     * Return whether updates should be skipped. This method will return true if the system property
     * {@value #PROPERTY_SKIP_UPDATES} is set and has the value "true" (case-insensitive).
     *
     * @return true if updates should be skipped
     */
    public boolean skipUpdates() {
        if (skip == null) {
            skip = Boolean.getBoolean(PROPERTY_SKIP_UPDATES);
        }
        return skip;
    }

    /**
     * Executes the first and second pass updates if updates should not be skipped.
     *
     * @throws DatabaseUpdateException
     */
    public void updateDatabase() throws DatabaseUpdateException {
        if (skipUpdates()) {
            LOGGER.info("Ignoring database updates because updates should be skipped");
        } else {
            LOGGER.info("Starting update of database");
            try {
                execute(DatabaseUpdateType.FIRST_PASS_UPDATE);
                execute(DatabaseUpdateType.SECOND_PASS_UPDATE);
            } catch (DatabaseUpdateException e) {
                LOGGER.error("Update of database failed: " + e.getMessage(), e);
                throw e;

            }
            LOGGER.info("Update of database succeeded");
        }
    }

    /**
     * Returns whether the updater run at least once against the database. It does not matter which
     * DatabaseUpdateType was executed.
     *
     * @return true if the updater was run before
     */
    public abstract boolean updaterRunBefore();

    /**
     * Return whether this updater has run the given update after this instance was created.
     *
     * @param updateType
     *            the update type to check for having been run
     * @return true if the update was run
     */
    public boolean updateRunInSession(DatabaseUpdateType updateType) {
        return runUpdates.contains(updateType);
    }
}
