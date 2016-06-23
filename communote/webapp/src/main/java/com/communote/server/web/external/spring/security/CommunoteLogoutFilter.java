package com.communote.server.web.external.spring.security;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;

import com.communote.server.core.common.session.SessionHandler;
import com.communote.server.web.commons.helper.ControllerHelper;

/**
 * Handles logout and takes care of clientId in target URL
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class CommunoteLogoutFilter extends LogoutFilter {
    /**
     * Constructor.
     *
     * @param logoutSuccessHandler
     *            The logout success handler.
     * @param handlers
     *            The handlers.
     */
    public CommunoteLogoutFilter(LogoutSuccessHandler logoutSuccessHandler, LogoutHandler[] handlers) {
        super(logoutSuccessHandler, handlers);
    }

    /**
     * Instantiates a new logout filter.
     *
     * @param logoutSuccessUrl
     *            the logout success url
     * @param handlers
     *            the handlers
     */
    public CommunoteLogoutFilter(final String logoutSuccessUrl, LogoutHandler[] handlers) {
        super(new SimpleUrlLogoutSuccessHandler() {
            {
                setDefaultTargetUrl(logoutSuccessUrl);
                setRedirectStrategy(new DefaultRedirectStrategy() {
                    @Override
                    public void sendRedirect(HttpServletRequest request,
                            HttpServletResponse response, String url) throws java.io.IOException {
                        if (url.startsWith("http://") || url.startsWith("https://")) {
                            throw new IllegalArgumentException(
                                    "could not add client id to this uri: '" + url + "'");
                        }
                        // reset session values
                        SessionHandler.instance().setFirstRequestedWasSecure(request, null);
                        SessionHandler.instance().resetOverriddenCurrentUserLocale(request);
                        ControllerHelper.sendInternalRedirect(request, response, url);
                    };
                });
            }
        }, handlers);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean requiresLogout(HttpServletRequest request, HttpServletResponse response) {
        String uri = request.getRequestURI();
        int pathParamIndex = uri.indexOf(';');

        if (pathParamIndex > 0) {
            // strip everything from the first semi-colon
            uri = uri.substring(0, pathParamIndex);
        }

        int queryParamIndex = uri.indexOf('?');

        if (queryParamIndex > 0) {
            // strip everything from the first question mark
            uri = uri.substring(0, queryParamIndex);
        }

        if ("".equals(request.getContextPath())) {
            return uri.endsWith(getFilterProcessesUrl());
        }

        return uri.matches(request.getContextPath() + "/[^/]*?/[^/]*?" + getFilterProcessesUrl());
    }

}
