package com.communote.plugins.mq.provider.activemq.settings;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.communote.plugins.core.services.PluginPropertyService;
import com.communote.plugins.core.services.PluginPropertyServiceException;
import com.communote.plugins.mq.adapter.activemq.configuration.ActiveMQConfiguration;
import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.security.AuthorizationException;

/**
 * MQ Settings to get and change the settings of the Active MQ Broker. All settings are application
 * specific.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
@Component
@Provides
@Instantiate
public class MQDatabaseSettingsDAO implements MQSettingsDAO {

    private static final Logger LOGGER = LoggerFactory.getLogger(MQDatabaseSettingsDAO.class);

    private static final String DEFAULT_COMMUNOTE_BROKER_NAME = "default_communote_broker";

    private static final String SETTING_KEY_BROKER_STARTED = "communote.plugin.mq.provider.activemq.started";

    private static final String SETTING_KEY_CONNECTOR_URLS = "communote.plugin.mq.provider.activemq.connector.urls";

    private static final String SETTING_KEY_DATA_DIRECTORY = "communote.plugin.mq.provider.activemq.data.dir";

    private static final String SETTING_KEY_BROKER_HOSTNAME = "communote.plugin.mq.provider.activemq.broker.hostname";

    private static final String SETTING_KEY_TCP_PORT = "communote.plugin.mq.provider.activemq.broker.tcp.port";

    private static final String SETTING_KEY_SSL_PORT = "communote.plugin.mq.provider.activemq.broker.ssl.port";

    private static final String SETTING_KEY_VM_ENABLE = "communote.plugin.mq.provider.activemq.broker.vm.enable";
    private static final String SETTING_KEY_TCP_ENABLE = "communote.plugin.mq.provider.activemq.broker.tcp.enable";
    private static final String SETTING_KEY_SSL_ENABLE = "communote.plugin.mq.provider.activemq.broker.ssl.enable";

    private static final String SETTING_KEY_SSL_FORCE = "communote.plugin.mq.provider.activemq.broker.ssl.force";

    private static final String SETTING_KEY_SSL_CLIENT_AUTH_FORCE =
            "plugin.mq.provider.activemq.broker.ssl.client.auth.force";

    private static final String SETTING_KEY_JMX_REMOTE_ENABLE = "communote.plugin.mq.provider.activemq.jmx.remote.enable";

    private static final String SETTING_KEY_JMX_MONITORING_ENABLE = "communote.plugin.mq.provider.activemq.jmx.monitoring.enable";

    private static final String SETTING_KEY_JMX_PORT = "communote.plugin.mq.provider.activemq.jmx.port";

    @Requires
    private PluginPropertyService pluginPropertyService;

    private final static String DEFAULT_TCP_PORT = "61616";

    private final static String DEFAULT_SSL_PORT = "61617";

    private final static String DEFAULT_JMX_PORT = "1099";

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.communote.plugins.mq.provider.activemq.settings.MQSettingsDAO#getBrokerConnectorURLs()
     */
    @Override
    public Set<String> getBrokerConnectorURLs() {
        String connectors = loadConnectorsSetting();

        HashSet<String> urls = new HashSet<String>();
        if (connectors != null) {
            urls.addAll(Arrays.asList(connectors.split(",")));
        }
        return urls;
    }

    @Override
    public Set<String> getBrokerConnectorURLsWithVM() {
        Set<String> urls = this.getBrokerConnectorURLs();
        if (this.isEnableVM()) {
            urls.add(this.getVMConnectorURL());
        }
        return urls;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.communote.plugins.mq.provider.activemq.settings.MQSettingsDAO#getBrokerName()
     */
    @Override
    public String getBrokerName() {
        return DEFAULT_COMMUNOTE_BROKER_NAME;
    }

    /**
     * @return communote data dir
     */
    private File getCommunoteDataDir() {
        return CommunoteRuntime.getInstance().getConfigurationManager().getStartupProperties()
                .getDataDirectory();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.communote.plugins.mq.provider.activemq.settings.MQSettingsDAO#getDataDirectory()
     */
    @Override
    public String getDataDirectory() {
        String dataDir = loadPluginProperty(SETTING_KEY_DATA_DIRECTORY, null);
        if (dataDir == null) {
            dataDir = getCommunoteDataDir().getAbsolutePath()
                    + System.getProperty("file.separator") + "mq";
        }
        return dataDir;
    }

    /**
     * 
     * @return the default ssl url
     */
    private String getDefaultSSLConnectorURL() {
        String url = "ssl://" + getUrlHostname() + ":" + getSSLPort();
        if (isForceSSLClientAuthentication()) {
            url += "?needClientAuth=true";
        }
        return url;
    }

    /**
     * 
     * @return the default tcp url
     */
    private String getDefaultTcpConnectorURL() {
        return "tcp://" + getUrlHostname() + ":" + getTcpPort();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.communote.plugins.mq.provider.activemq.settings.MQSettingsDAO#getJmxDomainName()
     */
    @Override
    public String getJmxDomainName() {
        return "communote-activemq-broker";
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.communote.plugins.mq.provider.activemq.settings.MQSettingsDAO#getJmxPort()
     */
    @Override
    public int getJmxPort() {
        String port = loadProperty(SETTING_KEY_JMX_PORT, DEFAULT_JMX_PORT);
        try {
            return Integer.parseInt(port);
        } catch (Exception e) {
            LOGGER.warn("Error parsing JMX Port. Using default=" + DEFAULT_JMX_PORT, e);
        }
        return Integer.parseInt(DEFAULT_JMX_PORT);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.communote.plugins.mq.provider.activemq.settings.MQSettingsDAO#getQueueName()
     */
    @Override
    public String getQueueName() {
        return ActiveMQConfiguration.COMMUNOTE_QUEUE_DEFAULT;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.communote.plugins.mq.provider.activemq.settings.MQSettingsDAO#getReplyQueueNamePrefix()
     */
    @Override
    public String getReplyQueueNamePrefix() {
        return ActiveMQConfiguration.COMMUNOTE_QUEUE_DEFAULT_REPLY;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.communote.plugins.mq.provider.activemq.settings.MQSettingsDAO#getSSLPort()
     */
    @Override
    public String getSSLPort() {
        return loadProperty(SETTING_KEY_SSL_PORT, DEFAULT_SSL_PORT);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.communote.plugins.mq.provider.activemq.settings.MQSettingsDAO#getTcpPort()
     */
    @Override
    public String getTcpPort() {
        return loadProperty(SETTING_KEY_TCP_PORT, DEFAULT_TCP_PORT);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.communote.plugins.mq.provider.activemq.settings.MQSettingsDAO#getUrlHostname()
     */
    @Override
    public String getUrlHostname() {
        return loadProperty(SETTING_KEY_BROKER_HOSTNAME, "localhost");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getVMConnectorURL() {
        /**
         * marshal=true => the messages is transformed into a wire format (aka low level
         * representation). This is the easiest solution to avoid class cast exception in an osgi
         * environment on handling a message
         * 
         * create=false => in case the adapter starts before the broker avoid to create broker. This
         * will lead to conflicts.
         * 
         * waitForStart => the broker may take some time to start, since create is false it is not
         * really needed, it just avoids to connect to early producing log warnings that may confuse
         */

        return "vm://" + DEFAULT_COMMUNOTE_BROKER_NAME
                + "?marshal=true&create=false&waitForStart=2500";
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.communote.plugins.mq.provider.activemq.settings.MQSettingsDAO#isBrokerStarted()
     */
    @Override
    public boolean isBrokerStarted() {
        String configuration = loadPluginProperty(SETTING_KEY_BROKER_STARTED,
                Boolean.FALSE.toString());
        return Boolean.parseBoolean(configuration);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.communote.plugins.mq.provider.activemq.settings.MQSettingsDAO#isEnableSSL()
     */
    @Override
    public boolean isEnableSSL() {
        String enable = loadProperty(SETTING_KEY_SSL_ENABLE, Boolean.FALSE.toString());
        return Boolean.parseBoolean(enable);
    }

    @Override
    public boolean isEnableTCP() {
        String enable = loadProperty(SETTING_KEY_TCP_ENABLE, Boolean.FALSE.toString());
        return Boolean.parseBoolean(enable);
    }

    @Override
    public boolean isEnableVM() {
        String enable = loadProperty(SETTING_KEY_VM_ENABLE, Boolean.TRUE.toString());
        return Boolean.parseBoolean(enable);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.communote.plugins.mq.provider.activemq.settings.MQSettingsDAO#isForceSSL()
     */
    @Override
    public boolean isForceSSL() {
        String configuration = loadPluginProperty(SETTING_KEY_SSL_FORCE, Boolean.FALSE.toString());
        return Boolean.parseBoolean(configuration);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.communote.plugins.mq.provider.activemq.settings.MQSettingsDAO#isForceSSLClientAuthentication
     * ()
     */
    @Override
    public boolean isForceSSLClientAuthentication() {
        String configuration = loadPluginProperty(SETTING_KEY_SSL_CLIENT_AUTH_FORCE,
                Boolean.FALSE.toString());
        return Boolean.parseBoolean(configuration);
    }

    @Override
    public boolean isJmxMonitoringEnabled() {
        String enable = loadProperty(SETTING_KEY_JMX_MONITORING_ENABLE, Boolean.TRUE.toString());
        return Boolean.parseBoolean(enable);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.communote.plugins.mq.provider.activemq.settings.MQSettingsDAO#isJmxEnabled()
     */
    @Override
    public boolean isJmxRemoteEnabled() {
        String enable = loadProperty(SETTING_KEY_JMX_REMOTE_ENABLE, Boolean.FALSE.toString());
        return Boolean.parseBoolean(enable);
    }

    /**
     * @return connector urls, either stored in DB or default one
     */
    private String loadConnectorsSetting() {

        String connectorUrl = loadProperty(SETTING_KEY_CONNECTOR_URLS, null);
        if (StringUtils.isBlank(connectorUrl)) {
            List<String> urls = new ArrayList<String>();

            if (isEnableTCP()) {
                urls.add(getDefaultTcpConnectorURL());
            }
            if (isEnableSSL()) {
                urls.add(getDefaultSSLConnectorURL());
            }

            if (urls.size() > 0) {
                connectorUrl = StringUtils.join(urls, ",");
            }
        }
        if (StringUtils.isBlank(connectorUrl)) {
            if (!isEnableVM()) {
                LOGGER.error("No connector url defined. Either set " + SETTING_KEY_CONNECTOR_URLS
                        + " or enable at least on of vm,tcp,ssl using " + SETTING_KEY_VM_ENABLE
                        + "true " + SETTING_KEY_TCP_ENABLE + "=true " + SETTING_KEY_SSL_ENABLE
                        + "=true");
            }
            connectorUrl = null;
        }
        return connectorUrl;
    }

    /**
     * Loads existing configuration setting if any, or default value
     * 
     * @param settingKey
     *            key of the setting to be loaded
     * @param defValue
     *            value of the setting to be used if requested setting doesn't exist. If set to null
     *            returns null in the case the requested property does not exist
     * @return loaded ConfigurationSetting object, or null if does not exist and no default value
     *         was specified
     */
    private String loadPluginProperty(String settingKey, String defValue) {
        String propValue = defValue;
        try {
            propValue = pluginPropertyService
                    .getApplicationPropertyWithDefault(settingKey, defValue);
        } catch (PluginPropertyServiceException e) {
            LOGGER.error("Error reading settings, key=" + settingKey, e);
        }
        return propValue;
    }

    /**
     * Load the property by exploiting the database and the system property
     * 
     * @param settingKey
     *            the key to use
     * @param defaultValue
     *            the default value
     * @return the value to use
     */
    private String loadProperty(String settingKey, String defaultValue) {
        String value = System.getProperty(settingKey);
        if (value == null || value.trim().length() == 0) {
            value = loadPluginProperty(settingKey, null);
        }
        if (value == null || value.trim().length() == 0) {
            value = defaultValue;
        }
        return value;
    }

    @Override
    public void setBrokerStarted(boolean isStarted) throws AuthorizationException  {
        setMQPluginProperty(SETTING_KEY_BROKER_STARTED, String.valueOf(isStarted));
    }

    @Override
    public void setDataDirectory(String dataDirectoryPath) throws AuthorizationException {
        setMQPluginProperty(SETTING_KEY_DATA_DIRECTORY, dataDirectoryPath);
    }

    @Override
    public void setEnableJmxRemote(boolean enableJmx) throws AuthorizationException {
        setMQPluginProperty(SETTING_KEY_JMX_REMOTE_ENABLE, Boolean.toString(enableJmx));
    }

    @Override
    public void setEnableSSL(boolean enableSSL) throws AuthorizationException {
        setMQPluginProperty(SETTING_KEY_SSL_ENABLE, Boolean.toString(enableSSL));
    }

    @Override
    public void setEnableTCP(boolean enableTCP) throws AuthorizationException {
        setMQPluginProperty(SETTING_KEY_TCP_ENABLE, Boolean.toString(enableTCP));
    }

    @Override
    public void setEnableVM(boolean enableVM) throws AuthorizationException {
        setMQPluginProperty(SETTING_KEY_VM_ENABLE, Boolean.toString(enableVM));
    }

    @Override
    public void setForceSSL(boolean forceSSL) throws AuthorizationException {
        setMQPluginProperty(SETTING_KEY_SSL_FORCE, Boolean.toString(forceSSL));
    }

    @Override
    public void setForceSSLClientAuthentication(boolean needClientAuth) throws AuthorizationException {
        setMQPluginProperty(SETTING_KEY_SSL_CLIENT_AUTH_FORCE, Boolean.toString(needClientAuth));
    }

    /**
     * updates existing configuration setting
     * 
     * @param key
     *            parameter key
     * 
     * @param value
     *            configuration setting to be updated
     * @throws AuthorizationException in case the current client is not the global client
     */
    private void setMQPluginProperty(String key, String value) throws AuthorizationException {
        pluginPropertyService.setApplicationProperty(key, value);
    }

}
