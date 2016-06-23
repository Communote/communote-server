package com.communote.plugins.api.rest.v24.resource.timelinenote;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.communote.common.util.PageableList;
import com.communote.plugins.api.rest.v24.exception.ExtensionNotSupportedException;
import com.communote.plugins.api.rest.v24.exception.ResponseBuildException;
import com.communote.plugins.api.rest.v24.resource.DefaultParameter;
import com.communote.plugins.api.rest.v24.resource.DefaultResourceHandler;
import com.communote.plugins.api.rest.v24.resource.ResourceHandlerHelper;
import com.communote.plugins.api.rest.v24.resource.note.NoteResourceHelper;
import com.communote.plugins.api.rest.v24.response.ResponseHelper;
import com.communote.plugins.api.rest.v24.to.ApiResult;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.note.NoteRenderContext;
import com.communote.server.api.core.security.AuthorizationException;
import com.communote.server.core.blog.NoteNotFoundException;
import com.communote.server.core.query.QueryManagement;
import com.communote.server.core.vo.blog.DiscussionNoteData;
import com.communote.server.core.vo.query.QueryDefinitionRepository;
import com.communote.server.core.vo.query.QueryParameters.OrderDirection;
import com.communote.server.core.vo.query.TimelineFilterViewType;
import com.communote.server.core.vo.query.config.NoteQueryParametersConfigurator;
import com.communote.server.core.vo.query.config.QueryParametersParameterNameProvider;
import com.communote.server.core.vo.query.note.SimpleNoteListItemToDiscussionNoteDataConverter;
import com.communote.server.core.vo.query.post.NoteQuery;
import com.communote.server.core.vo.query.post.NoteQueryParameters;
import com.communote.server.service.NoteService;

