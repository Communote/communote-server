package com.communote.server.web.external.spring.security;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.Assert;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.util.JsonHelper;
import com.communote.server.core.common.session.SessionHandler;
import com.communote.server.core.security.AuthenticationHelper;
import com.communote.server.core.security.AuthenticationManagement;
import com.communote.server.core.security.TermsOfUseNotAcceptedException;
import com.communote.server.core.security.authentication.AbstractCommunoteAuthenticationToken;
import com.communote.server.core.security.authentication.TokenFactory;
import com.communote.server.core.user.EmailAlreadyExistsException;
import com.communote.server.core.user.UserManagementHelper;
import com.communote.server.external.acegi.UserAccountPermanentlyLockedException;
import com.communote.server.external.acegi.UserAccountTemporarilyLockedException;
import com.communote.server.web.commons.MessageHelper;
import com.communote.server.web.commons.filter.PublicAccessProcessingFilter;
import com.communote.server.web.commons.helper.JsonRequestHelper;

/**
 * Authentication processing filter for the Communote token authentication method. This filter
 * checks if there is a token set as parameter or header field and uses the TokenFactory to create
 * an authentication object from it. The name of the parameter or header field is provided by
 * {@link TokenFactory#getTokenName()}. If the token is not set or blank this filter will do
 * nothing.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class CommunoteTokenAuthenticationProcessingFilter implements Filter, InitializingBean {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(CommunoteTokenAuthenticationProcessingFilter.class);

    private AuthenticationManager authenticationManager;
    private TokenFactory<?> tokenFactory;
    private boolean tokenFromRequestParameter = false;

    /**
     * {@inheritDoc}
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(this.authenticationManager, "An AuthenticationManager is required");
        Assert.notNull(this.tokenFactory, "A token factory is required.");
    }

    /**
     * Creates a descriptive error message for the authentication exception.
     *
     * @param request
     *            the request
     * @param e
     *            the exception
     * @return the error message
     */
    protected String createErrorMessage(HttpServletRequest request, AuthenticationException e) {
        String message;
        if (e instanceof UserAccountPermanentlyLockedException) {
            message = MessageHelper.getText(request, "login.external.error.userPermLocked");
        } else if (e instanceof UserAccountTemporarilyLockedException) {
            message = MessageHelper.getText(request, "login.external.error.userTempLocked",
                    new String[] { ((UserAccountTemporarilyLockedException) e)
                    .getFormattedLockedTimeout(
                                    SessionHandler.instance().getCurrentLocale(request),
                                    UserManagementHelper.getEffectiveUserTimeZone((Long) null)) });
        } else if (e instanceof LockedException) {
            message = MessageHelper.getText(request, "login.external.error.notactivated");
        } else if (e instanceof TermsOfUseNotAcceptedException) {
            message = MessageHelper.getText(request, "login.error.termsOfUseNotAccepted");
        } else if (e instanceof AuthenticationServiceException) {
            Throwable t = e.getCause();
            if (t != null && t instanceof EmailAlreadyExistsException) {
                message = MessageHelper.getText(request, "login.external.error.email.used");
            } else {
                message = MessageHelper.getText(request, "login.external.error.failed");
            }
        } else {
            message = MessageHelper.getText(request, "login.external.error.failed");
        }
        return message;
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
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) resp;

        if (isAuthenticationRequired(request)) {

            AbstractCommunoteAuthenticationToken<?> authenticationToken = tokenFactory
                    .createToken(extractToken(request));
            try {
                Authentication authResult = authenticationManager.authenticate(authenticationToken);

                ServiceLocator.findService(AuthenticationManagement.class)
                .onSuccessfulAuthentication(authResult);

            } catch (AuthenticationException e) {
                // Authentication failed
                if (LOGGER.isTraceEnabled()) {
                    LOGGER.trace(
                            "Authentication request for token: "
                                    + authenticationToken.getCredentials() + " failed: "
                                    + e.toString(), e);
                } else if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Authentication request for token: "
                            + authenticationToken.getCredentials() + " failed: " + e.toString());
                }

                AuthenticationHelper.removeAuthentication();
                response.setHeader("Cache-Control", "no-cache");
                String message = createErrorMessage(request, e);
                if (isJsonResponseRequested(request)) {
                    response.setContentType("application/json");
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    JsonHelper.writeJsonTree(response.getWriter(),
                            JsonRequestHelper.createJsonErrorResponse(message));
                } else {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, message);
                }
                // stop chain
                return;
            }
        }

        chain.doFilter(request, response);
    }

    /**
     * Extract the token string from the current request. The token will be taken from the request
     * parameters if {@link #isExtractTokenFromRequestParameter()} returns true, otherwise the token
     * is extracted from the request headers. The name of the parameter or header field is defined
     * by {@link TokenFactory#getTokenName()}.
     *
     * @param request
     *            the current request
     * @return the token or null if not set
     */
    protected String extractToken(HttpServletRequest request) {
        if (isExtractTokenFromRequestParameter()) {
            return request.getParameter(tokenFactory.getTokenName());
        } else {
            return request.getHeader(tokenFactory.getTokenName());
        }
    }

    /**
     * @return the authentication manager
     */
    public AuthenticationManager getAuthenticationManager() {
        return authenticationManager;
    }

    /**
     * @return the token factory
     */
    public TokenFactory<?> getTokenFactory() {
        return tokenFactory;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(FilterConfig arg0) throws ServletException {
    }

    /**
     * Check if an authentication is required. This implementation will return false if the
     * anonymous access is forced or if a token is provided and the current authenticated user was
     * authenticated with the same token.
     *
     * @param request
     *            the request to use
     * @return true if an authentication is required
     */
    protected boolean isAuthenticationRequired(HttpServletRequest request) {
        // do not authenticate if the public access has been granted
        if (Boolean.TRUE.equals(request
                .getAttribute(PublicAccessProcessingFilter.KENMEI_PUBLIC_ACCESS_FORCED))) {
            return false;
        }

        String currentToken = extractToken(request);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String sessionToken = null;
        if (auth != null && auth instanceof AbstractCommunoteAuthenticationToken
                && auth.getCredentials() instanceof String) {
            sessionToken = (String) auth.getCredentials();
        }

        return StringUtils.isNotBlank(currentToken)
                && !StringUtils.equals(currentToken, sessionToken);
    }

    /**
     * @return whether the token should be extracted from the request parameters or the request
     *         headers. This method returns true by default so that the token will be searched in
     *         the request parameters.
     */
    public boolean isExtractTokenFromRequestParameter() {
        return tokenFromRequestParameter;
    }

    /**
     * Test whether a JSON response should be returned by inspecting the Accept header.
     *
     * @param request
     *            the request
     * @return true if a JSON response is to be returned
     */
    protected boolean isJsonResponseRequested(HttpServletRequest request) {
        return JsonRequestHelper.isJsonResponseRequested(request);
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
     * Set whether the token should be extracted from the request parameters or the request headers
     *
     * @param tokenFromRequestParameter
     *            if true the token is extracted from the request parameters
     */
    public void setExtractTokenFromRequestParameter(boolean tokenFromRequestParameter) {
        this.tokenFromRequestParameter = tokenFromRequestParameter;
    }

    /**
     * @param tokenFactory
     *            the token factory
     */
    public void setTokenFactory(TokenFactory<?> tokenFactory) {
        Assert.notNull(tokenFactory, "A token factory is required.");
        this.tokenFactory = tokenFactory;
    }

}
