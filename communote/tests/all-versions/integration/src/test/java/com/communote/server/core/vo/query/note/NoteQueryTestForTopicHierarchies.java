package com.communote.server.core.vo.query.note;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.communote.common.util.PageableList;
import com.communote.server.core.filter.listitems.SimpleNoteListItem;
import com.communote.server.core.query.QueryManagement;
import com.communote.server.core.vo.query.TaggingCoreItemUTPExtension;
import com.communote.server.core.vo.query.post.NoteQuery;
import com.communote.server.core.vo.query.post.NoteQueryParameters;
import com.communote.server.model.blog.Blog;
import com.communote.server.model.user.User;
import com.communote.server.service.TopicHierarchyService;
import com.communote.server.test.CommunoteIntegrationTest;
import com.communote.server.test.util.AuthenticationTestUtils;
import com.communote.server.test.util.TestUtils;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class NoteQueryTestForTopicHierarchies extends CommunoteIntegrationTest {

    private static final NoteQuery NOTE_QUERY = new NoteQuery();

    @Autowired
    private TopicHierarchyService topicHierarchyService;

    @Autowired
    private QueryManagement queryManagement;

    /**
     * @param includeChildTopics
     *            Set to include child topics.
     * @param blogIds
     *            The blog ids, the query should filter for.
     * @return {@link NoteQueryParameters}
     */
    private NoteQueryParameters createNoteQueryInstance(boolean includeChildTopics, Long... blogIds) {
        NoteQueryParameters noteQueryInstance = new NoteQueryParameters();
        TaggingCoreItemUTPExtension filter = new TaggingCoreItemUTPExtension();
        filter.setBlogFilter(blogIds);
        filter.setIncludeChildTopics(includeChildTopics);
        noteQueryInstance.setTypeSpecificExtension(filter);
        return noteQueryInstance;
    }

    /**
     * Method to convert to a list of Long.
     * 
     * @param parameters
     *            The parameters for the query.
     * 
     * @return The notes as ids.
     */
    private List<Long> getAndConvertNotes(NoteQueryParameters parameters) {
        PageableList<SimpleNoteListItem> notes = queryManagement.query(NOTE_QUERY, parameters);
        List<Long> result = new ArrayList<Long>();
        for (SimpleNoteListItem note : notes) {
            result.add(note.getId());
        }
        return result;
    }

    /**
     * This tests, that the notes of direct and transitive topics will also be shown.
     * 
     * @throws Exception
     *             in case the test failed
     */
    @Test
    public void testIncludeChildTopics() throws Exception {
        User user1 = TestUtils.createRandomUser(false);
        User user2 = TestUtils.createRandomUser(false);
        Blog topic_1 = TestUtils.createRandomBlog(false, false, user1, user2);
        Blog topic_1_1 = TestUtils.createRandomBlog(false, false, user1, user2);
        Blog topic_1_1_1 = TestUtils.createRandomBlog(false, false, user1, user2);
        Blog topic_1_2 = TestUtils.createRandomBlog(false, false, user1);

        topicHierarchyService.addTopic(topic_1.getId(), topic_1_1.getId());
        topicHierarchyService.addTopic(topic_1.getId(), topic_1_2.getId());
        topicHierarchyService.addTopic(topic_1_1.getId(), topic_1_1_1.getId());

        Long note1 = TestUtils.createAndStoreCommonNote(topic_1, user1.getId(), "Test");
        Long note1_1 = TestUtils.createAndStoreCommonNote(topic_1_1, user1.getId(), "Test");
        Long note1_1_1 = TestUtils.createAndStoreCommonNote(topic_1_1_1, user1.getId(), "Test");
        Long note1_2 = TestUtils.createAndStoreCommonNote(topic_1_2, user1.getId(), "Test");

        // User 1 with access to all topics.
        AuthenticationTestUtils.setSecurityContext(user1);

        NoteQueryParameters parameters = createNoteQueryInstance(true, topic_1.getId());
        List<Long> notes = getAndConvertNotes(parameters);
        Assert.assertEquals(notes.size(), 4);
        Assert.assertTrue(notes.contains(note1));
        Assert.assertTrue(notes.contains(note1_1));
        Assert.assertTrue(notes.contains(note1_1_1));
        Assert.assertTrue(notes.contains(note1_2));

        parameters = createNoteQueryInstance(true, topic_1_1.getId(), topic_1_2.getId());
        notes = getAndConvertNotes(parameters);
        Assert.assertEquals(notes.size(), 3);
        Assert.assertTrue(notes.contains(note1_1));
        Assert.assertTrue(notes.contains(note1_1_1));
        Assert.assertTrue(notes.contains(note1_2));

        parameters = createNoteQueryInstance(false, topic_1.getId());
        notes = getAndConvertNotes(parameters);
        Assert.assertEquals(notes.size(), 1);
        Assert.assertTrue(notes.contains(note1));

        parameters = createNoteQueryInstance(false, topic_1_1.getId(), topic_1_2.getId());
        notes = getAndConvertNotes(parameters);
        Assert.assertEquals(notes.size(), 2);
        Assert.assertTrue(notes.contains(note1_1));
        Assert.assertTrue(notes.contains(note1_2));

        // User 2 with less access
        AuthenticationTestUtils.setSecurityContext(user2);

        parameters = createNoteQueryInstance(true, topic_1.getId());
        notes = getAndConvertNotes(parameters);
        Assert.assertEquals(notes.size(), 3);
        Assert.assertTrue(notes.contains(note1));
        Assert.assertTrue(notes.contains(note1_1));
        Assert.assertTrue(notes.contains(note1_1_1));

        parameters = createNoteQueryInstance(true, topic_1_1.getId(), topic_1_2.getId());
        notes = getAndConvertNotes(parameters);
        Assert.assertEquals(notes.size(), 2);
        Assert.assertTrue(notes.contains(note1_1));
        Assert.assertTrue(notes.contains(note1_1_1));

        parameters = createNoteQueryInstance(false, topic_1.getId());
        notes = getAndConvertNotes(parameters);
        Assert.assertEquals(notes.size(), 1);
        Assert.assertTrue(notes.contains(note1));

        parameters = createNoteQueryInstance(false, topic_1_1.getId(), topic_1_2.getId());
        notes = getAndConvertNotes(parameters);
        Assert.assertEquals(notes.size(), 1);
        Assert.assertTrue(notes.contains(note1_1));
    }
}
