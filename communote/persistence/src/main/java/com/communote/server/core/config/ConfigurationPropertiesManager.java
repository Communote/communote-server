package com.communote.server.core.config;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.communote.common.encryption.EncryptionException;
import com.communote.common.encryption.EncryptionUtils;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.config.ApplicationConfigurationProperties;
import com.communote.server.api.core.config.ApplicationConfigurationPropertyConstant;
import com.communote.server.api.core.config.ClientConfigurationProperties;
import com.communote.server.api.core.config.ClientConfigurationPropertyConstant;
import com.communote.server.api.core.config.ConfigurationInitializationException;
import com.communote.server.api.core.config.ConfigurationManager;
import com.communote.server.api.core.config.ConfigurationUpdateException;
import com.communote.server.api.core.config.CoreConfigurationPropertyConstant;
import com.communote.server.api.core.config.DevelopmentProperties;
import com.communote.server.api.core.config.StartupProperties;
import com.communote.server.api.core.config.database.DatabaseConfiguration;
import com.communote.server.api.core.config.database.DatabaseConfigurationFactory;
import com.communote.server.api.core.config.type.ApplicationProperty;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.api.core.user.PrimaryAuthenticationException;
import com.communote.server.core.ConfigurationManagement;
import com.communote.server.core.common.caching.ApplicationSingleElementCacheKey;
import com.communote.server.core.common.caching.Cache;
import com.communote.server.core.common.caching.CacheManager;
import com.communote.server.core.common.caching.ClientSingleElementCacheKey;
import com.communote.server.model.blog.Blog;
import com.communote.server.model.config.ConfluenceConfiguration;
import com.communote.server.model.config.LdapConfiguration;

