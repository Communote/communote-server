package com.communote.server.web.fe.portal.user.client.forms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.Factory;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.util.Assert;

import com.communote.common.string.StringHelper;
import com.communote.server.core.common.ldap.LdapAttributeMappingException;
import com.communote.server.core.common.ldap.LdapGroupAttribute;
import com.communote.server.core.common.ldap.LdapGroupAttributesMapper;
import com.communote.server.core.common.ldap.LdapUserAttribute;
import com.communote.server.core.common.ldap.LdapUserAttributesMapper;
import com.communote.server.model.config.LdapConfiguration;
import com.communote.server.model.config.LdapGroupSyncConfiguration;
import com.communote.server.model.config.LdapSearchBaseDefinition;
import com.communote.server.model.config.LdapSearchConfiguration;

/**
 * The Class LdapConfigurationForm.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class LdapConfigurationForm {

    /** Defines the known LDAP authentication modes. */
    public enum AuthenticationMode {
        /** Simple username/password authentication */
        SIMPLE(null),
        /** Cram-MD5 authentication using SASL */
        SASL_CRAM_MD5("CRAM-MD5"),
        /** Digest-MD5 authentication using SASL */
        SASL_DIGEST_MD5("DIGEST-MD5");

        private final String saslMode;

        /**
         * constructor of the enum
         * 
         * @param saslMode
         *            the IANA-registered SASL mechanism name
         */
        private AuthenticationMode(String saslMode) {
            this.saslMode = saslMode;
        }

        /**
         * @return the SASL mode name, can be null
         */
        public String getSaslMode() {
            return saslMode;
        }
    }

    /** Defines modes the server could be found. */
    public enum ServerDetectionMode {
        /** Static mode, where the server is referenced by url. */
        STATIC,
        /** Dynamic mode, where the server is found automagically. */
        DYNAMIC
    }

    private boolean passwordChanged = false;
    private String action;
    private String ldapPassword;
    private String ldapLogin;
    private String userIdentifier;
    private String userAlias;
    private String userEmail;
    private String userFirstName;
    private String userLastName;
    private String groupMembership;
    private String groupIdentifier;
    private String groupName;
    private String groupAlias;
    private String groupDescription;
    private Long pagingSize;
    private boolean allowPaging;
    private boolean incrementalGroupSync;
    private boolean incrementalUserSync;
    private LdapConfiguration config;
    private LdapGroupSyncConfiguration groupSyncConfig;
    private AuthenticationMode authenticationMode;
    private boolean configurationExists = true;

    /**
     * Instantiates a new ldap configuration form.
     * 
     * @param config
     *            the config
     */
    public LdapConfigurationForm(LdapConfiguration config) {
        if (config == null) {
            config = LdapConfiguration.Factory.newInstance();
            config.setUserSearch(LdapSearchConfiguration.Factory.newInstance());
            configurationExists = false;
            passwordChanged = true;
        }
        this.setConfig(config);
    }

    /**
     * @return the set authentication mode
     */
    public String getAuthenticationMode() {
        return this.authenticationMode.name();
    }

    /**
     * Wrapper for bind user (manager DN).
     * 
     * @return the bind user
     */
    public String getBindUser() {
        return config.getManagerDN();
    }

    /**
     * Wrapper for bind user password.
     * 
     * @return the bind user password
     */
    public String getBindUserPassword() {
        return config.getManagerPassword();
    }

    /**
     * @return the config
     */
    public LdapConfiguration getConfig() {
        return config;
    }

    /**
     * @return the domain
     */
    public String getDomain() {
        return config.getServerDomain();
    }

    /**
     * @return the config
     */
    public LdapConfiguration getFilledConfig() {
        Iterator<LdapSearchBaseDefinition> it = null;
        config.setSaslMode(authenticationMode.getSaslMode());

        // set user attributes
        config.getUserSearch().setPropertyMapping(getUserPropertyMappingAsString());

        // clean search base list
        it = config.getUserSearch().getSearchBases().iterator();
        while (it.hasNext()) {
            LdapSearchBaseDefinition searchBase = it.next();
            if (StringUtils.isBlank(searchBase.getSearchBase())) {
                it.remove();
            }
        }

        // set group attributes
        config.setSynchronizeUserGroups(isSynchronizeUserGroups());
        if (isSynchronizeUserGroups()) {

            // clean search base list
            it = groupSyncConfig.getGroupSearch().getSearchBases().iterator();
            while (it.hasNext()) {
                LdapSearchBaseDefinition searchBase = it.next();
                if (StringUtils.isBlank(searchBase.getSearchBase())) {
                    it.remove();
                }
            }

            config.setGroupSyncConfig(groupSyncConfig);
            config.getGroupSyncConfig().getGroupSearch().setPropertyMapping(
                    getGroupPropertyMappingAsString());
        }

        return config;
    }

    /**
     * Gets the groupAlias
     * 
     * @return the property name for the alias of the group
     */
    public String getGroupAlias() {
        return groupAlias;
    }

    /**
     * Gets the groupDescription
     * 
     * @return the property name for the description of the group
     */
    public String getGroupDescription() {
        return groupDescription;
    }

    /**
     * Gets the group object identifier
     * 
     * @return the property name for the identifier of the group
     */
    public String getGroupIdentifier() {
        return groupIdentifier;
    }

    /**
     * Gets the group membership
     * 
     * @return the groupMembership
     */
    public String getGroupMembership() {
        return groupMembership;
    }

    /**
     * @return the groupName
     */
    public String getGroupName() {
        return groupName;
    }

    /**
     * Creates a map with all group attributes and returns the map.
     * 
     * @return user attributes as map
     */
    public Map<String, String> getGroupPropertyMapping() {

        Map<String, String> map = new HashMap<String, String>();
        map.put(LdapGroupAttribute.UID.getName(), getGroupIdentifier());
        map.put(LdapGroupAttribute.NAME.getName(), getGroupName());
        map.put(LdapGroupAttribute.ALIAS.getName(), getGroupAlias());
        map.put(LdapGroupAttribute.DESCRIPTION.getName(), getGroupDescription());
        map.put(LdapGroupAttribute.MEMBERSHIP.getName(), getGroupMembership());

        return map;
    }

    /**
     * Returns the mapping of the group attributes as string in the form
     * kenmeiAttribute1=ldapAttribute1,kenmeiAttribute2=ldapAttribute2
     * 
     * @return the mapping as string
     */
    public String getGroupPropertyMappingAsString() {
        return StringHelper.toString(getGroupPropertyMapping());
    }

    /**
     * Gets the group searchbase
     * 
     * @return the search base
     */
    public List<LdapSearchBaseDefinition> getGroupSearchBases() {
        return groupSyncConfig.getGroupSearch().getSearchBases();
    }

    /**
     * @return the user search filter
     */
    public String getGroupSearchfilter() {
        return groupSyncConfig.getGroupSearch().getSearchFilter();
    }

    /**
     * @return the group synchronisation configuration
     */
    public LdapGroupSyncConfiguration getGroupSyncConfig() {
        return groupSyncConfig;
    }

    /**
     * @return the ldapLogin
     */
    public String getLdapLogin() {
        return ldapLogin;
    }

    /**
     * Gets the ldap password.
     * 
     * @return the ldap password
     */
    public String getLdapPassword() {
        return ldapPassword;
    }

    /**
     * Gets the paging size for the ad tracking plugin
     * 
     * @return the paging size
     */
    public Long getPagingSize() {
        return pagingSize;
    }

    /**
     * @return the queryPrefix
     */
    public String getQueryPrefix() {
        return config.getQueryPrefix();
    }

    /**
     * @return the serverDetectionMode
     */
    public ServerDetectionMode getServerDetectionMode() {
        return config.isDynamicMode() ? ServerDetectionMode.DYNAMIC : ServerDetectionMode.STATIC;
    }

    /**
     * Gets the action.
     * 
     * @return the action
     */
    public String getSubmitAction() {
        return action;
    }

    /**
     * @return the supported authentication modes
     */
    public AuthenticationMode[] getSupportedAuthenticationModes() {
        return AuthenticationMode.values();
    }

    /**
     * Wrapper for URL.
     * 
     * @return the URL
     */
    public String getUrl() {
        return config.getUrl();
    }

    /**
     * Gets the userEmail
     * 
     * @return userEmail
     */
    public String getUserAlias() {
        return userAlias;
    }

    /**
     * Gets the userEmail
     * 
     * @return userEmail
     */
    public String getUserEmail() {
        return userEmail;
    }

    /**
     * Gets the userFirstName
     * 
     * @return userFirstName
     */
    public String getUserFirstName() {
        return userFirstName;
    }

    /**
     * Gets the userIdentifier
     * 
     * @return userIdentifier
     */
    public String getUserIdentifier() {
        return userIdentifier;
    }

    /**
     * Gets the userLastName
     * 
     * @return userLastName
     */
    public String getUserLastName() {
        return userLastName;
    }

    /**
     * Creates a map with all user attributes and returns the map.
     * 
     * @return user attributes as map
     */
    public Map<String, String> getUserPropertyMapping() {

        Map<String, String> map = new HashMap<String, String>();

        map.put(LdapUserAttribute.UID.getName(), getUserIdentifier());
        map.put(LdapUserAttribute.ALIAS.getName(), getUserAlias());
        map.put(LdapUserAttribute.EMAIL.getName(), getUserEmail());
        map.put(LdapUserAttribute.FIRSTNAME.getName(), getUserFirstName());
        map.put(LdapUserAttribute.LASTNAME.getName(), getUserLastName());

        return map;
    }

    /**
     * Returns the mapping of the user attributes as string in the form
     * kenmeiAttribute1=ldapAttribute1,kenmeiAttribute2=ldapAttribute2
     * 
     * @return the mapping as string
     */
    public String getUserPropertyMappingAsString() {
        return StringHelper.toString(getUserPropertyMapping());
    }

    /**
     * Gets the userSearchBases
     * 
     * @return the userSearchBases
     */
    public List<LdapSearchBaseDefinition> getUserSearchBases() {
        return config.getUserSearch().getSearchBases();
    }

    /**
     * @return the user search filter
     */
    public String getUserSearchfilter() {
        return config.getUserSearch().getSearchFilter();
    }

    /**
     * initializes the authentication mode property with the SASL mode
     * 
     * @param saslMode
     *            the SASL mode to set
     */
    private void initAuthenticationMode(String saslMode) {
        if (saslMode == null) {
            authenticationMode = AuthenticationMode.SIMPLE;
        } else {
            for (AuthenticationMode m : getSupportedAuthenticationModes()) {
                if (saslMode.equals(m.getSaslMode())) {
                    authenticationMode = m;
                    break;
                }
            }
        }
    }

    /**
     * If directory supports paged results
     * 
     * @return true if paging is allowed
     */
    public boolean isAllowPaging() {
        return allowPaging;
    }

    /**
     * Wrapper for activation state of configuration.
     * 
     * @return whether the configuration is active
     */
    public boolean isConfigAllowExternalAuthentication() {
        return config.isAllowExternalAuthentication();
    }

    /**
     * @return if the ldap configuration already existed or will be created when submitting the form
     */
    public boolean isConfigurationExists() {
        return configurationExists;
    }

    /**
     * Wrapper for group identifier is binary.
     * 
     * @return whether the identifier is binary
     */
    public boolean isGroupIdentifierIsBinary() {
        return groupSyncConfig.isGroupIdentifierIsBinary();
    }

    /**
     * Ensures that only changes since the last synchronization will be requested
     * 
     * @return true if incremental synchronization is on
     */
    public boolean isIncrementalGroupSync() {
        return incrementalGroupSync;
    }

    /**
     * @return the incrementalUserSync
     */
    public boolean isIncrementalUserSync() {
        return incrementalUserSync;
    }

    /**
     * Gets the memberMode
     * 
     * @return whether the member mode is on or off
     */
    public boolean isMemberMode() {
        return groupSyncConfig.isMemberMode();
    }

    /**
     * @return the passwordChanged
     */
    public boolean isPasswordChanged() {
        return passwordChanged;
    }

    /**
     * Wrapper for primary flag.
     * 
     * @return whether the external configuration is the primary configuration
     */
    public boolean isPrimary() {
        return config.isPrimaryAuthentication();
    }

    /**
     * Wrapper for activation state of group synchronization flag.
     * 
     * @return whether the group synchronization is active
     */
    public boolean isSynchronizeUserGroups() {
        return config.isSynchronizeUserGroups();
    }

    /**
     * Wrapper for user identifier is binary.
     * 
     * @return whether the identifier is binary
     */
    public boolean isUserIdentifierIsBinary() {
        return config.isUserIdentifierIsBinary();
    }

    /**
     * 
     */
    private void prepareGroupPropertyMapping() {
        if (groupSyncConfig.getGroupSearch().getPropertyMapping() != null) {
            try {
                LdapGroupAttributesMapper groupAttributesMapper = new LdapGroupAttributesMapper(
                        groupSyncConfig.getGroupSearch().getPropertyMapping(),
                        config.getSystemId(), groupSyncConfig.isGroupIdentifierIsBinary());

                setGroupIdentifier(groupAttributesMapper.getLdapAttributName(LdapGroupAttribute.UID
                        .getName()));
                setGroupName(groupAttributesMapper.getLdapAttributName(LdapGroupAttribute.NAME
                        .getName()));
                setGroupAlias(groupAttributesMapper.getLdapAttributName(LdapGroupAttribute.ALIAS
                        .getName()));
                setGroupDescription(groupAttributesMapper
                        .getLdapAttributName(LdapGroupAttribute.DESCRIPTION.getName()));

                setGroupMembership(groupAttributesMapper
                        .getLdapAttributName(LdapGroupAttribute.MEMBERSHIP.getName()));

            } catch (LdapAttributeMappingException e) {
                // do nothing - leave attributes empty (null)
            }
        }
    }

    /**
     * Prepares the list of the group search base for a dynamic binding in Spring MVC.
     */
    @SuppressWarnings("unchecked")
    private void prepareGroupSearchBases() {
        List<LdapSearchBaseDefinition> searchBases = ListUtils.lazyList(
                new ArrayList<LdapSearchBaseDefinition>(), new Factory() {
                    @Override
                    public Object create() {
                        return LdapSearchBaseDefinition.Factory.newInstance();
                    }
                });

        if (groupSyncConfig.getGroupSearch().getSearchBases().isEmpty()) {
            searchBases.add(LdapSearchBaseDefinition.Factory.newInstance());

        } else {
            searchBases.addAll(groupSyncConfig.getGroupSearch().getSearchBases());
        }
        groupSyncConfig.getGroupSearch().setSearchBases(searchBases);
    }

    /**
     * 
     */
    private void prepareUserPropertyMapping() {
        if (config.getUserSearch().getPropertyMapping() != null) {
            try {
                LdapUserAttributesMapper userAttributesMapper = new LdapUserAttributesMapper(config);

                setUserIdentifier(userAttributesMapper.getLdapAttributName(LdapUserAttribute.UID
                        .getName()));
                setUserAlias(userAttributesMapper.getLdapAttributName(LdapUserAttribute.ALIAS
                        .getName()));
                setUserEmail(userAttributesMapper.getLdapAttributName(LdapUserAttribute.EMAIL
                        .getName()));
                setUserFirstName(userAttributesMapper
                        .getLdapAttributName(LdapUserAttribute.FIRSTNAME.getName()));
                setUserLastName(userAttributesMapper.getLdapAttributName(LdapUserAttribute.LASTNAME
                        .getName()));

            } catch (LdapAttributeMappingException e) {
                // do nothing - leave attributes empty (null)
            }
        }
    }

    /**
     * Prepares the list of the user search base for a dynamic binding in Spring MVC.
     */
    @SuppressWarnings("unchecked")
    private void prepareUserSearchBases() {
        List<LdapSearchBaseDefinition> searchBases = ListUtils.lazyList(
                new ArrayList<LdapSearchBaseDefinition>(), new Factory() {
                    @Override
                    public Object create() {
                        return LdapSearchBaseDefinition.Factory.newInstance();
                    }
                });

        if (config.getUserSearch().getSearchBases().isEmpty()) {
            searchBases.add(LdapSearchBaseDefinition.Factory.newInstance());

        } else {
            searchBases.addAll(config.getUserSearch().getSearchBases());
        }
        config.getUserSearch().setSearchBases(searchBases);
    }

    /**
     * Set the option for the AD tracking plugin if paging is allowed
     * 
     * @param allowPaging
     *            true if directory supports paged results
     */
    public void setAllowPaging(boolean allowPaging) {
        this.allowPaging = allowPaging;
    }

    /**
     * Set the authentication mode
     * 
     * @param typeName
     *            the name of the enum
     */
    public void setAuthenticationMode(String typeName) {
        this.authenticationMode = AuthenticationMode.valueOf(StringUtils.trim(typeName));
    }

    /**
     * Wrapper to set bind user (manager DN) in config.
     * 
     * @param bindUser
     *            the bindUser
     */
    public void setBindUser(String bindUser) {
        config.setManagerDN(StringUtils.trim(bindUser));
    }

    /**
     * Wrapper to set manager password in config.
     * 
     * @param bindUserPassword
     *            the password
     */
    public void setBindUserPassword(String bindUserPassword) {
        config.setManagerPassword(StringUtils.trim(bindUserPassword));
    }

    /**
     * Sets the config.
     * 
     * @param configuration
     *            the new config (optional)
     */
    private void setConfig(LdapConfiguration configuration) {
        Assert.notNull(configuration, "ldap config must be set");
        config = configuration;

        prepareUserSearchBases();
        prepareUserPropertyMapping();
        initAuthenticationMode(config.getSaslMode());

        // in case of a saved config with inactive groupSync
        if (config.getGroupSyncConfig() == null) {
            groupSyncConfig = LdapGroupSyncConfiguration.Factory.newInstance();
            groupSyncConfig.setGroupSearch(LdapSearchConfiguration.Factory.newInstance());
        } else {
            groupSyncConfig = config.getGroupSyncConfig();
        }

        prepareGroupSearchBases();
        prepareGroupPropertyMapping();
    }

    /**
     * @param domain
     *            the domain to set
     */
    public void setDomain(String domain) {
        config.setServerDomain(StringUtils.trim(domain));
    }

    /**
     * the alias of a group
     * 
     * @param groupAlias
     *            the alias
     */
    public void setGroupAlias(String groupAlias) {
        this.groupAlias = StringUtils.trim(groupAlias);
    }

    /**
     * the group description
     * 
     * @param groupDescription
     *            the optional group description
     */
    public void setGroupDescription(String groupDescription) {
        this.groupDescription = StringUtils.trim(groupDescription);
    }

    /**
     * @param groupIdentifier
     *            ...
     */
    public void setGroupIdentifier(String groupIdentifier) {
        this.groupIdentifier = StringUtils.trim(groupIdentifier);
    }

    /**
     * @param groupIdentifierIsBinary
     *            if the identifier is binary
     */
    public void setGroupIdentifierIsBinary(boolean groupIdentifierIsBinary) {
        groupSyncConfig.setGroupIdentifierIsBinary(groupIdentifierIsBinary);
    }

    /**
     * Wrapper to set group membership in config.
     * 
     * @param groupMembership
     *            the groupMembership
     */
    public void setGroupMembership(String groupMembership) {
        this.groupMembership = StringUtils.trim(groupMembership);
    }

    /**
     * @param groupName
     *            the groupName to set
     */
    public void setGroupName(String groupName) {
        this.groupName = StringUtils.trim(groupName);
    }

    /**
     * Wrapper to set group search bases in config.
     * 
     * @param searchBases
     *            the search bases
     */
    public void setGroupSearchBases(List<LdapSearchBaseDefinition> searchBases) {
        groupSyncConfig.getGroupSearch().setSearchBases(searchBases);
    }

    /**
     * Wrapper to set group search filter in config.
     * 
     * @param searchFilter
     *            the filter
     */
    public void setGroupSearchfilter(String searchFilter) {
        searchFilter = StringUtils.trim(searchFilter);
        if (!searchFilter.matches("\\(.*\\)")) {
            searchFilter = "(" + searchFilter + ")";
        }
        groupSyncConfig.getGroupSearch().setSearchFilter(searchFilter);
    }

    /**
     * Wrapper to set group synchronization configuration.
     * 
     * @param groupSyncConfig
     *            the group configuration
     */
    public void setGroupSyncConfig(LdapGroupSyncConfiguration groupSyncConfig) {
        this.groupSyncConfig = groupSyncConfig;
    }

    /**
     * Ensures that only changes since the last synchronisation will be requested
     * 
     * @param incrementalSync
     *            true = on / false = off
     */

    public void setIncrementalGroupSync(boolean incrementalSync) {
        this.incrementalGroupSync = incrementalSync;
    }

    /**
     * @param incrementalUserSync
     *            the incrementalUserSync to set
     */
    public void setIncrementalUserSync(boolean incrementalUserSync) {
        this.incrementalUserSync = incrementalUserSync;
    }

    /**
     * @param ldapLogin
     *            the ldapLogin to set
     */
    public void setLdapLogin(String ldapLogin) {
        this.ldapLogin = StringUtils.trim(ldapLogin);
    }

    /**
     * Sets the ldap password.
     * 
     * @param ldapPassword
     *            the new ldap password
     */
    public void setLdapPassword(String ldapPassword) {
        this.ldapPassword = StringUtils.trim(ldapPassword);
    }

    /**
     * Wrapper to set member mode flag in config.
     * 
     * @param isMemberMode
     *            the member mode
     */
    public void setMemberMode(boolean isMemberMode) {
        groupSyncConfig.setMemberMode(isMemberMode);
    }

    /**
     * Set the value for the results per page
     * 
     * @param pagingSize
     *            number of results per page
     */
    public void setPagingSize(Long pagingSize) {
        this.pagingSize = pagingSize;
    }

    /**
     * @param passwordChanged
     *            the passwordChanged to set
     */
    public void setPasswordChanged(boolean passwordChanged) {
        this.passwordChanged = passwordChanged;
    }

    /**
     * Wrapper for setting the primary flag.
     * 
     * @param primary
     *            whether the external configuration is the primary configuration
     */
    public void setPrimary(boolean primary) {
        config.setPrimaryAuthentication(primary);
    }

    /**
     * @param queryPrefix
     *            the queryPrefix to set
     */
    public void setQueryPrefix(String queryPrefix) {
        config.setQueryPrefix(StringUtils.trim(queryPrefix));
    }

    /**
     * @param serverDetectionMode
     *            the serverDetectionMode to set
     */
    public void setServerDetectionMode(ServerDetectionMode serverDetectionMode) {
        config.setDynamicMode(ServerDetectionMode.DYNAMIC.equals(serverDetectionMode));
    }

    /**
     * Sets the action.
     * 
     * @param action
     *            the new action
     */
    public void setSubmitAction(String action) {
        this.action = StringUtils.trim(action);
    }

    /**
     * Wrapper to set activation state of group synchronization flag.
     * 
     * @param activateGroupSync
     *            the activateGroupSync flag
     */
    public void setSynchronizeUserGroups(boolean activateGroupSync) {
        config.setSynchronizeUserGroups(activateGroupSync);
    }

    /**
     * Wrapper to set the URL in config.
     * 
     * @param url
     *            the URL
     */
    public void setUrl(String url) {
        url = StringUtils.trim(url);
        if (!(url.startsWith("ldap://") || url.startsWith("ldaps://"))) {
            url = "ldap://" + url;
        }
        config.setUrl(url);
    }

    /**
     * @param userAlias
     *            ...
     */
    public void setUserAlias(String userAlias) {
        this.userAlias = StringUtils.trim(userAlias);
    }

    /**
     * @param email
     *            ...
     */
    public void setUserEmail(String email) {
        this.userEmail = StringUtils.trim(email);
    }

    /**
     * @param firstName
     *            ...
     */
    public void setUserFirstName(String firstName) {
        this.userFirstName = StringUtils.trim(firstName);
    }

    /**
     * @param userIdentifier
     *            attribute referring to a permanent unique ID of an external User
     */
    public void setUserIdentifier(String userIdentifier) {
        this.userIdentifier = StringUtils.trim(userIdentifier);
    }

    /**
     * @param userIdentifierIsBinary
     *            ...
     */
    public void setUserIdentifierIsBinary(boolean userIdentifierIsBinary) {
        config.setUserIdentifierIsBinary(userIdentifierIsBinary);
    }

    /**
     * @param lastName
     *            ...
     */
    public void setUserLastName(String lastName) {
        this.userLastName = StringUtils.trim(lastName);
    }

    /**
     * Wrapper to set the user search base in config.
     * 
     * @param searchBases
     *            the search base
     */
    public void setUserSearchBases(List<LdapSearchBaseDefinition> searchBases) {
        config.getUserSearch().setSearchBases(searchBases);
    }

    /**
     * Wrapper to set the user search filter in config.
     * 
     * @param searchFilter
     *            the search filter
     */
    public void setUserSearchfilter(String searchFilter) {
        searchFilter = searchFilter == null ? null : searchFilter.trim();
        if (!searchFilter.matches("\\(.*\\)")) {
            searchFilter = "(" + searchFilter + ")";
        }
        config.getUserSearch().setSearchFilter(searchFilter);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "\naction: " + action + "\nldapPassword: *******\n+config.url: " + config.getUrl()
                + "\nconfig.managerDN: " + config.getManagerDN() + "\nconfig.managerPassword: "
                + config.getManagerPassword() + "\nconfig.searchFilter: "
                + config.getUserSearch().getSearchFilter() + "\n";
    }
}
