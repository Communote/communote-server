package com.communote.server.core.security.event;

import com.communote.server.api.core.event.Event;

/**
 * Event that is fired after a user was successfully authenticated.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class UserAuthenticatedEvent implements Event {

    private static final long serialVersionUID = 1L;
    private final Long userId;

    /**
     * Create a new event
     * 
     * @param userId
     *            ID of the authenticated user
     */
    public UserAuthenticatedEvent(Long userId) {
        this.userId = userId;
    }

    /**
     * @return ID of the authenticated user
     */
    public Long getUserId() {
        return userId;
    }

}
