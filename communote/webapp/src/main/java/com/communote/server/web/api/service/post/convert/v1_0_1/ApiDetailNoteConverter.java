package com.communote.server.web.api.service.post.convert.v1_0_1;

import java.util.ArrayList;

import com.communote.server.api.core.attachment.AttachmentData;
import com.communote.server.api.core.note.NoteData;
import com.communote.server.api.core.note.NoteRenderContext;
import com.communote.server.api.core.tag.TagData;
import com.communote.server.core.filter.listitems.SimpleNoteListItem;
import com.communote.server.core.tag.TagParser;
import com.communote.server.core.tag.TagParserFactory;
import com.communote.server.core.vo.query.QueryResultConverter;
import com.communote.server.core.vo.query.note.SimpleNoteListItemToNoteDataQueryResultConverter;
import com.communote.server.web.api.to.listitem.DiscussionListItem;
import com.communote.server.web.api.to.listitem.v1_0_1.DetailPostListItem;


/**
 * ApiDetailNoteConverter to convert the temporary object into a DetailPostListItem (v1_0_1)
 * 
 * Note: this converter is not thread-safe
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 * @param <T>
 *            type of the source objects
 * @param <O>
 *            type of the target objects
 * 
 * @deprecated Use new generated API.
 */
@Deprecated
public class ApiDetailNoteConverter<T extends SimpleNoteListItem, O extends DetailPostListItem>
        extends QueryResultConverter<T, O> {

    private static final TagParser DEFAULT_TAG_PARSER = TagParserFactory.instance()
            .getDefaultTagParser();

    private final SimpleNoteListItemToNoteDataQueryResultConverter<NoteData> noteListDataConverter;

    /**
     * Constructs a new converter
     * 
     * @param renderContext
     *            the render context to be used
     */
    public ApiDetailNoteConverter(NoteRenderContext renderContext) {
        noteListDataConverter = new SimpleNoteListItemToNoteDataQueryResultConverter<NoteData>(
                NoteData.class, renderContext);
    }

    @Override
    public boolean convert(T queryResult, O finalResult) {
        NoteData tempResult = noteListDataConverter.create();
        if (noteListDataConverter.convert(queryResult, tempResult)) {
            fillingResultItem(tempResult, finalResult);
            return true;
        }
        return false;
    }

    @Override
    public O create() {
        return (O) new DetailPostListItem();
    }

    /**
     * Filling the DetailPostListItem
     * 
     * @param note
     *            The note object
     * @param detailItem
     *            The DetailPostListitem
     */
    private void fillingResultItem(NoteData note, O detailItem) {

        detailItem.setBlogId(note.getBlog().getId());

        if (note.getParent() != null) {
            detailItem.setParentUserId(note.getParent().getUser().getId());
            detailItem.setParentPostId(note.getParent().getId());
        }

        // TODO setCanEdit - or better not, who knows what will happen with clients when they
        // encounter a true here
        // detailItem.setCanEdit(canEdit);
        detailItem.setNumberOfComments(note.getNumberOfComments());

        detailItem.setDiscussion(new DiscussionListItem());
        detailItem.getDiscussion().setDiscussionId(note.getDiscussionId());

        detailItem.setCreationDate(note.getCreationDate());
        detailItem.setLastModificationDate(note.getLastModificationDate());

        detailItem.setPostId(note.getId());

        detailItem.setText(note.getContent());
        detailItem.setUserId(note.getUser().getId());

        detailItem.setDirect(note.isDirect());
        detailItem.setFavorite(note.isFavorite());

        if (note.getTags() != null) {
            String[] tags = new String[note.getTags().size()];

            int i = 0;
            for (TagData tag : note.getTags()) {
                tags[i++] = tag.getName();
            }
            detailItem.setTags(DEFAULT_TAG_PARSER.buildTagString(tags));

        }
        if (note.getAttachments() == null) {
            detailItem.setAttachments(new ArrayList<AttachmentData>());
        } else {
            detailItem.setAttachments(note.getAttachments());
        }
    }

}