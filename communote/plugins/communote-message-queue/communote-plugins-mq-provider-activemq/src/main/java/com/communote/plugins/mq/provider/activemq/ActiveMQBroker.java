package com.communote.plugins.mq.provider.activemq;

import java.io.File;
import java.net.URI;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.net.ssl.SSLContext;

import org.apache.activemq.broker.BrokerPlugin;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.broker.SslContext;
import org.apache.activemq.broker.jmx.BrokerViewMBean;
import org.apache.activemq.broker.jmx.ManagementContext;
import org.apache.activemq.broker.jmx.QueueViewMBean;
import org.apache.activemq.broker.jmx.SubscriptionViewMBean;
import org.apache.activemq.security.AuthorizationPlugin;
import org.apache.activemq.store.PersistenceAdapter;
import org.apache.activemq.store.kahadb.KahaDBStore;
import org.apache.commons.lang3.StringUtils;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.communote.plugins.mq.adapter.activemq.configuration.ActiveMQEmbeddedConfiguration;
import com.communote.plugins.mq.provider.activemq.logging.CommunoteLoggingBrokerPlugin;
import com.communote.plugins.mq.provider.activemq.monitor.data.BrokerQueue;
import com.communote.plugins.mq.provider.activemq.monitor.data.MessageHandlerMQConsumer;
import com.communote.plugins.mq.provider.activemq.security.ActiveMQProviderSecurityHelper;
import com.communote.plugins.mq.provider.activemq.security.CommunoteJaasAuthenticationPlugin;
import com.communote.plugins.mq.provider.activemq.settings.MQSettingsDAO;

/**
 * Delivers ActiveMQ broker.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */

@Component
@Provides
@Instantiate
public class ActiveMQBroker implements ConfigurableBroker, MonitorableBroker {

    /** The LOG. */
    private static Logger LOGGER = LoggerFactory.getLogger(ActiveMQBroker.class);

    /** The broker. */
    private BrokerService broker;

    @Requires(proxy = false)
    private MQSettingsDAO settingsDao;

    @Requires(proxy = false)
    private ActiveMQEmbeddedConfiguration embeddedConfiguration;

    private final String loginConfURL;

    private String brokerViewBeanObjectName;

    /**
     * @param bc
     *            bundle context
     */
    public ActiveMQBroker(BundleContext bc) {
        this.loginConfURL = bc.getBundle().getResource("login.config")
                .toString();
    }

    /**
     * starts ActiveMQ broker.
     * 
     * @throws Exception
     *             the exception
     */
    @Override
    public void activateBroker() throws Exception {

        try {
            startBroker();

            settingsDao.setBrokerStarted(true);

        } catch (Exception e) {
            LOGGER.error("Error starting Broker: " + e.getMessage(), e);

            LOGGER.info("Stopping Broker due to errors on start.");

            try {
                stopBroker();

            } catch (Throwable th) {
                LOGGER.error("Exception during stopping. ", th);
            }

            throw e;
        }
    }

    /**
     * stops ActiveMQ broker. Is invoked explicitly by administrator.
     * 
     * @throws Exception
     *             the exception
     */
    @Override
    public void deactivateBroker() throws Exception {
        invalidateBrokerInstance();
        settingsDao.setBrokerStarted(false);
    }

    @Override
    public String getBrokerName() throws MessageQueueJmxException {
        String settingsName = settingsDao.getBrokerName();
        String viewBeanName = "";
        if (settingsName == null) {
            settingsName = "";
        }

        BrokerViewMBean viewMBean;

        viewMBean = getMBean(brokerViewBeanObjectName, BrokerViewMBean.class);
        if (viewMBean != null) {
            viewBeanName = viewMBean.getBrokerName();
            if (!StringUtils.equals(settingsName, viewBeanName)) {
                settingsName += " (" + viewBeanName + ")";
            }
        }

        return settingsName;
    }

