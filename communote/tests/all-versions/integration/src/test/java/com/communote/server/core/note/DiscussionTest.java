package com.communote.server.core.note;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.note.NoteStoringTO;
import com.communote.server.core.filter.listitems.SimpleNoteListItem;
import com.communote.server.core.vo.blog.NoteModificationResult;
import com.communote.server.core.vo.blog.NoteModificationStatus;
import com.communote.server.model.blog.Blog;
import com.communote.server.model.user.User;
import com.communote.server.service.NoteService;
import com.communote.server.test.CommunoteIntegrationTest;
import com.communote.server.test.util.AuthenticationTestUtils;
import com.communote.server.test.util.TestUtils;


/**
 * Test discussion related NoteMangement functionality
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class DiscussionTest extends CommunoteIntegrationTest {
    private User user2;
    private User user1;
    private User user3;
    private Blog blog;
    private NoteService noteManagement;
    private Long rootNoteId;

    /**
     * Asserts that the comments can be evaluated correctly
     * 
     * @param noteId
     *            a note which is part of a discussion
     * @param numberReplies
     *            the expected number of replies the note represented by the ID should have
     * @param commentNoteIds
     *            the expected note IDs of the discussion the current user can read
     * @throws Exception
     *             in case an assertion failed
     */
    private void assertCorrectComments(Long noteId, int numberReplies, List<Long> commentNoteIds)
            throws Exception {
        // increment by one for root note
        int numberNotesInDiscussion = commentNoteIds.size() + 1;
        Assert.assertEquals(noteManagement.getNumberOfReplies(noteId), numberReplies);
        Assert.assertEquals(noteManagement.getNumberOfNotesInDiscussion(noteId),
                numberNotesInDiscussion);
        List<SimpleNoteListItem> actualCommentNotes = noteManagement
                .getCommentsOfDiscussion(noteId);
        Assert.assertEquals(actualCommentNotes.size(), commentNoteIds.size());
        for (SimpleNoteListItem item : actualCommentNotes) {
            Assert.assertEquals(commentNoteIds.contains(item.getId()), true);
        }
    }

    /**
     * Creates the note from the provided TO and asserts that it was created correctly
     * 
     * @param storingTO
     *            the TO
     * @return the ID of the created note
     * @throws Exception
     *             in case the creation failed
     */
    private Long createNote(NoteStoringTO storingTO) throws Exception {
        NoteModificationResult result = noteManagement.createNote(storingTO, null);
        Assert.assertEquals(result.getStatus(), NoteModificationStatus.SUCCESS);
        Assert.assertNotNull(result.getNoteId());
        return result.getNoteId();
    }

    /**
     * Setup.
     * 
     * @throws Exception
     *             Exception.
     */
    @BeforeClass(dependsOnGroups = "integration-test-setup")
    public void setup() throws Exception {
        noteManagement = ServiceLocator.instance().getService(NoteService.class);
        user1 = TestUtils.createRandomUser(false);
        user2 = TestUtils.createRandomUser(false);
        user3 = TestUtils.createRandomUser(false);
        blog = TestUtils.createRandomBlog(true, true, user1, user2, user3);
        NoteStoringTO rootNoteStoringTO = TestUtils.createCommonNote(blog, user1.getId(),
                "root message");
        rootNoteId = createNote(rootNoteStoringTO);
    }

    /**
     * Test the comment retrieval methods of NoteManagement
     * 
     * @throws Exception
     *             in case the test fails
     */
    @Test
    public void testCommentRetrieval() throws Exception {
        List<Long> emptyList = Collections.emptyList();
        assertCorrectComments(rootNoteId, 0, emptyList);
        List<Long> commentIds = new ArrayList<Long>(5);
        int rootNoteReplies = 3;
        for (int i = 1; i <= rootNoteReplies; i++) {
            NoteStoringTO comment = TestUtils.createCommonNote(blog, user1.getId(), "comment " + i);
            comment.setParentNoteId(rootNoteId);
            commentIds.add(createNote(comment));
            AuthenticationTestUtils.setSecurityContext(user1);
            assertCorrectComments(rootNoteId, i, commentIds);
            AuthenticationTestUtils.setSecurityContext(user2);
            assertCorrectComments(rootNoteId, i, commentIds);
        }

        // check comment on comment
        Long firstCommentId = commentIds.get(0);
        NoteStoringTO comment = TestUtils.createCommonNote(blog, user1.getId(),
                "comment on comment");
        comment.setParentNoteId(firstCommentId);
        commentIds.add(createNote(comment));
        rootNoteReplies++;
        AuthenticationTestUtils.setSecurityContext(user1);
        assertCorrectComments(rootNoteId, rootNoteReplies, commentIds);
        // assert that the comment the comment was added to must only have one reply
        Assert.assertEquals(noteManagement.getNumberOfReplies(firstCommentId), 1);
        AuthenticationTestUtils.setSecurityContext(user2);
        assertCorrectComments(rootNoteId, rootNoteReplies, commentIds);
        Assert.assertEquals(noteManagement.getNumberOfReplies(firstCommentId), 1);

        // test Autosave exclusion
        Long commentOnFirstComment = commentIds.get(commentIds.size() - 1);
        comment = TestUtils.createCommonAutosave(blog, user1.getId(), "comment autosave");
        comment.setParentNoteId(commentOnFirstComment);
        createNote(comment);
        AuthenticationTestUtils.setSecurityContext(user1);
        assertCorrectComments(rootNoteId, rootNoteReplies, commentIds);
        Assert.assertEquals(noteManagement.getNumberOfReplies(firstCommentId), 1);
        AuthenticationTestUtils.setSecurityContext(user2);
        assertCorrectComments(rootNoteId, rootNoteReplies, commentIds);
        Assert.assertEquals(noteManagement.getNumberOfReplies(firstCommentId), 1);

        // DM exclusion where appropriate
        comment = TestUtils.createCommonNote(blog, user1.getId(), "DM comment");
        comment.setParentNoteId(rootNoteId);
        comment.getUsersToNotify().add(user2.getAlias());
        comment.setIsDirectMessage(true);
        List<Long> commentsWithDM = new ArrayList<Long>(commentIds);
        commentsWithDM.add(createNote(comment));
        rootNoteReplies++;

        AuthenticationTestUtils.setSecurityContext(user1);
        assertCorrectComments(rootNoteId, rootNoteReplies, commentsWithDM);
        AuthenticationTestUtils.setSecurityContext(user2);
        assertCorrectComments(rootNoteId, rootNoteReplies, commentsWithDM);
        AuthenticationTestUtils.setSecurityContext(user3);
        // also pass rootNoteReplies because it doesn't matter whether the user can read it or not
        assertCorrectComments(rootNoteId, rootNoteReplies, commentIds);
    }
}
