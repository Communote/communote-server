package com.communote.server.core.security;

import org.springframework.security.authentication.AccountStatusException;

/**
 * Exception to be thrown if a user was authenticated successfully but has not yet accepted the
 * Terms Of Use. This will occur if the Terms Of Use need to be accepted and it is the first login
 * of the user or the administrator wants the users to accept them again after changing them.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class TermsOfUseNotAcceptedException extends AccountStatusException {

    private static final long serialVersionUID = 1L;

    private final Long userId;

    /**
     * Constructs a new instance of TermsOfUseNotAcceptedException
     * 
     * @param message
     *            Message of details for this Exception
     * @param userId
     *            the ID of the user who has not accepted the terms
     */
    public TermsOfUseNotAcceptedException(String message, Long userId) {
        super(message);
        this.userId = userId;
    }

    /**
     * The ID of the user who has not accepted the terms.
     * 
     * @return the ID
     */
    public long getUserId() {
        return this.userId;
    }
}
