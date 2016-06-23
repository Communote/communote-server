package com.communote.server.web.api.service.blog;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ArrayNode;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.communote.server.web.api.service.AbstractApiTest;

/**
 * Test to retrieve blogs
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class BlogApiTest extends AbstractApiTest {

    private final static Logger LOG = Logger.getLogger(BlogApiTest.class);

    private final String username = getDefaultUsername();
    private final String password = getDefaultPassword();

    private final String searchString = "headway";

    /**
     * Check the blog id against the parameters
     *
     * @param blogId
     *            the blog id
     * @param parameters
     *            the parameters
     * @throws Exception
     *             in case of an error
     */
    private void checkBlog(Long blogId, Map<String, String> parameters) throws Exception {
        JsonNode blog = getBlog(blogId);

        Assert.assertEquals(blog.get("title").asText(), parameters.get("title"),
                "title does not match");
        Assert.assertEquals(blog.get("nameIdentifier").asText(), parameters.get("nameIdentifier"),
                "name identifier does not match");
        Assert.assertEquals(blog.get("description").asText(), parameters.get("description"),
                "description does not match");

    }

    /**
     * @throws Exception
     *             in case of an error
     */
    @Test()
    public void getBlog() throws Exception {
        Long blogId = getManageableBlogId(username, password, 0);
        JsonNode blog = doGetRequestAsJSONObject("/blogs/" + blogId + ".json", username, password);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Checking blog=" + blog);
        }
        checkJSONUserDetailBlogResult(blog, "blog");
    }

    /**
     * @throws Exception
     *             in case of an error
     */
    @Test()
    public void getLastModificatedBlogs() throws Exception {

        ArrayNode array = doGetRequestAsJSONArray(
                "/blogs.json?blogListType=LAST_MODIFIED&maxResults=10&lastModificationDate=0",
                username, password);

        for (int i = 0; i < array.size(); i++) {
            JsonNode blog = array.get(i);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Checking blog=" + blog);
            }
            checkJSONBlogResult(blog, "blog[" + i + "]");
        }

        JsonNode object = array.get(array.size() - 1);
        long date = object.get("lastModificationDate").asLong();

        ArrayNode array2 = doGetRequestAsJSONArray(
                "/blogs.json?blogListType=LAST_MODIFIED&maxResults=10&lastModificationDate=" + date,
                username, password);

        for (int i = 0; i < array2.size(); i++) {
            JsonNode blog = array2.get(i);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Checking blog=" + blog);
            }
            checkJSONBlogResult(blog, "blog[" + i + "]");
        }
    }

    /**
     * @throws Exception
     *             in case of an error
     */
    @Test()
    public void getManagerBlogs() throws Exception {

        ArrayNode array = doGetRequestAsJSONArray("/blogs.json?blogListType=MANAGER", username,
                password);

        for (int i = 0; i < array.size(); i++) {
            JsonNode blog = array.get(i);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Checking blog=" + blog);
            }
            checkJSONBlogResult(blog, "blog[" + i + "]");
        }
    }

    /**
     * @throws Exception
     *             in case of an error
     */
    @Test(dependsOnMethods = { "testCreateEditBlog" })
    public void getReadableBlogs() throws Exception {

        ArrayNode array = doGetRequestAsJSONArray("/blogs.json?blogListType=READ&searchString="
                + searchString, username, password);

        Assert.assertTrue(array.size() >= 1,
                "The array must have at least one blog with the search string! array=" + array);
        for (int i = 0; i < array.size(); i++) {
            JsonNode blog = array.get(i);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Checking blog=" + blog);
            }
            checkJSONBlogResult(blog, "blog[" + i + "]");
            String title = blog.get("title").asText();
            Assert.assertTrue(title.contains(searchString),
                    "Blog title must contain searchString! title=" + title + " searchString="
                            + searchString);
        }

    }

    /**
     * Set the blog rights and check them
     *
     * @param blogId
     *            the blog id
     * @throws Exception
     *             in case of an error
     */
    private void setBlogRights(Long blogId) throws Exception {
        Map<String, String> rightParameters = new HashMap<String, String>();
        rightParameters.put("allCanRead", Boolean.TRUE.toString());
        rightParameters.put("allCanWrite", Boolean.TRUE.toString());
        rightParameters.put("blogId", blogId.toString());

        JsonNode resultRights = doApiPostRequest("/blogRights.json", getDefaultUsername(),
                getDefaultPassword(), rightParameters);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Result of creating a blog=" + resultRights);
        }

        JsonNode blog = getBlog(blogId);

        Assert.assertEquals(blog.get("allCanRead").asBoolean(), true, "allCanRead does not match");
        Assert.assertEquals(blog.get("allCanWrite").asBoolean(), true, "allCanWrite does not match");
    }

    /**
     * @throws Exception
     *             in case of an error
     */
    public void setBlogRoles() throws Exception {
        // TODO
    }

    /**
     * @throws Exception
     *             in case of an error
     */
    @Test
    public void testCreateEditBlog() throws Exception {
        Map<String, String> parameters = new HashMap<String, String>();

        parameters.put("title", "Api Test Blog " + searchString);
        parameters.put("description", "This is an test blog created by the api tests.");
        parameters.put("tags", "apiblogtag0, apiblogtag1, apiblogtag2");
        parameters.put("nameIdentifier", "random_id_" + System.currentTimeMillis());

        JsonNode result = doApiPostRequest("/blogs.json", getDefaultUsername(),
                getDefaultPassword(), parameters);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Result of creating a blog=" + result);
        }

        Assert.assertNotNull(result.get("result"), "blogId should not be null!");
        long blogId = result.get("result").asLong();
        Assert.assertTrue(blogId > 0, "blogId should be greater 0! blogId=" + blogId);

        checkBlog(blogId, parameters);

        // edit the blog
        parameters.put("description",
                "This is an test blog created by the api tests and it has been updated.");

        result = doApiPostRequest("/blogs/" + blogId + ".json", getDefaultUsername(),
                getDefaultPassword(), parameters);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Result of creating a blog=" + result);
        }

        checkBlog(blogId, parameters);

        setBlogRights(blogId);
    }

    /**
     * @throws Exception
     *             in case of an error
     */
    @Test
    public void testFailInviteUser() throws Exception {
        Map<String, String> parameters = new HashMap<String, String>();

        Long blogId = getManageableBlogId(username, password, 0);

        String alias = "" + new Date().getTime();

        parameters.put("blogId", blogId.toString());
        parameters.put("role", "MEMBER");
        parameters.put("alias", alias);
        parameters.put("languageCode", "de");

        JsonNode result = doApiPostRequest("/blogInviteUser.json", username, password, parameters,
                "ERROR");

        Assert.assertTrue(result.get("result").isArray());
        ArrayNode errorFields = (ArrayNode) result.get("result");

        List<String> missingFields = new ArrayList<String>();
        missingFields.add("firstName");
        missingFields.add("lastName");
        missingFields.add("email");

        for (int i = 0; i < errorFields.size(); i++) {
            JsonNode errorField = errorFields.get(i);
            String fieldName = errorField.get("name").asText();
            String message = errorField.get("message").asText();
            String newValue = errorField.get("message").asText();

            if (!missingFields.contains(fieldName)) {
                Assert.fail("FieldName has error but should not! fieldName=" + fieldName
                        + " message=" + message + " newValue=" + newValue);
            }
            missingFields.remove(fieldName);
        }

        if (missingFields.size() > 0) {
            Assert.fail("There are missing fields that have not be returned as an error! Example fieldName="
                    + missingFields.iterator().next());
        }
    }

    /**
     * @throws Exception
     *             in case of an error
     */
    @Test
    public void testInviteUser() throws Exception {
        Map<String, String> parameters = new HashMap<String, String>();

        Long blogId = getManageableBlogId(username, password, 0);

        String alias = "user" + new Date().getTime();
        String email = alias + "@localhost";

        parameters.put("blogId", blogId.toString());
        parameters.put("role", "MEMBER");
        parameters.put("alias", alias);
        parameters.put("firstName", alias);
        parameters.put("lastName", alias);
        parameters.put("email", email);
        parameters.put("languageCode", "de");

        doApiPostRequest("/blogInviteUser.json", username, password, parameters);
    }
}
