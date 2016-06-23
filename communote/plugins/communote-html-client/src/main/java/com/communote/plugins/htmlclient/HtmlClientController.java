package com.communote.plugins.htmlclient;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

import com.communote.common.util.ParameterHelper;
import com.communote.plugins.core.views.annotations.UrlMappings;
import com.communote.server.core.security.SecurityHelper;
import com.communote.server.core.user.UserManagementHelper;
import com.communote.server.core.user.helper.UserNameFormat;
import com.communote.server.core.user.helper.UserNameHelper;

/**
 * Controller for delivering the HTMLClient.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */

@Component
@Provides
@Instantiate(name = "HtmlClientController")
@UrlMappings(mappings = { "/*/portal/mobile" })
public class HtmlClientController implements Controller {

    private static final String JSESSION_ID = "jsessionId";

    /** The Constant STRING_TRUE. */
    private static final String STRING_TRUE = "true";

    /** The Constant STRING_FALSE. */
    private static final String STRING_FALSE = "false";

    /** The Constant STRING_DOUBLE_QUOTES. */
    private static final String STRING_DOUBLE_QUOTES = "\"";

    /** The Constant PARAM_TIMEZONE_OFFSET. */
    private static final String PARAM_TIMEZONE_OFFSET = "utcTimeZoneOffset";

    /** The Constant PARAM_LANG. */
    private static final String PARAM_LANG = "lang";

    /** Name of parameter to disable the header */
    private static final String PARAM_SHOW_HEADER = "showHeader";

    /**
     * Adds the session id if Client does not send cookies.
     *
     * @param request
     *            the request
     * @param jsParameterMap
     *            the js parameter map
     */
    public void addSessionIdIfNeeded(HttpServletRequest request, Map<String, String> jsParameterMap) {
        if (request.getRequestedSessionId() != null && !request.isRequestedSessionIdFromCookie()) {
            jsParameterMap.put(JSESSION_ID, STRING_DOUBLE_QUOTES + request.getRequestedSessionId()
                    + STRING_DOUBLE_QUOTES);
        }
    }

    /**
     * Adds the timezone offset parameter if empty.
     *
     * @param jsParameterMap
     *            the js parameter map
     */
    private void addTimezoneOffsetParameterIfEmpty(Map<String, String> jsParameterMap) {
        if (!jsParameterMap.containsKey(PARAM_TIMEZONE_OFFSET)) {
            // set timezone offset from user profile or its fallbacks
            int offset = UserManagementHelper.getEffectiveUserTimeZone().getOffset(
                    System.currentTimeMillis());
            // need minutes offset
            offset = offset / 60000;
            jsParameterMap.put(PARAM_TIMEZONE_OFFSET, String.valueOf(offset));
        }
    }

    /**
     * Gets the current user name.
     *
     * @return the user name
     */
    private String getCurrentUserName() {
        String userName = UserNameHelper.getUserSignature(SecurityHelper.assertCurrentKenmeiUser(),
                UserNameFormat.MEDIUM);
        return userName;
    }

    /**
     * Gets html client parameters from request.
     *
     * @param request
     *            the request
     * @return the parameters from request
     */
    @SuppressWarnings("unchecked")
    private Map<String, String> getParametersFromRequest(HttpServletRequest request) {
        Map<String, ? extends Object> originalParameterMap = request.getParameterMap();
        Map<String, String> jsParameterMap = new HashMap<String, String>();
        for (Entry<String, ? extends Object> parameter : originalParameterMap.entrySet()) {
            String key = parameter.getKey();
            if (key.equals(PARAM_SHOW_HEADER)) {
                continue;
            }
            String parameterAsString = ParameterHelper.getParameterAsString(originalParameterMap,
                    key);
            if (!isBoolean(parameterAsString) && !isInteger(parameterAsString)) {
                parameterAsString = STRING_DOUBLE_QUOTES
                        + StringEscapeUtils.escapeEcmaScript(parameterAsString)
                        + STRING_DOUBLE_QUOTES;
            }
            jsParameterMap.put(key, parameterAsString);
        }
        return jsParameterMap;
    }

    /**
     * Handle request.
     *
     * @param request
     *            The request.
     * @param response
     *            The response.
     * @return ModelAndView The model and view.
     * @throws Exception
     *             Exception.
     */
    @Override
    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        Map<String, String> jsParameterMap = getParametersFromRequest(request);
        // clear language parameter since language is exposed to session if lang parameter was set
        // on request
        // TODO if changing behavior of lang parameter to only be valid for current request the lang
        // parameter must be set again if it differs from that of locale of current user/browser
        jsParameterMap.remove(PARAM_LANG);
        addSessionIdIfNeeded(request, jsParameterMap);
        addTimezoneOffsetParameterIfEmpty(jsParameterMap);
        Map<String, Object> contextMap = new HashMap<String, Object>();
        contextMap.put("jsParameterMap", jsParameterMap);
        contextMap.put("userName", getCurrentUserName());
        contextMap.put("jSessionId", request.getRequestedSessionId());
        contextMap.put("jSessionIdUrlExtension", "");
        contextMap.put("showHeader", ParameterHelper.getParameterAsBoolean(
                request.getParameterMap(), PARAM_SHOW_HEADER, true));
        if (!request.isRequestedSessionIdFromCookie()) {
            contextMap.put("jSessionIdUrlExtension",
                    ";jsessionid=" + request.getRequestedSessionId());
        }
        ModelAndView modelAndView = new ModelAndView("communote.plugins.htmlClient.main",
                contextMap);
        return modelAndView;
    }

    /**
     * Checks if a string is boolean.
     *
     * @param aStringValue
     *            a string value
     * @return true, if it is boolean
     */
    private boolean isBoolean(String aStringValue) {
        String trimmedValue = aStringValue.trim();
        return trimmedValue.equals(HtmlClientController.STRING_TRUE)
                || trimmedValue.equals(HtmlClientController.STRING_FALSE);
    }

    /**
     * Checks if a string is an integer.
     *
     * @param aStringValue
     *            a string value
     * @return true, if it is an integer
     */
    private boolean isInteger(String aStringValue) {
        try {
            Integer.parseInt(aStringValue);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

}