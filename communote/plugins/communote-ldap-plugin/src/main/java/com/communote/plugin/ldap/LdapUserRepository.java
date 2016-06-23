package com.communote.plugin.ldap;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.communote.common.i18n.LocalizedMessage;
import com.communote.plugins.core.services.PluginPropertyService;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.common.NotFoundException;
import com.communote.server.api.core.property.PropertyManagement;
import com.communote.server.api.core.property.PropertyType;
import com.communote.server.api.core.property.StringPropertyTO;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.core.ConfigurationManagement;
import com.communote.server.core.common.ldap.CommunoteLdapUserSearch;
import com.communote.server.core.common.ldap.LdapAttributeMappingException;
import com.communote.server.core.common.ldap.LdapUserAttribute;
import com.communote.server.core.common.ldap.LdapUserAttributesMapper;
import com.communote.server.core.external.AbstractExternalUserRepository;
import com.communote.server.core.external.ExternalRepositoryException;
import com.communote.server.core.external.ExternalUserRepository;
import com.communote.server.core.external.IncrementalRepositoryChangeTracker;
import com.communote.server.core.security.SecurityHelper;
import com.communote.server.core.user.UserGroupManagement;
import com.communote.server.core.user.UserManagement;
import com.communote.server.model.config.LdapConfiguration;
import com.communote.server.model.property.StringProperty;
import com.communote.server.model.user.ExternalUserAuthentication;
import com.communote.server.model.user.UserProfileFields;
import com.communote.server.persistence.common.messages.MessageKeyLocalizedMessage;
import com.communote.server.persistence.user.ExternalUserVO;
import com.communote.server.persistence.user.group.ExternalUserGroupDao;
import com.communote.server.plugins.api.externals.ExternalUserGroupAccessor;

