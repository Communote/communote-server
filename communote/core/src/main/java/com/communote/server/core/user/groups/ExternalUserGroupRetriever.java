package com.communote.server.core.user.groups;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.communote.server.api.ServiceLocator;
import com.communote.server.model.user.User;
import com.communote.server.model.user.group.ExternalUserGroup;
import com.communote.server.persistence.user.UserDao;
import com.communote.server.persistence.user.group.ExternalUserGroupDao;

/**
 * {@link UserGroupRetriever}, which retrieves the external users and groups from the internal
 * database.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ExternalUserGroupRetriever extends UserGroupRetriever {

    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(ExternalUserGroupRetriever.class);

    private final ExternalUserGroupDao externalUserGroupDao = ServiceLocator
            .findService(ExternalUserGroupDao.class);

    private final UserDao userDao = ServiceLocator.findService(UserDao.class);

    private final int maxCount = 50;
    private final String externalSystemId;
    private long lastGroupId = -1L;
    private long lastUserId = -1L;

    /**
     * Constructor.
     *
     * @param externalSystemId
     *            The external systems id.
     */
    public ExternalUserGroupRetriever(String externalSystemId) {
        this.externalSystemId = externalSystemId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<ExternalUserGroup> getNextGroups() {
        Collection<ExternalUserGroup> existingGroups = externalUserGroupDao.findLatestBySystemId(
                externalSystemId, lastGroupId, maxCount);
        List<ExternalUserGroup> groups = new ArrayList<ExternalUserGroup>(existingGroups);
        if (groups.size() > 0) {
            lastGroupId = groups.get(groups.size() - 1).getId();
        }
        return existingGroups;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<User> getNextUsers() {
        Collection<User> existingUsers = userDao.findLatestBySystemId(
                externalSystemId, lastUserId, maxCount);
        List<User> users = new ArrayList<User>(existingUsers);
        if (users.size() > 0) {
            lastUserId = users.get(users.size() - 1).getId();
        }
        return existingUsers;
    }

    /**
     * Resets the internal status.
     */
    @Override
    public void start() {
        lastGroupId = -1L;
        lastUserId = -1L;
    }

    /**
     * Logs the success status
     *
     * @param success
     *            True, if the synchronization was successfull.
     */
    @Override
    public void stop(boolean success) {
        LOGGER.debug("The synchronization for system '{}' was successfull={}", externalSystemId,
                success);
    }
}
