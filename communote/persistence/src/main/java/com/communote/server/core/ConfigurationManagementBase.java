package com.communote.server.core;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.communote.server.api.core.config.ApplicationConfigurationPropertyConstant;
import com.communote.server.api.core.config.ClientConfigurationPropertyConstant;
import com.communote.server.api.core.user.PrimaryAuthenticationException;
import com.communote.server.model.blog.Blog;
import com.communote.server.model.config.ApplicationConfigurationSetting;
import com.communote.server.persistence.config.ApplicationConfigurationSettingDao;
import com.communote.server.persistence.config.ClientConfigurationDao;
import com.communote.server.persistence.config.ConfigurationDao;
import com.communote.server.persistence.config.ConfluenceConfigurationDao;
import com.communote.server.persistence.config.ExternalSystemConfigurationDao;
import com.communote.server.persistence.config.LdapConfigurationDao;
import com.communote.server.persistence.config.SettingDao;

/**
 * <p>
 * Spring Service base class for <code>com.communote.server.service.ConfigurationManagement</code>,
 * provides access to all services and entities referenced by this service.
 * </p>
 *
 * @see com.communote.server.core.ConfigurationManagement
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
@Transactional(propagation = Propagation.REQUIRED)
public abstract class ConfigurationManagementBase implements ConfigurationManagement {

    @Autowired
    private ConfigurationDao configurationDao;
    @Autowired
    private SettingDao settingDao;

    @Autowired
    private LdapConfigurationDao ldapConfigurationDao;

    @Autowired
    private ClientConfigurationDao clientConfigurationDao;

    @Autowired
    private ExternalSystemConfigurationDao externalSystemConfigurationDao;

    @Autowired
    private ConfluenceConfigurationDao confluenceConfigurationDao;

    @Autowired
    private ApplicationConfigurationSettingDao applicationConfigurationSettingDao;

    /**
     * @return the applicationConfigurationSettingDao
     */
    public ApplicationConfigurationSettingDao getApplicationConfigurationSettingDao() {
        return applicationConfigurationSettingDao;
    }

    /**
     * @return A collection of all settings for the application.
     */
    @Override
    @Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
    public java.util.Collection<ApplicationConfigurationSetting> getApplicationConfigurationSettings() {
        try {
            return this.handleGetApplicationConfigurationSettings();
        } catch (RuntimeException rt) {
            throw new com.communote.server.core.ConfigurationManagementException(
                    "Error performing 'ConfigurationManagement.getApplicationConfigurationSettings()' --> "
                            + rt, rt);
        }
    }

    /**
     * @return the clientConfigurationDao
     */
    public ClientConfigurationDao getClientConfigurationDao() {
        return clientConfigurationDao;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public com.communote.server.api.core.image.ImageVO getClientLogo() {
        try {
            return this.handleGetClientLogo();
        } catch (RuntimeException rt) {
            throw new com.communote.server.core.ConfigurationManagementException(
                    "Error performing 'ConfigurationManagement.getClientLogo()' --> " + rt, rt);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public com.communote.server.model.config.Configuration getConfiguration() {
        try {
            return this.handleGetConfiguration();
        } catch (RuntimeException rt) {
            throw new com.communote.server.core.ConfigurationManagementException(
                    "Error performing 'ConfigurationManagement.getConfiguration()' --> " + rt, rt);
        }
    }

    /**
     * @return the configurationDao
     */
    public ConfigurationDao getConfigurationDao() {
        return configurationDao;
    }

    /**
     * @return the confluenceConfigurationDao
     */
    public ConfluenceConfigurationDao getConfluenceConfigurationDao() {
        return confluenceConfigurationDao;
    }

    /**
     * @return the externalSystemConfigurationDao
     */
    public ExternalSystemConfigurationDao getExternalSystemConfigurationDao() {
        return externalSystemConfigurationDao;
    }

    /**
     * @return the ldapConfigurationDao
     */
    public LdapConfigurationDao getLdapConfigurationDao() {
        return ldapConfigurationDao;
    }

    /**
     * Gets the current <code>principal</code> if one has been set, otherwise returns
     * <code>null</code>.
     *
     * @return the current principal
     */
    protected java.security.Principal getPrincipal() {
        return com.communote.server.PrincipalStore.get();
    }

    /**
     * @return the settingDao
     */
    public SettingDao getSettingDao() {
        return settingDao;
    }

    /**
     * Performs the core logic for
     * {@link #updateDefaultBlog(com.communote.server.model.config.ConfluenceConfiguration)}
     *
     */
    protected abstract void handleDefaultBlog(Blog blog);

    /**
     * Performs the core logic for {@link #getApplicationConfigurationSettings()}
     */
    protected abstract java.util.Collection<ApplicationConfigurationSetting> handleGetApplicationConfigurationSettings();

    /**
     * Performs the core logic for {@link #getClientLogo()}
     */
    protected abstract com.communote.server.api.core.image.ImageVO handleGetClientLogo();

    /**
     * Performs the core logic for {@link #getConfiguration()}
     */
    protected abstract com.communote.server.model.config.Configuration handleGetConfiguration();

    /**
     * Performs the core logic for {@link #removeClientLogo()}
     */
    protected abstract void handleRemoveClientLogo();

    /**
     * Performs the core logic for {@link #updateApplicationSettings(java.util.Map<
     * ApplicationConfigurationPropertyConstant, String>)}
     */
    protected abstract void handleUpdateApplicationSettings(
            java.util.Map<ApplicationConfigurationPropertyConstant, String> settings)
                    throws com.communote.server.api.core.security.AuthorizationException;

    /**
     * Performs the core logic for {@link #updateClientLogo(byte[])}
     */
    protected abstract void handleUpdateClientLogo(byte[] image);

    /**
     * Performs the core logic for
     * {@link #updateClientSetting(com.communote.server.api.core.config.ClientConfigurationPropertyConstant, String)}
     */
    protected abstract void handleUpdateClientSetting(
            com.communote.server.api.core.config.ClientConfigurationPropertyConstant key,
            String value);

    /**
     * Performs the core logic for {@link #updateClientSettings(java.util.Map<
     * ClientConfigurationPropertyConstant,String>)}
     */
    protected abstract void handleUpdateClientSettings(
            java.util.Map<ClientConfigurationPropertyConstant, String> settings);

    /**
     * Performs the core logic for {@link #updateClientTimeZoneId(String)}
     */
    protected abstract void handleUpdateClientTimeZoneId(String timeZoneId);

    /**
     * Performs the core logic for
     * {@link #updateConfluenceAuthConfig(com.communote.server.model.config.ConfluenceConfiguration)}
     *
     * @throws PrimaryAuthenticationException
     */
    protected abstract void handleUpdateConfluenceAuthConfig(
            com.communote.server.model.config.ConfluenceConfiguration confluenceAuthConf)
                    throws com.communote.server.api.core.security.AuthorizationException,
                    PrimaryAuthenticationException;

    /**
     * Performs the core logic for
     * {@link #updateLdapConfiguration(com.communote.server.model.config.LdapConfiguration)}
     *
     * @throws PrimaryAuthenticationException
     */
    protected abstract void handleUpdateLdapConfiguration(
            com.communote.server.model.config.LdapConfiguration ldapConfig)
                    throws com.communote.server.api.core.security.AuthorizationException,
                    PrimaryAuthenticationException;

    /**
     * @see com.communote.server.core.ConfigurationManagement#removeClientLogo()
     */
    @Override
    public void removeClientLogo() {
        try {
            this.handleRemoveClientLogo();
        } catch (RuntimeException rt) {
            throw new com.communote.server.core.ConfigurationManagementException(
                    "Error performing 'ConfigurationManagement.removeClientLogo()' --> " + rt, rt);
        }
    }

    /**
     * @param applicationConfigurationSettingDao
     *            the applicationConfigurationSettingDao to set
     */
    public void setApplicationConfigurationSettingDao(
            ApplicationConfigurationSettingDao applicationConfigurationSettingDao) {
        this.applicationConfigurationSettingDao = applicationConfigurationSettingDao;
    }

    /**
     * @param clientConfigurationDao
     *            the clientConfigurationDao to set
     */
    public void setClientConfigurationDao(ClientConfigurationDao clientConfigurationDao) {
        this.clientConfigurationDao = clientConfigurationDao;
    }

    /**
     * @param configurationDao
     *            the configurationDao to set
     */
    public void setConfigurationDao(ConfigurationDao configurationDao) {
        this.configurationDao = configurationDao;
    }

    /**
     * @param confluenceConfigurationDao
     *            the confluenceConfigurationDao to set
     */
    public void setConfluenceConfigurationDao(ConfluenceConfigurationDao confluenceConfigurationDao) {
        this.confluenceConfigurationDao = confluenceConfigurationDao;
    }

    /**
     * @param externalSystemConfigurationDao
     *            the externalSystemConfigurationDao to set
     */
    public void setExternalSystemConfigurationDao(
            ExternalSystemConfigurationDao externalSystemConfigurationDao) {
        this.externalSystemConfigurationDao = externalSystemConfigurationDao;
    }

    /**
     * @param ldapConfigurationDao
     *            the ldapConfigurationDao to set
     */
    public void setLdapConfigurationDao(LdapConfigurationDao ldapConfigurationDao) {
        this.ldapConfigurationDao = ldapConfigurationDao;
    }

    /**
     * @param settingDao
     *            the settingDao to set
     */
    public void setSettingDao(SettingDao settingDao) {
        this.settingDao = settingDao;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateApplicationSettings(
            java.util.Map<ApplicationConfigurationPropertyConstant, String> settings)
                    throws com.communote.server.api.core.security.AuthorizationException {
        if (settings == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.service.ConfigurationManagement.updateApplicationSettings(java.util.Map<ApplicationConfigurationPropertyConstant, String> settings) - 'settings' can not be null");
        }
        try {
            this.handleUpdateApplicationSettings(settings);
        } catch (RuntimeException rt) {
            throw new com.communote.server.core.ConfigurationManagementException(
                    "Error performing 'ConfigurationManagement.updateApplicationSettings(java.util.Map<ApplicationConfigurationPropertyConstant, String> settings)' --> "
                            + rt, rt);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateClientLogo(byte[] image) {
        if (image == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.service.ConfigurationManagement.updateClientLogo(byte[] image) - 'image' can not be null");
        }
        try {
            this.handleUpdateClientLogo(image);
        } catch (RuntimeException rt) {
            throw new com.communote.server.core.ConfigurationManagementException(
                    "Error performing 'ConfigurationManagement.updateClientLogo(byte[] image)' --> "
                            + rt, rt);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateClientSetting(
            com.communote.server.api.core.config.ClientConfigurationPropertyConstant key,
            String value) {
        if (key == null) {
            throw new IllegalArgumentException(
                    "ConfigurationManagement.updateClientSetting(ClientConfigurationPropertyConstant"
                            + " key,String value) - 'key' can not be null");
        }
        try {
            this.handleUpdateClientSetting(key, value);
        } catch (RuntimeException rt) {
            throw new com.communote.server.core.ConfigurationManagementException(
                    "Error performing 'ConfigurationManagement.updateClientSetting("
                            + "ClientConfigurationPropertyConstant key, String value)' --> " + rt,
                            rt);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateClientSettings(
            java.util.Map<ClientConfigurationPropertyConstant, String> settings) {
        if (settings == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.service.ConfigurationManagement.updateClientSettings(java.util.Map<ClientConfigurationPropertyConstant,String> settings) - 'settings' can not be null");
        }
        try {
            this.handleUpdateClientSettings(settings);
        } catch (RuntimeException rt) {
            throw new com.communote.server.core.ConfigurationManagementException(
                    "Error performing 'ConfigurationManagement.updateClientSettings(java.util.Map<ClientConfigurationPropertyConstant,String> settings)' --> "
                            + rt, rt);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateClientTimeZoneId(String timeZoneId) {
        if (timeZoneId == null || timeZoneId.trim().length() == 0) {
            throw new IllegalArgumentException(
                    "com.communote.server.service.ConfigurationManagement.updateClientTimeZoneId(String timeZoneId) - 'timeZoneId' can not be null or empty");
        }
        try {
            this.handleUpdateClientTimeZoneId(timeZoneId);
        } catch (RuntimeException rt) {
            throw new com.communote.server.core.ConfigurationManagementException(
                    "Error performing 'ConfigurationManagement.updateClientTimeZoneId(String timeZoneId)' --> "
                            + rt, rt);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateConfluenceAuthConfig(
            com.communote.server.model.config.ConfluenceConfiguration confluenceAuthConf)
                    throws com.communote.server.api.core.security.AuthorizationException,
                    PrimaryAuthenticationException {
        if (confluenceAuthConf == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.service.ConfigurationManagement.updateConfluenceAuthConfig(com.communote.server.persistence.config.ConfluenceConfiguration confluenceAuthConf) - 'confluenceAuthConf' can not be null");
        }
        try {
            this.handleUpdateConfluenceAuthConfig(confluenceAuthConf);
        } catch (RuntimeException rt) {
            throw new com.communote.server.core.ConfigurationManagementException(
                    "Error performing 'ConfigurationManagement.updateConfluenceAuthConfig(com.communote.server.persistence.config.ConfluenceConfiguration confluenceAuthConf)' --> "
                            + rt, rt);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateDefaultBlog(Blog blog) {
        try {
            this.handleDefaultBlog(blog);
        } catch (RuntimeException rt) {
            throw new com.communote.server.core.ConfigurationManagementException(
                    "Error performing 'updateDefaultBlog(Blog blog)' --> " + rt, rt);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateLdapConfiguration(
            com.communote.server.model.config.LdapConfiguration ldapConfig)
                    throws com.communote.server.api.core.security.AuthorizationException,
                    PrimaryAuthenticationException {
        if (ldapConfig == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.service.ConfigurationManagement.updateLdapConfiguration(com.communote.server.persistence.config.LdapConfiguration ldapConfig) - 'ldapConfig' can not be null");
        }
        try {
            this.handleUpdateLdapConfiguration(ldapConfig);
        } catch (RuntimeException rt) {
            throw new com.communote.server.core.ConfigurationManagementException(
                    "Error performing 'ConfigurationManagement.updateLdapConfiguration(com.communote.server.persistence.config.LdapConfiguration ldapConfig)' --> "
                            + rt, rt);
        }
    }

}