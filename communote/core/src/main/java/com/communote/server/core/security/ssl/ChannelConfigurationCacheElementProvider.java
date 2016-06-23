package com.communote.server.core.security.ssl;

import java.util.ArrayList;
import java.util.List;

import com.communote.server.api.ServiceLocator;
import com.communote.server.core.common.caching.CacheElementProvider;
import com.communote.server.core.common.caching.ClientSingleElementCacheKey;
import com.communote.server.core.security.ssl.ChannelManagement;
import com.communote.server.model.security.ChannelConfiguration;


/**
 * Cache Provider for Channel Configurations
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class ChannelConfigurationCacheElementProvider implements
        CacheElementProvider<ClientSingleElementCacheKey, ChannelConfiguration[]> {

    private final static String CONTENT_TYPE = "channel_configuration";
    private final static int TIME_TO_LIVE = 3600;

    /**
     * {@inheritDoc}
     */
    public String getContentType() {
        return CONTENT_TYPE;
    }

    /**
     * {@inheritDoc}
     */
    public int getTimeToLive() {
        return TIME_TO_LIVE;
    }

    /**
     * {@inheritDoc}
     */
    public ChannelConfiguration[] load(ClientSingleElementCacheKey key) {
        ChannelManagement cm = ServiceLocator.findService(ChannelManagement.class);
        List<ChannelConfiguration> channels = new ArrayList<ChannelConfiguration>();

        channels = cm.loadAll();

        return channels.toArray(new ChannelConfiguration[] { });
    }
}
