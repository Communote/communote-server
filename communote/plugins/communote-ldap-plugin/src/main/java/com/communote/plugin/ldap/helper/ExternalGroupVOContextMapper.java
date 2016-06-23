package com.communote.plugin.ldap.helper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DirContextAdapter;

import com.communote.server.core.common.ldap.LdapGroupAttributesMapper;
import com.communote.server.core.user.UserGroupManagement;
import com.communote.server.core.vo.user.group.ExternalGroupVO;
import com.communote.server.model.config.LdapGroupSyncConfiguration;
import com.communote.server.model.user.group.ExternalUserGroup;
import com.communote.server.persistence.user.group.ExternalUserGroupDao;

/**
 * Mapper for mapping a LDAP context to {@link ExternalGroupVO}
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ExternalGroupVOContextMapper implements ContextMapper {

    /** Logger. */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(ExternalGroupVOContextMapper.class);

    private final LdapGroupSyncConfiguration groupSyncConfig;
    private final ExternalUserGroupDao externalUserGroupDao;

    private final UserGroupManagement userGroupManagement;

    /**
     * Constructor.
     * 
     * @param groupSyncConfig
     *            Configuration for the group synchronization.
     * @param externalUserGroupDao
     *            Dao for external groups.
     * @param userGroupManagement
     *            UserGroupManagement to use.
     */
    public ExternalGroupVOContextMapper(LdapGroupSyncConfiguration groupSyncConfig,
            ExternalUserGroupDao externalUserGroupDao, UserGroupManagement userGroupManagement) {
        this.groupSyncConfig = groupSyncConfig;
        this.externalUserGroupDao = externalUserGroupDao;
        this.userGroupManagement = userGroupManagement;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ExternalUserGroup mapFromContext(Object contex) {
        DirContextAdapter context = (DirContextAdapter) contex;
        ExternalUserGroup result = ExternalUserGroup.Factory.newInstance();
        try {
            ExternalGroupVO externalGroup = new LdapGroupAttributesMapper(groupSyncConfig
                    .getGroupSearch()
                    .getPropertyMapping(),
                    groupSyncConfig.getLdapConfiguration().getSystemId(),
                    groupSyncConfig.isGroupIdentifierIsBinary()).mapAttributes(context.getDn()
                    .toString(), context.getAttributes());
            if (externalGroup != null && userGroupManagement != null) {
                // TODO is this a good idea to create any group?
                Long groupId = userGroupManagement.createOrUpdateExternalGroup(externalGroup);
                result = externalUserGroupDao.load(groupId);
            }
        } catch (Exception e) {
            LOGGER.error("Error during context mapping: {} ", e.getMessage());
        }
        return result;
    }
}
