package com.communote.plugins.htmlclient;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;

import com.communote.server.core.osgi.OSGiHelper;

/**
 * The Class ControllerUtils.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ControllerUtils {

    /** The Constant CONTEXT_ATTRIBUTE_REFERER_PATH. */
    private static final String CONTEXT_ATTRIBUTE_REFERER_PATH = "refererPath";

    private static final String STATIC_RESOURCES_BASE_PATH = "staticResourcesBasePath";

    /** The Constant ENCODING_UTF_8. */
    private static final String ENCODING_UTF_8 = "UTF-8";

    /** The Constant URL_PARAMETER_BASE_PATH. */
    private static final String URL_PARAMETER_BASE_PATH = "baseUrl";

    /** The Constant CACHE_CONTROL. */
    private static final String CACHE_CONTROL = "Cache-Control";

    /** The Constant EXPIRES. */
    private static final String EXPIRES = "Expires";

    /** The Constant LAST_MODIFIED. */
    private static final String LAST_MODIFIED = "Last-Modified";

    /** The Constant DAY_IN_SECONDS. */
    private static final int DAY_IN_SECONDS = 60 * 60 * 24;

    /** The Constant CACHE_FOR_DAYS. */
    private static final int CACHE_FOR_DAYS = 7;

    /** The Constant MAX_AGE. */
    private static final int MAX_AGE = DAY_IN_SECONDS * CACHE_FOR_DAYS;

    /** The Constant CACHE_CONTROL_VALUE. */
    private static final String CACHE_CONTROL_VALUE = "max-age=" + MAX_AGE + ", private";

    /**
     * Adds the referer path to context map.
     *
     * @param request
     *            the request
     * @param data
     *            the data
     * @throws UnsupportedEncodingException
     *             the unsupported encoding exception
     */
    public static void addRefererPathToContext(HttpServletRequest request, Map<String, Object> data)
            throws UnsupportedEncodingException {
        String referer = request.getParameter(URL_PARAMETER_BASE_PATH);
        if (StringUtils.isNotBlank(referer)) {
            data.put(CONTEXT_ATTRIBUTE_REFERER_PATH, URLDecoder.decode(referer, ENCODING_UTF_8));
        }
    }

    /**
     * Adds the static caching header.
     *
     * @param response
     *            the response
     */
    public static void addStaticCachingHeader(HttpServletResponse response) {
        long now = System.currentTimeMillis();
        response.addDateHeader(LAST_MODIFIED, now);
        response.addDateHeader(EXPIRES, now + DAY_IN_SECONDS * 1000);
        response.setHeader(CACHE_CONTROL, CACHE_CONTROL_VALUE);
    }

    public static void addStaticResourcesBasePath(String bundleName, Map<String, Object> data) {
        data.put(STATIC_RESOURCES_BASE_PATH,
                OSGiHelper.getRelativeBundleResourceLocation(bundleName, "static"));
    }

}
