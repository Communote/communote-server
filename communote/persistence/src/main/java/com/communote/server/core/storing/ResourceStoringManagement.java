package com.communote.server.core.storing;

import java.util.Collection;
import java.util.Date;

import com.communote.common.converter.Converter;
import com.communote.server.api.core.common.NotFoundException;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.core.blog.AttachmentNotFoundException;
import com.communote.server.core.crc.vo.ContentId;
import com.communote.server.core.vo.content.AttachmentTO;
import com.communote.server.model.attachment.Attachment;
import com.communote.server.persistence.crc.ContentRepositoryException;

/**
 * Service interface for attachment handling
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public interface ResourceStoringManagement {

    /**
     * Method to add a new processor to the list of processors.
     *
     * @param processor
     *            The processor.
     */
    public void addAttachmentStoringPreProcessor(AttachmentStoringPreProcessor processor);

    /**
     * Assert that the current user has read access to the attachment
     *
     * @param attachmentId
     *            the ID of the attachment
     * @throws AuthorizationException
     *             in case the user has no read access
     * @throws AttachmentNotFoundException
     *             in case the attachment does not exist
     */
    public void assertReadAccess(Long attachmentId) throws AuthorizationException,
            AttachmentNotFoundException;

    /**
     * Assert that the current user has access to edit the attachment
     *
     * @param attachmentId
     *            the ID of the attachment
     * @throws AuthorizationException
     *             in case the user has no read access
     * @throws AttachmentNotFoundException
     *             in case the attachment does not exist
     */
    public void assertWriteAccess(Long attachmentId) throws AuthorizationException,
            AttachmentNotFoundException;

    /**
     * Deletes an attachment.
     *
     * @param attachmentId
     *            the ID of attachment to delete
     * @throws AttachmentStillAssignedException
     *             in case the attachment cannot be deleted because it is still assigned to the note
     * @throws AuthorizationException
     *             Thrown, when the current user is not allowed to remove the given attachment.
     */
    public void deleteAttachment(Long attachmentId) throws AttachmentStillAssignedException,
            AuthorizationException;

    /**
     * Remove all attachments which are not connected to a note and which are older than the given
     * date.
     *
     * @param upperUploadDate
     *            Attachments must be older than this date.
     * @return The number of removed attachments.
     * @throws AuthorizationException
     *             in case the current user is not the internal system user
     */
    public int deleteOrphanedAttachments(Date upperUploadDate) throws AuthorizationException;

    /**
     * Deletes all provided attachments that are not connected to a note.
     *
     * @param attachmentIds
     *            the IDs of the attachments to delete
     */
    public void deleteOrphanedAttachments(java.util.Collection<Long> attachmentIds)
            throws AuthorizationException;

    /**
     * <p>
     * Returns an attachment with the delivered content id. This method checks if the current user
     * has read access to the note the attachment belongs to.
     * </p>
     */
    public AttachmentTO getAttachment(ContentId contentId) throws ContentRepositoryException,
            AuthorizationException;

    /**
     * Get an attachment by the given ID. This method checks if the current user has read access to
     * the note the attachment belongs to.
     *
     * @param attachmentId
     *            the ID of the attachment
     * @throws AuthorizationException
     *             in case the current user has no access to the attachment. This is the case if the
     *             attachment was not yet published and the current user is not the uploader or the
     *             attachment is published and the current user has no access to the note of the
     *             attachment.
     * @throws AttachmentNotFoundException
     *             in case the attachment does not exist
     * @throws ContentRepositoryException
     *             in case the attachment data cannot be read from the content repository were it is
     *             stored
     */
    public AttachmentTO getAttachment(Long attachmentId) throws ContentRepositoryException,
            AuthorizationException, AttachmentNotFoundException;

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
     * @return Collection of all attachments for the given note.
     * @throws NotFoundException
     *             in case the note does not exist
     * @throws AuthorizationException
     *             in case the current user has no read access to the note
     * @param <T>
     *            The target type of the conversion
     */
    public <T> Collection<T> getAttachmentsOfNote(Long noteId,
            Collection<Long> attachmentIdsToFilter, Converter<Attachment, T> converter)
            throws AuthorizationException, NotFoundException;

    /**
     * Get the note the given attachment is assigned to.
     *
     * @param contentId
     *            the content ID of the attachment
     * @return the ID of the note or null if the attachment does not exists or is not is not yet
     *         published and thus not connected to a note
     * @throws AuthorizationException
     *             in case the current user has no access to the note
     */
    public Long getNoteOfAttachment(ContentId contentId) throws AuthorizationException;

    /**
     * Get the note the given attachment is assigned to.
     *
     * @param attachmentId
     *            the ID of the attachment
     * @return the ID of the note or null if the attachment does not exists or is not is not yet
     *         published and thus not connected to a note
     * @throws AuthorizationException
     *             in case the current user has no access to the note
     */
    Long getNoteOfAttachment(Long attachmentId) throws AuthorizationException;

    /**
     * <p>
     * Migrate the attachments with an empty content type
     * </p>
     */
    public void migrateContentTypeEmptyAttachments();

    /**
     * Method to remove a new processor from the list of registered processors.
     *
     * @param processor
     *            The processor.
     */
    public void removeAttachmentStoringPreProcessor(AttachmentStoringPreProcessor processor);

    /**
     * Store the attachment in the repository and return the created entity. The uploader of
     * attachment will be the current user or in case the current user is the internal system user,
     * the user identified by the uploaderId.
     *
     * @param attachment
     *            TO containing the details of the attachment to store.
     * @return The stored attachment.
     * @throws AuthorizationException
     *             in case the current user is not the internal system or an authenticated Communote
     *             user
     */
    public Attachment storeAttachment(AttachmentTO attachment) throws AuthorizationException;

    /**
     * Create a copy of a stored attachment. The copy will be returned.
     *
     * @throws AuthorizationException
     *             in case the current user is not the internal system or an authenticated Communote
     *             user
     */
    public Attachment storeCopyOfAttachment(Attachment contentResource)
            throws AuthorizationException;

}
