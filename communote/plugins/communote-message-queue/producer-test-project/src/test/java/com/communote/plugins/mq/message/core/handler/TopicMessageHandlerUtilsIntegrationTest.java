package com.communote.plugins.mq.message.core.handler;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.communote.plugins.mq.message.core.data.property.StringProperty;
import com.communote.plugins.mq.message.core.data.topic.Topic;
import com.communote.plugins.mq.message.core.handler.converter.TopicConverter;
import com.communote.plugins.mq.message.core.util.StoringPolicy;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.blog.BlogManagement;
import com.communote.server.api.core.property.PropertyManagement;
import com.communote.server.api.core.property.PropertyType;
import com.communote.server.core.tag.TagStoreManagement;
import com.communote.server.api.core.blog.BlogTO;
import com.communote.server.model.blog.Blog;
import com.communote.server.model.i18n.Message;
import com.communote.server.persistence.tag.DefaultTagStore;
import com.communote.server.model.tag.Tag;
import com.communote.server.api.core.tag.TagStoreType;
import com.communote.server.api.core.tag.TagStoreType.Types;
import com.communote.server.api.core.tag.TagTO;
import com.communote.server.model.user.User;
import com.communote.server.model.user.Language;
import com.communote.server.test.CommunoteIntegrationTest;
import com.communote.server.test.util.TestUtils;

/**
 * This contains some integration test for {@link TopicMessageHandlerUtils}.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class TopicMessageHandlerUtilsIntegrationTest extends CommunoteIntegrationTest {

    private DefaultTagStore multilingualTagStore;

    /**
     * Setup.
     */
    @BeforeClass(dependsOnGroups = "integration-test-setup")
    public void setup() {
        multilingualTagStore = new DefaultTagStore("Multilingual"
                + Types.NOTE.getDefaultTagStoreId(), 1000, true, TagStoreType.Types.NOTE);
        ServiceLocator.instance().getService(TagStoreManagement.class)
                .addTagStore(multilingualTagStore);
    }

    /**
     * Regression test for KENMEI-5043: Lazy Init Exception while EditTopicMessageHandler tries to
     * merge blog tags of a multi-lingual TagStore. Tries to provoke the
     * LazyInitializationException.
     * 
     * @throws Exception
     *             in case the test failed
     */
    @Test
    public void testForKENMEI5043() throws Exception {
        TagTO tag = new TagTO("testWithLanguage" + UUID.randomUUID(),
                multilingualTagStore.getTagStoreId());
        Message translation = Message.Factory.newInstance("tag.translation.testWithLanguage",
                "testMitSprache", false);
        translation.setLanguage(Language.Factory.newInstance("de", "German", "Deutsch"));
        tag.getNames().add(translation);

        User user = TestUtils.createRandomUser(false);
        Blog topic = TestUtils.createRandomBlog(false, false, user);
        BlogTO blogTO = new BlogTO();
        blogTO.setDescription(topic.getDescription());
        blogTO.setNameIdentifier(topic.getNameIdentifier());
        blogTO.setTitle(topic.getTitle());
        blogTO.setTags(new HashSet<TagTO>());
        blogTO.getTags().add(tag);

        BlogManagement topicManagement = ServiceLocator.instance().getService(BlogManagement.class);
        topicManagement.updateBlog(topic.getId(), blogTO);

        Set<Tag> existingTags = topicManagement.findBlogById(topic.getId(), true).getTags();
        Set<TagTO> extractedTagTOs = TopicMessageHandlerUtils.extractTagTOs(
                new com.communote.plugins.mq.message.core.data.tag.Tag[0], existingTags,
                StoringPolicy.MERGE);
        for (TagTO extractedTagTO : extractedTagTOs) {
            for (Message name : extractedTagTO.getNames()) {
                // Within the original error a LazyLoadingException is thrown here.
                Assert.assertNotNull(name.getLanguage().getLanguageCode());
            }
        }
    }

    /**
     * Regression test for KENMEI-5044: blog properties are not contained in ReplyMessage of a
     * CreateTopicMessage or EditTopicMessage
     * 
     * @throws Exception
     *             in case the test failed
     */
    @Test
    public void testForKENMEI5044() throws Exception {
        String keyGroup = random();
        User user = TestUtils.createRandomUser(false);
        Blog topic = TestUtils.createRandomBlog(false, false, user);
        PropertyManagement propertyManagement = ServiceLocator.instance().getService(
                PropertyManagement.class);
        Set<String> keyValues = new HashSet<String>();
        for (int i = 0; i < new Random().nextInt(5) + 2; i++) {
            String key = random();
            String value = random();
            keyValues.add(key + "|" + value);
            propertyManagement.addObjectPropertyFilter(PropertyType.BlogProperty, keyGroup, key);
            propertyManagement.setObjectProperty(PropertyType.BlogProperty, topic.getId(),
                    keyGroup, key, value);
        }
        Topic mqTopic = new TopicConverter(propertyManagement).convertBlogToTopic(topic, null);
        Assert.assertEquals(mqTopic.getTopicId(), topic.getId());
        Assert.assertEquals(mqTopic.getProperties().length, keyValues.size());
        for (StringProperty property : mqTopic.getProperties()) {
            Assert.assertEquals(property.getGroup(), keyGroup);
            String keyValue = property.getKey() + "|" + property.getValue();
            Assert.assertTrue(keyValues.contains(keyValue));
            keyValues.remove(keyValue);
        }
    }
}
