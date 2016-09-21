package com.communote.server.core.bootstrap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.quartz.InterruptableJob;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.UnableToInterruptJobException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.scheduling.quartz.JobDetailAwareTrigger;

import com.communote.common.i18n.LocalizedMessage;
import com.communote.common.util.VersionComparator;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.bootstrap.ApplicationInitializationException;
import com.communote.server.api.core.bootstrap.CustomInitializer;
import com.communote.server.api.core.bootstrap.InitializationCompleteListener;
import com.communote.server.api.core.bootstrap.InitializationStatus;
import com.communote.server.api.core.config.ConfigurationUpdateException;
import com.communote.server.api.core.config.CoreConfigurationPropertyConstant;
import com.communote.server.api.core.config.StartupProperties;
import com.communote.server.api.core.config.database.DatabaseConnectionException;
import com.communote.server.api.core.config.type.CoreProperty;
import com.communote.server.api.core.event.EventDispatcher;
import com.communote.server.core.client.ClientInitializationException;
import com.communote.server.core.common.caching.CacheManager;
import com.communote.server.core.common.database.DatabaseUpdateException;
import com.communote.server.core.common.database.DatabaseUpdateType;
import com.communote.server.core.common.database.DatabaseUpdater;
import com.communote.server.core.common.util.DatabaseHelper;
import com.communote.server.core.messaging.NotificationManagement;
import com.communote.server.core.osgi.OSGiManagement;
import com.communote.server.core.service.CommunoteService;
import com.communote.server.core.service.CommunoteServiceManager;
import com.communote.server.core.user.listeners.CreateBuiltInNavigationItemsOnUserActivation;
import com.communote.server.persistence.common.messages.MessageKeyLocalizedMessage;
import com.communote.server.service.ClientCreationService;

