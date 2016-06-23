package com.communote.server.service;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.client.ClientDelegate;
import com.communote.server.api.core.client.ClientNotFoundException;
import com.communote.server.api.core.client.ClientTO;
import com.communote.server.api.core.client.GlobalClientDelegateCallback;
import com.communote.server.core.common.caching.CacheElementProvider;
import com.communote.server.core.common.caching.CacheElementProviderException;
import com.communote.server.core.common.caching.StringIdBasedCacheKey;
import com.communote.server.core.general.RunInTransactionWithResult;
import com.communote.server.core.general.TransactionException;
import com.communote.server.core.general.TransactionManagement;
import com.communote.server.core.general.TransactionManagementException;
import com.communote.server.core.user.client.ClientManagement;

/**
 * Cache provider for caching clients in a standalone environment.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class StandaloneClientCacheElementProvider implements
CacheElementProvider<StringIdBasedCacheKey, ClientTO> {

    /**
     * Need this for avoiding LazyInitializationExceptions.
     */
    private class ClientRunInTransaction implements RunInTransactionWithResult<ClientTO> {

        private final String clientId;

        /**
         * @param clientId
         *            The clients id, this is for.
         */
        public ClientRunInTransaction(String clientId) {
            this.clientId = clientId;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public ClientTO execute() throws TransactionException {
            ClientManagement clientManagement = ServiceLocator.findService(ClientManagement.class);
            ClientTO client;
            try {
                client = clientManagement.findClient(clientId);
            } catch (ClientNotFoundException e) {
                throw new TransactionException(e.getMessage(), e);
            }
            return client;
        }
    }

    /**
     * @return "communote/client"
     */
    @Override
    public String getContentType() {
        return "communote/client";
    }

    @Override
    public int getTimeToLive() {
        return 18000;
    }

    @Override
    public ClientTO load(final StringIdBasedCacheKey key) throws CacheElementProviderException {
        try {
            return new ClientDelegate().execute(new GlobalClientDelegateCallback<ClientTO>() {
                @Override
                public ClientTO doOnGlobalClient() throws Exception {
                    ClientRunInTransaction runInTransaction = new ClientRunInTransaction(key
                            .getId());
                    return ServiceLocator.findService(TransactionManagement.class).execute(
                            runInTransaction);
                }
            });
        } catch (TransactionManagementException e) {
            throw new CacheElementProviderException(e.getMessage(), e.getCause());
        } catch (Throwable e) {
            throw new CacheElementProviderException("Unknown error on calling findClient", e);
        }
    }
}
