package com.communote.server.core.security.authentication;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationEventPublisher;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.common.ClientAndChannelContextHolder;
import com.communote.server.core.security.AuthenticationManagement;

/**
 * Notifies the AuthenticationManagement about a failed authentication if the authentication was a
 * UsernamePasswordAuthentication and the authentication failed because of bad credentials.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class AuthenticationFailedEventPublisher implements AuthenticationEventPublisher {

    private static Logger LOGGER = LoggerFactory
            .getLogger(AuthenticationFailedEventPublisher.class);

    @Override
    public void publishAuthenticationFailure(AuthenticationException exception,
            Authentication authentication) {
        if (exception instanceof BadCredentialsException
                && authentication instanceof UsernamePasswordAuthenticationToken) {
            String username = authentication.getName();
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(
                        "Username and password authentication request for user '{}' on channel '{}' had bad credentials",
                        username, ClientAndChannelContextHolder.getChannel());
            }
            if (StringUtils.isNotBlank(username)) {
                AuthenticationManagement authManagement = ServiceLocator
                        .findService(AuthenticationManagement.class);
                if (exception instanceof BadCredentialsForExternalSystemException) {
                    authManagement.onUsernamePasswordAuthenticationFailed(username,
                            ((BadCredentialsForExternalSystemException) exception)
                            .getExternalSystemId());
                } else {
                    authManagement.onUsernamePasswordAuthenticationFailed(username);
                }
            }
        }
    }

    @Override
    public void publishAuthenticationSuccess(Authentication authentication) {
        // ignore
    }

}
