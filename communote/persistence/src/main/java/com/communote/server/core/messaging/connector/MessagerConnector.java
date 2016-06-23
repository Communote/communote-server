package com.communote.server.core.messaging.connector;

import com.communote.server.api.core.blog.BlogAccessException;
import com.communote.server.api.core.blog.BlogNotFoundException;

/**
 * <p>
 * Interface for integrating several instant messagers.
 * </p>
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public interface MessagerConnector {

    /**
     * <p>
     * This method is called if a user disables this connector.
     * </p>
     */
    public void disableUser(String username);

    /**
     * <p>
     * This method is called if a user enables this connector.
     * </p>
     */
    public void enableUser(String username);

    /**
     * <p>
     * returns the blog in the messages format.
     * </p>
     * 
     * @throws BlogAccessException
     * @throws BlogNotFoundException
     */
    public String getBlogMessagerId(long blogId) throws BlogNotFoundException,
            BlogAccessException;

    /**
     * 
     */
    public String getClientId(String client);

    /**
     * <p>
     * Returns the client in the messagers format.
     * </p>
     */
    public String getClientMessagerId(String clientId);

    /**
     * <p>
     * Returns a description for this messager.
     * </p>
     */
    public String getDescription();

    /**
     * <p>
     * Returns a unique messager id.
     * </p>
     */
    public String getId();

    /**
     * <p>
     * Returns the connectors name.
     * </p>
     */
    public String getName();

    /**
     * <p>
     * This method extracts the correct user alias for this connector.
     * </p>
     */
    public String getUserAlias(String username);

    /**
     * <p>
     * Returns the user in the messagers format.
     * </p>
     */
    public String getUserMessagerId(long userId);

    /**
     * <p>
     * This method checks if the user is available or not.
     * </p>
     */
    public boolean isAvailable(String username);

    /**
     * <p>
     * 
     * @return True, if this connector is connected.
     *         </p>
     */
    public boolean isConnected();

    /**
     * <p>
     * New messages are send to the connector via this method.
     * </p>
     */
    public void sendMessage(com.communote.server.core.messaging.vo.Message message)
            throws com.communote.server.core.messaging.connector.MessagerException;

    /**
     * <p>
     * Call this to start the connector.
     * </p>
     */
    public void start();

    /**
     * <p>
     * Should be called before the connector is removed.
     * </p>
     */
    public void stop();

}