package com.communote.server.core.filter.listitems;

/**
 * <p>
 * Describes a range in time which is marked by two date objects representing the start and end
 * points.
 * </p>
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class TimeRangeListItem
        extends com.communote.server.api.core.common.IdentifiableEntityData
        implements java.io.Serializable {
    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = -4948346073549076697L;

    private java.util.Date startTime;

    private java.util.Date endTime;

    public TimeRangeListItem() {
        super();
        this.startTime = null;
        this.endTime = null;
    }

    public TimeRangeListItem(java.util.Date startTime, java.util.Date endTime) {
        super();
        this.startTime = startTime;
        this.endTime = endTime;
    }

    /**
     * Copies constructor from other TimeRangeListItem
     * 
     * @param otherBean
     *            , cannot be <code>null</code>
     * @throws NullPointerException
     *             if the argument is <code>null</code>
     */
    public TimeRangeListItem(TimeRangeListItem otherBean) {
        this(otherBean.getStartTime(), otherBean.getEndTime());
    }

    /**
     * Copies all properties from the argument value object into this value object.
     */
    public void copy(TimeRangeListItem otherBean) {
        if (otherBean != null) {
            this.setStartTime(otherBean.getStartTime());
            this.setEndTime(otherBean.getEndTime());
        }
    }

    /**
     * 
     */
    public java.util.Date getEndTime() {
        return this.endTime;
    }

    /**
     * 
     */
    public java.util.Date getStartTime() {
        return this.startTime;
    }

    public void setEndTime(java.util.Date endTime) {
        this.endTime = endTime;
    }

    public void setStartTime(java.util.Date startTime) {
        this.startTime = startTime;
    }

}