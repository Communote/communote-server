package com.communote.server.core.storing;


/**
 * The default exception thrown for unexpected errors occurring within
 * {@link com.communote.server.core.storing.ResourceStoringManagement}.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ResourceStoringManagementException extends RuntimeException {
    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = 4504656427484994022L;

    /**
     * Constructs a new instance of <code>ResourceStoringManagementException</code>.
     *
     * @param message
     *            the throwable message.
     */
    public ResourceStoringManagementException(String message) {
        super(message);
    }

    /**
     * Constructs a new instance of <code>ResourceStoringManagementException</code>.
     *
     * @param message
     *            the throwable message.
     * @param throwable
     *            the parent of this Throwable.
     */
    public ResourceStoringManagementException(String message, Throwable throwable) {
        super(message, throwable);
    }
}