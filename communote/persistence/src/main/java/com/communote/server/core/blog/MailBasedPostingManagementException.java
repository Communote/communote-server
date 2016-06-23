package com.communote.server.core.blog;


/**
 * The default exception thrown for unexpected errors occurring within
 * {@link com.communote.server.core.blog.MailBasedPostingManagement}.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class MailBasedPostingManagementException extends RuntimeException {
    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = 5784412944809066935L;

    /**
     * Constructs a new instance of <code>MailBasedPostingManagementException</code>.
     *
     * @param message
     *            the throwable message.
     */
    public MailBasedPostingManagementException(String message) {
        super(message);
    }

    /**
     * Constructs a new instance of <code>MailBasedPostingManagementException</code>.
     *
     * @param message
     *            the throwable message.
     * @param throwable
     *            the parent of this Throwable.
     */
    public MailBasedPostingManagementException(String message, Throwable throwable) {
        super(message, throwable);
    }
}