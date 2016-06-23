package com.communote.plugins.mq.provider.activemq.settings;

import java.util.Set;
import java.util.UUID;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;

import com.communote.plugins.mq.adapter.activemq.configuration.ActiveMQEmbeddedConfiguration;

/**
 * This provides the embedded configuration for use by the embedded MQ client
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
@Component(immediate = true)
// do not expose the ActiveMQConfiguration directly to avoid ipojo confusion and take is the default
// active mq configuration
@Provides(specifications = { ActiveMQEmbeddedConfiguration.class })
@Instantiate
public class ActiveMQProviderEmbeddedConfiguration implements ActiveMQEmbeddedConfiguration {

    @Requires
    private MQSettingsDAO settingsDao;

    private String identifcationName = "embeddedUser";
    private String identifcationToken = UUID.randomUUID().toString() + Math.random();

    /**
     * Generate new identification token (that is the password for the embedded connection
     */
    @Override
    public void generateNewIdentification() {
        identifcationToken = UUID.randomUUID().toString() + Math.random();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getBrokerUrl() {

        String brokerUrl;

        Set<String> urls = settingsDao.getBrokerConnectorURLsWithVM();
        String tcpUrl = null;
        String vmUrl = null;
        for (String url : urls) {
            if (url.startsWith("tcp")) {
                tcpUrl = url;
            } else if (url.startsWith("vm")) {
                vmUrl = url;
            }
        }
        // if there is a manual defined vmUrl use it before the tcp url
        brokerUrl = vmUrl != null ? vmUrl : tcpUrl;

        return brokerUrl;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPassword() {
        return identifcationToken;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getUsername() {
        return identifcationName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isActive() {
        return settingsDao.isBrokerStarted();
    }

}