/**
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
@Component(immediate = true)
@Instantiate
@Provides(specifications = ExternalUserRepository.class)
public class LdapUserRepository extends AbstractExternalUserRepository implements PropertyKeys {
    /**
     * Default Ldap it.
     */
    public static final String EXTERNAL_SYSTEM_ID_DEFAULT_LDAP = ConfigurationManagement.DEFAULT_LDAP_SYSTEM_ID;
    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(LdapUserRepository.class);

    @Requires
    private PluginPropertyService pluginProperties;

    private PropertyManagement propertyManagement;

    private final BundleContext bundleContext;

    /**
     * Default constructor setting the default system id
     */
    public LdapUserRepository(BundleContext bundleContext) {
        super(LdapActivator.EXTERNAL_SYSTEM_ID_DEFAULT_LDAP);
        this.bundleContext = bundleContext;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LdapConfiguration createConfiguration() {
        return LdapConfiguration.Factory.newInstance();
    }

    /**
     * Get the ldap configuration of the client
     *
     * @return {@link LdapConfiguration}
     */
    @Override
    public LdapConfiguration getConfiguration() {
        return CommunoteRuntime.getInstance().getConfigurationManager()
                .getClientConfigurationProperties().getLdapConfiguration();
    }

    @Override
    public Collection<StringPropertyTO> getExternalLoginProperties(Long userId,
            ExternalUserAuthentication externalUserAuthentication) throws AuthorizationException {

        if (!SecurityHelper.isCurrentUserId(userId)) {
            if (!SecurityHelper.isInternalSystem()) {
                throw new AuthorizationException(
                        "Only the external logins of the current user can be accessed");
            }
        }

        Collection<StringPropertyTO> props = new HashSet<StringPropertyTO>();

        if (StringUtils.trimToNull(externalUserAuthentication.getAdditionalProperty()) != null) {
            StringPropertyTO prop = new StringPropertyTO();
            prop.setKeyGroup(bundleContext.getBundle().getSymbolicName());
            prop.setPropertyKey("dn");
            prop.setPropertyValue(externalUserAuthentication.getAdditionalProperty());
            props.add(prop);
        }

        String upn = this.getLdapUserPropertyFailsafe(userId, LdapUserAttribute.UPN);
        if (StringUtils.trimToNull(upn) != null) {
            StringPropertyTO prop = new StringPropertyTO();
            prop.setKeyGroup(bundleContext.getBundle().getSymbolicName());
            prop.setPropertyKey(LdapActivator.EXTERNAL_SYSTEM_ID_DEFAULT_LDAP);
            prop.setPropertyValue(upn);
            props.add(prop);
        }

        return props;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ExternalUserGroupAccessor getExternalUserGroupAccessor() {
        LdapConfiguration configuration = getConfiguration();
        if (configuration != null && configuration.isSynchronizeUserGroups()) {
            LdapUserGroupAccessor ldapUserGroupAccessor = new LdapUserGroupAccessor(
                    pluginProperties);
            if (ldapUserGroupAccessor.supports(configuration)) {
                return ldapUserGroupAccessor;
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IncrementalRepositoryChangeTracker getIncrementalRepositoryChangeTracker(
            boolean doFullSynchronization) {
        String isIncrementalEnabled = pluginProperties
                .getClientProperty(PROPERTY_KEY_ACTIVE_DIRECTORY_TRACKING_ENABLED_GROUP);
        if (!Boolean.parseBoolean(isIncrementalEnabled)) {
            return null;
        }
        ADTrackingIncrementalRepositoryChangeTracker tracker = new ADTrackingIncrementalRepositoryChangeTracker(
                getConfiguration(), pluginProperties, ServiceLocator.instance().getService(
                        UserManagement.class),
                        ServiceLocator.findService(ExternalUserGroupDao.class), doFullSynchronization);
        tracker.setUserGroupManagement(ServiceLocator.instance().getService(
                UserGroupManagement.class));
        return tracker;
    }

    /**
     * Gets the user principal name property.
     *
     * @param userId
     *            the user id
     * @return the user principal name. Null if not found.
     * @throws NotFoundException
     *             no user found for the given id
     */
    private String getLdapUserPropertyFailsafe(Long userId, LdapUserAttribute ldapUserAttribute) {

        StringProperty userPrincipalNameProperty;
        String userPrincipalName = null;

        try {
            userPrincipalNameProperty = propertyManagement.getObjectProperty(
                    PropertyType.UserProperty, userId, this.bundleContext.getBundle()
                            .getSymbolicName(), LdapUserAttributesMapper.getUserPropertyKeyName(
                            LdapActivator.EXTERNAL_SYSTEM_ID_DEFAULT_LDAP, ldapUserAttribute));

            if (userPrincipalNameProperty != null) {
                userPrincipalName = userPrincipalNameProperty.getPropertyValue();
            }
        } catch (AuthorizationException e) {
            LOGGER.debug("Error accessing user property. Probably UserPrinicpialName Property and LDAP Plugin not activated. Will ignore exception: "
                    + e.getMessage());
        } catch (NotFoundException e) {
            LOGGER.debug("Error accessing user property. UserPrinicpialName Property not found. userId: "
                    + userId + " " + e.getMessage());
        }

        return userPrincipalName;
    }

    @Override
    public LocalizedMessage getName() {
        return new MessageKeyLocalizedMessage("client.integration.types."
                + LdapUserRepository.EXTERNAL_SYSTEM_ID_DEFAULT_LDAP);
    }

    @Override
    public Set<UserProfileFields> getProvidedProfileFieldNames() {
        // TODO actually we should ask the LDAPUserAttributeMapper for the required fields and
        // extract the profile related fields
        HashSet<UserProfileFields> fields = new HashSet<>();
        fields.add(UserProfileFields.FIRSTNAME);
        fields.add(UserProfileFields.LASTNAME);
        return fields;
    }

    /**
     * Get external user from ldap repository
     *
     * @param externalUserId
     *            external user identifier conform with the alias in ldap
     * @return {@link ExternalUserVO}
     * @throws ExternalRepositoryException
     *             external repository exception
     */
    @Override
    public ExternalUserVO getUser(String externalUserId) throws ExternalRepositoryException {
        if (getConfiguration() == null) {
            throw new ExternalRepositoryException("ldap configuration is null");
        }

        try {
            CommunoteLdapUserSearch userSearch = new CommunoteLdapUserSearch(getConfiguration(),
                    LdapUserAttribute.ALIAS);
            return userSearch.searchForUserTransformed(externalUserId);
        } catch (LdapAttributeMappingException e) {
            LOGGER.error("There was an error mapping the LDAP user with externalUserId="
                    + externalUserId, e);
            throw new ExternalRepositoryException(e.getMessage(), e);
        }

    }

    /**
     * @return {@link IncrementalRepositoryChangeTrackerFactory} != null
     */
    @Override
    public boolean isIncrementalSynchronizationAvailable() {
        return true;
    }

    @Validate
    public void start() {
        this.propertyManagement = ServiceLocator.findService(PropertyManagement.class);
    }

    public void stop() {
        this.propertyManagement = null;
    }
}
