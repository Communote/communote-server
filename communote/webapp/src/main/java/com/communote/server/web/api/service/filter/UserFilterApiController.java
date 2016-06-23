package com.communote.server.web.api.service.filter;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.communote.common.util.PageableList;
import com.communote.common.util.Pair;
import com.communote.server.core.vo.query.QueryDefinitionRepository;
import com.communote.server.core.vo.query.TimelineQueryParameters;
import com.communote.server.core.vo.query.user.v1_0_1.UserTaggingCoreQuery;
import com.communote.server.core.vo.query.user.v1_0_1.UserTaggingCoreQueryParameters;

/**
 * Api Controller to filter for tags
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * @deprecated Use new generated REST-API instead.
 */
@Deprecated
public class UserFilterApiController extends BaseFilterApiController {

    private static UserTaggingCoreQuery USER_QUERY_DEFINITION = QueryDefinitionRepository
            .instance().getQueryDefinition(UserTaggingCoreQuery.class);

    /**
     * {@inheritDoc}
     */
    @Override
    protected Pair<UserTaggingCoreQuery, TimelineQueryParameters> createQueryInstance(
            HttpServletRequest request) {
        return new Pair<UserTaggingCoreQuery, TimelineQueryParameters>(
                USER_QUERY_DEFINITION, new UserTaggingCoreQueryParameters(USER_QUERY_DEFINITION));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void postConfigureQueryInstance(TimelineQueryParameters queryInstance) {
        UserTaggingCoreQueryParameters userQueryInstance = (UserTaggingCoreQueryParameters) queryInstance;
        userQueryInstance.sortByLastNameAsc();
        userQueryInstance.sortByFirstNameAsc();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List postProcessList(HttpServletRequest request, PageableList list) {
        return new ArrayList(list);
    }

}
