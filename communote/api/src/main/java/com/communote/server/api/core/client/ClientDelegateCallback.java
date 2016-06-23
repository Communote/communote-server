package com.communote.server.api.core.client;


/**
 * Callback to execute operations on the desired client.
 *
 * @see com.communote.server.api.core.client.ClientDelegate
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 * @param <T>
 *            Type of the result.
 */
public interface ClientDelegateCallback<T> {

    /**
     * Execute the method on the desired database.
     *
     * @param client
     *            the current client (optional, can be null in global context)
     * @return optional result
     * @throws Exception
     *             in case of an error
     */
    public T doOnClient(ClientTO client) throws Exception;
}
