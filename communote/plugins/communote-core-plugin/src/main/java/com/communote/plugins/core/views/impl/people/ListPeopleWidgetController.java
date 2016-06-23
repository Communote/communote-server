package com.communote.plugins.core.views.impl.people;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.hibernate.criterion.MatchMode;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

import com.communote.common.util.PageableList;
import com.communote.common.util.ParameterHelper;
import com.communote.plugins.core.views.annotations.UrlMapping;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.user.DetailedUserData;
import com.communote.server.core.filter.ResultSpecification;
import com.communote.server.core.query.QueryManagement;
import com.communote.server.core.user.UserProfileManagement;
import com.communote.server.core.vo.query.QueryDefinitionRepository;
import com.communote.server.core.vo.query.config.FilterApiParameterNameProvider;
import com.communote.server.core.vo.query.config.QueryParametersParameterNameProvider;
import com.communote.server.core.vo.query.converters.UserToUserDataQueryResultConverter;
import com.communote.server.core.vo.query.user.UserQuery;
import com.communote.server.core.vo.query.user.UserQueryParameters;
import com.communote.server.model.user.ImageSizeType;
import com.communote.server.model.user.UserProfile;
import com.communote.server.model.user.UserStatus;

/**
 * Controller for the the Widget to list workspaces.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */

@Component
@Provides
@Instantiate(name = "ListPeopleWidgetController")
@UrlMapping("/*/widgets/people/ListPeople.widget")
public class ListPeopleWidgetController implements Controller {

    private final static UserQuery USER_QUERY = QueryDefinitionRepository
            .instance().getQueryDefinition(UserQuery.class);

    private final QueryParametersParameterNameProvider nameProvider = new FilterApiParameterNameProvider();

    /**
     * Gets the blogs the user can read.
     *
     * @param request
     *            The request.
     * @param response
     *            The response.
     * @param model
     *            The model.
     * @return List of blogs.
     */
    private PageableList<DetailedUserData> getUsers(HttpServletRequest request,
            HttpServletResponse response, Map<String, Object> model) {
        int offset = ParameterHelper.getParameterAsInteger(request.getParameterMap(), nameProvider
                .getNameForOffset(), 0);
        int maxCount = "list".equals(request.getParameter("listType")) ? 32 : 20;
        ResultSpecification resultSpecification = new ResultSpecification(offset, maxCount);
        UserQueryParameters queryParameters = USER_QUERY.createInstance();

        String searchString = request.getParameter(nameProvider.getNameForFullTextSearchString());
        if (StringUtils.isNotEmpty(searchString)) {
            queryParameters.setMatchMode(MatchMode.ANYWHERE);
            queryParameters.setUserSearchFilters(searchString.split(" "));
        }
        queryParameters.setIncludeStatusFilter(new UserStatus[] { UserStatus.ACTIVE });
        queryParameters.setRetrieveOnlyFollowedUsers(request.getParameter("viewType") != null
                && "follow".equals(request.getParameter("viewType")));
        queryParameters.sortByLastNameAsc();
        queryParameters.sortByFirstNameAsc();
        queryParameters.sortByEmailAsc();
        queryParameters.setResultSpecification(resultSpecification);
        PageableList<DetailedUserData> result = ServiceLocator
                .instance().getService(QueryManagement.class)
                .query(USER_QUERY, queryParameters,
                        new UserToUserDataQueryResultConverter());
        model.put("pageInformation", queryParameters.getPageInformation(result, 5));
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        Map<String, Object> model = new HashMap<String, Object>();
        PageableList<DetailedUserData> users = getUsers(request, response, model);
        Object[][] templateUsers = new Object[users.size()][9];
        for (int i = 0; i < users.size(); i++) {
            DetailedUserData user = users.get(i);
            templateUsers[i][0] = user.getId();
            templateUsers[i][1] = user.getAlias();
            templateUsers[i][2] = user.getEmail();
            templateUsers[i][3] = user.getFirstName();
            templateUsers[i][4] = user.getLastName();
            templateUsers[i][5] = user.getSalutation();

            UserProfile profile = ServiceLocator.findService(UserProfileManagement.class)
                    .findUserProfileByUserId(user.getId());

            templateUsers[i][6] = StringUtils.isBlank(profile.getCompany()) ? "&nbsp;" : profile
                    .getCompany();
            if (profile.getContact() != null) {
                templateUsers[i][7] = profile.getContact().getPhone();
                templateUsers[i][8] = StringUtils.isBlank(profile.getContact().getCity()) ? "&nbsp;"
                        : profile.getContact().getCity();
            } else {
                templateUsers[i][8] = "&nbsp;";
            }
        }
        model.put("users", templateUsers);
        model.put("widgetId", request.getParameter("widgetId"));
        String listType = request.getParameter("listType") == null ? "grid" : request
                .getParameter("listType");
        model.put("listType", listType);
        model.put("imageMedium", ImageSizeType.MEDIUM);
        return new ModelAndView("widget.people.list", model);
    }
}
