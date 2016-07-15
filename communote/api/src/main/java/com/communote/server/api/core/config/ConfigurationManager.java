package com.communote.server.api.core.config;

import java.util.Map;

import com.communote.common.encryption.EncryptionException;
import com.communote.server.api.core.config.database.DatabaseConfiguration;
import com.communote.server.api.core.config.database.DatabaseConfigurationFactory;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.api.core.user.PrimaryAuthenticationException;
import com.communote.server.model.blog.Blog;
import com.communote.server.model.config.ConfluenceConfiguration;
import com.communote.server.model.config.LdapConfiguration;

/**
 * Manager for accessing and updating configuration
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public interface ConfigurationManager {

    /**
     * Deactivates the default blog of the current client.
     *
     */
    public void deactivateDefaultBlog();

    /**
     * Returns the current application properties.
     *
     * @return the application properties
     */
    public ApplicationConfigurationProperties getApplicationConfigurationProperties();

    /**
     * Returns the current application properties.
     *
     * @param forceCacheInvalidation
     *            true if the cache should be invalidated before fetching
     * @return the application properties
     */
    public ApplicationConfigurationProperties getApplicationConfigurationProperties(
            boolean forceCacheInvalidation);

    /**
     * Returns the client configuration properties of the current client.
     *
     * @return the client configuration properties
     */
    public ClientConfigurationProperties getClientConfigurationProperties();

    /**
     * Return the DatabaseConfiguration that is created by a {@link DatabaseConfigurationFactory}.
     * If no factory was set with
     * {@link #setDatabaseConfigurationFactory(DatabaseConfigurationFactory)} null will be returned.
     * The configuration will only created once if this method is called after the installation has
     * been completed.
     *
     * @return the {@link DatabaseConfiguration} to use
     */
    public DatabaseConfiguration getDatabaseConfiguration();

    /**
     * Returns the development properties of the running instance.
     *
     * @return the development properties
     */
    public DevelopmentProperties getDevelopmentProperties();

    /**
     * Returns the startup properties.
     *
     * @return the startup properties
     */
    public StartupProperties getStartupProperties();

    /**
     * Set the factory for creating the database configuration when
     * {@link #getDatabaseConfiguration()} is called.
     *
     * @param databaseConfigurationFactory
     *            the database configuration factory to use
     */
    public void setDatabaseConfigurationFactory(
            DatabaseConfigurationFactory databaseConfigurationFactory);

    /**
     * Activates the default blog of the current client.
     *
     * @param blog
     *            The default blog.
     *
     */
    public void setDefaultBlog(Blog blog);

    /**
     * @param externalSystemId
     *            The id of the external system. If this is null, the current authentication method
     *            will be removed.
     * @param allowDBAuth
     *            True, if internal database authentication is allowed.
     * @throws PrimaryAuthenticationException
     *             Exception.
     */
    public void setPrimaryAuthentication(String externalSystemId, boolean allowDBAuth)
            throws PrimaryAuthenticationException;

    /**
     * Updates a collection of application configuration properties.
     *
     * @throws ConfigurationUpdateException
     *             exception.
     * @param settings
     *            a mapping of the properties to their new value
     */
    public void updateApplicationConfigurationProperties(
            Map<ApplicationConfigurationPropertyConstant, String> settings)
                    throws ConfigurationUpdateException;

    /**
     * Updates a collection of client configuration properties.
     *
     * @param settings
     *            a mapping of the properties to their new value
     */
    public void updateClientConfigurationProperties(
            Map<ClientConfigurationPropertyConstant, String> settings);

    /**
     * Updates a client configuration properties.
     *
     * @param key
     *            the property to update
     * @param value
     *            the new value
     */
    public void updateClientConfigurationProperty(ClientConfigurationPropertyConstant key,
            String value);

    /**
     * Update the client timezone and reload the cache. <br>
     * Forwards to
     * {@link com.communote.server.core.ConfigurationManagement#updateClientTimeZoneId(String)}
     *
     * @param timeZoneId
     *            the new timezoneId
     */
    public void updateClientTimeZone(String timeZoneId);

    /**
     * Update the Confluence authentication and reload the cache. Forwards to
     * {@link com.communote.server.core.ConfigurationManagement#updateConfluenceAuthConfig(ConfluenceConfiguration)}
     *
     * @param confluenceAuthConfig
     *            the config
     * @throws AuthorizationException
     *             in case the current user is not client manager
     * @throws EncryptionException
     *             in case of an encryption exception
     * @throws PrimaryAuthenticationException
     *             Exception.
     */
    public void updateConfluenceConfig(ConfluenceConfiguration confluenceAuthConfig)
            throws EncryptionException, AuthorizationException, PrimaryAuthenticationException;

    /**
     * Update the LDAP authentication and reload the cache. <br>
     * Forwards to
     * {@link com.communote.server.core.ConfigurationManagement#updateLdapConfiguration(LdapConfiguration)}
     *
     * @param ldapConfig
     *            the config
     * @throws AuthorizationException
     *             in case the current user is not client manager
     * @throws EncryptionException
     *             in case of an encryption exception
     * @throws PrimaryAuthenticationException
     *             Exception.
     */
    public void updateLdapConfiguration(LdapConfiguration ldapConfig)
            throws AuthorizationException, EncryptionException, PrimaryAuthenticationException;

    /**
     * Update the startup properties.
     *
     * @param settings
     *            mapping with the new values to be set.
     * @throws ConfigurationUpdateException
     *             in case the update failed
     */
    public void updateStartupProperties(Map<CoreConfigurationPropertyConstant, String> settings)
            throws ConfigurationUpdateException;

    /**
     * Updates a startup property.
     *
     * @param property
     *            the property to update
     * @param value
     *            the new value
     * @throws ConfigurationUpdateException
     *             in case the update failed
     */
    public void updateStartupProperty(CoreConfigurationPropertyConstant property, String value)
            throws ConfigurationUpdateException;

}