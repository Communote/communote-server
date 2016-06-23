package com.communote.server.web.fe.widgets.user;

import com.communote.common.util.PageableList;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.user.UserData;
import com.communote.server.core.query.QueryManagement;
import com.communote.server.core.vo.query.QueryDefinitionRepository;
import com.communote.server.core.vo.query.config.FilterWidgetParameterNameProvider;
import com.communote.server.core.vo.query.config.TimelineQueryParametersConfigurator;
import com.communote.server.core.vo.query.user.UserTaggingCoreQuery;
import com.communote.server.core.vo.query.user.UserTaggingCoreQueryParameters;
import com.communote.server.model.note.NoteStatus;
import com.communote.server.web.fe.widgets.AbstractPagedListWidget;

/**
 * Widget for displaying a filterable List of users
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class AuthorFilterWidget extends AbstractPagedListWidget<UserData> {
    /** Supported Modes */
    public enum Modes {
        /** All. */
        ALL,
        /** Trend. */
        TREND;
        /**
         * @return The name of this mode in lower case.
         */
        @Override
        public String toString() {
            return super.toString().toLowerCase();
        };
    }

    private final static FilterWidgetParameterNameProvider NAME_PROVIDER = FilterWidgetParameterNameProvider.INSTANCE;

    private final static UserTaggingCoreQuery USER_QUERY = QueryDefinitionRepository
            .instance().getQueryDefinition(UserTaggingCoreQuery.class);

    /**
     * @param <Q>
     *            the type of the definition
     * @param definition
     *            the definition to use
     * @return the configured tagging instance
     */
    protected <Q extends UserTaggingCoreQuery> UserTaggingCoreQueryParameters configureQueryInstance(
            Q definition) {
        UserTaggingCoreQueryParameters userQueryInstance = definition.createInstance();
        userQueryInstance.setLimitResultSet(getParameter("mode", Modes.TREND.toString()).equals(
                Modes.TREND.toString()));
        TimelineQueryParametersConfigurator<UserTaggingCoreQueryParameters> queryInstanceConfigurator =
                new TimelineQueryParametersConfigurator<UserTaggingCoreQueryParameters>(
                        NAME_PROVIDER);
        queryInstanceConfigurator.configure(getParameters(), userQueryInstance);
        if (getBooleanParameter("ignoreUserIdsFilter", true)) {
            // ignore the userId filter because all users are shown and the filtered users are
            // selected
            userQueryInstance.setUserIds(null);
        }

        // exclude all unpublished notes
        userQueryInstance.setExcludeNoteStatus(new NoteStatus[] { NoteStatus.AUTOSAVED });

        return userQueryInstance;
    }

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
     * {@inheritDoc}
     */
    @Override
    public String getTile(String outputType) {
        return "core.widget.filter.author.pictures";
    }

    /**
     * Query the resource list on the tagging core API
     * 
     * @return list of RankedTag elements
     */
    @Override
    public PageableList<UserData> handleQueryList() {
        UserTaggingCoreQueryParameters userQueryInstance = configureQueryInstance(USER_QUERY);
        userQueryInstance.sortByLastNameAsc();
        userQueryInstance.sortByFirstNameAsc();
        userQueryInstance.sortByEmailAsc();
        PageableList<UserData> users = ServiceLocator.instance()
                .getService(QueryManagement.class).query(USER_QUERY, userQueryInstance);
        setPageInformation(userQueryInstance, users);
        return users;
    }

    /**
     * Initialize the widget parameters to these values: filter = '' offset = 0 maxCount = 30
     */
    @Override
    protected void initParameters() {
        super.initParameters();
    }
}
