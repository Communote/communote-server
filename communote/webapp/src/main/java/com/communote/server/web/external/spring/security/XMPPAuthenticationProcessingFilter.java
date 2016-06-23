package com.communote.server.web.external.spring.security;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.security.authentication.AuthenticationDetailsSource;
import org.springframework.security.authentication.AuthenticationDetailsSourceImpl;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.util.Assert;

import com.communote.server.api.ServiceLocator;
import com.communote.server.core.messaging.NotificationManagement;
import com.communote.server.external.acegi.UserAccountPermanentlyLockedException;
import com.communote.server.external.acegi.UserAccountTemporarilyLockedException;

/**
 * This filter authenticates an XMPP user.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class XMPPAuthenticationProcessingFilter implements Filter, InitializingBean {
    /** Logger. */
    private static final Logger LOG = LoggerFactory
            .getLogger(XMPPAuthenticationProcessingFilter.class);
    /** Default username parameter. */
    private static final String DEFAULT_PARAM_USERNAME = "username";

    /** Default password parameter. */
    private static final String DEFAULT_PARAM_PASSWORD = "password";

    private String paramUsername = DEFAULT_PARAM_USERNAME;
    private String paramPassword = DEFAULT_PARAM_PASSWORD;

    /** authentication manager. */
    private AuthenticationManager authenticationManager;
    /** authentication details source. */
    private AuthenticationDetailsSource<Object, Object> authenticationDetailsSource = new AuthenticationDetailsSourceImpl();

    /**
     * {@inheritDoc}
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(this.authenticationManager, "An AuthenticationManager is required");
    }

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
        String xmppUsername = request.getParameter(paramUsername);
        String password = request.getParameter(paramPassword);
        String username = ServiceLocator.findService(NotificationManagement.class).getUserAlias(
                xmppUsername, "xmpp");

        if (StringUtils.isBlank(username) || StringUtils.isBlank(password)) {
            ((HttpServletResponse) response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(
                username, password);
        authRequest.setDetails(authenticationDetailsSource.buildDetails(request));
        try {
            Authentication authResult = authenticationManager.authenticate(authRequest);
            LOG.debug("Authentication success: " + authResult.toString());
        } catch (AuthenticationException failed) {
            LOG.debug("Authentication request for user: " + username + " failed: "
                    + failed.toString());
            if (response instanceof HttpServletResponse) {

                if (failed instanceof UserAccountTemporarilyLockedException
                        || failed instanceof UserAccountPermanentlyLockedException) {
                    ((HttpServletResponse) response).sendError(HttpServletResponse.SC_FORBIDDEN,
                            failed.getMessage());
                } else {
                    ((HttpServletResponse) response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                }
            }
        }
    }

    /**
     * Returns the {@link AuthenticationManager}.
     *
     * @return {@link AuthenticationManager}.
     */
    public final AuthenticationManager getAuthenticationManager() {
        return authenticationManager;
    }

    /**
     * Returns the password parameter name.
     *
     * @return password parameter name.
     */
    public final String getParamPassword() {
        return paramPassword;
    }

    /**
     * Returns the user name parameter name.
     *
     * @return user name parameter name.
     */
    public final String getParamUsername() {
        return paramUsername;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(FilterConfig config) throws ServletException {
        // Do nothing.
    }

    /**
     * Sets the authentication details source.
     *
     * @param authenticationDetailsSource
     *            the auth details source.
     */
    public final void setAuthenticationDetailsSource(
            AuthenticationDetailsSource<Object, Object> authenticationDetailsSource) {
        Assert.notNull(authenticationDetailsSource, "AuthenticationDetailsSource required");
        this.authenticationDetailsSource = authenticationDetailsSource;
    }

    /**
     * Sets the authentication manager.
     *
     * @param authenticationManager
     *            the authentication manager.
     */
    public final void setAuthenticationManager(AuthenticationManager authenticationManager) {
        Assert.notNull(authenticationManager, "AuthenticationManager required");
        this.authenticationManager = authenticationManager;
    }

    /**
     * Sets the password parameter name.
     *
     * @param paramPassword
     *            the password
     */
    public final void setParamPassword(String paramPassword) {
        Assert.notNull(authenticationManager, "ParamPassword required");
        this.paramPassword = paramPassword;
    }

    /**
     * Sets the user name parameter name..
     *
     * @param paramUsername
     *            the username
     */
    public final void setParamUsername(String paramUsername) {
        Assert.notNull(authenticationManager, "ParamUsername required");
        this.paramUsername = paramUsername;
    }
}
