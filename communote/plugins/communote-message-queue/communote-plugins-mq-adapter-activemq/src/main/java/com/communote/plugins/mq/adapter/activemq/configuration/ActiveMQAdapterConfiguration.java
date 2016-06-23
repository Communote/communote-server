package com.communote.plugins.mq.adapter.activemq.configuration;

import org.apache.felix.ipojo.annotations.Bind;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Unbind;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.communote.common.encryption.EncryptionException;
import com.communote.common.encryption.EncryptionUtils;
import com.communote.plugins.core.services.PluginPropertyService;
import com.communote.plugins.core.services.PluginPropertyServiceException;
import com.communote.plugins.mq.adapter.activemq.ActiveMQAdapter;
import com.communote.server.api.core.config.type.ApplicationProperty;

/**
 * This is the configuration for the Active MQ Adapter. It internal holds an optional embedded
 * configuration. Depending on the property {@link #SETTING_KEY_CONNECTOR_USE_EMBEDDED} it will use
 * the embedded configuration or its own stored the database or the embedded configuration. If no
 * embedded configuration is used the system properties will have a higher priority as the database
 * s
 * 
 * This class also holds a callback which is used to indicate a change of configuration (e.g.
 * switching from embedded to non embedded configuration or by a change of the configuration in the
 * frontend.)
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
@Component
@Provides(specifications = { ActiveMQDatabaseConfiguration.class })
@Instantiate
public class ActiveMQAdapterConfiguration implements ActiveMQDatabaseConfiguration {

    private static final String SETTING_KEY_CONNECTOR_USE_EMBEDDED = "plugin.mq.client.activemq.use.embedded";
    private static final String SETTING_KEY_CONNECTOR_ACTIVE = "plugin.mq.client.activemq.connector.active";
    private static final String SETTING_KEY_CONNECTOR_URL = "plugin.mq.client.activemq.connector.url";
    private static final String SETTING_KEY_CONNECTOR_USERNAME = "plugin.mq.client.activemq.connector.username";
    private static final String SETTING_KEY_CONNECTOR_PASSWORD = "plugin.mq.client.activemq.connector.password";

    private final static Logger LOGGER = LoggerFactory.getLogger(ActiveMQAdapter.class);

    private ActiveMQEmbeddedConfiguration embeddedConfiguration;

    @Requires
    private PluginPropertyService pluginPropertyService;

    private ActiveMQConfigurationCallback callback;

    /**
     * If there is a embedded configuration set it
     * 
     * @param embeddedConfiguration
     *            the configuration
     */
    @Bind(optional = true)
    public synchronized void bindEmbeddedConfiguration(
            ActiveMQEmbeddedConfiguration embeddedConfiguration) {
        this.embeddedConfiguration = embeddedConfiguration;
        if (callback != null) {
            callback.onConfigurationChanged();
        }
    }

    /**
     * @return the url of the broker to connect to
     */
    @Override
    public String getBrokerUrl() {
        if (this.useEmbeddedConfiguration()) {
            return embeddedConfiguration.getBrokerUrl();
        }
        return loadProperty(SETTING_KEY_CONNECTOR_URL, null, false);
    }

    /**
     * 
     * @return the callback used to indicate configuration changes
     */
    public ActiveMQConfigurationCallback getCallback() {
        return callback;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPassword() {
        if (this.useEmbeddedConfiguration()) {
            return embeddedConfiguration.getPassword();
        }
        return loadProperty(SETTING_KEY_CONNECTOR_PASSWORD, null, true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getUsername() {
        if (this.useEmbeddedConfiguration()) {
            return embeddedConfiguration.getUsername();
        }
        return loadProperty(SETTING_KEY_CONNECTOR_USERNAME, null, false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isActive() {
        if (this.useEmbeddedConfiguration()) {
            return embeddedConfiguration.isActive();
        }
        String value = loadProperty(SETTING_KEY_CONNECTOR_ACTIVE, null, false);
        return Boolean.parseBoolean(value);
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
     * @param isEncoded
     *            indicates that if the parameter is from database it is an encoded one and hence
     *            needs decoding
     * @return the value to use
     */
    private String loadProperty(String settingKey, String defaultValue, boolean isEncoded) {
        String value = System.getProperty(settingKey);
        if (value == null || value.trim().length() == 0) {
            value = loadPluginProperty(settingKey, null);
            if (isEncoded) {
                try {
                    value = EncryptionUtils.decrypt(
                            value,
                            ApplicationProperty.INSTALLATION_UNIQUE_ID.getValue());
                } catch (EncryptionException e) {
                    LOGGER.error("Error decrypting value for settingKey=" + settingKey
                            + " Will ignore value.", e);
                }
            }
        }
        if (value == null || value.trim().length() == 0) {
            value = defaultValue;
        }
        return value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setCallback(ActiveMQConfigurationCallback callback) {
        this.callback = callback;
    }

    /**
     * Remove the embedded configuration
     * 
     * @param embeddedConfiguration
     *            the configuration to remove
     */
    @Unbind
    public synchronized void unbindEmbeddedConfiguration(
            ActiveMQEmbeddedConfiguration embeddedConfiguration) {
        this.embeddedConfiguration = null;
        if (callback != null) {
            callback.onConfigurationChanged();
        }
    }

    /**
     * @return true if the embedded configuration should be used
     */
    private boolean useEmbeddedConfiguration() {
        if (this.embeddedConfiguration != null) {
            String value = loadProperty(SETTING_KEY_CONNECTOR_USE_EMBEDDED,
                    Boolean.TRUE.toString(), false);
            return Boolean.valueOf(value);
        }
        return false;
    }

}
