package com.communote.server.core.osgi;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Dictionary;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.felix.fileinstall.internal.DirectoryWatcher;
import org.apache.felix.fileinstall.internal.FileInstall;
import org.apache.felix.framework.Felix;
import org.apache.felix.framework.util.FelixConstants;
import org.apache.felix.main.AutoProcessor;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleException;
import org.osgi.framework.BundleListener;
import org.osgi.framework.Constants;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.launch.Framework;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.communote.common.properties.PropertiesUtils;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.bootstrap.InitializationCompleteListener;
import com.communote.server.api.core.config.StartupProperties;

/**
 * Service for managing the OSGi Framework.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
@Service("oSGiManagement")
public class OSGiManagement implements BundleListener, InitializationCompleteListener {

    /** A property key for symbolic name, which could be used accross the whole application. */
    public final static String PROPERTY_KEY_SYMBOLIC_NAME = "symbolicName";

    private static final String CORE_PROPERTIES_LOCATION = "com/communote/server/core/osgi/core_osgi.properties";

    /** Logger. */
    private final static Logger LOG = LoggerFactory.getLogger(OSGiManagement.class);

    private final Collection<CommunoteBundleListener> listeners = new ArrayList<>();

    private final Set<String> startedBundles = new HashSet<>();

    /** OSGi Framework */
    private Framework framework = null;

    private Dictionary<String, String> propertiesDictionary;

    private final List<String> frameworkPropertiesLocations = new ArrayList<>();

    private FileInstall fileInstall;

    /**
     * Adds the bundle.
     *
     * @param frameworkProperties
     *            Framework properties.
     *
     * @param startLevelAsString
     *            The start level. May contain a prefix "level_".
     * @param defaultStartLevel
     *            Default start level, if the start level is not valid.
     * @param file
     *            Bundle to add (only if jar-File).
     */
    private void addBundleForStartLevel(Properties frameworkProperties, String startLevelAsString,
            int defaultStartLevel, File file) {
        if (!file.getName().endsWith(".jar")) {
            LOG.info("Ignoring non-bundle file: " + file.getAbsolutePath());
            return;
        }
        startLevelAsString = startLevelAsString.replace("level_", "");
        int startLevel = NumberUtils.toInt(startLevelAsString, defaultStartLevel);
        if (startLevel <= 0) {
            startLevel = 1;
        }
        String startLevelProperty = "felix.auto.start." + startLevel;
        String startLevelPropertyValue = frameworkProperties.getProperty(startLevelProperty);
        try {
            if (StringUtils.isBlank(startLevelPropertyValue)) {
                startLevelPropertyValue = "\"" + file.toURI().toURL().toExternalForm() + "\"";
            } else {
                startLevelPropertyValue = startLevelPropertyValue + " \"" + file.toURI().toURL()
                        + "\"";
            }
            frameworkProperties.setProperty(startLevelProperty, startLevelPropertyValue);
        } catch (MalformedURLException e) {
            LOG.error(e.getMessage());
        }
    }

    /**
     * Add a location to look for an properties file which defines additional settings that should
     * be passed to OSGi framework when it is created. The settings will override the default
     * settings of the core. An exception is the property
     * <code>org.osgi.framework.system.packages.extra</code>. This property will be merged by
     * appending the value after a comma. Adding locations after the framework was started has no
     * effect.
     *
     * @param location
     *            a location of a properties file on the classpath or filesystem. The former has to
     *            start with classpath: and the latter with file:
     */
    public void addFrameworkPropertiesLocation(String location) {
        if (framework == null) {
            if (location.startsWith("classpath:") || location.startsWith("file:")) {
                frameworkPropertiesLocations.add(location);
                LOG.debug("Added framework properties location {}", location);
            }
        }
    }

    /**
     * @param listener
     *            Listener.
     */
    public void addListener(CommunoteBundleListener listener) {
        this.listeners.add(listener);
    }

    /**
     * @param listeners
     *            List of listeners.
     */
    @Autowired(required = false)
    public void addListeners(Collection<CommunoteBundleListener> listeners) {
        this.listeners.addAll(listeners);
    }

    /**
     * Listens to bundle changes.
     *
     * @param event
     *            The event.
     */
    @Override
    public void bundleChanged(BundleEvent event) {
        switch (event.getType()) {
        case BundleEvent.STARTED:
            startedBundles.add(event.getBundle().getSymbolicName());
            startedBundles.add(event.getBundle().getSymbolicName().toLowerCase().replace(".", "-"));
            break;
        default:
            startedBundles.remove(event.getBundle().getSymbolicName());
            startedBundles.remove(event.getBundle().getSymbolicName().toLowerCase()
                    .replace(".", "-"));
        }
    }

    /**
     * @return the framework or null if not yet started
     */
    public Framework getFramework() {
        return framework;
    }

    /**
     * @return The listeners.
     */
    public Collection<CommunoteBundleListener> getListeners() {
        return listeners;
    }

    @PostConstruct
    private void init() {
        CommunoteRuntime.getInstance().addInitializationCompleteListener(this);
    }

    @Override
    public void initializationComplete() {
        // if stop was called and application context was recreated this might be an outdated
        // instance that is still registered as InitializationCompleteListener. In that case just
        // ignore the call.
        if (ServiceLocator.findService(OSGiManagement.class) == this) {
            try {
                start();
                LOG.info("OSGi Framework started.");
            } catch (BundleException e) {
                throw new BeanCreationException(
                        "Starting OSGi framework failed because of a BundleException.", e);
            }
        } else {
            LOG.debug("Ignoring initializationComplete invocation for this outdated instance");
        }
    }

    /**
     * Scans the plugins folder for included bundles and specific start level folders.
     *
     * @param frameworkProperties
     *            The framework properties, to add this.
     * @param rootPathToSystemPlugins
     *            The root path the folder including system bundles.
     */
    private void initSystemBundles(Properties frameworkProperties, String rootPathToSystemPlugins) {
        LOG.info("Scanning for system bundles: {}", rootPathToSystemPlugins);
        File rootDirectory = new File(rootPathToSystemPlugins);
        if (!rootDirectory.isDirectory()) {
            LOG.warn("The root folder for plugins may not be a file.");
            return;
        }
        Integer defaultStartLevel = NumberUtils.toInt(
                frameworkProperties.getProperty(FelixConstants.BUNDLE_STARTLEVEL_PROP),
                FelixConstants.FRAMEWORK_DEFAULT_STARTLEVEL);
        for (File file : rootDirectory.listFiles()) {
            if (file.isFile()) {
                addBundleForStartLevel(frameworkProperties, defaultStartLevel.toString(),
                        defaultStartLevel, file);
            } else {
                LOG.debug("Scanning directory for bundles: {}", file.getAbsolutePath());
                for (File subFile : file.listFiles()) {
                    if (subFile.isFile()) {
                        addBundleForStartLevel(frameworkProperties, file.getName(),
                                defaultStartLevel, subFile);
                    } else {
                        LOG.error("Bundle directories must not contain directories: {}",
                                file.getAbsolutePath());
                    }
                }
            }
        }
    }

    /**
     * @param symbolicName
     *            The symbolic name of the bundle.
     *
     * @return <code>true</code>, when the bundle is started, else <code>false</code>.
     */
    public boolean isBundleStarted(String symbolicName) {
        return startedBundles.contains(symbolicName);
    }

    private Properties loadFrameworkProperties() {

        try {
            Properties frameworkProperties = PropertiesUtils.load(CORE_PROPERTIES_LOCATION);
            if (frameworkProperties == null) {
                throw new BeanCreationException("Core osgi framework properties not found: "
                        + CORE_PROPERTIES_LOCATION);
            }
            // load additional properties
            for (String location : frameworkPropertiesLocations) {
                Properties addionalProperties = PropertiesUtils.load(locationToUrl(location));
                mergeProperties(frameworkProperties, addionalProperties);
            }

            return frameworkProperties;
        } catch (IOException e) {
            throw new BeanCreationException("Error reading osgi.properties.", e);
        }
    }

    private URL locationToUrl(String location) throws MalformedURLException {
        // there is no classpath: protocol handler per default (some spring component provides it
        // but this component might not exist, e.g. in tests scenario)
        if (location.startsWith("classpath:")) {
            location = location.substring(10);
            if (!location.startsWith("/")) {
                location = "/" + location;
            }
            return this.getClass().getResource(location);
        } else {
            return new URL(location);
        }
    }

    private void mergeProperties(Properties frameworkProperties, Properties addionalProperties) {
        for (Object key : addionalProperties.keySet()) {
            String propertyKey = key.toString();
            if (FelixConstants.FRAMEWORK_SYSTEMPACKAGES_EXTRA.equals(propertyKey)) {
                String existingPackages = frameworkProperties.getProperty(propertyKey);
                if (existingPackages == null) {
                    frameworkProperties.setProperty(propertyKey,
                            addionalProperties.getProperty(propertyKey));
                } else {
                    frameworkProperties.setProperty(propertyKey, existingPackages + ","
                            + addionalProperties.getProperty(propertyKey));
                }
            } else {
                frameworkProperties.setProperty(propertyKey,
                        addionalProperties.getProperty(propertyKey));
            }
        }

    }

    private void prepareFramework() {
        fileInstall = new FileInstall();
        propertiesDictionary = new Hashtable<String, String>();
        StartupProperties startupProperties = CommunoteRuntime.getInstance()
                .getConfigurationManager().getStartupProperties();
        propertiesDictionary.put(DirectoryWatcher.DIR, startupProperties.getPluginDir()
                .getAbsolutePath());
        propertiesDictionary.put(DirectoryWatcher.NO_INITIAL_DELAY, Boolean.TRUE.toString());
        propertiesDictionary.put(DirectoryWatcher.START_NEW_BUNDLES, Boolean.TRUE.toString());
        propertiesDictionary.put(DirectoryWatcher.LOG_LEVEL, Integer.toString(4));
        Properties frameworkProperties = loadFrameworkProperties();
        List<BundleActivator> activatorList = new ArrayList<BundleActivator>();
        activatorList.add(fileInstall);
        frameworkProperties.put(FelixConstants.SYSTEMBUNDLE_ACTIVATORS_PROP, activatorList);
        // TODO better add a setSystemBundlesLocation method
        String pathToWebInf = CommunoteRuntime.getInstance().getApplicationInformation()
                .getApplicationRealPath()
                + "WEB-INF" + File.separator;
        initSystemBundles(frameworkProperties, pathToWebInf + "plugins");
        frameworkProperties.put(Constants.FRAMEWORK_STORAGE, OSGiHelper.getBundleBasePath()
                .getAbsolutePath() + File.separator + "bundle-cache");
        try {
            framework = new Felix(frameworkProperties);
            framework.init();
            AutoProcessor.process(frameworkProperties, framework.getBundleContext());
            framework.getBundleContext().addBundleListener(this);
        } catch (BundleException e) {
            throw new BeanCreationException(
                    "Starting OSGi framework failed because of a BundleException.", e);
        }
        LOG.info("OSGi Framework initialized.");
    }

    /**
     * Starts the framework. There is usually no reason to call this method manually because it will
     * be invoked automatically by the {@link #initializationComplete()} callback.
     *
     * @throws BundleException
     *             Exception.
     */
    public synchronized void start() throws BundleException {
        if (this.framework == null) {
            this.prepareFramework();
        }
        for (BundleListener listener : listeners) {
            framework.getBundleContext().addBundleListener(listener);
        }
        framework.start();
        fileInstall.updated("initial", propertiesDictionary);
    }

    /**
     * Stops the Framework
     */
    public synchronized void stop() {
        if (this.framework == null) {
            return;
        }
        try {
            fileInstall.stop(framework.getBundleContext());
        } catch (Exception e) {
            LOG.error(e.getMessage());
            LOG.debug(e.getMessage(), e);
        }
        try {
            for (BundleListener listener : listeners) {
                framework.getBundleContext().removeBundleListener(listener);
            }
            framework.stop();
            FrameworkEvent frameworkEvent = null;
            do {
                frameworkEvent = framework.waitForStop(1000);
            } while (frameworkEvent.getType() != FrameworkEvent.STOPPED
                    && frameworkEvent.getType() != FrameworkEvent.ERROR);
            LOG.info("The OSGi-Framework has stopped with status: " + frameworkEvent.getType());
        } catch (BundleException | InterruptedException e) {
            LOG.error(e.getMessage(), e);
        }
    }
}
