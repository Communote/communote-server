package com.communote.server.core.security.authentication.ldap;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.ldap.authentication.BindAuthenticator;
import org.springframework.security.ldap.userdetails.LdapUserDetailsMapper;

import com.communote.server.core.common.ldap.CommunoteLdapUserSearch;
import com.communote.server.core.common.ldap.LdapAttributeMappingException;
import com.communote.server.core.common.ldap.LdapSearchUtils;
import com.communote.server.core.common.ldap.LdapUserAttribute;
import com.communote.server.core.common.ldap.LdapUserAttributesMapper;
import com.communote.server.model.config.LdapConfiguration;
import com.communote.server.model.user.UserStatus;
import com.communote.server.persistence.user.ExternalUserVO;


/**
 * Authenticator to authenticate against LDAP directory.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class LdapAuthenticator {

    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(LdapAuthenticator.class);

    private final LdapConfiguration ldapConfig;

    /**
     * Creates a new authenticator.
     * 
     * @param ldapConfig
     *            the configuration of the authenticator
     */
    public LdapAuthenticator(LdapConfiguration ldapConfig) {
        this.ldapConfig = ldapConfig;
    }

    /**
     * Tries to authenticate the user. If the authentication fails an appropriate
     * {@link AuthenticationException} is thrown
     * 
     * @param username
     *            the user name of the user to authenticate.
     * @param password
     *            the password
     * @param usernameAttribute
     *            determines whether the username parameter refers to an email address or alias
     * @return the VO of the authenticated user
     * @throws LdapAttributeMappingException
     *             in case the result returned from can not be mapped to the the VO
     */
    public ExternalUserVO authenticate(String username, String password,
            LdapUserAttribute usernameAttribute) throws LdapAttributeMappingException {
        LOGGER.debug("Attempting authentication of user {} against LDAP directory", username);
        LdapUserAttributesMapper mapper = new LdapUserAttributesMapper(ldapConfig);
        LdapContextSource context = LdapSearchUtils.createLdapContext(ldapConfig, mapper);
        DirContextOperations ldapDetails;
        CommunoteLdapUserSearch search;
        // simple authentication via search for user and bind with the found DN
        if (ldapConfig.getSaslMode() == null) {
            // create ldap search based on the given user attribute and reusing context
            search = new CommunoteLdapUserSearch(ldapConfig.getUserSearch(), mapper, context,
                    usernameAttribute, null, null);
            // create Bind authenticator (which first checks with a search whether the user exists
            // and than tries to bind with that user and provided pwd)
            BindAuthenticator authenticator = new BindAuthenticator(context);
            authenticator.setUserSearch(search);
            // authenticate with ldap server with username and password
            ldapDetails = authenticator.authenticate(new UsernamePasswordAuthenticationToken(
                    username, password));
        } else {
            // do search as that user
            search = new CommunoteLdapUserSearch(ldapConfig.getUserSearch(), mapper, context,
                    usernameAttribute, username, password);
            ldapDetails = search.searchForUser(username);
        }
        ExternalUserVO userVO = search.transformResult(ldapDetails);
        checkAccountStatus(ldapDetails, username);
        // LDAP account status allows logging in thus set user status to active
        userVO.setStatus(UserStatus.ACTIVE);
        return userVO;

    }

    /**
     * Tests the status of the LDAP details and throws an appropriate exception.
     * 
     * @param dirContextOperations
     *            The context containing user data.
     * @param username
     *            The username.
     * @throws AuthenticationException
     *             if the status would prevent logging in
     */
    private void checkAccountStatus(DirContextOperations dirContextOperations, String username)
            throws AuthenticationException {
        UserDetails ldapDetails = new LdapUserDetailsMapper()
                .mapUserFromContext(dirContextOperations, username,
                        new ArrayList<GrantedAuthority>());
        if (!ldapDetails.isEnabled()) {
            throw new DisabledException("LDAP account is disabled.");
        }
        if (!ldapDetails.isAccountNonLocked()) {
            throw new LockedException("LDAP account is locked.");
        }
        if (!ldapDetails.isCredentialsNonExpired()) {
            throw new CredentialsExpiredException("Credentials for LDAP account are expired.");
        }
        if (!ldapDetails.isAccountNonExpired()) {
            throw new AccountExpiredException("LDAP account is expired.");
        }
    }
}
