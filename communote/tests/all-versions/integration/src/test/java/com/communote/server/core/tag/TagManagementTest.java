package com.communote.server.core.tag;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.apache.commons.lang.math.RandomUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.blog.BlogManagement;
import com.communote.server.api.core.common.NotFoundException;
import com.communote.server.api.core.note.NoteRenderContext;
import com.communote.server.api.core.note.NoteRenderMode;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.api.core.tag.TagData;
import com.communote.server.api.core.tag.TagStoreType;
import com.communote.server.api.core.tag.TagTO;
import com.communote.server.api.core.tag.TagStoreType.Types;
import com.communote.server.core.filter.listitems.SimpleNoteListItem;
import com.communote.server.core.follow.FollowManagement;
import com.communote.server.core.query.QueryManagement;
import com.communote.server.core.security.AuthenticationHelper;
import com.communote.server.core.tag.TagManagement;
import com.communote.server.core.tag.TagStoreManagement;
import com.communote.server.core.tag.TagStoreNotFoundException;
import com.communote.server.core.vo.query.post.NoteQuery;
import com.communote.server.core.vo.query.post.NoteQueryParameters;
import com.communote.server.model.blog.Blog;
import com.communote.server.model.i18n.Message;
import com.communote.server.model.tag.Tag;
import com.communote.server.model.user.User;
import com.communote.server.model.user.Language;
import com.communote.server.persistence.tag.DefaultTagStore;
import com.communote.server.persistence.tag.TagStore;
import com.communote.server.service.NoteService;
import com.communote.server.test.CommunoteIntegrationTest;
import com.communote.server.test.util.AuthenticationTestUtils;
import com.communote.server.test.util.TestUtils;

