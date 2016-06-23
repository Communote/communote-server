package com.communote.server.core.security.iprange;


/**
 * The default exception thrown for unexpected errors occurring within
 * {@link com.communote.server.core.security.iprange.IpRangeFilterManagement}.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class IpRangeFilterManagementException extends RuntimeException {
    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = -677204979596321988L;

    /**
     * Constructs a new instance of <code>IpRangeFilterManagementException</code>.
     *
     * @param message
     *            the throwable message.
     */
    public IpRangeFilterManagementException(String message) {
        super(message);
    }

    /**
     * Constructs a new instance of <code>IpRangeFilterManagementException</code>.
     *
     * @param message
     *            the throwable message.
     * @param throwable
     *            the parent of this Throwable.
     */
    public IpRangeFilterManagementException(String message, Throwable throwable) {
        super(message, throwable);
    }
}