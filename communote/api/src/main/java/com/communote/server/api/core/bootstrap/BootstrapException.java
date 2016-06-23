package com.communote.server.api.core.bootstrap;

/**
 * Exception to be thrown if the bootstrapping fails.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class BootstrapException extends RuntimeException {

    /**
     * default serial version UID
     */
    private static final long serialVersionUID = 1L;

    /**
     * Create a new BootstrapException with the specified message.
     *
     * @param message
     *            the details message
     * @param localizedDetails
     *            additional localized details
     */
    public BootstrapException(String message) {
        super(message);
    }

    /**
     * Create a new BootstrapException with the specified message and root cause.
     *
     * @param message
     *            the message
     * @param cause
     *            the root cause
     */
    public BootstrapException(String message, Throwable cause) {
        super(message, cause);
    }

}
