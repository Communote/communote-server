package com.communote.server.core.vo.query.note;

import java.util.HashMap;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.communote.common.util.PageableList;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.blog.BlogRightsManagement;
import com.communote.server.api.core.blog.UserBlogData;
import com.communote.server.api.core.note.NoteData;
import com.communote.server.api.core.note.NotePermissionManagement;
import com.communote.server.api.core.note.NoteRenderContext;
import com.communote.server.api.core.note.processor.NoteRenderingPreProcessorException;
import com.communote.server.api.core.note.processor.NoteRenderingPreProcessorManager;
import com.communote.server.api.core.property.PropertyManagement;
import com.communote.server.api.core.property.PropertyType;
import com.communote.server.api.core.tag.TagData;
import com.communote.server.core.blog.FavoriteManagement;
import com.communote.server.core.blog.NoteNotFoundException;
import com.communote.server.core.blog.TopicInformationManagement;
import com.communote.server.core.filter.listitems.SimpleNoteListItem;
import com.communote.server.core.security.SecurityHelper;
import com.communote.server.core.vo.query.converters.AttachmentToAttachmentDataQueryResultConverter;
import com.communote.server.core.vo.query.converters.BlogToBlogListItemQueryResultConverter;
import com.communote.server.core.vo.query.converters.UserToUserDataQueryResultConverter;
import com.communote.server.core.vo.query.converters.TagToTagDataQueryResultConverter;
import com.communote.server.model.attachment.Attachment;
import com.communote.server.model.blog.Blog;
import com.communote.server.model.note.Note;
import com.communote.server.model.note.NoteCreationSource;
import com.communote.server.model.note.NoteStatus;
import com.communote.server.model.tag.Tag;
import com.communote.server.model.user.User;
import com.communote.server.service.NoteService;

/**
 * This converter converts a note to a note list data object.
 *
 * <b>Note: this converter is not thread safe</b>
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 * @param <O>
 *            type of the target objects of the conversion
 */
