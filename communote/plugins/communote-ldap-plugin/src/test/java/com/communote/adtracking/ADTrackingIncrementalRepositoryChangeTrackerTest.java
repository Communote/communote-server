package com.communote.adtracking;

import java.util.ArrayList;
import java.util.Collection;

import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import com.communote.plugin.ldap.ADTrackingIncrementalRepositoryChangeTracker;
import com.communote.plugin.ldap.PropertyKeys;
import com.communote.plugins.core.services.PluginPropertyService;
import com.communote.server.core.user.UserManagement;
import com.communote.server.model.config.LdapConfiguration;
import com.communote.server.model.config.LdapGroupSyncConfiguration;
import com.communote.server.model.config.LdapSearchBaseDefinition;
import com.communote.server.model.config.LdapSearchConfiguration;
import com.communote.server.model.user.User;
import com.communote.server.model.user.UserProfile;
import com.communote.server.model.user.group.ExternalUserGroup;
import com.communote.server.persistence.user.group.ExternalUserGroupDao;

/**
 * This is not a real test which validates the functionality. It's more like an implementation which
 * can be used to manually check whether the incremental change tracking works with a specific AD
 * server. Therefore, the tests are disabled.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ADTrackingIncrementalRepositoryChangeTrackerTest implements PropertyKeys {

    private PluginPropertyService pluginProperties;
    private ADTrackingIncrementalRepositoryChangeTracker tracker;
    private LdapSearchConfiguration userSearchConfiguration;
    private LdapGroupSyncConfiguration groupSyncConfig;
    private UserManagement userManagement;
    private long externalUsersCounter = 0;
    private ExternalUserGroupDao externalUserGroupDao;

    /**
     * Method to setup the group dao.
     */
    @BeforeMethod
    public void setupExternalUserGroupDao() {
        externalUserGroupDao = EasyMock.createMock(ExternalUserGroupDao.class);
        IAnswer<ExternalUserGroup> answer = new IAnswer<ExternalUserGroup>() {
            @Override
            public ExternalUserGroup answer() throws Throwable {
                ExternalUserGroup group = ExternalUserGroup.Factory.newInstance();
                group.setId(externalUsersCounter++);
                group.setAlias(String.valueOf(EasyMock.getCurrentArguments()[0]));
                return group;
            }
        };
        EasyMock.expect(
                externalUserGroupDao.findByExternalId(EasyMock.anyObject(String.class),
                        EasyMock.anyObject(String.class))).andAnswer(answer).anyTimes();
        EasyMock.replay(externalUserGroupDao);
    }

    /**
     * Setups the group search.
     * 
     * @param searchFilter
     *            The search filter for groups.
     * @param searchBase
     *            The search base for groups.
     * @param searchSubtree
     *            True, when subtrees should be search too.
     * @param propertyMapping
     *            Mapping of properties as String.
     * @param isMemberMode
     *            True, if the mode is "member", false if "memberOf"
     */
    @BeforeMethod(groups = "setupSearchBase")
    @Parameters({ "groupSearchFilter", "groupSearchBase", "groupSearchSubtree",
            "groupPropertyMapping", "isMemberMode" })
    public void setupGroupSearch(
            @Optional("(objectClass=group)") String searchFilter,
            String searchBase,
            @Optional("true") String searchSubtree,
            @Optional("name=name,alias=cn,membership=memberOf,description=name,uid=cn") String propertyMapping,
            @Optional("false") String isMemberMode) {
        groupSyncConfig = LdapGroupSyncConfiguration.Factory.newInstance();
        groupSyncConfig.setMemberMode(Boolean.parseBoolean(isMemberMode));
        groupSyncConfig.setGroupIdentifierIsBinary(false);
        LdapSearchConfiguration groupSearchConfiguration = LdapSearchConfiguration.Factory
                .newInstance();
        groupSearchConfiguration.setSearchFilter(searchFilter);
        LdapSearchBaseDefinition searchBaseDefinition = LdapSearchBaseDefinition.Factory
                .newInstance(searchBase, Boolean.parseBoolean(searchSubtree));
        groupSearchConfiguration.setSearchBases(new ArrayList<LdapSearchBaseDefinition>());
        groupSearchConfiguration.getSearchBases().add(searchBaseDefinition);
        groupSearchConfiguration.setPropertyMapping(propertyMapping);
        groupSyncConfig.setGroupSearch(groupSearchConfiguration);
    }

    /**
     * Setup the LDAP configuration for the ActiveDirectory to use.
     * 
     * @param url
     *            The LDAP URL of the ActiveDirectory server including protocol and port, e.g.
     *            ldap://ad.company.com:389
     * @param bindUserDN
     *            The distinguished name of the bind user which should be used for retrieving users
     *            and groups
     * @param bindUserPassword
     *            The password of the bind user
     */
    @BeforeMethod(dependsOnGroups = "setupSearchBase", dependsOnMethods = { "setupProperties",
            "setupUserManagement", "setupExternalUserGroupDao" })
    @Parameters({ "url", "bindUserDN", "bindUserPassword" })
    public void setupLdap(String url, String bindUserDN, String bindUserPassword) {
        LdapConfiguration ldapConfiguration = LdapConfiguration.Factory.newInstance();
        ldapConfiguration.setUserSearch(userSearchConfiguration);
        ldapConfiguration.setGroupSyncConfig(groupSyncConfig);
        groupSyncConfig.setLdapConfiguration(ldapConfiguration);
        ldapConfiguration.setDynamicMode(false);
        ldapConfiguration.setManagerDN(bindUserDN);
        ldapConfiguration.setManagerPassword(bindUserPassword);
        ldapConfiguration.setUrl(url);
        ldapConfiguration.setSystemId("External");
        tracker = new ADTrackingIncrementalRepositoryChangeTracker(ldapConfiguration,
                pluginProperties, userManagement, externalUserGroupDao, false);
    }

    /**
     * Setups the {@link PluginPropertyService}.
     */
    @BeforeMethod
    public void setupProperties() {
        pluginProperties = EasyMock.createMock(PluginPropertyService.class);
        EasyMock.expect(
                pluginProperties.getClientPropertyWithDefault(
                        PROPERTY_KEY_ACTIVE_DIRECTORY_TRACKING_ALLOW_PAGING, "true")).andReturn(
                "true");
        EasyMock.expect(
                pluginProperties.getClientPropertyWithDefault(
                        PROPERTY_KEY_ACTIVE_DIRECTORY_TRACKING_TIME_OUT, "10000")).andReturn(
                "10000");
        EasyMock.expect(
                pluginProperties.getClientPropertyWithDefault(
                        PROPERTY_KEY_ACTIVE_DIRECTORY_TRACKING_USER_SEQUENCE_NUMBER, "-1"))
                .andReturn("-1");
        pluginProperties.setClientProperty(EasyMock.anyObject(String.class),
                EasyMock.anyObject(String.class));
        EasyMock.expectLastCall().anyTimes();
        EasyMock.expect(
                pluginProperties.getClientPropertyWithDefault(
                        PROPERTY_KEY_ACTIVE_DIRECTORY_TRACKING_PAGING_SIZE, "1000"))
                .andReturn("50");
        EasyMock.expect(
                pluginProperties
                        .getClientProperty(PROPERTY_KEY_ACTIVE_DIRECTORY_TRACKING_ENABLED_USER))
                .andReturn("true");
        EasyMock.expect(
                pluginProperties
                        .getClientProperty(PROPERTY_KEY_ACTIVE_DIRECTORY_TRACKING_ENABLED_USER))
                .andReturn("false").anyTimes();
        EasyMock.expect(pluginProperties.getClientProperty("ad.last.used.server-url"))
                .andReturn(null).anyTimes();
        EasyMock.replay(pluginProperties);
    }

    /**
     * Setups the {@link UserManagement}.
     */
    @BeforeMethod
    public void setupUserManagement() {
        userManagement = EasyMock.createMock(UserManagement.class);
        IAnswer<User> answer = new IAnswer<User>() {
            @Override
            public User answer() throws Throwable {
                User user = User.Factory.newInstance();
                user.setId(externalUsersCounter++);
                user.setAlias(EasyMock.getCurrentArguments()[0].toString());
                user.setEmail(EasyMock.getCurrentArguments()[0] + "@example-test.com");
                user.setProfile(UserProfile.Factory.newInstance());
                user.getProfile().setFirstName(EasyMock.getCurrentArguments()[0].toString());
                user.getProfile().setLastName(EasyMock.getCurrentArguments()[0].toString());
                return user;
            }
        };
        EasyMock.expect(
                userManagement.findUserByExternalUserId(EasyMock.anyObject(String.class),
                        EasyMock.anyObject(String.class))).andAnswer(answer).anyTimes();
        EasyMock.replay(userManagement);
    }

    /**
     * Setups the user search.
     * 
     * @param searchFilter
     *            The search filter for users.
     * @param searchBase
     *            The search base for users.
     * @param searchSubtree
     *            True, when subtrees should be search too.
     * @param propertyMapping
     *            Mapping of properties as String.
     */
    @BeforeMethod(groups = "setupSearchBase")
    @Parameters({ "userSearchFilter", "userSearchBase", "userSearchSubtree", "userPropertyMapping" })
    public void setupUserSearch(@Optional("(objectClass=user)") String searchFilter,
            String searchBase, @Optional("true") String searchSubtree,
            @Optional("email=mail,alias=sAMAccountName,"
                    + "firstName=givenName,lastName=sn,uid=sAMAccountName") String propertyMapping) {
        userSearchConfiguration = LdapSearchConfiguration.Factory.newInstance();
        userSearchConfiguration.setSearchFilter(searchFilter);
        LdapSearchBaseDefinition searchBaseDefinition = LdapSearchBaseDefinition.Factory
                .newInstance(searchBase, Boolean.parseBoolean(searchSubtree));
        userSearchConfiguration.setSearchBases(new ArrayList<LdapSearchBaseDefinition>());
        userSearchConfiguration.getSearchBases().add(searchBaseDefinition);
        userSearchConfiguration.setPropertyMapping(propertyMapping);
    }

    /**
     * The test for getNextGroups.
     */
    @Test(enabled = false)
    public void testGetNextGroups() {
        tracker.start();
        Collection<ExternalUserGroup> nextGroups;
        int userCounter = 0;
        while ((nextGroups = tracker.getNextGroups()) != null) {
            userCounter += nextGroups.size();
        }
        Assert.assertNotEquals(userCounter, 0);
        tracker.stop(true);
    }

    /**
     * The test for getNextUsers.
     */
    @Test(enabled = false)
    public void testGetNextUsers() {
        tracker.start();
        Collection<User> nextUsers;
        int userCounter = 0;
        while ((nextUsers = tracker.getNextUsers()) != null) {
            userCounter += nextUsers.size();
        }
        Assert.assertNotEquals(userCounter, 0);
        tracker.stop(true);
    }
}
