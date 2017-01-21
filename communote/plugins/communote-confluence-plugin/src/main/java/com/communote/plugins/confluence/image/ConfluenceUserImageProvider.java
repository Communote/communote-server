package com.communote.plugins.confluence.image;

import java.net.URI;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.communote.common.util.UriUtils;
import com.communote.server.api.ServiceLocator;
import com.communote.server.core.ConfigurationManagement;
import com.communote.server.core.image.type.HttpExternalUserImageProvider;
import com.communote.server.core.plugin.PluginPropertyManagement;
import com.communote.server.core.user.UserProfileDetails;
import com.communote.server.core.user.UserProfileManagement;
import com.communote.server.model.config.ConfluenceConfiguration;
import com.communote.server.model.config.ExternalImageApiUrlProvider;
import com.communote.server.model.config.ExternalSystemConfiguration;
import com.communote.server.service.UserService;

/**
 * This image provider loads the image from Confluence, if the user is a Confluence user.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ConfluenceUserImageProvider extends
HttpExternalUserImageProvider<ConfluenceConfiguration> {
    /**
     * Key of a plugin property to store the last modification timestamp of the confluence
     * configuration since the configuration does not provide this information.
     */
    public static final String CLIENT_PROPERTY_KEY_CONFIG_MODIFIED = "config.last.modified";

    /** default image time to live (24h) 24 * 3600 */
    private static final int CONFLUENCE_IMAGES_TIME_TO_LIVE = 24 * 3600;

    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfluenceUserImageProvider.class);

    /**
     * @param symbolicName
     *            the the symbolic name of the bundle which will be used as property key group
     * @param pathToDefaultImage
     *            The path to the default image.
     */
    public ConfluenceUserImageProvider(String symbolicName, String pathToDefaultImage) {
        // 900000ms => 15 min
        super("confluenceUserImage", pathToDefaultImage, Long.getLong(
                "com.communote.plugin.confluence.external.images.timeout", 900000l), symbolicName);
    }

    @Override
    public boolean canLoad(String imageIdentifier) {
        String externalUserImageUrl = getImageURL(imageIdentifier, getConfiguration());
        return externalUserImageUrl != null && externalUserImageUrl.length() > 0;
    }

    @Override
    protected ConfluenceConfiguration getConfiguration() {
        ExternalSystemConfiguration config = ServiceLocator.findService(UserService.class)
                .getExternalSystemConfiguration(
                        ConfigurationManagement.DEFAULT_CONFLUENCE_SYSTEM_ID);
        if (config != null && config instanceof ConfluenceConfiguration) {
            return (ConfluenceConfiguration) config;
        }
        return null;
    }

    @Override
    protected Long getConfigurationChangeTimestamp(ConfluenceConfiguration config) {
        String symbolicName = getPropertyKeyGroup();
        String value = ServiceLocator.findService(PluginPropertyManagement.class)
                .getClientProperty(symbolicName, CLIENT_PROPERTY_KEY_CONFIG_MODIFIED);
        if (value != null) {
            try {
                return Long.parseLong(value);
            } catch (NumberFormatException e) {
                LOGGER.warn("Invalid client property value {}, {}", symbolicName,
                        CLIENT_PROPERTY_KEY_CONFIG_MODIFIED);
            }
        }
        return null;
    }

    /**
     * Returns the ID of the user in the external system. The ID will be URI-encoded.
     *
     * @param userId
     *            the local user ID
     * @return the ID or null if the user does not have a user ID in the external system.
     */
    private String getExternalUserId(Long userId) {
        UserProfileDetails profile = ServiceLocator.findService(UserProfileManagement.class)
                .getUserProfileDetailsById(userId, true);
        if (profile == null) {
            return null;
        }
        String externalUserId = profile
                .getExternalUserId(ConfigurationManagement.DEFAULT_CONFLUENCE_SYSTEM_ID);

        if (externalUserId != null) {
            return UriUtils.encodeUriComponent(externalUserId);
        }
        return null;
    }

    @Override
    protected String getImageURL(String imageIdentifier, ConfluenceConfiguration config) {
        Long userId = getUserId(imageIdentifier);
        if (userId != null) {
            String externalUserAlias = getExternalUserId(userId);
            if (externalUserAlias == null || config == null) {
                return null;
            }

            String url = null;
            // TODO forcing external authentication shouldn't be necessary
            if (config.isAllowExternalAuthentication()
                    && config instanceof ExternalImageApiUrlProvider) {
                ExternalImageApiUrlProvider provider = config;
                url = provider.getImageApiUrl();
            }
            if (StringUtils.isNotBlank(url)) {
                return url + externalUserAlias;
            }

        }
        return null;
    }

    @Override
    protected HttpContext getRequestContext(URI imageUrl, String imageIdentifier,
            ConfluenceConfiguration config) {
        HttpHost targetHost = new HttpHost(imageUrl.getHost(), imageUrl.getPort());
        CredentialsProvider credentialsProviderProvider = new BasicCredentialsProvider();
        credentialsProviderProvider.setCredentials(new AuthScope(targetHost.getHostName(),
                targetHost.getPort()), new UsernamePasswordCredentials(config.getAdminLogin(),
                config.getAdminPassword()));

        AuthCache authCache = new BasicAuthCache();
        BasicScheme basicAuth = new BasicScheme();
        // preemptive authentication (send credentials with request) by adding host to auth cache
        // TODO maybe use real basic auth challenge?
        authCache.put(targetHost, basicAuth);
        HttpClientContext context = HttpClientContext.create();
        context.setCredentialsProvider(credentialsProviderProvider);
        context.setAuthCache(authCache);
        return context;
    }

    @Override
    public int getTimeToLive() {
        // TODO allow overriding TTL with a plugin property
        return CONFLUENCE_IMAGES_TIME_TO_LIVE;
    }

    @Override
    public void imageChanged(String imageIdentifier) {
        if (imageIdentifier == null) {
            // all images changed, maybe because of a configuration change, thus invalidate
            // properties. Use a lazy approach that stores the timestamp of the last config
            // modification because removing properties of all users could be really expensive. The
            // modification timestamp will be checked against the modification timestamps of the
            // properties.
            // TODO in cluster this will lead to a race-condition. Better solution: move whole
            // confluence stuff to plugin, store settings as property and use last modification
            // timestamp of property
            ServiceLocator.findService(PluginPropertyManagement.class).setClientProperty(
                    getPropertyKeyGroup(), CLIENT_PROPERTY_KEY_CONFIG_MODIFIED,
                    String.valueOf(System.currentTimeMillis()));
            // re-enable if it was temporarily disabled
            enable();
        } else {
            super.imageChanged(imageIdentifier);
        }
    }

}
