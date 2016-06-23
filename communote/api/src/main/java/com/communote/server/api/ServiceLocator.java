package com.communote.server.api;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import com.communote.common.virusscan.VirusScanner;
import com.communote.common.virusscan.VirusScannerFactory;
import com.communote.common.virusscan.exception.InitializeException;
import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.config.type.ApplicationPropertyVirusScanning;

/**
 * Locator for Spring services of the core ApplicationContext. This is especially useful for
 * situations where autowiring is not possible.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ServiceLocator {
    private static ServiceLocator INSTANCE;

    /**
     * Get the service with the given type. Using this method is equivalent to {@link #instance()}.
     * {@link #getService(Class)}. If the service does not exist or the application context was not
     * initialized a runtime exception will be thrown.
     *
     * @param serviceClass
     *            Class of the service
     * @param <T>
     *            Type of the service
     * @return The service.
     */
    public static final <T> T findService(Class<T> serviceClass) {
        return INSTANCE.getService(serviceClass);
    }

    /**
     * Get the singleton instance.
     *
     * @return the service locator or null if the application context was not yet initialized
     */
    public static final ServiceLocator instance() {
        return INSTANCE;
    }

    /**
     * @return whether the application context is available and {@link #instance()} will return not
     *         null
     */
    public static boolean isApplicationContextAvailable() {
        return INSTANCE != null;
    }

    @Autowired
    private ApplicationContext appContext;

    private ServiceLocator() {
    }

    /**
     * Get the service of the given type. The simple name of the class, with the first letter
     * converted to lower case, will be used as name of the service. If there is no matching service
     * a runtime exception will be thrown.
     *
     * @param serviceClass
     *            Class of the service
     * @param <T>
     *            Type of the service
     * @return the service
     */
    public <T> T getService(Class<T> serviceClass) {
        return appContext.getBean(StringUtils.uncapitalize(serviceClass.getSimpleName()),
                serviceClass);
    }

    /**
     * Get a service by name. If there is no matching service a runtime exception will be thrown.
     *
     * @param serviceName
     *            The name of the service
     * @return the service
     * @deprecated Use {@link #getService(Class)} or {@link #getService(String, Class)} instead.
     */
    @Deprecated
    public Object getService(String serviceName) {
        return appContext.getBean(serviceName);
    }

    /**
     * Get a service by name that has the given type. If there is no such service a runtime
     * exception will be thrown.
     *
     * @param serviceName
     *            The name of the service
     * @param serviceClass
     *            Class of the service
     * @param <T>
     *            Type of the service
     * @return the service
     */
    public <T> T getService(String serviceName, Class<T> serviceClass) {
        return appContext.getBean(serviceName, serviceClass);
    }

    /**
     * @return the virus scanner or null if virus scanning is disabled
     * @throws InitializeException
     *             if virus scanning is enabled but the selected scanner was not correctly
     *             initialized
     */
    // TODO better use a FactoryBean
    public VirusScanner getVirusScanner() throws InitializeException {
        if (!CommunoteRuntime.getInstance().getConfigurationManager()
                .getApplicationConfigurationProperties()
                .getProperty(ApplicationPropertyVirusScanning.ENABLED, true)) {
            return null;
        }
        return VirusScannerFactory.instance().getScanner();
    }

    @PostConstruct
    private void init() {
        INSTANCE = this;
    }

}