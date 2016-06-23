package com.communote.server.core.vo.query.blog;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.communote.common.util.PageableList;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.blog.BlogData;
import com.communote.server.api.core.config.ConfigurationManager;
import com.communote.server.api.core.config.type.ClientProperty;
import com.communote.server.api.core.property.PropertyManagement;
import com.communote.server.api.core.property.PropertyType;
import com.communote.server.api.core.tag.TagStoreType;
import com.communote.server.api.core.tag.TagTO;
import com.communote.server.core.external.ExternalObjectManagement;
import com.communote.server.core.filter.ResultSpecification;
import com.communote.server.core.filter.listitems.blog.BlogTagListItem;
import com.communote.server.core.query.QueryManagement;
import com.communote.server.core.security.SecurityHelper;
import com.communote.server.core.tag.TagManagement;
import com.communote.server.core.tag.TagStoreManagement;
import com.communote.server.core.vo.query.Query;
import com.communote.server.core.vo.query.converters.BlogDataToBlogTagListItemQueryResultConverter;
import com.communote.server.core.vo.query.filter.PropertyFilter;
import com.communote.server.core.vo.query.filter.PropertyFilter.MatchMode;
import com.communote.server.model.blog.Blog;
import com.communote.server.model.external.ExternalObject;
import com.communote.server.model.tag.Tag;
import com.communote.server.model.user.User;
import com.communote.server.persistence.blog.BlogDao;
import com.communote.server.persistence.tag.DefaultTagStore;
import com.communote.server.persistence.tag.TagStore;
import com.communote.server.service.TopicHierarchyService;
import com.communote.server.test.CommunoteIntegrationTest;
import com.communote.server.test.external.MockExternalObjectSource;
import com.communote.server.test.util.AuthenticationTestUtils;
import com.communote.server.test.util.TestUtils;

