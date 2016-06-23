package com.communote.server.core.service;

/**
 * A component which provides some service functionality. A service can be enabled, disabled,
 * started and stopped.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public interface CommunoteService {

    /**
     * Returns the name of the service that is used to address it in the
     * {@link CommunoteServiceManager}.
     *
     * @return the name of the service
     */
    String getName();

    /**
     * @return <code>True</code>, when the service is enabled, else false.
     */
    boolean isEnabled();

    /**
     * Returns true if the service is currently running.
     *
     * @return true if the service is running, false otherwise
     */
    boolean isRunning();

    /**
     * Non-blocking function that starts the service. It is not checked whether the service is
     * enabled. The caller should do this.
     *
     * @param triggeredLocally
     *            true if the service was triggered on this Communote instance, false if it was
     *            triggered by an event from another Communote instance when running a clustered
     *            setup
     */
    void start(boolean triggeredLocally);

    /**
     * Stops the service and returns after the service was stopped. If the service is not running
     * nothing must happen.
     */
    void stop();

    /**
     * Returns whether the service can be restarted at runtime. False should be returned if a
     * restart would lead to loss of data.
     *
     * @return true if the service can be restarted when it is already running.
     */
    boolean supportsRestart();
}
