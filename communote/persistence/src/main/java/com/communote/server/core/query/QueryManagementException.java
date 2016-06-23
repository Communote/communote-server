package com.communote.server.core.query;

/**
 * The default exception thrown for unexpected errors occurring within
 * {@link com.communote.server.core.query.QueryManagement}.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class QueryManagementException extends RuntimeException {
    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = 190491449548619405L;

    /**
     * Constructs a new instance of <code>QueryManagementException</code>.
     *
     * @param message
     *            the throwable message.
     */
    public QueryManagementException(String message) {
        super(message);
    }

    /**
     * Constructs a new instance of <code>QueryManagementException</code>.
     *
     * @param message
     *            the throwable message.
     * @param throwable
     *            the parent of this Throwable.
     */
    public QueryManagementException(String message, Throwable throwable) {
        super(message, throwable);
    }
}