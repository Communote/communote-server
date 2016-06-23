package com.communote.server.web.security;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import com.communote.server.api.ServiceLocator;
import com.communote.server.core.security.AuthenticationProviderManagement;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class PluginAuthenticationProvider implements AuthenticationProvider {

    private AuthenticationProviderManagement authenticationProviderManagement;

    /**
     * {@inheritDoc}
     */
    @Override
    public Authentication authenticate(Authentication authentication)
            throws AuthenticationException {
        return getAuthenticationProviderManagement().authenticate(authentication);
    }

    /**
     * @return the authenticationProviderManagement
     */
    public AuthenticationProviderManagement getAuthenticationProviderManagement() {
        if (authenticationProviderManagement == null) {
            authenticationProviderManagement = ServiceLocator.instance().getService(
                    AuthenticationProviderManagement.class);
        }
        return authenticationProviderManagement;
    }

    /**
     * @param authenticationProviderManagement
     *            the authenticationProviderManagement to set
     */
    public void setAuthenticationProviderManagement(
            AuthenticationProviderManagement authenticationProviderManagement) {
        this.authenticationProviderManagement = authenticationProviderManagement;
    }

    /**
     * @param authentication
     *            Not used.
     * @return true.
     */
    @Override
    public boolean supports(Class<?> authentication) {
        return true;
    }
}
