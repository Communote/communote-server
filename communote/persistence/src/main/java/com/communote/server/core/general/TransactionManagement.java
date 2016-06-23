package com.communote.server.core.general;

/**
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public interface TransactionManagement {
    /**
     * This uses propagation REQUIRES_NEW
     * 
     * @param runInTransaction
     *            {@link RunInTransaction}
     */
    public void executeInNew(RunInTransaction runInTransaction);

    /**
     * This uses propagation REQUIRES_NEW
     * 
     * @param <T>
     *            Type of the result.
     * @param runInTransaction
     *            The run in transaction
     * @return The result.
     */
    public <T> T executeInNew(
            RunInTransactionWithResult<T> runInTransaction);

    /**
     * @param runInTransaction
     *            {@link RunInTransaction}
     */
    public void execute(RunInTransaction runInTransaction);

    /**
     * @param <T>
     *            Type of the result.
     * @param runInTransaction
     *            The run in transaction
     * @return The result.
     */
    public <T> T execute(
            RunInTransactionWithResult<T> runInTransaction);

}
