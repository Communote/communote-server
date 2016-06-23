package com.communote.server.core.storing;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.communote.common.converter.Converter;
import com.communote.common.converter.IdentityConverter;
import com.communote.common.util.DescendingOrderComparator;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.common.NotFoundException;
import com.communote.server.api.core.note.NotePermissionManagement;
import com.communote.server.api.core.property.PropertyManagement;
import com.communote.server.api.core.property.PropertyType;
import com.communote.server.api.core.property.StringPropertyTO;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.api.core.security.permission.Permission;
import com.communote.server.core.blog.AttachmentNotFoundException;
import com.communote.server.core.blog.BlogManagementException;
import com.communote.server.core.crc.FilesystemConnector;
import com.communote.server.core.crc.RepositoryConnectorDelegate;
import com.communote.server.core.crc.vo.ContentId;
import com.communote.server.core.crc.vo.ContentMetadata;
import com.communote.server.core.crc.vo.ExtendedContentId;
import com.communote.server.core.retrieval.helper.AttachmentHelper;
import com.communote.server.core.security.SecurityHelper;
import com.communote.server.core.vo.content.AttachmentTO;
import com.communote.server.model.attachment.Attachment;
import com.communote.server.model.attachment.AttachmentStatus;
import com.communote.server.model.note.Note;
import com.communote.server.model.user.User;
import com.communote.server.persistence.crc.ContentRepositoryException;
import com.communote.server.persistence.resource.AttachmentDao;
import com.communote.server.persistence.user.UserDao;