/**
 * Manages the different configuration properties.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
// This can't be a service as it must been available before services are loaded.
public class ConfigurationPropertiesManager implements ConfigurationManager {

    private final static Logger LOG = LoggerFactory.getLogger(ConfigurationPropertiesManager.class);

    private StartupPropertiesImpl startupProperties;

    private DevelopmentProperties developmentProperties;
    private ApplicationConfigurationElementProvider applicationConfigProvider;

    private ClientConfigurationElementProvider clientConfigProvider;

    private DatabaseConfigurationFactory databaseConfigFactory;
    private DatabaseConfiguration databaseConfiguration;

    /**
     * Creates and initializes a new instance of the configuration properties manager. A
     * {@link ConfigurationInitializationException} will be thrown if the initialization of the
     * manager failed.
     *
     * @param applicationRealPath
     *            the file system path to the web application which will be used to load some
     *            configuration files
     */
    public ConfigurationPropertiesManager(String applicationRealPath) {
        initInstance(applicationRealPath);
    }

    @Override
    public void deactivateDefaultBlog() {
        setDefaultBlog(null);
    }

    @Override
    public ApplicationConfigurationProperties getApplicationConfigurationProperties() {
        return getApplicationConfigurationProperties(false);
    }

    @Override
    public ApplicationConfigurationProperties getApplicationConfigurationProperties(
            boolean forceCacheInvalidation) {
        return internalGetApplicationProperties(forceCacheInvalidation);
    }

    /**
     * @return the cache manager
     */
    private CacheManager getCacheManager() {
        return ServiceLocator.findService(CacheManager.class);
    }

    @Override
    public ClientConfigurationProperties getClientConfigurationProperties() {
        // TODO this uses internal knowledge about when something is available...
        if (CommunoteRuntime.getInstance().isCoreInitialized()) {
            return getCacheManager().getCache().get(new ClientSingleElementCacheKey(),
                    clientConfigProvider);
        } else {
            return clientConfigProvider.load(new ClientSingleElementCacheKey());
        }
    }

    /**
     * @return the configuration management
     */
    private ConfigurationManagement getConfigurationManagement() {
        return ServiceLocator.findService(ConfigurationManagement.class);
    }

    @Override
    public DatabaseConfiguration getDatabaseConfiguration() {
        if (this.databaseConfiguration == null) {
            DatabaseConfigurationFactory factory = databaseConfigFactory;
            if (factory != null) {
                DatabaseConfiguration config = databaseConfigFactory.createDatabaseConfiguration();
                if (startupProperties.isInstallationDone()) {
                    // keep the config since installation is done
                    this.databaseConfiguration = config;
                }
                return config;
            }
        }
        return databaseConfiguration;
    }

    @Override
    public DevelopmentProperties getDevelopmentProperties() {
        return developmentProperties;
    }

    @Override
    public StartupProperties getStartupProperties() {
        return startupProperties;
    }

    /**
     * Initializes the managed properties.
     *
     * @param applicationRealPath
     *            the file system path to the web application which will be used to load some
     *            configuration files
     * @throws ConfigurationInitializationException
     *             if the initialization failed
     */
    private void initInstance(String applicationRealPath)
            throws ConfigurationInitializationException {
        startupProperties = new StartupPropertiesImpl(applicationRealPath);
        developmentProperties = new DevelopmentProperties(
                startupProperties.getConfigurationDirectory());
        applicationConfigProvider = new ApplicationConfigurationElementProvider();
        clientConfigProvider = new ClientConfigurationElementProvider();
    }

    /**
     * Returns the ApplicationConfigurationProperties. If the application is already initialized the
     * properties are loaded from cache otherwise from the database.
     *
     * @param forceCacheInvalidation
     *            true if the cache should be invalidated before fetching
     * @return the properties of the application configuration.
     */
    private ApplicationConfigurationProperties internalGetApplicationProperties(
            boolean forceCacheInvalidation) {
        if (CommunoteRuntime.getInstance().isCoreInitialized()) {
            if (forceCacheInvalidation) {
                LOG.debug("Invalidation of cached ApplicationConfigurationProperties forced");
                invalidateApplicationConfigurationPropertiesCache();
            }
            return getCacheManager().getCache().get(new ApplicationSingleElementCacheKey(),
                    applicationConfigProvider);
        } else {
            return applicationConfigProvider.load(new ApplicationSingleElementCacheKey());
        }
    }

    /**
     * Invalidates the cache holding the client configuration properties.
     */
    private void invalidateApplicationConfigurationPropertiesCache() {
        Cache cache = getCacheManager().getCache();
        // can be null in initialization phase
        if (cache != null) {
            cache.invalidate(new ApplicationSingleElementCacheKey(), applicationConfigProvider);
        }
    }

    /**
     * Invalidates the cache holding the client configuration properties.
     */
    private void invalidateClientConfigurationPropertiesCache() {
        Cache cache = getCacheManager().getCache();
        if (cache != null) {
            cache.invalidate(new ClientSingleElementCacheKey(), clientConfigProvider);
        }
    }

    @Override
    public void setDatabaseConfigurationFactory(
            DatabaseConfigurationFactory databaseConfigurationFactory) {
        this.databaseConfigFactory = databaseConfigurationFactory;
    }

    @Override
    public void setDefaultBlog(Blog blog) {
        getConfigurationManagement().updateDefaultBlog(blog);
        invalidateClientConfigurationPropertiesCache();
    }

    @Override
    public void setPrimaryAuthentication(final String externalSystemId, final boolean allowDBAuth)
            throws PrimaryAuthenticationException {
        getConfigurationManagement().setPrimaryAuthentication(externalSystemId, allowDBAuth,
                getClientConfigurationProperties().getPrimaryExternalAuthentication());
        invalidateClientConfigurationPropertiesCache();
    }

    @Override
    public void updateApplicationConfigurationProperties(
            Map<ApplicationConfigurationPropertyConstant, String> settings)
                    throws ConfigurationUpdateException {
        try {
            getConfigurationManagement().updateApplicationSettings(settings);
        } catch (AuthorizationException e) {
            throw new ConfigurationUpdateException(
                    "Current user is not allowed to update the properties",
                    "configuration.application.properties.update.not.authorized");
        }
        invalidateApplicationConfigurationPropertiesCache();
    }

    @Override
    public void updateClientConfigurationProperties(
            Map<ClientConfigurationPropertyConstant, String> settings) {
        if (settings.size() == 0) {
            return;
        }
        getConfigurationManagement().updateClientSettings(settings);
        invalidateClientConfigurationPropertiesCache();
    }

    @Override
    public void updateClientConfigurationProperty(ClientConfigurationPropertyConstant key,
            String value) {
        if (!StringUtils.equals(getClientConfigurationProperties().getProperty(key), value)) {
            getConfigurationManagement().updateClientSetting(key, value);
            invalidateClientConfigurationPropertiesCache();
        }
    }

    @Override
    public void updateClientTimeZone(String timeZoneId) {
        getConfigurationManagement().updateClientTimeZoneId(timeZoneId);
        invalidateClientConfigurationPropertiesCache();
    }

    @Override
    public void updateConfluenceConfig(ConfluenceConfiguration confluenceAuthConfig)
            throws EncryptionException, AuthorizationException, PrimaryAuthenticationException {

        // encrypt the password before update the configuration
        String decryptedPassword = confluenceAuthConfig.getAdminPassword();
        String encryptedPassword = EncryptionUtils.encrypt(decryptedPassword,
                ApplicationProperty.INSTALLATION_UNIQUE_ID.getValue());

        // set the encrypted password for update purposes
        confluenceAuthConfig.setAdminPassword(encryptedPassword);

        getConfigurationManagement().updateConfluenceAuthConfig(confluenceAuthConfig);

        // reset the password for further use
        confluenceAuthConfig.setAdminPassword(decryptedPassword);

        invalidateClientConfigurationPropertiesCache();
    }

    @Override
    public void updateLdapConfiguration(LdapConfiguration ldapConfig)
            throws AuthorizationException, EncryptionException, PrimaryAuthenticationException {

        // encrypt the password before update the configuration
        String decryptedPassword = ldapConfig.getManagerPassword();
        String encryptedPassword = EncryptionUtils.encrypt(decryptedPassword,
                ApplicationProperty.INSTALLATION_UNIQUE_ID.getValue());

        // set the encrypted password for update purposes
        ldapConfig.setManagerPassword(encryptedPassword);

        getConfigurationManagement().updateLdapConfiguration(ldapConfig);

        // reset the password for further use
        ldapConfig.setManagerPassword(decryptedPassword);

        invalidateClientConfigurationPropertiesCache();
    }

    @Override
    public void updateStartupProperties(Map<CoreConfigurationPropertyConstant, String> settings)
            throws ConfigurationUpdateException {
        startupProperties.update(settings);
    }

    @Override
    public void updateStartupProperty(CoreConfigurationPropertyConstant property, String value)
            throws ConfigurationUpdateException {
        startupProperties.update(property, value);
    }
}
