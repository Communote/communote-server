package com.communote.server.api.core.client;

/**
 * Exception to be thrown that a client ID is not valid.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class InvalidClientIdException extends Exception {

    private static final long serialVersionUID = 1L;
    private String clientId;

    public InvalidClientIdException(String clientId) {
        super("The client ID '" + clientId == null ? "" : clientId + "'is not valid");
        setClientId(clientId);

    }

    public InvalidClientIdException(String clientId, String message) {
        super(message);
        this.setClientId(clientId);
    }

    public InvalidClientIdException(String clientId, String message, Throwable cause) {
        super(message, cause);
        this.setClientId(clientId);
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }
}
