package com.communote.server.core.security.authentication.database;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.config.ClientConfigurationProperties;
import com.communote.server.api.core.config.type.ClientPropertySecurity;
import com.communote.server.api.core.user.UserNotFoundException;
import com.communote.server.api.core.user.UserVO;
import com.communote.server.core.ConfigurationManagement;
import com.communote.server.core.security.AccountNotActivatedException;
import com.communote.server.core.security.AuthenticationManagement;
import com.communote.server.core.security.UserDetails;
import com.communote.server.core.security.authentication.AuthAgainstInternalDBWhileExternalUserAccountException;
import com.communote.server.core.security.authentication.BaseCommunoteAuthenticationProvider;
import com.communote.server.external.acegi.UserAccountNotActivatedException;
import com.communote.server.persistence.user.InvitationField;
import com.communote.server.persistence.user.invitationfields.AliasInvitationField;
import com.communote.server.persistence.user.invitationfields.EmailInvitationField;
import com.communote.server.persistence.user.invitationfields.FirstnameInvitationField;
import com.communote.server.persistence.user.invitationfields.LanguageCodeInvitationField;
import com.communote.server.persistence.user.invitationfields.LastnameInvitationField;

/**
 * Authentication provider that authenticates a user against the local database.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class DatabaseAuthenticationProvider extends BaseCommunoteAuthenticationProvider {

    private static final Logger LOG = LoggerFactory.getLogger(DatabaseAuthenticationProvider.class);

    private final List<InvitationField> invitationFields;

    /**
     * Constructor.
     */
    public DatabaseAuthenticationProvider() {
        List<InvitationField> fields = new ArrayList<InvitationField>();
        fields.add(AliasInvitationField.INSTANCE);
        fields.add(EmailInvitationField.INSTANCE);
        fields.add(FirstnameInvitationField.INSTANCE);
        fields.add(LastnameInvitationField.INSTANCE);
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
     * @return ConfigurationManagement.DEFAULT_DATABASE_ID
     */
    @Override
    public String getIdentifier() {
        return ConfigurationManagement.DEFAULT_DATABASE_ID;
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
    protected UserDetails handleRetrieveUserDetails(Authentication auth)
            throws AuthenticationException {
        UsernamePasswordAuthenticationToken authentication = (UsernamePasswordAuthenticationToken) auth;

        String username = authentication.getName();
        if (StringUtils.isBlank(username)) {
            throw new BadCredentialsException("Empty username!");
        }
        username = username.trim();
        LOG.debug("Attempting database authentication of user '{}'", username);

        String providedPassword = authentication.getCredentials() == null ? null
                : authentication.getCredentials().toString();
        if (StringUtils.isEmpty(providedPassword)) {
            throw new BadCredentialsException("The provided password cannot be empty.");
        }
        UserDetails userDetails = null;
        try {
            userDetails = ServiceLocator.findService(AuthenticationManagement.class)
                    .checkLocalUserPasswordOnLogin(username, providedPassword);
            if (userDetails == null) {
                throw new BadCredentialsException("Authentication failed");
            }
        } catch (UserNotFoundException e) {
            throw new UsernameNotFoundException(e.getMessage());
        } catch (AccountNotActivatedException e) {
            throw new UserAccountNotActivatedException(e.getMessage(), username);
        } catch (AuthAgainstInternalDBWhileExternalUserAccountException e) {
            LOG.debug(e.getMessage());
        }
        return userDetails;
    }

    /**
     * Querying here means, might there be a user that exists in the universe with these data. Since
     * someone provided the data as such we assume there is such a person.<br>
     * {@inheritDoc}
     */
    @Override
    public UserVO queryUser(Map<InvitationField, String> queryData) throws AuthenticationException {
        UserVO userVO = new UserVO();
        userVO.setFirstName(queryData.get(FirstnameInvitationField.INSTANCE));
        userVO.setLastName(queryData.get(LastnameInvitationField.INSTANCE));
        userVO.setAlias(queryData.get(AliasInvitationField.INSTANCE));
        userVO.setEmail(queryData.get(EmailInvitationField.INSTANCE));
        userVO.setLanguage(new Locale(queryData.get(LanguageCodeInvitationField.INSTANCE)));
        return userVO;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean supports(Class<?> authentication) {
        ClientConfigurationProperties properties = CommunoteRuntime.getInstance()
                .getConfigurationManager().getClientConfigurationProperties();
        return (StringUtils.isBlank(properties.getPrimaryExternalAuthentication())
                || properties.getProperty(ClientPropertySecurity.ALLOW_DB_AUTH_ON_EXTERNAL,
                        ClientPropertySecurity.DEFAULT_ALLOW_DB_AUTH_ON_EXTERNAL))
                && UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean supportsUserQuerying() {
        ClientConfigurationProperties properties = CommunoteRuntime.getInstance()
                .getConfigurationManager().getClientConfigurationProperties();
        return StringUtils.isBlank(properties.getPrimaryExternalAuthentication())
                || properties.getProperty(ClientPropertySecurity.ALLOW_DB_AUTH_ON_EXTERNAL,
                        ClientPropertySecurity.DEFAULT_ALLOW_DB_AUTH_ON_EXTERNAL);
    }
}
