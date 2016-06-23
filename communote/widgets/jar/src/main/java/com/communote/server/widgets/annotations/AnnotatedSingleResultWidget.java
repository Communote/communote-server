package com.communote.server.widgets.annotations;

import static com.communote.server.widgets.annotations.AnnotatedWidgetProcesser.processActions;
import static com.communote.server.widgets.annotations.AnnotatedWidgetProcesser.processGetViewIdentifier;

import com.communote.server.widgets.AbstractWidget;

/**
 * You could use WidgetAction with this widget class.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class AnnotatedSingleResultWidget extends AbstractWidget {

    /**
     * {@inheritDoc}
     */
    @Deprecated
    public String getTile(String outputType) {
        return getViewIdentifier();
    }

    /**
     * @see AnnotatedWidgetProcesser#processGetViewIdentifier(com.communote.server.widgets.Widget).
     *      {@inheritDoc}
     */
    @Override
    public String getViewIdentifier() {
        return processGetViewIdentifier(this);
    }

    /**
     * Dispatches to call selected "widgetAction" before returning the result. {@inheritDoc}
     */
    public Object handleRequest() {
        processActions(this);
        return processSingleResult();
    }

    /**
     * @return the result
     */
    protected abstract Object processSingleResult();
}
