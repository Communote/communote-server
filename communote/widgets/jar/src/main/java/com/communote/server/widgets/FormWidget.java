package com.communote.server.widgets;

/**
 * Simple widget that differentiates between submits and refreshs. A request is considered a submit
 * if it is a POST request, otherwise it is a refresh.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class FormWidget extends AbstractWidget {

    /**
     * Handle a refresh
     * 
     * @return the result to be passed to the view for rendering
     */
    public abstract Object handleRefresh();

    @Override
    public Object handleRequest() {
        if ("POST".equals(getRequest().getMethod())) {
            return handleSubmit();
        }
        return handleRefresh();
    }

    /**
     * Handle a submit
     * 
     * @return the result to be passed to the view for rendering
     */
    public abstract Object handleSubmit();

    @Override
    protected void initParameters() {
        // nothing
    }

}
