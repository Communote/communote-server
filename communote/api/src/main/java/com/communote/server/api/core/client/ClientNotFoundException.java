package com.communote.server.api.core.client;

import com.communote.server.api.core.common.NotFoundException;

/**
 * Exception to be thrown if a client was not found
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class ClientNotFoundException extends NotFoundException {

    private static final long serialVersionUID = 1L;
    private String clientId;

    /**
     * Create a new exception
     *
     * @param clientId
     *            the string ID of the client that was not found
     */
    public ClientNotFoundException(String clientId) {
        this(clientId, "Client with ID " + clientId + " was not found");
    }

    /**
     * Create a new exception with detail message
     *
     * @param clientId
     *            the string ID of the client that was not found
     * @param message
     *            the detail message
     */
    public ClientNotFoundException(String clientId, String message) {
        super(message);
        this.clientId = clientId;
    }

    public ClientNotFoundException(String clientId, String message, Throwable cause) {
        super(message, cause);
        this.clientId = clientId;
    }

    /**
     * @return the string ID of the client that was not found. Can be null if not set.
     */
    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }
}
