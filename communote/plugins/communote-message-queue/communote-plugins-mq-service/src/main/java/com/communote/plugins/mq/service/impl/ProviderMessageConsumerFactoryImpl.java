package com.communote.plugins.mq.service.impl;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.apache.felix.ipojo.ComponentInstance;
import org.apache.felix.ipojo.ConfigurationException;
import org.apache.felix.ipojo.Factory;
import org.apache.felix.ipojo.MissingHandlerException;
import org.apache.felix.ipojo.UnacceptableConfiguration;
import org.apache.felix.ipojo.annotations.Bind;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Unbind;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.communote.plugins.mq.message.base.handler.CommunoteMessageHandler;
import com.communote.plugins.mq.message.base.message.BaseMessage;
import com.communote.plugins.mq.service.provider.ProviderMessageConsumerFactory;

/**
 * 
 * Factory of the CNT message consumers. Appropriate message consumer is created, as soon as a
 * message handler component is registered in the OSGi context
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
@Component(immediate = true)
@Provides
@Instantiate
public class ProviderMessageConsumerFactoryImpl implements ProviderMessageConsumerFactory {

    @Requires(filter = "(factory.name=message_consumer)")
    private Factory messageConsumerFactory;

    private static Logger LOG = LoggerFactory
            .getLogger(ProviderMessageConsumerFactoryImpl.class);

    private Map<CommunoteMessageHandler<? extends BaseMessage>, ComponentInstance> messageConsumers =
            new HashMap<CommunoteMessageHandler<? extends BaseMessage>, ComponentInstance>();

    /**
     * Is invoked when new message handler is registered in the OSGi context. Instantiates
     * appropriate provider message consumer
     * 
     * @param messageHandler
     *            handler instance
     */
    @Override
    @Bind(aggregate = true)
    public void bindMessageHandler(CommunoteMessageHandler<? extends BaseMessage> messageHandler) {
        try {
            Hashtable<String, Object> props = new Hashtable<String, Object>();
            props.put("messageHandler", messageHandler);
            ComponentInstance messageConsumer = messageConsumerFactory
                    .createComponentInstance(props);

            messageConsumers.put(messageHandler, messageConsumer);
            LOG.debug("Provider message consumer has been instantiated and bound to the message handler == "
                    + messageHandler);
        } catch (UnacceptableConfiguration e) {
            LOG.error("Message handler instance was not properly configured", e);
        } catch (MissingHandlerException e) {
            LOG.error("Exception during message handler instantiating", e);
        } catch (ConfigurationException e) {
            LOG.error(
                    "Exception during message handler instance configuration",
                    e);
        }
    }

    /**
     * 
     * only for test purposes
     * 
     * @return Handlers.
     */
    public Map<CommunoteMessageHandler<?>, ComponentInstance> getMessageConsumers() {
        return messageConsumers;
    }

    /**
     * 
     * only for test purposes
     * 
     * @param messageConsumerFactory
     *            Factory.
     */
    void setMessageConsumerFactory(Factory messageConsumerFactory) {
        this.messageConsumerFactory = messageConsumerFactory;
    }

    /**
     * 
     * only for test purposes
     * 
     * @param messageConsumers
     *            Consumers.
     */
    void setMessageConsumers(
            Map<CommunoteMessageHandler<?>, ComponentInstance> messageConsumers) {
        this.messageConsumers = messageConsumers;
    }

    /**
     * 
     * Is invoked when a communote message handler is removed from the OSGi context. The method is
     * responsible for stopping appropriate provider message consumer instance
     * 
     * @param messageHandler
     *            message handler, that was removed
     */
    @Override
    @Unbind
    public void unbindMessageHandler(
            CommunoteMessageHandler<? extends BaseMessage> messageHandler) {
        ComponentInstance consumerToBeRemoved = messageConsumers.remove(messageHandler);
        if (consumerToBeRemoved != null) {
            consumerToBeRemoved.dispose();
            LOG.debug("Provider message consumer has been unbound");
        } else {
            LOG.warn("Provider message consumer for the removed message handler was not found. MessageHandler == "
                    + messageHandler);
        }
    }
}
