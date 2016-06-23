package com.communote.server.core.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.event.EventDispatcher;
import com.communote.server.api.core.event.EventListener;
import com.communote.server.api.core.task.InvalidInstanceException;
import com.communote.server.api.core.task.TaskAlreadyExistsException;
import com.communote.server.api.core.task.TaskAlreadyRunningException;
import com.communote.server.api.core.task.TaskManagement;
import com.communote.server.api.core.task.TaskManagementException;
import com.communote.server.api.core.task.TaskNotActiveException;
import com.communote.server.core.tasks.ServiceTaskHandler;

/**
 * Manages all {@link CommunoteService} instances.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class CommunoteServiceManager implements EventListener<RestartServiceEvent> {

    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(CommunoteServiceManager.class);

    // about 10 concurrent threads should be enough
    private final Map<String, CommunoteService> registeredServices = new ConcurrentHashMap<String, CommunoteService>(
            16, 0.75f, 10);
    private final Map<Class<? extends CommunoteService>, List<String>> registeredServiceNames = new ConcurrentHashMap<Class<? extends CommunoteService>, List<String>>();

    private final AtomicBoolean shutdownCalled = new AtomicBoolean(false);

    /**
     * Returns the name of the registered service that has the provided class. In case there is no
     * registered service instance of that class null is returned. In case there is more than one
     * service instance of the provided class the name of the first matching is returned.
     *
     * @param serviceClass
     *            the class of the service
     * @return the name of the service or null
     */
    public String getNameOfService(Class<? extends CommunoteService> serviceClass) {
        String name = null;
        if (!shutdownCalled.get()) {
            List<String> names = registeredServiceNames.get(serviceClass);
            if (names != null && names.size() > 0) {
                name = names.get(0);
            }
        }
        return name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<RestartServiceEvent> getObservedEvent() {
        return RestartServiceEvent.class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handle(RestartServiceEvent event) {
        // get service by name and restart it if it exists
        restart(event.getServiceName(), event.getCreatedLocally());
    }

    /**
     * Handle the case of a TaskAlreadyRunningException while trying to start a singleton service.
     * The caller of this method must have the lock on the service instance!
     *
     * @param service
     *            the service that couldn't be started
     * @param taskManagement
     *            the TaskMangement for convenience
     */
    private void handleTaskRunningExceptionOnStart(CommunoteService service,
            TaskManagement taskManagement) {
        if (taskManagement.isTaskRunningOnCurrentInstance(service.getName())) {
            LOGGER.error("Service " + service.getName()
                    + " couldn't be started. Retry may succeed.");
            try {
                // not starting the service here because there might be strange effects when running
                // clustered
                taskManagement.stopTaskExecution(service.getName());
            } catch (InvalidInstanceException e) {
                LOGGER.error("Unexpected error stopping a task", e);
            }
        } else {
            LOGGER.debug(service.getName()
                    + " Tried to start service which is already running on another instance.");
        }
    }

    /**
     * Returns whether the named service is running. If there is no registered service that has the
     * provided name false is returned, otherwise the return value is the value of
     * {@link CommunoteService#isRunning()} of the named service.
     *
     * @param serviceName
     *            the name of the service whose status is to be returned
     * @return true if the service was started and not stopped in the meanwhile
     */
    public boolean isRunning(String serviceName) {
        CommunoteService service = registeredServices.get(serviceName);
        return service != null && service.isRunning();
    }

    /**
     * Registers the service if it is not yet registered. The service is identified by the name
     * returned by {@link CommunoteService#getName()}.
     *
     * @param service
     *            the service to register
     * @return true if the service was registered, false if it was already registered, cannot be
     *         registered on this instance or {@link #shutdown()} had been called
     */
    public boolean registerService(CommunoteService service) {
        boolean registered = false;
        if (!shutdownCalled.get()) {
            synchronized (this) {
                if (registeredServices.containsKey(service.getName())) {
                    return false;
                }
                registeredServices.put(service.getName(), service);
                List<String> names = registeredServiceNames.get(service.getClass());
                if (names != null) {
                    names.add(service.getName());
                } else {
                    names = new ArrayList<String>();
                    names.add(service.getName());
                    registeredServiceNames.put(service.getClass(), names);
                }
                if (service.supportsRestart()) {
                    ServiceLocator.findService(EventDispatcher.class).register(this);
                }
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Registered service with name " + service.getName());
                }
            }
            registered = true;
        }
        return registered;
    }

    /**
     * Registers the service if it is not yet registered. The service is identified by the name
     * returned by {@link CommunoteService#getName()}.
     *
     * @param service
     *            the service to register
     * @param start
     *            whether to directly start the service after registering
     * @return true if the service was registered, false if it was already registered, cannot be
     *         registered on this instance or {@link #shutdown()} had been called
     */
    public boolean registerService(CommunoteService service, boolean start) {
        if (registerService(service)) {
            start(service, true);
            return true;
        }
        return false;
    }

    /**
     * Stops a service by name and restarts it afterwards. If the service is not registered nothing
     * will happen.
     *
     * @param serviceName
     *            the name of the service to restart
     */
    public void restart(String serviceName) {
        restart(serviceName, true);
    }

    /**
     * Stops a service by name and restarts it afterwards. If the service is not registered nothing
     * will happen.
     *
     * @param serviceName
     *            the name of the service to restart
     * @param triggeredLocally
     *            true if the service was triggered on this Communote instance, false if it was
     *            triggered by an event from another Communote instance when running a clustered
     *            setup
     */
    private void restart(String serviceName, boolean triggeredLocally) {
        CommunoteService service = registeredServices.get(serviceName);
        if (service != null) {
            stop(service);
            start(service, triggeredLocally);
        }
    }

    /**
     * Stops and unregisters all running services.
     */
    public void shutdown() {
        if (shutdownCalled.compareAndSet(false, true)) {
            for (Map.Entry<String, CommunoteService> entry : registeredServices.entrySet()) {
                stop(entry.getValue());
            }
            registeredServices.clear();
        }
    }

    /**
     * Starts a service. If the service is not registered or is already running nothing will happen.
     *
     * @param service
     *            the service to start
     * @param triggeredLocally
     *            true if the service was triggered on this Communote instance, false if it was
     *            triggered by an event from another Communote instance when running a clustered
     *            setup
     */
    private void start(CommunoteService service, boolean triggeredLocally) {
        if (shutdownCalled.get()) {
            return;
        }
        synchronized (service) {
            if (!service.isEnabled() || service.isRunning()) {
                LOGGER.info("The service {} is not enabled -> Skip start.", service.getName());
                return;
            }
            if (service.isRunning()) {
                LOGGER.info("The service {} is already running -> Skip start.", service.getName());
                return;
            }
            if (service instanceof CommunoteSingletonService) {
                startKenmeiSingletionService((CommunoteSingletonService) service, triggeredLocally);
            } else {
                service.start(triggeredLocally);
            }
        }
    }

    /**
     * Starts a service by name. If the service is not registered or is already running nothing will
     * happen.
     *
     * @param serviceName
     *            the name of the service to start
     */
    public void start(String serviceName) {
        CommunoteService service = registeredServices.get(serviceName);
        if (service != null) {
            start(service, true);
        }
    }

    /**
     * Starts a singleton service
     *
     * Note: caller should have the lock on the service
     *
     * @param service
     *            A CommunoteSingletonService
     * @param triggeredLocally
     *            True, if this was triggered on this instance.
     */
    private void startKenmeiSingletionService(CommunoteSingletonService service,
            boolean triggeredLocally) {
        TaskManagement taskManagement = ServiceLocator.findService(TaskManagement.class);
        try {
            taskManagement.addTask(service.getName(), true, 0L, new Date(),
                    ServiceTaskHandler.class);
        } catch (TaskAlreadyExistsException e) {
            LOGGER.debug("Task for service already exists, not created again: {}",
                    service.getName());
        } catch (DataIntegrityViolationException e) {
            LOGGER.debug("Task for service already exists, not created again: {}",
                    service.getName());
        }
        try {
            LOGGER.debug("{} calling startTaskExecution", service.getName());
            Long taskExecutionId = taskManagement.startTaskExecution(service.getName());
            LOGGER.debug("{} taskExecutionId={}", service.getName(), taskExecutionId);
            if (taskExecutionId != null) {
                service.start(triggeredLocally);
            }
        } catch (TaskAlreadyRunningException e) {
            handleTaskRunningExceptionOnStart(service, taskManagement);
        } catch (TaskNotActiveException e) {
            LOGGER.error(service.getName() + " Task not active, but should be.", e);
        } catch (TaskManagementException e) {
            LOGGER.error(service.getName() + " Task couldn't be started.", e);
        }
    }

    /**
     * Stops a service.
     *
     * @param service
     *            the service to stop
     */
    private void stop(CommunoteService service) {
        synchronized (service) {
            if (service.isRunning()) {
                service.stop();
                if (service instanceof CommunoteSingletonService) {
                    TaskManagement taskManagement = ServiceLocator
                            .findService(TaskManagement.class);
                    try {
                        taskManagement.stopTaskExecution(service.getName());
                    } catch (InvalidInstanceException e) {
                        LOGGER.error("Tried to stop service that was not started by this instance",
                                e);
                    }
                }
            }
        }
    }

    /**
     * Stops a service by name. If the service is not registered nothing will happen.
     *
     * @param serviceName
     *            the name of the service to stop
     */
    public void stop(String serviceName) {
        CommunoteService service = registeredServices.get(serviceName);
        if (service != null) {
            stop(service);
        }
    }
}
