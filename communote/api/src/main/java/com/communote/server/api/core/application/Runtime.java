package com.communote.server.api.core.application;

import com.communote.server.api.core.bootstrap.BootstrapException;
import com.communote.server.api.core.bootstrap.CustomInitializer;
import com.communote.server.api.core.bootstrap.InitializationCompleteListener;
import com.communote.server.api.core.bootstrap.InitializationStatus;
import com.communote.server.api.core.config.ConfigurationManager;
import com.communote.server.api.core.installer.CommunoteInstaller;

/**
 * Communote runtime environment.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public interface Runtime {
    /**
     * Add an initializer to do custom initializations during startup. The added initializer will be
     * called after the core components were initialized. Initializers which are added after the
     * application was initialized are ignored.
     *
     * @param customInitializer
     *            the additional initializer
     */
    public void addCustomInitializer(CustomInitializer customInitializer);

    /**
     * Add a listener that is informed after the application was completely initialized.
     *
     * @param listener
     *            the listener to add
     */
    void addInitializationCompleteListener(InitializationCompleteListener listener);

    /**
     * Add a condition which needs to be fulfilled before the application is considered as
     * completely initialized. Adding a condition after the application was initialized has no
     * effect.
     *
     * @param conditionId
     *            the ID of the condition
     */
    void addInitializationCondition(String conditionId);

    /**
     * Mark a condition that was previously added with {@link #addInitializationCondition(String)}
     * as fulfilled. If this was the last unfulfilled init condition and the core is already
     * initialized the application will be considered as completely initialized and the
     * initialization complete listeners will be called. If the provided condition was not added the
     * call is ignored.
     *
     * @param conditionId
     *            the ID of the condition to fulfill.
     */
    void fulfillInitializationCondition(String conditionId);

    /**
     * @return information about the running Communote
     */
    ApplicationInformation getApplicationInformation();

    /**
     * @return manager for accessing and modifying configuration
     */
    ConfigurationManager getConfigurationManager();

    /**
     * @return the current status of the initialization
     */
    public InitializationStatus getInitializationStatus();

    /**
     * @return the installer to install Communote or null if Communote is already installed.
     */
    public CommunoteInstaller getInstaller();

    /**
     * @return whether the core application is initialized. This will be true if Communote is
     *         installed and the core application with all components is initialized. Some init
     *         conditions might not be fulfilled yet.
     */
    public boolean isCoreInitialized();

    /**
     * @return whether the application is completely initialized. This will be true if
     *         {@link #isCoreInitialized()} returns true and all conditions are fulfilled.
     */
    public boolean isInitialized();

    /**
     * Starts the bootstrapping. If the bootstrapping fails a {@link BootstrapException} will be
     * thrown.
     */
    void start();

    /**
     * Stop the runtime and free any resources held.
     */
    void stop();

}
