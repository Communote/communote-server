package com.communote.server.core.blog.notes.processors;

import java.util.HashMap;

import com.communote.common.converter.Converter;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.blog.BlogData;
import com.communote.server.api.core.common.NotFoundException;
import com.communote.server.api.core.note.NoteData;
import com.communote.server.api.core.note.NoteManagementAuthorizationException;
import com.communote.server.api.core.note.NotePermissionManagement;
import com.communote.server.api.core.note.NoteRenderContext;
import com.communote.server.api.core.note.NoteRenderMode;
import com.communote.server.api.core.note.processor.NoteMetadataRenderingPreProcessor;
import com.communote.server.api.core.note.processor.NoteRenderingPreProcessorException;
import com.communote.server.api.core.property.PropertyHelper;
import com.communote.server.api.core.property.PropertyManagement;
import com.communote.server.api.core.property.StringPropertyTO;
import com.communote.server.api.core.user.DetailedUserData;
import com.communote.server.core.converter.blog.BlogToBlogDataConverter;
import com.communote.server.core.user.UserManagement;
import com.communote.server.core.vo.query.converters.UserToUserDataQueryResultConverter;
import com.communote.server.model.note.Note;
import com.communote.server.model.user.UserStatus;

/**
 * PreProcessor which adds a property to the list item containing the details of a repost if the
 * note is a repost.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class RepostNoteRenderingPreProcessor implements NoteMetadataRenderingPreProcessor {

    /**
     * Helper to get the topic from a note as list item
     *
     * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
     *
     */
    private class NoteToBlogListItemConverter implements Converter<Note, BlogData> {
        BlogToBlogDataConverter<BlogData> blogConverter;

        public NoteToBlogListItemConverter() {
            this.blogConverter = new BlogToBlogDataConverter<BlogData>(BlogData.class,
                    false);
        }

        @Override
        public BlogData convert(Note source) {
            return blogConverter.convert(source.getBlog());
        }
    }

    public static final String PROPERTY_REPOST_ORIGINAL_NOTE_DATA = "repostOriginalNote";

    private UserManagement userManagement;
    private NotePermissionManagement notePermissionManagement;

    private final NoteToBlogListItemConverter repostTopicConverter;

    public RepostNoteRenderingPreProcessor() {
        repostTopicConverter = new NoteToBlogListItemConverter();
    }

    /**
     * Add properties denoting whether the topic exists and the user has access.
     *
     * @param originalNoteId
     *            the ID of the original note
     * @param repostOriginalNoteData
     *            the map to add the info to
     */
    private void addNoteDetails(Long originalNoteId, HashMap<String, Object> repostOriginalNoteData) {
        Boolean exists = Boolean.TRUE;
        BlogData topic = null;
        try {
            topic = getNotePermissionManagement().hasAndGetWithPermission(
                    originalNoteId, NotePermissionManagement.PERMISSION_READ,
                    this.repostTopicConverter);
        } catch (NoteManagementAuthorizationException e) {
        } catch (NotFoundException e) {
            exists = Boolean.FALSE;
        }
        if (topic != null) {
            repostOriginalNoteData.put("blog", topic);
        }
        repostOriginalNoteData.put("exists", exists);
    }

    /**
     * @return the lazily fetched NotePermissionManagement
     */
    private NotePermissionManagement getNotePermissionManagement() {
        if (notePermissionManagement == null) {
            notePermissionManagement = ServiceLocator.findService(NotePermissionManagement.class);
        }
        return notePermissionManagement;
    }

    @Override
    public int getOrder() {
        return NoteMetadataRenderingPreProcessor.DEFAULT_ORDER;
    }

    /**
     * @return the lazily fetched UserManagement
     */
    private UserManagement getUserManagement() {
        if (userManagement == null) {
            userManagement = ServiceLocator.findService(UserManagement.class);
        }
        return userManagement;
    }

    @Override
    public boolean process(NoteRenderContext context, NoteData item)
            throws NoteRenderingPreProcessorException {
        StringPropertyTO originalNoteIdProperty = PropertyHelper.getProperty(
                item.getObjectProperties(),
                PropertyManagement.KEY_GROUP, RepostNoteStoringPreProcessor.KEY_ORIGIN_NOTE_ID);
        if (originalNoteIdProperty != null) {
            Long originalNoteId = Long.valueOf(originalNoteIdProperty.getPropertyValue());
            HashMap<String, Object> repostOriginalNoteData = new HashMap<String, Object>();
            item.getProperties().put(PROPERTY_REPOST_ORIGINAL_NOTE_DATA, repostOriginalNoteData);
            repostOriginalNoteData.put("id", originalNoteId);
            StringPropertyTO originalAuthorId = PropertyHelper.getProperty(
                    item.getObjectProperties(), PropertyManagement.KEY_GROUP,
                    RepostNoteStoringPreProcessor.KEY_ORIGIN_AUTHOR_ID);
            Long authorId = Long.valueOf(originalAuthorId.getPropertyValue());
            DetailedUserData author = getUserManagement().getUserById(authorId,
                    new UserToUserDataQueryResultConverter());
            if (author != null && !author.getStatus().equals(UserStatus.DELETED)) {
                repostOriginalNoteData.put("user", author);
                addNoteDetails(originalNoteId, repostOriginalNoteData);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean supports(NoteRenderMode mode) {
        return NoteRenderMode.PORTAL.equals(mode);
    }

}