/**
 * The Class ResourceStoringManagementImpl.
 *
 * @see com.communote.server.core.storing.ResourceStoringManagement
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
@Service("resourceStoringManagement")
public class ResourceStoringManagementImpl extends ResourceStoringManagementBase {

    private static final Long UNKNOWN_CONTENT_SIZE = -1l;

    /** Logger. */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(ResourceStoringManagementImpl.class);

    private List<AttachmentStoringPreProcessor> attachmentStoringPreProcessors = new ArrayList<>();

    {
        attachmentStoringPreProcessors.add(new VirusScannerAttachmentStoringPreProcessor());
    }

    /** The repository connector delegate. */
    private RepositoryConnectorDelegate repositoryConnectorDelegate;

    @Autowired
    private AttachmentDao attachmentDao;

    @Autowired
    private UserDao kenmeiUserDao;

    @Autowired
    private NotePermissionManagement notePermissionManagement;

    @Autowired
    private PropertyManagement propertyManagement;

    @Autowired
    SessionFactory sessionFactory;

    @Override
    @Transactional(propagation = Propagation.SUPPORTS)
    public synchronized void addAttachmentStoringPreProcessor(
            AttachmentStoringPreProcessor processor) {
        ArrayList<AttachmentStoringPreProcessor> processors = new ArrayList<>(
                attachmentStoringPreProcessors);
        processors.add(processor);
        Collections.sort(processors, new DescendingOrderComparator());
        attachmentStoringPreProcessors = processors;
        LOGGER.debug("Registered AttachmentStoringPreProcessor: {}", processor.getClass().getName());
    }

    /**
     * Assert that the current user has read access to the given attachment.
     *
     * @param attachment
     *            the attachment to check
     * @throws AuthorizationException
     *             in case the user has no read access
     */
    private void assertAccess(Attachment attachment, Permission<Note> notePermission)
            throws AuthorizationException {
        Long currentUserId = SecurityHelper.getCurrentUserId();
        Note note = attachment.getNote();
        if (note == null || note.getBlog() == null) {
            if (!AttachmentStatus.PUBLISHED.equals(attachment.getStatus())
                    && (attachment.getUploader() == null || !attachment.getUploader().getId()
                    .equals(currentUserId))) {
                throw new AuthorizationException("The current user (" + currentUserId
                        + ") is or may not be the uploader (" + attachment.getUploader().getId()
                        + ") of the unpublished attachment with id " + attachment.getId());
            }
        } else if (!notePermissionManagement.hasPermission(note.getId(), notePermission)) {
            throw new AuthorizationException("The current user has no read access to this content.");
        }
    }

    @Override
    public void assertReadAccess(Long attachmentId) throws AuthorizationException,
            AttachmentNotFoundException {
        Attachment attachment = attachmentDao.load(attachmentId);
        if (attachment != null) {
            assertAccess(attachment, NotePermissionManagement.PERMISSION_READ);
        } else {
            throw new AttachmentNotFoundException(attachmentId, "Attachment with ID "
                    + attachmentId + " was not found");
        }
    }

    @Override
    public void assertWriteAccess(Long attachmentId) throws AuthorizationException,
            AttachmentNotFoundException {
        Attachment attachment = attachmentDao.load(attachmentId);
        if (attachment != null) {
            assertAccess(attachment, NotePermissionManagement.PERMISSION_EDIT);
        } else {
            throw new AttachmentNotFoundException(attachmentId, "Attachment with ID "
                    + attachmentId + " was not found");
        }
    }

    /**
     * Copy the metadata (content type, size) of the metadata instance to the attachment, take care
     * of an empty metadata instance.
     *
     * @param attachment
     *            the attachment to use
     * @param metaData
     *            the metadata to use
     */
    private void copyMetaDataToAttachment(Attachment attachment, ContentMetadata metaData) {
        String contentType = AttachmentHelper.MIME_TYPE_APPLICATION_UNKNOWN;
        Long size = UNKNOWN_CONTENT_SIZE;
        if (metaData != null) {
            contentType = metaData.getMimeType() == null ? AttachmentHelper.MIME_TYPE_APPLICATION_UNKNOWN
                    : metaData.getMimeType();
            size = metaData.getContentSize() >= 0 ? metaData.getContentSize()
                    : UNKNOWN_CONTENT_SIZE;
        }

        attachment.setContentType(contentType);
        attachment.setSize(size);
    }

    private Attachment createAttachment(AttachmentTO attachmentTO, User uploader,
            ContentId contentId) {
        Attachment attachment = Attachment.Factory.newInstance();

        attachment.setContentIdentifier(contentId.getContentId());
        attachment.setRepositoryIdentifier(contentId.getConnectorId());
        attachment.setName(attachmentTO.getMetadata().getFilename());
        attachment.setUploadDate(new Timestamp(System.currentTimeMillis()));
        attachment.setUploader(uploader);

        copyMetaDataToAttachment(attachment, attachmentTO.getMetadata());

        attachment.setStatus(attachmentTO.getStatus());
        attachment = attachmentDao.create(attachment);
        return attachment;
    }

    /**
     * Deletes the content of a content resource.
     *
     * @param res
     *            the content resource
     */
    private void deleteContentOfAttachment(Attachment res) {
        ContentId cId = new ContentId();
        cId.setConnectorId(res.getRepositoryIdentifier());
        cId.setContentId(res.getContentIdentifier());
        try {
            getRepositoryConnectorDelegate().deleteContent(cId);
        } catch (ContentRepositoryException e) {
            throw new ResourceStoringManagementException("Deleting existing resource with id "
                    + res.getId() + " failed.", e);
        }
    }

    @Override
    public int deleteOrphanedAttachments(Date upperUploadDate) throws AuthorizationException {
        if (!SecurityHelper.isInternalSystem()) {
            throw new AuthorizationException("Only the internal system user may use this method");
        }
        Collection<Long> orphandAttachmentIds = attachmentDao
                .findOrphanedAttachments(upperUploadDate);
        int counter = 0;
        for (Long id : orphandAttachmentIds) {
            Attachment attachment = attachmentDao.load(id);
            internalDeleteUnassignedAttachment(attachment);
            counter++;
            // avoid OOM by clearing first level cache
            if (counter % 40 == 0) {
                Session session = sessionFactory.getCurrentSession();
                session.flush();
                session.clear();
            }
        }
        return counter;
    }

    /**
     * Get the ID of the note the given attachment is assigned to
     *
     * @param attachment
     *            the attachment for which the note should be retrieved, can be null
     * @param identifier
     *            the ID which was used to find the attachment
     * @return the ID of note or null if the attachment was null or is not published
     * @throws AuthorizationException
     *             in case the current user has no access to the note of the attachment
     */
    private Long getNoteOfAttachment(Attachment attachment, String identifier)
            throws AuthorizationException {
        if (attachment != null && AttachmentStatus.PUBLISHED.equals(attachment.getStatus())
                && attachment.getNote() != null) {
            if (notePermissionManagement.hasPermission(attachment.getNote().getId(),
                    NotePermissionManagement.PERMISSION_READ)) {
                return attachment.getNote().getId();
            }
            throw new AuthorizationException(
                    "Current user has no access to the note of attachment " + identifier);
        }
        return null;
    }

    @Override
    public Long getNoteOfAttachment(ContentId contentId) throws AuthorizationException {
        Attachment attachment = attachmentDao.find(contentId.getContentId(),
                contentId.getConnectorId());
        return getNoteOfAttachment(attachment, contentId.toString());
    }

    @Override
    public Long getNoteOfAttachment(Long attachmentId) throws AuthorizationException {
        Attachment attachment = attachmentDao.load(attachmentId);
        return getNoteOfAttachment(attachment, attachmentId.toString());
    }

    /**
     * Returns the repository delegate.
     *
     * @return the delegate
     */
    private RepositoryConnectorDelegate getRepositoryConnectorDelegate() {
        if (repositoryConnectorDelegate == null) {
            repositoryConnectorDelegate = ServiceLocator
                    .findService(RepositoryConnectorDelegate.class);
        }
        return repositoryConnectorDelegate;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void handleDeleteAttachment(Long attachmentId)
            throws AttachmentStillAssignedException, AuthorizationException {
        internalDeleteAttachment(attachmentId, false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void handleDeleteOrphanedAttachments(Collection<Long> attachmentIds)
            throws AuthorizationException {
        try {
            for (Long id : attachmentIds) {
                internalDeleteAttachment(id, true);
            }
        } catch (AttachmentStillAssignedException e) {
            // shouldn't occur
            LOGGER.error("Unexpected exception while deleting orphaned attachments", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected AttachmentTO handleGetAttachment(ContentId contentId)
            throws ContentRepositoryException, AuthorizationException {

        final ExtendedContentId extendedContentId = new ExtendedContentId(contentId);

        Attachment attachment = extendedContentId.getAttachment();
        assertAccess(attachment, NotePermissionManagement.PERMISSION_READ);
        AttachmentTO attachmentTO = getRepositoryConnectorDelegate().getContent(extendedContentId);
        attachmentTO.setStatus(attachment.getStatus());
        // fill metadata from attachment if not provided by connector
        if (attachmentTO.getMetadata() == null) {
            ContentMetadata metadata = new ContentMetadata();
            metadata.setContentId(new ContentId(contentId));
            metadata.setContentSize(attachment.getSize());
            metadata.setDate(attachment.getUploadDate());
            metadata.setFilename(attachment.getName());
            metadata.setMimeType(attachment.getContentType());
            attachmentTO.setMetadata(metadata);
        }
        // for backwards compatibility uploader of old attachments that are not connected to a note
        // will be null
        attachmentTO.setUploaderId(attachment.getUploader() == null ? null : attachment
                .getUploader().getId());
        attachmentTO.setUploadDate(attachment.getUploadDate());
        Note note = this.attachmentDao.findNoteByContentId(extendedContentId);
        if (note != null) {
            attachmentTO.setNoteId(note.getId());
        }
        return attachmentTO;
    }

    @Override
    protected AttachmentTO handleGetAttachment(Long attachmentId)
            throws ContentRepositoryException, AuthorizationException, AttachmentNotFoundException {
        Attachment attachment = attachmentDao.load(attachmentId);
        if (attachment == null) {
            throw new AttachmentNotFoundException(attachmentId);
        }
        ContentId contentId = new ContentId();
        contentId.setConnectorId(attachment.getRepositoryIdentifier());
        contentId.setContentId(attachment.getContentIdentifier());
        return getAttachment(contentId);
    }

    @Override
    protected <T> Collection<T> handleGetAttachmentsOfNote(Long noteId,
            Collection<Long> attachmentIdsToFilter, Converter<Attachment, T> converter)
            throws AuthorizationException, NotFoundException {
        Note note = notePermissionManagement.hasAndGetWithPermission(noteId,
                NotePermissionManagement.PERMISSION_READ, new IdentityConverter<Note>());
        ArrayList<T> result = new ArrayList<>();
        if (note.getAttachments() != null) {
            for (Attachment attachment : note.getAttachments()) {
                if (attachmentIdsToFilter == null
                        || attachmentIdsToFilter.contains(attachment.getId())) {
                    result.add(converter.convert(attachment));
                }
            }
        }
        return result;
    }

    @Override
    protected Attachment handleStoreAttachment(AttachmentTO attachmentTO)
            throws AuthorizationException {
        if (attachmentTO.getMetadata() == null) {
            throw new IllegalArgumentException("attachmentTO.metadata cannot be null!");
        }
        Long uploaderId = null;
        if (SecurityHelper.isInternalSystem()) {
            uploaderId = attachmentTO.getUploaderId();
        } else if (!SecurityHelper.isPublicUser()) {
            uploaderId = SecurityHelper.getCurrentUserId();
        }
        if (uploaderId == null) {
            throw new AuthorizationException("Only the internal system user or an authenticated "
                    + "Communote user are allowed to store attachments");
        }
        User uploader = kenmeiUserDao.load(uploaderId);
        if (uploader == null) {
            throw new AuthorizationException("The user " + uploaderId + " does not exist");
        }

        if (attachmentTO.getMetadata().getDate() == null) {
            attachmentTO.getMetadata().setDate(new Date());
        }
        ContentId contentId = storeInRepository(attachmentTO);

        Attachment attachment = createAttachment(attachmentTO, uploader, contentId);
        updateAttachmentProperties(attachment, attachmentTO);
        return attachment;
    }

    @Override
    protected Attachment handleStoreCopyOfAttachment(Attachment contentResource)
            throws AuthorizationException {
        // TODO check that the current user has access to the attachment and throw
        // AuthorizationException if not!
        if (contentResource == null) {
            throw new IllegalArgumentException("content resource cannot be null");
        }
        ContentId cId = new ContentId();
        cId.setConnectorId(contentResource.getRepositoryIdentifier());
        cId.setContentId(contentResource.getContentIdentifier());
        try {
            AttachmentTO contentCopy = getRepositoryConnectorDelegate().getContent(cId);
            contentCopy.setStatus(AttachmentStatus.PUBLISHED);
            // remove content ID to force creation of new
            contentCopy.getMetadata().setContentId(null);
            return handleStoreAttachment(contentCopy);
        } catch (ContentRepositoryException e) {
            throw new ResourceStoringManagementException(
                    "Creating a copy of a content resource failed.", e);
        }
    }

    /**
     * Delete the attachment, that is delete from database and file structure<br>
     * TODO v1.1 do not delete here, only mark for deletion and delete attachment by job
     *
     * @param attachment
     *            the attachment to delete
     * @param skipAssigned
     *            ignore the call if the attachment is still assigned to a note
     * @throws AttachmentStillAssignedException
     *             if skipAssigned is false and the attachment is still assigned to a note
     * @throws AuthorizationException
     *             Thrown, when the current user is not allowed to delete the attachment.
     */
    private void internalDeleteAttachment(Attachment attachment, boolean skipAssigned)
            throws AttachmentStillAssignedException, AuthorizationException {
        if (attachment.getNote() != null) {
            if (skipAssigned) {
                return;
            } else {
                throw new AttachmentStillAssignedException("The attachment " + attachment.getId()
                        + " cannot be deleted because it is still assigned to the note "
                        + attachment.getNote().getId(), attachment.getId(), attachment.getNote()
                        .getId());
            }
        }
        if (!SecurityHelper.isInternalSystem()) {
            User uploader = attachment.getUploader();
            if (uploader == null) {
                // ignore calls for old, unassigned attachments to ensure backwards compatibility
                return;
            }
            if (!uploader.getId().equals(SecurityHelper.getCurrentUserId())) {
                throw new AuthorizationException(
                        "Only the uploader or the internal system user is allowed to delete this attachment.");
            }
        }
        internalDeleteUnassignedAttachment(attachment);
    }

    /**
     * Deletes an attachment.
     *
     * @param attachmentId
     *            the ID of the attachment
     * @param skipAssigned
     *            ignore the call if the attachment is still assigned to a note
     * @throws AttachmentStillAssignedException
     *             if skipAssigned is false and the attachment is still assigned to a note
     * @throws AuthorizationException
     *             Thrown, when the current user is not allowed to delete the attachment.
     */
    private void internalDeleteAttachment(Long attachmentId, boolean skipAssigned)
            throws AttachmentStillAssignedException, AuthorizationException {
        Attachment attachment = attachmentDao.load(attachmentId);
        if (attachment != null) {
            internalDeleteAttachment(attachment, skipAssigned);
        }
    }

    /**
     * Delete an attachment. This method assumes that the attachment is not connected to a note
     * anymore and the caller did already take care of the required authorization checks.
     *
     * @param attachment
     *            the attachment to delete
     */
    private void internalDeleteUnassignedAttachment(Attachment attachment) {
        deleteContentOfAttachment(attachment);
        attachmentDao.remove(attachment);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void migrateContentTypeEmptyAttachments() {
        Attachment attachment;
        while ((attachment = attachmentDao.findContentTypeNull()) != null) {
            ContentId contentId = new ContentId();
            contentId.setConnectorId(attachment.getRepositoryIdentifier());
            contentId.setContentId(attachment.getContentIdentifier());

            ContentMetadata metaData = null;
            try {
                metaData = getRepositoryConnectorDelegate().getMetadata(contentId);

            } catch (ContentRepositoryException e) {
                LOGGER.error("Error reading repository connector for attachment="
                        + attachment.attributesToString());
            }

            copyMetaDataToAttachment(attachment, metaData);

        }
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS)
    public synchronized void removeAttachmentStoringPreProcessor(
            AttachmentStoringPreProcessor processor) {
        if (attachmentStoringPreProcessors.contains(processor)) {
            ArrayList<AttachmentStoringPreProcessor> processors = new ArrayList<>(
                    attachmentStoringPreProcessors);
            processors.remove(processor);
            attachmentStoringPreProcessors = processors;
            LOGGER.debug("Removed AttachmentStoringPreProcessor: {}", processor.getClass()
                    .getName());
        }
    }

    /**
     * Store in repository.
     *
     * @param attachmentTO
     *            the content to
     * @return the content id
     */
    private ContentId storeInRepository(AttachmentTO attachmentTO) {
        for (AttachmentStoringPreProcessor processor : attachmentStoringPreProcessors) {
            processor.process(attachmentTO);
        }

        // store it in the repository
        ContentId contentId = attachmentTO.getMetadata().getContentId();

        if (contentId == null) {
            contentId = new ContentId();
            attachmentTO.getMetadata().setContentId(contentId);
        }

        // was it already stored?
        if (contentId.getContentId() == null) {
            try {
                if (contentId.getConnectorId() == null) {
                    // TODO better throw an exception if TO does not contain all the required
                    // details?
                    contentId.setConnectorId(FilesystemConnector.DEFAULT_FILESYSTEM_CONNECTOR);
                }

                contentId = getRepositoryConnectorDelegate().storeContent(attachmentTO);
            } catch (Exception e) {
                // TODO do not wrap exception here because it leads to problems as commented in
                // RepositoryConnectorDelegateImpl.handleAssertRepositorySizeLimitNotReached(RepositoryConnector,
                // long). But: the benefit of the RTE is that the transaction is rolled back which
                // is useful in some cases like cross posts that cause a
                // RepoSizeLimitReachedException. On the other hand an autosave of an edit with
                // attachments should not be rolled back only because of a
                // RepoSizeLimitReachedException which is caused by copying the attachments.
                LOGGER.error("Content Storing failed: " + e.getMessage(), e);
                if (e instanceof ResourceStoringManagementException) {
                    throw (ResourceStoringManagementException) e;
                }
                throw new ResourceStoringManagementException("Content Storing failed: "
                        + e.getMessage(), e);
            }
        }
        return contentId;
    }

    /**
     * Update the properties of a topic.
     *
     * @param blog
     *            the topic to update
     * @param blogDetails
     *            the transfer object holding the topic properties
     */
    private void updateAttachmentProperties(Attachment attachment, AttachmentTO attachmentTO) {
        if (attachmentTO.getProperties() != null) {
            try {
                propertyManagement.setObjectProperties(PropertyType.AttachmentProperty,
                        attachment.getId(),
                        new HashSet<StringPropertyTO>(attachmentTO.getProperties()));
            } catch (NotFoundException e) {
                LOGGER.error("Unexpected exception while updating properties topic with id "
                        + attachment.getId());
                throw new BlogManagementException("Unexpected exception.", e);
            } catch (AuthorizationException e) {
                LOGGER.error("Unexpected exception while updating properties topic with id "
                        + attachment.getId() + " and the current user "
                        + SecurityHelper.getCurrentUserAlias());
                throw new BlogManagementException("Unexpected exception.", e);
            }
        }
    }

}
