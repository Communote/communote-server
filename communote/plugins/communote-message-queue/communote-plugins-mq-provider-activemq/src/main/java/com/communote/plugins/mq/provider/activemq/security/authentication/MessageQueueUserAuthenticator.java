package com.communote.plugins.mq.provider.activemq.security.authentication;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.security.auth.login.LoginException;

import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.communote.plugins.core.services.PluginPropertyService;
import com.communote.plugins.core.services.PluginPropertyServiceException;
import com.communote.plugins.mq.provider.activemq.user.MQDatabaseUsersDAO;
import com.communote.plugins.mq.provider.activemq.user.MQUser;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class MessageQueueUserAuthenticator implements Authenticator {

    /** Logger. */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(MessageQueueUserAuthenticator.class);
    private final PluginPropertyService pluginPropertyService;

    /**
     * Constructor.
     * 
     * @param pluginPropertyService
     *            The plugins property service.
     */
    public MessageQueueUserAuthenticator(PluginPropertyService pluginPropertyService) {
        this.pluginPropertyService = pluginPropertyService;
    }

    @Override
    public Set<String> authenticate(String username, String password) throws LoginException {
        try {
            MQUser mqUser = getMQUser(username);
            if (mqUser == null) {
                LOGGER.debug("Login failed for user {}", username);
                throw new LoginException("User " + username + " is unkown.");
            }
            char[] correctPass = mqUser.getPassword();
            password = DigestUtils.sha512Hex(password);
            if (!Arrays.equals(correctPass, password.toCharArray())) {
                LOGGER.debug("Login failed for user {}", username);
                throw new LoginException("Password for " + username + " does not match");
            }
            return mqUser.getRoles();
        } catch (PluginPropertyServiceException e) {
            throw new LoginException(e.getMessage());
        }
    }

    /**
     * Get the user for the username. Assume everything may fail.
     * 
     * @param userName
     *            name of the user
     * @return stored MQ user or null if the user does not exist
     * @throws PluginPropertyServiceException
     *             exception
     */
    private MQUser getMQUser(String userName)
            throws PluginPropertyServiceException {
        String settingsPasswordKey;
        String settingsPassword = null;
        char[] password = null;
        String rolesAsString = null;
        String[] roles = null;
        MQUser user = null;

        settingsPasswordKey = MQDatabaseUsersDAO.getUserPasswordSettingKey(userName);
        if (settingsPasswordKey != null) {
            settingsPassword = pluginPropertyService.getApplicationProperty(
                    settingsPasswordKey);
        }
        if (settingsPassword != null) {
            password = settingsPassword.toCharArray();
        }
        if (password != null) {
            rolesAsString = pluginPropertyService.getApplicationProperty(
                    MQDatabaseUsersDAO.getUserRolesSettingKey(userName));
        }
        if (rolesAsString != null) {
            roles = rolesAsString.split(",");
        }

        if (password != null && roles != null) {
            user = new MQUser();
            user.setName(userName);
            user.setPassword(password);
            user.setRoles(new HashSet<String>(Arrays.asList(roles)));
        }
        return user;
    }

}
