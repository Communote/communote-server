package com.communote.server.core.security.authentication.database;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.communote.common.encryption.HashCodeGenerator;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.config.ClientConfigurationProperties;
import com.communote.server.api.core.config.type.ClientPropertySecurity;
import com.communote.server.api.core.user.UserVO;
import com.communote.server.core.ConfigurationManagement;
import com.communote.server.core.security.UserDetails;
import com.communote.server.core.security.authentication.BaseCommunoteAuthenticationProvider;
import com.communote.server.core.user.UserManagement;
import com.communote.server.external.acegi.AuthAgainstInternalDBWhileExternalUserAccountException;
import com.communote.server.external.acegi.UserAccountNotActivatedException;
import com.communote.server.model.user.ExternalUserAuthentication;
import com.communote.server.model.user.User;
import com.communote.server.model.user.UserStatus;
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
     * Asserts, that the user is not a member of an activated external system.
     *
     * TODO to be done in user service
     *
     * @param user
     *            The users id.
     */
    private void assertExternalSystem(User user) {
        ClientConfigurationProperties props = CommunoteRuntime.getInstance()
                .getConfigurationManager().getClientConfigurationProperties();
        String primaryExternalAuthentication = props.getPrimaryExternalAuthentication();
        if (primaryExternalAuthentication == null) {
            return;
        }
        if (!props.isDBAuthenticationAllowed()) {
            throw new AuthAgainstInternalDBWhileExternalUserAccountException(
                    "Authentication agaings the internal db is deactivated for the external system.",
                    user.getAlias(), primaryExternalAuthentication);
        }
        Set<ExternalUserAuthentication> externalAuthentications = ServiceLocator.instance()
                .getService(UserManagement.class)
                .getExternalExternalUserAuthentications(user.getId());
        for (ExternalUserAuthentication externalAuthentication : externalAuthentications) {
            if (externalAuthentication.getSystemId().equals(primaryExternalAuthentication)) {
                throw new AuthAgainstInternalDBWhileExternalUserAccountException(
                        "The user can't be authenticated against the internl db,"
                                + " as an external system is activated the user has a configuration for.",
                        user.getAlias(), primaryExternalAuthentication);
            }
        }
    }

    /**
     * Asserts the users passwords for correctness.
     *
     * @param username
     *            The users name.
     * @param userPassword
     *            The users password
     * @param providedPassword
     *            The provided password.
     */
    private void assertPasswords(String username, String userPassword, String providedPassword) {
        if (StringUtils.isEmpty(userPassword)) {
            throw new BadCredentialsException("The user password cannot be empty.");
        }
        if (StringUtils.isEmpty(providedPassword)) {
            throw new BadCredentialsException("The provided password cannot be empty.");
        }
        if (!StringUtils.equals(userPassword, providedPassword)) {
            // in case it was not a plain text password try the md5 one
            if (!StringUtils.equals(userPassword,
                    HashCodeGenerator.generateMD5HashCode(providedPassword))) {
                throw new BadCredentialsException("Authentication failed!");
            }
        }
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

        /**
         * we still use the user management here since this authentication provider is forced only
         * to lookup into the internal database.
         *
         * actually the best thing to do is:
         *
         * 1st move the sharepoint and confluenceauthenticators to their plugins <br>
         * 2nd let the sharepoint and confluence authentication providers only handle their token
         * (not the username and password one) <br>
         * 3rd let this provider be the one who handles the username and password and uses the user
         * services who when takes of checking database or external systems
         */

        User user = ServiceLocator.instance().getService(UserManagement.class)
                .findUserByEmailAlias(username);
        if (user == null) {
            throw new UsernameNotFoundException(username);
        }
        if (user.getStatus() == UserStatus.INVITED || user.getStatus() == UserStatus.REGISTERED) {
            throw new UserAccountNotActivatedException(username, username);
        }
        String userPassword = user.getPassword();
        String providedPassword = authentication.getCredentials() == null ? null : authentication
                .getCredentials().toString();
        try {
            assertExternalSystem(user);
        } catch (AuthAgainstInternalDBWhileExternalUserAccountException e) {
            LOG.debug(e.getMessage());
            return null;
        }
        assertPasswords(username, userPassword, providedPassword);
        return new UserDetails(user, username);
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
        return (StringUtils.isBlank(properties.getPrimaryExternalAuthentication()) || properties
                .getProperty(ClientPropertySecurity.ALLOW_DB_AUTH_ON_EXTERNAL,
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
