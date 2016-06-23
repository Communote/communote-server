package com.communote.server.core.security.ssl;

/**
 * <p>
 * Spring Service base class for
 * <code>com.communote.server.core.api.security.ssl.ChannelManagement</code>, provides
 * access to all services and entities referenced by this service.
 * </p>
 * 
 * @see com.communote.server.core.security.ssl.ChannelManagement
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class ChannelManagementBase
        implements com.communote.server.core.security.ssl.ChannelManagement {

    private com.communote.server.persistence.security.ssl.ChannelConfigurationDao channelConfigurationDao;

    /**
     * Sets the reference to <code>channelConfiguration</code>'s DAO.
     */
    public void setChannelConfigurationDao(
            com.communote.server.persistence.security.ssl.ChannelConfigurationDao channelConfigurationDao) {
        this.channelConfigurationDao = channelConfigurationDao;
    }

    /**
     * Gets the reference to <code>channelConfiguration</code>'s DAO.
     */
    protected com.communote.server.persistence.security.ssl.ChannelConfigurationDao getChannelConfigurationDao() {
        return this.channelConfigurationDao;
    }

    /**
     * @see com.communote.server.core.security.ssl.ChannelManagement#isForceSsl()
     */
    public Boolean isForceSsl() {
        try {
            return this.handleIsForceSsl();
        } catch (RuntimeException rt) {
            throw new com.communote.server.core.security.ssl.ChannelManagementException(
                    "Error performing 'com.communote.server.core.api.security.ssl.ChannelManagement.isForceSsl()' --> "
                            + rt,
                    rt);
        }
    }

    /**
     * Performs the core logic for {@link #isForceSsl()}
     */
    protected abstract Boolean handleIsForceSsl();

    /**
     * @see com.communote.server.core.security.ssl.ChannelManagement#isForceSsl(com.communote.server.model.security.ChannelType)
     */
    public Boolean isForceSsl(
            com.communote.server.model.security.ChannelType channelType) {
        try {
            return this.handleIsForceSsl(channelType);
        } catch (RuntimeException rt) {
            throw new com.communote.server.core.security.ssl.ChannelManagementException(
                    "Error performing 'com.communote.server.core.api.security.ssl.ChannelManagement.isForceSsl(com.communote.server.persistence.security.ChannelType channelType)' --> "
                            + rt,
                    rt);
        }
    }

    /**
     * Performs the core logic for
     * {@link #isForceSsl(com.communote.server.model.security.ChannelType)}
     */
    protected abstract Boolean handleIsForceSsl(
            com.communote.server.model.security.ChannelType channelType);

    /**
     * @see com.communote.server.core.security.ssl.ChannelManagement#loadAll()
     */
    public java.util.List<com.communote.server.model.security.ChannelConfiguration> loadAll() {
        try {
            return this.handleLoadAll();
        } catch (RuntimeException rt) {
            throw new com.communote.server.core.security.ssl.ChannelManagementException(
                    "Error performing 'com.communote.server.core.api.security.ssl.ChannelManagement.loadAll()' --> "
                            + rt,
                    rt);
        }
    }

    /**
     * Performs the core logic for {@link #loadAll()}
     */
    protected abstract java.util.List<com.communote.server.model.security.ChannelConfiguration> handleLoadAll();

    /**
     * @see 
     *      com.communote.server.core.api.security.ssl.ChannelManagement#update(java.util.List
     *      <com.communote.server.persistence.security.ssl.ChannelConfiguration>)
     */
    public void update(
            java.util.List<com.communote.server.model.security.ChannelConfiguration> channelConfigurations) {
        if (channelConfigurations == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.core.api.security.ssl.ChannelManagement.update(java.util.List<com.communote.server.persistence.security.ssl.ChannelConfiguration> channelConfigurations) - 'channelConfigurations' can not be null");
        }
        try {
            this.handleUpdate(channelConfigurations);
        } catch (RuntimeException rt) {
            throw new com.communote.server.core.security.ssl.ChannelManagementException(
                    "Error performing 'com.communote.server.core.api.security.ssl.ChannelManagement.update(java.util.List<com.communote.server.persistence.security.ssl.ChannelConfiguration> channelConfigurations)' --> "
                            + rt,
                    rt);
        }
    }

    /**
     * Performs the core logic for {@link
     * #update(java.util.List<com.communote.server.persistence.security.ssl.ChannelConfiguration>)}
     */
    protected abstract void handleUpdate(
            java.util.List<com.communote.server.model.security.ChannelConfiguration> channelConfigurations);

    /**
     * Gets the current <code>principal</code> if one has been set, otherwise returns
     * <code>null</code>.
     * 
     * @return the current principal
     */
    protected java.security.Principal getPrincipal() {
        return com.communote.server.PrincipalStore.get();
    }
}