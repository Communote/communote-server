package com.communote.server.core.mail;


/**
 * The default exception thrown for unexpected errors occurring within
 * {@link com.communote.server.core.mail.MailManagement}.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class MailManagementException extends RuntimeException {
    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = 3729463982507884248L;

    /**
     * Constructs a new instance of <code>MailManagementException</code>.
     *
     * @param message
     *            the throwable message.
     */
    public MailManagementException(String message) {
        super(message);
    }

    /**
     * Constructs a new instance of <code>MailManagementException</code>.
     *
     * @param message
     *            the throwable message.
     * @param throwable
     *            the parent of this Throwable.
     */
    public MailManagementException(String message, Throwable throwable) {
        super(message, throwable);
    }
}