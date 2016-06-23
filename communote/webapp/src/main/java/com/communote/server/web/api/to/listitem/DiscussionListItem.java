package com.communote.server.web.api.to.listitem;

/**
 * List item to be attached to a note to provide the details about the discussion the note belongs
 * to.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class DiscussionListItem implements java.io.Serializable {
    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = -3312620699759750887L;

    private Long discussionId;

    private Long[] parentDiscussionIds;

    private int discussionDepth;

    /**
     * Creates a new instance
     */
    public DiscussionListItem() {
        this.discussionId = null;
        this.parentDiscussionIds = null;
        this.discussionDepth = 0;
    }

    /**
     * Copies all properties from the argument value object into this value object.
     * 
     * @param otherBean
     *            the object to copy from
     */
    public void copy(DiscussionListItem otherBean) {
        if (otherBean != null) {
            this.setDiscussionId(otherBean.getDiscussionId());
            this.setParentDiscussionIds(otherBean.getParentDiscussionIds());
            this.setDiscussionDepth(otherBean.getDiscussionDepth());
        }
    }

    /**
     * @return the depth of the associated note within the discussion
     */
    // TODO guess this isn't used, was set by a comparator which wasn't invoked by API controllers
    public int getDiscussionDepth() {
        return this.discussionDepth;
    }

    /**
     * @return the ID of the discussion
     */
    public Long getDiscussionId() {
        return this.discussionId;
    }

    /**
     * @return the IDs of the parent notes sorted by the root note first
     */
    // TODO guess this isn't used, was set by a comparator which wasn't invoked by API controllers
    // (and sorted incorrectly)
    public Long[] getParentDiscussionIds() {
        return this.parentDiscussionIds;
    }

    /**
     * @param discussionDepth
     *            the depth of the associated note within the discussion
     */
    public void setDiscussionDepth(int discussionDepth) {
        this.discussionDepth = discussionDepth;
    }

    /**
     * @param discussionId
     *            the ID of the discussion
     */
    public void setDiscussionId(Long discussionId) {
        this.discussionId = discussionId;
    }

    /**
     * @param parentDiscussionIds
     *            the IDs of the parent notes
     */
    public void setParentDiscussionIds(Long[] parentDiscussionIds) {
        this.parentDiscussionIds = parentDiscussionIds;
    }

}