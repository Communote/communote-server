package com.communote.server.core.security.authentication;

import java.util.Collection;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

/**
 * Base class for holding the token that is used in the Communote token authentication method.
 * 
 * @param <T>
 *            the type of token it will hold
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class AbstractCommunoteAuthenticationToken<T extends Object> extends
        AbstractAuthenticationToken {
    private static final long serialVersionUID = 1L;

    private final T token;
    private Object principal;

    /**
     * Allows the creation of a successful authentication. When the authorities are not null
     * isAuthenticated will return true.
     * 
     * @param principal
     *            the principal
     * @param token
     *            a token for authenticating the user. The token is stored as credentials.
     * @param authorities
     *            the granted authorities
     */
    public AbstractCommunoteAuthenticationToken(Object principal, T token,
            Collection<GrantedAuthority> authorities) {
        super(authorities);
        if (authorities != null) {
            super.setAuthenticated(true);
        }
        this.principal = principal;
        this.token = token;
    }

    /**
     * Creates a token for authentication against Confluence.
     * 
     * @param token
     *            a token for authenticating the user. The token is stored as credentials.
     */
    public AbstractCommunoteAuthenticationToken(T token) {
        super(null);
        this.token = token;
    }

    /**
     * The credentials is the token retrieved
     * 
     * @return the token
     */
    @Override
    public T getCredentials() {
        return token;
    }

    /**
     * Returns null because this principal (the token) has no name.
     * 
     * @return null
     * @see java.security.Principal#getName()
     */
    @Override
    public String getName() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getPrincipal() {
        return principal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setAuthenticated(boolean authenticated) {
        if (authenticated) {
            throw new IllegalArgumentException(
                    "Cannot set this token to trusted - use constructor containing GrantedAuthority[]s instead");
        }

        super.setAuthenticated(false);
    }

}
