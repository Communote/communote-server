package com.communote.server.core.security.authentication.confluence;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreProtocolPNames;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ObjectNode;
import org.springframework.security.authentication.AuthenticationServiceException;

import com.communote.server.api.core.config.type.ClientProperty;
import com.communote.server.core.security.authentication.ExternalKenmeiApiAuthenticator;
import com.communote.server.model.config.ConfluenceConfiguration;
import com.communote.server.persistence.user.ExternalUserVO;

/**
 * Authenticator that checks the token and logins against Confluence. *
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ConfluenceAuthenticator extends
        ExternalKenmeiApiAuthenticator<ConfluenceAuthenticationRequest> {

    private static final String CF_PARAM_USERNAME = "username";

    private static final String CF_PARAM_PASSWORD = "password";

    private static final String CF_PARAM_USER_LOOKUP = "userLookup";

    private final ConfluenceConfiguration configuration;

    /**
     * @{inheritDoc
     */
    @Override
    protected boolean isValidJsonUser(ObjectNode jsonUser) {
        JsonNode userFound = jsonUser.get(JSON_PARAM_USER_FOUND);
        return userFound != null && userFound.asBoolean();
    }

    /**
     * @param configuration
     *            the configuration to use
     */
    public ConfluenceAuthenticator(ConfluenceConfiguration configuration) {
        super("confluence", true);
        if (configuration == null) {
            throw new IllegalArgumentException("Configuration cannot be null!");
        }
        if (configuration.getAuthenticationApiUrl() == null) {
            throw new IllegalArgumentException(
                    "configuration#getAuthenticationApiUrl() cannot be null!");
        }
        this.configuration = configuration;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected HttpRequestBase createMethod(ConfluenceAuthenticationRequest authenticationRequest,
            boolean isQuerying) {
        HttpPost method = new HttpPost(configuration.getAuthenticationApiUrl());
        method.getParams().setParameter(CoreProtocolPNames.HTTP_CONTENT_CHARSET, "UTF-8");
        List<BasicNameValuePair> formparams = new ArrayList<BasicNameValuePair>();
        if (isQuerying) {
            formparams.add(new BasicNameValuePair(CF_PARAM_USER_LOOKUP, authenticationRequest
                    .getUsername()));
        } else if (authenticationRequest.getToken() != null) {
            if (authenticationRequest.isSendTokenAsParameter()) {
                formparams.add(new BasicNameValuePair(
                        ClientProperty.GENERIC_AUTHENTICATOR_TOKEN_NAME.getValue("token"),
                        authenticationRequest
                                .getToken()));
            } else {
                method.addHeader("Cookie", "JSESSIONID=" + authenticationRequest.getToken());
            }
        } else {
            formparams.add(new BasicNameValuePair(CF_PARAM_USERNAME, authenticationRequest
                    .getUsername()));
            formparams.add(new BasicNameValuePair(CF_PARAM_PASSWORD, authenticationRequest
                    .getPassword()));
        }
        try {
            method.setEntity(new UrlEncodedFormEntity(formparams, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e); // This should never be thrown
        }
        method.getParams().setBooleanParameter(ClientPNames.HANDLE_AUTHENTICATION, true);
        return method;
    }

    /**
     * Creates a prepared value object.
     * 
     * @return the value object
     */
    @Override
    protected ExternalUserVO createUserVO() {
        ExternalUserVO userVO = prepareDefaultUserVO();
        userVO.setSystemId(configuration.getSystemId());
        return userVO;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void extractUserData(ObjectNode jsonUser, ExternalUserVO userVO)
            throws AuthenticationServiceException {
        assertFieldsExist(jsonUser, JSON_PARAM_LOGIN, JSON_PARAM_EMAIL, JSON_PARAM_LAST_NAME,
                JSON_PARAM_FIRST_NAME);
        userVO.setExternalUserName(jsonUser.get(JSON_PARAM_LOGIN).asText());
        userVO.setEmail(jsonUser.get(JSON_PARAM_EMAIL).asText());
        userVO.setLastName(jsonUser.get(JSON_PARAM_LAST_NAME).asText());
        userVO.setFirstName(jsonUser.get(JSON_PARAM_FIRST_NAME).asText());
        if (jsonUser.has(JSON_PARAM_LANG)) {
            String lang = jsonUser.get(JSON_PARAM_LANG).asText();
            Locale locale = new Locale(lang);
            userVO.setDefaultLanguage(locale);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getAuthenticationApiUrl() {
        return this.configuration.getAuthenticationApiUrl();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateRetrievedUser(ConfluenceAuthenticationRequest confuenceAuthRequest,
            ExternalUserVO userVO) {
        if (confuenceAuthRequest.getToken() == null
                && !StringUtils.equalsIgnoreCase(confuenceAuthRequest.getUsername(), userVO
                        .getExternalUserName())) {
            throw new AuthenticationServiceException(
                    "Response included login not matching request. requestedLogin="
                            + confuenceAuthRequest.getUsername() + " receivedLogin="
                            + userVO.getExternalUserName());
        }
    }

}
