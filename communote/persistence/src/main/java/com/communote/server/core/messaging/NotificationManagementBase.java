package com.communote.server.core.messaging;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 * Spring Service base class for
 * <code>MessagingManagement</code>, provides access to all
 * services and entities referenced by this service.
 * </p>
 * 
 * @see com.communote.server.core.messaging.NotificationManagement
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
@Transactional(propagation = Propagation.REQUIRED)
public abstract class NotificationManagementBase implements NotificationManagement {

    /**
     * {@inheritDoc}
     */
    public void disableUser(long userId, String connectorId) {
        if (connectorId == null || connectorId.trim().length() == 0) {
            throw new IllegalArgumentException(
                    "disableUser(long userId, String connectorId) - 'connectorId' can not be null or empty");
        }
        try {
            this.handleDisableUser(userId, connectorId);
        } catch (RuntimeException rt) {
            throw new com.communote.server.core.messaging.NotificationManagementException(
                    "Error performing 'disableUser(long userId, String connectorId)' --> "
                            + rt, rt);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void enableUser(long userId, String connectorId) {
        if (connectorId == null || connectorId.trim().length() == 0) {
            throw new IllegalArgumentException(
                    "enableUser(long userId, String connectorId) - 'connectorId' can not be null or empty");
        }
        try {
            this.handleEnableUser(userId, connectorId);
        } catch (RuntimeException rt) {
            throw new com.communote.server.core.messaging.NotificationManagementException(
                    "Error performing 'enableUser(long userId, String connectorId)' --> "
                            + rt, rt);
        }
    }

    /**
     * {@inheritDoc}
     */
    public String getClientId(String client, String connectorId) {
        if (client == null || client.trim().length() == 0) {
            throw new IllegalArgumentException(
                    "getClientId(String client, String connectorId) - 'client' can not be null or empty");
        }
        if (connectorId == null || connectorId.trim().length() == 0) {
            throw new IllegalArgumentException(
                    "getClientId(String client, String connectorId) - 'connectorId' can not be null or empty");
        }
        try {
            return this.handleGetClientId(client, connectorId);
        } catch (RuntimeException rt) {
            throw new com.communote.server.core.messaging.NotificationManagementException(
                    "Error performing 'getClientId(String client, String connectorId)' --> "
                            + rt, rt);
        }
    }

    /**
     * Gets the current <code>principal</code> if one has been set, otherwise returns
     * <code>null</code>.
     * 
     * @return the current principal
     */
    protected java.security.Principal getPrincipal() {
        return com.communote.server.PrincipalStore.get();
    }

    /**
     * {@inheritDoc}
     */
    @Transactional(propagation = Propagation.SUPPORTS)
    public String getUserAlias(String username, String connectorId) {
        if (username == null || username.trim().length() == 0) {
            throw new IllegalArgumentException(
                    "getUserAlias(String username, String connectorId) - 'username' can not be null or empty");
        }
        if (connectorId == null || connectorId.trim().length() == 0) {
            throw new IllegalArgumentException(
                    "getUserAlias(String username, String connectorId) - 'connectorId' can not be null or empty");
        }
        try {
            return this.handleGetUserAlias(username, connectorId);
        } catch (RuntimeException rt) {
            throw new com.communote.server.core.messaging.NotificationManagementException(
                    "Error performing 'getUserAlias(String username, String connectorId)' --> "
                            + rt, rt);
        }
    }

    /**
     * Performs the core logic for {@link #disableUser(long, String)}
     * 
     * @param userId
     *            Id of the user.
     * @param connectorId
     *            Id of the connector.
     */
    protected abstract void handleDisableUser(long userId, String connectorId);

    /**
     * Performs the core logic for {@link #enableUser(long, String)}
     * 
     * @param userId
     *            Id of the user.
     * @param connectorId
     *            Id of the connector.
     */
    protected abstract void handleEnableUser(long userId, String connectorId);

    /**
     * Performs the core logic for {@link #getClientId(String, String)}
     * 
     * @param client
     *            The client.
     * @param connectorId
     *            Id of the connector.
     * @return Id for the client within the connectors context.
     */
    protected abstract String handleGetClientId(String client, String connectorId);

    /**
     * Performs the core logic for {@link #getUserAlias(String, String)}
     * 
     * @param username
     *            The users name.
     * @param connectorId
     *            Id of the connector.
     * @return Id for the user within the connectors context.
     */
    protected abstract String handleGetUserAlias(String username, String connectorId);

    /**
     * Performs the core logic for {@link #removeMessagerConnector(String)}
     * 
     * @param connectorId
     *            Id of the connector.
     */
    protected abstract void handleRemoveMessagerConnector(String connectorId);

    /**
     * {@inheritDoc}
     */
    public void removeMessagerConnector(String connectorId) {
        if (connectorId == null || connectorId.trim().length() == 0) {
            throw new IllegalArgumentException(
                    "removeMessagerConnector(String connectorId) - 'connectorId' can not be null or empty");
        }
        try {
            this.handleRemoveMessagerConnector(connectorId);
        } catch (RuntimeException rt) {
            throw new com.communote.server.core.messaging.NotificationManagementException(
                    "Error performing 'removeMessagerConnector(String connectorId)' --> "
                            + rt, rt);
        }
    }
}