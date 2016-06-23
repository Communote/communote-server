package com.communote.server.core.messaging.connectors.xmpp;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.communote.common.encryption.EncryptionException;
import com.communote.common.encryption.EncryptionUtils;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.config.ApplicationConfigurationProperties;
import com.communote.server.api.core.config.type.ApplicationProperty;
import com.communote.server.api.core.config.type.ApplicationPropertyXmpp;
import com.communote.server.core.messaging.NotificationManagement;
import com.communote.server.core.service.CommunoteService;
import com.communote.server.model.messaging.MessagerConnectorType;

/**
 * Service for the XMPP messaging.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class XMPPService implements CommunoteService {

    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(XMPPService.class);

    private final String name;
    private boolean running;
    private String resource;

    /**
     * Creates a new instance of the XMPP service.
     *
     * @param name
     *            the name of the service
     */
    public XMPPService(String name) {
        this.name = name;
        this.running = false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return this.name;
    }

    /**
     * @return the XMPP resource to use
     */
    private String getResource() {
        if (resource == null) {
            resource = CommunoteRuntime.getInstance().getConfigurationManager()
                    .getStartupProperties().getInstanceName();
        }
        return resource;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEnabled() {
        // always force cache reload to avoid stale settings in clustered environment
        ApplicationConfigurationProperties props = CommunoteRuntime.getInstance()
                .getConfigurationManager().getApplicationConfigurationProperties(true);
        return xmppEnabled(props);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isRunning() {
        return running;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void start(boolean triggeredLocally) {
        // check for enabled state, default is false
        ApplicationConfigurationProperties props = CommunoteRuntime.getInstance()
                .getConfigurationManager().getApplicationConfigurationProperties(!triggeredLocally);

        String host = props.getProperty(ApplicationPropertyXmpp.HOST);
        String port = props.getProperty(ApplicationPropertyXmpp.PORT);
        String login = props.getProperty(ApplicationPropertyXmpp.LOGIN);
        String password;
        try {
            password = EncryptionUtils.decrypt(props.getProperty(ApplicationPropertyXmpp.PASSWORD),
                    props.getProperty(ApplicationProperty.INSTALLATION_UNIQUE_ID));
        } catch (EncryptionException e) {
            password = StringUtils.EMPTY;
            LOGGER.error("Decrypting the XMPP password failed. Using empty string as default", e);
        }
        XMPPConnector connector = null;
        try {
            connector = new XMPPConnector(host, port, login, password, getResource());
        } catch (IllegalArgumentException e) {
            LOGGER.error("Startig of XMPP service failed becauses of an incorrect configuration", e);
        }
        if (connector != null) {
            ServiceLocator.findService(NotificationManagement.class).registerMessagerConnector(
                    connector);
            running = true;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stop() {
        ServiceLocator.findService(NotificationManagement.class).removeMessagerConnector(
                MessagerConnectorType.XMPP.toString());
        running = false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean supportsRestart() {
        return true;
    }

    /**
     * Test whether XMPP can be started.
     *
     * @param props
     *            the application properties
     * @return true if XMPP is enabled and the configuration is correct
     */
    private boolean xmppEnabled(ApplicationConfigurationProperties props) {
        boolean result = true;
        String enabled = props.getProperty(ApplicationPropertyXmpp.ENABLED);
        if (!Boolean.parseBoolean(enabled)) {
            LOGGER.info("XMPP is disabled");
            result = false;
        } else {
            // TODO better put it in XMPPConnector?
            // check some configuration settings for correctness
            String blogSuffix = props.getProperty(ApplicationPropertyXmpp.BLOG_SUFFIX);
            String userSuffix = props.getProperty(ApplicationPropertyXmpp.USER_SUFFIX);
            if (StringUtils.isBlank(blogSuffix) || StringUtils.isBlank(userSuffix)
                    || !blogSuffix.contains("@") || !userSuffix.contains("@")) {
                LOGGER.error("XMPP configuration is not correct. XMPP will be disabled");
                result = false;
            }
        }
        return result;
    }

}
