package com.communote.server.core.security.authentication.confluence;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.user.UserVO;
import com.communote.server.core.ConfigurationManagement;
import com.communote.server.core.security.UserDetails;
import com.communote.server.core.security.authentication.ExternalAuthenticationProvider;
import com.communote.server.model.config.ConfluenceConfiguration;
import com.communote.server.persistence.user.ExternalUserVO;
import com.communote.server.persistence.user.InvitationField;
import com.communote.server.persistence.user.invitationfields.ExternalUsernameInvitationField;
import com.communote.server.persistence.user.invitationfields.LanguageCodeInvitationField;

/**
 * Authentication provider that authenticates a user against the user store of Confluence.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ConfluenceAuthenticationProvider extends ExternalAuthenticationProvider {

    private final List<InvitationField> invitationFields;

    /**
     *
     */
    public ConfluenceAuthenticationProvider() {
        List<InvitationField> fields = new ArrayList<InvitationField>();
        fields.add(ExternalUsernameInvitationField.INSTANCE);
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
        AbstractAuthenticationToken auth = null;
        if (authentication instanceof UsernamePasswordAuthenticationToken) {
            auth = new UsernamePasswordAuthenticationToken(details,
                    authentication.getCredentials(), details.getAuthorities());
        } else if (authentication instanceof ConfluenceAuthenticationToken) {
            auth = new ConfluenceAuthenticationToken(details,
                    (String) authentication.getCredentials(), details.getAuthorities());
        }
        if (auth != null) {
            auth.setDetails(authentication.getDetails());
        }
        return auth;
    }

    /**
     * @return the confluence configuration - can be null
     */
    @Override
    protected ConfluenceConfiguration getConfiguration() {
        return CommunoteRuntime.getInstance().getConfigurationManager()
                .getClientConfigurationProperties().getConfluenceConfiguration();
    }

    /**
     * @return ConfigurationManagement.DEFAULT_CONFLUENCE_SYSTEM_ID
     */
    @Override
    public String getIdentifier() {
        return ConfigurationManagement.DEFAULT_CONFLUENCE_SYSTEM_ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<InvitationField> getInvitationFields() {
        return invitationFields;

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserVO queryUser(Map<InvitationField, String> queryData) throws AuthenticationException {
        ConfluenceAuthenticator authenticator = new ConfluenceAuthenticator(getConfiguration());
        String username = queryData.get(ExternalUsernameInvitationField.INSTANCE);
        if (StringUtils.isEmpty(username)) {
            throw new IllegalArgumentException(
                    "Email cannot be empty when querying a confluence user!");
        }
        ConfluenceAuthenticationRequest request = new ConfluenceAuthenticationRequest(username,
                null);

        return authenticator.queryUserData(request);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ExternalUserVO retrieveExternalUser(Authentication authentication)
            throws AuthenticationException {

        ConfluenceConfiguration config = getConfiguration();
        if (config == null || !config.isAllowExternalAuthentication()) {
            throw new AuthenticationServiceException("Invalid or non active configuration! "
                    + (config == null ? "null" : config.attributesToString()));
        }

        ConfluenceAuthenticator authenticator = new ConfluenceAuthenticator(getConfiguration());
        if (authentication instanceof ConfluenceAuthenticationToken) {
            ConfluenceAuthenticationToken confluenceAuthenticationToken = (ConfluenceAuthenticationToken) authentication;
            ConfluenceAuthenticationRequest confluenceAuthenticationRequest = new ConfluenceAuthenticationRequest(
                    confluenceAuthenticationToken.getCredentials());
            confluenceAuthenticationRequest.setSendTokenAsParameter(confluenceAuthenticationToken
                    .isSendTokenAsParameter());
            return authenticator.authenticate(confluenceAuthenticationRequest);
        } else if (authentication instanceof UsernamePasswordAuthenticationToken) {
            UsernamePasswordAuthenticationToken upat = (UsernamePasswordAuthenticationToken) authentication;
            String login = upat.getName();
            if (StringUtils.isBlank(login)) {
                throw new BadCredentialsException("Empty login!");
            }
            String password = String.valueOf(upat.getCredentials());

            ConfluenceAuthenticationRequest confluenceAuthenticationRequest = new ConfluenceAuthenticationRequest(
                    login, password);

            return authenticator.authenticate(confluenceAuthenticationRequest);
        }
        throw new IllegalArgumentException("Only ConfluenceAuthenticationToken and "
                + "UsernamePasswordAuthenticationToken are supported");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean supports(Class authentication) {
        ConfluenceConfiguration configuration = getConfiguration();

        if (configuration != null && configuration.isAllowExternalAuthentication()) {
            // supporting both token-based authentication and username+password-based
            // authentication
            return ConfluenceAuthenticationToken.class.isAssignableFrom(authentication)
                    || (UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication) && configuration
                            .isPrimaryAuthentication());
        }
        return false;
    }

}
