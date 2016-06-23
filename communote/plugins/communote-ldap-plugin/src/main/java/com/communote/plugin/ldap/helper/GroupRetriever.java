package com.communote.plugin.ldap.helper;

import java.util.List;

import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.transaction.compensating.manager.ContextSourceTransactionManager;

import com.communote.plugin.ldap.PropertyKeys;
import com.communote.server.core.user.UserGroupManagement;
import com.communote.server.model.config.LdapConfiguration;
import com.communote.server.model.config.LdapSearchBaseDefinition;
import com.communote.server.model.user.group.ExternalUserGroup;
import com.communote.server.persistence.user.group.ExternalUserGroupDao;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class GroupRetriever extends Retriever<ExternalUserGroup> implements PropertyKeys {

    private final List<LdapSearchBaseDefinition> groupSearchBases;
    private final LdapConfiguration ldapConfiguration;
    private final ContextSourceTransactionManager contextSourceTransactionManager;
    private ExternalUserGroupDao externalUserGroupDao;
    private UserGroupManagement userGroupManagement;

    /**
     * Constructor.
     * 
     * @param ldapConfiguration
     *            The LDAP configuration to use.
     * @param ldapTemplate
     *            The template for LDAP.
     * @param internalHighestCommittedUSN
     *            The usn to use.
     * @param externalHighestCommittedUSN
     *            The usn from the external system.
     * @param pagingSize
     *            The maximal size of requested pages.
     * @param timeout
     *            The timeout for connections.
     * @param isPagingAllowed
     *            True, if paging is allowed.
     * 
     */
    public GroupRetriever(LdapConfiguration ldapConfiguration,
            LdapTemplate ldapTemplate,
            long internalHighestCommittedUSN, long externalHighestCommittedUSN, int pagingSize,
            int timeout, boolean isPagingAllowed) {
        super(ldapTemplate, timeout, pagingSize, internalHighestCommittedUSN,
                externalHighestCommittedUSN, isPagingAllowed);
        this.ldapConfiguration = ldapConfiguration;
        this.groupSearchBases = this.ldapConfiguration.getGroupSyncConfig().getGroupSearch()
                .getSearchBases();
        this.contextSourceTransactionManager = new ContextSourceTransactionManager();
        this.contextSourceTransactionManager.setContextSource(ldapTemplate.getContextSource());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ContextMapper getContextMapper() {
        return new ExternalGroupVOContextMapper(ldapConfiguration.getGroupSyncConfig(),
                externalUserGroupDao, userGroupManagement);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<LdapSearchBaseDefinition> getSearchBaseDefinitions() {
        return groupSearchBases;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getSearchFilter() {
        return this.ldapConfiguration.getGroupSyncConfig().getGroupSearch()
                .getSearchFilter();
    }

    /**
     * Method to set the dao.
     * 
     * @param externalUserGroupDao
     *            Dao.
     */
    public void setExternalUserGroupDao(ExternalUserGroupDao externalUserGroupDao) {
        this.externalUserGroupDao = externalUserGroupDao;
    }

    /**
     * @param userGroupManagement
     *            the userGroupManagement to set
     */
    public void setUserGroupManagement(UserGroupManagement userGroupManagement) {
        this.userGroupManagement = userGroupManagement;
    }
}
