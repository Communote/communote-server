package com.communote.plugin.ldap;

import java.util.Collection;
import java.util.Date;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;

import com.communote.plugin.ldap.helper.MemberAndNonMemberModeVisitingRetriever;
import com.communote.plugins.core.services.PluginPropertyService;
import com.communote.server.api.ServiceLocator;
import com.communote.server.core.common.ldap.LdapAttributeMappingException;
import com.communote.server.core.common.ldap.LdapGroupAttributesMapper;
import com.communote.server.core.common.ldap.LdapGroupSearch;
import com.communote.server.core.common.ldap.LdapSearchUtils;
import com.communote.server.core.common.ldap.LdapUserAttributesMapper;
import com.communote.server.core.user.UserGroupMemberManagement;
import com.communote.server.core.user.UserManagement;
import com.communote.server.core.vo.user.group.ExternalGroupVO;
import com.communote.server.model.config.ExternalSystemConfiguration;
import com.communote.server.model.config.LdapConfiguration;
import com.communote.server.model.user.ExternalUserAuthentication;
import com.communote.server.model.user.User;
import com.communote.server.model.user.group.ExternalUserGroup;
import com.communote.server.persistence.user.ExternalUserVO;
import com.communote.server.plugins.api.externals.ExternalEntityVisitor;
import com.communote.server.plugins.api.externals.ExternalUserGroupAccessor;
import com.communote.server.plugins.api.externals.ExternalUserGroupAccessorException;
import com.communote.server.plugins.exceptions.PluginException;

