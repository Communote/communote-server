package com.communote.server.web.fe.widgets.clouds;

import java.util.Locale;

import org.apache.commons.lang.StringUtils;

import com.communote.common.util.PageableList;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.tag.TagStoreType;
import com.communote.server.core.common.session.SessionHandler;
import com.communote.server.core.filter.listitems.NormalizedRankListItem;
import com.communote.server.core.filter.listitems.RankTagListItem;
import com.communote.server.core.query.QueryManagement;
import com.communote.server.core.tag.TagManagement;
import com.communote.server.core.tag.TagStoreManagement;
import com.communote.server.core.vo.query.config.FilterWidgetParameterNameProvider;
import com.communote.server.core.vo.query.config.QueryParametersParameterNameProvider;
import com.communote.server.core.vo.query.config.UserQueryParametersConfigurator;
import com.communote.server.core.vo.query.converters.RankTagListItemToRankTagListItemQueryResultConverter;
import com.communote.server.core.vo.query.tag.UserTagQuery;
import com.communote.server.core.vo.query.tag.UserTagQueryParameters;
import com.communote.server.web.fe.widgets.AbstractPagedListWidget;


/**
 * Widget for displaying a TagCloud of blog tags.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class UserTagCloudWidget extends
        AbstractPagedListWidget<NormalizedRankListItem<RankTagListItem>> {

    private static final String PARAMETER_SELECTED_TAGS = "selectedTags";

    /**
     * Parameter, which defines, that selected tags should be hidden or not (default is
     * <code>true</code> for hidden.
     */
    public static final String PARAMETER_HIDE_SELECTED_TAGS = "hideSelectedTags";

    /** */
    private final UserTagQuery queryDefinition = new UserTagQuery();

    /** */
    private final static QueryParametersParameterNameProvider NAME_PROVIDER = new FilterWidgetParameterNameProvider();
    private final static UserQueryParametersConfigurator CONFIGURATOR = new UserQueryParametersConfigurator(
            NAME_PROVIDER);

    private final QueryManagement queryManagement = ServiceLocator.instance().getService(
            QueryManagement.class);

    private final TagManagement tagManagement = ServiceLocator
            .instance().getService(TagManagement.class);

    /**
     * {@inheritDoc}
     * 
     * @return "core.widget.filter.user.tag.cloud"
     */
    @Override
    public String getTile(String type) {
        return "core.widget.filter.user.tag.cloud";
    }

    /**
     * Query the tag list on the Tagging Core API
     * 
     * @return list of RankedTag elements
     */
    @Override
    public PageableList<NormalizedRankListItem<RankTagListItem>> handleQueryList() {
        UserTagQueryParameters queryInstance = queryDefinition.createInstance();
        // queryInstanceConfigurator.configure(getParameters(), queryInstance);
        String tags = getParameter(NAME_PROVIDER.getNameForTagIds());
        if (StringUtils.isNotBlank(tags)) {
            setParameter(PARAMETER_SELECTED_TAGS, "," + tags + ",");
        }
        queryInstance.setHideSelectedTags(getBooleanParameter(PARAMETER_HIDE_SELECTED_TAGS, true));
        Locale locale = SessionHandler.instance()
                .getCurrentLocale(getRequest());
        CONFIGURATOR.configure(getParameters(), queryInstance, locale);
        queryInstance.setHideSelectedTags(true);

        PageableList<NormalizedRankListItem<RankTagListItem>> normalizedItems = queryManagement
                .query(queryDefinition, queryInstance,
                        new RankTagListItemToRankTagListItemQueryResultConverter(locale,
                                tagManagement));

        return normalizedItems;
    }

    /**
     * init the widget parameters to these values: maxCount = 0
     */
    @Override
    protected void initParameters() {
        setParameter(NAME_PROVIDER.getNameForMaxCount(), "0");
        setParameter(NAME_PROVIDER.getNameForCheckAtLeastMoreResults(), "0");
    }

    /**
     * @return See {@link TagStoreManagement#hasMoreThanOneTagStore(TagStoreType)}
     */
    public boolean moreThanOneTagStore() {
        return ServiceLocator.instance().getService(TagStoreManagement.class)
                .hasMoreThanOneTagStore(TagStoreType.Types.ENTITY);
    }
}
