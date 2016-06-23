package com.communote.plugins.mq.provider.jms;

import javax.jms.ConnectionFactory;

/**
 * Provides the connection factory for an embedded broker. The implementation is done in the
 * provider plugin (e.g. acive mq provider), so its correctly using the same instance and not trying
 * to use a new instance (aka broker).
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public interface EmbeddedConnectionFactoryProvider {

    /**
     * 
     * @param connectorUrl
     *            the url to connect to
     * @return the connection factory to be used
     */
    public ConnectionFactory createConnectionFactory(String connectorUrl);
}
