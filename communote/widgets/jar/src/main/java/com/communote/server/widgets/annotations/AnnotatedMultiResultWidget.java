package com.communote.server.widgets.annotations;

import static com.communote.server.widgets.annotations.AnnotatedWidgetProcesser.processActions;
import static com.communote.server.widgets.annotations.AnnotatedWidgetProcesser.processGetViewIdentifier;

import java.util.List;

import com.communote.server.widgets.AbstractMultipleResultWidget;

/**
 * 
 * @param <I>
 *            type of the result items
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class AnnotatedMultiResultWidget<I> extends AbstractMultipleResultWidget<I> {

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
    @Override
    protected List<I> handleQueryList() {
        processActions(this);
        return processQueryList();
    }

    /**
     * @see AbstractMultipleResultWidget#handleQueryList
     * @return List of items.
     */
    protected abstract List<I> processQueryList();
}
