package com.communote.server.model.config;

import java.util.ArrayList;
import java.util.List;

/**
 * @see com.communote.server.model.config.LdapConfiguration
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class LdapConfigurationImpl extends com.communote.server.model.config.LdapConfiguration {
    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = -8652042873959407407L;

    /**
     * clone the search bases
     *
     * @param orgList
     *            the original search bases
     * @return the cloned search bases
     */
    private List<LdapSearchBaseDefinition> cloneSearchBases(List<LdapSearchBaseDefinition> orgList) {
        List<LdapSearchBaseDefinition> clonedList = new ArrayList<LdapSearchBaseDefinition>();
        for (LdapSearchBaseDefinition def : orgList) {
            LdapSearchBaseDefinition cloneSearchBaseDef = LdapSearchBaseDefinition.Factory
                    .newInstance(def.getSearchBase(), def.isSearchSubtree());
            cloneSearchBaseDef.setId(def.getId());
            clonedList.add(cloneSearchBaseDef);
        }
        return clonedList;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LdapConfiguration deepCopy() {
        LdapSearchConfiguration userSearchConfig = this.getUserSearch();
        LdapSearchConfiguration cloneUserSearchConfig = LdapSearchConfiguration.Factory
                .newInstance(userSearchConfig.getSearchFilter(),
                        userSearchConfig.getPropertyMapping(),
                        cloneSearchBases(userSearchConfig.getSearchBases()));

        LdapConfiguration cloneConfig = LdapConfiguration.Factory.newInstance(this.getUrl(),
                this.getManagerPassword(), this.getManagerDN(), this.isUserIdentifierIsBinary(),
                this.getSaslMode(), this.getServerDomain(), this.getQueryPrefix(),
                this.isDynamicMode(), this.isAllowExternalAuthentication(), this.getSystemId(),
                this.isPrimaryAuthentication(), this.isSynchronizeUserGroups(), null,
                cloneUserSearchConfig);

        // group synchronization
        LdapGroupSyncConfiguration ldapGroupConfig = this.getGroupSyncConfig();
        if (ldapGroupConfig != null) {
            LdapSearchConfiguration cloneGroupSearch = LdapSearchConfiguration.Factory.newInstance(
                    ldapGroupConfig.getGroupSearch().getSearchFilter(), ldapGroupConfig
                            .getGroupSearch().getPropertyMapping(),
                    cloneSearchBases(ldapGroupConfig.getGroupSearch().getSearchBases()));

            LdapGroupSyncConfiguration cloneGroupSyncConfig = LdapGroupSyncConfiguration.Factory
                    .newInstance(ldapGroupConfig.isMemberMode(),
                            ldapGroupConfig.isGroupIdentifierIsBinary(), cloneConfig,
                            cloneGroupSearch);
            cloneConfig.setGroupSyncConfig(cloneGroupSyncConfig);
        }

        return cloneConfig;
    }

    @Override
    public String getConfigurationUrl() {
        return "/admin/client/ldapAuthentication";
    }

    @Override
    public String getImageApiUrl() {
        return null;
    }

}