package com.communote.server.core.virusscan;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.communote.common.virusscan.VirusScannerFactory;
import com.communote.common.virusscan.exception.InitializeException;
import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.config.ApplicationConfigurationProperties;
import com.communote.server.api.core.config.type.ApplicationPropertyVirusScanning;
import com.communote.server.core.service.CommunoteService;

/**
 * A short-lived service that restarts/reinitializes the {@link VirusScannerFactory}. It is only
 * marked as running while the {@link #start(boolean)} method is executed.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class VirusScannerService implements CommunoteService {
    private static final Logger LOG = LoggerFactory.getLogger(VirusScannerService.class);
    private final String name;
    private boolean running = false;

    /**
     * Creates a new virus scanner service.
     *
     * @param name
     *            the name of the service used for registration
     */
    public VirusScannerService(String name) {
        this.name = name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return this.name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEnabled() {
        ApplicationConfigurationProperties props = CommunoteRuntime.getInstance()
                .getConfigurationManager().getApplicationConfigurationProperties(true);
        return props.getProperty(ApplicationPropertyVirusScanning.ENABLED, true);
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
        running = true;
        ApplicationConfigurationProperties props = CommunoteRuntime.getInstance()
                .getConfigurationManager().getApplicationConfigurationProperties(!triggeredLocally);
        try {
            VirusScannerFactory.instance().init(props.getVirusScanningProperties());
        } catch (InitializeException e) {
            LOG.error("The selected virus scanner cannot be initialized", e);
        }
        running = false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stop() {
        // nothing

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean supportsRestart() {
        return true;
    }

}
