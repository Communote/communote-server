package com.communote.server.web.external.spring.security;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;

import com.communote.common.util.UriUtils;
import com.communote.server.core.common.session.SessionHandler;
import com.communote.server.persistence.user.client.ClientUrlHelper;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class CommunoteAuthenticationProcessingFilterEntryPoint extends
        LoginUrlAuthenticationEntryPoint {
    private String targetUrlParameter;

    /**
     * Constructor.
     *
     * @param loginFormUrl
     *            Url to the login form.
     */
    public CommunoteAuthenticationProcessingFilterEntryPoint(String loginFormUrl) {
        super(loginFormUrl);
    }

    /**
     * {@inheritDoc} Send an SC_UNATHORIZED Error if the request has been send by AJAX
     */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException authException) throws IOException, ServletException {
        HttpServletRequest httpRequest = request;
        HttpServletResponse httpResponse = response;

        if (isAjaxRequest(httpRequest)) {
            // if its an ajax request do not forward to entry point, send 401 and remove saved
            // request for further processing
            httpRequest.getSession().removeAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
            httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED);

            SessionHandler.instance().resetOverriddenCurrentUserLocale(httpRequest);
        } else {
            super.commence(request, response, authException);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String determineUrlToUseForThisRequest(HttpServletRequest request,
            HttpServletResponse response, AuthenticationException exception) {
        String moduleClientPart = ClientUrlHelper.prependModuleClientPart(null);
        StringBuilder url = new StringBuilder(moduleClientPart);
        ClientUrlHelper.appendUrlPath(url, getLoginFormUrl());
        if (targetUrlParameter != null) {
            // save request URL with parameters as URL parameter for the redirect URL
            StringBuilder originalUrl = null;
            String path = request.getServletPath();
            String pathInfo = request.getPathInfo();
            if (pathInfo != null) {
                path += pathInfo;
            }
            if (path.startsWith(moduleClientPart)) {
                originalUrl = new StringBuilder(path.substring(moduleClientPart.length()));
            }
            if (originalUrl == null || originalUrl.length() == 0) {
                // use absolute URL if pathinfo is missing
                originalUrl = new StringBuilder(request.getRequestURL().toString());
            }
            if (request.getQueryString() != null) {
                originalUrl.append('?');
                originalUrl.append(request.getQueryString());
            }
            String targetUrl = UriUtils.encodeUriComponent(originalUrl.toString());
            if (url.toString().indexOf('?') < 0) {
                url.append("?");
            } else {
                url.append("&");
            }
            url.append(targetUrlParameter).append("=").append(targetUrl);
        }
        return url.toString();
    }

    /**
     * @param request
     *            the request
     * @return true if request is done by a AJAX call
     */
    private boolean isAjaxRequest(HttpServletRequest request) {
        return StringUtils.equals(request.getHeader("X-Requested-With"), "XMLHttpRequest");
    }

    /**
     * Set the parameter to append which should hold the URL of the current request.
     *
     * @param targetUrlParameter
     *            name of the parameter to append. The parameter name is expected to consist only of
     *            ASCII alpha-numeric characters.
     */
    public void setTargetUrlParameter(String targetUrlParameter) {
        this.targetUrlParameter = targetUrlParameter;
    }

}
