package com.communote.server.core.vo.query.tag;

import java.util.HashSet;
import java.util.Set;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.communote.common.util.PageableList;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.tag.TagData;
import com.communote.server.api.core.tag.TagStoreType;
import com.communote.server.api.core.tag.TagTO;
import com.communote.server.core.query.QueryManagement;
import com.communote.server.core.tag.TagStoreManagement;
import com.communote.server.core.vo.query.blog.TopicAccessLevel;
import com.communote.server.model.user.User;
import com.communote.server.persistence.tag.DefaultTagStore;
import com.communote.server.test.CommunoteIntegrationTest;
import com.communote.server.test.util.AuthenticationTestUtils;
import com.communote.server.test.util.TestUtils;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class BlogTagQueryDefinitionTestForTags extends CommunoteIntegrationTest {
    private QueryManagement queryManagement;
    private TagStoreManagement tagStoreManagement;

    /**
     * Setup.
     */
    @BeforeClass(dependsOnGroups = "integration-test-setup")
    public void setup() {
        queryManagement = ServiceLocator.instance().getService(QueryManagement.class);
        tagStoreManagement = ServiceLocator.instance().getService(TagStoreManagement.class);
    }

    /**
     * Tests for the possibility to filter for tags from different TagStores.
     *
     * @throws Exception
     *             in case the test failed
     */
    @Test
    public void testForDifferentTagStoresAliases() throws Exception {
        User user = TestUtils.createRandomUser(false);
        DefaultTagStore myTagStore = new DefaultTagStore(random().toLowerCase(),
                TagStoreType.Types.BLOG);
        tagStoreManagement.addTagStore(myTagStore);
        TagTO defaultTag = new TagTO(random(), TagStoreType.Types.BLOG.getDefaultTagStoreId());
        TagTO myTag = new TagTO(random(), myTagStore.getTagStoreId());
        Set<TagTO> tags = new HashSet<TagTO>();
        tags.add(defaultTag);
        TestUtils.createRandomBlog(false, false, null, tags, user);
        tags.add(myTag);
        TestUtils.createRandomBlog(false, false, null, tags, user);
        tags.remove(defaultTag);
        TestUtils.createRandomBlog(false, false, null, tags, user);
        BlogTagQuery query = new BlogTagQuery();
        BlogTagQueryParameters queryParameters = new BlogTagQueryParameters();
        queryParameters.setUserId(user.getId());
        queryParameters.setAccessLevel(TopicAccessLevel.MANAGER);
        AuthenticationTestUtils.setSecurityContext(user);
        PageableList<?> result = queryManagement.query(query, queryParameters);
        Assert.assertEquals(result.getMinNumberOfElements(), 2);
        queryParameters.getTagStoreAliases().add(TagStoreType.Types.BLOG.getDefaultTagStoreId());
        result = queryManagement.query(query, queryParameters);
        Assert.assertEquals(result.getMinNumberOfElements(), 1);
        Assert.assertEquals(((TagData) result.get(0)).getName(), defaultTag.getName());
        queryParameters.getTagStoreAliases().add(myTagStore.getTagStoreId());
        result = queryManagement.query(query, queryParameters);
        Assert.assertEquals(result.getMinNumberOfElements(), 2);
        queryParameters.getTagStoreAliases().remove(TagStoreType.Types.BLOG.getDefaultTagStoreId());
        result = queryManagement.query(query, queryParameters);
        Assert.assertEquals(result.getMinNumberOfElements(), 1);
        Assert.assertEquals(((TagData) result.get(0)).getName(), myTag.getName());
    }
}
