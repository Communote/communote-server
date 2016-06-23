package com.communote.server.test.installer;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;

/**
 * Initial context factory to provide a mock context.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class MockInitialContextFactory implements InitialContextFactory {

    private static Context MOCK_CONTEXT = null;

    /**
     * Sets the mock object for the initial context
     *
     * @param context
     *            the context
     */
    public static void setMockContext(Context context) {
        MOCK_CONTEXT = context;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Context getInitialContext(Hashtable<?, ?> environment) throws NamingException {
        if (MOCK_CONTEXT == null) {
            throw new IllegalStateException("The mock context was not set");
        }
        return MOCK_CONTEXT;
    }

}
