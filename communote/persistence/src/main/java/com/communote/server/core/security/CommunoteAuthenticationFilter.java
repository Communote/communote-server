package com.communote.server.core.security;

import javax.servlet.Filter;

import org.springframework.security.authentication.AuthenticationManager;

import com.communote.common.util.Orderable;

/**
 * Interface to define filters, which could be provided by plugins.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public interface CommunoteAuthenticationFilter extends Filter, Orderable {
    /**
     * Method to set the current authentication manager.
     * 
     * @param authenticationManager
     *            The authentication manager.
     */
    public void setAuthenticationManager(AuthenticationManager authenticationManager);
}
