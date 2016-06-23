package com.communote.server.core.external;


/**
 * The default exception thrown for unexpected errors occurring within
 * {@link com.communote.server.core.external.ExternalObjectManagement}.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ExternalObjectManagementException extends RuntimeException {
    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = 6947530008905857031L;

    /**
     * Constructs a new instance of <code>ExternalObjectManagementException</code>.
     *
     * @param message
     *            the throwable message.
     */
    public ExternalObjectManagementException(String message) {
        super(message);
    }

    /**
     * Constructs a new instance of <code>ExternalObjectManagementException</code>.
     *
     * @param message
     *            the throwable message.
     * @param throwable
     *            the parent of this Throwable.
     */
    public ExternalObjectManagementException(String message, Throwable throwable) {
        super(message, throwable);
    }
}