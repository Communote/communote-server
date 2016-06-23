package com.communote.server.widgets;

import java.util.List;

/**
 * Abstract widget to be used by Widgets which only return a multiple result
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * @param <I>
 *            type of the result items
 */
public abstract class AbstractMultipleResultWidget<I> extends AbstractWidget {

    /**
     * hook that actually requests the list on the api
     * 
     * @return list of items
     */

    protected abstract List<?> handleQueryList();

    /**
     * {@inheritDoc}
     */
    @Override
    public Object handleRequest() {
        return handleQueryList();
    }

}
