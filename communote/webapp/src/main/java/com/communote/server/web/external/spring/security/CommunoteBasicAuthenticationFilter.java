package com.communote.server.web.external.spring.security;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

/**
 * Extension to the Basic Authentication Filter which allows disabling the check for the
 * authentication header with the help of an request parameter. This should typically be used
 * together with {@link CommunoteBasicAuthenticationEntryPoint} that checks for the same parameter.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class CommunoteBasicAuthenticationFilter extends BasicAuthenticationFilter {

    private String noChallengeParameter = CommunoteBasicAuthenticationEntryPoint.DEFAULT_NO_AUTHENTICATION_CHALLENGE_REQUEST_PARAM;

    /**
     * Default constructor
     */
    public CommunoteBasicAuthenticationFilter() {
        super();
    }

    /**
     * Constructor for consistency with parent class.
     * 
     * @param authenticationManager
     *            the authentication manager to use
     */
    public CommunoteBasicAuthenticationFilter(AuthenticationManager authenticationManager) {
        super(authenticationManager);
    }

    /**
     * Constructor for consistency with parent class.
     * 
     * @param authenticationManager
     *            the authentication manager to use
     * @param authenticationEntryPoint
     *            the entry point
     */
    public CommunoteBasicAuthenticationFilter(AuthenticationManager authenticationManager,
            AuthenticationEntryPoint authenticationEntryPoint) {
        super(authenticationManager, authenticationEntryPoint);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        String param = request.getParameter(noChallengeParameter);
        if (param != null && Boolean.parseBoolean(param)) {
            chain.doFilter(request, response);
        } else {
            super.doFilter(request, response, chain);
        }
    }

    /**
     * Set the name of the request parameter that when set to "true" will lead to ignoring the basic
     * authentication header. The parameter name defaults to
     * {@link CommunoteBasicAuthenticationEntryPoint#DEFAULT_NO_AUTHENTICATION_CHALLENGE_REQUEST_PARAM}
     * 
     * @param paramName
     *            the name of the parameter
     */
    public void setNoChallangeParameter(String paramName) {
        if (paramName != null) {
            this.noChallengeParameter = paramName;
        }
    }
}
