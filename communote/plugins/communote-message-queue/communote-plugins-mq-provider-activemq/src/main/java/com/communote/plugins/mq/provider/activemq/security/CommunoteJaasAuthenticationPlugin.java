package com.communote.plugins.mq.provider.activemq.security;

import org.apache.activemq.broker.Broker;
import org.apache.activemq.security.JaasDualAuthenticationPlugin;

import com.communote.plugins.mq.adapter.activemq.configuration.ActiveMQEmbeddedConfiguration;
import com.communote.plugins.mq.provider.activemq.settings.MQSettingsDAO;

/**
 * Communote Jaas Authentication Plugin
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class CommunoteJaasAuthenticationPlugin extends JaasDualAuthenticationPlugin {

    private MQSettingsDAO settingsDao;
    private ActiveMQEmbeddedConfiguration embeddedConfiguration;

    /**
     * 
     * @param settingsDao
     *            the settings dao
     * @param embeddedConfiguration
     *            the embedded configuration
     */
    public CommunoteJaasAuthenticationPlugin(
            MQSettingsDAO settingsDao, ActiveMQEmbeddedConfiguration embeddedConfiguration) {
        this.settingsDao = settingsDao;
        this.embeddedConfiguration = embeddedConfiguration;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Broker installPlugin(Broker broker) {
        initialiseJaas();
        return new CommunoteJaasAuthenticationBroker(settingsDao, embeddedConfiguration, broker,
                getConfiguration(),
                getSslConfiguration());
    }

}
