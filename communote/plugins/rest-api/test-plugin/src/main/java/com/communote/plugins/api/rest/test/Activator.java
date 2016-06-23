package com.communote.plugins.api.rest.test;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContext;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.property.PropertyManagement;
import com.communote.server.api.core.property.PropertyType;
import com.communote.server.core.security.AuthenticationHelper;
import com.communote.server.core.user.UserGroupManagement;
import com.communote.server.core.user.UserManagement;
import com.communote.server.core.vo.user.group.ExternalGroupVO;
import com.communote.server.persistence.user.ExternalUserVO;
import com.communote.server.model.user.User;
import com.communote.server.model.user.UserRole;
import com.communote.server.model.user.UserStatus;
import com.communote.server.service.UserPreferenceService;

/**
 * Activator, which adds a valid key group and key to the PropertyManagement. These than can be used
 * for tests.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class Activator implements BundleActivator {

    private static final String KEY_GROUP = "com.communote.rest.test";
    private static final String KEY = "rest-test";
    private static final String EXTERNAL_SYSTEM_ID = "ExternalRestApi";
    private static final String[] EXTERNAL_USERS = { "ExternalRestApiUser1", "ExternalRestApiUser2" };
    private static final String[] EXTERNAL_GROUPS = { "ExternalRestApiGroup1",
            "ExternalRestApiGroup2" };
    private final Set<Long> registeredUsers = new HashSet<Long>();
    private final Set<Long> registeredGroups = new HashSet<Long>();

    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(Activator.class);

    /**
     * Creates needed external users and groups.
     */
    private void addExternalUsersAndGroups() {
        UserManagement userManagement = ServiceLocator.instance().getService(UserManagement.class);
        // TODO this is ugly! BE should probably support the internal system 
        User clientManager = getClientManager();
        AuthenticationHelper.setAsAuthenticatedUser(clientManager);
        try {
            for (String externalUserAlias : EXTERNAL_USERS) {
                ExternalUserVO userVO = new ExternalUserVO();
                userVO.setAlias(externalUserAlias);
                userVO.setEmail(UUID.randomUUID() + "@" + UUID.randomUUID());
                userVO.setExternalUserName(externalUserAlias);
                userVO.setSystemId(EXTERNAL_SYSTEM_ID);
                userVO.setFirstName("REST");
                userVO.setLastName("TEST" + Math.random());
                userVO.setPassword("123456");
                try {
                    Long userId = userManagement.createOrUpdateExternalUser(userVO).getId();
                    userManagement.changeUserStatusByManager(userId, UserStatus.ACTIVE);
                    registeredUsers.add(userId);
                } catch (Exception e) {
                    LOGGER.warn(e.getMessage());
                }
            }
        } finally {
            AuthenticationHelper.removeAuthentication();
        }
        UserGroupManagement userGroupManagement = ServiceLocator.instance().getService(
                UserGroupManagement.class);
        for (String externalGroup : EXTERNAL_GROUPS) {
            ExternalGroupVO groupVO = new ExternalGroupVO(externalGroup, EXTERNAL_SYSTEM_ID,
                    externalGroup);
            try {
                registeredGroups.add(userGroupManagement.createExternalGroup(groupVO));
            } catch (Exception e) {
                LOGGER.warn(e.getMessage());
            }
        }
    }

    /**
     * Adds the key group and key as allowed filter to all objects.
     */
    private void addPropertyFilters() {
        PropertyManagement propertyManagement = ServiceLocator.instance().getService(
                PropertyManagement.class);
        for (PropertyType type : PropertyType.values()) {
            try {
                propertyManagement.addObjectPropertyFilter(type, KEY_GROUP, KEY);
            } catch (Exception e) {
                LOGGER.warn("Filter for property type {} could not be registered.", type);
            }
        }
    }
    
    /**
     * @return a client manager
     */
    private User getClientManager() {
        return ServiceLocator.instance().getService(UserManagement.class)
                .findUsersByRole(UserRole.ROLE_KENMEI_CLIENT_MANAGER,
                UserStatus.ACTIVE).get(0);
    }

    /**
     * Removes all registered external users and groups.
     */
    private void removeExternalUsersAndGroups() {
        UserManagement userManagement = ServiceLocator.instance().getService(UserManagement.class);
        // TODO this is ugly! BE should probably support the internal system 
        User clientManager = getClientManager();
        AuthenticationHelper.setAsAuthenticatedUser(clientManager);
        try {
            for (Long userId : registeredUsers) {
                try {
                    userManagement.anonymizeUser(userId, null, false);
                } catch (Exception e) {
                    LOGGER.warn(e.getMessage());
                }
            }
        } finally {
            AuthenticationHelper.removeAuthentication();
        }
        UserGroupManagement userGroupManagement = ServiceLocator.instance().getService(
                UserGroupManagement.class);
        SecurityContext context = AuthenticationHelper.setInternalSystemToSecurityContext();
        try {
            for (Long externalGroupId : registeredGroups) {
                try {
                    userGroupManagement.deleteExternalGroup(externalGroupId, EXTERNAL_SYSTEM_ID);
                } catch (Exception e) {
                    LOGGER.warn(e.getMessage());
                }
            }
        } finally {
            AuthenticationHelper.setSecurityContext(context);
        }
    }

    /**
     * Removes the filter.
     */
    private void removePropertyFilters() {
        PropertyManagement propertyManagement = ServiceLocator.instance().getService(
                PropertyManagement.class);
        for (PropertyType type : PropertyType.values()) {
            try {
                propertyManagement.removeObjectPropertyFilter(type, KEY_GROUP, KEY);
            } catch (Exception e) {
                LOGGER.warn("Filter for property type {} can't be removed.", type);
            }
        }
    }

    @Override
    public void start(BundleContext context) throws Exception {
        addPropertyFilters();
        addExternalUsersAndGroups();
        ServiceLocator.findService(UserPreferenceService.class).register(TestUserPreference.class);
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        removePropertyFilters();
        removeExternalUsersAndGroups();
        ServiceLocator.findService(UserPreferenceService.class)
                .unregister(TestUserPreference.class);
    }
}
