package com.communote.server.core.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.communote.server.model.user.User;
import com.communote.server.model.user.UserStatus;


/**
 * Represents an internal system user.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class InternalSystemUserDetails extends UserDetails {

    private static final long serialVersionUID = 1L;

    /** The public user name */
    private static final String INTERNAL_SYSTEM_NAME = "internal_system";

    private static final User DUMMY_SYSTEM_USER;
    private static final Collection<GrantedAuthority> GRANTED_AUTHORITIES = new ArrayList<GrantedAuthority>();

    static {
        DUMMY_SYSTEM_USER = User.Factory.newInstance();
        DUMMY_SYSTEM_USER.setId(-2L);
        DUMMY_SYSTEM_USER.setAlias(INTERNAL_SYSTEM_NAME);
        DUMMY_SYSTEM_USER.setPassword(INTERNAL_SYSTEM_NAME);
        DUMMY_SYSTEM_USER.setStatus(UserStatus.ACTIVE);
        DUMMY_SYSTEM_USER.setLanguageCode(Locale.ENGLISH.getLanguage());
        DUMMY_SYSTEM_USER.setLanguageLocale(Locale.ENGLISH);
        GRANTED_AUTHORITIES.add(new SimpleGrantedAuthority(
                AuthenticationHelper.ROLE_INTERNAL_SYSTEM));
    }

    /**
     * Construct the <code>User</code> with the details required by
     * {@link org.acegisecurity.providers.dao.DaoAuthenticationProvider}.
     */
    public InternalSystemUserDetails() {
        super(DUMMY_SYSTEM_USER, INTERNAL_SYSTEM_NAME, GRANTED_AUTHORITIES);
    }

    /**
     * The alias of the user
     * 
     * @return The user alias
     */
    @Override
    public String getUserAlias() {
        return null;
    }

    /**
     * The user id of the user
     * 
     * @return The user
     */
    @Override
    public Long getUserId() {
        return null;
    }

    /**
     * @return the userLocale
     */
    @Override
    public Locale getUserLocale() {
        return null;
    }
}
