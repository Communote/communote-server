package com.communote.server.api.service;

import java.util.Collection;

import com.communote.server.api.core.client.ClientNotFoundException;
import com.communote.server.api.core.client.ClientTO;

/**
 * Service for finding existing clients.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public interface ClientRetrievalService {

    /**
     * Notify the service that fields of a client changed.
     *
     * @param clientId
     *            the ID of the changed client
     */
    // TODO better use an event?
    public void clientChanged(String clientId);

    /**
     * Find a client by its unique string-based ID
     *
     * @param clientId
     *            the client id
     * @return the client
     * @throws ClientNotFoundException
     *             in case there is no client with the given ID
     */
    public ClientTO findClient(String clientId) throws ClientNotFoundException;

    /**
     * Get all clients with status ACTIVE
     *
     * @return all active clients
     */
    public Collection<ClientTO> getAllActiveClients();

    /**
     * Get all clients
     *
     * @return the all clients
     */
    public Collection<ClientTO> getAllClients();

}