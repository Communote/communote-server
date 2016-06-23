package com.communote.server.widgets;

/**
 * An empty widget for widgets with no functionality, just displaying functionality
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class EmptyWidget extends AbstractWidget {

    /**
     * {@inheritDoc}
     */
    @Override
    public Object handleRequest() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initParameters() {
        // Do nothing.
    }

}
