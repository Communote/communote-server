package com.communote.plugins.mq.provider.activemq.security.authentication;

import java.util.Set;

import javax.security.auth.login.LoginException;

/**
 * Interface for authenticators.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public interface Authenticator {
    /**
     * Authenticates the user with the given password.
     * 
     * @param username
     *            The users name.
     * @param password
     *            The password.
     * @return A set of roles of this user.
     * @throws LoginException
     *             Thrown, when the login fails.
     */
    public Set<String> authenticate(String username, String password) throws LoginException;
}