    @Override
    public Set<BrokerQueue> getBrokerQueues() throws MessageQueueJmxException {
        Set<BrokerQueue> res = new HashSet<BrokerQueue>();
        ObjectName[] queueObjects = new ObjectName[0];

        BrokerViewMBean viewMBean = getMBean(brokerViewBeanObjectName, BrokerViewMBean.class);
        if (viewMBean != null) {
            queueObjects = viewMBean.getQueues();
        }
        for (ObjectName objectName : queueObjects) {
            QueueViewMBean queue = getMBean(objectName, QueueViewMBean.class);
            BrokerQueue brokerQueue = new BrokerQueue();
            brokerQueue.setName(queue.getName());
            brokerQueue.setDispatchedMessagesCount(queue.getDequeueCount());
            brokerQueue.setPendingMessagesCount(queue.getQueueSize());
            res.add(brokerQueue);
        }
        return res;
    }

    /**
     * Get the mbean for the given object name
     * 
     * @param objectName
     *            the name
     * @param clazz
     *            the type
     * @return the mbean
     * @throws MessageQueueJmxException
     *             in case of an error finding the mbean
     */
    @SuppressWarnings("unchecked")
    private <B> B getMBean(ObjectName objectName, Class<B> clazz) throws MessageQueueJmxException {
        Object objectBean;
        try {

            objectBean = broker.getManagementContext().newProxyInstance(objectName, clazz, true);

        } catch (Exception e) {
            throw new MessageQueueJmxException("Error getting mbean for " + objectName, e);
        }

        if (clazz.isInstance(objectBean)) {
            return ((B) objectBean);
        }
        throw new MessageQueueJmxException("Mbean for " + objectName
                + " is not an instance for class " + clazz + " objectBean: " + objectBean);
    }

    /**
     * Get the mbean for the given object name
     * 
     * @param objectName
     *            the name
     * @param clazz
     *            the type
     * @return the mbean, null if the objectname is null
     * @throws MessageQueueJmxException
     *             in case of an error finding the mbean
     */
    private <B> B getMBean(String objectName, Class<B> clazz) throws MessageQueueJmxException {
        if (objectName == null) {
            return null;
        }
        try {
            return getMBean(new ObjectName(objectName), clazz);
        } catch (MalformedObjectNameException e) {
            throw new MessageQueueJmxException("Error getting mbean for " + objectName, e);
        } catch (NullPointerException e) {
            throw new MessageQueueJmxException("Error getting mbean for " + objectName, e);
        }
    }

    @Override
    public MessageHandlerMQConsumer[] getMessageHandlerConsumers() throws MessageQueueJmxException {
        Set<MessageHandlerMQConsumer> res = new HashSet<MessageHandlerMQConsumer>();
        ObjectName[] subscriberObjects = new ObjectName[0];

        BrokerViewMBean viewMBean = getMBean(brokerViewBeanObjectName, BrokerViewMBean.class);
        if (viewMBean != null) {
            subscriberObjects = viewMBean.getQueueSubscribers();
        }
        for (ObjectName objectName : subscriberObjects) {
            SubscriptionViewMBean subscriber = getMBean(objectName, SubscriptionViewMBean.class);
            MessageHandlerMQConsumer consumer = new MessageHandlerMQConsumer();
            consumer.setSelector(subscriber.getSelector());
            consumer.setDispatchedMessagesCount(subscriber.getDequeueCounter());
            consumer.setPendingMessagesCount(subscriber
                    .getMessageCountAwaitingAcknowledge());
            res.add(consumer);
        }
        MessageHandlerMQConsumer[] sortedRes = new MessageHandlerMQConsumer[res
                .size()];
        res.toArray(sortedRes);
        Arrays.sort(sortedRes);
        return sortedRes;
    }

    /**
     * is invoked by OSGi. If during previous session the broker was not stopped explicitly by
     * administrator, it will be started now
     * 
     * @throws Exception
     *             exception
     */
    @Validate
    public void initBrokerInstance() throws Exception {
        if (settingsDao.isBrokerStarted()) {
            activateBroker();
        }
    }

    /**
     * Init the ssl, takes the system default so far
     * 
     * @throws NoSuchAlgorithmException
     *             security context could not be applied
     * 
     */
    private void initSsl() throws NoSuchAlgorithmException {
        SslContext sslContext = new SslContext(null, null, null);
        sslContext.setSSLContext(SSLContext.getDefault());

        broker.setSslContext(sslContext);
    }

