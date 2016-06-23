package com.communote.server.core.external;

import java.util.Collection;
import java.util.Set;

import com.communote.common.i18n.LocalizedMessage;
import com.communote.common.util.Orderable;
import com.communote.server.api.core.property.StringPropertyTO;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.core.security.UserIdentification;
import com.communote.server.model.config.ExternalSystemConfiguration;
import com.communote.server.model.user.UserProfileFields;
import com.communote.server.model.user.ExternalUserAuthentication;
import com.communote.server.persistence.user.ExternalUserVO;
import com.communote.server.plugins.api.externals.ExternalUserGroupAccessor;

/**
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public interface ExternalUserRepository extends Orderable {

    /**
     * Create an empty configuration. E.g. to access the configuration url and name if no
     * configuration exists.
     *
     * @return the empty configuration
     */
    public ExternalSystemConfiguration createConfiguration();

    /**
     * Gets the external system configuration
     *
     * @return ExternalSystemConfiguration set, is null if not configured
     */
    public ExternalSystemConfiguration getConfiguration();

    /**
     * External Repos can return additional properties that will be included e.g. in the rest
     * resource
     *
     * @param userId
     *            the id of the user the authentication is for
     * @param externalUserAuthentication
     *            the authentication to get additional properties for
     * @return
     * @throws AuthorizationException
     */
    public Collection<StringPropertyTO> getExternalLoginProperties(Long userId,
            ExternalUserAuthentication externalUserAuthentication) throws AuthorizationException;

    /**
     * Get the external system identifier.
     *
     * @return external system identifier
     */
    public String getExternalSystemId();

    /**
     * Get the external user group accessor if user repository is also an group repository
     *
     * @return the external user group accessor or null if repository is not an group repository.
     */
    public ExternalUserGroupAccessor getExternalUserGroupAccessor();

    /**
     * Method to get an tracker for changes within an external repository. This method should only
     * return an object, if the incremental changes are possible and enabled.
     *
     * @param doFullSynchronization
     *            If set to true, a full synchronization should be done.
     * @return A tracker, if incremental synchronization is possible and enabled, else false.
     */
    public IncrementalRepositoryChangeTracker getIncrementalRepositoryChangeTracker(
            boolean doFullSynchronization);

    /**
     * @return a user friendly name of the repository
     */
    public LocalizedMessage getName();

    /**
     * @return the names of the user profile fields this user repository provides for all its users.
     *         If no fields are provided an empty set has to be returned.
     */
    public Set<UserProfileFields> getProvidedProfileFieldNames();

    /**
     * Get the {@link ExternalUserVO} of the external user identifier
     *
     * @param externalUserId
     *            external user identifier
     * @return {@link ExternalUserVO}
     * @throws ExternalRepositoryException
     *             repository exception
     */
    public ExternalUserVO getUser(String externalUserId) throws ExternalRepositoryException;

    /**
     * Get the {@link ExternalUserVO} for the {@link UserIdentification}. The
     * {@link UserIdentification} provides information about internal id, external id and so on. The
     * external user repo implementation of this method must considered the following:<br>
     * the internal id or alias have not been evaluated so far if the external system id is set or a
     * primary id is given the external system id of the user identification does not have to match
     * the system id of this repo, e.g. if a primary repo is given
     *
     * @param userIdentification
     *            {@link UserIdentification}
     * @return {@link ExternalUserVO}
     * @throws ExternalRepositoryException
     *             repository exception
     */
    public ExternalUserVO getUser(UserIdentification userIdentification)
            throws ExternalRepositoryException;

    /**
     * Check if external repo is active and can be used
     *
     * @return true or false
     */
    public boolean isActive();

    /**
     * @return True, if incremental synchronization is available, else false.
     */
    public boolean isIncrementalSynchronizationAvailable();

    /**
     *
     * @return True to show the repo in the Integration overview.
     */
    public boolean showInIntegrationOverview();
}