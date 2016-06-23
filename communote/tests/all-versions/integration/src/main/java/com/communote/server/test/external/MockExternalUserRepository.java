package com.communote.server.test.external;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.communote.common.i18n.LocalizedMessage;
import com.communote.server.api.core.property.StringPropertyTO;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.core.external.ExternalRepositoryException;
import com.communote.server.core.external.ExternalUserRepository;
import com.communote.server.core.external.IncrementalRepositoryChangeTracker;
import com.communote.server.core.security.UserIdentification;
import com.communote.server.model.config.ExternalSystemConfiguration;
import com.communote.server.model.user.UserProfileFields;
import com.communote.server.model.user.ExternalUserAuthentication;
import com.communote.server.persistence.user.ExternalUserVO;
import com.communote.server.plugins.api.externals.ExternalUserGroupAccessor;

/**
 * User repository for tests.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class MockExternalUserRepository implements ExternalUserRepository {

    /**
     * Mock, dummy configuration
     *
     * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
     *
     */
    public static class MockExternalSystemConfiguration extends ExternalSystemConfiguration {

        /**
         *
         */
        private static final long serialVersionUID = 1L;

        @Override
        public String getConfigurationUrl() {
            return null;
        }

        @Override
        public String getImageApiUrl() {
            return null;
        }

    }

    private final String externalSystemId;

    private MockExternalUserGroupAccessor userGroupAccessor;

    private final Map<String, ExternalUserVO> users;

    private MockExternalSystemConfiguration configuration;

    /**
     * Create a new user repository
     *
     * @param externalSystemId
     *            the ID of the external system of the repository
     * @param groupAccessor
     *            optional group accessor if the repository should be able to provide groups
     */
    public MockExternalUserRepository(String externalSystemId,
            MockExternalUserGroupAccessor groupAccessor) {
        this.externalSystemId = externalSystemId;
        this.userGroupAccessor = groupAccessor;
        this.users = new HashMap<String, ExternalUserVO>();
    }

    /**
     * Add a user the repository should be able to provide
     *
     * @param user
     *            the user to add
     */
    public void addUser(ExternalUserVO user) {
        users.put(user.getExternalUserName(), user);
    }

    @Override
    public MockExternalSystemConfiguration createConfiguration() {
        return new MockExternalSystemConfiguration();
    }

    @Override
    public MockExternalSystemConfiguration getConfiguration() {
        return configuration;
    }

    @Override
    public Collection<StringPropertyTO> getExternalLoginProperties(Long userId,
            ExternalUserAuthentication externalUserAuthentication) throws AuthorizationException {

        return Collections.emptySet();
    }

    @Override
    public String getExternalSystemId() {
        return externalSystemId;
    }

    @Override
    public ExternalUserGroupAccessor getExternalUserGroupAccessor() {
        return userGroupAccessor;
    }

    @Override
    public IncrementalRepositoryChangeTracker getIncrementalRepositoryChangeTracker(
            boolean doFullSynchronization) {
        return null;
    }

    @Override
    public LocalizedMessage getName() {
        return null;
    }

    @Override
    public int getOrder() {
        return 0;
    }

    @Override
    public Set<UserProfileFields> getProvidedProfileFieldNames() {
        return new HashSet<>();
    }

    @Override
    public ExternalUserVO getUser(String externalUserId) throws ExternalRepositoryException {
        return users.get(externalUserId);
    }

    @Override
    public ExternalUserVO getUser(UserIdentification userIdentification)
            throws ExternalRepositoryException {
        return users.get(userIdentification.getExternalUserId());
    }

    @Override
    public boolean isActive() {
        return true;
    }

    @Override
    public boolean isIncrementalSynchronizationAvailable() {
        return false;
    }

    /**
     * Remove a previously added user
     *
     * @param externalUserId
     *            the user to remove
     */
    public void removeUser(String externalUserId) {
        users.remove(externalUserId);
    }

    public void setConfiguration(MockExternalSystemConfiguration configuration) {
        this.configuration = configuration;
    }

    public void setExternalUserGroupAccessor(MockExternalUserGroupAccessor userGroupAccessor) {
        this.userGroupAccessor = userGroupAccessor;
    }

    @Override
    public boolean showInIntegrationOverview() {
        return true;
    }
}
