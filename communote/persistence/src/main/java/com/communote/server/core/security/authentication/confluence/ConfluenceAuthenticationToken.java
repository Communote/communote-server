package com.communote.server.core.security.authentication.confluence;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;

import com.communote.server.core.security.authentication.AbstractCommunoteAuthenticationToken;


/**
 * A token to authenticate a user against Confluence. The actual token string will be available as
 * credentails.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ConfluenceAuthenticationToken extends AbstractCommunoteAuthenticationToken<String> {
    private static final long serialVersionUID = 1L;
    private final boolean sendTokenAsParameter;

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
    public ConfluenceAuthenticationToken(Object principal, String token,
            Collection<GrantedAuthority> authorities) {
        super(principal, token, authorities);
        this.sendTokenAsParameter = false;
    }

    /**
     * Creates a token for authentication against Confluence.
     * 
     * @param token
     *            a token for authenticating the user. The token is stored as credentials.
     * @param sendTokenAsParameter
     *            <code>true</code>, when the token should be send as POST parameter instead header
     *            field.
     */
    public ConfluenceAuthenticationToken(String token, boolean sendTokenAsParameter) {
        super(token);
        this.sendTokenAsParameter = sendTokenAsParameter;

    }

    /**
     * @return <code>true</code>, when the parameter should be send via post parameter instead via
     *         header.
     */
    public boolean isSendTokenAsParameter() {
        return sendTokenAsParameter;
    }
}