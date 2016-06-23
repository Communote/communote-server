package com.communote.server.web.fe.widgets.management.user;

import static com.communote.server.web.fe.widgets.WidgetConstants.PARAM_SEARCH_STRING;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.communote.common.util.PageableList;
import com.communote.common.util.ParameterHelper;
import com.communote.server.api.ServiceLocator;
import com.communote.server.core.filter.listitems.UserManagementListItem;
import com.communote.server.core.query.QueryManagement;
import com.communote.server.core.vo.query.QueryDefinitionRepository;
import com.communote.server.core.vo.query.user.AbstractUserQuery;
import com.communote.server.core.vo.query.user.UserManagementQuery;
import com.communote.server.core.vo.query.user.UserManagementQueryParameters;
import com.communote.server.model.user.UserRole;
import com.communote.server.model.user.UserStatus;
import com.communote.server.web.fe.widgets.AbstractPagedListWidget;

/**
 * The Class UserManagementListWidget implements a user list with management functions.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class UserManagementListWidget extends AbstractPagedListWidget<UserManagementListItem> {

    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(UserManagementListWidget.class);
    /**
     * The Constant PARAM_USER_STATUS_FILTER defines the parameter for the user status filter.
     */
    public final static String PARAM_USER_STATUS_FILTER = "userStatusFilter";

    /**
     * The Constant PARAM_USER_ROLE_FILTER defines the parameter for the user role filter.
     */
    public final static String PARAM_USER_ROLE_FILTER = "userRoleFilter";

    /** The query definition. */
    private final static UserManagementQuery QUERY = QueryDefinitionRepository.instance()
            .getQueryDefinition(UserManagementQuery.class);

    /**
     * returns 'user'
     *
     * @return the message key suffix
     * @see com.communote.server.web.fe.widgets.PagedWidget#getDefaultPagingMessageKeySuffix()
     */
    @Override
    public String getDefaultPagingMessageKeySuffix() {
        return "user";
    }

    /**
     * Gets the status filter list. The list will contain all selected user statuses except DELETED.
     *
     * @return the status filter list
     */
    private UserStatus[] getStatusFilterList() {
        String userStatusFilter = getParameter(PARAM_USER_STATUS_FILTER);
        List<UserStatus> result = new LinkedList<UserStatus>();
        if (userStatusFilter != null
                && !userStatusFilter.equals(AbstractUserQuery.FILTER_STATUS_ALL)) {
            String[] list = StringUtils.split(userStatusFilter, ",");
            for (String s : list) {
                try {
                    UserStatus status = UserStatus.fromString(s);
                    if (status != null && !status.equals(UserStatus.DELETED)) {
                        result.add(status);
                    }
                } catch (IllegalArgumentException e) {
                    LOGGER.error("Invalid user status in parameter: '{}'", s);
                }
            }
        }
        return result.toArray(new UserStatus[result.size()]);
    }

    @Override
    public String getTile(String outputType) {
        return "widget.client.management.user.list.html";
    }

    /**
     * {@inheritDoc}
     *
     * @see com.communote.server.web.fe.widgets.AbstractPagedListWidget#handleQueryList()
     */
    @Override
    protected PageableList<UserManagementListItem> handleQueryList() {

        UserManagementQueryParameters queryParameters = QUERY.createInstance();

        String searchString = getParameter(PARAM_SEARCH_STRING);
        String[] userSearchString = StringUtils.isEmpty(searchString) ? null : searchString
                .split(" ");
        queryParameters.setUserSearchFilters(userSearchString, true);

        String[] roleValues = ParameterHelper.getParameterAsStringArray(getParameters(),
                PARAM_USER_ROLE_FILTER, ",");
        if (roleValues != null) {
            for (int i = 0; i < roleValues.length; i++) {
                try {
                    UserRole role = UserRole.fromString(roleValues[i]);
                    queryParameters.addRoleToInclude(role);
                } catch (IllegalArgumentException e) {
                    LOGGER.error("Invalid role value {}", roleValues[i]);
                }
            }
        }

        queryParameters.setIncludeStatusFilter(getStatusFilterList());
        // always exclude deleted users
        queryParameters.setExcludeStatusFilter(new UserStatus[] { UserStatus.DELETED });
        queryParameters.setResultSpecification(getResultSpecification());

        queryParameters.sortByLastNameAsc();
        queryParameters.sortByFirstNameAsc();
        queryParameters.sortByEmailAsc();

        PageableList<UserManagementListItem> list = ServiceLocator.findService(
                QueryManagement.class).executeQuery(QUERY, queryParameters);
        setPageInformation(queryParameters, list);
        return list;
    }

    /**
     * {@inheritDoc}
     *
     * @see com.communote.server.web.fe.widgets.AbstractPagedListWidget#initParameters()
     */
    @Override
    protected void initParameters() {
        super.initParameters();
        setParameter(PARAM_SEARCH_STRING, StringUtils.EMPTY);
        setParameter(PARAM_USER_STATUS_FILTER, AbstractUserQuery.FILTER_STATUS_ALL);
    }
}
