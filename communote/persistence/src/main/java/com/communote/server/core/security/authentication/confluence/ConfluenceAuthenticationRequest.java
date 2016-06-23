package com.communote.server.core.security.authentication.confluence;

import com.communote.server.core.security.authentication.AuthenticationRequest;

/**
 * A request for the confluence authentication api.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class ConfluenceAuthenticationRequest implements AuthenticationRequest {

    private String token;
    private String username;
    private String password;
    private boolean sendTokenAsParameter = false;

    /**
     * Construct a request based on a token.
     * 
     * @param token
     *            the token
     */
    public ConfluenceAuthenticationRequest(String token) {
        this.token = token;
    }

    /**
     * Construct a request based on Confluence user name and password. Password can be null for a
     * query request.
     * 
     * @param username
     *            the email.
     * @param password
     *            the password.
     */
    public ConfluenceAuthenticationRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    /**
     * @return the password. can be null.
     */
    public String getPassword() {
        return password;
    }

    /**
     * 
     * @return the token
     */
    public String getToken() {
        return token;
    }

    /**
     * 
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * @return <code>true</code>, when the parameter should be send via post parameter instead via
     *         header.
     */
    public boolean isSendTokenAsParameter() {
        return sendTokenAsParameter;
    }

    /**
     * @param sendTokenAsParameter
     *            <code>true</code>, when the parameter should be send via post parameter instead
     *            via header.
     */
    public void setSendTokenAsParameter(boolean sendTokenAsParameter) {
        this.sendTokenAsParameter = sendTokenAsParameter;
    }
}
