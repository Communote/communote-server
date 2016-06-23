package com.communote.server.core.user.client;

import com.communote.server.api.core.client.ClientNotFoundException;
import com.communote.server.api.core.client.ClientTO;
import com.communote.server.api.core.client.InvalidClientIdException;
import com.communote.server.api.core.user.UserVO;

/**
 * Management class for client handling.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public interface ClientManagement {

    /**
     * <p>
     * Create the global client (as entity with db config only)
     * </p>
     */
    public void createGlobalClient(String clientName, String timeZoneId)
            throws InvalidClientIdException;

    /**
     * <p>
     * Get the client by its client id
     * </p>
     */
    public ClientTO findClient(String clientId) throws ClientNotFoundException;

    /**
     *
     */
    public java.util.Collection<ClientTO> getAllClients();

    /**
     * <p>
     * Initializes the global client.
     *
     * @param userVO
     *            VO with details of the user to create as administrator for the client
     *            </p>
     */
    public void initializeGlobalClient(UserVO userVO)
            throws com.communote.server.api.core.common.EmailValidationException;

    /**
     * Update the name of the given client
     *
     * @param clientId
     *            the ID of the client to update
     * @param clientName
     *            the new name
     * @return the update client
     * @throws ClientNotFoundException
     *             in case there is no client with the ID
     */
    public ClientTO updateClientName(String clientId, String clientName)
            throws ClientNotFoundException;

}
