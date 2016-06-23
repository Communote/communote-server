package com.communote.server.web.external.spring.security;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.application.Runtime;

/**
 * Secures the application by hiding the installer component as soon as the installation completed
 * successfully.
 *
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class HideInstallerAfterInstallationFilter implements Filter {

    /**
     * {@inheritDoc}
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
        HttpServletResponse servletResponse = (HttpServletResponse) response;
        // if application is installed and initialized send a not found
        Runtime runtime = CommunoteRuntime.getInstance();
        if (runtime.getConfigurationManager().getStartupProperties().isInstallationDone()
                && runtime.isInitialized()) {
            servletResponse.sendError(HttpServletResponse.SC_NOT_FOUND);
        } else {
            chain.doFilter(request, response);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(FilterConfig arg0) throws ServletException {
        // Do nothing.
    }
}
