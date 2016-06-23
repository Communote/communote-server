package com.communote.server.api.core.client;

/**
 * Callback to execute operations on the global database.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 * @param <T>
 *            Parameter of the returning result.
 */
public interface GlobalClientDelegateCallback<T> {

    /**
     * Do on global database.
     *
     * @return optional result
     * @throws Exception
     *             in case of an error
     */
    public T doOnGlobalClient() throws Exception;
}
