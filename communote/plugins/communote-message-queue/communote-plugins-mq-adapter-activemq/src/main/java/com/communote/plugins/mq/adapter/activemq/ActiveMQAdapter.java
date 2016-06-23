package com.communote.plugins.mq.adapter.activemq;

import java.io.EOFException;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.ServiceController;
import org.apache.felix.ipojo.annotations.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.communote.plugins.core.services.PluginPropertyService;
import com.communote.plugins.mq.adapter.activemq.configuration.ActiveMQAdapterConfiguration;
import com.communote.plugins.mq.adapter.activemq.configuration.ActiveMQConfigurationCallback;
import com.communote.plugins.mq.adapter.activemq.configuration.ActiveMQDatabaseConfiguration;
import com.communote.plugins.mq.provider.jms.EmbeddedConnectionFactoryProvider;
import com.communote.plugins.mq.provider.jms.JMSAdapter;

/**
 * iPojo component intended for abstracting ActiveMQ provider from the CNT MQ plug-in. It is used in
 * conjunction with JMS adapter plug-in, that provides JMSAdapter interface.
 * 
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
@Component
@Provides
@Instantiate
public class ActiveMQAdapter implements JMSAdapter {
    /**
     * Runnable, implementing connection to the MQ provider process
     * 
     * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
     */
    private class ProviderConnecting implements Runnable, ExceptionListener {

        private static final int CONNECTION_TRY_PERIOD = 5000;

        /**
		 */
        public ProviderConnecting() {
        }

        /*
         * (non-Javadoc)
         * 
         * @see javax.jms.ExceptionListener#onException(javax.jms.JMSException)
         */
        @Override
        public void onException(JMSException e) {
            LOGGER.error("MQ Connection Exception was thrown. " + e.getMessage());

            // is thrown when broker becomes unavailable, needs to be
            // reconnected. Must be checked whether adapter was already
            // connected, to avoid endless loop.
            if (jmsAdapterActive && e.getCause() instanceof EOFException) {
                try {
                    connection.close();
                } catch (JMSException e1) {
                    LOGGER.error("Exception during stopping connection to MQ");
                }
                session = null;
                dest = null;
                jmsAdapterActive = false;
                initAdapter();
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see Runnable#run()
         */
        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                ConnectionFactory factory = getFactory();
                if (factory != null) {
                    try {
                        if (configuration.getUsername() != null) {
                            connection = factory.createConnection(configuration.getUsername(),
                                    configuration.getPassword());
                        }
                        else {
                            connection = factory.createConnection();
                        }

                        connection.setExceptionListener(this);
                        connection.start();

                        jmsAdapterActive = true;
                        LOGGER.info("Connection to ActiveMQ broker has been established");
                        // connection has been established -> stop looping
                        break;
                    } catch (JMSException e) {
                        LOGGER.info("No ActiveMQ provider under "
                                + currentConnectorUrl + " ("
                                + currentConnectorUrl
                                + ") is available. "
                                + e.getMessage());
                        LOGGER.debug(e.getMessage(), e);
                        try {
                            if (connection != null) {
                                connection.close();
                            }
                        } catch (JMSException e1) {
                            LOGGER.error(
                                    "Exception during connection closing was thrown",
                                    e1);
                        }
                    }
                }

                try {
                    Thread.sleep(CONNECTION_TRY_PERIOD);
                } catch (InterruptedException e1) {
                    Thread.currentThread().interrupt();
                }
            }

        }
    }

    /** The LOG. */
    private final static Logger LOGGER = LoggerFactory.getLogger(ActiveMQAdapter.class);

    @Requires
    private ActiveMQDatabaseConfiguration configuration;

    /** The connection. */
    private Connection connection;

    /**
     * broker connection thread
     */
    private Thread connectionThread;

    @Requires
    private PluginPropertyService pluginPropertyService;

    /**
     * Actually, this is optional, for the case we will run the adapter without the provider plugin
     */
    @Requires
    private EmbeddedConnectionFactoryProvider embeddedConnectionFactoryProvider;

    /**
     * is used to postpone this service publication, until ActiveMQ broker is available. Blocking of
     * the initAdapter() method for this aim does not work properly if this component should be
     * removed before the connection is established (component cannot be invalidated unless it is
     * validated)
     */
    @ServiceController
    private boolean jmsAdapterActive = false;

    private Session session;

    private Queue dest;

    private ConnectionFactory factory;

    // the current connector url is the url that has been used to create the factory. it is store
    // seperatly since the broker uri of the factory might have been altered with and hence not
    // usefull for comparison
    private String currentConnectorUrl = null;

    /**
     * Set the factory
     * 
     * @param connectorUrl
     *            the connector url
     */
    private synchronized void createConnectionFactory(String connectorUrl) {
        if (factory == null && connectorUrl != null) {
            factory = new ActiveMQConnectionFactory(
                    connectorUrl);
            currentConnectorUrl = connectorUrl;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.communote.plugins.mq.provider.jms.JMSAdapter#getDestination()
     */
    @Override
    public Queue getDestination() throws JMSException {
        if (dest == null) {
            dest = this.session.createQueue(ActiveMQAdapterConfiguration.COMMUNOTE_QUEUE_DEFAULT);
        }

        return dest;
    }

    /**
     * 
     * @return get the connection factory
     */
    public ConnectionFactory getFactory() {

        String connectorUrl = configuration.getBrokerUrl();

        if (factory != null
                && (currentConnectorUrl == null || !currentConnectorUrl.equals(connectorUrl))) {
            factory = null;
            currentConnectorUrl = null;
        }
        if (factory == null && connectorUrl != null) {

            synchronized (this) {
                if (factory == null && connectorUrl != null) {
                    if (this.embeddedConnectionFactoryProvider != null) {

                        factory = embeddedConnectionFactoryProvider
                                .createConnectionFactory(connectorUrl);

                        if (factory != null) {
                            LOGGER.info("Using EmbeddedConnectionFactoryProvider for url "
                                    + connectorUrl);
                        }
                    } else {

                        LOGGER.info("Using ConnectionFactoryProvider not from Provider Plugin for url "
                                + connectorUrl);
                        createConnectionFactory(connectorUrl);
                    }
                }
            }
        }

        return factory;

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.communote.plugins.mq.provider.jms.JMSAdapter#createSession()
     */
    @Override
    public Session getSession() throws JMSException {
        if (this.session == null) {
            this.session = connection.createSession(false,
                    Session.AUTO_ACKNOWLEDGE);
        }
        return this.session;
    }

    /**
     * Validate call back. Is used to initialize connection to the ActiveMQ broker.
     */
    public void initAdapter() {

        if (configuration.isActive()) {
            if (connectionThread != null) {
                if (connectionThread.isAlive()) {
                    // thread is alive but we do another one. why?
                    LOGGER.warn("connectionThread is alive but we do another one. why?",
                            new Exception(""));
                    connectionThread.interrupt();
                }

            }
            connectionThread = new Thread(new ProviderConnecting(), "ActiveMQ-Adapter");
            connectionThread.setContextClassLoader(this.getClass().getClassLoader());
            connectionThread.start();
        }
    }

    /**
     * Starting the adapter by setting the callback on the configuration and then start it
     */
    @Validate
    public void onStart() {
        this.configuration.setCallback(new ActiveMQConfigurationCallback(this));
        this.initAdapter();
    }

    /**
     * Stopping the adapter
     * 
     * @throws JMSException
     *             in case stopping failed
     */
    @Invalidate
    public void onStop() throws JMSException {
        this.configuration.setCallback(null);
        stopAdapter();
    }

    /**
     * Only for test purposes.
     * 
     * @param connection
     *            connection instance to be set
     */
    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    /**
     * stops active mq adapter
     * 
     * @throws JMSException
     *             exception
     */
    public void stopAdapter() throws JMSException {
        jmsAdapterActive = false;

        try {
            if (connectionThread != null && connectionThread.isAlive()) {
                connectionThread.interrupt();
            }
        } finally {
            try {
                if (this.session != null) {
                    this.session.close();
                }
            } finally {
                if (this.connection != null) {
                    this.connection.close();
                }
            }
        }
    }

}
