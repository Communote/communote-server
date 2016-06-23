package com.communote.plugins.api.rest.v24.resource.timelineuser;

import java.util.Map;

import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.communote.common.util.PageableList;
import com.communote.plugins.api.rest.v24.converter.UserDataToTimelineUserConverter;
import com.communote.plugins.api.rest.v24.exception.ExtensionNotSupportedException;
import com.communote.plugins.api.rest.v24.exception.ResponseBuildException;
import com.communote.plugins.api.rest.v24.resource.DefaultParameter;
import com.communote.plugins.api.rest.v24.resource.DefaultResourceHandler;
import com.communote.plugins.api.rest.v24.resource.ResourceHandlerHelper;
import com.communote.plugins.api.rest.v24.resource.timelinenote.TimelineNoteHelper;
import com.communote.plugins.api.rest.v24.response.ResponseHelper;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.user.UserData;
import com.communote.server.core.query.QueryManagement;
import com.communote.server.core.vo.query.QueryDefinitionRepository;
import com.communote.server.core.vo.query.config.TimelineQueryParametersConfigurator;
import com.communote.server.core.vo.query.user.UserTaggingCoreQuery;
import com.communote.server.core.vo.query.user.UserTaggingCoreQueryParameters;

/**
 * ResourceHandler which returns the users for the given timeline.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class TimelineUserResourceHandler
        extends
        DefaultResourceHandler<DefaultParameter, DefaultParameter, DefaultParameter, DefaultParameter,
        GetCollectionTimelineUserParameter> {

    private final static UserTaggingCoreQuery USER_QUERY = QueryDefinitionRepository
            .instance().getQueryDefinition(UserTaggingCoreQuery.class);

    /**
     * Get the configured {@link UserTaggingCoreQueryParameters}
     * 
     * @param parameters
     *            Parameter for filtering of blogs
     * @param request
     *            The request.
     * @return {@link UserTaggingCoreQueryParameters}
     */
    private UserTaggingCoreQueryParameters configureQueryInstance(Map<String, ?> parameters,
            Request request) {
        UserTaggingCoreQueryParameters queryParameters = new UserTaggingCoreQueryParameters(
                USER_QUERY);
        new TimelineQueryParametersConfigurator<UserTaggingCoreQueryParameters>(
                ResourceHandlerHelper.getNameProvider(request)).configure(parameters,
                queryParameters);
        queryParameters.sortByLastNameAsc();
        queryParameters.getTypeSpecificExtension().setIncludeChildTopics(false);
        return queryParameters;
    }

    /**
     * {@inheritDoc}
     * 
     * @param request
     *            - javax request
     * @throws ResponseBuildException
     *             exception while building the response
     * @throws ExtensionNotSupportedException
     *             extension is not supported
     */
    @Override
    public Response handleListInternally(
            GetCollectionTimelineUserParameter getCollectionTimelineUserParameter,
            String requestedMimeType, UriInfo uriInfo, String requestSessionId, Request request)
            throws ResponseBuildException, ExtensionNotSupportedException {

        Map<String, String> parameters = TimelineNoteHelper.toMap(uriInfo.getQueryParameters());
        UserTaggingCoreQueryParameters queryParameters = configureQueryInstance(parameters, request);
        PageableList<TimelineUserResource> timelineUserList = ServiceLocator
                .instance()
                .getService(QueryManagement.class)
                .query(USER_QUERY,
                        queryParameters,
                        new UserDataToTimelineUserConverter<UserData, TimelineUserResource>());
        Map<String, Object> metaData = ResourceHandlerHelper.generateMetaDataForPaging(
                getCollectionTimelineUserParameter.getOffset(),
                getCollectionTimelineUserParameter.getMaxCount(),
                timelineUserList.getMinNumberOfElements());
        return ResponseHelper.buildSuccessResponse(timelineUserList, request, metaData);
    }
}
