package com.communote.server.web.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.communote.common.converter.IdentityConverter;
import com.communote.server.api.ServiceLocator;
import com.communote.server.core.user.UserManagement;
import com.communote.server.model.user.User;

/**
 * Implementation of the UserDetailsService that interprets the username as a user ID like the
 * CommunoteRememberMeService does.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class CommunoteRememberMeUserDetailsService implements UserDetailsService {

    private UserManagement userManagement;

    /**
     * @return lazily initialized UserManagement
     */
    private UserManagement getUserManagement() {
        if (userManagement == null) {
            userManagement = ServiceLocator.findService(UserManagement.class);
        }
        return userManagement;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            Long userId = Long.parseLong(username);
            User user = getUserManagement().getUserById(userId, new IdentityConverter<User>());
            if (user != null) {
                return new com.communote.server.core.security.UserDetails(user, username);
            }
            throw new UsernameNotFoundException("User with ID " + username + " does not exist");
        } catch (NumberFormatException e) {
            throw new UsernameNotFoundException("Username " + username + " is not a valid user ID");
        }
    }

}
