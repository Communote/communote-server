package com.communote.server.core.note;

import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.communote.server.api.core.note.NoteStoringTO;
import com.communote.server.core.storing.ResourceStoringManagement;
import com.communote.server.core.vo.blog.NoteModificationResult;
import com.communote.server.core.vo.blog.NoteModificationStatus;
import com.communote.server.model.blog.Blog;
import com.communote.server.model.user.User;
import com.communote.server.persistence.resource.AttachmentDao;
import com.communote.server.service.NoteService;
import com.communote.server.test.CommunoteIntegrationTest;
import com.communote.server.test.util.AuthenticationTestUtils;
import com.communote.server.test.util.TestUtils;

/**
 * This class contains tests for creating notes with attachments.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class NoteManagementTestForAttachments extends CommunoteIntegrationTest {

    @Autowired
    private NoteService noteService;
    @Autowired
    private AttachmentDao attachmentDao;
    @Autowired
    private ResourceStoringManagement resourceStoringManagement;

    /**
     * Test that it is not possible to create a note with an attachment which is already assigned to
     * another note.
     *
     * @throws Exception
     *             in case the test failed
     */
    @Test
    public void testFailureOnAlreadyAssignedAttachment() throws Exception {
        User user = TestUtils.createRandomUser(false);
        AuthenticationTestUtils.setSecurityContext(user);
        Blog topic = TestUtils.createRandomBlog(true, true, user);
        Long attachmentId = TestUtils.createAttachment().getId();

        TestUtils
                .createAndStoreCommonNote(topic, user.getId(), "Test", new Long[] { attachmentId });
        NoteStoringTO noteStoringTO = TestUtils.createCommonNote(topic, user.getId(), "Test",
                new Long[] { attachmentId });

        NoteModificationResult noteModificationResult = noteService.createNote(noteStoringTO, null);
        Assert.assertEquals(noteModificationResult.getStatus(), NoteModificationStatus.SYSTEM_ERROR);
    }

    /**
     * Method to test that a note can't be created if it references an attachment which does not
     * exist anymore.
     *
     * @throws Exception
     *             in case the test failed
     */
    @Test
    public void testFailureOnMissingAttachment() throws Exception {
        User user = TestUtils.createRandomUser(false);
        Blog topic = TestUtils.createRandomBlog(true, true, user);
        AuthenticationTestUtils.setSecurityContext(user);
        Long attachmentId = TestUtils.createAttachment().getId();
        resourceStoringManagement.deleteAttachment(attachmentId);
        Assert.assertNull(attachmentDao.load(attachmentId));
        NoteStoringTO noteStoringTO = TestUtils.createCommonNote(topic, user.getId());
        noteStoringTO.setAttachmentIds(new Long[] { attachmentId });
        NoteModificationResult note = noteService.createNote(noteStoringTO, null);
        Assert.assertEquals(note.getStatus(), NoteModificationStatus.MISSING_ATTACHMENT);
    }
}
