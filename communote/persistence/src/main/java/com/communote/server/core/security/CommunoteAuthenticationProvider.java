package com.communote.server.core.security;

import org.springframework.security.authentication.AuthenticationProvider;

import com.communote.common.util.Orderable;

/**
 * Interface for orderable authentication providers, which could be implemented by plugins.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public interface CommunoteAuthenticationProvider extends AuthenticationProvider, Orderable {

}
