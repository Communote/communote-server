package com.communote.server.core.vo.query.blog;

import static com.communote.server.api.core.tag.TagStoreType.Types.BLOG;

import java.util.HashSet;
import java.util.Set;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.communote.common.util.PageableList;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.tag.TagTO;
import com.communote.server.core.query.QueryManagement;
import com.communote.server.core.tag.TagStoreManagement;
import com.communote.server.core.vo.query.tag.BlogTagQuery;
import com.communote.server.core.vo.query.tag.BlogTagQueryParameters;
import com.communote.server.model.i18n.Message;
import com.communote.server.model.user.User;
import com.communote.server.model.user.Language;
import com.communote.server.persistence.tag.DefaultTagStore;
import com.communote.server.persistence.user.LanguageDao;
import com.communote.server.test.CommunoteIntegrationTest;
import com.communote.server.test.util.TestUtils;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class BlogQueryDefinitionTestForTags extends CommunoteIntegrationTest {
    private Language english;
    private Language german;
    private QueryManagement queryManagement;
    private DefaultTagStore tagStore;

    /**
     * Setup.
     */
    @BeforeClass(dependsOnGroups = "integration-test-setup")
    public void setup() {
        queryManagement = ServiceLocator.instance().getService(QueryManagement.class);
        english = ServiceLocator.findService(LanguageDao.class).findByLanguageCode("en");
        german = ServiceLocator.findService(LanguageDao.class).findByLanguageCode("de");
        tagStore = new DefaultTagStore(random(), 1001, true, BLOG);
        ServiceLocator.instance().getService(TagStoreManagement.class).addTagStore(tagStore);
    }

    /**
     * Tests the search for a tag prefix:
     *
     * <ul>
     * <li>If DefaultName has prefix, but no translation in language available -> In Result</li>
     * <li>If DefaultName has prefix, but translation without prefix in given language available ->
     * Not in Result</li>
     * <li>If translation has prefix -> In Result</li>
     * </ul>
     *
     * @throws Exception
     *             Exception.
     */
    @Test
    public void testForTagPrefixSearch() throws Exception {
        User user = TestUtils.createRandomUser(false);
        String tagName1 = random();
        String tagName2 = random();
        TagTO tag1 = new TagTO(tagName1 + "1", tagStore.getTagStoreId());
        TagTO tag2 = new TagTO(tagName1 + "2", tagStore.getTagStoreId());
        tag2.getNames().add(Message.Factory.newInstance(null, tagName2, false, english));
        TagTO tag3 = new TagTO(tagName2, tagStore.getTagStoreId());
        tag3.getNames().add(Message.Factory.newInstance(null, tagName1, false, german));
        Set<TagTO> tags = new HashSet<TagTO>();
        tags.add(tag1);
        tags.add(tag2);
        tags.add(tag3);
        TestUtils.createRandomBlog(true, true, null, tags, user);
        BlogTagQuery queryDefinition = new BlogTagQuery();
        BlogTagQueryParameters queryInstance = queryDefinition.createInstance();
        queryInstance.setLanguageCode("en");
        queryInstance.setTagPrefix(tagName1);
        PageableList<?> result = queryManagement.query(queryDefinition, queryInstance);
        Assert.assertEquals(result.getMinNumberOfElements(), 1);
        queryInstance.setLanguageCode("de");
        result = queryManagement.query(queryDefinition, queryInstance);
        Assert.assertEquals(result.getMinNumberOfElements(), 3);
        queryInstance.setLanguageCode("en");
        queryInstance.setTagPrefix(tagName2);
        result = queryManagement.query(queryDefinition, queryInstance);
        Assert.assertEquals(result.getMinNumberOfElements(), 2);
    }
}