/**
 * Handler for requests the TimelineNoteResource
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class TimelineNoteResourceHandler
extends
DefaultResourceHandler<DefaultParameter, DefaultParameter, DefaultParameter,
GetTimelineNoteParameter, GetCollectionTimelineNoteParameter> {

    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(TimelineNoteResourceHandler.class);

    private static NoteQuery NOTE_QUERY = QueryDefinitionRepository.instance()
            .getQueryDefinition(NoteQuery.class);

    /**
     * Get the {@link NoteQueryParameters} from the {@link GetCollectionTimelineNoteParameter}
     *
     * @param parameters
     *            Parameter for filtering of notes
     * @param request
     *            The request.
     * @return {@link NoteQueryParameters}
     */
    private NoteQueryParameters configureQueryInstance(Map<String, ? extends Object> parameters,
            Request request) {
        NoteQueryParameters noteQueryParameters = NOTE_QUERY.createInstance();
        QueryParametersParameterNameProvider nameProvider = ResourceHandlerHelper
                .getNameProvider(request);
        NoteQueryParametersConfigurator queryInstanceConfigurator = new NoteQueryParametersConfigurator(
                nameProvider);
        queryInstanceConfigurator.configure(parameters, noteQueryParameters);
        noteQueryParameters.getTypeSpecificExtension().setIncludeChildTopics(false);
        return noteQueryParameters;
    }

    /**
     * Configure {@link GetCollectionTimelineNoteParameter} note parameter for showing direct
     * messages OR notes for me OR only followed items - it is an OR relationship
     *
     * @param getCollectionTimelineNoteParameter
     *            {@link GetCollectionTimelineNoteParameter}
     */
    private void configureShowFilter(
            GetCollectionTimelineNoteParameter getCollectionTimelineNoteParameter) {
        boolean showDirectMessages = BooleanUtils.toBoolean(getCollectionTimelineNoteParameter
                .getF_showDirectMessages());
        boolean showNotesForMe = BooleanUtils.toBoolean(getCollectionTimelineNoteParameter
                .getF_showNotesForMe());
        boolean showFollowedItems = BooleanUtils.toBoolean(getCollectionTimelineNoteParameter
                .getF_showFollowedItems());

        if ((showDirectMessages ? 1 : 0) + (showNotesForMe ? 1 : 0) + (showFollowedItems ? 1 : 0) > 1) {
            getCollectionTimelineNoteParameter.setOffset(0);
        }
        if (showDirectMessages) {
            getCollectionTimelineNoteParameter.setF_showNotesForMe(true);
            getCollectionTimelineNoteParameter.setF_showNotesForMe(false);
            getCollectionTimelineNoteParameter.setF_showFollowedItems(false);
        }
        if (showNotesForMe) {
            getCollectionTimelineNoteParameter.setF_showDirectMessages(false);
            getCollectionTimelineNoteParameter.setF_showNotesForMe(true);
            getCollectionTimelineNoteParameter.setF_showFollowedItems(false);
        }
        if (showFollowedItems) {
            getCollectionTimelineNoteParameter.setF_showDirectMessages(false);
            getCollectionTimelineNoteParameter.setF_showNotesForMe(false);
            getCollectionTimelineNoteParameter.setF_showFollowedItems(true);
        }
    }

    /**
     * Get the list of filtered {@link TimelineNoteResource}
     *
     * @param noteQueryParameters
     *            {@link NoteQueryParameters}
     * @param filterHtml
     *            filter text and short form of text to plain text of notes
     * @param request
     *            the current request
     * @return list of {@link TimelineNoteResource}
     */
    private PageableList<TimelineNoteResource> filterNotes(NoteQueryParameters noteQueryParameters,
            Boolean filterHtml, Request request) {
        NoteRenderContext renderContext = NoteResourceHelper.createNoteRenderContext(filterHtml,
                true, request);
        TimelineNoteResourceConverter converter = new TimelineNoteResourceConverter(
                noteQueryParameters, renderContext);
        PageableList<TimelineNoteResource> timelineNoteListItems;
        // optimization: when the threaded view is requested we just fetch the discussion from the
        // cache and bypass the query stuff
        if (noteQueryParameters.isThreadedView()) {
            SimpleNoteListItemToDiscussionNoteDataConverter discussionConverter =
                    new SimpleNoteListItemToDiscussionNoteDataConverter(renderContext,
                            TimelineFilterViewType.THREAD, null);
            try {
                DiscussionNoteData discussionData = ServiceLocator
                        .instance().getService(NoteService.class)
                        .getNoteWithComments(noteQueryParameters.getDiscussionId(),
                                discussionConverter);
                timelineNoteListItems = converter.convert(discussionData);
            } catch (NoteNotFoundException e) {
                // return empty results
                timelineNoteListItems = PageableList.emptyList();
            } catch (AuthorizationException e) {
                // return empty results
                timelineNoteListItems = PageableList.emptyList();
            }
        } else {
            timelineNoteListItems = ServiceLocator
                    .findService(QueryManagement.class)
                    .query(NOTE_QUERY, noteQueryParameters, converter);
        }
        return timelineNoteListItems;
    }

    /**
     * Generate map with all valid parameters
     *
     * @param getTimelineNoteParameter
     *            the parameters for a timeline note
     * @param request
     *            The request.
     * @return a map containing converted parameters
     */
    private Map<String, ? extends Object> generateParameterMap(
            GetTimelineNoteParameter getTimelineNoteParameter, Request request) {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(ResourceHandlerHelper.getNameProvider(request).getNamesForNoteId()[0],
                ObjectUtils.toString(getTimelineNoteParameter.getNoteId()));
        return parameters;
    }

    /**
     * Get a note with timeline specific attributes
     *
     * @param getTimelineNoteParameter
     *            {@link GetTimelineNoteParameter}
     * @param requestedMimeType
     *            is the mime type that indicates which data exchange format to use
     * @param uriInfo
     *            this object is created by the request and contains some request specific data
     * @param requestSessionId
     *            sessionId
     * @param request
     *            - javax request
     * @return {@link Response}
     * @throws ResponseBuildException
     *             exception while building the response
     * @throws ExtensionNotSupportedException
     *             extension is not supported
     */
    @Override
    public Response handleGetInternally(GetTimelineNoteParameter getTimelineNoteParameter,
            String requestedMimeType, UriInfo uriInfo, String requestSessionId, Request request)
                    throws ResponseBuildException, ExtensionNotSupportedException {
        NoteQueryParameters noteQueryParameters = configureQueryInstance(
                generateParameterMap(getTimelineNoteParameter, request), request);
        boolean filterHtml = getTimelineNoteParameter.getFilterHtml() == null ? false
                : getTimelineNoteParameter.getFilterHtml();
        List<TimelineNoteResource> resultTimelineNotes = filterNotes(noteQueryParameters,
                filterHtml, request);
        if (resultTimelineNotes.size() != 1) {
            LOGGER.debug("More Notes with identifier {} are available.",
                    getTimelineNoteParameter.getNoteId());
            return ResponseHelper.buildResponse(null, "Can not found note with identifier "
                    + getTimelineNoteParameter.getNoteId(), request, ApiResult.ResultStatus.ERROR,
                    Response.Status.NOT_FOUND);
        }
        return ResponseHelper.buildSuccessResponse(resultTimelineNotes.get(0), request);
    }

    /**
     * Get a list of note with timeline specific attributes
     *
     * @param parametersAsObject
     *            {@link GetCollectionTimelineNoteParameter}
     * @param requestedMimeType
     *            is the mime type that indicates which data exchange format to use
     * @param uriInfo
     *            this object is created by the request and contains some request specific data
     * @param requestSessionId
     *            sessionId
     * @param request
     *            - javax request
     * @return {@link Response}
     * @throws ResponseBuildException
     *             exception while building the response
     * @throws ExtensionNotSupportedException
     *             extension is not supported
     */
    @Override
    public Response handleListInternally(GetCollectionTimelineNoteParameter parametersAsObject,
            String requestedMimeType, UriInfo uriInfo, String requestSessionId, Request request)
                    throws ResponseBuildException, ExtensionNotSupportedException {
        configureShowFilter(parametersAsObject);

        Map<String, String> parameters = TimelineNoteHelper.toMap(uriInfo.getQueryParameters());
        NoteQueryParameters noteQueryParameters = configureQueryInstance(parameters, request);

        // if discussion is set notes must sort asc by date
        if (parametersAsObject.getF_discussionId() != null) {
            noteQueryParameters.setSortByDate(OrderDirection.ASCENDING);
        } else {
            noteQueryParameters.setSortByDate(OrderDirection.DESCENDING);
        }

        boolean filterHtml = parametersAsObject.getFilterHtml() == null ? false
                : parametersAsObject.getFilterHtml();
        PageableList<TimelineNoteResource> resultTimelineNotes;
        resultTimelineNotes = filterNotes(noteQueryParameters, filterHtml, request);

        Map<String, Object> metaData = ResourceHandlerHelper.generateMetaDataForPaging(
                parametersAsObject.getOffset(), parametersAsObject.getMaxCount(),
                resultTimelineNotes.getMinNumberOfElements());

        return ResponseHelper.buildSuccessResponse(resultTimelineNotes, request, metaData);
    }
}
