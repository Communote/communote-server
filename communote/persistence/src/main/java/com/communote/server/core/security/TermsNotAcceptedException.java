package com.communote.server.core.security;

import com.communote.server.core.user.exception.GeneralUserManagementException;

/**
 * <p>
 * Thrown to indicate that the user has not yet accepted the terms of use or the privacy policy.
 * </p>
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class TermsNotAcceptedException extends GeneralUserManagementException {

    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = -1803756849294513206L;

    private Long userId;

    /**
     * Constructs a new instance of TermsNotAcceptedException
     *
     */
    public TermsNotAcceptedException(String message, Long userId) {
        super(message);
        this.userId = userId;
    }

    /**
     * <p>
     * The ID of the user who has not accepted the terms.
     * </p>
     */
    public Long getUserId() {
        return this.userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

}
