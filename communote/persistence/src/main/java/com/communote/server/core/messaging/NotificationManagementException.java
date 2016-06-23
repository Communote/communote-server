package com.communote.server.core.messaging;

/**
 * The default exception thrown for unexpected errors occurring within
 * {@link com.communote.server.core.messaging.NotificationManagement}.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class NotificationManagementException extends RuntimeException {
    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = -3461445946301039149L;

    /**
     * Constructs a new instance of <code>NotificationManagementException</code>.
     *
     * @param message
     *            the throwable message.
     */
    public NotificationManagementException(String message) {
        super(message);
    }

    /**
     * Constructs a new instance of <code>NotificationManagementException</code>.
     *
     * @param message
     *            the throwable message.
     * @param throwable
     *            the cause
     */
    public NotificationManagementException(String message, Throwable throwable) {
        super(message, throwable);
    }

}