package com.communote.server.core.security.authentication.ldap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.util.Assert;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.core.ConfigurationManagement;
import com.communote.server.core.common.ldap.LdapAttributeMappingException;
import com.communote.server.core.common.ldap.LdapUserAttribute;
import com.communote.server.core.common.ldap.LdapUtils;
import com.communote.server.core.common.ldap.RequiredAttributeNotContainedException;
import com.communote.server.core.security.UserDetails;
import com.communote.server.core.security.authentication.ExternalAuthenticationProvider;
import com.communote.server.core.user.UserManagement;
import com.communote.server.model.config.LdapConfiguration;
import com.communote.server.model.user.User;
import com.communote.server.persistence.user.ExternalUserVO;
import com.communote.server.persistence.user.InvitationField;
import com.communote.server.persistence.user.invitationfields.EmailAliasInvitationField;
import com.communote.server.persistence.user.invitationfields.LanguageCodeInvitationField;

/**
 * Authentication provider that authenticates a user against a user directory that supports LDAP.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class LdapAuthenticationProvider extends ExternalAuthenticationProvider {
    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(LdapAuthenticationProvider.class);

    private final List<InvitationField> invitationFields;

    /**
     * Constructor.
     */
    public LdapAuthenticationProvider() {
        List<InvitationField> fields = new ArrayList<InvitationField>();
        fields.add(EmailAliasInvitationField.INSTANCE);
        fields.add(LanguageCodeInvitationField.INSTANCE);
        this.invitationFields = Collections.unmodifiableList(fields);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Authentication createSuccessAuthentication(UserDetails details,
            Authentication authentication) {
        if (details == null || authentication == null) {
            return null;
        }
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(details,
                authentication.getCredentials(), details.getAuthorities());
        auth.setDetails(authentication.getDetails());
        return auth;
    }

    /**
     * @return the confluence configuration - can be null
     */
    @Override
    protected LdapConfiguration getConfiguration() {
        return CommunoteRuntime.getInstance().getConfigurationManager()
                .getClientConfigurationProperties().getLdapConfiguration();
    }

    /**
     * @return ConfigurationManagement.DEFAULT_LDAP_SYSTEM_ID
     */
    @Override
    public String getIdentifier() {
        return ConfigurationManagement.DEFAULT_LDAP_SYSTEM_ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<InvitationField> getInvitationFields() {
        return this.invitationFields;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ExternalUserVO queryUser(Map<InvitationField, String> queryData) {
        String userName = queryData.get(EmailAliasInvitationField.INSTANCE);
        if (StringUtils.isEmpty(userName)) {
            throw new IllegalArgumentException(
                    "Email alias cannot be empty when querying a ldap user!");
        }

        return LdapUtils.queryUserByName(userName);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ExternalUserVO retrieveExternalUser(Authentication authentication)
            throws AuthenticationException {
        Assert.isInstanceOf(UsernamePasswordAuthenticationToken.class, authentication,
                "Only UsernamePasswordAuthenticationToken is supported");

        String username = authentication.getName();
        if (StringUtils.isBlank(username)) {
            throw new BadCredentialsException("Empty username!");
        }
        username = username.trim();
        String password = (authentication.getCredentials() == null) ? null
                : authentication.getCredentials().toString();

        // authenticate by email or by alias
        LdapUserAttribute usernameAttribute = null;
        if (username.contains("@")) {
            usernameAttribute = LdapUserAttribute.EMAIL;
        } else {
            usernameAttribute = LdapUserAttribute.ALIAS;
        }
        LdapAuthenticator authenticator = new LdapAuthenticator(getConfiguration());
        try {
            ExternalUserVO userVO = authenticator.authenticate(username, password,
                    usernameAttribute);
            LdapUtils.setSynchronizationFields(userVO);
            return userVO;
        } catch (RequiredAttributeNotContainedException e) {
            LOGGER.warn("Problem during context mapping: {}: {} for {}",
                    new Object[] { e.getMessage(), e.getLdapAttributeName(), username });
            throw new AuthenticationServiceException(
                    "LDAP authentication failed due to attribute mapping exception", e);
        } catch (LdapAttributeMappingException e) {
            LOGGER.error("Error during context mapping: {} for {}", e.getMessage(), username);
            throw new AuthenticationServiceException(
                    "LDAP authentication failed due to attribute mapping exception", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean supports(@SuppressWarnings("rawtypes") Class authentication) {
        LdapConfiguration configuration = getConfiguration();

        if (configuration != null && configuration.isAllowExternalAuthentication()
                && configuration.isPrimaryAuthentication()) {

            return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
        }
        return false;
    }

    /**
     * Synchronizes an LDAP user with the database.
     *
     * @param userVO
     *            the VO
     * @return the user that belongs to the external user or null if the there is no such user or
     *         the user cannot be synchronized
     */
    private User synchronizeLdapUser(ExternalUserVO userVO) {
        if (userVO == null) {
            return null;
        }
        UserManagement userManagement = ServiceLocator.findService(UserManagement.class);
        User user = null;
        try {
            user = userManagement.updateExternalUser(userVO);
        } catch (Exception e) {
            LOGGER.error("Error on synchronizing LDAP record with database", e);
        }
        return user;
    }

}
