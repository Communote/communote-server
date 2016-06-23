package com.communote.server.web.security;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.rememberme.TokenBasedRememberMeServices;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.common.NotFoundException;
import com.communote.server.core.security.AuthenticationTokenManagement;
import com.communote.server.core.security.UserDetails;

/**
 * This is a custom RememberMe service, which provides enhanced functionality, to enable remember me
 * independent of the used plattform.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class CommunoteRememberMeServices extends TokenBasedRememberMeServices {

    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(CommunoteRememberMeServices.class);

    /**
     * Constructor.
     * 
     * @param key
     *            An key.
     * @param userDetailsService
     *            The {@link UserDetailsService} for getting users.
     */
    public CommunoteRememberMeServices(String key, UserDetailsService userDetailsService) {
        super(key, userDetailsService);
    }

    private String makeTokenSignature(long tokenExpiryTime, Long userId) {
        AuthenticationTokenManagement authenticationTokenManagement = ServiceLocator
                .findService(AuthenticationTokenManagement.class);
        try {
            String token = authenticationTokenManagement.getAuthenticationToken(userId);
            return DigestUtils.sha512Hex(userId.toString() + tokenExpiryTime + token);
        } catch (NotFoundException e) {
            LOGGER.debug("User with ID {} not found", userId);
            throw new IllegalStateException(e);
        }
    }

    /**
     * Returns a specific authentication token for the user, instead of a combination of time and
     * password. The username is expected to be the user ID.
     * <p>
     * 
     * {@inheritDoc}
     */
    @Override
    protected String makeTokenSignature(long tokenExpiryTime, String username, String password) {
        try {
            Long userId = Long.parseLong(username);
            return makeTokenSignature(tokenExpiryTime, userId);
        } catch (NumberFormatException e) {
            LOGGER.debug("User name {} is not a valid user ID", username);
            throw new IllegalStateException("User name is not a valid user ID");
        }
    }

    /**
     * This implementation skips the password check, as we don't work with the password.
     * <p>
     * {@inheritDoc}
     */
    @Override
    public void onLoginSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication successfulAuthentication) {

        Long userId = retrieveUserId(successfulAuthentication);

        // If unable to find a user ID, just abort as TokenBasedRememberMeServices is
        // unable to construct a valid token in this case.
        if (userId == null) {
            LOGGER.debug("Unable to retrieve user ID");
            return;
        }

        int tokenLifetime = calculateLoginLifetime(request, successfulAuthentication);
        long expiryTime = System.currentTimeMillis();
        // SEC-949
        expiryTime += 1000L * (tokenLifetime < 0 ? TWO_WEEKS_S : tokenLifetime);
        String signatureValue = makeTokenSignature(expiryTime, userId);
        setCookie(new String[] { userId.toString(), Long.toString(expiryTime), signatureValue },
                tokenLifetime, request, response);
        LOGGER.debug("Added remember-me cookie for user '{}', expiry: '{}'", userId,
                new Date(
                        expiryTime));
    }

    /**
     * Get ID of authenticated user
     * 
     * @param authentication
     *            the authentication
     * @return the user ID or null
     */
    private Long retrieveUserId(Authentication authentication) {
        if (authentication.getPrincipal() instanceof UserDetails) {
            return ((UserDetails) authentication.getPrincipal()).getUserId();
        }
        return null;
    }

    @Override
    protected String retrieveUserName(Authentication authentication) {
        Long userId = retrieveUserId(authentication);
        if (userId != null) {
            return userId.toString();
        }
        LOGGER.debug("No user ID in authentication");
        return null;
    }
}
