package com.communote.server.web.api.service.filter;

import org.codehaus.jackson.node.ArrayNode;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.communote.server.web.api.service.AbstractApiTest;

/**
 * Test to check the filtering for posts api
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class PostFilterApiTest extends AbstractApiTest {

    /**
     * @throws Exception
     *             in case something goes wrong
     */
    @Test
    public void filterPosts() throws Exception {

        ArrayNode result = doGetRequestAsJSONArray("/filter/posts.json?maxCount=2",
                getDefaultUsername(), getDefaultPassword());
        Assert.assertEquals(result.size(), 2, "Result length must match!");

        for (int i = 0; i < result.size(); i++) {
            checkJSONDetailPostListItemResult(result.get(i), "result[" + i + "].post");
        }

    }

}
