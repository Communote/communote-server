package com.communote.server.core;


/**
 * The default exception thrown for unexpected errors occurring within
 * {@link com.communote.server.core.ConfigurationManagement}.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ConfigurationManagementException extends RuntimeException {
    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = 5851472433834994845L;

    /**
     * Constructs a new instance of <code>ConfigurationManagementException</code>.
     *
     * @param message
     *            the throwable message.
     */
    public ConfigurationManagementException(String message) {
        super(message);
    }

    /**
     * Constructs a new instance of <code>ConfigurationManagementException</code>.
     *
     * @param message
     *            the throwable message.
     * @param throwable
     *            the parent of this Throwable.
     */
    public ConfigurationManagementException(String message, Throwable throwable) {
        super(message, throwable);
    }
}