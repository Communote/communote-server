package com.communote.server.api.core.client;

import com.communote.server.api.core.event.Event;
import com.communote.server.model.client.ClientStatus;

/**
 * Event which signals a change of the status of a client which is not the global client. This is
 * especially useful for multi-tenant versions of Communote to be informed about the activation of a
 * new client/tenant.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ClientStatusChangeEvent implements Event {

    private static final long serialVersionUID = -2917261416215984643L;

    private final ClientStatus oldStatus;
    private final ClientStatus newStatus;
    private final String clientId;

    /**
     * Constructor.
     *
     * @param clientId
     *            The ID if the client whose status changed
     * @param oldStatus
     *            The original status of the client or null, if none.
     * @param newStatus
     *            The new status of the client.
     */
    public ClientStatusChangeEvent(String clientId, ClientStatus oldStatus, ClientStatus newStatus) {
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.clientId = clientId;
    }

    /**
     * @return the ID of the client
     */
    public String getClientId() {
        return clientId;
    }

    /**
     * @return the new status of the client
     */
    public ClientStatus getNewStatus() {
        return newStatus;
    }

    /**
     * @return the old status of the client. Can be null.
     */
    public ClientStatus getOldStatus() {
        return oldStatus;
    }
}
