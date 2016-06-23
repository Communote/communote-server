package com.communote.server.web.external.spring.security;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.event.InteractiveAuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.NullRememberMeServices;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.rememberme.RememberMeAuthenticationFilter;
import org.springframework.util.Assert;

import com.communote.server.api.ServiceLocator;
import com.communote.server.core.security.AuthenticationManagement;

/**
 * {@inheritDoc}
 * <p>
 * Small change of the original code of RememberMeProcessingFilter, because the current user must be
 * set before doFilter is called and after the last test, if the user is allowed to login.
 * </p>
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class CommunoteRememberMeProcessingFilter extends RememberMeAuthenticationFilter {

    private static final Logger LOG = LoggerFactory
            .getLogger(CommunoteRememberMeProcessingFilter.class);
    private AuthenticationManager authenticationManager;
    private ApplicationEventPublisher eventPublisher;
    private RememberMeServices rememberMeServices = new NullRememberMeServices();

    /**
     * {@inheritDoc}
     */
    @Override
    public void afterPropertiesSet() {
        Assert.notNull(authenticationManager, "authenticationManager must be specified");
        Assert.notNull(this.rememberMeServices);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (!(request instanceof HttpServletRequest)) {
            throw new ServletException("Can only process HttpServletRequest");
        }

        if (!(response instanceof HttpServletResponse)) {
            throw new ServletException("Can only process HttpServletResponse");
        }

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            Authentication rememberMeAuth = rememberMeServices.autoLogin(httpRequest, httpResponse);

            if (rememberMeAuth != null) {
                // Attempt authenticaton via AuthenticationManager
                try {
                    rememberMeAuth = authenticationManager.authenticate(rememberMeAuth);

                    ServiceLocator.findService(AuthenticationManagement.class)
                    .onSuccessfulAuthentication(rememberMeAuth);

                    if (LOG.isDebugEnabled()) {
                        LOG.debug("SecurityContextHolder populated with remember-me token: '"
                                + SecurityContextHolder.getContext().getAuthentication() + "'");
                    }

                    // Fire event
                    if (this.eventPublisher != null) {
                        eventPublisher.publishEvent(new InteractiveAuthenticationSuccessEvent(
                                SecurityContextHolder.getContext().getAuthentication(), this
                                .getClass()));
                    }
                } catch (AuthenticationException authenticationException) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("SecurityContextHolder not populated with remember-me token, as "
                                + "AuthenticationManager rejected Authentication "
                                + "returned by RememberMeServices: '" + rememberMeAuth
                                + "'; invalidating remember-me token", authenticationException);
                    }

                    rememberMeServices.loginFail(httpRequest, httpResponse);
                }

            }

            chain.doFilter(request, response);
        } else {
            if (LOG.isTraceEnabled()) {
                LOG.trace("SecurityContextHolder not populated with remember-me token, as it already contained: '"
                        + SecurityContextHolder.getContext().getAuthentication() + "'");
            }

            chain.doFilter(request, response);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher publisher) {
        this.eventPublisher = publisher;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setAuthenticationManager(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setRememberMeServices(RememberMeServices rememberMeServices) {
        this.rememberMeServices = rememberMeServices;
    }
}
