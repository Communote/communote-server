package com.communote.server.core.user;


/**
 * The default exception thrown for unexpected errors occurring within
 * {@link com.communote.server.core.user.MasterDataManagement}.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class MasterDataManagementException extends RuntimeException {
    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = 3447796721132571971L;

    /**
     * Constructs a new instance of <code>MasterDataManagementException</code>.
     *
     * @param message
     *            the throwable message.
     */
    public MasterDataManagementException(String message) {
        super(message);
    }

    /**
     * Constructs a new instance of <code>MasterDataManagementException</code>.
     *
     * @param message
     *            the throwable message.
     * @param throwable
     *            the parent of this Throwable.
     */
    public MasterDataManagementException(String message, Throwable throwable) {
        super(message, throwable);
    }
}