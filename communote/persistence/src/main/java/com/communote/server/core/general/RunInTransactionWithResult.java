package com.communote.server.core.general;

/**
 * Wrapper for actions, which should run within a transaction
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 * @param <T>
 *            Type of the result.
 */

public interface RunInTransactionWithResult<T> {

    /**
     * @return Object of type T
     * @throws TransactionException
     *             Exception.
     */
    public T execute() throws TransactionException;

}