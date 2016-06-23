package com.communote.plugins.mq.adapter.activemq.configuration;

/**
 * Marker interface to indicate that the configuration is a database one. Needed to clearify the
 * ipojo service injection.
 * 
 * This class also uses a callback which is used to indicate a change of configuration (e.g.
 * switching from embedded to non embedded configuration or by a change of the configuration in the
 * frontend)
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public interface ActiveMQDatabaseConfiguration extends ActiveMQConfiguration {

    /**
     * The callback is used to inform someone about the confiugration change
     * 
     * @param callback
     *            the callback
     */
    public void setCallback(ActiveMQConfigurationCallback callback);

}
