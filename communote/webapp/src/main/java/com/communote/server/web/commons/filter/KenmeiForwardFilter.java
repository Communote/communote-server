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

import com.communote.server.persistence.user.client.ClientUrlHelper;

/**
 * The Class KenmeiForwardFilter forwards a requests to the url without module and client id. It
 * does NOT continue the filter chain.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
// TODO still needed? What is/was it used for?
public class KenmeiForwardFilter implements Filter {
    /**
     * Name of the parameter for a static resource
     */
    public static final String PARAMETER_NAME_STATIC_RESOURCE = "staticResource";

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

        String staticResource = request.getParameter(PARAMETER_NAME_STATIC_RESOURCE);
        if (staticResource != null && "true".equals(staticResource)) {

            HttpServletRequest httpRequest = (HttpServletRequest) request;
            HttpServletResponse httpResponse = (HttpServletResponse) response;

            String uri = httpRequest.getRequestURI();

            // remove id's
            String forward = httpResponse.encodeURL(ClientUrlHelper.removeIds(uri));
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
