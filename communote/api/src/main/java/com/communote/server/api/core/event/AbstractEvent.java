package com.communote.server.api.core.event;

/**
 * Convenient abstract implementation of the event interface.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class AbstractEvent implements Event {

    /**
     * default serial version UID
     */
    private static final long serialVersionUID = 1L;
    private boolean distribute;

    /**
     * Convenient constructor for the event class.
     * 
     * @param distribute
     *            whether the event is to be distributed
     */
    public AbstractEvent(boolean distribute) {
        this.distribute = distribute;
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
    public void setDistribute(boolean distribute) {
        this.distribute = distribute;
    }
}
