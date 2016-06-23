package com.communote.server.web.api.service.user;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

import com.communote.common.util.PageableList;
import com.communote.common.util.ParameterHelper;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.user.UserData;
import com.communote.server.core.filter.ResultSpecification;
import com.communote.server.core.query.QueryManagement;
import com.communote.server.core.user.UserManagement;
import com.communote.server.core.vo.query.QueryDefinitionRepository;
import com.communote.server.core.vo.query.config.FilterApiParameterNameProvider;
import com.communote.server.core.vo.query.converters.UserToUserDataQueryResultConverter;
import com.communote.server.core.vo.query.user.UserQuery;
import com.communote.server.core.vo.query.user.UserQueryParameters;
import com.communote.server.model.user.User;
import com.communote.server.model.user.UserProfile;
import com.communote.server.model.user.UserStatus;
import com.communote.server.web.api.service.ApiException;
import com.communote.server.web.api.service.BaseRestApiController;
import com.communote.server.web.api.service.RequestedResourceNotFoundException;
import com.communote.server.web.api.to.ApiResult;

/**
 * Controller to handle user resource api requests
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * @deprecated Use new generated REST-API instead
 */
@Deprecated
public class UserApiController extends BaseRestApiController {

    private final static UserQuery USER_QUERY = QueryDefinitionRepository
            .instance().getQueryDefinition(UserQuery.class);

    private final static FilterApiParameterNameProvider NAME_PROVIDER = new FilterApiParameterNameProvider();

    /**
     * {@inheritDoc}
     */
    @Override
    protected Object doGet(ApiResult apiResult, HttpServletRequest request,
            HttpServletResponse response) throws ApiException {
        // throw resource not found exception one error
        Long userId;

        if (compareVersions(request, "1.0.1") >= 0) {
            userId = getResourceId(request, false);
        } else {
            userId = getResourceId(request, true);
        }

        if (userId == null) {
            return getUsers(request);
        } else {

            User user = getUserManagement().findUserByUserId(userId);
            if (user == null) {
                throw new RequestedResourceNotFoundException(getResourceType(), "" + userId,
                        "User not found");
            }
            UserProfile profile = user.getProfile();

            UserData userListItem;
            if (compareVersions(request, "1.0.1") >= 0) {
                com.communote.server.api.core.user.DetailedUserData uli;
                uli = new com.communote.server.api.core.user.DetailedUserData();
                uli.setLastModificationDate(profile.getLastModificationDate());
                uli.setLastPhotoModificationDate(profile.getLastPhotoModificationDate());
                userListItem = uli;
            } else {
                userListItem = new UserData();
            }

            userListItem.setAlias(user.getAlias());
            // dont expose email
            userListItem.setEmail(null);
            userListItem.setFirstName(profile.getFirstName());
            userListItem.setLastName(profile.getLastName());
            userListItem.setSalutation(profile.getSalutation());
            userListItem.setId(user.getId());
            userListItem.setStatus(user.getStatus());

            return userListItem;
        }
    }

    /**
     * @return the query management
     */
    private QueryManagement getQueryManagement() {
        return ServiceLocator.instance().getService(QueryManagement.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getResourceType() {
        return "user";
    }

    /**
     * @return the user management
     */
    private UserManagement getUserManagement() {
        return ServiceLocator.instance().getService(UserManagement.class);
    }

    /**
     * get the list of users as specified by the request parameters
     * 
     * @param request
     *            the http request
     * @return the list of users matching the request
     */
    private List<com.communote.server.api.core.user.DetailedUserData> getUsers(
            HttpServletRequest request) {
        String searchString = ParameterHelper.getParameterAsString(request.getParameterMap(),
                "searchString");
        Date lastModificationDate = ParameterHelper.getParameterAsDate(request.getParameterMap(),
                "lastModificationDate");
        int maxCount = ParameterHelper.getParameterAsInteger(request.getParameterMap(),
                NAME_PROVIDER.getNameForMaxCount(), 10);
        int offset = ParameterHelper.getParameterAsInteger(request.getParameterMap(),
                NAME_PROVIDER.getNameForOffset(), 0);
        boolean includeInvited = ParameterHelper.getParameterAsBoolean(request.getParameterMap(),
                "includeInvited", false);
        UserQueryParameters queryParameters = new UserQueryParameters();
        // last modification date
        if (lastModificationDate != null) {
            queryParameters.setLastModifiedAfter(new Timestamp(lastModificationDate.getTime()));
        }

        String[] userSearchFilter = StringUtils.split(searchString, " ");
        queryParameters.setUserSearchFilters(userSearchFilter);

        UserStatus[] statusFilter;
        if (includeInvited) {
            statusFilter = new UserStatus[] { UserStatus.ACTIVE,
                    UserStatus.INVITED };

        } else {
            statusFilter = new UserStatus[] { UserStatus.ACTIVE };
        }

        queryParameters.setIncludeStatusFilter(statusFilter);
        queryParameters.setResultSpecification(new ResultSpecification(offset, maxCount));
        queryParameters.sortByLastNameAsc();
        queryParameters.sortByFirstNameAsc();

        // execute
        PageableList<com.communote.server.api.core.user.DetailedUserData> list = getQueryManagement()
                .query(USER_QUERY, queryParameters,
                        new UserToUserDataQueryResultConverter());

        return list;
    }
}
