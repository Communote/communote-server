package com.communote.server.core.vo.query.note;

import java.util.Collection;
import java.util.List;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.communote.common.string.StringHelper;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.attachment.AttachmentData;
import com.communote.server.api.core.blog.BlogData;
import com.communote.server.api.core.common.NotFoundException;
import com.communote.server.api.core.note.NoteRenderContext;
import com.communote.server.api.core.property.PropertyHelper;
import com.communote.server.api.core.property.PropertyManagement;
import com.communote.server.api.core.property.StringPropertyTO;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.core.blog.notes.processors.RepostNoteStoringPreProcessor;
import com.communote.server.core.filter.listitems.SimpleNoteListItem;
import com.communote.server.core.security.SecurityHelper;
import com.communote.server.core.storing.ResourceStoringManagement;
import com.communote.server.core.vo.blog.AutosaveNoteData;
import com.communote.server.model.blog.Blog;
import com.communote.server.model.note.Note;
import com.communote.server.model.note.NoteStatus;

/**
 * Converter that builds the data object of an autosave of a note. Source objects that are not
 * autosaves won't be converted.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class SimpleNoteListItemToAutosaveNoteDataConverter extends
        SimpleNoteListItemToNoteDataQueryResultConverter<AutosaveNoteData> {
    /** Logger. */
    private final static Logger LOGGER = LoggerFactory
            .getLogger(SimpleNoteListItemToAutosaveNoteDataConverter.class);

    /**
     * Creates a new converter.
     * 
     * @param clazz
     *            the class of the target list item
     * @param locale
     *            The locale to use.
     */
    public SimpleNoteListItemToAutosaveNoteDataConverter(Class<AutosaveNoteData> clazz,
            Locale locale) {
        // do not call preprocessors for unpublished notes
        super(clazz, new NoteRenderContext(null, locale));
    }

    /**
     * If the autosave is a repost, add the attachments of the original note.
     * 
     * @param target
     *            the autosave item
     */
    private void addRepostData(AutosaveNoteData target) {
        StringPropertyTO repostNoteIdProperty = PropertyHelper.getProperty(
                target.getObjectProperties(), PropertyManagement.KEY_GROUP,
                RepostNoteStoringPreProcessor.KEY_ORIGIN_NOTE_ID);
        if (repostNoteIdProperty != null) {
            try {
                Long repostNoteId = Long.parseLong(repostNoteIdProperty.getPropertyValue());
                // check for Attachment autosave property of repost, and add those attachments
                StringPropertyTO repostAttachmentIds = PropertyHelper.getProperty(
                        target.getObjectProperties(), PropertyManagement.KEY_GROUP,
                        RepostNoteStoringPreProcessor.KEY_ORIGIN_ATTACHMENT_IDS);
                if (repostAttachmentIds != null) {
                    List<Long> attachmentIdsToFilter = StringHelper.getStringAsLongList(
                            repostAttachmentIds.getPropertyValue(),
                            RepostNoteStoringPreProcessor.ORIGIN_ATTACHMENT_IDS_SEPARATOR);
                    Collection<AttachmentData> repostAttachments = ServiceLocator.findService(
                            ResourceStoringManagement.class)
                            .getAttachmentsOfNote(repostNoteId, attachmentIdsToFilter,
                                    getAttachmentConverter());
                    target.getAttachments().addAll(repostAttachments);
                }
            } catch (NumberFormatException e) {
                LOGGER.error("ID of note to repost is not valid", e);
            } catch (AuthorizationException e) {
                // silently ignore the authorization exception and just don't include the
                // attachments
                LOGGER.debug("Current user has no access to the note to repost", e);
            } catch (NotFoundException e) {
                // silently ignore that the note to repost does not exist anymore and just don't
                // include the attachments
                LOGGER.debug("Note to repost does not exist anymore", e);
            }
        }
    }

    @Override
    public boolean convert(SimpleNoteListItem source, AutosaveNoteData target) {
        Note note = getNote(source.getId());
        if (note == null || !note.getStatus().equals(NoteStatus.AUTOSAVED)
                || SecurityHelper.isPublicUser()) {
            return false;
        }
        super.convert(note, target, true);
        // autosave information
        if (note.getCrosspostBlogs() != null) {
            for (Blog blog : note.getCrosspostBlogs()) {
                BlogData blogItem = new BlogData(blog.getNameIdentifier(),
                        blog.getDescription(),
                        blog.getId(), blog.getTitle(), blog.getLastModificationDate());
                target.getCrosspostBlogs().add(blogItem);
            }
        }
        if (note.getOrigin() != null) {
            target.setOriginalNoteId(note.getOrigin().getId());
        }
        addRepostData(target);

        return true;
    }
}