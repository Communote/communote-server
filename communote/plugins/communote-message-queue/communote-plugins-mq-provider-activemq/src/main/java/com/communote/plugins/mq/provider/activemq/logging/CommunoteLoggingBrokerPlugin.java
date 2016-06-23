package com.communote.plugins.mq.provider.activemq.logging;

import org.apache.activemq.broker.ProducerBrokerExchange;
import org.apache.activemq.broker.util.LoggingBrokerPlugin;
import org.apache.activemq.command.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * logging broker plugin for Communote MQ
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class CommunoteLoggingBrokerPlugin extends LoggingBrokerPlugin {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(CommunoteLoggingBrokerPlugin.class);

    @Override
    public void send(ProducerBrokerExchange producerExchange,
            Message messageSend) throws Exception {
        LOGGER.debug("New Message from {} is received. Message Headers == {}. Destination == {}",
                new Object[] { producerExchange
                        .getConnectionContext().getConnection()
                        .getRemoteAddress(), messageSend.getProperties(),
                        messageSend.getDestination().getQualifiedName() });
        LOGGER.trace("Receivied Message Content == {}", messageSend.getContent());
        super.send(producerExchange, messageSend);
    }
}
