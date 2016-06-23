package com.communote.server.core.blog;


/**
 * The default exception thrown for unexpected errors occurring within
 * {@link com.communote.server.api.core.blog.BlogManagement}.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class BlogManagementException extends RuntimeException {
    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = -964716535516128984L;

    /**
     * Constructs a new instance of <code>BlogManagementException</code>.
     *
     * @param message
     *            the throwable message.
     */
    public BlogManagementException(String message) {
        super(message);
    }

    /**
     * Constructs a new instance of <code>BlogManagementException</code>.
     *
     * @param message
     *            the throwable message.
     * @param throwable
     *            the parent of this Throwable.
     */
    public BlogManagementException(String message, Throwable throwable) {
        super(message, throwable);
    }
}