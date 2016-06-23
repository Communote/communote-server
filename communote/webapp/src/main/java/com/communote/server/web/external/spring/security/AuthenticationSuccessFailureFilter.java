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
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.web.filter.GenericFilterBean;

import com.communote.server.core.security.TermsOfUseNotAcceptedException;

/**
 * This filter centralizes the logic that otherwise would need to be done by other filters. Any
 * {@link AuthenticationException} that will occur in the chain will be handled. Also if a success
 * authentication is detected using
 * {@link AuthenticationSuccessRequestSaverHandler#getSuccessAuthentication(HttpServletRequest) the
 * success handling will be applied.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class AuthenticationSuccessFailureFilter extends GenericFilterBean {

    private final static Logger LOGGER = LoggerFactory
            .getLogger(AuthenticationSuccessFailureFilter.class);

    /**
     * Name of the session attribute holding the user ID which is stored when a user logs in and has
     * to accept the terms of use and privacy policy.
     */
    public static final String SESSION_ATTR_USER_ACCEPT_TERMS = "communoteAcceptTermsUserId";

    // is overwritten in authentication.xml using CommunoteAuthenticationSuccessHandler
    private AuthenticationSuccessHandler successHandler = new SavedRequestAwareAuthenticationSuccessHandler();
    // is overwritten in authentication.xml using CommunoteAuthenticationFailureHandler
    private AuthenticationFailureHandler failureHandler = new SimpleUrlAuthenticationFailureHandler();

    /**
     * {@inheritDoc}
     */
    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        try {
            // first do the chain
            chain.doFilter(request, response);
            if (AuthenticationSuccessRequestSaverHandler.hasRequestSuccessAuthentication(request)) {
                LOGGER.trace("Successful authentication detected. Running handler.");
                successHandler.onAuthenticationSuccess(request, response,
                        AuthenticationSuccessRequestSaverHandler.getSuccessAuthentication(request));
            }
        } catch (AuthenticationException authException) {

            if (authException instanceof TermsOfUseNotAcceptedException) {
                TermsOfUseNotAcceptedException termsException = (TermsOfUseNotAcceptedException) authException;
                request.getSession().setAttribute(SESSION_ATTR_USER_ACCEPT_TERMS,
                        termsException.getUserId());
            }
            // just to make sure.
            SecurityContextHolder.clearContext();

            LOGGER.debug("Handling authException: {} (Turn trace Logging on for details)",
                    authException.getMessage());
            LOGGER.trace("Authentication exception that will be handled: {}",
                    authException.getMessage(), authException);

            // now handle the exception
            failureHandler.onAuthenticationFailure(request, response, authException);
        }

    }

    /**
     *
     * @return the failure handler
     */
    public AuthenticationFailureHandler getFailureHandler() {
        return failureHandler;
    }

    /**
     *
     * @return the success handler
     */
    public AuthenticationSuccessHandler getSuccessHandler() {
        return successHandler;
    }

    /**
     *
     * @param failureHandler
     *            the failure handler
     */
    public void setFailureHandler(AuthenticationFailureHandler failureHandler) {
        this.failureHandler = failureHandler;
    }

    /**
     *
     * @param successHandler
     *            the success handler
     */

    public void setSuccessHandler(AuthenticationSuccessHandler successHandler) {
        this.successHandler = successHandler;
    }

}
