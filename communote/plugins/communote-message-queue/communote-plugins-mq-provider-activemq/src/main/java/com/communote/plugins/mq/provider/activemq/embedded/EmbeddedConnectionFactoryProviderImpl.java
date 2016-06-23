package com.communote.plugins.mq.provider.activemq.embedded;

import javax.jms.ConnectionFactory;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;

import com.communote.plugins.mq.provider.jms.EmbeddedConnectionFactoryProvider;

/**
 * Provides the {@link ActiveMQConnectionFactory} for an embedded broker. Caches the factory for the
 * last url used.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
@Component(immediate = true)
@Provides
@Instantiate
public class EmbeddedConnectionFactoryProviderImpl implements EmbeddedConnectionFactoryProvider {

    private ActiveMQConnectionFactory factory;
    private String connectorUrl;

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized ConnectionFactory createConnectionFactory(String connectorUrl) {
        if (factory == null || this.connectorUrl == null || !this.connectorUrl.equals(connectorUrl)) {
            factory = new ActiveMQConnectionFactory(connectorUrl);
            this.connectorUrl = connectorUrl;
        }
        return factory;
    }

}
