package com.communote.server.core.security.ssl;

import java.util.List;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.common.ClientAndChannelContextHolder;
import com.communote.server.core.common.caching.Cache;
import com.communote.server.core.common.caching.CacheManager;
import com.communote.server.core.common.caching.ClientSingleElementCacheKey;
import com.communote.server.model.security.ChannelConfiguration;
import com.communote.server.model.security.ChannelType;

/**
 * @see com.communote.server.core.security.ssl.ChannelManagement
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ChannelManagementImpl extends
        com.communote.server.core.security.ssl.ChannelManagementBase {

    /** The cache provider for all channels. */
    private final ChannelConfigurationCacheElementProvider cacheProv = new ChannelConfigurationCacheElementProvider();

    /**
     * Drop cache for a channels.
     */
    private synchronized void dropCache() {
        Cache cache = ServiceLocator.findService(CacheManager.class).getCache();
        if (cache != null) {
            ClientSingleElementCacheKey key = new ClientSingleElementCacheKey();
            cache.invalidate(key, cacheProv);
        }
    }

    /**
     * Gets the list of channel configuration objects from database or cache.
     * 
     * @return the channel list
     */
    public synchronized ChannelConfiguration[] getChannelConfigurations() {
        Cache cache = ServiceLocator.findService(CacheManager.class).getCache();
        ClientSingleElementCacheKey key = new ClientSingleElementCacheKey();
        if (cache == null) {
            return cacheProv.load(key);
        }
        return cache.get(key, cacheProv);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Boolean handleIsForceSsl() {
        return isForceSsl(null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Boolean handleIsForceSsl(ChannelType channelType) {

        if (channelType == null) {
            channelType = ClientAndChannelContextHolder.getChannel(ChannelType.WEB);
        }
        if (channelType.equals(ChannelType.XMPP)) {
            // the XMPP channel is an API channel and cannot be configured separately, thus we use
            // the API channel
            channelType = ChannelType.API;
        }

        Boolean isForceSsl = Boolean.FALSE;
        ChannelConfiguration[] channels = getChannelConfigurations();

        for (ChannelConfiguration channel : channels) {
            if (channelType.equals(channel.getChannelType())) {
                isForceSsl = channel.getForceSsl();
                break;
            }
        }

        return isForceSsl;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<ChannelConfiguration> handleLoadAll() {
        return (List<ChannelConfiguration>) getChannelConfigurationDao().loadAll();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void handleUpdate(List<ChannelConfiguration> newChannelConfig) {
        // truncate table
        getChannelConfigurationDao().remove(getChannelConfigurationDao().loadAll());

        // create new records
        for (ChannelConfiguration channel : newChannelConfig) {
            getChannelConfigurationDao().create(channel);
        }

        // invalidate the cache
        dropCache();
    }
}