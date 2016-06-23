package com.communote.plugins.mq.provider.activemq.user;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;

import com.communote.plugins.core.services.PluginPropertyService;
import com.communote.plugins.core.services.PluginPropertyServiceException;
import com.communote.server.api.core.security.AuthorizationException;

/**
 * DAO component.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
@Component
@Provides
@Instantiate
public class MQDatabaseUsersDAO implements MQUsersDAO {

    private static final String PASSWORD = "password";

    private static final String ACTIVEMQ_PROVIDER_PLUGIN_SECURITY_PREFIX = "plugin.mq.provider.activemq.security";

    private static final String ROLES = "roles";

    private static final String USERS = "users";

    /**
     * @param userName
     *            user name
     * @return setting key, expressing password of userName
     */
    public static String getUserPasswordSettingKey(String userName) {
        return ACTIVEMQ_PROVIDER_PLUGIN_SECURITY_PREFIX + "." + userName + "."
                + PASSWORD;
    }

    /**
     * @param userName
     *            user name
     * @return setting key, expressing roles of userName
     */
    public static String getUserRolesSettingKey(String userName) {
        return ACTIVEMQ_PROVIDER_PLUGIN_SECURITY_PREFIX + "." + userName + "."
                + ROLES;
    }

    /**
     * @return setting key, expressing users of the system
     */
    public static String getUsersSettingKey() {
        return ACTIVEMQ_PROVIDER_PLUGIN_SECURITY_PREFIX + "." + USERS;
    }

    @Requires
    private PluginPropertyService pluginPropertyService;

    @Override
    public void addUser(String userName, String password)
            throws PluginPropertyServiceException, AuthorizationException {
        String existingUsers = pluginPropertyService
                .getApplicationPropertyWithDefault(getUsersSettingKey(), "");
        pluginPropertyService.setApplicationProperty(getUsersSettingKey(),
                existingUsers.equals("") ? userName : existingUsers + ","
                        + userName);
        pluginPropertyService.setApplicationProperty(
                getUserPasswordSettingKey(userName), DigestUtils.sha512Hex(password));
        pluginPropertyService.setApplicationProperty(
                getUserRolesSettingKey(userName), "users");
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.communote.plugins.mq.provider.activemq.user.MQUsersDAO#getMQUser(String)
     */
    @Override
    public MQUser getMQUser(String userName)
            throws PluginPropertyServiceException {
        String password = pluginPropertyService
                .getApplicationProperty(getUserPasswordSettingKey(userName));
        String roles = pluginPropertyService
                .getApplicationProperty(getUserRolesSettingKey(userName));
        if (password != null && roles != null) {
            MQUser user = new MQUser();
            user.setName(userName);
            user.setPassword(password.toCharArray());
            user.setRoles(new HashSet<String>(Arrays.asList(roles.split(","))));
            return user;
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.communote.plugins.mq.provider.activemq.user.MQUsersDAO#getMQUsers()
     */
    @Override
    public List<String> getMQUsers() throws PluginPropertyServiceException {
        String[] users = pluginPropertyService
                .getApplicationPropertyWithDefault(getUsersSettingKey(), "")
                .split(",");
        if (users.length != 0 && !users[0].equals("")) {
            return Arrays.asList(users);
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public void removeUser(String userName)
            throws PluginPropertyServiceException, AuthorizationException {
        String[] existingUsers = pluginPropertyService
                .getApplicationPropertyWithDefault(getUsersSettingKey(), "")
                .split(",");
        if (existingUsers.length == 0 || existingUsers[0].equals("")) {
            return;
        }
        StringBuilder newUsersValue = new StringBuilder();
        for (String existingUser : existingUsers) {
            if (!existingUser.equals(userName)) {
                newUsersValue.append("," + existingUser);
            }
        }
        pluginPropertyService.setApplicationProperty(getUsersSettingKey(),
                newUsersValue.length() > 0 ? newUsersValue.substring(1) : "");
        pluginPropertyService.setApplicationProperty(
                getUserPasswordSettingKey(userName), null);
        pluginPropertyService.setApplicationProperty(
                getUserRolesSettingKey(userName), null);
    }
}
