package com.communote.server.web.api.service;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonNode;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class PostApiTest extends AbstractApiTest {

    private final static Logger LOG = Logger.getLogger(PostApiTest.class);

    private final String username = getDefaultUsername();
    private final String password = getDefaultPassword();

    /**
     * @throws Exception
     *             in case of an error
     */
    @Test
    public void createAnswerWithDifferentBlogId() throws Exception {
        Map<String, String> parameters = new HashMap<String, String>();
        Long blogId = getManageableBlogId(username, password, 0);

        parameters.put("blogIds", blogId.toString());
        parameters.put("postText", "API create Post Test");
        parameters.put("tags", "apitag0, apitag1, apitag2");
        parameters.put("isHtml", "false");

        JsonNode result = doApiPostRequest("/posts.json", username, password, parameters);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Checking result of creating a post=" + result);
        }

        Long differentBlogId = getManageableBlogId(username, password, 1);
        Assert.assertTrue(!blogId.equals(differentBlogId), "Ids of blogs must be different! ("
                + blogId + "==" + differentBlogId + ")");

        parameters = new HashMap<String, String>();

        parameters.put("blogIds", differentBlogId.toString());
        parameters.put("postText", "API create Comment Post Test with different blog id.");
        parameters.put("tags", "comment0, comment1, comment2");
        Long manageablePostId = getManageablePostId(blogId, username, password);
        parameters.put("parentPostId", manageablePostId.toString());
        parameters.put("isHtml", "false");

        result = doApiPostRequest("/posts.json", username, password, parameters, "ERROR");

        if (LOG.isDebugEnabled()) {
            LOG.debug("Checking result of creating a comment-post into a different blog=" + result);
        }
    }

    /**
     * @throws Exception
     *             in case of an error
     */
    @Test
    public void createPost() throws Exception {
        Map<String, String> parameters = new HashMap<String, String>();
        Long blogId = getManageableBlogId(username, password, 0);

        parameters.put("blogIds", blogId.toString());
        parameters.put("postText", "API create Post Test");
        parameters.put("tags", "apitag0, apitag1, apitag2");
        parameters.put("isHtml", "false");

        JsonNode result = doApiPostRequest("/posts.json", username, password, parameters);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Checking result of creating a post=" + result);
        }

        parameters = new HashMap<String, String>();

        parameters.put("blogIds", blogId.toString());
        parameters.put("postText", "API create Comment Post Test");
        parameters.put("tags", "comment0, comment1, comment2");
        Long manageablePostId = getManageablePostId(blogId, username, password);
        parameters.put("parentPostId", manageablePostId.toString());
        parameters.put("isHtml", "false");

        result = doApiPostRequest("/posts.json", username, password, parameters);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Checking result of creating a comment-post=" + result);
        }
    }

    /**
     * @throws Exception
     *             in case of an error
     */
    @Test(dependsOnMethods = { "createPost" })
    public void deletePost() throws Exception {
        Long blogId = getManageableBlogId(username, password, 0);
        Long postId = getManageablePostId(blogId, username, password);
        JsonNode result = doApiPostRequest("/deletePost/" + postId + ".json", username, password,
                null);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Checking result of deleting post=" + result);
        }
    }

    /**
     * Get a post with html markup
     * 
     * @throws Exception
     *             in case something goes wrong
     */
    @Test
    public void getNonHtmlPost() throws Exception {
        Long postId = getPostId(username, password);
        JsonNode result = doGetRequestAsJSONObject("/posts/" + postId + ".json", username,
                password);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Checking post with id=" + result.get("postId") + " post=" + result);
        }

        checkJSONDetailPostListItemResult(result, "post");

        String text = result.get("text").asText();
        Assert.assertTrue(text.indexOf("<p>") >= 0, "Text contains no html tags: " + text);

    }

    /**
     * @throws Exception
     *             in case something goes wrong
     */
    @Test
    public void getPost() throws Exception {

        Long postId = getPostId(username, password);

        JsonNode result = doGetRequestAsJSONObject("/posts/" + postId + ".json?filterHtml=true",
                username, password);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Checking post with id=" + result.get("postId") + " post=" + result);
        }

        checkJSONDetailPostListItemResult(result, "post");

        String text = result.get("text").asText();
        Assert.assertTrue(text.indexOf("<p>") < 0, "Text contains html tags: " + text);

    }

}
