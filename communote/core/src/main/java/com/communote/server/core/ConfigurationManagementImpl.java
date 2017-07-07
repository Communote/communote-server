package com.communote.server.core;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.communote.common.image.ImageFormatType;
import com.communote.common.image.ImageScaler;
import com.communote.common.image.ImageSize;
import com.communote.common.string.StringHelper;
import com.communote.common.virusscan.VirusScanner;
import com.communote.common.virusscan.exception.InitializeException;
import com.communote.common.virusscan.exception.VirusFoundException;
import com.communote.common.virusscan.exception.VirusScannerException;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.config.ApplicationConfigurationPropertyConstant;
import com.communote.server.api.core.config.ClientConfigurationPropertyConstant;
import com.communote.server.api.core.config.type.ClientPropertySecurity;
import com.communote.server.api.core.image.ImageManager;
import com.communote.server.api.core.image.ImageVO;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.api.core.user.PrimaryAuthenticationException;
import com.communote.server.core.common.ldap.LdapUserAttribute;
import com.communote.server.core.image.type.ClientImageDescriptor;
import com.communote.server.core.security.SecurityHelper;
import com.communote.server.core.user.UserManagement;
import com.communote.server.core.user.client.ClientManagementException;
import com.communote.server.model.blog.Blog;
import com.communote.server.model.config.ApplicationConfigurationSetting;
import com.communote.server.model.config.ClientConfiguration;
import com.communote.server.model.config.Configuration;
import com.communote.server.model.config.ConfluenceConfiguration;
import com.communote.server.model.config.ExternalSystemConfiguration;
import com.communote.server.model.config.LdapConfiguration;
import com.communote.server.model.config.LdapGroupSyncConfiguration;
import com.communote.server.model.config.LdapSearchBaseDefinition;
import com.communote.server.model.config.LdapSearchConfiguration;
import com.communote.server.model.config.Setting;
import com.communote.server.model.user.ImageSizeType;
import com.communote.server.model.user.UserRole;
import com.communote.server.persistence.config.LdapSearchBaseDefinitionDao;
import com.communote.server.persistence.user.client.ClientHelper;

