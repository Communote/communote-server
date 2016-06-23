package com.communote.server.web.api.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ArrayNode;
import org.testng.Assert;

import com.communote.server.api.util.JsonHelper;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class AbstractApiTest {

    private static final String API_RESULT_RESULT = "result";

    private static final String API_RESULT_MESSAGE = "message";

    private static final String API_RESULT_STATUS = "status";

    private final static Logger LOG = Logger.getLogger(AbstractApiTest.class);

    private final String protocol = "http";
    private final String host = "localhost";
    private final int port = 8080;
    private final String servlet = "";
    private final String moduleId = "microblog";
    private final String clientId = "global";
    private final String apiPart = "api";
    private final String apiVersion = "v1.0.1";

    private final String defaultUsername = "communote";
    private final String defaultPassword = "123456";

    /**
     * Check if all fields are set correctly for the JSON object representing a blog
     *
     * @param blog
     *            the blog to check
     * @param prefix
     *            the prefix used for logging only
     */
    protected void checkJSONBlogResult(JsonNode blog, String prefix) {
        Assert.assertNotNull(blog, prefix + " should not be null!");
        Assert.assertTrue(blog.isObject());

        Assert.assertNotNull(blog.get("blogId").asText(), "id should not be null!");
        Assert.assertNotNull(blog.get("title").asText(), "title should not be null!");
        Assert.assertNotNull(blog.get("lastModificationDate").asText(),
                "lastModificationDate should not be null!");
    }

    /**
     * Check the detail post list item, does a version check differently based on the version
     *
     * @param post
     *            the post to check
     * @param prefix
     *            the preifx
     */
    protected void checkJSONDetailPostListItemResult(JsonNode post, String prefix) {
        if (getApiVersion().equals("1.0")) {
            checkJSONv10DetailPostListItemResult(post, prefix);
        } else {
            checkJSONv101DetailPostListItemResult(post, prefix);
        }
    }

    /**
     * @param blog
     *            the json object to check
     * @param prefix
     *            the prefix for error messages only
     */
    protected void checkJSONUserDetailBlogResult(JsonNode blog, String prefix) {
        Assert.assertNotNull(blog, prefix + " should not be null!");
        Assert.assertTrue(blog.isObject());
        checkJSONBlogResult(blog, "blog");
        prefix += ".";

        Assert.assertTrue(blog.get("allCanRead").isBoolean(), "allCanRead should not be null!");
        Assert.assertTrue(blog.get("allCanWrite").isBoolean(), "allCanWrite should not be null!");
        Assert.assertNotNull(blog.get("tags"), "tags should not be null!");
        Assert.assertNotNull(blog.get("description"), "description should not be null!");

        Assert.assertNotNull(blog.get("userRole"), "userRole should not be null!");
        Assert.assertNotNull(blog.get("blogEmail"), "blogEmail should not be null!");

        Assert.assertNotNull(blog.get("readingUserIds"), "readingUserIds should not be null!");
        Assert.assertNotNull(blog.get("writingUserIds"), "writingUserIds should not be null!");
        Assert.assertNotNull(blog.get("managingUserIds"), "managingUserIds should not be null!");

    }

    /**
     * Check if all fields are set correctly for the JSON object representing a user
     *
     * @param user
     *            the user to check
     * @param prefix
     *            the prefix used for logging only
     */
    protected void checkJSONUserResult(JsonNode user, String prefix) {
        if (getApiVersion().equals("1.0")) {
            checkJSONv10UserResult(user, prefix);
        } else {
            checkJSONv101UserResult(user, prefix);
        }
    }

    /**
     * Check if all fields are set correctly for the JSON object representing a detailed post
     *
     * @param post
     *            the post to check
     * @param prefix
     *            the prefix used for logging only
     */
    protected void checkJSONv101DetailPostListItemResult(JsonNode post, String prefix) {
        Assert.assertNotNull(post, prefix + " should not be null!");
        Assert.assertTrue(post.isObject());
        prefix += ".";
        Assert.assertTrue(post.get("postId").isNumber(), prefix + "postId should not be null!");
        Assert.assertNotNull(post.get("text").asText(), prefix + "text should not be null!");
        Assert.assertTrue(post.get("blogId").isNumber(), prefix + "blogId should not be null!");
        Assert.assertTrue(post.get("userId").isNumber(), prefix + "userId should not be null!");

        if (post.has("parentUserId") && !post.get("parentUserId").isNull()) {
            Assert.assertTrue(post.get("parentPostId").isNumber(), prefix
                    + "parentPostId should not be null!");
        } else if (post.has("parentPostId") && !post.get("parentPostId").isNull()) {
            Assert.fail("parentPostId=" + post.get("parentPostId")
                    + " hence parentUserId should be set but is null!");
        }

        // TODO date check
        Assert.assertTrue(post.get("creationDate").isNumber(), "creationDate should not be null!");
        // TODO date check
        Assert.assertTrue(post.get("lastModificationDate").isNumber(),
                "lastModificationDate should not be null!");
        // TODO on client side
        // Assert.assertNotNull(result.get("canEdit"), "canEdit should not be null!");
    }

    /**
     * @param user
     *            the user to check
     * @param prefix
     *            the prefix to check
     */
    private void checkJSONv101UserResult(JsonNode user, String prefix) {

        Assert.assertNotNull(user, prefix + " should not be null!");
        Assert.assertTrue(user.isObject());
        prefix += ".";

        Assert.assertTrue(user.get("userId").isNumber(), prefix + "userId should not be null!");
        Assert.assertNotNull(user.get("firstName").asText(), prefix
                + "firstName should not be null!");
        Assert.assertNotNull(user.get("lastName").asText(), prefix + "lastName should not be null!");
        Assert.assertNotNull(user.get("email").asText(), prefix + "email should not be null!");
        Assert.assertNotNull(user.get("salutation"), prefix + "salutation should not be null!");
        Assert.assertNotNull(user.get("alias").asText(), prefix + "alias should not be null!");

        Assert.assertTrue(user.get("lastModificationDate").isNumber(), prefix
                + "lastModifcationDate should not be null!");
        // date can be null
        if (user.has("lastPhotoModificationDate")
                && !user.get("lastPhotoModificationDate").isNull()) {
            Assert.assertTrue(user.get("lastPhotoModificationDate").isNumber(), prefix
                    + "lastPhotoModifcationDate should not be null!");
        }
    }

    /**
     * Check if all fields are set correctly for the JSON object representing a detailed post
     *
     * @param post
     *            the post to check
     * @param prefix
     *            the prefix used for logging only
     */
    protected void checkJSONv10DetailPostListItemResult(JsonNode post, String prefix) {
        Assert.assertNotNull(post, prefix + " should not be null!");
        Assert.assertTrue(post.isObject());
        prefix += ".";
        Assert.assertTrue(post.get("postId").isNumber(), prefix + "postId should not be null!");
        Assert.assertTrue(post.get("text").isTextual(), prefix + "text should not be null!");

        JsonNode blog = post.get("blog");
        Assert.assertTrue(blog.isObject());
        checkJSONBlogResult(blog, prefix + "blog");

        JsonNode user = post.get("user");

        checkJSONUserResult(user, prefix + "user");
        if (!post.get("parentPostAuthor").isNull()) {
            JsonNode parentUser = post.get("parentPostAuthor");
            Assert.assertTrue(post.get("parentPostId").isNumber(),
                    "parentPostId should not be null if parentUser is set!");

            checkJSONUserResult(parentUser, prefix + "parentPostAuthor");
        } else if (post.has("parentPostId") && !post.get("parentPostId").isNull()) {
            Assert.fail("parentPostId=" + post.get("parentPostId")
                    + " hence parentPostUser should be set but is null!");
        }

        Assert.assertTrue(post.get("creationDate").isNumber(), "creationDate should not be null!");
        Assert.assertNotNull(post.get("lastModificationDate").isNumber(),
                "lastModificationDate should not be null!");
        // TODO on client side
        // Assert.assertNotNull(result.get("canEdit"), "canEdit should not be null!");
    }

    /**
     * @param user
     *            the user to check
     * @param prefix
     *            the prefix to check
     */
    private void checkJSONv10UserResult(JsonNode user, String prefix) {
        Assert.assertNotNull(user, prefix + " should not be null!");
        Assert.assertTrue(user.isObject());
        prefix += ".";

        Assert.assertTrue(user.get("userId").isNumber(), prefix + "userId should not be null!");
        Assert.assertNotNull(user.get("firstName").asText(), prefix
                + "firstName should not be null!");
        Assert.assertNotNull(user.get("lastName").asText(), prefix + "lastName should not be null!");
        Assert.assertNotNull(user.get("email").asText(), prefix + "email should not be null!");
        Assert.assertNotNull(user.get("salutation"), prefix + "salutation should not be null!");
        Assert.assertNotNull(user.get("alias").asText(), prefix + "alias should not be null!");
    }

    /**
     * @param username
     *            the username to use, can be null to avoid adding user name and password
     * @param password
     *            the password to use
     * @return the http client configured with username and password
     */
    private HttpClient configureHttpClient(String username, String password) {
        DefaultHttpClient httpClient = new DefaultHttpClient();
        if (username != null) {
            Credentials defaultcreds = new UsernamePasswordCredentials(username, password);
            httpClient.getCredentialsProvider().setCredentials(
                    new AuthScope(host, port, AuthScope.ANY_REALM), defaultcreds);
            HttpClientParams.setAuthenticating(httpClient.getParams(), true);
        }
        HttpClientParams.setRedirecting(httpClient.getParams(), false);
        return httpClient;
    }

    /**
     * Do a get request and return the result of api result object
     *
     * @param apiUri
     *            the uri of the api after the version
     * @param username
     *            username for authentication, if null username and password will be ignored
     * @param password
     *            password for authentication
     * @return the api result (with status, message) as json object
     * @throws Exception
     *             in case of an error
     */
    protected JsonNode doApiGetRequest(String apiUri, String username, String password)
            throws Exception {
        HttpClient httpClient = configureHttpClient(username, password);

        HttpGet httpGet = new HttpGet(getBasicUri() + apiUri);

        HttpResponse response = httpClient.execute(httpGet);
        int statusCode = response.getStatusLine().getStatusCode();
        LOG.debug("Status=" + statusCode + " for uri=" + httpGet.getURI());
        Assert.assertEquals(statusCode, HttpStatus.SC_OK, "Status must be " + HttpStatus.SC_OK);

        String responseString = EntityUtils.toString(response.getEntity());

        if (LOG.isDebugEnabled()) {
            for (Header header : response.getAllHeaders()) {
                LOG.debug(header.getName() + "=" + header.getValue());
            }
        }
        JsonNode json = JsonHelper.getSharedObjectMapper().readTree(responseString);
        String apiStatus = json.get(API_RESULT_STATUS).asText();
        Assert.assertEquals(apiStatus, "OK",
                "Api Result Status must be ok!. message=" + json.get(API_RESULT_MESSAGE));

        if (LOG.isDebugEnabled() && json.get(API_RESULT_MESSAGE).asText() != null) {
            LOG.debug(json.get(API_RESULT_MESSAGE));
        }
        httpClient.getConnectionManager().shutdown();
        return json;

    }

    /**
     * Do an post request to the api. Fails if the api result object does not contain an OK status
     *
     * @param apiUri
     *            the url, relative to the api (starting after .../api/v1.0)
     * @param username
     *            username for authentication, if null username and password will be ignored
     * @param password
     *            the password to use
     * @param parameters
     *            the parameters for the post (can be null or empty)
     * @return the api result object
     * @throws Exception
     *             in case of an error
     */
    protected JsonNode doApiPostRequest(String apiUri, String username, String password,
            Map<String, String> parameters) throws Exception {
        return doApiPostRequest(apiUri, username, password, parameters, "OK");
    }

    /**
     * Do an post request to the api. Fails if the api result object does not contain an OK status
     *
     * @param apiUri
     *            the url, relative to the api (starting after .../api/v1.0)
     * @param username
     *            username for authentication, if null username and password will be ignored
     * @param password
     *            the password to use
     * @param parameters
     *            the parameters for the post (can be null or empty)
     * @param expectedResultStatus
     *            the expected result status (OK, WARNING or ERROR)
     * @return the api result object
     * @throws Exception
     *             in case of an error
     */
    protected JsonNode doApiPostRequest(String apiUri, String username, String password,
            Map<String, String> parameters, String expectedResultStatus) throws Exception {

        String requestUrl = getBasicUri() + apiUri;

        String responseString = doPostRequest(requestUrl, username, password, parameters,
                HttpStatus.SC_OK);

        if (LOG.isDebugEnabled()) {
            LOG.debug("apiUrl= '" + requestUrl + "' responseString= '" + responseString + "'");
        }

        JsonNode json = JsonHelper.getSharedObjectMapper().readTree(responseString);
        String apiStatus = json.get(API_RESULT_STATUS).asText();
        Assert.assertEquals(apiStatus, expectedResultStatus, "Api Result Status must be "
                + expectedResultStatus + "!. message=" + json.get(API_RESULT_MESSAGE));

        if (LOG.isDebugEnabled() && json.has(API_RESULT_MESSAGE)
                && !json.get(API_RESULT_MESSAGE).isNull()) {
            LOG.debug(json.get(API_RESULT_MESSAGE));
        }
        return json;
    }

    /**
     * Do a get request and return the result of api result object
     *
     * @param apiUri
     *            the uri of the api after the version
     * @param username
     *            username for authentication, if null username and password will be ignored
     * @param password
     *            password for authentication
     * @return the result of the call
     * @throws Exception
     *             in case of an error
     */
    protected ArrayNode doGetRequestAsJSONArray(String apiUri, String username, String password)
            throws Exception {

        JsonNode apiResult = doApiGetRequest(apiUri, username, password);
        JsonNode result = apiResult.get(API_RESULT_RESULT);
        if (result.isArray() && result instanceof ArrayNode) {
            return (ArrayNode) result;
        }
        Assert.fail("response is not an array");
        return null;
    }

    /**
     * Do a get request and return the result of api result object
     *
     * @param apiUri
     *            the uri of the api after the version
     * @param username
     *            username for authentication, if null username and password will be ignored
     * @param password
     *            password for authentication
     * @return the result of the call
     * @throws Exception
     *             in case of an error
     */
    protected JsonNode doGetRequestAsJSONObject(String apiUri, String username, String password)
            throws Exception {

        JsonNode apiResult = doApiGetRequest(apiUri, username, password);
        JsonNode result = apiResult.get(API_RESULT_RESULT);

        Assert.assertTrue(result.isObject(), "Result should not be null!");

        return result;
    }

    /**
     *
     * @param requestUrl
     *            the complete request URL
     * @param usernameusername
     *            for authentication, if null username and password will be ignored
     * @param password
     *            the password to use
     * @param parameters
     *            the parameters for the post (can be null or empty)
     * @param expectedStatusCode
     *            the status code the request should have
     * @return the response of the request as string
     * @throws Exception
     *             in case of an error
     */
    protected String doPostRequest(String requestUrl, String username, String password,
            Map<String, String> parameters, int expectedStatusCode) throws Exception {
        HttpClient httpClient = configureHttpClient(username, password);

        HttpPost httpPost = new HttpPost(requestUrl);
        HttpProtocolParams.setContentCharset(httpPost.getParams(), HTTP.UTF_8);

        if (parameters != null) {
            List<NameValuePair> pairs = new ArrayList<NameValuePair>();
            for (Map.Entry<String, String> parameterEntry : parameters.entrySet()) {
                pairs.add(new BasicNameValuePair(parameterEntry.getKey(), parameterEntry.getValue()));
            }
            httpPost.setEntity(new UrlEncodedFormEntity(pairs, HTTP.UTF_8));
        }

        HttpResponse response = httpClient.execute(httpPost);
        int status = response.getStatusLine().getStatusCode();
        if (LOG.isDebugEnabled()) {
            LOG.debug("Status=" + status + " for uri=" + httpPost.getURI());
        }
        if (LOG.isDebugEnabled()) {
            for (Header header : response.getAllHeaders()) {
                LOG.debug(header.getName() + "=" + header.getValue());
            }
        }
        Assert.assertEquals(status, expectedStatusCode, "Status must be " + expectedStatusCode);
        String responseString = EntityUtils.toString(response.getEntity());
        httpClient.getConnectionManager().shutdown();
        return responseString;
    }

    /**
     * @return the apiVersion
     */
    public String getApiVersion() {
        return apiVersion;
    }

    /**
     * @return the base uri for the api calls
     */
    private String getBasicUri() {
        String[] segments = { host + ":" + port, servlet, moduleId, clientId, apiPart, apiVersion };
        return protocol + "://" + StringUtils.join(segments, "/");
    }

    /**
     * Use the api to get the blog
     *
     * @param blogId
     *            the blog id to get
     * @return the json objects of the blog
     * @throws Exception
     *             in case of an error
     */
    protected JsonNode getBlog(Long blogId) throws Exception {
        JsonNode blog = doGetRequestAsJSONObject("/blogs/" + blogId + ".json",
                getDefaultUsername(), getDefaultPassword());

        if (LOG.isDebugEnabled()) {
            LOG.debug("Checking blog=" + blog);
        }
        checkJSONUserDetailBlogResult(blog, "blog");
        return blog;
    }

    /**
     * @return the default password
     */
    public String getDefaultPassword() {
        return defaultPassword;
    }

    /**
     * @return the default username
     */
    public String getDefaultUsername() {
        return defaultUsername;
    }

    /**
     * Uses the api to get an id of a blog that can be managed
     *
     * @param username
     *            the username
     * @param password
     *            the password
     * @param index
     *            index of element to return
     * @return an id of an blog that can be managed by the user
     * @throws Exception
     *             in case of an error
     */
    public Long getManageableBlogId(String username, String password, int index) throws Exception {
        ArrayNode array = doGetRequestAsJSONArray("/blogs.json?blogListType=MANAGER", username,
                password);
        if (array.size() > 0 && index <= (array.size() - 1)) {
            JsonNode blog = array.get(index);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Checking blog=" + blog);
            }
            return blog.get("blogId").asLong();
        }
        throw new Exception("No blogId for management found!");
    }

    /**
     * Uses the api to get an id of a post that can be managed (delete, edit)
     *
     * @param blogId
     *            the blog id to use, must be one of a manageable blog, e.g. retrieved by
     *            {@link #getManageableBlogId(String, String)}
     * @param username
     *            the username
     * @param password
     *            the password
     * @return the post id
     * @throws Exception
     *             in case of an error
     */
    public Long getManageablePostId(Long blogId, String username, String password) throws Exception {
        ArrayNode array = doGetRequestAsJSONArray("/filter/posts.json?blogIds=" + blogId, username,
                password);

        if (array.size() > 0) {
            JsonNode post = array.get(0);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Checking blog=" + post);
            }
            return post.get("postId").asLong();
        }
        throw new Exception("No postId for blogId=" + blogId + " found!");
    }

    /**
     * Get a post id for whatever you want to do with it
     *
     * @param username
     *            the user name to use
     * @param password
     *            the password to use
     * @return the post id
     * @throws Exception
     *             in case something goes wrong
     */
    public Long getPostId(String username, String password) throws Exception {

        ArrayNode result = doGetRequestAsJSONArray("/filter/posts.json?maxCount=1", username,
                password);
        Assert.assertEquals(result.size(), 1, "Result length must match!");

        checkJSONDetailPostListItemResult(result.get(0), "result[" + 0 + "].post");

        return result.get(0).get("postId").asLong();

    }
}
