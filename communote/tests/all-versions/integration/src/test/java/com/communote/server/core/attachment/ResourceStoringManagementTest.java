package com.communote.server.core.attachment;

import java.util.Date;
import java.util.Locale;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.blog.BlogRightsManagement;
import com.communote.server.api.core.note.NoteData;
import com.communote.server.api.core.note.NoteRenderContext;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.core.security.AuthenticationHelper;
import com.communote.server.core.storing.AttachmentStillAssignedException;
import com.communote.server.core.storing.ResourceStoringManagement;
import com.communote.server.core.vo.content.AttachmentTO;
import com.communote.server.model.attachment.Attachment;
import com.communote.server.model.attachment.AttachmentStatus;
import com.communote.server.model.blog.Blog;
import com.communote.server.model.user.User;
import com.communote.server.persistence.resource.AttachmentDao;
import com.communote.server.service.NoteService;
import com.communote.server.test.CommunoteIntegrationTest;
import com.communote.server.test.util.AuthenticationTestUtils;
import com.communote.server.test.util.TestUtils;

/**
 * This contains tests for {@link ResourceStoringManagement}.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class ResourceStoringManagementTest extends CommunoteIntegrationTest {

    @Autowired
    private AttachmentDao attachmentDao;
    @Autowired
    private NoteService noteService;
    @Autowired
    private ResourceStoringManagement resourceStoringManagement;

    /**
     * Test that the access check for attachments is working and respecting published/unpublished
     * status.
     *
     * Note: Further tests for the access checks are done in {@link #testGetAttachment()}
     *
     * @throws Exception
     *             in case the test failed
     */
    @Test
    public void testAttachmentAccess() throws Exception {
        User user1 = TestUtils.createRandomUser(false);
        User user2 = TestUtils.createRandomUser(false);
        Blog blog = TestUtils.createRandomBlog(false, false, user2, user1);

        AuthenticationTestUtils.setSecurityContext(user1);
        Long attachmentId = TestUtils.createAttachment().getId();
        // uploader must have access
        AttachmentTO uploadedAttachment = resourceStoringManagement.getAttachment(attachmentId);
        Assert.assertNotNull(uploadedAttachment.getUploadDate());
        Assert.assertEquals(uploadedAttachment.getUploaderId(), user1.getId());
        Assert.assertEquals(uploadedAttachment.getStatus(), AttachmentStatus.UPLOADED);
        resourceStoringManagement.assertWriteAccess(attachmentId);
        AuthenticationTestUtils.setSecurityContext(user2);
        try {
            resourceStoringManagement.getAttachment(attachmentId);
            Assert.fail("Only the user who uploaded the attachment should have access to an unpublished attachment");
        } catch (AuthorizationException e) {
            // Okay
        }
        try {
            resourceStoringManagement.assertWriteAccess(attachmentId);
            Assert.fail("Only the user who uploaded the attachment should have write access to an unpublished attachment");
        } catch (AuthorizationException e) {
            // Okay
        }

        // Publish attachment
        TestUtils.createAndStoreCommonNote(blog, user1.getId(), UUID.randomUUID().toString(),
                new Long[] { attachmentId });
        // topic members should have access
        AuthenticationTestUtils.setSecurityContext(user1);
        uploadedAttachment = resourceStoringManagement.getAttachment(attachmentId);
        Assert.assertEquals(uploadedAttachment.getStatus(), AttachmentStatus.PUBLISHED);
        resourceStoringManagement.assertWriteAccess(attachmentId);
        AuthenticationTestUtils.setSecurityContext(user2);
        Assert.assertEquals(resourceStoringManagement.getAttachment(attachmentId).getUploaderId(),
                user1.getId());
        // only the author of the note should be allowed to modify the attachment (more
        // specifically: its properties)
        try {
            resourceStoringManagement.assertWriteAccess(attachmentId);
            Assert.fail("Only the author of the note of a published attachment should have write access");
        } catch (AuthorizationException e) {
            // Okay
        }
        // remove user1 from topic
        ServiceLocator.findService(BlogRightsManagement.class).removeMemberByEntityId(blog.getId(),
                user1.getId());
        Assert.assertNotNull(resourceStoringManagement.getAttachment(attachmentId));

        AuthenticationTestUtils.setSecurityContext(user1);
        try {
            resourceStoringManagement.getAttachment(attachmentId);
            Assert.fail("Only users with access to the note are allowed to access a published attachment assigned to a note");
        } catch (AuthorizationException e) {
            // Okay
        }
        // since author is not member of topic anymore he should not be allowed to modify the
        // attachment
        try {
            resourceStoringManagement.assertWriteAccess(attachmentId);
            Assert.fail("Author of note without topic access must not be allowed to modify attachment");
        } catch (AuthorizationException e) {
            // Okay
        }

    }

    /**
     * Test deletion of attachments
     *
     * @throws Exception
     *             in case the test failed
     */

    @Test
    public void testDeleteAttachment() throws Exception {
        User user = TestUtils.createRandomUser(false);
        Blog blog = TestUtils.createRandomBlog(true, true, user);
        AuthenticationTestUtils.setSecurityContext(user);
        Long attachmentId = TestUtils.createAttachment().getId();
        TestUtils.createAndStoreCommonNote(blog, user.getId(), UUID.randomUUID().toString(),
                new Long[] { attachmentId });
        try {
            resourceStoringManagement.deleteAttachment(attachmentId);
            Assert.fail("It should not be possible to delete an assigned attachment.");
        } catch (AttachmentStillAssignedException e) {
            // Okay
        }
        // Non existing attachment
        resourceStoringManagement.deleteAttachment(attachmentId + 1);

        // Legacy Code: unassigned attachments without owner should just be ignored
        Attachment attachment = attachmentDao.load(attachmentId);
        attachment.setNote(null);
        attachment.setUploader(null);
        attachmentDao.update(attachment);
        resourceStoringManagement.deleteAttachment(attachment.getId());
        Assert.assertNotNull(attachmentDao.load(attachment.getId()));
        // internal system user must be able to delete the attachment
        AuthenticationHelper.setInternalSystemToSecurityContext();
        resourceStoringManagement.deleteAttachment(attachment.getId());
        Assert.assertNull(attachmentDao.load(attachment.getId()));

        AuthenticationTestUtils.setSecurityContext(user);
        attachment = TestUtils.createAttachment();
        // Another user tries to delete the attachment
        AuthenticationTestUtils.setSecurityContext(TestUtils.createRandomUser(false));
        try {
            resourceStoringManagement.deleteAttachment(attachment.getId());
            Assert.fail("Only the user itself should be able to delete the attachment.");
        } catch (AuthorizationException e) {
            // Okay
        }

        // The uploader can delete the attachment
        AuthenticationTestUtils.setSecurityContext(user);
        resourceStoringManagement.deleteAttachment(attachment.getId());
        Assert.assertNull(attachmentDao.load(attachment.getId()));

        // The internal system user can delete any uploaded attachment
        AuthenticationTestUtils.setSecurityContext(user);
        attachment = TestUtils.createAttachment();
        AuthenticationHelper.setInternalSystemToSecurityContext();
        resourceStoringManagement.deleteAttachment(attachment.getId());
        Assert.assertNull(attachmentDao.load(attachment.getId()));
    }

    /**
     * Method to test the deletion of a bunch of orphaned attachments.
     *
     * @throws AuthorizationException
     *             The test should fail, if thrown.
     */
    @Test
    public void testDeleteManyOrphanedAttachments() throws AuthorizationException {
        User user = TestUtils.createRandomUser(false);
        Long upperUploadTimestamp = System.currentTimeMillis();

        AuthenticationTestUtils.setSecurityContext(user);
        int limit = 45;
        for (int i = 0; i < limit; i++) {
            TestUtils.createAttachment();
        }
        AuthenticationHelper.setInternalSystemToSecurityContext();
        Assert.assertEquals(resourceStoringManagement.deleteOrphanedAttachments(new Date(
                upperUploadTimestamp + 100000)), limit);
        Assert.assertEquals(
                attachmentDao.findOrphanedAttachments(new Date(upperUploadTimestamp + 100000))
                        .size(), 0);
    }

    /**
     * Method to test the deletion of an orphaned attachment.
     *
     * @throws AuthorizationException
     *             in case the test failed
     */
    @Test
    public void testDeleteOrphanedAttachment() throws AuthorizationException {

        User user = TestUtils.createRandomUser(false);
        Blog topic = TestUtils.createRandomBlog(false, false, user);
        Long upperUploadTimestamp = System.currentTimeMillis();

        AuthenticationTestUtils.setSecurityContext(user);
        Long orphanedAttachmentId = TestUtils.createAttachment().getId();
        Long noteAttachmentId = TestUtils.createAttachment().getId();

        TestUtils.createAndStoreCommonNote(topic, user.getId(), UUID.randomUUID().toString(),
                new Long[] { noteAttachmentId });
        try {
            resourceStoringManagement.deleteOrphanedAttachments(new Date(upperUploadTimestamp));
            Assert.fail();
        } catch (AuthorizationException e) {
            // Okay
        }

        AuthenticationTestUtils.setManagerContext();
        try {
            resourceStoringManagement.deleteOrphanedAttachments(new Date(upperUploadTimestamp));
            Assert.fail();
        } catch (AuthorizationException e) {
            // Okay
        }

        AuthenticationHelper.setInternalSystemToSecurityContext();
        Assert.assertEquals(resourceStoringManagement.deleteOrphanedAttachments(new Date(
                upperUploadTimestamp - 1000)), 0);
        Assert.assertEquals(resourceStoringManagement.deleteOrphanedAttachments(new Date(
                upperUploadTimestamp + 100000)), 1);
        Assert.assertEquals(
                attachmentDao.findOrphanedAttachments(new Date(upperUploadTimestamp + 100000))
                .size(), 0);
        Assert.assertNull(attachmentDao.load(orphanedAttachmentId));
        Assert.assertNotNull(attachmentDao.load(noteAttachmentId));
    }

    /**
     * Tests for {@link ResourceStoringManagement#getAttachment(Long)}
     *
     * @throws Exception
     *             in case the test failed
     */
    @Test
    public void testGetAttachment() throws Exception {
        User user1 = TestUtils.createRandomUser(false);
        User user2 = TestUtils.createRandomUser(false);
        User user3 = TestUtils.createRandomUser(false);
        Blog blog = TestUtils.createRandomBlog(true, true, user1, user2, user3);
        Long attachmentId1 = TestUtils.createAttachment().getId();
        Long attachmentId2 = TestUtils.createAttachment().getId();
        Long noteId1 = TestUtils.createAndStoreCommonNote(blog, user1.getId(), UUID.randomUUID()
                .toString(), new Long[] { attachmentId1 });
        Long noteId2 = TestUtils.createAndStoreCommonNote(blog, user2.getId(),
                "d @" + user3.getAlias() + " " + UUID.randomUUID().toString(),
                new Long[] { attachmentId2 });

        NoteRenderContext context = new NoteRenderContext(null, Locale.ENGLISH);
        NoteData noteData = noteService.getNote(noteId1, context);
        Assert.assertFalse(noteData.isDirect());
        // check attachments
        Assert.assertEquals(noteData.getAttachments().size(), 1);
        Assert.assertEquals(noteData.getAttachments().get(0).getId(), attachmentId1);
        AuthenticationTestUtils.setSecurityContext(user2);
        Assert.assertFalse(noteService.getNote(noteId1, context).isDirect());
        noteData = noteService.getNote(noteId2, context);
        Assert.assertTrue(noteData.isDirect());
        // check attachments
        Assert.assertEquals(noteData.getAttachments().size(), 1);
        Assert.assertEquals(noteData.getAttachments().get(0).getId(), attachmentId2);
        AuthenticationTestUtils.setSecurityContext(user3);
        Assert.assertFalse(noteService.getNote(noteId1, context).isDirect());
        Assert.assertTrue(noteService.getNote(noteId2, context).isDirect());

        // Check access for normal and direct messages.
        AuthenticationTestUtils.setSecurityContext(user1);
        Assert.assertNotNull(resourceStoringManagement.getAttachment(attachmentId1));
        AuthenticationTestUtils.setSecurityContext(user2);
        Assert.assertNotNull(resourceStoringManagement.getAttachment(attachmentId1));
        AuthenticationTestUtils.setSecurityContext(user3);
        Assert.assertNotNull(resourceStoringManagement.getAttachment(attachmentId1));

        try {
            AuthenticationTestUtils.setSecurityContext(user1);
            resourceStoringManagement.getAttachment(attachmentId2);
            Assert.fail("The user should not be able to access this attachment.");
        } catch (AuthorizationException e) {
            // expected
        }

        AuthenticationTestUtils.setSecurityContext(user2);
        Assert.assertNotNull(resourceStoringManagement.getAttachment(attachmentId2));
        AuthenticationTestUtils.setSecurityContext(user3);
        Assert.assertNotNull(resourceStoringManagement.getAttachment(attachmentId2));

        // Check access for different blogs.
        Blog blog2 = TestUtils.createRandomBlog(false, false, user2, user3);
        Long attachmentId3 = TestUtils.createAttachment().getId();
        TestUtils.createAndStoreCommonNote(blog2, user2.getId(), UUID.randomUUID().toString(),
                new Long[] { attachmentId3 });
        try {
            AuthenticationTestUtils.setSecurityContext(user1);
            resourceStoringManagement.getAttachment(attachmentId3);
            Assert.fail("The user should not be able to access this attachment.");
        } catch (AuthorizationException e) {
            // expected
        }
        AuthenticationTestUtils.setSecurityContext(user2);
        Assert.assertNotNull(resourceStoringManagement.getAttachment(attachmentId3));
        AuthenticationTestUtils.setSecurityContext(user3);
        Assert.assertNotNull(resourceStoringManagement.getAttachment(attachmentId3));
    }
}
