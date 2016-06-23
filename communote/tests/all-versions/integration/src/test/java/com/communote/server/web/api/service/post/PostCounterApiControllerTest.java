package com.communote.server.web.api.service.post;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockHttpServletRequest;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.tag.TagStoreType;
import com.communote.server.api.core.tag.TagTO;
import com.communote.server.core.tag.TagManagement;
import com.communote.server.model.blog.Blog;
import com.communote.server.model.user.User;
import com.communote.server.test.CommunoteIntegrationTest;
import com.communote.server.test.util.AuthenticationTestUtils;
import com.communote.server.test.util.TestUtils;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class PostCounterApiControllerTest extends CommunoteIntegrationTest {
    private static final Logger LOGGER = LoggerFactory
            .getLogger(PostCounterApiControllerTest.class);

    /**
     * Regression test for KENMEI-4617: the query counting the new notes returns that there are new
     * notes although there are none. This only occurs after filtering for a tag which is assigned
     * to several notes and one of these notes (on the first page) also has another tag. Further,
     * paging was used to show the next result set.
     *
     * @throws Exception
     *             in case the test failed
     */
    @Test
    public void testForKENMEI4617() throws Exception {
        User user = TestUtils.createRandomUser(false);
        Blog blog = TestUtils.createRandomBlog(false, false, user);
        String tag1 = random();
        String tag2 = random();
        Long tag1Id = ServiceLocator.instance().getService(TagManagement.class)
                .storeTag(new TagTO(tag1, TagStoreType.Types.NOTE)).getId();
        Integer maxCountAndOffset = 15;
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("offset", "0");
        request.setParameter("maxCount", maxCountAndOffset.toString());
        request.setParameter("pagingInterval", "5");
        request.setParameter("listViewType", "long");
        request.setParameter("tagIds", tag1Id.toString());
        LOGGER.debug("Testing postCount with tag ID {} and topic ID {}", tag1Id, blog.getId());

        // Entry for the second page.
        TestUtils.createAndStoreCommonNote(blog, user.getId(), "Message 0 #" + tag1 + " #" + tag2);
        Long afterFirstEntry = System.currentTimeMillis();
        // note: MySql rounds milliseconds to next second (e.g. 15:40:59.600 becomes 15:41:00, thus
        // if 'before' variable is set to 15:40:59.800 the test would fail)
        sleep(500);
        // Entries for the first page.
        PostCounterApiController controller = new PostCounterApiController();
        for (int i = 1; i <= maxCountAndOffset; i++) {
            Long before = System.currentTimeMillis();
            sleep(1000);
            TestUtils.createAndStoreCommonNote(blog, user.getId(), "Message " + i + " #" + tag1
                    + " #" + tag2);
            sleep(1000);
            AuthenticationTestUtils.setSecurityContext(user);
            Long after = System.currentTimeMillis();
            request.setParameter("startDate", before.toString());
            Map<String, Long> count = controller.doGet(null, request, null);
            Assert.assertTrue(count instanceof Map);
            Assert.assertEquals(count.get("count"), new Long(1L), "Failed in iteration " + i
                    + " with startDate " + before + ":");
            request.setParameter("startDate", after.toString());
            count = controller.doGet(null, request, null);
            Assert.assertEquals(count.get("count"), new Long(0L));
        }
        request.setParameter("startDate", afterFirstEntry.toString());
        request.setParameter("offset", maxCountAndOffset.toString());
        Map<String, Long> count = controller.doGet(null, request, null);
        Assert.assertEquals(count.get("count"), Long.valueOf(0L));
    }
}
