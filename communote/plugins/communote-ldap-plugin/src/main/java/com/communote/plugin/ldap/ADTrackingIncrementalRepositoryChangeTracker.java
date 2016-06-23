package com.communote.plugin.ldap;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.security.ldap.DefaultSpringSecurityContextSource;

import com.communote.common.string.StringHelper;
import com.communote.plugin.ldap.helper.GroupRetriever;
import com.communote.plugin.ldap.helper.UserRetriever;
import com.communote.plugins.core.services.PluginPropertyService;
import com.communote.server.api.ServiceLocator;
import com.communote.server.core.common.ldap.LdapAttributeMappingException;
import com.communote.server.core.common.ldap.LdapGroupAttributesMapper;
import com.communote.server.core.common.ldap.LdapSearchUtils;
import com.communote.server.core.common.ldap.LdapUserAttributesMapper;
import com.communote.server.core.common.ldap.LdapUtils;
import com.communote.server.core.external.IncrementalRepositoryChangeTracker;
import com.communote.server.core.user.UserGroupManagement;
import com.communote.server.core.user.UserManagement;
import com.communote.server.model.config.LdapConfiguration;
import com.communote.server.model.user.User;
import com.communote.server.model.user.group.ExternalUserGroup;
import com.communote.server.persistence.user.ExternalUserVO;
import com.communote.server.persistence.user.group.ExternalUserGroupDao;

