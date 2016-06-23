package com.communote.server.core.security.authentication;

/**
 * An authentication request is a request for an authentication of a user
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public interface AuthenticationRequest {

    /**
     * 
     * @return the password of the request.
     */
    public String getPassword();
}
