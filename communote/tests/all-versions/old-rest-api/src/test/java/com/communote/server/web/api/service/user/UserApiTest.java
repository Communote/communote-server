package com.communote.server.web.api.service.user;

import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ArrayNode;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.communote.server.web.api.service.AbstractApiTest;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class UserApiTest extends AbstractApiTest {

    private final String username = getDefaultUsername();
    private final String password = getDefaultPassword();

    private final static Logger LOG = Logger.getLogger(UserApiTest.class);

    /**
     * @throws Exception
     *             in case of an error
     */
    @Test
    public void getUser() throws Exception {
        Long userId = getUserId();
        JsonNode user = doGetRequestAsJSONObject("/users/" + userId + ".json", username, password);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Checking user" + user);
        }
        checkJSONUserResult(user, "user");
    }

    /**
     * Get the user id for the user defined by username
     * 
     * @return the user id
     * @throws Exception
     *             in case of an error
     */
    protected Long getUserId() throws Exception {
        JsonNode loginInfo = doGetRequestAsJSONObject("/login.json?loginId=" + username
                + "&password=" + password, username, password);

        Assert.assertTrue(loginInfo.get("userId").isNumber(),
                "User id should not be null on login!");
        return loginInfo.get("userId").asLong();
    }

    /**
     * @throws Exception
     *             in case of an error
     */
    @Test
    public void getUsers() throws Exception {
        ArrayNode array = doGetRequestAsJSONArray("/users.json", username, password);

        Assert.assertTrue(array.size() > 0, "Must have at least on user!");
        for (int i = 0; i < array.size(); i++) {
            JsonNode user = array.get(i);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Checking user=" + user);
            }
            checkJSONUserResult(user, "user[" + i + "]");
        }
    }
}
