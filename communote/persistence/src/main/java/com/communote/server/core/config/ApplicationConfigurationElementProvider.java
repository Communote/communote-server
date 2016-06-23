package com.communote.server.core.config;

import java.util.Collection;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.client.ClientDelegate;
import com.communote.server.api.core.client.GlobalClientDelegateCallback;
import com.communote.server.api.core.config.ApplicationConfigurationProperties;
import com.communote.server.core.ConfigurationManagement;
import com.communote.server.core.ConfigurationManagementException;
import com.communote.server.core.common.caching.ApplicationSingleElementCacheKey;
import com.communote.server.core.common.caching.CacheElementProvider;
import com.communote.server.model.config.ApplicationConfigurationSetting;

/**
 * Provider for retrieving the ApplicationConfigurationProperties
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ApplicationConfigurationElementProvider implements
        CacheElementProvider<ApplicationSingleElementCacheKey, ApplicationConfigurationProperties> {

    // An hour as default to recognize changes when setting a value directly on the DB
    private final static int TIME_TO_LIVE = Integer.getInteger("com.communote.cache.ttl."
            + ApplicationConfigurationElementProvider.class.getCanonicalName(), 3600);

    /**
     * {@inheritDoc}
     */
    @Override
    public String getContentType() {
        return "applicationConfig";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getTimeToLive() {
        return TIME_TO_LIVE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ApplicationConfigurationProperties load(final ApplicationSingleElementCacheKey key) {
        try {
            return new ClientDelegate()
                    .execute(new GlobalClientDelegateCallback<ApplicationConfigurationProperties>() {

                        @Override
                        public ApplicationConfigurationProperties doOnGlobalClient()
                        throws Exception {
                            Collection<ApplicationConfigurationSetting> settings = ServiceLocator
                                    .findService(ConfigurationManagement.class)
                                    .getApplicationConfigurationSettings();
                            return new ApplicationConfigurationProperties(settings);
                        }
                    });
        } catch (Exception e) {
            throw new ConfigurationManagementException(
                    "Unkown error while getting application configuration properties: "
                            + e.getMessage(), e);
        }

    }

}
