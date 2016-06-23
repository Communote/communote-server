package com.communote.server.core.vo.query.note;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.communote.common.converter.Converter;
import com.communote.common.converter.IdentityConverter;
import com.communote.common.matcher.Matcher;
import com.communote.common.util.PageableList;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.note.NoteData;
import com.communote.server.api.core.note.NoteRenderContext;
import com.communote.server.core.blog.NoteNotFoundException;
import com.communote.server.core.filter.listitems.SimpleNoteListItem;
import com.communote.server.core.query.QueryManagement;
import com.communote.server.core.vo.blog.DiscussionNoteData;
import com.communote.server.core.vo.query.DiscussionFilterMode;
import com.communote.server.core.vo.query.TimelineFilterViewType;
import com.communote.server.core.vo.query.post.NoteQuery;
import com.communote.server.core.vo.query.post.NoteQueryParameters;
import com.communote.server.model.note.Note;
import com.communote.server.service.NoteService;

/**
 * Converter which creates DiscussionNoteData objects. Depending on the given
 * {@link TimelineFilterViewType} the converter will add the comments of the note to the data
 * object. In that case it will ignore any note in the input list that is not a root note.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a> *
 */
public class SimpleNoteListItemToDiscussionNoteDataConverter extends
        SimpleNoteListItemToNoteDataQueryResultConverter<DiscussionNoteData> {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(SimpleNoteListItemToDiscussionNoteDataConverter.class);

    private final NoteService noteService;
    private final TimelineFilterViewType timelineFilterViewType;

    private final DataAccessNoteConverter<SimpleNoteListItem, NoteData> simpleConverter;

    private Matcher<NoteData> commentMatcher;

    private boolean filter;

    private final NoteQueryParameters queryParameters;

    private final NoteRenderContext noteRenderContext;

    /**
     * Creates a new converter which converts root notes of discussions into a data object that also
     * holds all the comments of the discussion sorted by a given order. The comments will be empty
     * if the provided view type is {@link TimelineFilterViewType#CLASSIC}.
     * 
     * @param noteRenderContext
     *            The rendering context. If the render mode of the context is null, no
     *            NoteRenderingPreProcessors will be called.
     * @param timelineFilterViewType
     *            The timeline filter type which defines how the comments of a note should be sorted
     *            (threaded or chronologically by creation date starting with oldest). In case the
     *            type is {@link TimelineFilterViewType#CLASSIC} the comments won't be filled.
     * @param queryParameters
     *            The parameters are needed to load missing elements for
     *            {@link TimelineFilterViewType#COMMENT}. It can be null, if not COMMENT is used.
     */
    public SimpleNoteListItemToDiscussionNoteDataConverter(
            NoteRenderContext noteRenderContext, TimelineFilterViewType timelineFilterViewType,
            NoteQueryParameters queryParameters) {
        super(DiscussionNoteData.class, noteRenderContext);
        this.queryParameters = queryParameters;
        this.noteRenderContext = noteRenderContext;
        noteService = ServiceLocator.instance().getService(NoteService.class);
        simpleConverter = new SimpleNoteListItemToNoteDataQueryResultConverter<NoteData>(
                NoteData.class, noteRenderContext);
        if (timelineFilterViewType == null) {
            timelineFilterViewType = TimelineFilterViewType.CLASSIC;
        }
        this.timelineFilterViewType = timelineFilterViewType;
    }

    /**
     * This message adds additional discussions for the current user, which are contained through
     * direct messages.
     * 
     * @param queryResult
     *            The result to add
     */
    private void addDiscussionsFromDirectMessages(PageableList<SimpleNoteListItem> queryResult) {
        NoteQueryParameters newParameters = NoteQueryParameters.clone(queryParameters);
        newParameters.setTimelineFilterViewType(TimelineFilterViewType.CLASSIC);
        newParameters.setDiscussionFilterMode(DiscussionFilterMode.ALL);
        newParameters.setDirectMessage(true);
        newParameters.setQueryForAdditionalDMs(true);
        if (queryResult.size() > 0) {
            Note note;
            // Filter only for upper date, if we are not at the top of the stream.
            if (queryParameters.getRetrieveOnlyNotesBeforeDate() != null) {
                newParameters.setUpperTagDate(queryParameters.getRetrieveOnlyNotesBeforeDate());
            }
            note = noteService.getNote(queryResult.get(queryResult.size() - 1).getId(),
                    new IdentityConverter<Note>());
            newParameters.setLowerTagDate(note.isDirect() ? note.getCreationDate()
                    : note.getLastDiscussionNoteCreationDate());
        }
        PageableList<SimpleNoteListItem> directMessages = ServiceLocator.instance()
                .getService(QueryManagement.class).query(new NoteQuery(), newParameters);
        Set<Long> containingDiscussions = new HashSet<Long>();
        if (directMessages.isEmpty()) {
            return;
        }
        for (SimpleNoteListItem item : queryResult) {
            containingDiscussions.add(item.getId());
        }
        for (SimpleNoteListItem item : directMessages) {
            try {
                Long discussionId = noteService.getDiscussionId(item.getId());
                if (!containingDiscussions.contains(discussionId)) {
                    queryResult.add(noteService.getNote(discussionId,
                            new Converter<Note, SimpleNoteListItem>() {
                                @Override
                                public SimpleNoteListItem convert(Note source) {
                                    return new SimpleNoteListItem(source.getId(), source
                                            .getCreationDate());
                                }
                            }));
                    containingDiscussions.add(discussionId);
                }
            } catch (NoteNotFoundException e) {
                LOGGER.debug("The note {} might be removed.", item.getId());
            }
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * In addition to simple loading, this also checks for DM's which might alter the result. And
     * performs additional queries for this.
     * </p>
     */
    @Override
    public PageableList<DiscussionNoteData> convert(PageableList<SimpleNoteListItem> queryResult) {
        if (TimelineFilterViewType.COMMENT.equals(timelineFilterViewType)) {
            int neededResultingSize = queryResult.size();
            removeSuperfluousDiscussions(queryResult);
            addDiscussionsFromDirectMessages(queryResult);
            fillListWithMissingDiscussions(neededResultingSize, queryResult);
            queryResult = sortAndCleanup(queryResult, neededResultingSize);
            // End of stream
            if (neededResultingSize < queryParameters.getResultSpecification()
                    .getNumberOfElements()) {
                queryResult.setMinNumberOfElements(queryResult.size());
            }
        }
        PageableList<DiscussionNoteData> result = super.convert(queryResult);
        return result;
    }

    @Override
    public boolean convert(SimpleNoteListItem source, DiscussionNoteData target) {
        // ignore notes that are not root notes of a discussion
        if (!super.convert(source, target)) {
            return false;
        }
        if (!TimelineFilterViewType.CLASSIC.equals(timelineFilterViewType)) {
            if (target.getDiscussionDepth() != 0) {
                return false;
            }
            fillComments(target);
        }
        // Update last discussion creation date for direct messages.
        if (TimelineFilterViewType.COMMENT.equals(timelineFilterViewType)) {
            for (SimpleNoteListItem comment : getComments(target.getDiscussionId())) {
                if (comment.getCreationDate().getTime() > target.getLastDiscussionCreationDate()
                        .getTime()) {
                    target.setLastDiscussionCreationDate(comment.getCreationDate());
                }
            }
        }
        return true;
    }

    /**
     * Converts the comments and takes care of the threaded view sorting
     * 
     * @param discussionId
     *            the ID of the discussion
     * @param comments
     *            the comments which must be sorted chronologically (oldest first)
     * @param convertedComments
     *            the list in which the converted and sorted results will be stored
     * @return the converted and sorted result
     */
    private List<NoteData> convertAndSortThreaded(Long discussionId,
            List<SimpleNoteListItem> comments, List<NoteData> convertedComments) {
        Map<Long, List<NoteData>> parentIdToChildren = new HashMap<Long, List<NoteData>>();
        for (SimpleNoteListItem comment : comments) {
            NoteData commentNoteListData = new NoteData();
            if (simpleConverter.convert(comment, commentNoteListData)) {
                // store mapping from parent note to its children, threaded view can be created
                // easily by collapsing the structure (because comments are sorted chronologically)
                List<NoteData> childNotes = parentIdToChildren.get(commentNoteListData
                        .getParent().getId());
                if (childNotes == null) {
                    childNotes = new ArrayList<NoteData>();
                    parentIdToChildren.put(commentNoteListData.getParent().getId(), childNotes);
                }
                childNotes.add(commentNoteListData);
            }
        }
        // collapse the threaded view into a list, start with root note
        processChildren(discussionId, parentIdToChildren, convertedComments);
        return convertedComments;
    }

    /**
     * Fill the comments of the list item according to the timelineFilterViewType.
     * 
     * @param target
     *            the item to enrich with the comments
     */
    private void fillComments(DiscussionNoteData target) {
        // comments from cache
        List<SimpleNoteListItem> comments = getComments(target.getId());
        if (comments != null) {
            List<NoteData> convertedComments = target.getComments();
            if (TimelineFilterViewType.THREAD.equals(timelineFilterViewType)) {
                convertAndSortThreaded(target.getDiscussionId(), comments, convertedComments);
            } else if (TimelineFilterViewType.COMMENT.equals(timelineFilterViewType)) {
                for (SimpleNoteListItem comment : comments) {
                    NoteData commentNoteListData = new NoteData();
                    // skip non existing notes
                    if (simpleConverter.convert(comment, commentNoteListData)) {
                        convertedComments.add(commentNoteListData);
                    }
                }
                if (commentMatcher != null) {
                    if (filter) {
                        convertedComments = new ArrayList<NoteData>(
                                commentMatcher.filter(convertedComments));
                        target.setComments(convertedComments);
                    } else {
                        commentMatcher.markMatching(convertedComments);
                    }
                }
            }

        }
    }

    /**
     * This method tries to fill the list with missing discussions, as originally contained
     * discussions might be removed through direct messages.
     * 
     * @param neededResultingSize
     *            The size originally returned.
     * @param queryResult
     *            The list of results.
     */
    private void fillListWithMissingDiscussions(int neededResultingSize,
            PageableList<SimpleNoteListItem> queryResult) {
        if (queryResult.size() >= neededResultingSize) {
            return; // Already full.
        }
        NoteQueryParameters parameters = NoteQueryParameters.clone(queryParameters);
        parameters.setDiscussionFilterMode(DiscussionFilterMode.IS_ROOT);
        parameters.getResultSpecification().setNumberOfElements(
                neededResultingSize - queryResult.size());
        parameters.getResultSpecification().setOffset(
                parameters.getResultSpecification().getOffset() + neededResultingSize);

        PageableList<DiscussionNoteData> missingDiscussions = ServiceLocator.instance()
                .getService(QueryManagement.class).query(new NoteQuery(), queryParameters,
                        new SimpleNoteListItemToDiscussionNoteDataConverter(noteRenderContext,
                                timelineFilterViewType, parameters));
        if (missingDiscussions.size() == 0) {
            return;
        }
        Set<Long> containingDiscussions = new HashSet<Long>();
        for (SimpleNoteListItem item : queryResult) {
            containingDiscussions.add(item.getId());
        }
        for (DiscussionNoteData missingDiscussion : missingDiscussions) {
            if (!containingDiscussions.contains(missingDiscussion.getDiscussionId())) {
                queryResult.add(new SimpleNoteListItem(missingDiscussion.getId(), missingDiscussion
                        .getCreationDate()));
                containingDiscussions.add(missingDiscussion.getDiscussionId());
            }
        }
    }

    /**
     * @param noteId
     *            an ID of a note, must not be the discussion root
     * @return the comments in the discussion the current user is allowed to read, sorted
     *         chronologically by creation date (oldest first), or null if the note does not exist
     */
    private List<SimpleNoteListItem> getComments(Long noteId) {
        try {
            List<SimpleNoteListItem> comments = noteService.getCommentsOfDiscussion(noteId);
            return comments;
        } catch (NoteNotFoundException e) {
            LOGGER.debug("Ignoring comments for note {} "
                    + " because the note does not seem to exist anymore", noteId);

        }
        return null;
    }

    /**
     * Adds notes sorted into the result list by calling itself recursively.
     * 
     * @param parentId
     *            the note ID of parent note within the discussion
     * @param parentIdToChildren
     *            mapping from note ID to child notes (direct answers). This map will be used to get
     *            the answers to the note identified by parentId. The child notes are expected to be
     *            sorted chronologically.
     * @param result
     *            the result list to be filled
     */
    private void processChildren(Long parentId, Map<Long, List<NoteData>> parentIdToChildren,
            List<NoteData> result) {
        List<NoteData> children = parentIdToChildren.get(parentId);
        if (children != null) {
            for (NoteData child : children) {
                result.add(child);
                processChildren(child.getId(), parentIdToChildren, result);
            }
        }
    }

    /**
     * Method to remove all discussions from the given result set, which are not within the needed
     * scope, because they have direct messages, which are too young.
     * 
     * @param queryResult
     *            The list of results to filter.
     */
    private void removeSuperfluousDiscussions(PageableList<SimpleNoteListItem> queryResult) {
        if (queryResult.size() == 0 || queryParameters.getRetrieveOnlyNotesBeforeDate() == null) {
            return;
        }
        Note firstNote = noteService.getNote(queryResult.get(0).getId(),
                new IdentityConverter<Note>());
        long lastDiscussionCreationDate = firstNote.getLastDiscussionNoteCreationDate().getTime();
        Iterator<SimpleNoteListItem> iterator = queryResult.iterator();
        outer: while (iterator.hasNext()) {
            List<SimpleNoteListItem> comments = getComments(iterator.next().getId());
            if (comments != null && comments.size() > 0) {
                long creationTime = comments.get(comments.size() - 1).getCreationDate().getTime();
                if (creationTime > lastDiscussionCreationDate) {
                    iterator.remove();
                    continue outer;
                }
            }
        }
    }

    /**
     * Sets a matcher to mark comments, if loadComments is set to true.
     * 
     * @param commentMatcher
     *            The matcher to set.
     * @param filter
     *            If <code>true</code> only matching comments will be returned, else all comments
     *            will be returned and matching comments will be marked as matched.
     */
    public void setCommentMatcher(Matcher<NoteData> commentMatcher, boolean filter) {
        this.commentMatcher = commentMatcher;
        this.filter = filter;
    }

    /**
     * A method to resort the result and remove discussions which adds more elements to the result
     * list as requested.
     * 
     * @param queryResult
     *            The result list.
     * @param neededResultingSize
     *            Size of needed elements.
     * @return the sorted and cleaned result list.
     */
    private PageableList<SimpleNoteListItem> sortAndCleanup(
            PageableList<SimpleNoteListItem> queryResult, int neededResultingSize) {
        if (!queryParameters.isSortByDayDateAndRank() || !queryParameters.isRankFilterActive()) {
            Collections.sort(queryResult, new LastDiscussionCreationDateComparator(noteService));
        }
        if (neededResultingSize < queryResult.size()) {
            PageableList<SimpleNoteListItem> interimResult = new PageableList<SimpleNoteListItem>(
                    queryResult.subList(0, neededResultingSize));
            interimResult
                    .setMinNumberOfElements(queryResult.getMinNumberOfElements());
            interimResult.setOffset(queryResult.getOffset());
            queryResult = interimResult;
        }
        return queryResult;
    }

}
