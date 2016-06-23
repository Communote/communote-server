package com.communote.server.core.security;

import javax.servlet.FilterConfig;

import org.springframework.security.authentication.AuthenticationManager;

import com.communote.server.api.ServiceLocator;


/**
 * Abstract implementation of {@link CommunoteAuthenticationFilter} with order 0 and concrete
 * methods for getting the {@link AuthenticationManager}.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class AbstractCommunoteAuthenticationFilter implements
        CommunoteAuthenticationFilter {

    private AuthenticationManager authenticationManager;

    private AuthenticationManagement authenticationManagement;

    /**
     * Does nothing.
     */
    @Override
    public void destroy() {
        // Do nothing.
    }

    /**
     * @return the authenticationManagement
     */
    public AuthenticationManagement getAuthenticationManagement() {
        if (authenticationManagement == null) {
            authenticationManagement = ServiceLocator.instance().getService(
                    AuthenticationManagement.class);
        }
        return authenticationManagement;
    }

    /**
     * @return the authenticationManager
     */
    public AuthenticationManager getAuthenticationManager() {
        return authenticationManager;
    }

    /**
     * @return 0
     */
    @Override
    public int getOrder() {
        return 0;
    }

    /**
     * Does nothing.
     * 
     * @param filterConfig
     *            Ignored.
     */
    @Override
    public void init(FilterConfig filterConfig) {
        // Do nothing.
    }

    /**
     * @param authenticationManagement
     *            the authenticationManagement to set
     */
    public void setAuthenticationManagement(AuthenticationManagement authenticationManagement) {
        this.authenticationManagement = authenticationManagement;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setAuthenticationManager(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

}
