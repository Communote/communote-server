package com.communote.server.core.security.authentication;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.util.Assert;

import com.communote.server.api.util.JsonHelper;
import com.communote.server.core.security.UnknownHostAuthenticationServiceException;
import com.communote.server.persistence.user.ExternalUserVO;

/**
 * Authenticator that runs against an external api. The api is defined by kenmei.
 *
 * @param <R>
 *            The type of the authentication request the class can handle
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public abstract class ExternalKenmeiApiAuthenticator<R extends AuthenticationRequest> {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(ExternalKenmeiApiAuthenticator.class);

    /**
     * json parameter for the email of the user
     */
    public static final String JSON_PARAM_EMAIL = "email";

    /**
     * json parameter for the last name
     */
    public static final String JSON_PARAM_LAST_NAME = "lastName";

    /**
     * json parameter for the first name
     */
    public static final String JSON_PARAM_FIRST_NAME = "firstName";

    /**
     * json parameter for the combined name
     */
    public static final String JSON_PARAM_NAME = "name";

    /**
     * json parameter for the login
     */
    public static final String JSON_PARAM_LOGIN = "login";

    /**
     * json parameter for the language code
     */
    public static final String JSON_PARAM_LANG = "lang";

    /**
     * json parameter for the user found flag.
     */
    protected static final String JSON_PARAM_USER_FOUND = "userFound";

    private final boolean httpClientReusable;
    private final AbstractHttpClient reusableHttpClient = new DefaultHttpClient();

    /**
     * Name of the authenticator. Used for logging only.
     */
    private final String name;

    /**
     * @param name
     *            name for logging
     * @param httpClientReusable
     *            true if the client can be reused. If true {@link #configureHttpClient(HttpClient)}
     *            is NOT called.
     */
    public ExternalKenmeiApiAuthenticator(String name, boolean httpClientReusable) {
        Assert.hasText(name, "name cannot be empty.");
        this.httpClientReusable = httpClientReusable;
        this.name = name;
    }

    /**
     * Test whether a provided JSON object has all the given members and throw an exception if not.
     *
     * @param jsonNode
     *            the JSON object
     * @param fieldNames
     *            the names of the members the object has to have
     * @throws AuthenticationServiceException
     *             in case one of the fields is missing
     */
    protected void assertFieldsExist(ObjectNode jsonNode, String... fieldNames) {
        for (String fieldName : fieldNames) {
            if (!jsonNode.has(fieldName)) {
                throw new AuthenticationServiceException(
                        "JSON response is missing field " + fieldName);
            }
        }
    }

    /**
     * Does the authentication. If the token is set it will be used, otherwise the email address,
     * password is used.
     *
     * @param authenticationRequest
     *            the context holding all the parameters
     * @return a value object with details about the successfully authenticated user
     * @throws BadCredentialsException
     *             Thrown, when the provided credentials are wrong.
     * @throws AuthenticationServiceException
     *             Thrown, when something else is wrong connecting the server.
     * @throws UnknownHostAuthenticationServiceException
     *             Thrown on any "physical" communication problem with the foreign server.
     */
    public ExternalUserVO authenticate(R authenticationRequest) {
        HttpRequestBase method = createMethod(authenticationRequest, false);
        String jsonResponse;
        try {
            HttpClient httpClient = getHttpClient(authenticationRequest);
            HttpResponse response = httpClient.execute(method);

            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                if (response.getStatusLine().getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
                    throw new BadCredentialsException(
                            "Authentication against " + name + " failed.");
                }
                throw new AuthenticationServiceException("Error contacting " + name + " url="
                        + getAuthenticationApiUrl() + " Unexpected Http Status Code: " + response);
            }
            jsonResponse = EntityUtils.toString(response.getEntity());
            ObjectNode jsonUser = getResponseAsJSON(jsonResponse);

            // check that the user has not been searched
            if (jsonUser.has(JSON_PARAM_USER_FOUND)
                    && jsonUser.get(JSON_PARAM_USER_FOUND).isBoolean()
                    && jsonUser.get(JSON_PARAM_USER_FOUND).asBoolean()) {
                throw new AuthenticationServiceException(name + " returned wrong JSON object.");
            }

            ExternalUserVO userVO = createUserVO();
            extractUserData(jsonUser, userVO);
            validateRetrievedUser(authenticationRequest, userVO);

            if (!httpClientReusable) {
                httpClient.getConnectionManager().shutdown();
            }

            return userVO;
        } catch (JsonProcessingException e) {
            throw new AuthenticationServiceException("Error contacting " + name + " url="
                    + getAuthenticationApiUrl() + " Error parsing JSON response: " + e.getMessage(),
                    e);
        } catch (IOException e) {
            throw new UnknownHostAuthenticationServiceException(
                    "Error contacting " + name + " url=" + getAuthenticationApiUrl(), e);

        }
    }

    /**
     * Hook to be overwritten to configure the http client prior to executing a method. Is only
     * called if {@link #httpClientReusable} is false.
     *
     * @param httpClient
     *            the client to configure
     * @param authenticationRequest
     *            the external auth for the currrent request
     */
    protected void configureHttpClient(AbstractHttpClient httpClient, R authenticationRequest) {
    }

    /**
     * @param authenticationRequest
     *            the authentication request
     * @param isQuerying
     *            true if the request is not an authentication one but to get the user data of a
     *            specific user
     * @return the method to use
     */
    protected abstract HttpRequestBase createMethod(R authenticationRequest, boolean isQuerying);

    /**
     * Creates a prepared value object.
     *
     * @return the value object
     */
    protected abstract ExternalUserVO createUserVO();

    /**
     * Copies user data from a JSON object to a value object.
     *
     * @param jsonUser
     *            the JSON object
     * @param userVO
     *            the value object
     * @throws AuthenticationServiceException
     *             in case the JSON object did not contain the required members
     */
    protected abstract void extractUserData(ObjectNode jsonUser, ExternalUserVO userVO)
            throws AuthenticationServiceException;

    /**
     * @return get the authentication api url
     */
    protected abstract String getAuthenticationApiUrl();

    /**
     * If {@link #reusableHttpClient} is true the instance returned is always the same.
     *
     * @param authenticationRequest
     *            the external auth for the currrent request
     * @return the http client to be used
     */
    private HttpClient getHttpClient(R authenticationRequest) {
        AbstractHttpClient httpClient = httpClientReusable ? reusableHttpClient
                : new DefaultHttpClient();
        httpClient.getParams().setParameter(ClientPNames.COOKIE_POLICY,
                CookiePolicy.IGNORE_COOKIES);
        if (!httpClientReusable) {
            configureHttpClient(httpClient, authenticationRequest);
        }
        return httpClient;
    }

    /**
     * Returns the response body as a JSON object.
     *
     * @param jsonResponse
     *            the response
     * @return the JSON object
     * @throws JsonProcessingException
     *             in case the content is no legal JSON
     * @throws IOException
     *             in case the content is no legal JSON
     */
    private ObjectNode getResponseAsJSON(String jsonResponse)
            throws JsonProcessingException, IOException {
        JsonNode jsonUser = JsonHelper.getSharedObjectMapper().readTree(jsonResponse);
        if (jsonUser instanceof ObjectNode) {
            return (ObjectNode) jsonUser;
        }
        throw new JsonMappingException("Returned response is not a JSON object");
    }

    /**
     * Can be used to validate the received user after receiving it from the foreign system.
     *
     * @param jsonUser
     *            The retrieved user as Json object
     * @return True (default), if the user is valid.
     */
    protected boolean isValidJsonUser(ObjectNode jsonUser) {
        return true;
    }

    /**
     * Creates a prepared value object with all update options set to true.
     *
     * @return the value object
     */
    protected ExternalUserVO prepareDefaultUserVO() {
        ExternalUserVO userVO = new ExternalUserVO();
        userVO.setUpdateEmail(true);
        userVO.setUpdateFirstName(true);
        userVO.setUpdateLanguage(true);
        userVO.setUpdateLastName(true);
        userVO.setClearPassword(false);
        return userVO;
    }

    /**
     * Queries the external system for data about a user
     *
     * @param authenticationRequest
     *            the authentication request
     * @return the data of the user (firstname, lastname and email) or null if not found
     */
    public ExternalUserVO queryUserData(R authenticationRequest) {
        HttpRequestBase method = createMethod(authenticationRequest, true);

        ExternalUserVO userVO = null;
        String jsonResponse = null;
        try {
            HttpClient httpClient = getHttpClient(authenticationRequest);
            HttpResponse response = httpClient.execute(method);

            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                jsonResponse = EntityUtils.toString(response.getEntity());
                ObjectNode jsonUser = getResponseAsJSON(jsonResponse);
                if (!isValidJsonUser(jsonUser)) {
                    LOGGER.debug("Didn't received a valid user for {}: {}", name, jsonResponse);
                    return null;
                }
                userVO = createUserVO();
                extractUserData(jsonUser, userVO);
                userVO.setPassword(null);
                userVO.setClearPassword(false);
            } else {
                LOGGER.info("Error contacting " + name + " url=" + getAuthenticationApiUrl()
                        + " status=" + response);
            }

            if (!httpClientReusable) {
                httpClient.getConnectionManager().shutdown();
            }
        } catch (JsonProcessingException | AuthenticationServiceException e) {
            LOGGER.debug("Error evaluating {} JSON response {}", name, jsonResponse, e);
        } catch (IOException e) {
            LOGGER.debug("Error contacting {} url= {}", name, getAuthenticationApiUrl(), e);
        }
        return userVO;
    }

    /**
     * Method is called after an authentication call has been made. Validate the user here and
     * thrown an exception if the user is not valid.
     *
     * @param authenticationRequest
     *            the request
     * @param retrievedVO
     *            the retrieved user
     */
    protected abstract void validateRetrievedUser(R authenticationRequest,
            ExternalUserVO retrievedVO);

}
