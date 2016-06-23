package com.communote.server.api.core.client;

import com.communote.server.api.core.common.ClientAndChannelContextHolder;

/**
 * Delegator which offers an abstract solution to execute operations on the desired client.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class ClientDelegate {

    /** The client (optional). */
    private ClientTO client = null;

    /**
     * Instantiates a new delegator and delegates the operations to the global database.
     */
    public ClientDelegate() {
        // Do nothing.
    }

    /**
     * Instantiates a new delegator and delegates the operations to the client database.
     *
     * @param client
     *            the client
     */
    public ClientDelegate(ClientTO client) {
        this.client = client;
    }

    /**
     * Executes operations of the callback, makes sure that we are on the right client (client or
     * global).
     *
     * @param callback
     *            the callback operations
     * @param <T>
     *            Type of the result.
     * @return a object (optional)
     * @throws Exception
     *             in case of an error
     */
    public <T> T execute(ClientDelegateCallback<T> callback) throws Exception {
        if (callback == null) {
            throw new IllegalArgumentException("callback is null");
        }
        ClientTO current = ClientAndChannelContextHolder.getClient();
        try {
            ClientAndChannelContextHolder.setClient(this.client);
            return callback.doOnClient(this.client);
        } finally {
            ClientAndChannelContextHolder.setClient(current);
        }
    }

    /**
     * Executes operations of the callback on the global client.
     *
     * @param callback
     *            the callback
     * @param <T>
     *            Type of the result.
     * @return the result
     * @throws Exception
     *             in case of an error
     */
    public <T> T execute(GlobalClientDelegateCallback<T> callback) throws Exception {
        if (callback == null) {
            throw new IllegalArgumentException("callback is null");
        }
        ClientTO current = ClientAndChannelContextHolder.getClient();
        try {
            // Use null because it is treated as global client. We cannot set the real global client
            // here as it might require a DB lookup on the global client -> endless recursion.
            ClientAndChannelContextHolder.setClient(null);
            return callback.doOnGlobalClient();
        } finally {
            ClientAndChannelContextHolder.setClient(current);
        }
    }
}
