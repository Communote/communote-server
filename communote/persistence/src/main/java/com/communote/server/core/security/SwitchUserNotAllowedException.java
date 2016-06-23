package com.communote.server.core.security;

import com.communote.server.api.core.security.AuthorizationException;

/**
 * Exception indicating that the current use had no right to change to another user
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class SwitchUserNotAllowedException extends AuthorizationException {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private final Long orginalUserId;

    private final Long targetUserId;

    /**
     * Constructs a new instance of SwitchUserNotAllowedException
     *
     * @param orginalUserId
     *            id of the user trying to change the context
     * @param targetUserId
     *            if of the user to switch to
     *
     *
     * @param message
     *            the message of this exception
     * @param e
     */
    public SwitchUserNotAllowedException(Long orginalUserId, Long targetUserId, String message) {
        super(message);
        this.orginalUserId = orginalUserId;
        this.targetUserId = targetUserId;
    }

    /**
     * Constructs a new instance of SwitchUserNotAllowedException
     *
     * @param orginalUserId
     *            id of the user trying to change the context
     * @param targetUserId
     *            if of the user to switch to
     * @param message
     *            the message
     * @param throwable
     *            the cause of the exception
     */
    public SwitchUserNotAllowedException(Long orginalUserId, Long targetUserId, String message,
            Throwable throwable) {
        super(message, throwable);
        this.orginalUserId = orginalUserId;
        this.targetUserId = targetUserId;
    }

    /**
     *
     * @return id of the user who tried to switch, can be null
     */
    public Long getOrginalUserId() {
        return orginalUserId;
    }

    /**
     *
     * @return id of the user the orginal user wanted to switch to, can be null
     */
    public Long getTargetUserId() {
        return targetUserId;
    }
}
