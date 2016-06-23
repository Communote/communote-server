package com.communote.server.core.messaging;

import java.util.Collection;
import java.util.Map;

import com.communote.server.model.note.Note;
import com.communote.server.model.user.User;

/**
 * NotificationManagement
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public interface NotificationManagement {

    /**
     * This method is called when a user disables a connector.
     * 
     * @param userId
     *            The users id.
     * @param connectorId
     *            The connectors id.
     */
    public void disableUser(long userId, String connectorId);

    /**
     * This method is called if a user enables a connector.
     * 
     * @param userId
     *            The users id.
     * @param connectorId
     *            The connectors id.
     */
    public void enableUser(long userId, String connectorId);

    /**
     * @param client
     *            The client.
     * @param connectorId
     *            The connectors id.
     * 
     * @return the client id dependent to the connector type.
     */
    public String getClientId(String client, String connectorId);

    /**
     * @param username
     *            Name of the user.
     * @param connectorId
     *            Id of the connector.
     * @return the user alias from the specific messager connector format.
     */
    public String getUserAlias(String username, String connectorId);

    /**
     * @return the jabber id the user has to use to connect to, e.g.
     *         tlu.comunardo@jabber.communote.com
     */
    public String getXMPPId();

    /**
     * @param connector
     *            Connector to register.
     */
    public void registerMessagerConnector(
            com.communote.server.core.messaging.connector.MessagerConnector connector);

    /**
     * @param connectorId
     *            Id of the connector to unregister.
     * 
     */
    public void removeMessagerConnector(String connectorId);

    /**
     * Send notifications to users to inform about created or edited notes.
     * 
     * @param noteId
     *            Id of the note, which should be send. the note to inform about
     * @param userToNotify
     *            Id of the user to notify.
     * @param notificationDefinition
     *            The current definition of the notification.
     * @param model
     *            Additional elements used for the velocity context.
     */
    public void sendMessage(Long noteId, Long userToNotify,
            NotificationDefinition notificationDefinition, Map<String, Object> model);

    /**
     * Send notifications to users to inform about created or edited notes.
     * 
     * @param note
     *            the note to inform about
     * @param usersToNotify
     *            the users to be notified
     * @param notificationDefinition
     *            The current definition of the notification.
     */
    public void sendMessage(Note note, Collection<User> usersToNotify,
            NotificationDefinition notificationDefinition);

    /**
     * <p>
     * Use this method to initialise the management.
     */
    public void start();

    /**
     * <p>
     * Use this method to stop the management.
     * </p>
     */
    public void stop();

}
