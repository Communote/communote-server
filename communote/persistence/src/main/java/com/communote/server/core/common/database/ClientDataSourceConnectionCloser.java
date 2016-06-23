package com.communote.server.core.common.database;

import com.communote.server.api.core.client.ClientTO;

/**
 * Interface for closing the data source connection of the provider
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public interface ClientDataSourceConnectionCloser {

    /**
     * Close the data source of the client
     * 
     * @param client
     *            the client
     */
    void close(ClientTO client);

}