/**
 * Initializes the application which includes creating the Spring ApplicationContext, updating the
 * database and starting Communote's core components.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ApplicationInitializer {

    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationInitializer.class);

    private static final String DEFAULT_CONTEXT_CONFIG_LOCATION = "classpath:com/communote/server/spring/applicationContext.xml";

    private boolean initialized;
    private boolean coreInitialized;

    private InitializationStatus status = new InitializationStatus(
            InitializationStatus.Type.IN_PROGRESS);

    private final List<String> contextConfigLocations;
    private final List<CustomInitializer> customInitializers;

    private ConfigurableApplicationContext applicationContext;

    private List<InitializationCompleteListener> completeListeners;
    private Set<String> initConditions;

    private ApplicationPreparedCallback applicationPreparedCallback;

    /**
     * Create the application initializer
     */
    public ApplicationInitializer() {
        contextConfigLocations = new ArrayList<>();
        contextConfigLocations.add(DEFAULT_CONTEXT_CONFIG_LOCATION);
        customInitializers = new ArrayList<>();
    }

    /**
     * Add the location of an XML file which contains Spring bean definitions that should be
     * included when creating the ApplicationContext. Adding locations after the application context
     * was created has no effect.
     *
     * @param configLocation
     *            an absolute location of a configuration resource with bean definitions to load.
     *            The string should start with classpath: or file: for loading classpath or file
     *            resources.
     */
    public synchronized void addApplicationContextConfigLocation(String configLocation) {
        contextConfigLocations.add(configLocation);
    }

    public synchronized void addInitializationCompleteListener(
            InitializationCompleteListener listener) {
        if (!initialized) {
            if (completeListeners == null) {
                completeListeners = new ArrayList<>();
            }
            completeListeners.add(listener);
        }
    }

    public synchronized void addInitializationCondition(String conditionId) {
        if (!initialized) {
            if (initConditions == null) {
                initConditions = new HashSet<>();
            }
            initConditions.add(conditionId);
        }
    }

    /**
     * Add an initializer that will be called during {@link #initializeApplication()} after the core
     * components were initialized.
     *
     * @param initializer
     *            the additional initializer
     */
    public synchronized void addInitializer(CustomInitializer initializer) {
        if (!coreInitialized) {
            customInitializers.add(initializer);
        } else {
            LOGGER.warn("Ignoring initializer {} because application is already initialized",
                    initializer.getClass().getName());
        }
    }

    /**
     * Adds the job triggers to the scheduler.
     *
     * @param scheduler
     *            the scheduler
     * @param triggers
     *            the quartz job triggers to add
     * @throws SchedulerException
     *             if adding a trigger failed
     */
    private void addTriggers(Scheduler scheduler, List<Trigger> triggers) throws SchedulerException {
        if (triggers != null) {
            for (Trigger trigger : triggers) {
                if (trigger instanceof JobDetailAwareTrigger) {
                    // register job detail too
                    JobDetail jobDetail = ((JobDetailAwareTrigger) trigger).getJobDetail();
                    scheduler.addJob(jobDetail, true);
                }
                scheduler.scheduleJob(trigger);
            }
        }
    }

    /**
     * This method checks, if the current installed version (from the war file) is applicable with
     * the metadata from the (maybe) previous version.
     *
     * @throws ApplicationInitializationException
     *             in case the version to install is older than the installed one
     *
     */
    private void assertVersionNumbers() {
        String previousApplicationVersion = CoreProperty.APPLICATION_VERSION.getValue();
        if (previousApplicationVersion == null) {
            previousApplicationVersion = "0";
        }
        String currentApplicationVersion = CommunoteRuntime.getInstance()
                .getApplicationInformation().getBuildNumber();
        VersionComparator versionComparator = new VersionComparator(true);
        int versionComparison = versionComparator.compare(previousApplicationVersion,
                currentApplicationVersion);
        if (versionComparison > 0) {
            throw new ApplicationInitializationException("You can't install an older version ("
                    + currentApplicationVersion + ") over an already installed newer version ("
                    + previousApplicationVersion + ").", new MessageKeyLocalizedMessage(
                            "initialization.error.install.old.over.new.version"));
        }
    }

    private synchronized void completeInitialization() {
        if (!initialized && coreInitialized) {
            if (initConditions == null || initConditions.isEmpty()) {
                initialized = true;
                this.status = new InitializationStatus(InitializationStatus.Type.SUCCESS);
                if (completeListeners != null) {
                    for (InitializationCompleteListener listener : completeListeners) {
                        listener.initializationComplete();
                    }
                    completeListeners = null;
                }
                LOGGER.info("\n\n\n Application SUCCESSFULLY initialized. \n\n\n");
            } else if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Core initialized, but waiting for conditions to be fulfilled: {}",
                        initConditions);
            }
        }
    }

    /**
     * Create the Spring ApplicationContext and inform the ApplicationPreparedCallback. If the
     * context was already created the call is ignored.
     *
     * @throws ApplicationInitializationException
     *             in case the ApplicationContext cannot be created
     */
    public synchronized void createApplicationContext() {
        if (applicationContext == null) {
            try {
                // ensure the Database can be accessed
                DatabaseHelper.testDatabaseConnection();
                String[] configLocations = contextConfigLocations
                        .toArray(new String[contextConfigLocations.size()]);
                applicationContext = new GenericXmlApplicationContext(configLocations);
            } catch (DatabaseConnectionException e) {
                throw new ApplicationInitializationException("Database is not accessible", e);
            } catch (Exception e) {
                throw new ApplicationInitializationException(
                        "Initializing the root ApplicationContext failed", e);
            }
            // notify callback
            if (applicationPreparedCallback != null) {
                applicationPreparedCallback.applicationPrepared(applicationContext);
            }
        }
    }

    public void fulfillInitializationCondition(String conditionId) {
        if (initConditions != null && initConditions.remove(conditionId)) {
            completeInitialization();
        }
    }

    public InitializationStatus getInitializationStatus() {
        return status;
    }

    /**
     * Initializes the application.
     *
     * @throws ApplicationInitializationException
     *             in case something goes wrong
     */
    public synchronized void initializeApplication() throws ApplicationInitializationException {
        // pseudo singleton check to avoid that initialization with another instance of this class
        // is triggered
        if (CommunoteRuntime.getInstance().isInitialized()) {
            return;
        }
        if (!CommunoteRuntime.getInstance().isCoreInitialized()) {
            try {
                // first check whether the version is valid before doing anything else to avoid
                // strange effects when starting the services (hibernate, ....)
                assertVersionNumbers();
                createApplicationContext();
                initializeCore();

            } catch (ApplicationInitializationException e) {
                LocalizedMessage message;
                if (e.getLocalizedDetails() == null) {
                    message = new MessageKeyLocalizedMessage("initialization.error.unknown");
                } else {
                    message = e.getLocalizedDetails();
                }
                status = new InitializationStatus(InitializationStatus.Type.FAILURE, message);
                throw e;
            } catch (Exception e) {
                status = new InitializationStatus(InitializationStatus.Type.FAILURE,
                        new MessageKeyLocalizedMessage("initialization.error.unknown"));
                throw new ApplicationInitializationException(e.getMessage(), e);
            }
        }
        completeInitialization();
    }

    /**
     * Initialize caching
     */
    private void initializeCaching() {
        try {
            System.setProperty("net.sf.ehcache.skipUpdateCheck", Boolean.TRUE.toString());
            ServiceLocator.findService(CacheManager.class).init(null);
        } catch (Exception e) {
            throw new ApplicationInitializationException(
                    "Failure in initializing the cache manager!", e);
        }
    }

    private void initializeComponents() throws ApplicationInitializationException {
        // setup trust stores first because other components may need the keys of the trust or key
        // store configured
        initKeyTrustStore();
        initializeCaching();
        try {
            ServiceLocator.findService(ClientCreationService.class).initGlobalClient();
        } catch (ClientInitializationException e) {
            throw new ApplicationInitializationException("Initializing the global client failed: "
                    + e.getMessage(), e);
        }
        initializeJobs();
        initializeMessaging();
        initializeDefaultServices();
        initializeDefaultEventListener();
    }

    /**
     * Does the actual initialization of the core if not already initialized.
     *
     * @throws ApplicationInitializationException
     *             in case initializing one of the components failed
     * @throws DatabaseUpdateException
     *             in case updating the database failed
     */
    private void initializeCore() throws ApplicationInitializationException,
    DatabaseUpdateException {
        if (coreInitialized) {
            return;
        }

        initializeSystemParameters();
        updateDatabase();

        initializeComponents();
        try {
            HashMap<CoreConfigurationPropertyConstant, String> settings = new HashMap<CoreConfigurationPropertyConstant, String>();
            settings.put(CoreProperty.APPLICATION_VERSION, CommunoteRuntime.getInstance()
                    .getApplicationInformation().getBuildNumber());
            CommunoteRuntime.getInstance().getConfigurationManager()
                    .updateStartupProperties(settings);
        } catch (ConfigurationUpdateException e) {
            throw new ApplicationInitializationException(
                    "Error storing the current application version.", e);
        }
        runCustomInitializers();
        coreInitialized = true;
        LOGGER.info("Core application initialized");
    }

    /**
     * This method initializes the default {@link com.communote.server.api.core.event.EventListener}
     * .
     */
    private void initializeDefaultEventListener() {
        EventDispatcher eventDispatcher = ServiceLocator.findService(EventDispatcher.class);
        eventDispatcher.register(new CreateBuiltInNavigationItemsOnUserActivation());
    }

    /**
     * initializes the default services
     */
    private void initializeDefaultServices() {
        CommunoteServiceManager manager = ServiceLocator.findService(CommunoteServiceManager.class);
        List<CommunoteService> services = (List<CommunoteService>) ServiceLocator.instance().getService(
                "builtInKenmeiServices");
        for (CommunoteService service : services) {
            manager.registerService(service, true);
        }
    }

    /**
     * Initialize the timers and jobs
     */
    private void initializeJobs() {
        StartupProperties startupProperties = CommunoteRuntime.getInstance()
                .getConfigurationManager().getStartupProperties();
        String instanceName = startupProperties.getInstanceName();
        LOGGER.info("Name of instance is: " + instanceName);
        try {
            // Skip Update Check for Quartz
            System.setProperty("org.terracotta.quartz.skipUpdateCheck", Boolean.TRUE.toString());
            Scheduler scheduler = ServiceLocator.findService(Scheduler.class);
            List<Trigger> triggers = ServiceLocator.instance()
                    .getService("jobTriggers", List.class);
            addTriggers(scheduler, triggers);
            scheduler.start();
        } catch (Exception e) {
            throw new ApplicationInitializationException("Failure in starting quartz scheduler!", e);
        }
    }

    /**
     * Initialize the messaging
     */
    private void initializeMessaging() {
        try {

            ServiceLocator.findService(NotificationManagement.class).start();
        } catch (Exception e) {
            throw new ApplicationInitializationException(
                    "Failure in initialising messaging management!", e);
        }
    }

    /**
     * Initializes standard values for unset system parameters.
     */
    private void initializeSystemParameters() {
        System.setProperty("file.encoding", "UTF-8");
        System.setProperty("mail.mime.decodeparameters", Boolean.TRUE.toString());
        System.setProperty("mail.mime.decodefilename", Boolean.TRUE.toString());
    }

    /**
     * Init the key and trust store
     */
    private void initKeyTrustStore() {
        CommunoteRuntime.getInstance().getConfigurationManager().getStartupProperties()
                .getTrustStore();
        CommunoteRuntime.getInstance().getConfigurationManager().getStartupProperties()
                .getKeyStore();
    }

    /**
     * @return true if the core is initialized
     */
    public boolean isCoreInitialized() {
        return coreInitialized;
    }

    /**
     * @return true if the core is initialized and all init conditions are fulfilled
     */
    public boolean isInitialized() {
        return initialized;
    }

    private void runCustomInitializers() {
        try {
            for (CustomInitializer initializer : customInitializers) {
                initializer.initialize();
            }
        } catch (ApplicationInitializationException e) {
            LOGGER.error("Running custom initializers failed", e);
            throw e;
        } catch (Exception e) {
            LOGGER.error("Running custom initializers failed", e);
            throw new ApplicationInitializationException("Running custom initializers failed", e);
        }
    }

    /**
     * Set the callback to invoke after building the Spring ApplicationContext is completed. If
     * Communote is already installed this callback is invoked within the startup process. If
     * Communote is not yet installed this callback is called as soon as the installation is done.
     * The callback is only invoked once. Setting the callback after startup has no effect.
     *
     * @param applicationPreparedCallback
     *            the callback to invoke
     */
    public void setApplicationPreparedCallback(
            ApplicationPreparedCallback applicationPreparedCallback) {
        this.applicationPreparedCallback = applicationPreparedCallback;
    }

    /**
     * Shutdown the core components and reset the application context. Additional application config
     * locations, custom initializers and complete listeners are not removed.
     */
    public synchronized void shutdown() {
        if (applicationContext != null) {
            ServiceLocator locator = ServiceLocator.instance();
            try {
                locator.getService(OSGiManagement.class).stop();
            } catch (Exception e) {
                LOGGER.error("could not shutdown osgi management", e);
            }
            try {
                shutdownScheduler();
            } catch (SchedulerException e) {
                LOGGER.error("could not shutdown scheduler", e);
            }
            try {
                locator.getService(CommunoteServiceManager.class).shutdown();
            } catch (Exception e) {
                LOGGER.error("could not shutdown service manager", e);
            }
            try {
                locator.getService(NotificationManagement.class).stop();
            } catch (Exception e) {
                LOGGER.error("could not shutdown messaging management", e);
            }
            try {
                locator.getService(CacheManager.class).shutdown();
            } catch (Exception e) {
                LOGGER.error("could not shutdown cache manger", e);
            }
            applicationContext.close();
            applicationContext = null;
        }
        coreInitialized = false;
        initialized = false;
        status = new InitializationStatus(InitializationStatus.Type.IN_PROGRESS);
    }

    /**
     * shutdown the scheduler<br>
     *
     *
     * @throws SchedulerException
     *             if stopping the jobs or the scheduler fails
     */
    private void shutdownScheduler() throws SchedulerException {
        Scheduler scheduler = ServiceLocator.findService(Scheduler.class);
        LOGGER.info("Stopping running jobs.");
        scheduler.shutdown();
        List<?> jobList = scheduler.getCurrentlyExecutingJobs();
        if (jobList != null) {
            for (Object job : jobList) {
                JobExecutionContext jobContext = (JobExecutionContext) job;
                if (jobContext.getJobInstance() instanceof InterruptableJob) {
                    try {
                        scheduler.interrupt(jobContext.getJobDetail().getName(), jobContext
                                .getJobDetail().getGroup());
                    } catch (UnableToInterruptJobException e) {
                        LOGGER.error("Unable to interrupt job "
                                + jobContext.getJobDetail().getFullName());
                    }
                }
            }
        }
        long now = System.currentTimeMillis();
        long waitedSeconds = 0;
        while ((jobList = scheduler.getCurrentlyExecutingJobs()) != null && !jobList.isEmpty()) {
            int runningJobCount = jobList.size();
            long secondsPassed = (System.currentTimeMillis() - now) / 1000L;
            if (secondsPassed > waitedSeconds) {
                waitedSeconds = secondsPassed;
                LOGGER.info("Waiting for {} jobs to stop", runningJobCount);
            }
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                LOGGER.error(e.getMessage());
            }
        }
        LOGGER.info("Stopped running jobs.");
    }

    /**
     * Update the database
     *
     * @throws DatabaseUpdateException
     *             in case the database update failed
     */
    private void updateDatabase() throws DatabaseUpdateException {
        DatabaseUpdater updater = ServiceLocator.findService(DatabaseUpdater.class);
        // little performance optimization: skip if the update has already been run. This will be
        // the case if the installer created and updated the database in this session.
        if (!updater.updateRunInSession(DatabaseUpdateType.SECOND_PASS_UPDATE)) {
            updater.updateDatabase();
        }
    }
}
