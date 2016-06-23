package com.communote.server.core.vo.query.note;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.communote.common.util.PageableList;
import com.communote.server.api.core.note.NoteRenderContext;
import com.communote.server.api.core.note.NoteRenderMode;
import com.communote.server.core.filter.ResultSpecification;
import com.communote.server.core.query.QueryManagement;
import com.communote.server.core.vo.blog.DiscussionNoteData;
import com.communote.server.core.vo.query.TaggingCoreItemUTPExtension;
import com.communote.server.core.vo.query.TimelineFilterViewType;
import com.communote.server.core.vo.query.post.NoteQuery;
import com.communote.server.core.vo.query.post.NoteQueryParameters;
import com.communote.server.model.blog.Blog;
import com.communote.server.model.user.User;
import com.communote.server.persistence.blog.NoteDao;
import com.communote.server.test.CommunoteIntegrationTest;
import com.communote.server.test.util.AuthenticationTestUtils;
import com.communote.server.test.util.TestUtils;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class SimpleNoteListItemToDiscussionNoteDataConverterTest extends CommunoteIntegrationTest {

    @Autowired
    private QueryManagement queryManagement;

    @Autowired
    private NoteDao noteDao;

    /**
     * Regression test for KENMEI-5721: some discussions are missing in discussion view after
     * loading more if direct messages are part of the discussions
     *
     * <p>
     * Scenario to reproduce error: discussion view, with max count of 15, shows discussions in
     * following order (abbreviations: DM=direct message, LDD=LastDiscussionCreationDate (as stored
     * in DB and thus without DM), DMD=CreationDate of DM, Y=yesterday, T=today)
     * <ol>
     * <li>Discussion without DM and LDD T 18:34:30</li>
     * <li>Discussion without DM and LDD T 18:34:10</li>
     * <li>Discussion with DM als letzten Kommentar. LDD Y 22:44, DMD T 17:27:10</li>
     * <li>Discussion with DM als letzten Kommentar. LDD T 16:32, DMD T 17:27:30</li>
     * <li>Discussion with DM als letzten Kommentar. LDD T 16:30, DMD T 16:31</li>
     * <li>Discussion without DM and LDD T 16:24</li>
     * <li>Discussion without DM and LDD Y 22:47</li>
     * <li>Discussion without DM and LDD Y 20:15</li>
     * </ol>
     * Problem occurs after changing maxCount to 3, refreshing and loading more results: discussion
     * number 5 is not contained
     * </p>
     * This test uses notes with following details
     * <ul>
     * <li>H LDD T 18:34:30</li>
     * <li>G LDD T 18:34:10</li>
     * <li>F.1 DMD T 17:27:30</li>
     * <li>B.1 DMD T 17:27:10</li>
     * <li>F LDD T 16:32</li>
     * <li>E.1 DMD T 16:31</li>
     * <li>E LDD T 16:30</li>
     * <li>D LDD T 16:24</li>
     * <li>C LDD Y 22:47</li>
     * <li>B LDD Y 22:44</li>
     * <li>A LDD Y 20:15</li>
     * </ul>
     */
    @Test
    public void testForKENMEI5721() {
        User user = TestUtils.createRandomUser(false);
        Blog topic = TestUtils.createRandomBlog(false, false, user);
        Calendar calendar = Calendar.getInstance();
        // avoid MySQL rounding effects by setting the time to full second
        long millis = (calendar.getTimeInMillis() / 1000L) * 1000L;
        calendar.setTimeInMillis(millis);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int date = calendar.get(Calendar.DATE) - 1;
        // Build the data structure.
        calendar.set(year, month, date - 1, 20, 15);
        TestUtils.createAndStoreCommonNote(topic, user.getId(), "A", calendar.getTime());
        calendar.set(year, month, date - 1, 22, 44);
        Long noteB = TestUtils.createAndStoreCommonNote(topic, user.getId(), "B",
                calendar.getTime());
        calendar.set(year, month, date - 1, 22, 47);
        TestUtils.createAndStoreCommonNote(topic, user.getId(), "C", calendar.getTime());
        calendar.set(year, month, date, 16, 24);
        Long noteD = TestUtils.createAndStoreCommonNote(topic, user.getId(), "D",
                calendar.getTime());
        calendar.set(year, month, date, 16, 30);
        Long noteE = TestUtils.createAndStoreCommonNote(topic, user.getId(), "E",
                calendar.getTime());
        calendar.set(year, month, date, 16, 31);
        Long noteE1 = TestUtils.createAndStoreCommonNote(topic, user.getId(),
                "d @" + user.getAlias() + " E.1", noteE, calendar.getTime());
        calendar.set(year, month, date, 16, 32);
        Long noteF = TestUtils.createAndStoreCommonNote(topic, user.getId(), "F",
                calendar.getTime());
        calendar.set(year, month, date, 17, 27, 10);
        Long noteB1 = TestUtils.createAndStoreCommonNote(topic, user.getId(),
                "d @" + user.getAlias() + " B.1", noteB, calendar.getTime());
        calendar.set(year, month, date, 17, 27, 30);
        Date beforeDate = calendar.getTime();
        Long noteF1 = TestUtils.createAndStoreCommonNote(topic, user.getId(),
                "d @" + user.getAlias() + " F.1", noteF, beforeDate);
        calendar.set(year, month, date, 18, 24, 10);
        TestUtils.createAndStoreCommonNote(topic, user.getId(), "G", calendar.getTime());
        calendar.set(year, month, date, 18, 24, 30);
        TestUtils.createAndStoreCommonNote(topic, user.getId(), "H", calendar.getTime());

        // Some assertions just to be sure
        Assert.assertTrue(noteDao.load(noteE1).isDirect());
        Assert.assertNotNull(noteDao.load(noteE1).getParent());
        Assert.assertTrue(noteDao.load(noteB1).isDirect());
        Assert.assertNotNull(noteDao.load(noteB1).getParent());
        Assert.assertTrue(noteDao.load(noteF1).isDirect());
        Assert.assertNotNull(noteDao.load(noteF1).getParent());

        // Query for the second set of notes.
        AuthenticationTestUtils.setSecurityContext(user);
        NoteQueryParameters parameters = new NoteQueryParameters();
        parameters.setTimelineFilterViewType(TimelineFilterViewType.COMMENT);
        parameters.setRetrieveOnlyNotesBeforeId(noteF);
        parameters.setRetrieveOnlyNotesBeforeDate(beforeDate);
        parameters.setResultSpecification(new ResultSpecification(0, 3));
        parameters.setTypeSpecificExtension(new TaggingCoreItemUTPExtension());
        parameters.getTypeSpecificExtension().setBlogId(topic.getId());

        SimpleNoteListItemToDiscussionNoteDataConverter converter = new SimpleNoteListItemToDiscussionNoteDataConverter(
                new NoteRenderContext(NoteRenderMode.PORTAL, Locale.ENGLISH),
                TimelineFilterViewType.COMMENT, parameters);
        PageableList<DiscussionNoteData> result = queryManagement.query(new NoteQuery(),
                parameters, converter);

        Assert.assertEquals(result.size(), 3);

        // In the error, the first result is not B
        Assert.assertEquals(result.get(0).getId(), noteB);
        Assert.assertEquals(result.get(1).getId(), noteE);
        Assert.assertEquals(result.get(2).getId(), noteD);
    }

    /**
     * Regression test for KENMEI-6147: discussion view does not always include all results
     * <p>
     * This tests the part where two notes were written at the same time.
     * </p>
     * <p>
     * The expected message stream is as follows:
     * </p>
     * <ul>
     * <li>Note E</li>
     * <li>Note D</li>
     * <li>Note A (@@all) with Child A1 (no @) at Time X</li>
     * <li>Note B with Child B1 (@user) at Time X</li>
     * <li>Note C with Time X - 1</li>
     * </ul>
     * But the faulty result is
     * <ul>
     * <li>Note E</li>
     * <li>Note D</li>
     * <li>Note A with Child A1 at Time X</li>
     * <li>Note C with Time X - 1</li>
     * </ul>
     */
    @Test
    public void testForKENMEI6147() {
        // round to seconds (MySQL) and substract some seconds
        Long now = ((System.currentTimeMillis() / 1000L) * 1000L) - 500000;
        User user = TestUtils.createRandomUser(false);
        Blog topic = TestUtils.createRandomBlog(false, false, user);

        Long noteAId = TestUtils.createAndStoreCommonNote(topic, user.getId(), "Note A @@all",
                new Date(now - 100000));
        Long noteBId = TestUtils.createAndStoreCommonNote(topic, user.getId(), "Note B @@authors",
                new Date(now - 50000));
        Long noteCId = TestUtils.createAndStoreCommonNote(topic, user.getId(),
                "Note C @" + user.getAlias(), new Date(now - 200000)); // Written before A

        TestUtils.createAndStoreCommonNote(topic, user.getId(), "Child B @" + user.getAlias(),
                noteBId, new Date(now));

        TestUtils.createAndStoreCommonNote(topic, user.getId(), "Child A ", noteAId, new Date(now));

        Long noteDId = TestUtils.createAndStoreCommonNote(topic, user.getId(),
                "Note D @" + user.getAlias(), new Date(now + 10000));
        Long noteEId = TestUtils.createAndStoreCommonNote(topic, user.getId(),
                "Note E @" + user.getAlias(), new Date(now + 50000));

        AuthenticationTestUtils.setSecurityContext(user);

        // Check notes before endless scroll (first 3 elements)
        NoteQueryParameters parameters = new NoteQueryParameters();
        parameters.setTimelineFilterViewType(TimelineFilterViewType.COMMENT);
        parameters.setResultSpecification(new ResultSpecification(0, 3, 1));
        parameters.setTypeSpecificExtension(new TaggingCoreItemUTPExtension());
        parameters.setUserToBeNotified(new Long[] { user.getId() });
        parameters.setMentionDiscussionAuthors(true);
        parameters.setMentionTopicAuthors(true);
        parameters.setMentionTopicReaders(true);
        parameters.setMentionTopicManagers(true);

        SimpleNoteListItemToDiscussionNoteDataConverter converter = new SimpleNoteListItemToDiscussionNoteDataConverter(
                new NoteRenderContext(NoteRenderMode.PORTAL, Locale.ENGLISH),
                TimelineFilterViewType.COMMENT, parameters);
        PageableList<DiscussionNoteData> result = queryManagement.query(new NoteQuery(),
                parameters, converter);

        Assert.assertEquals(result.size(), 3);
        Assert.assertEquals(result.get(0).getId(), noteEId);
        Assert.assertEquals(result.get(1).getId(), noteDId);
        Long lastId = result.get(2).getId();
        Assert.assertTrue(lastId.equals(noteAId) || lastId.equals(noteBId));

        // Check the endless scrolled notes.
        parameters = new NoteQueryParameters();
        parameters.setTimelineFilterViewType(TimelineFilterViewType.COMMENT);
        parameters.setRetrieveOnlyNotesBeforeId(lastId);
        parameters.setRetrieveOnlyNotesBeforeDate(new Date(now));
        parameters.setResultSpecification(new ResultSpecification(0, 3, 1));
        parameters.setTypeSpecificExtension(new TaggingCoreItemUTPExtension());
        parameters.setUserToBeNotified(new Long[] { user.getId() });
        parameters.setMentionDiscussionAuthors(true);
        parameters.setMentionTopicAuthors(true);
        parameters.setMentionTopicReaders(true);
        parameters.setMentionTopicManagers(true);

        converter = new SimpleNoteListItemToDiscussionNoteDataConverter(new NoteRenderContext(
                NoteRenderMode.PORTAL, Locale.ENGLISH), TimelineFilterViewType.COMMENT, parameters);
        result = queryManagement.query(new NoteQuery(), parameters, converter);

        Long currentId = result.get(0).getId();
        Assert.assertTrue((lastId.equals(noteAId) || lastId.equals(noteBId))
                && !currentId.equals(lastId));
        Assert.assertEquals(result.size(), 2);
        Assert.assertEquals(result.get(1).getId(), noteCId);
    }
}
