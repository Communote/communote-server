package com.communote.plugins.api.rest.v22.resource.timelinetag;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.communote.common.util.DateHelper;
import com.communote.common.util.PageableList;
import com.communote.common.util.Pair;
import com.communote.plugins.api.rest.v22.exception.ExtensionNotSupportedException;
import com.communote.plugins.api.rest.v22.exception.ResponseBuildException;
import com.communote.plugins.api.rest.v22.resource.DefaultParameter;
import com.communote.plugins.api.rest.v22.resource.DefaultResourceHandler;
import com.communote.plugins.api.rest.v22.resource.ResourceHandlerHelper;
import com.communote.plugins.api.rest.v22.resource.timelinenote.TimelineNoteHelper;
import com.communote.plugins.api.rest.v22.response.ResponseHelper;
import com.communote.server.api.ServiceLocator;
import com.communote.server.core.filter.SortType;
import com.communote.server.core.filter.SortedResultSpecification;
import com.communote.server.core.filter.listitems.NormalizedRankListItem;
import com.communote.server.core.filter.listitems.RankTagListItem;
import com.communote.server.core.query.QueryManagement;
import com.communote.server.core.tag.TagManagement;
import com.communote.server.core.vo.query.QueryDefinitionRepository;
import com.communote.server.core.vo.query.config.TimelineQueryParametersConfigurator;
import com.communote.server.core.vo.query.converters.RankTagListItemToRankTagListItemQueryResultConverter;
import com.communote.server.core.vo.query.tag.AbstractTagQuery;
import com.communote.server.core.vo.query.tag.RankTagQuery;
import com.communote.server.core.vo.query.tag.RankTagQueryParameters;
import com.communote.server.core.vo.query.tag.RelatedRankTagQuery;
import com.communote.server.model.note.NoteStatus;

