package com.communote.server.core.security.ssl;

/**
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public interface ChannelManagement {

    /**
     * <p>
     * Check if SSL is required for the current used channel.
     * </p>
     */
    public Boolean isForceSsl();

    /**
     * <p>
     * Check if SSL is required for the requested channel.
     * </p>
     */
    public Boolean isForceSsl(
            com.communote.server.model.security.ChannelType channelType);

    /**
     * <p>
     * Loads all channel configurations for a client.
     * </p>
     */
    public java.util.List<com.communote.server.model.security.ChannelConfiguration> loadAll();

    /**
     * <p>
     * Updates the channel configruations in the database. The process starts with removing all
     * existing records before the new records are created. At last the channel configuration is
     * invalidated.
     * </p>
     */
    public void update(
            java.util.List<com.communote.server.model.security.ChannelConfiguration> channelConfigurations);

}
