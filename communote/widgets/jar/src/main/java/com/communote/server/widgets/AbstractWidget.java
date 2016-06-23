package com.communote.server.widgets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.communote.common.string.StringEscapeHelper;

/**
 * abstract implementation of the widget interface
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */

public abstract class AbstractWidget implements Widget {

    private static final String BLANK = " ";
    private static final String COMMA = ",";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractWidget.class);

    /**
     * current output type
     */
    private static final String OUTPUT_TYPE_DHTML = "dhtml";

    /**
     * Key to be used in the response metadata to denote that a widget has no content.
     */
    protected static final String METADATA_KEY_NO_CONTENT = "noContent";

    private HttpServletRequest request;
    private HttpServletResponse response;

    private final Map<String, String> parameters = new HashMap<String, String>();
    private final Map<String, Object> attributes = new HashMap<String, Object>();
    private final Map<String, Object> responseMetadata = new HashMap<String, Object>();

    private String groupName = "";

    private boolean success = true;

    private String widgetId;

    /**
     * The initParameters() method is invoked implicitly.
     */

    public AbstractWidget() {

        this.initParameters();

    }

    /**
     * Determine the output type based on the request
     * 
     * @return the output type (e.g. dhtml)
     */
    protected String determineOutputType() {
        // determine output type
        String outputType = OUTPUT_TYPE_DHTML;
        if (request.getParameter(WidgetController.WIDGET_TYPE) != null) {
            outputType = request.getParameter(WidgetController.WIDGET_TYPE).toLowerCase();
        }
        return outputType;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getAttribute(String key) {
        return attributes.get(key);
    }

    /**
     * Get a parameter for a key and parse it to a <code>boolean</code> value
     * 
     * @param key
     *            the key
     * @param fallback
     *            the fall back to use in case of an NumberFormatException
     * @return the parsed value
     */
    public boolean getBooleanParameter(String key, boolean fallback) {
        if ("true".equalsIgnoreCase(parameters.get(key))) {
            fallback = Boolean.TRUE;
        } else if ("false".equalsIgnoreCase(parameters.get(key))) {
            fallback = Boolean.FALSE;
        }

        return fallback;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getGroupName() {
        return groupName;
    }

    /**
     * Get a parameter for a key and parse it to a <code>int</code> value
     * 
     * @param key
     *            the key
     * @return the parsed value
     */
    public int getIntParameter(String key) {

        return Integer.parseInt(parameters.get(key));
    }

    /**
     * Get a parameter for a key and parse it to a <code>int</code> value
     * 
     * @param key
     *            the key
     * @param fallback
     *            the fall back to use in case of an NumberFormatException
     * @return the parsed value
     */
    public int getIntParameter(String key, int fallback) {

        try {
            return Integer.parseInt(parameters.get(key));
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    /**
     * Get the value of the given parameter. If it is not null split it by COMMA. Return the array.
     * (null if the value of the parameter is null)
     * 
     * @param parameterName
     *            the name of the parameter
     * @return an array with the parameters splitted by comma, or null if the was parameter not set
     *         or did not contain any parseable long
     */
    public Long[] getLongArrayParameter(String parameterName) {
        String value = getParameter(parameterName);
        String[] splitted = value == null ? null : value.split(COMMA);
        if (splitted == null) {
            return null;
        }
        List<Long> longValues = new ArrayList<Long>();
        for (String s : splitted) {
            try {
                longValues.add(Long.parseLong(s));
            } catch (NumberFormatException e) {
                // ignore
            }
        }
        return longValues.size() == 0 ? null : longValues.toArray(new Long[1]);
    }

    /**
     * Get a parameter for a key and parse it to a <code>long</code> value
     * 
     * @param key
     *            the key
     * @return the parsed value
     */
    public long getLongParameter(String key) {

        return Long.parseLong(parameters.get(key));
    }

    /**
     * Get a parameter for a key and parse it to a <code>long</code> value
     * 
     * @param key
     *            the key
     * @param fallback
     *            the fallback to use in case of an NumberFormatException
     * @return the parsed value
     */
    public long getLongParameter(String key, long fallback) {
        try {
            return Long.parseLong(parameters.get(key));
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    /**
     * return widget parameter
     * 
     * @param key
     *            the parameter key
     * @return the parameter value
     */
    @Override
    public String getParameter(String key) {
        return parameters.get(key);
    }

    /**
     * return widget parameter
     * 
     * @param key
     *            the parameter key
     * @param fallback
     *            the fallback if the parameter key doesn't exist
     * @return the parameter value
     */

    public String getParameter(String key, String fallback) {

        if (parameters.containsKey(key)) {

            return parameters.get(key);
        } else {

            return fallback;
        }
    }

    /**
     * Return a parameter with all characters that are not in [A-Za-z0-9_.-] escaped with
     * underscore. Helps to avoid XSS when the parameter, which might have been manipulated, needs
     * to be written out to the HTML so that it can be processed in Javascript code. The return
     * value should be stored in a local variable when needed more than once.
     * 
     * @param key
     *            the name of the parameter
     * @return the escaped parameter value or null if not set
     */
    public String getParameterEscaped(String key) {
        String value = parameters.get(key);
        if (value != null) {
            return StringEscapeHelper.escapeNonWordCharacters(value);
        }
        return null;
    }

    /**
     * Like {@link #getParameterEscaped(String)} but returns the provided fallback if the parameter
     * is not set. The fallback is processed by the escape function.
     * 
     * @param key
     *            the name of the parameter to get
     * @param fallback
     *            the fallback value if the parameter is not set
     * @return the parameter value or the fallback
     */
    public String getParameterEscaped(String key, String fallback) {
        String value = getParameterEscaped(key);
        if (value == null) {
            return fallback;
        }
        return value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, String> getParameters() {
        return parameters;
    }

    /**
     * get the request for getting some more information
     * 
     * @return the requet for this widget
     */
    protected HttpServletRequest getRequest() {
        return request;
    }

    /**
     * The response object the widget is working on. Can for instance be used to set some header
     * values.
     * 
     * @return the response object
     */
    protected HttpServletResponse getResponse() {
        return response;
    }

    @Override
    public String getResponseMetadata() {
        if (!responseMetadata.isEmpty()) {
            try {
                return OBJECT_MAPPER.writeValueAsString(responseMetadata);
            } catch (IOException e) {
                LOGGER.error("Converting response metadata to JSON failed", e);
            }
        }
        return null;
    }

    /**
     * Get the value of the given parameter. If it is not null split it by BLANK. Return the array.
     * (null if the value of the parameter is null)
     * 
     * @param parameterName
     *            name of the parameter key
     * @return an array with the parameters splitted by blank
     */
    public String[] getStringArrayParameter(String parameterName) {
        String value = getParameter(parameterName);
        String[] splitted = StringUtils.split(value, BLANK);
        return splitted;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getViewIdentifier() {
        return getTile(determineOutputType());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getWidgetId() {
        if (this.widgetId == null) {
            this.widgetId = getParameterEscaped(PARAM_WIDGET_ID);
        }
        return this.widgetId;
    }

    /**
     * abstract method hook that initializes the widget's parameters
     */

    protected abstract void initParameters();

    /**
     * {@inheritDoc} Is true by default.
     */
    @Override
    public boolean isSuccess() {
        return this.success;
    }

    /**
     * {@inheritDoc}
     */
    public void setAttribute(String key, Object value) {
        attributes.put(key, value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setGroupName(String path) {
        this.groupName = path;

    }

    /**
     * set widget parameter
     * 
     * @param key
     *            the parameter key
     * @param value
     *            the parameter value
     */

    @Override
    public void setParameter(String key, String value) {
        parameters.put(key, value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setRequest(HttpServletRequest request) {
        this.request = request;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setResponse(HttpServletResponse response) {
        this.response = response;
    }

    /**
     * Add, remove or replace an entry in the response metadata that should be returned when
     * {@link #getResponseMetadata()} is called.
     * 
     * @param key
     *            the key of the entry to modify
     * @param value
     *            the value of the metadata entry, if null the entry with the provided key will be
     *            removed
     */
    protected void setResponseMetadata(String key, Object value) {
        if (value == null) {
            responseMetadata.remove(key);
        } else {
            responseMetadata.put(key, value);
        }
    }

    /**
     * Set whether the widget request was successful.
     * 
     * @param success
     *            true if the request succeeded
     */
    protected void setSuccess(boolean success) {
        this.success = success;
    }
}
