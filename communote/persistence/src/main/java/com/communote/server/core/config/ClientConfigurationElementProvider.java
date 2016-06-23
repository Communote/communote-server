package com.communote.server.core.config;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.config.ClientConfigurationProperties;
import com.communote.server.core.ConfigurationManagement;
import com.communote.server.core.common.caching.CacheElementProvider;
import com.communote.server.core.common.caching.ClientSingleElementCacheKey;
import com.communote.server.model.config.Configuration;


/**
 * Cache element provider for the client configuration properties
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ClientConfigurationElementProvider implements
        CacheElementProvider<ClientSingleElementCacheKey, ClientConfigurationProperties> {

    /**
     * @return the configuration management
     */
    private ConfigurationManagement getConfigurationManagement() {
        return ServiceLocator.findService(ConfigurationManagement.class);
    }

    /**
     * {@inheritDoc}
     */
    public String getContentType() {
        return "clientConfiguration";
    }

    /**
     * {@inheritDoc}
     */
    public int getTimeToLive() {
        // an hour as workaround to recognize changes when setting a value directly on the DB
        return 3600;
    }

    /**
     * {@inheritDoc}
     */
    public ClientConfigurationProperties load(ClientSingleElementCacheKey key) {
        Configuration clientConfig = getConfigurationManagement().getConfiguration();
        return new ClientConfigurationProperties(clientConfig);
    }
}
