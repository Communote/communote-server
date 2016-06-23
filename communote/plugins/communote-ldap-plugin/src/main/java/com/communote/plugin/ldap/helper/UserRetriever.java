package com.communote.plugin.ldap.helper;

import java.util.List;

import org.springframework.ldap.core.CollectingNameClassPairCallbackHandler;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.ContextMapperCallbackHandler;
import org.springframework.ldap.core.LdapTemplate;

import com.communote.plugin.ldap.PropertyKeys;
import com.communote.server.core.user.UserManagement;
import com.communote.server.model.config.LdapConfiguration;
import com.communote.server.model.config.LdapSearchBaseDefinition;
import com.communote.server.model.user.User;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class UserRetriever extends Retriever<User> implements PropertyKeys {

    private final List<LdapSearchBaseDefinition> userSearchBases;
    private UserManagement userManagement;
    private final LdapConfiguration ldapConfiguration;

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
     *            The maximal size of requestet pages.
     * @param timeout
     *            The timeout for connections.
     * @param isPagingAllowed
     *            True, if paging is allowed.
     * 
     */
    public UserRetriever(LdapConfiguration ldapConfiguration,
            LdapTemplate ldapTemplate,
            long internalHighestCommittedUSN, long externalHighestCommittedUSN, int pagingSize,
            int timeout, boolean isPagingAllowed) {
        super(ldapTemplate, timeout, pagingSize, internalHighestCommittedUSN,
                externalHighestCommittedUSN, isPagingAllowed);
        this.ldapConfiguration = ldapConfiguration;
        this.userSearchBases = this.ldapConfiguration.getUserSearch().getSearchBases();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected CollectingNameClassPairCallbackHandler getCollectingNameClassPairCallbackHandler() {
        return new ContextMapperCallbackHandler(
                new UserContextMapper(ldapConfiguration, userManagement));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ContextMapper getContextMapper() {
        return new UserContextMapper(ldapConfiguration, userManagement);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<LdapSearchBaseDefinition> getSearchBaseDefinitions() {
        return userSearchBases;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getSearchFilter() {
        return this.ldapConfiguration.getUserSearch().getSearchFilter();
    }

    /**
     * @param userManagement
     *            the userManagement to set
     */
    public void setUserManagement(UserManagement userManagement) {
        this.userManagement = userManagement;
    }
}
