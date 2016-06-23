package com.communote.server.web.commons.filter;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Filter to forward the rest api call to an internal servlet.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class CommunoteRestletForwardFilter implements Filter {

    /**
     * Attribute name for version of rest-api
     */
    public static final String ATTRIBUTE_NAME_REST_API_VERSION = "restApiVersion";

    /**
     * Internal forward url.
     */
    private static final String INTERNAL_URI_API = "/internal/rest-api/";
    private static final String INTERNAL_URI_CRAWL_API = "/internal/rest-crawl-api/";

    /**
     * Uri part for filtering.
     */
    private static final Pattern PATTERN_URI_API = Pattern
            .compile("/microblog/[\\w]+/(?:web|api)/rest/([\\w\\.-]+)/(.+)");

    private static final Pattern PATTERN_URI_CRAWL_API = Pattern
            .compile("/microblog/[\\w]+/api/rest-crawl/([\\w\\.-]+)/(.+)");

    /**
     * {@inheritDoc}
     *
     * @see javax.servlet.Filter#destroy()
     */
    @Override
    public void destroy() {
        // Do nothing.
    }

    /**
     * {@inheritDoc}
     *
     * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest,
     *      javax.servlet.ServletResponse, javax.servlet.FilterChain)
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String uri = httpRequest.getRequestURI();
        String apiUri = null;
        Matcher matcher = PATTERN_URI_API.matcher(uri);
        if (!matcher.find()) {
            matcher = PATTERN_URI_CRAWL_API.matcher(uri);
            if (matcher.find()) {
                apiUri = INTERNAL_URI_CRAWL_API;
            }
        } else {
            apiUri = INTERNAL_URI_API;
        }
        if (apiUri != null) {
            // extract version and resource from URI
            String version = matcher.group(1);
            String resource = matcher.group(2);
            String forward = httpResponse.encodeURL(apiUri + resource);
            httpRequest.setAttribute(ATTRIBUTE_NAME_REST_API_VERSION, version);
            httpRequest.getRequestDispatcher(forward).forward(httpRequest, response);
        } else {
            filterChain.doFilter(request, response);
        }

    }

    /**
     * {@inheritDoc}
     *
     * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
     */
    @Override
    public void init(FilterConfig config) throws ServletException {
        // Do nothing.
    }
}
