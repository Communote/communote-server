package com.communote.server.core.general;

/**
 * Exception to throw if a {@link RunInTransaction} or {@link RunInTransactionWithResult} operation
 * failed because of an application exception. The actual cause will be wrapped by this exception.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class TransactionException extends Exception {

    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = -8890655638736959799L;

    /**
     * Constructs a new instance of TransactionException
     *
     */
    public TransactionException(String message, Throwable throwable) {
        super(message, throwable);
    }

}