/**
 * 
 * Tests for {@link TagManagement}.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class TagManagementTest extends CommunoteIntegrationTest {

    /**
     * TagStore which reverts the id.
     */
    private class ReverseTagStore extends DefaultTagStore {
        /**
         * 
         * @param alias
         *            The alias.
         * @param types
         *            The types.
         */
        public ReverseTagStore(String alias, TagStoreType... types) {
            super(alias, types);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getTagStoreTagId(Tag tag) {
            return new StringBuilder(super.getTagStoreTagId(tag)).reverse().toString();
        }
    }

    @Autowired
    private NoteService noteService;

    @Autowired
    private TagManagement tagManagement;
    @Autowired
    private FollowManagement followManagement;
    @Autowired
    private QueryManagement queryManagement;

    @Autowired
    private TagStoreManagement tagStoreManagement;

    private TagStore multilingualTagStore;

    /**
     * 
     * @param user
     *            The user to check for.
     * @param tagManagement
     *            The tag management
     * @param tagName
     *            The tag.
     * @param count
     *            The expected count.
     */
    private void assertTagCount(User user, TagManagement tagManagement, String tagName,
            int count) {
        AuthenticationTestUtils.setSecurityContext(user);
        Long tagId = tagManagement.findTag(tagName, Types.NOTE).getId();
        Assert.assertEquals(tagManagement.getCount(tagId), count);
    }

    @DataProvider(name = "booleanProvider")
    private Object[][] booleanValues() {
        return new Object[][] {
                { Boolean.FALSE },
                { Boolean.TRUE },
        };
    }

    private List<SimpleNoteListItem> findFollowedNotes(Blog topic) {
        NoteQuery query = new NoteQuery();
        NoteQueryParameters queryParameters = query.createInstance();
        queryParameters.setRetrieveOnlyFollowedItems(true);
        queryParameters.getTypeSpecificExtension().setBlogFilter(new Long[] { topic.getId() });
        return queryManagement.executeQueryComplete(query, queryParameters);
    }

    /**
     * Setup.
     */
    @BeforeClass(dependsOnGroups = GROUP_INTEGRATION_TEST_SETUP)
    public void setup() {
        multilingualTagStore = new DefaultTagStore("Multilingual"
                + Types.NOTE.getDefaultTagStoreId(), 1000, true,
                TagStoreType.Types.NOTE);
        tagStoreManagement.addTagStore(multilingualTagStore);
    }

    /**
     * Tests the creation of tags with localizations.
     * 
     * @throws Exception
     *             Exception.
     */
    @Test
    public void testCreateAndUpdateTagWithTranslation() throws Exception {
        // Create new Tag
        TagTO tag = new TagTO("testWithLanguage" + UUID.randomUUID(),
                multilingualTagStore.getTagStoreId());
        Message translation = Message.Factory.newInstance("tag.translation.testWithLanguage",
                "testMitSprache", false);
        translation.setLanguage(Language.Factory.newInstance("de", "German", "Deutsch"));
        tag.getNames().add(translation);
        Tag databaseTag = tagManagement.storeTag(tag);
        Assert.assertNotNull(databaseTag.getId());
        Assert.assertEquals(databaseTag.getDefaultName(), tag.getDefaultName());
        Assert.assertEquals(databaseTag.getNames().size(), 1);
        Assert.assertEquals(databaseTag.getNames().iterator().next().getMessage(), tag.getNames()
                .iterator().next().getMessage());
        // Update Tag:
        // Change name -> should not be updated
        // Add name -> should be added
        Message name = tag.getNames().iterator().next();
        String oldName = name.getMessage();
        name.setMessage(UUID.randomUUID().toString());
        Message translation2 = Message.Factory.newInstance("tag.translation.testWithLanguage",
                "testWithLanguage", false);
        translation2.setLanguage(Language.Factory.newInstance("en", "English", "English"));
        tag.getNames().add(translation2);
        Tag updatedDatabaseTag = tagManagement.storeTag(tag);
        Assert.assertNotNull(updatedDatabaseTag.getId());
        Assert.assertEquals(updatedDatabaseTag.getDefaultName(), tag.getDefaultName());
        Assert.assertEquals(updatedDatabaseTag.getNames().size(), 2);
        for (Message message : updatedDatabaseTag.getNames()) {
            Assert.assertEquals(message.getMessage(),
                    message.getId().equals(name.getId()) ? oldName
                            : translation2.getMessage());
        }
    }

    /**
     * Tests, that there is an Exception thrown, when a tag should be created for an non existing
     * tag store.
     * 
     * @throws Exception
     *             Exception.
     */
    @Test(expectedExceptions = TagStoreNotFoundException.class)
    public void testCreateTagForNonExistingTagStore() throws Exception {
        TagTO tag = new TagTO(UUID.randomUUID().toString(), UUID.randomUUID().toString());
        tagManagement.storeTag(tag);
    }

    /**
     * Test for {@link TagManagement#getCount(Long)}
     * 
     * @throws Exception
     *             Exception.
     */
    @Test
    public void testGetCount() throws Exception {

        User user1 = TestUtils.createRandomUser(false);
        User user2 = TestUtils.createRandomUser(false);
        User user3 = TestUtils.createRandomUser(false);
        Blog blog1 = TestUtils.createRandomBlog(true, true, user1);
        Blog blog2 = TestUtils.createRandomBlog(false, false, user2, user3);
        String tag = UUID.randomUUID().toString();

        AuthenticationTestUtils.setSecurityContext(user1);
        int i = 1;
        for (; i <= RandomUtils.nextInt(100) + 10; i++) {
            TestUtils.createAndStoreCommonNote(blog1, user1.getId(), "#" + tag);
            assertTagCount(user1, tagManagement, tag, i);
            assertTagCount(user2, tagManagement, tag, i);
            assertTagCount(user3, tagManagement, tag, i);
        }

        AuthenticationTestUtils.setSecurityContext(user2);
        TestUtils.createAndStoreCommonNote(blog2, user2.getId(), "#" + tag);
        assertTagCount(user1, tagManagement, tag, i - 1);
        assertTagCount(user2, tagManagement, tag, i);
        assertTagCount(user3, tagManagement, tag, i);

        AuthenticationTestUtils.setSecurityContext(user2);
        TestUtils.createAndStoreCommonNote(blog2, user2.getId(), "d @" + user2.getAlias() + " #"
                + tag);
        assertTagCount(user1, tagManagement, tag, i - 1);
        assertTagCount(user2, tagManagement, tag, i + 1);
        assertTagCount(user3, tagManagement, tag, i);

        AuthenticationTestUtils.setSecurityContext(user1);
        Long noteId = TestUtils.createAndStoreCommonNote(blog1, user1.getId(), "#" + tag);
        assertTagCount(user1, tagManagement, tag, i);
        assertTagCount(user2, tagManagement, tag, i + 2);
        assertTagCount(user3, tagManagement, tag, i + 1);

        AuthenticationTestUtils.setSecurityContext(user1);
        ServiceLocator.instance().getService(NoteService.class).deleteNote(noteId, true, true);
        assertTagCount(user1, tagManagement, tag, i - 1);
        assertTagCount(user2, tagManagement, tag, i + 1);
        assertTagCount(user3, tagManagement, tag, i);

        AuthenticationTestUtils.setSecurityContext(user1);
        ServiceLocator.instance().getService(BlogManagement.class).deleteBlog(blog1.getId(), null);
        assertTagCount(user1, tagManagement, tag, 0);
        assertTagCount(user2, tagManagement, tag, 2);
        assertTagCount(user3, tagManagement, tag, 1);
    }

    /**
     * Test merging of a tag that is followed by a user.
     * 
     * @throws AuthorizationException
     *             The test should fail if thrown.
     * @throws NotFoundException
     *             The test should fail if thrown.
     */
    @Test
    public void testMergeFollowedNoteTag() throws AuthorizationException,
            NotFoundException {
        User user = TestUtils.createRandomUser(false);
        Blog topic = TestUtils.createRandomBlog(false, false, user);
        Long note1Id = TestUtils.createAndStoreCommonNote(topic, user.getId(), "blabla");
        Long note2Id = TestUtils.createAndStoreCommonNote(topic, user.getId(), "blabla");

        Long oldTag1Id = noteService.getNote(note1Id, new NoteRenderContext(
                NoteRenderMode.HTML, Locale.GERMAN)).getTags().get(0).getId();
        Long newTag1Id = noteService.getNote(note2Id, new NoteRenderContext(
                NoteRenderMode.HTML, Locale.GERMAN)).getTags().get(0).getId();

        AuthenticationTestUtils.setSecurityContext(user);
        followManagement.followTag(oldTag1Id);
        Assert.assertTrue(followManagement.followsTag(oldTag1Id));
        Assert.assertFalse(followManagement.followsTag(newTag1Id));

        AuthenticationTestUtils.setManagerContext();
        tagManagement.removeNoteTag(oldTag1Id, newTag1Id);

        // assert that the old tag was removed from the note and the new tag is set
        Assert.assertNull(tagManagement.findTag(oldTag1Id));
        AuthenticationTestUtils.setSecurityContext(user);
        Long mergedTagId = noteService.getNote(note1Id, new NoteRenderContext(
                NoteRenderMode.HTML, Locale.GERMAN)).getTags().get(0).getId();
        Assert.assertEquals(mergedTagId, newTag1Id);

        // assert that the user now follows the new tag, the other was merged with
        Assert.assertTrue(followManagement.followsTag(newTag1Id));
        Assert.assertFalse(followManagement.followsTag(oldTag1Id));
    }

    /**
     * Test merging two tags with help of the removeNoteTag method for tags that are used in many
     * notes.
     * 
     * @param followTag
     *            if true the tag to remove will be followed by a user, before removing it. The tag
     *            to merge will be followed by another user.
     * @throws Exception
     *             in case the test failed
     */
    @Test(dataProvider = "booleanProvider")
    public void testMergeFrequentlyUsedNoteTag(Boolean followTag) throws Exception {
        User user1 = TestUtils.createRandomUser(false);
        User user2 = TestUtils.createRandomUser(false);
        Blog topic = TestUtils.createRandomBlog(false, false, user1, user2);
        Long note1Id = TestUtils.createAndStoreCommonNote(topic, user1.getId(), "blabla");
        Long note2Id = TestUtils.createAndStoreCommonNote(topic, user1.getId(), "blabla");

        AuthenticationTestUtils.setSecurityContext(user1);
        TagData oldTag = noteService.getNote(note1Id, new NoteRenderContext(
                NoteRenderMode.HTML, Locale.GERMAN)).getTags().get(0);
        Long newTagId = noteService.getNote(note2Id, new NoteRenderContext(
                NoteRenderMode.HTML, Locale.GERMAN)).getTags().get(0).getId();
        if (followTag) {
            followManagement.followTag(oldTag.getId());
            AuthenticationTestUtils.setSecurityContext(user2);
            followManagement.followTag(newTagId);
        }
        ArrayList<Long> createdNotes = new ArrayList<>();
        createdNotes.add(note1Id);
        for (int i = 0; i < 100; i++) {
            createdNotes.add(TestUtils.createAndStoreCommonNote(topic, user1.getId(), "blabla #"
                    + oldTag.getDefaultName()));
        }
        if (followTag) {
            Assert.assertTrue(findFollowedNotes(topic).size() == 1, "Only one note uses the newTag");
            AuthenticationTestUtils.setSecurityContext(user1);
            Assert.assertTrue(findFollowedNotes(topic).size() > 1, "User1 should see all notes");
        }

        AuthenticationTestUtils.setSecurityContext(TestUtils.createRandomUser(true));
        tagManagement.removeNoteTag(oldTag.getId(), newTagId);
        AuthenticationTestUtils.setSecurityContext(user1);
        for (Long noteId : createdNotes) {
            List<TagData> noteTags = noteService.getNote(noteId, new NoteRenderContext(
                    NoteRenderMode.HTML, Locale.GERMAN)).getTags();
            boolean newTagFound = false;
            for (TagData item : noteTags) {
                if (item.getId().equals(newTagId)) {
                    newTagFound = true;
                } else {
                    Assert.assertNotEquals(item.getId(), oldTag.getId());
                }
            }
            Assert.assertTrue(newTagFound, "The new tag is not set on note " + noteId);
        }
        if (followTag) {
            // when removing a tag that is followed by users they will automatically follow the new
            // tag
            Assert.assertTrue(findFollowedNotes(topic).size() > 1, "User1 should see all notes");
            // user2 follows the merged tag newTag and thus should see all notes
            AuthenticationTestUtils.setSecurityContext(user2);
            Assert.assertTrue(findFollowedNotes(topic).size() > 1, "User2 should see all notes");
        }
    }

    /**
     * Test removing of a tag that is followed by a user.
     * 
     * @throws AuthorizationException
     *             The test should fail if thrown.
     * @throws NotFoundException
     *             The test should fail if thrown.
     */
    @Test
    public void testRemoveFollowedTag() throws AuthorizationException, NotFoundException {
        User user = TestUtils.createRandomUser(false);
        Blog topic = TestUtils.createRandomBlog(false, false, user);
        Long noteId = TestUtils.createAndStoreCommonNote(topic, user.getId(), "blabla");
        long tagId = noteService
                .getNote(noteId, new NoteRenderContext(NoteRenderMode.HTML, Locale.GERMAN))
                .getTags().get(0).getId();
        AuthenticationTestUtils.setSecurityContext(user);
        followManagement.followTag(tagId);
        Assert.assertTrue(followManagement.followsTag(tagId));

        AuthenticationTestUtils.setManagerContext();
        tagManagement.removeNoteTag(tagId, null);

        Assert.assertNull(tagManagement.findTag(tagId));

        AuthenticationTestUtils.setSecurityContext(user);
        Assert.assertFalse(followManagement.followsTag(tagId));
    }

    /**
     * Test removing a tag that is used in many notes.
     * 
     * @param followTag
     *            if true the tag to remove will be followed by a user, before removing it
     * @throws Exception
     *             in case the test failed
     */
    @Test(dataProvider = "booleanProvider")
    public void testRemoveFrequentlyUsedNoteTag(Boolean followTag) throws Exception {
        User user = TestUtils.createRandomUser(false);
        Blog topic = TestUtils.createRandomBlog(false, false, user);
        Long note1Id = TestUtils.createAndStoreCommonNote(topic, user.getId(), "blabla");

        TagData oldTag = noteService.getNote(note1Id, new NoteRenderContext(
                NoteRenderMode.HTML, Locale.GERMAN)).getTags().get(0);

        if (followTag) {
            AuthenticationTestUtils.setSecurityContext(user);
            followManagement.followTag(oldTag.getId());
        }
        ArrayList<Long> createdNotes = new ArrayList<>();
        createdNotes.add(note1Id);
        for (int i = 0; i < 100; i++) {
            createdNotes.add(TestUtils.createAndStoreCommonNote(topic, user.getId(), "blabla #"
                    + oldTag.getDefaultName()));
        }
        if (followTag) {
            Assert.assertTrue(findFollowedNotes(topic).size() > 0);
        }
        AuthenticationTestUtils.setSecurityContext(TestUtils.createRandomUser(true));
        tagManagement.removeNoteTag(oldTag.getId(), null);

        AuthenticationTestUtils.setSecurityContext(user);
        for (Long noteId : createdNotes) {
            List<TagData> noteTags = noteService.getNote(noteId, new NoteRenderContext(
                    NoteRenderMode.HTML, Locale.GERMAN)).getTags();
            for (TagData item : noteTags) {
                Assert.assertNotEquals(item.getId(), oldTag.getId());
            }
        }
        if (followTag) {
            Assert.assertTrue(findFollowedNotes(topic).size() == 0);
        }
    }

    /**
     * This method tests, that only managers and the internal system user can delete and move tags.
     * 
     * @throws Exception
     *             in case the test failed
     */
    @Test
    public void testRemoveNoteTag() throws Exception {
        User user = TestUtils.createRandomUser(false);
        Blog topic = TestUtils.createRandomBlog(false, false, user);
        Long note1Id = TestUtils.createAndStoreCommonNote(topic, user.getId(), "blabla");
        Long note2Id = TestUtils.createAndStoreCommonNote(topic, user.getId(), "blabla");

        Long oldTag1Id = noteService.getNote(note1Id, new NoteRenderContext(
                NoteRenderMode.HTML, Locale.GERMAN)).getTags().get(0).getId();
        Long newTag1Id = noteService.getNote(note2Id, new NoteRenderContext(
                NoteRenderMode.HTML, Locale.GERMAN)).getTags().get(0).getId();

        // Normal user are not allowed to do this
        AuthenticationTestUtils.setSecurityContext(user);
        try {
            tagManagement.removeNoteTag(oldTag1Id, newTag1Id);
            Assert.fail("A normal user may not move or delete tags.");
        } catch (AuthorizationException e) {
            // Okay.
        }

        // Manager
        AuthenticationTestUtils.setSecurityContext(TestUtils.createRandomUser(true));
        tagManagement.removeNoteTag(oldTag1Id, newTag1Id);
        Assert.assertNull(tagManagement.findTag(oldTag1Id));

        // Internal System User
        AuthenticationTestUtils.setSecurityContext(user);
        oldTag1Id = noteService.getNote(note1Id, new NoteRenderContext(
                NoteRenderMode.HTML, Locale.GERMAN)).getTags().get(0).getId();
        newTag1Id = noteService.getNote(note2Id, new NoteRenderContext(
                NoteRenderMode.HTML, Locale.GERMAN)).getTags().get(0).getId();
        AuthenticationHelper.setInternalSystemToSecurityContext();
        tagManagement.removeNoteTag(oldTag1Id, newTag1Id);
    }

    /**
     * This tests, that it is possible to rename a tag.
     * 
     * @throws Exception
     *             in case the test failed
     */
    @Test
    public void testRenameTag() throws Exception {
        String oldName = UUID.randomUUID().toString();
        String newName = UUID.randomUUID().toString();
        TagTO tag = new TagTO(oldName, TagStoreType.Types.NOTE);
        Long tagId = tagManagement.storeTag(tag).getId();
        Assert.assertEquals(tagManagement.findTag(tagId).getDefaultName(), oldName);
        AuthenticationTestUtils.setSecurityContext(TestUtils.createRandomUser(false));
        try {
            tagManagement.renameTag(tagId, newName);
            Assert.fail("Normal users may not update the tags default name.");
        } catch (AuthorizationException e) {
            // Okay
        }
        AuthenticationTestUtils.setSecurityContext(TestUtils.createRandomUser(true));
        tagManagement.renameTag(tagId, newName);
        Assert.assertEquals(tagManagement.findTag(tagId).getDefaultName(), newName);

        AuthenticationHelper.setInternalSystemToSecurityContext();
        tagManagement.renameTag(tagId, oldName);
        Assert.assertEquals(tagManagement.findTag(tagId).getDefaultName(), oldName);
    }

    /**
     * Test renaming a tag where the new name only differs in the case of some letters.
     * 
     * @throws Exception
     *             in case the test failed
     */
    @Test
    public void testRenameTagCaseSensitive() throws Exception {
        String uniqueSuffix = UUID.randomUUID().toString().toLowerCase();
        String oldName = "aBC" + uniqueSuffix;
        String newName = "Abc" + uniqueSuffix;
        AuthenticationTestUtils.setSecurityContext(TestUtils.createRandomUser(true));
        TagTO tag = new TagTO(oldName, TagStoreType.Types.NOTE);
        Long tagId = tagManagement.storeTag(tag).getId();
        Assert.assertEquals(tagManagement.findTag(tagId).getDefaultName(), oldName);
        tagManagement.renameTag(tagId, newName);
        Assert.assertEquals(tagManagement.findTag(tagId).getDefaultName(), newName);
    }

    /**
     * Test for {@link TagManagement#storeTag(TagTO)}
     * 
     * @throws Exception
     *             Exception.
     */
    @Test
    public void testStoreTag() throws Exception {
        TagTO tag1 = new TagTO(UUID.randomUUID().toString(), TagStoreType.Types.NOTE);
        TagTO tag1Clone = new TagTO(tag1.getDefaultName(), TagStoreType.Types.NOTE);
        Tag dbTag1 = tagManagement.storeTag(tag1);
        Assert.assertNotNull(dbTag1.getId());
        Tag dbTag1Clone = tagManagement.storeTag(tag1Clone);
        Assert.assertEquals(dbTag1Clone.getId(), dbTag1.getId(),
                "Both tags should have the same id.");
        Tag randomDbTag = tagManagement.storeTag(new TagTO(UUID.randomUUID().toString(),
                TagStoreType.Types.NOTE));
        Assert.assertNotEquals(randomDbTag.getId(), dbTag1.getId(),
                "Both tags should have the same id.");
        DefaultTagStore tagStore = new ReverseTagStore(UUID.randomUUID().toString(),
                TagStoreType.Types.NOTE);
        tagStoreManagement.addTagStore(tagStore);
        TagTO newTagStoreTag = new TagTO(UUID.randomUUID().toString(), tagStore.getTagStoreId());
        Tag newTagStoreDbTag = tagManagement.storeTag(newTagStoreTag);
        Assert.assertNotNull(newTagStoreDbTag.getId());
        Assert.assertEquals(newTagStoreDbTag.getTagStoreTagId(),
                new StringBuilder(newTagStoreTag.getDefaultName()).reverse().toString());
        Assert.assertEquals(newTagStoreDbTag.getDefaultName(), newTagStoreTag.getDefaultName());
        Assert.assertEquals(newTagStoreDbTag.getTagStoreAlias(), tagStore.getTagStoreId());
        Assert.assertNotNull(newTagStoreDbTag.getGlobalId());
        Assert.assertEquals(newTagStoreDbTag.getGlobalId(), newTagStoreDbTag.getFollowId());
        tagStoreManagement.removeTagStore(tagStore);
    }
}
