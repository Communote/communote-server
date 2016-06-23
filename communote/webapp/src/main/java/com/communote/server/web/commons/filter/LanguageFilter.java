package com.communote.server.web.commons.filter;

import java.io.IOException;
import java.util.Locale;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.i18n.LocaleContext;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.util.StringUtils;

import com.communote.server.api.ServiceLocator;
import com.communote.server.core.common.session.SessionHandler;
import com.communote.server.core.user.MasterDataManagement;

/**
 * Filter that extracts a locale out of a request parameter and uses the SessionHandler to override
 * the locale for the duration of the current session. Moreover a LocaleContext is exposed to the
 * LocaleContextHolder which uses the SessionHandler to get the current locale. This filter should
 * be included before the authentication filter (chain) so that the overridden locale and
 * LocaleContext is already available during login and registration.
 * 
 * @see LocaleContextHolder
 * @see SessionHandler
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class LanguageFilter implements Filter {

    /**
     * LocaleContext which uses the SessionHandler to get the current locale.
     * 
     * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
     */
    private class SessionHandlerAwareLocaleContext implements LocaleContext {

        private final HttpServletRequest request;

        /**
         * Create a LocaleContext with reference to the current request
         * 
         * @param request
         *            the current request
         */
        SessionHandlerAwareLocaleContext(HttpServletRequest request) {
            this.request = request;
        }

        @Override
        public Locale getLocale() {
            return SessionHandler.instance().getCurrentLocale(request);
        }

    }

    private static final Logger LOGGER = LoggerFactory.getLogger(LanguageFilter.class);

    private String paramName = "lang";

    private MasterDataManagement masterDataManagement;

    /**
     * {@inheritDoc}
     */
    @Override
    public void destroy() {
    }

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;

        Locale newLocale = extractLocale(request);
        if (newLocale != null) {
            if (getMasterDataManagement().isAvailableLanguage(newLocale)) {
                SessionHandler.instance().overrideCurrentUserLocale(request, newLocale);
            } else {
                LOGGER.debug("Locale/Language parameter was given in request but is not an "
                        + "registered language! locale={}", newLocale);
            }
        }

        LocaleContext oldLocaleContext = LocaleContextHolder.getLocaleContext();
        // expose a locale context which can access the SessionHandler so that the current locale
        // can be retrieved even at places where the current request is not available
        LocaleContextHolder.setLocaleContext(new SessionHandlerAwareLocaleContext(request));
        try {
            chain.doFilter(req, resp);
        } finally {
            LocaleContextHolder.setLocaleContext(oldLocaleContext);
        }

        // set the session locale for error pages (there no user is available)
        // request.setAttribute("overwrittenLocale",
        // SessionHandler.instance().getCurrentUserLocale(request));
    }

    /**
     * Extract the locale from a request parameter.
     * 
     * @param request
     *            the current request
     * @return the locale or null if the parameter was not set or did not contain a valid locale
     *         string
     */
    private Locale extractLocale(HttpServletRequest request) {
        // check the parameter name
        String newLocaleStr = request.getParameter(this.paramName);
        if (newLocaleStr != null) {
            newLocaleStr = newLocaleStr.replace('-', '_');
            // extract the local of the parameter value
            Locale newLocale = null;
            try {
                newLocale = StringUtils.parseLocaleString(newLocaleStr);
            } catch (IllegalArgumentException iae) {
                LOGGER.debug("Was not able to parse locale from request parameter " + newLocaleStr
                        + ". Will ignore it.", iae);
            }
            return newLocale;
        }
        return null;
    }

    /**
     * 
     * @return the master management
     */
    private MasterDataManagement getMasterDataManagement() {
        if (this.masterDataManagement == null) {
            this.masterDataManagement = ServiceLocator.findService(MasterDataManagement.class);
        }
        return masterDataManagement;
    }

    /**
     * {@inheritDoc}
     * 
     * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
     */
    @Override
    public void init(FilterConfig config) throws ServletException {
        String paramName = config.getInitParameter("paramName");
        if (paramName != null && paramName.trim().length() > 0) {
            this.paramName = paramName;
        }
    }

}
