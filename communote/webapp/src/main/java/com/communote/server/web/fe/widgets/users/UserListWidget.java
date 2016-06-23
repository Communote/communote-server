package com.communote.server.web.fe.widgets.users;

import javax.servlet.http.HttpServletRequest;

import com.communote.common.util.PageableList;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.user.DetailedUserData;
import com.communote.server.core.common.session.SessionHandler;
import com.communote.server.core.follow.FollowManagement;
import com.communote.server.core.query.QueryManagement;
import com.communote.server.core.vo.query.QueryDefinitionRepository;
import com.communote.server.core.vo.query.config.FilterWidgetParameterNameProvider;
import com.communote.server.core.vo.query.config.QueryParametersParameterNameProvider;
import com.communote.server.core.vo.query.config.UserQueryParametersConfigurator;
import com.communote.server.core.vo.query.converters.UserToUserDataQueryResultConverter;
import com.communote.server.core.vo.query.user.UserQuery;
import com.communote.server.core.vo.query.user.UserQueryParameters;
import com.communote.server.model.user.ImageSizeType;
import com.communote.server.model.user.User;
import com.communote.server.model.user.UserRole;
import com.communote.server.model.user.UserStatus;
import com.communote.server.web.fe.widgets.AbstractPagedListWidget;

/**
 * Controller for the the Widget to list users.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */

public class UserListWidget extends AbstractPagedListWidget<DetailedUserData> {
    /**
     * Converter for this widget.
     */
    private class ListUsersUserListItemQueryResultConverter extends
    UserToUserDataQueryResultConverter {
        /**
         * {@inheritDoc}
         */
        @Override
        public boolean convert(User source, DetailedUserData target) {
            boolean result = super.convert(source, target);
            if (result) {
                if (target instanceof ListUsersWidgetUserListItem) {
                    ListUsersWidgetUserListItem widgetTarget = (ListUsersWidgetUserListItem) target;
                    widgetTarget.setFollows(ServiceLocator.instance()
                            .getService(FollowManagement.class).followsUser(source.getId()));
                    widgetTarget.setCompany(source.getProfile().getCompany());
                    widgetTarget.setPosition(source.getProfile().getPosition());
                }
            }
            return result;
        }

        /**
         * @return new ListUsersWidgetUserListItem
         */
        @Override
        public DetailedUserData create() {
            return new ListUsersWidgetUserListItem();
        }
    }

    /**
     * UserData with special fields for this widget.
     */
    public class ListUsersWidgetUserListItem extends DetailedUserData {
        private static final long serialVersionUID = -1310176745801838449L;
        private boolean follows = false;
        private String company;
        private String position;

        /**
         * @return the company
         */
        public String getCompany() {
            return company;
        }

        /**
         * @return the position
         */
        public String getPosition() {
            return position;
        }

        /**
         * @return the follows
         */
        public boolean isFollows() {
            return follows;
        }

        /**
         * @param company
         *            the company to set
         */
        public void setCompany(String company) {
            this.company = company;
        }

        /**
         * @param follows
         *            the follows to set
         */
        public void setFollows(boolean follows) {
            this.follows = follows;
        }

        /**
         * @param position
         *            the position to set
         */
        public void setPosition(String position) {
            this.position = position;
        }
    }

    private final QueryParametersParameterNameProvider nameProvider = new FilterWidgetParameterNameProvider();

    private final static UserQuery USER_QUERY = QueryDefinitionRepository
            .instance().getQueryDefinition(UserQuery.class);

    private final UserQueryParametersConfigurator configurator = new UserQueryParametersConfigurator(
            nameProvider);

    /**
     * @param outputType
     *            This parameter is ignored.
     * @return "core.widget.users.list"
     */
    @Override
    public String getTile(String outputType) {
        return "core.widget.user.list";
    }

    /**
     * @return The list of users.
     */
    @Override
    protected PageableList<DetailedUserData> handleQueryList() {
        HttpServletRequest request = getRequest();
        UserQueryParameters queryParameters = USER_QUERY.createInstance();
        configurator.configure(getParameters(), queryParameters, SessionHandler.instance()
                .getCurrentLocale(request));
        queryParameters.setHideSelectedTags(false);
        queryParameters.setIncludeStatusFilter(new UserStatus[] { UserStatus.ACTIVE,
                UserStatus.TEMPORARILY_DISABLED, UserStatus.INVITED });
        queryParameters.addRoleToExclude(UserRole.ROLE_CRAWL_USER);
        PageableList<DetailedUserData> result = ServiceLocator
                .findService(QueryManagement.class)
                .query(USER_QUERY, queryParameters, new ListUsersUserListItemQueryResultConverter());
        request.setAttribute("pageInformation", queryParameters.getPageInformation(result, 5));
        request.setAttribute("imageSmall", ImageSizeType.SMALL);
        setResponseMetadata("nextOffset", result.getOffset() + result.size());
        return result;
    }
}
