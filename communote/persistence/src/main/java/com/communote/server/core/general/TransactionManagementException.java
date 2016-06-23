package com.communote.server.core.general;


/**
 * The default exception thrown for unexpected errors occurring within
 * {@link com.communote.server.core.general.TransactionManagement}.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class TransactionManagementException extends RuntimeException {
    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = -3626127344692709190L;

    /**
     * Constructs a new instance of <code>TransactionManagementException</code>.
     *
     * @param message
     *            the throwable message.
     */
    public TransactionManagementException(String message) {
        super(message);
    }

    /**
     * Constructs a new instance of <code>TransactionManagementException</code>.
     *
     * @param message
     *            the throwable message.
     * @param throwable
     *            the parent of this Throwable.
     */
    public TransactionManagementException(String message, Throwable throwable) {
        super(message, throwable);
    }

}