/**
 * Tracker for active directory.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ADTrackingIncrementalRepositoryChangeTracker implements
        IncrementalRepositoryChangeTracker, PropertyKeys {

    /** Logger. */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(ADTrackingIncrementalRepositoryChangeTracker.class);
    /** initial USN value */
    private final static long NO_USN_SUBMITTED = -1L;

    private final PluginPropertyService pluginProperties;
    private final LdapConfiguration ldapConfiguration;
    private final UserManagement userManagement;
    private long externalHighestCommittedUSN;
    private long internalHighestCommittedUSN = NO_USN_SUBMITTED;

    private UserRetriever userRetriever;
    private GroupRetriever groupRetriever;
    private Exception userSynchronizationException;
    private final ExternalUserGroupDao externalUserGroupDao;
    private Exception groupSynchronizationException;
    private final boolean doFullSynchronization;
    private UserGroupManagement userGroupManagement;

    /** Key for storing the last used server url of this tracker. */
    public final static String PROPERTY_KEY_AD_LAST_USED_SERVER = "ad.last.used.server-url";

    /**
     * Constructor.
     * 
     * @param ldapConfiguration
     *            The LDAP configuration to use.
     * @param pluginProperties
     *            The properties to use.
     * @param userManagement
     *            Management for users.
     * @param externalUserGroupDao
     *            Dao for external users.
     * @param doFullSynchronization
     *            If set to true, a full sync will done.
     */
    public ADTrackingIncrementalRepositoryChangeTracker(LdapConfiguration ldapConfiguration,
            PluginPropertyService pluginProperties, UserManagement userManagement,
            ExternalUserGroupDao externalUserGroupDao, boolean doFullSynchronization) {
        this.ldapConfiguration = ldapConfiguration;
        this.pluginProperties = pluginProperties;
        this.userManagement = userManagement;
        this.externalUserGroupDao = externalUserGroupDao;
        this.doFullSynchronization = doFullSynchronization;
    }

    /**
     * Creates a context source for the AD.
     * 
     * @param config
     *            the ldap configuration
     * @param binaryAttributes
     *            optional array of attributes to be returned as binary. This argument need not to
     *            be provided if the context is not intended to retrieve entries.
     * @return the context factory
     * @throws NamingException
     *             Exception
     */
    private LdapContextSource createLdapContextForAD(LdapConfiguration config,
            String[] binaryAttributes) throws NamingException {
        List<String> urls = Arrays.asList(LdapSearchUtils.getServerUrls(config));
        String lastUsedUrl = pluginProperties
                .getClientProperty(PROPERTY_KEY_AD_LAST_USED_SERVER);
        // Move to front, only if it is contained, else the servers might have changed.
        if (lastUsedUrl != null && urls.indexOf(lastUsedUrl) > 0) {
            urls.remove(lastUsedUrl);
            urls.add(0, lastUsedUrl);
        }
        for (String url : urls) {
            LdapContextSource targetContextSource = new DefaultSpringSecurityContextSource(url);
            targetContextSource.setUserDn(ldapConfiguration.getManagerDN());
            targetContextSource.setPassword(ldapConfiguration.getManagerPassword());
            try {
                Map<String, String> map = new HashMap<String, String>();
                map.put(Context.REFERRAL, "follow");
                if (binaryAttributes != null && binaryAttributes.length > 0) {
                    map.put("java.naming.ldap.attributes.binary",
                            StringHelper.toString(binaryAttributes,
                                    " "));
                }
                if (config.getSaslMode() != null) {
                    map.put(Context.SECURITY_AUTHENTICATION, config.getSaslMode());
                }
                targetContextSource.setBaseEnvironmentProperties(map);
                targetContextSource.afterPropertiesSet();
                targetContextSource.getReadOnlyContext().close();
                if (lastUsedUrl != null && !url.equals(lastUsedUrl)) {
                    pluginProperties.setClientProperty(
                            PROPERTY_KEY_ACTIVE_DIRECTORY_TRACKING_USER_SEQUENCE_NUMBER,
                            null);
                }
                pluginProperties.setClientProperty(PROPERTY_KEY_AD_LAST_USED_SERVER, url);
                return targetContextSource;
            } catch (Exception e) {
                LOGGER.error("There was an error configuring the LDAP context source.", e);
            }
        }
        return null;
    }

    /**
     * Gets the highest committed USN attribute from ad.
     * 
     * @param ldapTemplate
     *            The ldap connection to use.
     * 
     * @return The highest committed USN.
     */
    private long fetchHighestCommittedUSN(LdapTemplate ldapTemplate) {
        try {
            Object highestCommittedUSN = ldapTemplate
                    .lookup("", new String[] { "highestCommittedUSN" }, new AttributesMapper() {
                        @Override
                        public Object mapFromAttributes(Attributes attributes)
                                throws NamingException {
                            return attributes.get("highestCommittedUSN").get(0);
                        }
                    });
            long usn = Long.parseLong(highestCommittedUSN.toString());
            LOGGER.debug("The foreign highest committed USN: " + usn);
            return usn;
        } catch (org.springframework.ldap.NamingException e) {
            LOGGER.error("Error fetchHighestCommittedUSN", e);
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
        return NO_USN_SUBMITTED;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<ExternalUserGroup> getNextGroups() {
        try {
            return groupRetriever.getNextEntities();
        } catch (Exception e) {
            LOGGER.debug(e.getMessage(), e);
            groupSynchronizationException = e;
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<User> getNextUsers() {
        try {
            Collection<User> nextUsers = userRetriever.getNextEntities();
            if (nextUsers == null) {
                return null;
            }
            if (Boolean.parseBoolean(pluginProperties
                    .getClientProperty(PROPERTY_KEY_ACTIVE_DIRECTORY_TRACKING_ENABLED_USER))) {
                for (User user : nextUsers) {
                    if (user.getAlias() == null && user.getEmail() == null) {
                        continue;
                    }
                    try {
                        String searchString = user.getAlias() != null ? user.getAlias() : user
                                .getEmail();
                        ExternalUserVO externalKenmeiUserVO = LdapUtils
                                .queryUserByName(searchString);
                        ServiceLocator.instance().getService(UserManagement.class)
                                .updateExternalUser(externalKenmeiUserVO);
                        LOGGER.debug("Synchronized LDAP/AD user {}", user.getAlias());
                    } catch (Exception e) {
                        LOGGER.warn("There was an error synchronizing the LDAP/AD user {} : {}",
                                user.getAlias(), e.getMessage());
                    }
                }
            }
            return nextUsers;
        } catch (Exception e) {
            LOGGER.debug(e.getMessage(), e);
            userSynchronizationException = e;
        }
        return null;
    }

    /**
     * {@inheritDoc}
     * 
     * @return True.
     */
    @Override
    public boolean needsToAcceptMembersOfGroups() {
        return true;
    }

    /**
     * @param userGroupManagement
     *            the userGroupManagement to set
     */
    public void setUserGroupManagement(UserGroupManagement userGroupManagement) {
        this.userGroupManagement = userGroupManagement;
    }

    /**
     * Initializes the connection to the directory server.
     */
    @Override
    public void start() {
        try {
            userSynchronizationException = null;
            groupSynchronizationException = null;
            externalHighestCommittedUSN = fetchHighestCommittedUSN(new LdapTemplate(
                    createLdapContextForAD(ldapConfiguration, null)));
            if (!doFullSynchronization) {
                internalHighestCommittedUSN = Long.parseLong(pluginProperties
                        .getClientPropertyWithDefault(
                                PROPERTY_KEY_ACTIVE_DIRECTORY_TRACKING_USER_SEQUENCE_NUMBER,
                                Long.toString(NO_USN_SUBMITTED)));
            }
            LOGGER.debug("The internal highest committed USN: {}", internalHighestCommittedUSN);
            boolean isPagingAllowed = Boolean.parseBoolean(pluginProperties
                    .getClientPropertyWithDefault(
                            PROPERTY_KEY_ACTIVE_DIRECTORY_TRACKING_ALLOW_PAGING,
                            "true"));
            int timeout = Integer.parseInt(pluginProperties.getClientPropertyWithDefault(
                    PROPERTY_KEY_ACTIVE_DIRECTORY_TRACKING_TIME_OUT, "10000"));
            int pagingSize = Integer.parseInt(pluginProperties.getClientPropertyWithDefault(
                    PROPERTY_KEY_ACTIVE_DIRECTORY_TRACKING_PAGING_SIZE, "1000"));

            String[] binaryAttributes = new LdapUserAttributesMapper(ldapConfiguration)
                    .getBinaryLdapAttributeName();
            userRetriever = new UserRetriever(ldapConfiguration, new LdapTemplate(
                    createLdapContextForAD(ldapConfiguration, binaryAttributes)),
                    internalHighestCommittedUSN, externalHighestCommittedUSN,
                    pagingSize, timeout, isPagingAllowed);
            userRetriever.setUserManagement(userManagement);

            binaryAttributes = new LdapGroupAttributesMapper(ldapConfiguration
                    .getGroupSyncConfig().getGroupSearch()
                    .getPropertyMapping(), null, ldapConfiguration.getGroupSyncConfig()
                    .isGroupIdentifierIsBinary()).getBinaryLdapAttributeName();
            groupRetriever = new GroupRetriever(ldapConfiguration, new LdapTemplate(
                    createLdapContextForAD(ldapConfiguration, binaryAttributes)),
                    internalHighestCommittedUSN, externalHighestCommittedUSN,
                    pagingSize, timeout, isPagingAllowed);
            groupRetriever.setExternalUserGroupDao(externalUserGroupDao);
            groupRetriever.setUserGroupManagement(userGroupManagement);
        } catch (NamingException e) {
            LOGGER.error(e.getMessage(), e);
        } catch (LdapAttributeMappingException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stop(boolean successful) {
        if (successful && externalHighestCommittedUSN > internalHighestCommittedUSN
                && userSynchronizationException == null && groupSynchronizationException == null) {
            pluginProperties.setClientProperty(
                    PROPERTY_KEY_ACTIVE_DIRECTORY_TRACKING_USER_SEQUENCE_NUMBER,
                    Long.toString(externalHighestCommittedUSN));
            LOGGER.info("Synchronization was successful, next revision is {}",
                    externalHighestCommittedUSN);
        }
        if (userSynchronizationException != null) {
            LOGGER.error("Synchronization failed with error: {}",
                    userSynchronizationException.getMessage());
        }
        if (groupSynchronizationException != null) {
            LOGGER.error("Synchronization failed with error: {}",
                    groupSynchronizationException.getMessage());
        }
        userRetriever = null;
    }
}
