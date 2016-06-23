package com.communote.server.api.core.bootstrap;

import com.communote.common.i18n.LocalizedMessage;

/**
 * Exception to be thrown if basic bootstrapping like setting up logging succeeded but initializing
 * the main application components failed
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class ApplicationInitializationException extends BootstrapException {
    /**
     * default serial version UID
     */
    private static final long serialVersionUID = 1L;
    private final LocalizedMessage localizedDetails;

    /**
     * Create a new exception with a details message
     *
     * @param message
     *            the details message
     */
    public ApplicationInitializationException(String message) {
        this(message, (LocalizedMessage) null);
    }

    /**
     * Create a new ApplicationInitializationException with the specified message and additional
     * localized details.
     *
     * @param message
     *            the details message
     * @param localizedDetails
     *            additional localized details
     */
    public ApplicationInitializationException(String message, LocalizedMessage localizedDetails) {
        super(message);
        this.localizedDetails = localizedDetails;
    }

    /**
     * Create a new ApplicationInitializationException with the specified message and root cause.
     *
     * @param message
     *            the message
     * @param cause
     *            the root cause
     */
    public ApplicationInitializationException(String message, Throwable cause) {
        this(message, cause, null);
    }

    /**
     * Create a new ApplicationInitializationException with the specified message and root cause.
     *
     * @param message
     *            the message
     * @param cause
     *            the root cause
     * @param localizedDetails
     *            additional localized details
     */
    public ApplicationInitializationException(String message, Throwable cause,
            LocalizedMessage localizedDetails) {
        super(message, cause);
        this.localizedDetails = localizedDetails;
    }

    /**
     * @return additional localized details or null if no details are available
     */
    public LocalizedMessage getLocalizedDetails() {
        return localizedDetails;
    }
}