/**
 * The service class for configurations.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
// TODO check authorization!!
@Service("configurationManagement")
public class ConfigurationManagementImpl extends ConfigurationManagementBase {

    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationManagementImpl.class);

    private static final String SASL_MODE_CRAM_MD5 = "CRAM-MD5";

    private static final String SASL_MODE_DIGEST_MD5 = "DIGEST-MD5";

    @Autowired
    private UserManagement userManagement;
    @Autowired
    private LdapSearchBaseDefinitionDao ldapSearchBaseDefinitionDao;

    /**
     * asserts that the current user is client manager
     *
     * @throws AuthorizationException
     *             if the current user is not client manager
     */
    private void assertCurrentUserIsManager() throws AuthorizationException {
        if (!SecurityHelper.isClientManager() && !SecurityHelper.isInternalSystem()) {
            throw new AuthorizationException(
                    "Current user is not client manager or internal system user");
        }
    }

    /**
     * Assert that an external configuration identified by a system ID exists and return it
     *
     * @param externalSystemId
     *            the ID of the external system configuration to retrieve
     * @return the external system configuration
     * @throws PrimaryAuthenticationException
     *             in case the external configuration does not exist
     */
    private ExternalSystemConfiguration assertExternalSystemConfiguration(String externalSystemId)
            throws PrimaryAuthenticationException {
        Long id = getExternalSystemConfigurationDao().findBySystemId(externalSystemId);
        if (id == null) {
            throw new PrimaryAuthenticationException(
                    "There is no configuration for the external system with id.", externalSystemId,
                    PrimaryAuthenticationException.Reasons.EXTERNAL_SYSTEM_NOT_FOUND);
        }
        return getExternalSystemConfigurationDao().load(id);
    }

    /**
     * Returns the default blog.
     *
     * @return the default blog.
     */
    @Override
    public Blog getDefaultBlog() {
        ClientConfiguration clientConfiguration = handleGetConfiguration().getClientConfig();
        return clientConfiguration.getDefaultBlog();
    }

    /**
     * Sets the default blog.
     *
     * @param blog
     *            The new default blog.
     */
    @Override
    public void handleDefaultBlog(Blog blog) {
        ClientConfiguration clientConfiguration = handleGetConfiguration().getClientConfig();

        if (clientConfiguration != null) {
            clientConfiguration = getClientConfigurationDao().load(clientConfiguration.getId());
            clientConfiguration.setDefaultBlog(blog);
            getClientConfigurationDao().update(clientConfiguration);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Collection<ApplicationConfigurationSetting> handleGetApplicationConfigurationSettings() {
        return getApplicationConfigurationSettingDao().loadAll();
    }

    /**
     * {@inheritDoc}
     *
     * @see com.communote.server.core.ConfigurationManagementBase#handleGetClientLogo()
     */
    @Override
    protected ImageVO handleGetClientLogo() {
        Configuration configuration = handleGetConfiguration();
        ImageVO result = null;
        ClientConfiguration clientConfig = configuration.getClientConfig();
        if (clientConfig != null) {
            if (!Hibernate.isInitialized(clientConfig.getLogoImage())) {
                Hibernate.initialize(clientConfig.getLogoImage());
            }
            if (clientConfig.getLogoImage() != null) {
                result = new ImageVO();
                result.setImage(configuration.getClientConfig().getLogoImage());
                Timestamp date = configuration.getClientConfig().getLastLogoImageModificationDate();
                // use timestamp 0 if it is not yet set due to migration
                result.setLastModificationDate(date == null ? new Date(0) : date);
            }
        }
        return result;
    }

    /**
     * {@inheritDoc}
     *
     * @see com.communote.server.core.ConfigurationManagementBase#handleGetConfiguration()
     */
    @Override
    protected Configuration handleGetConfiguration() {
        Collection<Configuration> list = getConfigurationDao().loadAll();
        Configuration configuration = null;
        if (list == null || list.size() == 0) {
            configuration = Configuration.Factory.newInstance();
            ClientConfiguration clientConfiguration = ClientConfiguration.Factory.newInstance();
            configuration.setClientConfig(clientConfiguration);

            getClientConfigurationDao().create(clientConfiguration);
            configuration = getConfigurationDao().create(configuration);

        } else {
            configuration = list.iterator().next();
        }
        // initialize the default blog of the client
        Blog defaultBlog = configuration.getClientConfig().getDefaultBlog();
        if (defaultBlog != null && !Hibernate.isInitialized(defaultBlog)) {
            Hibernate.initialize(defaultBlog);
        }
        return configuration;
    }

    /**
     * {@inheritDoc}
     *
     * @see com.communote.server.core.ConfigurationManagementBase#handleRemoveClientLogo()
     */
    @Override
    protected void handleRemoveClientLogo() {
        Configuration configuration = handleGetConfiguration();
        ClientConfiguration clientConfiguration = configuration.getClientConfig();
        if (clientConfiguration != null && clientConfiguration.getLogoImage() != null) {
            clientConfiguration.setLogoImage(null);
            clientConfiguration
                    .setLastLogoImageModificationDate(new Timestamp(System.currentTimeMillis()));
            getClientConfigurationDao().update(clientConfiguration);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void handleUpdateApplicationSettings(
            Map<ApplicationConfigurationPropertyConstant, String> settings)
            throws AuthorizationException {
        boolean installed = CommunoteRuntime.getInstance().getConfigurationManager()
                .getStartupProperties().isInstallationDone();
        if (installed && !(ClientHelper.isCurrentClientGlobal()
                && (SecurityHelper.isClientManager() || SecurityHelper.isInternalSystem()))) {
            throw new AuthorizationException(
                    "The current user is not allowed to update the application properties");
        }
        for (ApplicationConfigurationPropertyConstant key : settings.keySet()) {
            updateApplicationConfigSetting(key, settings.get(key));
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see com.communote.server.core.ConfigurationManagementBase#handleUpdateClientLogo(byte[])
     */
    @Override
    protected void handleUpdateClientLogo(byte[] image) {
        ClientConfiguration clientConfiguration = handleGetConfiguration().getClientConfig();
        if (clientConfiguration == null) {
            throw new ClientManagementException(
                    "illegal database state, client configuration can not be null");
        }
        try {
            VirusScanner scanner = ServiceLocator.instance().getVirusScanner();
            if (scanner != null) {
                scanner.scan(image);
            } else {
                LOGGER.debug("No virus scan will be executed because the scanner is disabled");
            }
        } catch (InitializeException e) {
            throw new ConfigurationManagementException("Virus scanner not initialized", e);
        } catch (VirusScannerException e) {
            throw new ConfigurationManagementException("Unable to scan content", e);
        } catch (VirusFoundException e) {
            LOGGER.warn("Virus found uploading content. userId=" + SecurityHelper.getCurrentUserId()
                    + " " + e.getMessage());
            throw new ConfigurationManagementException("Virus was detected in byte array", e);
        }
        ImageFormatType format;
        try {
            String mimeType = Imaging.getImageInfo(image).getMimeType();
            format = ImageFormatType.fromMimeType(mimeType);
        } catch (IllegalArgumentException | ImageReadException | IOException e) {
            LOGGER.warn(e.getMessage());
            format = ImageFormatType.png;
        }
        // TODO why scaling here and not just storing the image in the uploaded size
        ImageSize size = ServiceLocator.findService(ImageManager.class)
                .getImageSize(ClientImageDescriptor.IMAGE_TYPE_NAME, ImageSizeType.LARGE);
        image = new ImageScaler(size, format).resizeImage(image);
        if (image == null) {
            LOGGER.warn("The user uploaded an illegal image. User: "
                    + SecurityHelper.getCurrentUserAlias());
            throw new IllegalArgumentException("Illegal image uploaded");
        }
        clientConfiguration.setLogoImage(image);
        clientConfiguration
                .setLastLogoImageModificationDate(new Timestamp(System.currentTimeMillis()));
        getClientConfigurationDao().update(clientConfiguration);
    }

    /**
     * {@inheritDoc}
     *
     * @see com.communote.server.core.ConfigurationManagementBase#handleUpdateSetting(String,
     *      String)
     */
    @Override
    protected void handleUpdateClientSetting(ClientConfigurationPropertyConstant key,
            String value) {
        // fail for key ALLOW_DB_AUTH because a change requires special handling
        if (key.equals(ClientPropertySecurity.ALLOW_DB_AUTH_ON_EXTERNAL)) {
            throw new IllegalArgumentException(
                    "The property ClientPropertySecurity.ALLOW_DB_AUTH_ON_EXTERNAL is not "
                            + " supported by this method use setPrimaryAuthentication instead");
        }
        updateClientConfigSetting(key, value);
    }

    /**
     * {@inheritDoc}
     *
     * @see com.communote.server.core.ConfigurationManagementBase#handleUpdateAllSettings(java.util.Map)
     */
    @Override
    protected void handleUpdateClientSettings(
            Map<ClientConfigurationPropertyConstant, String> settings) {
        for (ClientConfigurationPropertyConstant key : settings.keySet()) {
            handleUpdateClientSetting(key, settings.get(key));
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see com.communote.server.core.ConfigurationManagementBase#handleUpdateClientTimeZoneId(String)
     */
    @Override
    protected void handleUpdateClientTimeZoneId(String timeZoneId) {
        Configuration configuration = handleGetConfiguration();
        ClientConfiguration clientConfiguration = configuration.getClientConfig();
        if (clientConfiguration != null && (clientConfiguration.getTimeZoneId() == null
                || !clientConfiguration.getTimeZoneId().equals(timeZoneId))) {
            clientConfiguration.setTimeZoneId(timeZoneId);
            getClientConfigurationDao().update(clientConfiguration);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @throws PrimaryAuthenticationException
     *             Exception.
     *
     */
    @Override
    protected void handleUpdateConfluenceAuthConfig(ConfluenceConfiguration confluenceAuthConfig)
            throws AuthorizationException, PrimaryAuthenticationException {
        assertCurrentUserIsManager();
        confluenceAuthConfig.setSystemId(DEFAULT_CONFLUENCE_SYSTEM_ID);

        Long id = getExternalSystemConfigurationDao()
                .findBySystemId(confluenceAuthConfig.getSystemId());
        Configuration config = handleGetConfiguration();

        ConfluenceConfiguration existingConfig;
        if (id == null) {
            existingConfig = ConfluenceConfiguration.Factory.newInstance();
            existingConfig.setSystemId(confluenceAuthConfig.getSystemId());
        } else {
            existingConfig = getConfluenceConfigurationDao().load(id);
        }
        // common updates
        internalUpdateExternalSystemConfig(config, existingConfig, confluenceAuthConfig);
        // confluence data
        existingConfig.setAdminLogin(confluenceAuthConfig.getAdminLogin());
        existingConfig.setAdminPassword(confluenceAuthConfig.getAdminPassword());
        existingConfig.setAuthenticationApiUrl(confluenceAuthConfig.getAuthenticationApiUrl());
        existingConfig.setImageApiUrl(confluenceAuthConfig.getImageApiUrl());
        existingConfig.setBasePath(confluenceAuthConfig.getBasePath());
        existingConfig.setPermissionsUrl(confluenceAuthConfig.getPermissionsUrl());
        existingConfig.setServiceUrl(confluenceAuthConfig.getServiceUrl());
        if (id == null) {
            getConfluenceConfigurationDao().create(existingConfig);
            config.getExternalSystemConfigurations().add(existingConfig);
        }
    }

    /**
     * {@inheritDoc}
     *
     */
    @Override
    protected void handleUpdateLdapConfiguration(LdapConfiguration ldapConfig)
            throws AuthorizationException, PrimaryAuthenticationException {
        assertCurrentUserIsManager();
        // force systemId as long as we only have one
        ldapConfig.setSystemId(DEFAULT_LDAP_SYSTEM_ID);
        Configuration config = handleGetConfiguration();
        Long id = getExternalSystemConfigurationDao().findBySystemId(ldapConfig.getSystemId());

        LdapConfiguration existingConfig;
        if (id == null) {
            existingConfig = LdapConfiguration.Factory.newInstance();
            existingConfig.setSystemId(ldapConfig.getSystemId());
            existingConfig.setUserSearch(LdapSearchConfiguration.Factory.newInstance());
            config.getExternalSystemConfigurations().add(existingConfig);
        } else {
            existingConfig = getLdapConfigurationDao().load(id);
        }
        // common updates
        internalUpdateExternalSystemConfig(config, existingConfig, ldapConfig);
        // ldap specific updates
        existingConfig.setManagerDN(ldapConfig.getManagerDN());
        existingConfig.setManagerPassword(ldapConfig.getManagerPassword());
        existingConfig.setUrl(ldapConfig.getUrl());
        existingConfig.setDynamicMode(ldapConfig.isDynamicMode());
        existingConfig.setServerDomain(ldapConfig.getServerDomain());
        existingConfig.setQueryPrefix(ldapConfig.getQueryPrefix());
        updateLdapSaslMode(existingConfig, ldapConfig.getSaslMode());

        // check if the mapped attribute for user UID changed (mapping or isBinary flag)
        boolean permanentIdChanged = ldapConfig.isUserIdentifierIsBinary() != existingConfig
                .isUserIdentifierIsBinary();
        permanentIdChanged = permanentIdChanged
                || existingConfig.getUserSearch().getPropertyMapping() == null;

        if (!permanentIdChanged) {
            Map<String, String> newMapping = StringHelper
                    .getStringAsMap(ldapConfig.getUserSearch().getPropertyMapping());
            Map<String, String> oldMapping = StringHelper
                    .getStringAsMap(existingConfig.getUserSearch().getPropertyMapping());
            permanentIdChanged = !StringUtils.equals(
                    newMapping.get(LdapUserAttribute.UID.getName()),
                    oldMapping.get(LdapUserAttribute.UID.getName()));
        }
        existingConfig.setUserIdentifierIsBinary(ldapConfig.isUserIdentifierIsBinary());
        existingConfig.getUserSearch()
                .setSearchFilter(ldapConfig.getUserSearch().getSearchFilter());
        existingConfig.getUserSearch()
                .setPropertyMapping(ldapConfig.getUserSearch().getPropertyMapping());
        if (existingConfig.getId() == null) {
            existingConfig = getLdapConfigurationDao().create(existingConfig);
        }
        internalSyncSearchBases(existingConfig.getUserSearch(),
                ldapConfig.getUserSearch().getSearchBases());
        // only update group config if group sync is activated otherwise the input is not validated
        if (ldapConfig.isSynchronizeUserGroups()) {
            internalUpdateLdapGroupConfig(existingConfig, ldapConfig.getGroupSyncConfig());
        }

        if (permanentIdChanged) {
            userManagement.resetPermanentId(existingConfig.getSystemId());
        }
    }

    /**
     * Update the LDAP search bases
     *
     * @param existingConfig
     *            existing LDAP search configuration
     * @param newSearchBases
     *            the new search bases to set
     */
    private void internalSyncSearchBases(LdapSearchConfiguration existingConfig,
            List<LdapSearchBaseDefinition> newSearchBases) {
        List<LdapSearchBaseDefinition> existingSearchBases = existingConfig.getSearchBases();
        if (existingSearchBases == null) {
            existingSearchBases = new ArrayList<LdapSearchBaseDefinition>();
            existingConfig.setSearchBases(existingSearchBases);
        }
        if (newSearchBases == null || newSearchBases.size() == 0) {
            existingConfig.getSearchBases().clear();
        } else {
            int i;
            for (i = 0; i < newSearchBases.size(); i++) {
                if (existingSearchBases.size() == i) {
                    existingSearchBases.add(LdapSearchBaseDefinition.Factory.newInstance(
                            newSearchBases.get(i).getSearchBase(),
                            newSearchBases.get(i).isSearchSubtree()));
                } else {
                    existingSearchBases.get(i).setSearchBase(newSearchBases.get(i).getSearchBase());
                    existingSearchBases.get(i)
                            .setSearchSubtree(newSearchBases.get(i).isSearchSubtree());
                }
            }
            // remove other
            for (; i < existingSearchBases.size(); i++) {
                LdapSearchBaseDefinition searchBaseDef = existingSearchBases.remove(i);
                ldapSearchBaseDefinitionDao.remove(searchBaseDef);
            }
        }
    }

    /**
     * Updates the members of the external system configuration
     *
     * @param config
     *            the client configuration
     * @param existingConfig
     *            the configuration to update
     * @param newConfig
     *            the new configuration
     * @throws PrimaryAuthenticationException
     *             Exception.
     */
    private void internalUpdateExternalSystemConfig(Configuration config,
            ExternalSystemConfiguration existingConfig, ExternalSystemConfiguration newConfig)
            throws PrimaryAuthenticationException {
        if (!newConfig.isAllowExternalAuthentication()
                && existingConfig.isAllowExternalAuthentication()
                && existingConfig.isPrimaryAuthentication()) {
            // because the external auth is disabled it cannot be primary anymore
            // Note: no need to check whether there are enough admins because the current user is
            // one (and can request a password after the primary auth is disabled)
            existingConfig.setPrimaryAuthentication(false);
        }
        existingConfig.setAllowExternalAuthentication(newConfig.isAllowExternalAuthentication());
        existingConfig.setSynchronizeUserGroups(newConfig.isSynchronizeUserGroups());
    }

    /**
     * updates the LDAP group sync configuration
     *
     * @param existingConfig
     *            the existing LDAP configuration
     * @param newGroupConfig
     *            the new group sync configuration
     */
    private void internalUpdateLdapGroupConfig(LdapConfiguration existingConfig,
            LdapGroupSyncConfiguration newGroupConfig) {
        if (newGroupConfig == null) {
            // should cascade all associated objects
            existingConfig.setGroupSyncConfig(null);
            return;
        }
        LdapGroupSyncConfiguration existingGroupConfig = existingConfig.getGroupSyncConfig();
        if (existingGroupConfig == null) {
            existingGroupConfig = LdapGroupSyncConfiguration.Factory.newInstance();
            existingGroupConfig.setGroupSearch(LdapSearchConfiguration.Factory.newInstance());
            existingConfig.setGroupSyncConfig(existingGroupConfig);
        }
        existingGroupConfig.setGroupIdentifierIsBinary(newGroupConfig.isGroupIdentifierIsBinary());
        existingGroupConfig.setMemberMode(newGroupConfig.isMemberMode());
        existingGroupConfig.getGroupSearch()
                .setSearchFilter(newGroupConfig.getGroupSearch().getSearchFilter());
        existingGroupConfig.getGroupSearch()
                .setPropertyMapping(newGroupConfig.getGroupSearch().getPropertyMapping());
        internalSyncSearchBases(existingGroupConfig.getGroupSearch(),
                newGroupConfig.getGroupSearch().getSearchBases());
    }

    /**
     * {@inheritDoc}
     *
     */
    @Override
    public void setPrimaryAuthentication(String externalSystemId, boolean allowDBAuth,
            String currentPrimaryAuthentication) throws PrimaryAuthenticationException {
        ExternalSystemConfiguration externalSystemConfiguration = null;
        ExternalSystemConfiguration currentPrimaryExternalSystemConfiguration = null;
        if (StringUtils.isNotBlank(externalSystemId)) {
            externalSystemConfiguration = assertExternalSystemConfiguration(externalSystemId);
            // assert that there are enough admins if DB fallback is deactivated
            if (!allowDBAuth) {
                if (userManagement.getActiveUserCount(externalSystemId,
                        UserRole.ROLE_KENMEI_CLIENT_MANAGER) == 0) {
                    throw new PrimaryAuthenticationException(
                            "There must be at least one active admin for the external system.",
                            externalSystemId,
                            PrimaryAuthenticationException.Reasons.NOT_ENOUGH_ADMINS);
                }
            }
        }
        if (StringUtils.isNotBlank(currentPrimaryAuthentication)) {
            if (currentPrimaryAuthentication.equals(externalSystemId)) {
                currentPrimaryExternalSystemConfiguration = externalSystemConfiguration;
            } else {
                currentPrimaryExternalSystemConfiguration = assertExternalSystemConfiguration(
                        currentPrimaryAuthentication);
            }
        }
        // check if the primary system changed
        if (!ObjectUtils.equals(externalSystemConfiguration,
                currentPrimaryExternalSystemConfiguration)) {
            // when setting another primary system it must be active
            if (externalSystemConfiguration != null) {
                if (!externalSystemConfiguration.isAllowExternalAuthentication()) {
                    throw new PrimaryAuthenticationException(
                            "External authentication for the given system is not activated.",
                            externalSystemId,
                            PrimaryAuthenticationException.Reasons.EXTERNAL_AUTH_NOT_ALLOWED);
                }
                externalSystemConfiguration.setPrimaryAuthentication(true);
            }
            // deactivate current primary authentication
            if (currentPrimaryExternalSystemConfiguration != null) {
                currentPrimaryExternalSystemConfiguration.setPrimaryAuthentication(false);
            }
        }

        updateClientConfigSetting(ClientPropertySecurity.ALLOW_DB_AUTH_ON_EXTERNAL,
                Boolean.toString(allowDBAuth));
    }

    /**
     * Updates a single application configuration setting in the database. If the value is null the
     * entry will be removed if it exists.
     *
     * @param key
     *            the key
     * @param value
     *            the value
     */
    private void updateApplicationConfigSetting(ApplicationConfigurationPropertyConstant key,
            String value) {
        String keyString = key.getKeyString();

        ApplicationConfigurationSetting setting = getApplicationConfigurationSettingDao()
                .load(keyString);
        if (setting == null) {
            if (value != null) {
                setting = ApplicationConfigurationSetting.Factory.newInstance();
                setting.setSettingKey(keyString);
                setting.setSettingValue(value);
                setting = getApplicationConfigurationSettingDao().create(setting);
            }
        } else {
            if (value == null) {
                getApplicationConfigurationSettingDao().remove(setting);
            } else {
                setting.setSettingValue(value);
            }
        }
    }

    /**
     * Updates or creates a single config setting in the database. In case the value is null the
     * setting is removed.
     *
     * @param key
     *            the key
     * @param value
     *            the value
     */
    private void updateClientConfigSetting(ClientConfigurationPropertyConstant key, String value) {
        String keyString = key.getKeyString();
        Setting setting = getSettingDao().load(keyString);
        if (setting == null) {
            Configuration configuration = getConfiguration();
            setting = Setting.Factory.newInstance();
            setting.setSettingKey(keyString);
            setting.setSettingValue(value);
            setting.setLastModificationTimestamp(System.currentTimeMillis());
            setting = getSettingDao().create(setting);
            configuration.getSettings().add(setting);
        } else {
            if (value == null) {
                getSettingDao().remove(setting);
            } else {
                setting.setSettingValue(value);
                setting.setLastModificationTimestamp(System.currentTimeMillis());
            }
        }
    }

    /**
     * Updates the SASL mode of the LDAP authentication and checks whether the new value is
     * supported.
     *
     * @param config
     *            the configuration to update
     * @param newMode
     *            the new SASL mode to set. Can be null for simple authentication.
     */
    private void updateLdapSaslMode(LdapConfiguration config, String newMode) {
        if (newMode != null && !SASL_MODE_CRAM_MD5.equals(newMode)
                && !SASL_MODE_DIGEST_MD5.equals(newMode)) {
            LOGGER.warn("SASL mode " + newMode
                    + " is not supported. Falling back to simple authentication.");
            newMode = null;
        }
        config.setSaslMode(newMode);
    }
}