/**
 * {@link ExternalUserGroupAccessor} for LDAP.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class LdapUserGroupAccessor implements ExternalUserGroupAccessor, PropertyKeys {

    /**
     * Mapper for the member attribute, which respects the "range".
     */
    private final class MemberAttributesMapper implements AttributesMapper {

        private final ExternalEntityVisitor<Long> visitor;
        private final LdapUserAttributesMapper ldapUserAttributesMapper;
        private final UserManagement userManagement;
        private final Collection<Long> existingUsersOfGroup;
        private boolean membersLeft = true;
        private final int rangeFetchSize;

        /**
         * Constructor.
         * 
         * @param rangeFetchSize
         *            Number of elements, which are maximal fetch with one request.
         * @param visitor
         *            The visitor to use for visiting the elements.
         * @param ldapUserAttributesMapper
         *            Mapper for users.
         * @param userManagement
         *            UserManagement to use.
         * @param existingUsersOfGroup
         *            List of users, who are already a member of the given group.
         */
        private MemberAttributesMapper(int rangeFetchSize, ExternalEntityVisitor<Long> visitor,
                LdapUserAttributesMapper ldapUserAttributesMapper, UserManagement userManagement,
                Collection<Long> existingUsersOfGroup) {
            this.rangeFetchSize = rangeFetchSize;
            this.visitor = visitor;
            this.ldapUserAttributesMapper = ldapUserAttributesMapper;
            this.userManagement = userManagement;
            this.existingUsersOfGroup = existingUsersOfGroup;
        }

        @Override
        public Object mapFromAttributes(Attributes attributes) throws NamingException {
            NamingEnumeration<String> ids = attributes.getIDs();
            if (!ids.hasMore()) {
                membersLeft = false;
                return null;
            }
            while (ids.hasMore()) {
                String id = ids.next();
                if (id.equals(systemMemberName) || id.startsWith(systemMemberName + ";range")) {
                    Attribute attribute = attributes.get(id);
                    membersLeft = attribute.size() == rangeFetchSize;
                    for (int i = 0; i < attribute.size(); i++) {
                        String userDn = attribute.get(i).toString();
                        Object userObject = groupRetrievalLdapTemplate.lookup(userDn);
                        DirContextAdapter adapter = (DirContextAdapter) userObject;
                        try {
                            ExternalUserVO userVo = ldapUserAttributesMapper
                                    .mapAttributes(userDn, adapter.getAttributes());
                            User user = userManagement.findUserByExternalUserId(
                                    userVo.getExternalUserName(), userVo.getSystemId());
                            if (user != null) {
                                if (existingUsersOfGroup.contains(user.getId())) {
                                    LOGGER.debug("User {} already in group -> Skip.",
                                            user.getId());
                                    existingUsersOfGroup.remove(user.getId());
                                    continue;
                                }
                                visitor.visit(user.getId());
                            }
                        } catch (Exception e) {
                            LOGGER.warn(
                                    "Can't synchronize a ldap user: {}: {}. This might not be synchronizable user",
                                    userDn, e.getMessage());
                        }
                    }
                }
            }
            return null;
        }
    }

    /**
     * Specifies the amount of numbers each range should fetch. Default is 500. This value can be
     * changed using a system property "com.communote.ldap.range_incrementing_size".
     */
    private final int systemRangeFetchSize = Integer.getInteger(
            "com.communote.ldap.range_fetch_size", 500);

    /**
     * Name of the members attribute of LDAP/AD groups. Default is "member". Can be set using a
     * system property "com.communote.ldap.accept_members_attribute_name".
     */
    private final String systemMemberName = System.getProperty(
            "com.communote.ldap.accept_members_attribute_name", "member");

    /** Logger. */
    private final static Logger LOGGER = LoggerFactory.getLogger(LdapUserGroupAccessor.class);

    private LdapGroupSearch groupSearch;
    private LdapConfiguration ldapConfiguration;

    private final boolean incrementalGroupSync;

    private MemberAndNonMemberModeVisitingRetriever retriever;

    private final int pagingSize;

    private final int timeout;

    private final boolean isPagingAllowed;

    private LdapTemplate groupRetrievalLdapTemplate;

    /**
     * Constructor.
     * 
     * @param pluginProperties
     *            The properties to use.
     */
    public LdapUserGroupAccessor(PluginPropertyService pluginProperties) {
        isPagingAllowed = Boolean.parseBoolean(pluginProperties
                .getClientPropertyWithDefault(PROPERTY_KEY_ACTIVE_DIRECTORY_TRACKING_ALLOW_PAGING,
                        "true"));
        timeout = Integer.parseInt(pluginProperties.getClientPropertyWithDefault(
                PROPERTY_KEY_ACTIVE_DIRECTORY_TRACKING_TIME_OUT, "10000"));
        pagingSize = Integer.parseInt(pluginProperties.getClientPropertyWithDefault(
                PROPERTY_KEY_ACTIVE_DIRECTORY_TRACKING_PAGING_SIZE, "1000"));
        incrementalGroupSync = Boolean.parseBoolean(pluginProperties
                .getClientProperty(PROPERTY_KEY_ACTIVE_DIRECTORY_TRACKING_ENABLED_GROUP));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void acceptGroupsOfUser(User user, ExternalEntityVisitor<ExternalGroupVO> visitor)
            throws ExternalUserGroupAccessorException {
        for (ExternalUserAuthentication auth : user.getExternalAuthentications()) {
            if (ldapConfiguration.getSystemId().equals(auth.getSystemId())) {
                String dn = auth.getAdditionalProperty();
                if (dn == null) {
                    LOGGER.info("Cannot retrieve groups of user '" + user.getAlias()
                            + "' because the user's DN wasn't synchronized yet");
                    break;
                }
                try {
                    retriever.setFilter(dn);
                    retriever.accept(visitor, incrementalGroupSync);
                } catch (Exception e) {
                    throw new ExternalUserGroupAccessorException(
                            "There was a problem getting the groups for user "
                                    + user.getAlias(), e);
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void acceptMembersOfGroup(ExternalUserGroup group,
            final ExternalEntityVisitor<Long> visitor) throws ExternalUserGroupAccessorException {
        try {
            LOGGER.debug("Accepting members of group {}:{}", group.getId(),
                    group.getAdditionalProperty());
            String groupDn = group.getAdditionalProperty();
            final Collection<Long> existingUsersOfGroup = ServiceLocator.instance()
                    .getService(UserGroupMemberManagement.class)
                    .getUsersOfGroup(group.getId(), ldapConfiguration.getSystemId());
            final LdapUserAttributesMapper ldapUserAttributesMapper = new LdapUserAttributesMapper(
                    ldapConfiguration);
            final UserManagement userManagement = ServiceLocator.instance().getService(
                    UserManagement.class);
            MemberAttributesMapper mapper = new MemberAttributesMapper(systemRangeFetchSize,
                    visitor, ldapUserAttributesMapper, userManagement, existingUsersOfGroup);
            int rangeCounter = 0;
            while (mapper.membersLeft) {
                groupRetrievalLdapTemplate.lookup(groupDn, new String[] {
                        systemMemberName + ";range=" + rangeCounter + "-"
                                + (rangeCounter + systemRangeFetchSize - 1) },
                        mapper);
                rangeCounter = rangeCounter + systemRangeFetchSize;
            }
            // Removing all others users from the group
            UserGroupMemberManagement memberManagement = ServiceLocator.instance().getService(
                    UserGroupMemberManagement.class);
            for (Long removedUser : existingUsersOfGroup) {
                memberManagement.removeUserForExternal(group.getId(), removedUser,
                        ldapConfiguration.getSystemId());
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw new ExternalUserGroupAccessorException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void acceptParentGroups(ExternalUserGroup group,
            ExternalEntityVisitor<ExternalGroupVO> visitor)
            throws ExternalUserGroupAccessorException {
        try {
            retriever.setFilter(group.getAdditionalProperty());
            retriever.accept(visitor, incrementalGroupSync);
        } catch (Exception e) {
            throw new ExternalUserGroupAccessorException(
                    "There was a problem getting the parent groups of group "
                            + group.getAlias(), e);
        }
    }

    /**
     * @return True, if a context could be created and closed, else false.
     */
    @Override
    public boolean canConnect() {
        return LdapSearchUtils.canConnectToLdapDirectory(ldapConfiguration);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ExternalGroupVO getGroup(ExternalUserGroup group) throws PluginException {
        try {
            return groupSearch.getGroup(group.getAdditionalProperty());
        } catch (DataAccessException e) {
            throw new PluginException("There was problem retrieving the group", e);
        } catch (LdapAttributeMappingException e) {
            throw new PluginException(
                    "There was problem in mapping the attributes of the retrieved group", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ExternalGroupVO getGroup(String externalGroupId) {
        Collection<ExternalGroupVO> externalGroups = groupSearch.findGroups(externalGroupId);
        if (externalGroups != null) {
            for (ExternalGroupVO externalGroupVO : externalGroups) {
                if (externalGroupVO.getExternalId() != null
                        && externalGroupVO.getExternalId().equals(externalGroupId)) {
                    return externalGroupVO;
                }
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasGroup(ExternalUserGroup group) throws PluginException {
        try {
            return groupSearch.hasGroup(group.getAdditionalProperty());
        } catch (DataAccessException e) {
            throw new PluginException(
                    "There was problem retrieving the groups existence status.",
                    e);
        }
    }

    /**
     * @return <code>true</code>
     * 
     *         {@inheritDoc}
     */
    @Override
    public boolean needsSynchronization(Date date) {
        return true;
    }

    @Override
    public void start() {
        // Do nothing.
    }

    @Override
    public void stop() {
        // Do nothing.
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean supports(ExternalSystemConfiguration externalConfiguration) {
        if (!(externalConfiguration instanceof LdapConfiguration)) {
            return false;
        }
        try {
            ldapConfiguration = (LdapConfiguration) externalConfiguration;
            groupSearch = new LdapGroupSearch(ldapConfiguration);
            LdapGroupAttributesMapper ldapGroupAttributesMapper = new LdapGroupAttributesMapper(
                    ldapConfiguration.getGroupSyncConfig()
                            .getGroupSearch().getPropertyMapping(),
                    ldapConfiguration.getSystemId(),
                    ldapConfiguration
                            .getGroupSyncConfig().isGroupIdentifierIsBinary());
            retriever = new MemberAndNonMemberModeVisitingRetriever(null, ldapConfiguration,
                    timeout, pagingSize, -1, isPagingAllowed, ldapGroupAttributesMapper);
            LdapContextSource context = LdapSearchUtils.createLdapContext(ldapConfiguration,
                    ldapGroupAttributesMapper);
            groupRetrievalLdapTemplate = new LdapTemplate(context);
            return true;
        } catch (Exception e) {
            LOGGER.error("There was an error initializing the LDAP group search.", e);
        }
        return false;
    }
}
