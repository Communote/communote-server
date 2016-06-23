package com.communote.server.core.vo.query.user;

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
import com.communote.server.core.filter.listitems.RankTagListItem;
import com.communote.server.core.query.QueryManagement;
import com.communote.server.core.tag.TagStoreManagement;
import com.communote.server.core.user.UserManagement;
import com.communote.server.core.vo.query.tag.UserTagQuery;
import com.communote.server.core.vo.query.tag.UserTagQueryParameters;
import com.communote.server.model.i18n.Message;
import com.communote.server.model.user.Language;
import com.communote.server.model.user.User;
import com.communote.server.persistence.tag.DefaultTagStore;
import com.communote.server.persistence.user.LanguageDao;
import com.communote.server.test.CommunoteIntegrationTest;
import com.communote.server.test.util.TestUtils;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class UserTagQueryDefinitionTestForTags extends CommunoteIntegrationTest {

    private UserManagement userManagement;
    private Language english;
    private Language german;
    private QueryManagement queryManagement;
    private TagStoreManagement tagStoreManagement;

    /**
     * Setup.
     */
    @BeforeClass(dependsOnGroups = "integration-test-setup")
    public void setup() {
        userManagement = ServiceLocator.instance().getService(UserManagement.class);
        queryManagement = ServiceLocator.instance().getService(QueryManagement.class);
        english = ServiceLocator.findService(LanguageDao.class).findByLanguageCode("en");
        german = ServiceLocator.findService(LanguageDao.class).findByLanguageCode("de");
        tagStoreManagement = ServiceLocator.instance().getService(TagStoreManagement.class);
    }

    /**
     * Test whether it is possible to filter for tags from different TagStores.
     *
     * @throws Exception
     *             in case the test failed
     */
    @Test
    public void testForDifferentTagStoresAliases() throws Exception {
        User user1 = TestUtils.createRandomUser(false);
        User user2 = TestUtils.createRandomUser(false);
        User user3 = TestUtils.createRandomUser(false);
        DefaultTagStore myTagStore1 = new DefaultTagStore(random().toLowerCase(),
                TagStoreType.Types.ENTITY);
        DefaultTagStore myTagStore2 = new DefaultTagStore(random().toLowerCase(),
                TagStoreType.Types.ENTITY);
        tagStoreManagement.addTagStore(myTagStore1);
        tagStoreManagement.addTagStore(myTagStore2);
        TagTO myTag1 = new TagTO(random(), myTagStore1.getTagStoreId());
        TagTO myTag2 = new TagTO(random(), myTagStore2.getTagStoreId());
        Set<TagTO> tags = new HashSet<TagTO>();
        tags.add(myTag1);
        userManagement.updateUserTags(user1.getId(), tags);
        tags.add(myTag2);
        userManagement.updateUserTags(user2.getId(), tags);
        tags.remove(myTag1);
        userManagement.updateUserTags(user3.getId(), tags);
        UserTagQuery query = new UserTagQuery();
        UserTagQueryParameters queryInstance = query.createInstance();
        PageableList<?> result = queryManagement.query(query, queryInstance);
        Assert.assertTrue(result.getMinNumberOfElements() >= 2);
        queryInstance.getTagStoreAliases().add(myTag1.getTagStoreAlias());
        result = queryManagement.query(query, queryInstance);
        Assert.assertEquals(result.getMinNumberOfElements(), 1);
        Assert.assertEquals(((TagData) result.get(0)).getName(), myTag1.getName());
        queryInstance.getTagStoreAliases().add(myTag2.getTagStoreAlias());
        result = queryManagement.query(query, queryInstance);
        Assert.assertEquals(result.getMinNumberOfElements(), 2);
        queryInstance.getTagStoreAliases().remove(myTag1.getTagStoreAlias());
        result = queryManagement.query(query, queryInstance);
        Assert.assertEquals(result.getMinNumberOfElements(), 1);
        Assert.assertEquals(((TagData) result.get(0)).getName(), myTag2.getName());
    }

    /**
     * Tests the search for a tag prefix:
     *
     * <ul>
     * <li>If DefaultName has prefix, but no translation in language available: is in result</li>
     * <li>If DefaultName has prefix, but translation without prefix in given language available: is
     * not in result</li>
     * <li>If translation has prefix: is in result</li>
     * </ul>
     *
     * @throws Exception
     *             in case the test failed
     */
    @Test
    public void testForTagPrefixSearch() throws Exception {
        DefaultTagStore myTagStore = new DefaultTagStore(random().toLowerCase(), 1000, true,
                TagStoreType.Types.ENTITY);
        tagStoreManagement.addTagStore(myTagStore);
        User user = TestUtils.createRandomUser(false);
        String tagName1 = random();
        String tagName2 = random();
        TagTO tag1 = new TagTO(tagName1 + "1", myTagStore.getTagStoreId());
        TagTO tag2 = new TagTO(tagName1 + "2", myTagStore.getTagStoreId());
        tag2.getNames().add(Message.Factory.newInstance(null, tagName2, false, english));
        TagTO tag3 = new TagTO(tagName2, myTagStore.getTagStoreId());
        tag3.getNames().add(Message.Factory.newInstance(null, tagName1, false, german));
        Set<TagTO> tags = new HashSet<TagTO>();
        tags.add(tag1);
        tags.add(tag2);
        tags.add(tag3);
        userManagement.updateUserTags(user.getId(), tags);
        UserTagQuery query = new UserTagQuery();
        UserTagQueryParameters queryInstance = query.createInstance();
        queryInstance.setLanguageCode("en");
        queryInstance.setTagPrefix(tagName1);
        PageableList<?> result = queryManagement.query(query, queryInstance);
        Assert.assertEquals(result.getMinNumberOfElements(), 1);
        queryInstance.setLanguageCode("de");
        result = queryManagement.query(query, queryInstance);
        Assert.assertEquals(result.getMinNumberOfElements(), 3);
        queryInstance.setLanguageCode("en");
        queryInstance.setTagPrefix(tagName2);
        result = queryManagement.query(query, queryInstance);
        Assert.assertEquals(result.getMinNumberOfElements(), 2);
    }

    /**
     * Test that the tags sorted according to their rank.
     */
    @Test
    public void testTagOrderByRank() {
        User user1 = TestUtils.createRandomUser(false);
        User user2 = TestUtils.createRandomUser(false);
        User user3 = TestUtils.createRandomUser(false);
        DefaultTagStore tagStore = new DefaultTagStore(random().toLowerCase(),
                TagStoreType.Types.ENTITY);
        tagStoreManagement.addTagStore(tagStore);
        TagTO tag1 = new TagTO(random(), tagStore.getTagStoreId());
        TagTO tag2 = new TagTO(random(), tagStore.getTagStoreId());
        TagTO tag3 = new TagTO(random(), tagStore.getTagStoreId());

        Set<TagTO> tags = new HashSet<TagTO>();
        tags.add(tag1);
        userManagement.updateUserTags(user1.getId(), tags);
        tags.add(tag2);
        userManagement.updateUserTags(user2.getId(), tags);
        tags.add(tag3);
        userManagement.updateUserTags(user3.getId(), tags);

        UserTagQuery query = new UserTagQuery();
        UserTagQueryParameters queryParameters = new UserTagQueryParameters();
        queryParameters.getTagStoreAliases().add(tag1.getTagStoreAlias());
        PageableList<RankTagListItem> result = queryManagement.query(query, queryParameters);
        Assert.assertEquals(result.size(), 3);
        // This fails in the original error, as the correct order was not preserved.
        Assert.assertEquals(result.get(0).getDefaultName(), tag1.getDefaultName());
        Assert.assertEquals(result.get(0).getRank().intValue(), 3);
        Assert.assertEquals(result.get(1).getDefaultName(), tag2.getDefaultName());
        Assert.assertEquals(result.get(1).getRank().intValue(), 2);
        Assert.assertEquals(result.get(2).getDefaultName(), tag3.getDefaultName());
        Assert.assertEquals(result.get(2).getRank().intValue(), 1);
    }
}