/**
 * Resources handler for the tag cloud.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class TimelineTagResourceHandler
extends
DefaultResourceHandler<DefaultParameter, DefaultParameter, DefaultParameter, DefaultParameter,
GetCollectionTimelineTagParameter> {

    /** The Constant RELATED_RANK_TAG_QUERY_DEFINITION. */
    private static final RelatedRankTagQuery RELATED_RANK_TAG_QUERY_DEFINITION = QueryDefinitionRepository
            .instance().getQueryDefinition(RelatedRankTagQuery.class);

    /** The Constant RANK_TAG_QUERY_DEFINITION. */
    private static final RankTagQuery RANK_TAG_QUERY_DEFINITION = QueryDefinitionRepository
            .instance().getQueryDefinition(RankTagQuery.class);

    /**
     * Configures the date boundaries.
     *
     * @param interval
     *            The interval.
     * @param tagQueryInstance
     *            The query instance.
     */
    private void configureDates(long interval, RankTagQueryParameters tagQueryInstance) {
        if (interval > 0) {
            Date lowerDate = tagQueryInstance.getLowerTagDate();
            Date upperDate = tagQueryInstance.getUpperTagDate();
            if (upperDate == null) {
                // lower tag date is set
                Date startIntervallDate = new Date(new Date().getTime() - interval);
                if (lowerDate == null || lowerDate.compareTo(startIntervallDate) < 0) {
                    tagQueryInstance.setLowerTagDate(startIntervallDate);
                }
            } else {
                Date startIntervallDate = new Date(upperDate.getTime() - interval);
                if (lowerDate == null || lowerDate.compareTo(startIntervallDate) < 0) {
                    tagQueryInstance.setLowerTagDate(startIntervallDate);
                }
            }
        }
    }

    /**
     * @param parameters
     *            The input parameters.
     * @param interval
     *            The interval.
     * @param uriInfo
     *            The uri info
     * @param request
     *            The request.
     * @return the configured tagging instance
     */
    private Pair<AbstractTagQuery<RankTagListItem>, RankTagQueryParameters> configureQueryInstance(
            GetCollectionTimelineTagParameter parameters, long interval, UriInfo uriInfo,
            Request request) {
        Long[] tagFilter = parameters.getF_tagIds();
        AbstractTagQuery<RankTagListItem> query;
        if (tagFilter != null && tagFilter.length != 0) {
            query = RELATED_RANK_TAG_QUERY_DEFINITION;
        } else {
            query = RANK_TAG_QUERY_DEFINITION;
        }
        RankTagQueryParameters queryParameters = new RankTagQueryParameters(query);
        TimelineQueryParametersConfigurator<RankTagQueryParameters> queryInstanceConfigurator =
                new TimelineQueryParametersConfigurator<RankTagQueryParameters>(
                        ResourceHandlerHelper.getNameProvider(request));
        queryInstanceConfigurator.configure(TimelineNoteHelper.toMap(uriInfo.getQueryParameters()),
                queryParameters);

        SortedResultSpecification resultSpecification = new SortedResultSpecification(
                parameters.getOffset() == null ? 0 : parameters.getOffset(),
                        parameters.getMaxCount() == null ? 0 : parameters.getMaxCount());
        resultSpecification.setSortType(SortType.COUNT);
        queryParameters.setResultSpecification(resultSpecification);
        queryParameters.sortByTagCountDesc();
        queryParameters.setHideSelectedTags(parameters.getHideSelectedTags() == null ? false
                : parameters.getHideSelectedTags());
        queryParameters.setExcludeNoteStatus(new NoteStatus[] { NoteStatus.AUTOSAVED });
        configureDates(interval, queryParameters);
        queryParameters.getTypeSpecificExtension().setIncludeChildTopics(false);
        return new Pair<AbstractTagQuery<RankTagListItem>, RankTagQueryParameters>(query,
                queryParameters);
    }

    /**
     * {@inheritDoc}
     *
     * @param request
     *            - javax request
     * @return The tag cloud for the given filtering.
     * @throws ResponseBuildException
     *             exception while building the response
     * @throws ExtensionNotSupportedException
     *             extension is not supported
     */
    @Override
    public Response handleListInternally(GetCollectionTimelineTagParameter parameters,
            String requestedMimeType, UriInfo uriInfo, String requestSessionId, Request request)
                    throws ResponseBuildException, ExtensionNotSupportedException {
        String tagCloudMode = parameters.getTagCloudMode();
        long interval = (parameters.getNumberOfLastDays() == null ? -1 : parameters
                .getNumberOfLastDays()) * DateHelper.DAYS;
        if ("MyTags".equals(tagCloudMode)) {
            interval = -1;
        }
        Pair<AbstractTagQuery<RankTagListItem>, RankTagQueryParameters> pair = configureQueryInstance(
                parameters, interval, uriInfo, request);

        PageableList<NormalizedRankListItem<RankTagListItem>> normalizedRankTagListItems = ServiceLocator
                .findService(QueryManagement.class)
                .query(pair.getLeft(), pair.getRight(),
                        new RankTagListItemToRankTagListItemQueryResultConverter(
                                ResourceHandlerHelper.getCurrentUserLocale(request),
                                ServiceLocator.instance().getService(TagManagement.class)));

        Map<String, Object> metaData = ResourceHandlerHelper.generateMetaDataForPaging(
                parameters.getOffset(), parameters.getMaxCount(),
                normalizedRankTagListItems.getMinNumberOfElements());

        List<TimelineTagResource> timelineTagResources = new ArrayList<TimelineTagResource>();
        for (NormalizedRankListItem<RankTagListItem> normalizedRankListItem : normalizedRankTagListItems) {
            RankTagListItem rankTagListItem = normalizedRankListItem.getItem();
            TimelineTagResource timelineTagResource = new TimelineTagResource();
            timelineTagResource.setTagId(rankTagListItem.getId());
            timelineTagResource.setName(rankTagListItem.getName());
            timelineTagResource.setRank(rankTagListItem.getRank().intValue());
            timelineTagResources.add(timelineTagResource);
        }

        return ResponseHelper.buildSuccessResponse(timelineTagResources, request, metaData);
    }
}
