package com.communote.server.core.common.database;

/**
 * Holds the types for updating a Communote database.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public enum DatabaseUpdateType {

    /**
     * This update is only run once during the installation. It does the basic preparation of the
     * database by setting up the schema that was extracted at a certain point from a running
     * database. Changes to this schema which result from refactorings implemented after that
     * snapshot was taken will be applied with the other update types.
     */
    INSTALLATION,
    /**
     * This update is run every time during startup. It will be executed before the hibernate stack
     * becomes usable and thus should update the schema typically with SQL statements.
     */
    FIRST_PASS_UPDATE,
    /**
     * This update is executed every time during startup after the FIRST_PASS_UPDATE succeeded and
     * the application context, including the hibernate stack, were successfully initialized. This
     * update should not modify the schema but can be used to add entries to the database which have
     * to be available before the application is ready for use.
     */
    SECOND_PASS_UPDATE,
    /**
     * This update is executed with some delay after the application completed its initialization
     * and runs in the background while Communote can already be used. It is intended for changes to
     * the content of the database which take some time and are not directly required after startup.
     */
    RUNTIME_UPDATE;
}