/**
 * This contains tests for {@link BlogQuery}
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class BlogQueryTest extends CommunoteIntegrationTest {

    private final static Query<BlogData, BlogQueryParameters> QUERY = new BlogQuery<BlogData, BlogQueryParameters>(
            BlogData.class);
    @Autowired
    private BlogDao topicDao;
    @Autowired
    private QueryManagement queryManagement;
    @Autowired
    private TagManagement tagManagement;
    @Autowired
    private TagStoreManagement tagStoreManagement;
    @Autowired
    private TopicHierarchyService topicHierarchyService;

    @Autowired
    private PropertyManagement propertyManagement;

    @Autowired
    private ExternalObjectManagement externalObjectManagement;

    private MockExternalObjectSource externalObjectSource;

    @AfterClass
    public void cleanup() {
        ServiceLocator.findService(ExternalObjectManagement.class).unregisterExternalObjectSource(
                externalObjectSource);
    }

    /**
     * Method to convert to a list of Long.
     *
     * @param topics
     *            The topics to convert.
     * @return The converted list.
     */
    private List<Long> convertTopics(PageableList<BlogData> topics) {
        List<Long> result = new ArrayList<Long>();
        for (BlogData topic : topics) {
            result.add(topic.getId());
        }
        return result;
    }

    /**
     * @return A BlogQueryInstance with nothing configured.
     */
    private BlogQueryParameters creatBlogQueryParameters() {
        BlogQueryParameters blogQueryParameters = new BlogQueryParameters();
        blogQueryParameters.setResultSpecification(new ResultSpecification());
        blogQueryParameters.setUserId(SecurityHelper.assertCurrentUserId());
        blogQueryParameters.setAccessLevel(TopicAccessLevel.READ);
        return blogQueryParameters;
    }

    /**
     * Setups all common parameters for this tests.
     *
     * @throws Exception
     *             Exception.
     */
    @BeforeClass(dependsOnGroups = "integration-test-setup")
    public void setup() throws Exception {
        User randomUser = TestUtils.createRandomUser(false);
        TestUtils.createRandomBlog(false, false, randomUser);

        externalObjectSource = TestUtils.createNewExternalObjectSource(true);
    }

    /**
     * Test the blog filters with an external id
     *
     * @throws Exception
     *             in case the test failed
     *
     */
    @Test
    public void testFilterWithExternalIds() throws Exception {
        final BlogDataToBlogTagListItemQueryResultConverter converter = new BlogDataToBlogTagListItemQueryResultConverter(
                Locale.ENGLISH);
        final BlogQuery<BlogData, BlogQueryParameters> blogQuery = new BlogQuery<>(BlogData.class);
        List<BlogTagListItem> blogs;

        User user = TestUtils.createRandomUser(false);
        Blog blog1 = TestUtils.createRandomBlog(true, true, user);
        Blog blog2 = TestUtils.createRandomBlog(true, true, user);
        TestUtils.createRandomBlog(true, true, user);

        String externalObjectId1 = UUID.randomUUID().toString();
        String externalObjectId2 = UUID.randomUUID().toString();

        externalObjectManagement.assignExternalObject(blog1.getId(), ExternalObject.Factory
                .newInstance(externalObjectSource.getIdentifier(), externalObjectId1));
        externalObjectManagement.assignExternalObject(blog2.getId(), ExternalObject.Factory
                .newInstance(externalObjectSource.getIdentifier(), externalObjectId2));

        // first: filter for all topics with given external system id which should return blog1 and
        // blog2
        BlogQueryParameters params = new BlogQueryParameters();
        params.setResultSpecification(new ResultSpecification(0, 100));
        params.setExternalObjectSystemId(externalObjectSource.getIdentifier());

        blogs = queryManagement.query(blogQuery, params, converter);

        Assert.assertEquals(blogs.size(), 2);

        Set<Long> blogIds = new HashSet<>();
        for (BlogData u : blogs) {
            blogIds.add(u.getId());
        }
        Assert.assertTrue(blogIds.contains(blog1.getId()));
        Assert.assertTrue(blogIds.contains(blog2.getId()));

        // second: filter for all topics with given external system id and object id which should
        // return blog1 only
        params.setExternalObjectSystemId(externalObjectSource.getIdentifier());
        params.setExternalObjectId(externalObjectId1);

        blogs = this.queryManagement.query(blogQuery, params, converter);
        Assert.assertEquals(blogs.size(), 1);

        Assert.assertEquals(blogs.get(0).getId(), blog1.getId());
    }

    /**
     * Test that it is possible to correctly filter for parent topics. Only direct children the user
     * is allowed to access should be returned.
     *
     * @throws Exception
     *             in case the test failed
     */
    @Test
    public void testForParentTopicIds() throws Exception {
        User user1 = TestUtils.createRandomUser(false);
        User user2 = TestUtils.createRandomUser(false);
        Blog topic_1 = TestUtils.createRandomBlog(false, false, user1, user2);
        Blog topic_1_1 = TestUtils.createRandomBlog(false, false, user1, user2);
        Blog topic_1_1_1 = TestUtils.createRandomBlog(false, false, user1, user2);
        Blog topic_1_1_2 = TestUtils.createRandomBlog(false, false, user1, user2);
        Blog topic_1_2 = TestUtils.createRandomBlog(false, false, user1);

        topicHierarchyService.addTopic(topic_1.getId(), topic_1_1.getId());
        topicHierarchyService.addTopic(topic_1.getId(), topic_1_2.getId());
        topicHierarchyService.addTopic(topic_1_1.getId(), topic_1_1_1.getId());
        topicHierarchyService.addTopic(topic_1_1.getId(), topic_1_1_2.getId());

        // User 1 can see all topics.
        AuthenticationTestUtils.setSecurityContext(user1);

        BlogQueryParameters parameters = creatBlogQueryParameters();
        parameters.setParentTopicIds(new Long[] { topic_1.getId() });
        List<Long> result = convertTopics(queryManagement.query(QUERY, parameters));
        Assert.assertEquals(result.size(), 2);
        Assert.assertTrue(result.contains(topic_1_1.getId()));
        Assert.assertTrue(result.contains(topic_1_2.getId()));

        parameters = creatBlogQueryParameters();
        parameters.setParentTopicIds(new Long[] { topic_1_1.getId() });
        result = convertTopics(queryManagement.query(QUERY, parameters));
        Assert.assertEquals(result.size(), 2);
        Assert.assertTrue(result.contains(topic_1_1_1.getId()));
        Assert.assertTrue(result.contains(topic_1_1_2.getId()));

        // User 2 with more restrictions.
        AuthenticationTestUtils.setSecurityContext(user2);

        parameters = creatBlogQueryParameters();
        parameters.setParentTopicIds(new Long[] { topic_1.getId() });
        result = convertTopics(queryManagement.query(QUERY, parameters));
        Assert.assertEquals(result.size(), 1);
        Assert.assertTrue(result.contains(topic_1_1.getId()));

        parameters = creatBlogQueryParameters();
        parameters.setParentTopicIds(new Long[] { topic_1_1.getId() });
        result = convertTopics(queryManagement.query(QUERY, parameters));
        Assert.assertEquals(result.size(), 2);
        Assert.assertTrue(result.contains(topic_1_1_1.getId()));
        Assert.assertTrue(result.contains(topic_1_1_2.getId()));
    }

    /**
     * Test that client admin can find all topics, even those he has no access to. This is part of
     * the topic-take-over feature of.
     */
    @Test
    public void testRetrievalOfAllTopicsAsClientAdmin() {
        User manager = TestUtils.createRandomUser(true);
        User user = TestUtils.createRandomUser(false);
        Blog topic = TestUtils.createRandomBlog(false, false, user);

        // Normal access
        AuthenticationTestUtils.setSecurityContext(manager);
        BlogQueryParameters parameters = creatBlogQueryParameters();
        parameters.setBlogIds(new Long[] { topic.getId() });
        PageableList<BlogData> result = queryManagement.query(QUERY, parameters);
        Assert.assertEquals(result.size(), 0);

        // forceAllTopics for manager
        parameters = creatBlogQueryParameters();
        parameters.setBlogIds(new Long[] { topic.getId() });
        parameters.setForceAllTopics(true);
        result = queryManagement.query(QUERY, parameters);
        Assert.assertEquals(result.size(), 1);

        // forceAllTopics for normal user -> Exception
        AuthenticationTestUtils.setSecurityContext(user);
        parameters = creatBlogQueryParameters();
        parameters.setForceAllTopics(true);
        try {
            result = queryManagement.query(QUERY, parameters);
            Assert.fail("Normal users should not be able to use forceAllTopics");
        } catch (RuntimeException e) {
            if (!(e.getCause() instanceof AccessDeniedException)) {
                throw e;
            }
            // Okay.
        }

    }

    /**
     * Test that it is possible to filter for tags by their id.
     * {@link BlogQueryParameters#addUserTagIds(Long)}
     *
     * @throws Exception
     *             Exception.
     */
    @Test
    public void testSearchForTagsById() throws Exception {
        Tag tag1 = tagManagement.storeTag(new TagTO(UUID.randomUUID().toString(),
                TagStoreType.Types.BLOG));
        Tag tag2 = tagManagement.storeTag(new TagTO(UUID.randomUUID().toString(),
                TagStoreType.Types.BLOG));
        User user = TestUtils.createRandomUser(false);
        Blog blog1 = TestUtils.createRandomBlog(true, true, new String[] { tag1.getDefaultName(),
                tag2.getDefaultName() }, user);
        TestUtils.createRandomBlog(true, true, new String[] { tag2.getDefaultName() }, user);
        AuthenticationTestUtils.setSecurityContext(user);
        BlogQueryParameters queryInstance = creatBlogQueryParameters();
        Assert.assertTrue(queryManagement.query(QUERY, queryInstance).size() > 1);
        queryInstance.addTagId(tag1.getId());
        PageableList<BlogData> result = queryManagement.query(QUERY, queryInstance);
        Assert.assertEquals(result.getMinNumberOfElements(), 1);
        Assert.assertEquals(result.get(0).getId(), blog1.getId());
        queryInstance.addTagId(tag2.getId());
        result = queryManagement.query(QUERY, queryInstance);
        Assert.assertEquals(result.getMinNumberOfElements(), 1);
        Assert.assertEquals(result.get(0).getId(), blog1.getId());
        queryInstance = creatBlogQueryParameters();
        queryInstance.addTagId(tag1.getId());
        queryInstance.addTagId(tag2.getId());
        result = queryManagement.query(QUERY, queryInstance);
        Assert.assertEquals(result.getMinNumberOfElements(), 1);
        queryInstance = creatBlogQueryParameters();
        queryInstance.addTagId(tag2.getId());
        result = queryManagement.query(QUERY, queryInstance);
        Assert.assertEquals(result.getMinNumberOfElements(), 2);
    }

    /**
     * Test that it is possible to filter tags by TagStore.
     * {@link BlogQueryParameters#addTagStoreTagId(String, String)}
     *
     * @throws Exception
     *             Exception.
     */
    @Test
    public void testSearchForTagsByTagStore() throws Exception {
        User user = TestUtils.createRandomUser(false);
        Tag tag1 = tagManagement.storeTag(new TagTO(UUID.randomUUID().toString(),
                TagStoreType.Types.BLOG));
        Blog blog1 = TestUtils.createRandomBlog(true, true, new String[] { tag1.getDefaultName() },
                user);
        // create a new TagStore with a higher priority so that it has precedence for blog tags
        int prio = tagStoreManagement.getTagStore(TagStoreType.Types.BLOG).getOrder() + 1;
        TagStore newTagStore = new DefaultTagStore(UUID.randomUUID().toString(), prio, false,
                TagStoreType.Types.BLOG);
        tagStoreManagement.addTagStore(newTagStore);
        // stored in new tag store
        Tag tag2 = tagManagement.storeTag(new TagTO(UUID.randomUUID().toString(),
                TagStoreType.Types.BLOG));
        // will add tag with name of tag1 to new tag store
        Blog blog2 = TestUtils.createRandomBlog(true, true, new String[] { tag1.getDefaultName(),
                tag2.getDefaultName() }, user);
        AuthenticationTestUtils.setSecurityContext(user);
        BlogQueryParameters queryInstance = creatBlogQueryParameters();
        Assert.assertTrue(queryManagement.query(QUERY, queryInstance).size() > 1);
        queryInstance.addTagStoreTagId(tag1.getTagStoreAlias(), tag1.getTagStoreTagId());
        PageableList<BlogData> result = queryManagement.query(QUERY, queryInstance);
        // expect only one blog because blog2's tag with name of tag1 is in new tag store
        Assert.assertEquals(result.getMinNumberOfElements(), 1);
        Assert.assertEquals(result.get(0).getId(), blog1.getId());
        // further restriction must return no results because blog1 has only one tag
        queryInstance.addTagStoreTagId(tag2.getTagStoreAlias(), tag2.getTagStoreTagId());
        result = queryManagement.query(QUERY, queryInstance);
        Assert.assertEquals(result.getMinNumberOfElements(), 0);

        queryInstance = creatBlogQueryParameters();
        queryInstance.addTagStoreTagId(tag2.getTagStoreAlias(), tag1.getTagStoreTagId());
        queryInstance.addTagStoreTagId(tag2.getTagStoreAlias(), tag2.getTagStoreTagId());
        result = queryManagement.query(QUERY, queryInstance);
        // expect blog2 as result because it has 2 tags from new tag store
        Assert.assertEquals(result.getMinNumberOfElements(), 1);
        Assert.assertEquals(result.get(0).getId(), blog2.getId());
        // adding the same tag store again must not change the result
        queryInstance.addTagStoreTagId(tag2.getTagStoreAlias(), tag2.getTagStoreTagId());
        result = queryManagement.query(QUERY, queryInstance);
        Assert.assertEquals(result.getMinNumberOfElements(), 1);
        Assert.assertEquals(result.get(0).getId(), blog2.getId());
        tagStoreManagement.removeTagStore(newTagStore);
    }

    /**
     * Test the ability to filter for root and top level topics and the different combinations of
     * these filters.
     *
     * @throws Exception
     *             in case the test failed
     */
    @Test
    public void testTopicHierarchyConstraints() throws Exception {
        ConfigurationManager properties = CommunoteRuntime.getInstance().getConfigurationManager();
        properties.updateClientConfigurationProperty(ClientProperty.TOP_LEVEL_TOPICS_ENABLED,
                "false");
        User clientManager = TestUtils.createRandomUser(true);

        Blog topicA = TestUtils.createRandomBlog(false, false, clientManager);
        Blog topicAA = TestUtils.createRandomBlog(false, false, clientManager);
        Blog topicB = TestUtils.createRandomBlog(false, false, clientManager);
        Long[] topicIds = new Long[] { topicA.getId(), topicAA.getId(), topicB.getId() };

        AuthenticationTestUtils.setSecurityContext(clientManager);
        topicHierarchyService.addTopic(topicA.getId(), topicAA.getId());

        // Top level topics enabled
        properties.updateClientConfigurationProperty(ClientProperty.TOP_LEVEL_TOPICS_ENABLED,
                "true");
        topicA.setToplevelTopic(true);
        topicDao.update(topicA);

        BlogQueryParameters parameters = creatBlogQueryParameters();
        parameters.setBlogIds(topicIds);
        List<Long> result = convertTopics(queryManagement.query(QUERY, parameters));
        Assert.assertEquals(result.size(), 3);

        // test that only top level topics are returned
        parameters = creatBlogQueryParameters();
        parameters.setBlogIds(topicIds);
        parameters.setShowOnlyToplevelTopics(true);
        // next two parameters must be ignored because previous takes precedence
        parameters.setShowOnlyRootTopics(true);
        parameters.setExcludeToplevelTopics(true);
        result = convertTopics(queryManagement.query(QUERY, parameters));
        Assert.assertEquals(result.size(), 1);
        Assert.assertEquals(result.get(0), topicA.getId());

        // test for root topics, top-level topics should be included by default
        parameters = creatBlogQueryParameters();
        parameters.setBlogIds(topicIds);
        parameters.setShowOnlyRootTopics(true);
        result = convertTopics(queryManagement.query(QUERY, parameters));
        Assert.assertEquals(result.size(), 2);
        Assert.assertTrue(result.contains(topicA.getId()));
        Assert.assertTrue(result.contains(topicB.getId()));

        // test for root topics excluding top-level topics
        parameters = creatBlogQueryParameters();
        parameters.setBlogIds(topicIds);
        parameters.setShowOnlyRootTopics(true);
        parameters.setExcludeToplevelTopics(true);
        result = convertTopics(queryManagement.query(QUERY, parameters));
        Assert.assertEquals(result.size(), 1);
        Assert.assertTrue(result.contains(topicB.getId()));

        // test for all topics excluding top-level topics
        parameters = creatBlogQueryParameters();
        parameters.setBlogIds(topicIds);
        parameters.setShowOnlyRootTopics(false);
        parameters.setExcludeToplevelTopics(true);
        result = convertTopics(queryManagement.query(QUERY, parameters));
        Assert.assertEquals(result.size(), 2);
        Assert.assertTrue(result.contains(topicAA.getId()));
        Assert.assertTrue(result.contains(topicB.getId()));

        // test for all topics
        parameters = creatBlogQueryParameters();
        parameters.setBlogIds(topicIds);
        parameters.setShowOnlyRootTopics(false);
        result = convertTopics(queryManagement.query(QUERY, parameters));
        Assert.assertEquals(result.size(), 3);
        Assert.assertTrue(result.contains(topicA.getId()));
        Assert.assertTrue(result.contains(topicAA.getId()));
        Assert.assertTrue(result.contains(topicB.getId()));
    }

    /**
     * Test for property filtering
     *
     * @throws Exception
     *             in case the test failed
     */
    public void testTopicProperties() throws Exception {
        final String keyGroup = "com.communote.server.test";
        final String key = "property.blog.id";
        this.propertyManagement.addObjectPropertyFilter(PropertyType.BlogProperty, keyGroup, key);

        User user = TestUtils.createRandomUser(false);

        // the id matching the property
        List<Long> idsToMatch = new ArrayList<Long>();
        for (int i = 0; i <= 10; i++) {
            Blog blog = TestUtils.createRandomBlog(true, true, user);
            if (i % 2 == 0) {
                // add the property
                this.propertyManagement.setObjectProperty(PropertyType.BlogProperty, blog.getId(),
                        keyGroup, key, "true");
                idsToMatch.add(blog.getId());
                // just to be sure
                Assert.assertEquals(
                        this.propertyManagement.getObjectProperty(PropertyType.BlogProperty,
                                blog.getId(), keyGroup, key), "true");
            } else if (i % 3 == 0) {
                // add the property with different value
                this.propertyManagement.setObjectProperty(PropertyType.BlogProperty, blog.getId(),
                        keyGroup, key, "false");
                // just to be sure
                Assert.assertEquals(
                        this.propertyManagement.getObjectProperty(PropertyType.BlogProperty,
                                blog.getId(), keyGroup, key), "false");
            }
        }

        // get the property with the true value
        BlogQueryParameters params = QUERY.createInstance();
        PropertyFilter propFilter = new PropertyFilter(keyGroup, Blog.class);
        propFilter.addProperty(key, "true", MatchMode.EQUALS);

        params.addPropertyFilter(propFilter);

        List<BlogData> result = this.queryManagement.executeQueryComplete(QUERY, params);

        // now check that we get the blogs with the ids that have the property assigned
        Assert.assertEquals(result.size(), idsToMatch.size());

        for (BlogData item : result) {
            // should contain the id
            Assert.assertTrue(idsToMatch.contains(item.getId()));
            // remove
            idsToMatch.remove(item.getId());
        }
        // should be empty
        Assert.assertTrue(idsToMatch.isEmpty());

    }
}
