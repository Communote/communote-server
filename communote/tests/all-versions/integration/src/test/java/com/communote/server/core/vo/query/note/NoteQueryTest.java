package com.communote.server.core.vo.query.note;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang.math.RandomUtils;
import org.hibernate.impl.SessionFactoryImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.communote.common.util.PageableList;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.attachment.AttachmentData;
import com.communote.server.api.core.note.NoteData;
import com.communote.server.api.core.note.NoteRenderContext;
import com.communote.server.api.core.note.NoteRenderMode;
import com.communote.server.api.core.note.NoteStoringTO;
import com.communote.server.api.core.property.PropertyManagement;
import com.communote.server.api.core.property.PropertyType;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.api.core.tag.TagStoreType;
import com.communote.server.api.core.tag.TagTO;
import com.communote.server.core.blog.NoteNotFoundException;
import com.communote.server.core.crc.FilesystemConnector;
import com.communote.server.core.filter.listitems.SimpleNoteListItem;
import com.communote.server.core.follow.FollowManagement;
import com.communote.server.core.query.QueryManagement;
import com.communote.server.core.security.AuthenticationHelper;
import com.communote.server.core.tag.TagManagement;
import com.communote.server.core.tag.TagStoreManagement;
import com.communote.server.core.vo.query.DiscussionFilterMode;
import com.communote.server.core.vo.query.QueryConfigurationException;
import com.communote.server.core.vo.query.QueryParameters.OrderDirection;
import com.communote.server.core.vo.query.TaggingCoreItemUTPExtension;
import com.communote.server.core.vo.query.TimelineQueryParameters;
import com.communote.server.core.vo.query.filter.PropertyFilter;
import com.communote.server.core.vo.query.filter.PropertyFilter.MatchMode;
import com.communote.server.core.vo.query.logical.AtomicTagFormula;
import com.communote.server.core.vo.query.post.NoteQuery;
import com.communote.server.core.vo.query.post.NoteQueryParameters;
import com.communote.server.model.blog.Blog;
import com.communote.server.model.note.Note;
import com.communote.server.model.tag.Tag;
import com.communote.server.model.user.User;
import com.communote.server.model.user.note.UserNoteEntity;
import com.communote.server.persistence.blog.NoteDao;
import com.communote.server.persistence.tag.DefaultTagStore;
import com.communote.server.persistence.user.note.UserNoteEntityDao;
import com.communote.server.service.NoteService;
import com.communote.server.test.CommunoteIntegrationTest;
import com.communote.server.test.util.AuthenticationTestUtils;
import com.communote.server.test.util.TestUtils;
import com.communote.server.test.util.TestUtils.TestUtilsException;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class NoteQueryTest extends CommunoteIntegrationTest {

    private static final NoteQuery NOTE_QUERY = new NoteQuery();
    private User user;
    private Blog topic;

    @Autowired
    private FollowManagement followManagement;
    @Autowired
    private NoteDao noteDao;
    @Autowired
    private NoteService noteService;
    @Autowired
    private PropertyManagement propertyManagement;
    @Autowired
    private QueryManagement queryManagement;
    @Autowired
    private TagManagement tagManagement;
    @Autowired
    private TagStoreManagement tagStoreManagement;
    @Autowired
    private UserNoteEntityDao userNoteEntityDao;

    /**
     * @param blogIds
     *            The blog ids, the query should filter for.
     * @param tagPrefix
     *            Prefix for tag search.
     * @param tag
     *            Concrete tag to filter for.
     * @return {@link NoteQueryParameters}
     */
    private NoteQueryParameters createNoteQueryInstance(String tagPrefix, String tag,
            Long... blogIds) {
        NoteQueryParameters noteQueryInstance = new NoteQueryParameters();
        TaggingCoreItemUTPExtension filter = new TaggingCoreItemUTPExtension();
        filter.setBlogFilter(blogIds);
        noteQueryInstance.setTypeSpecificExtension(filter);
        noteQueryInstance.setTagPrefix(tagPrefix);
        noteQueryInstance.setLogicalTags(tag != null ? new AtomicTagFormula(tag, false) : null);
        return noteQueryInstance;
    }

    /**
     *
     * @param items
     *            the items to use
     * @return the ids of the items in a seperate list
     */
    private List<Long> getNoteIds(List<SimpleNoteListItem> items) {
        List<Long> ids = new ArrayList<Long>(items.size());
        for (SimpleNoteListItem item : items) {
            ids.add(item.getId());
        }
        return ids;
    }

    /**
     * @return {@link SimpleNoteListItemToNoteDataQueryResultConverter}
     */
    private SimpleNoteListItemToNoteDataQueryResultConverter<NoteData> getResultConverter() {
        return new SimpleNoteListItemToNoteDataQueryResultConverter<NoteData>(NoteData.class,
                new NoteRenderContext(NoteRenderMode.PORTAL, user.getLanguageLocale()));
    }

    /**
     * Setups all common parameters for this tests.
     *
     * @throws Exception
     *             in case the test failed
     */
    @BeforeClass(dependsOnGroups = "integration-test-setup")
    public void setup() throws Exception {
        user = TestUtils.createRandomUser(false);
        topic = TestUtils.createRandomBlog(true, true, user);
    }

    /**
     * Test that the filter for only followed, only direct-messages and mentioned messages can be
     * combined and the result is a disjunction of the matching notes.
     *
     * @throws Exception
     *             in case the test failed
     */
    @Test
    public void testCombinedFollowMentionDirectMessageFiltering() throws Exception {
        User user1 = TestUtils.createRandomUser(false);
        User user2 = TestUtils.createRandomUser(false);
        User user3 = TestUtils.createRandomUser(false);
        Blog topic = TestUtils.createRandomBlog(false, false, user1, user2, user3);
        TestUtils.createAndStoreCommonNote(topic, user1.getId(), "Note0");
        // mention of user 1
        Long note1Id = TestUtils.createAndStoreCommonNote(topic, user2.getId(),
                "@" + user1.getAlias() + " Note1");
        sleep(1000);
        // dm to user 1
        Long note2Id = TestUtils.createAndStoreCommonNote(topic, user2.getId(),
                "d @" + user1.getAlias() + " Note2");
        sleep(1000);
        // note of user followed user by user 1
        Long note3Id = TestUtils.createAndStoreCommonNote(topic, user3.getId(), "Note3");
        TestUtils.createAndStoreCommonNote(topic, user1.getId(), "Note4");
        TestUtils.createAndStoreCommonNote(topic, user2.getId(), "Note5");

        AuthenticationTestUtils.setSecurityContext(user1);
        followManagement.followUser(user3.getId());

        NoteQueryParameters queryParameters = new NoteQueryParameters();
        queryParameters.getTypeSpecificExtension().setBlogId(topic.getId());

        // Direct and Following
        queryParameters.setDirectMessage(true);
        queryParameters.setRetrieveOnlyFollowedItems(true);
        PageableList<SimpleNoteListItem> result = queryManagement.query(new NoteQuery(),
                queryParameters);
        Assert.assertEquals(result.size(), 2);
        Assert.assertEquals(result.get(0).getId(), note3Id);
        Assert.assertEquals(result.get(1).getId(), note2Id);

        // @me and Following
        queryParameters = NoteQueryParameters.clone(queryParameters);
        queryParameters.setDirectMessage(false);
        queryParameters.setRetrieveOnlyFollowedItems(true);
        queryParameters.setUserToBeNotified(new Long[] { user1.getId() });
        result = queryManagement.query(new NoteQuery(), queryParameters);
        Assert.assertEquals(result.size(), 3);
        Assert.assertEquals(result.get(0).getId(), note3Id);
        Assert.assertEquals(result.get(1).getId(), note2Id);
        Assert.assertEquals(result.get(2).getId(), note1Id);

        // @me, direct and following
        queryParameters = NoteQueryParameters.clone(queryParameters);
        queryParameters.setDirectMessage(true);
        queryParameters.setRetrieveOnlyFollowedItems(true);
        queryParameters.setUserToBeNotified(new Long[] { user1.getId() });
        result = queryManagement.query(new NoteQuery(), queryParameters);
        Assert.assertEquals(result.size(), 3);
        Assert.assertEquals(result.get(0).getId(), note3Id);
        Assert.assertEquals(result.get(1).getId(), note2Id);
        Assert.assertEquals(result.get(2).getId(), note1Id);
    }

    /**
     * Test that notes of certain authors can be excluded.
     */
    @Test
    public void testExcludeNotesOfAuthorById() {
        User user1 = TestUtils.createRandomUser(false);
        User user2 = TestUtils.createRandomUser(false);
        Blog topic = TestUtils.createRandomBlog(false, false, user1, user2);
        Long note11 = TestUtils.createAndStoreCommonNote(topic, user1.getId(), "11");
        Long note12 = TestUtils.createAndStoreCommonNote(topic, user1.getId(), "12");
        Long note21 = TestUtils.createAndStoreCommonNote(topic, user2.getId(), "21");
        Long note22 = TestUtils.createAndStoreCommonNote(topic, user2.getId(), "22");

        NoteQueryParameters parameters = new NoteQueryParameters();
        parameters.getTypeSpecificExtension().setBlogId(topic.getId());

        PageableList<SimpleNoteListItem> result = queryManagement.query(NOTE_QUERY, parameters);
        Assert.assertEquals(result.size(), 4);
        Assert.assertEquals(result.get(0).getId(), note22);
        Assert.assertEquals(result.get(1).getId(), note21);
        Assert.assertEquals(result.get(2).getId(), note12);
        Assert.assertEquals(result.get(3).getId(), note11);

        // Ignore first user
        parameters = new NoteQueryParameters();
        parameters.getTypeSpecificExtension().setBlogId(topic.getId());
        parameters.setUserIdsToIgnore(user1.getId());
        result = queryManagement.query(NOTE_QUERY, parameters);
        Assert.assertEquals(result.size(), 2);
        Assert.assertEquals(result.get(0).getId(), note22);
        Assert.assertEquals(result.get(1).getId(), note21);

        // Ignore second user
        parameters = new NoteQueryParameters();
        parameters.getTypeSpecificExtension().setBlogId(topic.getId());
        parameters.setUserIdsToIgnore(user2.getId());
        result = queryManagement.query(NOTE_QUERY, parameters);
        Assert.assertEquals(result.size(), 2);
        Assert.assertEquals(result.get(0).getId(), note12);
        Assert.assertEquals(result.get(1).getId(), note11);

        // Ignore both users
        parameters = new NoteQueryParameters();
        parameters.getTypeSpecificExtension().setBlogId(topic.getId());
        parameters.setUserIdsToIgnore(new Long[] { user1.getId(), user2.getId() });
        result = queryManagement.query(NOTE_QUERY, parameters);
        Assert.assertEquals(result.size(), 0);
    }

    @Test
    public void testFilterAttachments() throws TestUtilsException, AuthorizationException,
            NoteNotFoundException {
        Blog attachmentTopic = TestUtils.createRandomBlog(false, false, user);

        final NoteRenderContext context = new NoteRenderContext(null, Locale.ENGLISH);

        final Set<Long> notesWithoutAttachment = new HashSet<>();
        final Map<Long, String[]> noteToContentIds = new HashMap<>();
        for (int i = 0; i < 10; i++) {
            Long noteId = TestUtils.createAndStoreCommonNoteWithAttachments(attachmentTopic,
                    user.getId(), "Note with attachment.", null, new Date(), 0);
            notesWithoutAttachment.add(noteId);
        }

        for (int i = 0; i < 10; i++) {
            int numAttachments = i % 3 + 1;
            Long noteId = TestUtils.createAndStoreCommonNoteWithAttachments(attachmentTopic,
                    user.getId(), "Note with attachment.", null, new Date(), numAttachments);

            NoteData noteData = noteService.getNote(noteId, context);
            Assert.assertNotNull(noteData.getAttachments());
            Assert.assertEquals(noteData.getAttachments().size(), numAttachments);
            String[] contentIds = new String[numAttachments];
            int k = 0;
            for (AttachmentData item : noteData.getAttachments()) {
                contentIds[k++] = item.getContentId();
                Assert.assertEquals(item.getRepositoryId(),
                        FilesystemConnector.DEFAULT_FILESYSTEM_CONNECTOR);
            }
            noteToContentIds.put(noteId, contentIds);
        }

        NoteQueryParameters queryParameters = new NoteQueryParameters();
        queryParameters.getTypeSpecificExtension().setBlogFilter(
                new Long[] { attachmentTopic.getId() });

        // test filtering only be repository connector id
        queryParameters
                .setAttachmentRepositoryConnectorId(FilesystemConnector.DEFAULT_FILESYSTEM_CONNECTOR);

        PageableList<NoteData> result = queryManagement.query(NOTE_QUERY, queryParameters,
                getResultConverter());

        Assert.assertEquals(result.size(), noteToContentIds.size());
        for (NoteData data : result) {
            Assert.assertFalse(notesWithoutAttachment.contains(data.getId()));
            Assert.assertTrue(noteToContentIds.keySet().contains(data.getId()));

        }

        // test filtering only be repository connector id
        List<String> contentIds = new ArrayList<>();
        List<Long> notesToFilterFor = new ArrayList<>();
        List<Long> notesToNotFilterFor = new ArrayList<>();
        List<Long> allNotes = new ArrayList<>(noteToContentIds.keySet());

        for (int i = 0; i < allNotes.size(); i++) {
            if (i < allNotes.size() / 2) {
                notesToFilterFor.add(allNotes.get(i));
                contentIds.add(noteToContentIds.get(allNotes.get(i))[0]);
            } else {
                notesToNotFilterFor.add(allNotes.get(i));
            }
        }
        queryParameters.setAttachmentRepositoryConnectorId(null);
        queryParameters.setAttachmentContentIds(contentIds.toArray(new String[] { }));

        result = queryManagement.query(NOTE_QUERY, queryParameters, getResultConverter());

        Assert.assertEquals(result.size(), notesToFilterFor.size());
        for (NoteData data : result) {
            Assert.assertFalse(notesWithoutAttachment.contains(data.getId()));
            Assert.assertFalse(notesToNotFilterFor.contains(data.getId()));
            Assert.assertTrue(notesToFilterFor.contains(data.getId()));

        }

    }

    /**
     * Test that the setOrderByX can only be set to certain values if the setRetrieveOnlyX settings
     * are used.
     *
     * @throws Exception
     *             in case the test failed
     */
    @Test
    public void testForCorrectOrderOnRetrieveOnlyFilters() throws Exception {
        Blog topic = TestUtils.createRandomBlog(false, false, user);
        Timestamp creationDate = new Timestamp(System.currentTimeMillis());
        AuthenticationTestUtils.setSecurityContext(user);
        NoteStoringTO noteStoringTO = TestUtils.createCommonNote(topic, user.getId());
        noteStoringTO.setCreationDate(creationDate);
        Long noteId = noteService.createNote(noteStoringTO, null).getNoteId();

        // Before id
        NoteQueryParameters queryParameters = new NoteQueryParameters();
        queryParameters.setSortByDate(OrderDirection.DESCENDING);
        queryParameters.setSortById(OrderDirection.DESCENDING);
        queryParameters.getTypeSpecificExtension().setBlogId(topic.getId());
        queryParameters.setRetrieveOnlyNotesBeforeId(noteId);
        queryParameters.setRetrieveOnlyNotesBeforeDate(creationDate);
        queryManagement.query(NOTE_QUERY, queryParameters, getResultConverter());

        queryParameters = NoteQueryParameters.clone(queryParameters);
        queryParameters.setSortByDate(OrderDirection.ASCENDING);
        try {
            queryManagement.query(NOTE_QUERY, queryParameters, getResultConverter());
        } catch (RuntimeException exception) {
            if (!(exception.getCause() instanceof QueryConfigurationException)) {
                Assert.fail("It should not be possible to set a ASCENDING ordering");
            }
        }
        queryParameters = NoteQueryParameters.clone(queryParameters);
        queryParameters.setSortByDate(OrderDirection.DESCENDING);
        queryParameters.setSortById(OrderDirection.ASCENDING);
        try {
            queryManagement.query(NOTE_QUERY, queryParameters, getResultConverter());
            Assert.fail("It should not be possible to set a ASCENDING ordering");
        } catch (RuntimeException exception) {
            if (!(exception.getCause() instanceof QueryConfigurationException)) {
                Assert.fail("It should not be possible to set a ASCENDING ordering");
            }
        }

        // After Id
        queryParameters = NoteQueryParameters.clone(queryParameters);
        queryParameters.setRetrieveOnlyNotesAfterId(null);
        queryParameters.setRetrieveOnlyNotesAfterDate(null);
        queryParameters.setSortByDate(OrderDirection.DESCENDING);
        queryParameters.setSortById(OrderDirection.DESCENDING);
        queryManagement.query(NOTE_QUERY, queryParameters, getResultConverter());

        queryParameters = NoteQueryParameters.clone(queryParameters);
        queryParameters.setSortByDate(OrderDirection.ASCENDING);
        queryParameters.setSortById(OrderDirection.DESCENDING);
        try {
            queryManagement.query(NOTE_QUERY, queryParameters, getResultConverter());
            Assert.fail("It should not be possible to set a ASCENDING ordering");
        } catch (RuntimeException exception) {
            if (!(exception.getCause() instanceof QueryConfigurationException)) {
                Assert.fail("It should not be possible to set a ASCENDING ordering");
            }
        }
        queryParameters = NoteQueryParameters.clone(queryParameters);
        queryParameters.setSortByDate(OrderDirection.DESCENDING);
        queryParameters.setSortById(OrderDirection.ASCENDING);
        try {
            queryManagement.query(NOTE_QUERY, queryParameters, getResultConverter());
            Assert.fail("It should not be possible to set a ASCENDING ordering");
        } catch (RuntimeException exception) {
            if (!(exception.getCause() instanceof QueryConfigurationException)) {
                Assert.fail("It should not be possible to set a ASCENDING ordering");
            }
        }
    }

    /**
     * Tests that it is possible to filter for note properties. It tests that properties are
     * correctly rendered within the named query.
     *
     * @throws Exception
     *             in case the test failed
     */
    @Test
    public void testNoteProperties() throws Exception {
        User user = TestUtils.createRandomUser(false);
        Blog topic = TestUtils.createRandomBlog(true, true, user);
        TestUtils.createAndStoreCommonNote(topic, user.getId(), random());

        String[] property1 = { random(), random() };
        String[] property2 = { random(), random() };

        propertyManagement.addObjectPropertyFilter(PropertyType.NoteProperty, property1[0],
                property1[0]);
        propertyManagement.addObjectPropertyFilter(PropertyType.NoteProperty, property2[0],
                property2[0]);

        Long note1 = noteService.createNote(
                TestUtils.createCommonNote(topic, user.getId(), UUID.randomUUID().toString(),
                        property1), null).getNoteId();
        Long note2 = noteService.createNote(
                TestUtils.createCommonNote(topic, user.getId(), UUID.randomUUID().toString(),
                        property2), null).getNoteId();

        AuthenticationTestUtils.setSecurityContext(user);
        NoteQueryParameters queryInstance = createNoteQueryInstance(null, null, topic.getId());
        PageableList<NoteData> queryResult = queryManagement.query(NOTE_QUERY, queryInstance,
                getResultConverter());
        Assert.assertEquals(queryResult.size(), 3);

        PropertyFilter filter = new PropertyFilter(property1[0], Note.class);
        filter.addProperty(property1[0], property1[1], MatchMode.EQUALS);
        queryInstance.addPropertyFilter(filter);
        PageableList<NoteData> result = queryManagement.query(NOTE_QUERY, queryInstance,
                getResultConverter());
        Assert.assertEquals(result.size(), 1);
        Assert.assertEquals(result.get(0).getId(), note1);

        filter = new PropertyFilter(property2[0], Note.class);
        filter.addProperty(property2[0], property2[1], MatchMode.CONTAINS);
        queryInstance.getPropertyFilters().clear();
        queryInstance.addPropertyFilter(filter);
        result = queryManagement.query(NOTE_QUERY, queryInstance, getResultConverter());
        Assert.assertEquals(result.size(), 1);
        Assert.assertEquals(result.get(0).getId(), note2);
    }

    /**
     * Test the rank filtering
     *
     * @throws Exception
     *             in case of an error
     */
    @Test
    public void testRankFilters() throws Exception {
        // create some notes
        Blog testBlog = TestUtils.createRandomBlog(true, true, user);
        Long note1 = noteService.createNote(TestUtils.createCommonNote(testBlog, user.getId()),
                null).getNoteId();
        Long note2 = noteService.createNote(TestUtils.createCommonNote(testBlog, user.getId()),
                null).getNoteId();
        Long note3 = noteService.createNote(TestUtils.createCommonNote(testBlog, user.getId()),
                null).getNoteId();
        noteService.createNote(TestUtils.createCommonNote(testBlog, user.getId()), null)
        .getNoteId();

        // create some ranks
        UserNoteEntity userNoteEntity1 = UserNoteEntity.Factory.newInstance();
        UserNoteEntity userNoteEntity2 = UserNoteEntity.Factory.newInstance();
        UserNoteEntity userNoteEntity3 = UserNoteEntity.Factory.newInstance();
        userNoteEntity1.setUser(user);
        userNoteEntity2.setUser(user);
        userNoteEntity3.setUser(user);

        userNoteEntity1.setNote(noteDao.load(note1));
        userNoteEntity2.setNote(noteDao.load(note2));
        userNoteEntity3.setNote(noteDao.load(note3));

        userNoteEntity1.setRankNormalized(0.1);
        userNoteEntity2.setRankNormalized(0.5);
        userNoteEntity3.setRankNormalized(0.9);

        userNoteEntityDao.create(userNoteEntity1);
        userNoteEntityDao.create(userNoteEntity2);
        userNoteEntityDao.create(userNoteEntity3);

        // now filter
        NoteQueryParameters noteQueryParameters = createNoteQueryInstance(null, null,
                testBlog.getId());
        noteQueryParameters.setMinimumRank(0.4);
        List<SimpleNoteListItem> queryResult = queryManagement.executeQueryComplete(NOTE_QUERY,
                noteQueryParameters);

        Assert.assertEquals(queryResult.size(), 2);
        Assert.assertTrue(getNoteIds(queryResult).contains(note2));
        Assert.assertTrue(getNoteIds(queryResult).contains(note3));

        // filter again for maximum rank
        noteQueryParameters.setMinimumRank(null);
        noteQueryParameters.setMaximumRank(0.75);
        queryResult = queryManagement.executeQueryComplete(NOTE_QUERY, noteQueryParameters);
        Assert.assertTrue(getNoteIds(queryResult).contains(note1));
        Assert.assertTrue(getNoteIds(queryResult).contains(note2));
    }

    /**
     * Test that the result contains the expected notes in correct order when the
     * setRetrieveOnlyNotesX settings are used.
     *
     * @throws Exception
     *             in case the test failed
     */
    @Test
    public void testRetrieveOnly() throws Exception {
        Blog blog = TestUtils.createRandomBlog(false, false, user);
        ArrayList<Long> noteIds = new ArrayList<Long>();
        Timestamp creationDate = new Timestamp(System.currentTimeMillis());
        AuthenticationTestUtils.setSecurityContext(user);
        for (int i = 0; i <= new Random().nextInt(10) + 10; i++) {
            NoteStoringTO noteStoringTO = TestUtils.createCommonNote(blog, user.getId());
            noteStoringTO.setCreationDate(creationDate);
            Long noteId = noteService.createNote(noteStoringTO, null).getNoteId();
            noteIds.add(noteId);
        }
        // get actual stored date from database, requires evicting the cache because of MySQLs
        // date values which are only exact to the second
        ServiceLocator.instance().getService("sessionFactory", SessionFactoryImpl.class).getCache()
        .evictEntity(Note.class, noteIds.get(0));
        Timestamp storedCreationDate = noteDao.load(noteIds.get(0)).getCreationDate();

        NoteQueryParameters queryParameters = new NoteQueryParameters();
        queryParameters.getTypeSpecificExtension().setBlogId(blog.getId());
        for (Long noteId : noteIds) {
            // Test for notes before the given note.
            queryParameters.setRetrieveOnlyNotesBeforeId(noteId);
            queryParameters.setRetrieveOnlyNotesBeforeDate(storedCreationDate);
            PageableList<NoteData> result = queryManagement.query(NOTE_QUERY, queryParameters,
                    getResultConverter());
            List<Long> subList = noteIds.subList(0, noteIds.indexOf(noteId));
            Assert.assertEquals(result.size(), subList.size());
            for (NoteData note : result) {
                Assert.assertEquals(subList.get(0), note.getId());
                subList.remove(note.getId());
            }

            // Test for notes after the given note.
            queryParameters = new NoteQueryParameters();
            queryParameters.getTypeSpecificExtension().setBlogId(blog.getId());
            queryParameters.setRetrieveOnlyNotesAfterId(noteId);
            result = queryManagement.query(NOTE_QUERY, queryParameters, getResultConverter());
            subList = noteIds.subList(noteIds.indexOf(noteId) + 1, noteIds.size());
            Assert.assertEquals(result.size(), subList.size());
            for (NoteData note : result) {
                Assert.assertEquals(subList.get(subList.size() - 1), note.getId());
                subList.remove(note.getId());
            }
        }
    }

    /**
     * This method tests, that it is possible to retrieve notes as the internal system user.
     *
     * @throws Exception
     *             in case the test failed
     */
    @Test
    public void testSearchForNotesAsInternalSystemUser() throws Exception {
        User user = TestUtils.createRandomUser(false);
        Blog topic = TestUtils.createRandomBlog(false, false, user);
        TestUtils.createAndStoreCommonNote(topic, user.getId(), random());
        NoteQueryParameters queryInstance = createNoteQueryInstance(null, null, topic.getId());
        AuthenticationHelper.setInternalSystemToSecurityContext();
        PageableList<NoteData> queryResult = queryManagement.query(NOTE_QUERY, queryInstance,
                getResultConverter());
        Assert.assertEquals(queryResult.size(), 1);
    }

    /**
     * Tests, that it is possible to filter for tags by their id.
     * {@link TimelineQueryParameters#addUserTagIds(Long)}
     *
     * @throws Exception
     *             in case the test failed
     */
    @Test
    public void testSearchForTagsById() throws Exception {
        Tag tag1 = tagManagement.storeTag(new TagTO(random(), TagStoreType.Types.NOTE));
        Tag tag2 = tagManagement.storeTag(new TagTO(random(), TagStoreType.Types.NOTE));
        User user = TestUtils.createRandomUser(false);
        Blog blog = TestUtils.createRandomBlog(true, true, user);
        TestUtils.createAndStoreCommonNote(blog, user.getId(), "Note");
        TestUtils.createAndStoreCommonNote(blog, user.getId(), "Note #" + tag1.getDefaultName());
        TestUtils.createAndStoreCommonNote(blog, user.getId(), "Note #" + tag1.getDefaultName()
                + " #" + tag2.getDefaultName());
        NoteQueryParameters queryInstance = createNoteQueryInstance(null, null);
        queryInstance.addTagId(tag1.getId());
        PageableList<NoteData> result = queryManagement.query(NOTE_QUERY, queryInstance,
                getResultConverter());
        Assert.assertEquals(result.getMinNumberOfElements(), 2);
        queryInstance = createNoteQueryInstance(null, null);
        queryInstance.addTagId(tag2.getId());
        result = queryManagement.query(NOTE_QUERY, queryInstance, getResultConverter());
        Assert.assertEquals(result.getMinNumberOfElements(), 1);
        queryInstance = createNoteQueryInstance(null, null);
        queryInstance.addTagId(tag1.getId());
        queryInstance.addTagId(tag2.getId());
        result = queryManagement.query(NOTE_QUERY, queryInstance, getResultConverter());
        Assert.assertEquals(result.getMinNumberOfElements(), 1);

    }

    /**
     * Tests, that it is possible to filter tags by TagStore.
     * {@link TimelineQueryParameters#addTagStoreTagId(String, String)}
     *
     * @throws Exception
     *             in case the test failed
     */
    @Test
    public void testSearchForTagsByTagStore() throws Exception {
        User user = TestUtils.createRandomUser(false);
        Blog blog = TestUtils.createRandomBlog(true, true, user);
        Tag tag1 = tagManagement.storeTag(new TagTO(random(), TagStoreType.Types.NOTE));
        // added tag will be found in default tag store for notes
        Long noteId1 = TestUtils.createAndStoreCommonNote(blog, user.getId(),
                "Note #" + tag1.getDefaultName());
        // create a new TagStore with a higher priority so that it has precedence for note tags
        int prio = tagStoreManagement.getTagStore(TagStoreType.Types.NOTE).getOrder() + 1;
        DefaultTagStore newTagStore = new DefaultTagStore(random(), prio, false,
                TagStoreType.Types.NOTE);
        tagStoreManagement.addTagStore(newTagStore);
        // will be saved in new tag store because of higher prio
        Tag tag2 = tagManagement.storeTag(new TagTO(random(), TagStoreType.Types.NOTE));
        // tags will be searched in new tag store and tag1 will be created there
        Long noteId2 = TestUtils.createAndStoreCommonNote(blog, user.getId(),
                "Note #" + tag1.getDefaultName() + " #" + tag2.getDefaultName());
        Long noteId3 = TestUtils.createAndStoreCommonNote(blog, user.getId(),
                "Note #" + tag2.getDefaultName());

        NoteQueryParameters queryInstance = createNoteQueryInstance(null, null);
        queryInstance.addTagStoreTagId(tag1.getTagStoreAlias(), tag1.getTagStoreTagId());
        PageableList<NoteData> result = queryManagement.query(NOTE_QUERY, queryInstance,
                getResultConverter());
        // expect only one result (note1), because 2nd note is tagged with tag from new tagstore
        Assert.assertEquals(result.getMinNumberOfElements(), 1);
        Assert.assertEquals(result.get(0).getId(), noteId1);
        queryInstance.addTagStoreTagId(tag2.getTagStoreAlias(), tag2.getTagStoreTagId());
        result = queryManagement.query(NOTE_QUERY, queryInstance, getResultConverter());
        // expect 0 results because no note is tagged with tags from both stores
        Assert.assertEquals(result.getMinNumberOfElements(), 0);

        queryInstance = createNoteQueryInstance(null, null);
        queryInstance.addTagStoreTagId(tag2.getTagStoreAlias(), tag2.getTagStoreTagId());
        result = queryManagement.query(NOTE_QUERY, queryInstance, getResultConverter());
        Assert.assertEquals(result.getMinNumberOfElements(), 2);
        Assert.assertTrue(result.get(0).getId().equals(noteId2)
                || result.get(0).getId().equals(noteId3));
        Assert.assertTrue(result.get(1).getId().equals(noteId2)
                || result.get(1).getId().equals(noteId3));
        Assert.assertNotEquals(result.get(0).getId(), result.get(1).getId());
        // find tag with name of tag1 in new tagstore, which was added while creating 2nd note
        Tag tag3 = tagManagement.findTag(tag1.getDefaultName(), TagStoreType.Types.NOTE);
        Assert.assertNotNull(tag3);
        Assert.assertEquals(tag3.getTagStoreAlias(), tag2.getTagStoreAlias());
        queryInstance.addTagStoreTagId(tag3.getTagStoreAlias(), tag3.getTagStoreTagId());
        result = queryManagement.query(NOTE_QUERY, queryInstance, getResultConverter());
        // only 2nd note is tagged with both tags
        Assert.assertEquals(result.getMinNumberOfElements(), 1);
        Assert.assertEquals(result.get(0).getId(), noteId2);
        tagStoreManagement.removeTagStore(newTagStore);
    }

    /**
     * Test for {@link TimelineQueryParameters#setUserAliases(java.util.Set)}
     */
    @Test
    public void testSearchForUserAliases() {
        User user1 = TestUtils.createRandomUser(false);
        User user2 = TestUtils.createRandomUser(false);
        User user3 = TestUtils.createRandomUser(false);
        Blog blog = TestUtils.createRandomBlog(true, true, user1, user2, user3);
        TestUtils.createAndStoreCommonNote(blog, user1.getId(), "Beitrag 1");
        TestUtils.createAndStoreCommonNote(blog, user2.getId(), "Beitrag 2");
        TestUtils.createAndStoreCommonNote(blog, user3.getId(), "Beitrag 3");
        NoteQueryParameters queryInstance = createNoteQueryInstance(null, null, blog.getId());
        PageableList<NoteData> queryResult = queryManagement.query(NOTE_QUERY, queryInstance,
                getResultConverter());
        Assert.assertEquals(queryResult.size(), 3);
        queryInstance.getUserAliases().add(user1.getAlias());
        queryResult = queryManagement.query(NOTE_QUERY, queryInstance, getResultConverter());
        Assert.assertEquals(queryResult.size(), 1);
        queryInstance.getUserAliases().add(user2.getAlias());
        queryResult = queryManagement.query(NOTE_QUERY, queryInstance, getResultConverter());
        Assert.assertEquals(queryResult.size(), 2);
        queryInstance.getUserAliases().add(user3.getAlias());
        queryResult = queryManagement.query(NOTE_QUERY, queryInstance, getResultConverter());
        Assert.assertEquals(queryResult.size(), 3);
    }

    /**
     * This tests, that it is possible to search for a tag prefix and filter the searched results
     * for tags.
     */
    @Test
    public void testSearchTagAndTagFilter() {
        Blog blog = TestUtils.createRandomBlog(true, true, user);
        String tag1 = "tag1a";
        String tag2 = "tag2a";
        String tag3 = "tag3a";
        TestUtils.createAndStoreCommonNote(blog, user.getId(), "Beitrag #" + tag1);
        TestUtils.createAndStoreCommonNote(blog, user.getId(), "Beitrag #" + tag2);
        TestUtils.createAndStoreCommonNote(blog, user.getId(), "Beitrag #" + tag3);

        AuthenticationTestUtils.setSecurityContext(user);
        NoteQueryParameters queryInstance = createNoteQueryInstance(null, null, blog.getId());
        PageableList<NoteData> queryResult = queryManagement.query(NOTE_QUERY, queryInstance,
                getResultConverter());
        Assert.assertEquals(queryResult.size(), 3);
        queryInstance = createNoteQueryInstance("tag", null, blog.getId());
        queryResult = queryManagement.query(NOTE_QUERY, queryInstance, getResultConverter());
        Assert.assertEquals(3, queryResult.size());
        queryInstance = createNoteQueryInstance("tag2", null, blog.getId());
        queryResult = queryManagement.query(NOTE_QUERY, queryInstance, getResultConverter());
        Assert.assertEquals(1, queryResult.size());
        queryInstance = createNoteQueryInstance("tag2", tag1, blog.getId());
        queryResult = queryManagement.query(NOTE_QUERY, queryInstance, getResultConverter());
        Assert.assertEquals(0, queryResult.size());
        queryInstance = createNoteQueryInstance(null, tag1, blog.getId());
        queryResult = queryManagement.query(NOTE_QUERY, queryInstance, getResultConverter());
        Assert.assertEquals(1, queryResult.size());
        queryInstance = createNoteQueryInstance("tag", tag1, blog.getId());
        queryResult = queryManagement.query(NOTE_QUERY, queryInstance, getResultConverter());
        Assert.assertEquals(1, queryResult.size());
        queryInstance = createNoteQueryInstance("tag1", tag1, blog.getId());
        queryResult = queryManagement.query(NOTE_QUERY, queryInstance, getResultConverter());
        Assert.assertEquals(1, queryResult.size());
    }

    /**
     * Test for {@link TaggingCoreItemUTPExtension#setShowDiscussionParticipation(boolean)}
     *
     * @throws Exception
     *             in case the test failed
     */
    @Test
    public void testShowDiscussionParticipation() throws Exception {
        User user1 = TestUtils.createRandomUser(false);
        User user2 = TestUtils.createRandomUser(false);
        Blog blog = TestUtils.createRandomBlog(true, true, user1, user2);
        NoteStoringTO parent = TestUtils.createCommonNote(blog, user1.getId());
        NoteStoringTO child = TestUtils.createCommonNote(blog, user2.getId());
        Long parentId = noteService.createNote(parent, null).getNoteId();
        child.setParentNoteId(parentId);
        noteService.createNote(child, null);
        NoteQuery query = new NoteQuery();
        NoteQueryParameters queryParameter = new NoteQueryParameters();
        TaggingCoreItemUTPExtension extension = queryParameter.getTypeSpecificExtension();
        AuthenticationTestUtils.setSecurityContext(user1);
        queryParameter.setUserToBeNotified(new Long[] { user1.getId() });
        PageableList<SimpleNoteListItem> result = queryManagement.query(query, queryParameter);
        Assert.assertEquals(0, result.getMinNumberOfElements());
        extension.setShowDiscussionParticipation(true);
        result = queryManagement.query(query, queryParameter);
        Assert.assertEquals(2, result.getMinNumberOfElements());

        // The third user isn't an participant of the discussion -> should not get any results.
        User thirdUser = TestUtils.createRandomUser(false);
        extension.setUserId(thirdUser.getId());
        queryParameter.setUserToBeNotified(new Long[] { thirdUser.getId() });
        AuthenticationTestUtils.setSecurityContext(thirdUser);
        result = queryManagement.query(query, queryParameter);
        Assert.assertEquals(0, result.getMinNumberOfElements());
    }

    /**
     * Tests the functionality for filtering only discussions.
     *
     * @throws Exception
     *             in case the test failed
     */
    @Test
    public void testShowOnlyDiscussions() throws Exception {
        int numberOfNonDiscussions = RandomUtils.nextInt(10) + 10;
        int numberOfDiscussions = RandomUtils.nextInt(10) + 10;
        for (int i = 0; i < numberOfNonDiscussions; i++) {
            TestUtils.createAndStoreCommonNote(topic, user.getId(), "Note" + i);
        }
        int numberOfAnswers = 0;
        for (int i = 0; i < numberOfDiscussions; i++) {
            Long parentId = TestUtils.createAndStoreCommonNote(topic, user.getId(), "Parentnote"
                    + i);
            for (int e = 0; e <= RandomUtils.nextInt(100); e++) {
                Long answerId = TestUtils.createAndStoreCommonNote(topic, user.getId(), "Answer",
                        parentId);
                TestUtils.createAndStoreCommonNote(topic, user.getId(), "AnswerAnswer", answerId);
                numberOfAnswers += 2;
            }
        }
        NoteQueryParameters noteQueryParameters = createNoteQueryInstance(null, null, topic.getId());
        List<SimpleNoteListItem> queryResult = queryManagement.executeQueryComplete(NOTE_QUERY,
                noteQueryParameters);
        Assert.assertEquals(queryResult.size(), numberOfAnswers + numberOfNonDiscussions
                + numberOfDiscussions);
        noteQueryParameters.setDiscussionFilterMode(DiscussionFilterMode.IS_DISCUSSION);
        queryResult = queryManagement.executeQueryComplete(NOTE_QUERY, noteQueryParameters);
        Assert.assertEquals(queryResult.size(), numberOfAnswers + numberOfDiscussions);
    }
}
