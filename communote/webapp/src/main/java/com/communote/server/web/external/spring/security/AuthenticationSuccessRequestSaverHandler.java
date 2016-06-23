package com.communote.server.web.external.spring.security;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

/**
 * A {@link AuthenticationSuccessHandler} that just stores an attribute in the request
 * {@link #REQUEST_ATTR_SUCCESS_AUTHENTICATION} that can be reused later.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class AuthenticationSuccessRequestSaverHandler implements AuthenticationSuccessHandler {

    /**
     * Boolean Request Attribute saying if there is a authentication that has been just succeeded
     */
    public final static String REQUEST_ATTR_HAS_SUCCESS_AUTHENTICATION =
            "com.communote.core.authentication.success.exists";
    /**
     * Request attribution holding the {@link Authentication}
     */
    public final static String REQUEST_ATTR_SUCCESS_AUTHENTICATION =
            "com.communote.core.authentication.success";

    /**
     * 
     * @param request
     *            the request to check
     * @return true if there has been a successful authentication on this request
     */
    public static Authentication getSuccessAuthentication(HttpServletRequest request) {
        return (Authentication) request.getAttribute(REQUEST_ATTR_SUCCESS_AUTHENTICATION);
    }

    /**
     * 
     * @param request
     *            the request to check
     * @return true if there has been a successful authentication on this request
     */
    public static boolean hasRequestSuccessAuthentication(HttpServletRequest request) {
        return Boolean.TRUE.equals(request.getAttribute(REQUEST_ATTR_HAS_SUCCESS_AUTHENTICATION));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {
        request.setAttribute(REQUEST_ATTR_HAS_SUCCESS_AUTHENTICATION, Boolean.TRUE);
        request.setAttribute(REQUEST_ATTR_SUCCESS_AUTHENTICATION, authentication);

    }

}
