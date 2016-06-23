package com.communote.server.widgets;

import java.util.Map;

/**
 * Factory for creating a {@link Widget} instance for a given widget group name and widget type
 * name. Additionally the current request parameters can be evaluated to create a suitable widget.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public interface WidgetFactory {

    /**
     * Create a new widget instance that should handle the current widget request.
     *
     * @param widgetGroupName
     *            the group of the widget
     * @param widgetName
     *            the name of the widget
     * @param requestParameters
     *            the request parameters of the current request
     * @return the new widget instance
     * @throws WidgetCreationException
     *             in case the widget cannot be created
     */
    public Widget createWidget(String widgetGroupName, String widgetName,
            Map<String, String[]> requestParameters) throws WidgetCreationException;
}
