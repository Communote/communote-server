package com.communote.server.core.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.communote.server.api.core.application.ApplicationInformation;
import com.communote.server.api.core.application.Runtime;
import com.communote.server.api.core.bootstrap.ApplicationInitializationException;
import com.communote.server.api.core.bootstrap.BootstrapException;
import com.communote.server.api.core.bootstrap.CustomInitializer;
import com.communote.server.api.core.bootstrap.InitializationCompleteListener;
import com.communote.server.api.core.bootstrap.InitializationStatus;
import com.communote.server.api.core.config.ConfigurationManager;
import com.communote.server.api.core.installer.CommunoteInstaller;
import com.communote.server.core.bootstrap.ApplicationInitializer;
import com.communote.server.core.bootstrap.InstallationPreparedCallback;
import com.communote.server.core.installer.CommunoteInstallerImpl;

/**
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class DefaultRuntime implements Runtime {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultRuntime.class);
    private final ApplicationInformation information;
    private final ConfigurationManager configManager;
    private final ApplicationInitializer initializer;
    private final InstallationPreparedCallback installationPreparedCallback;
    private boolean startInvoked;
    private CommunoteInstaller installer;

    public DefaultRuntime(ApplicationInformation information, ConfigurationManager configManager,
            ApplicationInitializer initializer, InstallationPreparedCallback callback) {
        this.information = information;
        this.configManager = configManager;
        this.initializer = initializer;
        this.installationPreparedCallback = callback;
    }

    @Override
    public void addCustomInitializer(CustomInitializer customInitializer) {
        initializer.addInitializer(customInitializer);
    }

    @Override
    public void addInitializationCompleteListener(InitializationCompleteListener listener) {
        this.initializer.addInitializationCompleteListener(listener);
    }

    @Override
    public void addInitializationCondition(String conditionId) {
        this.initializer.addInitializationCondition(conditionId);
    }

    @Override
    public void fulfillInitializationCondition(String conditionId) {
        this.initializer.fulfillInitializationCondition(conditionId);

    }

    @Override
    public ApplicationInformation getApplicationInformation() {
        return information;
    }

    @Override
    public ConfigurationManager getConfigurationManager() {
        return configManager;
    }

    @Override
    public InitializationStatus getInitializationStatus() {
        if (initializer == null) {
            return new InitializationStatus(InitializationStatus.Type.IN_PROGRESS);
        }
        return initializer.getInitializationStatus();
    }

    /**
     * @return the installer to install Communote or null if Communote is already installed
     */
    @Override
    public CommunoteInstaller getInstaller() {
        if (installer == null) {
            if (startInvoked) {
                throw new IllegalStateException("Communote is already installed");
            } else {
                throw new IllegalStateException("CommunoteInstallerImpl is not yet initialized."
                        + " You must call the start method before this method can be used.");
            }
        } else {
            if (configManager.getStartupProperties().isInstallationDone() && isCoreInitialized()) {
                installer = null;
                throw new IllegalStateException("Communote is already installed");
            }
            return installer;
        }
    }

    @Override
    public boolean isCoreInitialized() {
        return initializer.isCoreInitialized();
    }

    @Override
    public boolean isInitialized() {
        return initializer.isInitialized();
    }

    /**
     * Starts the bootstrapping. If the bootstrapping fails a {@link BootstrapException} will be
     * thrown.
     *
     */
    @Override
    public synchronized void start() {
        if (startInvoked) {
            return;
        }
        LOGGER.info("Starting Communote {} (build time: {})", information.getBuildNumberWithType(),
                information.getBuildTime());
        // check whether the installation was finished
        boolean installationDone = configManager.getStartupProperties().isInstallationDone();
        if (installationDone) {
            try {
                this.initializer.initializeApplication();
            } catch (ApplicationInitializationException e) {
                LOGGER.error("FATAL: Application initialization failed.", e);
                throw e;
            }
        } else {
            // stop initialization to let the installer do it's work
            this.installer = new CommunoteInstallerImpl(initializer);
            if (installationPreparedCallback != null) {
                installationPreparedCallback.installationPrepared();
            }
        }
        startInvoked = true;
    }

    @Override
    public synchronized void stop() {
        if (!startInvoked) {
            return;
        }
        initializer.shutdown();
        startInvoked = false;
    }
}
