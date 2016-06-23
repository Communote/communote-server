package com.communote.server.web.external.spring.security;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;

/**
 * Authentication Entry Point for the Basic Authentication. It extends the spring standard
 * functionality by optional disabling this entry point controlled by a request parameter. This is
 * useful when communote runs in some embedded mode (e.g. confluence) and we do not want a basic
 * authentication dialog to pop up.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class CommunoteBasicAuthenticationEntryPoint extends BasicAuthenticationEntryPoint {

    /**
     * Default name for the request parameter that when set to "true" will lead to not sending a
     * basic authentication challenge.
     */
    public static final String DEFAULT_NO_AUTHENTICATION_CHALLENGE_REQUEST_PARAM = "noAuthenticationChallenge";
    private String noChallengeParameter = DEFAULT_NO_AUTHENTICATION_CHALLENGE_REQUEST_PARAM;

    /**
     * {@inheritDoc}
     */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException authException)
            throws IOException, ServletException {
        // if this is true we will not commence (start, force, challenge for) a basic authentication
        boolean noAuthenticationChallenge = Boolean.parseBoolean(request
                .getParameter(noChallengeParameter));
        if (!noAuthenticationChallenge) {
            response.addHeader("WWW-Authenticate", "Basic realm=\"" + getRealmName() + "\"");
        }
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, authException.getMessage());
    }

    /**
     * Set the name of the request parameter that when set to "true" will lead to not sending a
     * basic authentication challenge.
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
