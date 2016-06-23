package com.communote.server.core.user;

import com.communote.server.core.user.exception.GeneralUserManagementException;

/**
 * <p>
 * Thrown to indicate that changing the status of a user to another status is not possible because
 * the transition is not supported.
 * </p>
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class InvalidUserStatusTransitionException extends GeneralUserManagementException {

    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = 6187763307315254930L;

    private com.communote.server.model.user.UserStatus currentStatus;

    private com.communote.server.model.user.UserStatus failedNewStatus;

    private com.communote.server.model.user.User user;

    /**
     * Constructs a new instance of InvalidUserStatusTransitionException
     *
     */
    public InvalidUserStatusTransitionException(
            com.communote.server.model.user.UserStatus currentStatus,
            com.communote.server.model.user.UserStatus failedNewStatus,
            com.communote.server.model.user.User user) {
        super("Invalid status change from " + currentStatus + " to " + failedNewStatus);
        this.currentStatus = currentStatus;
        this.failedNewStatus = failedNewStatus;
        this.user = user;
    }

    /**
     * <p>
     * the current status of the user
     * </p>
     */
    public com.communote.server.model.user.UserStatus getCurrentStatus() {
        return this.currentStatus;
    }

    /**
     * <p>
     * the new status that should have been set, but could not be set.
     * </p>
     */
    public com.communote.server.model.user.UserStatus getFailedNewStatus() {
        return this.failedNewStatus;
    }

    /**
     * <p>
     * the user whose status should have been changed
     * </p>
     */
    public com.communote.server.model.user.User getUser() {
        return this.user;
    }

    public void setCurrentStatus(com.communote.server.model.user.UserStatus currentStatus) {
        this.currentStatus = currentStatus;
    }

    public void setFailedNewStatus(com.communote.server.model.user.UserStatus failedNewStatus) {
        this.failedNewStatus = failedNewStatus;
    }

    public void setUser(com.communote.server.model.user.User user) {
        this.user = user;
    }

}
