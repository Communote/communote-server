package com.communote.server.core.blog.notes.processors;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.communote.common.converter.IdentityConverter;
import com.communote.server.api.core.common.NotFoundException;
import com.communote.server.api.core.note.NoteManagementAuthorizationException;
import com.communote.server.api.core.note.NotePermissionManagement;
import com.communote.server.api.core.note.NoteStoringTO;
import com.communote.server.api.core.note.processor.NoteStoringImmutableContentPreProcessor;
import com.communote.server.api.core.note.processor.NoteStoringPreProcessorException;
import com.communote.server.api.core.property.PropertyHelper;
import com.communote.server.api.core.property.PropertyManagement;
import com.communote.server.api.core.property.PropertyType;
import com.communote.server.api.core.property.StringPropertyTO;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.core.blog.notes.processors.exceptions.InvalidPermissionForRepostException;
import com.communote.server.core.security.SecurityHelper;
import com.communote.server.core.storing.ResourceStoringManagement;
import com.communote.server.model.attachment.Attachment;
import com.communote.server.model.note.Note;

/**
 * Processor for reposts.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class RepostNoteStoringPreProcessor implements NoteStoringImmutableContentPreProcessor {

    /** Key for the ID of the note that was reposted. */
    public static final String KEY_ORIGIN_NOTE_ID = "repost.note.id";
    /** Key for the ID of author of the note that was reposted. */
    public static final String KEY_ORIGIN_AUTHOR_ID = "repost.author.id";
    /**
     * Key for holding the attachment IDs of the original note that should be reposted. This
     * property will only be set for autosaves.
     */
    public static final String KEY_ORIGIN_ATTACHMENT_IDS = "repost.autosave.attachment.ids";
    /**
     * Separator that is used to join the attachment IDs to store them in the property
     * KEY_ORIGIN_ATTACHMENT_IDS.
     */
    public static final String ORIGIN_ATTACHMENT_IDS_SEPARATOR = ",";

    private final NotePermissionManagement notePermissionManagement;
    private final ResourceStoringManagement resourceStoringManagement;

    /**
     * Constructor.
     * 
     * @param notePermissionManagement
     *            The note permission management to check for the required permission
     * @param propertyManagement
     *            PropertyManagement to register properties.
     * @param resourceStoringManagement
     *            The resource storing management to clone attachments
     */
    public RepostNoteStoringPreProcessor(NotePermissionManagement notePermissionManagement,
            PropertyManagement propertyManagement,
            ResourceStoringManagement resourceStoringManagement) {
        this.notePermissionManagement = notePermissionManagement;
        this.resourceStoringManagement = resourceStoringManagement;
        propertyManagement.addObjectPropertyFilter(PropertyType.NoteProperty,
                PropertyManagement.KEY_GROUP, KEY_ORIGIN_NOTE_ID);
        propertyManagement.addObjectPropertyFilter(PropertyType.NoteProperty,
                PropertyManagement.KEY_GROUP, KEY_ORIGIN_AUTHOR_ID);
        propertyManagement.addObjectPropertyFilter(PropertyType.NoteProperty,
                PropertyManagement.KEY_GROUP, KEY_ORIGIN_ATTACHMENT_IDS);

    }

    /**
     * Create a copy of the attachments attached to the original note which should also be attached
     * to the repost.
     * 
     * @param noteStoringTO
     *            the TO holding the details on the note to store
     * @param originalAttachments
     *            the attachments attached to the original note
     * @throws NoteStoringPreProcessorException
     *             in case the attachments couldn't be copied
     */
    private void cloneAttachments(NoteStoringTO noteStoringTO, Set<Attachment> originalAttachments)
            throws NoteStoringPreProcessorException {
        Long[] repostAttachmentIds = noteStoringTO.getAttachmentIds();
        if (originalAttachments == null || originalAttachments.size() == 0) {
            return;
        }
        HashSet<Long> newAttachmentIds = new HashSet<Long>();
        if (repostAttachmentIds != null) {
            for (Long attachmentId : repostAttachmentIds) {
                newAttachmentIds.add(attachmentId);
            }
        }
        ArrayList<Long> finalAttachmentIds = new ArrayList<Long>();
        ArrayList<Long> autosaveAttachmentIds = new ArrayList<Long>();
        // create a copy of every attachment that is attached to the original note and also listed
        // in the repost attachment IDs
        try {
            for (Attachment attachment : originalAttachments) {
                if (newAttachmentIds.contains(attachment.getId())) {
                    if (noteStoringTO.isPublish()) {
                        Attachment copy;
                        copy = resourceStoringManagement.storeCopyOfAttachment(attachment);
                        finalAttachmentIds.add(copy.getId());
                    } else {
                        autosaveAttachmentIds.add(attachment.getId());
                    }
                    newAttachmentIds.remove(attachment.getId());
                }
            }
        } catch (AuthorizationException e) {
            // shouldn't occur because there is a current user
            throw new NoteStoringPreProcessorException(
                    "Unexpected exception while cloning attachments for repost", e);
        }
        if (!noteStoringTO.isPublish()) {
            // set null value to remove property from previous autosave
            StringPropertyTO attachmentProperty = getAutosaveAttachmentProperty(noteStoringTO);
            attachmentProperty.setPropertyValue(autosaveAttachmentIds.size() > 0 ? StringUtils
                    .join(autosaveAttachmentIds,
                            ORIGIN_ATTACHMENT_IDS_SEPARATOR) : null);
        }
        // add remaining/new attachments
        finalAttachmentIds.addAll(newAttachmentIds);
        noteStoringTO.setAttachmentIds(finalAttachmentIds.toArray(new Long[finalAttachmentIds
                .size()]));
    }

    /**
     * Get the property holding the autosave attachment IDs of the original note. If it does not
     * exist create and add it.
     * 
     * @param noteStoringTO
     *            the note storing TO
     * @return the property
     */
    // TODO only required because StringPropertyTO equals and hashCode methods are not implemented
    // and thus adding a property with same group and key would not replace the exisiting entry in
    // the set
    private StringPropertyTO getAutosaveAttachmentProperty(NoteStoringTO noteStoringTO) {
        StringPropertyTO attachmentProperty = PropertyHelper.getProperty(
                noteStoringTO.getProperties(), PropertyManagement.KEY_GROUP,
                KEY_ORIGIN_ATTACHMENT_IDS);
        if (attachmentProperty == null) {
            attachmentProperty = new StringPropertyTO();
            attachmentProperty.setKeyGroup(PropertyManagement.KEY_GROUP);
            attachmentProperty.setPropertyKey(KEY_ORIGIN_ATTACHMENT_IDS);
            noteStoringTO.getProperties().add(attachmentProperty);
        }
        return attachmentProperty;
    }

    /**
     * @return 0.
     */
    @Override
    public int getOrder() {
        return 0;
    }

    @Override
    public boolean isProcessAutosave() {
        // must copy the attachments of the original note even when doing an autosave
        return true;
    }

    /**
     * Invokes the processor <b>before</b> the note is stored.
     * 
     * @param noteStoringTO
     *            The note to work on.
     * @return The altered NoteStoringTO.
     * @throws NoteStoringPreProcessorException
     *             thrown to indicate that the pre-processing failed and the note cannot be created
     * @throws NoteManagementAuthorizationException
     *             thrown to indicate that the note cannot be created because of access restrictions
     */
    @Override
    public NoteStoringTO process(NoteStoringTO noteStoringTO)
            throws NoteStoringPreProcessorException, NoteManagementAuthorizationException {
        StringPropertyTO repostNoteIdProperty = PropertyHelper.getProperty(
                noteStoringTO.getProperties(), PropertyManagement.KEY_GROUP, KEY_ORIGIN_NOTE_ID);
        if (repostNoteIdProperty != null) {
            Long originalNoteId = null;
            try {
                originalNoteId = Long.parseLong(repostNoteIdProperty.getPropertyValue());
                Note originalNote = notePermissionManagement.hasAndGetWithPermission(
                        originalNoteId, NotePermissionManagement.PERMISSION_REPOST,
                        new IdentityConverter<Note>());
                cloneAttachments(noteStoringTO, originalNote.getAttachments());
                if (noteStoringTO.isPublish()) {
                    noteStoringTO.getProperties().add(
                            new StringPropertyTO(originalNote.getUser().getId().toString(),
                                    PropertyManagement.KEY_GROUP,
                                    KEY_ORIGIN_AUTHOR_ID, new Date()));
                    // remove the property of the autosave attachments by setting its value to null
                    StringPropertyTO attachmentProperty = getAutosaveAttachmentProperty(noteStoringTO);
                    attachmentProperty.setPropertyValue(null);
                }
            } catch (NumberFormatException e) {
                throw new NoteStoringPreProcessorException("Invalid repost note ID", e);
            } catch (AuthorizationException e) {
                throw new InvalidPermissionForRepostException(
                        SecurityHelper.getCurrentUserId(), originalNoteId);
            } catch (NotFoundException e) {
                throw new NoteStoringPreProcessorException("The requested note does not exist", e);
            }
        }
        return noteStoringTO;
    }
}
