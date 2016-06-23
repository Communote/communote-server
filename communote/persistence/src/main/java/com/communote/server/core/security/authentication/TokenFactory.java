package com.communote.server.core.security.authentication;

/**
 * Factory to create a Communote authentication token
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * @param <T>
 *            the type of the token it will create
 */
public interface TokenFactory<T extends AbstractCommunoteAuthenticationToken<?>> {

    /**
     * 
     * @param token
     *            the token string
     * @return create the specific token
     */
    public T createToken(String token);

    /**
     * 
     * @return the name of the parameter or header field that holds the token value
     */
    public String getTokenName();
}
