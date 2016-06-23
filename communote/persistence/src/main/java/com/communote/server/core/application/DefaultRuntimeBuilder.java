package com.communote.server.core.application;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.communote.server.api.core.application.ApplicationInformation;
import com.communote.server.api.core.application.Runtime;
import com.communote.server.api.core.application.RuntimeBuilder;
import com.communote.server.api.core.bootstrap.BootstrapException;
import com.communote.server.api.core.bootstrap.CustomInitializer;
import com.communote.server.api.core.config.ConfigurationManager;
import com.communote.server.core.bootstrap.ApplicationInitializer;
import com.communote.server.core.bootstrap.ApplicationPreparedCallback;
import com.communote.server.core.bootstrap.InstallationPreparedCallback;
import com.communote.server.core.config.ConfigurationPropertiesManager;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class DefaultRuntimeBuilder implements RuntimeBuilder {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultRuntimeBuilder.class);
    private static final String LOGGING_PROPERTIES_FILENAME = "log4j.properties";
    private static final DefaultRuntimeBuilder INSTANCE = new DefaultRuntimeBuilder();

    public static DefaultRuntimeBuilder getInstance() {
        return INSTANCE;
    }

    private DefaultRuntime builtRuntime;

    private final ApplicationInitializer initializer;
    private InstallationPreparedCallback installationPreparedCallback;
    private ApplicationInformationProvider informationProvider;
    private List<CustomInitializer> customInitializers;

    private String applicationDirectory;

    private DefaultRuntimeBuilder() {
        this.initializer = new ApplicationInitializer();
        customInitializers = new ArrayList<>();
    }

    /**
     * Add the location of an XML file which contains Spring bean definitions that should be
     * included when creating the ApplicationContext. Adding locations after build method was called
     * has no effect.
     *
     * @param configLocation
     *            an absolute location of a configuration resource with bean definitions to load.
     *            The string should start with classpath: or file: for loading classpath or file
     *            resources.
     */
    public void addApplicationContextConfigLocation(String configLocation) {
        initializer.addApplicationContextConfigLocation(configLocation);
    }

    /**
     * Add a custom initializer which should be passed to the runtime after build was called
     *
     * @param customInitializer
     *            the initializer
     */
    public void addCustomInitializer(CustomInitializer customInitializer) {
        if (builtRuntime == null) {
            this.customInitializers.add(customInitializer);
        }
    }

    @Override
    public synchronized Runtime build() {
        if (builtRuntime == null) {
            if (applicationDirectory == null) {
                throw new BootstrapException("The application directory is not set");
            }
            if (informationProvider == null) {
                informationProvider = new ManifestApplicationInformationProvider(
                        applicationDirectory);
            }
            ApplicationInformation appInfo = informationProvider.load();
            ConfigurationManager configManager = createConfigurationManager(applicationDirectory);
            initLogging(configManager);
            this.builtRuntime = new DefaultRuntime(appInfo, configManager, initializer,
                    installationPreparedCallback);
            for (CustomInitializer customInitializer : customInitializers) {
                this.builtRuntime.addCustomInitializer(customInitializer);
            }
            customInitializers = null;
        }
        return this.builtRuntime;
    }

    private ConfigurationManager createConfigurationManager(String applicationRealPath) {
        // this will throw a ConfigurationInitializationException if the startup or core
        // properties cannot be read
        try {
            return new ConfigurationPropertiesManager(applicationRealPath);
        } catch (Exception e) {
            throw new BootstrapException(
                    "Initializing the configuration properties manager failed.", e);
        }
    }

    /**
     * Initialize the logging with log4j and observe the logging configuration file to support
     * modifications at runtime. Should be run from a synchronized context.
     */
    private void initLogging(ConfigurationManager configManager) {
        try {
            File configDir = configManager.getStartupProperties().getConfigurationDirectory();
            File logFile = new File(configDir, LOGGING_PROPERTIES_FILENAME);
            if (logFile.exists()) {
                PropertyConfigurator.configureAndWatch(logFile.getCanonicalPath());
            } else {
                LOG.warn("Logging properties file " + logFile.getCanonicalFile()
                        + " does not exist.");
            }
        } catch (Exception e) {
            LOG.error("Error initalizing logger!", e);
        }
    }

    /**
     * Set the directory where the application was extracted to
     *
     * @param applicationDirectory
     *            the absolute file system path pointing to the directory where the web application
     *            was extracted to
     */
    public void setApplicationDirectory(String applicationDirectory) {
        this.applicationDirectory = applicationDirectory;
    }

    public void setApplicationInformationProvider(ApplicationInformationProvider provider) {
        informationProvider = provider;
    }

    /**
     * Set the callback to invoke after basic preparations like reading configurations, setting up
     * logging and building the Spring ApplicationContext are completed. If Communote is already
     * installed this callback is invoked within the startup process. If Communote is not yet
     * installed this callback is called as soon as the installation is done. The callback is only
     * invoked once. Setting the callback after startup has no effect.
     *
     * @param applicationPreparedCallback
     *            the callback to invoke
     */
    public void setApplicationPreparedCallback(
            ApplicationPreparedCallback applicationPreparedCallback) {
        initializer.setApplicationPreparedCallback(applicationPreparedCallback);
    }

    /**
     * Set the callback to invoke during startup if Communote is not yet installed. If Communote is
     * already installed this callback is ignored.
     *
     * @param installationPreparedCallback
     *            the callback to invoke
     */
    public void setInstallationPreparedCallback(
            InstallationPreparedCallback installationPreparedCallback) {
        this.installationPreparedCallback = installationPreparedCallback;
    }
}
