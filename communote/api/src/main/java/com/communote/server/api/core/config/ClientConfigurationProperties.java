package com.communote.server.api.core.config;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.communote.common.encryption.EncryptionException;
import com.communote.common.encryption.EncryptionUtils;
import com.communote.common.util.Pair;
import com.communote.server.api.core.config.type.ApplicationProperty;
import com.communote.server.api.core.config.type.ClientProperty;
import com.communote.server.api.core.config.type.ClientPropertySecurity;
import com.communote.server.model.blog.Blog;
import com.communote.server.model.config.Configuration;
import com.communote.server.model.config.ConfluenceConfiguration;
import com.communote.server.model.config.ExternalSystemConfiguration;
import com.communote.server.model.config.LdapConfiguration;
import com.communote.server.model.config.Setting;

/**
 * Object holding client specific configuration properties.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class ClientConfigurationProperties extends
        AbstractConfigurationProperties<ClientConfigurationPropertyConstant> implements
        Serializable {

    /**
     * default serial version ID
     */
    private static final long serialVersionUID = 1L;

    /** Logger. */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(ClientConfigurationProperties.class);

    private final Map<String, Pair<String, Long>> properties;

    /** The ldap configuration. */
    private LdapConfiguration ldapConfiguration;

    private ConfluenceConfiguration confluenceConfig;

    private Collection<ExternalSystemConfiguration> externalSystemConfigs;

    private String primaryExternalAuthenticationSystemId;

    private Long defaultBlogId;
    private Locale defaultLanguage;

    private String clientTimeZoneId;

    /**
     * Constructs a new instance.
     *
     * @param configuration
     *            the client configuration for initializing the properties
     */
    public ClientConfigurationProperties(Configuration configuration) {
        properties = new HashMap<String, Pair<String, Long>>(
                configuration.getSettings() != null ? configuration.getSettings().size() : 0);
        init(configuration);
    }

    /**
     * Returns the time zone id of this client or null.
     *
     * @return the time zone id or null
     */
    public String getClientTimeZoneId() {
        return this.clientTimeZoneId;
    }

    /**
     * Returns the confluence authentication configuration. The configuration is returned as a copy
     * to avoid direct manipulation. To modify the configuration use
     * {@link com.communote.server.core.ConfigurationManagement}.
     *
     * @return the confluence authentication configuration
     */
    public ConfluenceConfiguration getConfluenceConfiguration() {
        ConfluenceConfiguration config = null;
        // clone config to avoid direct manipulation; modification should be done via
        // ConfigurationManagement and call to #reload
        if (this.confluenceConfig != null) {
            config = this.confluenceConfig.deepCopy();
        }
        return config;
    }

    /**
     *
     * @return The ID of the default blog or null if there is no default blog
     */
    public Long getDefaultBlogId() {
        return defaultBlogId;
    }

    /**
     * Get default locale
     *
     * if default language is null or not a valid language code return english
     *
     * @return Locale
     */
    public Locale getDefaultLanguage() {
        if (defaultLanguage == null) {
            defaultLanguage = Locale.ENGLISH; // fallback to english
            String defaultLanguageProperty = this.getProperty(ClientProperty.DEFAULT_LANGUAGE);
            LOGGER.debug("default language {}", defaultLanguageProperty);
            if (StringUtils.isNotBlank(defaultLanguageProperty)) {
                String defaultLanguageTrimmed = defaultLanguageProperty.trim();
                Locale locale = new Locale(defaultLanguageTrimmed);
                if (locale.getLanguage().equals(defaultLanguageTrimmed)) {
                    defaultLanguage = locale;
                } else {
                    LOGGER.warn("code of default language {} is not a valid language code",
                            defaultLanguageTrimmed);
                }

            }
        }
        return defaultLanguage;
    }

    /**
     * @return all available external system configurations
     */
    public Collection<ExternalSystemConfiguration> getExternalSystemConfigurations() {
        if (externalSystemConfigs == null) {
            Collection<ExternalSystemConfiguration> externalConfigs = new ArrayList<ExternalSystemConfiguration>();
            ExternalSystemConfiguration config = getLdapConfiguration();
            if (config != null) {
                externalConfigs.add(config);
            }
            config = getConfluenceConfiguration();
            if (config != null) {
                externalConfigs.add(config);
            }
            // TODO
            // config = getSharepointConfiguration();
            // if (config != null) {
            // externalConfigs.add(config);
            // }
            externalSystemConfigs = externalConfigs;
        }
        return externalSystemConfigs;
    }

    /**
     * Returns the LDAP configuration. The configuration is returned as a copy to avoid direct
     * manipulation. To modify the configuration use
     * {@link com.communote.server.core.ConfigurationManagement#updateLdapConfiguration(LdapConfiguration)}
     *
     *
     * @return the LDAP configuration
     * @deprecated use
     *             {@link com.communote.server.service.UserService#getExternalSystemConfiguration(String)}
     */
    @Deprecated
    public LdapConfiguration getLdapConfiguration() {
        LdapConfiguration cloneConfig = null;
        // clone ldap config to avoid direct manipulation; modification should be done via
        // ConfigurationManager
        if (this.ldapConfiguration != null) {
            cloneConfig = this.ldapConfiguration.deepCopy();
        }
        return cloneConfig;
    }

    /**
     * {@inheritDoc}
     */
    public String getPrimaryExternalAuthentication() {
        return this.primaryExternalAuthenticationSystemId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getProperty(ClientConfigurationPropertyConstant key) {
        Pair<String, Long> property = properties.get(key.getKeyString());
        if (property != null) {
            return property.getLeft();
        }
        return null;
    }

    /**
     * Return the time of the last modification of the property.
     * <p>
     * Note: for properties which were added before the modification time was stored this will
     * return the date of epoch
     * </p>
     *
     * @param key
     *            the key of the property to retrieve
     * @return the date of the last modification or null if there is no property for the given key
     */
    public Date getPropertyLastModification(ClientConfigurationPropertyConstant key) {
        Pair<String, Long> property = properties.get(key.getKeyString());
        if (property != null) {
            return new Date(property.getRight());
        }
        return null;
    }

    /**
     *
     * @return The configured Repository mode or the default if not set. Never returns null.
     */
    public ClientProperty.REPOSITORY_MODE getRepositoryMode() {
        String value = this.getProperty(ClientProperty.USER_SERVICE_REPOSITORY_MODE);
        ClientProperty.REPOSITORY_MODE mode = ClientProperty.DEFAULT_USER_SERVICE_REPOSITORY_MODE;

        if (value == null) {
            LOGGER.debug("No Repository mode configured. Default will be used: {} = {}",
                    ClientProperty.USER_SERVICE_REPOSITORY_MODE.getKeyString(), mode);
        } else {
            try {
                mode = ClientProperty.REPOSITORY_MODE.valueOf(value);
            } catch (IllegalArgumentException e) {
                LOGGER.error("Illegal RepositoryMode: "
                        + ClientProperty.USER_SERVICE_REPOSITORY_MODE.getKeyString() + "=" + value
                        + " Default will be used: " + mode);
            }
        }
        return mode;
    }

    /**
     * Initializes the properties.
     *
     * @param configuration
     *            the client configuration for initializing the properties
     */
    private void init(Configuration configuration) {
        if (configuration == null) {
            throw new IllegalArgumentException("Configuration must not be null");
        }
        LOGGER.info("Initializing client specific configuration properties");
        Set<Setting> settings = configuration.getSettings();
        if (settings != null) {
            for (Setting setting : settings) {
                Long lastModification = setting.getLastModificationTimestamp();
                if (lastModification == null) {
                    lastModification = 0L;
                }
                Pair<String, Long> entry = new Pair<>(setting.getSettingValue(), lastModification);
                this.properties.put(setting.getSettingKey(), entry);
                LOGGER.debug("Property '{}' -> '{}'", setting.getSettingKey(),
                        setting.getSettingValue());
            }
        }

        loadLdapConfiguration(configuration);
        loadConfluenceConfiguration(configuration);

        Blog defaultBlog = configuration.getClientConfig().getDefaultBlog();
        // only store ID of default blog because we would provide stale data if the title or
        // description of the blog was changed
        this.defaultBlogId = defaultBlog != null ? defaultBlog.getId() : null;

        this.clientTimeZoneId = configuration.getClientConfig().getTimeZoneId();
    }

    /**
     * Checks if a Confluence authentication is configured and activated.
     *
     * @return true, if Confluence authentication is configured and activated
     * @deprecated use
     *             {@link com.communote.server.service.UserService#getExternalSystemConfiguration(String)}
     */
    @Deprecated
    public boolean isConfluenceAuthenticationActivated() {
        return this.confluenceConfig != null
                && this.confluenceConfig.isAllowExternalAuthentication();
    }

    /**
     * @return True, when it is allowed to register users via the internal database.
     */
    public boolean isDBAuthenticationAllowed() {
        return getPrimaryExternalAuthentication() == null
                || getProperty(ClientPropertySecurity.ALLOW_DB_AUTH_ON_EXTERNAL,
                        ClientPropertySecurity.DEFAULT_ALLOW_DB_AUTH_ON_EXTERNAL);
    }

    /**
     * Checks if the default blog is configured.
     *
     * @return true, if there is a configured default blog
     */
    public boolean isDefaultBlogEnabled() {
        return this.defaultBlogId != null;
    }

    /**
     * Checks if an external authentication e.g. via LDAP is activated.
     *
     * @return true, if there is a configured and activated external authentication
     */
    public boolean isExternalAuthenticationActivated() {
        for (ExternalSystemConfiguration config : this.getExternalSystemConfigurations()) {
            if (config.isAllowExternalAuthentication()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if an LDAP authentication is configured and activated.
     *
     * @return true, if LDAP authentication is configured and activated
     * @deprecated use {@link #getExternalSystemConfigurations()}
     */
    @Deprecated
    public boolean isLdapAuthenticationActivated() {
        return this.ldapConfiguration != null
                && this.ldapConfiguration.isAllowExternalAuthentication();
    }

    /**
     * @return True, if is allowed for users to register themselves.
     */
    public boolean isRegistrationAllowed() {
        return getProperty(ClientProperty.USER_REGISTRATION_ALLOWED,
                getPrimaryExternalAuthentication() == null) && isDBAuthenticationAllowed();
    }

    /**
     * Loads the existing Confluence configuration if available.
     *
     * @param configuration
     *            The configuration.
     */
    private void loadConfluenceConfiguration(Configuration configuration) {
        this.confluenceConfig = null;

        ConfluenceConfiguration confluenceConfig = configuration.getConfluenceConfig();
        if (confluenceConfig != null) {
            // create a detached copy of the configuration to avoid unintended modification when
            // called from within a Management class
            this.confluenceConfig = confluenceConfig.deepCopy();
            try {
                String decryptedPassword = EncryptionUtils.decrypt(
                        confluenceConfig.getAdminPassword(),
                        ApplicationProperty.INSTALLATION_UNIQUE_ID.getValue());
                this.confluenceConfig.setAdminPassword(decryptedPassword);
            } catch (EncryptionException e) {
                LOGGER.warn("Was not able to decrypt the "
                        + "administrator password of the Confluence configuration. Treating the password as "
                        + "not being encrypted");
            }
        }
        // pass the original config to avoid exposure of the decrypted pwd when DEBUG is enabled
        postExternalAuthConfigLoad(confluenceConfig, "confluence configuration: {}");
    }

    /**
     * Loads the existing LDAP configuration if available.
     *
     * @param configuration
     *            The configuration.
     */
    private void loadLdapConfiguration(Configuration configuration) {
        this.ldapConfiguration = null;

        LdapConfiguration ldapConfig = configuration.getLdapConfig();
        if (ldapConfig != null) {
            // create a detached copy of the configuration to avoid unintended modification when
            // called from within a Management class
            this.ldapConfiguration = ldapConfig.deepCopy();
            try {
                String decryptedPassword = EncryptionUtils.decrypt(ldapConfig.getManagerPassword(),
                        ApplicationProperty.INSTALLATION_UNIQUE_ID.getValue());
                this.ldapConfiguration.setManagerPassword(decryptedPassword);
            } catch (EncryptionException e) {
                LOGGER.warn("Was not able to decrypt the "
                        + "manager password of the LDAP configuration. Treating the password as "
                        + "not being encrypted");
            }
        }
        // pass the original config to avoid exposure of the decrypted pwd when DEBUG is enabled
        postExternalAuthConfigLoad(ldapConfig, "ldap configuration: {}");
    }

    /**
     * Logs the configuration details and extracts the system ID if the external authentication is
     * active and primary.
     *
     * @param extAuth
     *            the external authentication
     * @param logMessage
     *            The message to log containing a placeholder for the configuration.
     */
    private void postExternalAuthConfigLoad(ExternalSystemConfiguration extAuth, String logMessage) {
        if (extAuth != null) {
            LOGGER.debug(logMessage, extAuth.attributesToString());
            if (extAuth.isAllowExternalAuthentication() && extAuth.isPrimaryAuthentication()) {
                this.primaryExternalAuthenticationSystemId = extAuth.getSystemId();
            }
        }
    }
}
