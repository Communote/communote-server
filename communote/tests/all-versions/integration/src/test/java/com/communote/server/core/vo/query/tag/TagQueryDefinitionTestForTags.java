package com.communote.server.core.vo.query.tag;

import static com.communote.server.api.core.tag.TagStoreType.Types.NOTE;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.communote.common.util.PageableList;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.note.NoteStoringTO;
import com.communote.server.api.core.tag.TagStoreType;
import com.communote.server.api.core.tag.TagTO;
import com.communote.server.core.filter.listitems.RankTagListItem;
import com.communote.server.core.query.QueryManagement;
import com.communote.server.core.tag.TagStoreManagement;
import com.communote.server.core.vo.query.TaggingCoreItemUTPExtension;
import com.communote.server.model.blog.Blog;
import com.communote.server.model.i18n.Message;
import com.communote.server.model.user.Language;
import com.communote.server.model.user.User;
import com.communote.server.persistence.tag.DefaultTagStore;
import com.communote.server.persistence.tag.TagStore;
import com.communote.server.persistence.user.LanguageDao;
import com.communote.server.service.NoteService;
import com.communote.server.test.CommunoteIntegrationTest;
import com.communote.server.test.util.TestUtils;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class TagQueryDefinitionTestForTags extends CommunoteIntegrationTest {
    private Language english;
    private Language german;
    private QueryManagement queryManagement;
    private NoteService noteManagement;
    private TagStoreManagement tagStoreManagement;

    /**
     * Setup.
     */
    @BeforeClass(dependsOnGroups = "integration-test-setup")
    public void setup() {
        queryManagement = ServiceLocator.instance().getService(QueryManagement.class);
        LanguageDao languageDao = ServiceLocator.findService(LanguageDao.class);
        english = languageDao.findByLanguageCode("en");
        german = languageDao.findByLanguageCode("de");
        noteManagement = ServiceLocator.instance().getService(NoteService.class);
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
        User user = TestUtils.createRandomUser(false);
        DefaultTagStore myTagStore = new DefaultTagStore(random().toLowerCase(),
                TagStoreType.Types.NOTE);
        tagStoreManagement.addTagStore(myTagStore);
        TagTO defaultTag = new TagTO(random(), TagStoreType.Types.NOTE.getDefaultTagStoreId());
        TagTO myTag = new TagTO(random(), myTagStore.getTagStoreId());
        Set<TagTO> tags = new HashSet<TagTO>();
        NoteStoringTO noteStoringTO = TestUtils.createCommonNote(
                TestUtils.createRandomBlog(true, true, user), user.getId(), "Das ist ein Test.");
        tags.add(defaultTag);
        tags.add(myTag);
        noteStoringTO.setUnparsedTags(null);
        noteStoringTO.setTags(tags);
        noteManagement.createNote(noteStoringTO, null);
        RankTagQuery query = new RankTagQuery();
        TagQueryParameters queryParameters = query.createInstance();
        queryParameters.setUserIds(new Long[] { user.getId() });
        PageableList<RankTagListItem> result = queryManagement.query(query, queryParameters);
        Assert.assertEquals(result.getMinNumberOfElements(), 2);
        queryParameters.getTagStoreAliases().add(TagStoreType.Types.NOTE.getDefaultTagStoreId());
        result = queryManagement.query(query, queryParameters);
        Assert.assertEquals(result.getMinNumberOfElements(), 1);
        Assert.assertEquals(result.get(0).getName(), defaultTag.getName());
        queryParameters.getTagStoreAliases().add(myTagStore.getTagStoreId());
        result = queryManagement.query(query, queryParameters);
        Assert.assertEquals(result.getMinNumberOfElements(), 2);
        queryParameters.getTagStoreAliases().remove(TagStoreType.Types.NOTE.getDefaultTagStoreId());
        result = queryManagement.query(query, queryParameters);
        Assert.assertEquals(result.getMinNumberOfElements(), 1);
        Assert.assertEquals(result.get(0).getName(), myTag.getName());
    }

    /**
     * Tests the search for a tag prefix with tag stores that are not multilingual.
     *
     *
     * @throws Exception
     *             in case the test failed
     */
    @Test
    public void testForTagPrefixSearch() throws Exception {
        User user = TestUtils.createRandomUser(false);
        Blog blog = TestUtils.createRandomBlog(true, true, user);
        String tagName1 = random();
        String tagName2 = random();
        TagTO tag1 = new TagTO(tagName1 + "1", NOTE);
        TagTO tag2 = new TagTO(tagName1 + "2", NOTE);
        tag2.getNames().add(Message.Factory.newInstance(null, tagName2, false, english));
        TagTO tag3 = new TagTO(tagName2, NOTE);
        tag3.getNames().add(Message.Factory.newInstance(null, tagName1, false, german));
        Set<TagTO> tags = new HashSet<TagTO>();
        NoteStoringTO noteStoringTO = TestUtils.createCommonNote(blog, user.getId(),
                "Das ist ein Test.");
        tags.add(tag1);
        tags.add(tag2);
        tags.add(tag3);
        noteStoringTO.setTags(tags);
        noteManagement.createNote(noteStoringTO, null);
        // default tagStore is not multi lingual, check that only defaultName is searched
        AbstractTagQuery<RankTagListItem> query = new RankTagQuery();
        TagQueryParameters queryInstance = query.createInstance();
        queryInstance.setUserIds(new Long[] { user.getId() });
        queryInstance.setLanguageCode("en");
        queryInstance.setTagPrefix(tagName1);
        PageableList<RankTagListItem> result = queryManagement.query(query, queryInstance);
        Assert.assertEquals(result.getMinNumberOfElements(), 2);
        Assert.assertTrue(result.get(0).getDefaultName().equals(tagName1 + "1")
                || result.get(0).getDefaultName().equals(tagName1 + "2"));
        Assert.assertTrue(result.get(1).getDefaultName().equals(tagName1 + "1")
                || result.get(1).getDefaultName().equals(tagName1 + "2"));
        Assert.assertNotEquals(result.get(0).getDefaultName(), result.get(1).getDefaultName());

        // changing the language code must not make a difference
        queryInstance.setLanguageCode("de");
        result = queryManagement.query(query, queryInstance);
        Assert.assertEquals(result.getMinNumberOfElements(), 2);
        Assert.assertTrue(result.get(0).getDefaultName().equals(tag1.getDefaultName())
                || result.get(0).getDefaultName().equals(tag2.getDefaultName()));
        Assert.assertTrue(result.get(1).getDefaultName().equals(tag1.getDefaultName())
                || result.get(1).getDefaultName().equals(tag2.getDefaultName()));
        Assert.assertNotEquals(result.get(0).getDefaultName(), result.get(1).getDefaultName());

        queryInstance.setTagPrefix(tagName2);
        result = queryManagement.query(query, queryInstance);
        Assert.assertEquals(result.getMinNumberOfElements(), 1);
        Assert.assertTrue(result.get(0).getDefaultName().equals(tagName2));
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
    public void testForTagPrefixSearchMultilingual() throws Exception {
        User user = TestUtils.createRandomUser(false);
        Blog blog = TestUtils.createRandomBlog(true, true, user);
        // create multilingual tag store with higher priority so that new tags will be stored there
        int prio = tagStoreManagement.getTagStore(NOTE).getOrder() + 1;
        TagStore multilingualTagStore = new DefaultTagStore(UUID.randomUUID().toString(), prio,
                true, NOTE);
        tagStoreManagement.addTagStore(multilingualTagStore);
        String tagName1 = random();
        String tagName2 = random();
        TagTO tag1 = new TagTO(tagName1 + "1", NOTE);
        TagTO tag2 = new TagTO(tagName1 + "2", NOTE);
        tag2.getNames().add(Message.Factory.newInstance(null, tagName2, false, english));
        TagTO tag3 = new TagTO(tagName2, NOTE);
        tag3.getNames().add(Message.Factory.newInstance(null, tagName1, false, german));
        NoteStoringTO noteStoringTO = TestUtils.createCommonNote(blog, user.getId(),
                "Das ist ein Test.");
        Set<TagTO> tags = new HashSet<TagTO>();
        tags.add(tag1);
        tags.add(tag2);
        tags.add(tag3);
        noteStoringTO.setTags(tags);
        noteManagement.createNote(noteStoringTO, null);
        AbstractTagQuery<RankTagListItem> query = new RankTagQuery();
        TagQueryParameters queryInstance = query.createInstance();
        queryInstance.setUserIds(new Long[] { user.getId() });
        queryInstance.setLanguageCode("en");
        queryInstance.setTagPrefix(tagName1);
        PageableList<RankTagListItem> result = queryManagement.query(query, queryInstance);
        Assert.assertEquals(result.getMinNumberOfElements(), 1);
        Assert.assertEquals(result.get(0).getDefaultName(), tag1.getDefaultName());

        queryInstance.setLanguageCode("de");
        result = queryManagement.query(query, queryInstance);
        Assert.assertEquals(result.getMinNumberOfElements(), 3);
        HashSet<String> foundDefaultNames = new HashSet<String>();
        foundDefaultNames.add(result.get(0).getDefaultName());
        foundDefaultNames.add(result.get(1).getDefaultName());
        foundDefaultNames.add(result.get(2).getDefaultName());
        Assert.assertTrue(foundDefaultNames.contains(tag1.getDefaultName()));
        Assert.assertTrue(foundDefaultNames.contains(tag2.getDefaultName()));
        Assert.assertTrue(foundDefaultNames.contains(tag3.getDefaultName()));

        queryInstance.setLanguageCode("en");
        queryInstance.setTagPrefix(tagName2);
        result = queryManagement.query(query, queryInstance);
        Assert.assertEquals(result.getMinNumberOfElements(), 2);
        foundDefaultNames.clear();
        foundDefaultNames.add(result.get(0).getDefaultName());
        foundDefaultNames.add(result.get(1).getDefaultName());
        Assert.assertTrue(foundDefaultNames.contains(tag2.getDefaultName()));
        Assert.assertTrue(foundDefaultNames.contains(tag3.getDefaultName()));
        tagStoreManagement.removeTagStore(multilingualTagStore);
    }

    /**
     * Test that the tag prefix search is case-insensitive.
     */
    @Test
    public void testTagPrefixSearchCaseInsensitivity() {
        String tagPrefix = "ABC";
        String tag = tagPrefix + random();
        User user = TestUtils.createRandomUser(false);
        Blog blog = TestUtils.createRandomBlog(true, true, user);
        TaggingCoreItemUTPExtension filter = new TaggingCoreItemUTPExtension();
        filter.setBlogId(blog.getId());
        TestUtils.createAndStoreCommonNote(blog, user.getId(), "With hashtag #" + tag);
        RankTagQuery query = new RankTagQuery();
        TagQueryParameters queryParameters = query.createInstance();
        queryParameters.setTypeSpecificExtension(filter);
        queryParameters.setTagPrefix(tagPrefix.toLowerCase());
        PageableList<RankTagListItem> result = queryManagement.query(query, queryParameters);
        Assert.assertEquals(result.size(), 1);
        Assert.assertEquals(result.get(0).getName(), tag);

        queryParameters = query.createInstance();
        queryParameters.setTagPrefix(tagPrefix);
        queryParameters.setTypeSpecificExtension(filter);
        result = queryManagement.query(query, queryParameters);
        Assert.assertEquals(result.size(), 1);
        Assert.assertEquals(result.get(0).getName(), tag);
    }
}
