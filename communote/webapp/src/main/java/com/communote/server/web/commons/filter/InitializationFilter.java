package com.communote.server.web.commons.filter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

import com.communote.server.api.core.application.CommunoteRuntime;

/**
 * This filter redirects to a default page, when the application is not initialized.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class InitializationFilter implements Filter {

    private static String NOT_INITIALIZED_PAGE = "starting";

    private static final List<String> INSTALLER_MIMETYPE_PATHS = Arrays.asList("styles/",
            "images/", "javascript/", "installer/", "portal/initializationStatus.json");

    /**
     * Does nothing.
     */
    @Override
    public void destroy() {
        // Do nothing.
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (CommunoteRuntime.getInstance().isInitialized()) {
            chain.doFilter(request, response);
        } else {
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            String forward = httpRequest.getServletPath();
            String pathInfo = httpRequest.getPathInfo();
            if (pathInfo != null) {
                forward += pathInfo;
            }
            boolean isIgnore = false;
            for (String path : INSTALLER_MIMETYPE_PATHS) {
                if (StringUtils.containsIgnoreCase(forward, path)) {
                    isIgnore = true;
                    break;
                }
            }
            if (!isIgnore) {
                forward = httpResponse.encodeURL(NOT_INITIALIZED_PAGE);
            }
            request.getRequestDispatcher(forward).forward(request, response);
        }
    }

    /**
     * Does nothing.
     *
     * {@inheritDoc}
     */
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Do nothing.
    }
}
