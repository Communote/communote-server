package com.communote.server.core.security;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.communote.server.api.core.common.NotFoundException;
import com.communote.server.model.user.User;
import com.communote.server.persistence.user.UserDao;


/**
 * This is a service class for managimg authentication tokens.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
@Service
public class AuthenticationTokenManagement {

    /** Logger. */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(AuthenticationTokenManagement.class);

    @Autowired
    private UserDao userDao;

    /**
     * Value for authentication tokens. Can be set via a system property
     * "com.communote.security.token-expiration-timeout". Default is 2 weeks = 14*24*60. Set it to 0
     * or less to skip the expiration timeout.
     */
    private final static int PROPERTY_AUTHENTICATION_TOKEN_EXPIRATION_TIMEOUT_IN_MINUTES =
            Integer.getInteger("com.communote.security.token-expiration-timeout", 14 * 24 * 60);

    /**
     * Method to get the authentication token for a given user. This method will automatically
     * create a new one, if the given one is outdated.
     * 
     * @param userId
     *            Id of the user to create the token for.
     * @return The authentication token.
     * @throws NotFoundException
     *             Thrown, when the user can't be found.
     */
    public String getAuthenticationToken(Long userId) throws NotFoundException {
        User user = userDao.load(userId);
        if (user == null) {
            throw new NotFoundException("The user with id " + userId + " can't be found.");
        }
        String token = user.getAuthenticationToken();
        if (token != null) {
            String[] split = token.split(":");
            token = split[1];
            long creationTime = Long.parseLong(split[0]);
            if (PROPERTY_AUTHENTICATION_TOKEN_EXPIRATION_TIMEOUT_IN_MINUTES > 0
                    && creationTime - System.currentTimeMillis()
                            + (PROPERTY_AUTHENTICATION_TOKEN_EXPIRATION_TIMEOUT_IN_MINUTES * 60000) < 0) {
                token = null;
            }
        }

        if (token == null) {
            LOGGER.debug(
                    "No authentication token found for user {}. Creating a new one.", userId);
            long now = System.currentTimeMillis();
            token = UUID.randomUUID().toString() + now;
            user.setAuthenticationToken(now + ":" + token);
            userDao.update(user);
        }
        return token;
    }
}
