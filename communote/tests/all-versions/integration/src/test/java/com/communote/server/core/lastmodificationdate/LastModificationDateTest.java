package com.communote.server.core.lastmodificationdate;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.communote.common.converter.Converter;
import com.communote.common.converter.IdentityConverter;
import com.communote.server.api.core.blog.BlogManagement;
import com.communote.server.api.core.blog.BlogNotFoundException;
import com.communote.server.api.core.blog.BlogRightsManagement;
import com.communote.server.api.core.blog.BlogTO;
import com.communote.server.api.core.note.NoteContentType;
import com.communote.server.api.core.note.NoteManagementAuthorizationException;
import com.communote.server.api.core.note.NoteStoringFailDefinition;
import com.communote.server.api.core.note.NoteStoringTO;
import com.communote.server.api.core.note.processor.NoteStoringPreProcessorException;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.core.lastmodifieddate.LastModificationDateManagement;
import com.communote.server.core.security.AuthenticationHelper;
import com.communote.server.core.vo.blog.NoteModificationResult;
import com.communote.server.core.vo.blog.NoteModificationStatus;
import com.communote.server.model.blog.Blog;
import com.communote.server.model.blog.BlogRole;
import com.communote.server.model.note.Note;
import com.communote.server.model.note.NoteStatus;
import com.communote.server.model.user.User;
import com.communote.server.persistence.lastmodifieddate.LastModificationDate;
import com.communote.server.persistence.lastmodifieddate.StandardLastModificationDateFactory;
import com.communote.server.service.NoteService;
import com.communote.server.test.CommunoteIntegrationTest;
import com.communote.server.test.util.AuthenticationTestUtils;
import com.communote.server.test.util.TestUtils;

