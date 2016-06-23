package com.communote.server.core.blog;


/**
 * The default exception thrown for unexpected errors occurring within
 * {@link com.communote.server.api.core.blog.BlogRightsManagement}.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class BlogRightsManagementException extends RuntimeException {
    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = -6148135248011601079L;

    /**
     * Constructs a new instance of <code>BlogRightsManagementException</code>.
     *
     * @param message
     *            the throwable message.
     */
    public BlogRightsManagementException(String message) {
        super(message);
    }

    /**
     * Constructs a new instance of <code>BlogRightsManagementException</code>.
     *
     * @param message
     *            the throwable message.
     * @param throwable
     *            the cause
     */
    public BlogRightsManagementException(String message, Throwable throwable) {
        super(message, throwable);
    }
}