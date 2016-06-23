package com.communote.server.widgets;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

/**
 * Simple implementation of the WidgetController that returns a string which identifies the view and
 * stores the details for rendering the view as attributes in the request.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class SimpleWidgetController extends WidgetController<String> {

    /**
     * {@inheritDoc}
     */
    @Override
    protected String createMultiResultView(HttpServletRequest request, Widget widget, List<?> result) {
        return prepareView(request, widget, result, WidgetController.OBJECT_LIST);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String createSingleResultView(HttpServletRequest request, Widget widget, Object result) {
        return prepareView(request, widget, result, WidgetController.OBJECT_SINGLE);
    }

    /**
     * Prepare the view rendering by storing all required details as request attributes.
     * 
     * @param request
     *            the request
     * @param widget
     *            the widget to be rendered
     * @param result
     *            the result returned by the query method of the widget
     * @param resultType
     *            a constant defining the type of the result parameter
     * @return a string identifying the view
     */
    private String prepareView(HttpServletRequest request, Widget widget, Object result,
            String resultType) {
        request.setAttribute(WidgetController.OBJECT_WIDGET, widget);
        return widget.getViewIdentifier();
    }

}
