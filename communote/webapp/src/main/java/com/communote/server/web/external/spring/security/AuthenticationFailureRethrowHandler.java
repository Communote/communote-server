package com.communote.server.web.external.spring.security;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

/**
 * This handler pushes the limit of the spring framework. It just rethrows the exception and expects
 * it be catch in some filter such as {@link AuthenticationSuccessFailureFilter}.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class AuthenticationFailureRethrowHandler implements AuthenticationFailureHandler {

    private final static Logger LOGGER = LoggerFactory
            .getLogger(AuthenticationSuccessFailureFilter.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException exception) throws IOException, ServletException {
        LOGGER.trace("Rethrowing exception {0}", exception.getMessage());
        throw exception;
    }

}
