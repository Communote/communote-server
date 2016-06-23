package com.communote.server.web.commons.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This filter can be used to timeout request manually, which provide a "bypassSession" parameter.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class BypassSessionTimeoutFilter implements Filter {

    /** Logger. */
    private final static Logger LOG = LoggerFactory.getLogger(BypassSessionTimeoutFilter.class);

    /**
     * Parameter for storing a timestamp in the session of the last request that should not bypass
     * the session
     */
    public static final String SESSION_PARAMETER = "lastInSessionRequestTime";

    /** Name of the parameter, request can use for this filter. */
    private String requestParameterName;

    /**
     * Does nothing.
     */
    @Override
    public void destroy() {
        // Do nothing
    }

    /**
     * Checks the current session.
     *
     * {@inheritDoc}
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpSession session;
        if (request instanceof HttpServletRequest
                && (session = ((HttpServletRequest) request).getSession(false)) != null) {
            if (Boolean.parseBoolean(request.getParameter(requestParameterName))
                    && session.getAttribute(SESSION_PARAMETER) != null) {
                long lastInSessionRequestTime = (Long) session.getAttribute(SESSION_PARAMETER);
                if (lastInSessionRequestTime + session.getMaxInactiveInterval() * 1000L < System
                        .currentTimeMillis()) {
                    try {
                        session.invalidate();
                    } catch (IllegalStateException e) {
                        LOG.debug(e.getMessage());
                    }
                    ((HttpServletResponse) response).sendError(401);
                    return;
                }
            } else {
                session.setAttribute(SESSION_PARAMETER, System.currentTimeMillis());
            }
        }
        chain.doFilter(request, response);
    }

    /**
     * Does nothing.
     *
     * {@inheritDoc}
     */
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        requestParameterName = filterConfig.getInitParameter("requestParameterName");
        if (StringUtils.isBlank(requestParameterName)) {
            throw new ServletException(
                    "The init parameter \"requestParameterName\" may not be blank");
        }
    }
}
