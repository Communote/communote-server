package com.communote.server.core.security;

import java.util.ArrayList;
import java.util.Locale;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.communote.server.model.user.User;
import com.communote.server.model.user.UserStatus;


/**
 * Represents a not logged in public user.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class PublicUserDetails extends UserDetails {

    private static final long serialVersionUID = 1L;

    /** The public user name */
    private static final String PUBLIC_USER_NAME = "kenmei_guest";

    private static final User DUMMY_USER;
    static {
        DUMMY_USER = User.Factory.newInstance();
        DUMMY_USER.setId(-1L);
        DUMMY_USER.setAlias(PUBLIC_USER_NAME);
        DUMMY_USER.setPassword(PUBLIC_USER_NAME);
        DUMMY_USER.setStatus(UserStatus.ACTIVE);
        DUMMY_USER.setLanguageCode(Locale.ENGLISH.getLanguage());
        DUMMY_USER.setLanguageLocale(Locale.ENGLISH);
    }

    private static final ArrayList<GrantedAuthority> GRANTED_AUTHORITIES;
    static {
        GRANTED_AUTHORITIES = new ArrayList<GrantedAuthority>();
        GRANTED_AUTHORITIES.add(new SimpleGrantedAuthority(AuthenticationHelper.PUBLIC_USER_ROLE));
    }

    /**
     * Construct the <code>User</code> with the details required by
     * {@link org.acegisecurity.providers.dao.DaoAuthenticationProvider}.
     */
    public PublicUserDetails() {
        super(DUMMY_USER, PUBLIC_USER_NAME, GRANTED_AUTHORITIES);
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
