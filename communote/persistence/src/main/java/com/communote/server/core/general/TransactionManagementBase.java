package com.communote.server.core.general;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 * Spring Service base class for
 * <code>com.communote.server.service.general.TransactionManagement</code>, provides access
 * to all services and entities referenced by this service.
 * </p>
 * 
 * @see com.communote.server.core.general.TransactionManagement
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
@Transactional(propagation = Propagation.REQUIRED)
public abstract class TransactionManagementBase implements
        com.communote.server.core.general.TransactionManagement {

    /**
     * {@inheritDoc}
     */
    public void execute(
            com.communote.server.core.general.RunInTransaction runInTransaction) {
        if (runInTransaction == null) {
            throw new IllegalArgumentException(
                    "TransactionManagement.execute(RunInTransaction runInTransaction) -"
                            + " 'runInTransaction' can not be null");
        }
        try {
            this.handleExecute(runInTransaction);
        } catch (RuntimeException rt) {
            throw new com.communote.server.core.general.TransactionManagementException(
                    "Error performing 'TransactionManagement.execute(RunInTransaction runInTransaction)' --> "
                            + rt, rt);
        }
    }

    /**
     * Gets the current <code>principal</code> if one has been set, otherwise returns
     * <code>null</code>.
     * 
     * @return the current principal
     */
    protected java.security.Principal getPrincipal() {
        return com.communote.server.PrincipalStore.get();
    }

    /**
     * {@inheritDoc}
     */
    protected abstract void handleExecute(
            com.communote.server.core.general.RunInTransaction runInTransaction);
}