package com.communote.server.core.blog.notes.processors;

import com.communote.server.api.core.note.NoteData;
import com.communote.server.api.core.note.NoteRenderContext;
import com.communote.server.api.core.note.NoteRenderMode;
import com.communote.server.api.core.note.processor.NoteMetadataRenderingPreProcessor;
import com.communote.server.api.core.note.processor.NoteRenderingPreProcessorException;
import com.communote.server.api.core.property.PropertyManagement;
import com.communote.server.api.core.property.StringPropertyTO;

/**
 * PreProcessor that prepares the note metadata for using it in an editor to create a repost to that
 * note. This preprocessor will clear most fields of the note list item (like topic, properties and
 * discussion data) and will add the repost note property. Keeping of tags and attachments can be
 * configured by mode options (whose keys are the MODE_OPTION_xyz fields).
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class CreateRepostNoteMetadataRenderingPreProcessor implements
        NoteMetadataRenderingPreProcessor {

    /**
     * Option for the repost modes to disable the copying of the attachments. If unset or not set to
     * "true", the note list data item returned when one of repost render modes is set will contain
     * the attachments of the note to repost.
     */
    public static final String MODE_OPTION_DISABLE_COPY_ATTACHMENTS = "repostModeOptionDisableCopyAttachments";
    /**
     * Option for the repost modes to enable the copying of the tags. If unset or not set to "true",
     * the note list data item returned when one of repost render modes is set will not contain the
     * tags of the note to repost.
     */
    public static final String MODE_OPTION_ENABLE_COPY_TAGS = "repostModeOptionEnableCopyTags";

    /**
     * {@inheritDoc}
     *
     * @return 10, so it runs at the end
     */
    @Override
    public int getOrder() {
        return 10;
    }

    @Override
    public boolean process(NoteRenderContext context, NoteData item)
            throws NoteRenderingPreProcessorException {
        Long orinalNoteId = item.getId();
        String disableCopyAttachments = context.getModeOptions().get(
                MODE_OPTION_DISABLE_COPY_ATTACHMENTS);
        if (disableCopyAttachments != null && Boolean.parseBoolean(disableCopyAttachments)) {
            item.setAttachments(null);
        }
        item.setBlog(null);
        item.setCreationDate(null);
        item.setCreationSource(null);
        item.setDirect(false);
        item.setDiscussionDepth(0);
        item.setDiscussionId(null);
        item.setDiscussionPath(null);
        item.setFavorite(false);
        item.setForMe(false);
        item.setId(null);
        item.setLastDiscussionCreationDate(null);
        item.setLastModificationDate(null);
        item.setNotifiedUsers(null);
        item.setNumberOfComments(0);
        item.getObjectProperties().clear();
        item.getProperties().clear();
        item.setSystemNote(false);
        String enableCopyTags = context.getModeOptions().get(MODE_OPTION_ENABLE_COPY_TAGS);
        if (enableCopyTags == null || !Boolean.parseBoolean(enableCopyTags)) {
            item.setTags(null);
        }
        item.setUser(null);
        item.setVersion(null);
        // create and add repost property
        StringPropertyTO repostProperty = new StringPropertyTO();
        repostProperty.setKeyGroup(PropertyManagement.KEY_GROUP);
        repostProperty.setPropertyKey(RepostNoteStoringPreProcessor.KEY_ORIGIN_NOTE_ID);
        repostProperty.setPropertyValue(String.valueOf(orinalNoteId));
        item.addObjectProperty(repostProperty);
        return false;
    }

    @Override
    public boolean supports(NoteRenderMode mode) {
        return NoteRenderMode.REPOST.equals(mode) || NoteRenderMode.REPOST_PLAIN.equals(mode);
    }

}
