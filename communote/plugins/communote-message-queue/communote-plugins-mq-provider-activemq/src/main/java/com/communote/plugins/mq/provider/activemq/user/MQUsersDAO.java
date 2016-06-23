package com.communote.plugins.mq.provider.activemq.user;

import java.util.List;

import com.communote.plugins.core.services.PluginPropertyServiceException;
import com.communote.server.api.core.security.AuthorizationException;

/**
 * Interface for configurating the users who can access the message queue broker
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public interface MQUsersDAO {

    /**
     * @param userName
     *            user name
     * @param password
     *            user password
     * @throws PluginPropertyServiceException
     *             exception
     */
    public void addUser(String userName, String password)
            throws PluginPropertyServiceException, AuthorizationException;

    /**
     * 
     * @param userName
     *            user name
     * @return user with the specified userName or null if such a user does not exist
     * @throws PluginPropertyServiceException
     *             exception
     */
    public MQUser getMQUser(String userName)
            throws PluginPropertyServiceException;

    /**
     * @return list of registered users
     * @throws PluginPropertyServiceException
     *             exception
     */
    public List<String> getMQUsers() throws PluginPropertyServiceException;

    /**
     * @param userName
     *            user name
     * @throws PluginPropertyServiceException
     *             exception
     */
    public void removeUser(String userName)
            throws PluginPropertyServiceException, AuthorizationException;

}