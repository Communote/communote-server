package com.communote.server.service;

import java.util.Collection;
import java.util.Iterator;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.client.ClientDelegate;
import com.communote.server.api.core.client.ClientNotFoundException;
import com.communote.server.api.core.client.ClientTO;
import com.communote.server.api.core.client.GlobalClientDelegateCallback;
import com.communote.server.api.service.ClientRetrievalService;
import com.communote.server.core.common.caching.Cache;
import com.communote.server.core.common.caching.CacheElementProvider;
import com.communote.server.core.common.caching.CacheManager;
import com.communote.server.core.common.caching.StringIdBasedCacheKey;
import com.communote.server.core.user.client.ClientManagement;
import com.communote.server.core.user.client.ClientManagementException;
import com.communote.server.model.client.ClientStatus;

/**
 * Service for finding existing clients.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
@Service("clientRetrievalService")
public class ClientRetrievalServiceImpl implements ClientRetrievalService {

    private CacheElementProvider<StringIdBasedCacheKey, ClientTO> clientCacheElementProvider;
    @Autowired
    private CacheManager cacheManager;

    // TODO better use an event?
    @Override
    public void clientChanged(String clientId) {
        cacheManager.getCache().invalidate(createCacheKey(clientId), clientCacheElementProvider);
    }

    private StringIdBasedCacheKey createCacheKey(String clientId) {
        return new StringIdBasedCacheKey(clientId, this.getClass().getName());
    }

    @Override
    public ClientTO findClient(final String clientId) throws ClientNotFoundException {
        Cache cache = cacheManager.getCache();
        ClientTO client = cache.get(createCacheKey(clientId), clientCacheElementProvider);
        if (client == null) {
            throw new ClientNotFoundException(clientId);
        }
        return client;
    }

    @Override
    public Collection<ClientTO> getAllActiveClients() {
        Collection<ClientTO> clients = getAllClients();
        for (Iterator<ClientTO> iterator = clients.iterator(); iterator.hasNext();) {
            ClientTO client = iterator.next();
            if (!ClientStatus.ACTIVE.equals(client.getClientStatus())) {
                iterator.remove();
            }
        }
        return clients;
    }

    @Override
    public Collection<ClientTO> getAllClients() {
        Collection<ClientTO> result = null;
        try {
            result = new ClientDelegate()
            .execute(new GlobalClientDelegateCallback<Collection<ClientTO>>() {
                @Override
                public Collection<ClientTO> doOnGlobalClient() {
                    return ServiceLocator.findService(ClientManagement.class)
                                    .getAllClients();
                }
            });
        } catch (Exception e) {
            throw new ClientManagementException("unknown error on calling getAllClients", e);
        }
        return result;
    }

    @PostConstruct
    private void init() {
        // create default cache element provider
        if (clientCacheElementProvider == null) {
            clientCacheElementProvider = new StandaloneClientCacheElementProvider();
        }
    }

    /**
     * Set a custom cache element provider for loading clients within findClient. The cache key is
     * {@link StringIdBasedCacheKey} whose ID member holds the clientId.
     *
     * @param cacheElementProvider
     *            the element provider
     */
    public void setCacheElementProvider(
            CacheElementProvider<StringIdBasedCacheKey, ClientTO> cacheElementProvider) {
        this.clientCacheElementProvider = cacheElementProvider;
    }

}
