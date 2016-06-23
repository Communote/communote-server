package com.communote.server.core.storing;

import java.util.Collection;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.communote.common.converter.Converter;
import com.communote.server.api.core.common.NotFoundException;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.core.blog.AttachmentNotFoundException;
import com.communote.server.model.attachment.Attachment;

/**
 * <p>
 * Spring Service base class for
 * <code>com.communote.server.service.storing.ResourceStoringManagement</code>, provides access to
 * all services and entities referenced by this service.
 * </p>
 *
 * @see ResourceStoringManagement
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
@Transactional(propagation = Propagation.REQUIRED)
public abstract class ResourceStoringManagementBase implements ResourceStoringManagement {

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteAttachment(Long attachmentId) throws AttachmentStillAssignedException,
    AuthorizationException {
        if (attachmentId == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.service.storing.ResourceStoringManagement"
                            + ".deleteAttachment(Long attachmentId) - 'attachmentId' can not be null");
        }
        try {
            this.handleDeleteAttachment(attachmentId);
        } catch (RuntimeException rt) {
            throw new ResourceStoringManagementException(
                    "Error performing 'com.communote.server.service.storing.ResourceStoringManagement"
                            + ".deleteAttachment(Long attachmentId)' --> " + rt, rt);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteOrphanedAttachments(Collection<Long> attachmentIds)
            throws AuthorizationException {
        if (attachmentIds == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.service.storing.ResourceStoringManagement"
                            + ".deleteOrphanedAttachments(Collection<Long> attachmentIds) - 'attachmentIds' can not be null");
        }
        try {
            this.handleDeleteOrphanedAttachments(attachmentIds);
        } catch (RuntimeException rt) {
            throw new ResourceStoringManagementException(
                    "Error performing 'com.communote.server.service.storing.ResourceStoringManagement"
                            + ".deleteOrphanedAttachments(Collection<Long> attachmentIds)' --> "
                            + rt, rt);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public com.communote.server.core.vo.content.AttachmentTO getAttachment(
            com.communote.server.core.crc.vo.ContentId contentId)
                    throws com.communote.server.persistence.crc.ContentRepositoryException,
                    com.communote.server.api.core.security.AuthorizationException {
        if (contentId == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.service.storing.ResourceStoringManagement"
                            + ".getAttachment(ContentId contentId) - 'contentId' can not be null");
        }
        if (contentId.getContentId() == null || contentId.getContentId().trim().length() == 0) {
            throw new IllegalArgumentException(
                    "com.communote.server.service.storing.ResourceStoringManagement"
                            + ".getAttachment(ContentId contentId) - 'contentId.contentId' can not be null or empty");
        }
        if (contentId.getConnectorId() == null || contentId.getConnectorId().trim().length() == 0) {
            throw new IllegalArgumentException(
                    "com.communote.server.service.storing.ResourceStoringManagement"
                            + ".getAttachment(ContentId contentId) - 'contentId.connectorId' can not be null or empty");
        }
        try {
            return this.handleGetAttachment(contentId);
        } catch (RuntimeException rt) {
            throw new ResourceStoringManagementException(
                    "Error performing 'com.communote.server.service.storing.ResourceStoringManagement"
                            + ".getAttachment(ContentId contentId)' --> "
                            + rt, rt);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public com.communote.server.core.vo.content.AttachmentTO getAttachment(Long attachmentId)
            throws com.communote.server.persistence.crc.ContentRepositoryException,
            com.communote.server.api.core.security.AuthorizationException,
            AttachmentNotFoundException {
        if (attachmentId == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.service.storing.ResourceStoringManagement"
                            + ".getAttachment(Long attachmentId) - 'attachmentId' can not be null");
        }
        try {
            return this.handleGetAttachment(attachmentId);
        } catch (RuntimeException rt) {
            throw new ResourceStoringManagementException(
                    "Error performing 'com.communote.server.service.storing.ResourceStoringManagement"
                            + ".getAttachment(Long attachmentId)' --> " + rt, rt);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public <T> Collection<T> getAttachmentsOfNote(Long noteId,
            Collection<Long> attachmentIdsToFilter, Converter<Attachment, T> converter)
                    throws AuthorizationException, NotFoundException {
        if (noteId == null) {
            throw new IllegalArgumentException("the noteId must not be null");
        }
        return handleGetAttachmentsOfNote(noteId, attachmentIdsToFilter, converter);
    }

    /**
     * Performs the core logic for {@link #deleteAttachment(Long)}
     */
    protected abstract void handleDeleteAttachment(Long attachmentId)
            throws AttachmentStillAssignedException, AuthorizationException;

    /**
     * Performs the core logic for {@link #deleteOrphanedAttachments(java.util.Collection)}
     */
    protected abstract void handleDeleteOrphanedAttachments(Collection<Long> attachmentIds)
            throws AuthorizationException;

    /**
     * Performs the core logic for
     * {@link #getAttachment(com.communote.server.core.crc.vo.ContentId)}
     */
    protected abstract com.communote.server.core.vo.content.AttachmentTO handleGetAttachment(
            com.communote.server.core.crc.vo.ContentId contentId)
                    throws com.communote.server.persistence.crc.ContentRepositoryException,
                    com.communote.server.api.core.security.AuthorizationException;

    /**
     * Performs the core logic for {@link #getAttachment(Long)}
     *
     * @throws AttachmentNotFoundException
     */
    protected abstract com.communote.server.core.vo.content.AttachmentTO handleGetAttachment(
            Long attachmentId)
                    throws com.communote.server.persistence.crc.ContentRepositoryException,
                    com.communote.server.api.core.security.AuthorizationException,
            AttachmentNotFoundException;

    /**
     * Return the attachments of a note converted with the help of the provided converter
     *
     * @param noteId
     *            the ID of the note
     * @param attachmentIdsToFilter
     *            optional collection of attachment IDs to filter the result by only adding those
     *            attachments whose ID is in the collection. If omitted all attachments will be
     *            considered.
     * @param converter
     *            the converter to convert the found attachments
     * @return Attachments of the given notes.
     * @throws NotFoundException
     *             in case the note does not exist
     * @throws AuthorizationException
     *             in case the current user has no read access to the note
     * @param <T>
     *            The target type of the conversion
     */
    protected abstract <T> Collection<T> handleGetAttachmentsOfNote(Long noteId,
            Collection<Long> attachmentIdsToFilter, Converter<Attachment, T> converter)
                    throws AuthorizationException, NotFoundException;

    /**
     * Performs the core logic for
     * {@link #storeAttachment(com.communote.server.core.vo.content.AttachmentTO)}
     */
    protected abstract Attachment handleStoreAttachment(
            com.communote.server.core.vo.content.AttachmentTO attachment)
                    throws AuthorizationException;

    /**
     * Performs the core logic for {@link #storeCopyOfAttachment(Attachment)}
     *
     * @throws AuthorizationException
     */
    protected abstract Attachment handleStoreCopyOfAttachment(Attachment contentResource)
            throws AuthorizationException;

    @Override
    public Attachment storeAttachment(com.communote.server.core.vo.content.AttachmentTO attachment)
            throws AuthorizationException {
        if (attachment == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.service.storing.ResourceStoringManagement"
                            + ".storeAttachment(AttachmentTO attachment) - 'attachment' can not be null");
        }
        try {
            return this.handleStoreAttachment(attachment);
        } catch (ResourceStoringManagementException e) {
            throw e;
        } catch (RuntimeException rt) {
            throw new ResourceStoringManagementException(
                    "Error performing 'com.communote.server.service.storing.ResourceStoringManagement"
                            + ".storeAttachment(AttachmentTO attachment)' --> "
                            + rt, rt);
        }
    }

    @Override
    public Attachment storeCopyOfAttachment(Attachment contentResource)
            throws AuthorizationException {
        if (contentResource == null) {
            throw new IllegalArgumentException(
                    "com.communote.server.service.storing.ResourceStoringManagement"
                            + ".storeCopyOfAttachment(Attachment contentResource) - 'contentResource' can not be null");
        }
        try {
            return this.handleStoreCopyOfAttachment(contentResource);
        } catch (ResourceStoringManagementException e) {
            throw e;
        } catch (RuntimeException rt) {
            throw new ResourceStoringManagementException(
                    "Error performing 'com.communote.server.service.storing.ResourceStoringManagement"
                            + ".storeCopyOfAttachment(Attachment contentResource)' --> " + rt, rt);
        }
    }
}