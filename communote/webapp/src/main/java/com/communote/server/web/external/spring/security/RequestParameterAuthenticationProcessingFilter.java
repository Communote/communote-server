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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.Assert;

import com.communote.server.api.ServiceLocator;
import com.communote.server.core.security.AuthenticationHelper;
import com.communote.server.core.security.AuthenticationManagement;
import com.communote.server.external.acegi.UserAccountPermanentlyLockedException;
import com.communote.server.external.acegi.UserAccountTemporarilyLockedException;

/**
 * Filter to authenticate the user based on request parameter. The names of the parameters may be
 * configured. By default it is &__username=USERNAME&__password=PASSWORD
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
// TODO at least require POST?
public class RequestParameterAuthenticationProcessingFilter implements Filter, InitializingBean {

    private static final Logger LOG = LoggerFactory
            .getLogger(RequestParameterAuthenticationProcessingFilter.class);

    private final static String DEFAULT_PARAM_USERNAME = "__username";

    private final static String DEFAULT_PARAM_PASSWORD = "__password";

    private String paramUsername = DEFAULT_PARAM_USERNAME;
    private String paramPassword = DEFAULT_PARAM_PASSWORD;

    private AuthenticationManager authenticationManager;

    private AuthenticationDetailsSource<Object, Object> authenticationDetailsSource = new AuthenticationDetailsSourceImpl();

    private boolean continueChainOnAuthenticationFailure;

    /**
     * {@inheritDoc}
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(this.authenticationManager, "An AuthenticationManager is required");
    }

    /**
     * Tries to authenticate a user whose credentials were contained as request parameters
     *
     * @param authRequest
     *            the data to be used for authentication
     * @throws AuthenticationException
     *             when the authentication has failed
     */
    private void authenticate(UsernamePasswordAuthenticationToken authRequest)
            throws AuthenticationException {

        Authentication authResult = authenticationManager.authenticate(authRequest);

        ServiceLocator.findService(AuthenticationManagement.class).onSuccessfulAuthentication(
                authResult);

    }

    /**
     * Check if an authentication is required, that is that no authentication already exists
     *
     * @param username
     *            the username
     * @return true if authenticationis required
     */
    private boolean authenticationIsRequired(String username) {
        // Only reauthenticate if username doesn't match SecurityContextHolder and user isn't
        // authenticated)
        Authentication existingAuth = SecurityContextHolder.getContext().getAuthentication();

        boolean isRequired = existingAuth == null || !existingAuth.isAuthenticated();

        // Limit username comparison to providers which use usernames (ie
        // UsernamePasswordAuthenticationToken)
        if (!isRequired) {
            isRequired = !(existingAuth instanceof UsernamePasswordAuthenticationToken)
                    || !existingAuth.getName().equals(username);
        }
        return isRequired;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void destroy() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        // get the username
        String username = request.getParameter(paramUsername);

        // get the password
        String password = request.getParameter(paramPassword);

        // should chain go on
        boolean chainContinue = true;

        // authenticate if password and username are set and authentication is required
        if (StringUtils.isNotBlank(username) && StringUtils.isNotBlank(password)) {
            if (authenticationIsRequired(username)) {

                // authenticate
                UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(
                        username, password);
                authRequest.setDetails(authenticationDetailsSource.buildDetails(request));

                try {
                    authenticate(authRequest);

                } catch (AuthenticationException failed) {
                    // Authentication failed
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Authentication request for user: " + username + " failed: "
                                + failed.toString());
                    }
                    chainContinue = continueChainOnAuthenticationFailure;
                    // on failure reset
                    AuthenticationHelper.removeAuthentication();

                    if (!chainContinue) {
                        if (failed instanceof UserAccountTemporarilyLockedException
                                || failed instanceof UserAccountPermanentlyLockedException) {
                            ((HttpServletResponse) response).sendError(
                                    HttpServletResponse.SC_FORBIDDEN, failed.getMessage());
                        } else {
                            ((HttpServletResponse) response)
                                    .setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        }
                    }
                }
            }
        }

        if (chainContinue) {
            chain.doFilter(request, response);
        }
    }

    /**
     * @return the authentication manager
     */
    public AuthenticationManager getAuthenticationManager() {
        return authenticationManager;
    }

    /**
     * @return name of parameter for the password
     */
    public String getParamPassword() {
        return paramPassword;
    }

    /**
     * @return name of parameter for the user name
     */
    public String getParamUsername() {
        return paramUsername;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(FilterConfig config) throws ServletException {
    }

    /**
     *
     * @return continue chaing on authenticationf failure
     */
    public boolean isContinueChainOnAuthenticationFailure() {
        return continueChainOnAuthenticationFailure;
    }

    /**
     * @param authenticationDetailsSource
     *            the {@link AuthenticationDetailsSource} to use, cannot be null
     */
    public void setAuthenticationDetailsSource(
            AuthenticationDetailsSource<Object, Object> authenticationDetailsSource) {
        Assert.notNull(authenticationDetailsSource, "AuthenticationDetailsSource required");
        this.authenticationDetailsSource = authenticationDetailsSource;
    }

    /**
     * @param authenticationManager
     *            the {@link AuthenticationManager} manager to use, cannot be null
     */
    public void setAuthenticationManager(AuthenticationManager authenticationManager) {
        Assert.notNull(authenticationManager, "AuthenticationManager required");
        this.authenticationManager = authenticationManager;
    }

    /**
     *
     * @param continueChainOnAuthenticationFailure
     *            set continue chain on authentication failure
     */
    public void setContinueChainOnAuthenticationFailure(boolean continueChainOnAuthenticationFailure) {
        this.continueChainOnAuthenticationFailure = continueChainOnAuthenticationFailure;
    }

    /**
     * @param paramPassword
     *            the name of parameter for the password
     */
    public void setParamPassword(String paramPassword) {
        Assert.notNull(authenticationManager, "ParamPassword required");
        this.paramPassword = paramPassword;
    }

    /**
     * @param paramUsername
     *            the name of parameter for the user name
     */
    public void setParamUsername(String paramUsername) {
        Assert.notNull(authenticationManager, "ParamUsername required");
        this.paramUsername = paramUsername;
    }
}
