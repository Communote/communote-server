package com.communote.plugins.api.rest.v30.resource.user.observation;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.communote.plugins.api.rest.v30.resource.DefaultParameter;
import com.communote.plugins.api.rest.v30.resource.DefaultResourceHandler;
import com.communote.plugins.api.rest.v30.response.ResponseHelper;
import com.communote.server.core.filter.listitems.CountListItem;
import com.communote.server.core.query.QueryManagement;
import com.communote.server.core.security.SecurityHelper;
import com.communote.server.core.vo.query.QueryParameters;
import com.communote.server.core.vo.query.TaggingCoreItemUTPExtension;
import com.communote.server.core.vo.query.blog.TopicAccessLevel;
import com.communote.server.core.vo.query.config.FilterWidgetParameterNameProvider;
import com.communote.server.core.vo.query.config.NoteQueryParametersConfigurator;
import com.communote.server.core.vo.query.config.TimelineQueryParametersConfigurator;
import com.communote.server.core.vo.query.post.CountNoteQuery;
import com.communote.server.core.vo.query.post.NoteQueryParameters;
import com.communote.server.core.vo.user.NavigationItemDataTO;
import com.communote.server.core.vo.user.NavigationItemTO;
import com.communote.server.service.NavigationItemService;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ObservationResourceHandler
        extends
        DefaultResourceHandler<DefaultParameter, DefaultParameter, DefaultParameter, DefaultParameter, GetCollectionObservationParameter> {

    private final NavigationItemService navigationItemService;
    private final QueryManagement queryManagement;

    private final TimelineQueryParametersConfigurator<NoteQueryParameters> queryInstanceConfigurator =
            new NoteQueryParametersConfigurator(FilterWidgetParameterNameProvider.INSTANCE);

    private final CountNoteQuery countNoteQuery = new CountNoteQuery();

    /**
     * Constructor.
     * 
     * @param navigationItemService
     *            Service to use.
     */
    public ObservationResourceHandler(NavigationItemService navigationItemService,
            QueryManagement queryManagement) {
        this.navigationItemService = navigationItemService;
        this.queryManagement = queryManagement;
    }

    /**
     * @{inheritDoc
     */
    @Override
    protected Response handleListInternally(GetCollectionObservationParameter listParameter,
            String requestedMimeType, UriInfo uriInfo, String requestSessionId, Request request)
            throws Exception {
        List<ObservationResource> result = new ArrayList<ObservationResource>();

        List<NavigationItemTO<NavigationItemDataTO>> navigationItemTOs = listParameter
                .getF_observations() == null ? navigationItemService
                .find() : navigationItemService.find(listParameter.getF_observations());
        Long currentuserId = SecurityHelper.getCurrentUserId();
        for (final NavigationItemTO<NavigationItemDataTO> navigationItemTO : navigationItemTOs) {
            NoteQueryParameters noteQueryParameters = countNoteQuery.createInstance();
            queryInstanceConfigurator.configure(navigationItemTO.getData().getFilters(),
                    noteQueryParameters);
            // ignore notes of current user
            noteQueryParameters.setUserIdsToIgnore(currentuserId);
            TaggingCoreItemUTPExtension extension = noteQueryParameters.getTypeSpecificExtension();
            extension.setTopicAccessLevel(TopicAccessLevel.READ);
            noteQueryParameters.setSortByDate(QueryParameters.OrderDirection.DESCENDING);
            noteQueryParameters.setIncludeStartDate(false);
            noteQueryParameters.setLimitResultSet(false);
            Date lastCheckDate = new Date(listParameter.getF_lastCheckDate() == null ? 0
                    : listParameter.getF_lastCheckDate());
            Date checkDate = lastCheckDate.after(navigationItemTO
                    .getLastAccessDate()) ? lastCheckDate : navigationItemTO.getLastAccessDate();
            noteQueryParameters.setLowerTagDate(checkDate);
            List<CountListItem> countListItems = queryManagement.query(countNoteQuery,
                    noteQueryParameters);

            ObservationResource resource = new ObservationResource();
            resource.setObservationId(navigationItemTO.getId());
            // doing a count so the paging is useless but we need the offset
            int offset = noteQueryParameters.getResultSpecification().getOffset();
            resource.setCount(countListItems.size() == 0 ? 0 : countListItems.get(0).getCount()
                    - offset);
            resource.setCheckDate(checkDate.getTime());
            result.add(resource);
        }
        return ResponseHelper.buildSuccessResponse(result, request);
    }
}
