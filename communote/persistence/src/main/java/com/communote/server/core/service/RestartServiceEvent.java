package com.communote.server.core.service;

import com.communote.server.api.core.event.DistributableEvent;

/**
 * The RestartServiceEvent represents an event that is triggered to restart a service.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class RestartServiceEvent implements DistributableEvent {

    /**
     * default serial version UID
     */
    private static final long serialVersionUID = 1L;
    private final String serviceName;
    private boolean createdLocally;
    private boolean distribute;

    /**
     * A locally created event to restart the named service. The method {@link #isDistribute()} will
     * return true.
     * 
     * @param serviceName
     *            the name of the service that should be restarted
     */
    public RestartServiceEvent(String serviceName) {
        this.distribute = true;
        this.createdLocally = true;
        this.serviceName = serviceName;
    }

    /**
     * {@inheritDoc}
     */
    public boolean getCreatedLocally() {
        return createdLocally;
    }

    /**
     * Returns the name of the service that should be restarted.
     * 
     * @return the name of the service to restart
     */
    public String getServiceName() {
        return this.serviceName;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isDistribute() {
        return distribute;
    }

    /**
     * {@inheritDoc}
     */
    public void setCreatedLocally(boolean createdLocally) {
        this.createdLocally = createdLocally;
    }

    /**
     * {@inheritDoc}
     */
    public void setDistribute(boolean distribute) {
        this.distribute = distribute;
    }
}
