package com.communote.plugins.mq.service.impl;

import java.util.Hashtable;
import java.util.Map;

import org.apache.felix.ipojo.ComponentInstance;
import org.apache.felix.ipojo.ConfigurationException;
import org.apache.felix.ipojo.Factory;
import org.apache.felix.ipojo.MissingHandlerException;
import org.apache.felix.ipojo.UnacceptableConfiguration;
import org.easymock.EasyMock;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.communote.plugins.mq.message.base.handler.CommunoteMessageHandler;

/**
 * A factory for creating TestProviderMessageConsumer objects.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ProviderMessageConsumerFactoryTest {

    /** The message consumer factory mock. */
    private Factory messageConsumerFactoryMock;

    /** The test handler mock. */
    private CommunoteMessageHandler<?> testHandlerMock;

    /** The message consumers mock. */
    private Map<CommunoteMessageHandler<?>, ComponentInstance> messageConsumersMock;

    /**
     * Inits the mocks.
     */
    @SuppressWarnings("unchecked")
    @BeforeMethod
    public void initMocks() {
        messageConsumerFactoryMock = EasyMock.createMock(Factory.class);
        testHandlerMock = EasyMock.createMock(CommunoteMessageHandler.class);
        messageConsumersMock = EasyMock.createMock(Map.class);
    }

    /**
     * Test bind message handler.
     * 
     * @throws UnacceptableConfiguration
     *             the unacceptable configuration
     * @throws MissingHandlerException
     *             the missing handler exception
     * @throws ConfigurationException
     *             the configuration exception
     */
    @SuppressWarnings("rawtypes")
    @Test
    public void testBindMessageHandler() throws UnacceptableConfiguration,
            MissingHandlerException, ConfigurationException {
        ComponentInstance ciMock = EasyMock.createMock(ComponentInstance.class);
        Hashtable<String, CommunoteMessageHandler> configuration = new Hashtable<String, CommunoteMessageHandler>();
        configuration.put("messageHandler", testHandlerMock);
        EasyMock.expect(
                messageConsumerFactoryMock
                        .createComponentInstance(configuration))
                .andReturn(ciMock);
        EasyMock.replay(messageConsumerFactoryMock);
        ProviderMessageConsumerFactoryImpl factory = new ProviderMessageConsumerFactoryImpl();
        factory.setMessageConsumerFactory(messageConsumerFactoryMock);
        factory.bindMessageHandler(testHandlerMock);
        EasyMock.verify(messageConsumerFactoryMock);
        Assert.assertNotNull(factory.getMessageConsumers().get(testHandlerMock));
    }

    /**
     * Test unbind message handler.
     */
    @Test
    public void testUnbindMessageHandler() {

        ComponentInstance componentInstanceMock = EasyMock
                .createMock(ComponentInstance.class);

        EasyMock.expect(messageConsumersMock.remove(testHandlerMock))
                .andReturn(componentInstanceMock);
        componentInstanceMock.dispose();

        EasyMock.replay(componentInstanceMock, messageConsumersMock);

        ProviderMessageConsumerFactoryImpl factory = new ProviderMessageConsumerFactoryImpl();
        factory.setMessageConsumerFactory(messageConsumerFactoryMock);
        factory.setMessageConsumers(messageConsumersMock);
        factory.unbindMessageHandler(testHandlerMock);
        EasyMock.verify(componentInstanceMock);

    }
}
