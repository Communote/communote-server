package com.communote.server.api.core.event;

/**
 * An event that can be distributed to other Communote nodes when running a clustered Communote
 * setup.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public interface DistributableEvent extends Event {
    /**
     * Whether the event was created on this Communote instance node if running a clustered
     * Communote setup.
     * 
     * @return true if the event was created on this instance, false if it was received from another
     *         instance because of event distribution
     */
    boolean getCreatedLocally();

    /**
     * Whether the event needs to be distributed to other Communote nodes if running a clustered
     * Communote setup.
     * 
     * @return true if the event has to be distributed
     */
    boolean isDistribute();

    /**
     * Defines whether the event was created on this Communote instance node if running a clustered
     * Communote setup.
     * 
     * @param createdLocally
     *            true if the event was created on this instance, false if it was received from
     *            another instance because of event distribution
     */
    void setCreatedLocally(boolean createdLocally);

    /**
     * Defines whether the event needs to be distributed to other Communote nodes if running a
     * clustered Communote setup.
     * 
     * @param distribute
     *            true if the event has to be distributed
     */
    void setDistribute(boolean distribute);
}
