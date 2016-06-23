package com.communote.server.core.database.liquibase;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.config.type.ClientProperty;
import com.communote.server.core.common.database.DatabaseUpdateException;
import com.communote.server.core.common.database.DatabaseUpdateType;

/**
 * Maps the {@link DatabaseUpdateType}s to liquibase change-log files and the properties to use
 * during the update.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class LiquibaseUpdateDataProvider {

    /** Changelog used for installing the software. */
    private static final String INSTALLER_CHANGELOG = "com/communote/server/liquibase/install/db.changelog.xml";
    /** Changelog for the initial database update at start time. */
    private static final String FIRST_PASS_CHANGELOG = "com/communote/server/liquibase/update/db.changelog.xml";
    /** Changelog containing updates where access to Hibernate is needed. */
    private final static String SECOND_PASS_CHANGELOG = "com/communote/server/liquibase/update_2nd_pass/db.changelog.xml";
    /** Changelog containing runtime updates. */
    private static final String RUNTIME_UPDATE_CHANGELOG = "com/communote/server/liquibase/update_runtime/db.changelog.runtime.xml";

    /**
     * Get the classpath location of the change-log file for the update type
     *
     * @param updateType
     *            the type of update which should be run
     * @return the location of change-log file
     * @throws DatabaseUpdateException
     *             in case there is no change-log for the given type
     */
    public String getChangeLogLocation(DatabaseUpdateType updateType)
            throws DatabaseUpdateException {
        if (updateType == null) {
            throw new DatabaseUpdateException("The updateType cannot be null");
        }
        String location;
        switch (updateType) {
        case INSTALLATION:
            location = INSTALLER_CHANGELOG;
            break;
        case FIRST_PASS_UPDATE:
            location = FIRST_PASS_CHANGELOG;
            break;
        case SECOND_PASS_UPDATE:
            location = SECOND_PASS_CHANGELOG;
            break;
        case RUNTIME_UPDATE:
            location = RUNTIME_UPDATE_CHANGELOG;
            break;
        default:
            throw new DatabaseUpdateException("Unsupported update type " + updateType);
        }
        return location;
    }

    /**
     * Get the parameters which should be passed to the liquibase instance when the change-log file
     * for the given update type is processed. The parameters are mapping from key to value where
     * the key represents a placeholder in a liquibase change-set which should be replaced with the
     * value.
     *
     * @param updateType
     *            the type of update which should be run
     * @return the parameters or null if the change-log file needs no parameters
     */
    public Map<String, Object> getChangeLogParameters(DatabaseUpdateType updateType) {
        if (DatabaseUpdateType.FIRST_PASS_UPDATE.equals(updateType)) {
            HashMap<String, Object> parameters = new HashMap<String, Object>();
            // the uuid for this client, its generated on every db update, however
            // the change-log inserting this id runs only once
            String uuid = UUID.randomUUID().toString();
            parameters.put(ClientProperty.UNIQUE_CLIENT_IDENTIFER.getKeyString(), uuid);
            parameters.put(
                    "communote.standalone.installation",
                    String.valueOf(CommunoteRuntime.getInstance().getApplicationInformation()
                            .isStandalone()));
            return parameters;
        }
        return null;
    }

}
