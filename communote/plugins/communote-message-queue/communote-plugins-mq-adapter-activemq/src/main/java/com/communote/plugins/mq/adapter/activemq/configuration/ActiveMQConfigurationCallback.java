package com.communote.plugins.mq.adapter.activemq.configuration;

import javax.jms.JMSException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.communote.plugins.mq.adapter.activemq.ActiveMQAdapter;

/**
 * A callback delegating the configuration change to an adapter restart to reflect the change.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class ActiveMQConfigurationCallback {

    private final static Logger LOGGER = LoggerFactory
            .getLogger(ActiveMQConfigurationCallback.class);

    private ActiveMQAdapter adapter;

    /**
     * 
     * @param adapter
     *            the adapter to inform about a configuration change
     */
    public ActiveMQConfigurationCallback(ActiveMQAdapter adapter) {
        this.adapter = adapter;
    }

    /**
     * Called if the configuration changed. Will stop and start the adapter
     */
    public void onConfigurationChanged() {
        try {
            adapter.stopAdapter();
            adapter.initAdapter();
        } catch (JMSException e) {
            LOGGER.error("Error in restarting the adapter on a configuration changed!", e);
        }
    }
}
