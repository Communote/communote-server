package com.communote.server.core.security.ssl;


/**
 * The default exception thrown for unexpected errors occurring within
 * {@link com.communote.server.core.security.ssl.ChannelManagement}.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ChannelManagementException extends RuntimeException {
    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = 5592658099223447098L;

    /**
     * Constructs a new instance of <code>ChannelManagementException</code>.
     *
     * @param message
     *            the throwable message.
     */
    public ChannelManagementException(String message) {
        super(message);
    }

    /**
     * Constructs a new instance of <code>ChannelManagementException</code>.
     *
     * @param message
     *            the throwable message.
     * @param throwable
     *            the cause
     */
    public ChannelManagementException(String message, Throwable throwable) {
        super(message, throwable);
    }
}