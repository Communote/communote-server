package com.communote.server.core.user;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.config.ClientConfigurationPropertyConstant;
import com.communote.server.api.core.config.type.ClientProperty;
import com.communote.server.core.user.UserManagement;
import com.communote.server.core.user.group.GroupNotFoundException;
import com.communote.server.core.vo.user.group.ExternalGroupVO;
import com.communote.server.model.config.LdapConfiguration;
import com.communote.server.model.user.User;
import com.communote.server.model.user.group.ExternalUserGroup;
import com.communote.server.model.user.group.Group;
import com.communote.server.persistence.user.ExternalUserVO;
import com.communote.server.persistence.user.group.ExternalUserGroupDao;
import com.communote.server.service.UserService;
import com.communote.server.test.external.MockExternalUserGroupAccessor;
import com.communote.server.test.external.MockExternalUserRepository;
import com.communote.server.test.ldap.LdapCommunoteIntegrationTest;
import com.communote.server.test.util.AuthenticationTestUtils;
import com.communote.server.test.util.TestUtils;

/**
 * Tests for {@link UserService}.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class UserServiceTest extends LdapCommunoteIntegrationTest {

    private String externalSystemId;
    private String externalUserId;

    private UserService userService;

    private MockExternalUserRepository externalUserRepository;

    private String externalGroupId;

    /**
     * Get the dao for external user group
     *
     * @return the external user group dao
     */
    private ExternalUserGroupDao getExternalUserGroupDao() {
        return ServiceLocator.findService(ExternalUserGroupDao.class);
    }

    /**
     * Get the user management
     *
     * @return the user management
     */
    private UserManagement getUserManagement() {
        return ServiceLocator.instance().getService(UserManagement.class);
    }

    /**
     * Set configuration that internal group create automatically from external group
     *
     * @param isCreateAutomatically
     *            true or false
     */
    private void setCreateExternalGroupAutomatically(boolean isCreateAutomatically) {
        Map<ClientConfigurationPropertyConstant, String> map;
        map = new HashMap<ClientConfigurationPropertyConstant, String>();
        map.put(ClientProperty.CREATE_EXTERNAL_GROUP_AUTOMATICALLY,
                Boolean.toString(isCreateAutomatically));
        CommunoteRuntime.getInstance().getConfigurationManager()
        .updateClientConfigurationProperties(map);
    }

    /**
     * Set configuration that internal user create automatically from external group
     *
     * @param isCreateAutomatically
     *            true or false
     */
    private void setCreateExternalUserAutomatically(boolean isCreateAutomatically) {
        Map<ClientConfigurationPropertyConstant, String> map;
        map = new HashMap<ClientConfigurationPropertyConstant, String>();
        map.put(ClientProperty.CREATE_EXTERNAL_USER_AUTOMATICALLY,
                Boolean.toString(isCreateAutomatically));
        CommunoteRuntime.getInstance().getConfigurationManager()
        .updateClientConfigurationProperties(map);
    }

    /**
     * Setups all common parameters for this tests.
     *
     * @throws Exception
     *             Exception.
     */
    @BeforeClass(dependsOnGroups = "integration-test-setup")
    public void setup() throws Exception {
        userService = ServiceLocator.instance().getService(UserService.class);
        AuthenticationTestUtils.setSecurityContext(TestUtils.createRandomUser(true));
        LdapConfiguration ldapConfiguration = createGroupConfiguration(true, false, "member");
        ldapConfiguration.setAllowExternalAuthentication(true);
        ldapConfiguration.setSynchronizeUserGroups(true);
        CommunoteRuntime.getInstance().getConfigurationManager()
        .updateLdapConfiguration(ldapConfiguration);
        externalSystemId = ldapConfiguration.getSystemId();

        CommunoteRuntime.getInstance().getConfigurationManager()
        .setPrimaryAuthentication(externalSystemId, true);

        // since the ldap stuff was moved into a plugin the ldap user repo is missing, thus we mock
        // it
        MockExternalUserGroupAccessor groupAccessor = new MockExternalUserGroupAccessor(
                externalSystemId);
        externalUserRepository = new MockExternalUserRepository(externalSystemId, groupAccessor);
        ServiceLocator.instance().getService(UserService.class)
        .registerRepository(externalSystemId, externalUserRepository);

        ExternalUserVO user = TestUtils.createRandomUserVoForExternalSystem(externalSystemId);
        externalUserId = user.getExternalUserName();
        externalUserRepository.addUser(user);

        externalGroupId = UUID.randomUUID().toString();
        ExternalGroupVO group = new ExternalGroupVO();
        group.setAlias(externalGroupId);
        group.setExternalId(externalGroupId);
        group.setDescription("This is the " + externalGroupId + " group.");
        group.setExternalSystemId(externalSystemId);
        group.setName("Name: " + externalGroupId);

        groupAccessor.addGroup(group);
    }

    /**
     * Setups all common parameters for this tests.
     *
     * @throws Exception
     *             Exception.
     */
    // @BeforeClass(dependsOnGroups = "integration-test-setup")
    public void setup2() throws Exception {

        // set that groups can been created
        Map<ClientConfigurationPropertyConstant, String> map;
        map = new HashMap<ClientConfigurationPropertyConstant, String>();
        map.put(ClientProperty.CREATE_EXTERNAL_GROUP_AUTOMATICALLY, Boolean.TRUE.toString());
        map.put(ClientProperty.CREATE_EXTERNAL_USER_AUTOMATICALLY, Boolean.TRUE.toString());
        CommunoteRuntime.getInstance().getConfigurationManager()
        .updateClientConfigurationProperties(map);

    }

    /**
     * Tests, if it is possible to create an external group from external system.
     *
     * @throws Exception
     *             Exception.
     */
    @Test
    public void testCreateAndGetExternalGroup() throws Exception {
        setCreateExternalGroupAutomatically(true);

        Group group = userService.getGroup(externalGroupId, externalSystemId);
        Assert.assertNotNull(group);

        ExternalUserGroup externalGroup = getExternalUserGroupDao().findByExternalId(
                externalGroupId, externalSystemId);
        Assert.assertNotNull(externalGroup);

        getExternalUserGroupDao().remove(group.getId());

        externalGroup = getExternalUserGroupDao().findByExternalId(externalGroupId,
                externalSystemId);
        Assert.assertNull(externalGroup);
    }

    /**
     * Tests, if it is possible to create an external group from external system. If the property
     * for creating an external group is set to false the user service should throw an
     * {@link GroupNotFoundException}.
     *
     * @throws Exception
     *             Exception.
     */
    @Test(expectedExceptions = GroupNotFoundException.class)
    public void testCreateAndGetExternalGroupWithException() throws Exception {
        setCreateExternalGroupAutomatically(false);

        Group group = userService.getGroup(UUID.randomUUID().toString(), externalSystemId);
        Assert.assertNull(group);

    }

    /**
     * Tests, if it is possible to create an external group from external system.
     *
     * @throws Exception
     *             Exception.
     */
    @Test
    public void testCreateAndGetExternalUser() throws Exception {
        setCreateExternalUserAutomatically(true);

        User user = userService.getUser(externalUserId, externalSystemId);
        Assert.assertNotNull(user);

        User externalUser = getUserManagement().findUserByExternalUserId(externalUserId,
                externalSystemId);
        Assert.assertNotNull(externalUser);

    }
}
