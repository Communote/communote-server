package de.communardo.kenmei.database.update;

import liquibase.FileOpener;
import liquibase.change.custom.CustomTaskChange;
import liquibase.database.Database;
import liquibase.exception.CustomChangeException;
import liquibase.exception.InvalidChangeDefinitionException;
import liquibase.exception.SetupException;
import liquibase.exception.UnsupportedChangeException;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.config.type.ClientProperty;
import com.communote.server.model.global.GlobalId;
import com.communote.server.persistence.global.GlobalIdDao;
import com.communote.server.persistence.global.GlobalIdType;

/**
 * Update task to correct wrong global identifier strings of global ID entities.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class CorrectGlobalIdUpdateTask implements CustomTaskChange {
    private static final Logger LOG = Logger.getLogger(CorrectGlobalIdUpdateTask.class);

    private String confirmMessage;

    /**
     * Corrects the global identifier string of the global ID entity if necessary.
     *
     * @param entityToUpdate
     *            the entity to fix
     * @param correctPrefix
     *            the expected correct prefix
     * @param globalIdDao
     *            the DAO to use
     * @return whether the entity was updated
     * @throws CustomChangeException
     *             if the update fails
     */
    private boolean correctGlobalIdString(GlobalId entityToUpdate, String correctPrefix,
            GlobalIdDao globalIdDao) throws CustomChangeException {
        String oldGlobalIdentifier = entityToUpdate.getGlobalIdentifier();
        String potentiallyWrongPrefix = extractPrefix(oldGlobalIdentifier);
        if (potentiallyWrongPrefix != null && !potentiallyWrongPrefix.equals(correctPrefix)) {
            String newGlobalIdentifier = correctPrefix
                    + StringUtils.removeStart(oldGlobalIdentifier, potentiallyWrongPrefix);
            entityToUpdate.setGlobalIdentifier(newGlobalIdentifier);
            try {
                globalIdDao.update(entityToUpdate);
                return true;
            } catch (Exception e) {
                // might have happened because the entity was deleted, if not fail
                if (globalIdDao.load(entityToUpdate.getId()) != null) {
                    throw new CustomChangeException("Updating global identifier of entity "
                            + entityToUpdate.getId() + " failed", e);
                }
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(Database arg0) throws CustomChangeException, UnsupportedChangeException {
        GlobalIdDao globalIdDao = ServiceLocator.findService(GlobalIdDao.class);
        GlobalId latestGlobalId = globalIdDao.findLatestGlobalId();
        int modifiedIds = 0;
        if (latestGlobalId != null) {
            long max = latestGlobalId.getId();
            String uniqueClientId = CommunoteRuntime.getInstance().getConfigurationManager()
                    .getClientConfigurationProperties()
                    .getProperty(ClientProperty.UNIQUE_CLIENT_IDENTIFER);
            String correctPrefix = "/" + uniqueClientId + "/";
            // update all globalIds up to the latest
            for (long i = 0; i <= max; i++) {
                // try loading, if it not exists just skip it
                GlobalId entityToUpdate = globalIdDao.load(i);
                if (entityToUpdate != null) {
                    if (correctGlobalIdString(entityToUpdate, correctPrefix, globalIdDao)) {
                        modifiedIds++;
                    }
                }
            }
        }
        confirmMessage = "Updated " + modifiedIds + " global IDs";
    }

    /**
     * Extracts the prefix of the global identifier string up to the type defining part (e.g. note,
     * tag). The return value ends with a slash and could be the correct prefix or one of the
     * following wrong prefixes: '/uniqueClientId/clientId/',
     * '/uniqueClientIdOfGlobalClient/clientId/' and '/null/clientId/'.
     *
     * @param globalIdentifier
     *            the global identifier
     * @return the prefix or null if the identifier is dead wrong
     */
    private String extractPrefix(String globalIdentifier) {
        int idx = globalIdentifier.lastIndexOf("/");
        if (idx > 0) {
            int startFrom = idx - 1;
            idx = globalIdentifier.lastIndexOf("/", startFrom);
            if (idx > 0) {
                String typeString = globalIdentifier.substring(idx + 1, startFrom + 1);
                // don't touch non supported globalIds
                if (typeString.equals(GlobalIdType.NOTE.getGlobalIdPath())
                        || typeString.equals(GlobalIdType.TAG.getGlobalIdPath())
                        || typeString.equals(GlobalIdType.BLOG.getGlobalIdPath())
                        || typeString.equals(GlobalIdType.ATTACHMENT.getGlobalIdPath())
                        || typeString.equals(GlobalIdType.USER.getGlobalIdPath())
                        || typeString.equals(GlobalIdType.GROUP.getGlobalIdPath())) {
                    return globalIdentifier.substring(0, idx + 1);
                }
            }
        }
        LOG.info("Cannot correct global identifier " + globalIdentifier
                + " because it does match the expected pattern.");
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getConfirmationMessage() {
        return confirmMessage;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setFileOpener(FileOpener arg0) {
        // nothing

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setUp() throws SetupException {
        // nothing

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validate(Database arg0) throws InvalidChangeDefinitionException {
        // nothing

    }

}
