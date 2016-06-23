package com.communote.server.core.user.events;

import com.communote.server.api.core.event.Event;
import com.communote.server.model.user.UserStatus;

/**
 * Event that is fired when the status of a user has changed.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class UserStatusChangedEvent implements Event {

    /**
     * default serial version UID
     */
    private static final long serialVersionUID = 1L;
    private final Long userId;
    private final UserStatus oldStatus;
    private final UserStatus newStatus;

    /**
     * Create a new event describing the status change.
     *
     * @param userId
     *            The ID of the user whose status changed
     * @param oldStatus
     *            the previous status
     * @param newStatus
     *            the new status
     */
    public UserStatusChangedEvent(Long userId, UserStatus oldStatus,
            UserStatus newStatus) {
        this.userId = userId;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
    }

    /**
     * @return the new status of the user
     */
    public UserStatus getNewStatus() {
        return newStatus;
    }

    /**
     * @return the previous status of the user, null if it is a new user
     */
    public UserStatus getOldStatus() {
        return oldStatus;
    }

    /**
     * @return the ID of the user whose status changed
     */
    public Long getUserId() {
        return userId;
    }

}