public class SimpleNoteListItemToNoteDataQueryResultConverter<O extends NoteData> extends
DataAccessNoteConverter<SimpleNoteListItem, O> {

    /** Logger. */
    private final static Logger LOGGER = LoggerFactory
            .getLogger(SimpleNoteListItemToNoteDataQueryResultConverter.class);

    private final UserToUserDataQueryResultConverter userConverter =
            new UserToUserDataQueryResultConverter();
    private final AttachmentToAttachmentDataQueryResultConverter attachmentConverter;

    private final BlogToBlogListItemQueryResultConverter blogConverter =
            new BlogToBlogListItemQueryResultConverter();

    // some local caches to speed up rendering of re-occurring data, also helps to give a consistent
    // view on the notes
    private final HashMap<Long, Integer> discussionCountCache = new HashMap<Long, Integer>();

    private final HashMap<Long, UserBlogData> blogItemCache = new HashMap<Long, UserBlogData>();
    private final HashMap<Long, TagData> tagItemCache = new HashMap<Long, TagData>();
    // because of discussions it's pretty likely that two notes have the same parent
    private final HashMap<Long, O> parentCache = new HashMap<Long, O>();
    private final FavoriteManagement favoriteManagement;

    // holds the current user ID or null if the user is using the public access feature
    private final Long currentUserId;

    private final NoteRenderContext noteRenderContext;

    private final TagToTagDataQueryResultConverter tagToTagListItemQueryResultConverter;

    private final Class<O> clazz;

    private final NotePermissionManagement notePermissionService;

    private final BlogRightsManagement topicManagement;

    private final TopicInformationManagement topicInformationManagement;
    private final NoteRenderingPreProcessorManager renderPreprocessor;

    /**
     * Creates a new converter.
     *
     * @param clazz
     *            the class of the target list item
     * @param noteRenderContext
     *            The rendering context. If the render mode of the context is null, no
     *            NoteRenderingPreProcessors will be called.
     */
    public SimpleNoteListItemToNoteDataQueryResultConverter(Class<O> clazz,
            NoteRenderContext noteRenderContext) {
        this.clazz = clazz;
        this.favoriteManagement = ServiceLocator.findService(FavoriteManagement.class);
        this.notePermissionService = ServiceLocator.findService(NotePermissionManagement.class);
        this.noteRenderContext = noteRenderContext;
        this.renderPreprocessor = ServiceLocator
                .findService(NoteRenderingPreProcessorManager.class);
        topicManagement = ServiceLocator.findService(BlogRightsManagement.class);

        topicInformationManagement = ServiceLocator.findService(TopicInformationManagement.class);
        tagToTagListItemQueryResultConverter = new TagToTagDataQueryResultConverter(
                noteRenderContext.getLocale());
        if (SecurityHelper.isPublicUser() || SecurityHelper.isInternalSystem()) {
            currentUserId = null;
        } else {
            currentUserId = SecurityHelper.assertCurrentUserId();
        }
        this.attachmentConverter = new AttachmentToAttachmentDataQueryResultConverter(
                noteRenderContext.getLocale());
    }

    /**
     * Converts a note with parent and children
     *
     * @param source
     *            the note to convert
     * @param target
     *            the filled result
     * @param fillParent
     *            if true the parent the note will be added if there is one
     * @return false if the source object does not exist
     */
    protected boolean convert(Note source, O target, boolean fillParent) {
        if (source == null) {
            return false;
        }
        target.setId(source.getId());
        target.setCreationDate(source.getCreationDate());
        target.setLastModificationDate(source.getLastModificationDate());
        target.setDiscussionId(source.getDiscussionId());
        target.setUser(userConverter.convert(source.getUser()));
        UserBlogData blogListItem = convertBlog(source.getBlog());
        target.setBlog(blogListItem);
        target.setAttachments(attachmentConverter.convert(new PageableList<Attachment>(
                source.getAttachments())));
        target.setContent(source.getContent().getContent());
        target.setShortContent(source.getContent().getShortContent());
        target.setDirect(source.isDirect());
        target.setLastDiscussionCreationDate(source.getLastDiscussionNoteCreationDate());
        target.setCreationSource(source.getCreationSource());
        // only published notes can be favorites
        boolean published = source.getStatus().equals(NoteStatus.PUBLISHED);
        if (published) {
            target.setFavorite(favoriteManagement.isFavorite(source.getId()));
        } else {
            target.setFavorite(false);
        }
        target.setSystemNote(NoteCreationSource.SYSTEM.equals(source.getCreationSource()));
        convertTags(source.getTags(), target);
        convertNotifiedUsers(source, target);
        fillCommentCounts(target, source.getId(), source.getDiscussionId());
        target.setDiscussionDepth(getDiscussionDepth(source));
        target.setDiscussionPath(source.getDiscussionPath());
        target.setVersion(source.getVersion());
        if (fillProperties(source, target)) {
            target.setPermissions(notePermissionService.getPermissions(source));
            if (fillParent) {
                target.setParent(convertParent(source));
            }
            try {
                renderPreprocessor.process(noteRenderContext, target);
                return true;
            } catch (NoteRenderingPreProcessorException e) {
                LOGGER.debug("Skipping note {} because preprocessing failed", target.getId());
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean convert(SimpleNoteListItem sourceNote, O resultingData) {
        return convert(getNote(sourceNote.getId()), resultingData, true);
    }

    /**
     * Converts the blog into a list item
     *
     * @param blog
     *            the blog to convert
     * @return the resulting list item
     */
    private UserBlogData convertBlog(Blog blog) {
        UserBlogData target = blogItemCache.get(blog.getId());
        if (target == null) {
            target = blogConverter.convert(blog);
            blogItemCache.put(blog.getId(), target);
        }
        return target;
    }

    /**
     * Converts the user, which where notified with this post and checks if the current user is
     * addressed with this note.
     *
     * @param note
     *            The note.
     * @param target
     *            The resulting data.
     */
    private void convertNotifiedUsers(Note note, NoteData target) {
        if (note.getUsersToBeNotified() != null) {
            for (User user : note.getUsersToBeNotified()) {
                target.getNotifiedUsers().add(userConverter.convert(user));
                target.setForMe(target.isForMe()
                        || user.getId().equals(currentUserId));
            }
        }
        target.setMentionDiscussionAuthors(note.isMentionDiscussionAuthors());
        target.setMentionTopicAuthors(note.isMentionTopicAuthors());
        target.setMentionTopicManagers(note.isMentionTopicManagers());
        target.setMentionTopicReaders(note.isMentionTopicReaders());
        target.setForMe(target.isForMe() || isForMe(note));
    }

    /**
     * Converts the parent note of a note into a note list data object. The parent of the parent
     * will not be converted.
     *
     * @param note
     *            the note whose parent should be processed
     * @return the converted object or null if the note had no parent
     */
    private NoteData convertParent(Note note) {
        Note parent = note.getParent();
        if (parent == null) {
            return null;
        }
        O parentNoteListData = parentCache.get(parent.getId());
        if (parentNoteListData == null) {
            parentNoteListData = create();
            convert(parent, parentNoteListData, false);
            parentCache.put(parent.getId(), parentNoteListData);
        }
        return parentNoteListData;
    }

    /**
     * Convert the tags and add them to the target object
     *
     * @param tags
     *            the tags to convert
     * @param target
     *            the target object to add the converted tags to
     */
    private void convertTags(Set<Tag> tags, NoteData target) {
        if (tags != null) {
            for (Tag tag : tags) {
                TagData tagItem = tagItemCache.get(tag.getId());
                if (tagItem == null) {
                    tagItem = tagToTagListItemQueryResultConverter.convert(tag);
                    tagItemCache.put(tag.getId(), tagItem);
                }
                target.getTags().add(tagItem);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public O create() {
        try {
            return clazz.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Fill details about number of items in a discussion
     *
     * @param targetItem
     *            the target list item
     * @param noteId
     *            the ID of the note
     * @param discussionId
     *            the ID of the discussion
     */
    private void fillCommentCounts(NoteData targetItem, Long noteId, Long discussionId) {
        Integer discussionCount = discussionCountCache.get(discussionId);
        NoteService noteService = ServiceLocator.instance().getService(NoteService.class);
        try {
            if (discussionCount == null) {
                discussionCount = noteService.getNumberOfNotesInDiscussion(noteId);
                discussionCountCache.put(discussionId, discussionCount);
            }
            targetItem.setNumberOfComments(noteService.getNumberOfReplies(noteId));
            targetItem.setNumberOfDiscussionNotes(discussionCount);
        } catch (NoteNotFoundException e) {
            // might have been deleted, can be ignored because it will fallback to 0
            LOGGER.debug(e.getMessage());
        }
    }

    /**
     * Fill the properties of the target with the properties of the note to convert.
     *
     * @param source
     *            the note to convert
     * @param targetItem
     *            the target list item
     * @return true if the filling the properties succeeded, false otherwise
     */
    private boolean fillProperties(Note source, NoteData targetItem) {
        PropertyManagement propertyManagement = ServiceLocator.instance().getService(
                PropertyManagement.class);
        try {
            targetItem.setObjectProperties(propertyManagement.getAllObjectProperties(
                    PropertyType.NoteProperty, source.getId()));
            return true;
        } catch (Exception e) {
            LOGGER.warn(
                    "Unexpected exception processing note {}. Skipping this note. Exception: {}",
                    source.getId(), e.getMessage());
        }
        return false;
    }

    /**
     * @return the converter for attachment entities
     */
    protected AttachmentToAttachmentDataQueryResultConverter getAttachmentConverter() {
        return attachmentConverter;
    }

    /**
     * Returns the depth of the note in the discussion. Depth 0 means the note is the root note of
     * the discussion.
     *
     * @param note
     *            the note to process
     * @return the depth
     */
    private int getDiscussionDepth(Note note) {
        if (note.getParent() == null) {
            return 0;
        }
        String path = note.getDiscussionPath();
        // add 2 for slash before and after root note id
        int offset = note.getDiscussionId().toString().length() + 2;
        // ignore the last slash, if there is one
        int end = path.length() - 1;
        // count slashes
        int depth = 1;
        for (int i = offset; i < end; i++) {
            if (path.charAt(i) == '/') {
                depth++;
            }
        }
        return depth;
    }

    /**
     * @param note
     *            The note to check.
     * @return True, if the note is for me, when an @@-notation was used.
     */
    private boolean isForMe(Note note) {
        Long topicId = note.getBlog().getId();
        boolean isForMe = note.isMentionTopicReaders();
        if (currentUserId != null) {
            // managers
            isForMe = isForMe || note.isMentionTopicManagers() && topicManagement
                    .currentUserHasManagementAccess(topicId);
            // Discussion participant
            isForMe = isForMe || note.isMentionDiscussionAuthors()
                    && topicInformationManagement.isAuthor(
                            currentUserId, topicId, note.getDiscussionId());

            // Author within the topic
            isForMe = isForMe || note.isMentionTopicAuthors()
                    && topicInformationManagement.isAuthor(
                            currentUserId, topicId, null);
        }
        return isForMe;
    }
}
