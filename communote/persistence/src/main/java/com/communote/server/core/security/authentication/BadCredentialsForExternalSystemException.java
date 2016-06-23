package com.communote.server.core.security.authentication;

import org.springframework.security.authentication.BadCredentialsException;

/**
 * Like the {@link BadCredentialsException} but should be thrown if the authentication for a user of
 * an external system failed.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class BadCredentialsForExternalSystemException extends BadCredentialsException {

    private final String externalSystemId;

    /**
     *
     * @param externalSystemId
     *            the ID of the external system for which the authentication failed
     * @param message
     *            a detailed message
     */
    public BadCredentialsForExternalSystemException(String externalSystemId, String message) {
        this(externalSystemId, message, null);
    }

    /**
     *
     * @param externalSystemId
     *            the ID of the external system for which the authentication failed
     * @param message
     *            a detailed message
     * @param cause
     *            the cause
     */
    public BadCredentialsForExternalSystemException(String externalSystemId, String message,
            Throwable cause) {
        super(message, cause);
        this.externalSystemId = externalSystemId;
    }

    /**
     *
     * @return the ID of the external system for which the authentication failed
     */
    public String getExternalSystemId() {
        return externalSystemId;
    }
}
