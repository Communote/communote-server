package com.communote.server.core.general;

/**
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public interface RunInTransaction {

    /**
     * 
     */
    public void execute()
            throws com.communote.server.core.general.TransactionException;

}