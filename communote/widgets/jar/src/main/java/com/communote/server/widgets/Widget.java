package com.communote.server.widgets;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * A widget
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */

public interface Widget {

    /**
     * Parameter of the widget id
     */
    public final static String PARAM_WIDGET_ID = "widgetId";

    /**
     * return widget attribute
     *
     * @param key
     *            the parameter key
     * @return the attribute value
     */

    public Object getAttribute(String key);

    /**
     * Get the group name to build the url based on it.
     *
     * @return relative path in which package this controller can be found
     */
    public String getGroupName();

    /**
     * return widget parameter
     *
     * @param key
     *            the parameter key
     * @return the parameter value
     */

    public String getParameter(String key);

    /**
     * Get the parameter list
     *
     * @return Parameter map
     */
    public Map<String, String> getParameters();

    /**
     * Return a JSON object containing arbitrary metadata the server-side widget component wants to
     * transmit to the client-side component as part of the current response.
     *
     * @return a string serialized JSON object or null if no metadata needs to be transmitted
     */
    public String getResponseMetadata();

    /**
     * Get the tile to be used for rendering
     *
     * @param outputType
     *            the output type (rss, dhtml ...) without any '.'
     * @return The tile to show
     * @deprecated user {@link #getViewIdentifier(String)} instead
     */
    @Deprecated
    public String getTile(String outputType);

    /**
     * Get the view identifier which defines the view for rendering the widgets content
     *
     * @return The tile to show
     */
    public String getViewIdentifier();

    /**
     * Get the id of the current widget
     *
     * @return the widget id
     */
    public String getWidgetId();

    /**
     * Handle the request and return a result
     *
     * @return the result
     */
    public Object handleRequest();

    /**
     * @return if the widget request was successful
     */
    public boolean isSuccess();

    /**
     * Set the group name to build the url based on it.
     *
     * @param path
     *            relative path in which package this controller can be found
     */
    public void setGroupName(String path);

    /**
     * set widget parameter
     *
     * @param key
     *            the parameter key
     * @param value
     *            the parameter value
     */

    public void setParameter(String key, String value);

    /**
     * set the http servlet request for this widget
     *
     * @param request
     *            the request
     */
    public void setRequest(HttpServletRequest request);

    /**
     * set the http servlet response for this widget
     *
     * @param response
     *            the response
     */
    public void setResponse(HttpServletResponse response);
}
