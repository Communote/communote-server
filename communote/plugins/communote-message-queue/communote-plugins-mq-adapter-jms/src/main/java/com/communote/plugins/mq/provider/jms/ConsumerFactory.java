package com.communote.plugins.mq.provider.jms;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Queue;
import javax.jms.Session;

import org.apache.felix.ipojo.annotations.Bind;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Unbind;
import org.apache.felix.ipojo.annotations.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.communote.plugins.mq.service.provider.ProviderMessageConsumer;
import com.communote.plugins.mq.service.provider.TransferMessage;

/**
 * Component intended for creating appropriate JMS message consumer for each message consumer
 * registered in the OSGi context.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
@Component
@Instantiate
public class ConsumerFactory {

    /** The provider. */
    @Requires
    private JMSAdapter provider;

    @Requires
    private JMSMessageSender jmsSender;

    /** The LOG. */
    private static Logger LOG = LoggerFactory.getLogger(ConsumerFactory.class);

    /** The jms message consumers. */
    private Map<ProviderMessageConsumer, JMSMessageConsumer> jmsMessageConsumers =
            new HashMap<ProviderMessageConsumer, JMSMessageConsumer>();

    /**
     * 
     * is invoked when new provider message consumer is registered in the OSGi context.
     * 
     * @param consumer
     *            registered provider message consumer
     */
    @Bind(aggregate = true, optional = true)
    public void bindMessageConsumer(ProviderMessageConsumer consumer) {
        try {
            MessageConsumer jmsConsumer = getSession().createConsumer(
                    getDestination(),
                    getMessageTypeSelector(consumer.getConsumedMessageType(),
                            consumer.getConsumedMessageVersion()));
            JMSMessageConsumerImpl cntJMSMessageConsumer = new JMSMessageConsumerImpl();
            cntJMSMessageConsumer.setCommunoteMessageConsumer(consumer);
            cntJMSMessageConsumer.setJmsMessageConsumer(jmsConsumer);
            cntJMSMessageConsumer.setJmsSender(jmsSender);
            jmsMessageConsumers.put(consumer, cntJMSMessageConsumer);
            cntJMSMessageConsumer.start();
            LOG.debug("JMS message consumer has been instantiated and bound.");
        } catch (JMSException e) {
            LOG.error("JMS message consumer has not been instantiated. "
                    + consumer.getClass().getName(), e);
        }
    }

    /**
     * @return destination
     * @throws JMSException
     *             exception
     */
    private Queue getDestination() throws JMSException {
        return provider.getDestination();
    }

    /**
     * creates message selector. It is used by the JMS message consumer, to receive only relevant
     * messages
     * 
     * @param messageType
     *            type of the message
     * @param messageVersion
     *            version of the message
     * @return selector expression
     */
    private String getMessageTypeSelector(String messageType,
            String messageVersion) {
        // TYPE = 'TYPE_VALUE' AND VERSION = 'VERSION_VALUE'
        return TransferMessage.HEADER_MESSAGE_TYPE + " = " + "'" + messageType
                + "' AND " + TransferMessage.HEADER_MESSAGE_VERSION + " = '"
                + messageVersion + "'";
    }

    /**
     * @return session
     * @throws JMSException
     *             exception
     */
    private Session getSession() throws JMSException {
        return provider.getSession();
    }

    /**
     * validates iPOJO instance
     */
    @Validate
    public void init() {
        // restores jms message consumers in the case they were destroyed.
        // Actually they are destroyed every time, this component stops, the
        // difference is only when component first time is instantiated and new
        // message consumers have came
        Set<ProviderMessageConsumer> consumers = jmsMessageConsumers.keySet();
        for (ProviderMessageConsumer providerMessageConsumer : consumers) {
            if (jmsMessageConsumers.get(providerMessageConsumer) == null) {
                bindMessageConsumer(providerMessageConsumer);
            }
        }
    }

    /**
     * only for test purposes.
     * 
     * @param jmsMessageConsumers
     *            the jms message consumers
     */
    void setJmsMessageConsumers(
            Map<ProviderMessageConsumer, JMSMessageConsumer> jmsMessageConsumers) {
        this.jmsMessageConsumers = jmsMessageConsumers;
    }

    /**
     * only for test purposes
     * 
     * @param provider
     *            the provider to set
     */
    void setProvider(JMSAdapter provider) {
        this.provider = provider;
    }

    /**
     * is invoked to properly stop the factory
     */
    @Invalidate
    public void stopConsumerFactory() {
        Set<ProviderMessageConsumer> messageConsumers = jmsMessageConsumers
                .keySet();
        for (ProviderMessageConsumer messageConsumer : messageConsumers) {
            jmsMessageConsumers.get(messageConsumer).stop();
            jmsMessageConsumers.put(messageConsumer, null);
        }
        LOG.info("JMS Consumer Factory was stopped");
    }

    /**
     * is invoked, when a provider message consumer is removed from the OSGi context. This method
     * stops appropriate JMS message consumer
     * 
     * @param consumer
     *            the consumer
     */
    @Unbind(aggregate = true)
    public void unbindMessageConsumer(ProviderMessageConsumer consumer) {
        JMSMessageConsumer consumerToBeRemoved = jmsMessageConsumers
                .remove(consumer);
        if (consumerToBeRemoved != null) {
            consumerToBeRemoved.stop();
            LOG.debug("JMS message consumer has been closed");
        } else {
            LOG.warn("JMS message consumer for the removed provider message "
                    + "consumer was not found. ProviderMessageConsumer == "
                    + consumer);
        }
    }

}
