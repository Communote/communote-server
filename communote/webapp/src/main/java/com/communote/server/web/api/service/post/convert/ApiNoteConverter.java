package com.communote.server.web.api.service.post.convert;

import com.communote.server.api.core.note.NoteData;
import com.communote.server.api.core.note.NoteRenderContext;
import com.communote.server.core.filter.listitems.SimpleNoteListItem;
import com.communote.server.core.vo.query.QueryResultConverter;
import com.communote.server.core.vo.query.note.SimpleNoteListItemToNoteDataQueryResultConverter;
import com.communote.server.web.api.to.listitem.PostListItem;


/**
 * ApiNoteConverter to convert the temporary object into a PostListItem
 * 
 * Note: this converter is not thread-safe
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 * @param <T>
 *            The SimpleNoteListItem which is the incoming list
 * @param <O>
 *            The PostListItem which is the final list
 * @deprecated Use new generated REST-API instead
 */
@Deprecated
public class ApiNoteConverter<T extends SimpleNoteListItem, O extends PostListItem>
        extends QueryResultConverter<T, O> {

    private final SimpleNoteListItemToNoteDataQueryResultConverter<NoteData> noteListDataConverter;

    /**
     * Creates a new converter
     * 
     * @param renderContext
     *            the render context to use for filling the target
     */
    public ApiNoteConverter(NoteRenderContext renderContext) {
        // using a NoteData converter will be slower but is necessary to handle template
        // notes correctly
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
        return (O) new PostListItem();
    }

    /**
     * Fills the target list item
     * 
     * @param note
     *            the note data object
     * @param postItem
     *            the filled target
     */
    private void fillingResultItem(NoteData note, O postItem) {
        postItem.setPostId(note.getId());
        postItem.setText((note.getContent()));
        postItem.setUserId(note.getUser().getId());
        postItem.setCreationDate(note.getCreationDate());
        postItem.setLastModificationDate(note.getLastModificationDate());
        postItem.setBlogId(note.getBlog().getId());
    }

}