    /**
     * is invoked by OSGi. This invocation should not change flag isStarted in settings. During next
     * start broker will be automatically started
     * 
     * @throws Exception
     *             exception
     */
    @Invalidate
    public void invalidateBrokerInstance() throws Exception {

        stopBroker();

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.communote.plugins.mq.provider.activemq.ConfigurableBroker#isActive()
     */
    @Override
    public boolean isActive() {
        return broker != null && broker.isStarted();
    }

    /**
     * starts broker
     * 
     * @throws Exception
     *             exception
     */
    private void startBroker() throws Exception {

        LOGGER.info("MQ Broker Starting ...");

        // generate new identification on new broker start
        this.embeddedConfiguration.generateNewIdentification();

        this.settingsDao.isForceSSL();

        broker = new BrokerService();
        broker.setBrokerName(settingsDao.getBrokerName());
        broker.setPopulateJMSXUserID(true);

        initSsl();

        CommunoteLoggingBrokerPlugin loggingPlugin = new CommunoteLoggingBrokerPlugin();

        // jaas TODO this should be a general configuration
        System.setProperty("java.security.auth.login.config", loginConfURL);
        AuthorizationPlugin authorizationPlugin = ActiveMQProviderSecurityHelper
                .createAuthorizationPlugin(
                        settingsDao.getQueueName(), settingsDao.getReplyQueueNamePrefix());

        CommunoteJaasAuthenticationPlugin authenticationPlugin = new CommunoteJaasAuthenticationPlugin(
                settingsDao,
                embeddedConfiguration);
        authenticationPlugin.setConfiguration("activemq-domain");

        broker.setPlugins(new BrokerPlugin[] { authenticationPlugin,
                authorizationPlugin, loggingPlugin });

        // persistence
        PersistenceAdapter kahaPersistenceAdapter = new KahaDBStore();
        kahaPersistenceAdapter.setDirectory(new File(settingsDao
                .getDataDirectory()));
        broker.setPersistenceAdapter(kahaPersistenceAdapter);

        for (String brokerURL : settingsDao.getBrokerConnectorURLs()) {

            if (brokerURL.startsWith("vm")) {
                broker.setVmConnectorURI(new URI(brokerURL));
            } else {
                broker.addConnector(brokerURL);
            }
        }

        // jmx
        broker.setUseJmx(settingsDao.isJmxMonitoringEnabled());

        ManagementContext managementContext = new ManagementContext();
        managementContext.setJmxDomainName(settingsDao.getJmxDomainName());
        managementContext.setConnectorPort(settingsDao.getJmxPort());

        if (!settingsDao.isJmxRemoteEnabled()) {
            // do not create the mbean server that will start on the remote part, just allow the
            // access to jmx via virtual machine
            managementContext.setCreateMBeanServer(false);
            managementContext.setFindTigerMbeanServer(true);
            managementContext.setUseMBeanServer(true);
            managementContext.setCreateConnector(false);
            managementContext.setAllowRemoteAddressInMBeanNames(false);

        }
        if (settingsDao.isJmxMonitoringEnabled()) {
            brokerViewBeanObjectName = settingsDao.getJmxDomainName() + ":BrokerName="
                    + settingsDao.getBrokerName() + ",Type=Broker";
        }

        LOGGER.info("JmxMonitoringEnabled: " + settingsDao.isJmxMonitoringEnabled()
                + " JmxRemoteEnabled: " + settingsDao.isJmxRemoteEnabled());

        broker.setManagementContext(managementContext);

        broker.start();

        LOGGER.info("Waiting for MQ Broker to start ...");

        broker.waitUntilStarted();

        String urls = "" + settingsDao.getBrokerConnectorURLs();
        if (settingsDao.isEnableVM()) {
            urls += " + VM Url: " + settingsDao.getVMConnectorURL();
        }
        LOGGER.info("MQ Broker has been started on URLs: " + urls);
    }

    private void stopBroker() throws Exception {

        if (broker != null) {

            try {

                LOGGER.info("Stopping MQ Broker ...");
                broker.stop();

                LOGGER.info("Waiting for MQ Broker to stop ...");
                broker.waitUntilStopped();

                LOGGER.info("MQ Broker has been stopped.");
            } finally {

                broker = null;
            }
        }

    }
}
