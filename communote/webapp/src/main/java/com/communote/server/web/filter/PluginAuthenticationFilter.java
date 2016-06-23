package com.communote.server.web.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import com.communote.server.api.ServiceLocator;
import com.communote.server.core.security.AuthenticationFilterManagement;

/**
 * Filter, which allows
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class PluginAuthenticationFilter implements Filter {

    private AuthenticationFilterManagement filterManagement;

    /**
     * Does nothing.
     */
    @Override
    public void destroy() {
        // Do nothing.
    }

    /**
     * Dispatches the logic to registered filters from plugins.
     * 
     * {@inheritDoc}
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        getFilterManagement().doFilter(request, response, chain);
    }

    /**
     * @return the filterManagement
     */
    public AuthenticationFilterManagement getFilterManagement() {
        if (filterManagement == null) {
            filterManagement = ServiceLocator.instance().getService(
                    AuthenticationFilterManagement.class);
        }
        return filterManagement;
    }

    /**
     * Does nothing.
     * 
     * @param filterConfig
     *            Not used.
     * @throws ServletException
     *             Exception.
     */
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Do nothing.
    }

    /**
     * @param filterManagement
     *            the filterManagement to set
     */
    public void setFilterManagement(AuthenticationFilterManagement filterManagement) {
        this.filterManagement = filterManagement;
    }

}
