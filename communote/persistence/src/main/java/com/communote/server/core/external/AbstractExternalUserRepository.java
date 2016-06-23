package com.communote.server.core.external;

import com.communote.server.core.security.UserIdentification;
import com.communote.server.model.config.ExternalSystemConfiguration;
import com.communote.server.persistence.user.ExternalUserVO;
import com.communote.server.plugins.api.externals.ExternalUserGroupAccessor;

/**
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public abstract class AbstractExternalUserRepository implements ExternalUserRepository {

    private ExternalUserGroupAccessor externalUserGroupAccessor;

    private final String defaultExternalSystemId;

    /**
     *
     * @param defaultExternalSystemId
     *            use this id as defaultExternalSystemId (see {@link #getDefaultExternalSystemId()
     *            and #getExternalSystemId()}
     */
    public AbstractExternalUserRepository(String defaultExternalSystemId) {
        if (defaultExternalSystemId == null) {
            throw new IllegalArgumentException("defaultExternalSystemId cannot be null!");
        }
        this.defaultExternalSystemId = defaultExternalSystemId;
    }

    /**
     * Get the external system identifier
     *
     * @return external system identifier or null
     */
    @Override
    public String getExternalSystemId() {
        String systemId = defaultExternalSystemId;
        if (isConfigurationSet()) {
            systemId = getConfiguration().getSystemId();
        }
        return systemId;
    }

    @Override
    public ExternalUserGroupAccessor getExternalUserGroupAccessor() {
        return externalUserGroupAccessor;
    }

    /**
     * {@inheritDoc}
     *
     * @return null
     */
    @Override
    public IncrementalRepositoryChangeTracker getIncrementalRepositoryChangeTracker(
            boolean doFullSynchronization) {
        return null;
    }

    @Override
    public int getOrder() {
        // TODO set the order over configuration
        return 0;
    }

    /**
     * Get the {@link ExternalUserVO} of the {@link UserIdentification}
     *
     * @param userIdentification
     *            {@link UserIdentification}
     * @return {@link ExternalUserVO}
     * @throws ExternalRepositoryException
     *             repository exception
     */
    @Override
    public ExternalUserVO getUser(UserIdentification userIdentification)
            throws ExternalRepositoryException {
        ExternalUserVO user = null;
        if (this.getExternalSystemId().equals(userIdentification.getExternalSystemId())) {
            user = userIdentification.getPreviousExternalUser();
        }
        if (user == null && userIdentification.getExternalUserId() != null) {
            user = getUser(userIdentification.getExternalUserId());
        }
        if (user == null && userIdentification.getUserAlias() != null) {
            user = getUser(userIdentification.getUserAlias());
        }
        return user;
    }

    /**
     * Check if configuration is set
     *
     * @return true or false
     */
    @Override
    public boolean isActive() {
        ExternalSystemConfiguration config = getConfiguration();
        return config != null && config.isAllowExternalAuthentication();
    }

    protected boolean isConfigurationSet() {
        return this.getConfiguration() != null;
    }

    /**
     * @return false
     */
    @Override
    public boolean isIncrementalSynchronizationAvailable() {
        return false;
    }

    @Override
    public boolean showInIntegrationOverview() {
        return true;
    }
}
