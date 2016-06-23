package com.communote.server.core.general;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

/**
 * @see com.communote.server.core.general.TransactionManagement
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
// TODO cleanup rollback behavior: TransactionException should state whether to rollback or not.
// Also: throwing the TransactionManagementException already causes a rollback!
// TODO add support for read-only operations
@Service("transactionManagement")
public class TransactionManagementImpl extends TransactionManagementBase {

    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionManagementImpl.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T execute(RunInTransactionWithResult<T> runInTransaction) {
        try {
            return runInTransaction.execute();
        } catch (TransactionException e) {
            LOGGER.warn("Rolling back transaction: {0}", e.getMessage());
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            // pass the actual cause
            throw new TransactionManagementException("Transactional operation failed", e.getCause());
        }
    }

    /**
     * This uses propagation REQUIRES_NEW
     *
     * @param runInTransaction
     *            {@link com.communote.server.core.general.RunInTransaction}
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void executeInNew(RunInTransaction runInTransaction) {
        try {
            runInTransaction.execute();
        } catch (TransactionException e) {
            LOGGER.warn("Rolling back transaction: {0}", e.getMessage());
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        }
    }

    /**
     * This uses propagation REQUIRES_NEW
     *
     * @param <T>
     *            Type of the result.
     * @param runInTransaction
     *            The run in transaction
     * @return The result.
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public <T> T executeInNew(RunInTransactionWithResult<T> runInTransaction) {
        try {
            return runInTransaction.execute();
        } catch (TransactionException e) {
            LOGGER.warn("Rolling back transaction: {0}", e.getMessage());
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            // pass the actual cause
            throw new TransactionManagementException("Transactional operation failed", e.getCause());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void handleExecute(RunInTransaction runInTransaction) {
        try {
            runInTransaction.execute();
        } catch (TransactionException e) {
            LOGGER.warn("Rolling back transaction: {0}", e.getMessage());
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        }
    }
}