/**
 * Test the last modification date methods
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class LastModificationDateTest extends CommunoteIntegrationTest {

    private static final class EasyNoteToNoteStoringTOConverter implements
            Converter<Note, NoteStoringTO> {
        @Override
        public NoteStoringTO convert(Note source) {
            NoteStoringTO store = new NoteStoringTO();

            store.setContent(source.getContent().getContent());
            store.setBlogId(source.getBlog().getId());
            store.setIsDirectMessage(source.isDirect());
            store.setPublish(NoteStatus.PUBLISHED.equals(source.getStatus()));
            store.setCreatorId(source.getUser().getId());
            store.setCreationSource(source.getCreationSource());
            store.setContentType(NoteContentType.HTML);
            store.setFailDefinition(new NoteStoringFailDefinition());
            store.setCreationDate(new Timestamp(new Date().getTime()));

            return store;
        }
    }

    @Autowired
    private LastModificationDateManagement lastModificationDateManagement;

    @Autowired
    private NoteService noteService;

    @Autowired
    private BlogManagement blogManagement;

    @Autowired
    private BlogRightsManagement blogRightsManagement;

    private final StandardLastModificationDateFactory lastModificationDateFactory = new StandardLastModificationDateFactory();

    private final EasyNoteToNoteStoringTOConverter converter = new EasyNoteToNoteStoringTOConverter();

    private Collection<Long> createSomeAutosaveNotes(User user, Blog topic)
            throws BlogNotFoundException,
            NoteManagementAuthorizationException, NoteStoringPreProcessorException {

        final Collection<Long> notesNotToBeReturned = new HashSet<>();

        // create some autosaved notes
        for (int i = 0; i < 5; i++) {

            NoteStoringTO noteStoring = TestUtils.createCommonAutosave(topic, user.getId(), "Note"
                    + i);

            NoteModificationResult noteModificationResult = noteService.createNote(noteStoring,
                    null, null);

            notesNotToBeReturned.add(noteModificationResult.getNoteId());

        }

        return notesNotToBeReturned;
    }

    private Map<Long, Date> createsSomeNotes(User user, Blog topic) {

        final Map<Long, Date> expectedResults = new HashMap<>();

        // create some notes
        for (int i = 0; i < 20; i++) {

            Long noteId = TestUtils.createAndStoreCommonNote(topic, user.getId(), "Note" + i);

            Note note = noteService.getNote(noteId, new IdentityConverter<Note>());

            expectedResults.put(noteId, note.getLastModificationDate());
        }
        return expectedResults;
    }

    private Map<Long, Date> createsSomeTopics(User user) {

        final Map<Long, Date> expectedResults = new HashMap<>();

        // create some notes
        for (int i = 0; i < 20; i++) {

            Blog blog = TestUtils.createRandomBlog(false, false, user);

            expectedResults.put(blog.getId(), blog.getCrawlLastModificationDate());
        }
        return expectedResults;
    }

    /**
     * Test for {@link LastModificationDateManagement#getNoteCrawlLastModificationDate(Long)} and
     * {@link LastModificationDateManagement#getAttachmentCrawlLastModificationDate(Long)}
     *
     * @throws Exception
     *             in case the test failed
     */
    @Test
    public void testGetNoteAndGetAttachmentCrawlLastModificationDate() throws Exception {
        User topicManager = TestUtils.createRandomUser(false);
        User noteCreator = TestUtils.createRandomUser(false);
        Blog topic = TestUtils.createRandomBlog(false, false, topicManager, noteCreator);
        AuthenticationTestUtils.setSecurityContext(noteCreator);
        // create old note, crawl modification date should be taken from topic
        String orgContent = RandomStringUtils.randomAlphanumeric(100).replace('b', ' ');
        Long attachmentId = TestUtils.createAttachment().getId();
        Long noteId = TestUtils.createAndStoreCommonNote(topic, noteCreator.getId(), orgContent,
                new Long[] { attachmentId }, null, new Date(
                        System.currentTimeMillis() - 300000L));
        Date noteCrawlModDate = lastModificationDateManagement
                .getNoteCrawlLastModificationDate(noteId);
        Date topicCrawlModDate = lastModificationDateManagement
                .getTopicCrawlLastModificationDate(topic.getId());
        Date attachmentLastModDate = lastModificationDateManagement
                .getAttachmentCrawlLastModificationDate(attachmentId);
        Assert.assertEquals(noteCrawlModDate.getTime(), topicCrawlModDate.getTime());
        Assert.assertEquals(attachmentLastModDate.getTime(), noteCrawlModDate.getTime());
        // update note, crawl modification date should be taken from note
        Thread.sleep(1000);
        NoteStoringTO noteTO = TestUtils.createCommonNote(topic, noteCreator.getId());
        noteTO.setContent(orgContent + " edited");
        noteTO.setAttachmentIds(new Long[] { attachmentId });
        NoteModificationResult result = noteService.updateNote(noteTO, noteId, null, false);
        Assert.assertEquals(result.getStatus(), NoteModificationStatus.SUCCESS);
        noteCrawlModDate = lastModificationDateManagement.getNoteCrawlLastModificationDate(noteId);
        topicCrawlModDate = lastModificationDateManagement.getTopicCrawlLastModificationDate(topic
                .getId());
        attachmentLastModDate = lastModificationDateManagement
                .getAttachmentCrawlLastModificationDate(attachmentId);
        Assert.assertTrue(noteCrawlModDate.getTime() > topicCrawlModDate.getTime());
        Date noteLastModDate = noteService.getNote(noteId, new IdentityConverter<Note>())
                .getLastModificationDate();
        Assert.assertEquals(noteCrawlModDate.getTime(), noteLastModDate.getTime());
        Assert.assertEquals(attachmentLastModDate.getTime(), noteCrawlModDate.getTime());
        // change topic, crawl modification date should be taken from topic
        AuthenticationTestUtils.setSecurityContext(topicManager);
        Thread.sleep(1000);
        BlogTO blogDetails = new BlogTO(false, false, topic.getTitle(), topic.isCreateSystemNotes());
        blogDetails.setDescription("Modified description from " + System.currentTimeMillis());
        blogDetails.setNameIdentifier(topic.getNameIdentifier());
        blogManagement.updateBlog(topic.getId(), blogDetails);
        noteCrawlModDate = lastModificationDateManagement.getNoteCrawlLastModificationDate(noteId);
        topicCrawlModDate = lastModificationDateManagement.getTopicCrawlLastModificationDate(topic
                .getId());
        attachmentLastModDate = lastModificationDateManagement
                .getAttachmentCrawlLastModificationDate(attachmentId);
        Date topicLastModDate = blogManagement.getBlogById(topic.getId(),
                new IdentityConverter<Blog>()).getLastModificationDate();
        noteLastModDate = noteService.getNote(noteId, new IdentityConverter<Note>())
                .getLastModificationDate();
        Assert.assertTrue(noteLastModDate.getTime() < noteCrawlModDate.getTime());
        Assert.assertEquals(noteCrawlModDate.getTime(), topicCrawlModDate.getTime());
        Assert.assertEquals(noteCrawlModDate.getTime(), topicLastModDate.getTime());
        Assert.assertEquals(attachmentLastModDate.getTime(), noteCrawlModDate.getTime());
        // check evaluation of access rights
        blogRightsManagement.removeMemberByEntityId(topic.getId(), noteCreator.getId());
        AuthenticationTestUtils.setSecurityContext(noteCreator);
        try {
            lastModificationDateManagement.getNoteCrawlLastModificationDate(noteId);
            Assert.fail("User without access to note must have no access to last modification date");
        } catch (AuthorizationException e) {
            // expected
        }
        try {
            lastModificationDateManagement.getAttachmentCrawlLastModificationDate(attachmentId);
            Assert.fail("User without access to note must have no access to last modification date of attachment");
        } catch (AuthorizationException e) {
            // expected
        }
    }

    /**
     * Test for {@link LastModificationDateManagement#getTopicCrawlLastModificationDate(Long)}
     *
     * @throws Exception
     *             in case the test failed
     */
    @Test
    public void testGetTopicCrawlLastModificationDate() throws Exception {
        User topicManager = TestUtils.createRandomUser(false);
        Long topicId = TestUtils.createRandomBlog(false, false, topicManager).getId();
        // test that crawl last modification date takes changes to topic access into account
        Thread.sleep(1000);
        Long newMemberUserId = TestUtils.createRandomUser(false).getId();
        AuthenticationTestUtils.setSecurityContext(topicManager);
        blogRightsManagement.addEntity(topicId, newMemberUserId, BlogRole.VIEWER);
        Date crawlModDate = lastModificationDateManagement
                .getTopicCrawlLastModificationDate(topicId);
        Date lastModDate = blogManagement.getBlogById(topicId, new IdentityConverter<Blog>())
                .getLastModificationDate();
        Assert.assertTrue(lastModDate.getTime() < crawlModDate.getTime());
        // test access check
        User unauthorizedUser = TestUtils.createRandomUser(false);
        AuthenticationTestUtils.setSecurityContext(unauthorizedUser);
        try {
            lastModificationDateManagement.getTopicCrawlLastModificationDate(topicId);
            Assert.fail("User without access to topic must have no access to last modification date");
        } catch (AuthorizationException e) {
            // expected
        }
    }

    @Test
    public void testLastModificationDatesAfterEditNote() throws Exception {

        // preparing stuff
        User user = TestUtils.createRandomUser(false);
        Blog topic = TestUtils.createRandomBlog(false, false, user);

        final Map<Long, Date> expectedResults = createsSomeNotes(user, topic);

        AuthenticationHelper.setInternalSystemToSecurityContext();

        List<LastModificationDate> dates = lastModificationDateManagement
                .getNoteCrawlLastModificationDates(lastModificationDateFactory);

        validateResults(dates, expectedResults, null);

        // edit a note
        Long noteId = expectedResults.keySet().iterator().next();

        Date before = expectedResults.get(noteId);
        Assert.assertNotNull(before);
        NoteStoringTO storing = noteService.getNote(noteId, converter);

        noteService.updateNote(storing, noteId, null, false);
        Note note = noteService.getNote(noteId, new IdentityConverter<Note>());

        Assert.assertTrue(note.getLastModificationDate().getTime() > before.getTime());
        expectedResults.put(noteId, note.getLastModificationDate());

        dates = lastModificationDateManagement
                .getNoteCrawlLastModificationDates(lastModificationDateFactory);
        validateResults(dates, expectedResults, null);
    }

    @Test
    public void testLastModificationDatesAfterMoveNote() throws Exception {

        // preparing stuff
        User user = TestUtils.createRandomUser(false);
        Blog topic = TestUtils.createRandomBlog(false, false, user);
        Blog newTopic = TestUtils.createRandomBlog(false, false, user);

        final Map<Long, Date> expectedResults = createsSomeNotes(user, topic);

        AuthenticationHelper.setInternalSystemToSecurityContext();

        List<LastModificationDate> dates = lastModificationDateManagement
                .getNoteCrawlLastModificationDates(lastModificationDateFactory);

        validateResults(dates, expectedResults, null);

        // edit a note
        Long noteId = expectedResults.keySet().iterator().next();

        Date before = expectedResults.get(noteId);
        Assert.assertNotNull(before);
        NoteStoringTO storing = noteService.getNote(noteId, converter);

        storing.setBlogId(newTopic.getId());
        noteService.updateNote(storing, noteId, null, false);
        noteService.getNote(noteId, new IdentityConverter<Note>());

        /**
         * Uncomment this to test the change of the last modification date if the note is moved
         */
        /**
         * Assert.assertTrue(note.getLastModificationDate().getTime() > before.getTime());
         * expectedResults.put(noteId, note.getLastModificationDate());
         *
         * dates = lastModificationDateManagement
         * .getNoteLastModificationDates(lastModificationDateFactory); validateResults(dates,
         * expectedResults, null);
         */
    }

    @Test
    public void testLastModificationDatesAfterTopicEdit() throws Exception {

        User user = TestUtils.createRandomUser(false);

        final Map<Long, Date> expectedResults = createsSomeTopics(user);

        AuthenticationHelper.setInternalSystemToSecurityContext();

        List<LastModificationDate> dates = lastModificationDateManagement
                .getTopicCrawlLastModificationDates(lastModificationDateFactory);

        validateResults(dates, expectedResults, null);

        // edit a topic
        Long topicId = expectedResults.keySet().iterator().next();

        Date before = expectedResults.get(topicId);
        Assert.assertNotNull(before);
        Blog topic = blogManagement.getBlogById(topicId, false);

        BlogTO topicDetails = new BlogTO();
        topicDetails.setTitle(topic.getTitle());
        topicDetails.setDescription(topic.getDescription() + " Changed on " + new Date());
        topic = blogManagement.updateBlog(topicId, topicDetails);
        topic = blogManagement.getBlogById(topicId, false);

        Assert.assertTrue(topic.getCrawlLastModificationDate().getTime() > before.getTime(),
                " current=" + topic.getCrawlLastModificationDate() + " before=" + before);
        expectedResults.put(topicId, topic.getCrawlLastModificationDate());

        dates = lastModificationDateManagement
                .getTopicCrawlLastModificationDates(lastModificationDateFactory);
        validateResults(dates, expectedResults, null);

    }

    @Test
    public void testLastModificationDatesAfterTopicRightChange() throws Exception {

        User user = TestUtils.createRandomUser(false);

        final Map<Long, Date> expectedResults = createsSomeTopics(user);

        AuthenticationHelper.setInternalSystemToSecurityContext();

        List<LastModificationDate> dates = lastModificationDateManagement
                .getTopicCrawlLastModificationDates(lastModificationDateFactory);

        validateResults(dates, expectedResults, null);

        // edit a topic
        Long topicId = expectedResults.keySet().iterator().next();

        Date before = expectedResults.get(topicId);
        Assert.assertNotNull(before);
        Blog topic = blogManagement.getBlogById(topicId, false);

        AuthenticationHelper.setAsAuthenticatedUser(user);
        topic = blogRightsManagement.setAllCanReadAllCanWrite(topicId, !topic.isAllCanRead(),
                !topic.isAllCanWrite());
        topic = blogManagement.getBlogById(topicId, false);

        Assert.assertTrue(topic.getCrawlLastModificationDate().getTime() > before.getTime(),
                " current=" + topic.getCrawlLastModificationDate() + " before=" + before);
        expectedResults.put(topicId, topic.getCrawlLastModificationDate());

        AuthenticationHelper.setInternalSystemToSecurityContext();
        dates = lastModificationDateManagement
                .getTopicCrawlLastModificationDates(lastModificationDateFactory);
        validateResults(dates, expectedResults, null);

    }

    @Test
    public void testLastModificationDatesAfterTopicRoleChange() throws Exception {

        User user = TestUtils.createRandomUser(false);
        User userToAssign = TestUtils.createRandomUser(false);

        final Map<Long, Date> expectedResults = createsSomeTopics(user);

        AuthenticationHelper.setInternalSystemToSecurityContext();

        List<LastModificationDate> dates = lastModificationDateManagement
                .getTopicCrawlLastModificationDates(lastModificationDateFactory);

        validateResults(dates, expectedResults, null);

        // edit a topic
        Long topicId = expectedResults.keySet().iterator().next();

        Date before = expectedResults.get(topicId);
        Assert.assertNotNull(before);

        blogRightsManagement.addEntity(topicId, userToAssign.getId(), BlogRole.MEMBER);

        Blog topic = blogManagement.getBlogById(topicId, false);

        Assert.assertTrue(topic.getCrawlLastModificationDate().getTime() > before.getTime(),
                " current=" + topic.getCrawlLastModificationDate() + " before=" + before);
        expectedResults.put(topicId, topic.getCrawlLastModificationDate());

        dates = lastModificationDateManagement
                .getTopicCrawlLastModificationDates(lastModificationDateFactory);
        validateResults(dates,
                expectedResults, null);

    }

    @Test
    public void testLastModificationDatesOfNotes() throws Exception {

        User user = TestUtils.createRandomUser(false);
        Blog topic = TestUtils.createRandomBlog(false, false, user);

        final Map<Long, Date> expectedResults = createsSomeNotes(user, topic);
        final Collection<Long> notesNotToBeReturned = createSomeAutosaveNotes(user, topic);

        AuthenticationHelper.setInternalSystemToSecurityContext();

        List<LastModificationDate> dates = lastModificationDateManagement
                .getNoteCrawlLastModificationDates(lastModificationDateFactory);

        validateResults(dates, expectedResults, notesNotToBeReturned);

    }

    @Test
    public void testLastModificationDatesOfTopics() throws Exception {

        User user = TestUtils.createRandomUser(false);

        final Map<Long, Date> expectedResults = createsSomeTopics(user);

        AuthenticationHelper.setInternalSystemToSecurityContext();

        List<LastModificationDate> dates = lastModificationDateManagement
                .getTopicCrawlLastModificationDates(lastModificationDateFactory);

        validateResults(dates, expectedResults, null);

    }

    private void validateResults(List<LastModificationDate> lastModifcationDates,
            final Map<Long, Date> expectedResults, final Collection<Long> idsNotToBeReturned) {

        Assert.assertNotNull(lastModifcationDates);
        // there should be at least as many returned dates as expected, there might be more entries
        // if a topic, note was created through another test
        Assert.assertTrue(lastModifcationDates.size() >= expectedResults.size());

        // generate a map for easier access
        final Map<Long, Date> actualResults = new HashMap<>();
        for (LastModificationDate date : lastModifcationDates) {
            actualResults.put(date.getEntityId(), date.getLastModificationDate());
        }

        // check for every expected date if it is in the returned result with the same date
        for (Entry<Long, Date> expected : expectedResults.entrySet()) {

            Date actual = actualResults.get(expected.getKey());
            Assert.assertNotNull(actual);
            Assert.assertEquals(actual, expected.getValue());
        }

        if (idsNotToBeReturned != null) {
            // check that some ids are not in the returned list
            for (Long entityId : idsNotToBeReturned) {
                Assert.assertFalse(actualResults.containsKey(entityId));
            }
        }
    }

}
