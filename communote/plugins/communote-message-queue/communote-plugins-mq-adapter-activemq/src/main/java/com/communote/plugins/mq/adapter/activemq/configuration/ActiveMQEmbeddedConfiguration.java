package com.communote.plugins.mq.adapter.activemq.configuration;

/**
 * Marker interface to indicate that the configuration is an embedded one. Needed to clearify the
 * ipojo service injection.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public interface ActiveMQEmbeddedConfiguration extends ActiveMQConfiguration {

    /**
     * Generate new identification token (that is the password for the embedded connection)
     */
    public void generateNewIdentification();

}
