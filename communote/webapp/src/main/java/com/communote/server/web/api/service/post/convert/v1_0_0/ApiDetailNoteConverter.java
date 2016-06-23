package com.communote.server.web.api.service.post.convert.v1_0_0;

import com.communote.server.api.core.note.NoteData;
import com.communote.server.api.core.note.NoteRenderContext;
import com.communote.server.api.core.tag.TagData;
import com.communote.server.core.filter.listitems.SimpleNoteListItem;
import com.communote.server.core.tag.TagParser;
import com.communote.server.core.tag.TagParserFactory;
import com.communote.server.core.vo.query.note.DataAccessNoteConverter;
import com.communote.server.core.vo.query.note.SimpleNoteListItemToNoteDataQueryResultConverter;
import com.communote.server.model.note.Note;
import com.communote.server.web.api.to.listitem.BlogListItem;
import com.communote.server.web.api.to.listitem.DetailPostListItem;

/**
 * ApiDetailNoteConverter to convert the temporary object into a DetailPostListItem (v1_0_0)
 * 
 * Note: this converter is not thread-safe
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 * @param <T>
 *            The SimpleNoteListItem which is the incoming list
 * @param <O>
 *            The DetailPostListItem which is the final list
 * 
 * @deprecated Use new generated api.
 */
@Deprecated
public class ApiDetailNoteConverter<T extends SimpleNoteListItem, O extends DetailPostListItem>
        extends DataAccessNoteConverter<T, O> {

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

        BlogListItem blogItem = new BlogListItem();
        blogItem.setId(note.getBlog().getId());
        blogItem.setTitle(note.getBlog().getTitle());
        detailItem.setBlog(blogItem);

        if (note.getParent() != null) {
            detailItem.setParentPostAuthor(note.getParent().getUser());
        }

        // TODO setCanEdit - or better not, who knows what will happen with clients when they
        // encounter a true here
        // detailItem.setCanEdit(canEdit);

        // this API version uses the number of comments differently: DMs and replies on replies are
        // ignored. There is no matching member in the noteListData so we have fetch it from the
        // note to
        // maintain compatibility
        Note noteEntity = getNote(note.getId());
        detailItem.setNumberOfComments(noteEntity.getChildren().size());

        detailItem.setCreationDate(note.getCreationDate());
        detailItem.setLastModificationDate(note.getLastModificationDate());

        if (note.getParent() != null) {
            detailItem.setParentPostId(note.getParent().getId());
        }
        detailItem.setPostId(note.getId());

        detailItem.setText(note.getContent());
        detailItem.setUser(note.getUser());

        if (note.getTags() != null) {
            String[] tags = new String[note.getTags().size()];

            int i = 0;
            for (TagData tag : note.getTags()) {
                tags[i++] = tag.getName();
            }
            detailItem.setTags(DEFAULT_TAG_PARSER.buildTagString(tags));

        }
    }

}