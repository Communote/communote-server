package com.communote.server.widgets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controller for widget requests.<br>
 * <br>
 * The controller creates a new {@link Widget} instance which will be invoked to handle the request.
 * The instance is created with the help of a configurable {@link WidgetFactory} which is by default
 * an instance of {@link SimpleWidgetFactory}. The factory is passed the group and the type name of
 * the widget and additionally has access to the request parameters to select and instantiate the
 * correct widget class. The widget group and name can be provided as request parameters named
 * 'widgetGroup' and 'widget'. Alternatively they can be part of the request URL which is expected
 * to look like /widgets/widgetGroup/widgetTypeName.widget The widgetGroup can contain slashes (in
 * both variants) for further structuring. Whether to parse the URL or check the parameters for
 * group and name can be defined with {@link #setUseRequestParametersForWidget(boolean)}. URL
 * parsing is the default.
 *
 *
 * @param <V>
 *            the type of view the controller supports
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */

public abstract class WidgetController<V> {

    private static final Logger LOGGER = LoggerFactory.getLogger(WidgetController.class);
    /**
     * URL pattern
     */
    // allow the url to start either with "rsswidgets" or "widgets"
    // allow the url to end with ;jsessionid
    private final static Pattern PATTERN_URI = Pattern
            .compile("/(?:rss)?widgets/(.+)/([^/]+).widget(?:;jsessionid=[a-zA-Z0-9.]+)?$");
    /**
     * Parameter for the type of the widget
     */
    private static final String PARAM_WIDGET_NAME = "widget";

    /**
     * Parameter for the group / part of the package of the widget
     */
    private static final String PARAM_WIDGET_GROUP = "widgetGroup";

    /**
     * widget type identifier (e.g. dhtml)
     */
    public static final String WIDGET_TYPE = "type";
    /**
     * single object result identifier in the view: If it the widgets result is a single reslult it
     * will be available in the request after the widgets handleRequest has been called
     */
    public static final String OBJECT_SINGLE = "singleResult";
    /**
     * object list identifier in the view: If the result is a list it will be available in the
     * request after the widgets handleRequest has been called
     */
    public static final String OBJECT_LIST = "list";
    /**
     * Widget identifier in the view: It will be available in the request after the widgets
     * handleRequest has been called
     */
    public static final String OBJECT_WIDGET = "widget";

    private boolean useRequestParametersForWidget;

    private boolean appendWidgetToTypeName = false;

    private WidgetFactory widgetFactory;

    /**
     * Default constructor
     */
    public WidgetController() {
        // create default factory
        widgetFactory = new SimpleWidgetFactory();
    }

    /**
     * Creates and prepares the view to render the response of the widget request. This method is
     * invoked if the widget is expected to return a list of items.
     *
     * @param request
     *            the request
     * @param widget
     *            the widget that was invoked
     * @param result
     *            the result returned from the query method of the widget
     * @return the view
     */
    protected abstract V createMultiResultView(HttpServletRequest request, Widget widget,
            List<?> result);

    /**
     * Creates and prepares the view to render the response of the widget request. This method is
     * invoked if the widget is expected to return a single item.
     *
     * @param request
     *            the request
     * @param widget
     *            the widget that was invoked
     * @param result
     *            the result returned from the query method of the widget
     * @return the view
     */
    protected abstract V createSingleResultView(HttpServletRequest request, Widget widget,
            Object result);

    /**
     * Get the widget for the given request and configure it
     *
     * @param request
     *            The request
     * @param response
     *            the response to use
     * @return the configured widget
     */
    protected Widget getWidgetInstance(HttpServletRequest request, HttpServletResponse response) {

        // get the widget
        Widget widget = newWidgetInstance(request);
        widget.setRequest(request);
        widget.setResponse(response);

        // set the parameters
        setWidgetParameters(request, widget);

        return widget;
    }

    /**
     * Parses the request, extracts the widget and invokes its query method. The result of the query
     * will be used to prepare a view. The created view will be returned and can be passed to a
     * rendering engine. The view creation is delegated to
     * {@link #createSingleResultView(HttpServletRequest, Widget, Object)} or
     * {@link #createMultiResultView(HttpServletRequest, Widget, List)} depending on the expected
     * result type of the widget.
     *
     * @param request
     *            the request
     * @param response
     *            the response
     * @return the view
     */
    public V handleWidgetRequest(HttpServletRequest request, HttpServletResponse response) {
        Widget widget = getWidgetInstance(request, response);
        V view;
        Object result = widget.handleRequest();
        if (result instanceof List) {
            view = createMultiResultView(request, widget, (List<?>) result);
        } else {
            view = createSingleResultView(request, widget, result);
        }
        // TODO code duplication with ControllerHandler
        if (widget.isSuccess()) {
            response.setHeader("X-APPLICATION-RESULT", "OK");
        } else {
            response.setHeader("X-APPLICATION-RESULT", "ERROR");
        }
        return view;
    }

    /**
     * Get the widget associate to this request uri. Return null if none exists.
     *
     * @param request
     *            the request
     * @return the widget associated to this uri
     */
    private Widget newWidgetInstance(HttpServletRequest request) {
        String widgetName;
        String widgetGroup;

        if (useRequestParametersForWidget) {
            // get it from parameters
            widgetName = request.getParameter(PARAM_WIDGET_NAME);
            widgetGroup = request.getParameter(PARAM_WIDGET_GROUP);
        } else {
            // get it from the url
            Matcher matcher = PATTERN_URI.matcher(request.getRequestURI());
            widgetName = StringUtils.EMPTY;
            widgetGroup = StringUtils.EMPTY;
            if (matcher.find()) {
                widgetGroup = matcher.group(1);
                widgetName = matcher.group(2);
            }
        }

        if (appendWidgetToTypeName) {
            widgetName += "Widget";
        }

        try {
            Widget widgetInstance = widgetFactory.createWidget(widgetGroup, widgetName,
                    request.getParameterMap());
            widgetInstance.setGroupName(widgetGroup);
            return widgetInstance;
        } catch (WidgetCreationException e) {
            LOGGER.error("Error creating widget " + widgetName + " of group " + widgetGroup, e);
        }
        throw new RuntimeException("Creating widget  " + widgetName + " of group " + widgetGroup
                + " failed");
    }

    /**
     * Whether to append the static string 'Widget' to all widget type names provided by URL or
     * request parameter. Defaults to false.
     *
     * @param appendWidget
     *            true to append the static string
     */
    public void setAppendWidgetToTypeName(boolean appendWidget) {
        this.appendWidgetToTypeName = appendWidget;
    }

    /**
     * Determines how a widget is found. If true the request parameters widgetGroup and widget are
     * used to determine the widget group and widget type name. Otherwise the URL is parsed and is
     * expected to look like /widgets/widgetGroupName/widgetTypeName.widget.
     *
     *
     * @param useRequestParametersForWidget
     *            true if the request parameters should be used
     */
    public void setUseRequestParametersForWidget(boolean useRequestParametersForWidget) {
        this.useRequestParametersForWidget = useRequestParametersForWidget;
    }

    /**
     * Set the factory to use for creating an instance of a widget that should handle a widget
     * request.
     *
     * @param factory
     *            the factory to use
     */
    public void setWidgetFactory(WidgetFactory factory) {
        if (factory == null) {
            throw new IllegalArgumentException("The factory must not be null");
        }
        this.widgetFactory = factory;
    }

    /**
     * Import parameters from request to widget
     *
     * @param request
     *            The request to import parameters from
     * @param widget
     *            The widget to set the parameters on
     */
    private void setWidgetParameters(HttpServletRequest request, Widget widget) {
        Map<?, ?> params = request.getParameterMap();
        Map<String, ArrayList<String>> arrayBuffer = new HashMap<String, ArrayList<String>>();

        for (Entry<?, ?> param : params.entrySet()) {
            String name = (String) param.getKey();
            String[] values = (String[]) param.getValue();
            String value = values[0];
            if (!WIDGET_TYPE.equals(name)) {
                // handle parameter like name[0] as array item
                if (name.matches("[^\\[]+?\\[\\d+\\]$")) {
                    name = StringUtils.substringBefore(name, "[");
                    if (!arrayBuffer.containsKey(name)) {
                        arrayBuffer.put(name, new ArrayList<String>());
                    }
                    arrayBuffer.get(name).add(value);
                } else {
                    widget.setParameter(name, value);
                }
            }
        }
        // transform arrays into string with ',' separator
        for (Entry<String, ArrayList<String>> entry : arrayBuffer.entrySet()) {
            String value = StringUtils.join(entry.getValue(), ",");
            String key = entry.getKey();
            widget.setParameter(key, value);
        }
    }
}
