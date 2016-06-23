package com.communote.server.core.blog;

import com.communote.server.api.core.event.Event;

/**
 * Event, which is fired, when a discussion as changed.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class DiscussionChangedEvent implements Event {
    /**
     * default serial version UID
     */
    private static final long serialVersionUID = 1L;
    private final Long discussionId;

    /**
     * Constructor.
     * 
     * @param discussionId
     *            Id of the discussion.
     */
    public DiscussionChangedEvent(Long discussionId) {
        this.discussionId = discussionId;
    }

    /**
     * @return Id of the discussion.
     */
    public Long getDiscussionId() {
        return this.discussionId;
    }

}
