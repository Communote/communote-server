package com.communote.plugins.mq.provider.activemq;

/**
 * Broker interface.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public interface ConfigurableBroker {
    /**
     * activates MQ broker
     * 
     * @throws Exception
     *             exception
     */
    void activateBroker() throws Exception;

    /**
     * deactivates broker
     * 
     * @throws Exception
     *             exception
     */
    void deactivateBroker() throws Exception;

    /**
     * @return whether broker is currently started or not
     */
    boolean isActive();
}
