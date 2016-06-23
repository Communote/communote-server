package com.communote.plugins.api.rest.v24.resource.timelinetopic;

import java.util.Map;

import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.communote.common.util.PageableList;
import com.communote.plugins.api.rest.v24.converter.BlogDataToTimelineTopicConverter;
import com.communote.plugins.api.rest.v24.exception.ExtensionNotSupportedException;
import com.communote.plugins.api.rest.v24.exception.ResponseBuildException;
import com.communote.plugins.api.rest.v24.resource.DefaultParameter;
import com.communote.plugins.api.rest.v24.resource.DefaultResourceHandler;
import com.communote.plugins.api.rest.v24.resource.ResourceHandlerHelper;
import com.communote.plugins.api.rest.v24.resource.timelinenote.TimelineNoteHelper;
import com.communote.plugins.api.rest.v24.response.ResponseHelper;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.blog.BlogData;
import com.communote.server.core.query.QueryManagement;
import com.communote.server.core.vo.query.QueryDefinitionRepository;
import com.communote.server.core.vo.query.blog.TopicTimelineParameters;
import com.communote.server.core.vo.query.blog.UserTaggedBlogQuery;
import com.communote.server.core.vo.query.config.TimelineQueryParametersConfigurator;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class TimelineTopicResourceHandler
        extends
        DefaultResourceHandler<DefaultParameter, DefaultParameter, DefaultParameter, DefaultParameter,
        GetCollectionTimelineTopicParameter> {

    private final static BlogDataToTimelineTopicConverter<BlogData, TimelineTopicResource> CONVERTER =
            new BlogDataToTimelineTopicConverter<BlogData, TimelineTopicResource>();

    private static UserTaggedBlogQuery TOPIC_QUERY = QueryDefinitionRepository
            .instance().getQueryDefinition(UserTaggedBlogQuery.class);

    /**
     * Get the configured {@link TopicTimelineParameters}
     * 
     * @param parameters
     *            Parameter for filtering of blogs
     * @param request
     *            The request.
     * @return {@link TopicTimelineParameters}
     */
    private TopicTimelineParameters configureQueryInstance(
            Map<String, ?> parameters, Request request) {
        TopicTimelineParameters queryParameters = new TopicTimelineParameters();
        TimelineQueryParametersConfigurator<TopicTimelineParameters> queryInstanceConfigurator =
                new TimelineQueryParametersConfigurator<TopicTimelineParameters>(
                        ResourceHandlerHelper.getNameProvider(request));
        queryInstanceConfigurator.configure(parameters, queryParameters);
        queryParameters.sortByBlogNameAsc();
        queryParameters.getTypeSpecificExtension().setIncludeChildTopics(false);
        return queryParameters;
    }

    /**
     * Get a list of topics with timeline specific attributes
     * 
     * @param getCollectionTimelineTopicParameter
     *            {@link GetCollectionTimelineTopicParameter}
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
    public Response handleListInternally(
            GetCollectionTimelineTopicParameter getCollectionTimelineTopicParameter,
            String requestedMimeType, UriInfo uriInfo, String requestSessionId, Request request)
            throws ResponseBuildException, ExtensionNotSupportedException {
        Map<String, String> parameters = TimelineNoteHelper.toMap(uriInfo.getQueryParameters());
        TopicTimelineParameters userTaggedBlogQueryInstance = configureQueryInstance(
                parameters, request);
        PageableList<TimelineTopicResource> blogListItems = ServiceLocator
                .instance().getService(QueryManagement.class)
                .query(TOPIC_QUERY, userTaggedBlogQueryInstance, CONVERTER);

        Map<String, Object> metaData = ResourceHandlerHelper.generateMetaDataForPaging(
                getCollectionTimelineTopicParameter.getOffset(),
                getCollectionTimelineTopicParameter.getMaxCount(),
                blogListItems.getMinNumberOfElements());

        return ResponseHelper.buildSuccessResponse(blogListItems, request, metaData);
    }
}
