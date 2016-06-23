package com.communote.plugins.api.rest.v22.resource.timelinenote;

import java.util.ArrayList;

import com.communote.common.util.PageableList;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.note.NoteData;
import com.communote.server.api.core.note.NoteRenderContext;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.core.blog.NoteNotFoundException;
import com.communote.server.core.filter.listitems.SimpleNoteListItem;
import com.communote.server.core.vo.blog.DiscussionNoteData;
import com.communote.server.core.vo.query.QueryResultConverter;
import com.communote.server.core.vo.query.TimelineFilterViewType;
import com.communote.server.core.vo.query.note.SimpleNoteListItemToDiscussionNoteDataConverter;
import com.communote.server.core.vo.query.note.SimpleNoteListItemToNoteDataQueryResultConverter;
import com.communote.server.core.vo.query.post.NoteQueryParameters;
import com.communote.server.service.NoteService;

/**
 * Converter to convert the query result depending on settings of the query instance. When a
 * discussionId is set in the queryInstance the converter will return the notes of the discussion in
 * threaded view order. Otherwise the items of the result will just be converted to the target type.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
// TODO is the threadedView logic really necessary? Well it's useful but currently we aren't calling
// it, see TimelineNoteResourceHandler optimization.
public class TimelineNoteResourceConverter extends
        QueryResultConverter<SimpleNoteListItem, TimelineNoteResource> {

    private SimpleNoteListItemToNoteDataQueryResultConverter<NoteData> noteConverter;

    private final boolean threadedView;
    private final Long discussionId;
    private final NoteRenderContext renderContext;

    /**
     * Creates a new converter which will convert the queryResult depending on settings of the query
     * instance. When a discussionId is set in the queryInstance the converter will return the notes
     * of the discussion in threaded view order. Otherwise the items of the result will just be
     * converted to the target type.
     * 
     * @param noteQueryParameters
     *            The query instance.
     * @param renderContext
     *            the note render context to use
     */
    public TimelineNoteResourceConverter(NoteQueryParameters noteQueryParameters,
            NoteRenderContext renderContext) {
        this.threadedView = noteQueryParameters.isThreadedView();
        this.discussionId = noteQueryParameters.getDiscussionId();
        this.renderContext = renderContext;
    }

    /**
     * Helper method which flattens a discussion data object into a list of note resources. This
     * list will contain the root note of the discussion as the first item, the following elements
     * are the comments which are sorted the same way as within the discussion data object.
     * 
     * @param source
     *            the object to convert
     * @return the list of note resources
     */
    public PageableList<TimelineNoteResource> convert(DiscussionNoteData source) {
        ArrayList<TimelineNoteResource> convertedResources = new ArrayList<TimelineNoteResource>(
                source.getComments().size() + 1);
        // add root note of discussion
        convertedResources.add(TimelineNoteHelper.buildTimelineNoteResource(source, create()));
        // add all comments
        for (NoteData comment : source.getComments()) {
            convertedResources.add(TimelineNoteHelper.buildTimelineNoteResource(comment, create()));
        }
        PageableList<TimelineNoteResource> result = new PageableList<TimelineNoteResource>(
                convertedResources);
        result.setOffset(0);
        result.setMinNumberOfElements(convertedResources.size());
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PageableList<TimelineNoteResource> convert(PageableList<SimpleNoteListItem> queryResult) {
        PageableList<TimelineNoteResource> result;
        // override for threaded view case: use another converter which takes care of sorting. This
        // makes the assumption that the queryResult only contains items of the one discussion,
        // which is true with respect to the way threadedView is determined.
        if (threadedView && queryResult.size() > 0) {
            result = PageableList.emptyList();
            // directly use discussionId as note ID because converter only handles root notes and
            // it's faster
            NoteService noteManagement = ServiceLocator.instance().getService(
                    NoteService.class);
            SimpleNoteListItemToDiscussionNoteDataConverter converter;
            converter = new SimpleNoteListItemToDiscussionNoteDataConverter(renderContext,
                    TimelineFilterViewType.THREAD, null);
            DiscussionNoteData discussionData;
            try {
                discussionData = noteManagement.getNoteWithComments(discussionId, converter);
                result = convert(discussionData);
            } catch (NoteNotFoundException e) {
                // return empty result
            } catch (AuthorizationException e) {
                // return empty result
            }
        } else {
            result = super.convert(queryResult);
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean convert(SimpleNoteListItem source, TimelineNoteResource target) {
        NoteData result = new NoteData();
        if (getNoteConverter().convert(source, result)) {
            TimelineNoteHelper.buildTimelineNoteResource(result, target);
            return true;
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TimelineNoteResource create() {
        return new TimelineNoteResource();
    }

    /**
     * @return the lazily initialized note converter
     */
    private SimpleNoteListItemToNoteDataQueryResultConverter<NoteData> getNoteConverter() {
        if (this.noteConverter == null) {
            this.noteConverter = new SimpleNoteListItemToNoteDataQueryResultConverter<NoteData>(
                    NoteData.class, renderContext);
        }
        return this.noteConverter;
    }

}
