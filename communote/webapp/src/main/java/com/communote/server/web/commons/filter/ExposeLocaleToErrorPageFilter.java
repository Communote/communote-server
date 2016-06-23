package com.communote.server.web.commons.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import com.communote.server.core.common.session.SessionHandler;

/**
 * Filter that exposes the current locale, which is retrieved from the SessionHandler, as a request
 * attribute so that it can be uses by the error page, where there is no access to the
 * SecurityContext (and thus to the locale authenticated user). The filter should be included
 * directly after the security filter (chain).
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ExposeLocaleToErrorPageFilter implements Filter {

    public static final String CURRENT_LOCALE = "localeOfCurrentRequest";

    @Override
    public void destroy() {
        // nothing
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        try {
            chain.doFilter(request, response);
        } finally {
            // take locale after application logic completed because there the locale might have
            // been modified
            request.setAttribute(CURRENT_LOCALE,
                    SessionHandler.instance().getCurrentLocale((HttpServletRequest) request));
        }

    }

    @Override
    public void init(FilterConfig arg0) throws ServletException {
        // nothing
    }

}
