package com.communote.server.web.commons.filter;

import java.io.IOException;
import java.util.Collection;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.Assert;
import org.springframework.web.bind.ServletRequestUtils;

import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.config.type.ClientProperty;
import com.communote.server.core.config.ClientConfigurationHelper;
import com.communote.server.core.security.AuthenticationHelper;
import com.communote.server.core.security.UserDetails;

/**
 * Filter to manage public access.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class PublicAccessProcessingFilter implements Filter, InitializingBean {

    /**
     * Request parameter that has been set to true only if the public access has been forced, that
     * is doPublicAccess=true and public access has been activated in the administration.
     */
    public static final String KENMEI_PUBLIC_ACCESS_FORCED = "__kenmeiPublicAccessForced";

    /**
     * This request parameter of the URL query string defines whether an anonymous access should be
     * done or not.
     */
    public static final String PARAM_PUBLIC_ACCESS = "doPublicAccess";

    private static final Logger LOG = Logger.getLogger(PublicAccessProcessingFilter.class);

    /** is set by authentication.xml property */
    private AuthenticationManager authenticationManager;

    /**
     * Does nothing.
     */
    public PublicAccessProcessingFilter() {
        // Do nothing.
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(this.authenticationManager, "An AuthenticationManager is required");
    }

    /**
     * Does nothing.
     */
    @Override
    public void destroy() {
        // Do nothing.
    }

    /**
     * @param request
     *            the servlet request
     * @param response
     *            the servlet response
     * @param filterChain
     *            the filter chain
     * @throws IOException
     *             ...
     * @throws ServletException
     *             ...
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
            throws IOException, ServletException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Public assess check." + request.getRemoteAddr());
        }
        boolean doPublicAccess = ServletRequestUtils.getBooleanParameter(request,
                PARAM_PUBLIC_ACCESS, false);

        boolean isPublicAccess = CommunoteRuntime
                .getInstance()
                .getConfigurationManager()
                .getClientConfigurationProperties()
                .getProperty(ClientProperty.CLIENT_BLOG_ALLOW_PUBLIC_ACCESS,
                        ClientConfigurationHelper.DEFAULT_ALLOW_PUBLIC_ACCESS);

        if (doPublicAccess && isPublicAccess) {
            if (isAnonymousOrNoAuthentication()) {
                // set anonymous user and its role
                AuthenticationHelper.setPublicUserToSecurityContext(request);
            }
            // set this attribute always if public access is forced
            request.setAttribute(KENMEI_PUBLIC_ACCESS_FORCED, Boolean.TRUE);
        }

        // destroy a public authentication if client manager turned off the public access
        if (!isPublicAccess && isPublicAuthentication()) {

            AuthenticationHelper.removeAuthentication();
            request.setAttribute(KENMEI_PUBLIC_ACCESS_FORCED, Boolean.FALSE);
        }
        filterChain.doFilter(request, response);
    }

    /**
     * @return the authentication manager
     */
    public AuthenticationManager getAuthenticationManager() {
        return authenticationManager;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // nothing to do
    }

    /**
     * Checks if an authentication already exists and whether it is an anonymous authentication or
     * not.
     *
     * @return true if request is authenticated as anonymous or not authenticated
     */
    private boolean isAnonymousOrNoAuthentication() {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        boolean isAuthenticated = auth != null && auth.isAuthenticated();

        if (isAuthenticated) {
            return auth instanceof AnonymousAuthenticationToken;
        } else {
            return true;
        }
    }

    /**
     * Checks if an authentication already exists and whether it is a public authentication or not.
     *
     * @return true if request is authenticated as public or not authenticated
     */
    private boolean isPublicAuthentication() {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        boolean isAuthenticated = auth != null && auth.isAuthenticated();

        if (isAuthenticated) {
            Collection<? extends GrantedAuthority> grantedAutorities = auth.getAuthorities();
            if (auth.getPrincipal() instanceof UserDetails
                    && grantedAutorities.contains(new SimpleGrantedAuthority(
                            AuthenticationHelper.PUBLIC_USER_ROLE))) {
                isAuthenticated = true;
            } else {
                isAuthenticated = false;
            }
        }

        return isAuthenticated;
    }

    /**
     * @param authenticationManager
     *            the {@link AuthenticationManager} manager to use, cannot be null
     */
    public void setAuthenticationManager(AuthenticationManager authenticationManager) {
        Assert.notNull(authenticationManager, "AuthenticationManager required");
        this.authenticationManager = authenticationManager;
    }

}
