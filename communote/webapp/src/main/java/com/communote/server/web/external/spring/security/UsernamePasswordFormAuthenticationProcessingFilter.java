package com.communote.server.web.external.spring.security;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.event.InteractiveAuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.communote.server.api.ServiceLocator;
import com.communote.server.core.security.AuthenticationManagement;
import com.communote.server.core.security.UserDetails;

/**
 * Overrides the success method on {@link AuthenticationProcessingFilter} to do some additional
 * action.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class UsernamePasswordFormAuthenticationProcessingFilter extends
UsernamePasswordAuthenticationFilter {

    private boolean postOnly;

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request,
            HttpServletResponse response) throws AuthenticationException {
        if (postOnly && !request.getMethod().equals("POST")) {
            throw new AuthenticationServiceException("Authentication method not supported: "
                    + request.getMethod());
        }
        String username = obtainUsername(request);
        String password = obtainPassword(request);

        if (StringUtils.isBlank(username)) {
            throw new BadCredentialsException("A blank username is not allowed.");
        }
        if (password == null) {
            password = StringUtils.EMPTY;
        }

        UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(
                username, password);

        // Allow subclasses to set the "details" property
        setDetails(request, authRequest);

        return getAuthResult(authRequest);
    }

    /**
     * get authentication result
     *
     * @param authRequest
     *            current authentication request
     * @return authentication object
     */
    private Authentication getAuthResult(UsernamePasswordAuthenticationToken authRequest) {
        Authentication authResult = null;

        AuthenticationManager manager = getAuthenticationManager();
        authResult = manager.authenticate(authRequest);

        if (authResult != null) {
            if (!(authResult.getPrincipal() instanceof UserDetails)) {
                throw new AuthenticationServiceException(
                        "Principal in authentication result has wrong type.");
            }
        }
        return authResult;
    }

    @Override
    protected boolean requiresAuthentication(HttpServletRequest request,
            HttpServletResponse response) {
        String uri = request.getRequestURI();

        int pathParamIndex = uri.indexOf(';');

        if (pathParamIndex > 0) {
            // strip everything after the first semicolon
            uri = uri.substring(0, pathParamIndex);
        }

        if (StringUtils.isNotEmpty(request.getContextPath())) {
            return uri.endsWith(getFilterProcessesUrl());
        }
        String matchIdsInUri = "/[^/]*?/[^/]*?";

        return uri.matches(request.getContextPath() + matchIdsInUri + getFilterProcessesUrl());
    }

    @Override
    public void setPostOnly(boolean postOnly) {
        this.postOnly = postOnly;
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request,
            HttpServletResponse response, Authentication authResult) throws IOException,
            ServletException {

        ServiceLocator.findService(AuthenticationManagement.class).onSuccessfulAuthentication(
                authResult);

        getRememberMeServices().loginSuccess(request, response, authResult);

        // Fire event
        if (this.eventPublisher != null) {
            eventPublisher.publishEvent(new InteractiveAuthenticationSuccessEvent(authResult, this
                    .getClass()));
        }
        getSuccessHandler().onAuthenticationSuccess(request, response, authResult);
    }
}
