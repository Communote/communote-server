package com.communote.server.core.note;

import java.util.Date;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.communote.common.util.PageableList;
import com.communote.server.api.core.note.NoteRenderContext;
import com.communote.server.api.core.note.NoteRenderMode;
import com.communote.server.core.query.QueryManagement;
import com.communote.server.core.vo.blog.DiscussionNoteData;
import com.communote.server.core.vo.query.TimelineFilterViewType;
import com.communote.server.core.vo.query.note.SimpleNoteListItemToDiscussionNoteDataConverter;
import com.communote.server.core.vo.query.post.NoteQuery;
import com.communote.server.core.vo.query.post.NoteQueryParameters;
import com.communote.server.model.blog.Blog;
import com.communote.server.model.user.User;
import com.communote.server.persistence.blog.NoteDao;
import com.communote.server.test.CommunoteIntegrationTest;
import com.communote.server.test.util.AuthenticationTestUtils;
import com.communote.server.test.util.TestUtils;

/**
 * Tests for retrieval of notes when discussion view sorting is used and discussion contains direct
 * messages.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class DiscussionViewDirectMessageTest extends CommunoteIntegrationTest {

    private User user1;
    private User user2;

    @Autowired
    private QueryManagement queryManagement;

    @Autowired
    private NoteDao noteDao;

    /**
     * Return notes for the given topic.
     *
     * @param topic
     *            The topic to return the notes for.
     * @param numberOfElements
     *            The number of elements to return.
     * @param beforeId
     *            The upper id bound.
     * @param beforeDate
     *            The upper before date bound.
     * @return A list of notes for this topic.
     */
    private PageableList<DiscussionNoteData> getNotes(Blog topic, int numberOfElements,
            Long beforeId, Date beforeDate) {
        NoteQuery query = new NoteQuery();
        NoteQueryParameters parameters = new NoteQueryParameters();
        parameters.getTypeSpecificExtension().setBlogId(topic.getId());
        parameters.setTimelineFilterViewType(TimelineFilterViewType.COMMENT);
        parameters.getResultSpecification().setNumberOfElements(numberOfElements);
        parameters.setRetrieveOnlyNotesBeforeId(beforeId);
        parameters.setRetrieveOnlyNotesBeforeDate(beforeDate);

        SimpleNoteListItemToDiscussionNoteDataConverter converter = new SimpleNoteListItemToDiscussionNoteDataConverter(
                new NoteRenderContext(NoteRenderMode.HTML, Locale.ENGLISH),
                TimelineFilterViewType.COMMENT, parameters);
        PageableList<DiscussionNoteData> notes = queryManagement
                .query(query, parameters, converter);
        return notes;
    }

    /**
     * This creates a blog and some users.
     */
    @BeforeClass(dependsOnGroups = GROUP_INTEGRATION_TEST_SETUP)
    public void setup() {
        user1 = TestUtils.createRandomUser(false);
        user2 = TestUtils.createRandomUser(false);
    }

    /**
     * Notes are logically structured like this:
     * <ul>
     * <li>A.1
     * <ul>
     * <li>A.2
     * <li>A.3
     * <li>A.4.DM
     * </ul>
     * <li>B.1
     * <ul>
     * <li>B.3
     * <li>B.4
     * </ul>
     * <li>C.1
     * <li>D.1
     * <li>E.1
     * <ul>
     * <li>E.2
     * </ul>
     * <li>F.1
     * <ul>
     * <li>F.2.DM
     * </ul>
     * <li>G.1
     * </ul>
     *
     * Notes are sorted by creation date desc like this in the database:
     * <ul>
     * <li>A.4.DM
     * <li>E.2
     * <li>E.1
     * <li>A.3
     * <li>D.1
     * <li>F.2.DM
     * <li>C.1
     * <li>B.4
     * <li>B.3
     * <li>A.2
     * <li>B.1
     * <li>G.1
     * <li>F.1
     * <li>A.1
     * </ul>
     *
     * The expected result (maxResult=5) of root notes is:
     *
     * <ul>
     * <li>D
     * <li>F
     * <li>C
     * <li>B
     * <li>G
     * </ul>
     */
    @Test
    public void testForExample1() {
        Blog topic = TestUtils.createRandomBlog(false, false, user1, user2);
        Long noteA1 = TestUtils.createAndStoreCommonNote(topic, user1.getId(), "A.1");
        Long noteF1 = TestUtils.createAndStoreCommonNote(topic, user1.getId(), "F.1");
        Long noteG1 = TestUtils.createAndStoreCommonNote(topic, user1.getId(), "G.1");
        Long noteB1 = TestUtils.createAndStoreCommonNote(topic, user1.getId(), "B.1");
        TestUtils.createAndStoreCommonNote(topic, user1.getId(), "A.2", noteA1);
        sleep(1000);
        TestUtils.createAndStoreCommonNote(topic, user1.getId(), "B.2", noteB1);
        sleep(1000);
        TestUtils.createAndStoreCommonNote(topic, user1.getId(), "B.3", noteB1);
        sleep(1000);
        Long noteC1 = TestUtils.createAndStoreCommonNote(topic, user1.getId(), "C.1");
        sleep(1000);
        TestUtils.createAndStoreCommonNote(topic, user1.getId(), "d @" + user2.getAlias()
                + " F.2.DM", noteF1);
        sleep(1000);
        Long noteD1 = TestUtils.createAndStoreCommonNote(topic, user1.getId(), "D.1");
        sleep(1000);
        TestUtils.createAndStoreCommonNote(topic, user1.getId(), "A.3", noteA1);
        sleep(1000);
        Long noteE1 = TestUtils.createAndStoreCommonNote(topic, user1.getId(), "E.1");
        sleep(1000);
        TestUtils.createAndStoreCommonNote(topic, user1.getId(), "E.2", noteE1);
        sleep(1000);
        TestUtils.createAndStoreCommonNote(topic, user1.getId(), "d @" + user2.getAlias()
                + " A.4.DM", noteA1);

        AuthenticationTestUtils.setSecurityContext(user2);
        PageableList<DiscussionNoteData> notes = getNotes(topic, 5, noteE1, noteDao.load(noteE1)
                .getCreationDate());

        Assert.assertEquals(notes.size(), 5);
        Assert.assertEquals(notes.get(0).getId(), noteD1);
        Assert.assertEquals(notes.get(1).getId(), noteF1);
        Assert.assertEquals(notes.get(2).getId(), noteC1);
        Assert.assertEquals(notes.get(3).getId(), noteB1);
        Assert.assertEquals(notes.get(4).getId(), noteG1);
    }

    /**
     * Notes are logically structured like this:
     * <ul>
     * <li>F.1
     * <li>E.1
     * <li>G.1
     * <ul>
     * <li>G.2.DM
     * </ul>
     * <li>D.1
     * <ul>
     * <li>D.2.DM
     * </ul>
     * <li>C.1
     * <ul>
     * <li>C.2.DM
     * </ul>
     * <li>B.1
     * <ul>
     * <li>B.2.DM
     * </ul>
     * <li>A.1
     * <ul>
     * <li>A.2.DM
     * </ul>
     * <li>X.1
     * <li>Y.1
     * <li>Z.1
     * </ul>
     *
     * Notes are sorted by creation date desc like this in the database:
     * <ul>
     * <li>A.2.DM
     * <li>B.2.DM
     * <li>C.2.DM
     * <li>D.2.DM
     * <li>G.2.DM
     * <li>F.1
     * <li>E.1
     * <li>G.1
     * <li>D.1
     * <li>C.1
     * <li>B.1
     * <li>A.1
     * <li>X.1
     * <li>Y.1
     * <li>Z.1
     * </ul>
     *
     * The expected result (notesBefore=E, maxResult=5) of root notes is:
     *
     * <ul>
     * <li>X
     * <li>Y
     * <li>Z
     * </ul>
     */
    @Test
    public void testForExample2() {
        Blog topic = TestUtils.createRandomBlog(false, false, user1, user2);
        Long noteZ1 = TestUtils.createAndStoreCommonNote(topic, user1.getId(), "Z.1");
        sleep(1100);
        Long noteY1 = TestUtils.createAndStoreCommonNote(topic, user1.getId(), "Y.1");
        sleep(1100);
        Long noteX1 = TestUtils.createAndStoreCommonNote(topic, user1.getId(), "X.1");
        sleep(1100);
        Long noteA1 = TestUtils.createAndStoreCommonNote(topic, user1.getId(), "A.1");
        sleep(1100);
        Long noteB1 = TestUtils.createAndStoreCommonNote(topic, user1.getId(), "B.1");
        sleep(1100);
        Long noteC1 = TestUtils.createAndStoreCommonNote(topic, user1.getId(), "C.1");
        sleep(1100);
        Long noteD1 = TestUtils.createAndStoreCommonNote(topic, user1.getId(), "D.1");
        sleep(1100);
        Long noteG1 = TestUtils.createAndStoreCommonNote(topic, user1.getId(), "G.1");
        sleep(1100);
        Long noteE1 = TestUtils.createAndStoreCommonNote(topic, user1.getId(), "E.1");
        sleep(1100);
        TestUtils.createAndStoreCommonNote(topic, user1.getId(), "F.1");
        sleep(1100);
        TestUtils.createAndStoreCommonNote(topic, user1.getId(), "d @" + user2.getAlias()
                + " G.2.DM", noteG1);
        sleep(1100);
        TestUtils.createAndStoreCommonNote(topic, user1.getId(), "d @" + user2.getAlias()
                + " D.2.DM", noteD1);
        sleep(1100);
        TestUtils.createAndStoreCommonNote(topic, user1.getId(), "d @" + user2.getAlias()
                + " C.2.DM", noteC1);
        sleep(1100);
        TestUtils.createAndStoreCommonNote(topic, user1.getId(), "d @" + user2.getAlias()
                + " B.2.DM", noteB1);
        sleep(1100);
        TestUtils.createAndStoreCommonNote(topic, user1.getId(), "d @" + user2.getAlias()
                + " A.2.DM", noteA1);

        AuthenticationTestUtils.setSecurityContext(user2);
        PageableList<DiscussionNoteData> notes = getNotes(topic, 5, noteE1, noteDao.load(noteE1)
                .getCreationDate());

        Assert.assertEquals(notes.size(), 3);
        Assert.assertEquals(notes.get(0).getId(), noteX1);
        Assert.assertEquals(notes.get(1).getId(), noteY1);
        Assert.assertEquals(notes.get(2).getId(), noteZ1);
    }

    /**
     * Notes are logically structured like this:
     * <ul>
     * <li>G.1
     * <li>F.1
     * <li>E.1
     * <li>D.1
     * <li>C.1
     * <li>B.1
     * <ul>
     * <li>B.2
     * </ul>
     * <li>A.1
     * <ul>
     * <li>A.2.DM
     * </ul>
     * </ul>
     *
     * Notes are sorted by creation date desc like this in the database:
     * <ul>
     * <li>A.2.DM
     * <li>B.2
     * <li>G.1
     * <li>F.1
     * <li>E.1
     * <li>D.1
     * <li>C.1
     * <li>B.1
     * <li>A.1
     * </ul>
     *
     * The expected result (maxResult=10) of root notes is:
     *
     * <ul>
     * <li>A
     * <li>B
     * <li>G
     * <li>F
     * <li>E
     * <li>D
     * <li>C
     * </ul>
     */
    @Test
    public void testForExample3() {
        Blog topic = TestUtils.createRandomBlog(false, false, user1, user2);
        Long noteA1 = TestUtils.createAndStoreCommonNote(topic, user1.getId(), "A.1");
        sleep(1100);
        Long noteB1 = TestUtils.createAndStoreCommonNote(topic, user1.getId(), "B.1");
        sleep(1100);
        Long noteC1 = TestUtils.createAndStoreCommonNote(topic, user1.getId(), "C.1");
        sleep(1100);
        Long noteD1 = TestUtils.createAndStoreCommonNote(topic, user1.getId(), "D.1");
        sleep(1100);
        Long noteE1 = TestUtils.createAndStoreCommonNote(topic, user1.getId(), "E.1");
        sleep(1100);
        Long noteF1 = TestUtils.createAndStoreCommonNote(topic, user1.getId(), "F.1");
        sleep(1100);
        Long noteG1 = TestUtils.createAndStoreCommonNote(topic, user1.getId(), "G.1");
        sleep(1100);
        TestUtils.createAndStoreCommonNote(topic, user1.getId(), "B.2", noteB1);
        sleep(1100);
        TestUtils.createAndStoreCommonNote(topic, user1.getId(), "d @" + user2.getAlias() + " A.1",
                noteA1);

        AuthenticationTestUtils.setSecurityContext(user2);
        PageableList<DiscussionNoteData> notes = getNotes(topic, 10, null, null);

        Assert.assertEquals(notes.size(), 7);
        Assert.assertEquals(notes.get(0).getId(), noteA1);
        Assert.assertEquals(notes.get(1).getId(), noteB1);
        Assert.assertEquals(notes.get(2).getId(), noteG1);
        Assert.assertEquals(notes.get(3).getId(), noteF1);
        Assert.assertEquals(notes.get(4).getId(), noteE1);
        Assert.assertEquals(notes.get(5).getId(), noteD1);
        Assert.assertEquals(notes.get(6).getId(), noteC1);
    }

    /**
     * Logical Structure
     * <ul>
     * <li>A.1
     * <ul>
     * <li>A.2 DM</li>
     * </ul>
     * </li>
     * <li>B.1
     * <ul>
     * <li>B.2</li>
     * </ul>
     * </li>
     * <li>C.1
     * <ul>
     * <li>C.2 DM</li>
     * </ul>
     * </li>
     * <li>D.1
     * <ul>
     * <li>D.2</li>
     * </ul>
     * </li>
     * <li>E.1
     * <ul>
     * <li>E.2 DM</li>
     * </ul>
     * </li>
     * <li>F.1
     * <ul>
     * <li>F.2</li>
     * </ul>
     * </li>
     * <li>G.1
     * <ul>
     * <li>G.2 DN</li>
     * </ul>
     * </li>
     * </ul>
     * Database Sorting
     * <ul>
     * <li>C2 DM</li>
     * <li>A2 DM</li>
     * <li>B2</li>
     * <li>G2</li>
     * <li>F2</li>
     * <li>E2</li>
     * <li>D2</li>
     * <li>G1</li>
     * <li>F1</li>
     * <li>E1</li>
     * <li>D1</li>
     * <li>C1</li>
     * <li>B1</li>
     * <li>A1</li>
     * </ul>
     * Expected Result
     * <ul>
     * <li>C</li>
     * <li>A</li>
     * <li>B</li>
     * <li>G</li>
     * <li>F</li>
     * <li>E</li>
     * <li>D</li>
     * </ul>
     */
    @Test
    public void testForExample4() {
        Blog topic = TestUtils.createRandomBlog(false, false, user1, user2);
        Long noteA1 = TestUtils.createAndStoreCommonNote(topic, user2.getId(), "A.1");
        sleep(1100);
        Long noteB1 = TestUtils.createAndStoreCommonNote(topic, user1.getId(), "B.1");
        sleep(1100);
        Long noteC1 = TestUtils.createAndStoreCommonNote(topic, user1.getId(), "C.1");
        sleep(1100);
        Long noteD1 = TestUtils.createAndStoreCommonNote(topic, user1.getId(), "D.1");
        sleep(1100);
        Long noteE1 = TestUtils.createAndStoreCommonNote(topic, user1.getId(), "E.1");
        sleep(1100);
        Long noteF1 = TestUtils.createAndStoreCommonNote(topic, user1.getId(), "F.1");
        sleep(1100);
        Long noteG1 = TestUtils.createAndStoreCommonNote(topic, user1.getId(), "G.1");
        sleep(1100);

        TestUtils.createAndStoreCommonNote(topic, user1.getId(), "D.2", noteD1);
        sleep(1100);
        TestUtils.createAndStoreCommonNote(topic, user1.getId(), "E.2", noteE1);
        sleep(1100);
        TestUtils.createAndStoreCommonNote(topic, user1.getId(), "F.2", noteF1);
        sleep(1100);
        TestUtils.createAndStoreCommonNote(topic, user1.getId(), "G.2", noteG1);
        sleep(1100);
        TestUtils.createAndStoreCommonNote(topic, user1.getId(), "B.2", noteB1);
        sleep(1100);
        TestUtils.createAndStoreCommonNote(topic, user1.getId(), "d @" + user2.getAlias() + " A.2",
                noteA1);
        sleep(1100);
        TestUtils.createAndStoreCommonNote(topic, user2.getId(), "d @" + user1.getAlias() + " C.2",
                noteC1);

        AuthenticationTestUtils.setSecurityContext(user1);
        PageableList<DiscussionNoteData> notes = getNotes(topic, 10, null, null);
        Assert.assertEquals(notes.size(), 7);
        Assert.assertEquals(notes.get(0).getId(), noteC1);
        Assert.assertEquals(notes.get(1).getId(), noteA1);
        Assert.assertEquals(notes.get(2).getId(), noteB1);
        Assert.assertEquals(notes.get(3).getId(), noteG1);
        Assert.assertEquals(notes.get(4).getId(), noteF1);
        Assert.assertEquals(notes.get(5).getId(), noteE1);
        Assert.assertEquals(notes.get(6).getId(), noteD1);
    }
}
