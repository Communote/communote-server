package com.communote.server.core.security;

import java.util.Collection;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.springframework.security.core.GrantedAuthority;

import com.communote.server.core.user.UserAuthorityHelper;
import com.communote.server.model.user.User;

/**
 * Value object with user details which is compatible to the spring security framework.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class UserDetails extends org.springframework.security.core.userdetails.User {

    private static final long serialVersionUID = 1L;

    private Long userId = null;
    private String userAlias = null;

    // TODO Remove and use UserProfileDetails instead
    /**
     * @deprecated Use UserProfileDetails instead.
     */
    @Deprecated
    private Locale userLocale = null;

    /**
     *
     * @param user
     *            the user represented by this details, cannot be null.
     * @param username
     *            the username that was used for logging in
     */
    public UserDetails(User user, String username) {
        this(user, username, UserAuthorityHelper.getGrantedAuthorities(user));
    }

    /**
     * @param user
     *            the user represented by this details, cannot be null.
     * @param username
     *            the username that was used for logging in
     * @param grantedAuthorities
     *            The authorities of the user.
     */
    public UserDetails(User user, String username, Collection<GrantedAuthority> grantedAuthorities) {
        super(username, user.getPassword() == null ? StringUtils.EMPTY : user.getPassword(), user
                .isActivated(), true, true, true, grantedAuthorities);
        this.setUserId(user.getId());
        this.setUserAlias(user.getAlias());
        this.setUserLocale(user.getLanguageLocale());
    }

    /**
     * The alias of the user
     *
     * @return The user alias
     */
    public String getUserAlias() {
        return userAlias;
    }

    /**
     * The user id of the user
     *
     * @return The user
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * @return the userLocale
     */
    public Locale getUserLocale() {
        return userLocale;
    }

    /**
     * Sets the user alias.
     *
     * @param alias
     *            the user alias
     */
    private void setUserAlias(String alias) {
        if (alias == null) {
            throw new IllegalArgumentException("User alias cannot be null");
        }
        this.userAlias = alias;
    }

    /**
     * Sets the user id.
     *
     * @param id
     *            the user id
     */
    private void setUserId(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("User id cannot be null");
        }
        this.userId = id;
    }

    /**
     * @param userLocale
     *            the userLocale to set
     */
    public void setUserLocale(Locale userLocale) {
        this.userLocale = userLocale;
    }
}
