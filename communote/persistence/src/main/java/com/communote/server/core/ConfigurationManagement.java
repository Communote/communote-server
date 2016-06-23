package com.communote.server.core;

import java.util.Collection;
import java.util.Map;

import com.communote.server.api.core.config.ApplicationConfigurationPropertyConstant;
import com.communote.server.api.core.config.ClientConfigurationPropertyConstant;
import com.communote.server.api.core.image.ImageVO;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.api.core.user.PrimaryAuthenticationException;
import com.communote.server.model.blog.Blog;
import com.communote.server.model.config.ApplicationConfigurationSetting;
import com.communote.server.model.config.Configuration;
import com.communote.server.model.config.ConfluenceConfiguration;
import com.communote.server.model.config.LdapConfiguration;

/**
 * <p>
 * Handling configurations
 * </p>
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public interface ConfigurationManagement {

    /** DEFAULT_CONFLUENCE_SYSTEM_ID. */
    public static final String DEFAULT_CONFLUENCE_SYSTEM_ID = "DefaultConfluence";

    /**
     * DEFAULT_LDAP_SYSTEM_ID.
     * 
     * @deprecated Use LdapUserRepository#EXTERNAL_SYSTEM_ID_DEFAULT_LDAP where possible. All LDAP
     *             specifics should be moved to the ldap plugin.
     */
    @Deprecated
    public static final String DEFAULT_LDAP_SYSTEM_ID = "DefaultLDAP";
    /** DEFAULT_DATABASE_ID. */
    public static final String DEFAULT_DATABASE_ID = "DATABASE";

    /**
     * @return the current application configuration settings.
     */
    public Collection<ApplicationConfigurationSetting> getApplicationConfigurationSettings();

    /**
     * 
     * @return the client logo
     */
    public ImageVO getClientLogo();

    /**
     * @return The current configuration.
     */
    public Configuration getConfiguration();

    /**
     * <p>
     * Gets the default blog.
     * </p>
     * 
     * @return The default blog.
     */
    public Blog getDefaultBlog();

    /**
     * 
     */
    public void removeClientLogo();

    /**
     * @param externalSystemId
     *            The ID of the external system which should be made the primary authentication
     *            system. If this is null, the current primary authentication system will be
     *            removed.
     * @param allowDBAuth
     *            True, if authentication against the internal database should be allowed.
     * @param currentPrimaryAuthentication
     *            The ID of the external system which is the current primary authentication system.
     *            Can be null if there is currently no primary authentication.
     * @throws PrimaryAuthenticationException
     *             Thrown if there is no active configuration for the externalSystemId or no
     *             configuration for the currentPrimaryAuthentication. This Exception is also thrown
     *             if allowDBAuth is false and there are no client managers that can authenticate
     *             against the new primary system.
     */
    public void setPrimaryAuthentication(final String externalSystemId, final boolean allowDBAuth,
            String currentPrimaryAuthentication) throws PrimaryAuthenticationException;

    /**
     * Updates or creates the provided application settings.
     * 
     * @param settings
     *            The updated settings.
     * @throws AuthorizationException
     *             Exception.
     */
    public void updateApplicationSettings(
            Map<ApplicationConfigurationPropertyConstant, String> settings)
            throws AuthorizationException;

    /**
     * Updates the client logo.
     * 
     * @param image
     *            The logo as byte array.
     */
    public void updateClientLogo(byte[] image);

    /**
     * Update the given setting using the key and value. If the key already exists it will be
     * updated.
     * 
     * @param key
     *            The key of the setting.
     * @param value
     *            The updated value.
     */
    public void updateClientSetting(ClientConfigurationPropertyConstant key, String value);

    /**
     * Updates or creates the provided client configuration settings.
     * 
     * @param settings
     *            The updated settings.
     */
    public void updateClientSettings(
            Map<ClientConfigurationPropertyConstant, String> settings);

    /**
     * @param timeZoneId
     *            The clients new timezone.
     */
    public void updateClientTimeZoneId(String timeZoneId);

    /**
     * <p>
     * Updates or creates the confluence authentication configuration
     * </p>
     * 
     * @param confluenceAuthConf
     *            The updated Confluence authentication configuration.
     * @throws AuthorizationException
     *             Exception.
     * @throws PrimaryAuthenticationException
     *             Exception.
     */
    public void updateConfluenceAuthConfig(ConfluenceConfiguration confluenceAuthConf)
            throws AuthorizationException, PrimaryAuthenticationException;

    /**
     * <p>
     * Updates the default blog.
     * </p>
     * 
     * @param blog
     *            The new default blog.
     */
    public void updateDefaultBlog(Blog blog);

    /**
     * 
     * @param ldapConfig
     *            The updated LDAP authentication configuration.
     * 
     * @throws AuthorizationException
     *             Exception.
     * @throws PrimaryAuthenticationException
     *             Exception.
     * 
     */
    public void updateLdapConfiguration(LdapConfiguration ldapConfig)
            throws AuthorizationException, PrimaryAuthenticationException;

}
