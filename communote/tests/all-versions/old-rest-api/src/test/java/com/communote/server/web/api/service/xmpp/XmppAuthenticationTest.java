package com.communote.server.web.api.service.xmpp;

import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpStatus;
import org.testng.annotations.Test;

import com.communote.server.web.api.service.AbstractApiTest;

/**
 * Tests the XMPP authentication filter.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class XmppAuthenticationTest extends AbstractApiTest {

    private static final String XMPP_AUTH_USERNAME_PARAM = "username";
    private static final String XMPP_AUTH_PWD_PARAM = "password";
    private static final String BASE_URL = "http://localhost:8080/microblog/global";
    // TODO the JID should look something like this communote.global.suffix:8080@host thus the user
    // suffix must be configurable
    private static final String XMPP_JID = "communote";

    /**
     * Tests whether the filter correctly checks the syntax of the request by omitting the password
     * parameter.
     *
     * @throws Exception
     *             if the test failed
     */
    @Test
    public void testBadRequest() throws Exception {
        Map<String, String> params = new HashMap<String, String>();
        params.put(XMPP_AUTH_USERNAME_PARAM, XMPP_JID);
        doPostRequest(BASE_URL + "/api/" + getApiVersion() + "/local/xmpp.do", null, null, params,
                HttpStatus.SC_BAD_REQUEST);
    }

    /**
     * Tests a successful authentication against the XMPP authentication processing filter.
     *
     * @throws Exception
     *             in case the test failed
     */
    @Test
    public void testSuccessfulAuthentication() throws Exception {
        Map<String, String> params = new HashMap<String, String>();
        params.put(XMPP_AUTH_USERNAME_PARAM, XMPP_JID);
        params.put(XMPP_AUTH_PWD_PARAM, getDefaultPassword());
        doPostRequest(BASE_URL + "/api/" + getApiVersion() + "/local/xmpp.do", null, null, params,
                HttpStatus.SC_OK);
    }

    /**
     * Tests an authentication against the XMPP authentication processing filter with existing user
     * and wrong password.
     *
     * @throws Exception
     *             in case the test failed
     */
    @Test
    public void testUnauthorizedAuthentication() throws Exception {
        Map<String, String> params = new HashMap<String, String>();
        params.put(XMPP_AUTH_USERNAME_PARAM, XMPP_JID);
        params.put(XMPP_AUTH_PWD_PARAM, "dfsdfewrWERSDFCXXVewRWERsdf$SDFEWRX_SDF");
        doPostRequest(BASE_URL + "/api/" + getApiVersion() + "/local/xmpp.do", null, null, params,
                HttpStatus.SC_UNAUTHORIZED);
    }
}
